package org.fenixedu.academictreasury.scripts;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.paymentcodes.PaymentCodeTarget;

public class SaveFiscalInformationAndDeleteMergedCustomers extends CustomTask {

    @Override
    public void runTask() throws Exception {
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

            if(personCustomer.getDebtAccountsSet().isEmpty()) {
                continue;
            }
            
            if (personCustomer.getDebtAccountsSet().size() > 1) {
                throw new RuntimeException("how to handle it");
            }

            final DebtAccount debtAccount = personCustomer.getDebtAccountsSet().iterator().next();

            for (final PersonCustomer ipc : person.getInactivePersonCustomersSet()) {
                if(ipc.getDebtAccountsSet().isEmpty()) {
                    continue;
                }
                
                if (ipc.getDebtAccountsSet().size() > 1) {
                    throw new RuntimeException("how to handle it");
                }

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
                
                while(!ipcDebtAccount.getDebitEntriesSet().isEmpty()) {
                    DebitEntry entry = ipcDebtAccount.getDebitEntriesSet().iterator().next();
                    entry.setDebtAccount(debtAccount);
                }
                
                ipcDebtAccount.delete();
                
                ipc.delete();
            }
            
            personCustomer.setCountryCode(PersonCustomer.countryCode(person));
            personCustomer.setFiscalNumber(PersonCustomer.fiscalNumber(person));
        }

        taskLog("Finish");
        System.out.println("TreasuryAcademicBoot - Finished Validating Students and Customers DebtAccount");

    }

}
