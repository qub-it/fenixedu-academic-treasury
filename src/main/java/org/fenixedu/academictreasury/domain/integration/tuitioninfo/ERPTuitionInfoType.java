package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class ERPTuitionInfoType extends ERPTuitionInfoType_Base {

    public static final Comparator<ERPTuitionInfoType> COMPARE_BY_NAME = new Comparator<ERPTuitionInfoType>() {

        @Override
        public int compare(final ERPTuitionInfoType o1, final ERPTuitionInfoType o2) {
            int c = o1.getErpTuitionInfoProduct().getName().compareTo(o2.getErpTuitionInfoProduct().getName());

            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }

    };

    public ERPTuitionInfoType() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setErpTuitionInfoSettings(ERPTuitionInfoSettings.getInstance());
        setActive(true);
    }

    private ERPTuitionInfoType(final ERPTuitionInfoTypeBean bean) {
        this();

        setExecutionYear(bean.getExecutionYear());
        setErpTuitionInfoProduct(bean.getErpTuitionInfoProduct());
        
        getTuitionProductsSet().addAll(bean.getTuitionProducts());

        if(bean.getTuitionPaymentPlanGroup() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.TuitionPaymentPlanGroup.required.to.infer.academic.info");
        }
        
        if(bean.getTuitionPaymentPlanGroup().isForExtracurricular()) {

            ERPTuitionInfoTypeAcademicEntry.createForExtracurricularTuition(this);

        } else if(bean.getTuitionPaymentPlanGroup().isForStandalone()) {

            ERPTuitionInfoTypeAcademicEntry.createForStandaloneTuition(this);

        } else if(bean.getTuitionPaymentPlanGroup().isForRegistration()) {
            
            for (DegreeType degreeType : bean.getDegreeTypes()) {
                ERPTuitionInfoTypeAcademicEntry.createForRegistrationTuition(this, degreeType);
            }
            
            for (Degree degree : bean.getDegrees()) {
                ERPTuitionInfoTypeAcademicEntry.createForRegistrationTuition(this, degree);
            }
            
            for (DegreeCurricularPlan degreeCurricularPlan : bean.getDegreeCurricularPlans()) {
                ERPTuitionInfoTypeAcademicEntry.createForRegistrationTuition(this, degreeCurricularPlan);
            }
        }
        
        checkRules();
    }

    private void checkRules() {

        if (getDomainRoot() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.bennu.required");
        }

        if (getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.executionYear.required");
        }

        if (getTuitionProductsSet().isEmpty()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.tuitionProducts.required");
        }
        
        if(getErpTuitionInfoProduct() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.erpTuitionProduct.required");
        }
        
        final ExecutionYear executionYear = getExecutionYear();
        if(getErpTuitionInfoProduct().getErpTuitionInfoTypesSet().stream().filter(t -> t.getExecutionYear() == executionYear).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.erpTuitionProduct.already.defined");
        }
        
        if(getErpTuitionInfoTypeAcademicEntriesSet().isEmpty()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.academic.entries.required");
        }
        
    }
    
    public boolean isActive() {
        return getActive();
    }

    public void delete() {
        if (!getErpTuitionInfosSet().isEmpty()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.delete.not.possible");
        }

        setDomainRoot(null);
        setErpTuitionInfoProduct(null);
        setErpTuitionInfoSettings(null);
        setExecutionYear(null);
        getTuitionProductsSet().clear();

        while(!getErpTuitionInfoTypeAcademicEntriesSet().isEmpty()) {
            getErpTuitionInfoTypeAcademicEntriesSet().iterator().next().delete();
        }
        
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
    
    public static Stream<? extends ERPTuitionInfoType> findForExecutionYear(final ExecutionYear executionYear) {
        return executionYear.getErpTuitionInfoTypesSet().stream();
    }

    public static Stream<ERPTuitionInfoType> findActiveForExecutionYear(final ExecutionYear executionYear) {
        return executionYear.getErpTuitionInfoTypesSet().stream().filter(e -> e.isActive());
    }

    public static Stream<ERPTuitionInfoType> findByCode(final String code) {
        return findAll().filter(e -> e.getErpTuitionInfoProduct().getCode().equals(code));
    }

    public static Optional<ERPTuitionInfoType> findUniqueByCode(final String code) {
        return findAll().filter(e -> e.getErpTuitionInfoProduct().getCode().equals(code)).findFirst();
    }

    @Atomic
    public static ERPTuitionInfoType create(final ERPTuitionInfoTypeBean bean) {
        return new ERPTuitionInfoType(bean);
    }

}
