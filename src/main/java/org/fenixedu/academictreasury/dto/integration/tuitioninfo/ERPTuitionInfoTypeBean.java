package org.fenixedu.academictreasury.dto.integration.tuitioninfo;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.treasury.domain.Product;

import com.google.common.collect.Lists;

public class ERPTuitionInfoTypeBean implements Serializable, IBean {

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
    }
    
    private List<TupleDataSourceBean> fillTuitionTypeDataSourceList() {
        final List<TupleDataSourceBean> result = Lists.newArrayList();
        
        for(final Product product : Product.findAll().collect(Collectors.toSet())) {
            
        }
        
        return null;
    }

    private List<TupleDataSourceBean> fillDegreeDataSourceList() {
        return null;
    }

    private List<TupleDataSourceBean> fullDegreeTypeDataSourceList() {
        return null;
    }

    private List<TupleDataSourceBean> fillProductDataSourceList() {
        return null;
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
