<%@page import="org.fenixedu.academictreasury.ui.customer.CustomerAccountingController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/academicTreasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>                        
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/academicTreasury/js/omnis.js"></script>


<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.accounting.manageCustomer.readCustomer" />
    </h1>
    <small></small>
</div>
<%-- NAVIGATION --%>
<!-- <div class="well well-sm" style="display: inline-block"> -->
<%--     <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/"><spring:message --%>
<%--             code="label.event.back" /></a> &nbsp; --%>
<!-- </div> -->
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
                    <c:if test='${customer.isPersonCustomer() }'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.Customer.code" /></th>
                            <td><c:out value='${customer.businessIdentification}' /></td>
                        </tr>
                    </c:if>
                    <c:if test='${customer.isAdhocCustomer() }'>
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.Customer.code" /></th>
                            <td><c:out value='${customer.code}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.name" /></th>
                        <td><c:out value='${customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber" /></th>
                        <td><c:out value='${customer.fiscalNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Customer.identificationNumber" /></th>
                        <td><c:out value='${customer.identificationNumber}' /></td>
                    </tr>
                </tbody>
            </table>

        </form>
    </div>
</div>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.Customer.debtAccountsBalances" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
                    <c:forEach var="debtAccount" items='${customer.debtAccountsSet}'>
                        <tr>
                            <th scope="row" class="col-xs-3"><c:out value="${debtAccount.finantialInstitution.name}" /></th>
                            <td style="vertical-align: middle">
                                <div class="col-xs-3">
                                    <c:out value="${debtAccount.finantialInstitution.currency.getValueFor(debtAccount.totalInDebt + debtAccount.calculatePendingInterestAmount())}" />
                                </div> &nbsp;&nbsp;<a class="btn btn-default btn-xs"
                                href="${pageContext.request.contextPath}${readAccountUrl}${debtAccount.externalId}"><spring:message
                                        code="label.customer.read.showdebtaccount"></spring:message></a> <c:if test="${debtAccount.totalInDebt < 0 }">
                                    <span class="label label-primary"> <spring:message code="label.DebtAccount.customerHasAmountToRehimburse" />
                                    </span>
                                </c:if> <c:if test="${debtAccount.closed}">
                                    <span class="label label-warning"><spring:message code="warning.DebtAccount.is.closed" /></span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

        </form>
    </div>
</div>
</br>

<script>
	$(document).ready(function() {

	});
</script>
