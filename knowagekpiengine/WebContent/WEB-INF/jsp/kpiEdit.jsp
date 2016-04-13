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
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="templateBuid">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Template build</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/kpi-dinamic-list/KpiDinamicList.js"></script>

<!-- <script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/controllerBuildTemplate/kpiEditController.js"></script> -->
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/sbi_default/css/commons/css/customStyle.css"> 

</head>
<body ng-controller="templateBuildController">
<md-toolbar  class="miniheadimportexport">
	<div class="md-toolbar-tools">
		<h2 class="md-flex" >Kpi Document Designer</h2>
	</div>
</md-toolbar>
<md-whiteframe class="md-whiteframe-2dp relative" layout-fill layout-margin flex  >
	<expander-box id="Info" color="white" style ="background-color:#a9c3db !important" background-color="#a9c3db !important" expanded="false" title="'KpiList'">
		<!--<dinamic-list ng-model="selectedKpis" selected-item ="addKpis"></dinamic-list>  -->
	</expander-box>
</md-whiteframe>
</body>
</html>
