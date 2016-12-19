package org.fenixedu.academictreasury.domain.customer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.PhysicalAddress;
import org.fenixedu.academic.dto.person.PersonBean;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.CustomerType;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.util.Constants;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;

public class PersonCustomer extends PersonCustomer_Base {

    private static final String STUDENT_CODE = "STUDENT";
    private static final String CANDIDACY_CODE = "CANDIDATE";

    protected PersonCustomer() {
        super();
    }

    protected PersonCustomer(final Person person, final String fiscalCountry, final String fiscalNumber) {
        this();

        setPerson(person);
        setCustomerType(getDefaultCustomerType(this));
        super.setCountryCode(fiscalCountry);
        super.setFiscalNumber(fiscalNumber);

        checkRules();
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        /* Person Customer can be associated to Person with only one of two relations
         */

        if (getPerson() == null && getPersonForInactivePersonCustomer() == null) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.person.required");
        }

        if (Strings.isNullOrEmpty(getCountryCode())) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.countryCode");
        }

        if (Strings.isNullOrEmpty(getFiscalNumber())) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalNumber");
        }

        if (getPerson() != null && getPersonForInactivePersonCustomer() != null) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.may.only.be.related.to.person.with.one.relation");
        }

        if (DEFAULT_FISCAL_NUMBER.equals(getFiscalNumber()) && !Constants.isDefaultCountry(getFiscalCountry())) {
            throw new AcademicTreasuryDomainException(
                    "error.PersonCustomer.default.fiscal.number.applied.only.to.default.country");
        }

        if (!DEFAULT_FISCAL_NUMBER.equals(getFiscalNumber())
                && find(getPerson(), getFiscalCountry(), getFiscalNumber()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.person.customer.duplicated");
        }
    }

    @Override
    public String getCode() {
        return this.getExternalId();
    }

    public static String fiscalNumber(final Person person) {
        return person.getSocialSecurityNumber();
    }

    @Override
    public String getName() {
        if (!isActive()) {
            return getPersonForInactivePersonCustomer().getName();
        }

        return getPerson().getName();
    }

    @Override
    public String getFirstNames() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();
        return person.getProfile().getGivenNames();
    }

    @Override
    public String getLastNames() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();
        return person.getProfile().getFamilyNames();
    }

    @Override
    public String getEmail() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();
        return person.getDefaultEmailAddressValue();
    }

    @Override
    public String getPhoneNumber() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();

        if (!Strings.isNullOrEmpty(person.getDefaultPhoneNumber())) {
            return person.getDefaultPhoneNumber();
        }

        return person.getDefaultMobilePhoneNumber();
    }

    @Override
    public String getIdentificationNumber() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();
        return identificationNumber(person);
    }

    public static String identificationNumber(final Person person) {
        return person.getDocumentIdNumber();
    }

    private PhysicalAddress getPhysicalAddress() {
        return physicalAddress(isActive() ? getPerson() : getPersonForInactivePersonCustomer());
    }

    public static PhysicalAddress physicalAddress(final Person person) {
        if (person.getDefaultPhysicalAddress() != null) {
            return person.getDefaultPhysicalAddress();
        }

        if (person.getPendingOrValidPhysicalAddresses().size() == 1) {
            return person.getPendingOrValidPhysicalAddresses().get(0);
        }

        return null;
    }

    @Override
    public String getAddress() {
        if (getPhysicalAddress() == null) {
            return null;
        }

        return getPhysicalAddress().getAddress();
    }

    @Override
    public String getDistrictSubdivision() {
        if (getPhysicalAddress() == null) {
            return null;
        }

        if (!Strings.isNullOrEmpty(getPhysicalAddress().getArea())) {
            return getPhysicalAddress().getArea();
        }

        if (!Strings.isNullOrEmpty(getPhysicalAddress().getDistrictSubdivisionOfResidence())) {
            return getPhysicalAddress().getDistrictSubdivisionOfResidence();
        }

        if (!Strings.isNullOrEmpty(getPhysicalAddress().getDistrictOfResidence())) {
            return getPhysicalAddress().getDistrictOfResidence();
        }

        return null;
    }

    @Override
    public String getDistrict() {
        if (getPhysicalAddress() == null) {
            return null;
        }

        return getPhysicalAddress().getDistrictOfResidence();
    }

    @Override
    public String getZipCode() {
        if (getPhysicalAddress() == null) {
            return null;
        }

        return getPhysicalAddress().getAreaCode();
    }

    @Override
    public String getAddressCountryCode() {
        if (getPhysicalAddress() == null) {
            return null;
        }

        if (getPhysicalAddress().getCountryOfResidence() == null) {
            return null;
        }

        return getPhysicalAddress().getCountryOfResidence().getCode();
    }

    @Override
    public String getCountryCode() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();

        return countryCode(person);
    }

    public static String countryCode(final Person person) {
        return person.getFiscalCountry() != null ? person.getFiscalCountry().getCode() : null;
    }

    public static String countryCode(final PersonBean personBean) {
        if (personBean == null) {
            return null;
        }

        if (personBean.getFiscalCountry() == null) {
            return null;
        }

        return personBean.getFiscalCountry().getCode();
    }

    public static boolean isValidFiscalNumber(final Person person) {
        return FiscalCodeValidation.isValidFiscalNumber(countryCode(person), fiscalNumber(person));
    }

    @Override
    public String getNationalityCountryCode() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();

        if (person.getCountry() != null) {
            return person.getCountry().getCode();
        }

        return null;
    }

    @Override
    public String getBusinessIdentification() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();

        if (person.getStudent() != null) {
            return person.getStudent().getNumber().toString();
        }

        return this.getIdentificationNumber();
    }

    @Override
    public String getFiscalCountry() {
        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();

        return countryCode(person);
    }

    @Override
    public String getPaymentReferenceBaseCode() {
        return this.getCode();
    }

    @Override
    public boolean isPersonCustomer() {
        return true;
    }

    @Override
    public boolean isActive() {
        return getPerson() != null;
    }

    public boolean isBlockingAcademicalActs(final LocalDate when) {

        if (DebtAccount.find(this).map(da -> Constants.isGreaterThan(da.getTotalInDebt(), BigDecimal.ZERO))
                .reduce((a, c) -> a || c).orElse(Boolean.FALSE)) {
            return DebitEntry.find(this).map(d -> isDebitEntryBlockingAcademicalActs(d, when)).reduce((a, c) -> a || c)
                    .orElse(Boolean.FALSE);
        }

        return false;
    }

    public static boolean isDebitEntryBlockingAcademicalActs(final DebitEntry debitEntry, final LocalDate when) {
        if (debitEntry.isAnnulled()) {
            return false;
        }

        if (!debitEntry.isInDebt()) {
            return false;
        }

        if (!debitEntry.isBlockAcademicActsOnDebt() && !debitEntry.isDueDateExpired(when)) {
            return false;
        }

        if (!AcademicTreasurySettings.getInstance().isAcademicalActBlocking(debitEntry.getProduct())) {
            return false;
        }

        if (debitEntry.isAcademicalActBlockingSuspension()) {
            return false;
        }

        return true;
    }

    public BigDecimal getGlobalBalance() {
        BigDecimal globalBalance = BigDecimal.ZERO;

        final Person person = isActive() ? getPerson() : getPersonForInactivePersonCustomer();

        if (person.getPersonCustomer() != null) {
            for (final DebtAccount debtAccount : person.getPersonCustomer().getDebtAccountsSet()) {
                globalBalance = globalBalance.add(debtAccount.getTotalInDebt());
            }
        }

        for (final PersonCustomer personCustomer : person.getInactivePersonCustomersSet()) {
            for (final DebtAccount debtAccount : personCustomer.getDebtAccountsSet()) {
                globalBalance = globalBalance.add(debtAccount.getTotalInDebt());
            }
        }

        return globalBalance;
    }

    public void mergeWithPerson(final Person person) {

        if (getPerson() == person) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.merging.not.happening");
        }

        if (getPersonForInactivePersonCustomer() == person) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.merged.already.with.person");
        }

        if (person.getPersonCustomer() != null) {
            final PersonCustomer personCustomer = person.getPersonCustomer();
            personCustomer.setPersonForInactivePersonCustomer(getPerson());
            personCustomer.setPerson(null);
        }

        for (final PersonCustomer personCustomer : person.getInactivePersonCustomersSet()) {
            personCustomer.setPersonForInactivePersonCustomer(getPerson());
        }

        checkRules();
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<? extends PersonCustomer> findAll() {
        return Customer.findAll().filter(c -> c instanceof PersonCustomer).map(PersonCustomer.class::cast);
    }

    public static Stream<? extends PersonCustomer> find(final Person person) {
        return findAll().filter(pc -> pc.getPerson() == person || pc.getPersonForInactivePersonCustomer() == person);
    }

    protected static Stream<? extends PersonCustomer> find(final Person person, final String fiscalCountryCode,
            final String fiscalNumber) {
        return find(person).filter(pc -> pc.getFiscalCountry() != null && pc.getFiscalCountry().equals(fiscalCountryCode)
                && !Strings.isNullOrEmpty(pc.getFiscalNumber()) && pc.getFiscalNumber().equals(fiscalNumber));
    }

    public static Optional<? extends PersonCustomer> findUnique(final Person person, final String fiscalCountryCode,
            final String fiscalNumber) {
        return find(person, fiscalCountryCode, fiscalNumber).findFirst();
    }

//    public static Stream<? extends PersonCustomer> findInactivePersonCustomers(final Person person) {
//        return PersonCustomer.findAll().filter(pc -> pc.getPersonForInactivePersonCustomer() == person);
//    }

    @Atomic
    public static PersonCustomer create(final Person person, final String fiscalCountry, final String fiscalNumber) {
        return new PersonCustomer(person, fiscalCountry, fiscalNumber);
    }

    public static boolean switchCustomer(final Person person, final String fiscalCountryCode, final String fiscalNumber) {
        PersonCustomer personCustomer = person.getPersonCustomer();
        Optional<? extends PersonCustomer> newCustomer = PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber);

        if (newCustomer.isPresent() && newCustomer.get().isActive()) {
            return false;
        }

        personCustomer.setPerson(null);
        personCustomer.setPersonForInactivePersonCustomer(person);
        if (!newCustomer.isPresent()) {
            PersonCustomer.create(person, fiscalCountryCode, fiscalNumber);
            newCustomer = PersonCustomer.findUnique(person, fiscalCountryCode, fiscalNumber);
        } else {
            newCustomer.get().setPerson(person);
            newCustomer.get().setPersonForInactivePersonCustomer(null);
        }

        personCustomer.checkRules();
        newCustomer.get().checkRules();

        return true;
    }

    public static CustomerType getDefaultCustomerType(PersonCustomer person) {
        if (person.getPerson().getStudent() != null) {
            return CustomerType.findByCode(STUDENT_CODE).findFirst().orElse(null);
        } else {
            return CustomerType.findByCode(CANDIDACY_CODE).findFirst().orElse(null);
        }
    }

}
