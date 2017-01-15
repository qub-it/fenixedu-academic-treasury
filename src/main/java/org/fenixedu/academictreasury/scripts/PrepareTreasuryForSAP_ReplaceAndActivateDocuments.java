package org.fenixedu.academictreasury.scripts;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.ProductGroup;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.VatExemptionReason;
import org.fenixedu.treasury.domain.VatType;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.AdvancedPaymentCreditNote;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.CreditNote;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.document.DebitNote;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.Series;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class PrepareTreasuryForSAP_ReplaceAndActivateDocuments extends CustomTask {

    @Override
    public void runTask() throws Exception {

        // Create product for credits imported from SIGES and replace series with regulation
        replaceProductAndSeriesForCreditsImportedFromSIGES();

        // Replace advance payment credits of 2016
        replacePendingAdvancePaymentCredits();

    }

    private void replaceProductAndSeriesForCreditsImportedFromSIGES() {
        taskLog("replaceProductAndSeriesForCreditsImportedFromSIGES");

        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().iterator().next();
        if (!Product.findUniqueByCode("CREDITO").isPresent()) {

            final LocalizedString productName = new LocalizedString(Constants.DEFAULT_LANGUAGE, "Crédito");
            final LocalizedString unitDescription = new LocalizedString(Constants.DEFAULT_LANGUAGE, "Unidade");
            Product.create(ProductGroup.findByCode("OTHER"), "CREDITO", productName, unitDescription, true, false, 0,
                    VatType.findByCode("ISE"), Lists.newArrayList(finantialInstitution), VatExemptionReason.findByCode("M07"));
        }

        final Product product = Product.findUniqueByCode("CREDITO").get();
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

    private void replacePendingAdvancePaymentCredits() {
        taskLog("replacePendingAdvancePaymentCredits");

        final FinantialInstitution finantialInstitution = FinantialInstitution.findAll().iterator().next();
        if (!Product.findUniqueByCode("CREDITO_PAGAMENTO").isPresent()) {

            final LocalizedString productName = new LocalizedString(Constants.DEFAULT_LANGUAGE, "Crédito de Adiantamento");
            final LocalizedString unitDescription = new LocalizedString(Constants.DEFAULT_LANGUAGE, "Unidade");
            Product.create(ProductGroup.findByCode("OTHER"), "CREDITO_PAGAMENTO", productName, unitDescription, true, false, 0,
                    VatType.findByCode("ISE"), Lists.newArrayList(finantialInstitution), VatExemptionReason.findByCode("M07"));
        }

        final Product product = Product.findUniqueByCode("CREDITO_PAGAMENTO").get();
        final Series regulationSeries = Series.findByCode(finantialInstitution, "REG");

        for (final CreditEntry creditEntry : CreditEntry.findAll().collect(Collectors.toSet())) {
            if (creditEntry.getFinantialDocument() == null) {
                continue;
            }

            final CreditNote creditNote = (CreditNote) creditEntry.getFinantialDocument();

            if (!creditNote.isAdvancePayment()) {
                continue;
            }

            if (!Constants.isPositive(creditEntry.getOpenAmount())) {
                continue;
            }

            if (creditEntry.getProduct() != TreasurySettings.getInstance().getAdvancePaymentProduct()) {
                continue;
            }

            if (creditNote.isPreparing()) {
                throw new RuntimeException("error");
            }

            taskLog("Change in  [%s - %s]: %s\n", creditEntry.getDebtAccount().getCustomer().getFiscalNumber(),
                    creditEntry.getDebtAccount().getCustomer().getName(),
                    creditEntry.getFinantialDocument().getUiDocumentNumber());

            {
                final DebtAccount debtAccount = creditEntry.getDebtAccount();
                final Series defaultSeries = Series.findUniqueDefault(finantialInstitution).get();
                final DocumentNumberSeries debitNoteSeries =
                        DocumentNumberSeries.find(FinantialDocumentType.findForDebitNote(), regulationSeries);
                final DocumentNumberSeries creditNoteSeries =
                        DocumentNumberSeries.find(FinantialDocumentType.findForCreditNote(), regulationSeries);
                final DocumentNumberSeries settlementNoteSeries =
                        DocumentNumberSeries.find(FinantialDocumentType.findForSettlementNote(), defaultSeries);
                final DateTime now = new DateTime();
                final Vat transferVat = Vat.findActiveUnique(product.getVatType(), finantialInstitution, now).get();

                final BigDecimal creditOpenAmount = creditEntry.getOpenAmount();
                final BigDecimal creditOpenAmountWithoutVat =
                        creditOpenAmount.subtract(creditOpenAmount.multiply(transferVat.getTaxRate()));

                final DebitNote regulationDebitNote = DebitNote.create(debtAccount, debitNoteSeries, now);

                DebitEntry regulationDebitEntry = DebitEntry.create(Optional.of(regulationDebitNote), debtAccount, null,
                        transferVat, creditOpenAmountWithoutVat, now.toLocalDate(), null, product, creditEntry.getDescription(),
                        BigDecimal.ONE, null, now);

                regulationDebitNote.closeDocument();
                regulationDebitNote.clearDocumentToExport("Migração de dados para integração SAP");
                final CreditNote regulationCreditNote =
                        CreditNote.create(debtAccount, creditNoteSeries, now, null, regulationDebitNote.getUiDocumentNumber());
                CreditEntry.create(regulationCreditNote, creditEntry.getDescription(), product, transferVat,
                        creditOpenAmountWithoutVat, now, null, BigDecimal.ONE);
                regulationCreditNote.setCloseDate(creditEntry.getFinantialDocument().getCloseDate());
                regulationCreditNote.setDocumentDate(creditEntry.getFinantialDocument().getDocumentDate());

                final SettlementNote settlementNote =
                        SettlementNote.create(debtAccount, settlementNoteSeries, now, now, null, null);
                if (creditEntry.getFinantialDocument().isPreparing()) {
                    creditEntry.getFinantialDocument().closeDocument();
                }

                SettlementEntry.create(regulationDebitEntry, settlementNote, regulationDebitEntry.getOpenAmount(),
                        creditEntry.getDescription(), now, false);
                SettlementEntry.create(creditEntry, settlementNote, creditOpenAmount, creditEntry.getDescription(), now, false);

                settlementNote.closeDocument();
                settlementNote.clearDocumentToExport("Migração de dados para integração SAP");
            }
        }
    }
    
}
