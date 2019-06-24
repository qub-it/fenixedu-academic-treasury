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
        <spring:message code="label.ERPTuitionInfoExportOperation.read.title" />
        <small></small>
    </h1>
</div>

<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
            
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
	<a href="${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.SEARCH_URL %>?erpTuitionInfoDocumentNumber=${operation.erpTuitionInfo.uiDocumentNumber}">
		<spring:message code="label.event.back" /></a>
    &nbsp;|&nbsp; 
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
	<a href="${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.DOWNLOAD_URL %>/${operation.externalId}">
		<spring:message code="label.ERPTuitionInfoExportOperation.download" /></a>
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

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.details" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
                    <tr>
                        <th scope="row" class="col-xs-3">
                        	<spring:message code="label.ERPTuitionInfoExportOperation.executionDate" />
                        </th>
                        <td><joda:format value='${operation.executionDate}' style='SS'/></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3">
                        	<spring:message code="label.ERPTuitionInfoExportOperation.executor" />
                        </th>
                        <td><c:out value='${operation.versioningCreator}' /></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3">
                        	<spring:message code="label.ERPTuitionInfoExportOperation.success" />
                        </th>
                        <td>
                        	<spring:message code='label.${operation.getSuccess()}' />
						</td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3">
                        	<spring:message code="label.ERPTuitionInfoExportOperation.size" /></th>
                        <td><pre><c:out value='${operation.file.getSize()} Bytes' /></pre></td>
                    </tr>

                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.ERPTuitionInfoExportOperation.errorLog" /></th>
                        <td><pre><c:out value='${operation.errorLog}' /></pre></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.ERPTuitionInfoExportOperation.integrationLog" /></th>
                        <td><pre><c:out
                                value='${operation.integrationLog}' /></pre></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3">
                        	<spring:message code="label.ERPTuitionInfoExportOperation.soapOutboundMessage" /></th>
                        <td><a href="${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.SOAPOUTBOUNDMESSAGE_URL %>/${operation.externalId}">
                        	<spring:message code="label.event.integration.erp.downloadFile" />
                        </a></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.ERPTuitionInfoExportOperation.soapInboundMessage" /></th>
                        <td><a href="${pageContext.request.contextPath}<%= ERPTuitionInfoExportOperationController.SOAPINBOUNDMESSAGE_URL %>/${operation.externalId}">
                        	<spring:message code="label.event.integration.erp.downloadFile" />
                        </a></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message
                                code="label.ERPTuitionInfoExportOperation.erpTuitionInfo" /></th>
                        <td>
                            <ul>
	                            <li><c:out value="${operation.erpTuitionInfo.uiDocumentNumber}" /></li>
                            </ul>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<script>
	$(document).ready(function() {
	});
</script>
