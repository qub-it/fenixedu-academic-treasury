package org.fenixedu.academictreasury.dto.reports;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academictreasury.domain.academicalAct.AcademicActBlockingSuspension;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;

public class AcademicActBlockingSuspensionReportEntryBean extends AbstractReportEntryBean {

    public static String[] SPREADSHEET_HEADERS =
            { academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.identification"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.versioningCreator"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.creationDate"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.name"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.identificationType"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.identificationNumber"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.vatNumber"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.email"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.address"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.addressCountryCode"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.studentNumber"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.beginDate"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.endDate"),
                    academicTreasuryBundle("label.AcademicActBlockingSuspensionReportEntryBean.header.reason") };

    private String identification;
    private String versioningCreator;
    private DateTime creationDate;
    private String name;
    private String identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private String addressCountryCode;
    private Integer studentNumber;
    private LocalDate beginDate;
    private LocalDate endDate;
    private String reason;

    private AcademicActBlockingSuspension academicActBlockingSuspension;

    boolean completed = false;

    public AcademicActBlockingSuspensionReportEntryBean(final AcademicActBlockingSuspension academicActBlockingSuspension,
            final ErrorsLog errorsLog) {
        final ITreasuryPlatformDependentServices treasuryServices = TreasuryPlataformDependentServicesFactory.implementation();
        
        this.academicActBlockingSuspension = academicActBlockingSuspension;

        try {
            this.identification = academicActBlockingSuspension.getExternalId();
            this.versioningCreator = treasuryServices.versioningCreatorUsername(academicActBlockingSuspension);
            this.creationDate = treasuryServices.versioningCreationDate(academicActBlockingSuspension);

            final Person person = academicActBlockingSuspension.getPerson();

            this.name = person.getName();

            if (academicActBlockingSuspension.getPerson().getIdDocumentType() != null) {
                this.identificationType = academicActBlockingSuspension.getPerson().getIdDocumentType().getLocalizedName();
            }

            this.identificationNumber = PersonCustomer.identificationNumber(person);
            this.vatNumber = PersonCustomer.uiPersonFiscalNumber(person);
            this.email = academicActBlockingSuspension.getPerson().getInstitutionalOrDefaultEmailAddressValue();
            this.address =
                    PersonCustomer.physicalAddress(person) != null ? PersonCustomer.physicalAddress(person).getAddress() : "";
            this.addressCountryCode = PersonCustomer.physicalAddress(person) != null
                    && PersonCustomer.physicalAddress(person).getCountryOfResidence() != null ? PersonCustomer
                            .physicalAddress(person).getCountryOfResidence().getCode() : "";

            if (academicActBlockingSuspension.getPerson().getStudent() != null) {
                this.studentNumber = academicActBlockingSuspension.getPerson().getStudent().getNumber();
            }

            this.beginDate = academicActBlockingSuspension.getBeginDate();
            this.endDate = academicActBlockingSuspension.getEndDate();
            this.reason = academicActBlockingSuspension.getReason();

            completed = true;
        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(academicActBlockingSuspension, e);
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
            row.createCell(i++).setCellValue(valueOrEmpty(versioningCreator));
            row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
            row.createCell(i++).setCellValue(valueOrEmpty(name));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(email));
            row.createCell(i++).setCellValue(valueOrEmpty(address));
            row.createCell(i++).setCellValue(valueOrEmpty(addressCountryCode));
            row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(beginDate));
            row.createCell(i++).setCellValue(valueOrEmpty(endDate));
            row.createCell(i++).setCellValue(valueOrEmpty(reason));

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(academicActBlockingSuspension, e);
        }
    }

}
