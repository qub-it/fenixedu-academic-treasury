package org.fenixedu.academictreasury.domain.importation;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class TreasuryImportFileDomainObject extends TreasuryImportFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";
    
    protected TreasuryImportFileDomainObject() {
        super();

        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

//    protected TreasuryImportFileDomainObject(final TreasuryImportType type, final String filename, final byte[] content) {
//        this();
//        
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        services.createFile(this, filename, CONTENT_TYPE, content);
//
//        setTreasuryImportType(type);
//        checkRules();
//    }

    private void checkRules() {
        if(getTreasuryImportType() == null) {
            throw new AcademicTreasuryDomainException("error.TreasuryImportFile.type.required");
        }
    }

    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    public boolean isProcessed() {
        return getWhenProcessed() != null;
    }
    
    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        setDomainRoot(null);
        setTreasuryImportType(null);
        
        services.deleteFile(this);
        
        super.deleteDomainObject();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<TreasuryImportFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getTreasuryImportFileDomainObjectsSet().stream();
    }
    
    public static Optional<TreasuryImportFileDomainObject> findUniqueByTreasuryImportFile(final TreasuryImportFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }

//    @Atomic
//    public static TreasuryImportFileDomainObject create(final TreasuryImportType type, final String filename, final byte[] content) {
//        return new TreasuryImportFileDomainObject(type, filename, content);
//    }

    public static TreasuryImportFileDomainObject createFromTreasuryImportFile(final TreasuryImportFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        final TreasuryImportFileDomainObject result = new TreasuryImportFileDomainObject();
        
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());
        result.setTreasuryFile(file);
        result.setTreasuryImportType(file.getTreasuryImportType());
        result.setWhenProcessed(file.getWhenProcessed());
        
        return result;
    }
    
}
