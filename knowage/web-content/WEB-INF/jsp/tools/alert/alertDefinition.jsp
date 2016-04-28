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

	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/wysiwyg.min.js")%>"></script>	
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/editor.min.css")%>"> 
	
<!-- cronFrequency -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/cronFrequency/cronFrequency.js"></script>
  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/alert/listeners/kpiListener/alertKpiListenerController.js"></script>

<script type="text/javascript"  src="http://code.angularjs.org/1.2.14/angular-route.js"></script>
</head>

<body ng-controller="alertDefinitionController" layout="row" >
	<md-content layout="column" layout-fill > 
		<md-toolbar class="md-hue-2" >
      		<div class="md-toolbar-tools"> 
        		<h2> 
        			<span>{{translate.load("Alarms definition **")}}</span>
        		</h2>  
      		</div>
      	</md-toolbar>
       	<md-select layout-margin ng-model="alert.selectedListener" placeholder="Select a listener" ng-change="changeListener()" >
			<md-option ng-value="listener" ng-repeat="listener in listeners">{{ listener.name }}</md-option>
		</md-select>  
	 	<md-whiteframe ng-if="alert.selectedListener!=undefined" class="md-whiteframe-1dp"> 
    		<cron-frequency ng-model=alert.frequency></cron-frequency>
  		</md-whiteframe>
  		
  		<div ng-view ng-if="listenerIsSelected()" flex layout class="md-whiteframe-1dp" layout-margin ></div>
  		 						  
	</md-content>
</body>
 
</html>