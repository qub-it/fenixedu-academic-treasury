package org.fenixedu.academictreasury.schoolsbootstrapscript;

import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.settings.TreasurySettings;

public class BlockAcademicalActsInCaseOfDebtForAllProducts extends CustomTask {

    @Override
    public void runTask() throws Exception {
        
        Product.findAll().forEach(p -> AcademicTreasurySettings.getInstance().removeAcademicalActBlockingProduct(p));
    }
    
}
