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
<html>

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/designer/chartImport.jsp"%>

<link rel="stylesheet" type="text/css"
	href="<%=GeneralUtilities.getSpagoBiContext()%>/themes/commons/css/customStyle.css">
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular/designer/chartDesigner.js"></script>

</head>

<body class="bodyStyle" ng-app="chartDesignerManager">

	<md-toolbar>
	<div class="md-toolbar-tools">
		<h2 flex><%=docLabel%></h2>
		<span class="extraButtonContainer"></span>
		<md-button aria-label="save">PREVIEW</md-button>
		<md-button aria-label="save">SAVE</md-button>
		<md-button aria-label="cancel">BACK</md-button>
	</div>
	</md-toolbar>

	<md-tabs md-selected="selectedTab" style="min-height:calc(100% - 40px);"> 
		<md-tab	label='chart' chart-tab> </md-tab> 
		<md-tab label='structure'> </md-tab> 
		<md-tab	label='configuration'> </md-tab>
		<md-tab label='advanced'> </md-tab> 
	</md-tabs>

</body>
</html>
