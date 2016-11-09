<%@page import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.DegreeCurricularPlanController"%>
<%@page import="org.fenixedu.academictreasury.ui.managetuitionpaymentplan.TuitionPaymentPlanController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
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


<div class="page-header">
    <h1>
        <spring:message code="label.manageTuitionPaymentPlan.searchTuitionPaymentPlan" />
        <small>(${executionYear.qualifiedName})</small>
    </h1>
</div>

<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span> &nbsp; <a class=""
        href="${pageContext.request.contextPath}<%= DegreeCurricularPlanController.CHOOSEDEGREECURRICULARPLAN_URL %>/${finantialEntity.externalId}/${executionYear.externalId}">
        <spring:message code="label.event.back" />
    </a>
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

<script type="text/javascript">
    function processDelete(externalId) {
        url = "${pageContext.request.contextPath}/academictreasury/managetuitionpaymentplan/tuitionpaymentplan/search/delete/${finantialEntity.externalId}/${executionYear.externalId}/${degreeCurricularPlan.externalId}/" + externalId;
        $("#deleteForm").attr("action", url);
        $('#deleteModal').modal('toggle')
      }
</script>

<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="#" method="POST">
                <div class="modal-header">
                    <button type="button" class="close"
                        data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message
                            code="label.TuitionPaymentPlan.confirmDelete" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default"
                        data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger"
                        type="submit">
                        <spring:message code="label.delete" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<div class="panel-group" id="accordion">
    <c:forEach items="${searchtuitionpaymentplanResultsDataSet}" var="paymentPlan" varStatus="loopStatus">
        <div class="panel panel-default">
            <div class="panel-heading">
                <div style="float:left; display:inline">
                    <p>
	                    <span class="badge">${loopStatus.index + 1}</span>&nbsp;&nbsp;<strong> <c:out value="${paymentPlan.name.content}" /></strong>&nbsp;
	                    <em>
		                    <c:if test="${paymentPlan.defaultPaymentPlan}">
	                            <spring:message code="label.TuitionPaymentPlan.defaultPaymentPlan" />
		                    </c:if>
	                    </em>
                    </p>
                    <c:if test="${paymentPlan.payorDebtAccount != null}">
                    	<p><strong><spring:message code="label.TuitionPaymentPlan.payorDebtAccount" /></strong>: <c:out value="${paymentPlan.payorDebtAccount.customer.name}" /></p>
                    </c:if>
                </div>
                <div style="float:right; display:inline">
                <a class="btn btn-default" data-toggle="collapse" data-target="#collapsePayment${loopStatus.index}" href="#collapsePayment${loopStatus.index}">
                    <span class="glyphicon glyphicon-list-alt"/></span>  
<%--                         <spring:message code="label.TuitionPaymentPlan.details" /> --%>
                   </a>
 &nbsp; 
                    <c:if test="${loopStatus.index > 0}">
                        <a class="btn btn-default" 
                            href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.ORDER_UP_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}/${degreeCurricularPlan.externalId}/${paymentPlan.externalId}">
                             <span class="glyphicon glyphicon-arrow-up"/></span>
<%--                              <spring:message code="label.TuitionPaymentPlan.order.up" /> --%>
                        </a> &nbsp; 
                    </c:if>
                    <c:if test="${not (loopStatus.index > 0)}">
                        <a class="btn btn disabled"  
                            href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.ORDER_UP_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}/${degreeCurricularPlan.externalId}/${paymentPlan.externalId}">
                             <span class="glyphicon glyphicon-arrow-up"/></span>
<%--                              <spring:message code="label.TuitionPaymentPlan.order.up" /> --%>
                        </a> &nbsp; 
                    </c:if>
                    <c:if test="${loopStatus.index + 1 < searchtuitionpaymentplanResultsDataSet.size()}">
                        <a class="btn btn-default" 
                            href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.ORDER_DOWN_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}/${degreeCurricularPlan.externalId}/${paymentPlan.externalId}">
                             <span class="glyphicon glyphicon-arrow-down"/></span>
<%--                              <spring:message code="label.TuitionPaymentPlan.order.down" /> --%>
                        </a>                
                    </c:if>
                    <c:if test="${not(loopStatus.index + 1 < searchtuitionpaymentplanResultsDataSet.size())}">
                        <a class="btn btn disabled" 
                            href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.ORDER_DOWN_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}/${degreeCurricularPlan.externalId}/${paymentPlan.externalId}">
                             <span class="glyphicon glyphicon-arrow-down"/></span>
<%--                              <spring:message code="label.TuitionPaymentPlan.order.down" /> --%>
                        </a>                
                    </c:if>&nbsp;
                                        <a class="btn btn-warning" 
                        onClick="javascript:processDelete('${paymentPlan.externalId}')"
                        href="#"
<%--                         href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.SEARCH_TO_DELETE_ACTION_URL %>/${finantialEntity.externalId}/${executionYear.externalId}/${degreeCurricularPlan.externalId}/${paymentPlan.externalId}" --%>
                        >
                        <span class="glyphicon glyphicon-trash"/></span>
<%--                         <spring:message code="label.TuitionPaymentPlan.delete.plan" /> --%>
                    </a>
                </div>
                <div style="clear:both">&nbsp;</div>
                <p style="color: blue">
                   <a data-toggle="collapse" data-target="#collapsePayment${loopStatus.index}" href="#collapsePayment${loopStatus.index}">  
                        <strong><em><c:out value="${paymentPlan.conditionsDescription.content}" /></em></strong>
                   </a> &nbsp; 
                   
                </p>

            </div>
            <div id="collapsePayment${loopStatus.index}" class="panel-collapse collapse">
                <datatables:table id="paymentPlans-${loopStatus.index}" row="installment" data="${paymentPlan.orderedTuitionInstallmentTariffs}"
                    cssClass="table responsive table-bordered table-hover" cdn="false" cellspacing="2">

                    <datatables:column cssStyle="width:10%">
                        <datatables:columnHead>
                            <spring:message code="label.TuitionInstallmentTariff.installmentOrder" />
                        </datatables:columnHead>
                        <c:out value="${installment.installmentOrder}" />
                    </datatables:column>

                    <datatables:column cssStyle="width:20%">
                        <datatables:columnHead>
                            <spring:message code="label.TuitionInstallmentTariff.amount" />
                        </datatables:columnHead>

                        <p><strong><c:out value="${installment.product.name.content}" /></strong></p>

                        <c:choose>
                            <c:when test="${installment.tuitionCalculationType.fixedAmount}">
                                <p>
                                    <em><spring:message code="TuitionCalculationType.FIXED_AMOUNT" />:&nbsp;</em>
	                                <c:out value="${installment.finantialEntity.finantialInstitution.currency.getValueFor(installment.fixedAmount)}" />
                                </p>
                            </c:when>
                            <c:when test="${installment.tuitionCalculationType.ects}">
                                <p>
                                    <em> <c:out value="${installment.tuitionCalculationType.descriptionI18N.content}" /> &nbsp; [<c:out
                                            value="${installment.ectsCalculationType.descriptionI18N.content}" />]:&nbsp;
                                    </em>

	                                <c:if test="${installment.ectsCalculationType.fixedAmount}">
                                        <spring:message code="label.TuitionInstallmentTariff.amountPerEcts"
                                            arguments="${installment.finantialEntity.finantialInstitution.currency.getValueFor(installment.amountPerEctsOrUnit)}" />
	                                </c:if>
	                                <c:if test="${installment.ectsCalculationType.dependentOnDefaultPaymentPlan}">
                                        <spring:message code="label.TuitionInstallmentTariff.defaultPaymentPlanIndexed.ectsParameters"
                                                arguments="${installment.factor},${installment.totalEctsOrUnits}" />
	                                </c:if>
                                </p>
                                <c:if test="${installment.ectsCalculationType.dependentOnDefaultPaymentPlan && installment.defaultPaymentPlanDefined && !installment.ectsCalculationType.defaultPaymentPlanCourseFunctionCostIndexed}">
                                    <p>
                                        <spring:message code="label.TuitionInstallmentTariff.amountPerEcts"
                                            arguments="${installment.finantialEntity.finantialInstitution.currency.getValueFor(installment.amountPerEctsOrUnit, 3)}" />
                                    </p>
                                    <p>
                                        <em><spring:message code="label.TuitionInstallmentTariff.calculatedAutomaticaly" /></em>
                                    </p>
                                </c:if>
                                <c:if test="${installment.ectsCalculationType.dependentOnDefaultPaymentPlan && !installment.defaultPaymentPlanDefined}">
                                    <p>
                                        <span class="label label-danger"><em><spring:message code="error.TuitionInstallmentTariff.default.payment.plan.not.defined" /></em></span>
                                    </p>
                                </c:if>
                            </c:when>
                            <c:when test="${installment.tuitionCalculationType.units}">
                                <p>
                                    <strong> <c:out value="${installment.tuitionCalculationType.descriptionI18N.content}" /> &nbsp; [<c:out
                                            value="${installment.ectsCalculationType.descriptionI18N.content}" />]
                                    </strong>
                                </p>

                                <c:if test="${installment.ectsCalculationType.fixedAmount}">
                                    <p>&nbsp;</p>

                                    <p>
                                        <spring:message code="label.TuitionInstallmentTariff.amountPerUnits"
                                            arguments="${installment.finantialEntity.finantialInstitution.currency.getValueFor(installment.amountPerEctsOrUnit)}" />
                                    </p>
                                </c:if>
                                <c:if test="${installment.ectsCalculationType.dependentOnDefaultPaymentPlan}">
                                    <p>
                                        <em><spring:message code="label.TuitionInstallmentTariff.defaultPaymentPlanIndexed.unitsParameters"
                                                arguments="${installment.factor},${installment.totalEctsOrUnits}" /></em>
                                    </p>
                                    <p>&nbsp;</p>
									
									<c:if test="${installment.defaultPaymentPlanDefined && !installment.ectsCalculationType.defaultPaymentPlanCourseFunctionCostIndexed}">
	                                    <p>
	                                        <spring:message code="label.TuitionInstallmentTariff.amountPerUnits"
	                                            arguments="${installment.finantialEntity.finantialInstitution.currency.getValueFor(installment.amountPerEctsOrUnit, 3)}" />
	                                    </p>
	                                    <p>
	                                        <em><spring:message code="label.TuitionInstallmentTariff.calculatedAutomaticaly" /></em>
	                                    </p>
	                               </c:if>
	                               
	                                <c:if test="${!installment.defaultPaymentPlanDefined}">
	                                    <p>
	                                        <span class="label label-danger"><em><spring:message code="error.TuitionInstallmentTariff.default.payment.plan.not.defined" /></em></span>
	                                    </p>
	                                </c:if>
                                </c:if>
                            </c:when>
                        </c:choose>

                        <c:choose>
                            <c:when test="${installment.applyMaximumAmount}">
                                <p>
                                    <em><spring:message code="label.TuitionPaymentPlan.maximumAmount" />:&nbsp;</em>
	                                <c:out value="${installment.finantialEntity.finantialInstitution.currency.getValueFor(installment.maximumAmount)}" />
                                </p>
							</c:when>
						</c:choose>
						
                        <c:if test="${installment.academicalActBlockingOff}"> 
                            <p><span class="label label-warning">
                                    <spring:message code="label.TuitionPaymentPlan.academicalActBlockingOff" />
                            </span></p>
                        </c:if>
						
                        <c:if test="${installment.blockAcademicActsOnDebt}"> 
                            <p><span class="label label-warning">
                                    <spring:message code="label.TuitionPaymentPlan.blockAcademicActsOnDebt" />
                            </span></p>
                        </c:if>
						
                    </datatables:column>
                    <datatables:column>
                        <datatables:columnHead>
                            <spring:message code="label.TuitionInstallmentTariff.beginDate" />
                        </datatables:columnHead>
                        <joda:format value="${installment.beginDate}" style="S-" />
                    </datatables:column>
                    <datatables:column>
                        <datatables:columnHead>
                            <spring:message code="label.TuitionInstallmentTariff.dueDate" />
                        </datatables:columnHead>
                        <c:choose>
                            <c:when test="${installment.dueDateCalculationType.noDueDate}">
                                <p>
                                    <spring:message code="label.TuitionInstallmentTariff.noDueDate" />
                                </p>
                            </c:when>
                            <c:when test="${installment.dueDateCalculationType.fixedDate}">
                                <p>
                                    <joda:format value="${installment.fixedDueDate}" style="S-" />
                                </p>
                            </c:when>
                            <c:when test="${installment.dueDateCalculationType.daysAfterCreation}">
                                <p>
                                    <spring:message code="label.TuitionInstallmentTariff.daysAfterCreation" arguments="${installment.numberOfDaysAfterCreationForDueDate}" />
                                </p>
                            </c:when>
                            <c:when test="${installment.dueDateCalculationType.bestOfFixedDateAndDaysAfterCreation}">
                                <p>
                                    <joda:format value="${installment.fixedDueDate}" style="S-" />
                                    <spring:message code="label.TuitionInstallmentTariff.bestOfFixedDateAndDaysAfterCreation"
                                        arguments="${installment.numberOfDaysAfterCreationForDueDate}" />
                                </p>
                            </c:when>
                        </c:choose>
                    </datatables:column>

                    <datatables:column cssStyle="width:25%">
                        <datatables:columnHead>
                            <spring:message code="label.TuitionInstallmentTariff.interests" />
                        </datatables:columnHead>
                        <c:if test="${not installment.applyInterests}">
                            <p>
                                <strong><spring:message code="label.TuitionInstallmentTariff.interests.not.applied" /></strong>
                            </p>
                        </c:if>
                        <c:if test="${installment.applyInterests}">
                            <p>
                                <strong>[<c:out value="${installment.interestRate.interestType.descriptionI18N.content}" />]
                                </strong>
                            </p>

                            <c:choose>
                                <c:when test="${installment.interestRate.interestType.daily}">
                                    <p>
                                        <strong><spring:message code="label.TuitionInstallmentTariff.numberOfDaysAfterCreationForDueDate" />:&nbsp;</strong>
                                        <c:out value="${installment.interestRate.numberOfDaysAfterDueDate}" />
                                    </p>
                                    <p>
                                        <strong><spring:message code="label.TuitionInstallmentTariff.applyInFirstWorkday" />:&nbsp;</strong>
                                        <c:if test="${installment.interestRate.applyInFirstWorkday}">
                                            <spring:message code="label.true" />
                                        </c:if>
                                        <c:if test="${not installment.interestRate.applyInFirstWorkday}">
                                            <spring:message code="label.false" />
                                        </c:if>
                                    </p>

                                    <c:if test="${installment.interestRate.maximumDaysToApplyPenaltyApplied}">
                                        <p>
                                            <strong><spring:message code="label.TuitionInstallmentTariff.maximumDaysToApplyPenalty" />:&nbsp;</strong>
                                            <c:out value="${installment.interestRate.maximumDaysToApplyPenalty}" />
                                        </p>
                                    </c:if>

                                    <p>
                                        <strong><spring:message code="label.TuitionInstallmentTariff.rate" />:&nbsp;</strong>
                                        <c:out value="${installment.interestRate.rate}" />
                                        &nbsp;&#37;
                                    </p>
                                </c:when>
                                <c:when test="${installment.interestRate.interestType.monthly}">
                                    <p>
                                        <strong><spring:message code="label.TuitionInstallmentTariff.applyInFirstWorkday" />:&nbsp;</strong>
                                        <c:if test="${installment.interestRate.applyInFirstWorkday}">
                                            <spring:message code="label.true" />
                                        </c:if>
                                        <c:if test="${not installment.interestRate.applyInFirstWorkday}">
                                            <spring:message code="label.false" />
                                        </c:if>
                                    </p>

                                    <c:if test="${installment.interestRate.maximumMonthsToApplyPenaltyApplied}">
                                        <p>
                                            <strong><spring:message code="label.TuitionInstallmentTariff.maximumMonthsToApplyPenalty" />:&nbsp;</strong>
                                            <c:out value="${installment.interestRate.maximumMonthsToApplyPenalty}" />
                                        </p>
                                    </c:if>

                                    <p>
                                        <strong><spring:message code="label.TuitionInstallmentTariff.rate" />:&nbsp;</strong>
                                        <c:out value="${installment.interestRate.rate}" />
                                        &nbsp;&#37;
                                    </p>
                                </c:when>
                                <c:when test="${installment.interestRate.interestType.fixedAmount}">
                                    <p>
                                        <strong><spring:message code="label.TuitionInstallmentTariff.interestFixedAmount" />:&nbsp;</strong>
                                        <c:out value="${finantialEntity.finantialInstitution.currency.getValueFor(installment.interestRate.interestFixedAmount)}" />
                                    </p>
                                </c:when>
                            </c:choose>
                        </c:if>
                    </datatables:column>
                    <datatables:column cssStyle="width:10%">
		                <a style="margin-left:10px;margin-bottom:10px;" class="btn btn-default btn-xs" href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.EDIT_TUITION_INSTALLMENT_URL %>/${installment.externalId}">
		                	<spring:message code="label.edit" />
		                </a>
					</datatables:column>
                </datatables:table>
                
                <a style="margin-left:10px;margin-bottom:10px;" class="btn btn-default btn-xs" href="${pageContext.request.contextPath}<%= TuitionPaymentPlanController.COPY_PAYMENT_PLAN_CHOOSE_EXECUTION_YEAR_DEGREE_CURRICULAR_PLANS_URL %>/${paymentPlan.finantialEntity.externalId}/${paymentPlan.executionYear.externalId}/${paymentPlan.degreeCurricularPlan.externalId}/${paymentPlan.externalId}">
                	<spring:message code="label.copy" />
                </a>
                
                <script>
					createDataTables(
							"paymentPlans-${loopStatus.index}",
							false,
							false,
							false,
							"${pageContext.request.contextPath}",
							"${datatablesI18NUrl}");
				</script>
            </div>
        </div>
    </c:forEach>
</div>

<script>
	$(document).ready(function() {
	});
	
</script>

