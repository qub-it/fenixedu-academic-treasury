package org.fenixedu.academictreasury.domain.integration.tuitioninfo.task;

import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfo;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoSettings;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;

@Task(englishTitle = "Export tuition information to ERP", readOnly = true)
public class ExportTuitionInfoToERPCronTask extends CronTask {

    @Override
    public void runTask() throws Exception {
        if(!ERPTuitionInfoSettings.getInstance().isExportationActive()) {
            return;
        }
        
        ERPTuitionInfo.triggerTuitionExportationToERP(null);
    }
    
}
