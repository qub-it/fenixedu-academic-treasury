package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.Series;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class ERPTuitionInfo extends ERPTuitionInfo_Base {

    public static Comparator<ERPTuitionInfo> COMPARE_BY_CREATION_DATE = new Comparator<ERPTuitionInfo>() {

        @Override
        public int compare(final ERPTuitionInfo o1, final ERPTuitionInfo o2) {
            int c = o1.getCreationDate().compareTo(o2.getCreationDate());
            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }
    };

    public ERPTuitionInfo() {
        super();
        setBennu(Bennu.getInstance());
    }

    public ERPTuitionInfo(final Customer customer, final ExecutionYear executionYear, final Product product,
            final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount, final LocalDate beginDate,
            final LocalDate endDate, final ERPTuitionInfo lastSucessfulSentErpTuitionInfo) {
        this();

        setCreationDate(new DateTime());
        setCustomer(customer);
        setExecutionYear(executionYear);
        setProduct(product);
        setTuitionTotalAmount(tuitionTotalAmount);
        setTuitionDeltaAmount(deltaTuitionAmount);
        setBeginDate(beginDate);
        setEndDate(endDate);

        setLastSuccessfulSentERPTuitionInfo(lastSucessfulSentErpTuitionInfo);

        final Series series = ERPTuitionInfoSettings.getInstance().getSeries();
        DocumentNumberSeries documentNumberSeries = null;

        if (Constants.isPositive(getTuitionDeltaAmount())) {
            documentNumberSeries = DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), series);
        } else if (Constants.isNegative(getTuitionDeltaAmount())) {
            documentNumberSeries = DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), series);
        }

        setDocumentNumberSeries(documentNumberSeries);

        this.setDocumentNumber("" + this.getDocumentNumberSeries().getSequenceNumberAndIncrement());

        setFirstERPTuitionInfo(findFirstIntegratedWithSuccess(this).orElse(null));

        checkRules();

        markToInfoExport();
    }

    private void checkRules() {
        if (getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.bennu.required");
        }

        if (getCreationDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.createDate.required");
        }

        if (getDocumentNumberSeries() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.documentNumberSeries.required");
        }

        if (Strings.isNullOrEmpty(getDocumentNumber())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.documentNumber.required");
        }

        if (getTuitionTotalAmount() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionTotalAmount.required");
        }

        if (Constants.isNegative(getTuitionTotalAmount())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionTotalAmount.negative");
        }

        if (getTuitionDeltaAmount() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionDeltaAmount.required");
        }

        if (Constants.isZero(getTuitionDeltaAmount())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.tuitionDeltaAmount.cannot.be.zero");
        }

        if (getBeginDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.beginDate.required");
        }

        if (getEndDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.endDate.required");
        }

        if (getBeginDate().isAfter(getEndDate())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.beginDate.after.endDate");
        }

        if (Constants.isPositive(getTuitionDeltaAmount())
                && !getDocumentNumberSeries().getFinantialDocumentType().getType().isDebitNote()) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfo.tuitionDeltaAmount.positive.but.finantialDocument.not.debit.note");
        }

        if (Constants.isNegative(getTuitionDeltaAmount())
                && !getDocumentNumberSeries().getFinantialDocumentType().getType().isCreditNote()) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfo.tuitionDeltaAmount.negative.but.finantialDocument.not.credit.note");
        }

        if (findPendingToExport(getCustomer(), getExecutionYear(), getProduct()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.pending.to.export.already.exists");
        }

        if (isSubsequent() ^ isFollowedBySuccessfulSent()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.first.and.last.successful.sent.incoerent");

        }
    }

    public String getUiDocumentNumber() {
        return String.format("%s %s/%s",
                this.getDocumentNumberSeries().getFinantialDocumentType().getDocumentNumberSeriesPrefix(),
                this.getDocumentNumberSeries().getSeries().getCode(), Strings.padStart(this.getDocumentNumber(), 7, '0'));
    }

    public boolean isDebit() {
        return getDocumentNumberSeries().getFinantialDocumentType().getType().isDebitNote();
    }

    public boolean isCredit() {
        return getDocumentNumberSeries().getFinantialDocumentType().getType().isCreditNote();
    }

    public String getUiDocumentNumberForERP() {
        final String seriesCode =
                String.format("%s %s", this.getDocumentNumberSeries().getFinantialDocumentType().getDocumentNumberSeriesPrefix(),
                        this.getDocumentNumberSeries().getSeries().getCode());
        final String erpSeriesCode = isDebit() ? ERPTuitionInfoSettings.getInstance()
                .getExpectedSeriesDebitCode() : ERPTuitionInfoSettings.getInstance().getExpectedSeriesCreditCode();

        return getUiDocumentNumber().replaceAll(seriesCode, erpSeriesCode);
    }

    public boolean isPendingToExport() {
        return getBennuPendingToExport() != null;
    }

    public boolean isExportationSuccess() {
        return getExportationSuccess();
    }

    public boolean isSubsequent() {
        return getFirstERPTuitionInfo() != null;
    }

    public boolean isFollowedBySuccessfulSent() {
        return getLastSuccessfulSentERPTuitionInfo() != null;
    }

    public void export() {
        ERPTuitionInfoSettings.getInstance().exporter().export(this);
    }

    @Atomic
    public void markToInfoExport() {
        setBennuPendingToExport(Bennu.getInstance());
    }

    public void markIntegratedWithSuccess(final String message) {
        setBennuPendingToExport(null);
        setExportationMessage(message);
        setExportationSuccess(true);

        checkRules();
    }

    private void cancelExportation(final String reason) {
        setBennuPendingToExport(null);
        setExportationMessage(reason);
        setExportationSuccess(false);

        checkRules();
    }

    public void editPendingToExport(final BigDecimal tuitionTotalAmount, final BigDecimal tuitionDeltaAmount,
            final LocalDate beginDate, final LocalDate endDate) {
        if (!isPendingToExport()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.editPendingToExport.already.exported");
        }

        setTuitionTotalAmount(tuitionTotalAmount);
        setTuitionDeltaAmount(tuitionDeltaAmount);
        setBeginDate(beginDate);
        setEndDate(endDate);

        checkRules();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<ERPTuitionInfo> findAll() {
        return Bennu.getInstance().getErpTuitionInfosSet().stream();
    }

    public static Stream<ERPTuitionInfo> find(final Customer customer) {
        return customer.getErpTuitionInfosSet().stream();
    }

    public static Stream<ERPTuitionInfo> find(final Customer customer, final ExecutionYear executionYear, final Product product) {
        return find(customer).filter(i -> i.getExecutionYear() == executionYear).filter(i -> i.getProduct() == product);
    }

    public static Optional<ERPTuitionInfo> findUniqueByDocumentNumber(final String documentNumber) {
        return findAll().filter(e -> documentNumber.equals(e.getUiDocumentNumber())).findFirst();
    }

    public static Stream<ERPTuitionInfo> findPendingToExport(final Customer customer, final ExecutionYear executionYear,
            final Product product) {
        return find(customer, executionYear, product).filter(i -> i.isPendingToExport());
    }

    public static Optional<ERPTuitionInfo> findUniquePendingToExport(final Customer customer, final ExecutionYear executionYear,
            final Product product) {
        return findPendingToExport(customer, executionYear, product).findFirst();
    }

    public static Optional<ERPTuitionInfo> findFirstIntegratedWithSuccess(final Customer customer,
            final ExecutionYear executionYear, final Product product) {
        return find(customer, executionYear, product).filter(t -> t.isExportationSuccess()).sorted(COMPARE_BY_CREATION_DATE)
                .findFirst();
    }

    public static Optional<ERPTuitionInfo> findFirstIntegratedWithSuccess(final ERPTuitionInfo erpTuitionInfo) {
        return findFirstIntegratedWithSuccess(erpTuitionInfo.getCustomer(), erpTuitionInfo.getExecutionYear(),
                erpTuitionInfo.getProduct());
    }

    public static Optional<ERPTuitionInfo> findLastIntegratedWithSuccess(final Customer customer,
            final ExecutionYear executionYear, final Product product) {
        return find(customer, executionYear, product).filter(t -> t.isExportationSuccess())
                .sorted(COMPARE_BY_CREATION_DATE.reversed()).findFirst();
    }

    private static ERPTuitionInfo create(final Customer customer, final ExecutionYear executionYear, final Product product,
            final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount, final LocalDate beginDate,
            final LocalDate endDate, final ERPTuitionInfo lastSucessfulSentErpTuitionInfo) {

        if (findUniquePendingToExport(customer, executionYear, product).isPresent()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.pending.to.export.already.exists");
        }

        return new ERPTuitionInfo(customer, executionYear, product, tuitionTotalAmount, deltaTuitionAmount, beginDate, endDate,
                lastSucessfulSentErpTuitionInfo);
    }

    public static void triggerFullExportation() {
        List<Callable<ERPTuitionInfo>> callablesList = Lists.newArrayList();

        for (final ERPTuitionInfoType type : ERPTuitionInfoType.findAll().collect(Collectors.toSet())) {
            for (final ExecutionYear executionYear : ERPTuitionInfoSettings.getInstance().getActiveExecutionYearsSet()) {
                for (final PersonCustomer customer : PersonCustomer.findAll().collect(Collectors.<PersonCustomer> toSet())) {
                    exportTuitionInformationCallable(customer, type, executionYear);
                }
            }
        }

        try {
            final ExecutorService executor = Executors.newFixedThreadPool(3);
            executor.invokeAll(callablesList);
            executor.awaitTermination(5, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
        }
    }

    @Atomic(mode = TxMode.WRITE)
    public static ERPTuitionInfo exportTuitionInformation(final PersonCustomer customer, final ERPTuitionInfoType type,
            final ExecutionYear executionYear) {
        final Product product = type.getProduct();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (final AcademicTreasuryEvent event : AcademicTreasuryEvent.find(customer)
                .collect(Collectors.<AcademicTreasuryEvent> toSet())) {
            if (type.isForRegistration() && event.isForRegistrationTuition()
                    && type.getDegreeType() == event.getRegistration().getDegreeType()) {
                totalAmount = totalAmount.add(event.getAmountToPay());
            } else if (type.isForStandalone() && event.isForStandaloneTuition()) {
                totalAmount = totalAmount.add(event.getAmountToPay());
            } else if (type.isForExtracurricular() && event.isForExtracurricularTuition()) {
                totalAmount = totalAmount.add(event.getAmountToPay());
            }
        }

        final Optional<ERPTuitionInfo> lastIntegratedWithSuccess =
                findLastIntegratedWithSuccess(customer, executionYear, product);
        final BigDecimal deltaAmount = totalAmount.subtract(lastIntegratedWithSuccess.isPresent() ? lastIntegratedWithSuccess
                .get().getTuitionTotalAmount() : BigDecimal.ZERO);

        if (findUniquePendingToExport(customer, executionYear, product).isPresent()) {
            final ERPTuitionInfo pendingErpTuitionInfo = findUniquePendingToExport(customer, executionYear, product).get();

            pendingErpTuitionInfo.editPendingToExport(totalAmount, deltaAmount, executionYear.getBeginLocalDate(),
                    executionYear.getEndLocalDate());

            if (Constants.isZero(deltaAmount)) {
                pendingErpTuitionInfo.cancelExportation(Constants.bundle("label.ERPTuitionInfo.cancelExportation.delta.zero"));
                return null;
            }

            return pendingErpTuitionInfo;
        }

        if (org.fenixedu.academictreasury.util.Constants.isZero(deltaAmount)) {
            throw new AcademicTreasuryDomainException("error.ErpTuitionInfo.no.differences.from.last.successul.exportation");
        }

        return ERPTuitionInfo.create(customer, executionYear, product, totalAmount, deltaAmount,
                executionYear.getBeginLocalDate(), executionYear.getEndLocalDate(), lastIntegratedWithSuccess.orElse(null));
    }

    protected static Callable<ERPTuitionInfo> exportTuitionInformationCallable(final PersonCustomer customer,
            final ERPTuitionInfoType type, final ExecutionYear executionYear) {
        return new Callable<ERPTuitionInfo>() {

            private String customerId = customer.getExternalId();
            private String erpTuitionInfoTypeId = type.getExternalId();
            private String executionYearId = executionYear.getExternalId();

            @Override
            @Atomic(mode = TxMode.READ)
            public ERPTuitionInfo call() throws Exception {
                final PersonCustomer c = FenixFramework.getDomainObject(customerId);
                final ERPTuitionInfoType t = FenixFramework.getDomainObject(erpTuitionInfoTypeId);
                final ExecutionYear e = FenixFramework.getDomainObject(executionYearId);

                return exportTuitionInformation(c, t, e);
            }
        };
    }

}
