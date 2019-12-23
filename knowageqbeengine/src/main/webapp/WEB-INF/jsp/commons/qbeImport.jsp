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
	qbeEngineContext - context name of particular engine, in this case: /qbeengine  
  --------------------------------------------------------------------------------------- -->

<!--MAIN-->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/app.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/controller/controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/config/config.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/directive.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/services.js")%>"></script>

<!-- DIRECTIVES -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/advanced-visualization/advancedVisualization.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/filter-visualization/filterVisualization.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/custom-table/customTable.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/expander-list/expanderList.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/calculated-field-editor/calculatedFieldEditor.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/calculated-field-editor/withoutPropertyValue.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/filter/filter.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/parameters/parameter.js")%>"></script>
<%-- <script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/group/group.js")%>"></script> --%>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/save/save.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/having/having.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/relations/relationshipsModule.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/relations/joinTypeDefinition.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/directive/pagination/pagination.js")%>"></script>


<!-- SERVICES -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/entities/entities.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/queries/queries.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/filters/filters.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/filters/expression.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/queries/queryObject.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/save/saveService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/parameter/parameterService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/relationships/selectedEntitiesRelationshipsModule.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/relationships/selectedEntitiesRelationshipsService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/relationships/queryEntitiesService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/format/formatModule.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/format/formatter/formatter.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/services/format/formatDate/formatDate.js")%>"></script>


<!-- FILTERS -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/filters/byNotExistingMembers.js")%>"></script>




<!-- EXPORT -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/common/export/exportModule.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/common/export/services/exportService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/qbe/common/export/directives/export-menu/exportMenu.js")%>"></script>

<!-- ADVANCE FILTERS -->
<%@include file="/WEB-INF/jsp/commons/advancedFilterAppImport/advancedFilterImport.jspf"%>

<!-- FILTER TARGET APP -->
<%@include file="/WEB-INF/jsp/commons/targetApp/targetAppImport.jspf"%>
