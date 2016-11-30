package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(ERPTuitionInfoTypeController.CONTROLLER_URL)
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.ERPTuitionInfoType.title", accessGroup = "#managers")
public class ERPTuitionInfoTypeController extends AcademicTreasuryBaseController {
    
    public static final String CONTROLLER_URL = "/academictreasury/erptuitioninfotype";
    public static final String JSP_PATH = "academicTreasury/erptuitioninfotype";
    
    @RequestMapping
    public String home() {
        return "redirect:" + SEARCH_URL;
    }
    
    private static final String _SEARCH_URI = "";
    public static final String SEARCH_URL  = CONTROLLER_URL + _SEARCH_URI;
    
    @RequestMapping(value=_SEARCH_URI, method=RequestMethod.GET)
    public String search(final Model model) {
        return jspPage(_SEARCH_URI);
    }
    
    private static final String _CREATE_URI = "";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    public String create(final Model model) {
        return null;
    }
    
    private static final String _UPDATE_URI = "";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;
    
    private static final String _DELETE_URI = "";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;
    
    private String jspPage(final String page) {
        return JSP_PATH + page;
    }
    
}
