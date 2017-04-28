<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoTypeController"%>
<%@page import="org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoType"%>
<%@page import="org.fenixedu.academictreasury.ui.tuitiondebtcreation.standalone.OtherTuitionDebtCreationBeanController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

${portal.angularToolkit()}

<link href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message code="label.TuitionDebtCreation.createTuitionDebtCreationBean" />
		<small></small>
	</h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span> &nbsp; <a class=""
		href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}">
		<spring:message code="label.event.back" />
	</a> &nbsp;
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
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">

		<c:forEach items="${errorMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>

<script>
	angular.module('angularApp',
			[ 'ngSanitize', 'ui.select', 'bennuToolkit' ]).controller(
			'angularController', [ '$scope', function($scope) {

				$scope.object = $
				{
					tuitionDebtCreationBeanJson
				}
				;
				// $scope.$apply();
				$scope.booleanvalues = [ {
					name : '<spring:message code="label.no"/>',
					value : false
				}, {
					name : '<spring:message code="label.yes"/>',
					value : true
				} ];

				$scope.postBack = createAngularPostbackFunction($scope);

				$scope.onRegistrationChange = function(model) {
					$scope.postBack(model);
				}
			} ]);
</script>

<form name='form' method="post" class="form-horizontal" ng-app="angularApp" ng-controller="angularController"
	action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CREATE_URL %>/${debtAccount.externalId}'>

	<input type="hidden" name="postback" value='${pageContext.request.contextPath}<%=  %>/${debtAccount.externalId}' />

	<input name="bean" type="hidden" value="{{ object }}" />
	<div class="panel panel-default">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoTypeBean.tuitionType" />
				</div>

				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="erpTuitionInfoTypeBean_tuitionType" name="mode" ng-model="$parent.object.tuitionType"
						theme="bootstrap" ng-disabled="disabled" on-select="postBack($model)">
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="tuitionType.id as tuitionType in object.tuitionTypeDataSourceList | filter: $select.search">
							<span ng-bind-html="tuitionType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoTypeBean.product" />
				</div>

				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="erpTuitionInfoTypeBean_product" name="product" ng-model="$parent.object.product"
						theme="bootstrap" ng-disabled="disabled" on-select="postBack($model)">
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="product.id as product in object.productDataSourceList | filter: $select.search">
							<span ng-bind-html="product.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoTypeBean.degreeType" />
				</div>

				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="erpTuitionInfoTypeBean_degreeType" name="degreeType" ng-model="$parent.object.product"
						theme="bootstrap" ng-disabled="disabled" on-select="postBack($model)">
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSourceList | filter: $select.search">
							<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoTypeBean.degree" />
				</div>

				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="erpTuitionInfoTypeBean_degree" name="degree" ng-model="$parent.object.product"
						theme="bootstrap" ng-disabled="disabled" on-select="postBack($model)">
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="degree.id as degree in object.degreeDataSourceList | filter: $select.search">
							<span ng-bind-html="degree.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			
		</div>
		<div class="panel-footer">
			<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.submit" />" />
		</div>
	</div>
</form>

<script>
	$(document).ready(function() {
	});
</script>
