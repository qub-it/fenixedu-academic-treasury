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
package org.fenixedu.academictreasury.ui.academicservicerequestdebtcreation;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.academictreasury.dto.academicservicerequest.AcademicServiceRequestDebtCreationBean;
import org.fenixedu.academictreasury.services.EmolumentServices;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.FinantialEntity;
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
@RequestMapping(AcademicServiceRequestDebtCreationBeanController.CONTROLLER_URL)
public class AcademicServiceRequestDebtCreationBeanController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL =
            "/academictreasury/academicservicerequestdebtcreation/academicservicerequestdebtcreationbean";
    private static final String JSP_PATH =
            "academicTreasury/academicservicerequestdebtcreation/academicservicerequestdebtcreationbean";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/create/";
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model) {
        final AcademicServiceRequestDebtCreationBean bean = new AcademicServiceRequestDebtCreationBean(debtAccount);
        return _createFirstPage(debtAccount, bean, model);
    }

    private static final String _BACKTOCREATE_URI = "/backtocreate";
    public static final String BACKTOCREATE_URL = CONTROLLER_URL + _BACKTOCREATE_URI;

    @RequestMapping(value = _BACKTOCREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String backTocreate(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicServiceRequestDebtCreationBean bean,
            final Model model) {
        return _createFirstPage(debtAccount, bean, model);
    }

    public String _createFirstPage(final DebtAccount debtAccount, final AcademicServiceRequestDebtCreationBean bean,
            final Model model) {
        model.addAttribute("AcademicServiceRequestDebtCreationBean_registration_options",
                ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent().getRegistrationsSet());

        model.addAttribute("bean", bean);
        model.addAttribute("debtAccount", debtAccount);
        model.addAttribute("academicServiceRequestDebtCreationBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI + "/{debtAccountId}", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createpostback(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicServiceRequestDebtCreationBean bean,
            final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicServiceRequestDebtCreationBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {

            boolean dataMissing = false;
            if (bean.getRegistration() == null) {
                addErrorMessage(
                        academicTreasuryBundle("error.AcademicServiceRequestDebtCreation.registration.required"),
                        model);
                dataMissing = true;
            }

            if (bean.getDebtDate() == null) {
                addErrorMessage(
                        academicTreasuryBundle("error.AcademicServiceRequestDebtCreation.debtDate.required"),
                        model);
                dataMissing = true;
            }

            if (bean.getAcademicServiceRequest() == null) {
                addErrorMessage(academicTreasuryBundle("error.AcademicServiceRequestDebtCreation.academicServiceRequest.required"), model);
                dataMissing = true;
            }

            if (dataMissing) {
                return _createFirstPage(debtAccount, bean, model);
            }

            ITreasuryServiceRequest iTreasuryServiceRequest = (ITreasuryServiceRequest) bean.getAcademicServiceRequest();
            if (EmolumentServices.findTariffForAcademicServiceRequestForDefaultFinantialEntity(iTreasuryServiceRequest, bean.getDebtDate()) == null) {
                addErrorMessage(
                        academicTreasuryBundle("error.AcademicServiceRequestDebtCreation.tariff.not.found"),
                        model);
                return _createFirstPage(debtAccount, bean, model);
            }

            final AcademicTreasuryEvent event = EmolumentServices.findAcademicTreasuryEvent(iTreasuryServiceRequest);

            if (event != null && event.isChargedWithDebitEntry()) {
                addErrorMessage(
                        academicTreasuryBundle("error.AcademicServiceRequestDebtCreation.event.is.charged"),
                        model);

                return _createFirstPage(debtAccount, bean, model);
            }

            model.addAttribute("debtAccount", debtAccount);
            model.addAttribute("bean", bean);
            model.addAttribute("academicServiceRequestDebtCreationBeanJson", getBeanJson(bean));

            model.addAttribute("debt",
                    EmolumentServices.calculateForAcademicServiceRequestForDefaultFinantialEntity(iTreasuryServiceRequest, bean.getDebtDate()));

            return jspPage("confirmacademicservicerequestdebtcreation");
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _createFirstPage(debtAccount, bean, model);
        }
    }

    private static final String _CONFIRMACADEMICSERVICEREQUESTDEBTCREATION_URI = "/confirmacademicservicerequestdebtcreation";
    public static final String CONFIRMACADEMICSERVICEREQUESTDEBTCREATION_URL =
            CONTROLLER_URL + _CONFIRMACADEMICSERVICEREQUESTDEBTCREATION_URI;

    @RequestMapping(value = _CONFIRMACADEMICSERVICEREQUESTDEBTCREATION_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String confirmacademicservicerequestdebtcreation(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final AcademicServiceRequestDebtCreationBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {

            EmolumentServices.createAcademicServiceRequestEmolumentForDefaultFinantialEntity((ITreasuryServiceRequest) bean.getAcademicServiceRequest(),
                    bean.getDebtDate());

            addInfoMessage(academicTreasuryBundle("label.AcademicServiceRequest.debit.entries.created.success"),
                    model);

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
