package org.fenixedu.academictreasury.dto.reports;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;

public class DebtAccountReportEntryBean implements SpreadsheetRow {

    // @formatter:off
    public static String[] SPREADSHEET_HEADERS = { 
            Constants.bundle("label.DebtAccountReportEntryBean.header.identification"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.versioningCreator"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.creationDate"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.finantialInstitutionName"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.customerId"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.name"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.identificationType"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.identificationNumber"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.vatNumber"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.email"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.address"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.countryCode"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.studentNumber"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.vatNumberValid"),
            Constants.bundle("label.DebtAccountReportEntryBean.header.totalInDebt") };
    // @formatter:on

    final DebtAccount debtAccount;
    boolean completed = false;

    private String identification;
    private String versioningCreator;
    private DateTime creationDate;
    private String finantialInstitutionName;
    private String customerId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private String countryCode;
    private Integer studentNumber;
    private boolean vatNumberValid;
    private String totalInDebt;

    public DebtAccountReportEntryBean(final DebtAccount debtAccount, final DebtReportRequest request, final ErrorsLog errorsLog) {
        final String decimalSeparator = request.getDecimalSeparator();
        
        this.debtAccount = debtAccount;

        try {
            this.identification = debtAccount.getExternalId();
            this.versioningCreator = debtAccount.getVersioningCreator();
            this.creationDate = debtAccount.getVersioningCreationDate();
            this.finantialInstitutionName = debtAccount.getFinantialInstitution().getName();
            this.customerId = debtAccount.getCustomer().getExternalId();
            this.name = debtAccount.getCustomer().getName();

            if (debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson().getIdDocumentType() != null) {
                this.identificationType =
                        ((PersonCustomer) debtAccount.getCustomer()).getPerson().getIdDocumentType().getLocalizedNameI18N();
            } else if(debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer().getIdDocumentType() != null) {
                this.identificationType =
                        ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer().getIdDocumentType().getLocalizedNameI18N();
            }

            this.identificationNumber = debtAccount.getCustomer().getIdentificationNumber();
            this.vatNumber = debtAccount.getCustomer().getFiscalNumber();

            if (debtAccount.getCustomer().isPersonCustomer() && ((PersonCustomer) debtAccount.getCustomer()).getPerson() != null) {
                this.email =
                        ((PersonCustomer) debtAccount.getCustomer()).getPerson().getInstitutionalOrDefaultEmailAddressValue();
            } else if(debtAccount.getCustomer().isPersonCustomer() && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer() != null) {
                this.email =
                        ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer().getInstitutionalOrDefaultEmailAddressValue();
            }

            this.address = debtAccount.getCustomer().getAddress();
            this.countryCode = debtAccount.getCustomer().getFiscalCountry();

            if (debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent() != null) {
                this.studentNumber = ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent().getNumber();
            } else if(debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer().getStudent() != null) {
                this.studentNumber = ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer().getStudent().getNumber();
            }

            this.vatNumberValid =
                    (!Strings.isNullOrEmpty(this.countryCode) && !Constants.isDefaultCountry(this.countryCode))
                            || FiscalCodeValidation.isValidFiscalNumber(this.countryCode, this.vatNumber);

            this.totalInDebt = debtAccount.getFinantialInstitution().getCurrency().getValueWithScale(debtAccount.getTotalInDebt()).toString();

            if(DebtReportRequest.COMMA.equals(decimalSeparator)) {
                this.totalInDebt = this.totalInDebt.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
            }
            
            this.completed = true;
        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(debtAccount, e);
        }
    }

    @Override
    public void writeCellValues(Row row, IErrorsLog ierrorsLog) {
        final ErrorsLog errorsLog = (ErrorsLog) ierrorsLog;
        
        try {
            row.createCell(0).setCellValue(identification);

            if (!completed) {
                row.createCell(1).setCellValue(Constants.bundle("error.DebtReportEntryBean.report.generation.verify.entry"));
                return;
            }

            int i = 1;

            row.createCell(i++).setCellValue(versioningCreator);
            row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
            row.createCell(i++).setCellValue(valueOrEmpty(finantialInstitutionName));
            row.createCell(i++).setCellValue(valueOrEmpty(customerId));
            row.createCell(i++).setCellValue(valueOrEmpty(name));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(email));
            row.createCell(i++).setCellValue(valueOrEmpty(address));
            row.createCell(i++).setCellValue(valueOrEmpty(countryCode));
            row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumberValid));
            row.createCell(i++).setCellValue(totalInDebt.toString());

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(debtAccount, e);
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
