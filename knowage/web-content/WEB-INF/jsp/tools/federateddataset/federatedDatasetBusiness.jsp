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


<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="it.eng.spagobi.tools.dataset.bo.IDataSet"%>
<%@page import="java.util.Set"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.tools.dataset.federation.FederationDefinition"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>{{translate.load("sbi.federationdefinition.title");}}</title>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/federateddataset/federatedDataset.js"></script>
		<!-- 
		<link rel="stylesheet" type="text/css" href="/knowage/themes/federateddataset/css/federateddatasetStyle.css">
		<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/generalStyle.css">
		--> 
		<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css">
		
		<!-- Retrieveing datasets used in creating a federation definition, as well as the whole relationships column -->
		<%
		 	String contextName = ChannelUtilities.getSpagoBIContextName(request);
			String relString = "";
			String lisOfDSL = "";
			String federationID = "";
			if(request.getParameter("id")!=null){
				federationID = request.getParameter("id");
				if(federationID!=null && federationID.length()!=0){
					FederationDefinition obj = DAOFactory.getFedetatedDatasetDAO().loadFederationDefinition(new Integer(federationID));
					Set<IDataSet> datasets = obj.getSourceDatasets();
					List<String> listOfDatasetLabels = new ArrayList<String>();
					if(datasets!=null){
						for(IDataSet ds : datasets){
							listOfDatasetLabels.add(ds.getLabel());
						}
					}
					lisOfDSL = listOfDatasetLabels.toString();
					relString = obj.getRelationships();
				}
			}
		%>
		
		<!-- Making lisOfDSL and relString avaliable for use in federatedDataset.js -->
		<script>
			var value = '<%= lisOfDSL %>';
			var valueRelString = '<%= relString  %>';
			var contextName = '<%=contextName %>';
			var federation_id = '<%=federationID %>';
		</script> 
	</head>

	<body class="federatedDataset" ng-app="federationDefinitionModule" id="ng-app">
		
		<!-- Binding the Angular controller FederationDefinitionCTRL from the module FEDERATIONDEFINITION to the div -->
		<div ng-controller="federationDefinitionCTRL as ctrl" layout-fill class="contentdemoBasicUsage">		
		  <div class ="md-container" >
			<md-toolbar class="miniheadfederation" >
				<div class="md-toolbar-tools">
					<i class="fa fa-connectdevelop fa-2x"></i>
					<h2 class="md-flex" >{{translate.load("sbi.federationdefinition.title");}}</h2>
				</div>
			</md-toolbar>
	
			<md-content layout-padding class="mainContainer" >
			
				<!-- Wrapping content that will be shown when ctrl.state is true -->
				<div ng-show="ctrl.state" layout="row" >
					
					<!-- Avaliable datasets -->
					<div flex class="datasetBox">
					  <md-card>	
						<md-toolbar class="miniheadfedsmall"  >
							<div class="md-toolbar-tools">
								<i class="fa fa-bars "></i>
								<h2 class="md-flex" >{{translate.load("sbi.federationdefinition.datasets.avaliable");}}</h2>
								<span flex=""></span>					
							</div>
						</md-toolbar>
					
						<md-content  layout-padding>
							<angular-list
							layout-fill 
							id="availableDatasets" 
							ng-model="ctrl.list" 
							item-name="name"
							show-search-bar=true
							speed-menu-option=ctrl.showDatasetInfo
							click-function="ctrl.moveToListNew(item)"
							style="overflow:hidden"	
							>					
							</angular-list>
						</md-content>
					 </md-card>	
					</div>
				
					<!-- Selected datasets -->
					<div  flex class="datasetBox">
					 <md-card>
						<md-toolbar class="miniheadfedsmall"  >
							<div class="md-toolbar-tools">
								<i class="fa fa-bars"></i>
								<h2 class="md-flex" >{{translate.load("sbi.federationdefinition.datasets.selected");}}</h2>
								<span flex=""></span>					
							</div>
						</md-toolbar>
					
						<md-content layout-padding >
							<angular-list layout-fill 
							id="selectedDatasets" 
							ng-model="ctrl.listaNew" 
							item-name="name" 
							speed-menu-option=ctrl.removeDatasetFromListaNew
							>					
							</angular-list>
						</md-content>
					</md-card>
				</div>
				
				</div>
			
				<!-- Wrapping content that will be shown when ctrl.state is false -->
				<div ng-hide="ctrl.state">
					
					<!-- Associations editor -->
				 <md-card>	
					<md-toolbar >
						<div class="md-toolbar-tools">
							<h2 class="md-flex">{{translate.load("sbi.federationdefinition.associationsEditor");}}</h2>
							<span flex=""></span>
						</div>
					</md-toolbar>
					
					<md-content	class="associationsBox">
						<!-- Going throuh ctrl.listaNew and making a list of metadata for every single dataset -->
						<div ng-repeat="dataset in ctrl.listaNew track by $index">
							<div class="datasetInAssociationBox">
								<md-card class="fedAssociationsBoxCard">
									<!-- Datset name-->
									<md-toolbar class="miniheadfedsmall">
									<div class="md-toolbar-tools">
										<md-tooltip  md-direction="top">{{dataset.name | uppercase}}</md-tooltip>
										<h2 class="md-flex fedAssociationsBoxEllipsis">{{dataset.name | uppercase}}</h2>
										
									</div>
									</md-toolbar>
									
									<md-content ng-show="true" class="listBox" layout="column">
										<angular-list
											layout-fill
											id='{{dataset.label}}'
											ng-model="dataset.metadata.fieldsMeta" 
											item-name="alias"
											highlights-selected-item="isDSCountained(dataset.label)"
											selected-item="ctrl.myselectedvariable[dataset.label]"
											click-function="ctrl.selectDeselect(item, listId)"
											class="noScrol"									
										>
										</angular-list>
									</md-content>
								</md-card>
							</div>
						</div>
					</md-content>
				 </md-card>	
				</div>
				
			   	
				<div ng-hide="ctrl.state">
				 <md-card>
					<md-toolbar >
					<div class="md-toolbar-tools">
						<h2 class="md-flex">{{translate.load("sbi.federationdefinition.associationsList");}}</h2>
						<span flex=""></span><md-button class="md-fab md-ExtraMini createRelationButton" ng-click="ctrl.addSingleRelation()"><md-tooltip md-delay=1500 md-direction="left">{{translate.load("sbi.federationdefinition.add.relationship");}}</md-tooltip><md-icon md-font-icon="fa fa-plus">
						</md-icon></md-button> 
					</div>
		
		
					</md-toolbar>
					<md-content class="associationsBox">
		
						<div>
							
							<md-content >
							
								<md-list >
									<div ng-repeat="k in multiRelationships">
										
										<md-list-item class="associationItem">
											<div ng-style="myStyle" ng-click="ctrl.retrieveSelectionsString(k)">
											<span>
												<span>
												{{k}}
											</span>
											
											</div>
											<span flex=""></span>
											 
											 		<md-button aria-label="trash" class="md-fab md-ExtraMini trashcan-background deleteIcon" ng-click="ctrl.deleteRelationship(k)">
														 <i class="fa fa-times-circle" ></i>
													</md-button>
										
									</md-list-item>
									
									</div>
									
								</md-list>
													
							</md-content>
							
						</div>
					
					</md-content>
			 </md-card>
			</div>

			
			
				<div ng-show="ctrl.state">
					<md-button class="md-raised buttonR" aria-label="btn_next_step"
						ng-click="ctrl.toggle();">{{translate.load("sbi.federationdefinition.button.nextStep");}}
					</md-button>
				</div>
			
				<div ng-hide="ctrl.state">
					<md-button  class="md-raised buttonL" aria-label="btn_back_to_first_page" ng-click="ctrl.toggleBack(); ctrl.ispisiSleektovane(); ctrl.clearSelections()">{{translate.load("sbi.federationdefinition.button.back");}}</md-button> 
					<md-button  class="md-raised buttonR" aria-label="btn_save_federation" ng-click="ctrl.showAdvanced($event)">{{translate.load("sbi.federationdefinition.button.saveFederation");}}</md-button>	
				</div>
			</md-content>
		 <!-- end div container -->
		 </div>	
		</div>
	
	</body>

</html>
