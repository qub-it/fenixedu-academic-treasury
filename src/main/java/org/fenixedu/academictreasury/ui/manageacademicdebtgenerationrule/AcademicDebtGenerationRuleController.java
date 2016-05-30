/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: xpto@qub-it.com
 *
 * 
 * This file is part of FenixEdu Academictreasury.
 *
 * FenixEdu Academictreasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academictreasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academictreasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academictreasury.ui.manageacademicdebtgenerationrule;

import java.util.Collections;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.util.Comparators;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRuleType;
import org.fenixedu.academictreasury.dto.debtGeneration.AcademicDebtGenerationRuleBean;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.ui.administration.base.managelog.TreasuryOperationLogController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Sets;

//@Component("org.fenixedu.academictreasury.ui.manageacademicdebtgenerationrule") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.title.manageacademicdebtgenerationrule",
        accessGroup = "treasuryManagers")
// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
@RequestMapping(AcademicDebtGenerationRuleController.CONTROLLER_URL)
public class AcademicDebtGenerationRuleController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/manageacademicdebtgenerationrule/academicdebtgenerationrule";
    private static final String JSP_PATH = "academicTreasury/manageacademicdebtgenerationrule/academicdebtgenerationrule";

    @RequestMapping
    public String home(Model model) {
        //this is the default behaviour, for handling in a Spring Functionality
        return "forward:" + CHOOSE_TYPE_URL;
    }

    private static final String _CHOOSE_TYPE_URI = "/choosetype";
    public static final String CHOOSE_TYPE_URL = CONTROLLER_URL + _CHOOSE_TYPE_URI;

    @RequestMapping(value = _CHOOSE_TYPE_URI)
    public String choosetype(Model model) {
        model.addAttribute("academicDebtGenerationRuleTypesSet", Bennu.getInstance().getAcademicDebtGenerationRuleTypesSet());

        return jspPage("choosetype");
    }

    private static final String _CHOOSE_EXECUTION_YEAR_URI = "/chooseexecutionyear";
    public static final String CHOOSE_EXECUTION_YEAR_URL = CONTROLLER_URL + _CHOOSE_EXECUTION_YEAR_URI;

    @RequestMapping(value = _CHOOSE_EXECUTION_YEAR_URI + "/{typeId}")
    public String chooseexecutionyear(@PathVariable("typeId") final AcademicDebtGenerationRuleType type, final Model model) {
        model.addAttribute("academicDebtGenerationRuleType", type);

        final SortedSet<ExecutionYear> executionYearsSet =
                Sets.newTreeSet(Collections.reverseOrder(ExecutionYear.COMPARATOR_BY_YEAR));
        executionYearsSet.addAll(ExecutionYear.readNotClosedExecutionYears());

        model.addAttribute("executionYearsSet", executionYearsSet);

        return jspPage("chooseexecutionyear");
    }

    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI + "/{typeId}/{executionYearId}")
    public String search(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear, final Model model) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        model.addAttribute("searchacademicdebtgenerationruleResultsDataSet", AcademicDebtGenerationRule.find(type, executionYear)
                .sorted(AcademicDebtGenerationRule.COMPARE_BY_ORDER_NUMBER).collect(Collectors.toList()));

        return jspPage("search");
    }

    private static final String _SEARCH_TO_DELETE_ACTION_URI = "/search/delete/";
    public static final String SEARCH_TO_DELETE_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_DELETE_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_DELETE_ACTION_URI + "/{typeId}/{executionYearId}/{oid}", method = RequestMethod.POST)
    public String processSearchToDeleteAction(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") final AcademicDebtGenerationRule academicDebtGenerationRule, final Model model,
            final RedirectAttributes redirectAttributes) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        model.addAttribute("academicDebtGenerationRule", academicDebtGenerationRule);
        try {
            academicDebtGenerationRule.delete();

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.AcademicDebtGenerationRule.delete.success"), model);
            return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                    redirectAttributes);
        } catch (DomainException ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
        }

        return search(type, executionYear, model);
    }

    private static final String _SEARCH_TO_INACTIVATE_ACTION_URI = "/search/inactivate/";
    public static final String SEARCH_TO_INACTIVATE_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_INACTIVATE_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_INACTIVATE_ACTION_URI + "/{typeId}/{executionYearId}/{oid}")
    public String processSearchToInactivateAction(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") AcademicDebtGenerationRule academicDebtGenerationRule, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        academicDebtGenerationRule.inactivate();

        // CHANGE_ME Insert code here for processing rowAction inactivate
        // If you selected multiple exists you must choose which one to use below
        return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                redirectAttributes);
    }

    private static final String _SEARCH_TO_ACTIVATE_ACTION_URI = "/search/activate/";
    public static final String SEARCH_TO_ACTIVATE_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_ACTIVATE_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_ACTIVATE_ACTION_URI + "/{typeId}/{executionYearId}/{oid}")
    public String processSearchToActivateAction(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") final AcademicDebtGenerationRule academicDebtGenerationRule, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        academicDebtGenerationRule.activate();

        return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                redirectAttributes);
    }

    private static final String _SEARCH_TO_READ_LOG_ACTION_URI = "/readlog/";
    public static final String SEARCH_TO_READ_LOG_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_READ_LOG_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_READ_LOG_ACTION_URI + "/{typeId}/{executionYearId}/{oid}")
    public String processSearchToReadLogAction(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") final AcademicDebtGenerationRule academicDebtGenerationRule, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        return redirect(TreasuryOperationLogController.READ_URL + academicDebtGenerationRule.getExternalId(), model,
                redirectAttributes);
    }

    private static final String _PROCESS_ACTION_URI = "/search/process/";
    public static final String PROCESS_ACTION_URL = CONTROLLER_URL + _PROCESS_ACTION_URI;

    @RequestMapping(value = _PROCESS_ACTION_URI + "/{typeId}/{executionYearId}/{oid}")
    public String processProcessAction(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") AcademicDebtGenerationRule academicDebtGenerationRule, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        academicDebtGenerationRule.getAcademicDebtGenerationRuleType().strategyImplementation()
                .process(academicDebtGenerationRule);

        // CHANGE_ME Insert code here for processing rowAction inactivate
        // If you selected multiple exists you must choose which one to use below
        return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                redirectAttributes);
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI + "/{typeId}/{executionYearId}", method = RequestMethod.GET)
    public String create(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear, final Model model) {
        AcademicDebtGenerationRuleBean bean = new AcademicDebtGenerationRuleBean(type, executionYear);

        return _create(bean, type, executionYear, model);
    }

    private String _create(final AcademicDebtGenerationRuleBean bean, final AcademicDebtGenerationRuleType type,
            final ExecutionYear executionYear, Model model) {
        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _ADDPRODUCT_URI = "/addproduct";
    public static final String ADDPRODUCT_URL = CONTROLLER_URL + _ADDPRODUCT_URI;

    @RequestMapping(value = _ADDPRODUCT_URI + "/{typeId}/{executionYearId}", method = RequestMethod.POST)
    public String addproduct(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, final Model model) {

        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        bean.addEntry();

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _CHOOSEEXECUTIONYEARPOSTBACK_URI = "/chooseexecutionyearpostback";
    public static final String CHOOSEEXECUTIONYEARPOSTBACK_URL = CONTROLLER_URL + _CHOOSEEXECUTIONYEARPOSTBACK_URI;

    @RequestMapping(value = _CHOOSEEXECUTIONYEARPOSTBACK_URI + "/{typeId}/{executionYearId}", method = RequestMethod.POST)
    public String chooseExecutionYearPostback(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, final Model model) {

        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _CHOOSEDEGREETYPEPOSTBACK_URI = "/choosedegreetypepostback";
    public static final String CHOOSEDEGREETYPEPOSTBACK_URL = CONTROLLER_URL + _CHOOSEDEGREETYPEPOSTBACK_URI;

    @RequestMapping(value = _CHOOSEDEGREETYPEPOSTBACK_URI + "/{typeId}/{executionYearId}", method = RequestMethod.POST)
    public String chooseDegreeTypePostback(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, final Model model) {

        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        bean.chooseDegreeType();

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _ADDDEGREECURRICULARPLANS_URI = "/adddegreecurricularplans";
    public static final String ADDDEGREECURRICULARPLANS_URL = CONTROLLER_URL + _ADDDEGREECURRICULARPLANS_URI;

    @RequestMapping(value = _ADDDEGREECURRICULARPLANS_URI + "/{typeId}/{executionYearId}", method = RequestMethod.POST)
    public String adddegreeCurricularPlans(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, final Model model) {

        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        bean.addDegreeCurricularPlans();

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _REMOVEDEGREECURRICULARPLAN_URI = "/removedegreecurricularplan";
    public static final String REMOVEDEGREECURRICULARPLAN_URL = CONTROLLER_URL + _REMOVEDEGREECURRICULARPLAN_URI;

    @RequestMapping(value = _REMOVEDEGREECURRICULARPLAN_URI + "/{typeId}/{executionYearId}/{entryIndex}",
            method = RequestMethod.POST)
    public String removeDegreeCurricularPlan(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("entryIndex") int entryIndex,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, final Model model) {

        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        bean.removeDegreeCurricularPlan(entryIndex);

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");

    }

    private static final String _REMOVEPRODUCT_URI = "/removeproduct";
    public static final String REMOVEPRODUCT_URL = CONTROLLER_URL + _REMOVEPRODUCT_URI;

    @RequestMapping(value = _REMOVEPRODUCT_URI + "/{typeId}/{executionYearId}/{entryIndex}", method = RequestMethod.POST)
    public String removeproduct(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear, @PathVariable("entryIndex") int entryIndex,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, final Model model) {

        model.addAttribute("academicDebtGenerationRuleType", type);
        model.addAttribute("executionYear", executionYear);

        bean.removEntry(entryIndex);

        model.addAttribute("academicDebtGenerationRuleBean", bean);
        model.addAttribute("academicDebtGenerationRuleBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    @RequestMapping(value = _CREATE_URI + "/{typeId}/{executionYearId}", method = RequestMethod.POST)
    public String create(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @RequestParam(value = "bean", required = false) final AcademicDebtGenerationRuleBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        try {

            final AcademicDebtGenerationRule academicDebtGenerationRule = AcademicDebtGenerationRule.create(bean);

            //Success Validation
            //Add the bean to be used in the View
            model.addAttribute("academicDebtGenerationRule", academicDebtGenerationRule);
            return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                    redirectAttributes);

        } catch (DomainException de) {
            addErrorMessage(de.getLocalizedMessage(), model);

            return _create(bean, type, executionYear, model);
        }
    }

    private static final String _TOGGLE_BACKGROUND_EXECUTION_URI = "/togglebackgroundexecution";
    public static final String TOGGLE_BACKGROUND_EXECUTION_URL = CONTROLLER_URL + _TOGGLE_BACKGROUND_EXECUTION_URI;

    @RequestMapping(value = _TOGGLE_BACKGROUND_EXECUTION_URI + "/{typeId}/{executionYearId}/{oid}")
    public String toggleBackgroundExecution(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") final AcademicDebtGenerationRule academicDebtGenerationRule, final Model model,
            final RedirectAttributes redirectAttributes) {

        academicDebtGenerationRule.toggleBackgroundExecution();
        return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                redirectAttributes);
    }

    private static final String _ORDER_UP_URI = "/orderup";
    public static final String ORDER_UP_URL = CONTROLLER_URL + _ORDER_UP_URI;

    @RequestMapping(value = _ORDER_UP_URI + "/{typeId}/{executionYearId}/{oid}")
    public String orderup(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") final AcademicDebtGenerationRule academicDebtGenerationRule, final Model model,
            final RedirectAttributes redirectAttributes) {

        academicDebtGenerationRule.orderUp();
        return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                redirectAttributes);
    }

    private static final String _ORDER_DOWN_URI = "/orderdown";
    public static final String ORDER_DOWN_URL = CONTROLLER_URL + _ORDER_DOWN_URI;

    @RequestMapping(value = _ORDER_DOWN_URI + "/{typeId}/{executionYearId}/{oid}")
    public String orderdown(@PathVariable("typeId") final AcademicDebtGenerationRuleType type,
            @PathVariable("executionYearId") final ExecutionYear executionYear,
            @PathVariable("oid") final AcademicDebtGenerationRule academicDebtGenerationRule, final Model model,
            final RedirectAttributes redirectAttributes) {

        academicDebtGenerationRule.orderDown();
        return redirect(String.format("%s/%s/%s", SEARCH_URL, type.getExternalId(), executionYear.getExternalId()), model,
                redirectAttributes);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }
}
