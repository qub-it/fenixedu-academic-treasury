package org.fenixedu.academictreasury.domain.debtGeneration.requests;

import java.util.Comparator;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

/**
 * This class applies not only for debt generation but also for other
 * operations with debts
 * 
 * @author anilmamede
 *
 */
public class MassiveDebtGenerationRequestFile extends MassiveDebtGenerationRequestFile_Base
        implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    public static final Comparator<MassiveDebtGenerationRequestFile> COMPARE_BY_CREATION_DATE = (o1, o2) -> {

        int c = o1.getCreationDate().compareTo(o2.getCreationDate());

        return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
    };

    protected MassiveDebtGenerationRequestFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
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
        
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        services.createFile(this, filename, CONTENT_TYPE, content);

        setMassiveDebtGenerationType(type);
        setTuitionPaymentPlanGroup(tuitionPaymentPlanGroup);
        setAcademicTax(academicTax);
        setExecutionYear(executionYear);
        setDebtDate(debtDate);
        setReason(reason);
        setFinantialInstitution(bean.getFinantialInstitution());

        checkRules();
    }

    private void checkRules() {
        if (getDomainRoot() == null) {
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
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        setDomainRoot(null);
        setMassiveDebtGenerationType(null);
        setTuitionPaymentPlanGroup(null);
        setAcademicTax(null);
        setExecutionYear(null);
        setFinantialInstitution(null);

        services.deleteFile(this);

        super.deleteDomainObject();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<MassiveDebtGenerationRequestFile> findAll() {
        return FenixFramework.getDomainRoot().getMassiveDebtGenerationRequestFilesSet().stream();
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
