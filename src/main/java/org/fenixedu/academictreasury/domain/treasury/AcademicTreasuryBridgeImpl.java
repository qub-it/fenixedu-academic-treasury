package org.fenixedu.academictreasury.domain.treasury;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.treasury.IAcademicServiceRequestAndAcademicTaxTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IImprovementTreasuryEvent;
import org.fenixedu.academic.domain.treasury.ITreasuryBridgeAPI;
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
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;

public class AcademicTreasuryBridgeImpl implements ITreasuryBridgeAPI {

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
        return AcademicTreasuryEvent.find(person, executionYear).collect(Collectors.<IAcademicTreasuryEvent> toList());
    }

    @Override
    public List<IAcademicTreasuryEvent> getAllAcademicTreasuryEventsList(final Person person) {
        return AcademicTreasuryEvent.find(person).collect(Collectors.<IAcademicTreasuryEvent> toList());
    }

    @Override
    public String getPersonAccountTreasuryManagementURL(final Person person) {
        final String countryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        return CustomerController.READ_URL + PersonCustomer.findUnique(person, countryCode, fiscalNumber).get().getExternalId();
    }

    @Override
    public boolean isPersonAccountTreasuryManagementAvailable(Person person) {
        final String countryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        return PersonCustomer.findUnique(person, countryCode, fiscalNumber).isPresent();
    }

    @Override
    public String getRegistrationAccountTreasuryManagementURL(Registration registration) {
        if (registration.getDegree().getAdministrativeOffice().getFinantialEntity() == null) {
            return getPersonAccountTreasuryManagementURL(registration.getPerson());
        }

        final FinantialInstitution inst =
                registration.getDegree().getAdministrativeOffice().getFinantialEntity().getFinantialInstitution();
        final Person person = registration.getPerson();
        final String countryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);

        final PersonCustomer customer = PersonCustomer.findUnique(person, countryCode, fiscalNumber).get();

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

    @Override
    public boolean isValidFiscalNumber(final String fiscalCountryCode, final String fiscalNumber) {
        return FiscalCodeValidation.isValidFiscalNumber(fiscalCountryCode, fiscalNumber);
    }

    @Override
    public boolean updateCustomer(final Person person, final String fiscalCountryCode, final String fiscalNumber) {
        return PersonCustomer.switchCustomer(person, fiscalCountryCode, fiscalNumber);
    }

}
