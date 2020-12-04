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

<!-- ---------------------------------------------------------------------------------------
	urlBuilder - for dynamically getting the full URL path to the specific resource.
	spagoBiContext - context path of core engine: /knowage
	cockpitEngineContext - context name of particular engine, in this case: /cockpitengine  
  -------------------------------------------------------------------------------------- -->

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/@fortawesome/fontawesome-free/css/all.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/node_modules/weather-icons/css/weather-icons.min.css")%>">

<!-- angular reference-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/dist/angular-bundle.js")%>"></script>

<!-- IE11 polyfills bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/dist/polyfills-bundle.js")%>"></script>

<!-- All internal/external libraries bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/dist/knowage-lib-bundle.js")%>"></script>

<!-- All internal/external cockpit specific libraries bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/dist/knowagecockpit-lib-bundle.js")%>"></script>

<script type="text/javascript">
    if(/MSIE \d|Trident.*rv:/.test(navigator.userAgent))
        document.write('<script type="text/javascript" src="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/rgbcolor/rgbcolor.js"><\/script>');
</script>

<!-- All node_modules libraries bundle -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/dist/knowagecockpit-modules-bundle.js")%>"></script>

<!-- All external styles bundle -->
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(cockpitEngineContext,"/dist/knowagecockpit-modules-styles-bundle.css")%>">
<link rel="stylesheet" href="<%=urlBuilder.getResourcePath(spagoBiContext,"/node_modules/ng-wysiwyg/dist/editor.min.css")%>">

<%@include file="/WEB-INF/jsp/commons/includeCometd.jspf"%>
<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>

<% if(Version.getEnvironment() == Environment.PRODUCTION) { %>

	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/dist/knowagecockpit-sources-bundle_"+ Version.getCompleteVersion() +".js")%>"></script>
	
<% } else { %>

	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-table/utils/daff.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/component-tree/componentTree.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/calculated-field/calculatedFieldMode.js")%>"></script>
 
 <%}%>

<!-- KNOWAGE main css import -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(spagoBiContext, "/themes/commons/css/customStyle.css")%>"/>

	
