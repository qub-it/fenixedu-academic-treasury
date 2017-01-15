package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.bennu.core.domain.Bennu;

import com.google.common.base.Strings;

public class ERPTuitionInfoType extends ERPTuitionInfoType_Base {

    public static final Comparator<ERPTuitionInfoType> COMPARE_BY_NAME = new Comparator<ERPTuitionInfoType>() {

        @Override
        public int compare(final ERPTuitionInfoType o1, final ERPTuitionInfoType o2) {
            int c = o1.getName().compareTo(o2.getName());

            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }

    };

    public ERPTuitionInfoType() {
        super();
        setBennu(Bennu.getInstance());
        setErpTuitionInfoSettings(ERPTuitionInfoSettings.getInstance());
        setActive(true);
    }

    public ERPTuitionInfoType(final DegreeType degreeType, final String code, final String name) {
        this();

        setDegreeType(degreeType);
        setCode(code);
        setName(name);
        
        setForRegistration(true);
        setForStandalone(false);
        setForExtracurricular(false);

        checkRules();
    }

    public ERPTuitionInfoType(final String code, final String name, final boolean forStandalone,
            final boolean forExtracurricular) {
        this();
        setCode(code);
        setName(name);

        setForRegistration(false);
        setForStandalone(forStandalone);
        setForExtracurricular(forExtracurricular);

        checkRules();
    }

    private void checkRules() {
        if (getBennu() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.bennu.required");
        }

        if (Strings.isNullOrEmpty(getCode())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.code.required");
        }

        if (Strings.isNullOrEmpty(getName())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.name.required");
        }

        if (!(isForRegistration() ^ isForStandalone() ^ isForExtracurricular())) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.entry.for.one.tuition.type.only");
        }

        if (isForRegistration() && getDegreeType() == null) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.entry.degreeType.required");
        }

        if ((isForStandalone() || isForExtracurricular()) && getDegreeType() != null) {
            throw new AcademicTreasuryDomainException(
                    "error.ERPTuitionInfoType.entry.degreeType.not.supported.for.standalone.or.extracurricular");
        }

        if (findByCode(getCode()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ERPTuitionInfoType.code.not.unique");
        }
    }

    public boolean isForRegistration() {
        return getForRegistration();
    }

    public boolean isForStandalone() {
        return getForStandalone();
    }

    public boolean isForExtracurricular() {
        return getForExtracurricular();
    }

    public boolean isActive() {
        return getActive();
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<ERPTuitionInfoType> findAll() {
        return ERPTuitionInfoSettings.getInstance().getErpTuitionInfoTypesSet().stream();
    }

    public static Stream<ERPTuitionInfoType> findActive() {
        return findAll().filter(e -> e.isActive());
    }

    public static Stream<ERPTuitionInfoType> findByCode(final String code) {
        return findAll().filter(e -> e.getCode().equals(code));
    }

    public static Optional<ERPTuitionInfoType> findUniqueByCode(final String code) {
        return findAll().filter(e -> e.getCode().equals(code)).findFirst();
    }

    public static ERPTuitionInfoType createForRegistrationTuition(final DegreeType degreeType, final String code,
            final String name) {
        return new ERPTuitionInfoType(degreeType, code, name);
    }

    public static ERPTuitionInfoType createForStandaloneTuition(final String code, final String name) {
        return new ERPTuitionInfoType(code, name, true, false);
    }

    public static ERPTuitionInfoType createForExtracurricularTuition(final String code, final String name) {
        return new ERPTuitionInfoType(code, name, false, true);
    }

}
