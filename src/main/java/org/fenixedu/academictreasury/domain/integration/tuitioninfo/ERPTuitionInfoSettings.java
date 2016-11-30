package org.fenixedu.academictreasury.domain.integration.tuitioninfo;

import org.fenixedu.academictreasury.domain.integration.tuitioninfo.exporter.ERPTuitionInfoExporterForSAP;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.document.Series;

import pt.ist.fenixframework.Atomic;

public class ERPTuitionInfoSettings extends ERPTuitionInfoSettings_Base {
    
    public ERPTuitionInfoSettings() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    @Atomic
    public void edit(final Series series, final String expectedSeriesDebitCode, final String expectedSeriesCreditCode) {
        setSeries(series);
        setExpectedSeriesDebitCode(expectedSeriesDebitCode);
        setExpectedSeriesCreditCode(expectedSeriesCreditCode);
    }
    
    public IERPTuitionInfoExporter exporter() {
        return new ERPTuitionInfoExporterForSAP();
    }
    
    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on
    
    @Atomic
    public static ERPTuitionInfoSettings getInstance() {
        if(Bennu.getInstance().getErpTuitionInfoSettings() == null) {
            new ERPTuitionInfoSettings();
        }
        
        return Bennu.getInstance().getErpTuitionInfoSettings();
    }
}
