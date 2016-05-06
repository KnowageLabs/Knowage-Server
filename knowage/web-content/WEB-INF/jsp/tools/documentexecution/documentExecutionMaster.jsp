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
 
<%@ page language="java" pageEncoding="utf-8" session="true"%>
<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
 

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="documentExecutionMasterModule">
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/customStyle.css"> 
	
	<!-- 	breadCrumb -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
	
	<!-- cross navigation -->
	<script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/cross-navigation/crossNavigationDirective.js")%>"></script>
	<script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecutionMaster.js")%>"></script>
	<script type="text/javascript">
	angular.module('documentExecutionMasterModule').factory('sourceDocumentExecProperties', function() {
		
		var obj = { 
				'OBJECT_ID' : 			'<%= request.getParameter("OBJECT_ID") != null ? request.getParameter("OBJECT_ID") : aRequestContainer.getServiceRequest().getAttribute("OBJECT_ID")  %>', 
				'OBJECT_LABEL' : 		'<%= request.getParameter("OBJECT_LABEL") != null ? request.getParameter("OBJECT_LABEL") : aRequestContainer.getServiceRequest().getAttribute("OBJECT_LABEL") %>',
				'OBJECT_NAME' : 		'<%= request.getParameter("OBJECT_NAME") != null ? request.getParameter("OBJECT_NAME") : aRequestContainer.getServiceRequest().getAttribute("OBJECT_NAME") %>',
				'isSourceDocument' : 	'<%= request.getParameter("IS_SOURCE_DOCUMENT") != null ? request.getParameter("IS_SOURCE_DOCUMENT") : aRequestContainer.getServiceRequest().getAttribute("IS_SOURCE_DOCUMENT") %>',
				'isFromCross' : false, 
				'isPossibleToComeBackToRolePage' : false,
				'SBI_EXECUTION_ID' : ''
			  	
		};
		return obj;
	});
	</script>
</head>
<body ng-controller="docExMasterController">
 <cross-navigation layout="column" layout-fill>
 <cross-navigation-bread-crumb ng-show="false" id="docExecCrossNav"> </cross-navigation-bread-crumb>
 
<!-- <iframe ng-show="crossNavigationHelper.crossNavigationSteps.value==0" flex class=" noBorder" ng-src="{{sourceDocumentUrl}}"> </iframe> -->
<iframe ng-show="crossNavigationHelper.crossNavigationSteps.value==$index" flex class=" noBorder" ng-src="{{crossDoc.url}}" ng-repeat="crossDoc in crossNavigationHelper.crossNavigationSteps.stepItem"> </iframe>
</cross-navigation>
 
</body>
</html>
	