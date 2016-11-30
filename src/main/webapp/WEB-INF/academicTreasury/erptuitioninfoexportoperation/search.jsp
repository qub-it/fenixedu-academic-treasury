<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoExportOperationController"%>
<%@page import="org.fenixedu.academictreasury.domain.integration.ERPTuitionInfoExportOperation"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>



<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ERPTuitionInfoExportOperation.title" />
        <small></small>
    </h1>
</div>

<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">

        <c:forEach items="${infoMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon glyphicon-ok-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty warningMessages}">
    <div class="alert alert-warning" role="alert">

        <c:forEach items="${warningMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="alert alert-danger" role="alert">

        <c:forEach items="${errorMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>

<div class="panel panel-default">
    <form method="get" class="form-horizontal">
        <div class="panel-body">
        
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.ERPTuitionInfoExportOperation.fromDate" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfoExportOperation_fromDate" class="form-control" type="text"
                        name="fromDate" bennu-date value='<c:out value='${param.fromDate }'/>' />
                </div>
            </div>
            <div class="form-group row">                
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPTuitionInfoExportOperation.toDate" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfoExportOperation_toDate" class="form-control" type="text"
                        name="toDate" bennu-date value='<c:out value='${param.toDate }'/>' />
                </div>
            </div>
            
            <div class="form-group row">                
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.ERPTuitionInfoExportOperation.erpTuitionInfoDocumentNumber" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfoExportOperation_erpTuitionInfoDocumentNumber" class="form-control" type="text"
                        name="erpTuitionInfoDocumentNumber"  value='<c:out value='${param.erpTuitionInfoDocumentNumber}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.ERPTuitionInfoExportOperation.success" />
                </div>

                <div class="col-sm-2">
                    <select id="erpTuitionInfoExportOperation_success"
                        name="success" class="form-control">
                        <option></option>
                        <option value="false"><spring:message code="label.no" /></option>
                        <option value="true"><spring:message code="label.yes" /></option>
                    </select>
                    <script>
						$("#eRPExportOperation_success").select2().select2('val', '<c:out value='${param.success}'/>');
					</script>
                </div>
            </div>
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button"
                value="<spring:message code="label.search" />" />
        </div>
    </form>
</div>

<c:if test="${limit_exceeded}">
    <div class="alert alert-warning" role="alert">

        <p>
            <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
            <spring:message code="label.limitexceeded" arguments="${result.size()};${result_totalCount}" argumentSeparator=";"
                htmlEscape="false" />
        </p>

    </div>
</c:if>
<c:choose>
    <c:when test="${not empty result}">
        <table id="simpletable"
            class="table responsive table-bordered table-hover" width="100%">
            <thead>
                <tr>
                    <%--!!!  Field names here --%>
                    <th><spring:message code="label.ERPTuitionInfoExportOperation.executionDate" /></th>
                    <th><spring:message code="label.ERPTuitionInfoExportOperation.success" /></th>
                    <%-- Operations Column --%>
                    <th></th>
                </tr>
            </thead>
            <tbody>
				<c:forEach var="row" items="${result}">
					<tr>
						<td><c:out value='${row.executionDate.toString("YYYY-MM-dd HH:mm:ss")}'/></td>
						<td><spring:message code='label.${row.getSuccess()}' /></td>
						<td>
							<a class="btn btn-default btn-xs" 
								href="${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.READ_URL %>/${row.externalId}">
								<spring:message code='label.view'/>
							</a>
						</td>
					</tr>
				</c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <div class="alert alert-warning" role="alert">

            <p>
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span>
                <spring:message code="label.noResultsFound" />
            </p>

        </div>

    </c:otherwise>
</c:choose>

<script>
	var searcherpexportoperationDataSet = [
			<c:forEach items="${searcherpexportoperationResultsDataSet}" var="searchResult">
				<%-- Field access / formatting  here CHANGE_ME --%>
				{
				"DT_RowId" : '<c:out value='${searchResult.externalId}'/>',
				"creator" : "<c:out value='${searchResult.versioningCreator}'/>",
				"executiondate" : "",
"finantialinstitution" : "<c:out value='${searchResult.finantialInstitution.name}'/>",
"success" : "<c:if test="${searchResult.success}"></c:if><c:if test="${not searchResult.success}"><spring:message code="label.false" /></c:if>",
// "corrected" : "<c:if test="${searchResult.corrected}"><spring:message code="label.true" /></c:if><c:if test="${not searchResult.corrected}"><spring:message code="label.false" /></c:if>",
"actions" :
" " +
                "" 
			},
            </c:forEach>
    ];
	
	$(document).ready(function() {

	


		var table = $('#searcherpexportoperationTable').DataTable({language : {
			url : "${datatablesI18NUrl}",			
		},
		"columns": [
			{ data: 'executiondate' },
            { data: 'finantialinstitution' },
			{ data: 'success' },
// 			{ data: 'corrected' },
//			{ data: 'creator' },
			{ data: 'actions',className:"all" }
			
		],
		//CHANGE_ME adjust the actions column width if needed
		"columnDefs": [
		//54
		               { "width": "54px", "targets": 3 } 
		             ],
		"data" : searcherpexportoperationDataSet,
		"order": [[ 0, "desc" ]],
		//Documentation: https://datatables.net/reference/option/dom
//"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
"dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
//"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
//"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
        "tableTools": {
            "sSwfPath": "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"        	
        }
		});
		table.columns.adjust().draw();
		
		  $('#searcherpexportoperationTable tbody').on( 'click', 'tr', function () {
		        $(this).toggleClass('selected');
		    } );
		  
	}); 
</script>

