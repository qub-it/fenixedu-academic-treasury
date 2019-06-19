package org.fenixedu.academictreasury.domain.integration;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class ERPTuitionInfoCreationReportFile extends ERPTuitionInfoCreationReportFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    public ERPTuitionInfoCreationReportFile() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

    public ERPTuitionInfoCreationReportFile(final String displayName, final String filename, final byte[] content) {
        this();

        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        services.createFile(this, filename, CONTENT_TYPE, content);
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        setDomainRoot(null);

        services.deleteFile(this);

        super.deleteDomainObject();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    @Atomic
    public static final ERPTuitionInfoCreationReportFile create(final String displayName, final String filename,
            final byte[] content) {
        return new ERPTuitionInfoCreationReportFile(displayName, filename, content);
    }

    public static Stream<ERPTuitionInfoCreationReportFile> findAll() {
        return FenixFramework.getDomainRoot().getErpTuitionInfoCreationReportFilesSet().stream();
    }

}
