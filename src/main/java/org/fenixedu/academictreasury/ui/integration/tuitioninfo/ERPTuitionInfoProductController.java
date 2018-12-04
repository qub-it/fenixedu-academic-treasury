package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoProduct;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoTypeBean;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.ERPTuitionInfoProduct.title", accessGroup = "#managers")
@RequestMapping(value=ERPTuitionInfoProductController.CONTROLLER_URL)
public class ERPTuitionInfoProductController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/erptuitioninfoproduct";
    public static final String JSP_PATH = "academicTreasury/erptuitioninfoproduct";
    
    @RequestMapping
    public String home() {
        return "redirect:" + SEARCH_URL;
    }
    
    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL  = CONTROLLER_URL + _SEARCH_URI;
    
    @RequestMapping(value=_SEARCH_URI, method=RequestMethod.GET)
    public String search(final Model model) {
        
        final Set<ERPTuitionInfoProduct> erpTuitionInfoProductsSet = ERPTuitionInfoProduct.findAll().collect(Collectors.toSet());
        
        model.addAttribute("result", erpTuitionInfoProductsSet);
        
        return jspPage(_SEARCH_URI);
    }
    
    
    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value=_CREATE_URI, method=GET)
    public String create(final Model model) {
        return jspPage(_CREATE_URI);
    }
        
    @RequestMapping(value=_CREATE_URI, method=POST)
    public String create(final Model model, @RequestParam("code") final String code, @RequestParam("name") final String name) {
        
        try {
            ERPTuitionInfoProduct.create(code, name);
            
            return "redirect:" + SEARCH_URL;
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            
            return jspPage(_CREATE_URI);
        }
        
    }
    
    private static final String _UPDATE_URI = "/update";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;
    
    @RequestMapping(value=_UPDATE_URI + "/{erpTuitionInfoProductId}", method=GET)
    public String update(final Model model, @PathVariable("erpTuitionInfoProductId") final ERPTuitionInfoProduct erpTuitionInfoProduct) {
        
        model.addAttribute("erpTuitionInfoProduct", erpTuitionInfoProduct);
        
        return jspPage(_UPDATE_URI);
    }
    
    @RequestMapping(value=_UPDATE_URI + "/{erpTuitionInfoProductId}", method=POST)
    public String update(final Model model, @PathVariable("erpTuitionInfoProductId") final ERPTuitionInfoProduct erpTuitionInfoProduct, 
            @RequestParam("name") final String name) {
        
        try {
            erpTuitionInfoProduct.update(name);
            
            return "redirect:" + SEARCH_URL;
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            
            return jspPage(_UPDATE_URI);
        }
        
    }
    
    private static final String _DELETE_URI = "/delete";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value=_DELETE_URI + "/{erpTuitionInfoProductId}", method=POST)
    public String delete(@PathVariable("erpTuitionInfoProductId") final ERPTuitionInfoProduct erpTuitionInfoProduct, final Model model, final RedirectAttributes redirectAttributes) {
        try {
            erpTuitionInfoProduct.delete();
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }
        
        return redirect(SEARCH_URL, model, redirectAttributes);
    }
    
    private String jspPage(final String page) {
        return JSP_PATH + page;
    }
    
}
