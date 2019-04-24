package org.fenixedu.academictreasury.services;


import org.fenixedu.academic.domain.administrativeOffice.AdministrativeOffice;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;

import pt.ist.fenixframework.Atomic;

public class OrganizationalStructureServices {

    @Atomic
    public static FinantialEntity createFinantialEntityWithAdministrativeOffice(final FinantialInstitution finantialInstitution,
            final String code, final LocalizedString name, final AdministrativeOffice administrativeOffice) {
        FinantialEntity finantialEntity = FinantialEntity.create(finantialInstitution, code, name);

        finantialEntity.setAdministrativeOffice(administrativeOffice);

        checkRulesForFinantialEntity(finantialEntity);
        
        return finantialEntity;
    }

    @Atomic
    public static FinantialEntity createFinantialEntityWithUnit(final FinantialInstitution finantialInstitution,
            final String code, final LocalizedString name, final Unit unit) {
        FinantialEntity finantialEntity = FinantialEntity.create(finantialInstitution, code, name);

        finantialEntity.setUnit(unit);

        checkRulesForFinantialEntity(finantialEntity);

        return finantialEntity;
    }

    @Atomic
    public static void associateFinantialEntityWithAdministrativeOffice(final FinantialEntity finantialEntity,
            final AdministrativeOffice administrativeOffice) {
        finantialEntity.setAdministrativeOffice(administrativeOffice);

        checkRulesForFinantialEntity(finantialEntity);
    }
    
    @Atomic
    public static void associateFinantialEntityWithUnit(final FinantialEntity finantialEntity, final Unit unit) {
        finantialEntity.setUnit(unit);

        checkRulesForFinantialEntity(finantialEntity);
    }

    private static void checkRulesForFinantialEntity(final FinantialEntity finantialEntity) {
        finantialEntity.checkRules();

        if (finantialEntity.getUnit() == null && finantialEntity.getAdministrativeOffice() == null) {
            throw new AcademicTreasuryDomainException(
                    "error.FinantialEntity.association.with.unit.or.administrativeOffice.required");
        }

        if (finantialEntity.getUnit() != null && finantialEntity.getAdministrativeOffice() != null) {
            throw new AcademicTreasuryDomainException(
                    "error.FinantialEntity.must.be.associated.with.unit.or.administrativeOffice.but.not.both");
        }
    }

}
