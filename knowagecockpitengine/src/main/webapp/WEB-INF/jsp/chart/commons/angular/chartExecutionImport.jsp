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

<%-- 
	author:  Danilo Ristovski (danristo, danilo.ristovski@mht.net)  
--%>

<!--
	Import of all Angular JS files that are associated to and needed by the ChartEngine execution.
	Here we import Angular application files, such as main application and controller. 	
-->

<!-- 
	This file specifies the name and the structure of the Angular application that will be responsible for managing 
	the execution of charts. 
-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/tools/scripts/app.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/tools/scripts/directives/data-preview-table/dataPreviewTable.js")%>"></script>

<!-- 
	Controller (the logic) that handles the execution of the chart.
-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/tools/scripts/controller/chartExecutionController.js")%>"></script>


<!-- 
	Configuration that is needed for our Angular application - all module that we need for it (e.g. sbiModule).
-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/tools/scripts/config/chartExecutionConfig.js")%>"></script>

<!-- 
	All directives that our application will use (for now, an empty array).
-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/tools/scripts/directives/chartExecutionDirectives.js")%>"></script>

<!-- The configuration properties container for the rendering of the charts. (danristo) -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/commons/Settings.js")%>"></script>






