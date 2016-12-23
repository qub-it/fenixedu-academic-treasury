package org.fenixedu.academictreasury.domain.event;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.text.StrSubstitutor;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationDataByExecutionYear;
import org.fenixedu.academic.domain.treasury.AcademicTreasuryEventPayment;
import org.fenixedu.academic.domain.treasury.IAcademicServiceRequestAndAcademicTaxTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEventPayment;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryTarget;
import org.fenixedu.academic.domain.treasury.IImprovementTreasuryEvent;
import org.fenixedu.academic.domain.treasury.IPaymentReferenceCode;
import org.fenixedu.academic.domain.treasury.ITuitionTreasuryEvent;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.emoluments.ServiceRequestMapEntry;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.domain.tuition.TuitionInstallmentTariff;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlan;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.core.AbstractDomainObject;

public class AcademicTreasuryEvent extends AcademicTreasuryEvent_Base implements IAcademicTreasuryEvent, ITuitionTreasuryEvent,
        IImprovementTreasuryEvent, IAcademicServiceRequestAndAcademicTaxTreasuryEvent {

    public AcademicTreasuryEvent() {
    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final ITreasuryServiceRequest iTreasuryServiceRequest) {
        init(debtAccount, iTreasuryServiceRequest, ServiceRequestMapEntry.findProduct(iTreasuryServiceRequest));
    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final Product product, final Registration registration, final ExecutionYear executionYear) {
        init(debtAccount, tuitionPaymentPlanGroup, product, registration, executionYear);

        checkRules();
    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final AcademicTax academicTax, final Registration registration,
            final ExecutionYear executionYear) {
        init(debtAccount, academicTax, registration, executionYear);
    }

    protected AcademicTreasuryEvent(final DebtAccount debtAccount, final Product product, final IAcademicTreasuryTarget target) {
        init(debtAccount, product, target);
    }

    @Override
    protected void init(final DebtAccount debtAccount, final Product product, final LocalizedString name) {
        throw new RuntimeException("wrong call");
    }

    protected void init(final DebtAccount debtAccount, final ITreasuryServiceRequest iTreasuryServiceRequest,
            final Product product) {
        super.init(debtAccount, product, nameForAcademicServiceRequest(product, iTreasuryServiceRequest));

        setITreasuryServiceRequest(iTreasuryServiceRequest);
        setPropertiesJsonMap(propertiesMapToJson(fillPropertiesMap()));
        setDescription(descriptionForAcademicServiceRequest());

        checkRules();
    }

    private LocalizedString nameForAcademicServiceRequest(final Product product,
            final ITreasuryServiceRequest iTreasuryServiceRequest) {
        LocalizedString result = new LocalizedString();

        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            if (iTreasuryServiceRequest.getExecutionYear() != null) {
                result = result
                        .with(locale,
                                String.format("%s [%s - %s] (%s)", product.getName().getContent(locale),
                                        iTreasuryServiceRequest.getRegistration().getDegree().getPresentationNameI18N()
                                                .getContent(),
                                        iTreasuryServiceRequest.getExecutionYear().getQualifiedName(),
                                        iTreasuryServiceRequest.getServiceRequestNumberYear()));
            } else {
                result = result.with(locale,
                        String.format("%s [%s] (%s)", product.getName().getContent(locale),
                                iTreasuryServiceRequest.getRegistration().getDegree().getPresentationNameI18N().getContent(),
                                iTreasuryServiceRequest.getServiceRequestNumberYear()));
            }
        }

        return result;
    }

    protected void init(final DebtAccount debtAccount, final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final Product product, final Registration registration, final ExecutionYear executionYear) {
        super.init(debtAccount, product, nameForTuition(product, registration, executionYear));

        setTuitionPaymentPlanGroup(tuitionPaymentPlanGroup);
        setRegistration(registration);
        setExecutionYear(executionYear);
        setPropertiesJsonMap(propertiesMapToJson(fillPropertiesMap()));

        checkRules();
    }

    private LocalizedString nameForTuition(final Product product, final Registration registration,
            final ExecutionYear executionYear) {
        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            final String name = String.format("%s [%s - %s]", product.getName().getContent(),
                    registration.getDegree().getPresentationNameI18N().getContent(), executionYear.getQualifiedName());

            result = result.with(locale, name);
        }

        return result;
    }

    protected void init(final DebtAccount debtAccount, final AcademicTax academicTax, final Registration registration,
            final ExecutionYear executionYear) {
        super.init(debtAccount, academicTax.getProduct(), nameForAcademicTax(academicTax, registration, executionYear));

        setAcademicTax(academicTax);

        setRegistration(registration);
        setExecutionYear(executionYear);
        setPropertiesJsonMap(propertiesMapToJson(fillPropertiesMap()));

        checkRules();
    }

    private LocalizedString nameForAcademicTax(final AcademicTax academicTax, final Registration registration,
            final ExecutionYear executionYear) {
        LocalizedString result = new LocalizedString();
        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            String name = null;
            if (academicTax.isAppliedOnRegistration()) {
                name = String.format("%s [%s - %s]", academicTax.getProduct().getName().getContent(),
                        registration.getDegree().getPresentationNameI18N().getContent(), executionYear.getQualifiedName());
            } else {
                name = String.format("%s [%s]", academicTax.getProduct().getName().getContent(),
                        executionYear.getQualifiedName());
            }

            result = result.with(locale, name);
        }

        return result;
    }

    protected void init(final DebtAccount debtAccount, final Product product, final IAcademicTreasuryTarget target) {
        if (target == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.target.required");
        }

        super.init(debtAccount, product, target.getAcademicTreasuryTargetDescription());
        setTreasuryEventTarget((AbstractDomainObject) target);
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        if (!isForAcademicServiceRequest() && !isTuitionEvent() && !isForAcademicTax() && !isForImprovementTax()) {
            throw new AcademicTreasuryDomainException(
                    "error.AcademicTreasuryEvent.not.for.service.request.nor.tuition.nor.academic.tax");
        }

        if (!(isForAcademicServiceRequest() ^ isForRegistrationTuition() ^ isForStandaloneTuition()
                ^ isForExtracurricularTuition() ^ isForImprovementTax() ^ isForAcademicTax() ^ isForTreasuryEventTarget())) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.for.one.type");
        }

        if ((isTuitionEvent() || isForImprovementTax()) && getRegistration() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.registration.required");
        }

        if ((isTuitionEvent() || isForImprovementTax()) && getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.executionYear.required");
        }

        if (isForAcademicServiceRequest() && find(getITreasuryServiceRequest()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.event.for.academicServiceRequest.duplicate");
        }

        if (isForAcademicServiceRequest()) {
            //Ensuring that the Academic Service Request implements the ITreasuryServiceRequest.
            getITreasuryServiceRequest();
        }

        if (isForRegistrationTuition() && findForRegistrationTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.registration.tuition.duplicate");
        }

        if (isForStandaloneTuition() && findForStandaloneTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.standalone.tuition.duplicate");
        }

        if (isForExtracurricularTuition() && findForExtracurricularTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.extracurricular.tuition.duplicate");
        }

        if (isForImprovementTax() && findForImprovementTuition(getRegistration(), getExecutionYear()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.improvement.tuition.duplicate");
        }

        if (isForAcademicTax() && getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.academic.tax.execution.year.required");
        }

        if (isForAcademicTax() && getRegistration() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.academic.tax.registration.required");
        }

        if (isForAcademicTax() && findForAcademicTax(getRegistration(), getExecutionYear(), getAcademicTax()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.for.academic.tax.duplicate");
        }
    }

    public boolean isForTreasuryEventTarget() {
        return getTreasuryEventTarget() != null;
    }

    public boolean isForAcademicServiceRequest() {
        return getITreasuryServiceRequest() != null;
    }

    public boolean isForRegistrationTuition() {
        return getTuitionPaymentPlanGroup() != null && getTuitionPaymentPlanGroup().isForRegistration();
    }

    public boolean isForStandaloneTuition() {
        return getTuitionPaymentPlanGroup() != null && getTuitionPaymentPlanGroup().isForStandalone();
    }

    public boolean isLegacy() {
        return false;
    }

    public boolean isForExtracurricularTuition() {
        return getTuitionPaymentPlanGroup() != null && getTuitionPaymentPlanGroup().isForExtracurricular();
    }

    public boolean isForImprovementTax() {
        return getAcademicTax() != null && getAcademicTax() == AcademicTreasurySettings.getInstance().getImprovementAcademicTax();
    }

    public boolean isForAcademicTax() {
        return getAcademicTax() != null && !isImprovementTax();
    }

    public int getNumberOfUnits() {
        if (isForAcademicServiceRequest()) {
            return Constants.getNumberOfUnits(getITreasuryServiceRequest());
        } else if (isForAcademicTax()) {
            return 0;
        } else if (isForImprovementTax()) {
            return 0;
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.numberOfUnits.not.applied");
    }

    public int getNumberOfPages() {
        if (isForAcademicServiceRequest()) {
            return getITreasuryServiceRequest().hasNumberOfPages() ? getITreasuryServiceRequest().getNumberOfPages() : 0;
        } else if (isForAcademicTax()) {
            return 0;
        } else if (isForImprovementTax()) {
            return 0;
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.numberOfPages.not.applied");
    }

    public boolean isUrgentRequest() {
        if (isForAcademicServiceRequest()) {
            return getITreasuryServiceRequest().isUrgent();
        } else if (isForAcademicTax()) {
            return false;
        } else if (isForImprovementTax()) {
            return false;
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.urgentRequest.not.applied");
    }

    public LocalDate getRequestDate() {
        if (isForAcademicServiceRequest()) {
            return getITreasuryServiceRequest().getRequestDate().toLocalDate();
        } else if (isForAcademicTax() && !isForImprovementTax()) {
            final LocalDate requestDate = RegistrationDataByExecutionYear
                    .getOrCreateRegistrationDataByYear(getRegistration(), getExecutionYear()).getEnrolmentDate();

            return requestDate != null ? requestDate : new LocalDate();
        } else if (isForImprovementTax()) {
            final LocalDate requestDate = RegistrationDataByExecutionYear
                    .getOrCreateRegistrationDataByYear(getRegistration(), getExecutionYear()).getEnrolmentDate();

            return requestDate != null ? requestDate : new LocalDate();
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.requestDate.not.applied");
    }

    public Locale getLanguage() {
        if (isForAcademicServiceRequest()) {
            return getITreasuryServiceRequest().getLanguage();
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.language.not.applied");
    }

    public boolean isChargedWithDebitEntry(final TuitionInstallmentTariff tariff) {
        return DebitEntry.findActive(this).filter(d -> d.getProduct().equals(tariff.getProduct())).count() > 0;
    }

    public boolean isCharged() {
        return DebitEntry.findActive(this).count() > 0;
    }

    public boolean isChargedWithDebitEntry(final Enrolment enrolment) {
        if (!isForStandaloneTuition() && !isForExtracurricularTuition()) {
            throw new RuntimeException("wrong call");
        }

        return findActiveEnrolmentDebitEntry(enrolment).isPresent();
    }

    public boolean isChargedWithDebitEntry(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isForImprovementTax()) {
            throw new RuntimeException("wrong call");
        }

        return findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent();
    }

    @Override
    @Atomic
    public LocalDate getTreasuryEventDate() {
        if (isForAcademicServiceRequest()) {
            return getITreasuryServiceRequest().getRequestDate().toLocalDate();
        } else if (isForImprovementTax() || isForAcademicTax() || isForRegistrationTuition() || isForExtracurricularTuition()
                || isForStandaloneTuition()) {

            final RegistrationDataByExecutionYear data =
                    RegistrationDataByExecutionYear.getOrCreateRegistrationDataByYear(getRegistration(), getExecutionYear());
            if (data.getEnrolmentDate() != null) {
                return data.getEnrolmentDate();
            }

            return getExecutionYear().getBeginLocalDate();
        } else if(isForTreasuryEventTarget()) {
            return ((IAcademicTreasuryTarget) getTreasuryEventTarget()).getAcademicTreasuryTargetEventDate();
        }
        
        throw new RuntimeException("dont know how to handle this!");
    }

    public Optional<? extends DebitEntry> findActiveAcademicServiceRequestDebitEntry() {
        return DebitEntry.findActive(this).findFirst();
    }

    public Optional<? extends DebitEntry> findActiveEnrolmentDebitEntry(final Enrolment enrolment) {
        return DebitEntry.findActive(this).filter(d -> d.getCurricularCourse() == enrolment.getCurricularCourse()
                && d.getExecutionSemester() == enrolment.getExecutionPeriod()).findFirst();
    }

    public Optional<? extends DebitEntry> findActiveEnrolmentEvaluationDebitEntry(final EnrolmentEvaluation enrolmentEvaluation) {
        return DebitEntry.findActive(this)
                .filter(d -> d.getCurricularCourse() == enrolmentEvaluation.getEnrolment().getCurricularCourse()
                        && d.getExecutionSemester() == enrolmentEvaluation.getExecutionPeriod()
                        && d.getEvaluationSeason() == enrolmentEvaluation.getEvaluationSeason())
                .findFirst();
    }

    public void associateEnrolment(final DebitEntry debitEntry, final Enrolment enrolment) {
        if (!isForStandaloneTuition() && !isForExtracurricularTuition()) {
            throw new RuntimeException("wrong call");
        }

        if (enrolment == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.enrolment.cannot.be.null");
        }

        if (enrolment.isOptional()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.optional.enrolments.not.supported");
        }

        debitEntry.setCurricularCourse(enrolment.getCurricularCourse());
        debitEntry.setExecutionSemester(enrolment.getExecutionPeriod());
    }

    public void associateEnrolmentEvaluation(final DebitEntry debitEntry, final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isForImprovementTax()) {
            throw new RuntimeException("wrong call");
        }

        if (enrolmentEvaluation == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.enrolmentEvaluation.cannot.be.null");
        }

        debitEntry.setCurricularCourse(enrolmentEvaluation.getEnrolment().getCurricularCourse());
        debitEntry.setExecutionSemester(enrolmentEvaluation.getEnrolment().getExecutionPeriod());

        if (enrolmentEvaluation.getExecutionPeriod() != null) {
            debitEntry.setExecutionSemester(enrolmentEvaluation.getExecutionPeriod());
        }

        debitEntry.setEvaluationSeason(enrolmentEvaluation.getEvaluationSeason());
    }

    @Override
    public Set<Product> getPossibleProductsToExempt() {
        if (isForRegistrationTuition()) {
            return TuitionPaymentPlan
                    .find(getTuitionPaymentPlanGroup(),
                            getRegistration().getStudentCurricularPlan(getExecutionYear()).getDegreeCurricularPlan(),
                            getExecutionYear())
                    .map(t -> t.getTuitionInstallmentTariffsSet()).reduce((a, b) -> Sets.union(a, b)).orElse(Sets.newHashSet())
                    .stream().map(i -> i.getProduct()).collect(Collectors.toSet());
        }

        return Sets.newHashSet(getProduct());
    }

    private LocalizedString descriptionForAcademicServiceRequest() {
        final ServiceRequestMapEntry serviceRequestMapEntry = ServiceRequestMapEntry.findMatch(getITreasuryServiceRequest());

        LocalizedString result = new LocalizedString();

        for (final Locale locale : CoreConfiguration.supportedLocales()) {
            String text =
                    getProduct().getName().getContent(locale) + ": " + getITreasuryServiceRequest().getServiceRequestNumberYear();

            if (!Strings.isNullOrEmpty(serviceRequestMapEntry.getDebitEntryDescriptionExtensionFormat())) {
                final StrSubstitutor str = new StrSubstitutor(getITreasuryServiceRequest().getPropertyValuesMap());

                final String extString = str.replace(serviceRequestMapEntry.getDebitEntryDescriptionExtensionFormat());
                text += " " + extString;
            }

            result = result.with(locale, text);
        }

        return result;
    }

    @Override
    public String getDegreeCode() {
        if (degree() == null) {
            return null;
        }

        return degree().getCode();
    }

    @Override
    public String getDegreeName() {
        if (degree() == null) {
            return null;
        }

        if (getExecutionYear() != null) {
            return degree().getPresentationNameI18N(getExecutionYear()).getContent();
        }

        return degree().getPresentationNameI18N().getContent();
    }

    @Override
    public String getExecutionYearName() {
        if (getExecutionYear() != null) {
            return getExecutionYear().getQualifiedName();
        }

        return null;
    }

    private Degree degree() {
        Degree degree = null;

        if (isForRegistrationTuition() && getRegistration() != null) {
            degree = getRegistration().getDegree();
        } else if ((isForStandaloneTuition() || isForExtracurricularTuition())) {
        } else if (isForImprovementTax()) {
        } else if (isForAcademicTax() && getRegistration() != null) {
            degree = getRegistration().getDegree();
        } else if (isForAcademicServiceRequest() && getRegistration() != null) {
            degree = getRegistration().getDegree();
        }

        return degree;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<? extends AcademicTreasuryEvent> findAll() {
        return TreasuryEvent.findAll().filter(e -> e instanceof AcademicTreasuryEvent).map(AcademicTreasuryEvent.class::cast);
    }

    public static Stream<? extends AcademicTreasuryEvent> find(final Customer customer) {
        return findAll().filter(l -> l.getDebtAccount().getCustomer() == customer);
    }

    public static Stream<? extends AcademicTreasuryEvent> find(final Customer customer, final ExecutionYear executionYear) {
        return find(customer).filter(l -> l.getExecutionYear() == executionYear || l.isAcademicServiceRequestEvent()
                || executionYear.containsDate(l.getRequestDate()));
    }

    /* --- Academic Service Requests --- */

    public static Stream<? extends AcademicTreasuryEvent> find(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        if (iTreasuryServiceRequest == null) {
            throw new RuntimeException("wrong call");
        }

        return findAll().filter(e -> e.getITreasuryServiceRequest() != null
                && e.getITreasuryServiceRequest().getExternalId().equals(iTreasuryServiceRequest.getExternalId()));
    }

    public static Optional<? extends AcademicTreasuryEvent> findUnique(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        return find(iTreasuryServiceRequest).findFirst();
    }

    public static AcademicTreasuryEvent createForAcademicServiceRequest(final DebtAccount debtAccount,
            final ITreasuryServiceRequest iTreasuryServiceRequest) {
        return new AcademicTreasuryEvent(debtAccount, iTreasuryServiceRequest);
    }

    /* *******
     * TUITION
     * *******
     */

    /* For Registration */

    protected static Stream<? extends AcademicTreasuryEvent> findForRegistrationTuition(final Registration registration,
            final ExecutionYear executionYear) {
//      return findAll().filter(e -> e.isForRegistrationTuition() && e.getRegistration() == registration
//              && e.getExecutionYear() == executionYear);
//        
        return registration.getAcademicTreasuryEventSet().stream().filter(e -> e.isForRegistrationTuition()
                && e.getRegistration() == registration && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForRegistrationTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForRegistrationTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForRegistrationTuition(final DebtAccount debtAccount, final Product product,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get(),
                product, registration, executionYear);
    }

    /* For Standalone */

    protected static Stream<? extends AcademicTreasuryEvent> findForStandaloneTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(
                e -> e.isForStandaloneTuition() && e.getRegistration() == registration && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForStandaloneTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForStandaloneTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForStandaloneTuition(final DebtAccount debtAccount, final Product product,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
                product, registration, executionYear);
    }

    /* For Extracurricular */

    protected static Stream<? extends AcademicTreasuryEvent> findForExtracurricularTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(e -> e.isForExtracurricularTuition() && e.getRegistration() == registration
                && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForExtracurricularTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForExtracurricularTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForExtracurricularTuition(final DebtAccount debtAccount, final Product product,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForExtracurricular().get(),
                product, registration, executionYear);
    }

    /* For Improvement */

    protected static Stream<? extends AcademicTreasuryEvent> findForImprovementTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(
                e -> e.isForImprovementTax() && e.getRegistration() == registration && e.getExecutionYear() == executionYear);
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForImprovementTuition(final Registration registration,
            final ExecutionYear executionYear) {
        return findForImprovementTuition(registration, executionYear).findFirst();
    }

    public static AcademicTreasuryEvent createForImprovementTuition(final DebtAccount debtAccount,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, AcademicTreasurySettings.getInstance().getImprovementAcademicTax(),
                registration, executionYear);
    }

    /* ************
     * ACADEMIC TAX
     * ************
     */

    public static Stream<? extends AcademicTreasuryEvent> findAllForAcademicTax(final Registration registration,
            final ExecutionYear executionYear) {
        return findAll().filter(e -> e.isForAcademicTax() && e.getExecutionYear() == executionYear);
    }

    public static Stream<? extends AcademicTreasuryEvent> findForAcademicTax(final Registration registration,
            final ExecutionYear executionYear, final AcademicTax academicTax) {
        final PersonCustomer pc = PersonCustomer.findUnique(registration.getPerson()).orElse(null);

        return academicTax.getAcademicTreasuryEventSet().stream()
                .filter(e -> e.isForAcademicTax() && e.getAcademicTax() == academicTax && e.getExecutionYear() == executionYear
                        && (!e.getAcademicTax().isAppliedOnRegistration() && e.getDebtAccount().getCustomer() == pc
                                || e.getRegistration() == registration));
    }

    public static Optional<? extends AcademicTreasuryEvent> findUniqueForAcademicTax(final Registration registration,
            final ExecutionYear executionYear, final AcademicTax academicTax) {
        return findForAcademicTax(registration, executionYear, academicTax).findFirst();
    }

    public static AcademicTreasuryEvent createForAcademicTax(final DebtAccount debtAccount, final AcademicTax academicTax,
            final Registration registration, final ExecutionYear executionYear) {
        return new AcademicTreasuryEvent(debtAccount, academicTax, registration, executionYear);
    }

    public static AcademicTreasuryEvent createForAcademicTreasuryEventTarget(final DebtAccount debtAccount, final Product product,
            final IAcademicTreasuryTarget target) {

        return new AcademicTreasuryEvent(debtAccount, product, target);
    }

    /* -----
     * UTILS
     * -----
     */

    // @formatter:off
    public static enum AcademicTreasuryEventKeys {

        ACADEMIC_SERVICE_REQUEST_NAME("1"), ACADEMIC_SERVICE_REQUEST_NUMBER_YEAR("2"), EXECUTION_YEAR("3"), EXECUTION_SEMESTER(
                "4"), EVALUATION_SEASON("5"), DETAILED("6"), URGENT("7"), LANGUAGE("8"), BASE_AMOUNT("9"), UNITS_FOR_BASE("10"),
        UNIT_AMOUNT("11"), ADDITIONAL_UNITS("12"), CALCULATED_UNITS_AMOUNT("13"), PAGE_AMOUNT("14"), NUMBER_OF_PAGES("15"),
        CALCULATED_PAGES_AMOUNT("16"), MAXIMUM_AMOUNT("17"), AMOUNT_WITHOUT_RATES("18"), FOREIGN_LANGUAGE_RATE("19"),
        CALCULATED_FOREIGN_LANGUAGE_RATE("20"), URGENT_PERCENTAGE("21"), CALCULATED_URGENT_AMOUNT("22"), FINAL_AMOUNT("23"),
        TUITION_PAYMENT_PLAN("24"), TUITION_PAYMENT_PLAN_CONDITIONS("25"), TUITION_CALCULATION_TYPE("26"), FIXED_AMOUNT("27"),
        ECTS_CREDITS("28"), AMOUNT_PER_ECTS("29"), ENROLLED_COURSES("30"), AMOUNT_PER_COURSE("31"), DUE_DATE("32"), DEGREE("33"),
        DEGREE_CODE("34"), DEGREE_CURRICULAR_PLAN("35"), ENROLMENT("36"), FACTOR("37"), TOTAL_ECTS_OR_UNITS("38"),
        COURSE_FUNCTION_COST("39"), DEFAULT_TUITION_TOTAL_AMOUNT("40"), USED_DATE("41");

        private String code;

        private AcademicTreasuryEventKeys(final String code) {
            this.code = code;
        }

        public LocalizedString getDescriptionI18N() {
            return BundleUtil.getLocalizedString(Constants.BUNDLE, "label." + AcademicTreasuryEventKeys.class.getSimpleName()
                    + "." + name());
        }

        public static String valueFor(final DebitEntry debitEntry, final AcademicTreasuryEventKeys key) {
            if (debitEntry.getPropertiesMap() == null) {
                return null;
            }

            // HACK Should retrieve with code and not with the description
            final LocalizedString descriptionI18N = key.getDescriptionI18N();
            if (debitEntry.getPropertiesMap().containsKey(descriptionI18N.getContent(Constants.DEFAULT_LANGUAGE))) {
                return debitEntry.getPropertiesMap().get(descriptionI18N.getContent(Constants.DEFAULT_LANGUAGE));
            }

            return null;
        }
    }

    // @formatter:on

    private Map<String, String> fillPropertiesMap() {
        final Map<String, String> propertiesMap = Maps.newHashMap();

        //THIS IS WRONG!!! - Ricardo Pedro 5-9-2015
        // The properties MAP SHOULD BE FILLED WITH KEY:VALUE
        // Then in the JSP should be used the "getDescriptionI18N().getContent()" to show to the description

        if (isForAcademicServiceRequest()) {
            propertiesMap.put(AcademicTreasuryEventKeys.ACADEMIC_SERVICE_REQUEST_NAME.getDescriptionI18N().getContent(),
                    getITreasuryServiceRequest().getServiceRequestType().getName().getContent());

            propertiesMap.put(AcademicTreasuryEventKeys.ACADEMIC_SERVICE_REQUEST_NUMBER_YEAR.getDescriptionI18N().getContent(),
                    getITreasuryServiceRequest().getServiceRequestNumberYear());

            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(),
                    getITreasuryServiceRequest().getRegistration().getDegree()
                            .getPresentationNameI18N(getITreasuryServiceRequest().getExecutionYear()).getContent());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(),
                    getITreasuryServiceRequest().getRegistration().getDegree().getCode());

            if (getITreasuryServiceRequest().hasExecutionYear()) {
                propertiesMap.put(AcademicTreasuryEventKeys.EXECUTION_YEAR.getDescriptionI18N().getContent(),
                        getITreasuryServiceRequest().getExecutionYear().getQualifiedName());
            }

            propertiesMap.put(AcademicTreasuryEventKeys.DETAILED.getDescriptionI18N().getContent(),
                    booleanLabel(getITreasuryServiceRequest().isDetailed()).getContent());
            propertiesMap.put(AcademicTreasuryEventKeys.URGENT.getDescriptionI18N().getContent(),
                    booleanLabel(getITreasuryServiceRequest().isUrgent()).getContent());
            if (getITreasuryServiceRequest().hasLanguage()) {
                propertiesMap.put(AcademicTreasuryEventKeys.LANGUAGE.getDescriptionI18N().getContent(),
                        getITreasuryServiceRequest().getLanguage().getLanguage());
            }
        } else if (isForRegistrationTuition() || isForStandaloneTuition() || isForExtracurricularTuition()) {
            propertiesMap.put(AcademicTreasuryEventKeys.EXECUTION_YEAR.getDescriptionI18N().getContent(),
                    getExecutionYear().getQualifiedName());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(),
                    getRegistration().getDegree().getPresentationNameI18N(getExecutionYear()).getContent());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CURRICULAR_PLAN.getDescriptionI18N().getContent(),
                    getRegistration().getDegreeCurricularPlanName());
            propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(),
                    getRegistration().getDegree().getCode());
        }

        return propertiesMap;
    }

    private LocalizedString booleanLabel(final boolean detailed) {
        return BundleUtil.getLocalizedString(Constants.BUNDLE, detailed ? "label.true" : "label.false");
    }

    public BigDecimal getEnrolledEctsUnits() {
        if (getTuitionPaymentPlanGroup().isForRegistration()) {
            final Set<Enrolment> normalEnrolments = Sets.newHashSet(
                    getRegistration().getStudentCurricularPlan(getExecutionYear()).getRoot().getEnrolmentsBy(getExecutionYear()));

            normalEnrolments.removeAll(getRegistration().getStandaloneCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).collect(Collectors.toSet()));

            normalEnrolments.removeAll(getRegistration().getExtraCurricularCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).collect(Collectors.toSet()));

            return normalEnrolments.stream().map(e -> new BigDecimal(e.getEctsCredits())).reduce((a, b) -> a.add(b))
                    .orElse(BigDecimal.ZERO);

        } else if (getTuitionPaymentPlanGroup().isForStandalone()) {
            return getRegistration().getStandaloneCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear())
                    .map(e -> new BigDecimal(e.getEctsCredits())).reduce((a, c) -> a.add(c)).orElse(BigDecimal.ZERO);
        } else if (getTuitionPaymentPlanGroup().isForExtracurricular()) {
            return getRegistration().getExtraCurricularCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear())
                    .map(e -> new BigDecimal(e.getEctsCredits())).reduce((a, c) -> a.add(c)).orElse(BigDecimal.ZERO);
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.unknown.tuition.group");
    }

    public BigDecimal getEnrolledCoursesCount() {
        if (getTuitionPaymentPlanGroup().isForRegistration()) {
            return new BigDecimal(getRegistration().getEnrolments(getExecutionYear()).size());
        } else if (getTuitionPaymentPlanGroup().isForStandalone()) {
            return new BigDecimal(getRegistration().getStandaloneCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).count());
        } else if (getTuitionPaymentPlanGroup().isForExtracurricular()) {
            return new BigDecimal(getRegistration().getExtraCurricularCurriculumLines().stream()
                    .filter(l -> l.isEnrolment() && l.getExecutionYear() == getExecutionYear()).count());
        }

        throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.unknown.tuition.group");
    }

    public void updatePricingFields(final BigDecimal baseAmount, final BigDecimal amountForAdditionalUnits,
            final BigDecimal amountForPages, final BigDecimal maximumAmount, final BigDecimal amountForLanguageTranslationRate,
            final BigDecimal amountForUrgencyRate) {

        super.setBaseAmount(baseAmount);
        super.setAmountForAdditionalUnits(amountForAdditionalUnits);
        super.setAmountForPages(amountForPages);
        super.setMaximumAmount(maximumAmount);
        super.setAmountForLanguageTranslationRate(amountForLanguageTranslationRate);
        super.setAmountForUrgencyRate(amountForUrgencyRate);
    }

    /* ----------------------
     * IAcademicTreasuryEvent
     * ----------------------
     */

    @Override
    public String getDebtAccountURL() {
        return DebtAccountController.READ_URL + getDebtAccount().getExternalId();
    }

    /* -------------------------
     * KIND OF EVENT INFORMATION
     * -------------------------
     */

    @Override
    public boolean isTuitionEvent() {
        return isForRegistrationTuition() || isForStandaloneTuition() || isForExtracurricularTuition();
    }

    @Override
    public boolean isAcademicServiceRequestEvent() {
        return isForAcademicServiceRequest();
    }

    @Override
    public boolean isAcademicTax() {
        return isForAcademicTax();
    }

    @Override
    public boolean isImprovementTax() {
        return isForImprovementTax();
    }

    /* ---------------------
     * FINANTIAL INFORMATION
     * ---------------------
     */

    @Override
    public boolean isWithDebitEntry() {
        return isChargedWithDebitEntry();
    }

    @Override
    public boolean isExempted() {
        return !getTreasuryExemptionsSet().isEmpty();
    }

    @Override
    public boolean isDueDateExpired(final LocalDate when) {
        return DebitEntry.findActive(this).map(l -> l.isDueDateExpired(when)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isBlockingAcademicalActs(final LocalDate when) {
        /* Iterate over active debit entries which 
         * are not marked with academicActBlockingSuspension
         * and ask if it is in debt
         */

        return DebitEntry.find(this).filter(l -> PersonCustomer.isDebitEntryBlockingAcademicalActs(l, when)).count() > 0;
    }

    @Override
    public LocalDate getDueDate() {
        return DebitEntry.findActive(this).sorted(DebitEntry.COMPARE_BY_DUE_DATE).map(l -> l.getDueDate()).findFirst()
                .orElse(null);
    }

    @Override
    public String getExemptionReason() {
        return String.join(", ", TreasuryExemption.find(this).map(l -> l.getReason()).collect(Collectors.toSet()));
    }

    @Override
    public List<IAcademicTreasuryEventPayment> getPaymentsList() {
        return DebitEntry.findActive(this).map(l -> l.getSettlementEntriesSet()).reduce((a, b) -> Sets.union(a, b))
                .orElse(Sets.newHashSet()).stream().filter(l -> l.getFinantialDocument().isClosed())
                .map(l -> new AcademicTreasuryEventPayment(l)).collect(Collectors.toList());
    }

    /* ---------------------------------------------
     * ACADEMIC SERVICE REQUEST EVENT & ACADEMIC TAX
     * ---------------------------------------------
     */

    @Override
    public BigDecimal getBaseAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.baseAmount.unavailable");
        }

        return super.getBaseAmount();
    }

    @Override
    public BigDecimal getAdditionalUnitsAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.additionalUnitsAmount.unavailable");
        }

        return super.getAmountForAdditionalUnits();
    }

    @Override
    public BigDecimal getMaximumAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.maximumAmount.unavailable");
        }

        return super.getMaximumAmount();
    }

    @Override
    public BigDecimal getPagesAmount() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.pagesAmount.unavailable");
        }

        return super.getAmountForPages();
    }

    @Override
    public BigDecimal getAmountForLanguageTranslationRate() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.amountForLanguageTranslationRate.unavailable");
        }

        return super.getAmountForLanguageTranslationRate();
    }

    @Override
    public BigDecimal getAmountForUrgencyRate() {
        if (!isChargedWithDebitEntry()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.amountForUrgencyRate.unavailable");
        }

        return super.getAmountForUrgencyRate();
    }

    /* -------------------
     * TUITION INFORMATION
     * -------------------
     */

    @Override
    public int getTuitionInstallmentSize() {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return orderedTuitionDebitEntriesList().size();
    }

    @Override
    public BigDecimal getTuitionInstallmentAmountToPay(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return orderedTuitionDebitEntriesList().get(installmentOrder).getOpenAmount();
    }

    @Override
    public BigDecimal getTuitionInstallmentRemainingAmountToPay(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return orderedTuitionDebitEntriesList().get(installmentOrder).getOpenAmount();
    }

    @Override
    public BigDecimal getTuitionInstallmentExemptedAmount(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);

        BigDecimal result = debitEntry.getExemptedAmount();
        result = result.add(debitEntry.getCreditEntriesSet().stream().filter(l -> l.isFromExemption())
                .map(l -> l.getAmountWithVat()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO));

        return result;
    }

    @Override
    public LocalDate getTuitionInstallmentDueDate(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        return debitEntry.getDueDate();
    }

    @Override
    public String getTuitionInstallmentDescription(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        return debitEntry.getDescription();
    }

    @Override
    public boolean isTuitionInstallmentExempted(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        return TreasuryExemption.findUnique(this, debitEntry.getProduct()).isPresent();
    }

    @Override
    public String getTuitionInstallmentExemptionReason(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);
        if (!TreasuryExemption.findUnique(this, debitEntry.getProduct()).isPresent()) {
            return null;
        }

        return TreasuryExemption.findUnique(this, debitEntry.getProduct()).get().getReason();
    }

    @Override
    public List<IAcademicTreasuryEventPayment> getTuitionInstallmentPaymentsList(int installmentOrder) {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        final DebitEntry debitEntry = orderedTuitionDebitEntriesList().get(installmentOrder);

        return debitEntry.getSettlementEntriesSet().stream().map(l -> new AcademicTreasuryEventPayment(l))
                .collect(Collectors.toList());
    }

    /*
     * -----------
     * IMPROVEMENT
     * -----------
     */

    @Override
    public boolean isWithDebitEntry(final EnrolmentEvaluation enrolmentEvaluation) {
        return isChargedWithDebitEntry(enrolmentEvaluation);
    }

    @Override
    public boolean isExempted(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return false;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();

        return TreasuryExemption.findUnique(this, debitEntry.getProduct()).isPresent();
    }

    @Override
    public boolean isDueDateExpired(final EnrolmentEvaluation enrolmentEvaluation, final LocalDate when) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return false;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.isDueDateExpired(when);
    }

    @Override
    public boolean isBlockingAcademicalActs(final EnrolmentEvaluation enrolmentEvaluation, final LocalDate when) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return false;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.isInDebt() && debitEntry.isDueDateExpired(when);
    }

    @Override
    public BigDecimal getAmountToPay(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return BigDecimal.ZERO;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getAmount();
    }

    @Override
    public BigDecimal getRemainingAmountToPay(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).isPresent()) {
            return BigDecimal.ZERO;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getOpenAmount();
    }

    @Override
    public BigDecimal getExemptedAmount(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isExempted(enrolmentEvaluation)) {
            return BigDecimal.ZERO;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getOpenAmount();
    }

    @Override
    public LocalDate getDueDate(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isWithDebitEntry(enrolmentEvaluation)) {
            return null;
        }

        final DebitEntry debitEntry = findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get();
        return debitEntry.getDueDate();
    }

    @Override
    public String getExemptionReason(final EnrolmentEvaluation enrolmentEvaluation) {
        return getExemptionReason();
    }

    @Override
    public List<IAcademicTreasuryEventPayment> getPaymentsList(final EnrolmentEvaluation enrolmentEvaluation) {
        if (!isWithDebitEntry(enrolmentEvaluation)) {
            return Collections.emptyList();
        }

        return findActiveEnrolmentEvaluationDebitEntry(enrolmentEvaluation).get().getSettlementEntriesSet().stream()
                .filter(l -> l.getFinantialDocument().isClosed()).map(l -> new AcademicTreasuryEventPayment(l))
                .collect(Collectors.toList());
    }

    @Override
    public String formatMoney(BigDecimal moneyValue) {
        return getDebtAccount().getFinantialInstitution().getCurrency().getValueFor(moneyValue);
    }

    private static class PaymentReferenceCodeImpl implements IPaymentReferenceCode {

        private PaymentReferenceCode paymentReferenceCode;

        private PaymentReferenceCodeImpl(final PaymentReferenceCode referenceCode) {
            this.paymentReferenceCode = referenceCode;

        }

        @Override
        public LocalDate getEndDate() {
            return paymentReferenceCode.getEndDate();
        }

        @Override
        public String getEntityCode() {
            return paymentReferenceCode.getPaymentCodePool().getEntityReferenceCode();
        }

        @Override
        public String getFormattedCode() {
            return paymentReferenceCode.getFormattedCode();
        }

        @Override
        public String getReferenceCode() {
            return paymentReferenceCode.getReferenceCode();
        }

        @Override
        public boolean isAnnuled() {
            return paymentReferenceCode.getState().isAnnuled();
        }

        @Override
        public boolean isUsed() {
            return paymentReferenceCode.getState().isUsed();
        }

        @Override
        public boolean isProcessed() {
            return paymentReferenceCode.getState().isProcessed();
        }

    }

    @Override
    public List<IPaymentReferenceCode> getPaymentReferenceCodesList() {
        return DebitEntry.findActive(this).flatMap(d -> d.getPaymentCodesSet().stream()).map(t -> t.getPaymentReferenceCode())
                .map(p -> new PaymentReferenceCodeImpl(p)).collect(Collectors.toList());
    }

    /*
     * This is used only for methods above
     */
    protected List<DebitEntry> orderedTuitionDebitEntriesList() {
        if (!isForRegistrationTuition()) {
            throw new AcademicTreasuryDomainException("error.AcademicTreasuryEvent.only.tuition.for.registration.supported");
        }

        return DebitEntry.findActive(this).sorted((a, b) -> a.getExternalId().compareTo(b.getExternalId()))
                .collect(Collectors.<DebitEntry> toList());
    }

    @Override
    public String getERPIntegrationMetadata() {
        String degreeCode = "";
        String executionYear = "";
//HACK: This should be done using GJSON
        if (this.getDegree() != null) {
            degreeCode = this.getDegree().getCode();
        } else {
            if (this.getPropertiesMap().containsKey(AcademicTreasuryEventKeys.DEGREE_CODE)) {
                degreeCode = this.getPropertiesMap().get(AcademicTreasuryEventKeys.DEGREE_CODE);
            }
        }
        if (this.getExecutionYear() != null) {
            executionYear = this.getExecutionYear().getQualifiedName();
        } else {
            if (this.getPropertiesMap().containsKey(AcademicTreasuryEventKeys.EXECUTION_YEAR)) {
                executionYear = this.getPropertiesMap().get(AcademicTreasuryEventKeys.EXECUTION_YEAR);
            }
        }
        return "{\"" + AcademicTreasuryEventKeys.DEGREE_CODE + "\":\"" + degreeCode + "\",\""
                + AcademicTreasuryEventKeys.EXECUTION_YEAR + "\":\"" + executionYear + "\"}";
    }

    /**
     * You should use the abstraction. API setITreasuryServiceRequest
     */
    @Deprecated
    @Override
    public void setAcademicServiceRequest(AcademicServiceRequest academicServiceRequest) {
        super.setAcademicServiceRequest(academicServiceRequest);
    }

    public void setITreasuryServiceRequest(ITreasuryServiceRequest iTreasuryServiceRequest) {
        super.setAcademicServiceRequest((AcademicServiceRequest) iTreasuryServiceRequest);
    }

    /**
     * You should use the abstraction. API getITreasuryServiceRequest
     */
    @Deprecated
    @Override
    public AcademicServiceRequest getAcademicServiceRequest() {
        return super.getAcademicServiceRequest();
    }

    public ITreasuryServiceRequest getITreasuryServiceRequest() {
        return (ITreasuryServiceRequest) super.getAcademicServiceRequest();
    }

}
