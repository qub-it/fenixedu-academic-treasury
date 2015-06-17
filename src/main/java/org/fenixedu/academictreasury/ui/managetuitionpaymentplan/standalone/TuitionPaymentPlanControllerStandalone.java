/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
 *
 * 
 * This file is part of FenixEdu AcademicTreasury.
 *
 * FenixEdu AcademicTreasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu AcademicTreasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu AcademicTreasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academictreasury.ui.managetuitionpaymentplan.standalone;

import java.util.stream.Collectors;

import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlan;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.dto.tariff.TuitionPaymentPlanBean;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.emory.mathcs.backport.java.util.Collections;

//@Component("org.fenixedu.academicTreasury.ui.manageTuitionPaymentPlan") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.title.manageTuitionPaymentPlanStandalone",accessGroup = "logged")// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
@RequestMapping(TuitionPaymentPlanControllerStandalone.CONTROLLER_URL)
public class TuitionPaymentPlanControllerStandalone extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/tuitionpaymentplanstandalone";
    private static final String JSP_PATH = "academicTreasury/tuitionpaymentplanstandalone";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CHOOSEFINANTIALENTITY_URL;
    }

    private static final String _CHOOSEFINANTIALENTITY_URI = "/choosefinantialentity";
    public static final String CHOOSEFINANTIALENTITY_URL = CONTROLLER_URL + _CHOOSEFINANTIALENTITY_URI;

    @RequestMapping(value = _CHOOSEFINANTIALENTITY_URI)
    public String chooseFinantialEntity(final Model model) {
        model.addAttribute("choosefinantialentityResultsDataSet", FinantialEntity.findAll().collect(Collectors.toSet()));
        model.addAttribute("executionYear", ExecutionYear.readCurrentExecutionYear());
        
        return jspPage("choosefinantialentity");
    }

    private static final String _CHOOSEDEGREECURRICULARPLAN_URI = "/choosedegreecurricularplan";
    public static final String CHOOSEDEGREECURRICULARPLAN_URL = CONTROLLER_URL + _CHOOSEDEGREECURRICULARPLAN_URI;

    @RequestMapping(value = _CHOOSEDEGREECURRICULARPLAN_URI + "/{finantialEntityId}/{executionYearId}")
    public String chooseDegreeCurricularPlan(@PathVariable("finantialEntityId") FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear, final Model model) {

        model.addAttribute("choosedegreecurricularplanResultsDataSet", ExecutionDegree.getAllByExecutionYear(executionYear)
                .stream().map(e -> e.getDegreeCurricularPlan()).collect(Collectors.toList()));
        model.addAttribute("finantialEntity", finantialEntity);
        model.addAttribute("executionYear", executionYear);
        model.addAttribute(
                "executionYearOptions",
                ExecutionYear.readNotClosedExecutionYears().stream()
                        .sorted(Collections.reverseOrder(ExecutionYear.COMPARATOR_BY_BEGIN_DATE))
                        .collect(Collectors.toList()));

        return jspPage("choosedegreecurricularplan");
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI + "{finantialEntityId}/{executionYearId}/{degreeCurricularPlanId}")
    public String search(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("degreeCurricularPlanId") final DegreeCurricularPlan degreeCurricularPlan, final Model model) {

        //add the results dataSet to the model
        model.addAttribute("finantialEntity", finantialEntity);
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("degreeCurricularPlan", degreeCurricularPlan);

        model.addAttribute(
                "searchtuitionpaymentplanResultsDataSet",
                TuitionPaymentPlan.findSortedByPaymentPlanOrder(
                        TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(), degreeCurricularPlan,
                        executionYear).collect(Collectors.toList()));

        return jspPage("search");
    }

    private static final String _SEARCH_TO_DELETE_ACTION_URI = "/search/delete";
    public static final String SEARCH_TO_DELETE_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_DELETE_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_DELETE_ACTION_URI
            + "/{finantialEntityId}/{executionYearId}/{degreeCurricularPlanId}/{tuitionPaymentPlanId}",
            method = RequestMethod.GET)
    public String processSearchToDeleteAction(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("degreeCurricularPlanId") final DegreeCurricularPlan degreeCurricularPlan,
            @PathVariable("tuitionPaymentPlanId") TuitionPaymentPlan tuitionPaymentPlan, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {

            tuitionPaymentPlan.delete();

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.TuitionPaymentPlan.deletion.success"), model);
        } catch (DomainException ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(String.format(SEARCH_URL + "/%s/%s/%s", finantialEntity.getExternalId(), executionYear.getExternalId(),
                degreeCurricularPlan.getExternalId()), model, redirectAttributes);
    }

    private static final String _CREATECHOOSEDEGREECURRICULARPLANS_URI = "/createchoosedegreecurricularplans";
    public static final String CREATECHOOSEDEGREECURRICULARPLANS_URL = CONTROLLER_URL + _CREATECHOOSEDEGREECURRICULARPLANS_URI;

    @RequestMapping(value = _CREATECHOOSEDEGREECURRICULARPLANS_URI + "/{finantialEntityId}/{executionYearId}",
            method = RequestMethod.GET)
    public String createchoosedegreecurricularplans(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear, final Model model) {

        final TuitionPaymentPlanBean bean =
                new TuitionPaymentPlanBean(null, TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get(),
                        finantialEntity, executionYear);

        model.addAttribute("finantialEntity", finantialEntity);
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("bean", bean);
        model.addAttribute("tuitionPaymentPlanBeanJson", getBeanJson(bean));

        return jspPage("createchoosedegreecurricularplans");
    }

    private static final String _CREATECHOOSEDEGREECURRICULARPLANSPOSTBACK_URI = "/createchoosedegreecurricularplanspostback";
    public static final String CREATECHOOSEDEGREECURRICULARPLANSPOSTBACK_URL = CONTROLLER_URL
            + _CREATECHOOSEDEGREECURRICULARPLANSPOSTBACK_URI;

    @RequestMapping(value = _CREATECHOOSEDEGREECURRICULARPLANSPOSTBACK_URI + "/{finantialEntityId}/{executionYearId}",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createchoosedegreecurricularplanspostback(
            @PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam("bean") final TuitionPaymentPlanBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private static final String _CREATEDEFINESTUDENTCONDITIONS_URI = "/createdefinestudentconditions";
    public static final String CREATEDEFINESTUDENTCONDITIONS_URL = CONTROLLER_URL + _CREATEDEFINESTUDENTCONDITIONS_URI;

    @RequestMapping(value = _CREATEDEFINESTUDENTCONDITIONS_URI + "/{finantialEntityId}/{executionYearId}",
            method = RequestMethod.POST)
    public String createdefinestudentconditions(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam("bean") final TuitionPaymentPlanBean bean, final Model model) {

        model.addAttribute("finantialEntity", finantialEntity);
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("bean", bean);
        model.addAttribute("tuitionPaymentPlanBeanJson", getBeanJson(bean));

        return jspPage("createdefinestudentconditions");
    }

    private static final String _CREATEDEFINESTUDENTCONDITIONSPOSTBACK_URI = "/createdefinestudentconditionspostback";
    public static final String CREATEDEFINESTUDENTCONDITIONSPOSTBACK_URL = CONTROLLER_URL
            + _CREATEDEFINESTUDENTCONDITIONSPOSTBACK_URI;

    @RequestMapping(value = _CREATEDEFINESTUDENTCONDITIONSPOSTBACK_URI + "/{finantialEntityId}/{executionYearId}",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createdefinestudentconditionspostback(
            @PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam("bean") final TuitionPaymentPlanBean bean, final Model model) {

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private static final String _CREATEPAYMENTPLAN_URI = "/createpaymentplan";
    public static final String CREATEPAYMENTPLAN_URL = CONTROLLER_URL + _CREATEPAYMENTPLAN_URI;

    @RequestMapping(value = _CREATEPAYMENTPLAN_URI + "/{finantialEntityId}/{executionYearId}", method = RequestMethod.POST)
    public String createinsertinstallments(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam("bean") final TuitionPaymentPlanBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {
            bean.addInstallment();
            
            TuitionPaymentPlan.create(bean);

            return redirect(
                    String.format("%s/%s/%s", CHOOSEDEGREECURRICULARPLAN_URL, finantialEntity.getExternalId(),
                            executionYear.getExternalId()), model, redirectAttributes);
        } catch (final DomainException de) {
            addErrorMessage(de.getLocalizedMessage(), model);
            return createdefinestudentconditions(finantialEntity, executionYear, bean, model);
        }
    }

    private static final String BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URI = "/backtochoosedegreecurricularplans";
    public static final String BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URL = CONTROLLER_URL
            + BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URI;

    @RequestMapping(value = BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URI + "/{finantialEntityId}/{executionYearId}")
    public String processBackToChooseDegreeCurricularPlansAction(
            @PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam("bean") final TuitionPaymentPlanBean bean, final Model model) {

        model.addAttribute("finantialEntity", finantialEntity);
        model.addAttribute("executionYear", executionYear);
        model.addAttribute("bean", bean);
        model.addAttribute("tuitionPaymentPlanBeanJson", getBeanJson(bean));

        return jspPage("createchoosedegreecurricularplans");
    }

    private static final String _ORDER_UP_ACTION_URI = "/paymentplanorderup";
    public static final String ORDER_UP_ACTION_URL = CONTROLLER_URL + _ORDER_UP_ACTION_URI;

    @RequestMapping(value = _ORDER_UP_ACTION_URI
            + "/{finantialEntityId}/{executionYearId}/{degreeCurricularPlanId}/{tuitionPaymentPlanId}",
            method = RequestMethod.GET)
    public String processOrderUpAction(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("degreeCurricularPlanId") final DegreeCurricularPlan degreeCurricularPlan,
            @PathVariable("tuitionPaymentPlanId") TuitionPaymentPlan tuitionPaymentPlan, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {

            tuitionPaymentPlan.orderUp();

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.TuitionPaymentPlan.order.up.success"), model);
        } catch (DomainException ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(String.format(SEARCH_URL + "/%s/%s/%s", finantialEntity.getExternalId(), executionYear.getExternalId(),
                degreeCurricularPlan.getExternalId()), model, redirectAttributes);
    }

    private static final String _ORDER_DOWN_ACTION_URI = "/paymentplanorderdown";
    public static final String ORDER_DOWN_ACTION_URL = CONTROLLER_URL + _ORDER_DOWN_ACTION_URI;

    @RequestMapping(value = _ORDER_DOWN_ACTION_URI
            + "/{finantialEntityId}/{executionYearId}/{degreeCurricularPlanId}/{tuitionPaymentPlanId}",
            method = RequestMethod.GET)
    public String processOrderDownAction(@PathVariable("finantialEntityId") final FinantialEntity finantialEntity,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("degreeCurricularPlanId") final DegreeCurricularPlan degreeCurricularPlan,
            @PathVariable("tuitionPaymentPlanId") TuitionPaymentPlan tuitionPaymentPlan, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {

            tuitionPaymentPlan.orderDown();

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.TuitionPaymentPlan.order.down.success"), model);
        } catch (DomainException ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return redirect(String.format(SEARCH_URL + "/%s/%s/%s", finantialEntity.getExternalId(), executionYear.getExternalId(),
                degreeCurricularPlan.getExternalId()), model, redirectAttributes);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
