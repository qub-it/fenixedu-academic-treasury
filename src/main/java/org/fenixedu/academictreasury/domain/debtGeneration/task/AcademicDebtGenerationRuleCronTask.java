package org.fenixedu.academictreasury.domain.debtGeneration.task;

import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;

@Task(englishTitle = "Create academic debts for pending registrations", readOnly = true)
public class AcademicDebtGenerationRuleCronTask extends CronTask {

    @Override
    public void runTask() throws Exception {
        
        AcademicDebtGenerationRule.runAllActive(true);
    }
}
