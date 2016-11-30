package org.fenixedu.academictreasury.domain.integration;

import java.util.Comparator;
import java.util.stream.Stream;

import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfo;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.OperationFile;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class ERPTuitionInfoExportOperation extends ERPTuitionInfoExportOperation_Base {

    public static final Comparator<ERPTuitionInfoExportOperation> COMPARE_BY_EXECUTION_DATE =
            new Comparator<ERPTuitionInfoExportOperation>() {

                @Override
                public int compare(final ERPTuitionInfoExportOperation o1, final ERPTuitionInfoExportOperation o2) {
                    int c = o1.getExecutionDate().compareTo(o2.getExecutionDate());

                    return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
                }
            };

    public ERPTuitionInfoExportOperation() {
        super();
    }

    public ERPTuitionInfoExportOperation(final ERPTuitionInfo erpTuitionInfo) {
        this();

        setErpTuitionInfo(erpTuitionInfo);
    }

    protected void init(final OperationFile file, final FinantialInstitution finantialInstitution, final String erpOperationId,
            final DateTime executionDate) {
        setFile(file);
        setFinantialInstitution(finantialInstitution);
        setExecutionDate(executionDate);

        checkRules();
    }

    private void checkRules() {
        if (getFile() == null) {
            throw new TreasuryDomainException("error.ERPTuitionInfoExportOperation.file.required");
        }

        if (getFinantialInstitution() == null) {
            throw new TreasuryDomainException("error.ERPTuitionInfoExportOperation.finantialInstitution.required");
        }
    }

    // @formatter:off
    /* ********
     * SERVICES
     * ********
     */
    // @formatter:on

    public static Stream<ERPTuitionInfoExportOperation> findAll() {
        return ERPTuitionInfo.findAll().flatMap(e -> e.getErpTuitionInfoExportOperationsSet().stream());
    }

    @Atomic
    public static ERPTuitionInfoExportOperation create(final ERPTuitionInfo erpTuitionInfo, final byte[] data,
            final String filename, final FinantialInstitution finantialInstitution, final String erpOperationId,
            final DateTime executionDate) {
        ERPTuitionInfoExportOperation erpTuitionInfoExportOperation = new ERPTuitionInfoExportOperation(erpTuitionInfo);
        OperationFile file;

        if (data == null) {
            file = OperationFile.create(filename, new byte[0], erpTuitionInfoExportOperation);
        } else {
            file = OperationFile.create(filename, data, erpTuitionInfoExportOperation);
        }

        erpTuitionInfoExportOperation.init(file, finantialInstitution, erpOperationId, executionDate);

        return erpTuitionInfoExportOperation;
    }

}
