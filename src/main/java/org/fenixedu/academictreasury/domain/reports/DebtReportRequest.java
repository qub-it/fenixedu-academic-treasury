package org.fenixedu.academictreasury.domain.reports;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.reports.task.PendingDebtReportRequestsCronTask;
import org.fenixedu.academictreasury.dto.reports.AcademicActBlockingSuspensionReportEntryBean;
import org.fenixedu.academictreasury.dto.reports.DebtAccountReportEntryBean;
import org.fenixedu.academictreasury.dto.reports.DebtReportEntryBean;
import org.fenixedu.academictreasury.dto.reports.DebtReportRequestBean;
import org.fenixedu.academictreasury.dto.reports.PaymentReferenceCodeEntryBean;
import org.fenixedu.academictreasury.dto.reports.PaymentReportEntryBean;
import org.fenixedu.academictreasury.dto.reports.ProductReportEntryBean;
import org.fenixedu.academictreasury.dto.reports.SettlementReportEntryBean;
import org.fenixedu.academictreasury.dto.reports.SibsTransactionDetailEntryBean;
import org.fenixedu.academictreasury.dto.reports.TreasuryExemptionReportEntryBean;
import org.fenixedu.academictreasury.services.debtReports.DebtReportService;
import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.treasury.util.streaming.spreadsheet.ExcelSheet;
import org.fenixedu.treasury.util.streaming.spreadsheet.Spreadsheet;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class DebtReportRequest extends DebtReportRequest_Base {

    public static final String DOT = ".";
    public static final String COMMA = ",";

    protected DebtReportRequest() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setDomainRootForPendingReportRequests(FenixFramework.getDomainRoot());
    }

    protected DebtReportRequest(final DebtReportRequestBean bean) {
        this();

        this.setBeginDate(bean.getBeginDate());
        this.setEndDate(bean.getEndDate());
        this.setType(bean.getType());
        this.setDecimalSeparator(bean.getDecimalSeparator());
        this.setIncludeAnnuledEntries(bean.isIncludeAnnuledEntries());

        this.setIncludeExtraAcademicInfo(bean.isIncludeExtraAcademicInfo());
        this.setIncludeErpIntegrationInfo(bean.isIncludeErpIntegrationInfo());
        this.setIncludeSibsInfo(bean.isIncludeSibsInfo());
        this.setIncludeProductsInfo(bean.isIncludeProductsInfo());

        this.setDegreeType(bean.getDegreeType());
        this.setExecutionYear(bean.getExecutionYear());
        
        checkRules();
    }

    private void checkRules() {

        if (getBeginDate() == null) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequest.beginDate.required");
        }

        if (getEndDate() == null) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequest.endDate.required");
        }

        if (getType() == null) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequest.type.required");
        }

        if (Strings.isNullOrEmpty(getDecimalSeparator())
                || (!getDecimalSeparator().equals(COMMA) && !getDecimalSeparator().equals(DOT))) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequest.decimalSeparator.invalid");
        }

    }

    public boolean isPending() {
        return getDomainRootForPendingReportRequests() != null;
    }

    public boolean isIncludeAnnuledEntries() {
        return super.getIncludeAnnuledEntries();
    }
    
    public boolean isIncludeExtraAcademicInfo() {
        return getIncludeExtraAcademicInfo();
    }
    
    public boolean isIncludeErpIntegrationInfo() {
        return getIncludeErpIntegrationInfo();
    }
    
    public boolean isIncludeSibsInfo() {
        return getIncludeSibsInfo();
    }
    
    public boolean isIncludeProductsInfo() {
        return getIncludeProductsInfo();
    }

    @Atomic(mode = TxMode.READ)
    public void processRequest() {

        if (getType().isRequestForInvoiceEntries()) {

            final ErrorsLog errorsLog = new ErrorsLog();
            final byte[] debitCreditsContent = extractInformationForDebitAndCredits(errorsLog);
            final byte[] settlementsContent = extractInformationForSettlements(errorsLog);
            final byte[] paymentCodesContent = extractInformationForPaymentCodes(errorsLog);
            final byte[] othersContent = extractOtherTreasuryData(errorsLog);

            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);

                zos.putNextEntry(new ZipEntry(academicTreasuryBundle("label.DebtReportRequestResultFile.INVOICE_ENTRIES.filename", new DateTime().toString("YYYYMMddHHmmss"))));
                zos.write(debitCreditsContent);
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry(academicTreasuryBundle("label.DebtReportRequestResultFile.SETTLEMENT_ENTRIES.filename", new DateTime().toString("YYYYMMddHHmmss"))));
                zos.write(settlementsContent);
                zos.closeEntry();
                
                zos.putNextEntry(new ZipEntry(academicTreasuryBundle("label.DebtReportRequestResultFile.PAYMENT_CODES.filename", new DateTime().toString("YYYYMMddHHmmss"))));
                zos.write(paymentCodesContent);
                zos.closeEntry();
                
                zos.putNextEntry(new ZipEntry(academicTreasuryBundle("label.DebtReportRequestResultFile.OTHER.filename", new DateTime().toString("YYYYMMddHHmmss"))));
                zos.write(othersContent);
                zos.closeEntry();

                zos.close();
                baos.close();

                final byte[] contents = baos.toByteArray();
                
                
                writeReportResultFile(errorsLog, contents);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            
            
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private void writeReportResultFile(final ErrorsLog errorsLog, final byte[] content) {
        DebtReportRequestResultFile.create(this, content);
        DebtReportRequestResultErrorsFile.create(this, errorsLog.getLog().getBytes());
        setDomainRootForPendingReportRequests(null);
    }
    
    private byte[] extractInformationForSettlements(final ErrorsLog errorsLog) {
        return Spreadsheet.buildSpreadsheetContent(new Spreadsheet() {

            @Override
            public ExcelSheet[] getSheets() {
                return new ExcelSheet[] {
                        ExcelSheet.create(settlementEntriesSheetName(), SettlementReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.settlementEntriesReport(DebtReportRequest.this, errorsLog)),

                        ExcelSheet.create(paymentEntriesSheetName(), PaymentReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.paymentEntriesReport(DebtReportRequest.this, errorsLog)),

                        ExcelSheet.create(reimbursementEntriesSheetName(), PaymentReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.reimbursementEntriesReport(DebtReportRequest.this, errorsLog)),

                };
            }
        }, errorsLog);
    }
    
    private byte[] extractInformationForPaymentCodes(final ErrorsLog errorsLog) {
        return Spreadsheet.buildSpreadsheetContent(new Spreadsheet() {

            @Override
            public ExcelSheet[] getSheets() {
                return new ExcelSheet[] {
                    ExcelSheet.create(paymentReferenceCodeSheetName(), PaymentReferenceCodeEntryBean.SPREADSHEET_HEADERS,
                            DebtReportService.paymentReferenceCodeReport(DebtReportRequest.this, errorsLog)),

                    ExcelSheet.create(sibsTransactionDetailSheetName(),
                            SibsTransactionDetailEntryBean.SPREADSHEET_HEADERS,
                            DebtReportService.sibsTransactionDetailReport(DebtReportRequest.this, errorsLog)),
                };
            }
        }, errorsLog);
    }
    
    private byte[] extractOtherTreasuryData(final ErrorsLog errorsLog) {
        return Spreadsheet.buildSpreadsheetContent(new Spreadsheet() {

            @Override
            public ExcelSheet[] getSheets() {
                return new ExcelSheet[] {
                        ExcelSheet.create(debtAccountEntriesSheetName(), DebtAccountReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.debtAccountEntriesReport(DebtReportRequest.this, errorsLog)),

                        ExcelSheet.create(academicActBlockingSuspensionSheetName(),
                                AcademicActBlockingSuspensionReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.academicActBlockingSuspensionReport(DebtReportRequest.this, errorsLog)),

                        ExcelSheet.create(treasuryExemptionSheetName(), TreasuryExemptionReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.treasuryExemptionReport(DebtReportRequest.this, errorsLog)),

                        ExcelSheet.create(productSheetName(), ProductReportEntryBean.SPREADSHEET_HEADERS,
                                DebtReportService.productReport(DebtReportRequest.this, errorsLog)) 
                        };
                };
        }, errorsLog);
    }

    private byte[] extractInformationForDebitAndCredits(final ErrorsLog errorsLog) {
        return Spreadsheet.buildSpreadsheetContent(new Spreadsheet() {

            @Override
            public ExcelSheet[] getSheets() {
                return new ExcelSheet[] {

                        ExcelSheet.create(debitEntriesSheetName(), DebtReportEntryBean.SPREADSHEET_DEBIT_HEADERS,
                                DebtReportService.debitEntriesReport(DebtReportRequest.this, errorsLog)),

                        ExcelSheet.create(creditEntriesSheetName(), DebtReportEntryBean.SPREADSHEET_CREDIT_HEADERS,
                                DebtReportService.creditEntriesReport(DebtReportRequest.this, errorsLog))
                };
            }

            private String decimalSeparator() {
                if (Strings.isNullOrEmpty(getDecimalSeparator())) {
                    return DOT;
                }

                return getDecimalSeparator();
            }

        }, errorsLog);
    }

    @Atomic
    public void cancelRequest() {
        setDomainRootForPendingReportRequests(null);
    }

    protected String treasuryExemptionSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.treasuryExemptionSheetName");
    }

    protected String sibsTransactionDetailSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.sibsTransactionDetailSheetName");
    }

    protected String paymentReferenceCodeSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.paymentReferenceCodeSheetName");
    }

    private String academicActBlockingSuspensionSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.academicActBlockingSuspensionSheetName");
    }

    private String debitEntriesSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.debitEntriesSheetName");
    }

    private String creditEntriesSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.creditEntriesSheetName");
    }

    private String paymentEntriesSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.paymentEntriesSheetName");
    }

    private String settlementEntriesSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.settlementEntriesSheetName");
    }

    private String reimbursementEntriesSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.reimbursementEntriesSheetName");
    }

    private String debtAccountEntriesSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.debtAccountEntriesSheetName");
    }

    private String productSheetName() {
        return academicTreasuryBundle("label.DebtReportRequest.productsSheetName");
    }

    public static Stream<DebtReportRequest> findAll() {
        return FenixFramework.getDomainRoot().getDebtReportRequestsSet().stream();
    }

    public static Stream<DebtReportRequest> findPending() {
        return FenixFramework.getDomainRoot().getDebtReportRequestsSet().stream().filter(i -> i.isPending());
    }

    @Atomic
    public static DebtReportRequest create(final DebtReportRequestBean bean) {
        final DebtReportRequest request = new DebtReportRequest(bean);

        new Thread() {

            @Atomic(mode = TxMode.READ)
            public void run() {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                SchedulerSystem.queue(new TaskRunner(new PendingDebtReportRequestsCronTask()));
            };

        }.start();

        return request;
    }

}
