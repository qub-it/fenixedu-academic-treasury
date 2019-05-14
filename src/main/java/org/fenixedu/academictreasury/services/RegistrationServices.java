package org.fenixedu.academictreasury.services;

import java.util.Optional;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

import pt.ist.fenixframework.Atomic;

public class RegistrationServices {

    @Atomic
    public static PersonCustomer createPersonCustomer(final Person p) {
        final String fiscalCountryCode = PersonCustomer.addressCountryCode(p);
        final String fiscalNumber = PersonCustomer.fiscalNumber(p);

        if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        Optional<? extends PersonCustomer> findUnique = PersonCustomer.findUnique(p, fiscalCountryCode, fiscalNumber);
        if (findUnique.isPresent()) {
            return findUnique.get();
        }
        return PersonCustomer.create(p, fiscalCountryCode, fiscalNumber);
    }

    @Subscribe
    public void newRegistrationEvent(final DomainObjectEvent<Registration> event) {
        Registration reg = event.getInstance();
        if (reg.getStudent() != null) {
            createPersonCustomer(reg.getStudent().getPerson());
        }
    }
}
