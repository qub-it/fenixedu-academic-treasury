package org.fenixedu.academictreasury.domain.reports;


import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.io.domain.IGenericFile;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.services.integration.ITreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class DebtReportRequestResultErrorsFileDomainObject extends DebtReportRequestResultErrorsFileDomainObject_Base implements IGenericFile {

    public static final String CONTENT_TYPE = "text/plain";

    public DebtReportRequestResultErrorsFileDomainObject() {
        super();

        setDomainRoot(FenixFramework.getDomainRoot());
    }
    
//    protected DebtReportRequestResultErrorsFileDomainObject(final DebtReportRequest request, final byte[] content) {
//        this();
//
//        setDebtReportRequest(request);
//
//        final String filename =
//                academicTreasuryBundle("label.DebtReportRequestResultErrorsFile.filename", new DateTime().toString("YYYYMMddHHmmss"));
//
//        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
//        
//        services.createFile(this, filename, CONTENT_TYPE, content);
//        
//        checkRules();
//    }

    private void checkRules() {
        if (getDebtReportRequest() == null) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequestResultErrorsFile.debtReportRequest.required");
        }
    }
    
    @Override
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }
    
    @Override
    public void delete() {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();
        
        setDomainRoot(null);
        setDebtReportRequest(null);
        
        services.deleteFile(this);
        
        super.deleteDomainObject();
    }

//    public static DebtReportRequestResultErrorsFileDomainObject create(final DebtReportRequest request, final byte[] content) {
//        return new DebtReportRequestResultErrorsFileDomainObject(request, content);
//    }
    
    public static DebtReportRequestResultErrorsFileDomainObject createFromDebtReportRequestResultErrorsFile(final DebtReportRequestResultErrorsFile file) {
        final ITreasuryPlatformDependentServices services = TreasuryPlataformDependentServicesFactory.implementation();

        DebtReportRequestResultErrorsFileDomainObject result = new DebtReportRequestResultErrorsFileDomainObject();
        
        result.setDebtReportRequest(file.getDebtReportRequest());
        result.setCreationDate(file.getCreationDate());
        result.setCreator(services.versioningCreatorUsername(file));
        result.setTreasuryFile(file);
        result.setFileId(file.getExternalId());
        
        result.checkRules();
        
        return result;
    }
    
    public static Stream<DebtReportRequestResultErrorsFileDomainObject> findAll() {
        return FenixFramework.getDomainRoot().getDebtReportRequestResultErrorsFileDomainObjectsSet().stream();
    }

    public static Optional<DebtReportRequestResultErrorsFileDomainObject> findUniqueByDebtReportRequestResultErrorsFile(final DebtReportRequestResultErrorsFile file) {
        return findAll().filter(o -> o.getTreasuryFile() == file).findFirst();
    }
    
}
