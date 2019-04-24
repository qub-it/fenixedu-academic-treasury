package org.fenixedu.academictreasury.dto.reports;


import static org.fenixedu.academictreasury.util.AcademicTreasuryConstants.academicTreasuryBundle;

import org.fenixedu.academictreasury.util.AcademicTreasuryConstants;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.util.streaming.spreadsheet.SpreadsheetRow;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;

public abstract class AbstractReportEntryBean implements SpreadsheetRow {

    protected String valueOrEmpty(final LocalDate value) {
        if (value == null) {
            return "";
        }

        return value.toString(AcademicTreasuryConstants.DATE_FORMAT_YYYY_MM_DD);        
    }
    
    protected String valueOrEmpty(final DateTime value) {
        if (value == null) {
            return "";
        }

        return value.toString(AcademicTreasuryConstants.DATE_TIME_FORMAT_YYYY_MM_DD);
    }

    protected String valueOrEmpty(final Boolean value) {
        if (value == null) {
            return "";
        }

        return academicTreasuryBundle(value ? "label.yes" : "label.no");
    }

    protected String valueOrEmpty(final Integer value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    protected String valueOrEmpty(final LocalizedString value) {
        if (Strings.isNullOrEmpty(value.getContent())) {
            return "";
        }

        return value.getContent();
    }

    protected String valueOrEmpty(final String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }

        return "";
    }
    
}
