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


<!-- include file="/WEB-INF/jsp/commons/portlet_base.jsp" --> 
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@ page language="java" pageEncoding="utf-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% 
	String contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
%>

<%
		//TODO check for user profile autorization
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION});
		}
%>

<% if(canSee ){ 
	String objectId = request.getParameter(ObjectsTreeConstants.OBJECT_ID);
	Map backUrlPars = new HashMap();
	backUrlPars.put("PAGE", "detailBIObjectPage");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO,"1");
	backUrlPars.put("MESSAGEDET", "DETAIL_SELECT");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>


<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/crossnavigation/css/cross-definition.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/cross/outpars/ManageOutputParameters.js"></script>
<script>
var objectId = <%=objectId%>;
</script>
</head>

<body class="bodyStyle" ng-app="crossOutPars" id="ng-app" >
	<div layout="row" layout-align="end center">
		<a href='<%=backUrl%>'>
			<md-icon md-font-icon="fa fa-times fa-2x" ></md-icon>
		</a>
	</div>
	<angular-list-detail ng-controller="outputParametersController as ctrl" new-function="ctrl.newFunc" save-function="ctrl.saveFunc" cancel-function="ctrl.cancelFunc" >
       <list label="translate.load('sbi.outputparameter.lst')"  > <!-- Requires an instruction like $scope.translate = sbiModule_translate on myController -->
			<!-- parameters list -->
			<angular-table 
				layout-fill
				id="dataSourceList"
				ng-model="ctrl.list"
				columns="config.list.columns"
				highlights-selected-item=true
				click-function="ctrl.loadSelected(item)"
				speed-menu-option="config.list.dsSpeedMenu"					
			>						
			</angular-table>
			<div ng-show="ctrl.listloadingSpinner" class="loadingSpinner">
				<i class="fa fa-spinner fa-pulse fa-4x"></i> 
			</div>
		</list>
		
        <detail label="ctrl.detail.title || ''" > <!-- assuming that $scope.selectedItem stores the selected item on teh controller  -->
			<form name="tsForm" novalidate >			
				<div layout="column" layout-wrap>
					<md-input-container > <label>{{translate.load("sbi.crossnavigation.parname.lbl");}}</label>
						<input maxlength="100" type="text" ng-model="ctrl.detail.name" required > 
					</md-input-container>
					<md-input-container flex="80"> <label>{{translate.load("sbi.crossnavigation.type.lbl");}}</label> 
						<md-select ng-model="ctrl.detail.typeId" required>
				        	<md-option ng-repeat="l in ctrl.typeList" value="{{l.id}}"> {{l.descr}} </md-option>
				        </md-select>
					</md-input-container>
				</div>
			</form>
		</detail>
			
	</angular-list-detail>
</body>
</html>


<%}else{ %>
Access Denied
<%} %>

