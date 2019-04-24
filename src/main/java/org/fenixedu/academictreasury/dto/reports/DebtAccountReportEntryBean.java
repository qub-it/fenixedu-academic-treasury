package org.fenixedu.academictreasury.dto.reports;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

public class DebtAccountReportEntryBean implements SpreadsheetRow {

    // @formatter:off
    public static String[] SPREADSHEET_HEADERS = { 
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.identification"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.versioningCreator"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.creationDate"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.finantialInstitutionName"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.customerId"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.name"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.identificationType"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.identificationNumber"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.vatNumber"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.email"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.address"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.addressCountryCode"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.studentNumber"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.vatNumberValid"),
            academicTreasuryBundle("label.DebtAccountReportEntryBean.header.totalInDebt") };
    // @formatter:on

    final DebtAccount debtAccount;
    boolean completed = false;

    private String identification;
    private String versioningCreator;
    private DateTime creationDate;
    private String finantialInstitutionName;
    private String customerId;
    private String name;
    private String identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private String addressCountryCode;
    private Integer studentNumber;
    private boolean vatNumberValid;
    private String totalInDebt;

    public DebtAccountReportEntryBean(final DebtAccount debtAccount, final DebtReportRequest request, final ErrorsLog errorsLog) {
        final ITreasuryPlatformDependentServices treasuryServices = TreasuryPlataformDependentServicesFactory.implementation();

        final String decimalSeparator = request.getDecimalSeparator();

        this.debtAccount = debtAccount;

        try {
            this.identification = debtAccount.getExternalId();
            this.versioningCreator = treasuryServices.versioningCreatorUsername(debtAccount);
            this.creationDate = treasuryServices.versioningCreationDate(debtAccount);
            this.finantialInstitutionName = debtAccount.getFinantialInstitution().getName();
            this.customerId = debtAccount.getCustomer().getExternalId();
            this.name = debtAccount.getCustomer().getName();

            if (debtAccount.getCustomer().isPersonCustomer() && ((PersonCustomer) debtAccount.getCustomer()).getPerson() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson().getIdDocumentType() != null) {
                this.identificationType =
                        ((PersonCustomer) debtAccount.getCustomer()).getPerson().getIdDocumentType().getLocalizedName();
            } else if (debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer()
                            .getIdDocumentType() != null) {
                this.identificationType = ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer()
                        .getIdDocumentType().getLocalizedName();
            }

            this.identificationNumber = debtAccount.getCustomer().getIdentificationNumber();
            this.vatNumber = debtAccount.getCustomer().getUiFiscalNumber();

            if (debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson() != null) {
                this.email =
                        ((PersonCustomer) debtAccount.getCustomer()).getPerson().getInstitutionalOrDefaultEmailAddressValue();
            } else if (debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer() != null) {
                this.email = ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer()
                        .getInstitutionalOrDefaultEmailAddressValue();
            }

            this.address = debtAccount.getCustomer().getUiCompleteAddress();
            this.addressCountryCode = valueOrEmpty(debtAccount.getCustomer().getAddressCountryCode());

            if (debtAccount.getCustomer().isPersonCustomer() && ((PersonCustomer) debtAccount.getCustomer()).getPerson() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent() != null) {
                this.studentNumber = ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent().getNumber();
            } else if (debtAccount.getCustomer().isPersonCustomer()
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer() != null
                    && ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer().getStudent() != null) {
                this.studentNumber = ((PersonCustomer) debtAccount.getCustomer()).getPersonForInactivePersonCustomer()
                        .getStudent().getNumber();
            }

            this.vatNumberValid = FiscalCodeValidation.isValidFiscalNumber(debtAccount.getCustomer().getFiscalCountry(),
                    debtAccount.getCustomer().getFiscalNumber());

            this.totalInDebt = debtAccount.getFinantialInstitution().getCurrency().getValueWithScale(debtAccount.getTotalInDebt())
                    .toString();

            if (DebtReportRequest.COMMA.equals(decimalSeparator)) {
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
                row.createCell(1).setCellValue(academicTreasuryBundle("error.DebtReportEntryBean.report.generation.verify.entry"));
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
            row.createCell(i++).setCellValue(valueOrEmpty(addressCountryCode));
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

        return value.toString(AcademicTreasuryConstants.DATE_TIME_FORMAT_YYYY_MM_DD);
    }

    private String valueOrEmpty(final Boolean value) {
        if (value == null) {
            return "";
        }

        return academicTreasuryBundle(value ? "label.yes" : "label.no");
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

        if (Strings.isNullOrEmpty(value.getContent())) {
            return "";
        }

        return value.getContent();
    }

    private String valueOrEmpty(final String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }

        return "";
    }

}
