package org.fenixedu.academictreasury.domain.coursefunctioncost;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.dto.coursefunctioncost.CourseFunctionCostBean;
import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import pt.ist.fenixframework.FenixFramework;

import pt.ist.fenixframework.Atomic;

public class CourseFunctionCost extends CourseFunctionCost_Base {

    protected CourseFunctionCost(final DegreeCurricularPlan degreeCurricularPlan, final CompetenceCourse competenceCourse,
            final ExecutionYear executionYear, final BigDecimal functionCost) {
        super();

        setDomainRoot(FenixFramework.getDomainRoot());

        setDegreeCurricularPlan(degreeCurricularPlan);
        setCompetenceCourses(competenceCourse);
        setExecutionYear(executionYear);
        setFunctionCost(functionCost);

        checkRules();
    }

    private void checkRules() {
        if (getDegreeCurricularPlan() == null) {
            throw new AcademicTreasuryDomainException("error.CourseFunctionCost.degreeCurricularPlan.required");
        }

        if (getCompetenceCourses() == null) {
            throw new AcademicTreasuryDomainException("error.CourseFunctionCost.competenceCourses.required");
        }

        if (getExecutionYear() == null) {
            throw new AcademicTreasuryDomainException("error.CourseFunctionCost.executionYear.required");
        }

        if (getFunctionCost() == null) {
            throw new AcademicTreasuryDomainException("error.CourseFunctionCost.functionCost.required");
        }

        if (AcademicTreasuryConstants.isNegative(getFunctionCost())) {
            throw new AcademicTreasuryDomainException("error.CourseFunctionCost.functionCost.must.be.positive.or.zero");
        }
    }

    @Atomic
    public void delete() {

        setDomainRoot(null);
        setExecutionYear(null);
        setDegreeCurricularPlan(null);
        setCompetenceCourses(null);

        deleteDomainObject();
    }

    public static Stream<CourseFunctionCost> findAll() {
        return FenixFramework.getDomainRoot().getCourseFunctionCostsSet().stream();
    }

    public static Stream<CourseFunctionCost> find(final ExecutionYear executionYear, final CurricularCourse curricularCourse) {
        return find(curricularCourse.getDegreeCurricularPlan(), curricularCourse.getCompetenceCourse(), executionYear);
    }

    public static Stream<CourseFunctionCost> find(final DegreeCurricularPlan degreeCurricularPlan,
            final CompetenceCourse competenceCourse, final ExecutionYear executionYear) {
        return competenceCourse.getCourseFunctionCostsSet().stream()
                .filter(l -> l.getExecutionYear() == executionYear && l.getDegreeCurricularPlan() == degreeCurricularPlan);
    }

    public static Optional<CourseFunctionCost> findUnique(final ExecutionYear executionYear,
            final CurricularCourse curricularCourse) {
        return find(executionYear, curricularCourse).findFirst();
    }

    public static Optional<CourseFunctionCost> findUnique(final DegreeCurricularPlan degreeCurricularPlan,
            final CompetenceCourse competenceCourse, final ExecutionYear executionYear) {
        return find(degreeCurricularPlan, competenceCourse, executionYear).findFirst();
    }

    @Atomic
    public static CourseFunctionCost create(final ExecutionYear executionYear, final CurricularCourse curricularCourse,
            final BigDecimal functionCost) {
        return new CourseFunctionCost(curricularCourse.getDegreeCurricularPlan(), curricularCourse.getCompetenceCourse(),
                executionYear, functionCost);
    }

    @Atomic
    public static CourseFunctionCost create(CourseFunctionCostBean bean) {
        return new CourseFunctionCost(bean.getDegreeCurricularPlan(), bean.getCompetenceCourses(), bean.getExecutionYear(),
                bean.getFunctionCost());
    }

}
