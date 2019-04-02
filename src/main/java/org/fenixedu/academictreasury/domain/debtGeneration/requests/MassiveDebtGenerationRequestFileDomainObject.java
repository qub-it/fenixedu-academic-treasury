package org.fenixedu.academictreasury.domain.debtGeneration.requests;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

/**
 * This class applies not only for debt generation but also for other
 * operations with debts
 * 
 * @author anilmamede
 *
 */
public class MassiveDebtGenerationRequestFileDomainObject extends MassiveDebtGenerationRequestFileDomainObject_Base
        implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    public static final Comparator<MassiveDebtGenerationRequestFileDomainObject> COMPARE_BY_CREATION_DATE = (o1, o2) -> {

        int c = o1.getCreationDate().compareTo(o2.getCreationDate());

        return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
    };

    protected MassiveDebtGenerationRequestFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

//    protected MassiveDebtGenerationRequestFileDomainObject(final MassiveDebtGenerationRequestFileBean bean, final String filename,
//            final byte[] content) {
//        this();
//
//
//        final MassiveDebtGenerationType type = bean.getMassiveDebtGenerationType();
//        final TuitionPaymentPlanGroup tuitionPaymentPlanGroup = bean.getTuitionPaymentPlanGroup();
//        final AcademicTax academicTax = bean.getAcademicTax();
//        final ExecutionYear executionYear = bean.getExecutionYear();
//        final LocalDate debtDate = bean.getDebtDate();
//        final String reason = bean.getReason();
//        
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        services.createFile(this, filename, CONTENT_TYPE, content);
//
//        setMassiveDebtGenerationType(type);
//        setTuitionPaymentPlanGroup(tuitionPaymentPlanGroup);
//        setAcademicTax(academicTax);
//        setExecutionYear(executionYear);
//        setDebtDate(debtDate);
//        setReason(reason);
//        setFinantialInstitution(bean.getFinantialInstitution());
//
//        checkRules();
//    }

    private void checkRules() {
        if (getDomainRoot() == null) {
            throw new AcademicTreasuryDomainException("error.MassiveDebtGenerationRequestFile.bennu.required");
        }

        if (getMassiveDebtGenerationType() == null) {
            throw new AcademicTreasuryDomainException(
                    "error.MassiveDebtGenerationRequestFile.massiveDebtGenerationType.required");
        }

        getMassiveDebtGenerationType().implementation().checkRules((MassiveDebtGenerationRequestFile) this.getTreasuryFile());
    }

    public String getDataDescription() {
        return getMassiveDebtGenerationType().implementation().dataDescription((MassiveDebtGenerationRequestFile) this.getTreasuryFile());
    }

    @Atomic
    public void process() {
        getMassiveDebtGenerationType().implementation().process((MassiveDebtGenerationRequestFile) this.getTreasuryFile());
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

    public static Stream<MassiveDebtGenerationRequestFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getMassiveDebtGenerationRequestFileDomainObjectsSet().stream();
    }

    public static Stream<MassiveDebtGenerationRequestFileDomainObject> findAllActive() {
        return findAll().filter(m -> m.getMassiveDebtGenerationType().isActive());
    }

    public static Optional<MassiveDebtGenerationRequestFileDomainObject> findUniqueByMassiveDebtGenerationRequestFile(
            final MassiveDebtGenerationRequestFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

//    @Atomic
//    public static MassiveDebtGenerationRequestFileDomainObject create(final MassiveDebtGenerationRequestFileBean bean, final String filename,
//            final byte[] content) {
//        return new MassiveDebtGenerationRequestFileDomainObject(bean, filename, content);
//    }

    public static MassiveDebtGenerationRequestFileDomainObject createFromMassiveDebtGenerationRequestFile(
            final MassiveDebtGenerationRequestFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final MassiveDebtGenerationRequestFileDomainObject result = new MassiveDebtGenerationRequestFileDomainObject();

        result.setMassiveDebtGenerationType(file.getMassiveDebtGenerationType());
        result.setTuitionPaymentPlanGroup(file.getTuitionPaymentPlanGroup());
        result.setAcademicTax(file.getAcademicTax());
        result.setExecutionYear(file.getExecutionYear());
        result.setDebtDate(file.getDebtDate());
        result.setReason(file.getReason());
        result.setFinantialInstitution(file.getFinantialInstitution());

        result.setTreasuryFile(file);
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());

        return result;
    }

}
