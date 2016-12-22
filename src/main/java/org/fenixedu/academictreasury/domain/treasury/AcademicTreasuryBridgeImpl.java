package org.fenixedu.academictreasury.domain.treasury;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.treasury.IAcademicServiceRequestAndAcademicTaxTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryTarget;
import org.fenixedu.academic.domain.treasury.IImprovementTreasuryEvent;
import org.fenixedu.academic.domain.treasury.ITreasuryBridgeAPI;
import org.fenixedu.academic.domain.treasury.ITreasuryInstitution;
import org.fenixedu.academic.domain.treasury.ITreasuryProduct;
import org.fenixedu.academic.domain.treasury.ITuitionTreasuryEvent;
import org.fenixedu.academictreasury.domain.academicalAct.AcademicActBlockingSuspension;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
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
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.services.reports.dataproviders.DebtAccountDataProvider;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;

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
            return (obj instanceof AcademicProduct) && ((AcademicProduct) obj).product == product;
        }
    }

    private static class TreasuryInstitution implements ITreasuryInstitution {

        private FinantialInstitution finantialInstitution;

        public TreasuryInstitution(final FinantialInstitution finantialInstitution) {
            this.finantialInstitution = finantialInstitution;
        }

        @Override
        public String getFiscalNumber() {
            return finantialInstitution.getFiscalNumber();
        }

        @Override
        public String getName() {
            return finantialInstitution.getName();
        }

        @Override
        public int hashCode() {
            return finantialInstitution.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof TreasuryInstitution)
                    && ((TreasuryInstitution) obj).finantialInstitution == finantialInstitution;
        }
    }

    // @formatter:off
    /* ---------------------------------
     * TREASURY INSTITUTION AND PRODUCTS
     * ---------------------------------
     */
    // @formatter:on

    @Override
    public Set<ITreasuryInstitution> getTreasuryInstitutions() {
        return FinantialInstitution.findAll().map(f -> new TreasuryInstitution(f)).collect(Collectors.toSet());
    }

    @Override
    public ITreasuryInstitution getTreasuryInstitutionByFiscalNumber(final String fiscalNumber) {
        return FinantialInstitution.findUniqueByFiscalCode(fiscalNumber).map(f -> new TreasuryInstitution(f)).orElse(null);
    }

    @Override
    public Set<ITreasuryProduct> getProducts(final ITreasuryInstitution treasuryInstitution) {
        return Product.findAllActive()
                .filter(p -> p.getFinantialInstitutionsSet()
                        .contains(((TreasuryInstitution) treasuryInstitution).finantialInstitution))
                .map(p -> new AcademicProduct(p)).collect(Collectors.toSet());
    }

    @Override
    public ITreasuryProduct getProductByCode(final String code) {
        return Product.findUniqueByCode(code).map(p -> new AcademicProduct(p)).orElse(null);
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
        final PersonCustomer personCustomer = target.getPerson().getPersonCustomer();
        
        if(personCustomer == null) {
            return null;
        }
        
        return AcademicTreasuryEvent.find(personCustomer).filter(t -> t.getTreasuryEventTarget() == target).findFirst().orElse(null);
    }

    public IAcademicTreasuryEvent createDebtForTarget(final IAcademicTreasuryTarget target, final ITreasuryProduct product,
            final ITreasuryInstitution treasuryInstitution, final BigDecimal amount, final LocalDate dueDate,
            final boolean applyInterestGlobalTax) {
        
        PersonCustomer personCustomer = target.getPerson().getPersonCustomer();
        
        if(personCustomer == null) {
            personCustomer = PersonCustomer.create(target.getPerson());
        }
        
        FinantialInstitution finantialInstitution = ((TreasuryInstitution) treasuryInstitution).finantialInstitution;
        DebtAccount debtAccount = DebtAccount.findUnique(finantialInstitution, personCustomer).orElse(null);
        
        if(debtAccount == null) {
            debtAccount = DebtAccount.create(finantialInstitution, personCustomer);
        }
        
        AcademicTreasuryEvent treasuryEvent = (AcademicTreasuryEvent) getAcademicTreasuryEventForTarget(target);
        
        if(treasuryEvent == null) {
            treasuryEvent = AcademicTreasuryEvent.create
        }
        
        DebitEntry.create(null, debtAccount, treasuryEvent, ((Product) product).getVatType(), amount, dueDate, propertiesMap, product, description, BigDecimal.ONE, interestRate, entryDateTime);
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
