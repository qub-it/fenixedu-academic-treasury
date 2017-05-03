<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoExportOperationController"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoController"%>
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
        <spring:message code="label.ERPTuitionInfo.title" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
   	<a href="${pageContext.request.contextPath}/academictreasury/erptuitioninfo/pendingdocuments">
   		<spring:message code="label.ERPTuitionInfo.search.pending.documents" />
    </a>
    
    <c:if test="${not empty customer}">
	    &nbsp;|&nbsp;
	    <span class="glyphicon glyphicon-upload" aria-hidden="true"></span>&nbsp;
	    <a href="${pageContext.request.contextPath}<%= ERPTuitionInfoController.CREATE_URL %>/${customer.externalId}">
	    	<spring:message code="label.ERPTuitionInfo.create" />
	    </a>
    </c:if>
</div>

<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">

        <c:forEach items="${infoMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span> ${message}
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
                    <spring:message code="label.ERPTuitionInfo.fromDate" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfo_fromDate" class="form-control" type="text"
                        name="fromDate" bennu-date value='<c:out value='${param.fromDate }'/>' />
                </div>
            </div>
            <div class="form-group row">                
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPTuitionInfo.toDate" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfo_toDate" class="form-control" type="text"
                        name="toDate" bennu-date value='<c:out value='${param.toDate }'/>' />
                </div>
            </div>
            
			<div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPTuitionInfo.executionYear" />
                </div>
                <div class="col-sm-3">
                    <select id="erpTuitionInfo_executionYear" name="executionYearId">
	                        <option></option>
	                    <c:forEach var="executionYear" items="${executionYearsList}">
	                        <option value="${executionYear.externalId}">
	                        	<c:out value="${executionYear.qualifiedName}" />
	                        </option>
	                    </c:forEach>
                    </select>
                </div>
				<script>
					$(document).ready(function() {
						$("#erpTuitionInfo_executionYear").select2().select2('val', '${param.executionYearId != null ? param.executionYearId : null}');
					});
				</script>
			</div>        	
        	
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.ERPTuitionInfo.studentNumber" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfo_studentNumber" class="form-control" type="text" name="studentNumber" 
                        value='<c:out value='${param.studentNumber != null ? param.studentNumber : studentNumber}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.ERPTuitionInfo.customerName" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfo_customerName" class="form-control" type="text" name="customerName" 
                        value='<c:out value='${param.customerName != null ? param.customerName : customerName}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.ERPTuitionInfo.erpTuitionDocumentNumber" />
                </div>
                <div class="col-sm-3">
                    <input id="erpTuitionInfo_erpTuitionDocumentNumber" class="form-control" type="text" name="erpTuitionDocumentNumber"
                        value='<c:out value='${param.erpTuitionDocumentNumber}'/>' />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPTuitionInfo.pendingToExport" />
                </div>

                <div class="col-sm-2">
                    <select id="erpTuitionInfo_pendingToExport" name="pendingToExport" class="form-control">
                        <option></option>
                        <option value="false"><spring:message code="label.no" /></option>
                        <option value="true"><spring:message code="label.yes" /></option>
                    </select>
					<script>
						$("#erpTuitionInfo_pendingToExport").select2()
							.select2('val', '<c:out value='${not empty param.pendingToExport ? param.pendingToExport : null }'/>');
					</script>
                </div>
            </div>
            
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.ERPTuitionInfo.exportationSuccess" />
                </div>

                <div class="col-sm-2">
                    <select id="erpTuitionInfo_exportationSuccess" name="exportationSuccess" class="form-control">
                        <option></option>
                        <option value="false"><spring:message code="label.no" /></option>
                        <option value="true"><spring:message code="label.yes" /></option>
                    </select>
					<script>
						$("#erpTuitionInfo_exportationSuccess").select().select('val', '<c:out value='${not empty param.exportationSuccess ? param.exportationSuccess : null }'/>');
					</script>
                </div>
            </div>
            
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.search" />" />
        </div>
    </form>
</div>

<c:if test="${limit_exceeded}">
    <div class="alert alert-warning" role="alert">
        <p>
            <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
            <spring:message code="label.limitexceeded" arguments="${result.size()};${result_totalCount}" argumentSeparator=";" htmlEscape="false" />
        </p>
    </div>
</c:if>


<c:choose>
	<c:when test="${not empty result}">
		<table id="simpletable" class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message code="label.ERPTuitionInfo.creationDate" /></th>
					<th><spring:message code="label.ERPTuitionInfo.documentNumber" /></th>
					<th><spring:message code="label.ERPTuitionInfo.data" /></th>
					<th><spring:message code="label.ERPTuitionInfo.tuitionTotalAmount" /></th>
					<th><spring:message code="label.ERPTuitionInfo.tuitionDeltaAmount" /></th>
					<th><spring:message code="label.ERPTuitionInfo.pendingToExport" /></th>
					<th><spring:message code="label.ERPTuitionInfo.exportationSuccess" /></th>
					<%-- Operations Column --%>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="row" items="${result}">
					<tr>
						<td><c:out value='${row.creationDate.toString("YYYY-MM-dd HH:mm:ss")}' /></td>
						<td><c:out value='${row.uiDocumentNumber}' /></td>
						<td>
							<p>
								<strong><spring:message code="label.ERPTuitionInfo.customer" />:</strong>
								<c:out value='${row.customer.businessIdentification} - ${row.customer.name}' />
							</p>
							<p>
								<strong><spring:message code="label.ERPTuitionInfo.executionYear" />:&nbsp;</strong>
								<c:out value='${row.executionYear.qualifiedName}' />
							</p>
							<p>
								<strong><spring:message code="label.ERPTuitionInfo.product" />:&nbsp;</strong>
								<c:out value='${row.product.name.content}' />
							</p>
						</td>
						<td><c:out value='${row.tuitionTotalAmount}' /></td>
						<td><c:out value='${row.tuitionDeltaAmount}' /></td>
						<td><spring:message code='label.${row.pendingToExport}' /></td>
						<td><spring:message code='label.${row.exportationSuccess}' /></td>
						<td>
							<a class="btn-default btn btn-xs" 
								href="${pageContext.request.contextPath}<%= ERPTuitionInfoController.READ_URL %>/${row.externalId}">
								<spring:message code='label.view' />
							</a>
							<a class="btn-default btn btn-xs" 
								href="${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.SEARCH_URL %>?erpTuitionInfoDocumentNumber=${row.uiDocumentNumber}">
								<spring:message code='label.ERPTuitionInfo.logs' />
							</a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<script>
			$(document).ready(function() {
				var table = $('#simpletable').DataTable({
					language : {
						url : "${datatablesI18NUrl}",
					},
					"paging": false,
					//CHANGE_ME adjust the actions column width if needed
					"columnDefs" : [],
					"order": [[ 0, "desc" ]],
					"dom" : '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
					"tableTools" : {
						"sSwfPath" : "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"
					}
				});
				
				table.columns.adjust().draw();

				$('#simpletable tbody').on('click', 'tr', function() {
					$(this).toggleClass('selected');
				});
			});
		</script>
	</c:when>
	<c:otherwise>
		<div class="alert alert-warning" role="alert">

			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
				<spring:message code="label.noResultsFound" />
			</p>

		</div>

	</c:otherwise>
</c:choose>

