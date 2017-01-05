package org.fenixedu.academictreasury.ui.customer;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.services.reports.DocumentPrinter;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.academictreasury.ui.customer.forwardpayments.CustomerAccountingForwardPaymentController;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.exemption.TreasuryExemption;
import org.fenixedu.treasury.domain.paymentcodes.FinantialDocumentPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.MultipleEntriesPaymentCode;
import org.fenixedu.treasury.domain.paymentcodes.PaymentCodeTarget;
import org.fenixedu.treasury.services.integration.erp.ERPExporterManager;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Sets;
import com.qubit.terra.docs.util.ReportGenerationException;

//@Component("org.fenixedu.treasury.ui.customer.viewAccounting") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.title.customer.viewAccount",
        accessGroup = "activeStudents")
@RequestMapping(CustomerAccountingController.CONTROLLER_URL)
public class CustomerAccountingController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/customer/viewaccount";

    private static final String JSP_PATH = "academicTreasury/customer/";

    private static final String READ_CUSTOMER_URI = "/customer/read/";
    private static final String READ_CUSTOMER_URL = CONTROLLER_URL + READ_CUSTOMER_URI;
    private static final String READ_ACCOUNT_URI = "/account/read/";
    public static final String READ_ACCOUNT_URL = CONTROLLER_URL + READ_ACCOUNT_URI;

    public String getReadCustomerUrl() {
        return CustomerAccountingController.READ_CUSTOMER_URL;
    }

    public String getReadAccountUrl() {
        return CustomerAccountingController.READ_ACCOUNT_URL;
    }

    protected String getForwardPaymentUrl(final DebtAccount debtAccount) {
        return FORWARD_PAYMENT_URL(debtAccount);
    }

    public static String FORWARD_PAYMENT_URL(final DebtAccount debtAccount) {
        return String.format(CONTROLLER_URL + "/read/%s/forwardpayment", debtAccount.getExternalId());
    }

    protected String getPrintSettlementNote() {
        return PRINT_SETTLEMENT_NOTE_URL;
    }

    @RequestMapping
    public String home(Model model) {
        return "forward:" + getReadCustomerUrl();
    }

    @RequestMapping(value = READ_CUSTOMER_URI)
    public String readCustomer(Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("readCustomerUrl", getReadCustomerUrl());
        model.addAttribute("readAccountUrl", getReadAccountUrl());

        Customer customer = Authenticate.getUser().getPerson().getPersonCustomer();
        model.addAttribute("customer", customer);

        if (customer == null) {
            return redirect(getCustomerNotCreatedUrl(), model, redirectAttributes);
        }

        if (customer.getDebtAccountsSet().size() == 1) {
            DebtAccount debtAccount = customer.getDebtAccountsSet().iterator().next();
            return redirect(getReadAccountUrl() + debtAccount.getExternalId(), model, redirectAttributes);
        }

        List<InvoiceEntry> pendingInvoiceEntries = new ArrayList<InvoiceEntry>();
        for (DebtAccount debtAccount : customer.getDebtAccountsSet()) {
            pendingInvoiceEntries.addAll(debtAccount.getPendingInvoiceEntriesSet());
        }
        model.addAttribute("pendingDocumentsDataSet", pendingInvoiceEntries);

        return jspPage("readCustomer");
    }

    @RequestMapping(value = READ_ACCOUNT_URI + "{oid}")
    public String readAccount(@PathVariable(value = "oid") final DebtAccount debtAccount, final Model model) {
        model.addAttribute("debtAccount", debtAccount);
        model.addAttribute("fowardPaymentUrl", getForwardPaymentUrl(debtAccount));
        model.addAttribute("printSettlementNoteUrl", getPrintSettlementNote());

        List<InvoiceEntry> allInvoiceEntries = new ArrayList<InvoiceEntry>();
        List<SettlementNote> paymentEntries = new ArrayList<SettlementNote>();
        List<TreasuryExemption> exemptionEntries = new ArrayList<TreasuryExemption>();
        List<InvoiceEntry> pendingInvoiceEntries = new ArrayList<InvoiceEntry>();

        allInvoiceEntries.addAll(debtAccount.getActiveInvoiceEntries().collect(Collectors.toList()));

        if (debtAccount.getCustomer().isActive() && debtAccount.getCustomer().isPersonCustomer()) {
            for (final PersonCustomer inactivePersonCustomer : ((PersonCustomer) debtAccount.getCustomer()).getPerson()
                    .getInactivePersonCustomersSet()) {
                if (inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()) == null) {
                    continue;
                }

                allInvoiceEntries.addAll(inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution())
                        .getActiveInvoiceEntries().collect(Collectors.toList()));
            }
        }

        paymentEntries = SettlementNote.findByDebtAccount(debtAccount).filter(x -> x.isClosed() || x.isPreparing())
                .filter(x -> !x.getPaymentEntriesSet().isEmpty() || !x.getReimbursementEntriesSet().isEmpty())
                .collect(Collectors.toList());

        if (debtAccount.getCustomer().isActive() && debtAccount.getCustomer().isPersonCustomer()) {
            for (final PersonCustomer inactivePersonCustomer : ((PersonCustomer) debtAccount.getCustomer()).getPerson()
                    .getInactivePersonCustomersSet()) {
                if (inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()) == null) {
                    continue;
                }

                paymentEntries
                        .addAll(SettlementNote
                                .findByDebtAccount(
                                        inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()))
                                .filter(x -> x.isClosed() || x.isPreparing())
                                .filter(x -> !x.getPaymentEntriesSet().isEmpty() || !x.getReimbursementEntriesSet().isEmpty())
                                .collect(Collectors.toList()));

            }
        }

        exemptionEntries.addAll(TreasuryExemption.findByDebtAccount(debtAccount).collect(Collectors.toList()));

        if (debtAccount.getCustomer().isActive() && debtAccount.getCustomer().isPersonCustomer()) {
            for (final PersonCustomer inactivePersonCustomer : ((PersonCustomer) debtAccount.getCustomer()).getPerson()
                    .getInactivePersonCustomersSet()) {
                if (inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()) == null) {
                    continue;
                }

                exemptionEntries.addAll(TreasuryExemption
                        .findByDebtAccount(inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()))
                        .collect(Collectors.toList()));
            }
        }

        for (InvoiceEntry entry : debtAccount.getPendingInvoiceEntriesSet()) {
            if (entry.getFinantialDocument() == null) {
                pendingInvoiceEntries.add(entry);
            } else {
                if (pendingInvoiceEntries.stream().anyMatch(
                        x -> x.getFinantialDocument() != null && x.getFinantialDocument().equals(entry.getFinantialDocument()))) {
                    //if there is any entry for this document, don't add
                } else {
                    pendingInvoiceEntries.add(entry);
                }
            }
        }

        if (debtAccount.getCustomer().isActive() && debtAccount.getCustomer().isPersonCustomer()) {
            for (final PersonCustomer inactivePersonCustomer : ((PersonCustomer) debtAccount.getCustomer()).getPerson()
                    .getInactivePersonCustomersSet()) {
                if (inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()) == null) {
                    continue;
                }

                for (InvoiceEntry entry : inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution())
                        .getPendingInvoiceEntriesSet()) {
                    if (entry.getFinantialDocument() == null) {
                        pendingInvoiceEntries.add(entry);
                    } else {
                        if (pendingInvoiceEntries.stream().anyMatch(x -> x.getFinantialDocument() != null
                                && x.getFinantialDocument().equals(entry.getFinantialDocument()))) {
                            //if there is any entry for this document, don't add
                        } else {
                            pendingInvoiceEntries.add(entry);
                        }
                    }
                }
            }
        }

        model.addAttribute("pendingDocumentsDataSet", pendingInvoiceEntries);
        model.addAttribute("allDocumentsDataSet", allInvoiceEntries);
        model.addAttribute("paymentsDataSet", paymentEntries);
        model.addAttribute("exemptionDataSet", exemptionEntries);

        final Set<PaymentCodeTarget> usedPaymentCodeTargets = Sets.newHashSet();
        for (final InvoiceEntry invoiceEntry : debtAccount.getPendingInvoiceEntriesSet()) {
            if (!invoiceEntry.isDebitNoteEntry()) {
                continue;
            }

            usedPaymentCodeTargets.addAll(
                    MultipleEntriesPaymentCode.findUsedByDebitEntry((DebitEntry) invoiceEntry).collect(Collectors.toSet()));

            if (invoiceEntry.getFinantialDocument() != null) {
                usedPaymentCodeTargets
                        .addAll(FinantialDocumentPaymentCode.findUsedByFinantialDocument(invoiceEntry.getFinantialDocument())
                                .collect(Collectors.<PaymentCodeTarget> toSet()));
            }
        }

        for (final PersonCustomer inactivePersonCustomer : ((PersonCustomer) debtAccount.getCustomer()).getPerson()
                .getInactivePersonCustomersSet()) {
            if (inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution()) == null) {
                continue;
            }

            if (!inactivePersonCustomer.getUiFiscalNumber()
                    .equals(((PersonCustomer) debtAccount.getCustomer()).getUiFiscalNumber())) {
                continue;
            }

            for (InvoiceEntry invoiceEntry : inactivePersonCustomer.getDebtAccountFor(debtAccount.getFinantialInstitution())
                    .getPendingInvoiceEntriesSet()) {
                if (!invoiceEntry.isDebitNoteEntry()) {
                    continue;
                }

                usedPaymentCodeTargets.addAll(
                        MultipleEntriesPaymentCode.findUsedByDebitEntry((DebitEntry) invoiceEntry).collect(Collectors.toSet()));

                if (invoiceEntry.getFinantialDocument() != null) {
                    usedPaymentCodeTargets
                            .addAll(FinantialDocumentPaymentCode.findUsedByFinantialDocument(invoiceEntry.getFinantialDocument())
                                    .collect(Collectors.<PaymentCodeTarget> toSet()));
                }
            }
        }

        model.addAttribute("usedPaymentCodeTargets", usedPaymentCodeTargets);

        return jspPage("readDebtAccount");
    }

    @RequestMapping(value = "/read/{oid}/forwardpayment")
    public String processReadToForwardPayment(@PathVariable("oid") DebtAccount debtAccount, final Model model,
            final RedirectAttributes redirectAttributes) {
        return redirect(CustomerAccountingForwardPaymentController.CHOOSE_INVOICE_ENTRIES_URL + debtAccount.getExternalId(),
                model, redirectAttributes);
    }

    private static final String CUSTOMER_NOT_CREATED_URI = "/customernotcreated";
    public static final String CUSTOMER_NOT_CREATED_URL = CONTROLLER_URL + CUSTOMER_NOT_CREATED_URI;

    protected String getCustomerNotCreatedUrl() {
        return CUSTOMER_NOT_CREATED_URL;
    }

    @RequestMapping(value = CUSTOMER_NOT_CREATED_URI)
    public String customernotcreated(final Model model) {
        return jspPage("customernotcreated");
    }

    private static final String PRINT_SETTLEMENT_NOTE_URI = "/printsettlementnote";
    public static final String PRINT_SETTLEMENT_NOTE_URL = CONTROLLER_URL + PRINT_SETTLEMENT_NOTE_URI;

    @RequestMapping(value = PRINT_SETTLEMENT_NOTE_URI + "/{settlementNoteId}", produces = "application/pdf")
    @ResponseBody
    public Object printsettlementnote(@PathVariable("settlementNoteId") final SettlementNote settlementNote, final Model model,
            final RedirectAttributes redirectAttributes) {
        try {
            byte[] report = org.fenixedu.treasury.services.reports.DocumentPrinter.printFinantialDocument(settlementNote,
                    DocumentPrinter.PDF);
            return new ResponseEntity<byte[]>(report, HttpStatus.OK);
        } catch (ReportGenerationException rex) {
            addErrorMessage(rex.getLocalizedMessage(), model);
            addErrorMessage(rex.getCause().getLocalizedMessage(), model);

            return redirect(getReadAccountUrl() + settlementNote.getDebtAccount().getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);

            return redirect(getReadAccountUrl() + settlementNote.getDebtAccount().getExternalId(), model, redirectAttributes);
        }
    }

    private static final String PRINT_PAYMENT_REFERENCES_URI = "/printpaymentreferences";
    public static final String PRINT_PAYMENT_REFERENCES_URL = CONTROLLER_URL + PRINT_PAYMENT_REFERENCES_URI;

    @RequestMapping(value = PRINT_PAYMENT_REFERENCES_URI + "/{oid}", produces = "application/pdf")
    @ResponseBody
    public Object printpaymentreferences(@PathVariable("oid") DebtAccount debtAccount, final Model model,
            final RedirectAttributes redirectAttributes, final HttpServletResponse response) {

        try {
            response.addHeader("Content-Disposition",
                    "attachment; filename=referencias_" + new DateTime().toString("yyyyMMddHHmmss") + ".pdf");

            final byte[] tuitionPlanbytes =
                    DocumentPrinter.printRegistrationTuititionPaymentPlan(debtAccount, DocumentPrinter.PDF);
            return new ResponseEntity<byte[]>(tuitionPlanbytes, HttpStatus.OK);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);

            return redirect(getReadAccountUrl() + debtAccount.getExternalId(), model, redirectAttributes);
        }
    }

    private static final String _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI = "/downloadcertifieddocumentprint";
    public static final String DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URL = CONTROLLER_URL + _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI;

    @RequestMapping(value = _DOWNLOAD_CERTIFIED_DOCUMENT_PRINT_URI + "/{oid}", method = RequestMethod.GET)
    public String downloadcertifieddocumentprint(@PathVariable("oid") final FinantialDocument finantialDocument,
            final Model model, final RedirectAttributes redirectAttributes, final HttpServletResponse response) {

        try {
            final byte[] contents = ERPExporterManager.downloadCertifiedDocumentPrint(finantialDocument);

            response.setContentType("application/pdf");
            String filename = URLEncoder.encode(StringNormalizer.normalizePreservingCapitalizedLetters(
                    (finantialDocument.getDebtAccount().getFinantialInstitution().getFiscalNumber() + "_"
                            + finantialDocument.getUiDocumentNumber() + ".pdf").replaceAll("/", "_").replaceAll("\\s", "_")
                                    .replaceAll(" ", "_")),
                    "Windows-1252");

            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(contents);

            return null;
        } catch (final TreasuryDomainException | IOException e) {
            addErrorMessage(e.getLocalizedMessage(), model);
            return readAccount(finantialDocument.getDebtAccount(), model);
        }
    }

    public String jspPage(final String page) {
        return JSP_PATH + page;
    }
}
