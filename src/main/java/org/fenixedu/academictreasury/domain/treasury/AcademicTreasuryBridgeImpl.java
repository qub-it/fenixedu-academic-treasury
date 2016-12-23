package org.fenixedu.academictreasury.domain.treasury;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.administrativeOffice.AdministrativeOffice;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.treasury.IAcademicServiceRequestAndAcademicTaxTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryTarget;
import org.fenixedu.academic.domain.treasury.IImprovementTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IPaymentCodePool;
import org.fenixedu.academic.domain.treasury.ITreasuryBridgeAPI;
import org.fenixedu.academic.domain.treasury.ITreasuryEntity;
import org.fenixedu.academic.domain.treasury.ITreasuryProduct;
import org.fenixedu.academic.domain.treasury.ITuitionTreasuryEvent;
import org.fenixedu.academictreasury.domain.academicalAct.AcademicActBlockingSuspension;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.tariff.AcademicTariff;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.services.EmolumentServices;
import org.fenixedu.academictreasury.services.PersonServices;
import org.fenixedu.academictreasury.services.RegistrationServices;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.academictreasury.services.signals.AcademicServiceRequestCancelOrRejectHandler;
import org.fenixedu.academictreasury.services.signals.ExtracurricularEnrolmentHandler;
import org.fenixedu.academictreasury.services.signals.ImprovementEnrolmentHandler;
import org.fenixedu.academictreasury.services.signals.NormalEnrolmentHandler;
import org.fenixedu.academictreasury.services.signals.StandaloneEnrolmentHandler;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.commons.i18n.I18N;
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
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.FenixFramework;

public class AcademicTreasuryBridgeImpl implements ITreasuryBridgeAPI {

    private static class AcademicProduct implements ITreasuryProduct {

        private Product product;

        private AcademicProduct(final Product product) {
            this.product = product;
        }

        @Override
        public String getCode() {
            return product.getCode();
        }

        @Override
        public String getName() {
            return product.getName().getContent();
        }

        @Override
        public int hashCode() {
            return product.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof AcademicProduct) && ((AcademicProduct) obj).product == this.product;
        }
    }

    private static class TreasuryEntity implements ITreasuryEntity {

        private FinantialEntity finantialEntity;

        public TreasuryEntity(final FinantialEntity finantialEntity) {
            this.finantialEntity = finantialEntity;
        }

        @Override
        public String getCode() {
            return finantialEntity.getExternalId();
        }

        @Override
        public String getName() {
            return finantialEntity.getName().getContent();
        }

        @Override
        public int hashCode() {
            return finantialEntity.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof TreasuryEntity) && ((TreasuryEntity) obj).finantialEntity == this.finantialEntity;
        }
    }

    private static class PaymentCodePoolImpl implements IPaymentCodePool {

        private PaymentCodePool paymentCodePool;

        public PaymentCodePoolImpl(final PaymentCodePool paymentCodePool) {
            this.paymentCodePool = paymentCodePool;
        }

        @Override
        public String getCode() {
            return paymentCodePool.getExternalId();
        }

        @Override
        public String getName() {
            return paymentCodePool.getName();
        }

        public boolean isActive() {
            return paymentCodePool.getActive() != null && paymentCodePool.getActive();
        }

        @Override
        public int hashCode() {
            return paymentCodePool.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof PaymentCodePoolImpl
                    && ((PaymentCodePoolImpl) obj).paymentCodePool == this.paymentCodePool;
        }
    }

    // @formatter:off
    /* ---------------------------------
     * TREASURY INSTITUTION AND PRODUCTS
     * ---------------------------------
     */
    // @formatter:on

    @Override
    public Set<ITreasuryEntity> getTreasuryEntities() {
        return FinantialEntity.findAll().map(f -> new TreasuryEntity(f)).collect(Collectors.toSet());
    }

    @Override
    public ITreasuryEntity getTreasuryEntityByCode(final String code) {
        if (FenixFramework.getDomainObject(code) == null) {
            throw new AcademicTreasuryDomainException("error.ITreasuryBridgeAPI.finantial.entity.not.found");
        }

        return new TreasuryEntity(FenixFramework.getDomainObject(code));
    }

    @Override
    public Set<ITreasuryProduct> getProducts(final ITreasuryEntity treasuryEntity) {
        return Product.findAllActive()
                .filter(p -> p.getFinantialInstitutionsSet()
                        .contains(((TreasuryEntity) treasuryEntity).finantialEntity.getFinantialInstitution()))
                .map(p -> new AcademicProduct(p)).collect(Collectors.toSet());
    }

    @Override
    public ITreasuryProduct getProductByCode(final String code) {
        return Product.findUniqueByCode(code).map(p -> new AcademicProduct(p)).orElse(null);
    }

    public List<IPaymentCodePool> getPaymentCodePools(final ITreasuryEntity treasuryEntity) {
        final FinantialInstitution finantialInstitution =
                ((TreasuryEntity) treasuryEntity).finantialEntity.getFinantialInstitution();

        return finantialInstitution.getPaymentCodePoolsSet().stream().map(p -> new PaymentCodePoolImpl(p))
                .collect(Collectors.toList());
    }

    public IPaymentCodePool getPaymentCodePoolByCode(final String code) {
        if (FenixFramework.getDomainObject(code) == null) {
            throw new AcademicTreasuryDomainException("error.ITreasuryBridgeAPI.paymentCodePool.not.found");
        }

        return new PaymentCodePoolImpl((PaymentCodePool) FenixFramework.getDomainObject(code));
    }

    /* ------------------------ 
     * ACADEMIC SERVICE REQUEST
     * ------------------------ 
     */

    @Override
    public void registerNewAcademicServiceRequestSituationHandler() {
        Signal.register(ACADEMIC_SERVICE_REQUEST_NEW_SITUATION_EVENT, new EmolumentServices());
        //The PERSON CREATE SIGNAL Was replaced for the REGISTRATION CREATE SIGNAL, 
//        Signal.register(Person.PERSON_CREATE_SIGNAL, new PersonServices());
        Signal.register(Registration.REGISTRATION_CREATE_SIGNAL, new RegistrationServices());
    }

    @Override
    public void registerAcademicServiceRequestCancelOrRejectHandler() {
        Signal.register(ACADEMIC_SERVICE_REQUEST_REJECT_OR_CANCEL_EVENT, new AcademicServiceRequestCancelOrRejectHandler());
    }

    @Override
    public IAcademicServiceRequestAndAcademicTaxTreasuryEvent academicTreasuryEventForAcademicServiceRequest(
            final AcademicServiceRequest academicServiceRequest) {
        return academicServiceRequest.getAcademicTreasuryEvent();
    }

    /* ----------
     * ENROLMENTS
     * ----------
     */

    @Override
    public void registerStandaloneEnrolmentHandler() {
        Signal.register(STANDALONE_ENROLMENT, new StandaloneEnrolmentHandler());
    }

    @Override
    public void registerExtracurricularEnrolmentHandler() {
        Signal.register(EXTRACURRICULAR_ENROLMENT, new ExtracurricularEnrolmentHandler());
    }

    @Override
    public void registerImprovementEnrolmentHandler() {
        Signal.register(IMPROVEMENT_ENROLMENT, new ImprovementEnrolmentHandler());
    }

    public void registerNormalEnrolmentHandler() {
        Signal.register(NORMAL_ENROLMENT, new NormalEnrolmentHandler());
    }

    @Override
    public void standaloneUnenrolment(final Enrolment standaloneEnrolment) {
        TuitionServices.removeDebitEntryForStandaloneEnrolment(standaloneEnrolment);
    }

    @Override
    public void extracurricularUnenrolment(final Enrolment extracurricularEnrolment) {
        TuitionServices.removeDebitEntryForExtracurricularEnrolment(extracurricularEnrolment);
    }

    /* --------
     * TUITIONS
     * --------
     */

    @Override
    public ITuitionTreasuryEvent getTuitionForRegistrationTreasuryEvent(final Registration registration,
            final ExecutionYear executionYear) {
        return TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);
    }

    @Override
    public ITuitionTreasuryEvent getTuitionForStandaloneTreasuryEvent(final Registration registration,
            final ExecutionYear executionYear) {
        return TuitionServices.findAcademicTreasuryEventTuitionForStandalone(registration, executionYear);
    }

    @Override
    public ITuitionTreasuryEvent getTuitionForExtracurricularTreasuryEvent(final Registration registration,
            final ExecutionYear executionYear) {
        return null;
    }

    @Override
    public ITuitionTreasuryEvent getTuitionForImprovementTreasuryEvent(final Registration registration,
            final ExecutionYear executionYear) {
        return AcademicTaxServices.findAcademicTreasuryEventForImprovementTax(registration, executionYear);
    }

    @Override
    public void improvementUnrenrolment(EnrolmentEvaluation improvementEnrolmentEvaluation) {
        AcademicTaxServices.removeDebitEntryForImprovement(improvementEnrolmentEvaluation);
    }

    @Override
    public boolean isToPayTuition(final Registration registration, final ExecutionYear executionYear) {
        return TuitionServices.isToPayRegistrationTuition(registration, executionYear);
    }

    /* --------------
     * ACADEMIC TAXES
     * --------------
     */

    @Override
    public List<IAcademicTreasuryEvent> getAcademicTaxesList(final Registration registration, final ExecutionYear executionYear) {
        return AcademicTaxServices.findAllTreasuryEventsForAcademicTaxes(registration, executionYear);
    }

    @Override
    public IImprovementTreasuryEvent getImprovementTaxTreasuryEvent(Registration registration, ExecutionYear executionYear) {
        if (!AcademicTreasuryEvent.findUniqueForImprovementTuition(registration, executionYear).isPresent()) {
            return null;
        }

        return AcademicTreasuryEvent.findUniqueForImprovementTuition(registration, executionYear).get();
    }

    // @formatter:off
    /* ------------------------
     * ACADEMIC TREASURY TARGET
     * ------------------------
     */
    // @formatter:on

    @Override
    public IAcademicTreasuryEvent getAcademicTreasuryEventForTarget(final IAcademicTreasuryTarget target) {
        final PersonCustomer personCustomer = target.getAcademicTreasuryTargetPerson().getPersonCustomer();

        if (personCustomer == null) {
            return null;
        }

        if (target.getAcademicTreasuryTargetDomainObject() == null) {
            return null;
        }

        return AcademicTreasuryEvent.find(personCustomer).filter(t -> t.getTreasuryEventTarget() == target.getAcademicTreasuryTargetDomainObject())
                .findFirst().orElse(null);
    }

    @Override
    public IAcademicTreasuryEvent createDebt(final ITreasuryEntity treasuryEntity, final ITreasuryProduct treasuryProduct,
            final IAcademicTreasuryTarget target, final LocalDate when, final boolean createPaymentCode,
            final IPaymentCodePool paymentCodePool) {

        final FinantialInstitution finantialInstitution =
                ((TreasuryEntity) treasuryEntity).finantialEntity.getFinantialInstitution();
        final DocumentNumberSeries documentNumberSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(), finantialInstitution).get();
        final DateTime now = new DateTime();
        final Product product = ((AcademicProduct) treasuryProduct).product;
        final Vat vat =
                Vat.findActiveUnique(((Product) product).getVatType(), finantialInstitution, when.toDateTimeAtStartOfDay()).get();
        final AdministrativeOffice administrativeOffice =
                ((TreasuryEntity) treasuryEntity).finantialEntity.getAdministrativeOffice();
        final PaymentCodePool pool = ((PaymentCodePoolImpl) paymentCodePool).paymentCodePool;

        PersonCustomer personCustomer = target.getAcademicTreasuryTargetPerson().getPersonCustomer();
        if (personCustomer == null) {
            personCustomer = PersonCustomer.create(target.getAcademicTreasuryTargetPerson());
        }

        DebtAccount debtAccount = DebtAccount.findUnique(finantialInstitution, personCustomer).orElse(null);
        if (debtAccount == null) {
            debtAccount = DebtAccount.create(finantialInstitution, personCustomer);
        }

        AcademicTreasuryEvent treasuryEvent = (AcademicTreasuryEvent) getAcademicTreasuryEventForTarget(target);

        if (treasuryEvent == null) {
            treasuryEvent = AcademicTreasuryEvent.createForAcademicTreasuryEventTarget(debtAccount, product, target);
        }

        final AcademicTariff academicTariff =
                AcademicTariff.findMatch(product, administrativeOffice, when.toDateTimeAtStartOfDay());
        final LocalDate dueDate = academicTariff.dueDate(when);

        final DebitNote debitNote = DebitNote.create(debtAccount, documentNumberSeries, now);
        final DebitEntry debitEntry = DebitEntry.create(Optional.of(debitNote), debtAccount, treasuryEvent, vat,
                academicTariff.getBaseAmount(), dueDate, target.getAcademicTreasuryTargetPropertiesMap(), product,
                target.getAcademicTreasuryTargetDescription().getContent(), BigDecimal.ONE, academicTariff.getInterestRate(),
                when.toDateTimeAtStartOfDay());

        if (createPaymentCode) {
            createPaymentReferenceCode(pool, debitEntry, when, dueDate);
        }

        return treasuryEvent;
    }

    @Override
    public IAcademicTreasuryEvent createDebt(final ITreasuryEntity treasuryEntity, final ITreasuryProduct treasuryProduct,
            final IAcademicTreasuryTarget target, final BigDecimal amount, final LocalDate when, final LocalDate dueDate,
            final boolean createPaymentCode, final IPaymentCodePool paymentCodePool) {

        final FinantialInstitution finantialInstitution =
                ((TreasuryEntity) treasuryEntity).finantialEntity.getFinantialInstitution();
        final DocumentNumberSeries documentNumberSeries =
                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(), finantialInstitution).get();
        final DateTime now = new DateTime();
        final Product product = ((AcademicProduct) treasuryProduct).product;
        final Vat vat =
                Vat.findActiveUnique(((Product) product).getVatType(), finantialInstitution, when.toDateTimeAtStartOfDay()).get();
        final PaymentCodePool pool = ((PaymentCodePoolImpl) paymentCodePool).paymentCodePool;

        PersonCustomer personCustomer = target.getAcademicTreasuryTargetPerson().getPersonCustomer();
        if (personCustomer == null) {
            personCustomer = PersonCustomer.create(target.getAcademicTreasuryTargetPerson());
        }

        DebtAccount debtAccount = DebtAccount.findUnique(finantialInstitution, personCustomer).orElse(null);
        if (debtAccount == null) {
            debtAccount = DebtAccount.create(finantialInstitution, personCustomer);
        }

        AcademicTreasuryEvent treasuryEvent = (AcademicTreasuryEvent) getAcademicTreasuryEventForTarget(target);

        if (treasuryEvent == null) {
            treasuryEvent = AcademicTreasuryEvent.createForAcademicTreasuryEventTarget(debtAccount, product, target);
        }

        final DebitNote debitNote = DebitNote.create(debtAccount, documentNumberSeries, now);
        final DebitEntry debitEntry = DebitEntry.create(Optional.of(debitNote), debtAccount, treasuryEvent, vat, amount, dueDate,
                target.getAcademicTreasuryTargetPropertiesMap(), product,
                target.getAcademicTreasuryTargetDescription().getContent(), BigDecimal.ONE, null, when.toDateTimeAtStartOfDay());

        if (createPaymentCode) {
            createPaymentReferenceCode(pool, debitEntry, when, dueDate);
        }

        return treasuryEvent;
    }

    private PaymentReferenceCode createPaymentReferenceCode(final PaymentCodePool paymentCodePool, final DebitEntry debitEntry,
            final LocalDate when, final LocalDate dueDate) {
        final PaymentReferenceCode paymentReferenceCode =
                paymentCodePool.getReferenceCodeGenerator().generateNewCodeFor(debitEntry.getOpenAmount(), when, dueDate, true);
        paymentReferenceCode.createPaymentTargetTo(Sets.newHashSet(debitEntry), debitEntry.getOpenAmount());

        return paymentReferenceCode;
    }

    /* --------------
     * ACADEMICAL ACT
     * --------------
     */

    @Override
    public boolean isAcademicalActsBlocked(final Person person, final LocalDate when) {
        return PersonServices.isAcademicalActsBlocked(person, when);
    }

    @Override
    public boolean isAcademicalActBlockingSuspended(final Person person, final LocalDate when) {
        return AcademicActBlockingSuspension.isBlockingSuspended(person, when);
    }

    /* -----
     * OTHER
     * -----
     */

    @Override
    public List<IAcademicTreasuryEvent> getAllAcademicTreasuryEventsList(final Person person, final ExecutionYear executionYear) {
        if (!PersonCustomer.findUnique(person).isPresent()) {
            return Lists.newArrayList();
        }

        return AcademicTreasuryEvent.find(PersonCustomer.findUnique(person).get(), executionYear)
                .collect(Collectors.<IAcademicTreasuryEvent> toList());
    }

    @Override
    public List<IAcademicTreasuryEvent> getAllAcademicTreasuryEventsList(final Person person) {
        if (!PersonCustomer.findUnique(person).isPresent()) {
            return Lists.newArrayList();
        }

        return AcademicTreasuryEvent.find(PersonCustomer.findUnique(person).get())
                .collect(Collectors.<IAcademicTreasuryEvent> toList());
    }

    @Override
    public String getPersonAccountTreasuryManagementURL(final Person person) {
        if (!PersonCustomer.findUnique(person).isPresent()) {
            return null;
        }

        return CustomerController.READ_URL + PersonCustomer.findUnique(person).get().getExternalId();
    }

    @Override
    public boolean isPersonAccountTreasuryManagementAvailable(Person person) {
        return PersonCustomer.findUnique(person).isPresent();
    }

    @Override
    public String getRegistrationAccountTreasuryManagementURL(Registration registration) {
        if (registration.getDegree().getAdministrativeOffice().getFinantialEntity() == null) {
            return getPersonAccountTreasuryManagementURL(registration.getPerson());
        }

        final FinantialInstitution inst =
                registration.getDegree().getAdministrativeOffice().getFinantialEntity().getFinantialInstitution();
        final Person person = registration.getPerson();
        final PersonCustomer customer = PersonCustomer.findUnique(person).get();

        final DebtAccount account = customer.getDebtAccountFor(inst);
        if (account != null) {
            return DebtAccountController.READ_URL + customer.getDebtAccountFor(inst).getExternalId();
        } else {
            return getPersonAccountTreasuryManagementURL(person);
        }
    }

    @Override
    public void createAcademicDebts(final Registration registration) {
        AcademicDebtGenerationRule.runAllActiveForRegistration(registration, true);
    }

}
