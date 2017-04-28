package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import java.util.stream.Collectors;

import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoType;
import org.fenixedu.academictreasury.dto.integration.tuitioninfo.ERPTuitionInfoTypeBean;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping(ERPTuitionInfoTypeController.CONTROLLER_URL)
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.ERPTuitionInfoType.title", accessGroup = "#managers")
public class ERPTuitionInfoTypeController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/erptuitioninfotype";
    public static final String JSP_PATH = "academicTreasury/erptuitioninfotype";

    @RequestMapping
    public String home() {
        return "redirect:" + SEARCH_URL;
    }

    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI, method = RequestMethod.GET)
    public String search(final Model model) {
        model.addAttribute("result",
                ERPTuitionInfoType.findAll().sorted(ERPTuitionInfoType.COMPARE_BY_PRODUCT_NAME).collect(Collectors.toList()));

        return jspPage(_SEARCH_URI);
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(final Model model) {

        return null;
    }

    private String _create(final ERPTuitionInfoTypeBean bean, final Model model) {

        model.addAttribute("bean", bean);
        model.addAttribute("bean", getBeanJson(bean));

        return jspPage(_CREATE_URI);
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String createpost(final ERPTuitionInfoTypeBean bean, final Model model, final RedirectAttributes redirectAttributes) {

        try {
            if (bean.getTuitionType() == null) {
                throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.tuitionType.required");
            }

            if (bean.getTuitionType().isForRegistrationTuition()) {
                if (bean.getDegree() != null) {
                    ERPTuitionInfoType.createForRegistrationTuition(bean.getProduct(), bean.getDegree());
                } else {
                    ERPTuitionInfoType.createForRegistrationTuition(bean.getProduct(), bean.getDegreeType());
                }
            } else if (bean.getTuitionType().isForExtracurricularTuition()) {
                ERPTuitionInfoType.createForExtracurricularTuition(bean.getProduct());
            } else if (bean.getTuitionType().isForStandaloneTuition()) {
                ERPTuitionInfoType.createForStandaloneTuition(bean.getProduct());
            }

            addInfoMessage(Constants.bundle("error.ERPTuitionInfoType.success"), model);

            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (org.fenixedu.bennu.core.domain.exceptions.DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _create(bean, model);
        }
    }

    private static final String _CREATE_POSTBACK_URI = "/createpostback";
    public static final String CREATE_POSTBACK_URL = CONTROLLER_URL + _CREATE_POSTBACK_URI;

    @RequestMapping(value = _CREATE_POSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    private @ResponseBody ResponseEntity<String> createpostback(
            @RequestParam(value = "bean", required = false) final ERPTuitionInfoTypeBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private static final String _UPDATE_URI = "/update";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI, method = RequestMethod.GET)
    public String update(final Model model) {
        return null;
    }

    @RequestMapping(value = _UPDATE_URI, method = RequestMethod.POST)
    public String updatepost(final Model model, final RedirectAttributes redirectAttributes) {
        return null;
    }

    private static final String _DELETE_URI = "/delete";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
