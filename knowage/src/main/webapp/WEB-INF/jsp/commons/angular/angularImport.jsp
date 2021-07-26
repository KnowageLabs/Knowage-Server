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
<%@page import="it.eng.knowage.wapp.Environment"%>
<%@page import="it.eng.knowage.wapp.Version"%>
<script>
/*${disable.console.logging}*/
</script>

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-title" content="Knowage">
<link rel="manifest" href="<%=urlBuilder.getResourceLink(request,"/manifest.json")%>" crossorigin="use-credentials">

<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/node_modules/@fortawesome/fontawesome-free/css/all.min.css")%>">

<!-- angular reference -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/dist/angular-bundle.js")%>"></script> 

<!-- IE11 polyfills bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/polyfills-bundle.js")%>"></script>

<!-- All internal/external libraries bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/knowage-lib-bundle.js")%>"></script>

<!-- All node_modules libraries bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/knowage-modules-bundle.js")%>"></script>

<!-- All external styles bundle -->
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/dist/knowage-modules-styles-bundle.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/node_modules/ng-wysiwyg/dist/editor.min.css")%>">

<script>agGrid.initialiseAgGridWithAngular1(angular);</script>

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>

<% if(Version.getEnvironment() == Environment.PRODUCTION) { %>

	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/knowage-sources-bundle_"+ Version.getCompleteVersion() +".js")%>"></script>
	
<% } else { %>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/sbiModule_services/sbiModule_dateServices.js")%>"></script> 	
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/AngularList.js")%>"></script> 		 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-table/utils/daff.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/kn-table/knTable.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/component-tree/componentTree.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/upload-file/FileUploadBase64.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-list-detail/angular2Col.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-toastr.tpls.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewer.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerCommunicationService.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/workspace/scripts/directive/dataset-save/datasetSaveModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/workspace/scripts/directive/dataset-save/datasetSave.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/workspace/scripts/services/datasetSave_service.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetSchedulerModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetScheduler.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetScheduler_service.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/schedulerTimeUnit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/services/knModule_aggridLabels.js")%>"></script>
	
<%}%>

<!-- KNOWAGE main css import -->
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/themes/commons/css/customStyle.css")%>">




	
