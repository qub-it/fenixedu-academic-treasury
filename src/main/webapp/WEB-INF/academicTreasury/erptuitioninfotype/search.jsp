<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoTypeController"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoExportOperationController"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
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

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.ERPTuitionInfoType.title" />
        <small></small>
    </h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;
    <a href="${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CREATE_URL %>">
    	<spring:message code="label.ERPTuitionInfoType.create" />
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
                <span class="glyphicon glyphicon-exclamation-sign"
                    aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>

<script type="text/javascript">
	  function processDelete(externalId) {
	    url = "${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.DELETE_URL %>/" + externalId;
	    $("#deleteForm").attr("action", url);
	    $('#deleteModal').modal('toggle')
	  }
</script>


<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="#" method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.ERPTuitionInfoType.confirmDelete" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-warning" type="submit">
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

<c:choose>
	<c:when test="${not empty result}">
		<table id="simpletable" class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message code="label.ERPTuitionInfoType.product" /></th>
					<th><spring:message code="label.ERPTuitionInfoType.appliedTo" /></th>
					<th><spring:message code="label.ERPTuitionInfoType.degreeInformation" /></th>
					<%-- Operations Column --%>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="row" items="${result}">
					<tr>
						<td><c:out value='${row.product.name.content}' /></td>
						<td>
							<p>
								<strong><spring:message code="label.ERPTuitionInfoType.forRegistration" />:</strong>
								<spring:message code="label.${row.forRegistration}" />
							</p>
							<p>
								<strong><spring:message code="label.ERPTuitionInfoType.forStandalone" />:</strong>
								<spring:message code="label.${row.forStandalone}" />
							</p>
							<p>
								<strong><spring:message code="label.ERPTuitionInfoType.forExtracurricular" />:</strong>
								<spring:message code="label.${row.forExtracurricular}" />
							</p>
						</td>
						<td>
							<c:if test="${row.degreeType != null}">
								<p>
									<strong><spring:message code="label.ERPTuitionInfoType.degreeType" />:</strong>
									<c:out value='${row.degreeType.name.content}' />
								</p>
							</c:if>
							<c:if test="${row.degree != null}">
								<p>
									<strong><spring:message code="label.ERPTuitionInfoType.degree" />:</strong>
									<c:out value='${row.degree.presentationNameI18N.content}' />
								</p>
							</c:if>
						</td>
						<td>
							<a  class="btn btn-xs btn-warning" href="#" onClick="javascript:processDelete('${row.externalId}')">
								<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;
								<spring:message code='label.delete'/>
							</a>							
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<script>
			$(document).ready(function() {
				var table = $('#simpletable').DataTable({
					language : {
						url : "${datatablesI18NUrl}",
					},
					"paging": false,
					//CHANGE_ME adjust the actions column width if needed
					"columnDefs" : [],
					"order": [[ 0, "desc" ]],
					"dom" : '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
					"tableTools" : {
						"sSwfPath" : "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"
					}
				});
				
				table.columns.adjust().draw();

				$('#simpletable tbody').on('click', 'tr', function() {
					$(this).toggleClass('selected');
				});
			});
		</script>
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

