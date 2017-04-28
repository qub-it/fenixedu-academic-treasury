package org.fenixedu.academictreasury.dto.integration.tuitioninfo;

public enum ERPTuitionInfoTuitionType {
    REGISTRATION,
    STANDALONE,
    EXTRACURRICULAR;
    
    public boolean isForRegistrationTuition() {
        return this == REGISTRATION;
    }
    
    public boolean isForStandaloneTuition() {
        return this == STANDALONE;
    }
    
    public boolean isForExtracurricularTuition() {
        return this == EXTRACURRICULAR;
    }
    
}