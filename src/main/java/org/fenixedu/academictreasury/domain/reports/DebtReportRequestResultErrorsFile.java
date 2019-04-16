package org.fenixedu.academictreasury.domain.reports;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;

public class DebtReportRequestResultErrorsFile extends DebtReportRequestResultErrorsFile_Base {

    protected DebtReportRequestResultErrorsFile(final DebtReportRequest request, final byte[] content) {
        super();

        setBennu(Bennu.getInstance());
        setDebtReportRequest(request);

        final String filename =
                academicTreasuryBundle("label.DebtReportRequestResultErrorsFile.filename", new DateTime().toString("YYYYMMddHHmmss"));

        init(filename, filename, content);
        checkRules();
        
        DebtReportRequestResultErrorsFileDomainObject.createFromDebtReportRequestResultErrorsFile(this);
    }

    private void checkRules() {
        if (getDebtReportRequest() == null) {
            throw new AcademicTreasuryDomainException("error.DebtReportRequestResultErrorsFile.debtReportRequest.required");
        }
    }

    @Override
    public boolean isAccessible(final User user) {
        return isAccessible(user.getUsername());
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
    
    public static DebtReportRequestResultErrorsFile create(final DebtReportRequest request, final byte[] content) {
        return new DebtReportRequestResultErrorsFile(request, content);
    }
    
    public static Stream<DebtReportRequestResultErrorsFile> findAll() {
        return Bennu.getInstance().getDebtReportRequestResultErrorsFilesSet().stream();
    }
    
}
