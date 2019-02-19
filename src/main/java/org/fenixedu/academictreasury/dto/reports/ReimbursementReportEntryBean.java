package org.fenixedu.academictreasury.dto.reports;

import static org.fenixedu.academictreasury.util.Constants.academicTreasuryBundle;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.ReimbursementEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

public class ReimbursementReportEntryBean implements SpreadsheetRow {

    public static String[] SPREADSHEET_HEADERS = { academicTreasuryBundle("label.ReimbursementReportEntryBean.header.identification"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.creationDate"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.responsible"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.settlementNoteNumber"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.settlementNoteDocumentDate"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.reimbursementDate"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.settlementNoteAnnuled"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.documentExportationPending"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.paymentMethod"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.amount"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.customerId"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.debtAccountId"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.name"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.identificationType"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.identificationNumber"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.vatNumber"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.email"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.address"),
            academicTreasuryBundle("label.ReimbursementReportEntryBean.header.studentNumber") };

    private ReimbursementEntry paymentEntry;
    private boolean completed;

    private String identification;
    private DateTime creationDate;
    private String responsible;
    private String settlementNoteNumber;
    private DateTime settlementNoteDocumentDate;
    private DateTime paymentDate;
    private boolean settlementNoteAnnuled;
    private boolean documentExportationPending;
    private String paymentMethod;
    private BigDecimal amount;
    private String customerId;
    private String debtAccountId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private Integer studentNumber;

    private DateTime closeDate;
    private Boolean exportedInLegacyERP;

    private LocalDate erpCertificationDate;
    private String erpCertificateDocumentReference;
    
    private String erpCustomerId;
    private String erpPayorCustomerId;
    
    private String decimalSeparator;
    
    public ReimbursementReportEntryBean(final ReimbursementEntry entry, final DebtReportRequest request, final ErrorsLog errorsLog) {
        this.decimalSeparator = request != null ? request.getDecimalSeparator() : DebtReportRequest.DOT;
        
        paymentEntry = entry;

        try {
            final SettlementNote settlementNote = entry.getSettlementNote();

            this.identification = entry.getExternalId();
            this.creationDate = TreasuryPlataformDependentServicesFactory.implementation().versioningCreationDate(entry);
            this.responsible = TreasuryPlataformDependentServicesFactory.implementation().versioningCreatorUsername(entry);
            this.settlementNoteNumber = settlementNote.getUiDocumentNumber();
            this.settlementNoteDocumentDate = settlementNote.getDocumentDate();
            this.paymentDate = settlementNote.getPaymentDate();
            this.settlementNoteAnnuled = settlementNote.isAnnulled();
            this.documentExportationPending = settlementNote.isDocumentToExport();
            this.paymentMethod = entry.getPaymentMethod().getName().getContent();
            this.amount = settlementNote.getDebtAccount().getFinantialInstitution().getCurrency()
                    .getValueWithScale(entry.getReimbursedAmount());

            fillStudentInformation(entry);

            fillERPInformation(settlementNote);
            
            this.completed = true;

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(entry, e);
        }
    }

    private void fillERPInformation(final SettlementNote settlementNote) {
        this.closeDate = settlementNote != null ? settlementNote.getCloseDate() : null;
        this.exportedInLegacyERP =
                settlementNote != null ? settlementNote.isExportedInLegacyERP() : false;

        this.erpCertificationDate =
                settlementNote != null ? settlementNote.getErpCertificationDate() : null;

        this.erpCertificateDocumentReference = settlementNote != null ? settlementNote
                .getErpCertificateDocumentReference() : null;

        this.erpCustomerId = settlementNote.getDebtAccount().getCustomer().getErpCustomerId();

        
        if(!settlementNote.getSettlemetEntriesSet().isEmpty()) {
            final SettlementEntry settlementEntry = settlementNote.getSettlemetEntriesSet().iterator().next();
            if(settlementEntry.getInvoiceEntry().getFinantialDocument() != null && ((Invoice) settlementEntry.getInvoiceEntry().getFinantialDocument()).getPayorDebtAccount() != null) {
                this.erpPayorCustomerId = ((Invoice) settlementEntry.getInvoiceEntry().getFinantialDocument()).getPayorDebtAccount().getCustomer().getErpCustomerId();
            }
        }
    }

    private void fillStudentInformation(final ReimbursementEntry entry) {
        final Customer customer = entry.getSettlementNote().getDebtAccount().getCustomer();

        this.customerId = customer.getExternalId();
        this.debtAccountId = entry.getSettlementNote().getDebtAccount().getExternalId();

        this.name = customer.getName();

        if (customer.isPersonCustomer() 
                && ((PersonCustomer) customer).getPerson() != null
                && ((PersonCustomer) customer).getPerson().getIdDocumentType() != null) {
            this.identificationType = ((PersonCustomer) customer).getPerson().getIdDocumentType().getLocalizedNameI18N();
        } else if (customer.isPersonCustomer() 
                && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null
                && ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getIdDocumentType() != null) {
            this.identificationType =
                    ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getIdDocumentType().getLocalizedNameI18N();
        }

        this.identificationNumber = customer.getIdentificationNumber();
        this.vatNumber = customer.getUiFiscalNumber();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null) {
            this.email = ((PersonCustomer) customer).getPerson().getInstitutionalOrDefaultEmailAddressValue();
        } else if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null) {
            this.email =
                    ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getInstitutionalOrDefaultEmailAddressValue();
        }

        this.address = customer.getAddress();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null && ((PersonCustomer) customer).getPerson().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getPerson().getStudent().getNumber();
        } else if(customer.isPersonCustomer() 
                && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null 
                && ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getStudent().getNumber();
        }
    }

    @Override
    public void writeCellValues(final Row row, final IErrorsLog ierrorsLog) {
        final ErrorsLog errorsLog = (ErrorsLog) ierrorsLog;

        try {
            row.createCell(0).setCellValue(identification);

            if (!completed) {
                row.createCell(1).setCellValue(academicTreasuryBundle("error.DebtReportEntryBean.report.generation.verify.entry"));
                return;
            }

            int i = 1;

            row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
            row.createCell(i++).setCellValue(valueOrEmpty(responsible));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementNoteNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementNoteDocumentDate));
            row.createCell(i++).setCellValue(valueOrEmpty(paymentDate));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementNoteAnnuled));
            row.createCell(i++).setCellValue(valueOrEmpty(documentExportationPending));
            row.createCell(i++).setCellValue(valueOrEmpty(paymentMethod));
            
            {
                String value = amount != null ? amount.toString() : "";
                if(DebtReportRequest.COMMA.equals(decimalSeparator)) {
                    value = value.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                }
                
                row.createCell(i++).setCellValue(value);
            }
            
            row.createCell(i++).setCellValue(customerId);
            row.createCell(i++).setCellValue(debtAccountId);
            row.createCell(i++).setCellValue(valueOrEmpty(name));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(email));
            row.createCell(i++).setCellValue(valueOrEmpty(address));
            row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(paymentEntry, e);
        }
    }

    private String valueOrEmpty(final DateTime value) {
        if (value == null) {
            return "";
        }

        return value.toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD);
    }

    private String valueOrEmpty(final Boolean value) {
        if (value == null) {
            return "";
        }

        return academicTreasuryBundle(value ? "label.true" : "label.false");
    }

    private String valueOrEmpty(final Integer value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private String valueOrEmpty(final LocalizedString value) {
        if (value == null) {
            return "";
        }

        if (StringUtils.isEmpty(value.getContent())) {
            return "";
        }

        return value.getContent();
    }

    private String valueOrEmpty(final String value) {
        if (!StringUtils.isEmpty(value)) {
            return value;
        }

        return "";
    }

    // @formatter:off
    /* *****************
     * GETTERS & SETTERS
     * *****************
     */
    // @formatter:on
    
    public ReimbursementEntry getPaymentEntry() {
        return paymentEntry;
    }

    public void setPaymentEntry(ReimbursementEntry paymentEntry) {
        this.paymentEntry = paymentEntry;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getSettlementNoteNumber() {
        return settlementNoteNumber;
    }

    public void setSettlementNoteNumber(String settlementNoteNumber) {
        this.settlementNoteNumber = settlementNoteNumber;
    }

    public DateTime getSettlementNoteDocumentDate() {
        return settlementNoteDocumentDate;
    }

    public void setSettlementNoteDocumentDate(DateTime settlementNoteDocumentDate) {
        this.settlementNoteDocumentDate = settlementNoteDocumentDate;
    }

    public DateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(DateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isSettlementNoteAnnuled() {
        return settlementNoteAnnuled;
    }

    public void setSettlementNoteAnnuled(boolean settlementNoteAnnuled) {
        this.settlementNoteAnnuled = settlementNoteAnnuled;
    }

    public boolean isDocumentExportationPending() {
        return documentExportationPending;
    }

    public void setDocumentExportationPending(boolean documentExportationPending) {
        this.documentExportationPending = documentExportationPending;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDebtAccountId() {
        return debtAccountId;
    }

    public void setDebtAccountId(String debtAccountId) {
        this.debtAccountId = debtAccountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalizedString getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(LocalizedString identificationType) {
        this.identificationType = identificationType;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(Integer studentNumber) {
        this.studentNumber = studentNumber;
    }

    public DateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(DateTime closeDate) {
        this.closeDate = closeDate;
    }

    public Boolean getExportedInLegacyERP() {
        return exportedInLegacyERP;
    }

    public void setExportedInLegacyERP(Boolean exportedInLegacyERP) {
        this.exportedInLegacyERP = exportedInLegacyERP;
    }

    public LocalDate getErpCertificationDate() {
        return erpCertificationDate;
    }

    public void setErpCertificationDate(LocalDate erpCertificationDate) {
        this.erpCertificationDate = erpCertificationDate;
    }

    public String getErpCertificateDocumentReference() {
        return erpCertificateDocumentReference;
    }

    public void setErpCertificateDocumentReference(String erpCertificateDocumentReference) {
        this.erpCertificateDocumentReference = erpCertificateDocumentReference;
    }

    public String getErpCustomerId() {
        return erpCustomerId;
    }

    public void setErpCustomerId(String erpCustomerId) {
        this.erpCustomerId = erpCustomerId;
    }

    public String getErpPayorCustomerId() {
        return erpPayorCustomerId;
    }

    public void setErpPayorCustomerId(String erpPayorCustomerId) {
        this.erpPayorCustomerId = erpPayorCustomerId;
    }

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    
}
