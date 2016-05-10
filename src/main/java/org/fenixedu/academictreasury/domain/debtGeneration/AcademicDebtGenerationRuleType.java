package org.fenixedu.academictreasury.domain.debtGeneration;

import java.util.Optional;

import org.fenixedu.bennu.core.domain.Bennu;

public class AcademicDebtGenerationRuleType extends AcademicDebtGenerationRuleType_Base {
    
    public static final String DEPRECATED_STRATEGY_CODE = "DEPRECATED_STRATEGY";
    
    protected AcademicDebtGenerationRuleType() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    public AcademicDebtGenerationRuleType(final String code, final String name, final String strategyImplementation) {
        this();
        setCode(code);
        setName(name);
        setStrategyImplementation(strategyImplementation);
    }
    public IAcademicDebtGenerationRuleStrategy strategyImplementation() {
        try {
            return (IAcademicDebtGenerationRuleStrategy) Class.forName(getStrategyImplementation()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on
    
    public static Optional<AcademicDebtGenerationRuleType> findByCode(final String code) {
        return Bennu.getInstance().getAcademicDebtGenerationRuleTypesSet().stream().filter(l -> l.getCode().equals(code)).findAny();
    }
    
    public static AcademicDebtGenerationRuleType create(final String code, final String name, final String strategyImplementation) {
        return new AcademicDebtGenerationRuleType(code, name, strategyImplementation);
    }

}
