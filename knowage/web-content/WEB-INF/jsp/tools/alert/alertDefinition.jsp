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
<html ng-app="alertDefinitionManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Alert definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>



<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/alert/alertDefinitionController.js"></script>


<%@include file="/WEB-INF/jsp/tools/alert/include/actions/actionsInclude.jsp"%>
<%@include file="/WEB-INF/jsp/tools/alert/include/listeners/listenersInclude.jsp"%>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/wysiwyg.min.js")%>"></script>	
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/editor.min.css")%>"> 
	
<!-- cronFrequency -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/cronFrequency/cronFrequency.js"></script>
  


<!-- <script type="text/javascript"  src="http://code.angularjs.org/1.2.14/angular-route.js"></script> -->
</head>

<body >

<angular-list-detail ng-controller="alertDefinitionController"  full-screen="true">
		
		<list label="translate.load('alarm list**')" ng-controller="alertDefinitionListController" new-function="newAlertFunction" layout-column>
		 	<angular-table flex id='alertListTable' ng-model=listAlert
				columns='alertColumnsList'
			 	 show-search-bar=true
				 click-function="alertClickEditFunction(item, index);" > </angular-table>
		</list>
		
<!-- 			 	 speed-menu-option = alertListAction -->
		
				
		<detail label="translate.load('Alarms definition **')" ng-controller="alertDefinitionDetailController"
		 save-function="saveAlertFunction"
		 cancel-function="cancelAlertFunction"
		 disable-save-button="isValidListener.status!=true || isValidListenerCrono.status!=true || alert.name == undefined || alert.name.length==0"
		 layout="column" >

  
      	<div layout="row" layout-margin>
			<md-input-container flex class="md-block">
				<label>{{translate.load("name**")}}</label>
			    <input ng-model="alert.name" >
			</md-input-container>
         
			<md-input-container flex>
				<label>{{translate.load("Listener**")}}</label>
				<md-select  ng-model="alert.alertListener" ng-model-options="{trackBy: '$value.id'}"  >
					<md-option ng-value="listener" ng-repeat="listener in listeners" >{{ listener.name }}</md-option>
				</md-select>  
			</md-input-container>
      	</div>
      	
	 	<cron-frequency ng-if="alert.frequency!=undefined" is-valid="isValidListenerCrono" ng-model=alert.frequency></cron-frequency> 
  		
  		<action-maker flex ng-if="listenerIsSelected()"   ng-model="alert.jsonOptions" template-url="alert.alertListener.template" is-valid="isValidListener"></action-maker>
       				 
	
			 </detail>
</angular-list-detail>
</body>
 
</html>