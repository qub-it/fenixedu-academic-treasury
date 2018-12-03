package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import static org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoTypeBean.DEGREES_OPTION;
import static org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTION;
import static org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoType;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoTypeBean;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.Product;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

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
        final ERPTuitionInfoTypeBean bean = new ERPTuitionInfoTypeBean(executionYear);
        bean.update();

        return _create(executionYear, bean, model);
    }
    
    private String _create(@PathVariable("executionYearId") final ExecutionYear executionYear, final ERPTuitionInfoTypeBean bean, final Model model) {
        
        model.addAttribute("bean", bean);
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("beanJson", getBeanJson(bean));
        
        return jspPage(_CREATE_URI);
    }
    
    @RequestMapping(value=_CREATE_URI + "/{executionYearId}", method=POST)
    public String createpost(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model, final RedirectAttributes redirectAttributes) {
        try {
            ERPTuitionInfoType.create(bean);
            
            return redirect(SEARCH_URL + "/" + executionYear.getExternalId(), model, redirectAttributes);
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return _create(executionYear, bean, model);
        }
    }
    
    private static final String _REMOVE_TUITION_PRODUCT_URI = "/removetuitionproduct";
    public static final String REMOVE_TUITION_PRODUCT_URL = CONTROLLER_URL + _REMOVE_TUITION_PRODUCT_URI;
    
    @RequestMapping(value=_REMOVE_TUITION_PRODUCT_URI + "/{executionYearId}/{productId}", method=POST)
    public String removetuitionproduct(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("productId") final Product product,
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        bean.removeTuitionProduct(product);
        
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
        try {
            bean.addDegreeType();
            
            bean.update();
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return _create(executionYear, bean, model);
    }
    
    private static final String _ADD_DEGREES_URI = "/adddegrees";
    public static final String ADD_DEGREES_URL = CONTROLLER_URL + _ADD_DEGREES_URI;
    
    @RequestMapping(value=_ADD_DEGREES_URI + "/{executionYearId}", method=POST)
    public String adddegrees(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        try {
            bean.addDegrees();
            
            bean.update();
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _ADD_DEGREE_CURRICULAR_PLANS_URI = "/adddegreecurricularplans";
    public static final String ADD_DEGREE_CURRICULAR_PLANS_URL = CONTROLLER_URL + _ADD_DEGREE_CURRICULAR_PLANS_URI;
    
    @RequestMapping(value=_ADD_DEGREE_CURRICULAR_PLANS_URI + "/{executionYearId}", method=POST)
    public String adddegreecurricularplan(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {

        try {
            bean.addDegreeCurricularPlans();
            
            bean.update();
        } catch(final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
        }

        return _create(executionYear, bean, model);
    }
    
    private static final String _CHOOSE_TUITION_PAYMENT_PLAN_GROUP_URI = "/choosetuitionpaymentplangroup";
    public static final String CHOOSE_TUITION_PAYMENT_PLAN_GROUP_URL = CONTROLLER_URL + _CHOOSE_TUITION_PAYMENT_PLAN_GROUP_URI;
    
    @RequestMapping(value=_CHOOSE_TUITION_PAYMENT_PLAN_GROUP_URI + "/{executionYearId}/{tuitionPaymentPlanGroupId}", method=POST)
    public String choosetuitionpaymentplangroup(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("tuitionPaymentPlanGroupId")  final TuitionPaymentPlanGroup tuitionPaymentPlanGroup, 
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        final TuitionPaymentPlanGroup e = TuitionPaymentPlanGroup.findUniqueDefaultGroupForExtracurricular().get();
        final TuitionPaymentPlanGroup s = TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get();
        final TuitionPaymentPlanGroup r = TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get();
        
        if(!Lists.newArrayList(r, s, e).contains(tuitionPaymentPlanGroup)) {
            throw new RuntimeException("invalid request");
        }
        
        bean.setTuitionPaymentPlanGroup(tuitionPaymentPlanGroup);

        return _create(executionYear, bean, model);
    }
    
    private static final String _CHOOSE_DEGREE_TYPE_POSTBACK_URI = "/choosedegreetypepostback";
    public static final String CHOOSE_DEGREE_TYPE_POSTBACK_URL = CONTROLLER_URL + _CHOOSE_DEGREE_TYPE_POSTBACK_URI;
    
    @RequestMapping(value=_CHOOSE_DEGREE_TYPE_POSTBACK_URI + "/{executionYearId}", method=POST)
    public String choosedegreetypepostback(@PathVariable("executionYearId") final ExecutionYear executionYear, @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        bean.update();
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _CHOOSE_DEGREE_INFORMATION_TO_ADD_URI = "/choosedegreeinformationtoadd";
    public static final String CHOOSE_DEGREE_INFORMATION_TO_ADD_URL = CONTROLLER_URL + _CHOOSE_DEGREE_INFORMATION_TO_ADD_URI;
    
    @RequestMapping(value=_CHOOSE_DEGREE_INFORMATION_TO_ADD_URI + "/{executionYearId}/{option}", method=POST)
    public String choosedegreeinformationtoadd(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("option") final String option, 
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        if(!Lists.newArrayList(DEGREE_TYPE_OPTION, DEGREES_OPTION, DEGREE_CURRICULAR_PLANS_OPTION).contains(option)) {
            throw new RuntimeException("invalid request");
        }
        
        bean.setDegreeInfoSelectOption(option);
        bean.update();
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _REMOVE_DEGREE_TYPE_URI = "/removedegreetype";
    public static final String REMOVE_DEGREE_TYPE_URL = CONTROLLER_URL + _REMOVE_DEGREE_TYPE_URI;
    
    @RequestMapping(value=_REMOVE_DEGREE_TYPE_URI + "/{executionYearId}/{degreeTypeId}", method=POST)
    public String removedegreetype(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("degreeTypeId") final DegreeType degreeType, 
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {
        
        bean.removeDegreeType(degreeType);
        
        return _create(executionYear, bean, model);
    }
    
    private static final String _REMOVE_DEGREE_URI = "/removedegree";
    public static final String REMOVE_DEGREE_URL = CONTROLLER_URL + _REMOVE_DEGREE_URI;
    
    @RequestMapping(value=_REMOVE_DEGREE_URI + "/{executionYearId}/{degreeId}", method=POST)
    public String removedegree(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("degreeId") final Degree degree, 
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {

        bean.removeDegree(degree);

        
        return _create(executionYear, bean, model);
    }
    
    private static final String _REMOVE_DEGREE_CURRICULAR_PLAN_URI = "/removedegreecurricularplan";
    public static final String REMOVE_DEGREE_CURRICULAR_PLAN_URL = CONTROLLER_URL + _REMOVE_DEGREE_CURRICULAR_PLAN_URI;
    
    @RequestMapping(value=_REMOVE_DEGREE_CURRICULAR_PLAN_URI + "/{executionYearId}/{degreeCurricularPlanId}", method=POST)
    public String removedegreecurricularplan(@PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("degreeCurricularPlanId") final DegreeCurricularPlan degreeCurricularPlan, 
            @RequestParam("bean") final ERPTuitionInfoTypeBean bean, final Model model) {

        bean.removeDegreeCurricularPlan(degreeCurricularPlan);

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
