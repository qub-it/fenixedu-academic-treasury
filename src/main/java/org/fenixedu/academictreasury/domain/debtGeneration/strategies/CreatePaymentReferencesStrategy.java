package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleEntry;
import org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy;
import org.fenixedu.academictreasury.domain.debtGeneration.LogBean;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class CreatePaymentReferencesStrategy implements IAcademicDebtGenerationRuleStrategy {

    private static Logger logger = LoggerFactory.getLogger(CreatePaymentReferencesStrategy.class);

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
        return false;
    }

    @Override
    public boolean isToCreatePaymentReferenceCodes() {
        return true;
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

    private void processDebtsForDebtAccount(AcademicDebtGenerationRule rule, DebtAccount debtAccount, LogBean logBean) {

        logger.debug(String.format("[AcademicDebtGenerationRule] processDebtsForRegistration for student '%d'", registration
                .getStudent().getNumber()));

        // For each product try to grab or create if requested
        long startCreatingDebts = System.currentTimeMillis();

        // For each product try to grab or create if requested
        final Set<DebitEntry> debitEntries = Sets.newHashSet();
        
        DebitEntry grabbedDebitEntry = null;
        for (final AcademicDebtGenerationRuleEntry entry : rule.getAcademicDebtGenerationRuleEntriesSet()) {
            final Product product = entry.getProduct();

            if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == product.getProductGroup()) {
                grabbedDebitEntry = grabOrCreateDebitEntryForTuition(rule, registration, entry, logBean);
            } else if (AcademicTax.findUnique(product).isPresent()) {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabOrCreateDebitEntryForAcademicTax(rule, registration, entry, logBean);
            } else {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabDebitEntry(rule, registration, entry, logBean);
            }
        }
        
        logger.debug(String.format("[AcademicDebtGenerationRule][%d] Debit entries in: %d", registration.getStudent().getNumber(),
                System.currentTimeMillis() - startCreatingDebts));

        long startDebitNote = System.currentTimeMillis();

        DebitNote debitNote = null;
        if (isAllWithClosedDebitNote(debitEntries)) {
            logBean.registerAllWithClosedDebitNote(registration);

            if (!allWithTheSameClosedDebitNote(debitEntries)) {
                logBean.registerDebitEntriesWithDifferentClosedDebitNotes(registration, debitNote);
                return;
            }

            debitNote = (DebitNote) debitEntries.iterator().next().getFinantialDocument();
        } else {

            debitNote = grabPreparingOrCreateDebitEntry(registration, debitEntries, logBean);

            for (final DebitEntry debitEntry : debitEntries) {
                if (debitEntry.getFinantialDocument() == null) {
                    debitEntry.setFinantialDocument(debitNote);
                }
            }

            if (debitNote.getFinantialDocumentEntriesSet().isEmpty()) {
                throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.debit.note.without.debit.entries");
            }

            logBean.registerDebitEntriesOnDebitNote(registration, debitNote);

            if (debitNote.isPreparing() && rule.isCloseDebitNote()) {
                final LocalDate maxDebitEntryDueDate = maxDebitEntryDueDate(debitNote);
                debitNote.setDocumentDueDate(maxDebitEntryDueDate);

                if (rule.isAlignAllAcademicTaxesDebitToMaxDueDate()) {
                    for (final DebitEntry debitEntry : debitNote.getDebitEntriesSet()) {
                        if (!AcademicTax.findUnique(debitEntry.getProduct()).isPresent()) {
                            continue;
                        }
                        debitEntry.setDueDate(maxDebitEntryDueDate);
                    }
                }

                logBean.registerDebitNoteClosing(registration, debitNote);
            }

            if (rule.isAggregateAllOrNothing()) {
                for (final DebitEntry debitEntry : debitEntries) {
                    if (debitEntry.getFinantialDocument() != debitNote) {
                        throw new AcademicTreasuryDomainException(
                                "error.AcademicDebtGenerationRule.debit.entries.not.aggregated.on.same.debit.note");
                    }
                }

                // Check if all configured produts are in debitNote
                for (final Product product : rule.getAcademicDebtGenerationRuleEntriesSet().stream()
                        .map(AcademicDebtGenerationRuleEntry::getProduct).collect(Collectors.toSet())) {
                    if (debitNote.getDebitEntries().filter(l -> l.getProduct() == product).count() == 0) {
                        throw new AcademicTreasuryDomainException(
                                "error.AcademicDebtGenerationRule.debit.entries.not.aggregated.on.same.debit.note");
                    }
                }
            }
        }

        logger.debug(String.format("[AcademicDebtGenerationRule][%d] Debit note in: %d", registration.getStudent().getNumber(),
                System.currentTimeMillis() - startDebitNote));
    }

}
