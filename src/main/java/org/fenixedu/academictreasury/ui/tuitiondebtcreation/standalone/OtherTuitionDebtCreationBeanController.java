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
package org.fenixedu.academictreasury.ui.tuitiondebtcreation.standalone;

import static org.fenixedu.academictreasury.util.Constants.academicTreasuryBundle;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup;
import org.fenixedu.academictreasury.dto.tuition.TuitionDebtCreationBean;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
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

import com.google.common.collect.Sets;

@BennuSpringController(value = CustomerController.class)
@RequestMapping(OtherTuitionDebtCreationBeanController.CONTROLLER_URL)
public class OtherTuitionDebtCreationBeanController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/othertuitiondebtcreation/tuitiondebtcreationbean";
    private static final String JSP_PATH = "academicTreasury/tuitiondebtcreation/tuitiondebtcreationbean";

    protected TuitionPaymentPlanGroup standaloneTuitionPaymentPlanGroup() {
        return TuitionPaymentPlanGroup.findUniqueDefaultGroupForStandalone().get();
    }

    protected TuitionPaymentPlanGroup extracurricularTuitionPaymentPlanGroup() {
        return TuitionPaymentPlanGroup.findUniqueDefaultGroupForExtracurricular().get();
    }

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/create/";
    }

    @RequestMapping(value = _CREATE_REGISTRATION_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String createregistraton(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model) {
        final TuitionDebtCreationBean bean =
                new TuitionDebtCreationBean(debtAccount, TuitionPaymentPlanGroup.findUniqueDefaultGroupForRegistration().get());
        return _createFirstPage(debtAccount, bean, model);
    }

    private static final String _CREATESTANDALONE_URI = "/createstandalone";
    public static final String CREATESTANDALONE_URL = CONTROLLER_URL + _CREATESTANDALONE_URI;

    @RequestMapping(value = _CREATESTANDALONE_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String createstandalone(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model) {
        final TuitionDebtCreationBean bean = new TuitionDebtCreationBean(debtAccount, standaloneTuitionPaymentPlanGroup());
        return _createFirstPage(debtAccount, bean, model);
    }

    private static final String _CREATEEXTRACURRICULAR_URI = "/createextracurricular";
    public static final String CREATEEXTRACURRICULAR_URL = CONTROLLER_URL + _CREATEEXTRACURRICULAR_URI;

    @RequestMapping(value = _CREATEEXTRACURRICULAR_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String createextracurricular(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model) {
        final TuitionDebtCreationBean bean = new TuitionDebtCreationBean(debtAccount, extracurricularTuitionPaymentPlanGroup());
        return _createFirstPage(debtAccount, bean, model);
    }

    private static final String _BACKTOCREATE_URI = "/backtocreate";
    public static final String BACKTOCREATE_URL = CONTROLLER_URL + _BACKTOCREATE_URI;

    @RequestMapping(value = _BACKTOCREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String backTocreate(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final TuitionDebtCreationBean bean, final Model model) {
        return _createFirstPage(debtAccount, bean, model);
    }

    public String _createFirstPage(final DebtAccount debtAccount, final TuitionDebtCreationBean bean, final Model model) {
        model.addAttribute("TuitionDebtCreationBean_executionYear_options", ExecutionYear.readNotClosedExecutionYears());
        model.addAttribute("TuitionDebtCreationBean_registration_options",
                ((PersonCustomer) debtAccount.getCustomer()).getPerson().getStudent().getRegistrationsSet());

        model.addAttribute("bean", bean);
        model.addAttribute("debtAccount", debtAccount);
        model.addAttribute("tuitionDebtCreationBeanJson", getBeanJson(bean));

        return jspPage("create");
    }

    private static final String _CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI + "/{debtAccountId}", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> createpostback(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final TuitionDebtCreationBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private static final String _CREATE_REGISTRATION_URI = "/createregistration";
    public static final String CREATE_REGISTRATION_URL = CONTROLLER_URL + _CREATE_REGISTRATION_URI;

    @RequestMapping(value = _CREATE_REGISTRATION_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String createregistration(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final TuitionDebtCreationBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("debtAccount", debtAccount);
            model.addAttribute("bean", bean);
            model.addAttribute("tuitionDebtCreationBeanJson", getBeanJson(bean));

            boolean dataMissing = false;
            if (bean.getRegistration() == null) {
                addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.registration.required"),
                        model);
                dataMissing = true;
            }

            if (bean.getExecutionYear() == null) {
                addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.executionYear.required"),
                        model);
                dataMissing = true;
            }

            if (bean.getDebtDate() == null) {
                addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.debtDate.required"), model);
                dataMissing = true;
            }

            if (!bean.isRegistrationTuition() && bean.getEnrolment() == null) {
                addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.enrolment.required"),
                        model);
                dataMissing = true;
            }

            if (bean.getTuitionPaymentPlan() == null) {
                addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.tuitionPaymentPlan.required"),
                        model);
                dataMissing = true;
            }
            
            if (dataMissing) {
                return _createFirstPage(debtAccount, bean, model);
            }
            
            if(bean.isTuitionCharged()) {
                addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.tuition.registration.already.charged"), model);
                return _createFirstPage(debtAccount, bean, model);
            }

            if (bean.isRegistrationTuition()) {

                model.addAttribute("tuitionPaymentPlan", TuitionServices.usedPaymentPlan(bean.getRegistration(),
                        bean.getExecutionYear(), bean.getDebtDate(), bean.getTuitionPaymentPlan()));

                model.addAttribute("installments", TuitionServices.calculateInstallmentDebitEntryBeans(bean.getRegistration(),
                        bean.getExecutionYear(), bean.getDebtDate(), bean.getTuitionPaymentPlan(), true));
            } else if (bean.isStandaloneTuition()) {

                final AcademicTreasuryEvent event = TuitionServices
                        .findAcademicTreasuryEventTuitionForStandalone(bean.getRegistration(), bean.getExecutionYear());

                if (event != null && event.isChargedWithDebitEntry(bean.getEnrolment())) {
                    addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.event.is.charged"),
                            model);
                    return _createFirstPage(debtAccount, bean, model);
                }

                model.addAttribute("tuitionPaymentPlan", TuitionServices.usedPaymentPlanForStandalone(bean.getRegistration(),
                        bean.getExecutionYear(), bean.getEnrolment(), bean.getDebtDate(), bean.getTuitionPaymentPlan()));

                model.addAttribute("installments",
                        TuitionServices.calculateInstallmentDebitEntryBeansForStandalone(bean.getRegistration(),
                                bean.getExecutionYear(), bean.getDebtDate(), bean.getTuitionPaymentPlan(),
                                Sets.newHashSet(bean.getEnrolment())));
            } else if (bean.isExtracurricularTuition()) {
                final AcademicTreasuryEvent event = TuitionServices
                        .findAcademicTreasuryEventTuitionForExtracurricular(bean.getRegistration(), bean.getExecutionYear());

                if (event != null && event.isChargedWithDebitEntry(bean.getEnrolment())) {
                    addErrorMessage(academicTreasuryBundle("error.TuitionDebtCreationBean.event.is.charged"),
                            model);
                    return _createFirstPage(debtAccount, bean, model);
                }

                model.addAttribute("tuitionPaymentPlan",
                        TuitionServices.usedPaymentPlanForExtracurricular(bean.getRegistration(), bean.getExecutionYear(),
                                bean.getEnrolment(), bean.getDebtDate(), bean.getTuitionPaymentPlan()));

                model.addAttribute("installments",
                        TuitionServices.calculateInstallmentDebitEntryBeansForExtracurricular(bean.getRegistration(),
                                bean.getExecutionYear(), bean.getDebtDate(), bean.getTuitionPaymentPlan(),
                                Sets.newHashSet(bean.getEnrolment())));
            }

            return jspPage("confirmtuitiondebtcreation");
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _createFirstPage(debtAccount, bean, model);
        }
    }

    private static final String _CONFIRMTUITIONDEBTCREATION_URI = "/confirmtuitiondebtcreation";
    public static final String CONFIRMTUITIONDEBTCREATION_URL = CONTROLLER_URL + _CONFIRMTUITIONDEBTCREATION_URI;

    @RequestMapping(value = _CONFIRMTUITIONDEBTCREATION_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String confirmtuitiondebtcreation(@PathVariable("debtAccountId") final DebtAccount debtAccount,
            @RequestParam(value = "bean", required = false) final TuitionDebtCreationBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {
            boolean createdWithSuccess = false;
            if (bean.isRegistrationTuition()) {
                createdWithSuccess = TuitionServices.createTuitionForRegistration(bean.getRegistration(), bean.getExecutionYear(),
                        bean.getDebtDate(), true, bean.getTuitionPaymentPlan(), true);
            } else if (bean.isStandaloneTuition()) {
                createdWithSuccess = TuitionServices.createTuitionForStandalone(bean.getEnrolment(), bean.getTuitionPaymentPlan(),
                        bean.getDebtDate(), true);
            } else if (bean.isExtracurricularTuition()) {
                createdWithSuccess = TuitionServices.createTuitionForExtracurricular(bean.getEnrolment(),
                        bean.getTuitionPaymentPlan(), bean.getDebtDate(), true);
            }

            //Add the bean to be used in the View
            if (createdWithSuccess) {
                addInfoMessage(academicTreasuryBundle("label.TuitionPaymentPlan.tuition.installments.debit.entries.created.success"),
                        model);
                return redirect(DebtAccountController.READ_URL + "/" + debtAccount.getExternalId(), model, redirectAttributes);
            }

            addErrorMessage(academicTreasuryBundle("label.TuitionPaymentPlan.tuition.installments.debit.entries.created.insuccess"),
                    model);
        } catch (DomainException de) {
            addErrorMessage(de.getLocalizedMessage(), model);
        }

        return _createFirstPage(debtAccount, bean, model);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }
}
