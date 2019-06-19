<%@page import="org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory"%>
<%@page import="org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI"%>
<%@page import="org.fenixedu.academictreasury.ui.managedebtreportrequests.DebtReportRequestController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<spring:url var="datatablesUrl"
	value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl"
	value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl"
	value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl"
	value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
${portal.angularToolkit()}
<%--${portal.toolkit()}--%>

<link
	href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link
	href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link
	href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script
	src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>

<script
	src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script
	src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message
			code="label.manageDebtReportRequests.searchDebtReportRequest" />
		<small></small>
	</h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}<%= DebtReportRequestController.CREATE_URL %>">
			<spring:message code="label.event.create" /></a> |&nbsp;&nbsp;
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

<c:choose>
	<c:when test="${not empty searchdebtreportrequestResultsDataSet}">

		<div class="alert alert-warning" role="alert">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
				<spring:message code="label.DebtReportRequest.zip.file.warning.message" />
			</p>
		</div>

		<datatables:table id="searchdebtreportrequestTable" row="row" data="${searchdebtreportrequestResultsDataSet}" 
			cssClass="table table-bordered table-hover" cdn="false" cellspacing="2">
			<datatables:column cssStyle="width:80px;align:center">
				<datatables:columnHead>
					<spring:message code="label.DebtReportRequest.creationDate" />
				</datatables:columnHead>
				<c:out value='${row.versioningCreationDate.toString("YYYY-MM-dd HH:mm:ss")}' />
			</datatables:column>
			<datatables:column cssStyle="width:80px;align:center">
				<datatables:columnHead>
					<spring:message code="label.DebtReportRequest.requestor" />
				</datatables:columnHead>
				<c:out value='${row.versioningCreator}' />
			</datatables:column>
			<datatables:column cssStyle="width:80px;align:center">
				<datatables:columnHead>
					<spring:message code="label.DebtReportRequest.processing" />
				</datatables:columnHead>
				<c:if test="${row.pending}">
					<spring:message code="label.DebtReportRequest.pending.processing" />
				</c:if>
			</datatables:column>

			<datatables:column cssStyle="width:80px;align:center">
				<datatables:columnHead>
					<spring:message code="label.DebtReportRequest.description" />
				</datatables:columnHead>
				<p>
					<strong><spring:message code="label.DebtReportRequest.beginDate" />:&nbsp;</strong>
					<joda:format value="${row.beginDate}" style="S-" />
				</p>
				<p>
					<strong><spring:message code="label.DebtReportRequest.endDate" />:&nbsp;</strong>
					<joda:format value="${row.endDate}" style="S-" />
				</p>
				<c:forEach var="result" items="${row.debtReportRequestResultFiles}">
					<p><a href="${pageContext.request.contextPath}<%= DebtReportRequestController.DOWNLOAD_URL %>/${result.externalId}">
						<c:out value="${result.getFilename()}" /> (<em><c:out value="${result.filesizeMb}" /> Mb</em>)
					</a></p>
				</c:forEach>
				
				<% if(TreasuryAccessControlAPI.isManager(TreasuryPlataformDependentServicesFactory.implementation().getLoggedUsername())) { %>
				
				<c:forEach var="result" items="${row.debtReportRequestResultErrorsFiles}">
					<p><a href="${pageContext.request.contextPath}<%= DebtReportRequestController.DOWNLOAD_ERRORS_URL %>/${result.externalId}">
						<c:out value="${result.getFilename()}" />
					</a></p>
				</c:forEach>
				
				<% } %>
			</datatables:column>

			<datatables:column cssStyle="width:80px;align:center">
				<c:if test="${row.pending}">
					<a  class="btn btn-default btn-xs" href="${pageContext.request.contextPath}<%= DebtReportRequestController.SEARCH_TO_CANCELREQUEST_ACTION_URL %>/${row.externalId}">
						<spring:message code='label.manageDebtReportRequests.cancelRequest' />
					</a>
				</c:if>
			</datatables:column>
			
		</datatables:table>
		
		<script>
			createDataTables(
					'searchdebtreportrequestTable',
					false,
					false,
					true,
					"${pageContext.request.contextPath}",
					"${datatablesI18NUrl}");
		</script>
		
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
	$(document).ready(function() {
		
		var oTable = $('#searchdebtreportrequestTable').dataTable();
		if(oTable) {
			oTable.fnSort([[0, 'desc']]);			
		}
		
	}); 
</script>

