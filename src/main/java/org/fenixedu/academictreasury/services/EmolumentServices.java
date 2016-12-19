package org.fenixedu.academictreasury.services;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequestSituation;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequestSituationType;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.emoluments.ServiceRequestMapEntry;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.domain.tariff.AcademicTariff;
import org.fenixedu.academictreasury.dto.academicservicerequest.AcademicServiceRequestDebitEntryBean;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.VatType;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import pt.ist.fenixframework.Atomic;

public class EmolumentServices {

    @Atomic
    public static Product createEmolument(final String code, final LocalizedString name, final VatType vatType,
            FinantialInstitution finantialInstitution) {
        if (AcademicTreasurySettings.getInstance().getEmolumentsProductGroup() == null) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.emoluments.product.group.not.defined");
        }

        return Product.create(AcademicTreasurySettings.getInstance().getEmolumentsProductGroup(), code, name,
                Product.defaultUnitOfMeasure(), true, false, 0, vatType, Collections.singletonList(finantialInstitution), null);
    }

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
        };

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

        return createAcademicServiceRequestEmolument(iTreasuryServiceRequest);
    }

    public static AcademicTreasuryEvent findAcademicTreasuryEvent(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        return AcademicTreasuryEvent.findUnique(iTreasuryServiceRequest).orElse(null);
    }

    public static AcademicTariff findTariffForAcademicServiceRequest(final ITreasuryServiceRequest iTreasuryServiceRequest,
            final LocalDate when) {
        final Degree degree = iTreasuryServiceRequest.getRegistration().getDegree();
        final CycleType cycleType = iTreasuryServiceRequest.getCycleType();
        final Product product = ServiceRequestMapEntry.findProduct(iTreasuryServiceRequest);

        return iTreasuryServiceRequest.hasCycleType() ? AcademicTariff.findMatch(product, degree, cycleType,
                when.toDateTimeAtStartOfDay()) : AcademicTariff.findMatch(product, degree, when.toDateTimeAtStartOfDay());
    }

    @Atomic
    public static AcademicServiceRequestDebitEntryBean calculateForAcademicServiceRequest(
            final ITreasuryServiceRequest iTreasuryServiceRequest, final LocalDate debtDate) {
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
        final String fiscalCountryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        if (!PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber).isPresent()) {
            PersonCustomer.create(person, fiscalCountryCode, fiscalNumber);
        }

        final PersonCustomer personCustomer = PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber).get();
        if (!personCustomer.isActive()) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.not.active", fiscalCountryCode, fiscalNumber);
        }

        // Find tariff

        final AcademicTariff academicTariff = findTariffForAcademicServiceRequest(iTreasuryServiceRequest, debtDate);

        if (academicTariff == null) {
            return null;
        }

        final FinantialEntity finantialEntity = academicTariff.getFinantialEntity();
        final FinantialInstitution finantialInstitution = finantialEntity.getFinantialInstitution();

        if (!DebtAccount.findUnique(finantialInstitution, personCustomer).isPresent()) {
            DebtAccount.create(finantialInstitution, personCustomer);
        }

        // Find or create event if does not exists
        if (findAcademicTreasuryEvent(iTreasuryServiceRequest) == null) {
            AcademicTreasuryEvent.createForAcademicServiceRequest(iTreasuryServiceRequest);
        }

        final AcademicTreasuryEvent academicTreasuryEvent = findAcademicTreasuryEvent(iTreasuryServiceRequest);

        final LocalizedString debitEntryName = academicTariff.academicServiceRequestDebitEntryName(academicTreasuryEvent);
        final LocalDate dueDate = academicTariff.dueDate(debtDate);
        final Vat vat = academicTariff.vat(debtDate);
        final BigDecimal amount = academicTariff.amountToPay(academicTreasuryEvent);

        return new AcademicServiceRequestDebitEntryBean(debitEntryName, dueDate, vat.getTaxRate(), amount);
    }

    @Atomic
    public static boolean createAcademicServiceRequestEmolument(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        final LocalDate when = possibleDebtDateOnAcademicService(iTreasuryServiceRequest);

        return createAcademicServiceRequestEmolument(iTreasuryServiceRequest, when);
    }

    @Atomic
    public static boolean createAcademicServiceRequestEmolument(final ITreasuryServiceRequest iTreasuryServiceRequest,
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
        final String fiscalCountryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        if (!PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber).isPresent()) {
            PersonCustomer.create(person, fiscalCountryCode, fiscalNumber);
        }

        final PersonCustomer personCustomer = PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber).get();
        if(!personCustomer.isActive()) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.not.active");
        }
        
        // Find tariff

        final AcademicTariff academicTariff = findTariffForAcademicServiceRequest(iTreasuryServiceRequest, when);

        if (academicTariff == null) {
            return false;
        }

        final FinantialEntity finantialEntity = academicTariff.getFinantialEntity();
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
            return false;
        }

        final DebtAccount personDebtAccount = DebtAccount.findUnique(finantialInstitution, personCustomer).orElse(null);
        final DebitEntry debitEntry = academicTariff.createDebitEntryForAcademicServiceRequest(personDebtAccount, academicTresuryEvent);

        if (debitEntry == null) {
            return false;
        }

        if (Constants.isEqual(debitEntry.getOpenAmount(), BigDecimal.ZERO)) {
            throw new AcademicTreasuryDomainException("error.EmolumentServices.academicServiceRequest.amount.equals.to.zero");
        }

        final DebitNote debitNote = DebitNote.create(personDebtAccount,
                DocumentNumberSeries
                        .findUniqueDefault(FinantialDocumentType.findForDebitNote(), personDebtAccount.getFinantialInstitution())
                        .get(),
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
            PaymentReferenceCode referenceCode = pool.getReferenceCodeGenerator().generateNewCodeFor(
                    academicTresuryEvent.getRemainingAmountToPay(), now, dueDate.compareTo(now) > 0 ? dueDate : now, true);

            referenceCode.createPaymentTargetTo(Sets.newHashSet(debitEntry), debitEntry.getOpenAmount());
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
            debitEntry.annulDebitEntry(org.fenixedu.academictreasury.util.Constants
                    .bundle("label.EmolumentServices.removeDebitEntryForAcademicService.reason"));
            debitEntry.delete();

            return true;
        } else if (debitEntry.getCreditEntriesSet().isEmpty()) {
            debitNote.anullDebitNoteWithCreditNote(org.fenixedu.academictreasury.util.Constants
                    .bundle("label.EmolumentServices.removeDebitEntryForAcademicService.reason"), false);

            return true;
        }

        return false;
    }
}
