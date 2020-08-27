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

<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/@fortawesome/fontawesome-free/css/all.css")%>"/>
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/@fortawesome/fontawesome-free/css/v4-shims.min.css")%>"/>

<!-- angular reference-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-sanitize.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-messages.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-cookies.js"></script>

<!-- angular-material-->
<script type="text/javascript" src="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/angular/angular-material_1.1.0/angular-material.min.js"></script>
<link rel="stylesheet" href="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/angular/angular-material_1.1.0/angular-material.min.css">

<!-- angular tree -->
<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.js"></script>

<!-- angular list -->
<%-- <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/angular-list.css"> --%>
<%-- <script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/AngularList.js"></script>		 --%>

<!-- context menu -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.js"></script>

<!--pagination-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>


<!-- expanderBox -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/expander-box/expanderBox.js"></script>

<!-- angular table -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script>

<!-- document tree -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>

<!-- component tree -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/component-tree/componentTree.js"></script>

<!-- file upload -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js"></script>

<!-- 	angular time picker -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>

<!-- 	angular list dewtail template -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js"></script>

<!-- deprecated angular 2 col -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-list-detail/angular2Col.js"></script>

<!-- colorpicker -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/color-picker/tinycolor-min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/color-picker/tinygradient.min.js"></script>

<!-- moment JS -->
<script type="text/javascript" src="<%=KnowageSystemConfiguration.getKnowageContext() %>/node_modules/moment/min/moment-with-locales.js"></script>  


<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/color-picker/angularjs-color-picker.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/color-picker/angularjs-color-picker.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/color-picker/mdColorPickerPersonalStyle.css">

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/themes/commons/css/customStyle.css")%>">

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
	
<!-- AG GRID -->
<script type="text/javascript" src="<%= KnowageSystemConfiguration.getKnowageContext() %>/node_modules/ag-grid-community/dist/ag-grid-community.min.js"></script>
<script>agGrid.initialiseAgGridWithAngular1(angular);</script>