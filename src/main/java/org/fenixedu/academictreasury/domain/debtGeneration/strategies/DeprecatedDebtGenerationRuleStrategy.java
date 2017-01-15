package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class DeprecatedDebtGenerationRuleStrategy implements IAcademicDebtGenerationRuleStrategy {

    private static Logger logger = LoggerFactory.getLogger(DeprecatedDebtGenerationRuleStrategy.class);

    public boolean isAppliedOnTuitionDebitEntries() {
        return true;
    }

    public boolean isAppliedOnAcademicTaxDebitEntries() {
        return true;
    }

    public boolean isAppliedOnOtherDebitEntries() {
        return false;
    }

    @Override
    public boolean isToCreateDebitEntries() {
        return true;
    }

    @Override
    public boolean isToAggregateDebitEntries() {
        return true;
    }

    @Override
    public boolean isToCloseDebitNote() {
        return true;
    }

    @Override
    public boolean isToCreatePaymentReferenceCodes() {
        return true;
    }

    @Override
    public boolean isEntriesRequired() {
        return true;
    }

    @Override
    public boolean isToAlignAcademicTaxesDueDate() {
        return true;
    }

    @Atomic(mode = TxMode.READ)
    @Override
    public void process(final AcademicDebtGenerationRule rule) {
        // This strategy is no longer in use and must be deleted
        return;
    }

    @Atomic(mode = TxMode.READ)
    @Override
    public void process(final AcademicDebtGenerationRule rule, final Registration registration) {
        // This strategy is no longer in use and must be deleted
        return;
    }
}
