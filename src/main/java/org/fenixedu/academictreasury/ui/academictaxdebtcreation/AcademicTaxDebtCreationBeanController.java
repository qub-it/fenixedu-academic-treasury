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
package org.fenixedu.academictreasury.ui.academictaxdebtcreation;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.dto.academictax.AcademicTaxDebtCreationBean;
import org.fenixedu.academictreasury.services.AcademicTaxServices;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@BennuSpringController(value = CustomerController.class)
@RequestMapping(AcademicTaxDebtCreationBeanController.CONTROLLER_URL)
public class AcademicTaxDebtCreationBeanController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/academictaxdebtcreation/academictaxdebtcreationbean";
    private static final String JSP_PATH = "academicTreasury/academictaxdebtcreation/academictaxdebtcreationbean";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/create/";
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model) {
        final AcademicTaxDebtCreationBean bean = new AcademicTaxDebtCreationBean(debtAccount);
        return _createFirstPage(debtAccount, bean, model);
    }

    private static final String _BACKTOCREATE_URI = "/backtocreate";
    public static final String BACKTOCREATE_URL = CONTROLLER_URL + _BACKTOCREATE_URI;

    @RequestMapping(value = _BACKTOCREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String backTocreate(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicTaxDebtCreationBean bean, final Model model) {
        return _createFirstPage(debtAccount, bean, model);
    }

    public String _createFirstPage(final DebtAccount debtAccount, final AcademicTaxDebtCreationBean bean, final Model model) {
        model.addAttribute("AcademicTaxDebtCreationBean_executionYear_options", ExecutionYear.readNotClosedExecutionYears());
        model.addAttribute("AcademicTaxDebtCreationBean_registration_options",
                ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent().getRegistrationsSet());

        model.addAttribute("bean", bean);
        model.addAttribute("debtAccount", debtAccount);
        model.addAttribute("academicTaxDebtCreationBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI + "/{debtAccountId}", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createpostback(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicTaxDebtCreationBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicTaxDebtCreationBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {

            boolean dataMissing = false;
            if (bean.getRegistration() == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.registration.required"), model);
                dataMissing = true;
            }

            if (bean.getExecutionYear() == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.executionYear.required"), model);
                dataMissing = true;
            }

            if (bean.getDebtDate() == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.debtDate.required"), model);
                dataMissing = true;
            }

            if (bean.getAcademicTax() == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.academicTax.required"), model);
                dataMissing = true;
            }

            if (bean.isImprovementTax() && bean.getImprovementEvaluation() == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.improvementEvaluation.required"), model);
                dataMissing = true;
            }

            if (dataMissing) {
                return _createFirstPage(debtAccount, bean, model);
            }

            if (bean.isImprovementTax()
                    && AcademicTaxServices.findAcademicTariffForDefaultFinantialEntity(bean.getImprovementEvaluation(), bean.getDebtDate()) == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.tariff.not.found"), model);
                return _createFirstPage(debtAccount, bean, model);
            } else if (AcademicTaxServices.findAcademicTariff(bean.getAcademicTax(), bean.getRegistration(),
                    bean.getDebtDate()) == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.tariff.not.found"), model);
                dataMissing = true;
                return _createFirstPage(debtAccount, bean, model);
            }

            if (bean.isImprovementTax()) {
                final AcademicTreasuryEvent event = AcademicTaxServices
                        .findAcademicTreasuryEventForImprovementTax(bean.getRegistration(), bean.getExecutionYear());

                if (event != null && event.isChargedWithDebitEntry(bean.getImprovementEvaluation())) {
                    addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.event.is.charged"), model);
                    return _createFirstPage(debtAccount, bean, model);
                }
            } else {
                final AcademicTreasuryEvent event = AcademicTaxServices.findAcademicTreasuryEvent(bean.getRegistration(),
                        bean.getExecutionYear(), bean.getAcademicTax());

                if (event != null && event.isChargedWithDebitEntry()) {
                    addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.event.is.charged"), model);
                    return _createFirstPage(debtAccount, bean, model);
                }
            }

            if (!bean.getAcademicTax().isImprovementTax() && !AcademicTaxServices.isAppliableOnRegistration(bean.getAcademicTax(),
                    bean.getRegistration(), bean.getExecutionYear())) {
                if (AcademicTaxServices.isRegistrationFirstYear(bean.getRegistration(), bean.getExecutionYear())) {
                    addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.academicTax.not.for.first.year"), model);
                } else {
                    addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.academicTax.not.for.subsequent.years"),
                            model);
                }

                return _createFirstPage(debtAccount, bean, model);
            }

            if (bean.isCharged()) {
                addErrorMessage(academicTreasuryBundle("error.AcademicTaxDebtCreation.academic.tax.already.charged"), model);

                return _createFirstPage(debtAccount, bean, model);
            }

            model.addAttribute("debtAccount", debtAccount);
            model.addAttribute("bean", bean);
            model.addAttribute("academicTaxDebtCreationBeanJson", getBeanJson(bean));

            if (bean.isImprovementTax()) {
                model.addAttribute("debt",
                        AcademicTaxServices.calculateImprovementTaxForDefaultEntity(bean.getImprovementEvaluation(), bean.getDebtDate()));
            } else {
                model.addAttribute("debt", AcademicTaxServices.calculateAcademicTaxForDefaultFinantialEntity(bean.getRegistration(),
                        bean.getExecutionYear(), bean.getAcademicTax(), bean.getDebtDate(), true));
            }

            return jspPage("confirmacademictaxdebtcreation");
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _createFirstPage(debtAccount, bean, model);
        }
    }

    private static final String _CONFIRMACADEMICTAXDEBTCREATION_URI = "/confirmacademictaxdebtcreation";
    public static final String CONFIRMACADEMICTAXDEBTCREATION_URL = CONTROLLER_URL + _CONFIRMACADEMICTAXDEBTCREATION_URI;

    @RequestMapping(value = _CONFIRMACADEMICTAXDEBTCREATION_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String confirmacademictaxdebtcreation(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicTaxDebtCreationBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {

            if (bean.isImprovementTax()) {
                AcademicTaxServices.createImprovementTaxForDefaultFinantialEntity(bean.getImprovementEvaluation(), bean.getDebtDate());
            } else {
                AcademicTaxServices.createAcademicTaxForDefaultFinantialEntity(bean.getRegistration(), bean.getExecutionYear(), bean.getAcademicTax(),
                        bean.getDebtDate(), true);
            }

            addInfoMessage(academicTreasuryBundle("label.AcademicTax.debit.entries.created.success"), model);

            return redirect(DebtAccountController.READ_URL + "/" + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (DomainException de) {
            addErrorMessage(de.getLocalizedMessage(), model);
            return create(debtAccount, bean, model, redirectAttributes);
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }
}
