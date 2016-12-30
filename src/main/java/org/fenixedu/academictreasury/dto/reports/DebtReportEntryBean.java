package org.fenixedu.academictreasury.dto.reports;

import org.apache.poi.ss.usermodel.Row;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent.AcademicTreasuryEventKeys;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.ErrorsLog;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.util.streaming.spreadsheet.IErrorsLog;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;

public class DebtReportEntryBean implements SpreadsheetRow {

    // @formatter:off
    public static String[] SPREADSHEET_DEBIT_HEADERS = { 
            Constants.bundle("label.DebtReportEntryBean.header.identification"),
            Constants.bundle("label.DebtReportEntryBean.header.entryType"),
            Constants.bundle("label.DebtReportEntryBean.header.versioningCreator"),
            Constants.bundle("label.DebtReportEntryBean.header.creationDate"),
            Constants.bundle("label.DebtReportEntryBean.header.entryDate"),
            Constants.bundle("label.DebtReportEntryBean.header.dueDate"),
            Constants.bundle("label.DebtReportEntryBean.header.customerId"),
            Constants.bundle("label.DebtReportEntryBean.header.debtAccountId"),
            Constants.bundle("label.DebtReportEntryBean.header.name"),
            Constants.bundle("label.DebtReportEntryBean.header.identificationType"),
            Constants.bundle("label.DebtReportEntryBean.header.identificationNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.vatNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.email"),
            Constants.bundle("label.DebtReportEntryBean.header.address"),
            Constants.bundle("label.DebtReportEntryBean.header.studentNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.degreeType"),
            Constants.bundle("label.DebtReportEntryBean.header.degreeCode"),
            Constants.bundle("label.DebtReportEntryBean.header.degreeName"),
            Constants.bundle("label.DebtReportEntryBean.header.executionYear"),
            Constants.bundle("label.DebtReportEntryBean.header.executionSemester"),
            Constants.bundle("label.DebtReportEntryBean.header.productCode"),
            /* TODO Consider first: Constants.bundle("label.DebtReportEntryBean.header.productAmount"), */
            Constants.bundle("label.DebtReportEntryBean.header.invoiceEntryDescription"),
            Constants.bundle("label.DebtReportEntryBean.header.documentNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.documentExportationPending"),
            /* TODO Consider first: Constants.bundle("label.DebtReportEntryBean.header.annuled"), */
            Constants.bundle("label.DebtReportEntryBean.header.amountToPay"),
            Constants.bundle("label.DebtReportEntryBean.header.openAmountToPay"),
            Constants.bundle("label.DebtReportEntryBean.header.agreement"),
            Constants.bundle("label.DebtReportEntryBean.header.ingression"),
            Constants.bundle("label.DebtReportEntryBean.header.firstTimeStudent"),
            Constants.bundle("label.DebtReportEntryBean.header.partialRegime"),
            Constants.bundle("label.DebtReportEntryBean.header.statutes"),
            Constants.bundle("label.DebtReportEntryBean.header.numberOfNormalEnrolments"),
            Constants.bundle("label.DebtReportEntryBean.header.numberOfStandaloneEnrolments"),
            Constants.bundle("label.DebtReportEntryBean.header.numberOfExtracurricularEnrolments"),
            Constants.bundle("label.DebtReportEntryBean.header.tuitionPaymentPlan"),
            Constants.bundle("label.DebtReportEntryBean.header.tuitionPaymentPlanConditions"),
            Constants.bundle("label.DebtReportEntryBean.header.documentAnnuled"),
            Constants.bundle("label.DebtReportEntryBean.header.documentAnnuledReason")
    };

    public static String[] SPREADSHEET_CREDIT_HEADERS = { 
            Constants.bundle("label.DebtReportEntryBean.header.identification"),
            Constants.bundle("label.DebtReportEntryBean.header.entryType"),
            Constants.bundle("label.DebtReportEntryBean.header.versioningCreator"),
            Constants.bundle("label.DebtReportEntryBean.header.creationDate"),
            Constants.bundle("label.DebtReportEntryBean.header.entryDate"),
            Constants.bundle("label.DebtReportEntryBean.header.dueDate"),
            Constants.bundle("label.DebtReportEntryBean.header.customerId"),
            Constants.bundle("label.DebtReportEntryBean.header.debtAccountId"),
            Constants.bundle("label.DebtReportEntryBean.header.name"),
            Constants.bundle("label.DebtReportEntryBean.header.identificationType"),
            Constants.bundle("label.DebtReportEntryBean.header.identificationNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.vatNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.email"),
            Constants.bundle("label.DebtReportEntryBean.header.address"),
            Constants.bundle("label.DebtReportEntryBean.header.studentNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.degreeType"),            
            Constants.bundle("label.DebtReportEntryBean.header.degreeCode"),
            Constants.bundle("label.DebtReportEntryBean.header.degreeName"),
            Constants.bundle("label.DebtReportEntryBean.header.executionYear"),
            Constants.bundle("label.DebtReportEntryBean.header.executionSemester"),
            Constants.bundle("label.DebtReportEntryBean.header.productCode"),
            Constants.bundle("label.DebtReportEntryBean.header.invoiceEntryDescription"),
            Constants.bundle("label.DebtReportEntryBean.header.documentNumber"),
            Constants.bundle("label.DebtReportEntryBean.header.documentExportationPending"),
            /* TODO Consider first: Constants.bundle("label.DebtReportEntryBean.header.annuled"), */
            Constants.bundle("label.DebtReportEntryBean.header.debitEntry.identification"),
            Constants.bundle("label.DebtReportEntryBean.header.amountToCredit"),
            Constants.bundle("label.DebtReportEntryBean.header.openAmountToCredit"),
            Constants.bundle("label.DebtReportEntryBean.header.agreement"),
            Constants.bundle("label.DebtReportEntryBean.header.ingression"),
            Constants.bundle("label.DebtReportEntryBean.header.firstTimeStudent"),
            Constants.bundle("label.DebtReportEntryBean.header.partialRegime"),
            Constants.bundle("label.DebtReportEntryBean.header.statutes"),
            Constants.bundle("label.DebtReportEntryBean.header.numberOfNormalEnrolments"),
            Constants.bundle("label.DebtReportEntryBean.header.numberOfStandaloneEnrolments"),
            Constants.bundle("label.DebtReportEntryBean.header.numberOfExtracurricularEnrolments"),
            Constants.bundle("label.DebtReportEntryBean.header.tuitionPaymentPlan"),
            Constants.bundle("label.DebtReportEntryBean.header.tuitionPaymentPlanConditions"),
            Constants.bundle("label.DebtReportEntryBean.header.documentAnnuled"),
            Constants.bundle("label.DebtReportEntryBean.header.documentAnnuledReason")
    };
    // @formatter:on

    private InvoiceEntry invoiceEntry;
    private boolean completed = false;

    private String identification;
    private String entryType;
    private String versioningCreator;
    private DateTime creationDate;
    private DateTime entryDate;
    private LocalDate dueDate;
    private String customerId;
    private String debtAccountId;
    private String name;
    private LocalizedString identificationType;
    private String identificationNumber;
    private String vatNumber;
    private String email;
    private String address;
    private Integer studentNumber;
    private String degreeType;
    private String degreeCode;
    private LocalizedString degreeName;
    private String executionYear;
    private String executionSemester;
    private String productCode;
    private String invoiceEntryDescription;
    private String documentNumber;
    private boolean documentExportationPending;
    private boolean annuled;
    private String annuledReason;
    private String debitEntryIdentification;
    private String baseAmount;
    private String creditedAmount;
    private String amountToPay;
    private String openAmountToPay;
    private LocalizedString agreement;
    private LocalizedString ingression;
    private Boolean firstTimeStudent;
    private Boolean partialRegime;
    private String statutes;
    private int numberOfNormalEnrolments;
    private int numberOfStandaloneEnrolments;
    private int numberOfExtracurricularEnrolments;
    private String tuitionPaymentPlan;
    private String tuitionPaymentPlanConditions;

    public DebtReportEntryBean(final InvoiceEntry entry, final DebtReportRequest request, final ErrorsLog errorsLog) {
        final String decimalSeparator = request.getDecimalSeparator();
        
        this.invoiceEntry = entry;

        try {
            this.identification = entry.getExternalId();
            this.entryType = entryType(entry);
            this.creationDate = entry.getVersioningCreationDate();
            this.versioningCreator = entry.getVersioningCreator();
            this.entryDate = entry.getEntryDateTime();
            this.dueDate = entry.getDueDate();

            fillStudentInformation(entry);

            this.productCode = entry.getProduct().getCode();
            this.invoiceEntryDescription = entry.getDescription();

            if (entry.getFinantialDocument() != null) {
                this.documentNumber = entry.getFinantialDocument().getUiDocumentNumber();
                this.documentExportationPending = entry.getFinantialDocument().isDocumentToExport();
            }

            this.annuled = entry.isAnnulled();
            
            if(this.annuled && entry.getFinantialDocument() != null) {
                this.annuledReason = entry.getFinantialDocument().getAnnulledReason();
            }

            if (entry.isCreditNoteEntry() && ((CreditEntry) entry).getDebitEntry() != null) {
                this.debitEntryIdentification = ((CreditEntry) entry).getDebitEntry().getExternalId();
            }

            final Currency currency = entry.getDebtAccount().getFinantialInstitution().getCurrency();

            this.baseAmount = currency.getValueWithScale(entry.getAmountWithVat()).toString();

            /* TODO Consider: 
            if (entry.isDebitNoteEntry()) {
                final DebitEntry debitEntry = (DebitEntry) entry;
                this.amountToPay = currency.getValueWithScale(debitEntry.getAmountWithVat()).toString();
            } else if(entry.isCreditNoteEntry()) {
            }
            */

            this.amountToPay = currency.getValueWithScale(entry.getAmountWithVat()).toString();
            this.openAmountToPay = currency.getValueWithScale(entry.getOpenAmount()).toString();
            
            if(DebtReportRequest.COMMA.equals(decimalSeparator)) {
                this.amountToPay = this.amountToPay.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
                this.openAmountToPay = this.openAmountToPay.replace(DebtReportRequest.DOT, DebtReportRequest.COMMA);
            }

            this.completed = true;
        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(entry, e);
        }

    }

    private void fillStudentInformation(final InvoiceEntry entry) {
        final Customer customer = entry.getDebtAccount().getCustomer();

        this.customerId = customer.getExternalId();
        this.debtAccountId = entry.getDebtAccount().getExternalId();

        this.name = customer.getName();

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null
                && ((PersonCustomer) customer).getPerson().getIdDocumentType() != null) {
            this.identificationType = ((PersonCustomer) customer).getPerson().getIdDocumentType().getLocalizedNameI18N();
        } else if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null
                && ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getIdDocumentType() != null) {

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

        if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPerson() != null
                && ((PersonCustomer) customer).getPerson().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getPerson().getStudent().getNumber();
        } else if (customer.isPersonCustomer() && ((PersonCustomer) customer).getPersonForInactivePersonCustomer() != null
                && ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getStudent() != null) {
            this.studentNumber = ((PersonCustomer) customer).getPersonForInactivePersonCustomer().getStudent().getNumber();
        }

        final DebitEntry debitEntry = entry.isDebitNoteEntry() ? (DebitEntry) entry : ((CreditEntry) entry).getDebitEntry();

        if (debitEntry != null) {

            // Degree && ExecutionYear && ExecutionSemester
            if (debitEntry.getTreasuryEvent() != null && debitEntry.getTreasuryEvent() instanceof AcademicTreasuryEvent) {
                final AcademicTreasuryEvent academicTreasuryEvent = (AcademicTreasuryEvent) debitEntry.getTreasuryEvent();

                if (academicTreasuryEvent.isForRegistrationTuition()) {
                    this.degreeType = academicTreasuryEvent.getRegistration().getDegree().getDegreeType().getName().getContent();
                    this.degreeCode = academicTreasuryEvent.getRegistration().getDegree().getCode();
                    this.degreeName = academicTreasuryEvent.getRegistration().getDegree().getPresentationNameI18N();
                    this.executionYear = academicTreasuryEvent.getExecutionYear().getQualifiedName();
                    this.tuitionPaymentPlan =
                            AcademicTreasuryEventKeys.valueFor(debitEntry, AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN);
                    this.tuitionPaymentPlanConditions = AcademicTreasuryEventKeys.valueFor(debitEntry,
                            AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN_CONDITIONS);

                    fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                            academicTreasuryEvent.getExecutionYear());

                } else if (academicTreasuryEvent.isForStandaloneTuition()
                        || academicTreasuryEvent.isForExtracurricularTuition()) {
                    if (debitEntry.getCurricularCourse() != null) {
                        this.degreeType = debitEntry.getCurricularCourse().getDegree().getDegreeType().getName().getContent();
                        this.degreeCode = debitEntry.getCurricularCourse().getDegree().getCode();
                        this.degreeName = debitEntry.getCurricularCourse().getDegree().getPresentationNameI18N();
                    }

                    if (debitEntry.getExecutionSemester() != null) {
                        this.executionYear = debitEntry.getExecutionSemester().getExecutionYear().getQualifiedName();
                        this.executionSemester = debitEntry.getExecutionSemester().getQualifiedName();
                    }

                    this.tuitionPaymentPlan =
                            AcademicTreasuryEventKeys.valueFor(debitEntry, AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN);
                    this.tuitionPaymentPlanConditions = AcademicTreasuryEventKeys.valueFor(debitEntry,
                            AcademicTreasuryEventKeys.TUITION_PAYMENT_PLAN_CONDITIONS);

                    if (academicTreasuryEvent.getRegistration() != null && academicTreasuryEvent.getExecutionYear() != null) {
                        fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                                academicTreasuryEvent.getExecutionYear());

                    }

                } else if (academicTreasuryEvent.isForImprovementTax()) {
                    if (debitEntry.getCurricularCourse() != null) {
                        this.degreeType = debitEntry.getCurricularCourse().getDegree().getDegreeType().getName().getContent();
                        this.degreeCode = debitEntry.getCurricularCourse().getDegree().getCode();
                        this.degreeName = debitEntry.getCurricularCourse().getDegree().getPresentationNameI18N();
                    }

                    if (debitEntry.getExecutionSemester() != null) {
                        this.executionYear = debitEntry.getExecutionSemester().getExecutionYear().getQualifiedName();
                        this.executionSemester = debitEntry.getExecutionSemester().getQualifiedName();
                    }

                    if (academicTreasuryEvent.getRegistration() != null && academicTreasuryEvent.getExecutionYear() != null) {
                        fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                                academicTreasuryEvent.getExecutionYear());
                    }
                } else if (academicTreasuryEvent.isForAcademicTax()) {

                    this.degreeType = academicTreasuryEvent.getRegistration().getDegree().getDegreeType().getName().getContent();
                    this.degreeCode = academicTreasuryEvent.getRegistration().getDegree().getCode();
                    this.degreeName = academicTreasuryEvent.getRegistration().getDegree().getPresentationNameI18N();
                    this.executionYear = academicTreasuryEvent.getExecutionYear().getQualifiedName();

                    fillStudentConditionsInformation(academicTreasuryEvent.getRegistration(),
                            academicTreasuryEvent.getExecutionYear());

                } else if (academicTreasuryEvent.isForAcademicServiceRequest()) {

                    final ITreasuryServiceRequest iTreasuryServiceRequest = academicTreasuryEvent.getITreasuryServiceRequest();

                    this.degreeType =
                            iTreasuryServiceRequest.getRegistration().getDegree().getDegreeType().getName().getContent();
                    this.degreeCode = iTreasuryServiceRequest.getRegistration().getDegree().getCode();
                    this.degreeName = iTreasuryServiceRequest.getRegistration().getDegree().getPresentationNameI18N();

                    if (iTreasuryServiceRequest.hasExecutionYear()) {
                        this.executionYear = iTreasuryServiceRequest.getExecutionYear().getQualifiedName();
                        fillStudentConditionsInformation(iTreasuryServiceRequest.getRegistration(),
                                iTreasuryServiceRequest.getExecutionYear());
                    }
                }
            } else if(debitEntry.getTreasuryEvent() != null) {
                final TreasuryEvent treasuryEvent = debitEntry.getTreasuryEvent();
                
                if(!Strings.isNullOrEmpty(treasuryEvent.getDegreeCode())) {
                    this.degreeCode = treasuryEvent.getDegreeCode();
                }
                
                if(!Strings.isNullOrEmpty(treasuryEvent.getDegreeName())) {
                    this.degreeName = new LocalizedString(I18N.getLocale(), treasuryEvent.getDegreeName()) ;
                }
                
                if(!Strings.isNullOrEmpty(treasuryEvent.getExecutionYearName())) {
                    this.executionYear = treasuryEvent.getExecutionYearName();
                }
            }
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

    private void fillStudentConditionsInformation(final Registration registration, final ExecutionYear executionYear) {
        this.firstTimeStudent = registration.isFirstTime(executionYear);
        this.partialRegime = registration.isPartialRegime(executionYear);
        this.statutes = statutes(registration, executionYear);
        this.agreement = registration.getRegistrationProtocol().getDescription();
        this.ingression = registration.getIngressionType().getDescription();

        this.numberOfNormalEnrolments = TuitionServices.normalEnrolments(registration, executionYear).size();
        this.numberOfStandaloneEnrolments = TuitionServices.standaloneEnrolments(registration, executionYear).size();
        this.numberOfExtracurricularEnrolments = TuitionServices.extracurricularEnrolments(registration, executionYear).size();
    }

    private String statutes(final Registration registration, final ExecutionYear executionYear) {
        return registration.getStudent().getStatutesTypesValidOnAnyExecutionSemesterFor(executionYear).stream()
                .map(s -> s != null ? s.getName().getContent() : "").reduce((a, c) -> c + ", " + a).orElse(null);
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

            if (invoiceEntry.isDebitNoteEntry()) {
                int i = 1;

                row.createCell(i++).setCellValue(entryType);
                row.createCell(i++).setCellValue(versioningCreator);
                row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
                row.createCell(i++).setCellValue(entryDate.toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(dueDate.toString(Constants.DATE_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(customerId);
                row.createCell(i++).setCellValue(debtAccountId);
                row.createCell(i++).setCellValue(name);
                row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
                row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(email));
                row.createCell(i++).setCellValue(valueOrEmpty(address));
                row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeType));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeCode));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeName));
                row.createCell(i++).setCellValue(valueOrEmpty(executionYear));
                row.createCell(i++).setCellValue(valueOrEmpty(executionSemester));
                row.createCell(i++).setCellValue(productCode);
                row.createCell(i++).setCellValue(invoiceEntryDescription);
                row.createCell(i++).setCellValue(valueOrEmpty(documentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(documentExportationPending));
                row.createCell(i++).setCellValue(valueOrEmpty(amountToPay));
                row.createCell(i++).setCellValue(valueOrEmpty(openAmountToPay));
                row.createCell(i++).setCellValue(valueOrEmpty(agreement));
                row.createCell(i++).setCellValue(valueOrEmpty(ingression));
                row.createCell(i++).setCellValue(valueOrEmpty(firstTimeStudent));
                row.createCell(i++).setCellValue(valueOrEmpty(partialRegime));
                row.createCell(i++).setCellValue(valueOrEmpty(statutes));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfNormalEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfStandaloneEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfExtracurricularEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlan));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlanConditions));
                row.createCell(i++).setCellValue(valueOrEmpty(annuled));
                row.createCell(i++).setCellValue(valueOrEmpty(annuledReason));

            } else if (invoiceEntry.isCreditNoteEntry()) {
                int i = 1;
                row.createCell(i++).setCellValue(entryType);
                row.createCell(i++).setCellValue(versioningCreator);
                row.createCell(i++).setCellValue(valueOrEmpty(creationDate));
                row.createCell(i++).setCellValue(entryDate.toString(Constants.DATE_TIME_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(dueDate.toString(Constants.DATE_FORMAT_YYYY_MM_DD));
                row.createCell(i++).setCellValue(customerId);
                row.createCell(i++).setCellValue(debtAccountId);
                row.createCell(i++).setCellValue(name);
                row.createCell(i++).setCellValue(valueOrEmpty(identificationType));
                row.createCell(i++).setCellValue(valueOrEmpty(identificationNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(vatNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(email));
                row.createCell(i++).setCellValue(valueOrEmpty(address));
                row.createCell(i++).setCellValue(valueOrEmpty(studentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeType));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeCode));
                row.createCell(i++).setCellValue(valueOrEmpty(degreeName));
                row.createCell(i++).setCellValue(valueOrEmpty(executionYear));
                row.createCell(i++).setCellValue(valueOrEmpty(executionSemester));
                row.createCell(i++).setCellValue(productCode);
                row.createCell(i++).setCellValue(invoiceEntryDescription);
                row.createCell(i++).setCellValue(valueOrEmpty(documentNumber));
                row.createCell(i++).setCellValue(valueOrEmpty(documentExportationPending));
                row.createCell(i++).setCellValue(valueOrEmpty(debitEntryIdentification));
                row.createCell(i++).setCellValue(valueOrEmpty(amountToPay));
                row.createCell(i++).setCellValue(valueOrEmpty(openAmountToPay));
                row.createCell(i++).setCellValue(valueOrEmpty(agreement));
                row.createCell(i++).setCellValue(valueOrEmpty(ingression));
                row.createCell(i++).setCellValue(valueOrEmpty(firstTimeStudent));
                row.createCell(i++).setCellValue(valueOrEmpty(partialRegime));
                row.createCell(i++).setCellValue(valueOrEmpty(statutes));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfNormalEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfStandaloneEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(numberOfExtracurricularEnrolments));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlan));
                row.createCell(i++).setCellValue(valueOrEmpty(tuitionPaymentPlanConditions));
                row.createCell(i++).setCellValue(valueOrEmpty(annuled));
                row.createCell(i++).setCellValue(valueOrEmpty(annuledReason));
                
            }

        } catch (final Exception e) {
            e.printStackTrace();
            errorsLog.addError(invoiceEntry, e);
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
