package org.fenixedu.academictreasury.services.signals;

import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequest;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.services.EmolumentServices;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;

import com.google.common.eventbus.Subscribe;

public class AcademicServiceRequestCancelOrRejectHandler {

    @Subscribe
    public void academicServiceRequestCancelOrRejectHandler(final DomainObjectEvent<AcademicServiceRequest> event) {
        EmolumentServices.removeDebitEntryForAcademicService((ITreasuryServiceRequest) event.getInstance());
    }
}
