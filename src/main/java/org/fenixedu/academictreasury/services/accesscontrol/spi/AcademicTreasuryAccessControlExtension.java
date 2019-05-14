package org.fenixedu.academictreasury.services.accesscontrol.spi;

import java.util.Set;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicAccessRule;
import org.fenixedu.academic.domain.accessControl.academicAdministration.AcademicOperationType;
import org.fenixedu.academictreasury.services.AcademicTreasuryPlataformDependentServicesFactory;
import org.fenixedu.academictreasury.services.IAcademicTreasuryPlatformDependentServices;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.services.accesscontrol.spi.ITreasuryAccessControlExtension;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

public class AcademicTreasuryAccessControlExtension implements ITreasuryAccessControlExtension<Object> {

    @Override
    public boolean isFrontOfficeMember(final String username) {
        return FinantialInstitution.findAll().map(l -> isFrontOfficeMember(username, l)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isFrontOfficeMember(final String username, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.find(finantialInstitution).map(l -> isFrontOfficeMember(username, l)).reduce((a, b) -> a || b)
                .orElse(false);
    }

    private boolean isFrontOfficeMember(final String username, final FinantialEntity finantialEntity) {
        return AcademicTreasuryPlataformDependentServicesFactory.implementation().isFrontOfficeMember(username, finantialEntity);
    }

    @Override
    public boolean isBackOfficeMember(final String username) {
        return FinantialInstitution.findAll().map(l -> isBackOfficeMember(username, l)).reduce((a, b) -> a || b).orElse(false);
    }

    @Override
    public boolean isBackOfficeMember(final String username, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.find(finantialInstitution).map(l -> isBackOfficeMember(username, l)).reduce((a, b) -> a || b)
                .orElse(false);
    }

    @Override
    public boolean isBackOfficeMember(final String username, final FinantialEntity finantialEntity) {
        return AcademicTreasuryPlataformDependentServicesFactory.implementation().isBackOfficeMember(username, finantialEntity);
    }

    @Override
    public boolean isBackOfficeMemberWithinContext(String username, Object context) {
        final IAcademicTreasuryPlatformDependentServices services = AcademicTreasuryPlataformDependentServicesFactory.implementation();
        if (context instanceof Degree) {
            final Degree degree = (Degree) context;
            final FinantialEntity finantialEntity = services.finantialEntityOfDegree(degree, new LocalDate());
            
            return isBackOfficeMember(username, finantialEntity);
        }

        return false;
    }

    @Override
    public boolean isContextObjectApplied(Object context) {
        if (context instanceof Degree) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isManager(final String username) {
        return false;
    }

    @Override
    public boolean isAllowToModifySettlements(final String username, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.find(finantialInstitution).map(l -> isAllowToModifySettlements(username, l))
                .reduce((a, b) -> a || b).orElse(false);
    }

    private boolean isAllowToModifySettlements(final String username, final FinantialEntity finantialEntity) {
        return AcademicTreasuryPlataformDependentServicesFactory.implementation().isAllowToModifySettlements(username,
                finantialEntity);
    }

    @Override
    public boolean isAllowToModifyInvoices(final String username, final FinantialInstitution finantialInstitution) {
        return FinantialEntity.find(finantialInstitution).map(l -> isAllowToModifyInvoices(username, l)).reduce((a, b) -> a || b)
                .orElse(false);
    }

    private boolean isAllowToModifyInvoices(final String username, final FinantialEntity finantialEntity) {
        return AcademicTreasuryPlataformDependentServicesFactory.implementation().isAllowToModifyInvoices(username,
                finantialEntity);
    }

    @Override
    public boolean isAllowToConditionallyAnnulSettlementNote(final String username, final SettlementNote settlementNote) {
        return false;
    }

    @Override
    public boolean isAllowToAnnulSettlementNoteWithoutAnyRestriction(final String username, final SettlementNote settlementNote) {
        return false;
    }

    @Override
    public Set<String> getFrontOfficeMemberUsernames() {
        final IAcademicTreasuryPlatformDependentServices services =
                AcademicTreasuryPlataformDependentServicesFactory.implementation();

        final Set<String> result = Sets.newHashSet();

        FinantialEntity.findAll().forEach(entity -> {
            result.addAll(services.getFrontOfficeMemberUsernames(entity));
        });

        return result;
    }

    @Override
    public Set<String> getBackOfficeMemberUsernames() {
        final IAcademicTreasuryPlatformDependentServices services =
                AcademicTreasuryPlataformDependentServicesFactory.implementation();

        final Set<String> result = Sets.newHashSet();

        FinantialEntity.findAll().forEach(entity -> {
            result.addAll(services.getBackOfficeMemberUsernames(entity));
        });

        return result;
    }

    @Override
    public Set<String> getTreasuryManagerMemberUsernames() {
        return Sets.newHashSet();
    }

}
