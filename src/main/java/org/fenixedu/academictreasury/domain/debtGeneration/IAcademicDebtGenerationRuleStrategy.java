package org.fenixedu.academictreasury.domain.debtGeneration;

public interface IAcademicDebtGenerationRuleStrategy {

    public boolean isAppliedOnTuitionDebitEntries();
    public boolean isAppliedOnAcademicTaxDebitEntries();
    public boolean isAppliedOnOtherDebitEntries();
    
    public boolean isToCreateDebitEntries();
    public boolean isToAggregateDebitEntries();
    public boolean isToCloseDebitNotes();
    public boolean isToCreatePaymentReferenceCodes();
    
    public void process(final AcademicDebtGenerationRule rule);
    
    
}
