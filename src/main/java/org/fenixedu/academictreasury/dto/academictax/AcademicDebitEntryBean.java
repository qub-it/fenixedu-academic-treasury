package org.fenixedu.academictreasury.dto.academictax;

import java.math.BigDecimal;

import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.LocalDate;

public class AcademicDebitEntryBean {

    private LocalizedString description;
    private LocalDate dueDate;
    private BigDecimal vatRate;
    private BigDecimal amount;

    public AcademicDebitEntryBean() {
    }
    
    public AcademicDebitEntryBean(final LocalizedString description, final LocalDate dueDate, final BigDecimal vatRate,
            final BigDecimal amount) {
        this.description = description;
        this.dueDate = dueDate;
        this.vatRate = vatRate;
        this.amount = amount;
    }

    public LocalizedString getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
