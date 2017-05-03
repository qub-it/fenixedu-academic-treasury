<%@page import="org.fenixedu.academictreasury.domain.tuition.TuitionCalculationType"%>
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

	<h3><spring:message code="label.manageTuitionPaymentPlan.createChooseDegreeCurricularPlans" /></h3>
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
	$scope.postBack = createAngularPostbackFunction($scope); 
	
	$scope.backToChooseDegreeCurricularPlans = function() {
		$("#form").attr("action", $("#backUrl").attr('value'));
		$("#form").submit();
	}
	
    $scope.booleanvalues = [ {
        name : '<spring:message code="label.no"/>',
        value : false
    }, {
        name : '<spring:message code="label.yes"/>',
        value : true
    } ];
 	
}]);
</script>

<form id="form" name='form' method="post" class="form-horizontal"
	ng-app="angularAppTuitionPaymentPlan" ng-controller="TuitionPaymentPlanController"
	action='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CREATEPAYMENTPLAN_URL %>/${finantialEntity.externalId}/${executionYear.externalId}'>
	
	<input id="backUrl" type="hidden" name="backUrl" 
		value="${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.BACKTODEGREECURRICULARPLAN_TO_CHOOSE_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}" />
		
	<input type="hidden" name="postback"
		value='${pageContext.request.contextPath}<%= TuitionPaymentPlanControllerStandalone.CREATEDEFINESTUDENTCONDITIONSPOSTBACK_URL %>/${finantialEntity.externalId}/${executionYear.externalId}' />
			
	<input name="bean" type="hidden" value="{{ object }}" />
	
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="form-group row">
				<div class="col-sm-2 control-label"><span id="executionYearLabel"><spring:message code="label.TuitionPaymentPlan.executionYear"/></span></div> 
				
				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<label for="executionYearLabel">${executionYear.qualifiedName}</label>
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.registrationProtocol"/></div> 
				
				<div class="col-sm-6">
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
				
				<div class="col-sm-6">
					<ui-select id="tuitionPaymentPlan_ingression" name="ingression" ng-model="$parent.object.ingression" theme="bootstrap" >
						<ui-select-match >{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="ingression.id as ingression in object.ingressionDataSource | filter: $select.search">
							<span ng-bind-html="ingression.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>				
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionPaymentPlan.withLaboratorialClasses" />
				</div>
	
				<div class="col-sm-2">
					<select id="tuitionInstallmentTariff_withLaboratorialClasses"
						name="withLaboratorialClasses" class="form-control"
						ng-model="object.withLaboratorialClasses" required
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
					</select>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.customized"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_customized" name="customized" class="form-control" ng-model="object.customized"
                    ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">				
					</select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.customized">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.customizedName"/></div> 
				
				<div class="col-sm-6">
					<input id="tuitionPaymentPlan_customizedName" class="form-control" type="text" name="customizedname" ng-model="object.name" />
				</div>
			</div>		
		
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.tuitionCalculationType" />
				</div>
	
				<div class="col-sm-6">
					<ui-select id="tuitionInstallmentTariff_tuitionCalculationType"
						name="tuitioncalculationtype"
						ng-model="$parent.object.tuitionCalculationType" theme="bootstrap"
						required>
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="tuitionCalculationType.id as tuitionCalculationType in object.tuitionCalculationTypeDataSource | filter: $select.search">
						<span
							ng-bind-html="tuitionCalculationType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show='object.tuitionCalculationType == "ECTS" || object.tuitionCalculationType == "UNITS"'>
				<div class="col-sm-2 control-label">
					<span ng-show='object.tuitionCalculationType == "ECTS"'>
						<spring:message code="label.TuitionInstallmentTariff.ectsCalculationType"
							arguments="<%= TuitionCalculationType.ECTS.getDescriptionI18N().getContent() %>" />
					</span>
					<span ng-show='object.tuitionCalculationType == "UNITS"'>
						<spring:message code="label.TuitionInstallmentTariff.ectsCalculationType"
							arguments="<%= TuitionCalculationType.UNITS.getDescriptionI18N().getContent() %>" />
					</span>
				</div>
	
				<div class="col-sm-6">
					<ui-select id="tuitionInstallmentTariff_ectsCalculationType"
						name="ectscalculationtype"
						ng-model="$parent.object.ectsCalculationType" theme="bootstrap"
						ng-required='object.tuitionCalculationType == "ECTS" || object.tuitionCalculationType == "UNITS"'> 
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="ectsCalculationType.id as ectsCalculationType in object.ectsCalculationTypeDataSource | filter: $select.search">
						<span ng-bind-html="ectsCalculationType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.tuitionCalculationType == 'FIXED_AMOUNT' || object.ectsCalculationType == 'FIXED_AMOUNT'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.fixedAmount" />
				</div>
	
				<div class="col-sm-6">
                    <div class="input-group">
                        <div class="input-group-addon">
                            <c:out value="${finantialEntity.finantialInstitution.currency.symbol}" />
                        </div>
					    <input id="tuitionInstallmentTariff_fixedAmount"
						class="form-control" type="number" ng-model="object.fixedAmount" 
						name="fixedamount" pattern="[0-9]+(\.[0-9][0-9]?[0-9]?)?" min="0" step="0.01"
						value='<c:out value='${bean.fixedAmount}'/>' ng-required="object.tuitionCalculationType == 'FIXED_AMOUNT' || object.ectsCalculationType == 'FIXED_AMOUNT'" />
                    </div>
				</div>
			</div>
			<div class="form-group row" ng-show="(object.tuitionCalculationType == 'ECTS' || object.tuitionCalculationType == 'UNITS') && (object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_INDEXED' || object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_COURSE_FUNCTION_COST_INDEXED')">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.factor" />
				</div>
	
				<div class="col-sm-6">
					<input id="tuitionInstallmentTariff_factor" class="form-control"
						type="number" pattern="\d+(\.\d{4})?" min="0" step="0.001"
                        ng-model="object.factor" name="factor"
						value="<c:out value='${bean.factor}'/>" ng-required="(object.tuitionCalculationType == 'ECTS' || object.tuitionCalculationType == 'UNITS') && (object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_INDEXED' || object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_COURSE_FUNCTION_COST_INDEXED')" />
				</div>
			</div>
			<div class="form-group row" ng-show="(object.tuitionCalculationType == 'ECTS' || object.tuitionCalculationType == 'UNITS') && (object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_INDEXED' || object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_COURSE_FUNCTION_COST_INDEXED')">
				<div class="col-sm-2 control-label">
					<span ng-show='object.tuitionCalculationType == "ECTS"'>
						<spring:message code="label.TuitionInstallmentTariff.totalEctsOrUnits"
							arguments="<%= TuitionCalculationType.ECTS.getDescriptionI18N().getContent() %>" />
					</span>
					<span ng-show='object.tuitionCalculationType == "UNITS"'>
						<spring:message code="label.TuitionInstallmentTariff.totalEctsOrUnits"
							arguments="<%= TuitionCalculationType.UNITS.getDescriptionI18N().getContent() %>" />
					</span>
				</div>
	
				<div class="col-sm-6" >
					<input id="tuitionInstallmentTariff_totalEctsOrUnits"
						class="form-control" type="number" pattern="[0-9]?" min="0" step="1"
						ng-model="object.totalEctsOrUnits" name="totalectsorunits"
						value='<c:out value='${bean.totalEctsOrUnits}'/>' ng-required="(object.tuitionCalculationType == 'ECTS' || object.tuitionCalculationType == 'UNITS') && (object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_INDEXED' || object.ectsCalculationType == 'DEFAULT_PAYMENT_PLAN_COURSE_FUNCTION_COST_INDEXED')" pattern="\d+(\.\d{2})?" />
				</div>
			</div>
			
			<!-- MONTANTE MAXIMO -->
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.applyMaximumAmount" />
				</div>
				<div class="col-sm-6">
					<select id="tuitionInstallmentTariff_applyMaximumAmount"
						name="applyMaximumAmount" class="form-control"
						ng-model="object.applyMaximumAmount"
						ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
					</select>
				</div>
			</div>
			
			<div class="form-group row" ng-show="object.applyMaximumAmount">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.maximumAmount" />
				</div>

				<div class="col-sm-6">
                <div class="input-group">
                    <div class="input-group-addon">
                        <c:out value="${finantialEntity.finantialInstitution.currency.symbol}" />
                    </div>
					<input id="tuitionInstallmentTariff_maximumAmount"
						class="form-control" type="number" ng-model="object.maximumAmount"
						name="maximumamount" pattern="[0-9]+(\.[0-9][0-9]?[0-9]?)?" min="0" step="0.01"
						value="<c:out value='${bean.maximumAmount}'/>" ng-required="object.applyMaximumAmount" />
				</div>
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.beginDate" />
				</div>
	
				<div class="col-sm-6">
					<input id="tuitionInstallmentTariff_beginDate" class="form-control" type="text" name="begindate"  
						ng-required="true" bennu-date="object.beginDate" />
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.dueDateCalculationType" />
				</div>
	
				<div class="col-sm-6">
					<ui-select id="tuitionInstallmentTariff_dueDateCalculationType"
						name="duedatecalculationtype"
						ng-model="$parent.object.dueDateCalculationType" theme="bootstrap"
						required>
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="dueDateCalculationType.id as dueDateCalculationType in object.dueDateCalculationTypeDataSource | filter: $select.search">
							<span
								ng-bind-html="dueDateCalculationType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.dueDateCalculationType == 'FIXED_DATE' || object.dueDateCalculationType == 'BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.fixedDueDate" />
				</div>
	
				<div class="col-sm-6">
					<input id="tuitionInstallmentTariff_fixedduedate" class="form-control" 
						type="text" name="fixedduedate"  bennu-date="object.fixedDueDate" ng-required="object.dueDateCalculationType == 'FIXED_DATE' || object.dueDateCalculationType == 'BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION'" />
				</div>
			</div>
			<div class="form-group row" ng-show="object.dueDateCalculationType == 'DAYS_AFTER_CREATION' || object.dueDateCalculationType == 'BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.numberOfDaysAfterCreationForDueDate" />
				</div>
	
				<div class="col-sm-6">
					<input
						id="tuitionInstallmentTariff_numberOfDaysAfterCreationForDueDate"
						class="form-control" type="number" pattern="[0-9]?" min="0" step="1"
						ng-model="object.numberOfDaysAfterCreationForDueDate"
						name="numberofdaysaftercreationforduedate"
						value='<c:out value='${bean.numberOfDaysAfterCreationForDueDate}'/>' ng-required="object.dueDateCalculationType == 'DAYS_AFTER_CREATION' || object.dueDateCalculationType == 'BEST_OF_FIXED_DATE_AND_DAYS_AFTER_CREATION'" />
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.academicalActBlockingOn"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_academicalActBlockingOn" name="academicalactblockingon" class="form-control" ng-model="object.academicalActBlockingOn"
                    ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">				
					</select>
				</div>
			</div>		
			<div class="form-group row">
				<div class="col-sm-2 control-label"><spring:message code="label.TuitionPaymentPlan.blockAcademicActsOnDebt"/></div> 
				
				<div class="col-sm-2">
					<select id="tuitionPaymentPlan_blockAcademicActsOnDebt" name="blockacademicactsondebt" class="form-control" ng-model="object.blockAcademicActsOnDebt"
                    ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
					</select>
				</div>
			</div>		

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.applyInterests" />
				</div>
	
				<div class="col-sm-2">
					<select id="tuitionInstallmentTariff_applyInterests"
						name="applyinterests" class="form-control"
						ng-model="object.applyInterests" required
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
					</select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.interestType" />
				</div>
	
				<div class="col-sm-4">
					<ui-select id="tuitionInstallmentTariff_interestType"
						name="interesttype"
						ng-model="$parent.object.interestType" theme="bootstrap"
						ng-required="object.applyInterests" >
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices
							repeat="interestType.id as interestType in object.interestTypeDataSource | filter: $select.search">
							<span ng-bind-html="interestType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests && object.interestType == 'DAILY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.numberOfDaysAfterDueDate" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_numberOfDaysAfterDueDate"
						class="form-control" type="text"
						ng-model="object.numberOfDaysAfterDueDate"
						name="numberofdaysafterduedate"
						value='<c:out value='${tuitionInstallmentTariff.numberOfDaysAfterDueDate}'/>' ng-required="object.applyInterests && object.interestType == 'DAILY'" pattern="\d+" />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests && object.interestType == 'DAILY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.applyInFirstWorkday" />
				</div>
	
				<div class="col-sm-2">
					<select id="tuitionInstallmentTariff_applyInFirstWorkday"
						name="applyinfirstworkday" class="form-control"
						ng-model="object.applyinfirstworkday" ng-required="object.applyInterests && object.interestType == 'DAILY'"
                        ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
					</select>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests && object.interestType == 'DAILY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.maximumDaysToApplyPenalty" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_maximumDaysToApplyPenalty"
						class="form-control" type="text"
						ng-model="object.maximumDaysToApplyPenalty"
						name="maximumdaystoapplypenalty"
						value='<c:out value='${bean.maximumDaysToApplyPenalty}'/>' ng-required="object.applyInterests && object.interestType == 'DAILY'" pattern="\d+" />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests && object.interestType == 'MONTHLY'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.maximumMonthsToApplyPenalty" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_maximumMonthsToApplyPenalty"
						class="form-control" type="text"
						ng-model="object.maximumMonthsToApplyPenalty"
						name="maximummonthstoapplypenalty"
						value='<c:out value='${bean.maximumMonthsToApplyPenalty}'/>' />
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests && object.interestType == 'FIXED_AMOUNT'">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.interestFixedAmount" />
				</div>
	
				<div class="col-sm-10">
                    <div class="input-group">
	                    <div class="input-group-addon">
	                        <c:out value="${finantialEntity.finantialInstitution.currency.symbol}" />
	                    </div>
						<input id="tuitionInstallmentTariff_interestFixedAmount"
							class="form-control" type="number" pattern="[0-9]+(\.[0-9][0-9]?[0-9]?)?" min="0" step="0.01"
							ng-model="object.interestFixedAmount" name="interestfixedamount"
							value='<c:out value='${bean.interestFixedAmount}'/>' ng-required="object.applyInterests && object.interestType == 'FIXED_AMOUNT'" />
					</div>
				</div>
			</div>
			<div class="form-group row" ng-show="object.applyInterests && (object.interestType == 'DAILY' || object.interestType == 'MONTHLY')">
				<div class="col-sm-2 control-label">
					<spring:message code="label.TuitionInstallmentTariff.rate" />
				</div>
	
				<div class="col-sm-10">
					<input id="tuitionInstallmentTariff_rate" class="form-control"
						type="text" ng-model="object.rate" name="rate"
						value='<c:out value='${bean.rate}'/>' ng-required="object.applyInterests && (object.interestType == 'DAILY' || object.interestType == 'MONTHLY')"   pattern="\d+(\.\d{4})?" min="0" max="100" />
				</div>
			</div>
		</div>
		
		<div class="panel-footer">
			<input type="button" class="btn btn-default" role="button" value="<spring:message code="label.back" />" ng-click="backToChooseDegreeCurricularPlans();"/>
			<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.submit" />"/>
		</div>
	</div>
</form>

<script>
$(document).ready(function() {

});
</script>
