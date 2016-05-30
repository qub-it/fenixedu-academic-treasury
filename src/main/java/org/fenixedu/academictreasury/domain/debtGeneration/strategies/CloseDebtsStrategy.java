package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleEntry;
import org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
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
        return false;
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
    @Atomic(mode = TxMode.READ)
    public void process(final AcademicDebtGenerationRule rule) {

        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        for (final DegreeCurricularPlan degreeCurricularPlan : rule.getDegreeCurricularPlansSet()) {
            for (final Registration registration : degreeCurricularPlan.getRegistrations()) {

                try {
                    processDebtsForRegistration(rule, registration);
                } catch(final AcademicTreasuryDomainException e) {
                    logger.info(e.getMessage());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    @Atomic(mode = TxMode.READ)
    public void process(final AcademicDebtGenerationRule rule, final Registration registration) {
        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }
        
        try {
            processDebtsForRegistration(rule, registration);
        } catch(final AcademicTreasuryDomainException e) {
            logger.info(e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    @Atomic(mode = TxMode.WRITE)
    private void processDebtsForRegistration(final AcademicDebtGenerationRule rule, final Registration registration) {
        for (final AcademicDebtGenerationRuleEntry entry : rule.getAcademicDebtGenerationRuleEntriesSet()) {
            final Product product = entry.getProduct();

            Set<DebitEntry> grabbedDebitEntries = null;
            if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == product.getProductGroup()) {
                grabbedDebitEntries = grabDebitEntryForTuitions(rule, registration, entry);
            } else if (AcademicTax.findUnique(product).isPresent()) {
                grabbedDebitEntries = grabDebitEntryForAcademicTax(rule, registration, entry);
            }
            
            for(final DebitEntry grabbedDebitEntry : grabbedDebitEntries) {
                if(grabbedDebitEntry.getFinantialDocument() == null || !grabbedDebitEntry.getFinantialDocument().isPreparing()) {
                    continue;
                }
                
                final LocalDate dueDate = grabbedDebitEntry.getDueDate();
                if(dueDate.plusDays(rule.getDays()).isAfter(new LocalDate())) {
                    continue;
                }
                
                final DebitNote debitNote = (DebitNote) grabbedDebitEntry.getFinantialDocument();
                
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
                
                debitNote.closeDocument();
            }
        }
    }

    private Set<DebitEntry> grabDebitEntryForTuitions(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();

        final AcademicTreasuryEvent t =
                TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);

        if (t == null || !t.isChargedWithDebitEntry(product)) {
            return Sets.newHashSet();
        }

        return DebitEntry.findActive(t, product).collect(Collectors.<DebitEntry> toSet());
    }
    
    private Set<DebitEntry> grabDebitEntryForAcademicTax(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();
        final AcademicTax academicTax = AcademicTax.findUnique(product).get();

        final AcademicTreasuryEvent academicTreasuryEvent =
                AcademicTaxServices.findAcademicTreasuryEvent(registration, executionYear, academicTax);

        if (academicTreasuryEvent != null && academicTreasuryEvent.isChargedWithDebitEntry()) {
            return DebitEntry.findActive(academicTreasuryEvent).collect(Collectors.<DebitEntry> toSet());
        }

        return Sets.newHashSet();
    }

    private LocalDate maxDebitEntryDueDate(final DebitNote debitNote) {
        final LocalDate maxDate =
                debitNote.getDebitEntries().max(DebitEntry.COMPARE_BY_DUE_DATE).map(DebitEntry::getDueDate)
                        .orElse(new LocalDate());
        return maxDate.isAfter(new LocalDate()) ? maxDate : new LocalDate();
    }

}
