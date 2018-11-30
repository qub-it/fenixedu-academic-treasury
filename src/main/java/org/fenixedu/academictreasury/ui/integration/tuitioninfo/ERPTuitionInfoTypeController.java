package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoType;
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

import com.google.common.collect.Sets;

@RequestMapping(ERPTuitionInfoTypeController.CONTROLLER_URL)
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.ERPTuitionInfoType.title", accessGroup = "#managers")
public class ERPTuitionInfoTypeController extends AcademicTreasuryBaseController {
    
    public static final String CONTROLLER_URL = "/academictreasury/erptuitioninfotype";
    public static final String JSP_PATH = "academicTreasury/erptuitioninfotype";
    
    @RequestMapping
    public String home() {
        return "redirect:" + SEARCH_URL + "/" + ExecutionYear.readCurrentExecutionYear().getExternalId();
    }
    
    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL  = CONTROLLER_URL + _SEARCH_URI;
    
    @RequestMapping(value=_SEARCH_URI + "/{executionYearId}", method=RequestMethod.GET)
    public String search(final Model model, @PathVariable("executionYearId") final ExecutionYear executionYear) {
        final Set<ERPTuitionInfoType> erpTuitionInfoTypesSet = ERPTuitionInfoType.findForExecutionYear(executionYear).collect(Collectors.toSet());
        
        List<ExecutionYear> executionYearOptions = new ArrayList<>(ExecutionYear.readNotClosedExecutionYears());
        Collections.sort(executionYearOptions, ExecutionYear.REVERSE_COMPARATOR_BY_YEAR);
        
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("executionYearOptions", executionYearOptions);
        model.addAttribute("result", erpTuitionInfoTypesSet);
       
        return jspPage(_SEARCH_URI);
    }
    
    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value=_CREATE_URI + "/{executionYearId}", method=RequestMethod.GET)
    private String create(final Model model, @PathVariable("executionYearId") final ExecutionYear executionYear) {
        final ERPTuitionInfoTypeBean bean = new ERPTuitionInfoTypeBean();
        bean.update();

        return _create(executionYear, bean, model);
    }
    
    private String _create(@PathVariable("executionYearId") final ExecutionYear executionYear, final ERPTuitionInfoTypeBean bean, final Model model) {
        
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("beanJson", getBeanJson(bean));
        
        return jspPage(_CREATE_URI);
    }
    
    @RequestMapping(value=_CREATE_URI + "/{executionYearId}", method=POST)
    public String create(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model, final RedirectAttributes redirectAttributes) {
        try {
            ERPTuitionInfoType.create(executionYear, bean.getErpTuitionInfoProduct(), Sets.newHashSet(bean.getTuitionProducts()));
            
            return redirect("/", model, redirectAttributes);
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return _create(executionYear, bean, model);
        }
    }
    
    private static final String _REMOVE_TUITION_PRODUCT_URI = "/removetuitionproduct";
    public static final String REMOVE_TUITION_PRODUCT_URL = CONTROLLER_URL + _REMOVE_TUITION_PRODUCT_URI;
    
    @RequestMapping(value=_REMOVE_TUITION_PRODUCT_URI + "/{executionYearId}/{productIndex}", method=POST)
    public String removetuitionproduct(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("productIndex") final int productIndex,
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        // bean.removeTuitionProduct(productIndex);
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _ADD_TUITION_PRODUCT_URI = "/addtuitionproduct";
    public static final String ADD_TUITION_PRODUCT_URL = CONTROLLER_URL + _ADD_TUITION_PRODUCT_URI;
    
    @RequestMapping(value=_ADD_TUITION_PRODUCT_URI + "/{executionYearId}", method=POST)
    public String addtuitionproduct(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, 
            final Model model) {
        
        bean.addTuitionProduct();
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _ADD_DEGREE_TYPE_URI = "/adddegreetype";
    public static final String ADD_DEGREE_TYPE_URL = CONTROLLER_URL + _ADD_DEGREE_TYPE_URI;
    
    @RequestMapping(value=_ADD_DEGREE_TYPE_URI + "/{executionYearId}", method=POST)
    public String adddegreetype(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        bean.addDegreeType();
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _ADD_DEGREES_URI = "/adddegrees";
    public static final String ADD_DEGREES_URL = CONTROLLER_URL + _ADD_DEGREES_URI;
    
    @RequestMapping(value=_ADD_DEGREES_URI + "/{executionYearId}", method=POST)
    public String adddegrees(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        bean.addDegrees();
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _ADD_DEGREE_CURRICULAR_PLANS_URI = "adddegreecurricularplans";
    public static final String ADD_DEGREE_CURRICULAR_PLANS_URL = CONTROLLER_URL + _ADD_DEGREE_CURRICULAR_PLANS_URI;
    
    @RequestMapping(value=_ADD_DEGREE_CURRICULAR_PLANS_URI + "/{executionYearId}", method=POST)
    public String adddegreecurricularplan(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        bean.addDegreeCurricularPlans();
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _CHOOSE_DEGREE_TYPE_POSTBACK_URI = "choosedegreetypepostback";
    public static final String CHOOSE_DEGREE_TYPE_POSTBACK_URL = CONTROLLER_URL + _CHOOSE_DEGREE_TYPE_POSTBACK_URI;
    
    @RequestMapping()
    public String choosedegreetypepostback(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        bean.update();
        
        return _create(executionYear, bean, model);
    }
    
    
    private static final String _UPDATE_URI = "";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;
    
    private static final String _DELETE_URI = "";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;
    
    private String jspPage(final String page) {
        return JSP_PATH + page;
    }
    
}
