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
<meta charset="utf-8">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-title" content="Knowage">
<link rel="manifest" href="<%=urlBuilder.getResourceLink(request,"/manifest.json")%>">

<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css")%>">

<!-- angular reference-->
<!-- START-DEBUG -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular.js")%>"></script> 
<!-- END-DEBUG -->

<!-- START-PRODUCTION 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular.min.js")%>"></script> 
END-PRODUCTION -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-animate.min.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-aria.min.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-sanitize.min.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-messages.min.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-cookies.js")%>"></script> 

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

<!-- angular-material-->

<%
// Angular Material 1.1.0 compresses the document viewer during document executing in phantomJs browser
// then during exports of documents we use Material 0.10.0
String importAngularMaterialForExport = null;
importAngularMaterialForExport = (String)(request.getParameter(SpagoBIConstants.IS_FOR_EXPORT));

if(importAngularMaterialForExport == null) {
%>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.js")%>"></script> 
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.css")%>">
<%
} else if(("true").equalsIgnoreCase(importAngularMaterialForExport)) {
%>
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.js")%>"></script> 
<%
}
%>


<!-- angular tree -->
<link rel="stylesheet" 	href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-tree/angular-ui-tree.min.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-tree/angular-ui-tree.js")%>"></script> 

<!-- angular list -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/AngularList.js")%>"></script> 		

<!-- context menu -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/contextmenu/ng-context-menu.js")%>"></script> 

<!--pagination-->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/pagination/dirPagination.js")%>"></script> 


<!-- expanderBox -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/expander-box/expanderBox.js")%>"></script> 

<!-- angular table -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-table/utils/daff.js")%>"></script> 

<!-- document tree -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script> 

<!-- component tree -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/component-tree/componentTree.js")%>"></script> 

<!-- file upload -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/upload-file/FileUploadBase64.js")%>"></script> 


<!-- 	angular time picker -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js")%>"></script> 

<!-- 	angular list dewtail template -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js")%>"></script> 

<!-- deprecated angular 2 col -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-list-detail/angular2Col.js")%>"></script> 

<!-- toastr -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request,"/themes/sbi_default/css/angular-toastr.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/commons/angular-toastr.tpls.js")%>"></script> 		

<!-- colorpicker -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/color-picker/tinycolor-min.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/color-picker/tinygradient.min.js")%>"></script> 

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/color-picker/angularjs-color-picker.js")%>"></script>
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/color-picker/angularjs-color-picker.min.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/color-picker/mdColorPickerPersonalStyle.css")%>">

<!-- qbe viewer -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewer.js")%>"></script> 

<!-- xml2js -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/xml2js/jquery-2.1.3.min.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/xml2js/xml2json.js")%>"></script> 

<!-- angular-base64 -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-base64/angular-base64.min.js")%>"></script> 

<!-- angular-xregexp -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/node_modules/xregexp/xregexp-all.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/node_modules/angular-xregexp/angular-xregexp.js")%>"></script> 

<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/themes/commons/css/customStyle.css")%>">

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
	
