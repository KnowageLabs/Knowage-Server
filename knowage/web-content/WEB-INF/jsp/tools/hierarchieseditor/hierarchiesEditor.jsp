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
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/data2.js"></script>

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierMasterController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierTechnicalController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierBackupController.js"></script>
	
	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/hierarchies/hierarchiesStyle.css", currTheme)%>">
	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/themes/commons/css/generalStyle.css", currTheme)%>">

	
	
<title>HierarchiesEditor</title>

</head>

<body class="hierBodyStyle">
	<div ng-cloak ng-controller="hierCtrl">
		<md-content>
			<md-tabs md-dynamic-height md-border-bottom>
				<md-tab label="MASTER" ng-if="<%=canSeeMasterHier%>==true">
					<md-content layout-padding>
						<ng-include src="'${pageContext.request.contextPath}/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierMaster.html'"></ng-include>
					</md-content> 
				</md-tab>		      
				<md-tab label="TECHNICAL" md-on-select ="loadTechnical()" ng-if="<%=canSeeTechnicalHier%>==true">
					<md-content layout-padding ng-if="technicalLoaded">
						<ng-include src="'${pageContext.request.contextPath}/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierTechnical.html'"></ng-include>
					</md-content>
				</md-tab>
				<md-tab label="BACKUP"  md-on-select ="loadBackup()" ng-if="<%=canSeeTechnicalHier%>==true">
					<md-content layout-padding ng-if="backupLoaded">
						<ng-include src="'${pageContext.request.contextPath}/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierBackup.html'"></ng-include>
					</md-content>
				</md-tab>
			</md-tabs>
		</md-content>	
	</div>
 
</body>

</html>	
