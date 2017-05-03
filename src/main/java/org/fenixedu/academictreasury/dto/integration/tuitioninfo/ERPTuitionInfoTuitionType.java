package org.fenixedu.academictreasury.dto.integration.tuitioninfo;

import org.fenixedu.academictreasury.util.Constants;
import org.fenixedu.commons.i18n.LocalizedString;

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

    public LocalizedString getLocalizedName() {
        return Constants.bundleI18N("label." + getClass().getName() + "." + name());
    }
    
}