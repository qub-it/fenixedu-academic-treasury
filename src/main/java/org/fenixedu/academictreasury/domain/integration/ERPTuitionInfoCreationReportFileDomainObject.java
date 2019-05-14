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

public class ERPTuitionInfoCreationReportFileDomainObject extends ERPTuitionInfoCreationReportFileDomainObject_Base
        implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";

    public ERPTuitionInfoCreationReportFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

//    public ERPTuitionInfoCreationReportFileDomainObject(final String displayName, final String filename, final byte[] content) {
//        this();
//
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//
//        services.createFile(this, filename, CONTENT_TYPE, content);
//    }

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

//    @Atomic
//    public static final ERPTuitionInfoCreationReportFileDomainObject create(final String displayName, final String filename,
//            final byte[] content) {
//        return new ERPTuitionInfoCreationReportFileDomainObject(displayName, filename, content);
//    }
    
    public static final ERPTuitionInfoCreationReportFileDomainObject createFromERPTuitionInfoCreationReportFile(final ERPTuitionInfoCreationReportFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final ERPTuitionInfoCreationReportFileDomainObject result = new ERPTuitionInfoCreationReportFileDomainObject();
        
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());
        result.setTreasuryFile(file);
        
        return result;
    }
    
    public static Stream<ERPTuitionInfoCreationReportFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getErpTuitionInfoCreationReportFileDomainObjectsSet().stream();
    }
    
    public static Optional<ERPTuitionInfoCreationReportFileDomainObject> findUniqueByERPTuitionInfoCreationReportFile(final ERPTuitionInfoCreationReportFile file) {
        return findAll().filter(f -> f.getTreasuryFile() == file).findFirst();
    }
    
}
