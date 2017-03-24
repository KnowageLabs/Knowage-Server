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


<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>

<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

 <%
		 	
 			Map backUrlPars = new HashMap();
 			backUrlPars.put("PAGE", "detailBIObjectPage");
 			backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO,"0");
 			backUrlPars.put("MESSAGEDET", "DETAIL_SELECT");
 			String backUrl = urlBuilder.getUrl(request, backUrlPars);
 			 			
		%>
		
		<script>
			
			var backUrl =  '<%= backUrl %>';
		</script> 



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Analytical Drivers List</title>
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/documentbrowser/analyticalDriversList.js"></script>
		<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css">
	
		</head>

	<body class="bodyStyle" ng-app="analyticalDriversListModule" id="ng-app">
	
	<div ng-controller="analyticalDriversListCTRL" layout-fill>
		<md-toolbar>
	     <div class="md-toolbar-tools layout-align-center-center layout-row">
      	
      	
       	<h2>Analytical Drivers List</h2> 
       	
       
     </div>   
   </md-toolbar>
   <md-content layout-padding>
 
   <angular-table
			flex
			id="adList_id" 
			ng-model="adList"
			columns='[
					  {"label":"Label","name":"label"},
					  {"label":"Name","name":"name"},
					  {"label":"Type","name":"type"}
					]'
			show-search-bar=true
			highlights-selected-item=true
			click-function="selectAD(item)">
		     </angular-table>
		     </md-content>
		<md-dialog-actions layout="row">
		<span flex></span>
			<md-button class="md-raised" ng-click="goBackandSave()">
				{{translate.load("sbi.generic.save");}}
			</md-button>
		</md-dialog-actions>
   
	</div>
	</body>

</html>
