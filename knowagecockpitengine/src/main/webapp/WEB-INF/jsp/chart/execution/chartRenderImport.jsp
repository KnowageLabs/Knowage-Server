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


	<%@page import="it.eng.spagobi.engine.chart.ChartEngineConfig"%>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartInitializer/chartInitializerModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/designer/services/chartDesignerServices.js")%>"></script>
	
	<!-- Chart initiliazer Services-->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartInitializer/services/chartInitializerRetriverService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartInitializer/services/chartJsInitializerService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartInitializer/services/d3js244InitializerService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartInitializer/services/highchartsInitializerService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartInitializer/services/highchartsDrilldownHelper.js")%>"></script>
	
	<!-- Chart Renderer Module-->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartRenderer/chartRendererModule.js")%>"></script>
	
	<!-- Chart Renderer Directives-->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartRenderer/directives/chartRenderer.js")%>"></script>
	
	<!-- JsonChartTemplateService Module -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/JsonChartTemplateService/JsonChartTemplateServiceModule.js")%>"></script>
	
	<!-- JsonChartTemplateService -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/JsonChartTemplateService/services/JsonChartTemplateService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartRenderer/services/chartConfMergeService.js")%>"></script><!-- izmena -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/chartRenderer/services/chartSonifyService.js")%>"></script><!-- izmena -->
	
	<!-- d3  -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/d3/d3.js")%>"></script>
	
	<!-- ChartJs  -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/lib/chartJs/Chart.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/d3/renderD3.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/d3/renderD3Wordcloud.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/d3/renderD3Sunburst.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/d3/renderD3Parallel.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/d3/renderD3Chord.js")%>"></script>
	
	<!-- Settings  -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/commons/Settings.js")%>"></script>

