package org.fenixedu.academictreasury.domain.tariff;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.administrativeOffice.AdministrativeOffice;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent.AcademicTreasuryEventKeys;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.dto.tariff.AcademicTariffBean;
import org.fenixedu.academictreasury.services.AcademicTreasuryPlataformDependentServicesFactory;
import org.fenixedu.academictreasury.services.IAcademicTreasuryPlatformDependentServices;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.tariff.DueDateCalculationType;
import org.fenixedu.treasury.domain.tariff.InterestRate;
import org.fenixedu.treasury.domain.tariff.InterestType;
import org.fenixedu.treasury.domain.tariff.Tariff;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pt.ist.fenixframework.Atomic;

public class AcademicTariff extends AcademicTariff_Base {

    protected AcademicTariff(final FinantialEntity finantialEntity, final Product product, final AcademicTariffBean bean) {
        super();

        init(finantialEntity, product, bean);
    }

    @Override
    protected void init(final FinantialEntity finantialEntity, final Product product, final DateTime beginDate,
            final DateTime endDate, final DueDateCalculationType dueDateCalculationType, final LocalDate fixedDueDate,
            final int numberOfDaysAfterCreationForDueDate, final boolean applyInterests, final InterestType interestType,
            final int numberOfDaysAfterDueDate, final boolean applyInFirstWorkday, final int maximumDaysToApplyPenalty,
            final BigDecimal interestFixedAmount, final BigDecimal rate) {
        throw new RuntimeException("wrong call");
    }

    protected void init(final FinantialEntity finantialEntity, final Product product, final AcademicTariffBean bean) {

        super.init(finantialEntity, product, bean.getBeginDate().toDateTimeAtStartOfDay(),
                bean.getEndDate() != null ? bean.getEndDate().toDateTimeAtStartOfDay() : null, bean.getDueDateCalculationType(),
                bean.getFixedDueDate() != null ? bean.getFixedDueDate() : null, bean.getNumberOfDaysAfterCreationForDueDate(),
                bean.isApplyInterests(), bean.getInterestType(), bean.getNumberOfDaysAfterDueDate(), bean.isApplyInFirstWorkday(),
                bean.getMaximumDaysToApplyPenalty(), bean.getInterestFixedAmount(),
                bean.getRate());

        setBaseAmount(bean.getBaseAmount());
        setUnitsForBase(bean.getUnitsForBase());
        setUnitAmount(bean.getUnitAmount());
        setPageAmount(bean.getPageAmount());
        setMaximumAmount(bean.getMaximumAmount());
        setUrgencyRate(bean.getUrgencyRate());
        setLanguageTranslationRate(bean.getLanguageTranslationRate());

        setAdministrativeOffice(bean.getAdministrativeOffice());
        setDegreeType(bean.getDegreeType());
        setDegree(bean.getDegree());
        setCycleType(bean.getCycleType());

        checkRules();
    }

    public boolean isApplyUnitsAmount() {
        return isPositive(getUnitAmount());
    }

    public boolean isApplyPagesAmount() {
        return isPositive(getPageAmount());
    }

    public boolean isApplyMaximumAmount() {
        return isPositive(getMaximumAmount());
    }

    public boolean isApplyUrgencyRate() {
        return isPositive(getUrgencyRate());
    }

    public boolean isApplyLanguageTranslationRate() {
        return isPositive(getLanguageTranslationRate());
    }

    public boolean isApplyBaseAmount() {
        return isPositive(getBaseAmount());
    }

    @Override
    protected void checkRules() {
        super.checkRules();

        if (getCycleType() != null && getDegree() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.degree.required");
        }

        if (getDegree() != null && getDegreeType() != getDegree().getDegreeType()) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.degreeType.required");
        }

        if (getCycleType() != null && !getDegreeType().getCycleTypes().contains(getCycleType())) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.cycleType.does.not.belong.degree.type");
        }

        if (getBaseAmount() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.baseAmount.required");
        }

        if (isNegative(getBaseAmount())) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.baseAmount.negative");
        }

        if (getUnitsForBase() < 0) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.unitsForBase.negative");
        }

        if (getUnitAmount() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.unitAmount.required");
        }

        if (isNegative(getUnitAmount())) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.unitAmount.negative");
        }

        if (getPageAmount() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.pageAmount.required");
        }

        if (isNegative(getPageAmount())) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.pageAmount.negative");
        }

        if (getUrgencyRate() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.urgencyRate.required");
        }

        if (isNegative(getUrgencyRate())) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.urgencyRate.negative");
        }

        if (AcademicTreasuryConstants.HUNDRED_PERCENT.compareTo(getUrgencyRate()) < 0) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.urgencyRate.greater.than.hundred");
        }

        if (getLanguageTranslationRate() == null) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.languageTranslationRate.required");
        }

        if (isNegative(getLanguageTranslationRate())) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.languageTranslationRate.negative");
        }

        if (AcademicTreasuryConstants.HUNDRED_PERCENT.compareTo(getLanguageTranslationRate()) < 0) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.languageTranslationRate.greater.than.hundred");
        }

        /*
         * The following checkings aims to garantee that are not created overlapping tariffs
         * in all finantial entities.
         */

        if (getCycleType() != null) {
            if (findInInterval(getFinantialEntity(), getProduct(), getAdministrativeOffice(), getDegreeType(), getDegree(), getCycleType(),
                    getInterval()).count() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.overlaps.with.other",
                        getProduct().getName().getContent());
            } ;
        } else if (getDegree() != null) {
            if (findInInterval(getFinantialEntity(), getProduct(), getAdministrativeOffice(), getDegreeType(), getDegree(), getInterval())
                    .filter(t -> t.getCycleType() == null)
                    .count() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.overlaps.with.other",
                        getProduct().getName().getContent());
            }
        } else if (getDegreeType() != null) {
            if (findInInterval(getFinantialEntity(), getProduct(), getAdministrativeOffice(), getDegreeType(), getInterval())
                    .filter(t -> t.getDegree() == null)
                    .count() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.overlaps.with.other",
                        getProduct().getName().getContent());
            } ;
        } else if(getAdministrativeOffice() != null) {
            if (findInInterval(getFinantialEntity(), getProduct(), getAdministrativeOffice(), getInterval())
                    .filter(t -> t.getDegreeType() == null)
                    .count() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.overlaps.with.other",
                        getProduct().getName().getContent());
            }
        } else {
            if (findInInterval(getFinantialEntity(), getProduct(), getInterval())
                    .filter(t -> t.getAdministrativeOffice() == null)
                    .count() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.overlaps.with.other",
                        getProduct().getName().getContent());
            }
        }
    }

    @Atomic
    public void edit(final AcademicTariffBean bean) {
        super.edit(bean.getBeginDate().toDateTimeAtStartOfDay(),
                bean.getEndDate().toDateTimeAtStartOfDay().plusDays(1).minusSeconds(1));

        if (bean.isApplyInterests() && getInterestRate() == null) {
            InterestRate.createForTariff(this, bean.getInterestType(), bean.getNumberOfDaysAfterDueDate(),
                    bean.isApplyInFirstWorkday(), bean.getMaximumDaysToApplyPenalty(),
                    bean.getInterestFixedAmount(), bean.getRate());
        } else if (bean.isApplyInterests()) {
            getInterestRate().edit(bean.getInterestType(), bean.getNumberOfDaysAfterDueDate(), bean.isApplyInFirstWorkday(),
                    bean.getMaximumDaysToApplyPenalty(), bean.getInterestFixedAmount(),
                    bean.getRate());
        }

        super.setApplyInterests(bean.isApplyInterests());

        checkRules();
    }

    @Override
    public boolean isDeletable() {
        return super.isDeletable();
    }

    @Override
    @Atomic
    public void delete() {
        setAdministrativeOffice(null);
        setDegreeType(null);
        setDegree(null);

        super.delete();
    }

    public BigDecimal amountToPay(final AcademicTreasuryEvent academicTreasuryEvent) {
        final int numberOfUnits = academicTreasuryEvent.getNumberOfUnits();
        final int numberOfPages = academicTreasuryEvent.getNumberOfPages(); 
        final Locale language = academicTreasuryEvent.getLanguage();
        final boolean urgentRequest = academicTreasuryEvent.isUrgentRequest();
        
        return amountToPay(numberOfUnits, numberOfPages, language, urgentRequest);
    }

    public BigDecimal amountToPay(final int numberOfUnits, final int numberOfPages, final Locale language, boolean urgentRequest) {
        BigDecimal amount = amountWithLanguageRate(numberOfUnits, numberOfPages, language);

        if (isApplyUrgencyRate() && urgentRequest) {
            amount = amount.add(amountForUrgencyRate(numberOfUnits, numberOfPages, language));
        }

        return amount;
    }

    public BigDecimal amountForUrgencyRate(final int numberOfUnits, final int numberOfPages, final Locale language) {
        BigDecimal amount = amountWithLanguageRate(numberOfUnits, numberOfPages, language);

        return amount.multiply(getUrgencyRate().setScale(20, RoundingMode.HALF_EVEN).divide(AcademicTreasuryConstants.HUNDRED_PERCENT).setScale(2,
                RoundingMode.HALF_EVEN));
    }

    public BigDecimal amountForLanguageTranslationRate(final int numberOfUnits, final int numberOfPages) {
        final BigDecimal amount = amountToPayWithoutRates(numberOfUnits, numberOfPages);

        final BigDecimal result = amount.multiply(getLanguageTranslationRate().setScale(20, RoundingMode.HALF_EVEN)
                .divide(AcademicTreasuryConstants.HUNDRED_PERCENT).setScale(2, RoundingMode.HALF_EVEN));

        return isPositive(result) ? result : BigDecimal.ZERO;
    }

    public BigDecimal amountToPay(final int numberOfUnits, final int numberOfPages) {
        BigDecimal amount = getBaseAmount();

        if (isApplyUnitsAmount()) {
            int remainingUnits = (numberOfUnits - getUnitsForBase()) >= 0 ? numberOfUnits - getUnitsForBase() : 0;
            if (remainingUnits > 0) {
                amount = amount.add(getUnitAmount().multiply(new BigDecimal(remainingUnits)));
            }
        }

        if (isApplyPagesAmount()) {
            amount = amount.add(getPageAmount().multiply(new BigDecimal(numberOfPages)));
        }

        if (isApplyMaximumAmount() && isGreaterThan(amount, getMaximumAmount())) {
            amount = getMaximumAmount();
        }

        return amount;
    }

    public BigDecimal amountToPayWithoutRates(final int numberOfUnits, final int numberOfPages) {
        BigDecimal amount = getBaseAmount();

        if (isApplyUnitsAmount()) {
            int remainingUnits = numberOfAdditionalUnits(numberOfUnits);

            if (remainingUnits > 0) {
                amount = amount.add(amountForAdditionalUnits(numberOfUnits));
            }
        }

        if (isApplyPagesAmount()) {
            amount = amount.add(amountForPages(numberOfPages));
        }

        if (isApplyMaximumAmount() && isGreaterThan(amount, getMaximumAmount())) {
            amount = getMaximumAmount();
        }
        return amount;
    }

    public BigDecimal amountForPages(final int numberOfPages) {
        final BigDecimal result = getPageAmount().multiply(new BigDecimal(numberOfPages));

        return isPositive(result) ? result : BigDecimal.ZERO;
    }

    public BigDecimal amountForAdditionalUnits(final int numberOfUnits) {
        final int remainingUnits = numberOfAdditionalUnits(numberOfUnits);
        final BigDecimal result = getUnitAmount().multiply(new BigDecimal(remainingUnits));

        return isPositive(result) ? result : BigDecimal.ZERO;
    }

    public int numberOfAdditionalUnits(final int numberOfUnits) {
        int result = numberOfUnits - getUnitsForBase();

        return result >= 0 ? result : 0;
    }

    public BigDecimal amountToPay(final AcademicTreasuryEvent academicTreasuryEvent,
            final EnrolmentEvaluation enrolmentEvaluation) {
        return getBaseAmount();
    }

    public Vat vat(final LocalDate when) {
        return Vat.findActiveUnique(getProduct().getVatType(), getFinantialEntity().getFinantialInstitution(),
                when.toDateTimeAtStartOfDay()).get();
    }

    public LocalizedString academicServiceRequestDebitEntryName(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        return AcademicTreasuryEvent.nameForAcademicServiceRequest(getProduct(), iTreasuryServiceRequest);
    }

    public LocalizedString academicTaxDebitEntryName(final AcademicTreasuryEvent academicTreasuryEvent) {
        if (!academicTreasuryEvent.isForAcademicTax()) {
            throw new RuntimeException("wrong call");
        }

        return academicTreasuryEvent.getDescription();
    }

    public static LocalizedString improvementDebitEntryName(final AcademicTax improvementAcademicTax,
            final EnrolmentEvaluation enrolmentEvaluation) {
        final ITreasuryPlatformDependentServices treasuryServices = TreasuryPlataformDependentServicesFactory.implementation();
        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();

        final Registration registration = enrolmentEvaluation.getEnrolment().getRegistration();
        final ExecutionYear executionYear = enrolmentEvaluation.getExecutionPeriod().getExecutionYear();
        
        LocalizedString result = new LocalizedString();

        for (final Locale locale : treasuryServices.availableLocales()) {
            String enrolmentName = academicTreasuryServices.localizedNameOfEnrolment(enrolmentEvaluation.getEnrolment(), locale);
            String executionIntervalName = enrolmentEvaluation.getExecutionPeriod().getQualifiedName();
            String academicTreasuryEventDescription = AcademicTreasuryEvent.nameForAcademicTax(improvementAcademicTax, registration, executionYear).getContent(locale);
            
            result = result.with(locale, academicTreasuryEventDescription + format(" (%s - %s)", enrolmentName, executionIntervalName));
        }

        return result;
    }

    public DebitEntry createDebitEntryForAcademicServiceRequest(final DebtAccount debtAccoubt,
            final AcademicTreasuryEvent academicTreasuryEvent) {
        final LocalDate when = academicTreasuryEvent.getRequestDate();

        return createDebitEntryForAcademicServiceRequest(debtAccoubt, academicTreasuryEvent, when);
    }

    public DebitEntry createDebitEntryForAcademicServiceRequest(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {
        if (!academicTreasuryEvent.isForAcademicServiceRequest()) {
            throw new RuntimeException("wrong call");
        }

        final BigDecimal amount = amountToPay(academicTreasuryEvent);
        final LocalDate dueDate = dueDate(when);
        final LocalizedString debitEntryName = academicServiceRequestDebitEntryName(academicTreasuryEvent.getITreasuryServiceRequest());
        final Vat vat = vat(when);

        updatePriceValuesInEvent(academicTreasuryEvent);

        final Map<String, String> fillPriceProperties =
                fillPriceCommonPropertiesForAcademicServiceRequest(debtAccount, academicTreasuryEvent, when);

        return DebitEntry.create(Optional.<DebitNote> empty(), debtAccount, academicTreasuryEvent, vat, amount, dueDate,
                fillPriceProperties, getProduct(), debitEntryName.getContent(TreasuryConstants.DEFAULT_LANGUAGE), AcademicTreasuryConstants.DEFAULT_QUANTITY,
                this.getInterestRate(), when.toDateTimeAtStartOfDay());
    }

    public DebitEntry createDebitEntryForAcademicTax(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent) {
        final LocalDate when = academicTreasuryEvent.getRequestDate();

        return createDebitEntryForAcademicTax(debtAccount, academicTreasuryEvent, when);
    }

    public DebitEntry createDebitEntryForAcademicTax(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {
        if (!academicTreasuryEvent.isForAcademicTax() || academicTreasuryEvent.isImprovementTax()) {
            throw new RuntimeException("wrong call");
        }

        final LocalDate dueDate = dueDate(when);
        final LocalizedString debitEntryName = academicTaxDebitEntryName(academicTreasuryEvent);
        final Vat vat = vat(when);
        final BigDecimal amount = amountToPay(academicTreasuryEvent);

        updatePriceValuesInEvent(academicTreasuryEvent);

        final Map<String, String> fillPriceProperties =
                fillPricePropertiesForAcademicTax(debtAccount, academicTreasuryEvent, when);

        return DebitEntry.create(Optional.<DebitNote> empty(), debtAccount, academicTreasuryEvent, vat, amount, dueDate,
                fillPriceProperties, getProduct(), debitEntryName.getContent(TreasuryConstants.DEFAULT_LANGUAGE), AcademicTreasuryConstants.DEFAULT_QUANTITY,
                this.getInterestRate(), when.toDateTimeAtStartOfDay());
    }
    
    public DebitEntry createDebitEntryForCustomAcademicDebt(final DebtAccount debtAccount, final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {
        if (!academicTreasuryEvent.isForCustomAcademicDebt()) {
            throw new RuntimeException("wrong call");
        }
        
        final LocalDate dueDate = dueDate(when);
        final LocalizedString debitEntryName = AcademicTreasuryEvent
                .nameForCustomAcademicDebt(academicTreasuryEvent.getProduct(), academicTreasuryEvent.getRegistration(), academicTreasuryEvent.getExecutionYear());
        final Vat vat = vat(when);
        final BigDecimal amount = amountToPay(academicTreasuryEvent);

        final Map<String, String> fillPriceProperties =
                fillPricePropertiesForAcademicTax(debtAccount, academicTreasuryEvent, when);
        
        return DebitEntry.create(Optional.<DebitNote> empty(), debtAccount, academicTreasuryEvent, vat, amount, dueDate,
                fillPriceProperties, getProduct(), debitEntryName.getContent(), TreasuryConstants.DEFAULT_QUANTITY,
                this.getInterestRate(), when.toDateTimeAtStartOfDay());
    }

    public DebitEntry createDebitEntryForImprovement(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final EnrolmentEvaluation enrolmentEvaluation) {
        return createDebitEntryForImprovement(debtAccount, academicTreasuryEvent, enrolmentEvaluation,
                enrolmentEvaluation.getWhenDateTime().toLocalDate());
    }

    public DebitEntry createDebitEntryForImprovement(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final EnrolmentEvaluation enrolmentEvaluation,
            final LocalDate when) {

        if (!academicTreasuryEvent.isForImprovementTax()) {
            throw new RuntimeException("wrong call");
        }

        final LocalizedString debitEntryName = improvementDebitEntryName(academicTreasuryEvent.getAcademicTax(), enrolmentEvaluation);
        final LocalDate dueDate = dueDate(when);
        final Vat vat = vat(when);
        final BigDecimal amount = amountToPay(academicTreasuryEvent, enrolmentEvaluation);

        updatePriceValuesInEvent(academicTreasuryEvent, enrolmentEvaluation);

        final Map<String, String> fillPriceProperties = fillPriceProperties(academicTreasuryEvent, enrolmentEvaluation);

        final DebitEntry debitEntry = DebitEntry.create(Optional.<DebitNote> empty(), debtAccount, academicTreasuryEvent, vat,
                amount, dueDate, fillPriceProperties, getProduct(), debitEntryName.getContent(TreasuryConstants.DEFAULT_LANGUAGE), AcademicTreasuryConstants.DEFAULT_QUANTITY,
                this.getInterestRate(), new DateTime());

        academicTreasuryEvent.associateEnrolmentEvaluation(debitEntry, enrolmentEvaluation);

        if (debitEntry != null) {
            final DebitNote debitNote = DebitNote.create(debtAccount, DocumentNumberSeries
                    .findUniqueDefault(FinantialDocumentType.findForDebitNote(), debtAccount.getFinantialInstitution()).get(),
                    new DateTime());

            debitNote.addDebitNoteEntries(Lists.newArrayList(debitEntry));

            if (AcademicTreasurySettings.getInstance().isCloseServiceRequestEmolumentsWithDebitNote()) {
                debitNote.closeDocument();
            }
        }

        return debitEntry;
    }

    private void updatePriceValuesInEvent(final AcademicTreasuryEvent academicTreasuryEvent,
            final EnrolmentEvaluation enrolmentEvaluation) {
        final BigDecimal baseAmount = getBaseAmount();

        academicTreasuryEvent.updatePricingFields(baseAmount, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    private void updatePriceValuesInEvent(final AcademicTreasuryEvent academicTreasuryEvent) {

        final BigDecimal baseAmount = getBaseAmount();
        final BigDecimal amountForAdditionalUnits = amountForAdditionalUnits(academicTreasuryEvent.getNumberOfUnits());
        final BigDecimal amountForPages = amountForPages(academicTreasuryEvent.getNumberOfPages());
        final BigDecimal maximumAmount = getMaximumAmount();
        final BigDecimal amountForLanguageTranslationRate = amountForLanguageTranslationRate(academicTreasuryEvent.getNumberOfUnits(), academicTreasuryEvent.getNumberOfPages());
        final BigDecimal amountForUrgencyRate = amountForUrgencyRate(academicTreasuryEvent.getNumberOfUnits(), academicTreasuryEvent.getNumberOfPages(), academicTreasuryEvent.getLanguage());

        academicTreasuryEvent.updatePricingFields(baseAmount, amountForAdditionalUnits, amountForPages, maximumAmount,
                amountForLanguageTranslationRate, amountForUrgencyRate);
    }

    private Map<String, String> fillPriceCommonPropertiesForAcademicServiceRequest(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, LocalDate when) {
        final Map<String, String> propertiesMap = Maps.newHashMap();

        propertiesMap.putAll(fillPriceCommonProperties(debtAccount, academicTreasuryEvent, when));

        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getITreasuryServiceRequest().getRegistration().getDegree()
                        .getPresentationNameI18N(academicTreasuryEvent.getITreasuryServiceRequest().getExecutionYear()).getContent(AcademicTreasuryConstants.DEFAULT_LANGUAGE));
        
        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getITreasuryServiceRequest().getRegistration().getDegree().getCode());

        if (academicTreasuryEvent.getITreasuryServiceRequest().hasExecutionYear()) {
            propertiesMap.put(AcademicTreasuryEventKeys.EXECUTION_YEAR.getDescriptionI18N().getContent(),
                    academicTreasuryEvent.getITreasuryServiceRequest().getExecutionYear().getQualifiedName());
        }

        return propertiesMap;
    }

    private Map<String, String> fillPricePropertiesForAcademicTax(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {
        final Map<String, String> propertiesMap = Maps.newHashMap();

        propertiesMap.putAll(fillPriceCommonProperties(debtAccount, academicTreasuryEvent, when));

        propertiesMap.put(AcademicTreasuryEventKeys.EXECUTION_YEAR.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getExecutionYear().getQualifiedName());

        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(), academicTreasuryEvent
                .getRegistration().getDegree().getPresentationNameI18N(academicTreasuryEvent.getExecutionYear()).getContent(AcademicTreasuryConstants.DEFAULT_LANGUAGE));
        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CURRICULAR_PLAN.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getRegistration().getDegreeCurricularPlanName());

        propertiesMap.put(AcademicTreasuryEventKeys.DEGREE_CODE.getDescriptionI18N().getContent(),
                academicTreasuryEvent.getRegistration().getDegree().getCode());

        return propertiesMap;
    }

    private Map<String, String> fillPriceCommonProperties(final DebtAccount debtAccount,
            final AcademicTreasuryEvent academicTreasuryEvent, final LocalDate when) {
        final Map<String, String> propertiesMap = Maps.newHashMap();

        final Currency currency = debtAccount.getFinantialInstitution().getCurrency();

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.BASE_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(getBaseAmount()));
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.UNITS_FOR_BASE.getDescriptionI18N().getContent(),
                String.valueOf(getUnitsForBase()));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.UNIT_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(getUnitAmount()).toString());
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ADDITIONAL_UNITS.getDescriptionI18N().getContent(),
                String.valueOf(numberOfAdditionalUnits(academicTreasuryEvent.getNumberOfUnits())));
        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.CALCULATED_UNITS_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(amountForAdditionalUnits(academicTreasuryEvent.getNumberOfUnits())));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.PAGE_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(getPageAmount()).toString());
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.NUMBER_OF_PAGES.getDescriptionI18N().getContent(),
                String.valueOf(academicTreasuryEvent.getNumberOfPages()));
        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.CALCULATED_PAGES_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(amountForPages(academicTreasuryEvent.getNumberOfPages())));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.MAXIMUM_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(getMaximumAmount()));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FOREIGN_LANGUAGE_RATE.getDescriptionI18N().getContent(),
                getLanguageTranslationRate().toString());
        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.CALCULATED_FOREIGN_LANGUAGE_RATE.getDescriptionI18N()
                .getContent(), currency.getValueFor(amountForLanguageTranslationRate(academicTreasuryEvent.getNumberOfUnits(), academicTreasuryEvent.getNumberOfPages())));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.URGENT_PERCENTAGE.getDescriptionI18N().getContent(),
                getUrgencyRate().toString());
        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.CALCULATED_URGENT_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(amountForUrgencyRate(academicTreasuryEvent.getNumberOfUnits(), academicTreasuryEvent.getNumberOfPages(), academicTreasuryEvent.getLanguage())));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                currency.getValueFor(amountToPay(academicTreasuryEvent)));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.USED_DATE.getDescriptionI18N().getContent(),
                when.toString(AcademicTreasuryConstants.DATE_FORMAT));

        return propertiesMap;
    }

    private Map<String, String> fillPriceProperties(final AcademicTreasuryEvent academicTreasuryEvent,
            final EnrolmentEvaluation improvementEnrolmentEvaluation) {
        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();

        final Map<String, String> propertiesMap = Maps.newHashMap();

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.BASE_AMOUNT.getDescriptionI18N().getContent(),
                getBaseAmount().toString());

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.FINAL_AMOUNT.getDescriptionI18N().getContent(),
                amountToPay(academicTreasuryEvent).toString());

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.ENROLMENT.getDescriptionI18N().getContent(),
                academicTreasuryServices.localizedNameOfEnrolment(improvementEnrolmentEvaluation.getEnrolment()));

        propertiesMap.put(
                AcademicTreasuryEvent.AcademicTreasuryEventKeys.DEGREE_CURRICULAR_PLAN.getDescriptionI18N().getContent(),
                improvementEnrolmentEvaluation.getDegreeCurricularPlan().getName());

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.DEGREE.getDescriptionI18N().getContent(),
                improvementEnrolmentEvaluation.getDegreeCurricularPlan().getDegree().getPresentationNameI18N().getContent(AcademicTreasuryConstants.DEFAULT_LANGUAGE));

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.EXECUTION_SEMESTER.getDescriptionI18N().getContent(),
                improvementEnrolmentEvaluation.getExecutionPeriod().getQualifiedName());

        propertiesMap.put(AcademicTreasuryEvent.AcademicTreasuryEventKeys.EVALUATION_SEASON.getDescriptionI18N().getContent(),
                improvementEnrolmentEvaluation.getEvaluationSeason().getName().getContent(AcademicTreasuryConstants.DEFAULT_LANGUAGE));

        return propertiesMap;
    }

    private BigDecimal amountWithLanguageRate(final int numberOfUnits, final int numberOfPages, final Locale language) {
        BigDecimal amount = amountToPayWithoutRates(numberOfUnits, numberOfPages);

        if (isApplyLanguageTranslationRate() && AcademicTreasuryConstants.isForeignLanguage(language)) {
            amount = amount.add(amountForLanguageTranslationRate(numberOfUnits, numberOfPages));
        }

        return amount;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    @Atomic
    public static AcademicTariff create(final FinantialEntity finantialEntity, final Product product,
            final AcademicTariffBean bean) {
        return new AcademicTariff(finantialEntity, product, bean);
    }

    public static Stream<? extends AcademicTariff> findAll() {
        return Tariff.findAll().filter(t -> t instanceof AcademicTariff).map(AcademicTariff.class::cast);
    }

    public static Stream<? extends AcademicTariff> find(final Product product) {
        return product.getTariffSet().stream().filter(t -> t instanceof AcademicTariff).map(AcademicTariff.class::cast);
    }

    public static Stream<? extends AcademicTariff> find(final FinantialEntity finantialEntity) {
        return finantialEntity.getTariffSet().stream().filter(t -> t instanceof AcademicTariff).map(AcademicTariff.class::cast);
    }

    public static Stream<? extends AcademicTariff> find(final FinantialEntity finantialEntity, final Product product) {
        return find(product).filter(t -> t.getFinantialEntity() == finantialEntity);
    }

    private static Stream<? extends AcademicTariff> find(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice) {
        return find(finantialEntity, product).filter(i -> administrativeOffice == i.getAdministrativeOffice());
    }

    private static Stream<? extends AcademicTariff> find(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice,
            final DegreeType degreeType) {
        if (degreeType == null) {
            throw new RuntimeException("degree type is null. wrong find call");
        }

        return find(finantialEntity, product, administrativeOffice).filter(i -> degreeType == i.getDegreeType());
    }

    private static Stream<? extends AcademicTariff> find(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice,
            final DegreeType degreeType, final Degree degree) {
        if (degree == null) {
            throw new RuntimeException("degree is null. wrong find call");
        }

        return find(finantialEntity, product, administrativeOffice, degreeType).filter(t -> t.getDegree() == degree);
    }

    private static Stream<? extends AcademicTariff> find(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice,
            final DegreeType degreeType, final Degree degree, final CycleType cycleType) {
        if (cycleType == null) {
            throw new RuntimeException("cycle is null. wrong find call");
        }

        return find(finantialEntity, product, administrativeOffice, degreeType, degree).filter(t -> t.getCycleType() == cycleType);
    }

    public static Stream<? extends AcademicTariff> findActive(final DateTime when) {
        return findAll().filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findActive(final FinantialEntity finantialEntity, final DateTime when) {
        return find(finantialEntity).filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findActive(final FinantialEntity finantialEntity, final Product product,
            final DateTime when) {
        return find(finantialEntity, product).filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findActive(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice, final DateTime when) {
        return find(finantialEntity, product, administrativeOffice).filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findActive(final FinantialEntity finantialEntity, final Product product,
            final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final DateTime when) {
        return find(finantialEntity, product, administrativeOffice, degreeType).filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findActive(final FinantialEntity finantialEntity, final Product product,
            final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final Degree degree,
            final DateTime when) {
        return find(finantialEntity, product, administrativeOffice, degreeType, degree).filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findActive(final FinantialEntity finantialEntity, final Product product,
            final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final Degree degree,
            final CycleType cycleType, final DateTime when) {
        return find(finantialEntity, product, administrativeOffice, degreeType, degree, cycleType).filter(t -> t.isActive(when));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final Interval interval) {
        return findAll().filter(t -> t.isActive(interval));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final FinantialEntity finantialEntity,
            final Interval interval) {
        return find(finantialEntity).filter(t -> t.isActive(interval));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final FinantialEntity finantialEntity, final Product product,
            final Interval interval) {
        return find(finantialEntity, product).filter(t -> t.isActive(interval));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice, final Interval interval) {
        return find(finantialEntity, product, administrativeOffice).filter(t -> t.isActive(interval));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final Interval interval) {
        return find(finantialEntity, product, administrativeOffice, degreeType).filter(t -> t.isActive(interval));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final FinantialEntity finantialEntity, final Product product,
            final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final Degree degree,
            final Interval interval) {
        return find(finantialEntity, product, administrativeOffice, degreeType, degree).filter(t -> t.isActive(interval));
    }

    public static Stream<? extends AcademicTariff> findInInterval(final FinantialEntity finantialEntity, final Product product,
            final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final Degree degree,
            final CycleType cycleType, final Interval interval) {
        return find(finantialEntity, product, administrativeOffice, degreeType, degree, cycleType).filter(t -> t.isActive(interval));
    }

    protected static AcademicTariff findMatch(final FinantialEntity finantialEntity, final Product product, final DateTime when) {
        if (product == null) {
            throw new RuntimeException("product is null. wrong findMatch call");
        }

        if (finantialEntity == null) {
            throw new RuntimeException("finantial entity is null. wrong findMatch call");
        }

        // Fallback to product only in finantial entity
        final Set<? extends AcademicTariff> activeTariffs =
                findActive(finantialEntity, product, when)
                .filter(e -> e.getAdministrativeOffice() == null)
                .filter(e -> e.getDegreeType() == null)
                .filter(e -> e.getDegree() == null)
                .filter(e -> e.getCycleType() == null)
                .collect(Collectors.<AcademicTariff> toSet());

        if (activeTariffs.size() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.findActive.more.than.one");
        }

        if (!activeTariffs.isEmpty()) {
            return activeTariffs.iterator().next();
        }

        return null;
    }
    
    public static AcademicTariff findMatch(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice,
            final DateTime when) {
        if (administrativeOffice == null) {
            throw new RuntimeException("administrative office is null. wrong findMatch call");
        }
        
        // Fallback to administrativeOffice and return
        final Set<? extends AcademicTariff> activeTariffs =
                findActive(finantialEntity, product, administrativeOffice, when)
                .filter(e -> e.getDegreeType() == null)
                .filter(e -> e.getDegree() == null)
                .filter(e -> e.getCycleType() == null)
                .collect(Collectors.<AcademicTariff> toSet());

        if (activeTariffs.size() > 1) {
            throw new AcademicTreasuryDomainException("error.AcademicTariff.findActive.more.than.one");
        } else if(activeTariffs.size() == 1) {
            return activeTariffs.iterator().next();
        }

        return findMatch(finantialEntity, product, when);
    }
    
    protected static AcademicTariff findMatch(final FinantialEntity finantialEntity, final Product product, final AdministrativeOffice administrativeOffice, final DegreeType degreeType, final DateTime when) {
        if (administrativeOffice == null) {
            throw new RuntimeException("administrative office is null. wrong findMatch call");
        }
        
        if(degreeType == null) {
            throw new RuntimeException("degreeType is null. wrong findMatch call");
        }
        
        {
            // Fallback to degreeType
            Set<? extends AcademicTariff> activeTariffs =
                    findActive(finantialEntity, product, administrativeOffice, degreeType, when)
                    .filter(e -> e.getDegree() == null)
                    .filter(e -> e.getCycleType() == null)
                    .collect(Collectors.<AcademicTariff> toSet());

            if (activeTariffs.size() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.findActive.more.than.one");
            } else if (activeTariffs.size() == 1) {
                return activeTariffs.iterator().next();
            }
        }
        
        return findMatch(finantialEntity, product, administrativeOffice, when);
    }

    public static AcademicTariff findMatch(final FinantialEntity finantialEntity, final Product product, final Degree degree, final DateTime when) {
        if (degree == null) {
            throw new RuntimeException("degree is null. wrong findMatch call");
        }

        final AdministrativeOffice administrativeOffice = degree.getAdministrativeOffice();
        final DegreeType degreeType = degree.getDegreeType();

        {
            // With the most specific conditions tariff was not found. Fallback to degree
            Set<? extends AcademicTariff> activeTariffs = findActive(finantialEntity, product, administrativeOffice, degreeType, degree, when)
                    .filter(e -> e.getCycleType() == null)
                    .collect(Collectors.<AcademicTariff> toSet());
            if (activeTariffs.size() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.findActive.more.than.one");
            } else if (activeTariffs.size() == 1) {
                return activeTariffs.iterator().next();
            }
        }
        
        return findMatch(finantialEntity, product, administrativeOffice, degreeType, when);
    }

    public static AcademicTariff findMatch(final FinantialEntity finantialEntity, final Product product, final Degree degree, final CycleType cycleType,
            final DateTime when) {
        if (degree == null || cycleType == null) {
            throw new RuntimeException("degree or cycle type is null. wrong findMatch call");
        }

        final AdministrativeOffice administrativeOffice = degree.getAdministrativeOffice();
        final DegreeType degreeType = degree.getDegreeType();

        {
            Set<? extends AcademicTariff> activeTariffs =
                    findActive(finantialEntity, product, administrativeOffice, degreeType, degree, cycleType, when)
                            .collect(Collectors.<AcademicTariff> toSet());

            if (activeTariffs.size() > 1) {
                throw new AcademicTreasuryDomainException("error.AcademicTariff.findActive.more.than.one");
            } else if (activeTariffs.size() == 1) {
                return activeTariffs.iterator().next();
            }

        }

        return findMatch(finantialEntity, product, degree, when);
    }

}
