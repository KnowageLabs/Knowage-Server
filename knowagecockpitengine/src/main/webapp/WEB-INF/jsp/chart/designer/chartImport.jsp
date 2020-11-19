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

<!-- --------------------------------------------------------------------------------------
	urlBuilder - for dynamically getting the full URL path to the specific resource.
	spagoBiContext - context path of core engine: /knowage
	cockpitEngineContext - context name of particular engine, in this case: /cockpitengine  
  --------------------------------------------------------------------------------------- -->
  
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/custom_directives/customDirectives.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/custom_directives/chart-tab/chart-tab.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/configuration-tab.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/custom_directives/advanced-tab/advanced-tab.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/chartstructure-tab.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/commons/Settings.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/common_directives/commonDirectives.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/third_party/thirdPartyDirectives.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/directives/chartDirectives.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/chartDesignerServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibilityModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibility/chartBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/gaugeBackwardCompatibility/gaugeBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/sunburstBackwardCompatibility/sunburstBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/scatterBackwardCompatibility/scatterBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/parallelBackwardCompatibility/parallelBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/treemapBackwardCompatibility/treemapBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/heatmapBackwardCompatibility/heatmapBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/barBackwardCompatibility/barBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/lineBackwardCompatibility/lineBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/backward/radarBackwardCompatibility/radarBackwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/channelMessaging.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/chartDesigner.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/treemap/treemap.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/heatmap/heatmap.js")%>"></script>		
