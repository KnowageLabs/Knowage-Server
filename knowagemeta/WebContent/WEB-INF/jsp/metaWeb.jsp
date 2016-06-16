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


<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="metaManager">

<head>
<%-- <%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%> --%>
<%
String datasourceId= request.getParameter("datasourceId");
%>

<script> 
var datasourceId='<%= datasourceId%>';
</script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>meta Definition</title>
	
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/meta/metaDefinitionController.js"></script>
</head>
<body ng-controller="metaDefinitionController" layout="column" ng-switch on="steps.current">
	<md-toolbar>
		<h1 class="md-toolbar-tools" layout="row">
			<span flex>{{translate.load("sbi.meta.definition")}}</span>
			 <md-button ng-click="continueToMeta()">{{translate.load("sbi.general.continue")}}</md-button>
			 <md-button ng-click="closeMetaDefinition()">{{translate.load("sbi.general.cancel")}}</md-button>
		</h1>
	</md-toolbar>
	<md-content ng-controller="metaModelDefinitionController" flex layout ng-switch-when="0">
		<angular-table flex id='datasourceStructureListTable' ng-model=dataSourceStructure
				columns='datasourceStructureColumnsList'
				scope-functions='datasourceStructureScopeFunctions'
			 	show-search-bar=true no-pagination="true"
			 	sortable-column="['columnName']" >
			 	
			 	<queue-table>
				 	<md-divider ></md-divider>
			 		<div layout="row" layout-align="space-around center" >
				 		<md-checkbox aria-label='check all business' ng-checked='scopeFunctions.allPhysicalModelAreChecked()' ng-click='scopeFunctions.toggleAllPhysicalModel()'> {{scopeFunctions.translate.load("sbi.meta.model.physical.selectAll")}}</md-checkbox>
				 		<md-checkbox aria-label='check all business' ng-checked='scopeFunctions.allBusinessModelAreChecked()' ng-click='scopeFunctions.toggleAllBusinessModel()'>  {{scopeFunctions.translate.load("sbi.meta.model.business.selectAll")}}</md-checkbox>
			 		</div>
			 	</queue-table>
	 	 </angular-table>
	
	</md-content>
	
	<md-content ng-controller="metaModelCreationController" flex layout ng-switch-when="1">
		
		<md-tabs flex>
			<md-tab id="businessTab">
				<md-tab-label>{{translate.load("sbi.meta.model.business")}}</md-tab-label>
				<md-tab-body >
					<md-content layout="row" flex layout-fill>
						<md-content layout="row" flex="30"   layout-margin  class="md-whiteframe-9dp">
							 	<component-tree id="bcmTree" layout-fill style="position:absolute"
									ng-model="businessModel"
									highlights-selected-item="true"   
									subnode-key="columns" 
									click-function="selectBusinessModel(node)"
									hide-progress=true
									not-hide-on-load = true
									is-folder-fn="businessModel_isFolder(node)"
									folder-icon-fn="businesslModel_getlevelIcon(node)"
									open-folder-icon-fn="getOpenFolderIcons(node)"
									interceptor="businessModelTreeInterceptor"
									static-tree=true
									expand-on-click=false
								></component-tree>
						</md-content>
<!-- 									dynamic-tree=true -->
					
						<md-content layout="column" flex class="md-whiteframe-9dp"  layout-margin  ng-if="selectedBusinessModel.name!=undefined" >
							<md-toolbar class="md-theme-indigo">
								<h1 class="md-toolbar-tools">{{selectedBusinessModel.name}}</h1>
							</md-toolbar>

							<md-tabs flex>
								<md-tab id="propertiestab" label="{{translate.load('sbi.udp.udpList')}}">
									<md-content layout="column"  layout-fill>
										<expander-box layout-margin expanded="true" title="catProp" background-color="transparent" color="black" ng-repeat="catProp in currentBusinessModelParameterCategories">
											<md-input-container ng-repeat="prop in selectedBusinessModel.properties | filterByCategory:catProp"
											ng-init="prop.value.value= (prop.value.value==undefined || prop.value.value==null) ? prop.value.propertyType.defaultValue : prop.value.value">
												<label>{{prop.value.propertyType.name}}</label>
												<md-select ng-model="prop.value.value" ng-if="prop.value.propertyType.admissibleValues.length!=0">
													<md-option ng-repeat="admissibleValue in prop.value.propertyType.admissibleValues" value="{{admissibleValue}}" >
														{{admissibleValue}}
													</md-option>
												</md-select>
												
												<input ng-model="prop.value.value" ng-if="prop.value.propertyType.admissibleValues.length==0">
												
											</md-input-container>
										</expander-box>
									</md-content>
								</md-tab>
								
								<md-tab id="attributesTab" label="{{translate.load('sbi.generic.attributes')}}" ng-if="selectedBusinessModel.columns!=undefined">
									<md-content layout  layout-fill >
										<angular-table id="bmAttr" ng-model="selectedBusinessModel.simpleBusinessColumns"
										 columns="selectedBusinessModelAttributes" scope-functions="selectedBusinessModelAttributesScopeFunctions" no-pagination=true flex>
										</angular-table>
									</md-content>
									
								</md-tab>
								
								<md-tab id="inboundTab" label="{{translate.load('sbi.meta.model.business.inbound')}}"  ng-if="selectedBusinessModel.columns!=undefined">
									<angular-table id="inbountTable"
									 	ng-model="selectedBusinessModel.relationships" 
									 	show-search-bar=true
									 	no-pagination="true"
									 	columns="[{label:'name',name:'name'}]"
									 	visible-row-function="isInbound(item)">
									 </angular-table>
								
								</md-tab>
								
								<md-tab id="outboundTab" label="{{translate.load('sbi.meta.model.business.outbound')}}"  ng-if="selectedBusinessModel.columns!=undefined">
									<angular-table id="outbountTable"
									 	ng-model="selectedBusinessModel.relationships" 
									 	columns="[{label:'name',name:'name'}]"
									 	show-search-bar=true
									 	no-pagination="true"
									 	visible-row-function="isOutbound(item)">
									 </angular-table>
								
								</md-tab>
							</md-tabs>


							
						</md-content>
											
					</md-content>
				</md-tab-body>
			</md-tab>
			
			<md-tab id="physicalTab">
				<md-tab-label>{{translate.load("sbi.meta.model.physical")}}</md-tab-label>
				<md-tab-body>
					<md-content layout="row" flex layout-fill>
						<md-content layout="row" flex="30" class="md-whiteframe-9dp"  layout-margin >
							<component-tree id="pmTree" layout-fill style="position:absolute"
								ng-model="physicalModel"
								highlights-selected-item="true"   
								subnode-key="columns" 
								click-function="selectPhysicalModel(node)"
								hide-progress=true
								not-hide-on-load = true
								folder-icon-fn="physicalModel_getlevelIcon(node)"
								open-folder-icon-fn="getOpenFolderIcons(node)"
								is-folder-fn="physicalModel_isFolder(node)"
							></component-tree>
							
						</md-content>
						
						<md-content layout="column" flex class="md-whiteframe-9dp"  layout-margin  ng-if="selectedPhysicalModel.name!=undefined" >
							<md-toolbar class="md-theme-indigo">
								<h1 class="md-toolbar-tools">{{selectedPhysicalModel.name}}</h1>
							</md-toolbar>

							<md-tabs flex>
								<md-tab id="propertiestab" label="{{translate.load('sbi.udp.udpList')}}">
									<md-content layout="column"  layout-fill>
									
										<expander-box layout-margin expanded="true" title="'Misc'" background-color="transparent" color="black" >
											<md-input-container ng-repeat="prop in modelMiscInfo "  >
												<label>{{prop.label}}</label>
												 <input ng-model="selectedPhysicalModel[prop.name]" disabled>
											</md-input-container>
										
										</expander-box>									
									
										<expander-box layout-margin expanded="true" title="catProp" background-color="transparent" color="black" ng-repeat="catProp in currentPhysicalModelParameterCategories">
											<md-input-container ng-repeat="prop in selectedPhysicalModel.properties | filterByCategory:catProp"
											ng-init="prop.value.value= (prop.value.value==undefined || prop.value.value==null) ? prop.value.propertyType.defaultValue : prop.value.value">
												<label>{{prop.value.propertyType.name}}</label>
												<input ng-model="prop.value.value"  disabled>
											</md-input-container>
										</expander-box>
									</md-content>
								</md-tab>
								
								<md-tab id="inboundTab" label="{{translate.load('sbi.meta.model.business.fk')}}">
								</md-tab>
							</md-tabs>
						</md-content>
					</md-content>
				</md-tab-body>
			</md-tab>
		</md-tabs>

	</md-content>

</body>
</html>

<style>
angular-table#datasourceStructureListTable .centerCheckbox{
	margin-left: 50%;
}
angular-table#datasourceStructureListTable .centerHeadText{
    text-align: center;
}

.goldKey{
color: gold !important;
}

component-tree#bcmTree>div , component-tree#pmTree>div{
margin:20px;
}

md-input-container.md-knowage-theme .md-input[disabled], md-input-container.md-knowage-theme .md-input [disabled]{
	color: black;
}

md-tabs.md-knowage-theme .md-tab.md-active{
    font-weight: 500;
}

</style>