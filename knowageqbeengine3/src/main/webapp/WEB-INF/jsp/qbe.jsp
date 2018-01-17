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

<%@ page language="java"  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>   
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>  
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/qbeImport.jsp"%>
<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>

<html ng-app="qbeManager">

	<body ng-controller="qbeController" class="kn-qbe md-knowage-theme">
	<rest-loading></rest-loading>
	<div layout="row">
		<div flex=30 layout-fill class="qbeList">
			<qbe-expander-list 
			flex drag-action="droppedFunction(data)" 
			ng-model="entityModel" 
			font-icons="fa" 
			entities-actions="entitiesFunctions" 
			fields-actions="fieldsFunctions" 
			colors="colors"
			
			>
				<md-button 	aria-label="query settings menu" class="md-icon-button" ng-click="saveEntityTree()">
		      		<md-tooltip md-direction="top">{{translate.load("kn.qbe.general.save")}}</md-tooltip>
		        	<md-icon md-menu-origin class="fa fa-floppy-o"></md-icon>
		      	</md-button>
        	</qbe-expander-list>
        	
        	<qbe-expander-list 
			flex 
			ng-model="subqueriesModel" 
			font-icons="fa" 
			entities-actions="queryFunctions"
			display-property-name="name"
			children-name="fields"
			>
				<md-button 	aria-label="query settings menu" class="md-icon-button" ng-click="createSubquery()">
		      		<md-tooltip md-direction="top">{{translate.load("kn.qbe.general.add")}}</md-tooltip>
		        	<md-icon md-menu-origin class="fa fa-plus-circle"></md-icon>
		      	</md-button>
        	</qbe-expander-list>
		</div>
		<div flex layout-fill class="vertical-devider">
			<qbe-custom-table ng-drop="true" ng-drop-success="onDropComplete($data,$event)" ng-model="queryModel" expression="expression" filters="filters">
				<div >
					<md-button  ng-click="stopEditingSubqueries()">
                    	{{query.name}}
                	</md-button>
                	<md-icon ng-if="editQueryObj.name !== query.name" md-font-icon="fa fa-chevron-right"></md-icon>
                	<md-button ng-if="editQueryObj.name !== query.name">
                    	 {{editQueryObj.name}}
                	</md-button>
				</div>
			</qbe-custom-table>
		</div>
	</div>
	</body>

</html>
