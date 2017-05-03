package org.fenixedu.academictreasury.dto.integration.tuitioninfo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoType;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.treasury.domain.Product;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ERPTuitionInfoBean implements IBean, Serializable {

    private static final long serialVersionUID = 1L;

    private ExecutionYear executionYear;
    private Product product;

    private List<TupleDataSourceBean> executionYearDataSourceList;
    private List<TupleDataSourceBean> productDataSourceList;

    public ERPTuitionInfoBean() {
    }
    
    public ERPTuitionInfoBean(final PersonCustomer personCustomer) {
        updateData(personCustomer);
    }

    public void updateData(final PersonCustomer personCustomer) {
        this.executionYearDataSourceList = fillExecutionYearDataSourceList(personCustomer);
        this.productDataSourceList = fillProductDataSourceList(personCustomer);
        
        if(getExecutionYear() == null) {
            setProduct(null);
        }
    }

    private List<TupleDataSourceBean> fillExecutionYearDataSourceList(final PersonCustomer personCustomer) {
        return personCustomer.getTreasuryEventsSet().stream().filter(e -> e instanceof AcademicTreasuryEvent)
                .map(AcademicTreasuryEvent.class::cast)
                .filter(e -> e.isForRegistrationTuition() || e.isForStandaloneTuition() || e.isForExtracurricularTuition())
                .map(e -> e.getExecutionYear()).collect(Collectors.toSet()).stream().sorted(ExecutionYear.COMPARATOR_BY_YEAR)
                .map(e -> new TupleDataSourceBean(e.getExternalId(), e.getQualifiedName())).collect(Collectors.toList());
    }

    private List<TupleDataSourceBean> fillProductDataSourceList(final PersonCustomer personCustomer) {
        if (getExecutionYear() == null) {
            return Lists.newArrayList();
        }

        final Set<AcademicTreasuryEvent> events = personCustomer.getTreasuryEventsSet().stream()
                .filter(e -> e instanceof AcademicTreasuryEvent).map(AcademicTreasuryEvent.class::cast)
                .filter(e -> e.isForRegistrationTuition() || e.isForStandaloneTuition() || e.isForExtracurricularTuition())
                .filter(e -> e.getExecutionYear() == getExecutionYear()).collect(Collectors.toSet());

        final Set<ERPTuitionInfoType> appliableTypes = Sets.newHashSet();
        for (final ERPTuitionInfoType type : ERPTuitionInfoType.findActive().collect(Collectors.toSet())) {
            for (final AcademicTreasuryEvent e : events) {
                if (type.isAppliedToAcademicTreasuryEvent(e)) {
                    appliableTypes.add(type);
                }
            }
        }

        return appliableTypes.stream().map(e -> e.getProduct()).collect(Collectors.toSet()).stream()
                .map(p -> new TupleDataSourceBean(p.getExternalId(), p.getName().getContent())).collect(Collectors.toList());
    }

    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<TupleDataSourceBean> getExecutionYearDataSourceList() {
        return executionYearDataSourceList;
    }

    public void setExecutionYearDataSourceList(List<TupleDataSourceBean> executionYearDataSourceList) {
        this.executionYearDataSourceList = executionYearDataSourceList;
    }

    public List<TupleDataSourceBean> getProductDataSourceList() {
        return productDataSourceList;
    }

    public void setProductDataSourceList(List<TupleDataSourceBean> productDataSourceList) {
        this.productDataSourceList = productDataSourceList;
    }

}
