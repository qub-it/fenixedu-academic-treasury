package org.fenixedu.academictreasury.services.signals;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;

import com.google.common.eventbus.Subscribe;

public class StandaloneEnrolmentHandler {

    @Subscribe
    public void standaloneEnrolment(final DomainObjectEvent<Enrolment> event) {
        TuitionServices.createInferedTuitionForStandalone(event.getInstance(), event.getInstance().getCreationDateDateTime()
                .toLocalDate(), false);
    }
    
}
