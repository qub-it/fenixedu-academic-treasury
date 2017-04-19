package org.fenixedu.academictreasury.domain.exemptions.requests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.util.ExcelUtils;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.exemption.TreasuryExemptionType;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;

public class ExemptionsGenerationRequestFile extends ExemptionsGenerationRequestFile_Base {

    public static final Comparator<ExemptionsGenerationRequestFile> COMPARE_BY_CREATION_DATE =
            new Comparator<ExemptionsGenerationRequestFile>() {

                @Override
                public int compare(final ExemptionsGenerationRequestFile o1, final ExemptionsGenerationRequestFile o2) {
                    int c = o1.getVersioningCreationDate().compareTo(o2.getVersioningCreationDate());

                    return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
                }
            };

    private static final int MAX_COLS = 24;

    private static final int STUDENT_NUMBER_IDX = 0;
    private static final int STUDENT_NAME_IDX = 1;
    private static final int DEGREE_IDX = 2;
    private static final int EXECUTION_YEAR_IDX = 3;
    private static final int TREASURY_EVENT_IDX = 4;
    private static final int DEBIT_ENTRY_IDX = 5;
    private static final int REASON_IDX = 6;
    private static final int AMOUNT_TO_EXEMPT_IDX = 7;
    private static final int TUITION_INSTALLMENT_ORDER_IDX = 8;

    protected ExemptionsGenerationRequestFile() {
        super();

        setBennu(Bennu.getInstance());
    }

    protected ExemptionsGenerationRequestFile(final TreasuryExemptionType treasuryExemptionType, final String filename,
            final byte[] content) {
        this();
        setTreasuryExemptionType(treasuryExemptionType);

        init(filename, filename, content);

        checkRules();
    }

    private void checkRules() {
        if (getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.bennu.required");
        }

        if (getTreasuryExemptionType() == null) {
            throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.treasuryExemptionType.required");
        }
    }

    @Override
    public boolean isAccessible(final User user) {
        return TreasuryAccessControlAPI.isBackOfficeMember(user);
    }

    @Atomic
    public void process() {
        List<ExemptionsGenerationRowResult> rowResult =
                ExemptionsGenerationRequestFile.readExcel(getTreasuryExemptionType(), getContent());

        for (final ExemptionsGenerationRowResult row : rowResult) {
            try {
                if (row.isTreasuryEventForRegistrationTuition()) {
                    for (int installmentOrder : row.getTuitionInstallmentsOrderSet()) {
                        DebitEntry tuitionDebitEntry = row.getTuitionDebitEntry(installmentOrder);
                        if (tuitionDebitEntry != null) {
                            TreasuryExemption.create(getTreasuryExemptionType(), row.getTreasuryEvent(), row.getReason(),
                                    row.getDiscountAmount(installmentOrder), tuitionDebitEntry);
                        }
                    }
                } else {
                    TreasuryExemption.create(getTreasuryExemptionType(), row.getTreasuryEvent(), row.getReason(),
                            row.getDiscountAmount(), row.getDebitEntry());
                }
            } catch (final DomainException e) {
                throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.unable.to.create.exemption",
                        String.valueOf(row.getRowNum()), String.valueOf(row.getRegistration().getNumber()),
                        row.getRegistration().getStudent().getName(), e.getLocalizedMessage());
            }
        }

        setWhenProcessed(new DateTime());
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<ExemptionsGenerationRequestFile> findAll() {
        return Bennu.getInstance().getExemptionsGenerationRequestFileSet().stream();
    }

    @Atomic
    public static ExemptionsGenerationRequestFile create(final TreasuryExemptionType treasuryExemptionType, final String filename,
            final byte[] content) {
        return new ExemptionsGenerationRequestFile(treasuryExemptionType, filename, content);
    }

    public static List<ExemptionsGenerationRowResult> readExcel(final TreasuryExemptionType treasuryExemptionType,
            byte[] content) {
        try {

            if (treasuryExemptionType == null) {
                throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.treasuryExemptionType.required");
            }

            final List<List<String>> data = ExcelUtils.readExcel(new ByteArrayInputStream(content), MAX_COLS);

            final List<ExemptionsGenerationRowResult> result = Lists.newArrayList();

            int rowNum = 0;
            for (final List<String> row : data) {
                rowNum++;
                if (rowNum == 1) {
                    continue;
                }

                final String studentNumberValue = trim(row.get(STUDENT_NUMBER_IDX));
                final String studentNameValue = trim(row.get(STUDENT_NAME_IDX));
                final String degreeCodeValue = trim(row.get(DEGREE_IDX));
                final String executionYearValue = trim(row.get(EXECUTION_YEAR_IDX));
                final String treasuryEventValue = trim(row.get(TREASURY_EVENT_IDX));
                final String debitEntryValue = trim(row.get(DEBIT_ENTRY_IDX));
                final String reasonValue = trim(row.get(REASON_IDX));
                final String amountToExemptValue = trim(row.get(AMOUNT_TO_EXEMPT_IDX));

                if (Strings.isNullOrEmpty(studentNumberValue)) {
                    continue;
                }

                if (Strings.isNullOrEmpty(studentNameValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.student.name.required",
                            String.valueOf(rowNum));
                }

                if (Strings.isNullOrEmpty(degreeCodeValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.degree.code.required",
                            String.valueOf(rowNum));
                }

                if (Strings.isNullOrEmpty(executionYearValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.execution.year.required",
                            String.valueOf(rowNum));
                }

                if (Strings.isNullOrEmpty(treasuryEventValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.treasuryEvent.required",
                            String.valueOf(rowNum));
                }

                if (Strings.isNullOrEmpty(amountToExemptValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.amountToExempt.required",
                            String.valueOf(rowNum));
                }

                if (Strings.isNullOrEmpty(reasonValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.reasonValue.required",
                            String.valueOf(rowNum));
                }

                int registrationNumber = -1;
                try {
                    registrationNumber = Integer.parseInt(studentNumberValue);
                } catch (final NumberFormatException e) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.student.number.invalid",
                            String.valueOf(rowNum), studentNumberValue);
                }

                final Degree degree = Degree.find(degreeCodeValue);

                if (degree == null) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.degree.invalid",
                            String.valueOf(rowNum), degreeCodeValue);
                }

                final ExecutionYear executionYear = ExecutionYear.readExecutionYearByName(executionYearValue);

                if (executionYear == null) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.executionYear.invalid",
                            String.valueOf(rowNum), executionYearValue);
                }

                final Registration registration = findActiveRegistration(executionYear, registrationNumber, degree, null, rowNum);

                if (registration == null) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.registration.not.found",
                            String.valueOf(rowNum), studentNumberValue, degreeCodeValue,
                            degree.getPresentationNameI18N().getContent());
                }

                if (!registration.getStudent().getName().trim().equals(studentNameValue)) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.student.name.not.equal",
                            String.valueOf(rowNum), studentNameValue, registration.getStudent().getName().trim());
                }

                final Person person = registration.getPerson();
                final String fiscalCountryCode = PersonCustomer.countryCode(person);
                final String fiscalNumber = PersonCustomer.fiscalNumber(person);
                if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
                    throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
                }

                if (!PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber).isPresent()) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.student.has.no.debt.account",
                            String.valueOf(rowNum), studentNumberValue);
                }

                final PersonCustomer personCustomer = PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber).get();
                if (!personCustomer.isActive()) {
                    throw new AcademicTreasuryDomainException("error.PersonCustomer.not.active", fiscalCountryCode, fiscalNumber);
                }

                TreasuryEvent treasuryEvent = null;

                // First find treasury event by description
                final Set<AcademicTreasuryEvent> treasuryEventsSet = AcademicTreasuryEvent
                        .findByDescription(person, treasuryEventValue, true).collect(Collectors.<AcademicTreasuryEvent> toSet());
                if (treasuryEventsSet.size() > 0) {

                    if (treasuryEventsSet.size() > 1) {
                        throw new AcademicTreasuryDomainException(
                                "error.ExemptionsGenerationRequestFile.found.more.than.one.treasuryEvent", String.valueOf(rowNum),
                                treasuryEventValue);
                    }

                    treasuryEvent = treasuryEventsSet.iterator().next();
                }

                if (treasuryEvent == null) {
                    // Find by description not found, find by tuition or academic tax
                    final Product product = Product.findByName(treasuryEventValue).findFirst().orElse(null);

                    if (product != null) {
                        if (product == TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get()
                                .getCurrentProduct()) {
                            // Find by registration tuition
                            treasuryEvent = AcademicTreasuryEvent.findUniqueForRegistrationTuition(registration, executionYear)
                                    .orElse(null);
                        } else if (AcademicTax.findUnique(product).isPresent()) {
                            // Find by academic tax
                            treasuryEvent = AcademicTreasuryEvent
                                    .findUniqueForAcademicTax(registration, executionYear, AcademicTax.findUnique(product).get())
                                    .orElse(null);
                        }
                    }
                }

                if (treasuryEvent == null) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.treasuryEvent.invalid",
                            String.valueOf(rowNum), treasuryEventValue);
                }

                DebitEntry debitEntry = null;
                if (!Strings.isNullOrEmpty(debitEntryValue)) {
                    Set<DebitEntry> debitEntries = DebitEntry.findActiveByDescription(treasuryEvent, debitEntryValue, true)
                            .collect(Collectors.<DebitEntry> toSet());

                    if (debitEntries.size() == 0) {
                        throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.debitEntry.invalid",
                                String.valueOf(rowNum), debitEntryValue, treasuryEvent.getDescription().getContent());
                    } else if (debitEntries.size() > 1) {
                        throw new AcademicTreasuryDomainException(
                                "error.ExemptionsGenerationRequestFile.debitEntry.found.more.than.one", String.valueOf(rowNum),
                                debitEntryValue, treasuryEvent.getDescription().getContent());
                    } else {
                        debitEntry = debitEntries.iterator().next();
                    }
                }

                BigDecimal amountToExempt = null;
                try {
                    amountToExempt = new BigDecimal(amountToExemptValue);
                } catch (final NumberFormatException e) {
                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.amountToExempt.invalid",
                            String.valueOf(rowNum), amountToExemptValue);
                }

                final SortedSet<Integer> tuitionInstallmentsOrderSet = Sets.newTreeSet();
                if (isTreasuryEventForRegistrationTuition(treasuryEvent)) {

                    for (int i = TUITION_INSTALLMENT_ORDER_IDX; i < row.size(); i++) {
                        final String installmentOrderValue = trim(row.get(i));

                        if (Strings.isNullOrEmpty(installmentOrderValue)) {
                            continue;
                        }

                        int installmentOrder = -1;
                        try {
                            installmentOrder = Integer.parseInt(installmentOrderValue);
                        } catch (final NumberFormatException e) {
                            throw new AcademicTreasuryDomainException(
                                    "error.ExemptionsGenerationRequestFile.installmentOrder.invalid", String.valueOf(rowNum),
                                    installmentOrderValue);
                        }

                        if (installmentOrder <= 0) {
                            throw new AcademicTreasuryDomainException(
                                    "error.ExemptionsGenerationRequestFile.installmentOrder.invalid", String.valueOf(rowNum),
                                    installmentOrderValue);
                        }

                        {
                            final int installmentOrderFinal = installmentOrder;
                            if (installmentOrderFinal > 0) {
                                final Set<? extends DebitEntry> debitEntriesSet = DebitEntry.findActive(treasuryEvent)
                                        .filter(d -> d.getProduct().getTuitionInstallmentOrder() == installmentOrderFinal)
                                        .collect(Collectors.<DebitEntry> toSet());

                                if (debitEntriesSet.size() > 1) {
                                    throw new AcademicTreasuryDomainException(
                                            "error.ExemptionsGenerationRequestFile.installmentOrder.debit.entries.found.more.than.one",
                                            String.valueOf(rowNum), installmentOrderValue);
                                } else if(debitEntriesSet.isEmpty()) {
                                    throw new AcademicTreasuryDomainException(
                                            "error.ExemptionsGenerationRequestFile.installmentOrder.debit.entries.not.found",
                                            String.valueOf(rowNum), installmentOrderValue);
                                }

                                final DebitEntry tuitionDebitEntry = debitEntriesSet.iterator().next();
                                if (Constants.isLessThan(amountToExempt, BigDecimal.ZERO) || Constants.isGreaterThan(amountToExempt, tuitionDebitEntry.getTotalAmount())) {
                                    throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.amountToExempt.invalid",
                                            String.valueOf(rowNum), amountToExemptValue);
                                }
                                
                                tuitionInstallmentsOrderSet.add(installmentOrder);
                            }
                        }
                    }

                    if (tuitionInstallmentsOrderSet.isEmpty()) {
                        throw new AcademicTreasuryDomainException(
                                "error.ExemptionsGenerationRequestFile.installmentOrders.required", String.valueOf(rowNum));
                    }
                }

                if (debitEntry == null && !isTreasuryEventForRegistrationTuition(treasuryEvent)) {
                    final Set<DebitEntry> debitEntries =
                            DebitEntry.findActive(treasuryEvent).collect(Collectors.<DebitEntry> toSet());

                    if (debitEntries.size() == 0) {
                        throw new AcademicTreasuryDomainException(
                                "error.ExemptionsGenerationRequestFile.event.has.no.debitEntries", String.valueOf(rowNum),
                                treasuryEvent.getDescription().getContent());
                    } else if (debitEntries.size() > 1) {
                        throw new AcademicTreasuryDomainException(
                                "error.ExemptionsGenerationRequestFile.event.found.more.than.one.debit.entry",
                                String.valueOf(rowNum), treasuryEvent.getDescription().getContent());
                    } else {
                        debitEntry = debitEntries.iterator().next();

                        if (Constants.isLessThan(amountToExempt, BigDecimal.ZERO) || Constants.isGreaterThan(amountToExempt, debitEntry.getTotalAmount())) {
                            throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.amountToExempt.invalid",
                                    String.valueOf(rowNum), amountToExemptValue);
                        }
                        
                    }
                }

                
                final ExemptionsGenerationRowResult rowResult = new ExemptionsGenerationRowResult(rowNum, registration,
                        executionYear, treasuryEvent, debitEntry, amountToExempt, reasonValue, tuitionInstallmentsOrderSet);

                result.add(rowResult);
            }

            if (result.isEmpty()) {
                throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.result.empty");
            }

            return result;
        } catch (final IOException e) {
            throw new AcademicTreasuryDomainException("error.ExemptionsGenerationRequestFile.invalid.sheet");
        }
    }

    private static boolean isTreasuryEventForRegistrationTuition(final TreasuryEvent treasuryEvent) {
        return treasuryEvent instanceof AcademicTreasuryEvent && ((AcademicTreasuryEvent) treasuryEvent).isTuitionEvent();
    }

    private static Registration findActiveRegistration(final ExecutionYear executionYear, final int registrationNumber,
            final Degree degree, final String dcpName, final int rowNum) {

        Registration result = null;
        for (final Registration registration : Registration.readByNumber(registrationNumber)) {
            if (registration.getLastStateType() == null && !registration.getLastStateType().isActive()) {
                continue;
            }

            if (registration.getDegree() != degree) {
                continue;
            }

            if (!Strings.isNullOrEmpty(dcpName) && registration.getStudentCurricularPlan(executionYear) == null) {
                continue;
            } else if (Strings.isNullOrEmpty(dcpName) && registration.getLastStudentCurricularPlan() == null) {
                continue;
            }

            if (!Strings.isNullOrEmpty(dcpName)
                    && !registration.getStudentCurricularPlan(executionYear).getName().equals(dcpName)) {
                continue;
            }

            if (result != null) {
                throw new AcademicTreasuryDomainException(
                        "error.ExemptionsGenerationRequestFile.found.more.than.one.registration", String.valueOf(rowNum));
            }

            result = registration;
        }

        return result;
    }

    private static String trim(String string) {
        if (string == null) {
            return null;
        }

        return string.trim();
    }
}
