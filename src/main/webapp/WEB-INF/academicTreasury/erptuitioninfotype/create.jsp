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
        <spring:message code="label.ERPTuitionInfoType.create.title" /> <small>[<c:out value="${executionYear.qualifiedName}" />]</small>
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
	
}]);
</script>

<div ng-app="angularApp" ng-controller="Controller">

<h3><spring:message code="label.ERPTuitionInfo.tuitionProducts" /></h3>

<c:choose>
	<c:when test="${not empty bean.products}">
		<table class="table responsive table-bordered table-hover" width="100%">
			<thead>
				<tr>
					<th><spring:message code="label.ERPTuitionInfoType.tuitionProduct" /></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="entry" items="${bean.products}" varStatus="loopStatus">
					<tr>
						<td><c:out value="${entry.name.content}" /></td>

						<td>
							<form method="post" class="form-horizontal"
								action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_TUITION_PRODUCT_URL %>/${executionYear.externalId}/${loopStatus.index}'>
								<input name="bean" type="hidden" value="{{ object }}" />
								
								<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.delete" />" />
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

	<div class="panel panel-default">
		<div class="panel-body">
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.product" />
				</div>
				
				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="erpTuitionInfoType_product" name="product" ng-model="$parent.object.product" theme="bootstrap" ng-disabled="disabled">
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
			
		</div>
	</div>
</form>

<h3 style="margin-top:100px;">
    <spring:message code="label.ERPTuitionInfoType.degreeInformation.title" />
</h3>

<c:choose>
	<c:when test="${not bean.isDegreeInformationDefined()}">
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
								action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_DEGREE_TYPE_URL %>/${executionYear.externalId}/${loopStatus.index}'>
								<input name="bean" type="hidden" value="{{ object }}" />
								
								<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.delete" />" />
							</form>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="entry" items="${bean.degrees}" varStatus="loopStatus">
					<tr>
						<td><c:out value="${entry.degreeType.name.content}" /> > <c:out value="${entry.presentationNameI18N.content}" /></td>

						<td>
							<form method="post" class="form-horizontal"
								action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_DEGREE_URL %>/${executionYear.externalId}/${loopStatus.index}'>
								<input name="bean" type="hidden" value="{{ object }}" />
								
								<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.delete" />" />
							</form>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="entry" items="${bean.degreeCurricularPlans}" varStatus="loopStatus">
					<tr>
						<td><c:out value="${entry.degree.degreeType.name.content}" /> > <c:out value="${entry.degree.presentationNameI18N.content}" /> > <c:out value="${entry.name.content}" /></td>

						<td>
							<form method="post" class="form-horizontal"
								action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.REMOVE_DEGREE_CURRICULAR_PLAN_URL %>/${executionYear.externalId}/${loopStatus.index}'>
								<input name="bean" type="hidden" value="{{ object }}" />
								
								<input type="submit" class="btn btn-xs btn-default" role="button" value="<spring:message code="label.delete" />" />
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

<div class="form-group row">
	<div class="col-sm-2 control-label">
		<spring:message code="label.ERPTuitionInfoType.degreeType" />
	</div>
	<div class="col-sm-6">
		<label class="radio-inline">
		  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION %>" ng-model="$parent.object.degreeInfoSelectOption">
		  <spring:message  code="label.ERPTuitionInfoType.degreeType.option" />
		</label>
		<label class="radio-inline">
		  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREES_OPTION %>" ng-model="$parent.object.degreeInfoSelectOption">
		  <spring:message  code="label.ERPTuitionInfoType.degree.option" />
		</label>
		<label class="radio-inline">
		  <input type="radio" name="inlineRadioOptions" value="<%= ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTIONS %>" ng-model="$parent.object.degreeInfoSelectOption">
		  <spring:message  code="label.ERPTuitionInfoType.degreeCurricularPlan.option" />
		</label>
	</div>
	
</div>

<form method="post" class="form-horizontal" action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADD_DEGREE_TYPE_URL %>/${executionYear.externalId}'
	ng-show='$parent.object.degreeInfoSelectOption === '<%= ERPTuitionInfoTypeBean.DEGREE_TYPE_OPTION %>'>

	<div class="panel panel-default">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.degreeType" />
				</div>

				<div class="col-sm-6">
					<ui-select name="degreeType" ng-model="$parent.object.degreeType" theme="bootstrap" ng-disabled="disabled"> 
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

		</div>
	</div>
</form>

<form method="post" class="form-horizontal" action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADD_DEGREES_URL %>/${executionYear.externalId}'
	ng-show='$parent.object.degreeInfoSelectOption === '<%= ERPTuitionInfoTypeBean.DEGREES_OPTION %>'>

	<input name="bean" type="hidden" value="{{ object }}" /> 
	
	<input id="degree-type-postback" type="hidden" name="postback"
		value='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CHOOSE_DEGREE_TYPE_POSTBACK_URL %>/${executionYear.externalId}' />

	<div class="panel panel-default">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.degreeType" />
				</div>

				<div class="col-sm-6">
					<ui-select name="degreeType" ng-model="$parent.object.degreeType" theme="bootstrap" ng-disabled="disabled"
						on-select="onDegreeTypeChange($model)"> 
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSource | filter: $select.search">
							<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitioninfoType.degrees" />
				</div>
				<div class="col-sm-6">

					<div ng-hide="object.degreesDataSource" class="alert alert-warning">
						<spring:message code="label.ERPTuitioninfoType.degreeDataSource.is.empty" />
					</div>
					<div ng-repeat="dcp in object.degreesDataSource">
						<div class="checkbox">
							<input class="checkbox pull-left" name="{{d.id}}" type="checkbox" id="{{d.id}}"
								ng-checked="object.degrees.indexOf(d.id) > -1" ng-click="toggleDegrees(d.id)" />
							<span><label for="{{dcp.id}}">{{dcp.text}}</label></span>
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

		</div>
	</div>
</form>



<form method="post" class="form-horizontal"
	action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.ADDDEGREECURRICULARPLANS_URL %>/${executionYear.externalId}'
	ng-show='$parent.object.degreeInfoSelectOption === '<%= ERPTuitionInfoTypeBean.DEGREE_CURRICULAR_PLANS_OPTIONS %>'>

	<input name="bean" type="hidden" value="{{ object }}" /> 
	<input id="degree-type-postback" type="hidden" name="postback"
		value='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CHOOSEDEGREETYPEPOSTBACK_URL %>/${executionYear.externalId}' />

	<div class="panel panel-default">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.degreeType" />
				</div>

				<div class="col-sm-6">
					<ui-select name="degreeType" ng-model="$parent.object.degreeType" theme="bootstrap" ng-disabled="disabled" on-select="onDegreeTypeChange($model)"> 
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="degreeType.id as degreeType in object.degreeTypeDataSource | filter: $select.search">
							<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitioninfoType.degreeCurricularPlans" />
				</div>
				<div class="col-sm-6">
					<div ng-hide="object.degreeCurricularPlanDataSource" class="alert alert-warning">
						<spring:message code="label.ERPTuitioninfoType.degreeCurricularPlanDataSource.is.empty" />
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
				<div class="col-sm-8">
					<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.add" />" style="float: right;" />
				</div>
			</div>
		</div>
	</div>
</form>

<form id="form" name='form' method="post" class="form-horizontal"
	action='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CREATE_URL %>/${executionYear.externalId}'>

	<input name="bean" type="hidden" value="{{ object }}" />


	<div class="panel panel-default">
		<div class="panel-body">
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ERPTuitionInfoType.erpTuitionProduct" />
				</div>
				
				<div class="col-sm-6">
					<%-- Relation to side 1 drop down rendered in input --%>
					<ui-select id="erpTuitionInfoType_erpTuitionInfoProduct" name="erpTuitionInfoProduct" ng-model="$parent.object.erpTuitionInfoProduct" theme="bootstrap" ng-disabled="disabled">
						<ui-select-match>{{$select.selected.text}}</ui-select-match>
						<ui-select-choices repeat="product.id as product in object.erpTuitionInfoProductDataSource | filter: $select.search">
							<span ng-bind-html="product.text | highlight: $select.search"></span>
						</ui-select-choices>
					</ui-select>
				</div>
			</div>
		</div>
		<div class="panel-footer">
			<input type="submit" class="btn btn-default" role="button"
				value="<spring:message code="label.submit" />" />
		</div>
	</div>
</form>

