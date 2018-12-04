<%@page import="org.fenixedu.academictreasury.domain.integration.tuitioninfo.ERPTuitionInfoSettings"%>
<%@page import="org.fenixedu.academictreasury.ui.integration.tuitioninfo.ERPTuitionInfoTypeController"%>
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

<form id="toogleExportationActiveForm" action="${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.TOOGLE_EXPORTATION_ACTIVE_URL %>/${executionYear.externalId}" method="post">
</form>


<% if(!ERPTuitionInfoSettings.getInstance().isExportationActive()) { %>

<script type="text/javascript">
	function processDelete(externalId) {
		url = "${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.DELETE_URL %>/${executionYear.externalId}" + "/" + externalId;
		$("#deleteForm").attr("action", url);
		$('#deleteModal').modal('toggle')
	}
	
	function processToogleActive(externalId) {
		url = "${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.TOOGLE_ACTIVE_URL %>/${executionYear.externalId}" + "/" + externalId;
		$("#toogleForm").attr("action", url);
		$('#toogleModal').modal('toggle')
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
                        <spring:message code="label.ERPTuitionInfoType.delete.confirm" />
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

<div class="modal fade" id="toogleModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="toogleForm" action="#" method="POST">
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
                        <spring:message code="label.ERPTuitionInfoType.toogle.confirm" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-warning" type="submit">
                        <spring:message code="label.confirm" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->



<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span>&nbsp;
    <a href="${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.CREATE_URL %>/${executionYear.externalId}">
    	<spring:message code="label.ERPTuitionInfoType.create" />
    </a>
    &nbsp;|&nbsp;
    <span class="glyphicon glyphicon-off" aria-hidden="true"></span>&nbsp;
    <a href="#" onclick="$('#toogleExportationActiveForm').submit(); return false;">
    	<spring:message code="label.ERPTuitionInfoType.enable.exportation.active" />
    </a>
    
    
    
</div>

<div class="row">
    <label for="executionYearId" class="col-xs-3">
        <strong><spring:message code="label.ERPTuitionInfoType.exportationActive.state" /></strong>
    </label>
    <div class="col-xs-1">
		<span class="label label-danger">
			<spring:message code="label.false" />
		</span>
    </div>

    <p>&nbsp;</p>
    <p>&nbsp;</p>
</div>

<div class="alert alert-warning" role="alert">
	<p>
		<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
		<spring:message code="label.ERPTuitionInfoType.for.exportation.enable.after.info.type.edition" />
	</p>
</div>


<% } else { %>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-off" aria-hidden="true"></span>&nbsp;
    <a href="#" onclick="$('#toogleExportationActiveForm').submit(); return false;">
    	<spring:message code="label.ERPTuitionInfoType.disable.exportation.active" />
    </a>
</div>

<div class="row">
    <label for="executionYearId" class="col-xs-3">
        <strong><spring:message code="label.ERPTuitionInfoType.exportationActive.state" /></strong>
    </label>
    <div class="col-xs-2">
		<span class="label label-success">
			<spring:message code="label.true" />
		</span>
    </div>
    
    <p>&nbsp;</p>
    <p>&nbsp;</p>
</div>

<div class="alert alert-warning" role="alert">
	<p>
		<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
		<spring:message code="label.ERPTuitionInfoType.for.info.type.edition.disable.export" />
	</p>
</div>

<% } %>

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

<%-- Choose Execution Year --%>
<div class="form-group row">
    <label for="executionYearId" class="col-xs-1 control-label">
        <strong><spring:message code="label.ERPTuitionInfoType.executionYear" /></strong>
    </label>
    <div class="col-xs-2">
        <select id="executionYearOptionsId" class="form-control" name="executionYearId" value=""
        	onchange="window.location='${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.SEARCH_URL %>/' + this.options[this.selectedIndex].value;">
            <c:forEach items="${executionYearOptions}" var="e">
            	<c:choose>
	            	<c:when test="${e == executionYear}">
		                <option value="${e.externalId}" selected>${e.qualifiedName}</option>
	            	</c:when>
	            	<c:otherwise>
		                <option value="${e.externalId}">${e.qualifiedName}</option>
	            	</c:otherwise>
            	</c:choose>
            </c:forEach>
        </select>
    </div>
</div>

<c:choose>
	<c:when test="${not empty result}">
		<table id="simpletable" class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message code="label.ERPTuitionInfoType.code" /></th>
					<th><spring:message code="label.ERPTuitionInfoType.name" /></th>
					<th><spring:message code="label.ERPTuitionInfoType.active" /></th>
					<th><spring:message code="label.ERPTuitionInfoType.degreeInformation" /></th>
					<th><spring:message code="label.ERPTuitionInfoType.tuitionProducts" /></th>

					<% if(!ERPTuitionInfoSettings.getInstance().isExportationActive()) { %>

						<%-- Operations Column --%>
						<th></th>
					<% } %>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="row" items="${result}">
					<tr>
						<td><c:out value='${row.erpTuitionInfoProduct.code}' /></td>
						<td><c:out value='${row.erpTuitionInfoProduct.name}' /></td>
						<td>
							<c:choose>
								<c:when test="${row.active}">
									<span class="label label-success">
										<spring:message code="label.${row.active}" />
									</span>
								</c:when>
								<c:otherwise>
									<span class="label label-danger">
										<spring:message code="label.${row.active}" />
									</span>								
								</c:otherwise>
							</c:choose> 
						</td>
						<td>

							<ul>
							<c:forEach var="e" items="${row.erpTuitionInfoTypeAcademicEntriesSet}">
								<li><c:out value="${e.description.content}" /></li>
							</c:forEach>
							</ul>
							
						</td>
						<td>
							<ul>
								<c:forEach items="${row.tuitionProducts}" var="p">
									<li><c:out value='${p.name.content}' /></li>
								</c:forEach>
							</ul>
						</td>

						<% if(!ERPTuitionInfoSettings.getInstance().isExportationActive()) { %>
						<td>



							<button class="btn btn-default btn-xs" href="#" onclick="processToogleActive(${row.externalId}); return false;">
				                <span class="glyphicon glyphicon-off" aria-hidden="true">&nbsp;</span>
								<c:choose>
									<c:when test="${row.active}">
										<spring:message code='label.ERPTuitionInfoType.inactivate'/>
									</c:when>
									<c:otherwise>
										<spring:message code='label.ERPTuitionInfoType.activate'/>
									</c:otherwise>
								</c:choose> 
							</button>

							<button class="btn btn-default btn-xs" href="${pageContext.request.contextPath}<%= ERPTuitionInfoTypeController.UPDATE_URL %>/${executionYear.externalId}/${row.externalId}">
				                <span class="glyphicon glyphicon-pencil" aria-hidden="true">&nbsp;</span>
								<spring:message code='label.update'/>
							</button>

							<button class="btn btn-danger btn-xs" href="#" onclick="processDelete(${row.externalId}); return false;">
				                <span class="glyphicon glyphicon-trash" aria-hidden="true">&nbsp;</span>
								<spring:message code='label.delete' />
							</button>
							
							
						</td>
						<% } %>

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

