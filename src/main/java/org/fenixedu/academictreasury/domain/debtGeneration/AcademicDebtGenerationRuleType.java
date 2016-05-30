package org.fenixedu.academictreasury.domain.debtGeneration;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;

public class AcademicDebtGenerationRuleType extends AcademicDebtGenerationRuleType_Base {
    
    public static Comparator<AcademicDebtGenerationRuleType> COMPARE_BY_ORDER_NUMBER = new Comparator<AcademicDebtGenerationRuleType>() {

        @Override
        public int compare(final AcademicDebtGenerationRuleType o1, final AcademicDebtGenerationRuleType o2) {
            int c = Integer.compare(o1.getOrderNumber(), o2.getOrderNumber());
            
            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }
        
    };
    
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
    
    public static Stream<AcademicDebtGenerationRuleType> findAll() {
        return Bennu.getInstance().getAcademicDebtGenerationRuleTypesSet().stream();
    }
    
    public static Optional<AcademicDebtGenerationRuleType> findByCode(final String code) {
        return findAll().filter(l -> l.getCode().equals(code)).findAny();
    }
    
    public static AcademicDebtGenerationRuleType create(final String code, final String name, final String strategyImplementation) {
        return new AcademicDebtGenerationRuleType(code, name, strategyImplementation);
    }

}
