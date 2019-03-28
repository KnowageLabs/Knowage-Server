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
<%
	String spagoBiContext = request.getParameter(SpagoBIConstants.SBI_HOST)+request.getParameter(SpagoBIConstants.SBI_CONTEXT);
%>

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<link rel="stylesheet" href="<%=GeneralUtilities.getSpagoBiContext()%>/node_modules/font-awesome/css/font-awesome.min.css">

<!--  script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular.js"></script-->

<!-- angular reference-->
<!-- START-DEBUG -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular/angular.js"></script>

<!-- END-DEBUG -->
<!-- START-PRODUCTION 
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular.min.js"></script>
END-PRODUCTION -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular-animate/angular-animate.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular-aria/angular-aria.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular-sanitize/angular-sanitize.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular-messages/angular-messages.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular-cookies/angular-cookies.min.js"></script>
<!--  script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular-sanitize.min.js"></script>
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular-messages.min.js"></script>
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular_1.4/angular-cookies.js"></script-->

<!-- POLYFILLS -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/canvas-toBlob/canvas-toBlob.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/promise-polyfill/promise-polyfill.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/map-polyfill/map-polyfill.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/append-polyfill/append-polyfill.js"></script>


<!-- angular-material-->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-material_1.1.0/angular-material.min.js"></script>
<link rel="stylesheet" href="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-material_1.1.0/angular-material.min.css">

<!-- angular tree -->
<link rel="stylesheet" 	href="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-tree/angular-ui-tree.min.css">
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-tree/angular-ui-tree.js"></script> 

<!-- context menu -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/contextmenu/ng-context-menu.js"></script>

<!--pagination-->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/pagination/dirPagination.js"></script>


<!-- expanderBox -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/expander-box/expanderBox.js"></script>

<!-- angular table -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/tools/commons/cockpit-angular-table/CockpitAngularTable.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/tools/commons/cockpit-table/cockpitTable.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/tools/commons/accessible-angular-table/AccessibleAngularTable.js"></script>
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/angular-table/utils/daff.js"></script>

<!-- document tree -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>

<!-- component tree -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/component-tree/componentTree.js"></script>

<!-- file upload -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js"></script>

<!-- 	angular time picker -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>

<!-- 	angular list dewtail template -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js"></script>

 <!-- angular-gridster-->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-gridster/angular-gridster.min.js"></script>
<link rel="stylesheet" href="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-gridster/angular-gridster.min.css">
  
<!-- colorpicker -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/color-picker/tinycolor-min.js"></script>
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/color-picker/tinygradient.min.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/lib/angular/color-picker/angularjs-color-picker.js"></script>
<link rel="stylesheet" href="<%=engineContext%>/js/lib/angular/color-picker/angularjs-color-picker.min.css">
<link rel="stylesheet" href="<%=engineContext%>/js/lib/angular/color-picker/mdColorPickerPersonalStyle.css">

<!--  wysiwyg -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/ngWYSIWYG/wysiwyg.min.js"></script>	
<link rel="stylesheet" type="text/css" href="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/ngWYSIWYG/editor.min.css">

<!-- 		angular-drag-and-drop-lists -->
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-drag-and-drop-lists/angular-drag-and-drop-lists.js"></script>	

<!--[if gte IE 8]>
	<link rel="stylesheet" type="text/css" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/customStyle_ie.css" />
<![endif]-->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/canvg/dist/browser/canvg.min.js"></script>
<link rel="stylesheet" type="text/css" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/customStyle.css">	

<!--  angular JSON FORMATTER -->
<link rel="stylesheet" type="text/css" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/jsonformatter/dist/json-formatter.min.css">
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/jsonformatter/dist/json-formatter.min.js"></script>	

<!-- UI.Codemirror  -->
<link rel="stylesheet" type="text/css" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/codemirror/lib/codemirror.css">
<link type="text/css" rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/codemirror/theme/eclipse.css">
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/codemirror/lib/codemirror.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/codemirror/addon/mode/simple.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/codemirror/mode/xml/xml.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/codemirror/mode/css/css.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/angular-ui-codemirror/src/ui-codemirror.js"></script>

<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/ag-grid.min.js"></script>

<!-- angular json tree -->
<link rel="stylesheet" 	href="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-json-tree/json-tree.css">
<script type="text/javascript" src="<%=GeneralUtilities.getSpagoBiContext()%>/js/lib/angular/angular-json-tree/json-tree.js"></script>
<!-- MOMENT.JS -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/moment/min/moment-with-locales.min.js"></script>

<!-- html2canvas -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/html2canvas/dist/html2canvas.min.js"></script>

<!--  JSPDF -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/jspdf/dist/jspdf.min.js"></script>

<!-- FILESAVER -->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/file-saver/dist/FileSaver.min.js"></script>

<%@include file="/WEB-INF/jsp/commons/includeCometd.jspf"%>

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>