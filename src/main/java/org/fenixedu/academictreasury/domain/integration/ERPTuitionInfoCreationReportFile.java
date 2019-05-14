package org.fenixedu.academictreasury.domain.integration;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;

import pt.ist.fenixframework.Atomic;

public class ERPTuitionInfoCreationReportFile extends ERPTuitionInfoCreationReportFile_Base {
    
    public ERPTuitionInfoCreationReportFile() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    public ERPTuitionInfoCreationReportFile(final String displayName, final String filename, final byte[] content) {
        this();
        
        init(displayName, filename, content);
        
        ERPTuitionInfoCreationReportFileDomainObject.createFromERPTuitionInfoCreationReportFile(this);
    }

    @Override
    public boolean isAccessible(User arg0) {
        return isAccessible(arg0.getUsername());
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
    
    @Atomic
    public static final ERPTuitionInfoCreationReportFile create(final String displayName, final String filename, final byte[] content) {
        return new ERPTuitionInfoCreationReportFile(displayName, filename, content);
    }

    public static Stream<ERPTuitionInfoCreationReportFile> findAll() {
        return Bennu.getInstance().getErpTuitionInfoCreationReportFilesSet().stream();
    }
    
}
