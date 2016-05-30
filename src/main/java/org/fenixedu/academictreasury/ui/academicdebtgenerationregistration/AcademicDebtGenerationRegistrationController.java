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
package org.fenixedu.academictreasury.ui.academicdebtgenerationregistration;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.debtGeneration.AcademicDebtGenerationRule;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@BennuSpringController(value = CustomerController.class)
@RequestMapping(AcademicDebtGenerationRegistrationController.CONTROLLER_URL)
public class AcademicDebtGenerationRegistrationController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/academicdebtgenerationregistration/academicdebtgenerationregistration";
    private static final String JSP_PATH = "academicTreasury/academicdebtgenerationregistration/academicdebtgenerationregistration";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/create/";
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.GET)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount, final Model model) {
        return _createFirstPage(debtAccount, model);
    }

    private String _createFirstPage(final DebtAccount debtAccount, final Model model) {
        model.addAttribute("AcademicDebtGenerationRegistration_registration_options", ((PersonCustomer) debtAccount.getCustomer())
                .getPerson().getStudent().getRegistrationsSet());
        
        model.addAttribute("debtAccount", debtAccount);
        
        return jspPage("create");
    }

    @RequestMapping(value = _CREATE_URI + "/{debtAccountId}", method = RequestMethod.POST)
    public String create(@PathVariable("debtAccountId") final DebtAccount debtAccount, @RequestParam(value = "registrationId",
            required = false) final Registration registration, final Model model, final RedirectAttributes redirectAttributes) {

        try {

            AcademicDebtGenerationRule.runAllActiveForRegistration(registration, false);
            
            return redirect(DebtAccountController.READ_URL + debtAccount.getExternalId(), model, redirectAttributes);
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _createFirstPage(debtAccount, model);
        }
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }
}
