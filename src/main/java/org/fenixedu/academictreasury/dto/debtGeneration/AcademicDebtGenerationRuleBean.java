package org.fenixedu.academictreasury.dto.debtGeneration;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleType;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;
import org.fenixedu.treasury.domain.settings.TreasurySettings;

import com.google.common.collect.Lists;

public class AcademicDebtGenerationRuleBean implements Serializable, IBean {

    private static final long serialVersionUID = 1L;

    public static class ProductEntry implements IBean, Serializable {

        private static final long serialVersionUID = 1L;

        private Product product;
        private boolean createDebt;
        private boolean toCreateAfterLastRegistrationStateDate;
        private boolean forceCreation;
        private boolean limitToRegisteredOnExecutionYear;

        public ProductEntry(Product product, boolean createDebt, boolean toCreateAfterLastRegistrationStateDate, boolean forceCreation, boolean limitToRegisteredOnExecutionYear) {
            this.product = product;
            this.createDebt = createDebt;
            this.toCreateAfterLastRegistrationStateDate = toCreateAfterLastRegistrationStateDate;
            this.forceCreation = forceCreation;
            this.limitToRegisteredOnExecutionYear = limitToRegisteredOnExecutionYear;
        }

        public Product getProduct() {
            return product;
        }

        public boolean isCreateDebt() {
            return createDebt;
        }
        
        public boolean isToCreateAfterLastRegistrationStateDate() {
            return toCreateAfterLastRegistrationStateDate;
        }
        
        public boolean isForceCreation() {
            return forceCreation;
        }
        
        public boolean isLimitToRegisteredOnExecutionYear() {
            return limitToRegisteredOnExecutionYear;
        }
        
    }

    private AcademicDebtGenerationRuleType type;
    
    private ExecutionYear executionYear;
    private boolean aggregateOnDebitNote;
    private boolean aggregateAllOrNothing;
    private boolean closeDebitNote;
    private boolean alignAllAcademicTaxesDebitToMaxDueDate;
    private boolean createPaymentReferenceCode;

    private List<ProductEntry> entries = Lists.newArrayList();
    
    private DegreeType degreeType;

    private List<DegreeCurricularPlan> degreeCurricularPlans = Lists.newArrayList();
    private List<DegreeCurricularPlan> degreeCurricularPlansToAdd = Lists.newArrayList();

    private Product product;
    private boolean createDebt;
    private boolean toCreateAfterLastRegistrationStateDate;
    private boolean forceCreation;
    private boolean limitToRegisteredOnExecutionYear;
    private PaymentCodePool paymentCodePool;

    private List<TupleDataSourceBean> executionYearDataSource = Lists.newArrayList();
    private List<TupleDataSourceBean> productDataSource = Lists.newArrayList();
    private List<TupleDataSourceBean> paymentCodePoolDataSource = Lists.newArrayList();

    private List<TupleDataSourceBean> degreeTypeDataSource = Lists.newArrayList();
    private List<TupleDataSourceBean> degreeCurricularPlanDataSource = Lists.newArrayList();

    public AcademicDebtGenerationRuleBean(final AcademicDebtGenerationRuleType type) {
        this.type = type;
        
        executionYearDataSource =
                ExecutionYear.readNotClosedExecutionYears().stream()
                        .sorted(Collections.reverseOrder(ExecutionYear.COMPARATOR_BY_BEGIN_DATE))
                        .map(l -> new TupleDataSourceBean(l.getExternalId(), l.getQualifiedName())).collect(Collectors.toList());

        final List<Product> availableProducts = Lists.newArrayList();

        availableProducts.addAll(AcademicTax.findAll().filter(AcademicTax::isAppliedAutomatically).map(l -> l.getProduct())
                .collect(Collectors.toList()));
        availableProducts.addAll(AcademicTreasurySettings.getInstance().getTuitionProductGroup().getProductsSet());
        availableProducts.add(TreasurySettings.getInstance().getInterestProduct());

        productDataSource =
                availableProducts.stream().sorted(Product.COMPARE_BY_NAME)
                        .map(l -> new TupleDataSourceBean(l.getExternalId(), l.getName().getContent()))
                        .collect(Collectors.toList());

        paymentCodePoolDataSource =
                PaymentCodePool.findAll().filter(PaymentCodePool::getActive)
                        .map(l -> new TupleDataSourceBean(l.getExternalId(), l.getName())).collect(Collectors.toList());

        degreeTypeDataSource =
                DegreeType.all().map(l -> new TupleDataSourceBean(l.getExternalId(), l.getName().getContent()))
                        .sorted(TupleDataSourceBean.COMPARE_BY_TEXT).collect(Collectors.toList());

        
        
        this.aggregateOnDebitNote = false;
        this.aggregateAllOrNothing = false;
        this.closeDebitNote = false;
        this.alignAllAcademicTaxesDebitToMaxDueDate = false;
        this.createPaymentReferenceCode = false;
    }
    
    public void chooseDegreeType() {
        if (getExecutionYear() == null) {
            degreeCurricularPlanDataSource = Collections.<TupleDataSourceBean> emptyList();
            return;
        }

        if (getDegreeType() == null) {
            degreeCurricularPlanDataSource = Collections.<TupleDataSourceBean> emptyList();
            return;
        }

        
        final List<TupleDataSourceBean> result =
                ExecutionDegree.getAllByExecutionYearAndDegreeType(getExecutionYear(), getDegreeType()).stream()
                        .map(e -> e.getDegreeCurricularPlan())
                        .map((dcp) -> new TupleDataSourceBean(dcp.getExternalId(), dcp.getPresentationName(getExecutionYear())))
                        .collect(Collectors.toList());

        degreeCurricularPlanDataSource = result.stream().sorted(TupleDataSourceBean.COMPARE_BY_TEXT).collect(Collectors.toList());
    }

    public void addEntry() {
        if (product == null) {
            return;
        }

        if (entries.stream().filter(l -> l.getProduct() == product).count() > 0) {
            return;
        }

        entries.add(new ProductEntry(this.product, this.createDebt, this.toCreateAfterLastRegistrationStateDate, 
                this.forceCreation, this.limitToRegisteredOnExecutionYear));

        this.product = null;
        this.createDebt = false;
        this.forceCreation = false;
        this.limitToRegisteredOnExecutionYear = false;
    }

    public void removEntry(final int index) {
        entries.remove(index);
    }

    public void addDegreeCurricularPlans() {
        degreeCurricularPlans.addAll(degreeCurricularPlansToAdd);
        
        degreeCurricularPlansToAdd = Lists.newArrayList();
    }

    public void removeDegreeCurricularPlan(int entryIndex) {
        degreeCurricularPlans.remove(entryIndex);
    }
    
    public AcademicDebtGenerationRuleType getType() {
        return type;
    }
    
    public void setType(AcademicDebtGenerationRuleType type) {
        this.type = type;
    }
    
    public boolean isToAggregateDebitEntries() {
        return getType().strategyImplementation().isToAggregateDebitEntries();
    }
    
    public boolean isToCloseDebitNotes() {
        return getType().strategyImplementation().isToCloseDebitNotes();
    }
    
    public boolean isAggregateOnDebitNote() {
        return isToAggregateDebitEntries() && aggregateOnDebitNote;
    }
    
    public boolean isToCreatePaymentReferenceCodes() {
        return getType().strategyImplementation().isToCreatePaymentReferenceCodes();
    }
    
    public boolean isToCreateDebitEntries() {
        return getType().strategyImplementation().isToCreateDebitEntries();
    }
    
    public void setAggregateOnDebitNote(boolean aggregateOnDebitNote) {
        this.aggregateOnDebitNote = aggregateOnDebitNote;
    }

    public boolean isCloseDebitNote() {
        return isToCloseDebitNotes() && closeDebitNote;
    }

    public void setCloseDebitNote(boolean closeDebitNote) {
        this.closeDebitNote = closeDebitNote;
    }
    
    public boolean isAlignAllAcademicTaxesDebitToMaxDueDate() {
        return isToCloseDebitNotes() && alignAllAcademicTaxesDebitToMaxDueDate;
    }
    
    public void setAlignAllAcademicTaxesDebitToMaxDueDate(boolean alignAllAcademicTaxesDebitToMaxDueDate) {
        this.alignAllAcademicTaxesDebitToMaxDueDate = alignAllAcademicTaxesDebitToMaxDueDate;
    }

    public boolean isCreatePaymentReferenceCode() {
        return isToCreatePaymentReferenceCodes() && createPaymentReferenceCode;
    }

    public void setCreatePaymentReferenceCode(boolean createPaymentReferenceCode) {
        this.createPaymentReferenceCode = createPaymentReferenceCode;
    }

    public List<ProductEntry> getEntries() {
        return entries;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isCreateDebt() {
        return isToCreateDebitEntries() && createDebt;
    }

    public void setCreateDebt(boolean createDebt) {
        this.createDebt = createDebt;
    }
    
    public boolean isToCreateAfterLastRegistrationStateDate() {
        return isToCreateDebitEntries() && toCreateAfterLastRegistrationStateDate;
    }
    
    public void setToCreateAfterLastRegistrationStateDate(boolean toCreateAfterLastRegistrationStateDate) {
        this.toCreateAfterLastRegistrationStateDate = toCreateAfterLastRegistrationStateDate;
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
    
    public boolean isAggregateAllOrNothing() {
        return isToAggregateDebitEntries() && aggregateAllOrNothing;
    }

    public void setAggregateAllOrNothing(boolean aggregateAllOrNothing) {
        this.aggregateAllOrNothing = aggregateAllOrNothing;
    }

    public PaymentCodePool getPaymentCodePool() {
        return paymentCodePool;
    }

    public void setPaymentCodePool(PaymentCodePool paymentCodePool) {
        this.paymentCodePool = paymentCodePool;
    }
    
    public List<DegreeCurricularPlan> getDegreeCurricularPlans() {
        return degreeCurricularPlans;
    }

    public List<TupleDataSourceBean> getExecutionYearDataSource() {
        return executionYearDataSource;
    }

    public List<TupleDataSourceBean> getProductDataSource() {
        return productDataSource;
    }

    public List<TupleDataSourceBean> getPaymentCodePoolDataSource() {
        return paymentCodePoolDataSource;
    }
    
    public List<TupleDataSourceBean> getDegreeTypeDataSource() {
        return degreeTypeDataSource;
    }
    
    public List<TupleDataSourceBean> getDegreeCurricularPlanDataSource() {
        return degreeCurricularPlanDataSource;
    }
    
    public boolean isForceCreation() {
        return isToCreateDebitEntries() && forceCreation;
    }
    
    public void setForceCreation(boolean forceCreation) {
        this.forceCreation = forceCreation;
    }
    
    public boolean isLimitToRegisteredOnExecutionYear() {
        return isToCreateDebitEntries() && limitToRegisteredOnExecutionYear;
    }
    
    public void setLimitToRegisteredOnExecutionYear(boolean limitToRegisteredOnExecutionYear) {
        this.limitToRegisteredOnExecutionYear = limitToRegisteredOnExecutionYear;
    }
    
}
