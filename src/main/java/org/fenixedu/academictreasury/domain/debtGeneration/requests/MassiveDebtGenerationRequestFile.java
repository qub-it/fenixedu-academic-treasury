package org.fenixedu.academictreasury.domain.debtGeneration.requests;

import java.util.Comparator;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

/**
 * This class applies not only for debt generation but also for other
 * operations with debts
 * 
 * @author anilmamede
 *
 */
public class MassiveDebtGenerationRequestFile extends MassiveDebtGenerationRequestFile_Base {

    public static final Comparator<MassiveDebtGenerationRequestFile> COMPARE_BY_CREATION_DATE =
            new Comparator<MassiveDebtGenerationRequestFile>() {

                @Override
                public int compare(final MassiveDebtGenerationRequestFile o1, final MassiveDebtGenerationRequestFile o2) {
                    int c = TreasuryPlataformDependentServicesFactory.implementation().versioningCreationDate(o1).compareTo(TreasuryPlataformDependentServicesFactory.implementation().versioningCreationDate(o2));

                    return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
                }
            };

    protected MassiveDebtGenerationRequestFile() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected MassiveDebtGenerationRequestFile(final MassiveDebtGenerationRequestFileBean bean, final String filename,
            final byte[] content) {
        this();

        final MassiveDebtGenerationType type = bean.getMassiveDebtGenerationType();
        final TuitionPaymentPlanGroup tuitionPaymentPlanGroup = bean.getTuitionPaymentPlanGroup();
        final AcademicTax academicTax = bean.getAcademicTax();
        final ExecutionYear executionYear = bean.getExecutionYear();
        final LocalDate debtDate = bean.getDebtDate();
        final String reason = bean.getReason();
        
        init(filename, filename, content);

        setMassiveDebtGenerationType(type);
        setTuitionPaymentPlanGroup(tuitionPaymentPlanGroup);
        setAcademicTax(academicTax);
        setExecutionYear(executionYear);
        setDebtDate(debtDate);
        setReason(reason);
        setFinantialInstitution(bean.getFinantialInstitution());

        checkRules();
        
        MassiveDebtGenerationRequestFileDomainObject.createFromMassiveDebtGenerationRequestFile(this);
    }

    private void checkRules() {
        if (getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.MassiveDebtGenerationRequestFile.bennu.required");
        }

        if (getMassiveDebtGenerationType() == null) {
            throw new AcademicTreasuryDomainException(
                    "error.MassiveDebtGenerationRequestFile.massiveDebtGenerationType.required");
        }

        getMassiveDebtGenerationType().implementation().checkRules(this);
    }
    
    public String getDataDescription() {
        return getMassiveDebtGenerationType().implementation().dataDescription(this);
    }

    @Atomic
    public void process() {
        getMassiveDebtGenerationType().implementation().process(this);
    }

    @Override
    public boolean isAccessible(final User user) {
        return isAccessible(user.getUsername());
    }
    
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<MassiveDebtGenerationRequestFile> findAll() {
        return Bennu.getInstance().getMassiveDebtGenerationRequestFilesSet().stream();
    }
    
    public static Stream<MassiveDebtGenerationRequestFile> findAllActive() {
        return findAll().filter(m -> m.getMassiveDebtGenerationType().isActive());
    }

    @Atomic
    public static MassiveDebtGenerationRequestFile create(final MassiveDebtGenerationRequestFileBean bean, final String filename,
            final byte[] content) {
        return new MassiveDebtGenerationRequestFile(bean, filename, content);
    }

}
