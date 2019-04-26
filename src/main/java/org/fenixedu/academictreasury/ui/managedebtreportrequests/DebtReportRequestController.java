/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: xpto@qub-it.com
 *
 * 
 * This file is part of FenixEdu Academictreasury.
 *
 * FenixEdu Academictreasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academictreasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academictreasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academictreasury.ui.managedebtreportrequests;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.academictreasury.domain.reports.DebtReportRequest;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequestResultErrorsFile;
import org.fenixedu.academictreasury.domain.reports.DebtReportRequestResultFile;
import org.fenixedu.academictreasury.dto.reports.DebtReportRequestBean;
import org.fenixedu.academictreasury.ui.AcademicTreasuryBaseController;
import org.fenixedu.academictreasury.ui.AcademicTreasuryController;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@SpringFunctionality(app = AcademicTreasuryController.class, title = "label.title.manageDebtReportRequests",
        accessGroup = "treasuryBackOffice")
@RequestMapping(DebtReportRequestController.CONTROLLER_URL)
public class DebtReportRequestController extends AcademicTreasuryBaseController {

    public static final String CONTROLLER_URL = "/academictreasury/managedebtreportrequests/debtreportrequest";
    private static final String JSP_PATH = "/academicTreasury/managedebtreportrequests/debtreportrequest";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private void setDebtReportRequestBean(DebtReportRequestBean bean, Model model) {
        model.addAttribute("debtReportRequestBeanJson", getBeanJson(bean));
        model.addAttribute("debtReportRequestBean", bean);
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(Model model) {
        List<DebtReportRequest> searchdebtreportrequestResultsDataSet = filterSearchDebtReportRequest();

        //add the results dataSet to the model
        model.addAttribute("searchdebtreportrequestResultsDataSet", searchdebtreportrequestResultsDataSet);
        return jspPage("search");
    }

    private Stream<DebtReportRequest> getSearchUniverseSearchDebtReportRequestDataSet() {
        return DebtReportRequest.findAll();
    }

    private List<DebtReportRequest> filterSearchDebtReportRequest() {

        return getSearchUniverseSearchDebtReportRequestDataSet().collect(Collectors.toList());
    }

    private static final String _SEARCH_TO_CANCELREQUEST_ACTION_URI = "/search/cancelRequest/";
    public static final String SEARCH_TO_CANCELREQUEST_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_CANCELREQUEST_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_CANCELREQUEST_ACTION_URI + "{oid}")
    public String processSearchToCancelRequestAction(@PathVariable("oid") final DebtReportRequest debtReportRequest,
            final Model model, final RedirectAttributes redirectAttributes) {

        debtReportRequest.cancelRequest();
        
        return redirect(SEARCH_URL, model, redirectAttributes);
    }

    // @formatter: off

    private static final String _CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + _CREATE_URI;

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {

        final DebtReportRequestBean bean = new DebtReportRequestBean();
        this.setDebtReportRequestBean(bean, model);

        return jspPage("create");
    }

    private static final String _CREATEPOSTBACK_URI = "/createpostback";
    public static final String CREATEPOSTBACK_URL = CONTROLLER_URL + _CREATEPOSTBACK_URI;

    @RequestMapping(value = _CREATEPOSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody String createpostback(@RequestParam(value = "bean", required = false) final DebtReportRequestBean bean,
            final Model model) {

        this.setDebtReportRequestBean(bean, model);
        return getBeanJson(bean);
    }

    @RequestMapping(value = _CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "bean", required = false) DebtReportRequestBean bean, final Model model,
            final RedirectAttributes redirectAttributes) {

        try {

            final DebtReportRequest debtReportRequest = DebtReportRequest.create(bean);

            model.addAttribute("debtReportRequest", debtReportRequest);
            
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (final DomainException de) {
            addErrorMessage(de.getLocalizedMessage(), model);

            return create(model);
        }
    }

    private static final String _DOWNLOAD_URI = "/download";
    public static final String DOWNLOAD_URL = CONTROLLER_URL + _DOWNLOAD_URI;

    @RequestMapping(value = _DOWNLOAD_URI + "/{debtReportRequestResultFileId}", method = RequestMethod.GET)
    public String download(final @PathVariable("debtReportRequestResultFileId") DebtReportRequestResultFile resultFile,
            final Model model, final HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-disposition", "attachment; filename=" + resultFile.getFilename());
        response.setContentLength((int) resultFile.getSize());
        
        try {
            response.getOutputStream().write(resultFile.getContent());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }
    
    private static final String _DOWNLOAD_ERRORS_URI = "/downloadErrors";
    public static final String DOWNLOAD_ERRORS_URL = CONTROLLER_URL + _DOWNLOAD_ERRORS_URI;
    
    @RequestMapping(value = _DOWNLOAD_ERRORS_URI + "/{debtReportRequestResultErrorsFileId}", method = RequestMethod.GET)
    public String downloadErrors(final @PathVariable("debtReportRequestResultErrorsFileId") DebtReportRequestResultErrorsFile resultFile,
            final Model model, final HttpServletResponse response) {
        response.setContentType("text/plain");
        response.setHeader("Content-disposition", "attachment; filename=" + resultFile.getFilename());

        try {
            response.getOutputStream().write(resultFile.getContent());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }
    

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
