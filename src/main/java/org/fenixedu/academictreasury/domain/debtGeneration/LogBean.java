package org.fenixedu.academictreasury.domain.debtGeneration;

import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.paymentcodes.FinantialDocumentPaymentCode;
import org.joda.time.DateTime;

public class LogBean {
    public DateTime processDate = new DateTime();
    private final StringBuilder log = new StringBuilder();

    public void registerException(final Registration registration, final Exception e) {
        log.append(String.format("The registration of student '%s' [%s] has the following exception: '%s'\n", registration
                .getStudent().getNumber(), registration.getDegreeName(), e.getMessage()));
        log.append(ExceptionUtils.getStackTrace(e));
        log.append("\n");
    }

    public void registerCreatedAcademicTreasuryEvent(final Registration registration, final AcademicTax academicTax) {
        log.append(String.format("Created academic event treasury for student '%s' [%s] for academic tax: '%s'", registration
                .getStudent().getNumber(), registration.getDegreeName(), academicTax.getProduct().getName().getContent()));
        log.append("\n");
    }

    public void registerDebitNoteCreation(Registration registration, DebitNote debitNote) {
        log.append(String.format("Created debit note for student '%s' [%s] on finantial institution: '%s'", registration
                .getStudent().getNumber(), registration.getDegreeName(), debitNote.getDebtAccount().getFinantialInstitution()
                .getName()));
        log.append("\n");
    }

    public void registerDebitNoteClosing(Registration registration, DebitNote debitNote) {
        log.append(String.format("Closing debit note for student '%s' [%s] on finantial institution: '%s'", registration
                .getStudent().getNumber(), registration.getDegreeName(), debitNote.getDebtAccount().getFinantialInstitution()
                .getName()));
        log.append("\n");
    }

    public void registerDebitEntriesOnDebitNote(Registration registration, DebitNote debitNote) {
        final String description =
                String.join(
                        ", ",
                        debitNote.getFinantialDocumentEntriesSet().stream()
                                .map(l -> ((DebitEntry) l).getProduct().getName().getContent()).collect(Collectors.toSet()));

        log.append(String.format("Closing debit note for student '%s' [%s] with entries: '%s'", registration.getStudent()
                .getNumber(), registration.getDegreeName(), debitNote.getDebtAccount().getFinantialInstitution().getName(),
                description));
        log.append("\n");
    }

    public void registerCreatedTuition(Registration registration) {
        log.append(String.format("Created tuition for student '%s' [%s]", registration.getStudent().getNumber(),
                registration.getDegreeName()));
        log.append("\n");
    }

    public void registerCreatedPaymentReference(Registration registration, FinantialDocumentPaymentCode paymentCode) {
        log.append(String.format("Created payment code '%s' [%s] with reference: '%s'",
                registration.getStudent().getNumber(), registration.getDegreeName(), paymentCode.getPaymentReferenceCode()
                        .getReferenceCode()));
        log.append("\n");
    }

    public void registerAllWithClosedDebitNote(Registration registration) {
        log.append(String.format("All are in the same debit note on student '%s' [%s]",
                registration.getStudent().getNumber(), registration.getDegreeName()));
        log.append("\n");
    }

    public void registerDebitEntriesWithDifferentClosedDebitNotes(Registration registration, DebitNote debitNote) {
        log.append(String.format("Debit entries with different debit notes on student '%s' [%s]", registration.getStudent()
                .getNumber(), registration.getDegreeName()));
        log.append("\n");
    }

    public void registerWithoutDebitEntriesToProcess(Registration registration) {
        log.append(String.format("Without debit entries to process on student '%s' [%s]", registration.getStudent()
                .getNumber(), registration.getDegreeName()));
        log.append("\n");
    }

    public void registerStudentNotActiveInExecutionYear(final Registration registration, final ExecutionYear executionYear) {
        log.append(String.format("Student not active '%s' [%s - %s]", registration.getStudent().getNumber(),
                registration.getDegreeName(), executionYear.getQualifiedName()));
        log.append("\n");
    }

    public void registerStudentWithNoEnrolments(final Registration registration, final ExecutionYear executionYear) {
        log.append(String.format("Student with no enrolments '%s' [%s - %s]", registration.getStudent().getNumber(),
                registration.getDegreeName(), executionYear.getQualifiedName()));
        log.append("\n");
    }
}