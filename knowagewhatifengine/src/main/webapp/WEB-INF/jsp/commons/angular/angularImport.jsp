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

	<!-- polyfill -->
	<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/polyfills/includes-polyfill/includes-polyfill.js")%>"/>

	<!-- angular reference-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
	
	<!-- angular-material-->
	<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/angular-material_1.1.0/angular-material.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-material_1.1.0/angular-material.js"></script>
	
	<!-- angular-sanitize  -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-sanitize/angular-sanitize.js"></script>
	<!-- angular-cookies  -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-cookies/angular-cookies.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-cookies/ngStorage.js"></script>
	<!-- context menu -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>

	<!-- angular tree -->
	<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	
	<!-- angular table -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-table/AngularTable.js"></script>
	
	<!-- ng-draggable -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/ng-draggable/ngDraggable.js"></script>
	
	<!-- toastr -->
	<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/toastr/angular-toastr.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/toastr/angular-toastr.tpls.js"></script>
		
	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/angular-list/angular-list.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-list/AngularList.js"></script>	
	
	<!-- scroll pagination -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/kn-scroll-pagination/knScrollPagination.js"></script>	
	
	<!-- UI.Codemirror  -->
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/codemirror/lib/codemirror.css")%>"/>
	<link type="text/css" rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/codemirror/theme/eclipse.css")%>"/>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/codemirror/lib/codemirror.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/codemirror/mode/sql/sql.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/angular-ui-codemirror/src/ui-codemirror.js")%>"></script>
	
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/themes/commons/css/customStyle.css")%>">
	
	<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>