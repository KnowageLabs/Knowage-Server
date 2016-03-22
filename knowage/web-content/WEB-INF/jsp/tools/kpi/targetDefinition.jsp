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
<html ng-app="kpiTarget">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Target definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/kpiCustomStyle.css", currTheme)%>">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/targetDefinitionController.js"></script>


<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/mathematicaModified.js"></script>  

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/clike/clike.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js"></script>




</head>
<body>
	<angular-list-detail ng-controller="kpiTargetController" full-screen=true >
		<list label="translate.load('sbi.kpi.list')" new-function="addKpi" >
		<angular-table 
		id='kpiListTable' ng-model=kpiList
		columns='[{"label":"Name","name":"name"},{"label":"DateCreation","name":"datacreation"},{"label":"Category","name":"valueCd"},{"label":"Author","name":"author"}]'
		columnsSearch='["name"]' show-search-bar=true
		speed-menu-option=measureMenuOption 
		scope-functions=tableFunction 
		click-function="loadKPI(item);"> </angular-table>
		</list>
		
		</angular-list-detail>
</body>
</html>
