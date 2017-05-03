package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Product;

import pt.ist.fenixframework.Atomic;

public class ERPTuitionInfoType extends ERPTuitionInfoType_Base {

    public static final Comparator<ERPTuitionInfoType> COMPARE_BY_PRODUCT_NAME = new Comparator<ERPTuitionInfoType>() {

        @Override
        public int compare(final ERPTuitionInfoType o1, final ERPTuitionInfoType o2) {
            int c = o1.getProduct().getName().getContent().compareTo(o2.getProduct().getName().getContent());

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
    
    public ERPTuitionInfoType(final Product product, final Degree degree) {
        this();
        
        setProduct(product);
        setDegree(degree);
        
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
        if (getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.bennu.required");
        }
        
        if(getProduct() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.product.required");
        }
        
        if(isForRegistration() && !(getDegree() != null ^ getDegreeType() != null)) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.degree.or.degree.type.required");
        }

        if (!(isForRegistration() ^ isForStandalone() ^ isForExtracurricular())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.entry.for.one.tuition.type.only");
        }

        if ((isForStandalone() || isForExtracurricular()) && (getDegreeType() != null || getDegree() != null)) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfoType.entry.degreeType.not.supported.for.standalone.or.extracurricular");
        }
        
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

    private boolean isDeletable() {
        return true;
    }
    
    public boolean isAppliedToAcademicTreasuryEvent(final AcademicTreasuryEvent event) {
        if (isForRegistration() && event.isForRegistrationTuition()) {
            if(getDegree() == event.getRegistration().getDegree()) {
                return true;
            } 
            
            if(getDegreeType() == event.getRegistration().getDegreeType()) {
                return true;
            }                    
        } 
        
        if(isForStandalone() && event.isForStandaloneTuition()) {
            return true;
        } 
        
        if (isForExtracurricular() && event.isForExtracurricularTuition()) {
            return true;
        }
        
        return false;
    }
    
    @Atomic
    public void delete() {
        if(!isDeletable()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.delete.impossible");
        }
        
        setProduct(null);
        setBennu(null);
        setDegree(null);
        setDegreeType(null);
        setErpTuitionInfoSettings(null);
        
        deleteDomainObject();
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

    public static Stream<Product> findActiveProducts() {
        return findActive().map(e -> e.getProduct()).collect(Collectors.toSet()).stream();
    }
    
    public static Stream<ERPTuitionInfoType> findActive(final Product product) {
        return findActive().filter(e -> e.getProduct() == product);
    }
    
    @Atomic
    public static ERPTuitionInfoType createForRegistrationTuition(final Product product, final DegreeType degreeType) {
        return new ERPTuitionInfoType(product, degreeType);
    }
    
    @Atomic
    public static ERPTuitionInfoType createForRegistrationTuition(final Product product, final Degree degree) {
        return new ERPTuitionInfoType(product, degree);
    }

    @Atomic
    public static ERPTuitionInfoType createForStandaloneTuition(final Product product) {
        return new ERPTuitionInfoType(product, true, false);
    }

    @Atomic
    public static ERPTuitionInfoType createForExtracurricularTuition(final Product product) {
        return new ERPTuitionInfoType(product, false, true);
    }

}
