package org.fenixedu.academictreasury.scripts;

import org.fenixedu.academic.domain.Country;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.PhysicalAddress;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.treasury.util.FiscalCodeValidation;

import com.google.common.base.Strings;

public class SaveFiscalCountryOnPersons extends CustomTask {

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
            
            if(Strings.isNullOrEmpty(PersonCustomer.fiscalNumber(person))) {
                person.editSocialSecurityNumber(Country.readDefault(), PersonCustomer.DEFAULT_FISCAL_NUMBER);
            } else if (PersonCustomer.DEFAULT_FISCAL_NUMBER.equals(PersonCustomer.fiscalNumber(person))) {
                person.editSocialSecurityNumber(Country.readDefault(), PersonCustomer.fiscalNumber(person));
            } else if(FiscalCodeValidation.isValidFiscalNumber(Country.readDefault().getCode(), PersonCustomer.fiscalNumber(person))) {
                person.editSocialSecurityNumber(Country.readDefault(), PersonCustomer.fiscalNumber(person));
            } else {
                final PhysicalAddress physicalAddress = PersonCustomer.physicalAddress(person);
                if(physicalAddress != null && physicalAddress.getCountryOfResidence() != null) {
                    person.editSocialSecurityNumber(physicalAddress.getCountryOfResidence(), PersonCustomer.fiscalNumber(person));
                }
            }
        }

    }
}
