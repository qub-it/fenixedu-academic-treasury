<%@page import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.DegreeCurricularPlanController"%>
<%@page import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.TuitionPaymentPlanController"%>
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

${portal.angularToolkit()} 

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
	<h1><spring:message code="label.manageTuitionPaymentPlan.createTuitionPaymentPlan" /></h1>

	<h3><spring:message code="label.manageTuitionPaymentPlan.createDefineStudentConditions" /></h3>
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

 	$scope.object=angular.fromJson('${tuitionPaymentPlanBeanJson}');
	$scope.postBack = createAngularPostbackFunction($scope); 
	
	if(!$scope.object.registrationRegimeType) {
		$scope.object.registrationRegimeType = "";
	}
	
	$scope.backToChooseDegreeCurricularPlans = function() {
		$("#form").attr("action", $("#backUrl").attr('value'));
		$("#form").submit();
	}
 	
}]);
</script>

<form id="form" name='form' method="post" class="form-horizontal"
	ng-app="angularAppTuitionPaymentPlan" ng-controller="TuitionPaymentPlanController"
	action='${pageContext.request.contextPath}<%= TuitionPaymentPlanController.CREATEINSERTINSTALLMENTS_URL %>/${finantialEntity.externalId}/${executionYear.externalId}'>
	
	<input id="backUrl" type="hidden" name="backUrl" 
		value="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}" />
		
	<input type="hidden" name="postback"
		value='${pageContext.request.contextPath}<%= TuitionPaymentPlanController.CREATEINSERTINSTALLMENTSPOSTBACK_URL %>/${finantialEntity.externalId}/${executionYear.externalId}' />
			
	<input name="bean" type="hidden" value="{{ object }}" />
	
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="form-group row">
				<div class="col-sm-2 control-label"><span id="executionYearLabel"><spring:message code="label.TuitionPaymentPlan.executionYear"/></span></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<label for="executionYearLabel">${executionYear.qualifiedName}</label>
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.defaultPaymentPlan"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_defaultPaymentPlan" name="defaultpaymentplan" class="form-control" ng-model="object.defaultPaymentPlan">
						<option value="false"><spring:message code="label.no"/></option>
						<option value="true"><spring:message code="label.yes"/></option>				
					</select>
					<script>
						$("#tuitionPaymentPlan_defaultPaymentPlan").select2().select2('val', '<c:out value='${bean.defaultPaymentPlan }'/>');
					</script>	
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.registrationRegimeType"/></div> 
				
				<div class="col-sm-4">
					<ui-select id="tuitionPaymentPlan_registrationRegimeType" name="registrationregimetype" ng-model="$parent.object.registrationRegimeType" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="registrationRegimeType.id as registrationRegimeType in object.registrationRegimeTypeDataSource | filter: $select.search">
							<span ng-bind-html="registrationRegimeType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.registrationProtocol"/></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="tuitionPaymentPlan_registrationProtocol" name="registrationprotocol" ng-model="$parent.object.registrationProtocol" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="registrationProtocol.id as registrationProtocol in object.registrationProtocolDataSource | filter: $select.search">
							<span ng-bind-html="registrationProtocol.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.ingression"/></div> 
				
				<div class="col-sm-4">
					<ui-select id="tuitionPaymentPlan_ingression" name="ingression" ng-model="$parent.object.ingression" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="ingression.id as ingression in object.ingressionDataSource | filter: $select.search">
							<span ng-bind-html="ingression.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>	
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.curricularYear"/></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="tuitionPaymentPlan_curricularYear" name="curricularyear" ng-model="$parent.object.curricularYear" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="curricularYear.id as curricularYear in object.curricularYearDataSource | filter: $select.search">
							<span ng-bind-html="curricularYear.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.curricularSemester"/></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="tuitionPaymentPlan_semester" name="semester" ng-model="$parent.object.executionSemester" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="semester.id as semester in object.semesterDataSource | filter: $select.search">
							<span ng-bind-html="semester.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>		
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.statuteType"/></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="tuitionPaymentPlan_statuteType" name="statuteType" ng-model="$parent.object.statuteType" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="statuteType.id as statuteType in object.statuteTypeDataSource | filter: $select.search">
							<span ng-bind-html="statuteType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>	
				</div>
			</div>			
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.firstTimeStudent"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_firstTimeStudent" name="firsttimestudent" class="form-control" ng-model="object.firstTimeStudent">
						<option value="false"><spring:message code="label.no"/></option>
						<option value="true"><spring:message code="label.yes"/></option>				
					</select>
					<script>
						$("#tuitionPaymentPlan_firstTimeStudent").select2().select2('val', '<c:out value='${bean.firstTimeStudent }'/>');
					</script>	
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.customized"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_customized" name="customized" class="form-control" ng-model="object.customized">
						<option value="false"><spring:message code="label.no"/></option>
						<option value="true"><spring:message code="label.yes"/></option>				
					</select>
					<script>
						$("#tuitionPaymentPlan_customized").select2().select2('val', '<c:out value='${bean.customized}'/>');
					</script>	
				</div>
			</div>
			<div class="form-group row" ng-show="object.customized || (object.customized == 'true')">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.customizedName"/></div> 
				
				<div class="col-sm-10">
					<input id="tuitionPaymentPlan_customizedName" class="form-control" type="text" name="customizedname" ng-model="object.name" />
				</div>
			</div>	
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.payorDebtAccount"/></div> 
				
				<div class="col-sm-4">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="tuitionPaymentPlan_payorDebtAccount" name="payorDebtAccount" ng-model="$parent.object.payorDebtAccount" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="payorDebtAccount.id as payorDebtAccount in object.payorDebtAccountDataSource | filter: $select.search">
							<span ng-bind-html="payorDebtAccount.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>			
		</div>
		
		<div class="panel-footer">
			<button type="button" class="btn btn-default" role="button" ng-click="backToChooseDegreeCurricularPlans();">
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
$(document).ready(function() {

});
</script>
