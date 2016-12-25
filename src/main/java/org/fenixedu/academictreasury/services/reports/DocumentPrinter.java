package org.fenixedu.academictreasury.services.reports;

import java.io.InputStream;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.services.reports.dataproviders.CustomerDataProvider;
import org.fenixedu.treasury.services.reports.dataproviders.DebtAccountDataProvider;
import org.fenixedu.treasury.services.reports.dataproviders.FinantialInstitutionDataProvider;
import org.fenixedu.treasury.services.reports.helpers.DateHelper;
import org.fenixedu.treasury.services.reports.helpers.EnumerationHelper;
import org.fenixedu.treasury.services.reports.helpers.LanguageHelper;
import org.fenixedu.treasury.services.reports.helpers.MoneyHelper;
import org.fenixedu.treasury.services.reports.helpers.NumbersHelper;
import org.fenixedu.treasury.services.reports.helpers.StringsHelper;

import com.google.common.base.Strings;
import com.qubit.terra.docs.core.DocumentGenerator;
import com.qubit.terra.docs.core.DocumentTemplateEngine;
import com.qubit.terra.docs.core.IDocumentTemplateService;

public class DocumentPrinter {
    private static final String TEMPLATES_TUITIONS_PAYMENT_PLAN = "templates/tuitionsPaymentPlan.odt";

    static {
        registerService();
    }

    public static synchronized void registerService() {
        IDocumentTemplateService service = new DocumentPrinterConfiguration();
        DocumentTemplateEngine.registerServiceImplementations(service);
    }

    public static final String PDF = DocumentGenerator.PDF;
    public static final String ODT = DocumentGenerator.ODT;

    private static void registerHelpers(DocumentGenerator generator) {
        generator.registerHelper("dates", new DateHelper());
        generator.registerHelper("lang", new LanguageHelper());
        generator.registerHelper("numbers", new NumbersHelper());
        generator.registerHelper("enumeration", new EnumerationHelper());
        generator.registerHelper("strings", new StringsHelper());
        generator.registerHelper("money", new MoneyHelper());
    }

    //https://github.com/qub-it/fenixedu-qubdocs-reports/blob/master/src/main/java/org/fenixedu/academic/util/report/DocumentPrinter.java
    public static byte[] printRegistrationTuititionPaymentPlan(Registration registration, String outputMimeType) {
        final Person p = registration.getStudent().getPerson();
        final String fiscalCountryCode = PersonCustomer.countryCode(p);
        final String fiscalNumber = PersonCustomer.fiscalNumber(p);
        if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        final PersonCustomer customer = PersonCustomer.findUnique(p, fiscalCountryCode, fiscalNumber).orElse(null);
        final FinantialInstitution finst =
                registration.getDegree().getAdministrativeOffice().getFinantialEntity().getFinantialInstitution();
        final DebtAccount account = DebtAccount.findUnique(finst, customer).orElse(null);

        return printRegistrationTuititionPaymentPlan(account, outputMimeType);
    }

    public static byte[] printRegistrationTuititionPaymentPlan(final DebtAccount debtAccount, String outputMimeType) {

        DocumentGenerator generator = null;

        //TODO refactor: there should be an application runtime configuration to enable those templates
        //Gets file templates/tuitionsPaymentPlan-NIF.odt
        InputStream resourceAsStream =
                DocumentGenerator.class.getClassLoader().getResourceAsStream(TEMPLATES_TUITIONS_PAYMENT_PLAN);

        generator = DocumentGenerator.create(resourceAsStream, outputMimeType);
//          throw new TreasuryDomainException("error.ReportExecutor.document.template.not.available");
//      }

        registerHelpers(generator);
        generator.registerDataProvider(new DebtAccountDataProvider(debtAccount, null));
        generator.registerDataProvider(new CustomerDataProvider(debtAccount.getCustomer()));
        generator.registerDataProvider(new FinantialInstitutionDataProvider(debtAccount.getFinantialInstitution()));

        //... add more providers...

        byte[] outputReport = generator.generateReport();

        return outputReport;
    }

}
