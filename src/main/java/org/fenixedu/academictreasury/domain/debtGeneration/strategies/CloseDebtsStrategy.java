package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleEntry;
import org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy;
import org.fenixedu.academictreasury.domain.debtGeneration.LogBean;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class CloseDebtsStrategy implements IAcademicDebtGenerationRuleStrategy {

    private static Logger logger = LoggerFactory.getLogger(CloseDebtsStrategy.class);

    @Override
    public boolean isAppliedOnTuitionDebitEntries() {
        return true;
    }

    @Override
    public boolean isAppliedOnAcademicTaxDebitEntries() {
        return true;
    }

    @Override
    public boolean isAppliedOnOtherDebitEntries() {
        return true;
    }

    @Override
    public boolean isToCreateDebitEntries() {
        return false;
    }

    @Override
    public boolean isToAggregateDebitEntries() {
        return false;
    }

    @Override
    public boolean isToCloseDebitNotes() {
        return true;
    }

    @Override
    public boolean isToCreatePaymentReferenceCodes() {
        return false;
    }

    @Override
    public void process(final AcademicDebtGenerationRule rule) {
        logger.debug(String.format("[AcademicDebtGenerationRule] START: %s", rule.getExternalId()));

        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        final LogBean logBean = new LogBean();
        logBean.processDate = new DateTime();

        long timeInMillis = System.currentTimeMillis();
        for (final DebtAccount debtAccount : DebtAccount.findAll().collect(Collectors.toSet())) {
            processDebtsForDebtAccount(rule, debtAccount, logBean);
        }

        logger.debug(String.format("[AcademicDebtGenerationRule] Elapsed: %d", (System.currentTimeMillis() - timeInMillis)));
    }

    @Atomic(mode = TxMode.WRITE)
    private void processDebtsForDebtAccount(final AcademicDebtGenerationRule rule, final DebtAccount debtAccount,
            final LogBean logBean) {
        _processDebtsForDebtAccount(rule, debtAccount, logBean);
    }

    private void _processDebtsForDebtAccount(final AcademicDebtGenerationRule rule, final DebtAccount debtAccount,
            final LogBean logBean) {
        logger.debug(String.format("[AcademicDebtGenerationRule] _processDebtsForDebtAccount for client '%s'",
                debtAccount.getCustomer().getCode()));

        // For each product try to grab or create if requested
        long startCreatingDebts = System.currentTimeMillis();

        final Set<DebitEntry> debitEntriesResult = Sets.newHashSet();
        for (final InvoiceEntry invoiceEntry : debtAccount.getInvoiceEntrySet()) {
            if (!invoiceEntry.isDebitNoteEntry()) {
                continue;
            }

            if (invoiceEntry.isAnnulled()) {
                continue;
            }

            if (!isProductDefinedInRule(rule, invoiceEntry)) {
                continue;
            }

            final DebitEntry debitEntry = (DebitEntry) invoiceEntry;

            if (debitEntry.isProcessedInClosedDebitNote()) {
                continue;
            }

            if (!debitEntry.getDueDate().minusDays(getDays()).isAfter(new LocalDate())) {
                continue;
            }

            debitEntriesResult.add(debitEntry);
        }

        if (debitEntriesResult.isEmpty()) {
            return;
        }

        if (rule.isAggregateAllOrNothing() && !Sets
                .difference(debitEntriesResult.stream().map(d -> d.getProduct()).collect(Collectors.toSet()), rule
                        .getAcademicDebtGenerationRuleEntriesSet().stream().map(e -> e.getProduct()).collect(Collectors.toSet()))
                .isEmpty()) {
            throw new AcademicTreasuryDomainException("error.CloseDebtsStrategy.unable.to.aggregate.all.products");
        }

        final Set<DebitNote> debitNotes =
                debitEntriesResult.stream().map(d -> (DebitNote) d.getFinantialDocument()).collect(Collectors.toSet());

        for (final DebitNote debitNote : debitNotes) {
            debitNote.closeDocument();
        }

        logger.debug(String.format("[AcademicDebtGenerationRule][%s] _processDebtsForDebtAccount: %d",
                debtAccount.getCustomer().getCode(), System.currentTimeMillis() - startCreatingDebts));
    }

    private boolean isProductDefinedInRule(final AcademicDebtGenerationRule rule, final InvoiceEntry invoiceEntry) {
        for (final AcademicDebtGenerationRuleEntry entry : rule.getAcademicDebtGenerationRuleEntriesSet()) {
            if (entry.getProduct() == invoiceEntry.getProduct()) {
                return true;
            }
        }

        return false;
    }

}
