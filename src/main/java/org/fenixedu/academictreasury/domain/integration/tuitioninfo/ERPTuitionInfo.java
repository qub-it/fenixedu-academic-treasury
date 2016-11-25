package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
            final LocalDate endDate) {
        this();

        setCreationDate(new DateTime());
        setCustomer(customer);
        setExecutionYear(executionYear);
        setProduct(product);
        setTuitionTotalAmount(tuitionTotalAmount);
        setDeltaTuitionAmount(deltaTuitionAmount);
        setBeginDate(beginDate);
        setEndDate(endDate);

        final Series series = ERPTuitionInfoSettings.getInstance().getSeries();
        DocumentNumberSeries documentNumberSeries = null;

        if (Constants.isPositive(getDeltaTuitionAmount())) {
            documentNumberSeries = DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), series);
        } else if (Constants.isNegative(getDeltaTuitionAmount())) {
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

        if (getDeltaTuitionAmount() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.required");
        }

        if (Constants.isZero(getDeltaTuitionAmount())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.cannot.be.zero");
        }

        if (getBeginDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.beginDate");
        }

        if (getEndDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.endDate");
        }

        if (getBeginDate().isAfter(getEndDate())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.endDate");
        }

        if (Constants.isPositive(getDeltaTuitionAmount())
                && !getDocumentNumberSeries().getFinantialDocumentType().getType().isDebitNote()) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfo.deltaTuitionAmount.positive.but.finantialDocument.not.debit.note");
        }

        if (Constants.isNegative(getDeltaTuitionAmount())
                && !getDocumentNumberSeries().getFinantialDocumentType().getType().isCreditNote()) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfo.deltaTuitionAmount.negative.but.finantialDocument.not.credit.note");
        }

        if(findPendingToExport(getCustomer(), getExecutionYear(), getProduct()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.pending.to.export.already.exists");
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

    public void editPendingToExport(final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount,
            final LocalDate beginDate, final LocalDate endDate) {
        if (!isPendingToExport()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.editPendingToExport.already.exported");
        }

        setTuitionTotalAmount(tuitionTotalAmount);
        setDeltaTuitionAmount(deltaTuitionAmount);
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
            final LocalDate endDate) {

        if(findUniquePendingToExport(customer, executionYear, product).isPresent()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.pending.to.export.already.exists");
        }
        
        return new ERPTuitionInfo(customer, executionYear, product, tuitionTotalAmount, deltaTuitionAmount, beginDate, endDate);
    }

    public static void exportTuitionInformation() {
        for (final PersonCustomer customer : PersonCustomer.findAll().collect(Collectors.<PersonCustomer> toSet())) {
            exportTuitionInformation(customer);
        }
    }
    
    public static void exportTuitionInformation(final PersonCustomer personCustomer) {
        final ERPTuitionInfoExporterThread thread = new ERPTuitionInfoExporterThread(personCustomer);
        thread.start();
        
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    public static class ERPTuitionInfoExporterThread extends Thread {

        private String personCustomerId;

        public ERPTuitionInfoExporterThread(final PersonCustomer personCustomer) {
            this.personCustomerId = personCustomer.getExternalId();
        }

        @Override
        @Atomic(mode=TxMode.READ)
        public void run() {
            final PersonCustomer customer = FenixFramework.getDomainObject(personCustomerId);

            for (final Product product : ERPTuitionInfoType.findProducts().collect(Collectors.toSet())) {
                exportTuitionInfoForCustomerAndProduct(customer, product);
            }

            super.run();
        }
    }

    @Atomic(mode=TxMode.WRITE)
    private static void exportTuitionInfoForCustomerAndProduct(final PersonCustomer customer, final Product product) {
        final Multimap<ExecutionYear, AcademicTreasuryEvent> treasuryEventsMap = ArrayListMultimap.create();

        for (final ERPTuitionInfoType type : ERPTuitionInfoType.findByProduct(product).collect(Collectors.toSet())) {
            for (final AcademicTreasuryEvent event : AcademicTreasuryEvent.find(customer)
                    .collect(Collectors.<AcademicTreasuryEvent> toSet())) {
                if (type.isForRegistration() && event.isForRegistrationTuition()
                        && type.getDegreeType() == event.getRegistration().getDegreeType()) {
                    treasuryEventsMap.put(event.getExecutionYear(), event);
                } else if (type.isForStandalone() && event.isForStandaloneTuition()) {
                    treasuryEventsMap.put(event.getExecutionYear(), event);
                } else if (type.isForExtracurricular() && event.isForExtracurricularTuition()) {
                    treasuryEventsMap.put(event.getExecutionYear(), event);
                }
            }
        }

        for (final ExecutionYear executionYear : treasuryEventsMap.keySet()) {
            if (treasuryEventsMap.get(executionYear).isEmpty()) {
                continue;
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (final AcademicTreasuryEvent event : treasuryEventsMap.get(executionYear)) {
                totalAmount = totalAmount.add(event.getAmountToPay());
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
                    pendingErpTuitionInfo
                            .cancelExportation(Constants.bundle("label.ERPTuitionInfo.cancelExportation.delta.zero"));
                }

                continue;
            }

            if (org.fenixedu.academictreasury.util.Constants.isZero(deltaAmount)) {
                continue;
            }

            ERPTuitionInfo.create(customer, executionYear, product, totalAmount, deltaAmount, executionYear.getBeginLocalDate(),
                    executionYear.getEndLocalDate());
        }
    }

}
