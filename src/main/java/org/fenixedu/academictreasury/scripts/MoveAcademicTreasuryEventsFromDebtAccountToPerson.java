package org.fenixedu.academictreasury.scripts;

import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.treasury.domain.debt.DebtAccount;

public class MoveAcademicTreasuryEventsFromDebtAccountToPerson extends CustomTask {

    @Override
    public void runTask() throws Exception {
        
        int count = 0;
        final long totalCount = AcademicTreasuryEvent.findAll().count();
        for (AcademicTreasuryEvent academicTreasuryEvent : AcademicTreasuryEvent.findAll().collect(Collectors.<AcademicTreasuryEvent> toList())) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " parties.");
            }

            DebtAccount debtAccount = academicTreasuryEvent.getDebtAccount();
            
            if(debtAccount != null) {
                final Person person = ((PersonCustomer) debtAccount.getCustomer()).getPerson();
                academicTreasuryEvent.setPerson(person);
            }
        }
        
    }

}
