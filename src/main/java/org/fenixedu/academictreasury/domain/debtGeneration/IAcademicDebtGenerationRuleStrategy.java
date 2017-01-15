package org.fenixedu.academictreasury.domain.debtGeneration;

import java.util.stream.Stream;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;

public interface IAcademicDebtGenerationRuleStrategy {

    public boolean isAppliedOnTuitionDebitEntries();

    public boolean isAppliedOnAcademicTaxDebitEntries();

    public boolean isAppliedOnOtherDebitEntries();

    public boolean isToCreateDebitEntries();

    public boolean isToAggregateDebitEntries();

    public boolean isToCloseDebitNote();

    public boolean isToCreatePaymentReferenceCodes();

    public boolean isEntriesRequired();

    public boolean isToAlignAcademicTaxesDueDate();

    public void process(final AcademicDebtGenerationRule rule);

    public void process(final AcademicDebtGenerationRule rule, final Registration registration);

    public static Stream<? extends DebitEntry> findActiveDebitEntries(final PersonCustomer customer, final TreasuryEvent treasuryEvent) {
        return DebitEntry.findActive(treasuryEvent).filter(d -> d.getDebtAccount().getCustomer() == customer);
    }

    public static Stream<? extends DebitEntry> findActiveDebitEntries(final PersonCustomer customer, final TreasuryEvent treasuryEvent,
            final Product product) {
        return DebitEntry.findActive(treasuryEvent, product).filter(d -> d.getDebtAccount().getCustomer() == customer);
    }
}
