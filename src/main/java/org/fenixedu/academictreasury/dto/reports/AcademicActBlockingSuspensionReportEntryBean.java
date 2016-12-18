package org.fenixedu.academictreasury.dto.reports;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academictreasury.domain.academicalAct.AcademicActBlockingSuspension;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class AcademicActBlockingSuspensionReportEntryBean extends AbstractReportEntryBean {

    public static String[] SPREADSHEET_HEADERS = { 
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.identification"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.versioningCreator"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.creationDate"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.customerId"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.name"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.identificationType"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.identificationNumber"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.vatNumber"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.email"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.address"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.countryCode"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.studentNumber"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.beginDate"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.endDate"),
        Constants.bundle("label.AcademicActBlockingSuspensionReportEntryBean.header.reason") };
    
    private String identification;
    private String versioningCreator;
    private DateTime creationDate;
    private String customerId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private String countryCode;
    private Integer studentNumber;
    private LocalDate beginDate;
    private LocalDate endDate;
    private String reason;
    
    private AcademicActBlockingSuspension academicActBlockingSuspension;
    
    boolean completed = false;
    
    public AcademicActBlockingSuspensionReportEntryBean(final AcademicActBlockingSuspension academicActBlockingSuspension, final ErrorsLog errorsLog) {
        this.academicActBlockingSuspension = academicActBlockingSuspension;
        
        try {
            this.identification = academicActBlockingSuspension.getExternalId();
            this.versioningCreator = academicActBlockingSuspension.getVersioningCreator();
            this.creationDate = academicActBlockingSuspension.getVersioningCreationDate();
            
            if(PersonCustomer.findUnique(academicActBlockingSuspension.getPerson()).isPresent()) {
                this.customerId = PersonCustomer.findUnique(academicActBlockingSuspension.getPerson()).get().getExternalId(); 
                
                final PersonCustomer personCustomer = PersonCustomer.findUnique(academicActBlockingSuspension.getPerson()).get();
                
                this.name = personCustomer.getName();
                
                if (academicActBlockingSuspension.getPerson().getIdDocumentType() != null) {
                    this.identificationType =
                            academicActBlockingSuspension.getPerson().getIdDocumentType().getLocalizedNameI18N();
                }
                
                this.identificationNumber = personCustomer.getIdentificationNumber();
                this.vatNumber = personCustomer.getFiscalNumber();
                this.email =
                        academicActBlockingSuspension.getPerson().getInstitutionalOrDefaultEmailAddressValue();
                this.address = personCustomer.getAddress();
                this.countryCode = personCustomer.getFiscalCountry();

                if (academicActBlockingSuspension.getPerson().getStudent() != null) {
                    this.studentNumber = academicActBlockingSuspension.getPerson().getStudent().getNumber();
                }
            }
            
            this.beginDate = academicActBlockingSuspension.getBeginDate();
            this.endDate = academicActBlockingSuspension.getEndDate();
            this.reason = academicActBlockingSuspension.getReason();
            
            completed = true;
        } catch(final Exception e) {
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
                row.createCell(1).setCellValue(Constants.bundle("error.DebtReportEntryBean.report.generation.verify.entry"));
                return;
            }

            int i = 1;
            row.createCell(i++).setCellValue(valueOrEmpty(versioningCreator));
            row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
            row.createCell(i++).setCellValue(valueOrEmpty(customerId));
            row.createCell(i++).setCellValue(valueOrEmpty(name));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
            row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
            row.createCell(i++).setCellValue(valueOrEmpty(email));
            row.createCell(i++).setCellValue(valueOrEmpty(address));
            row.createCell(i++).setCellValue(valueOrEmpty(countryCode));
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
