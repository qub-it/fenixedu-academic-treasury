package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleEntry;
import org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.paymentcodes.FinantialDocumentPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

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
    
    @Atomic(mode = TxMode.READ)
    @Override
    public void process(final AcademicDebtGenerationRule rule) {

        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        for (final DegreeCurricularPlan degreeCurricularPlan : rule.getDegreeCurricularPlansSet()) {
            for (final Registration registration : degreeCurricularPlan.getRegistrations()) {

                if (registration.getStudentCurricularPlan(rule.getExecutionYear()) == null) {
                    continue;
                }

                if (!rule.getDegreeCurricularPlansSet().contains(
                        registration.getStudentCurricularPlan(rule.getExecutionYear()).getDegreeCurricularPlan())) {
                    continue;
                }

                // Discard registrations not active and with no enrolments
                if (!registration.hasAnyActiveState(rule.getExecutionYear())) {
                    continue;
                }
                
                if(!registration.hasAnyEnrolmentsIn(rule.getExecutionYear())) {
                    // only return is this rule has not entry that forces creation
                    if (!isRuleWithOnlyOneAcademicTaxEntryForcingCreation(rule)) {
                        continue;
                    }
                }
                
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

    @Atomic(mode = TxMode.READ)
    @Override
    public void process(final AcademicDebtGenerationRule rule, final Registration registration) {
        try {
            if (!rule.isActive()) {
                throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
            }
            
            if (registration.getStudentCurricularPlan(rule.getExecutionYear()) == null) {
                return;
            }
            
            if (!rule.getDegreeCurricularPlansSet().contains(
                    registration.getStudentCurricularPlan(rule.getExecutionYear()).getDegreeCurricularPlan())) {
                return;
            }
            
            logger.debug("AcademicDebtGenerationRule: Start");
            
            // Discard registrations not active and with no enrolments
            if (!registration.hasAnyActiveState(rule.getExecutionYear())) {
                return;
            }
            
            if (!registration.hasAnyEnrolmentsIn(rule.getExecutionYear())) {
                
                // only return is this rule has not entry that forces creation
                if (!isRuleWithOnlyOneAcademicTaxEntryForcingCreation(rule)) {
                    return;
                }
            }
            
            processDebtsForRegistration(rule, registration);
        } catch(final AcademicTreasuryDomainException e) {
            logger.info(e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRuleWithOnlyOneAcademicTaxEntryForcingCreation(final AcademicDebtGenerationRule rule) {
        if(rule.getAcademicDebtGenerationRuleEntriesSet().size() != 1) {
            return false;
        }
        
        if(!AcademicTax.findUnique(rule.getAcademicDebtGenerationRuleEntriesSet().iterator().next().getProduct()).isPresent()) {
            return false;
        }
        
        return rule.getAcademicDebtGenerationRuleEntriesSet().iterator().next().isForceCreation();
    }


    @Atomic(mode = TxMode.WRITE)
    private void processDebtsForRegistration(final AcademicDebtGenerationRule rule, final Registration registration) {
        _processDebtsForRegistration(rule, registration);
    }

    private void _processDebtsForRegistration(final AcademicDebtGenerationRule rule, final Registration registration) {
        final ExecutionYear executionYear = rule.getExecutionYear();

        logger.debug(String.format("[AcademicDebtGenerationRule] processDebtsForRegistration for student '%d'", registration
                .getStudent().getNumber()));

        // For each product try to grab or create if requested
        final Set<DebitEntry> debitEntries = Sets.newHashSet();

        long startCreatingDebts = System.currentTimeMillis();

        for (final AcademicDebtGenerationRuleEntry entry : rule.getAcademicDebtGenerationRuleEntriesSet()) {
            final Product product = entry.getProduct();

            DebitEntry grabbedDebitEntry = null;
            // Check if the product is tuition kind
            if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == product.getProductGroup()) {
                grabbedDebitEntry = grabOrCreateDebitEntryForTuition(rule, registration, entry);
            } else if (AcademicTax.findUnique(product).isPresent()) {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabOrCreateDebitEntryForAcademicTax(rule, registration, entry);
            } else {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabDebitEntry(rule, registration, entry);
            }

            if (grabbedDebitEntry != null) {
                debitEntries.add(grabbedDebitEntry);
            }
        }

        logger.debug(String.format("[AcademicDebtGenerationRule][%d] Debit entries in: %d", registration.getStudent().getNumber(),
                System.currentTimeMillis() - startCreatingDebts));

        if (!rule.isAggregateOnDebitNote()) {
            return;
        }

        if (debitEntries.isEmpty()) {
            return;
        }

        long startDebitNote = System.currentTimeMillis();

        DebitNote debitNote = null;
        if (isAllWithClosedDebitNote(debitEntries)) {

            if (!allWithTheSameClosedDebitNote(debitEntries)) {
                return;
            }

            debitNote = (DebitNote) debitEntries.iterator().next().getFinantialDocument();
        } else {

            debitNote = grabPreparingOrCreateDebitEntry(registration, debitEntries);

            for (final DebitEntry debitEntry : debitEntries) {
                if (debitEntry.getFinantialDocument() == null) {
                    debitEntry.setFinantialDocument(debitNote);
                }
            }

            if (debitNote.getFinantialDocumentEntriesSet().isEmpty()) {
                throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.debit.note.without.debit.entries");
            }


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

                debitNote.closeDocument();
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

        long startPaymentCodes = System.currentTimeMillis();

        if (debitNote.isClosed() && rule.isCreatePaymentReferenceCode()
                && FinantialDocumentPaymentCode.findNewByFinantialDocument(debitNote).count() == 0
                && FinantialDocumentPaymentCode.findUsedByFinantialDocument(debitNote).count() == 0) {

            if (!debitNote.getPaymentCodesSet().isEmpty()) {
                return;
            }

            final PaymentReferenceCode paymentReferenceCode =
                    rule.getPaymentCodePool().getReferenceCodeGenerator().generateNewCodeFor(
                            debitNote.getOpenAmount(), new LocalDate(), debitNote.getDocumentDueDate(), true);

            
            FinantialDocumentPaymentCode paymentCode = FinantialDocumentPaymentCode.create(debitNote, paymentReferenceCode, true);
        }

        logger.debug(String.format("[AcademicDebtGenerationRule][%d] Payment codes in: %d", registration.getStudent().getNumber(),
                System.currentTimeMillis() - startPaymentCodes));

    }

    private DebitEntry grabDebitEntry(final AcademicDebtGenerationRule rule, final Registration registration, final AcademicDebtGenerationRuleEntry entry) {
        if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == entry.getProduct().getProductGroup()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.entry.is.tuition");
        }
        
        if (AcademicTax.findUnique(entry.getProduct()).isPresent()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.entry.is.academicTax");            
        }
        
        final FinantialEntity finantialEntity = registration.getDegree().getAdministrativeOffice().getFinantialEntity();
        
        if(finantialEntity == null) {
            return null;
        }
        
        if(!PersonCustomer.findUnique(registration.getPerson()).isPresent()) {
            return null;
        }
        
        if(!DebtAccount.findUnique(finantialEntity.getFinantialInstitution(), PersonCustomer.findUnique(registration.getPerson()).get()).isPresent()) {
            return null;
        }
        
        final DebtAccount debtAccount = DebtAccount.findUnique(finantialEntity.getFinantialInstitution(), PersonCustomer.findUnique(registration.getPerson()).get()).get();
        
        for (final DebitEntry debitEntry : DebitEntry.findActive(debtAccount, entry.getProduct()).collect(Collectors.<DebitEntry>toSet())) {

            if(!debitEntry.isInDebt()) {
                continue;
            }
            
            if(debitEntry.isAnnulled()) {
                continue;
            }
            
            if(!rule.isCreatePaymentReferenceCode() && debitEntry.getFinantialDocument().isClosed()) {
                continue;
            }
            
            if(rule.isCreatePaymentReferenceCode() && debitEntry.getFinantialDocument().isClosed()
                    && (FinantialDocumentPaymentCode.findNewByFinantialDocument(debitEntry.getFinantialDocument()).count() != 0
                    || FinantialDocumentPaymentCode.findUsedByFinantialDocument(debitEntry.getFinantialDocument()).count() != 0)) {
                continue;
            }
            
            return debitEntry;
        }
        
        return null;
    }

    private LocalDate maxDebitEntryDueDate(final DebitNote debitNote) {
        final LocalDate maxDate =
                debitNote.getDebitEntries().max(DebitEntry.COMPARE_BY_DUE_DATE).map(DebitEntry::getDueDate)
                        .orElse(new LocalDate());
        return maxDate.isAfter(new LocalDate()) ? maxDate : new LocalDate();
    }

    private boolean allWithTheSameClosedDebitNote(final Set<DebitEntry> debitEntries) {
        FinantialDocument finantialDocument = debitEntries.iterator().next().getFinantialDocument();

        if (finantialDocument == null || !finantialDocument.isClosed()) {
            throw new RuntimeException("Should not be here");
        }

        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() != finantialDocument) {
                return false;
            }
        }

        return true;
    }

    private boolean isAllWithClosedDebitNote(Set<DebitEntry> debitEntries) {
        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() == null || !debitEntry.getFinantialDocument().isClosed()) {
                return false;
            }
        }

        return true;
    }

    private DebitNote grabPreparingOrCreateDebitEntry(final Registration registration, final Set<DebitEntry> debitEntries) {

        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() != null && debitEntry.getFinantialDocument().isPreparing()) {
                return (DebitNote) debitEntry.getFinantialDocument();
            }
        }

        final DebitNote debitNote =
                DebitNote.create(
                        debitEntries.iterator().next().getDebtAccount(),
                        DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(),
                                debitEntries.iterator().next().getDebtAccount().getFinantialInstitution()).get(), new DateTime());

        return debitNote;
    }

    private DebitEntry grabOrCreateDebitEntryForAcademicTax(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();
        final AcademicTax academicTax = AcademicTax.findUnique(product).get();

        {
            AcademicTreasuryEvent t = AcademicTaxServices.findAcademicTreasuryEvent(registration, executionYear, academicTax);
            if (t == null || !t.isChargedWithDebitEntry()) {
                if (!entry.isCreateDebt()) {
                    return null;
                }

                /* HACK: For now limit forcing for first time students only */
                boolean forceCreation = entry.isCreateDebt() && registration.isFirstTime(rule.getExecutionYear());

                AcademicTaxServices.createAcademicTax(registration, executionYear, academicTax, forceCreation);
            }
        }

        final AcademicTreasuryEvent academicTreasuryEvent =
                AcademicTaxServices.findAcademicTreasuryEvent(registration, executionYear, academicTax);

        if (academicTreasuryEvent != null && academicTreasuryEvent.isChargedWithDebitEntry()) {
            return DebitEntry.findActive(academicTreasuryEvent).findFirst().get();
        }

        return null;
    }

    private DebitEntry grabOrCreateDebitEntryForTuition(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();

        // Is of tuition kind try to catch the tuition event
        boolean createdTuition = false;
        {
            AcademicTreasuryEvent t =
                    TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);

            if (t == null || !t.isChargedWithDebitEntry(product)) {

                if (!entry.isCreateDebt()) {
                    return null;
                }

                if (entry.isToCreateAfterLastRegistrationStateDate()) {
                    final LocalDate lastRegisteredStateDate = TuitionServices.lastRegisteredDate(registration, executionYear);
                    if (lastRegisteredStateDate == null) {
                        return null;
                    } else if (lastRegisteredStateDate.isAfter(new LocalDate())) {
                        return null;
                    } else {
                        createdTuition =
                                TuitionServices.createInferedTuitionForRegistration(registration, executionYear,
                                        lastRegisteredStateDate, false);
                    }
                } else {
                    final LocalDate enrolmentDate = TuitionServices.enrolmentDate(registration, executionYear, false);
                    createdTuition =
                            TuitionServices
                                    .createInferedTuitionForRegistration(registration, executionYear, enrolmentDate, false);
                }

            }
        }

        if (TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear) == null) {
            // Did not create exit with nothing
            return null;
        }

        final AcademicTreasuryEvent academicTreasuryEvent =
                TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);

        if (!academicTreasuryEvent.isChargedWithDebitEntry(product)) {
            return null;
        }

        return DebitEntry.findActive(academicTreasuryEvent, product).findFirst().get();
    }
 
}
