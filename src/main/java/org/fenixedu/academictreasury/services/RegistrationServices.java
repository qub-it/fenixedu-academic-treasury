package org.fenixedu.academictreasury.services;

import java.util.Optional;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

import pt.ist.fenixframework.Atomic;

public class RegistrationServices {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationServices.class);

    @Atomic
    public static PersonCustomer createPersonCustomer(final Person p) {
        final String fiscalCountryCode = PersonCustomer.addressCountryCode(p);
        final String fiscalNumber = PersonCustomer.fiscalNumber(p);

        if (Strings.isNullOrEmpty(fiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            AcademicTreasuryDomainException exception = new AcademicTreasuryDomainException(
                    "error.RegistrationServices.createPersonCustomer.fiscalInformation.required", p.getName());
            logger.warn(exception.getLocalizedMessage());
            throw exception;
        }

        Optional<? extends PersonCustomer> findUnique = PersonCustomer.findUnique(p, fiscalCountryCode, fiscalNumber);
        if (findUnique.isPresent()) {
            return findUnique.get();
        }
        return PersonCustomer.create(p, fiscalCountryCode, fiscalNumber);
    }

    public void newRegistrationEvent(final DomainObjectEvent<Registration> event) {
        Registration reg = event.getInstance();
        if (reg.getStudent() != null) {
            createPersonCustomer(reg.getStudent().getPerson());
        }
    }
}
