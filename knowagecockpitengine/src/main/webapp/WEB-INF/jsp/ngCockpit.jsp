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
<%@include file="/WEB-INF/jsp/chart/designer/chartImport.jsp"%>
<%@include file="/WEB-INF/jsp/chart/designer/chartDesignerConf.jspf"%>
<%@include file="/WEB-INF/jsp/chart/execution/chartRenderImport.jsp"%>
<base href="/" />

<script type="text/javascript">

angular.module("cockpitModule").factory("cockpitModule_properties",function(){
	return {
		EDIT_MODE:angular.equals("<%= StringEscapeUtils.escapeJavaScript(documentMode) %>","EDIT"),
		DOCUMENT_ID: <%=  docId%>,
		DOCUMENT_NAME: "<%=  StringEscapeUtils.escapeJavaScript(docName)%>",
		DOCUMENT_LABEL:"<%=  StringEscapeUtils.escapeJavaScript(docLabel)%>",
		DOCUMENT_DESCRIPTION:"<%=  StringEscapeUtils.escapeJavaScript(docDescription)%>",
		WIDGET_EXPANDED:{},
		SELECTED_ROLE:"<%= 	StringEscapeUtils.escapeJavaScript(executionRole) %>",
		OUTPUT_PARAMETERS: <%=outputParameters%>,
		DS_IN_CACHE:[],
		HAVE_SELECTIONS_OR_FILTERS:false,
		STARTING_SELECTIONS:[],
		STARTING_FILTERS:[],
		CURRENT_SHEET: <%=initialSheet%>,
		FOLDER_ID: "<%=folderId%>",
		EXPORT_MODE: <%=exportMode%>,
		INITIALIZED_WIDGETS : [],
		DIRTY_WIDGETS : [],
		CURRENT_KNOWAGE_VERSION: "<%=it.eng.knowage.wapp.Version.getVersionForDatabase()%>"
	}
});

angular.module("cockpitModule").factory("accessibility_preferences",function(){
	return {
		accessibilityModeEnabled:<%= isUIOEnabled %>,
		isUIOEnabled:<%= isUIOEnabled %>,
		isRobobrailleEnabled:<%= isRobobrailleEnabled %>,
		isVoiceEnable:<%= isVoiceEnable %>,
		isGraphSonificationEnabled:<%= isGraphSonificationEnabled %>
		
		
	}
});

angular.module("cockpitModule").factory("cockpitModule_template",function(sbiModule_translate,cockpitModule_properties,cockpitModule_defaultTheme){
	var template = <%=  template%>
	
	if(template.sheets==undefined){
		template.sheets=[{label:sbiModule_translate.load("sbi.cockpit.new.sheet"),widgets:[]}];
	}
	
	if(template.configuration==undefined){
		template.configuration={};
	}
	if(template.configuration.showMenuOnView==undefined){
		template.configuration.showMenuOnView=true;
	}
	if(template.configuration.showSelectionButton==undefined){
		template.configuration.showSelectionButton=true;
	}
	if(template.configuration.style==undefined){
		template.configuration.style=cockpitModule_defaultTheme.cockpit.style;
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
	if(template.configuration.variables==undefined){
		template.configuration.variables=[];
	}

    var cockpitSelections = JSON.parse('<%=initialSelections%>');
	if(cockpitSelections.aggregations && cockpitSelections.aggregations.length > 0) {
	    angular.merge(template.configuration.aggregations, cockpitSelections.aggregations);
	}
	if(cockpitSelections.filters && !angular.equals(cockpitSelections.filters, {})) {
    	template.configuration.filters = cockpitSelections.filters;
    }
	var chartLibNamesConfig = <%=ChartEngineUtil.getChartLibNamesConfig()%>;
	function filterForInitialSelection(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_SELECTIONS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_SELECTIONS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}
	
	function filterForInitialFilter(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_FILTERS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_FILTERS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}	
	
	template.getSelections=function(){
		
		template.selections=[];
		var tmpFilters = {};
		var tmpSelection=[];
				
		angular.copy(template.configuration.filters, tmpFilters);
		for(var ds in tmpFilters){
			for(var col in tmpFilters[ds]){
				var tmpObj={
						ds :ds,
						columnName : col,
						value : tmpFilters[ds][col],
						aggregated:false
				}
				 
				if(!filterForInitialFilter(tmpObj)){
					template.selections.push(tmpObj);
				}
			}
		}
		
		angular.copy(template.configuration.aggregations, tmpSelection);
		if(tmpSelection.length >0){
			for(var i=0;i<tmpSelection.length;i++){
				selection = tmpSelection[i].selection;
				for(var key in selection){
					var string = key.split(".");
					var obj = {
							ds : string[0],
							columnName : string[1],
							value : selection[key],
							aggregated:true
					};
					if(!filterForInitialSelection(obj)){
						template.selections.push(obj);
					}
				}
			}
		}
		return template.selections;	
	}
	
	console.log("template:"+template);
	// back compatibility with old document parameters
	for(var i=0; i<template.configuration.associations.length; i++){
		var association = template.configuration.associations[i];
		for(var j=0; j<association.fields.length; j++){
			var field = association.fields[j];
			if(field.type=="document"
					&& !field.column.startsWith("$P{")
					&& !field.column.endsWith("}")){
				association.description = association.description.replace(field.store+"."+field.column,field.store+"."+"$P{" + field.column + "}");
				field.column = "$P{" + field.column + "}";
			}
		}
	}

	for(var i in template.sheets){
        var sheet = template.sheets[i];
        for(var j in sheet.widgets){
            var widget = sheet.widgets[j];
            cockpitModule_properties.DIRTY_WIDGETS.push(widget.id);
        }
    }

	return template;
});

angular.module("cockpitModule").factory("cockpitModule_analyticalDrivers",function(){
	var ad = <%=  analyticalDriversParams%>
	return ad;
});

angular.module("cockpitModule").factory("cockpitModule_analyticalDriversUrls",function(){
	var ad = <%= analyticalDriversParamsObj %>
	return ad;
});

var chartLibNamesConfig = <%=ChartEngineUtil.getChartLibNamesConfig()%>;

</script>

<title>Cockpit engine</title>
</head> 
	<body class="kn-cockpit " id="kn-cockpit" ng-class="{'disableanimation':sbiModule_device.browser.name!='chrome'}" md-no-ink ng-controller="cockpitMasterControllerWrapper" layout="column" style="background-image:url({{imageBackgroundUrl}}); background-color:{{cockpitModule_template.configuration.style.sheetsBackgroundColor}}; background-size:{{cockpitModule_template.configuration.style.imageBackgroundSize||'contain'}}; background-repeat: no-repeat; background-position: center;" >
		<style ng-bind-html="trustedGeneralCss"></style>
		<cockpit-toolbar config="configurator"></cockpit-toolbar>
		<cockpit-sheet flex ng-if="datasetLoaded"></cockpit-sheet>
	</body>
</html>