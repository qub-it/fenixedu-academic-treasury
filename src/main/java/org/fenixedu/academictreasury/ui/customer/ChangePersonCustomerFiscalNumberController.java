package org.fenixedu.academictreasury.ui.customer;

import static org.fenixedu.treasury.util.TreasuryConstants.isSameCountryCode;

import java.util.stream.Collectors;

import org.fenixedu.academic.domain.contacts.PhysicalAddress;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.dto.AdhocCustomerBean;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.FenixFramework;

@BennuSpringController(value = CustomerController.class)
@RequestMapping(ChangePersonCustomerFiscalNumberController.CONTROLLER_URI)
public class ChangePersonCustomerFiscalNumberController extends TreasuryBaseController {

    public static final String CONTROLLER_URI = "/academictreasury/accounting/managecustomer/changefiscalnumber";
    private static final String JSP_PATH = "/treasury/accounting/managecustomer/changefiscalnumber";

    private static final String CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI = "/changefiscalnumberactionconfirm";
    public static final String CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URL = CONTROLLER_URI + CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI;

    protected String getControllerURI() {
        return CONTROLLER_URI;
    }

    @RequestMapping(value = CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI + "/{oid}", method = RequestMethod.GET)
    public String changefiscalnumberactionconfirm(@PathVariable("oid") final PersonCustomer customer, final Model model) {
        assertUserIsBackOfficeMember(model);

        model.addAttribute("customer", customer);
        model.addAttribute("changeFiscalNumberActionFormURI", getControllerURI());

        if (customer.isFiscalValidated() && customer.isFiscalCodeValid()) {
            model.addAttribute("fiscalNumberValid", true);
        }

        return jspPage(CHANGE_FISCAL_NUMBER_ACTION_CONFIRM_URI);
    }

    private static final String CHANGE_FISCAL_NUMBER_FORM_URI = "/changefiscalnumberform";
    public static final String CHANGE_FISCAL_NUMBER_FORM_URL = CONTROLLER_URI + CHANGE_FISCAL_NUMBER_FORM_URI;

    @RequestMapping(value = CHANGE_FISCAL_NUMBER_FORM_URI + "/{oid}", method = RequestMethod.POST)
    public String changefiscalnumberform(@PathVariable("oid") final PersonCustomer customer, final Model model) {
        assertUserIsBackOfficeMember(model);

        final AdhocCustomerBean bean = new AdhocCustomerBean(customer);

        return _changefiscalnumberactionconfirm(customer, model, bean);
    }

    private String _changefiscalnumberactionconfirm(final PersonCustomer customer, final Model model,
            final AdhocCustomerBean bean) {

        model.addAttribute("person", customer.getAssociatedPerson());
        model.addAttribute("customer", customer);
        model.addAttribute("customerBeanJson", getBeanJson(bean));

        return "/academicTreasury/customer/changefiscalnumber/changefiscalnumberform";
    }

    private static final String CHANGE_FISCAL_NUMBER_URI = "/change";
    public static final String CHANGE_FISCAL_NUMBER_URL = CONTROLLER_URI + CHANGE_FISCAL_NUMBER_URI;

    @RequestMapping(value = CHANGE_FISCAL_NUMBER_URI + "/{oid}", method = RequestMethod.POST)
    public String change(@PathVariable("oid") final PersonCustomer customer, @RequestParam("bean") final AdhocCustomerBean bean,
            @RequestParam("fiscalAddressId") PhysicalAddress fiscalAddress, final Model model) {
        assertUserIsBackOfficeMember(model);

        try {

            if (!bean.isChangeFiscalNumberConfirmed()) {
                throw new TreasuryDomainException("message.Customer.changeFiscalNumber.confirmation");
            }

            customer.changeFiscalNumber(bean, fiscalAddress);

            return "redirect:" + CustomerController.READ_URL + customer.getExternalId();
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);

            return _changefiscalnumberactionconfirm(customer, model, bean);
        }
    }

    private static final String UPDATE_FISCAL_ADDRESS_URI = "/updateFiscalAddress";
    public static final String UPDATE_FISCAL_ADDRESS_URL = CONTROLLER_URI + UPDATE_FISCAL_ADDRESS_URI;

    @RequestMapping(value = UPDATE_FISCAL_ADDRESS_URI + "/{oid}", method = RequestMethod.GET)
    public String updatefiscaladdress(@PathVariable("oid") final PersonCustomer customer, final Model model) {
        assertUserIsBackOfficeMember(model);
        
        if(customer.isActive()) {
            throw new RuntimeException("customer is active");
        }
        
        model.addAttribute("person", customer.getAssociatedPerson());
        model.addAttribute("customer", customer);
        model.addAttribute("fiscalAddresses", customer.getAssociatedPerson()
                    .getValidAddressesForFiscalData()
                    .stream()
                    .filter(pa -> isSameCountryCode(customer.getAddressCountryCode(), pa.getCountryOfResidence().getCode()))
                    .collect(Collectors.toList()));
        
        return "/academicTreasury/customer/updatefiscaladdress";
        
    }
    
    @RequestMapping(value = UPDATE_FISCAL_ADDRESS_URI + "/{oid}", method = RequestMethod.POST)
    public String editfiscaladdresspost(@PathVariable("oid") final PersonCustomer customer, @RequestParam("fiscalAddressId") PhysicalAddress fiscalAddress, final Model model) {
        assertUserIsBackOfficeMember(model);
        
        if(customer.isActive()) {
            throw new RuntimeException("customer is active");
        }
        
        try {
            
            FenixFramework.atomic(() -> {
                customer.saveFiscalAddressFieldsInCustomer(fiscalAddress);
            });
            
            return "redirect:" + CustomerController.READ_URL + customer.getExternalId();
        } catch (final DomainException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            
            return updatefiscaladdress(customer, model);
        }
        
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
