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
<!-- --------------------------------------------------------------------------------------
	urlBuilder - for dynamically getting the full URL path to the specific resource.
	spagoBiContext - context path of core engine: /knowage
	qbeEngineContext - context name of particular engine, in this case: /qbeengine  
  --------------------------------------------------------------------------------------- -->

<!-- angular -->
<!-- START-DEBUG -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular/angular.js")%>"></script> 
<!-- END-DEBUG -->

<!-- angular material-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-animate/angular-animate.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-aria/angular-aria.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-sanitize/angular-sanitize.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-cookies/angular-cookies.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-messages/angular-messages.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-material/angular-material.min.js")%>"></script>
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-material/angular-material.min.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/lib/bootstrap/css/bootstrap.min.css")%>">
<!-- ngdraggable -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/ngdraggable/ngDraggable.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/lib/angular/angular-drag-and-drop-lists/angular-drag-and-drop-lists.js")%>"></script>

<!-- angular list detail template -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js")%>"></script> 

<!-- Angular table -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/lib/angular/pagination/dirPagination.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/kn-table/knTable.js")%>"></script> 

<!-- moment JS -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/moment/min/moment-with-locales.js")%>"></script>  

<!-- Angular time picker -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/lib/registry-date-time-picker/registryDateTimePicker.js")%>"></script> 



<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-table/utils/daff.js")%>"></script>

<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/al-click/al-click.js")%>"></script> 

<!-- UI.Codemirror  -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/codemirror/lib/codemirror.css")%>">
<link type="text/css" rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/codemirror/theme/eclipse.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/codemirror/lib/codemirror.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/codemirror/addon/mode/simple.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/codemirror/mode/javascript/javascript.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/calculated-field-editor/customMode.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/angular-ui-codemirror/src/ui-codemirror.js")%>"></script>

<!-- AG GRID -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/ag-grid-community/dist/ag-grid-community.min.js")%>"></script>

<!-- font-awesome -->
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/@fortawesome/fontawesome-free/css/all.min.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/@fortawesome/fontawesome-free/css/v4-shims.min.css")%>"> 

<!-- main css -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/themes/commons/css/customStyle.css")%>">

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule/sbiModule.jspf"%>

<%@include file="/WEB-INF/jsp/commons/angular/formulas/formulas.jspf"%>

