package org.fenixedu.academictreasury.scripts;

import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Country;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.PhysicalAddress;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.domain.organizationalStructure.PartySocialSecurityNumber;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoSettings;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.AdhocCustomer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.ProductGroup;
import org.fenixedu.treasury.domain.VatExemptionReason;
import org.fenixedu.treasury.domain.VatType;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.AdvancedPaymentCreditNote;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.document.reimbursement.ReimbursementProcessStatusType;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.paymentcodes.PaymentCodeTarget;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation.SAPExternalService;
import org.fenixedu.treasury.util.Constants;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qubit.solution.fenixedu.bennu.webservices.domain.webservice.WebServiceClientConfiguration;

/*
 * This script aims to prepare instance for SAP integration
 */
public class PrepareTreasuryForSAP extends CustomTask {

    @Override
    public void runTask() throws Exception {

        // Create webservice client for SAP
        createWebserviceForSAP();

        // Configure ERP configuration for Finantial Institution
        configureERPInFinantialInstitution();

        // Make available internal and legacy document series
        activateSeriesAndCreateNewForRegulation();

        // Configure ERP tuition info exportation
        configureERPTuitionInfoExportation();

        // Create reimbursements process state types
        createReimbursementStatusTypes();

        // Fill fiscal country on persons
        saveFiscalCountryOnPersons();

        // Fill fiscal country for adhoc customers
        saveAddressCountryOnAdhocCustomers();

        // For all finantial documents exported set close date and mark as erp legacy
        saveCloseDateForFinantialDocumentsClosed();

        // Mark documents exported in legacy ERP
        markDocumentsExportedInLegacyERP();

        // Fill person on academic treasury events
        moveAcademicTreasuryEventsFromDebtAccountToPerson();

        // Save fiscal information in person customer and delete merged customers
        saveFiscalInformationAndDeleteMergedCustomers();

        // Create product for credits imported from SIGES and replace series with regulation
        replaceProductAndSeriesForCreditsImportedFromSIGES();

        checkAllPersonCustomersWithFiscalCountryAndNumber();

        taskLog("End");

    }

    private void checkAllPersonCustomersWithFiscalCountryAndNumber() {
        taskLog("checkAllPersonCustomersWithFiscalCountryAndNumber");

        for (final Customer c : Customer.findAll().collect(Collectors.<Customer> toSet())) {
            if (Strings.isNullOrEmpty(c.getFiscalCountry()) || Strings.isNullOrEmpty(c.getFiscalNumber())) {
                throw new RuntimeException("without fiscal information: " + c.getName());
            }
        }
    }

    public void createReimbursementStatusTypes() throws Exception {
        taskLog("createReimbursementStatusTypes");

        ReimbursementProcessStatusType.create("PENDING", "Reembolso pendente", 1, true, false, false);
        ReimbursementProcessStatusType.create("ANNULED", "Reembolso anulado", 2, false, true, true);
        ReimbursementProcessStatusType.create("CONCLUDED", "Reembolso concluído", 3, false, true, false);
    }

    private void replaceProductAndSeriesForCreditsImportedFromSIGES() {
        taskLog("replaceProductAndSeriesForCreditsImportedFromSIGES");

        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().iterator().next();
        if (!Product.findUniqueByCode("CREDITO_SIGES").isPresent()) {

            final LocalizedString productName = new LocalizedString(Constants.DEFAULT_LANGUAGE, "Crédito SIGES");
            final LocalizedString unitDescription = new LocalizedString(Constants.DEFAULT_LANGUAGE, "Unidade");
            Product.create(ProductGroup.findByCode("OTHER"), "CREDITO_SIGES",
                    new LocalizedString(Constants.DEFAULT_LANGUAGE, "Crédito SIGES"), unitDescription, true, false, 0,
                    VatType.findByCode("ISE"), Lists.newArrayList(finantialInstitution), VatExemptionReason.findByCode("M07"));
        }

        final Product product = Product.findUniqueByCode("CREDITO_SIGES").get();
        final Series regulationSeries = Series.findByCode(finantialInstitution, "REG");
        DocumentNumberSeries documentNumberSeries =
                DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), regulationSeries);

        for (final CreditEntry creditEntry : CreditEntry.findAll().collect(Collectors.toSet())) {
            if (creditEntry.getFinantialDocument() == null) {
                continue;
            }

            final CreditNote creditNote = (CreditNote) creditEntry.getFinantialDocument();

            if (creditNote instanceof AdvancedPaymentCreditNote) {
                continue;
            }

            if (Constants.isZero(creditNote.getOpenAmount())) {
                continue;
            }

            if (creditEntry.getProduct() != TreasurySettings.getInstance().getAdvancePaymentProduct()) {
                continue;
            }

            if (!creditNote.isPreparing()) {
                continue;
            }

            taskLog("Change in  [%s - %s]: %s\n", creditEntry.getDebtAccount().getCustomer().getFiscalNumber(),
                    creditEntry.getDebtAccount().getCustomer().getName(),
                    creditEntry.getFinantialDocument().getUiDocumentNumber());

            creditEntry.setProduct(product);
            creditNote.setDocumentNumberSeries(documentNumberSeries);
        }
    }

    private void moveAcademicTreasuryEventsFromDebtAccountToPerson() {
        taskLog("moveAcademicTreasuryEventsFromDebtAccountToPerson");

        int count = 0;
        final long totalCount = AcademicTreasuryEvent.findAll().count();
        for (AcademicTreasuryEvent academicTreasuryEvent : AcademicTreasuryEvent.findAll()
                .collect(Collectors.<AcademicTreasuryEvent> toList())) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " treasury events.");
            }

            DebtAccount debtAccount = academicTreasuryEvent.getDebtAccount();
            if (debtAccount != null) {
                PersonCustomer customer = (PersonCustomer) debtAccount.getCustomer();
                final Person person = customer.isActive() ? customer.getPerson() : customer.getPersonForInactivePersonCustomer();
                academicTreasuryEvent.setPerson(person);
            }
        }
    }

    private void saveFiscalInformationAndDeleteMergedCustomers() {
        taskLog("saveFiscalInformationAndDeleteMergedCustomers");

        int count = 0;
        int totalCount = Bennu.getInstance().getPartysSet().size();
        for (Party party : Bennu.getInstance().getPartysSet()) {
            if (++count % 100 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " parties.");
            }

            if (!party.isPerson()) {
                continue;
            }

            final Person person = (Person) party;

            if(person.getPersonCustomer() == null && !person.getInactivePersonCustomersSet().isEmpty()) {
                throw new RuntimeException("how to handle it");
            }
            
            if (person.getPersonCustomer() == null) {
                continue;
            }

            final PersonCustomer personCustomer = person.getPersonCustomer();
            if (personCustomer.getDebtAccountsSet().size() > 1) {
                throw new RuntimeException("how to handle it");
            }

            final DebtAccount debtAccount =
                    !personCustomer.getDebtAccountsSet().isEmpty() ? personCustomer.getDebtAccountsSet().iterator().next() : null;
            for (final PersonCustomer ipc : person.getInactivePersonCustomersSet()) {
                if (ipc.getDebtAccountsSet().size() > 1) {
                    throw new RuntimeException("how to handle it");
                }
                
                if (!ipc.getDebtAccountsSet().isEmpty()) {
                    final DebtAccount ipcDebtAccount = ipc.getDebtAccountsSet().iterator().next();

                    if (debtAccount != null) {
                        taskLog("Merging %s\n", personCustomer.getName());

                        for (final Invoice invoice : Sets.newHashSet(ipcDebtAccount.getInvoiceSet())) {
                            invoice.setPayorDebtAccount(debtAccount);
                        }

                        for (final InvoiceEntry invoiceEntry : Sets.newHashSet(ipcDebtAccount.getInvoiceEntrySet())) {
                            invoiceEntry.setDebtAccount(debtAccount);
                        }

                        for (final FinantialDocument finantialDocument : Sets
                                .newHashSet(ipcDebtAccount.getFinantialDocumentsSet())) {
                            finantialDocument.setDebtAccount(debtAccount);
                        }

                        for (final PaymentCodeTarget target : Sets.newHashSet(ipcDebtAccount.getPaymentCodeTargetsSet())) {
                            target.setDebtAccount(debtAccount);
                        }

                        for (final ForwardPayment payment : ipcDebtAccount.getForwardPaymentsSet()) {
                            payment.setDebtAccount(debtAccount);
                        }

                        for (final DebitEntry entry : ipcDebtAccount.getDebitEntriesSet()) {
                            entry.setDebtAccount(debtAccount);
                        }

                        for (final TreasuryEvent treasuryEvent : ipcDebtAccount.getTreasuryEventsSet()) {
                            treasuryEvent.setDebtAccount(debtAccount);
                        }

                    }

                    ipcDebtAccount.delete();
                }

                ipc.delete();
            }

            if (Strings.isNullOrEmpty(PersonCustomer.countryCode(person))) {
                throw new RuntimeException("error");
            }

            if (Strings.isNullOrEmpty(PersonCustomer.fiscalNumber(person))) {
                throw new RuntimeException("error");
            }

            personCustomer.setCountryCode(PersonCustomer.countryCode(person));
            personCustomer.setFiscalNumber(PersonCustomer.fiscalNumber(person));
        }

        taskLog("Finish");
        System.out.println("TreasuryAcademicBoot - Finished Validating Students and Customers DebtAccount");
    }

    private void markDocumentsExportedInLegacyERP() {
        taskLog("markDocumentsExportedInLegacyERP");

        int count = 0;
        long totalCount = FinantialDocument.findAll().count();

        for (final FinantialDocument doc : FinantialDocument.findAll().collect(Collectors.<FinantialDocument> toSet())) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " finantial documents.");
            }

            if (doc.isPreparing()) {
                continue;
            }

            if (doc.isAnnulled()) {
                doc.setExportedInLegacyERP(true);
                continue;
            }

            if (doc.isClosed() && !doc.isDocumentToExport()) {
                doc.setExportedInLegacyERP(true);
                continue;
            }
        }
    }

    private void saveCloseDateForFinantialDocumentsClosed() {
        taskLog("saveCloseDateForFinantialDocumentsClosed");

        int count = 0;
        long totalCount = FinantialDocument.findAll().count();

        for (final FinantialDocument doc : FinantialDocument.findAll().collect(Collectors.<FinantialDocument> toSet())) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " finantial documents.");
            }

            if (!doc.isClosed()) {
                continue;
            }

            if (doc.getCloseDate() != null) {
                continue;
            }

            doc.setCloseDate(new DateTime().minusDays(1));
        }
    }

    private void saveAddressCountryOnAdhocCustomers() {
        taskLog("saveAddressCountryOnAdhocCustomers");

        int count = 0;
        long totalCount = AdhocCustomer.findAll().count();

        for (final AdhocCustomer customer : AdhocCustomer.findAll().collect(Collectors.toList())) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " adhoc customers.");
            }
            
            if(Strings.isNullOrEmpty(customer.getFiscalCountry())) {
                customer.setCountryCode(Constants.DEFAULT_COUNTRY);
            }

            customer.setAddressCountryCode(customer.getFiscalCountry());
        }
    }

    private void saveFiscalCountryOnPersons() {
        taskLog("saveFiscalCountryOnPersons");

        int count = 0;
        int totalCount = Bennu.getInstance().getPartysSet().size();
        for (Party party : Bennu.getInstance().getPartysSet()) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " parties.");
            }

            if (!party.isPerson()) {
                continue;
            }

            final Person person = (Person) party;

            // If person does not have fiscal number throw error if it has customer
            if (Strings.isNullOrEmpty(PersonCustomer.fiscalNumber(person))) {
                if (PersonCustomer.find(person).count() > 0) {
                    // taskLog("%s - %s without fiscal information\n", person.getExternalId(), person.getName());
                }

                editFiscalInformation(person, Country.readDefault(), PersonCustomer.DEFAULT_FISCAL_NUMBER);
                continue;
            }

            // If it is default fill with default country
            if (PersonCustomer.DEFAULT_FISCAL_NUMBER.equals(PersonCustomer.fiscalNumber(person))) {
                editFiscalInformation(person, Country.readDefault(), PersonCustomer.fiscalNumber(person));
                continue;
            }

            // If it is valid of fiscal number for default country fill with default country
            if (FiscalCodeValidation.isValidFiscalNumber(Country.readDefault().getCode(), PersonCustomer.fiscalNumber(person))) {
                editFiscalInformation(person, Country.readDefault(), PersonCustomer.fiscalNumber(person));
                continue;
            }

            // Relay on address country
            final PhysicalAddress physicalAddress = PersonCustomer.physicalAddress(person);
            if (physicalAddress != null && physicalAddress.getCountryOfResidence() != null) {
                editFiscalInformation(person, physicalAddress.getCountryOfResidence(), PersonCustomer.fiscalNumber(person));
                continue;
            }

            if (person.getFiscalCountry() == null) {
                // Fill with default country
                editFiscalInformation(person, Country.readDefault(), PersonCustomer.fiscalNumber(person));
            }

        }
    }

    private void configureERPTuitionInfoExportation() {
        taskLog("configureERPTuitionInfoExportation");

        final ERPTuitionInfoSettings settings = ERPTuitionInfoSettings.getInstance();
        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().findFirst().get();

        final Series series = Series.create(finantialInstitution, "PRO",
                new LocalizedString(I18N.getLocale(), "Especialização de Propinas"), false, true, false, false, false);
        settings.edit(series, "NG", "NJ");
    }

    private void activateSeriesAndCreateNewForRegulation() {
        taskLog("activateSeries");

        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().findFirst().get();
        Series.find(finantialInstitution).stream().filter(s -> Lists.newArrayList("INT", "LEG").contains(s.getCode()))
                .forEach(s -> s.setSelectable(true));

        if (Series.findByCode(finantialInstitution, "REG") == null) {
            final Series regulationSeries = Series.create(finantialInstitution, "REG",
                    new LocalizedString(Constants.DEFAULT_LANGUAGE, "Regularização"), false, true, false, false, false);

            finantialInstitution.setRegulationSeries(regulationSeries);

            DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), regulationSeries).editReplacingPrefix(true, "NY");
            DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), regulationSeries).editReplacingPrefix(true,
                    "NZ");
        }
    }

    private void configureERPInFinantialInstitution() {
        taskLog("configureERPInFinantialInstitution");

        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().findFirst().get();

        final ERPConfiguration erpConfiguration = finantialInstitution.getErpIntegrationConfiguration();

        erpConfiguration.setErpIdProcess("006");
        erpConfiguration.setImplementationClassName(SAPExternalService.class.getName());
    }

    private void createWebserviceForSAP() {
        taskLog("createWebserviceForSAP");

        new WebServiceClientConfiguration(SAPExternalService.class.getName());
    }

    private void editFiscalInformation(final Party party, final Country fiscalCountry, final String socialSecurityNumber) {
        PartySocialSecurityNumber partySocialSecurityNumber = party.getPartySocialSecurityNumber();

        if (partySocialSecurityNumber == null) {
            partySocialSecurityNumber = new PartySocialSecurityNumber();
            partySocialSecurityNumber.setParty(party);
        }

        partySocialSecurityNumber.setFiscalCountry(fiscalCountry);
        partySocialSecurityNumber.setSocialSecurityNumber(socialSecurityNumber);
    }
}
