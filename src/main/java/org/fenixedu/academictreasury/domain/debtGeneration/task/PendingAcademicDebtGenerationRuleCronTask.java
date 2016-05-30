package org.fenixedu.academictreasury.domain.debtGeneration.task;

import java.util.Set;

import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Create academic debts", readOnly = true)
public class PendingAcademicDebtGenerationRuleCronTask extends CronTask {

    private static final int MAX_REGISTRATIONS = 100;

    @Override
    public void runTask() {
        
        final Set<String> pendingRegistrations = TuitionServices.dequeueAllRegistrationsForAcademicDebtRuleExecution();
        
        for (String registrationId : pendingRegistrations) {
            final Thread taskExecutor = new TaskExecutor(registrationId);
            
            try {
                taskExecutor.start();
                taskExecutor.join();
            } catch(InterruptedException e) {
            }
        }
    }
    
    private static class TaskExecutor extends Thread {

        private String registrationId;
        
        public TaskExecutor(final String registrationId) {
            this.registrationId = registrationId;
        }
        
        @Override
        @Atomic(mode=TxMode.READ)
        public void run() {
            AcademicDebtGenerationRule.runAllActiveForRegistration(FenixFramework.getDomainObject(registrationId), true);
        }
    }
}
