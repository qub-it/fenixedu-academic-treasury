package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import org.fenixedu.academictreasury.domain.integration.ERPTuitionInfoExportOperation;

public interface IERPTuitionInfoExporter {
    
    public ERPTuitionInfoExportOperation export(final ERPTuitionInfo erpTuitionInfo);
}
