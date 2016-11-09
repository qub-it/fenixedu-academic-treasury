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
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class ERPTuitionInfo extends ERPTuitionInfo_Base {

    public static Comparator<ERPTuitionInfo> COMPARE_BY_CREATION_DATE = new Comparator<ERPTuitionInfo>() {

        @Override
        public int compare(final ERPTuitionInfo o1, final ERPTuitionInfo o2) {
            int c = o1.getCreationDate().compareTo(o2.getCreationDate());
            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }
    };

    public ERPTuitionInfo(final Customer customer, final ExecutionYear executionYear, final Product product,
            final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount, final LocalDate beginDate,
            final LocalDate endDate) {
        super();
        setBennu(Bennu.getInstance());
        setCreationDate(new DateTime());

        checkRules();
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
        
        if(Constants.isZero(getDeltaTuitionAmount())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.cannot.be.zero");
        }

        if (getBeginDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.beginDate");
        }

        if (getEndDate() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.endDate");
        }
        
        if(getBeginDate().isAfter(getEndDate())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.endDate");
        }
        
        if(Constants.isPositive(getDeltaTuitionAmount() && !getDocumentNumberSeries().getFinantialDocumentType().getType().isDebitNote()) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfo.deltaTuitionAmount.positve");
        }
    }

    public boolean isPendingToExport() {
        return getBennuPendingToExport() != null;
    }

    public boolean isIntegratedWithSuccess() {
        return getIntegrationSuccess();
    }

    public void integratedWithSuccess() {
    }

    public void cancelExportation(final String reason) {
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

    public static Optional<ERPTuitionInfo> findLastPendingToExport(final Customer customer, final ExecutionYear executionYear,
            final Product product) {
        return findPendingToExport(customer, executionYear, product).sorted(COMPARE_BY_CREATION_DATE.reversed()).findFirst();
    }

    public static Optional<ERPTuitionInfo> findFirstIntegratedWithSuccess(final Customer customer,
            final ExecutionYear executionYear, final Product product) {
        return find(customer, executionYear, product).filter(t -> t.isIntegratedWithSuccess()).sorted(COMPARE_BY_CREATION_DATE)
                .findFirst();
    }

    public static Optional<ERPTuitionInfo> findFirstIntegratedWithSuccess(final ERPTuitionInfo erpTuitionInfo) {
        return findFirstIntegratedWithSuccess(erpTuitionInfo.getCustomer(), erpTuitionInfo.getExecutionYear(),
                erpTuitionInfo.getProduct());
    }

    public static Optional<ERPTuitionInfo> findLastIntegratedWithSuccess(final Customer customer,
            final ExecutionYear executionYear, final Product product) {
        return find(customer, executionYear, product).filter(t -> t.isIntegratedWithSuccess())
                .sorted(COMPARE_BY_CREATION_DATE.reversed()).findFirst();
    }

    public static ERPTuitionInfo create(final Customer customer, final ExecutionYear executionYear, final Product product,
            final BigDecimal tuitionTotalAmount, final BigDecimal deltaTuitionAmount, final LocalDate beginDate,
            final LocalDate endDate) {

        return new ERPTuitionInfo(customer, executionYear, product, tuitionTotalAmount, deltaTuitionAmount, beginDate, endDate);
    }

    public static void exportTuitionInformation() {
        for (final PersonCustomer customer : PersonCustomer.findAll().collect(Collectors.toSet())) {
            final ERPTuitionInfoExporterThread thread = new ERPTuitionInfoExporterThread(customer);
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public static class ERPTuitionInfoExporterThread extends Thread {

        private String personCustomerId;

        public ERPTuitionInfoExporterThread(final PersonCustomer personCustomer) {
            this.personCustomerId = personCustomer.getExternalId();
        }

        @Override
        public void run() {
            final PersonCustomer customer = FenixFramework.getDomainObject(personCustomerId);

            for (final Product product : ERPTuitionInfoType.findProducts().collect(Collectors.toSet())) {
                exportTuitionInfoForCustomerAndProduct(customer, product);
            }

            super.run();
        }
    }

    @Atomic
    private static void exportTuitionInfoForCustomerAndProduct(final PersonCustomer customer, final Product product) {
        final Multimap<ExecutionYear, AcademicTreasuryEvent> treasuryEventsMap = ArrayListMultimap.create();

        for (final ERPTuitionInfoType type : ERPTuitionInfoType.findByProduct(product).collect(Collectors.toSet())) {
            for (final AcademicTreasuryEvent event : AcademicTreasuryEvent.find(customer).collect(Collectors.toSet())) {
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

            if (findLastPendingToExport(customer, executionYear, product).isPresent()) {
                findLastPendingToExport(customer, executionYear, product).get().cancelExportation("Something");
            }

            final Optional<ERPTuitionInfo> lastIntegratedWithSuccess =
                    findLastIntegratedWithSuccess(customer, executionYear, product);
            final BigDecimal deltaAmount = totalAmount.subtract(lastIntegratedWithSuccess.isPresent() ? lastIntegratedWithSuccess
                    .get().getTuitionTotalAmount() : BigDecimal.ZERO);

            ERPTuitionInfo.create(customer, executionYear, product, totalAmount, deltaAmount, executionYear.getBeginLocalDate(),
                    executionYear.getEndLocalDate());
        }
    }

}
