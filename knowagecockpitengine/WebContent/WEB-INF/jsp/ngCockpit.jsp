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

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS               --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="cockpitModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/cockpitImport.jsp"%>

<script type="text/javascript">
angular.module("cockpitModule").factory("cockpitModule_template",function(sbiModule_translate){
	var template = <%=  template%>
	
	if(template.sheets==undefined){
		template.sheets=[{label:sbiModule_translate.load("sbi.cockpit.new.sheet"),widgets:[]}];
	}
	
	if(template.configuration==undefined){
		template.configuration={};
	}
	
	
	if(template.configuration.style==undefined){
		template.configuration.style={titles : true};
	}
	
	if(template.configuration.datasets==undefined){
		template.configuration.datasets=[];
	}
	
	if(template.configuration.documents==undefined){
		template.configuration.documents=[];
	}

	if(template.configuration.associations==undefined){
		template.configuration.associations=[];
	}
	if(template.configuration.aggregations==undefined){
		template.configuration.aggregations=[];
	}
	if(template.configuration.filters==undefined){
		template.configuration.filters={};
	}
	
	return template;
});

angular.module("cockpitModule").factory("cockpitModule_analyticalDrivers",function(){
	var ad = <%=  analyticalDriversParams%>
	return ad;
});

angular.module("cockpitModule").factory("cockpitModule_properties",function(){
	return {
		EDIT_MODE:angular.equals("<%= documentMode %>","EDIT"),
		DOCUMENT_ID: <%=  docId%>,
		DOCUMENT_NAME: "<%=  docName%>",
		DOCUMENT_LABEL:"<%=  docLabel%>",
		DOCUMENT_DESCRIPTION:"<%=  docDescription%>",
		WIDGET_EXPANDED:{},
		SELECTED_ROLE:"<%= 	executionRole %>",
		DS_IN_CACHE:[],
		HAVE_SELECTIONS_OR_FILTERS:false,
		STARTING_SELECTIONS:[],
		STARTING_FILTERS:[],
	}
});


</script>
<%-- <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> --%>
<%-- <link rel="stylesheet" type="text/css" href="${request.contextPath}/themes/commons/css/customStyle.css"> --%>
<!-- <link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/customStyle.css"> -->
<link rel="stylesheet" type="text/css" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/customStyle.css">

<title>Cockpit engine</title>
</head>
<body class="kn-cockpit " ng-class="{'disableanimation':sbiModule_device.browser.name!='chrome'}" md-no-ink ng-controller="cockpitMasterController" layout="column">

	<cockpit-toolbar config="configurator"></cockpit-toolbar>
	<cockpit-sheet flex ng-if="datasetLoaded"></cockpit-sheet>
</body>
</html>