package org.fenixedu.academictreasury.dto.academictax;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationDataByExecutionYear;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.treasury.dto.ITreasuryBean;
import org.fenixedu.treasury.dto.TreasuryTupleDataSourceBean;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;

public class AcademicTaxDebtCreationBean implements Serializable, ITreasuryBean {

    private static final long serialVersionUID = 1L;

    private LocalDate debtDate;
    private ExecutionYear executionYear;
    private Registration registration;
    private EnrolmentEvaluation improvementEvaluation;

    private DebtAccount debtAccount;

    private List<TreasuryTupleDataSourceBean> registrationDataSource;
    private List<TreasuryTupleDataSourceBean> executionYearDataSource;
    private List<TreasuryTupleDataSourceBean> academicTaxesDataSource;
    private List<TreasuryTupleDataSourceBean> improvementEnrolmentEvaluationsDataSource;

    private AcademicTax academicTax;

    private boolean improvementTaxSelected;

    private String errorMessage;

    public AcademicTaxDebtCreationBean(final DebtAccount debtAccount) {
        this.debtAccount = debtAccount;

        updateData();
    }

    @Atomic
    public void updateData() {
        if (executionYear != null && (registration == null || !possibleExecutionYears().contains(executionYear))) {
            executionYear = null;
            improvementEvaluation = null;
        }

        getAcademicTaxesDataSource();
        getRegistrationDataSource();
        getExecutionYearDataSource();
        getImprovementEnrolmentEvaluationsDataSource();

        isImprovementTaxSelected();

        if (registration != null && executionYear != null) {
            debtDate = RegistrationDataByExecutionYear.getOrCreateRegistrationDataByYear(registration, executionYear)
                    .getEnrolmentDate();

            if (debtDate == null) {
                debtDate = new LocalDate();
            }

        } else {
            debtDate = new LocalDate();
        }
        
        getErrorMessage();
    }

    public List<TreasuryTupleDataSourceBean> getExecutionYearDataSource() {
        executionYearDataSource = Lists.newArrayList();

        for (final ExecutionYear executionYear : possibleExecutionYears()) {
            final String id = executionYear.getExternalId();
            String text = executionYear.getQualifiedName();

            if (registration != null) {
                final Set<?> enrolments = isImprovementTax() ? TuitionServices.improvementEnrolments(registration,
                        executionYear) : TuitionServices.normalEnrolments(registration, executionYear);

                if (enrolments.size() == 1) {
                    text += " " + academicTreasuryBundle("label.AcademicTaxDebtCreationBean.enrolments.one");
                } else if (enrolments.size() > 1) {
                    text += " " + academicTreasuryBundle("label.AcademicTaxDebtCreationBean.enrolments", String.valueOf(enrolments.size()));
                }
            }

            executionYearDataSource.add(new TreasuryTupleDataSourceBean(id, text));
        }

        return executionYearDataSource;
    }

    private List<ExecutionYear> possibleExecutionYears() {
        if (academicTax == null || registration == null) {
            return Lists.newArrayList();
        }

        return Sets.newHashSet(ExecutionYear.readNotClosedExecutionYears()).stream()
                .sorted(ExecutionYear.REVERSE_COMPARATOR_BY_YEAR).collect(Collectors.toList());
    }

    public List<TreasuryTupleDataSourceBean> getRegistrationDataSource() {
        if (!isStudent()) {
            registrationDataSource = Lists.newArrayList();
            return registrationDataSource;
        }

        registrationDataSource =
                ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent().getRegistrationsSet().stream()
                        .map(r -> new TreasuryTupleDataSourceBean(r.getExternalId(),
                                r.getDegree().getPresentationNameI18N(getExecutionYear()).getContent()))
                .collect(Collectors.toList());

        return registrationDataSource;
    }

    public boolean isCharged() {
        if (academicTax == null) {
            return false;
        }
        
        if (registration == null || executionYear == null) {
            return false;
        }
        
        if(isImprovementTax() && this.improvementEvaluation == null) {
            return false;
        }
        
        if(isImprovementTax()) {
            return AcademicTaxServices.isImprovementAcademicTaxCharged(registration, executionYear, improvementEvaluation);
        } else {
            return AcademicTaxServices.isAcademicTaxCharged(registration, executionYear, academicTax);
        }
    }

    public String getErrorMessage() {
        errorMessage = "";

        if (academicTax == null) {
            errorMessage = academicTreasuryBundle("error.AcademicTaxDebtCreation.select.academic.tax");
            return errorMessage;
        }
        
        if (registration == null || executionYear == null) {
            errorMessage = academicTreasuryBundle("error.AcademicTaxDebtCreation.select.registration.and.execution.year");
            return errorMessage;
        }
        
        if(isImprovementTax() && this.improvementEvaluation == null) {
            errorMessage = academicTreasuryBundle("error.AcademicTaxDebtCreation.select.improvement.evaluation");
            return errorMessage;
        }
        
        if(isCharged()) {
            errorMessage = academicTreasuryBundle("error.AcademicTaxDebtCreation.academic.tax.already.charged");
            return errorMessage;
        }
            
        return errorMessage;
    }

    public List<TreasuryTupleDataSourceBean> getAcademicTaxesDataSource() {
        academicTaxesDataSource =
                AcademicTax.findAll().map(l -> new TreasuryTupleDataSourceBean(l.getExternalId(), l.getProduct().getName().getContent()))
                        .sorted(TreasuryTupleDataSourceBean.COMPARE_BY_TEXT).collect(Collectors.toList());

        return academicTaxesDataSource;
    }

    public List<TreasuryTupleDataSourceBean> getImprovementEnrolmentEvaluationsDataSource() {
        if (!isStudent()) {
            improvementEnrolmentEvaluationsDataSource = Lists.newArrayList();
            return improvementEnrolmentEvaluationsDataSource;
        }

        if (academicTax == null) {
            improvementEnrolmentEvaluationsDataSource = Lists.newArrayList();
            return improvementEnrolmentEvaluationsDataSource;
        }

        if (!academicTax.isImprovementTax()) {
            improvementEnrolmentEvaluationsDataSource = Lists.newArrayList();
            return improvementEnrolmentEvaluationsDataSource;
        }

        if (getRegistration() == null || getExecutionYear() == null) {
            improvementEnrolmentEvaluationsDataSource = Lists.newArrayList();
            return improvementEnrolmentEvaluationsDataSource;
        }

        improvementEnrolmentEvaluationsDataSource =
                TuitionServices.improvementEnrolments(getRegistration(), getExecutionYear()).stream()
                        .map(l -> new TreasuryTupleDataSourceBean(l.getExternalId(),
                                l.getEnrolment().getName().getContent() + " - " + l.getExecutionPeriod().getQualifiedName()))
                .collect(Collectors.toList()).stream().sorted(TreasuryTupleDataSourceBean.COMPARE_BY_TEXT).collect(Collectors.toList());

        return improvementEnrolmentEvaluationsDataSource;
    }

    public boolean isImprovementTax() {
        return this.academicTax != null && this.academicTax.isImprovementTax();
    }

    public boolean isStudent() {
        return debtAccount.getCustomer().isPersonCustomer()
                && ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent() != null;
    }

    public boolean isImprovementTaxSelected() {
        this.improvementTaxSelected = isImprovementTax();

        return this.improvementTaxSelected;
    }

    /* -----------------
     * GETTERS & SETTERS
     * -----------------
     */
    public LocalDate getDebtDate() {
        return debtDate;
    }

    public void setDebtDate(LocalDate debtDate) {
        this.debtDate = debtDate;
    }

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    public AcademicTax getAcademicTax() {
        return academicTax;
    }

    public void setAcademicTax(AcademicTax academicTax) {
        this.academicTax = academicTax;
    }

    public EnrolmentEvaluation getImprovementEvaluation() {
        return improvementEvaluation;
    }

    public void setImprovementEvaluation(EnrolmentEvaluation improvementEvaluation) {
        this.improvementEvaluation = improvementEvaluation;
    }

}
