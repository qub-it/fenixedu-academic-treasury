package org.fenixedu.academictreasury.services;


import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequestSituation;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequestSituationType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.emoluments.ServiceRequestMapEntry;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.domain.tariff.AcademicTariff;
import org.fenixedu.academictreasury.dto.academictax.AcademicDebitEntryBean;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.dto.document.managepayments.PaymentReferenceCodeBean;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import pt.ist.fenixframework.Atomic;

public class EmolumentServices {

    public static Stream<Product> findEmoluments(final FinantialEntity finantialEntity) {
        if (AcademicTreasurySettings.getInstance().getEmolumentsProductGroup() == null) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.emoluments.product.group.not.defined");
        }

        return AcademicTreasurySettings.getInstance().getEmolumentsProductGroup().getProductsSet().stream()
                .filter(l -> l.getFinantialInstitutionsSet().contains(finantialEntity.getFinantialInstitution()));
    }

    @Subscribe
    public void newAcademicServiceRequestSituationEvent(final DomainObjectEvent<AcademicServiceRequest> event) {
        newAcademicServiceRequestSituationEvent(event.getInstance());
    }

    public boolean newAcademicServiceRequestSituationEvent(final AcademicServiceRequest academicServiceRequest) {
        // ITreasuryServiceRequest have always a registration which has a degree
        if (!(academicServiceRequest instanceof ITreasuryServiceRequest)) {
            return false;
        } ;

        ITreasuryServiceRequest iTreasuryServiceRequest = (ITreasuryServiceRequest) academicServiceRequest;

        if (!iTreasuryServiceRequest.getServiceRequestType().isPayable()) {
            return false;
        }

        // Find configured map entry for service request type
        final ServiceRequestMapEntry serviceRequestMapEntry = ServiceRequestMapEntry.findMatch(iTreasuryServiceRequest);

        if (serviceRequestMapEntry == null) {
            return false;
        }

        // Check if the academicServiceRequest is ready to be charged
        if (!academicServiceRequest.getAcademicServiceRequestSituationsSet().stream()
                .map(AcademicServiceRequestSituation::getAcademicServiceRequestSituationType).collect(Collectors.toSet())
                .contains(serviceRequestMapEntry.getCreateEventOnSituation())) {

            // It is not ready
            return false;
        }

        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();
        final LocalDate when = possibleDebtDateOnAcademicService(iTreasuryServiceRequest);
        final FinantialEntity finantialEntity = academicTreasuryServices.finantialEntityOfDegree(iTreasuryServiceRequest.getRegistration().getDegree(), 
                when);
        
        return createAcademicServiceRequestEmolument(finantialEntity, iTreasuryServiceRequest, when);
    }

    public static AcademicTreasuryEvent findAcademicTreasuryEvent(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        return AcademicTreasuryEvent.findUnique(iTreasuryServiceRequest).orElse(null);
    }

    public static AcademicTariff findTariffForAcademicServiceRequestForDefaultFinantialEntity(
            final ITreasuryServiceRequest iTreasuryServiceRequest,
            final LocalDate when) {
        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();
        
        final FinantialEntity finantialEntity = academicTreasuryServices.finantialEntityOfDegree(iTreasuryServiceRequest.getRegistration().getDegree(), when);
        return findTariffForAcademicServiceRequest(finantialEntity, iTreasuryServiceRequest, when);
    }
    
    public static AcademicTariff findTariffForAcademicServiceRequest(
            final FinantialEntity finantialEntity,
            final ITreasuryServiceRequest iTreasuryServiceRequest,
            final LocalDate when) {
        final Degree degree = iTreasuryServiceRequest.getRegistration().getDegree();
        final CycleType cycleType = iTreasuryServiceRequest.getCycleType();
        final Product product = ServiceRequestMapEntry.findProduct(iTreasuryServiceRequest);

        if(iTreasuryServiceRequest.hasCycleType()) {
            AcademicTariff.findMatch(finantialEntity, product, degree, cycleType, when.toDateTimeAtStartOfDay());
        }
        
        return AcademicTariff.findMatch(finantialEntity, product, degree, when.toDateTimeAtStartOfDay());
    }
    
    public static AcademicDebitEntryBean calculateForAcademicServiceRequestForDefaultFinantialEntity(final ITreasuryServiceRequest iTreasuryServiceRequest, final LocalDate debtDate) {
        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();
        
        final FinantialEntity finantialEntity = academicTreasuryServices.finantialEntityOfDegree(iTreasuryServiceRequest.getRegistration().getDegree(), debtDate);
        
        return calculateForAcademicServiceRequest(finantialEntity, iTreasuryServiceRequest, debtDate);
    }

    public static AcademicDebitEntryBean calculateForAcademicServiceRequest(
            final FinantialEntity finantialEntity, final ITreasuryServiceRequest iTreasuryServiceRequest, final LocalDate debtDate) {
        if (!iTreasuryServiceRequest.getServiceRequestType().isPayable()) {
            return null;
        }

        // Find configured map entry for service request type
        final ServiceRequestMapEntry serviceRequestMapEntry = ServiceRequestMapEntry.findMatch(iTreasuryServiceRequest);

        if (serviceRequestMapEntry == null) {
            return null;
        }

        // Read person customer

        final Person person = iTreasuryServiceRequest.getPerson();
        final String addressFiscalCountryCode = PersonCustomer.addressCountryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        if (Strings.isNullOrEmpty(addressFiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        if (!PersonCustomer.findUnique(person, addressFiscalCountryCode, fiscalNumber).isPresent()) {
            PersonCustomer.create(person, addressFiscalCountryCode, fiscalNumber);
        }

        final PersonCustomer personCustomer = PersonCustomer.findUnique(person, addressFiscalCountryCode, fiscalNumber).get();
        if (!personCustomer.isActive()) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.not.active", addressFiscalCountryCode, fiscalNumber);
        }

        // Find tariff

        final AcademicTariff academicTariff = findTariffForAcademicServiceRequest(finantialEntity, iTreasuryServiceRequest, debtDate);

        if (academicTariff == null) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.tariff.not.found",
                    debtDate.toString(TreasuryConstants.DATE_FORMAT));
        }

        final LocalizedString debitEntryName = academicTariff.academicServiceRequestDebitEntryName(iTreasuryServiceRequest);
        final LocalDate dueDate = academicTariff.dueDate(debtDate);
        final Vat vat = academicTariff.vat(debtDate);
        
        final int numberOfUnits = AcademicTreasuryEvent.numberOfUnits(iTreasuryServiceRequest);
        final int numberOfPages = AcademicTreasuryEvent.numberOfPages(iTreasuryServiceRequest); 
        final Locale language = AcademicTreasuryEvent.language(iTreasuryServiceRequest);
        final boolean urgentRequest = AcademicTreasuryEvent.urgentRequest(iTreasuryServiceRequest);
        
        final BigDecimal amount = academicTariff.amountToPay(numberOfUnits, numberOfPages, language, urgentRequest);

        return new AcademicDebitEntryBean(debitEntryName, dueDate, vat.getTaxRate(), amount);
    }

    @Atomic
    public static boolean createAcademicServiceRequestEmolumentForDefaultFinantialEntity(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        final LocalDate when = possibleDebtDateOnAcademicService(iTreasuryServiceRequest);
        
        return createAcademicServiceRequestEmolumentForDefaultFinantialEntity(iTreasuryServiceRequest, when);
    }

    @Atomic
    public static boolean createAcademicServiceRequestEmolumentForDefaultFinantialEntity(final ITreasuryServiceRequest iTreasuryServiceRequest,
            final LocalDate when) {
        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();
        
        final FinantialEntity finantialEntity = academicTreasuryServices.finantialEntityOfDegree(iTreasuryServiceRequest.getRegistration().getDegree(), when);
        
        return createAcademicServiceRequestEmolument(finantialEntity, iTreasuryServiceRequest, when);
    }
    
    @Atomic
    public static boolean createAcademicServiceRequestEmolument(final FinantialEntity finantialEntity, final ITreasuryServiceRequest iTreasuryServiceRequest) {
        final LocalDate when = possibleDebtDateOnAcademicService(iTreasuryServiceRequest);

        return createAcademicServiceRequestEmolument(finantialEntity, iTreasuryServiceRequest, when);
    }

    @Atomic
    public static boolean createAcademicServiceRequestEmolument(final FinantialEntity finantialEntity, final ITreasuryServiceRequest iTreasuryServiceRequest,
            final LocalDate when) {

        if (!iTreasuryServiceRequest.getServiceRequestType().isPayable()) {
            return false;
        }

        // Find configured map entry for service request type
        final ServiceRequestMapEntry serviceRequestMapEntry = ServiceRequestMapEntry.findMatch(iTreasuryServiceRequest);

        if (serviceRequestMapEntry == null) {
            return false;
        }

        // Read person customer

        final Person person = iTreasuryServiceRequest.getPerson();
        final String addressFiscalCountryCode = PersonCustomer.addressCountryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        if (Strings.isNullOrEmpty(addressFiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        if (!PersonCustomer.findUnique(person, addressFiscalCountryCode, fiscalNumber).isPresent()) {
            PersonCustomer.create(person, addressFiscalCountryCode, fiscalNumber);
        }

        final PersonCustomer personCustomer = PersonCustomer.findUnique(person, addressFiscalCountryCode, fiscalNumber).get();
        if (!personCustomer.isActive()) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.not.active");
        }

        // Find tariff

        final AcademicTariff academicTariff = findTariffForAcademicServiceRequest(finantialEntity, iTreasuryServiceRequest, when);

        if (academicTariff == null) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.tariff.not.found",
                    when.toString(TreasuryConstants.DATE_FORMAT));
        }

        final FinantialInstitution finantialInstitution = finantialEntity.getFinantialInstitution();

        if (!DebtAccount.findUnique(finantialInstitution, personCustomer).isPresent()) {
            DebtAccount.create(finantialInstitution, personCustomer);
        }

        // Find or create event if does not exists
        if (findAcademicTreasuryEvent(iTreasuryServiceRequest) == null) {
            AcademicTreasuryEvent.createForAcademicServiceRequest(iTreasuryServiceRequest);
        }

        final AcademicTreasuryEvent academicTresuryEvent = findAcademicTreasuryEvent(iTreasuryServiceRequest);

        if (academicTresuryEvent.isChargedWithDebitEntry()) {
            //Old amount has the amount plus the credit amount plus the direct exempted amount
            BigDecimal oldtotalAmount = academicTresuryEvent.getAmountToPay().add(academicTresuryEvent.getCreditAmount())
                    .add(DebitEntry.findActive(academicTresuryEvent).map(l -> l.getExemptedAmount()).reduce((a, b) -> a.add(b))
                            .orElse(BigDecimal.ZERO));
            BigDecimal newTotalAmount = academicTariff.amountToPay(academicTresuryEvent);
            //Do nothing, since the value is the same
            if (TreasuryConstants.isEqual(oldtotalAmount, newTotalAmount)) {
                return false;
            }
            //Annul debit entries, since new ones will be created to reflect the service request's changes
            String reason = academicTreasuryBundle("info.EmolumentServices.serviceRequest.change.value.anull.debit.entries");
            academicTresuryEvent.annulAllDebitEntries(reason);
        }

        final DebtAccount personDebtAccount = DebtAccount.findUnique(finantialInstitution, personCustomer).orElse(null);
        final DebitEntry debitEntry =
                academicTariff.createDebitEntryForAcademicServiceRequest(personDebtAccount, academicTresuryEvent);

        if (debitEntry == null) {
            return false;
        }

        if (TreasuryConstants.isEqual(debitEntry.getOpenAmount(), BigDecimal.ZERO)) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.academicServiceRequest.amount.equals.to.zero");
        }

        final DebitNote debitNote = DebitNote.create(personDebtAccount, DocumentNumberSeries
                .findUniqueDefault(FinantialDocumentType.findForDebitNote(), personDebtAccount.getFinantialInstitution()).get(),
                new DateTime());

        debitNote.addDebitNoteEntries(Collections.singletonList(debitEntry));

        if (AcademicTreasurySettings.getInstance().isCloseServiceRequestEmolumentsWithDebitNote()) {
            debitNote.closeDocument();
        }

        if (serviceRequestMapEntry.getGeneratePaymentCode()) {
            PaymentCodePool pool = serviceRequestMapEntry.getPaymentCodePool();
            if (pool == null) {
                throw new AcademicTreasuryDomainException(
                        "error.EmolumentServices.academicServiceRequest.paymentCodePool.is.required");
            }

            final LocalDate dueDate = academicTresuryEvent.getDueDate();
            final LocalDate now = new LocalDate();

            final PaymentReferenceCodeBean referenceCodeBean = new PaymentReferenceCodeBean(pool, debitEntry.getDebtAccount());
            referenceCodeBean.setBeginDate(now);
            referenceCodeBean.setEndDate(dueDate.compareTo(now) > 0 ? dueDate : now);
            referenceCodeBean.setSelectedDebitEntries(new ArrayList<DebitEntry>());
            referenceCodeBean.getSelectedDebitEntries().add(debitEntry);

            final PaymentReferenceCode referenceCode =
                    PaymentReferenceCode.createPaymentReferenceCodeForMultipleDebitEntries(debitEntry.getDebtAccount(), referenceCodeBean);
        }

        return true;
    }

    public static LocalDate possibleDebtDateOnAcademicService(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        // Find the configured state to create debt on academic service request

        if (!iTreasuryServiceRequest.getServiceRequestType().isPayable()) {
            return null;
        }

        // Find configured map entry for service request type
        final ServiceRequestMapEntry serviceRequestMapEntry = ServiceRequestMapEntry.findMatch(iTreasuryServiceRequest);

        if (serviceRequestMapEntry == null) {
            return null;
        }

        AcademicServiceRequestSituationType createEventOnSituation = serviceRequestMapEntry.getCreateEventOnSituation();

        if (iTreasuryServiceRequest.getSituationByType(createEventOnSituation) == null) {
            return iTreasuryServiceRequest.getRequestDate().toLocalDate();
        }

        return iTreasuryServiceRequest.getSituationByType(createEventOnSituation).getSituationDate().toLocalDate();
    }

    public static boolean removeDebitEntryForAcademicService(final ITreasuryServiceRequest iTreasuryServiceRequest) {

        if (findAcademicTreasuryEvent(iTreasuryServiceRequest) == null) {
            return false;
        }

        final AcademicTreasuryEvent academicTreasuryEvent = findAcademicTreasuryEvent(iTreasuryServiceRequest);

        if (!academicTreasuryEvent.isChargedWithDebitEntry()) {
            return false;
        }

        final DebitEntry debitEntry = academicTreasuryEvent.findActiveAcademicServiceRequestDebitEntry().get();

        final DebitNote debitNote = (DebitNote) debitEntry.getFinantialDocument();
        if (!debitEntry.isProcessedInDebitNote()) {
            debitEntry
                    .annulDebitEntry(academicTreasuryBundle("label.EmolumentServices.removeDebitEntryForAcademicService.reason"));
            debitEntry.delete();

            return true;
        } else if (debitEntry.getCreditEntriesSet().isEmpty()) {
            debitNote.anullDebitNoteWithCreditNote(
                    academicTreasuryBundle("label.EmolumentServices.removeDebitEntryForAcademicService.reason"), false);

            return true;
        }

        return false;
    }


    /* Custom Academic Debt */
    
    public static AcademicDebitEntryBean calculateForCustomAcademicDebt(final FinantialEntity finantialEntity,
            final Product product, final Registration registration, final ExecutionYear executionYear, 
            final int numberOfUnits, final int numberOfPages, boolean urgentRequest,
            final LocalDate debtDate) {
        // Find tariff

        final AcademicTariff academicTariff =
                AcademicTariff.findMatch(finantialEntity, product, registration.getDegree(), debtDate.toDateTimeAtStartOfDay());

        if (academicTariff == null) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.tariff.not.found",
                    debtDate.toString(TreasuryConstants.DATE_FORMAT));
        }

        final LocalizedString debitEntryName = AcademicTreasuryEvent.nameForCustomAcademicDebt(product, registration, executionYear);
        final LocalDate dueDate = academicTariff.dueDate(debtDate);
        final Vat vat = academicTariff.vat(debtDate);
        final BigDecimal amount = academicTariff.amountToPay(numberOfUnits, numberOfPages, null, urgentRequest);

        return new AcademicDebitEntryBean(debitEntryName, dueDate, vat.getTaxRate(), amount);
    }
    
    public static boolean createCustomAcademicDebt(final FinantialEntity finantialEntity, final Product product, 
            final Registration registration, final ExecutionYear executionYear, final int numberOfUnits, 
            final int numberOfPages, boolean urgentRequest, final LocalDate when) {
        
        final Person person = registration.getPerson();
        final String addressFiscalCountryCode = PersonCustomer.addressCountryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        
        // Read person customer
        if (!PersonCustomer.findUnique(person, addressFiscalCountryCode, fiscalNumber).isPresent()) {
            PersonCustomer.create(person, addressFiscalCountryCode, fiscalNumber);
        }

        final PersonCustomer personCustomer = PersonCustomer.findUnique(person, addressFiscalCountryCode, fiscalNumber).get();
        if (!personCustomer.isActive()) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.not.active", addressFiscalCountryCode, fiscalNumber);
        }

        final DebtAccount debtAccount =
                DebtAccount.findUnique(finantialEntity.getFinantialInstitution(), personCustomer).get();

        final AcademicTreasuryEvent academicTreasuryEvent = AcademicTreasuryEvent.createForCustomAcademicDebt(product, registration, executionYear, numberOfUnits, numberOfPages, urgentRequest, when);
        
        final AcademicTariff academicTariff =
                AcademicTariff.findMatch(finantialEntity, product, registration.getDegree(), when.toDateTimeAtStartOfDay());
        
        if (academicTariff == null) {
            return false;
        }

        academicTariff.createDebitEntryForCustomAcademicDebt(debtAccount, academicTreasuryEvent, when);
        
        return true;
    }
    
}
