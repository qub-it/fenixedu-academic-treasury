package org.fenixedu.academictreasury.services.signals;

import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.services.AcademicTreasuryPlataformDependentServicesFactory;
import org.fenixedu.academictreasury.services.IAcademicTreasuryPlatformDependentServices;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.joda.time.LocalDate;

import com.google.common.eventbus.Subscribe;

public class ImprovementEnrolmentHandler {

    @Subscribe
    public void improvementEnrolment(final DomainObjectEvent<EnrolmentEvaluation> event) {
        final IAcademicTreasuryPlatformDependentServices academicTreasuryServices = AcademicTreasuryPlataformDependentServicesFactory.implementation();
        
        final EnrolmentEvaluation enrolmentEvaluation = event.getInstance();
        final LocalDate when = enrolmentEvaluation.getWhenDateTime().toLocalDate();
        final FinantialEntity finantialEntity = academicTreasuryServices.finantialEntityOfDegree(enrolmentEvaluation.getDegreeCurricularPlan().getDegree(), when);
        
        AcademicTaxServices.createImprovementTax(finantialEntity, enrolmentEvaluation, when);
    }


}
