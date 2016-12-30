package org.fenixedu.academictreasury.dto.reports;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.ReimbursementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

public class ReimbursementReportEntryBean implements SpreadsheetRow {

    public static String[] SPREADSHEET_HEADERS = { Constants.bundle("label.ReimbursementReportEntryBean.header.identification"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.creationDate"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.responsible"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.settlementNoteNumber"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.settlementNoteDocumentDate"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.reimbursementDate"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.settlementNoteAnnuled"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.documentExportationPending"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.paymentMethod"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.amount"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.customerId"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.debtAccountId"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.name"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.identificationType"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.identificationNumber"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.vatNumber"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.email"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.address"),
            Constants.bundle("label.ReimbursementReportEntryBean.header.studentNumber") };

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
    private String amount;
    private String customerId;
    private String debtAccountId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private Integer studentNumber;

    public ReimbursementReportEntryBean(final ReimbursementEntry entry, final DebtReportRequest request, final ErrorsLog errorsLog) {
        final String decimalSeparator = request.getDecimalSeparator();
        
        paymentEntry = entry;

        try {
            final SettlementNote settlementNote = entry.getSettlementNote();

            this.identification = entry.getExternalId();
            this.creationDate = entry.getVersioningCreationDate();
            this.responsible = entry.getVersioningCreator();
            this.settlementNoteNumber = settlementNote.getUiDocumentNumber();
            this.settlementNoteDocumentDate = settlementNote.getDocumentDate();
            this.paymentDate = settlementNote.getPaymentDate();
            this.settlementNoteAnnuled = settlementNote.isAnnulled();
            this.documentExportationPending = settlementNote.isDocumentToExport();
            this.paymentMethod = entry.getPaymentMethod().getName().getContent();
            this.amount = settlementNote.getDebtAccount().getFinantialInstitution().getCurrency()
                    .getValueWithScale(entry.getReimbursedAmount()).toString();

            if(DebtReportRequest.COMMA.equals(decimalSeparator)) {
                this.amount = this.amount.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
            }
            
            fillStudentInformation(entry);

            this.completed = true;

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(entry, e);
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
            row.createCell(i++).setCellValue(valueOrEmpty(paymentMethod));
            row.createCell(i++).setCellValue(amount.toString());
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
