package org.fenixedu.academictreasury.dto.integration.tuitioninfo;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.treasury.domain.Product;

import com.google.common.collect.Lists;

public class ERPTuitionInfoTypeBean implements Serializable, IBean {

    private static final long serialVersionUID = 1L;

    private Product product;
    private DegreeType degreeType;
    private Degree degree;
    private ERPTuitionInfoTuitionType tuitionType;

    public List<TupleDataSourceBean> productDataSourceList;
    public List<TupleDataSourceBean> degreeTypeDataSourceList;
    public List<TupleDataSourceBean> degreeDataSourceList;
    public List<TupleDataSourceBean> tuitionTypeDataSourceList;

    public ERPTuitionInfoTypeBean() {
        updateData();
    }

    public boolean isDegreeTypeOrDegreeRequired() {
        return tuitionType != null && tuitionType.isForRegistrationTuition();
    }

    public void updateData() {
        this.productDataSourceList = fillProductDataSourceList();
        this.degreeTypeDataSourceList = fullDegreeTypeDataSourceList();
        this.degreeDataSourceList = fillDegreeDataSourceList();
        this.tuitionTypeDataSourceList = fillTuitionTypeDataSourceList();
        
        if(getTuitionType() != null && !getTuitionType().isForRegistrationTuition()) {
            setDegreeType(null);
            setDegree(null);
        } else if(getTuitionType() == null) {
            setDegreeType(null);
            setDegree(null);
        }
    }

    private List<TupleDataSourceBean> fillTuitionTypeDataSourceList() {
        final List<TupleDataSourceBean> result = Lists.newArrayList();
        
        for(final ERPTuitionInfoTuitionType type : ERPTuitionInfoTuitionType.values()) {
            result.add(new TupleDataSourceBean(type.name(), type.getLocalizedName().getContent()));
        }
        
        return result;
    }

    private List<TupleDataSourceBean> fullDegreeTypeDataSourceList() {
        final List<TupleDataSourceBean> result = Lists.newArrayList();
        
        for(final DegreeType degreeType : DegreeType.all().collect(Collectors.toSet())) {
            result.add(new TupleDataSourceBean(degreeType.getExternalId(), 
                    String.format("[%s] %s", degreeType.getCode(), degreeType.getName().getContent())));
        }
        
        return result;
    }

    private List<TupleDataSourceBean> fillProductDataSourceList() {
        final List<TupleDataSourceBean> result = Lists.newArrayList();

        for (final Product product : Product.findAll().collect(Collectors.toSet())) {
            if(product.getProductGroup() != AcademicTreasurySettings.getInstance().getTuitionProductGroup()) {
                continue;
            }
            
            result.add(new TupleDataSourceBean(product.getExternalId(),
                    String.format("[%s] %s", product.getCode(), product.getName().getContent())));
        }

        return result;
    }

    private List<TupleDataSourceBean> fillDegreeDataSourceList() {
        if(getDegreeType() == null) {
            return Lists.newArrayList();
        }
        
        final List<TupleDataSourceBean> result = Lists.newArrayList();
        for(final Degree degree : getDegreeType().getDegreeSet()) {
            result.add(new TupleDataSourceBean(degree.getExternalId(), degree.getPresentationNameI18N().getContent()));
        }
        
        return result;
    }
    
    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public DegreeType getDegreeType() {
        return degreeType;
    }

    public void setDegreeType(DegreeType degreeType) {
        this.degreeType = degreeType;
    }

    public Degree getDegree() {
        return degree;
    }

    public void setDegree(Degree degree) {
        this.degree = degree;
    }

    public ERPTuitionInfoTuitionType getTuitionType() {
        return tuitionType;
    }

    public void setTuitionType(ERPTuitionInfoTuitionType tuitionType) {
        this.tuitionType = tuitionType;
    }

}
