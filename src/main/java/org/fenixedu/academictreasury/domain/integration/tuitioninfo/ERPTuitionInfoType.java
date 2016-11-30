package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Product;

public class ERPTuitionInfoType extends ERPTuitionInfoType_Base {
    
    public static final Comparator<ERPTuitionInfoType> COMPARE_BY_NAME = new Comparator<ERPTuitionInfoType>() {

        @Override
        public int compare(final ERPTuitionInfoType o1, final ERPTuitionInfoType o2) {
            int c = o1.getName().compareTo(o2.getName());
            
            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }
        
    };
    
    
    public ERPTuitionInfoType() {
        super();
        setBennu(Bennu.getInstance());
        setErpTuitionInfoSettings(ERPTuitionInfoSettings.getInstance());
        setActive(true);
    }
    
    public ERPTuitionInfoType(final Product product, final DegreeType degreeType) {
        this();
        
        setProduct(product);
        setDegreeType(degreeType);
        
        setForRegistration(true);
        setForStandalone(false);
        setForExtracurricular(false);
        
        checkRules();
    }
    
    public ERPTuitionInfoType(final Product product, final boolean forStandalone, final boolean forExtracurricular) {
        this();
        
        setProduct(product);
        
        setForRegistration(false);
        setForStandalone(forStandalone);
        setForExtracurricular(forExtracurricular);
        
        checkRules();
    }
    
    private void checkRules() {
        if(getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoSettingsEntry.bennu.required");
        }
        
        if(getProduct() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoSettingsEntry.product.required");
        }
        
        if(!(isForRegistration() ^ isForStandalone() ^ isForExtracurricular())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoSettingsEntry.entry.for.one.tuition.type.only");
        }
        
        if(isForRegistration() && getDegreeType() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoSettingsEntry.entry.degreeType.required");
        }
        
        if((isForStandalone() || isForExtracurricular()) && getDegreeType() != null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoSettingsEntry.entry.degreeType.not.supported.for.standalone.or.extracurricular");
        }
    }
    
    public String getCode() {
        return getProduct().getCode();
    }
    
    public String getName() {
        return getProduct().getName().getContent();
    }
    
    public boolean isForRegistration() {
        return getForRegistration();
    }
    
    public boolean isForStandalone() {
        return getForStandalone();
    }
    
    public boolean isForExtracurricular() {
        return getForExtracurricular();
    }
    
    public boolean isActive() {
        return getActive();
    }
    
    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    public static Stream<ERPTuitionInfoType> findAll() {
        return ERPTuitionInfoSettings.getInstance().getErpTuitionInfoTypesSet().stream();
    }
    
    public static Stream<ERPTuitionInfoType> findActive() {
        return findAll().filter(e -> e.isActive());
    }
    
    public static Stream<Product> _findProducts() {
        return findAll().map(t -> t.getProduct()).collect(Collectors.toSet()).stream();
    }
    
    public static Stream<ERPTuitionInfoType> findByProduct(final Product product) {
        return findAll().filter(t -> t.getProduct() == product);
    }

    public static ERPTuitionInfoType createForRegistrationTuition(final Product product, final DegreeType degreeType) {
        return new ERPTuitionInfoType(product, degreeType);
    }
    
    public static ERPTuitionInfoType createForStandaloneTuition(final Product product) {
        return new ERPTuitionInfoType(product, true, false);
    }
    
    public static ERPTuitionInfoType createForExtracurricularTuition(final Product product) {
        return new ERPTuitionInfoType(product, false, true);
    }

}
