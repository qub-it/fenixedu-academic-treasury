package org.fenixedu.academictreasury.ui.integration.tuitioninfo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.academictreasury.domain.integration.ERPTuitionInfoExportOperation;
import org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfo;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;

@RequestMapping(ERPTuitionInfoExportOperationController.CONTROLLER_URL)
@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.ERPTuitionInfoExportOperation.title",
        accessGroup = "#managers")
public class ERPTuitionInfoExportOperationController extends AcademicTreasuryBaseController {

    private static final int MAX_SEARCH_RESULT_SIZE = 3000;

    public static final String CONTROLLER_URL = "/academictreasury/erptuitioninfoexportoperation";
    private static final String JSP_PATH = "academicTreasury/erptuitioninfoexportoperation";

    @RequestMapping
    public String home() {
        return "redirect:" + SEARCH_URL;
    }

    private static final String _SEARCH_URI = "/search";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(_SEARCH_URI)
    public String search(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate toDate,
            @RequestParam(value = "erpTuitionInfoDocumentNumber", required = false) final String erpTuitionInfoDocumentNumber,
            @RequestParam(value = "success", required = false) final Boolean success, final Model model) {

        List<ERPTuitionInfoExportOperation> result = filter(fromDate, toDate, erpTuitionInfoDocumentNumber, success);

        if (result.size() > MAX_SEARCH_RESULT_SIZE) {
            model.addAttribute("result_totalCount", result.size());
            result = result.subList(0, MAX_SEARCH_RESULT_SIZE);
            model.addAttribute("limit_exceeded", true);
        }

        model.addAttribute("result", result);

        return jspPage(_SEARCH_URI);
    }

    private List<ERPTuitionInfoExportOperation> filter(final LocalDate fromDate, final LocalDate toDate,
            final String erpTuitionInfoDocumentNumber, final Boolean success) {
        Stream<ERPTuitionInfoExportOperation> base = null;

        if (!Strings.isNullOrEmpty(erpTuitionInfoDocumentNumber)) {
            final Optional<ERPTuitionInfo> info = ERPTuitionInfo.findUniqueByDocumentNumber(erpTuitionInfoDocumentNumber);

            if (info.isPresent()) {
                base = info.get().getErpTuitionInfoExportOperationsSet().stream();
            }
        }

        if (base == null) {
            base = ERPTuitionInfoExportOperation.findAll();
        }

        if (fromDate != null) {
            base = base.filter(e -> !e.getExecutionDate().isBefore(fromDate.toDateTimeAtStartOfDay()));
        }

        if (toDate != null) {
            base = base.filter(e -> !e.getExecutionDate().isAfter(toDate.plusDays(1).toDateTimeAtStartOfDay().minusSeconds(1)));
        }

        if (success != null) {
            base = base.filter(e -> e.getSuccess() == success);
        }

        return base.sorted(ERPTuitionInfoExportOperation.COMPARE_BY_EXECUTION_DATE.reversed()).collect(Collectors.toList());
    }

    private static final String _READ_URI = "/read";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "/{operationId}", method = RequestMethod.GET)
    public String read(@PathVariable("operationId") final ERPTuitionInfoExportOperation erpTuitionInfoExportOperation,
            final Model model) {
        model.addAttribute("operation", erpTuitionInfoExportOperation);

        return jspPage(_READ_URI);
    }

    private static final String _DOWNLOAD_URI = "/download";
    public static final String DOWNLOAD_URL = CONTROLLER_URL + _DOWNLOAD_URI;

    @RequestMapping(value = _DOWNLOAD_URI + "/{operationId}")
    public String download(@PathVariable("operationId") final ERPTuitionInfoExportOperation operation,
            final HttpServletResponse response, final Model model) {

        final String filename = operation.getFile().getFilename();

        response.setContentType(operation.getFile().getContentType());
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        try {
            response.getOutputStream().write(operation.getFile().getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static final String _RETRY_URI = "/retry";
    public static final String RETRY_URL = CONTROLLER_URL + _RETRY_URI;

    private static final String _SOAPOUTBOUNDMESSAGE_URI = "/soapoutboundmessage";
    public static final String SOAPOUTBOUNDMESSAGE_URL = CONTROLLER_URL + _SOAPOUTBOUNDMESSAGE_URI;

    @RequestMapping(value = _SOAPOUTBOUNDMESSAGE_URI + "/{operationId}")
    public String soapoutboundmessage(@PathVariable("operationId") final ERPTuitionInfoExportOperation operation,
            final HttpServletResponse response, final Model model) {

        response.setContentType(com.google.common.net.MediaType.XML_UTF_8.toString());
        response.setHeader("Content-disposition",
                String.format("attachment; filename=SOAP_Outbound_Message_%s.xml", operation.getExternalId()));
        try {
            response.getWriter().write(operation.getSoapOutboundMessage() != null ? operation.getSoapOutboundMessage() : "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static final String _SOAPINBOUNDMESSAGE_URI = "/soapinboundmessage";
    public static final String SOAPINBOUNDMESSAGE_URL = CONTROLLER_URL + _SOAPINBOUNDMESSAGE_URI;

    @RequestMapping(value = _SOAPINBOUNDMESSAGE_URI + "/{operationId}")
    public String soapinboundmessage(@PathVariable("operationId") final ERPTuitionInfoExportOperation operation,
            final HttpServletResponse response, final Model model) {

        response.setContentType(com.google.common.net.MediaType.XML_UTF_8.toString());
        response.setHeader("Content-disposition",
                String.format("attachment; filename=SOAP_Inbound_Message_%s.xml", operation.getExternalId()));
        try {
            response.getWriter().write(operation.getSoapInboundMessage() != null ? operation.getSoapInboundMessage() : "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private String jspPage(final String page) {
        return JSP_PATH + page;
    }

}
