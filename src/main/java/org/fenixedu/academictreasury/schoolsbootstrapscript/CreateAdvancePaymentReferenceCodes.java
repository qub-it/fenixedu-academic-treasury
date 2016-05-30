package org.fenixedu.academictreasury.schoolsbootstrapscript;

import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;

public class CreateAdvancePaymentReferenceCodes extends CustomTask {

    @Override
    public void runTask() throws Exception {
        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().findFirst().get();
        final PaymentCodePool paymentCodePool = finantialInstitution.getPaymentCodePoolsSet().iterator().next();

        for(int i = 0; i < 50000; i++) {
            paymentCodePool.getReferenceCodeGenerator().generateNewCodeFor(paymentCodePool.getMaxAmount(),
                    paymentCodePool.getValidFrom(), paymentCodePool.getValidTo(), false, true);
        }
        
    }

}
