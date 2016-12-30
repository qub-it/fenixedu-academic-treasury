package org.fenixedu.academictreasury.dto.reports;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

public class SettlementReportEntryBean implements SpreadsheetRow {

    public static String[] SPREADSHEET_HEADERS = { 
        Constants.bundle("label.SettlementReportEntryBean.header.identification"),
        Constants.bundle("label.SettlementReportEntryBean.header.creationDate"),
        Constants.bundle("label.SettlementReportEntryBean.header.responsible"),
        Constants.bundle("label.SettlementReportEntryBean.header.settlementNoteNumber"),
        Constants.bundle("label.SettlementReportEntryBean.header.settlementNoteDocumentDate"),
        Constants.bundle("label.SettlementReportEntryBean.header.paymentDate"),
        Constants.bundle("label.SettlementReportEntryBean.header.settlementNoteAnnuled"),
        Constants.bundle("label.SettlementReportEntryBean.header.documentExportationPending"),
        Constants.bundle("label.SettlementReportEntryBean.header.settlementEntryOrder"),
        Constants.bundle("label.SettlementReportEntryBean.header.amount"),
        Constants.bundle("label.SettlementReportEntryBean.header.productCode"),
        Constants.bundle("label.SettlementReportEntryBean.header.settlementEntryDescription"),
        Constants.bundle("label.SettlementReportEntryBean.header.invoiceEntryIdentification"),
        Constants.bundle("label.SettlementReportEntryBean.header.invoiceEntryType"),        
        Constants.bundle("label.SettlementReportEntryBean.header.invoiceEntryAmountToPay"),
        Constants.bundle("label.SettlementReportEntryBean.header.invoiceDocumentNumber"),
        Constants.bundle("label.SettlementReportEntryBean.header.customerId"),
        Constants.bundle("label.SettlementReportEntryBean.header.debtAccountId"),
        Constants.bundle("label.SettlementReportEntryBean.header.name"),
        Constants.bundle("label.SettlementReportEntryBean.header.identificationType"),
        Constants.bundle("label.SettlementReportEntryBean.header.identificationNumber"),
        Constants.bundle("label.SettlementReportEntryBean.header.vatNumber"),
        Constants.bundle("label.SettlementReportEntryBean.header.email"),
        Constants.bundle("label.SettlementReportEntryBean.header.address"),
        Constants.bundle("label.SettlementReportEntryBean.header.studentNumber") };
 
    private SettlementEntry settlementEntry;
    private boolean completed;
    
    private String identification;
    private DateTime creationDate;
    private String responsible;
    private String invoiceEntryIdentification;
    private String invoiceEntryType;
    private String invoiceEntryAmountToPay;
    private String invoiceDocumentNumber;
    private String settlementNoteNumber;
    private DateTime settlementNoteDocumentDate;
    private DateTime paymentDate;
    private boolean settlementNoteAnnuled;
    private boolean documentExportationPending;
    private Integer settlementEntryOrder;
    private String amount;
    private String productCode;
    private String settlementEntryDescription;
    private String customerId;
    private String debtAccountId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private Integer studentNumber;

    public SettlementReportEntryBean(final SettlementEntry entry, final DebtReportRequest request, final ErrorsLog errorsLog) {
        final String decimalSeparator = request.getDecimalSeparator();
        
        this.settlementEntry = entry;

        try {
            final SettlementNote settlementNote = (SettlementNote) entry.getFinantialDocument();
            final Currency currency = settlementNote.getDebtAccount().getFinantialInstitution().getCurrency();
            
            this.identification = entry.getExternalId();
            this.creationDate = entry.getVersioningCreationDate();
            this.responsible = entry.getVersioningCreator();
            this.invoiceEntryIdentification = entry.getInvoiceEntry().getExternalId();
            this.settlementNoteNumber = settlementNote.getUiDocumentNumber();
            this.settlementNoteDocumentDate = settlementNote.getDocumentDate();
            this.paymentDate = settlementNote.getPaymentDate();
            this.settlementNoteAnnuled = settlementNote.isAnnulled();
            this.documentExportationPending = settlementNote.isDocumentToExport();
            this.invoiceEntryType = entryType(entry.getInvoiceEntry());
            this.invoiceEntryAmountToPay = currency.getValueWithScale(entry.getInvoiceEntry().getAmountWithVat()).toString();
            this.invoiceDocumentNumber = entry.getInvoiceEntry().getFinantialDocument().getUiDocumentNumber();
            this.settlementEntryOrder = entry.getEntryOrder();
            this.amount =
                    settlementNote.getDebtAccount().getFinantialInstitution().getCurrency()
                            .getValueWithScale(entry.getTotalAmount()).toString();
            
            if(DebtReportRequest.COMMA.equals(decimalSeparator)) {
                this.invoiceEntryAmountToPay = this.invoiceEntryAmountToPay.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                this.amount = this.amount.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
            }
            
            this.productCode = entry.getInvoiceEntry().getProduct().getCode();
            this.settlementEntryDescription = entry.getDescription();
            
            fillStudentInformation(entry);
            
            this.completed = true;
        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(entry, e);
        }

    }
    
    private void fillStudentInformation(final SettlementEntry entry) {
        final Customer customer = entry.getFinantialDocument().getDebtAccount().getCustomer();

        this.customerId = customer.getExternalId();
        this.debtAccountId = entry.getFinantialDocument().getDebtAccount().getExternalId();
        
        this.name = customer.getName();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null && ((PersonCustomer) customer).getPerson().getIdDocumentType() != null) {
            this.identificationType = ((PersonCustomer) customer).getPerson().getIdDocumentType().getLocalizedNameI18N();
        } else if(customer.isPersonCustomer() && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null && ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getIdDocumentType() != null) {
            this.identificationType = ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getIdDocumentType().getLocalizedNameI18N();
        }

        this.identificationNumber = customer.getIdentificationNumber();
        this.vatNumber = customer.getUiFiscalNumber();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null) {
            this.email = ((PersonCustomer) customer).getPerson().getInstitutionalOrDefaultEmailAddressValue();
        } else if(customer.isPersonCustomer() && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null) {
            this.email = ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getInstitutionalOrDefaultEmailAddressValue();
        }

        this.address = customer.getAddress();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null && ((PersonCustomer) customer).getPerson().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getPerson().getStudent().getNumber();
        } else if(customer.isPersonCustomer() && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null && ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getStudent().getNumber();
        }
    }

    private String entryType(final InvoiceEntry entry) {
        if (entry.isDebitNoteEntry()) {
            return Constants.bundle("label.DebtReportEntryBean.debitNoteEntry");
        } else if (entry.isCreditNoteEntry()) {
            return Constants.bundle("label.DebtReportEntryBean.creditNoteEntry");
        }

        return null;
    }

    @Override
    public void writeCellValues(final Row row, final IErrorsLog ierrorsLog) {
        final ErrorsLog errorsLog = (ErrorsLog) ierrorsLog;
        
        try {
            row.createCell(0).setCellValue(identification);

            if(!completed) {
                row.createCell(1).setCellValue(Constants.bundle("error.DebtReportEntryBean.report.generation.verify.entry"));
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
            row.createCell(i++).setCellValue(valueOrEmpty(settlementEntryOrder));
            row.createCell(i++).setCellValue(amount.toString());
            row.createCell(i++).setCellValue(valueOrEmpty(productCode));
            row.createCell(i++).setCellValue(valueOrEmpty(settlementEntryDescription));
            row.createCell(i++).setCellValue(valueOrEmpty(invoiceEntryIdentification));
            row.createCell(i++).setCellValue(valueOrEmpty(invoiceEntryType));
            row.createCell(i++).setCellValue(valueOrEmpty(invoiceEntryAmountToPay));
            row.createCell(i++).setCellValue(valueOrEmpty(invoiceDocumentNumber));
            row.createCell(i++).setCellValue(customerId);
            row.createCell(i++).setCellValue(debtAccountId);
            row.createCell(i++).setCellValue(valueOrEmpty(name));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(email));
            row.createCell(i++).setCellValue(valueOrEmpty(address));
            row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
            
        } catch(final Exception e) {
            e.printStackTrace();
            errorsLog.addError(settlementEntry, e);
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

        return Constants.bundle(value ? "label.true" : "label.false");
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

    
}
