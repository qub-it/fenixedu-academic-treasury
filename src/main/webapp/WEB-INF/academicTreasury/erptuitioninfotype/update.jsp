<%@page import="org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoTypeBean"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoTypeController"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>

<script type="text/javascript" src="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"></script>
<script type="text/javascript" src="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></script>

<link rel="stylesheet" href="/CSS/dataTables/dataTables.bootstrap.min.css" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

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
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ERPTuitionInfoType.update.title" /> <small>[<c:out value="${executionYear.qualifiedName}" />]</small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;
    <a href="${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.SEARCH_URL %>/${executionYear.externalId}">
    	<spring:message code="label.event.back" />
    </a>&nbsp;
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

angular.module('angularApp', ['ngSanitize', 'ui.select']).controller('Controller', ['$scope', function($scope) {

	 $scope.booleanvalues = [ 
		 { name : '<spring:message code="label.no"/>', value : false}, 
		 { name : '<spring:message code="label.yes"/>', value : true} ];
	 
 	$scope.object=${beanJson};
	$scope.postBack = createAngularPostbackFunction($scope); 

	$scope.onDegreeTypeChangeInAddDegrees = function(model) {
		$scope.$apply();
		$('#add-degrees-form').attr('action', $("#degree-type-postback").attr('value'));
		$('#add-degrees-form').submit();
	}
	
	$scope.onDegreeTypeChangeInAddDegreeCurricularPlans = function(model) {
		$scope.$apply();
		$('#add-degree-curricular-plan-form').attr('action', $("#degree-type-postback").attr('value'));
		$('#add-degree-curricular-plan-form').submit();
	}
	
	$scope.onDegreeInfoOptionChange = function(model, option) {
		$('#choose-degree-info-form').attr('action', $('#choose-degree-info-form-url').attr('value') + '/' + option);
		$('#choose-degree-info-form').submit();
	}
	
	$scope.onTuitionPaymentPlanGroupChange = function(model) {
		$scope.$apply();
		$('#payment-plan-group-form').attr('action', $("#payment-plan-group-postback").attr('value'));
		$('#payment-plan-group-form').submit();
	}
	
	$scope.toggleDegrees = function(degreeId) {
		var idx = $scope.object.selectedDegrees.indexOf(degreeId);
		
		// is currently selected
		if (idx > -1) {
		  $scope.object.selectedDegrees.splice(idx, 1);
		} else {
			// is newly selected
		  $scope.object.selectedDegrees.push(degreeId);
		}
	};

	$scope.toggleDegreeCurricularPlans = function(dcpId) {
		var idx = $scope.object.selectedDegreeCurricularPlans.indexOf(dcpId);
		
		// is currently selected
		if (idx > -1) {
		  $scope.object.selectedDegreeCurricularPlans.splice(idx, 1);
		} else {
			// is newly selected
		  $scope.object.selectedDegreeCurricularPlans.push(dcpId);
		}
	};

	
}]);
</script>

<div ng-app="angularApp" ng-controller="Controller">

	<div class="panel panel-default">
		<div class="panel-body">
	
		<form id="payment-plan-group-form" name='form' method="post" class="form-horizontal">
		
			<input name="bean" type="hidden" value="{{ object }}" />

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.tuitionPaymentPlanGroup" />
				</div>
				
				<div class="col-sm-6">
					<c:out value="${bean.tuitionPaymentPlanGroup.name.content}" />
				</div>
			</div>
		
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.erpTuitionProduct" />
				</div>
				
				<div class="col-sm-6">
					<c:out value="${bean.erpTuitionInfoProduct.name}" />
				</div>
			</div>
		</form>
	
	</div>

	<div class="panel panel-default">
		<div class="panel-body">

			<h3><spring:message code="label.ERPTuitionInfo.tuitionProducts" /></h3>
			
			<c:choose>
				<c:when test="${not empty bean.tuitionProducts}">
					<table class="table responsive table-bordered table-hover" width="100%">
						<thead>
							<tr>
								<th><spring:message code="label.ERPTuitionInfoType.tuitionProduct" /></th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="entry" items="${bean.tuitionProducts}" varStatus="loopStatus">
								<tr>
									<td><c:out value="${entry.name.content}" /></td>
			
									<td>
										<form method="post" class="form-horizontal"
											action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_TUITION_PRODUCT_URL %>/${executionYear.externalId}/${entry.externalId}'>
											<input name="bean" type="hidden" value="{{ object }}" />
											
											<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.remove" />" />
										</form>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
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
			
			<form method="post" class="form-horizontal" action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADD_TUITION_PRODUCT_URL %>/${executionYear.externalId}'>
			
				<input name="bean" type="hidden" value="{{ object }}" />
			
				<div class="form-group row">
					<div class="col-sm-2 control-label">
						<spring:message code="label.ERPTuitionInfoType.tuitionProduct" />
					</div>
					
					<div class="col-sm-6">
						<%-- Relation to side 1 drop down rendered in input --%>
						<ui-select id="erpTuitionInfoType_product" name="product" ng-model="$parent.object.selectedTuitionProduct" theme="bootstrap" ng-disabled="disabled">
							<ui-select-match>{{$select.selected.text}}</ui-select-match>
							<ui-select-choices repeat="product.id as product in object.productDataSource | filter: $select.search">
								<span ng-bind-html="product.text | highlight: $select.search"></span>
							</ui-select-choices>
						</ui-select>
					</div>
				</div>
				<div class="form-group row">
					<div class="col-sm-8">
		                <input style="float:right;" type="submit" class="btn btn-default" role="button" value="<spring:message code="label.add" />" />
	                </div>
	            </div>
						
			</form>

		</div>
	</div>
	
	<c:if test="${bean.tuitionPaymentPlanGroup.forRegistration}">
	<div class="panel panel-default">
		<div class="panel-body">
	
			<h3 style="margin-top:100px;">
			    <spring:message code="label.ERPTuitionInfoType.degreeInformation.title" />
			</h3>
			
			<c:choose>
				<c:when test="${bean.isDegreeInformationDefined()}">
					<table class="table responsive table-bordered table-hover" width="100%">
						<thead>
							<tr>
								<th><spring:message code="label.ERPTuitionInfoType.degreeInformation" /></th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="entry" items="${bean.degreeTypes}" varStatus="loopStatus">
								<tr>
									<td><c:out value="${entry.name.content}" /></td>
			
									<td>
										<form method="post" class="form-horizontal"
											action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_DEGREE_TYPE_URL %>/${executionYear.externalId}/${entry.externalId}'>
											<input name="bean" type="hidden" value="{{ object }}" />
											
											<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.remove" />" />
										</form>
									</td>
								</tr>
							</c:forEach>
			
							<c:forEach var="entry" items="${bean.degrees}" varStatus="loopStatus">
								<tr>
									<td><c:out value="${entry.presentationNameI18N.content}" /></td>
			
									<td>
										<form method="post" class="form-horizontal"
											action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_DEGREE_URL %>/${executionYear.externalId}/${entry.externalId}'>
											<input name="bean" type="hidden" value="{{ object }}" />
											
											<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.remove" />" />
										</form>
									</td>
								</tr>
							</c:forEach>
			
							<c:forEach var="entry" items="${bean.degreeCurricularPlans}" varStatus="loopStatus">
								<tr>
									<td><c:out value="${entry.degree.presentationNameI18N.content}" /> - <c:out value="${entry.name}" /></td>
			
									<td>
										<form method="post" class="form-horizontal"
											action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_DEGREE_CURRICULAR_PLAN_URL %>/${executionYear.externalId}/${entry.externalId}'>
											<input name="bean" type="hidden" value="{{ object }}" />
											
											<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.remove" />" />
										</form>
									</td>
								</tr>
							</c:forEach>
			
						</tbody>
					</table>
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
			
			<h4 style="margin-top:100px;">
			    <spring:message code="label.ERPTuitionInfoType.addDegreeInformation" />
			</h4>

			<div class="form-group row">
				<div class="col-sm-6">
					<form id="choose-degree-info-form" class="form-horizontal" method="post">

						<input id="choose-degree-info-form-url" name="actionUrl" type="hidden" 
							value="${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CHOOSE_DEGREE_INFORMATION_TO_ADD_URL %>/${executionYear.externalId}" />	
						<input name="bean" type="hidden" value="{{ object }}" />

					</form>
					
					<label class="radio-inline">
						<c:choose>
							<c:when test="${bean.isDegreeInfoSelectOptionDegreeType()}">
								<input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION %>" 
									checked="checked" ng-click="onDegreeInfoOptionChange($model, '<%= ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION %>')">
							</c:when>
							<c:otherwise>
								<input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION %>" 
									ng-click="onDegreeInfoOptionChange($model, '<%= ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION %>')">
							</c:otherwise>
						</c:choose>
					  <spring:message  code="label.ERPTuitionInfoType.degreeType.option" />
					</label>
					<label class="radio-inline">
						<c:choose>
							<c:when test="${bean.isDegreeInfoSelectOptionDegrees()}">
							  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREES_OPTION %>" 
							  	checked="checked" ng-click="onDegreeInfoOptionChange($model, '<%= ERPTuitionInfoTypeBean.DEGREES_OPTION %>')">
							</c:when>
							<c:otherwise>
							  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREES_OPTION %>" 
							  	ng-click="onDegreeInfoOptionChange($model, '<%= ERPTuitionInfoTypeBean.DEGREES_OPTION %>')">
							</c:otherwise>
						</c:choose>
					  <spring:message  code="label.ERPTuitionInfoType.degree.option" />
					</label>
					<label class="radio-inline">
						<c:choose>
							<c:when test="${bean.isDegreeInfoSelectOptionDegreeCurricularPlans()}">
								  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTION %>" 
								  	checked="checked" ng-click="onDegreeInfoOptionChange($model, '<%= ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTION %>')">
							</c:when>
							<c:otherwise>
								  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTION %>" 
								  	ng-click="onDegreeInfoOptionChange($model, '<%= ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTION %>')">
							</c:otherwise>
						</c:choose>
					
					  <spring:message  code="label.ERPTuitionInfoType.degreeCurricularPlan.option" />
					</label>
				</div>
				
			</div>
			
			<c:choose>
				<c:when test="${bean.isDegreeInfoSelectOptionDegreeType()}">
			
					<form method="post" class="form-horizontal" action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADD_DEGREE_TYPE_URL %>/${executionYear.externalId}'>
					
						<input name="bean" type="hidden" value="{{ object }}" /> 

						<div class="form-group row">
							<div class="col-sm-2 control-label">
								<spring:message code="label.ERPTuitionInfoType.degreeType" />
							</div>
			
							<div class="col-sm-6">
								<ui-select name="degreeType" ng-model="$parent.object.selectedDegreeType" theme="bootstrap" ng-disabled="disabled"> 
									<ui-select-match>{{$select.selected.text}}</ui-select-match>
									<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSource | filter: $select.search">
										<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
									</ui-select-choices>
								</ui-select>
							</div>
						</div>
			
						<div class="form-group row">
							<div class="col-sm-8">
								<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.add" />" style="float: right;" />
							</div>
						</div>
					
					</form>
				</c:when>
				
				<c:when test="${bean.isDegreeInfoSelectOptionDegrees()}">
				
					<form id="add-degrees-form" method="post" class="form-horizontal" action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADD_DEGREES_URL %>/${executionYear.externalId}'>
					
						<input name="bean" type="hidden" value="{{ object }}" /> 
						
						<input id="degree-type-postback" type="hidden" name="postback"
							value='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CHOOSE_DEGREE_TYPE_POSTBACK_URL %>/${executionYear.externalId}' />
					
					
						<div class="form-group row">
							<div class="col-sm-2 control-label">
								<spring:message code="label.ERPTuitionInfoType.degreeType" />
							</div>
			
							<div class="col-sm-6">
								<ui-select name="degreeType" ng-model="$parent.object.selectedDegreeType" theme="bootstrap" ng-disabled="disabled" on-select="onDegreeTypeChangeInAddDegrees($model)"> 
									<ui-select-match>{{$select.selected.text}}</ui-select-match>
									<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSource | filter: $select.search">
										<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
									</ui-select-choices>
								</ui-select>
							</div>
						</div>
						<div class="form-group row">
							<div class="col-sm-2 control-label">
								<spring:message code="label.ERPTuitionInfoType.degrees" />
							</div>
							<div class="col-sm-6">
			
								<div ng-hide="object.degreeDataSource" class="alert alert-warning">
									<spring:message code="label.ERPTuitionInfoType.degreeDataSource.is.empty" />
								</div>
								<div ng-repeat="d in object.degreeDataSource">
									<div class="checkbox">
										<input class="checkbox pull-left" name="{{d.id}}" type="checkbox" id="{{d.id}}"
											ng-checked="object.degrees.indexOf(d.id) > -1" ng-click="toggleDegrees(d.id)" />
										<span><label for="{{d.id}}">{{d.text}}</label></span>
									</div>
								</div>
			
							</div>
						</div>
			
						<div class="form-group row">
							<div class="col-sm-8">
								<input type="submit" class="btn btn-default" role="button"
									value="<spring:message code="label.add" />" style="float: right;" />
							</div>
						</div>
					
					</form>
					
				</c:when>
				
				<c:when test="${bean.isDegreeInfoSelectOptionDegreeCurricularPlans()}">
				
					<form id="add-degree-curricular-plan-form" method="post" class="form-horizontal"
						action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADD_DEGREE_CURRICULAR_PLANS_URL %>/${executionYear.externalId}'>
					
						<input name="bean" type="hidden" value="{{ object }}" /> 
						
						<input id="degree-type-postback" type="hidden" name="postback"
							value='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CHOOSE_DEGREE_TYPE_POSTBACK_URL %>/${executionYear.externalId}' />
					
					
							<div class="form-group row">
								<div class="col-sm-2 control-label">
									<spring:message code="label.ERPTuitionInfoType.degreeType" />
								</div>
				
								<div class="col-sm-6">
									<ui-select name="degreeType" ng-model="$parent.object.selectedDegreeType" theme="bootstrap" ng-disabled="disabled" on-select="onDegreeTypeChangeInAddDegreeCurricularPlans($model)"> 
										<ui-select-match>{{$select.selected.text}}</ui-select-match>
										<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSource | filter: $select.search">
											<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
										</ui-select-choices>
									</ui-select>
								</div>
							</div>
							<div class="form-group row">
								<div class="col-sm-2 control-label">
									<spring:message code="label.ERPTuitionInfoType.degreeCurricularPlans" />
								</div>
								<div class="col-sm-8">
									<div ng-hide="object.degreeCurricularPlanDataSource" class="alert alert-warning">
										<spring:message code="label.ERPTuitionInfoType.degreeCurricularPlanDataSource.is.empty" />
									</div>
									<div ng-repeat="dcp in object.degreeCurricularPlanDataSource">
										<div class="checkbox">
											<input class="checkbox pull-left" name="{{dcp.id}}" type="checkbox" id="{{dcp.id}}"
												ng-checked="object.degreeCurricularPlans.indexOf(dcp.id) > -1"
												ng-click="toggleDegreeCurricularPlans(dcp.id)" /> 
											<span><label for="{{dcp.id}}">{{dcp.text}}</label></span>
										</div>
									</div>
				
								</div>
							</div>
							<div class="form-group row">
								<div class="col-sm-10">
									<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.add" />" style="float: right;" />
								</div>
							</div>
					</form>
					
				</c:when>
					
				<c:otherwise>
					<div class="alert alert-warning" role="alert">
						<p>
							<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
							<spring:message code="label.ERPTuitionInfoType.choose.option.to.add.degree.info" />
						</p>
					</div>
				</c:otherwise>		
			</c:choose>

		</div>
	</div>
	
	</c:if>
	<div class="panel panel-default">
		<div class="panel-body">
	
		<form id="form" name='form' method="post" class="form-horizontal"
			action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.UPDATE_URL %>/${executionYear.externalId}'>
		
			<input name="bean" type="hidden" value="{{ object }}" />
		
			<div class="panel-footer">
				<input type="submit" class="btn btn-default" role="button"
					value="<spring:message code="label.submit" />" />
			</div>
		</form>
	
	</div>

</div>