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
<html ng-app="geoManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/behaviouralmodel/analyticaldriver/mapFilter/geoImport.jsp"%>

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/mapFilter/geoController.js"></script>
	<title><spagobi:message key="SBIDev.mapFilter.pagetitle"/></title>
</head>

<%
	String selectedLayer = "";
	String selectedLayerProp = "";
	String selectedPropData = "";
	String multivalueFlag = "false";
	
	if (request.getParameter("SELECTEDLAYER") != null && !request.getParameter("SELECTEDLAYER").equals("")) {
		selectedLayer = request.getParameter("SELECTEDLAYER");
	}
	if (request.getParameter("SELECTEDLAYERPROP") != null && !request.getParameter("SELECTEDLAYERPROP").equals("")) {
		selectedLayerProp = request.getParameter("SELECTEDLAYERPROP");
	}
	if (request.getParameter("SELECTEDPROPDATA") != null && !request.getParameter("SELECTEDPROPDATA").equals("")) {
		selectedPropData = request.getParameter("SELECTEDPROPDATA");
		selectedPropData = selectedPropData.replaceAll("'", "&quot;");
	}
	if (request.getParameter("MULTIVALUE") != null && !request.getParameter("MULTIVALUE").equals("")) {
		multivalueFlag = request.getParameter("MULTIVALUE");
	}
%>

<body class="mapBodyStyle">
	<div ng-controller="mapCtrl" ng-cloak layout-fill 
			ng-init="setLayerConf('<%=selectedLayer%>', '<%=selectedLayerProp%>', '<%=selectedPropData%>', <%=multivalueFlag%>)">
		<div class='geoFilterSelection'>
			<div class="md-whiteframe-z2 itemboxGU" ng-show="showPanelFlag()">
				<!--
				<div layout-fill class="unselectable"></div> 
				-->
				<div layout="column" layout-padding>
					<div flex layout-padding>
						{{sbiModule_translate.load('sbi.execution.parametersselection.mapfilter.save')}}
					</div>
					<div flex layout-padding>
						{{sbiModule_translate.load('sbi.execution.parametersselection.mapfilter.selectedfeaturenumber')}}: {{geo_interaction.selectedFeatures.length}}
					</div>
				</div>
			</div>
		</div>
		<div id='map'></div>
	</div>
</body>

</html>
