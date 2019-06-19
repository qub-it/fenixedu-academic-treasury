package org.fenixedu.academictreasury.domain.importation;

import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class TreasuryImportFile extends TreasuryImportFile_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";
    
    protected TreasuryImportFile() {
        super();

        setDomainRoot(FenixFramework.getDomainRoot());
        setCreationDate(new DateTime());
    }

    protected TreasuryImportFile(final TreasuryImportType type, final String filename, final byte[] content) {
        this();
        
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        services.createFile(this, filename, CONTENT_TYPE, content);

        setTreasuryImportType(type);
        checkRules();
    }

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

    public static Stream<TreasuryImportFile> findAll() {
        return FenixFramework.getDomainRoot().getTreasuryImportFilesSet().stream();
    }
    
    @Atomic
    public static TreasuryImportFile create(final TreasuryImportType type, final String filename, final byte[] content) {
        return new TreasuryImportFile(type, filename, content);
    }

}
