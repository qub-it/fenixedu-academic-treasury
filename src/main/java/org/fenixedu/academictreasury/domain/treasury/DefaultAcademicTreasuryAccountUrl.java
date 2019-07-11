package org.fenixedu.academictreasury.domain.treasury;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.event.TreasuryEvent;
import org.fenixedu.treasury.ui.accounting.managecustomer.CustomerController;
import org.fenixedu.treasury.ui.accounting.managecustomer.DebtAccountController;

public class DefaultAcademicTreasuryAccountUrl implements IAcademicTreasuryAccountUrl {

    @Override
    public String getPersonAccountTreasuryManagementURL(Person person) {
        final String countryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);
        return CustomerController.READ_URL
                + PersonCustomer.findUnique(person, countryCode, fiscalNumber).get().getExternalId();
    }

    @Override
    public String getRegistrationAccountTreasuryManagementURL(Registration registration) {
        if (registration.getDegree().getAdministrativeOffice().getFinantialEntity() == null) {
            return getPersonAccountTreasuryManagementURL(registration.getPerson());
        }

        final FinantialInstitution inst =
                registration.getDegree().getAdministrativeOffice().getFinantialEntity().getFinantialInstitution();
        final Person person = registration.getPerson();
        final String countryCode = PersonCustomer.countryCode(person);
        final String fiscalNumber = PersonCustomer.fiscalNumber(person);

        final PersonCustomer customer = PersonCustomer.findUnique(person, countryCode, fiscalNumber).get();

        final DebtAccount account = customer.getDebtAccountFor(inst);
        if (account != null) {
            return DebtAccountController.READ_URL
                    + customer.getDebtAccountFor(inst).getExternalId();
        } else {
            return getPersonAccountTreasuryManagementURL(person);
        }
    }

    @Override
    public String getDebtAccountURL(final TreasuryEvent treasuryEvent) {
        if (DebitEntry.findActive(treasuryEvent).findFirst().isPresent()) {
            return DebtAccountController.READ_URL
                    + DebitEntry.findActive(treasuryEvent).findFirst().get().getDebtAccount().getExternalId();
        }

        return null;
    }

}
