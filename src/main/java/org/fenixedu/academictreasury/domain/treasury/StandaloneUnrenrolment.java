package org.fenixedu.academictreasury.domain.treasury;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;

import com.google.common.eventbus.Subscribe;

public class StandaloneUnrenrolment {

    @Subscribe
    public void standaloneUnenrolment(final DomainObjectEvent<Enrolment> event) {
        TuitionServices.removeDebitEntryForStandaloneEnrolment(event.getInstance());
    }

}
