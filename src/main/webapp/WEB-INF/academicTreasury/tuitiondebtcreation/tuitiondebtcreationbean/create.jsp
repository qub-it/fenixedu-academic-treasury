<%@page
    import="org.fenixedu.academictreasury.ui.tuitiondebtcreation.standalone.OtherTuitionDebtCreationBeanController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
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

${portal.angularToolkit()}

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
            code="label.TuitionDebtCreation.createTuitionDebtCreationBean" />
        <small></small>
    </h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
    &nbsp; <a class=""
        href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${debtAccount.externalId}">
        <spring:message code="label.event.back" />
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

<script>
	angular
			.module('angularAppTuitionDebtCreationBean',
					[ 'ngSanitize', 'ui.select', 'bennuToolkit' ])
			.controller(
					'TuitionDebtCreationBeanController',
					[
							'$scope',
							function($scope) {

								$scope.object = ${tuitionDebtCreationBeanJson};
								// $scope.$apply();
								$scope.booleanvalues = [
										{
											name : '<spring:message code="label.no"/>',
											value : false
										},
										{
											name : '<spring:message code="label.yes"/>',
											value : true
										} ];

								$scope.postBack = createAngularPostbackFunction($scope);

								$scope.onRegistrationChange = function(model) {
									$scope.postBack(model);
								}
							} ]);
</script>

<form name='form' method="post" class="form-horizontal"
    ng-app="angularAppTuitionDebtCreationBean"
    ng-controller="TuitionDebtCreationBeanController"
    action='${pageContext.request.contextPath}<%= OtherTuitionDebtCreationBeanController.CREATE_REGISTRATION_URL %>/${debtAccount.externalId}'>

    <input type="hidden" name="postback"
        value='${pageContext.request.contextPath}<%= OtherTuitionDebtCreationBeanController.CREATEPOSTBACK_URL  %>/${debtAccount.externalId}' />

    <input name="bean" type="hidden" value="{{ object }}" />

	<div class="panel panel-primary">
	    <div class="panel-heading">
	        <h3 class="panel-title">
	            <spring:message code="label.Customer.customerDetails" />
	        </h3>
	    </div>
	    <div class="panel-body">
	          <table class="table">
	              <tbody>
	                  <tr>
	                      <th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber" /></th>
	                      <td><c:out value='${debtAccount.customer.uiFiscalNumber}' /></td>
	                  </tr>
	
	                  <tr>
	                      <th scope="row" class="col-xs-3"><spring:message code="label.DebtAccount.customer" /></th>
	                      <td><c:out value='${debtAccount.customer.businessIdentification}' /> - <c:out value='${debtAccount.customer.name}' /></td>
	                  </tr>
	              </tbody>
	          </table>
	    </div>
	</div>

    <div class="panel panel-default">
        <div class="panel-body">

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TuitionDebtCreationBean.registration" />
                </div>

                <div class="col-sm-10">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <ui-select id="tuitionDebtCreationBean_registration"
                        name="registration"
                        ng-model="$parent.object.registration"
                        theme="bootstrap" ng-disabled="disabled"
                        on-select="onRegistrationChange($model)">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices
                        repeat="registration.id as registration in object.registrationDataSource | filter: $select.search">
                    <span
                        ng-bind-html="registration.text | highlight: $select.search"></span>
                    </ui-select-choices> </ui-select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TuitionDebtCreationBean.executionYear" />
                </div>

                <div class="col-sm-10">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <ui-select
                        id="tuitionDebtCreationBean_executionYear"
                        name="executionyear"
                        ng-model="$parent.object.executionYear"
                        theme="bootstrap" ng-disabled="disabled"
                        on-select="onRegistrationChange($model)">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices
                        repeat="executionYear.id as executionYear in object.executionYearDataSource | filter: $select.search">
                    <span
                        ng-bind-html="executionYear.text | highlight: $select.search"></span>
                    </ui-select-choices> </ui-select>
                </div>
            </div>

            <c:if test="${bean.standaloneTuition}">
                <div class="form-group row">
                    <div class="col-sm-2 control-label">
                        <spring:message
                            code="label.TuitionDebtCreationBean.enrolment" />
                    </div>

                    <div class="col-sm-10">
                        <ui-select
                            id="tuitionDebtCreationBean_enrolments"
                            name="enrolments"
                            ng-model="$parent.object.enrolment"
                            theme="bootstrap" ng-disabled="disabled"
                            on-select="onRegistrationChange($model)">
                        <ui-select-match>{{$select.selected.text}}</ui-select-match>
                        <ui-select-choices
                            repeat="enrolments.id as enrolments in object.standaloneEnrolmentsDataSource | filter: $select.search">
                        <span
                            ng-bind-html="enrolments.text | highlight: $select.search"></span>
                        </ui-select-choices> </ui-select>
                    </div>
                </div>
            </c:if>

            <c:if test="${bean.extracurricularTuition}">
                <div class="form-group row">
                    <div class="col-sm-2 control-label">
                        <spring:message
                            code="label.TuitionDebtCreationBean.enrolment" />
                    </div>

                    <div class="col-sm-10">
                        <ui-select
                            id="tuitionDebtCreationBean_enrolments"
                            name="enrolments"
                            ng-model="$parent.object.enrolment"
                            theme="bootstrap" ng-disabled="disabled"
                            on-select="onRegistrationChange($model)">
                        <ui-select-match>{{$select.selected.text}}</ui-select-match>
                        <ui-select-choices
                            repeat="enrolments.id as enrolments in object.extracurricularEnrolmentsDataSource | filter: $select.search">
                        <span
                            ng-bind-html="enrolments.text | highlight: $select.search"></span>
                        </ui-select-choices> </ui-select>
                    </div>
                </div>
            </c:if>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TuitionDebtCreationBean.debtDate" />
                </div>

                <div class="col-sm-6">
                    <input id="tuitionDebtCreationBean_debtDate"
                        class="form-control" type="text" name="debtDate"
                        bennu-date="object.debtDate" />
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.TuitionDebtCreationBean.tuitionPaymentPlan" />
                </div>

                <div class="col-sm-10">
                    <%-- Relation to side 1 drop down rendered in input --%>
                    <ui-select
                        id="tuitionDebtCreationBean_tuitionPaymentPlans"
                        name="tuitionpaymentplans"
                        ng-model="$parent.object.tuitionPaymentPlan"
                        theme="bootstrap" ng-disabled="disabled">
                    <ui-select-match>{{$select.selected.text}}</ui-select-match>
                    <ui-select-choices
                        repeat="tuitionPaymentPlans.id as tuitionPaymentPlans in object.tuitionPaymentPlansDataSource | filter: $select.search">
                    <span
                        ng-bind-html="tuitionPaymentPlans.text | highlight: $select.search"></span>
                    </ui-select-choices> </ui-select>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-2 control-label"></div>
                <div class="col-sm-10">
                    <p>
                        <em class="label label-warning">{{object.errorMessage}}</em>
                    </p>
                </div>
            </div>
        </div>
        <div class="panel-footer">
            <input type="submit" class="btn btn-default" role="button"
                value="<spring:message code="label.submit" />" />
        </div>
    </div>
</form>

<script>
	$(document).ready(function() {
	});
</script>
