package org.fenixedu.academictreasury.services.accesscontrol.spi;

import java.util.Collections;
import java.util.Set;

import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicAccessRule;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicOperationType;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.services.accesscontrol.spi.ITreasuryAccessControlExtension;

import com.google.common.collect.Sets;

public class AcademicTreasuryAccessControlExtension implements ITreasuryAccessControlExtension {

    @Override
    public boolean isFrontOfficeMember(final User user) {
        return FinantialInstitution.findAll().map(l -> isFrontOfficeMember(user, l)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isFrontOfficeMember(final User user, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.find(finantialInstitution).map(l -> isFrontOfficeMember(user, l)).reduce((a, b) -> a || b)
                .orElse(false);
    }

    private boolean isFrontOfficeMember(final User user, final FinantialEntity finantialEntity) {
        if (finantialEntity.getAdministrativeOffice() == null) {
            return false;
        }

        return AcademicAccessRule.isMember(user, AcademicOperationType.MANAGE_STUDENT_PAYMENTS, Collections.emptySet(),
                Collections.singleton(finantialEntity.getAdministrativeOffice()));
    }
    
    @Override
    public boolean isBackOfficeMember(final User user) {
        return FinantialInstitution.findAll().map(l -> isBackOfficeMember(user, l)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isBackOfficeMember(final User user, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.findAll().map(l -> isBackOfficeMember(user, l)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isBackOfficeMember(final User user, final FinantialEntity finantialEntity) {
        if (finantialEntity.getAdministrativeOffice() == null) {
            return false;
        }

        return AcademicAccessRule.isMember(user, AcademicOperationType.MANAGE_STUDENT_PAYMENTS_ADV, Collections.emptySet(),
                Collections.singleton(finantialEntity.getAdministrativeOffice()));
    }

    @Override
    public Set<User> getFrontOfficeMembers() {
        return FinantialEntity
                .findAll()
                .filter(l -> l.getAdministrativeOffice() != null)
                .map(l -> AcademicAccessRule.getMembers(AcademicOperationType.MANAGE_STUDENT_PAYMENTS, Collections.emptySet(),
                        Collections.singleton(l.getAdministrativeOffice()))).reduce((a, b) -> Sets.union(a, b))
                .orElse(Collections.emptySet());
    }

    @Override
    public Set<User> getBackOfficeMembers() {
        return FinantialEntity
                .findAll()
                .filter(l -> l.getAdministrativeOffice() != null)
                .map(l -> AcademicAccessRule.getMembers(AcademicOperationType.MANAGE_STUDENT_PAYMENTS_ADV,
                        Collections.emptySet(), Collections.singleton(l.getAdministrativeOffice())))
                .reduce((a, b) -> Sets.union(a, b)).orElse(Collections.emptySet());
    }

    @Override
    public boolean isAllowToModifySettlements(final User user, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.findAll().map(l -> isAllowToModifySettlements(user, l)).reduce((a, b) -> a || b).orElse(false);
    }

    private boolean isAllowToModifySettlements(final User user, final FinantialEntity finantialEntity) {
        if (finantialEntity.getAdministrativeOffice() == null) {
            return false;
        }

        return AcademicAccessRule.isMember(user, AcademicOperationType.PAYMENTS_MODIFY_SETTLEMENTS, Collections.emptySet(),
                Collections.singleton(finantialEntity.getAdministrativeOffice()));
    }

    @Override
    public boolean isAllowToModifyInvoices(final User user, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.findAll().map(l -> isAllowToModifyInvoices(user, l)).reduce((a, b) -> a || b).orElse(false);
    }

    private boolean isAllowToModifyInvoices(final User user, final FinantialEntity finantialEntity) {
        if (finantialEntity.getAdministrativeOffice() == null) {
            return false;
        }

        return AcademicAccessRule.isMember(user, AcademicOperationType.PAYMENTS_MODIFY_INVOICES, Collections.emptySet(),
                Collections.singleton(finantialEntity.getAdministrativeOffice()));
    }

}
