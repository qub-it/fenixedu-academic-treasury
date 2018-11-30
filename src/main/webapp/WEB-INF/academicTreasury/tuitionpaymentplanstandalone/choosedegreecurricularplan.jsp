<%@page
    import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.standalone.TuitionPaymentPlanControllerStandalone"%>
<%@page import="org.fenixedu.academic.domain.ExecutionYear"%>
<%@page import="org.fenixedu.academic.domain.DegreeCurricularPlan"%>
<%@page
    import="org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlanGroup"%>
<%@page
    import="org.fenixedu.academictreasury.domain.tuition.TuitionPaymentPlan"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables"
    uri="http://github.com/dandelion/datatables"%>

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
<%-- ${portal.toolkit()} --%>

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



<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
            code="label.manageTuitionPaymentPlan.chooseDegreeCurricularPlan" />
        <small></small>
    </h1>
</div>

<%-- Choose Execution Year --%>
<div ng-app="changeExample" ng-controller="ExampleController" style="margin-bottom: 20px" class="row">
    <div class="col-sx-2">
        <strong><spring:message code="label.DegreeCurricularPlan.executionYear" /></strong>
    </div>
    <div class="col-sx-2">
        <select id="executionYearOptions"
            class="js-example-basic-single form-control"
            name="executionYearId"
            ng-change="change(executionYearId, '{{ executionYearId }}')"
            ng-model="executionYearId">
            <option value=""></option>
            <c:forEach items="${executionYearOptions}" var="e">
                <option value="${e.externalId}">${e.qualifiedName}</option>
            </c:forEach>
        </select>
    </div>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true">
    </span>&nbsp; <a class=""
        href="${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CHOOSEFINANTIALENTITY_URL %>">
        <spring:message code="label.event.back" />
    </a> |&nbsp;&nbsp; <span class="glyphicon glyphicon-plus-sign"
        aria-hidden="true"></span> &nbsp; <a class=""
        href="${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CREATECHOOSEDEGREECURRICULARPLANS_URL %>/${finantialEntity.externalId}/${executionYear.externalId}">
        <spring:message code="label.event.create" />
    </a> &nbsp;
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
        nder code 'label.DegreeCurricularPlan.executionYear
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

<script type="text/javascript">
angular.module('changeExample', ['bennuToolkit']).controller('ExampleController', ['$scope', function($scope) {
	$scope.change = function(newValue, oldValue) {

		if(oldValue !== "" && newValue !== oldValue) {
			document.location.href=$('input[name="executionYearPostback"]').attr('value') + $scope.executionYearId;
		}
	};
}]);
</script>

<input type="hidden" name="executionYearPostback"
    value="${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CHOOSEDEGREECURRICULARPLAN_URL %>/${finantialEntity.externalId}/" />

<datatables:table id="choosedegreecurricularplanTable" row="dcp"
    data="${choosedegreecurricularplanResultsDataSet}"
    cssClass="table responsive table-bordered table-hover" cdn="false"
    cellspacing="2">

    <datatables:column>
        <datatables:columnHead>
            <spring:message
                code="label.DegreeCurricularPlan.degreeTypeName" />
        </datatables:columnHead>
        <c:out value="${dcp.degree.degreeType.name.content}" />
    </datatables:column>

    <datatables:column>
        <datatables:columnHead>
            <spring:message
                code="label.DegreeCurricularPlan.degreeTypeName" />
        </datatables:columnHead>
        <c:set var="dcp" scope="request" value="${dcp}" />

        <p>
            <strong><c:out value="[${dcp.degree.code}] ${dcp.getPresentationName(executionYear)}" /></strong>
        </p>


        <%
            request.setAttribute(
        							"tuitionPaymentPlanCount",
        							(Long) TuitionPaymentPlan
        									.find(TuitionPaymentPlanGroup
        											.findUniqueDefaultGroupForStandalone()
        											.get(),
        											(DegreeCurricularPlan) request
        													.getAttribute("dcp"),
        											(ExecutionYear) request
        													.getAttribute("executionYear"))
        									.count());

        					if ((Long) request.getAttribute("tuitionPaymentPlanCount") == 0) {
        %>
        <p class="label label-warning">
            <em><spring:message
                    code="label.TuitionPaymentPlan.tuition.count.on.degree.curricular.plan.zero" /></em>
        </p>
        <%
            } else if ((Long) request
        							.getAttribute("tuitionPaymentPlanCount") == 1) {
        %>
        <p class="label label-info">
            <em><spring:message
                    code="label.TuitionPaymentPlan.tuition.count.on.degree.curricular.plan.only.one" /></em>
        </p>
        <%
            } else {
        %>
        <p class="label label-info">
            <em><spring:message
                    code="label.TuitionPaymentPlan.tuition.count.on.degree.curricular.plan"
                    arguments="${tuitionPaymentPlanCount}" /></em>
        </p>
        <%
            }
        %>

    </datatables:column>
    <datatables:column>
        <a
            href="${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.SEARCH_URL %>${finantialEntity.externalId}/${executionYear.externalId}/${dcp.externalId}"
            class="btn btn-default btn-xs"> <spring:message
                code="label.manageTuitionPaymentPlan.chooseDegreeCurricularPlan.choose" />
        </a>
    </datatables:column>

</datatables:table>

<script>
	createDataTables("choosedegreecurricularplanTable", true, false, false, "${pageContext.request.contextPath}", "${datatablesI18NUrl}");
</script>

<script>
$(document).ready(function() {

	$("#executionYearOptions").select2();
    $("#executionYearOptions").select2().select2('val', '<c:out value='${executionYear.externalId}'/>');

});
</script>