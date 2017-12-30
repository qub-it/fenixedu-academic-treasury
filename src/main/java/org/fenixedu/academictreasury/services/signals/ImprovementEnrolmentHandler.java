package org.fenixedu.academictreasury.services.signals;

import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;

import com.google.common.eventbus.Subscribe;

public class ImprovementEnrolmentHandler {

    @Subscribe
    public void improvementEnrolment(final DomainObjectEvent<EnrolmentEvaluation> event) {
        AcademicTaxServices.createImprovementTax(event.getInstance(), event.getInstance().getWhenDateTime().toLocalDate());
    }


}
