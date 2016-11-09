package org.fenixedu.academictreasury.dto.tariff;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CurricularYear;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.StatuteType;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.domain.tuition.EctsCalculationType;
import org.fenixedu.academictreasury.domain.tuition.TuitionCalculationType;
import org.fenixedu.academictreasury.domain.tuition.TuitionInstallmentTariff;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlan;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.tariff.DueDateCalculationType;
import org.fenixedu.treasury.domain.tariff.InterestType;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

public class TuitionPaymentPlanBean implements Serializable, IBean {

    private static final long serialVersionUID = 1L;

    private FinantialEntity finantialEntity;

    private Product product;
    private TuitionPaymentPlanGroup tuitionPaymentPlanGroup;
    private ExecutionYear executionYear;

    private DegreeType degreeType;
    private Set<DegreeCurricularPlan> degreeCurricularPlans = Sets.newHashSet();

    private boolean defaultPaymentPlan;
    private RegistrationRegimeType registrationRegimeType;
    private RegistrationProtocol registrationProtocol;
    private IngressionType ingression;
    private CurricularYear curricularYear;
    private ExecutionSemester executionSemester;
    private boolean firstTimeStudent;
    private boolean customized;
    private StatuteType statuteType;
    private DebtAccount payorDebtAccount;

    // TODO: Anil Use LocalizedString when web component is compatible with AngularJS
    private String name;
    private boolean withLaboratorialClasses;

    private List<TupleDataSourceBean> executionYearDataSource = null;
    private List<TupleDataSourceBean> degreeTypeDataSource = null;
    private List<TupleDataSourceBean> degreeCurricularPlanDataSource = null;
    private List<TupleDataSourceBean> registrationRegimeTypeDataSource = null;
    private List<TupleDataSourceBean> registrationProtocolDataSource = null;
    private List<TupleDataSourceBean> ingressionDataSource = null;
    private List<TupleDataSourceBean> curricularYearDataSource = null;
    private List<TupleDataSourceBean> semesterDataSource = null;
    private List<TupleDataSourceBean> statuteTypeDataSource = null;
    private List<TupleDataSourceBean> payorDebtAccountDataSource = null;

    private List<TupleDataSourceBean> tuitionCalculationTypeDataSource = null;
    private List<TupleDataSourceBean> ectsCalculationTypeDataSource = null;
    private List<TupleDataSourceBean> interestTypeDataSource = null;
    private List<TupleDataSourceBean> dueDateCalculationTypeDataSource = null;
    private List<TupleDataSourceBean> tuitionInstallmentProductDataSource = null;

    public List<AcademicTariffBean> tuitionInstallmentBeans = Lists.newArrayList();

    // @formatter:off
    /*--------------
     * TARIFF FIELDS
     * -------------
     */
    // @formatter:on

    /* Tariff */
    private LocalDate beginDate = new LocalDate();
    private LocalDate endDate = Constants.INFINITY_DATE.toLocalDate();
    private DueDateCalculationType dueDateCalculationType;
    private LocalDate fixedDueDate = new LocalDate();
    private int numberOfDaysAfterCreationForDueDate;

    /* InterestRate */
    private boolean applyInterests;
    private InterestType interestType;
    private int numberOfDaysAfterDueDate;
    private boolean applyInFirstWorkday;
    private int maximumDaysToApplyPenalty;
    private int maximumMonthsToApplyPenalty;
    private BigDecimal interestFixedAmount;
    private BigDecimal rate;

    /* TuitionInstallment */
    private Product tuitionInstallmentProduct;
    private int installmentOrder;
    private TuitionCalculationType tuitionCalculationType;
    private BigDecimal fixedAmount;
    private EctsCalculationType ectsCalculationType;
    private BigDecimal factor;
    private BigDecimal totalEctsOrUnits;
    private boolean applyMaximumAmount;
    private BigDecimal maximumAmount;
    private boolean academicalActBlockingOn;
    private boolean blockAcademicActsOnDebt;

    // @formatter:off
    /*---------------------
     * END OF TARIFF FIELDS
     * --------------------
     */
    // @formatter:on

    // To be used on copy tuition payment plan
    private ExecutionYear copiedExecutionYear;

    // Named in tuition importation
    private String sheetName;

    public TuitionPaymentPlanBean(final Product product, final TuitionPaymentPlanGroup tuitionPaymentPlanGroup,
            final FinantialEntity finantialEntity, final ExecutionYear executionYear) {
        this.product = product;
        this.tuitionPaymentPlanGroup = tuitionPaymentPlanGroup;
        this.finantialEntity = finantialEntity;
        this.executionYear = executionYear;

        updateData();
        resetInstallmentFields();
    }

    public TuitionPaymentPlanBean(TuitionPaymentPlan tuitionPaymentPlan) {
        this(tuitionPaymentPlan.getProduct(), tuitionPaymentPlan.getTuitionPaymentPlanGroup(),
                tuitionPaymentPlan.getFinantialEntity(), tuitionPaymentPlan.getExecutionYear());

        this.copiedExecutionYear = tuitionPaymentPlan.getExecutionYear();

        this.curricularYear = tuitionPaymentPlan.getCurricularYear();
        this.customized = tuitionPaymentPlan.isCustomized();
        this.defaultPaymentPlan = tuitionPaymentPlan.isDefaultPaymentPlan();

        if (tuitionPaymentPlan.getSemester() != null) {
            this.executionSemester = getExecutionYear().getExecutionSemesterFor(tuitionPaymentPlan.getSemester());
        }

        this.firstTimeStudent = tuitionPaymentPlan.isFirstTimeStudent();
        this.ingression = tuitionPaymentPlan.getIngression();
        this.name = tuitionPaymentPlan.getCustomizedName().getContent();
        this.registrationProtocol = tuitionPaymentPlan.getRegistrationProtocol();
        this.registrationRegimeType = tuitionPaymentPlan.getRegistrationRegimeType();
        this.statuteType = tuitionPaymentPlan.getStatuteType();
        this.withLaboratorialClasses = tuitionPaymentPlan.isWithLaboratorialClasses();

        fillWithInstallments(tuitionPaymentPlan);
    }

    private void fillWithInstallments(final TuitionPaymentPlan tuitionPaymentPlan) {
        for (final TuitionInstallmentTariff tuitionInstallmentTariff : tuitionPaymentPlan.getOrderedTuitionInstallmentTariffs()) {

            this.tuitionInstallmentProduct = tuitionInstallmentTariff.getProduct();
            this.beginDate = tuitionInstallmentTariff.getBeginDate().toLocalDate();
            this.dueDateCalculationType = tuitionInstallmentTariff.getDueDateCalculationType();
            this.fixedDueDate = tuitionInstallmentTariff.getFixedDueDate();
            this.numberOfDaysAfterCreationForDueDate = tuitionInstallmentTariff.getNumberOfDaysAfterCreationForDueDate();

            this.applyInterests = tuitionInstallmentTariff.getApplyInterests();
            if (this.applyInterests) {
                this.interestType = tuitionInstallmentTariff.getInterestRate().getInterestType();
                this.numberOfDaysAfterDueDate = tuitionInstallmentTariff.getNumberOfDaysAfterCreationForDueDate();
                this.applyInFirstWorkday = tuitionInstallmentTariff.getInterestRate().isApplyInFirstWorkday();
                this.maximumDaysToApplyPenalty = tuitionInstallmentTariff.getInterestRate().getMaximumDaysToApplyPenalty();
                this.maximumMonthsToApplyPenalty = tuitionInstallmentTariff.getInterestRate().getMaximumMonthsToApplyPenalty();
                this.interestFixedAmount = tuitionInstallmentTariff.getInterestRate().getInterestFixedAmount();
                this.rate = tuitionInstallmentTariff.getInterestRate().getRate();
            }

            this.tuitionCalculationType = tuitionInstallmentTariff.getTuitionCalculationType();
            this.fixedAmount = tuitionInstallmentTariff.getFixedAmount();
            this.ectsCalculationType = tuitionInstallmentTariff.getEctsCalculationType();
            this.factor = tuitionInstallmentTariff.getFactor();
            this.totalEctsOrUnits = tuitionInstallmentTariff.getTotalEctsOrUnits();
            this.applyMaximumAmount = tuitionInstallmentTariff.isApplyMaximumAmount();
            this.maximumAmount = tuitionInstallmentTariff.getMaximumAmount();
            this.academicalActBlockingOn = !tuitionInstallmentTariff.getAcademicalActBlockingOff();

            addInstallment();
        }
    }

    public void updateData() {
        this.degreeTypeDataSource = degreeTypeDataSource();
        this.degreeCurricularPlanDataSource = degreeCurricularPlanDataSource();

        this.degreeCurricularPlans.clear();

        this.registrationRegimeTypeDataSource = registrationRegimeTypeDataSource();
        this.registrationProtocolDataSource = registrationProtocolDataSource();
        this.ingressionDataSource = ingressionDataSource();
        this.curricularYearDataSource = curricularYearDataSource();
        this.semesterDataSource = semesterDataSource();

        this.tuitionCalculationTypeDataSource = tuitionCalculationTypeDataSource();

        this.ectsCalculationTypeDataSource = ectsCalculationTypeDataSource();

        this.interestTypeDataSource = interestTypeDataSource();

        this.dueDateCalculationTypeDataSource = dueDateCalculationTypeDataSource();

        this.tuitionInstallmentProductDataSource =
                tuitionInstallmentProductDataSource(getTuitionPaymentPlanGroup(), this.tuitionInstallmentBeans.size() + 1);

        this.statuteTypeDataSource = statuteTypeDataSource();

        this.payorDebtAccountDataSource = payorDebtAccountDataSource();

        this.executionYearDataSource = executionYearDataSource();

    }

    public void updateDatesBasedOnSelectedExecutionYear() {
        int executionYearInterval = executionYear.getBeginCivilYear() - copiedExecutionYear.getBeginCivilYear();

        for (final AcademicTariffBean academicTariffBean : tuitionInstallmentBeans) {
            academicTariffBean.setBeginDate(academicTariffBean.getBeginDate().plusYears(executionYearInterval));

            if (academicTariffBean.getFixedDueDate() != null) {
                academicTariffBean.setFixedDueDate(academicTariffBean.getFixedDueDate().plusYears(executionYearInterval));
            }
        }
    }

    public static List<TupleDataSourceBean> dueDateCalculationTypeDataSource() {
        return ((List<DueDateCalculationType>) Arrays.asList(DueDateCalculationType.values())).stream()
                .filter(t -> !t.isNoDueDate() && !t.isFixedDate())
                .map(t -> new TupleDataSourceBean(t.name(), t.getDescriptionI18N().getContent())).collect(Collectors.toList());
    }

    public List<String> addInstallment() {

        List<String> errorMessages = Lists.newArrayList();

        final AcademicTariffBean installmentBean = new AcademicTariffBean(tuitionInstallmentBeans.size() + 1);

        if (this.tuitionInstallmentProduct == null) {
            errorMessages.add("error.TuitionPaymentPlan.tuitionInstallmentProduct.required");
        }

        if (this.tuitionCalculationType == null) {
            errorMessages.add("error.TuitionPaymentPlan.tuitionCalculationType.required");
        }

        if (this.tuitionCalculationType != null && this.tuitionCalculationType.isFixedAmount() && this.fixedAmount == null) {
            errorMessages.add("error.TuitionPaymentPlan.fixedAmount.required");
        }

        if (this.tuitionCalculationType != null && (this.tuitionCalculationType.isEcts() || this.tuitionCalculationType.isUnits())
                && this.ectsCalculationType == null) {
            errorMessages.add("error.TuitionPaymentPlan.ectsCalculationType.required");
        }

        if (this.tuitionCalculationType != null && (this.tuitionCalculationType.isEcts() || this.tuitionCalculationType.isUnits())
                && this.ectsCalculationType != null && this.ectsCalculationType.isFixedAmount() && this.fixedAmount == null) {
            errorMessages.add("error.TuitionPaymentPlan.fixedAmount.required");
        }

        if (this.tuitionCalculationType != null && (this.tuitionCalculationType.isEcts() || this.tuitionCalculationType.isUnits())
                && this.ectsCalculationType != null && this.ectsCalculationType.isDependentOnDefaultPaymentPlan()
                && this.factor == null) {
            errorMessages.add("error.TuitionPaymentPlan.factor.required");
        }

        if (this.tuitionCalculationType != null && (this.tuitionCalculationType.isEcts() || this.tuitionCalculationType.isUnits())
                && this.ectsCalculationType != null && this.ectsCalculationType.isDependentOnDefaultPaymentPlan()
                && this.totalEctsOrUnits == null) {
            errorMessages.add("error.TuitionPaymentPlan.totalEctsOrUnits.required");
        }

        if (this.applyMaximumAmount && (this.maximumAmount == null || !Constants.isPositive(this.maximumAmount))) {
            errorMessages.add("error.TuitionPaymentPlan.maximumAmount.required");
        }

        if (this.beginDate == null) {
            errorMessages.add("error.TuitionPaymentPlan.beginDate.required");
        }

        if (this.dueDateCalculationType == null) {
            errorMessages.add("error.TuitionPaymentPlan.dueDateCalculationType.required");
        }

        if (this.dueDateCalculationType != null && this.dueDateCalculationType.isFixedDate() && this.fixedDueDate == null) {
            errorMessages.add("error.TuitionPaymentPlan.fixedDueDate.required");
        }

        if (this.applyInterests && this.interestType == null) {
            errorMessages.add("error.TuitionPaymentPlan.interestType.required");
        }

        if (this.applyInterests && this.interestType != null && this.interestType.isFixedAmount()
                && this.interestFixedAmount == null) {
            errorMessages.add("error.TuitionPaymentPlan.interestFixedAmount.required");
        }

        if (this.applyInterests && this.interestType != null && (this.interestType.isDaily() || this.interestType.isMonthly())
                && this.rate == null) {
            errorMessages.add("error.TuitionPaymentPlan.interestRate.required");
        }

        if (getTuitionInstallmentBeans().stream().filter(l -> l.getTuitionInstallmentProduct() == getTuitionInstallmentProduct())
                .count() > 0) {
            errorMessages.add("error.TuitionPaymentPlan.installment.already.with.product");
        }

        if (getTuitionPaymentPlanGroup().isForRegistration()
                && (getTuitionCalculationType().isEcts() || getTuitionCalculationType().isUnits())
                && getEctsCalculationType().isDefaultPaymentPlanCourseFunctionCostIndexed()) {
            errorMessages.add(
                    "error.TuitionInstallmentTariff.defaultPaymentPlanCourseFunctionCostIndexed.not.supported.for.registrationTuition");
        }

        if (!errorMessages.isEmpty()) {
            return errorMessages;
        }

        installmentBean.setBeginDate(this.beginDate);
        installmentBean.setEndDate(this.endDate);
        installmentBean.setDueDateCalculationType(dueDateCalculationType);
        installmentBean.setFixedDueDate(this.fixedDueDate);
        installmentBean.setNumberOfDaysAfterCreationForDueDate(this.numberOfDaysAfterCreationForDueDate);

        installmentBean.setApplyInterests(this.applyInterests);
        installmentBean.setInterestType(this.interestType);
        installmentBean.setNumberOfDaysAfterDueDate(this.numberOfDaysAfterDueDate);
        installmentBean.setApplyInFirstWorkday(this.applyInFirstWorkday);
        installmentBean.setMaximumDaysToApplyPenalty(this.maximumDaysToApplyPenalty);
        installmentBean.setMaximumMonthsToApplyPenalty(this.maximumMonthsToApplyPenalty);
        installmentBean.setInterestFixedAmount(this.interestFixedAmount);
        installmentBean.setRate(this.rate);

        installmentBean.setTuitionInstallmentProduct(getTuitionInstallmentProduct());
        installmentBean.setTuitionCalculationType(this.tuitionCalculationType);
        installmentBean.setFixedAmount(this.fixedAmount);
        installmentBean.setEctsCalculationType(this.ectsCalculationType);
        installmentBean.setFactor(this.factor);
        installmentBean.setTotalEctsOrUnits(this.totalEctsOrUnits);
        installmentBean.setApplyMaximumAmount(this.applyMaximumAmount);
        installmentBean.setMaximumAmount(this.maximumAmount);
        installmentBean.setAcademicalActBlockingOn(this.academicalActBlockingOn);
        installmentBean.setBlockAcademicActsOnDebt(this.blockAcademicActsOnDebt);

        this.tuitionInstallmentBeans.add(installmentBean);

        this.tuitionInstallmentProductDataSource =
                tuitionInstallmentProductDataSource(getTuitionPaymentPlanGroup(), this.tuitionInstallmentBeans.size() + 1);

        return errorMessages;
    }

    public void removeInstallment(final int installmentNumber) {
        if (findTariffBeanByInstallmentNumber(installmentNumber + 1) != null) {
            throw new AcademicTreasuryDomainException("error.TuitionPaymentPlan.delete.after.first");
        }

        AcademicTariffBean removeBean = findTariffBeanByInstallmentNumber(installmentNumber);

        if (removeBean != null) {
            getTuitionInstallmentBeans().remove(removeBean);

            int i = 1;
            for (AcademicTariffBean academicTariffBean : getTuitionInstallmentBeans()) {
                academicTariffBean.setInstallmentOrder(i++);
            }
        }

        this.tuitionInstallmentProductDataSource =
                tuitionInstallmentProductDataSource(getTuitionPaymentPlanGroup(), this.tuitionInstallmentBeans.size() + 1);
    }

    private AcademicTariffBean findTariffBeanByInstallmentNumber(int installmentNumber) {
        for (final AcademicTariffBean academicTariffBean : getTuitionInstallmentBeans()) {
            if (academicTariffBean.getInstallmentOrder() == installmentNumber) {
                return academicTariffBean;
            }
        }

        return null;
    }

    public void resetInstallmentFields() {
        this.beginDate = this.executionYear.getBeginLocalDate();
        this.endDate = Constants.INFINITY_DATE.toLocalDate();
        this.dueDateCalculationType = DueDateCalculationType.DAYS_AFTER_CREATION;
        this.fixedDueDate = this.executionYear.getBeginLocalDate();
        this.numberOfDaysAfterCreationForDueDate = 0;

        this.applyInterests = true;
        this.interestType = InterestType.GLOBAL_RATE;
        this.numberOfDaysAfterDueDate = 1;
        this.applyInFirstWorkday = false;
        this.maximumDaysToApplyPenalty = 0;
        this.maximumMonthsToApplyPenalty = 0;
        this.interestFixedAmount = null;
        this.rate = null;

        this.tuitionInstallmentProduct = null;
        this.tuitionCalculationType = TuitionCalculationType.FIXED_AMOUNT;
        this.fixedAmount = null;
        this.ectsCalculationType = EctsCalculationType.FIXED_AMOUNT;
        this.factor = null;
        this.totalEctsOrUnits = null;
        this.applyMaximumAmount = false;
        this.maximumAmount = null;
        this.academicalActBlockingOn = true;
        this.blockAcademicActsOnDebt = false;

        if (tuitionPaymentPlanGroup.isForExtracurricular() || tuitionPaymentPlanGroup.isForStandalone()) {
            setTuitionInstallmentProduct(tuitionPaymentPlanGroup.getCurrentProduct());
        }
    }

    public FinantialEntity getFinantialEntity() {
        return finantialEntity;
    }

    public void setFinantialEntity(FinantialEntity finantialEntity) {
        this.finantialEntity = finantialEntity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public TuitionPaymentPlanGroup getTuitionPaymentPlanGroup() {
        return tuitionPaymentPlanGroup;
    }

    public void setTuitionPaymentPlanGroup(TuitionPaymentPlanGroup tuitionPaymentPlanGroup) {
        this.tuitionPaymentPlanGroup = tuitionPaymentPlanGroup;
    }

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public DegreeType getDegreeType() {
        return degreeType;
    }

    public void setDegreeType(DegreeType degreeType) {
        this.degreeType = degreeType;
    }

    public Set<DegreeCurricularPlan> getDegreeCurricularPlans() {
        return degreeCurricularPlans;
    }

    public void setDegreeCurricularPlans(Set<DegreeCurricularPlan> degreeCurricularPlans) {
        this.degreeCurricularPlans = degreeCurricularPlans;
    }

    public boolean isDefaultPaymentPlan() {
        return defaultPaymentPlan;
    }

    public void setDefaultPaymentPlan(boolean defaultPaymentPlan) {
        this.defaultPaymentPlan = defaultPaymentPlan;
    }

    public RegistrationRegimeType getRegistrationRegimeType() {
        return registrationRegimeType;
    }

    public void setRegistrationRegimeType(RegistrationRegimeType registrationRegimeType) {
        this.registrationRegimeType = registrationRegimeType;
    }

    public RegistrationProtocol getRegistrationProtocol() {
        return registrationProtocol;
    }

    public void setRegistrationProtocol(RegistrationProtocol registrationProtocol) {
        this.registrationProtocol = registrationProtocol;
    }

    public IngressionType getIngression() {
        return ingression;
    }

    public void setIngression(IngressionType ingression) {
        this.ingression = ingression;
    }

    public CurricularYear getCurricularYear() {
        return curricularYear;
    }

    public void setCurricularYear(CurricularYear curricularYear) {
        this.curricularYear = curricularYear;
    }

    public ExecutionSemester getExecutionSemester() {
        return executionSemester;
    }

    public void setExecutionSemester(ExecutionSemester executionSemester) {
        this.executionSemester = executionSemester;
    }

    public boolean isFirstTimeStudent() {
        return firstTimeStudent;
    }

    public void setFirstTimeStudent(boolean firstTimeStudent) {
        this.firstTimeStudent = firstTimeStudent;
    }

    public boolean isCustomized() {
        return customized;
    }

    public void setCustomized(boolean customized) {
        this.customized = customized;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWithLaboratorialClasses() {
        return withLaboratorialClasses;
    }

    public void setWithLaboratorialClasses(boolean withLaboratorialClasses) {
        this.withLaboratorialClasses = withLaboratorialClasses;
    }

    public List<AcademicTariffBean> getTuitionInstallmentBeans() {
        return tuitionInstallmentBeans;
    }

    public void setTuitionInstallmentBeans(final List<AcademicTariffBean> tuitionInstallmentBeans) {
        this.tuitionInstallmentBeans = tuitionInstallmentBeans;
    }

    public List<TupleDataSourceBean> getDegreeTypeDataSource() {
        return degreeTypeDataSource;
    }

    public List<TupleDataSourceBean> getDegreeCurricularPlanDataSource() {
        return degreeCurricularPlanDataSource;
    }

    public List<TupleDataSourceBean> getRegistrationRegimeTypeDataSource() {
        return registrationRegimeTypeDataSource;
    }

    public List<TupleDataSourceBean> getRegistrationProtocolDataSource() {
        return registrationProtocolDataSource;
    }

    public List<TupleDataSourceBean> getIngressionDataSource() {
        return ingressionDataSource;
    }

    public List<TupleDataSourceBean> getCurricularYearDataSource() {
        return curricularYearDataSource;
    }

    public List<TupleDataSourceBean> getSemesterDataSource() {
        return semesterDataSource;
    }

    public List<TupleDataSourceBean> getTuitionCalculationTypeDataSource() {
        return tuitionCalculationTypeDataSource;
    }

    public List<TupleDataSourceBean> getEctsCalculationTypeDataSource() {
        return ectsCalculationTypeDataSource;
    }

    public List<TupleDataSourceBean> getInterestTypeDataSource() {
        return interestTypeDataSource;
    }

    public List<TupleDataSourceBean> getTuitionInstallmentProductDataSource() {
        return tuitionInstallmentProductDataSource;
    }

    public DebtAccount getPayorDebtAccount() {
        return payorDebtAccount;
    }

    public void setPayorDebtAccount(DebtAccount payorDebtAccount) {
        this.payorDebtAccount = payorDebtAccount;
    }

    /*
     * GETTERS & SETTERS
     */

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public DueDateCalculationType getDueDateCalculationType() {
        return dueDateCalculationType;
    }

    public void setDueDateCalculationType(DueDateCalculationType dueDateCalculationType) {
        this.dueDateCalculationType = dueDateCalculationType;
    }

    public LocalDate getFixedDueDate() {
        return fixedDueDate;
    }

    public void setFixedDueDate(LocalDate fixedDueDate) {
        this.fixedDueDate = fixedDueDate;
    }

    public int getNumberOfDaysAfterCreationForDueDate() {
        return numberOfDaysAfterCreationForDueDate;
    }

    public void setNumberOfDaysAfterCreationForDueDate(int numberOfDaysAfterCreationForDueDate) {
        this.numberOfDaysAfterCreationForDueDate = numberOfDaysAfterCreationForDueDate;
    }

    public boolean isApplyInterests() {
        return applyInterests;
    }

    public void setApplyInterests(boolean applyInterests) {
        this.applyInterests = applyInterests;
    }

    public boolean isApplyInFirstWorkday() {
        return applyInFirstWorkday;
    }

    public void setApplyInFirstWorkday(boolean applyInFirstWorkday) {
        this.applyInFirstWorkday = applyInFirstWorkday;
    }

    /* InterestRate */

    public InterestType getInterestType() {
        return interestType;
    }

    public void setInterestType(InterestType interestType) {
        this.interestType = interestType;
    }

    public int getNumberOfDaysAfterDueDate() {
        return numberOfDaysAfterDueDate;
    }

    public void setNumberOfDaysAfterDueDate(int numberOfDaysAfterDueDate) {
        this.numberOfDaysAfterDueDate = numberOfDaysAfterDueDate;
    }

    public int getMaximumDaysToApplyPenalty() {
        return maximumDaysToApplyPenalty;
    }

    public void setMaximumDaysToApplyPenalty(int maximumDaysToApplyPenalty) {
        this.maximumDaysToApplyPenalty = maximumDaysToApplyPenalty;
    }

    public int getMaximumMonthsToApplyPenalty() {
        return maximumMonthsToApplyPenalty;
    }

    public void setMaximumMonthsToApplyPenalty(int maximumMonthsToApplyPenalty) {
        this.maximumMonthsToApplyPenalty = maximumMonthsToApplyPenalty;
    }

    public BigDecimal getInterestFixedAmount() {
        return interestFixedAmount;
    }

    public void setInterestFixedAmount(BigDecimal interestFixedAmount) {
        this.interestFixedAmount = interestFixedAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    /* TuitionInstallmentTariff */

    public Product getTuitionInstallmentProduct() {
        return tuitionInstallmentProduct;
    }

    public void setTuitionInstallmentProduct(Product tuitionInstallmentProduct) {
        this.tuitionInstallmentProduct = tuitionInstallmentProduct;
    }

    public int getInstallmentOrder() {
        return installmentOrder;
    }

    public void setInstallmentOrder(int installmentOrder) {
        this.installmentOrder = installmentOrder;
    }

    public TuitionCalculationType getTuitionCalculationType() {
        return tuitionCalculationType;
    }

    public void setTuitionCalculationType(TuitionCalculationType tuitionCalculationType) {
        this.tuitionCalculationType = tuitionCalculationType;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public EctsCalculationType getEctsCalculationType() {
        return ectsCalculationType;
    }

    public void setEctsCalculationType(EctsCalculationType ectsCalculationType) {
        this.ectsCalculationType = ectsCalculationType;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }

    public BigDecimal getTotalEctsOrUnits() {
        return totalEctsOrUnits;
    }

    public void setTotalEctsOrUnits(BigDecimal totalEctsOrUnits) {
        this.totalEctsOrUnits = totalEctsOrUnits;
    }

    public boolean isAcademicalActBlockingOn() {
        return academicalActBlockingOn;
    }

    public void setAcademicalActBlockingOn(boolean academicalActBlockingOn) {
        this.academicalActBlockingOn = academicalActBlockingOn;
    }

    public boolean isBlockAcademicActsOnDebt() {
        return blockAcademicActsOnDebt;
    }

    public void setBlockAcademicActsOnDebt(boolean blockAcademicActsOnDebt) {
        this.blockAcademicActsOnDebt = blockAcademicActsOnDebt;
    }

    public StatuteType getStatuteType() {
        return statuteType;
    }

    public void setStatuteType(StatuteType statuteType) {
        this.statuteType = statuteType;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public boolean isApplyMaximumAmount() {
        return applyMaximumAmount;
    }

    public void setApplyMaximumAmount(boolean applyMaximumAmount) {
        this.applyMaximumAmount = applyMaximumAmount;
    }

    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(BigDecimal maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    /*
     * -------------
     * Other Methods
     * -------------
     */

    public static final Comparator<TupleDataSourceBean> COMPARE_BY_ID_AND_TEXT = new Comparator<TupleDataSourceBean>() {

        @Override
        public int compare(final TupleDataSourceBean o1, final TupleDataSourceBean o2) {
            if (o1.getId() == "") {
                return -1;
            } else if (o2.getId() == "") {
                return 1;
            }

            return TupleDataSourceBean.COMPARE_BY_TEXT.compare(o1, o2);
        }
    };

    private List<TupleDataSourceBean> degreeTypeDataSource() {
        final List<TupleDataSourceBean> result = Lists
                .newArrayList(DegreeType.all().map((dt) -> new TupleDataSourceBean(dt.getExternalId(), dt.getName().getContent()))
                        .collect(Collectors.toList()));

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> degreeCurricularPlanDataSource() {
        if (getExecutionYear() == null) {
            return Collections.<TupleDataSourceBean> emptyList();
        }

        if (getDegreeType() == null) {
            return Collections.<TupleDataSourceBean> emptyList();
        }

        final List<TupleDataSourceBean> result =
                ExecutionDegree.getAllByExecutionYearAndDegreeType(getExecutionYear(), getDegreeType()).stream()
                        .map(e -> e.getDegreeCurricularPlan())
                        .map((dcp) -> new TupleDataSourceBean(dcp.getExternalId(),
                                "[" + dcp.getDegree().getCode() + "] " + dcp.getPresentationName(getExecutionYear())))
                .collect(Collectors.toList());

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> semesterDataSource() {
        final List<TupleDataSourceBean> result = getExecutionYear().getExecutionPeriodsSet().stream()
                .map((cs) -> new TupleDataSourceBean(cs.getExternalId(), cs.getName())).collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> curricularYearDataSource() {
        final List<TupleDataSourceBean> result = Bennu.getInstance().getCurricularYearsSet().stream()
                .map((cy) -> new TupleDataSourceBean(cy.getExternalId(), cy.getYear().toString())).collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> ingressionDataSource() {
        final List<TupleDataSourceBean> result = Bennu.getInstance().getIngressionTypesSet().stream()
                .map((i) -> new TupleDataSourceBean(i.getExternalId(), i.getDescription().getContent()))
                .collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> registrationProtocolDataSource() {
        final List<TupleDataSourceBean> result = Bennu.getInstance().getRegistrationProtocolsSet().stream()
                .map((rp) -> new TupleDataSourceBean(rp.getExternalId(), rp.getDescription().getContent()))
                .collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> registrationRegimeTypeDataSource() {
        List<TupleDataSourceBean> result = ((List<RegistrationRegimeType>) Arrays.asList(RegistrationRegimeType.values()))
                .stream().map((t) -> new TupleDataSourceBean(t.name(), t.getLocalizedName())).collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    public static List<TupleDataSourceBean> interestTypeDataSource() {
        List<TupleDataSourceBean> result = InterestType.findAll().stream()
                .map((it) -> new TupleDataSourceBean(it.name(), it.getDescriptionI18N().getContent()))
                .collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    public static List<TupleDataSourceBean> ectsCalculationTypeDataSource() {
        List<TupleDataSourceBean> result = ((List<EctsCalculationType>) Arrays.asList(EctsCalculationType.values())).stream()
                .map((ct) -> new TupleDataSourceBean(ct.name(), ct.getDescriptionI18N().getContent()))
                .collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    public static List<TupleDataSourceBean> tuitionCalculationTypeDataSource() {
        List<TupleDataSourceBean> result = ((List<TuitionCalculationType>) Arrays.asList(TuitionCalculationType.values()))
                .stream().map((ct) -> new TupleDataSourceBean(ct.name(), ct.getDescriptionI18N().getContent()))
                .collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    public static List<TupleDataSourceBean> tuitionInstallmentProductDataSource(
            final TuitionPaymentPlanGroup tuitionPaymentPlanGroup, int desiredTuitionInstallmentOrder) {
        List<TupleDataSourceBean> result = null;

        if (tuitionPaymentPlanGroup != null && tuitionPaymentPlanGroup.isForRegistration()) {
            result = AcademicTreasurySettings.getInstance().getTuitionProductGroup().getProductsSet().stream()
                    .filter(p -> p.isActive() && p.getTuitionInstallmentOrder() == desiredTuitionInstallmentOrder)
                    .map(p -> new TupleDataSourceBean(p.getExternalId(), p.getName().getContent())).collect(Collectors.toList());
        } else {
            result = AcademicTreasurySettings.getInstance().getTuitionProductGroup().getProductsSet().stream()
                    .filter(p -> p.isActive()).map(p -> new TupleDataSourceBean(p.getExternalId(), p.getName().getContent()))
                    .collect(Collectors.toList());
        }

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> statuteTypeDataSource() {
        final List<TupleDataSourceBean> result = Bennu.getInstance().getStatuteTypesSet().stream()
                .map(l -> new TupleDataSourceBean(l.getExternalId(), l.getName().getContent())).collect(Collectors.toList());

        result.add(Constants.SELECT_OPTION);

        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> payorDebtAccountDataSource() {
        final SortedSet<DebtAccount> payorDebtAccountsSet =
                DebtAccount.findAdhocDebtAccountsSortedByCustomerName(finantialEntity.getFinantialInstitution());

        final List<TupleDataSourceBean> result = payorDebtAccountsSet.stream().map(l -> new TupleDataSourceBean(l.getExternalId(),
                String.format("%s - %s", l.getCustomer().getFiscalNumber(), l.getCustomer()))).collect(Collectors.toList());
        
        result.add(Constants.SELECT_OPTION);
        
        return result.stream().sorted(COMPARE_BY_ID_AND_TEXT).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> executionYearDataSource() {
        final List<TupleDataSourceBean> result = ExecutionYear.readNotClosedExecutionYears().stream()
                .sorted(ExecutionYear.REVERSE_COMPARATOR_BY_YEAR).collect(Collectors.toList()).stream()
                .map(l -> new TupleDataSourceBean(l.getExternalId(), l.getQualifiedName())).collect(Collectors.toList());

        result.add(0, Constants.SELECT_OPTION);

        return result;
    }

    public List<String> validateStudentConditions() {
        List<String> result = Lists.newArrayList();
        if (!hasAtLeastOneConditionSpecified()) {
            result.add("error.TuitionPaymentPlan.specify.at.least.one.condition");
        }

        if (isCustomized() && (isDefaultPaymentPlan() || hasStudentConditionSelected())) {
            result.add("error.TuitionPaymentPlan.customized.plan.cannot.have.other.options");
        }

        if (isDefaultPaymentPlan() && hasStudentConditionSelected()) {
            result.add("error.TuitionPaymentPlan.default.payment.plan.cannot.have.other.options");
        }

        return result;
    }

    private boolean hasStudentConditionSelected() {
        return getRegistrationRegimeType() != null || getRegistrationProtocol() != null || getIngression() != null
                || getCurricularYear() != null || getExecutionSemester() != null || isFirstTimeStudent()
                || getStatuteType() != null;
    }

    private boolean hasAtLeastOneConditionSpecified() {
        boolean hasAtLeastOneCondition = false;

        hasAtLeastOneCondition |= isDefaultPaymentPlan();
        hasAtLeastOneCondition |= getRegistrationRegimeType() != null;
        hasAtLeastOneCondition |= getRegistrationProtocol() != null;
        hasAtLeastOneCondition |= getIngression() != null;
        hasAtLeastOneCondition |= getCurricularYear() != null;
        hasAtLeastOneCondition |= getStatuteType() != null;
        hasAtLeastOneCondition |= getExecutionSemester() != null;
        hasAtLeastOneCondition |= isFirstTimeStudent();
        hasAtLeastOneCondition |= isCustomized();
        hasAtLeastOneCondition |= isWithLaboratorialClasses();

        return hasAtLeastOneCondition;
    }

}
