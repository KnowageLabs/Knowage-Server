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

<!-- angular reference-->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/dist/angular-bundle.js")%>"></script> 

<!-- IE Patch (official polyfill provided by https://developer.mozilla.org )-->
<script type="text/javascript">
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
      position = position || 0;
      return this.substr(position, searchString.length) === searchString;
  };
}
if (!String.prototype.endsWith) {
	String.prototype.endsWith = function(searchString, position) {
		var subjectString = this.toString();
		if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
			position = subjectString.length;
		}
		position -= searchString.length;
		var lastIndex = subjectString.lastIndexOf(searchString, position);
		return lastIndex !== -1 && lastIndex === position;
	};
}
</script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/polyfills-bundle.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/knowage-lib-bundle.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/dist/knowage-modules-bundle.js")%>"></script>
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/dist/knowage-modules-styles-bundle.css")%>">

<!-- angular list -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/AngularList.js")%>"></script> 		 

<!-- angular table -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-table/utils/daff.js")%>"></script> 

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/kn-table/knTable.js")%>"></script> 

<!-- document tree -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script> 

<!-- component tree -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/component-tree/componentTree.js")%>"></script> 

<!-- file upload -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/upload-file/FileUploadBase64.js")%>"></script> 

<!-- 	angular time picker -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js")%>"></script> 

<!-- 	angular list detail template -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js")%>"></script> 

<!-- deprecated angular 2 col -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-list-detail/angular2Col.js")%>"></script> 

<!-- toastr -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-toastr.tpls.js")%>"></script> 

<!-- qbe viewer -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewer.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerCommunicationService.js")%>"></script> 

<!-- dataset save -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/workspace/scripts/directive/dataset-save/datasetSaveModule.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/workspace/scripts/directive/dataset-save/datasetSave.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/workspace/scripts/services/datasetSave_service.js")%>"></script>

<!-- dataset scheduler -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetSchedulerModule.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetScheduler.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetScheduler_service.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/dataset-scheduler/schedulerTimeUnit.js")%>"></script>
<!-- xml2js -->


<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/xml2js/xml2json.js")%>"></script> 

<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/themes/commons/css/customStyle.css")%>">

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>

<!-- AG GRID -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/node_modules/ag-grid-community/dist/ag-grid-community.min.js")%>"></script>
<script>agGrid.initialiseAgGridWithAngular1(angular);</script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/services/knModule_aggridLabels.js")%>"></script>
	
