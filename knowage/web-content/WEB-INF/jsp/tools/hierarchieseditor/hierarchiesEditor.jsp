<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
 
<%
boolean canSeeMasterHier=false, canSeeTechnicalHier=false,canSeeAdmin=false;
if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.HIERARCHIES_MANAGEMENT})){
 canSeeMasterHier=true;
 canSeeTechnicalHier=true;
 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.HIERARCHIES_MANAGEMENT});
} 
%>
 
<html ng-app="hierManager" >
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierarchiesController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/data.js"></script>
<!-- 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierMaster/hierTableController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierMaster/hierTreeController.js"></script>
 -->	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierMaster/hierMasterController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierTechnical/hierTechnicalController.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/commons/css/generalStyle.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/hierarchies/css/hierarchiesStyle.css">
<title>HierarchiesEditor</title>

</head>

<body class="hierBodyStyle">
	<div ng-cloak>
		<md-content>
			<md-tabs md-dynamic-height md-border-bottom>
				<md-tab label="MASTER" ng-if="<%=canSeeMasterHier%>==true">
					<md-content layout-padding>
						<ng-include src="'${pageContext.request.contextPath}/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierMaster.html'"></ng-include>
					</md-content> 
				</md-tab>		      
				<md-tab label="TECHNICAL" ng-if="<%=canSeeTechnicalHier%>==true">
					<md-content layout-padding>
						<ng-include src="'${pageContext.request.contextPath}/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierTechnical.html'"></ng-include>
					</md-content>
				</md-tab>
			</md-tabs>
		</md-content>	
	</div>
 
</body>

</html>	