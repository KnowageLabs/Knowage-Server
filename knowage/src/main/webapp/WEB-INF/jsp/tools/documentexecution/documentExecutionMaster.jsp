<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@ page language="java" pageEncoding="UTF-8" session="true"%>
<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="documentExecutionMasterModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<!-- 	breadCrumb -->
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>

<%-- ---------------------------------------------------------------------- --%>
<%-- INCLUDE Persist JS                                                     --%>
<%-- ---------------------------------------------------------------------- --%>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/lib/persist-0.1.0/persist.js")%>"></script>

<!-- cross navigation -->
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request,
					"js/src/angular_1.4/tools/commons/cross-navigation/crossNavigationDirective.js")%>"></script>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request,
					"js/src/angular_1.4/tools/documentexecution/documentExecutionMaster.js")%>"></script>
<script type="text/javascript">
	angular.module('documentExecutionMasterModule').factory('sourceDocumentExecProperties', function() {
		/*
			EXEC_FROM added for the need of the Workspace Organizer as a starting point of the document execution.
			NOTE: Can be used for other document execution starting points, as well.
			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		*/ 
		<%BIObject biObj = null;
			IBIObjectDAO biObjDAO = DAOFactory.getBIObjectDAO();
			String objectName = null;
			if (request.getParameter("OBJECT_NAME") == null && aRequestContainer.getServiceRequest().getAttribute("OBJECT_NAME") == null) {
				if (request.getParameter("OBJECT_ID") != null) {
					Object id = request.getParameter("OBJECT_ID");
					biObj = biObjDAO.loadBIObjectById(Integer.valueOf(id.toString()));
				} else if (aRequestContainer.getServiceRequest().getAttribute("OBJECT_ID") != null) {
					Object id = aRequestContainer.getServiceRequest().getAttribute("OBJECT_ID");
					biObj = biObjDAO.loadBIObjectById(Integer.valueOf(id.toString()));
				} else if (request.getParameter("OBJECT_LABEL") != null) {
					Object label = request.getParameter("OBJECT_LABEL");
					biObj = biObjDAO.loadBIObjectByLabel(label.toString());
				} else if (aRequestContainer.getServiceRequest().getAttribute("OBJECT_LABEL") != null) {
					Object label = aRequestContainer.getServiceRequest().getAttribute("OBJECT_LABEL");
					biObj = biObjDAO.loadBIObjectByLabel(label.toString());
				}

				objectName = biObj.getName();
			} else {
				objectName = request.getParameter("OBJECT_NAME") != null ? request.getParameter("OBJECT_NAME").toString(): aRequestContainer.getServiceRequest()
						.getAttribute("OBJECT_NAME").toString();
			}
			
			String cockpitParameters = request.getParameter("COCKPIT_PARAMETER") != null ? request.getParameter("COCKPIT_PARAMETER") : "null";
			
%>
        /*
        * Validation check for exec_from variable for security reasons
        */
		var execFrom = '<%=request.getParameter("EXEC_FROM")%>';
		if (execFrom !='WORKSPACE_ORGANIZER') {
			execFrom = null;
		}
		
		var obj = { 
				'OBJECT_ID' : 			'<%=request.getParameter("OBJECT_ID") != null
					? request.getParameter("OBJECT_ID")
					: aRequestContainer.getServiceRequest().getAttribute("OBJECT_ID")%>', 
				'OBJECT_LABEL' : 		'<%=request.getParameter("OBJECT_LABEL") != null
					? request.getParameter("OBJECT_LABEL")
					: aRequestContainer.getServiceRequest().getAttribute("OBJECT_LABEL")%>',
				'OBJECT_NAME' : 		'<%=objectName.replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\'"))%>',
				'isSourceDocument' : 	'<%=request.getParameter("IS_SOURCE_DOCUMENT") != null
					? request.getParameter("IS_SOURCE_DOCUMENT")
					: aRequestContainer.getServiceRequest().getAttribute("IS_SOURCE_DOCUMENT")%>',
				'SBI_EXECUTION_ID' : '',
				'MENU_PARAMETERS' : 	'<%=aRequestContainer.getServiceRequest().getAttribute("PARAMETERS")%>',
				'EDIT_MODE': '<%=request.getParameter("EDIT_MODE") != null
					? request.getParameter("EDIT_MODE")
					: aRequestContainer.getServiceRequest().getAttribute("EDIT_MODE")%>',
				'EXEC_FROM': execFrom,
				'COCKPIT_PARAMETER' : '<%=cockpitParameters.replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\'"))%>',
				'IS_FROM_DOCUMENT_WIDGET' : '<%=request.getParameter("IS_FROM_DOCUMENT_WIDGET")%>',
                'TOOLBAR_VISIBLE' : '<%=request.getParameter("TOOLBAR_VISIBLE")%>',
                'CAN_RESET_PARAMETERS' : '<%=request.getParameter("CAN_RESET_PARAMETERS")%>',
               

		};
		
		<%
		if (request.getParameter("SELECTED_ROLE") != null
					&& !request.getParameter("SELECTED_ROLE").equalsIgnoreCase("")) {%>
			obj.SELECTED_ROLE = '<%=request.getParameter("SELECTED_ROLE")%>';
        <%} else if (request.getParameter("ROLE") != null
                && !request.getParameter("ROLE").equalsIgnoreCase("")) {%>
                obj.SELECTED_ROLE = '<%=request.getParameter("ROLE")%>';
        <%}%>
        
	return obj;
					});
</script>
</head>
<body ng-controller="docExMasterController" class="kn-documentExecutionMaster">
	<cross-navigation layout="column" layout-fill> <cross-navigation-bread-crumb
		ng-show="false" id="docExecCrossNav"> </cross-navigation-bread-crumb>

	<!-- <iframe ng-show="crossNavigationHelper.crossNavigationSteps.value==0" flex class=" noBorder" ng-src="{{sourceDocumentUrl}}"> </iframe> -->
	<iframe
		ng-show="crossNavigationHelper.crossNavigationSteps.value==$index"
		flex class=" noBorder" ng-src="{{crossDoc.url}}"
		ng-repeat="crossDoc in crossNavigationHelper.crossNavigationSteps.stepItem">
	</iframe> </cross-navigation>

</body>
</html>
