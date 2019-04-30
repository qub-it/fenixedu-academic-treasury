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
package org.fenixedu.academictreasury.ui.manageacademictax;

import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import java.util.stream.Collectors;

import org.fenixedu.academictreasury.domain.emoluments.AcademicTax;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.settings.AcademicTreasurySettings;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.Product;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.title.manageAcademicTax",
        accessGroup = "treasuryBackOffice")
@RequestMapping(AcademicTaxController.CONTROLLER_URL)
public class AcademicTaxController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/manageacademictax/academictax";

    private static final String JSP_PATH = "academicTreasury/manageacademictax/academictax";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + SEARCH_URL;
    }

    private AcademicTax getAcademicTax(Model model) {
        return (AcademicTax) model.asMap().get("academicTax");
    }

    private void setAcademicTax(AcademicTax academicTax, Model model) {
        model.addAttribute("academicTax", academicTax);
    }

    @Atomic
    public void deleteAcademicTax(AcademicTax academicTax) {
        academicTax.delete();
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(Model model) {
        model.addAttribute("searchacademictaxResultsDataSet", AcademicTax.findAll().sorted(AcademicTax.COMPARE_BY_PRODUCT_NAME)
                .collect(Collectors.toList()));

        return jspPage("search");
    }

    private static final String _SEARCH_TO_DELETE_ACTION_URI = "/search/delete/";
    public static final String SEARCH_TO_DELETE_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_DELETE_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_DELETE_ACTION_URI + "{academicTaxId}", method = RequestMethod.POST)
    public String processSearchToDeleteAction(@PathVariable("academicTaxId") AcademicTax academicTax, Model model,
            RedirectAttributes redirectAttributes) {
        processDelete(academicTax, model, redirectAttributes);
        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{academicTaxId}", method = RequestMethod.POST)
    public String delete(@PathVariable("academicTaxId") AcademicTax academicTax, Model model,
            RedirectAttributes redirectAttributes) {
        boolean success = processDelete(academicTax, model, redirectAttributes);
        if (success) {
            return redirect(SEARCH_URL, model, redirectAttributes);
        }
        return jspPage("read");
    }

    private boolean processDelete(AcademicTax academicTax, Model model, RedirectAttributes redirectAttributes) {
        setAcademicTax(academicTax, model);
        try {
            //call the Atomic delete function
            deleteAcademicTax(academicTax);

            addInfoMessage(academicTreasuryBundle("label.success.create"), model);
            return true;
        } catch (AcademicTreasuryDomainException ex) {
            addErrorMessage(academicTreasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(academicTreasuryBundle("label.error.delete") + ex.getLocalizedMessage(), model);
        }
        return false;
    }

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        model.addAttribute("AcademicTax_product_options", AcademicTreasurySettings.getInstance().getEmolumentsProductGroup()
                .getProductsSet().stream().sorted(Product.COMPARE_BY_NAME).collect(Collectors.toList()));

        return jspPage("create");
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(
            @RequestParam(value = "product", required = false) final Product product,
            @RequestParam(value = "appliedonregistration", required = false) final boolean appliedOnRegistration,
            @RequestParam(value = "appliedonregistrationfirstyear", required = false) final boolean appliedOnRegistrationFirstYear,
            @RequestParam(value = "appliedonregistrationsubsequentyears", required = false) final boolean appliedOnRegistrationSubsequentYears,
            @RequestParam(value = "appliedautomatically", required = false) final boolean appliedAutomatically,
            final Model model, final RedirectAttributes redirectAttributes) {
        try {

            final AcademicTax academicTax =
                    AcademicTax.create(product, appliedOnRegistration, appliedOnRegistrationFirstYear,
                            appliedOnRegistrationSubsequentYears, appliedAutomatically);

            return redirect(READ_URL + academicTax.getExternalId(), model, redirectAttributes);
        } catch (AcademicTreasuryDomainException ex) {
            addErrorMessage(academicTreasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(academicTreasuryBundle("label.error.create") + ex.getLocalizedMessage(), model);
        }
        return create(model);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") final AcademicTax academicTax, Model model) {
        setAcademicTax(academicTax, model);
        return jspPage("read");
    }

    private static final String _UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + _UPDATE_URI;

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") final AcademicTax academicTax, Model model) {
        model.addAttribute("academicTax", academicTax);

        return jspPage("update");
    }

    @RequestMapping(value = _UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") AcademicTax academicTax, @RequestParam(value = "appliedonregistration",
            required = false) boolean appliedOnRegistration, @RequestParam(value = "appliedonregistrationfirstyear",
            required = false) boolean appliedOnRegistrationFirstYear, @RequestParam(
            value = "appliedonregistrationsubsequentyears", required = false) boolean appliedOnRegistrationSubsequentYears,
            @RequestParam(value = "appliedautomatically", required = false) final boolean appliedAutomatically, Model model,
            RedirectAttributes redirectAttributes) {

        try {

            academicTax.edit(appliedOnRegistration, appliedOnRegistrationFirstYear, appliedOnRegistrationSubsequentYears,
                    appliedAutomatically);

            return redirect(READ_URL + academicTax.getExternalId(), model, redirectAttributes);
        } catch (AcademicTreasuryDomainException ex) {
            addErrorMessage(academicTreasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(academicTreasuryBundle("label.error.update") + ex.getLocalizedMessage(), model);
        }
        return update(academicTax, model);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
