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
			String relString = "";
			String lisOfDSL = "";
			if(request.getParameter("id")!=null){
				String federationID = request.getParameter("id");
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
							speed-menu-option=ctrl.fdsSpeedMenuOptAD
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
							speed-menu-option=ctrl.fdsSpeedMenuOpt
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
								<md-card style="height:93%">
									<!-- Datset name-->
									<md-toolbar class="miniheadfedsmall">
									<div class="md-toolbar-tools">
										<h2 class="md-flex">{{dataset.name | uppercase}}</h2>
									</div>
									</md-toolbar>
									
									<md-content ng-show="true" class="listBox" layout="column">
										
										<angular-list
											layout-fill
											id='{{dataset.label}}'
											ng-model="dataset.metadata.fieldsMeta" 
											item-name="name"
											highlights-selected-item="isDSCountained(dataset.name)"
											selected-item="ctrl.myselectedvariable[dataset.name]"
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
						<span flex=""></span><md-button class="md-fab md-ExtraMini createRelationButton" ng-click="ctrl.fillTheArray()"><md-tooltip md-delay=1500 md-direction="left">{{translate.load("sbi.federationdefinition.add.relationship");}}</md-tooltip><md-icon md-font-icon="fa fa-plus">
						</md-icon></md-button> 
					</div>
		
		
					</md-toolbar>
					<md-content class="associationsBox">
		
						<div>
							
							<md-content >
							
								<md-list >
									<div ng-repeat="k in ctrl.multiArray track by $index">
										
										<md-list-item class="associationItem">
											
											<div ng-style="myStyle"  ng-click="ctrl.retrieveSelections(k)" ng-repeat="bla in k track by $index">
											<span>
												<span ng-if="$index==0">
												{{bla.sourceTable.name | uppercase }}.{{bla.sourceColumns[0]}}</span>&nbsp; &#10140; &nbsp;{{bla.destinationTable.name | uppercase }}.{{bla.destinationColumns[0]}}
											</span>
											
											</div>
											<span flex=""></span>
											 
											 		<md-button aria-label="trash" class="md-fab md-ExtraMini trashcan-background deleteIcon" ng-click="ctrl.deleteFromMultiArray(k)">
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
