package org.fenixedu.academictreasury.domain.importation;

import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import pt.ist.fenixframework.FenixFramework;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;

import pt.ist.fenixframework.Atomic;

public class TreasuryImportFile extends TreasuryImportFile_Base {

    protected TreasuryImportFile() {
        super();

        setBennu(Bennu.getInstance());
    }

    protected TreasuryImportFile(final TreasuryImportType type, final String filename, final byte[] content) {
        this();
        init(filename, filename, content);

        setTreasuryImportType(type);
        checkRules();
        
        TreasuryImportFileDomainObject.createFromTreasuryImportFile(this);
    }

    private void checkRules() {
        if(getTreasuryImportType() == null) {
            throw new AcademicTreasuryDomainException("error.TreasuryImportFile.type.required");
        }
    }

    @Override
    public boolean isAccessible(final User user) {
        return isAccessible(user.getUsername());
    }
    
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }

    public boolean isProcessed() {
        return getWhenProcessed() != null;
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<TreasuryImportFile> findAll() {
        return Bennu.getInstance().getTreasuryImportFilesSet().stream();
    }

    @Atomic
    public static TreasuryImportFile create(final TreasuryImportType type, final String filename, final byte[] content) {
        return new TreasuryImportFile(type, filename, content);
    }

}
