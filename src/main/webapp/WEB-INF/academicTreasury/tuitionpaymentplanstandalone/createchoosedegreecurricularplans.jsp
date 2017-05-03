<%@page import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.standalone.TuitionPaymentPlanControllerStandalone"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
${portal.angularToolkit()} 
<%--${portal.toolkit()}--%>

<link href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>						
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message code="label.manageTuitionPaymentPlan.createTuitionPaymentPlan" />
	</h1>
	
	<h3>
		<spring:message code="label.manageTuitionPaymentPlan.createChooseDegreeCurricularPlans" />
	</h3>
</div>

<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">
		
		<c:forEach items="${infoMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">
		
		<c:forEach items="${warningMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">
		
		<c:forEach items="${errorMessages}" var="message"> 
			<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
						${message}
					</p>
		</c:forEach>
		
	</div>	
</c:if>

<script>

angular.module('angularAppTuitionPaymentPlan', ['ngSanitize', 'ui.select','bennuToolkit']).controller('TuitionPaymentPlanController', ['$scope', function($scope) {

 	$scope.object=${tuitionPaymentPlanBeanJson};
	
 	$scope.object.degreeCurricularPlans=[];
 	
 	$scope.postBack = createAngularPostbackFunction($scope); 

	//Begin here of Custom Screen business JS - code
	
	$scope.onDegreeTypeChange = function(degreeType, model) {
		$scope.postBack(model);
	}
 	
	$scope.toggleDegreeCurricularPlans = function toggleSelection(dcpId) {
		var idx = $scope.object.degreeCurricularPlans.indexOf(dcpId);
		
		// is currently selected
		if (idx > -1) {
		  $scope.object.degreeCurricularPlans.splice(idx, 1);
		} else {
			// is newly selected
		  $scope.object.degreeCurricularPlans.push(dcpId);
		}
	};
	
	$scope.cancelCreatePaymentPlan = function() {
		$("#form").attr("action", $("#cancelUrl").attr('value'));
		$("#form").submit();
	}
}]);
</script>

<form id="form" name='form' method="post" class="form-horizontal"
	ng-app="angularAppTuitionPaymentPlan" ng-controller="TuitionPaymentPlanController"
	action='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CREATEDEFINESTUDENTCONDITIONS_URL %>/${finantialEntity.externalId}/${executionYear.externalId}'>
	
	
	<input id="cancelUrl" type="hidden" name="cancelUrl" 
		value='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CHOOSEDEGREECURRICULARPLAN_URL %>/${finantialEntity.externalId}/${executionYear.externalId}' />
	
	<input type="hidden" name="postback"
		value='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CREATECHOOSEDEGREECURRICULARPLANSPOSTBACK_URL %>/${finantialEntity.externalId}/${executionYear.externalId}' />
			
	<input name="bean" type="hidden" value="{{ object }}" />
	
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.executionYear"/></div>
				<div class="col-sm-4">
					<c:out value="${executionYear.qualifiedName}" />
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.degreeType"/></div> 
				
				<div class="col-sm-8">
					<ui-select id="tuitionPaymentPlan_degreeType" name="degreeType" ng-model="$parent.object.degreeType" theme="bootstrap" ng-disabled="disabled" 
						on-select="onDegreeTypeChange($product, $model)" >
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSource | filter: $select.search">
							<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.degreeCurricularPlans"/></div> 
				<div class="col-sm-8">
                    <div ng-hide="object.degreeCurricularPlanDataSource" class="alert alert-warning">
                        <spring:message code="label.TuitionPaymentPlan.degreeCurricularPlanDataSource.is.empty"/>
                    </div>
					<div ng-repeat="dcp in object.degreeCurricularPlanDataSource">
                        <div class="checkbox">
						<label for="{{dcp.id}}">
							<input class="checkbox" name="{{dcp.id}}" type="checkbox" id="{{dcp.id}}" 
							ng-checked="object.degreeCurricularPlans.indexOf(dcp.id) > -1"
							ng-click="toggleDegreeCurricularPlans(dcp.id)" />
							{{dcp.text}}
						</label>
                        </div>
					</div>
					
				</div>
			</div>		
		</div>
		      <div class="panel-footer">
            <button type="button" class="btn btn-default" role="button" ng-click="cancelCreatePaymentPlan();">
            <span class="glyphicon glyphicon-chevron-left"
                aria-hidden="true"></span> &nbsp;
            <spring:message code="label.back" />
            </button>
            <button type="submit" class="btn btn-primary" role="button">
            <spring:message code="label.continue" />
            &nbsp;<span class="glyphicon glyphicon-chevron-right"
                aria-hidden="true"></span>
            </button>
        </div>
	</div>
</form>

<script>
	$(document).ready(function() {});
</script>
