package org.fenixedu.academictreasury.domain.reports;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import pt.ist.fenixframework.FenixFramework;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.joda.time.DateTime;

public class DebtReportRequestResultFile extends DebtReportRequestResultFile_Base {

    protected DebtReportRequestResultFile(final DebtReportRequest request, final byte[] content) {
        super();

        setBennu(Bennu.getInstance());
        setDebtReportRequest(request);

        final String filename = academicTreasuryBundle("label.DebtReportRequestResultFile.ZIP.filename", 
                new DateTime().toString("YYYYMMddHHmmss"));

        init(filename, filename, content);
        checkRules();
        
        DebtReportRequestResultFileDomainObject.createFromDebtReportRequestResultFile(this);
    }

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

    @Override
    public boolean isAccessible(final User user) {
        return isAccessible(user.getUsername());
    }
    
    public boolean isAccessible(final String username) {
        return TreasuryAccessControlAPI.isBackOfficeMember(username);
    }
    
    public static Stream<DebtReportRequestResultFile> findAll() {
        return Bennu.getInstance().getDebtReportRequestResultFilesSet().stream();
    }

    public static DebtReportRequestResultFile create(final DebtReportRequest request, final byte[] content) {
        return new DebtReportRequestResultFile(request, content);
    }
}
