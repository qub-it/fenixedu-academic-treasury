package org.fenixedu.academictreasury;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.treasury.ITreasuryBridgeAPI;
import org.fenixedu.academic.domain.treasury.TreasuryBridgeAPIFactory;
import org.fenixedu.academictreasury.domain.listeners.DebitEntryDeletionListener;
import org.fenixedu.academictreasury.domain.listeners.ProductDeletionListener;
import org.fenixedu.academictreasury.domain.treasury.AcademicTreasuryBridgeImpl;
import org.fenixedu.academictreasury.services.EmolumentServices;
import org.fenixedu.academictreasury.services.RegistrationServices;
import org.fenixedu.academictreasury.services.accesscontrol.spi.AcademicTreasuryAccessControlExtension;
import org.fenixedu.academictreasury.services.signals.AcademicServiceRequestCancelOrRejectHandler;
import org.fenixedu.academictreasury.services.signals.ExtracurricularEnrolmentHandler;
import org.fenixedu.academictreasury.services.signals.ImprovementEnrolmentHandler;
import org.fenixedu.academictreasury.services.signals.StandaloneEnrolmentHandler;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.treasury.domain.bennu.signals.BennuSignalsServices;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;

@WebListener
public class AcademicTreasuryInitializer implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        registerNewRegistrationHandler();
        registerNewAcademicServiceRequestSituationHandler();
        registerAcademicServiceRequestCancelOrRejectHandler();
        registerStandaloneEnrolmentHandler();
        registerExtracurricularEnrolmentHandler();
        registerImprovementEnrolmentHandler();
        
        TreasuryAccessControlAPI.registerExtension(new AcademicTreasuryAccessControlExtension());
        
        DebitEntryDeletionListener.attach();
        ProductDeletionListener.attach();

        final AcademicTreasuryBridgeImpl impl = new AcademicTreasuryBridgeImpl();

        TreasuryBridgeAPIFactory.registerImplementation(impl);
        BennuSignalsServices.registerSettlementEventHandler(impl);
    }
    
    private static void registerNewRegistrationHandler() {
        Signal.register(Registration.REGISTRATION_CREATE_SIGNAL, new RegistrationServices());
    }
    
    private static void registerNewAcademicServiceRequestSituationHandler() {
        Signal.register(ITreasuryBridgeAPI.ACADEMIC_SERVICE_REQUEST_NEW_SITUATION_EVENT, new EmolumentServices());
    }
    
    private static void registerAcademicServiceRequestCancelOrRejectHandler() {
        Signal.register(ITreasuryBridgeAPI.ACADEMIC_SERVICE_REQUEST_REJECT_OR_CANCEL_EVENT, new AcademicServiceRequestCancelOrRejectHandler());
    }
    
    private static void registerStandaloneEnrolmentHandler() {
        Signal.register(ITreasuryBridgeAPI.STANDALONE_ENROLMENT, new StandaloneEnrolmentHandler());
    }

    private static void registerExtracurricularEnrolmentHandler() {
        Signal.register(ITreasuryBridgeAPI.EXTRACURRICULAR_ENROLMENT, new ExtracurricularEnrolmentHandler());
    }

    private static void registerImprovementEnrolmentHandler() {
        Signal.register(ITreasuryBridgeAPI.IMPROVEMENT_ENROLMENT, new ImprovementEnrolmentHandler());
    }

}
