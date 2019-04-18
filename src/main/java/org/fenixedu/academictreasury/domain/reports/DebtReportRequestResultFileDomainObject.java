package org.fenixedu.academictreasury.domain.reports;


import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class DebtReportRequestResultFileDomainObject extends DebtReportRequestResultFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "application/octet-stream";
    
    
    protected DebtReportRequestResultFileDomainObject() {
        super();
        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
//    protected DebtReportRequestResultFileDomainObject(final DebtReportRequest request, final byte[] content) {
//        this();
//
//        setDebtReportRequest(request);
//
//        final String filename = academicTreasuryBundle("label.DebtReportRequestResultFile.ZIP.filename", 
//                new DateTime().toString("YYYYMMddHHmmss"));
//
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        
//        services.createFile(this, filename, CONTENT_TYPE, content);
//
//        checkRules();
//    }

    private void checkRules() {
        if(getDebtReportRequest() == null) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequestResultFile.debtReportRequest.required");
        }
    }
    
    public BigDecimal getFilesizeMb() {
        return (new BigDecimal(getSize()).setScale(4, RoundingMode.DOWN)
                .divide(new BigDecimal(1024)))
                .divide(new BigDecimal(1024))
                .setScale(1, RoundingMode.DOWN);
    }

    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }
    
    @Override
    public void delete() {
        
        setDomainRoot(null);
        
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        services.deleteFile(this);
        
        super.deleteDomainObject();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    
//    public static DebtReportRequestResultFileDomainObject create(final DebtReportRequest request, final byte[] content) {
//        return new DebtReportRequestResultFileDomainObject(request, content);
//    }
    

    public static DebtReportRequestResultFileDomainObject createFromDebtReportRequestResultFile(final DebtReportRequestResultFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        final DebtReportRequestResultFileDomainObject result = new DebtReportRequestResultFileDomainObject();
        
        result.setTreasuryFile(file);
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setFileId(file.getExternalId());
        
        return result;
    }
    
    public static Stream<DebtReportRequestResultFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getDebtReportRequestResultFileDomainObjectsSet().stream();
    }
    
    public static Optional<DebtReportRequestResultFileDomainObject> findUniqueByDebtReportRequestResultFile(DebtReportRequestResultFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }
    
}
