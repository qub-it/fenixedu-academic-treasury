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
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.paymentcodes.PaymentCodeTarget;
import org.fenixedu.treasury.services.integration.erp.ERPExternalServiceImplementation.SAPExternalService;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
        activateSeries();

        // Configure ERP tuition info exportation
        configureERPTuitionInfoExportation();

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
    }

    private void moveAcademicTreasuryEventsFromDebtAccountToPerson() {
        taskLog("moveAcademicTreasuryEventsFromDebtAccountToPerson");

        int count = 0;
        final long totalCount = AcademicTreasuryEvent.findAll().count();
        for (AcademicTreasuryEvent academicTreasuryEvent : AcademicTreasuryEvent.findAll()
                .collect(Collectors.<AcademicTreasuryEvent> toList())) {
            if (++count % 1000 == 0) {
                taskLog("Processing " + count + "/" + totalCount + " parties.");
            }

            DebtAccount debtAccount = academicTreasuryEvent.getDebtAccount();
            if (debtAccount != null) {
                final Person person = ((PersonCustomer) debtAccount.getCustomer()).getPerson();
                academicTreasuryEvent.setPerson(person);
            }
        }
    }

    private void saveFiscalInformationAndDeleteMergedCustomers() {
        taskLog("saveFiscalInformationAndDeleteMergedCustomers");

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

            if (person.getPersonCustomer() == null) {
                continue;
            }

            final PersonCustomer personCustomer = person.getPersonCustomer();

            if (personCustomer.getDebtAccountsSet().isEmpty()) {
                continue;
            }

            if (personCustomer.getDebtAccountsSet().size() > 1) {
                throw new RuntimeException("how to handle it");
            }

            final DebtAccount debtAccount = personCustomer.getDebtAccountsSet().iterator().next();

            for (final PersonCustomer ipc : person.getInactivePersonCustomersSet()) {
                if (ipc.getDebtAccountsSet().size() > 1) {
                    throw new RuntimeException("how to handle it");
                }

                if (ipc.getDebtAccountsSet().isEmpty()) {
                    continue;
                }

                if (!ipc.getDebtAccountsSet().isEmpty()) {

                    taskLog("Merging %s\n", personCustomer.getName());
                    
                    final DebtAccount ipcDebtAccount = ipc.getDebtAccountsSet().iterator().next();

                    while (!ipcDebtAccount.getInvoiceSet().isEmpty()) {
                        final Invoice invoice = ipcDebtAccount.getInvoiceSet().iterator().next();
                        invoice.setDebtAccount(debtAccount);
                    }

                    while (!ipcDebtAccount.getInvoiceEntrySet().isEmpty()) {
                        InvoiceEntry invoiceEntry = ipcDebtAccount.getInvoiceEntrySet().iterator().next();
                        invoiceEntry.setDebtAccount(debtAccount);
                    }

                    while (!ipcDebtAccount.getFinantialDocumentsSet().isEmpty()) {
                        FinantialDocument finantialDocument = ipcDebtAccount.getFinantialDocumentsSet().iterator().next();
                        finantialDocument.setDebtAccount(debtAccount);
                    }

                    while (!ipcDebtAccount.getPaymentCodeTargetsSet().isEmpty()) {
                        PaymentCodeTarget target = ipcDebtAccount.getPaymentCodeTargetsSet().iterator().next();
                        target.setDebtAccount(debtAccount);
                    }

                    while (!ipcDebtAccount.getForwardPaymentsSet().isEmpty()) {
                        ForwardPayment payment = ipcDebtAccount.getForwardPaymentsSet().iterator().next();
                        payment.setDebtAccount(debtAccount);
                    }

                    while (!ipcDebtAccount.getDebitEntriesSet().isEmpty()) {
                        DebitEntry entry = ipcDebtAccount.getDebitEntriesSet().iterator().next();
                        entry.setDebtAccount(debtAccount);
                    }

                    while (!ipcDebtAccount.getTreasuryEventsSet().isEmpty()) {
                        TreasuryEvent treasuryEvent = ipcDebtAccount.getTreasuryEventsSet().iterator().next();
                        treasuryEvent.setDebtAccount(debtAccount);
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
                    taskLog("%s - %s without fiscal information\n", person.getExternalId(), person.getName());
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

    private void activateSeries() {
        taskLog("activateSeries");

        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().findFirst().get();
        Series.find(finantialInstitution).stream().filter(s -> Lists.newArrayList("INT", "LEG").contains(s.getCode()))
                .forEach(s -> s.setSelectable(true));
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
