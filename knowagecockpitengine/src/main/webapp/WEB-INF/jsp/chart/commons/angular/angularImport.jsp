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
	cockpitEngineContext - context name of particular engine, in this case: /cockpitengine  
  --------------------------------------------------------------------------------------- -->

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<!-- Font awesome CSS for fancy icons. -->
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css")%>">

<!-- angular reference-->
<!-- START-DEBUG -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular_1.4/angular.js")%>"></script>
<!-- END-DEBUG -->
<!-- START-PRODUCTION 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.min.js"></script>
END-PRODUCTION -->
<!-- angular-material-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular-material_1.1.0/angular-material.min.js")%>"></script>
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular-material_1.1.0/angular-material.min.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular_1.4/angular-animate.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular_1.4/angular-aria.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular_1.4/angular-sanitize.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular_1.4/angular-messages.min.js")%>"></script>

<!-- toastr -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/themes/sbi_default/css/angular-toastr.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/commons/angular-toastr.tpls.js")%>"></script>
<!-- angular tree -->
<link rel="stylesheet" 	href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular-tree/angular-ui-tree.min.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular-tree/angular-ui-tree.js")%>"></script>

<!-- angular json tree -->
<link rel="stylesheet" 	href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular-json-tree/json-tree.css")%>">
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/angular-json-tree/json-tree.js")%>"></script>

<!-- expanderBox -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/expander-box/expanderBox.js")%>"></script>

<!-- colorpicker -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/color-picker/tinycolor-min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/color-picker/tinygradient.min.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/color-picker/angularjs-color-picker.js")%>"></script>
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/color-picker/angularjs-color-picker.min.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/angular/color-picker/mdColorPickerPersonalStyle.css")%>">
	

<!-- 
	Specifying all Angular services that we are eventually going to use (inject into the controller that is created for the 
	execution of the ChartEngine). For example, services such as sbiModule_translate, sbiModule_messaging
	@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 -->
<%@include file="/WEB-INF/jsp/chart/commons/angular/sbiModule.jspf"%>