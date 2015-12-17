<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
 
<%
boolean canSeeMasterHier=false, canSeeTechnicalHier=false,canSeeAdmin=false;
if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.HIERARCHIES_MANAGEMENT})){
 canSeeMasterHier=true;
 canSeeTechnicalHier=true;
 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.HIERARCHIES_MANAGEMENT});
} 
%>
 
<html ng-app="hierManager" >
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierarchiesController.js"></script>
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierMaster/hierTableController.js"></script>
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/hierarchies/hierMaster/hierTreeController.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/commons/css/generalStyle.css">
 
<title>HierarchiesEditor</title>

</head>

<body class="hierBodyStyle">

	<div ng-cloak>
		 <md-content>
		    <md-tabs md-dynamic-height md-border-bottom>
		      <md-tab label="MASTER" ng-if="<%=canSeeMasterHier%>==true">
		      	  <angular-2-col id="myId">  
 					<left-col width="50%">	
					    <!-- <div ng-controller="hierTableController"  ng-init="initTable(data.columns);" layout="column"   style="padding:0 15px"> -->
					    <div ng-controller="hierTableController"  ng-init="loadTableDimension=false;" layout="column"   style="padding:0 15px">
					       <md-input-container>
					      	<div layout="row" layout-align="end center">
					      		<div flex="100" style="height:10%">
							        <label style="vertical-align:middle;">Dimensione</label>
							        <md-select  ng-model="dimName" md-on-open="getDimensions()" required>
							          <md-option ng-click="setDimension(dim)" ng-repeat="dim in dimensions track by dim.DIMENSION_NM" value="{{$index}}">
							            {{dim.DIMENSION_NM}}
							          </md-option>
							        </md-select>
							     </div>
					        </div>
					       </md-input-container> 
					       <div layout="row"  layout-align="space-between center"  style="padding:0 15px" >
							       <label>Data</label>
							        <md-datepicker ng-model="selDate" md-placeholder="Data riferimento" required></md-datepicker>
							        <md-button ng-click="loadDimensionTable($event)" md-no-ink class="md-raised" >Carica dati</md-button>	
		
					       </div> 		 	
					       <div  layout="row" layout-align="start center" style="padding:0 15px">	     
						       <div layout="row"  md-no-float  ng-init="showFilter=false;showPostData=false;"> 		      		
								     <md-icon md-font-icon="fa fa-filter" ng-click="showFilter=true;"></md-icon> 									 
									 <md-checkbox class="tableSearchBar" md-no-ink ng-if="showFilter" ng-model="filter4Hier" ng-change="filterData(filter4Hier,'hier')">
									   		Foglie non presenti in gerarchia
									 </md-checkbox>
									 <md-checkbox class="tableSearchBar" md-no-ink    ng-if="showFilter" ng-model="filter4Data" >Data</md-checkbox>
									 <md-datepicker ng-model="filterDate" md-placeholder="" ng-if="showFilter"  ></md-datepicker>
									 <md-icon ng-show="showFilter" md-font-icon="fa fa-trash" ng-click="showFilter=false;showPostData=false;filterVal='';searchFilter(searchVal);" 
									 		  class="closeSearchBar" style="padding-left:30px;"></md-icon>
					        	</div> 
				        	</div>
				        	<div style="height:60%;position:relative">
					      		 <angular-table  ng-if="loadTableDimension"
							        id='dimTable'
							        ng-model=data.root 
			 					    columns=visibleMetadata
			 					    columns-search=data.columns_search	
			 					    show-search-bar="true"		
			 					    highlights-selected-item="true"											
			 					    click-function="showDetails(row,data.columns,listId)"					 					   						 								 					   
								>
								</angular-table>
								
							</div> 	  	
							<div layout="row"  layout-align="end center" >
								<md-button ng-disabled="true" class="md-raised" md-no-ink>Crea Gerarchia Automatica</md-button>
							</div>	
						</div>												    
					</left-col>
 					<right-col>
	   			 		<div ng-controller="hierTreeController" >	
	   			 		<md-input-container>
					      	<div layout="row" layout-align="end center">
					      		<div flex="100" style="height:10%">
							        <label style="vertical-align:middle;">Tipo Gerarchia</label>
							        <md-select  ng-model="typeHierarchy" required>
							          <md-option ng-repeat="hierType in hierTypes.root track by hierType.idx" value="{{hierType.type}}">
							            {{hierType.type}}
							          </md-option>
							        </md-select>
							     </div>
					        </div>
					       </md-input-container> 
					       <md-input-container>
					      	<div layout="row" layout-align="end center">
					      		<div flex="100" style="height:10%">
							        <label style="vertical-align:middle;">Gerarchia</label>
							        <md-select  ng-model="hierName" md-on-open="getHierarchies(typeHierarchy, gDimensionName)"  required>
							          <!-- <md-option ng-repeat="hier in hierarchies track by hier.HIERARCHY_CD" value="{{hier.HIERARCHY_CD}}">  -->
							          <md-option ng-repeat="hier in hierarchies track by hier.HIERARCHY_NM" value="{{hier.HIERARCHY_NM}}">
							            {{hier.HIERARCHY_NM}}
							          </md-option>
							        </md-select>
							        <!-- <div style="position:absolute;width:100%;height:100%" ng-show="showPreloader"> -->
							        <div class="preloader" ng-init="showPreloader=false;" ng-show="showPreloader()">
										<md-progress-circular md-mode="indeterminate"></md-progress-circular>
									</div>
							     </div>
					        </div>
					       </md-input-container> 
					       <div layout="row"  layout-align="space-between center"  style="padding:0 15px" >
							       <label>Data</label>
							        <md-datepicker ng-model="selDateHier" md-placeholder="Data riferimento" required></md-datepicker>
							        <md-button md-no-ink class="md-raised" ng-click="loadTree(hierName)">Carica dati</md-button>	
							        <md-button md-no-ink class="md-raised" ng-click="saveHierMaster()">Salva</md-button>	
					       </div> 
					        <div  layout="row" layout-align="start center" style="padding:0 15px">	     
						       <div layout="row"  md-no-float  ng-init="showFilter=false;showPostData=false;"> 		      		
								     <md-icon md-font-icon="fa fa-filter" ng-click="showFilter=true;"></md-icon> 									 
									 <md-checkbox class="tableSearchBar" md-no-ink ng-if="showFilter" ng-model="filter4Dim" ng-change="filterData(filter4Dim,'hier')">
									   		Foglie non presenti nella dimensione
									 </md-checkbox>
									 <md-checkbox class="tableSearchBar" md-no-ink    ng-if="showFilter" ng-model="filter4Data" >Data</md-checkbox>
									 <md-datepicker ng-model="filterDate" md-placeholder="" ng-if="showFilter"  ></md-datepicker>
									 <md-icon ng-show="showFilter" md-font-icon="fa fa-trash" ng-click="showFilter=false;showPostData=false;filterVal='';searchFilter(searchVal);" 
									 		  class="closeSearchBar" style="padding-left:30px;"></md-icon>
					        	</div> 
				        	</div>
							<div  id="docTree" ui-tree="" data-drag-enabled="false" data-drag-delay="false" data-empty-placeholder-enabled="false">								
								<script type="text/ng-template" id="hierTreeNodeTemplate">		
								<div  ui-tree-handle layout="row">			
									<div									
										context-menu data-target="hierNode-{{elementToIterate.name}}" 
										 class="panel panel-default position-fixed" 
										 ng-class="{ 'highlight': highlight, 'expanded' : expanded }">
											<span style="padding-left:10;" ng-if="!collapsed" class="fa fa-folder-open-o"></span>
											<span style="padding-left:10;" ng-if="collapsed" class="fa fa-folder-o"></span>
											<span ng-if="elementToIterate.leafId==''" style="padding-left:5;font-weight:500;font-size: small;" ng-click="toggleNode(this);" >{{elementToIterate.name}}</span>
											<span ng-if="(elementToIterate.leafId!=undefined && elementToIterate.leafId!='')" style="padding-left:5;font-size: small;" ng-click="toggleNode(this);" >{{elementToIterate.name}}</span>
										</div> 

										<div class="dropdown position-fixed" style="z-index: 999; width: 150px;"
													id="hierNode-{{elementToIterate.name}}"
 													ng-class="{ 'highlight': highlight, 'expanded' : expanded }">
												<md-list class="dropdown-menu bottomBorder" role="menu"> 
													<md-list-item  ng-if="elementToIterate.leafId==''"
															ng-click='addNode(elementToIterate)' role="menuitem"
															tabindex="1">
														<p style="height:30px !important;">Aggiungi</p>
													</md-list-item> 
													<md-list-item 
															ng-click='modifyNode(elementToIterate)'
															role="menuitem" tabindex="2">
														<p style="height:30px !important;">Modifica</p>
													</md-list-item> 											
													<md-list-item 
															ng-click='deleteNode(elementToIterate)' role="menuitem"
															tabindex="3">
														<p style="height:30px !important;">Cancella</p>
													</md-list-item> 
													<md-list-item  ng-if="elementToIterate.leafId!=''"
															ng-click='copyLeaf(elementToIterate)' role="menuitem"
															tabindex="4" >
														<p style="height:30px !important;">Duplica foglia</p>
													</md-list-item> 
												</md-list>
											</div>							
										</div>	<!--fine menu contestuale albero -->

		
										<ol ng-if="!collapsed" ui-tree-nodes ng-model="elementToIterate" ng-if="elementToIterate.children">
											<li ng-repeat="elementToIterate in elementToIterate.children" 
													ui-tree-node class="figlioVisibile" 
													ng-include="'hierTreeNodeTemplate'" class="indicator-child"
											></li>
										</ol>
								</script>
								
								<ol  id="root" ui-tree-nodes ng-model="data.root" ng-class="{hideChildren: collapsed}">								
									 <p style="font-weight: bold;padding-left:10px;">{{data.hierName | uppercase}}</p>  																			
									<li data-collapsed="true" ng-repeat="elementToIterate in data.root" ui-tree-node 
											ng-include="'hierTreeNodeTemplate'" class="noBorder"></li>
								</ol>
								
							</div>
					</right-col>
			    </angular-2-col>  
		      </md-tab>
		      <md-tab label="TECHNICAL" ng-if="<%=canSeeTechnicalHier%>==true">
		        <md-content class="md-padding">
		         	<md-whiteframe class="md-whiteframe-3dp" layout layout-align="center center"  >
	   			 		<div layout="column" layout-wrap> 
						    <div layout="row" layout-wrap >
						    <div class="colorbox" style="background-color: {{item.color}}"></div>
						    	<span>	WORK IN PROGRESS ..</span>	    
						    </div>			
						 </div>
					</md-whiteframe>
		        </md-content>
		      </md-tab>		      
		    </md-tabs>
	  	</md-content>	
	</div>
 
</body>

</html>