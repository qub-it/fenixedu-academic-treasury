package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import com.google.common.base.Strings;

public class ERPTuitionInfoProduct extends ERPTuitionInfoProduct_Base {
    
    public ERPTuitionInfoProduct() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
    protected ERPTuitionInfoProduct(final String code, final String name) {
        this();
        
        setCode(code);
        setName(name);
        
        checkRules();
    }

    private void checkRules() {
        if (getDomainRoot() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.bennu.required");
        }

        if (isNullOrEmpty(getCode())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.code.required");
        }

        if (isNullOrEmpty(getName())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.name.required");
        }

        if (findByCode(getCode()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.code.not.unique");
        }

    }

    @Atomic
    public void update(final String name) {
        setName(name);
        
        checkRules();
    }
    
    private boolean isDeletable() {
        return getErpTuitionInfoTypesSet().isEmpty();
    }
    
    @Atomic
    public void delete() {
        if(!isDeletable()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoProduct.cannot.delete");
            
        }
        
        setDomainRoot(null);
        deleteDomainObject();
    }
    
    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on

    public static Stream<ERPTuitionInfoProduct> findAll() {
        return FenixFramework.getDomainRoot().getErpTuitionInfoProductsSet().stream();
    }

    public static Stream<ERPTuitionInfoProduct> findByCode(final String code) {
        return findAll().filter(p -> p.getCode().equals(code));
    }
    
    @Atomic
    public static ERPTuitionInfoProduct create(final String code, final String name) {
        return new ERPTuitionInfoProduct(code, name);
    }

}
