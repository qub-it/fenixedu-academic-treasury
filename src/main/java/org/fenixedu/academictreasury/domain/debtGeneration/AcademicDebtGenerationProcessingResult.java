package org.fenixedu.academictreasury.domain.debtGeneration;

import java.io.Serializable;

import org.fenixedu.academic.domain.student.Registration;
import org.joda.time.DateTime;

public class AcademicDebtGenerationProcessingResult implements Serializable {

    private AcademicDebtGenerationRule academicDebtGenerationRule;
    private Registration registration;
    
    private Exception exception;

    private DateTime processingStartDate;
    private DateTime processingEndDate;
    
    public AcademicDebtGenerationProcessingResult(final AcademicDebtGenerationRule rule, final Registration registration) {
        this.processingStartDate = new DateTime();
        this.academicDebtGenerationRule = rule;
        this.registration = registration;
    }

    public void markProcessingEndDateTime() {
        this.processingEndDate = new DateTime();
    }

    public void markException(final Exception e) {
        this.exception = e;
    }
    
    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on
    
    public AcademicDebtGenerationRule getAcademicDebtGenerationRule() {
        return academicDebtGenerationRule;
    }
    
    public Registration getRegistration() {
        return registration;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public DateTime getProcessingStartDate() {
        return processingStartDate;
    }
    
    public DateTime getProcessingEndDate() {
        return processingEndDate;
    }
    
}
