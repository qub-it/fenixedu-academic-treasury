package org.fenixedu.academictreasury.domain.debtGeneration;

import org.fenixedu.academic.domain.student.Registration;

public interface IAcademicDebtGenerationRuleStrategy {

    public boolean isAppliedOnTuitionDebitEntries();
    public boolean isAppliedOnAcademicTaxDebitEntries();
    public boolean isAppliedOnOtherDebitEntries();
    
    public boolean isToCreateDebitEntries();
    public boolean isToAggregateDebitEntries();
    public boolean isToCloseDebitNotes();
    public boolean isToCreatePaymentReferenceCodes();
    
    public void process(final AcademicDebtGenerationRule rule);
    public void process(final AcademicDebtGenerationRule rule, final Registration registration);
    public void process(final AcademicDebtGenerationRule rule, final Registration registration, LogBean logBean);
    
    
}
