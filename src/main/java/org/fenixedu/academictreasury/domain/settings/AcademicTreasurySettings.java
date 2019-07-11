package org.fenixedu.academictreasury.domain.settings;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.treasury.IAcademicTreasuryAccountUrl;

import pt.ist.fenixframework.FenixFramework;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.ProductGroup;

import pt.ist.fenixframework.Atomic;

public class AcademicTreasurySettings extends AcademicTreasurySettings_Base {

    protected AcademicTreasurySettings() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }

    @Atomic
    public void edit(final ProductGroup emolumentsProductGroup, final ProductGroup tuitionProductGroup,
            final AcademicTax improvementAcademicTax, final boolean closeServiceRequestEmolumentsWithDebitNote,
            boolean runAcademicDebtGenerationRuleOnNormalEnrolment) {
        setEmolumentsProductGroup(emolumentsProductGroup);
        setTuitionProductGroup(tuitionProductGroup);
        setImprovementAcademicTax(improvementAcademicTax);
        setCloseServiceRequestEmolumentsWithDebitNote(closeServiceRequestEmolumentsWithDebitNote);
        setRunAcademicDebtGenerationRuleOnNormalEnrolment(runAcademicDebtGenerationRuleOnNormalEnrolment);
    }

    @Atomic
    public void addAcademicalActBlockingProduct(final Product product) {
        super.addAcademicalActBlockingProducts(product);
    }

    @Atomic
    public void removeAcademicalActBlockingProduct(final Product product) {
        super.removeAcademicalActBlockingProducts(product);
    }

    @Atomic
    public void addProductsForAcademicalActBlocking(final Set<Product> products) {
        for (Product product : products) {
            addAcademicalActBlockingProduct(product);
        }
    }

    @Atomic
    public void removeProductsForAcademicalActBlocking(final Set<Product> products) {
        for (Product product : products) {
            removeAcademicalActBlockingProduct(product);
        }
    }

    public boolean isAcademicalActBlocking(final Product product) {
        return getAcademicalActBlockingProductsSet().contains(product);
    }

    public boolean isCloseServiceRequestEmolumentsWithDebitNote() {
        return getCloseServiceRequestEmolumentsWithDebitNote();
    }

    public boolean isRunAcademicDebtGenerationRuleOnNormalEnrolment() {
        return getRunAcademicDebtGenerationRuleOnNormalEnrolment();
    }
    
    public IAcademicTreasuryAccountUrl getAcademicTreasuryAccountUrl() {
        try {
            return (IAcademicTreasuryAccountUrl) ClassUtils.getClass(getAcademicTreasuryAccountUrlImpl()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    protected static Optional<AcademicTreasurySettings> find() {
        return FenixFramework.getDomainRoot().getAcademicTreasurySettingsSet().stream().findFirst();
    }

    @Atomic
    public static AcademicTreasurySettings getInstance() {
        if (!find().isPresent()) {
            return new AcademicTreasurySettings();
        }

        return find().get();
    }

}
