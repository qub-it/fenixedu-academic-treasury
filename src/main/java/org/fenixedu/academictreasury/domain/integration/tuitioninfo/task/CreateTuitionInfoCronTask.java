package org.fenixedu.academictreasury.domain.integration.tuitioninfo.task;

import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfo;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoSettings;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;

@Task(englishTitle = "Create tuition information for ERP", readOnly = true)
public class CreateTuitionInfoCronTask extends CronTask {

    @Override
    public void runTask() throws Exception {
        if(!ERPTuitionInfoSettings.getInstance().isExportationActive()) {
            taskLog("Tuition info exportation is disabled.");
            return;
        }
        
        ERPTuitionInfo.triggerTuitionInfoCalculation(null, null);
    }

}
