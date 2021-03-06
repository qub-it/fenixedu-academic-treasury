package org.fenixedu.academictreasury.domain.debtGeneration.strategies;

import static org.fenixedu.academictreasury.domain.debtGeneration.IAcademicDebtGenerationRuleStrategy.findActiveDebitEntries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationProcessingResult;
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
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class CreateDebtsStrategy implements IAcademicDebtGenerationRuleStrategy {

    private static Logger logger = LoggerFactory.getLogger(CreateDebtsStrategy.class);

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
        return true;
    }

    @Override
    public boolean isToAggregateDebitEntries() {
        return true;
    }

    @Override
    public boolean isToCloseDebitNote() {
        return false;
    }

    @Override
    public boolean isToCreatePaymentReferenceCodes() {
        return false;
    }

    @Override
    public boolean isEntriesRequired() {
        return true;
    }

    @Override
    public boolean isToAlignAcademicTaxesDueDate() {
        return false;
    }

    @Override
    @Atomic(mode = TxMode.READ)
    public List<AcademicDebtGenerationProcessingResult> process(final AcademicDebtGenerationRule rule) {

        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        final List<AcademicDebtGenerationProcessingResult> resultList = Lists.newArrayList();
        for (final DegreeCurricularPlan degreeCurricularPlan : rule.getDegreeCurricularPlansSet()) {
            for (final Registration registration : degreeCurricularPlan.getRegistrations()) {

                if (isToDiscard(rule, registration)) {
                    continue;
                }

                final AcademicDebtGenerationProcessingResult result =
                        new AcademicDebtGenerationProcessingResult(rule, registration);
                resultList.add(result);
                try {
                    processDebtsForRegistration(rule, registration);
                    result.markProcessingEndDateTime();
                } catch (final AcademicTreasuryDomainException e) {
                    result.markException(e);
                    logger.debug(e.getMessage());
                } catch (final Exception e) {
                    result.markException(e);
                    e.printStackTrace();
                }
            }
        }

        return resultList;
    }

    @Override
    @Atomic(mode = TxMode.READ)
    public List<AcademicDebtGenerationProcessingResult> process(final AcademicDebtGenerationRule rule,
            final Registration registration) {
        if (!rule.isActive()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.active.to.process");
        }

        final AcademicDebtGenerationProcessingResult result = new AcademicDebtGenerationProcessingResult(rule, registration);
        try {

            if (isToDiscard(rule, registration)) {
                return Lists.newArrayList();
            }

            // TODO legidio, should the same try/catch as above be applied?
            processDebtsForRegistration(rule, registration);
            result.markProcessingEndDateTime();
        } catch (final AcademicTreasuryDomainException e) {
            result.markException(e);
            logger.debug(e.getMessage());
        } catch (final Exception e) {
            result.markException(e);
            e.printStackTrace();
        }

        return Lists.newArrayList(result);

    }

    static private boolean isToDiscard(final AcademicDebtGenerationRule rule, final Registration registration) {

        if (rule.getDebtGenerationRuleRestriction() != null
                && !rule.getDebtGenerationRuleRestriction().strategyImplementation().isToApply(rule, registration)) {
            return true;
        }

        final ExecutionYear year = rule.getExecutionYear();
        final StudentCurricularPlan scp = registration.getStudentCurricularPlan(year);
        if (scp == null) {
            return true;
        }

        if (!rule.getDegreeCurricularPlansSet().contains(scp.getDegreeCurricularPlan())) {
            return true;
        }

        // Discard registrations not active and with no enrolments
        if (!registration.hasAnyActiveState(year)) {
            return true;
        }

        if (registration.getRegistrationDataByExecutionYearSet().stream().noneMatch(i -> i.getExecutionYear() == year)) {

            // only return is this rule has not entry that forces creation
            if (!isRuleWithOnlyOneAcademicTaxEntryForcingCreation(rule)) {
                return true;
            }
        }

        return false;
    }

    static private boolean isRuleWithOnlyOneAcademicTaxEntryForcingCreation(final AcademicDebtGenerationRule rule) {
        if (rule.getAcademicDebtGenerationRuleEntriesSet().size() != 1) {
            return false;
        }

        if (!AcademicTax.findUnique(rule.getAcademicDebtGenerationRuleEntriesSet().iterator().next().getProduct()).isPresent()) {
            return false;
        }

        return rule.getAcademicDebtGenerationRuleEntriesSet().iterator().next().isForceCreation();
    }

    @Atomic(mode = TxMode.WRITE)
    private void processDebtsForRegistration(final AcademicDebtGenerationRule rule, final Registration registration) {

        // For each product try to grab or create if requested
        final Set<DebitEntry> debitEntries = Sets.newHashSet();

        DebitEntry grabbedDebitEntry = null;
        for (final AcademicDebtGenerationRuleEntry entry : rule
                .getOrderedAcademicDebtGenerationRuleEntries() /* rule.getAcademicDebtGenerationRuleEntriesSet() */) {
            final Product product = entry.getProduct();

            if (AcademicTreasurySettings.getInstance().getTuitionProductGroup() == product.getProductGroup()) {
                grabbedDebitEntry = grabOrCreateDebitEntryForTuition(rule, registration, entry);
            } else if (AcademicTax.findUnique(product).isPresent()) {
                // Check if the product is an academic tax
                grabbedDebitEntry = grabOrCreateDebitEntryForAcademicTax(rule, registration, entry);
            }

            if (grabbedDebitEntry != null) {
                debitEntries.add(grabbedDebitEntry);
            }
        }

        if (debitEntries.isEmpty()) {
            return;
        }

        if (!rule.isAggregateOnDebitNote()) {
            return;
        }

        DebitNote debitNote = grabPreparingOrCreateDebitNote(debitEntries);

        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() == null) {
                debitEntry.setFinantialDocument(debitNote);
            }

            if (debitNote.getPayorDebtAccount() == null && debitEntry.getPayorDebtAccount() != null) {
                debitNote.updatePayorDebtAccount(debitEntry.getPayorDebtAccount());
            }

            if (debitEntry.getDebtAccount() != debitNote.getDebtAccount()) {
                throw new AcademicTreasuryDomainException(
                        "error.AcademicDebtGenerationRule.debitEntry.debtAccount.not.equal.to.debitNote.debtAccount");
            }
        }

        if (debitNote.getFinantialDocumentEntriesSet().isEmpty()) {
            throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.debit.note.without.debit.entries");
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

        if (rule.isEventDebitEntriesMustEqualRuleProducts()) {
            final TreasuryEvent treasuryEvent = debitEntries.iterator().next().getTreasuryEvent();

            // First ensure all academic debitEntries all from same academic event
            for (final DebitEntry db : debitEntries) {
                if (db.getTreasuryEvent() != treasuryEvent) {
                    throw new AcademicTreasuryDomainException(
                            "error.AcademicDebtGenerationRule.debitEntry.not.from.same.academic.event");
                }

                final Product interestProduct = TreasurySettings.getInstance().getInterestProduct();
                final Set<DebitEntry> treasuryEventDebitEntries = DebitEntry.findActive(treasuryEvent)
                        .filter(d -> d.getProduct() != interestProduct).collect(Collectors.<DebitEntry> toSet());

                if (!treasuryEventDebitEntries.equals(debitEntries)) {
                    throw new AcademicTreasuryDomainException("error.AcademicDebtGenerationRule.not.all.debitEntries.aggregated");
                }
            }
        }
    }

    private DebitEntry grabOrCreateDebitEntryForAcademicTax(final AcademicDebtGenerationRule rule,
            final Registration registration, final AcademicDebtGenerationRuleEntry entry) {
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
                boolean forceCreation =
                        entry.isCreateDebt() && entry.isForceCreation() && registration.isFirstTime(rule.getExecutionYear());

                AcademicTaxServices.createAcademicTaxForEnrolmentDateAndDefaultFinantialEntity(registration, executionYear, academicTax, forceCreation);
            }
        }

        final PersonCustomer customer = registration.getPerson().getPersonCustomer();

        if (customer == null) {
            return null;
        }

        final AcademicTreasuryEvent t = AcademicTaxServices.findAcademicTreasuryEvent(registration, executionYear, academicTax);

        if (t != null && t.isChargedWithDebitEntry()) {
            return findActiveDebitEntries(customer, t).filter(d -> d.isInDebt()).findFirst().orElse(null);
        }

        return null;
    }

    private DebitEntry grabOrCreateDebitEntryForTuition(final AcademicDebtGenerationRule rule, final Registration registration,
            final AcademicDebtGenerationRuleEntry entry) {
        final Product product = entry.getProduct();
        final ExecutionYear executionYear = rule.getExecutionYear();

        // Is of tuition kind try to catch the tuition event
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
                        TuitionServices.createInferedTuitionForRegistration(registration, executionYear, lastRegisteredStateDate,
                                false);
                    }
                } else {
                    final LocalDate enrolmentDate = TuitionServices.enrolmentDate(registration, executionYear, false);
                    TuitionServices.createInferedTuitionForRegistration(registration, executionYear, enrolmentDate, false);
                }
            }
        }

        if (TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear) == null) {
            // Did not create exit with nothing
            return null;
        }

        final AcademicTreasuryEvent t =
                TuitionServices.findAcademicTreasuryEventTuitionForRegistration(registration, executionYear);

        if (!t.isChargedWithDebitEntry(product)) {
            return null;
        }

        final PersonCustomer customer = registration.getPerson().getPersonCustomer();
        if (customer == null) {
            return null;
        }

        return findActiveDebitEntries(customer, t, product).filter(d -> d.isInDebt()).findFirst().orElse(null);
    }

    private DebitNote grabPreparingOrCreateDebitNote(final Set<DebitEntry> debitEntries) {

        for (final DebitEntry debitEntry : debitEntries) {
            if (debitEntry.getFinantialDocument() != null && debitEntry.getFinantialDocument().isPreparing()) {
                return (DebitNote) debitEntry.getFinantialDocument();
            }
        }

        final DebitNote debitNote =
                DebitNote
                        .create(debitEntries.iterator().next().getDebtAccount(),
                                DocumentNumberSeries.findUniqueDefault(FinantialDocumentType.findForDebitNote(),
                                        debitEntries.iterator().next().getDebtAccount().getFinantialInstitution()).get(),
                                new DateTime());

        return debitNote;
    }

}
