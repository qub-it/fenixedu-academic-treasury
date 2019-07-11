package org.fenixedu.academictreasury.domain.treasury;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.treasury.domain.event.TreasuryEvent;

public interface IAcademicTreasuryAccountUrl {

    public String getPersonAccountTreasuryManagementURL(final Person person);
    
    public String getRegistrationAccountTreasuryManagementURL(final Registration registration);
    
    public String getDebtAccountURL(final TreasuryEvent treasuryEvent);

}
