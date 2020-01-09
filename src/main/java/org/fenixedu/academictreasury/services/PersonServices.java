package org.fenixedu.academictreasury.services;

import java.util.Optional;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academictreasury.domain.academicalAct.AcademicActBlockingSuspension;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

public class PersonServices {

    @Atomic
    public static PersonCustomer createPersonCustomer(final Person p) {
        final String addressFiscalCountryCode = PersonCustomer.addressCountryCode(p);
        final String fiscalNumber = PersonCustomer.fiscalNumber(p);
        if (Strings.isNullOrEmpty(addressFiscalCountryCode) || Strings.isNullOrEmpty(fiscalNumber)) {
            throw new AcademicTreasuryDomainException("error.PersonCustomer.fiscalInformation.required");
        }

        final Optional<? extends PersonCustomer> findUnique = PersonCustomer.findUnique(p, addressFiscalCountryCode, fiscalNumber);
        if (findUnique.isPresent()) {
            return findUnique.get();
        }

        return PersonCustomer.create(p, addressFiscalCountryCode, fiscalNumber);
    }

    public static boolean isAcademicalActsBlocked(final Person person, final LocalDate when) {
        if (AcademicActBlockingSuspension.isBlockingSuspended(person, when)) {
            return false;
        }
        
        for (final PersonCustomer personCustomer : PersonCustomer.find(person).collect(Collectors.<PersonCustomer> toSet())) {
            if (personCustomer.isBlockingAcademicalActs(when)) {
                return true;
            }
        }
        
        return false;
    }

    @Subscribe
    public void newPersonEvent(final DomainObjectEvent<Person> event) {
        Person p = event.getInstance();
        createPersonCustomer(event.getInstance());
    }
}
