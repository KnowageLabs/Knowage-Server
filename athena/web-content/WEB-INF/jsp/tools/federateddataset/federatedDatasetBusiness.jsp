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
		<title>{{sbiModule_translate.load("sbi.federationdefinition.title");}}</title>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/federateddataset/federatedDataset.js"></script>
		<link rel="stylesheet" type="text/css" href="/athena/themes/federateddataset/css/federateddatasetStyle.css">
		<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">

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

	<body class="bodyStyle" ng-app="FEDERATIONDEFINITION" id="ng-app">
		
		<!-- Binding the Angular controller FederationDefinitionCTRL from the module FEDERATIONDEFINITION to the div -->
		<div ng-controller="FederationDefinitionCTRL as ctrl" layout="column" style="width: 100%; height: 100%;" class="contentdemoBasicUsage" >		
		
			<md-toolbar class="miniheadfederation" style="height:4%">
				<div class="md-toolbar-tools">
					<i class="fa fa-bar-chart fa-2x"></i>
					<h2 class="md-flex" style="padding-left: 14px">{{translate.load("sbi.federationdefinition.title");}}</h2>
				</div>
			</md-toolbar>
	
			<md-content layout-padding style="height: 96%; padding: 20px;">
			
				<!-- Wrapping content that will be shown when ctrl.state is true -->
				<div ng-show="ctrl.state" layout="row" layout-sm="column" layout-wrap">
					
					<!-- Avaliable datasets -->
					<div  flex flex-sm="100" style="margin-right: 20px;">
					
						<md-toolbar class="miniheadfedsmall" style="border-bottom: 2px solid grey;" >
							<div class="md-toolbar-tools">
								<i class="fa fa-list-alt fa-2x"></i>
								<h2 class="md-flex" style="padding-left: 14px">{{translate.load("sbi.federationdefinition.datasets.avaliable");}}</h2>
								<span flex=""></span>					
							</div>
						</md-toolbar>
					
						<md-content  layout-padding style="height:80%">
							<angular-list
							layout-fill 
							id="availableDatasets" 
							ng-model="ctrl.list" 
							item-name="label"
							show-search-bar=true
							speed-menu-option=ctrl.glossSpeedMenuOptAD
							click-function="ctrl.moveToListNew(item)"
							>					
							</angular-list>
						</md-content>
						
					</div>
				
					<!-- Selected datasets -->
					<div  flex flex-sm="100">
				
					<md-toolbar class="miniheadfedsmall" style="border-bottom: 2px solid grey;" >
						<div class="md-toolbar-tools">
							<i class="fa fa-list-alt fa-2x"></i>
							<h2 class="md-flex" style="padding-left: 14px">{{translate.load("sbi.federationdefinition.datasets.selected");}}</h2>
							<span flex=""></span>					
						</div>
					</md-toolbar>
				
					<md-content layout-padding style="height:80%" >
						<angular-list  
						id="selectedDatasets" 
						ng-model="ctrl.listaNew" 
						item-name="label" 
						speed-menu-option=ctrl.glossSpeedMenuOpt
						>					
						</angular-list>
					</md-content>
					
				</div>
				
				</div>
			
				<!-- Wrapping content that will be shown when ctrl.state is false -->
				<div ng-hide="ctrl.state">
					
					<!-- Associations editor -->
					<md-toolbar class="miniheadfedsmall" style="">
						<div class="md-toolbar-tools">
							<h2 class="md-flex">{{translate.load("sbi.federationdefinition.associationsEditor");}}</h2>
							<span flex=""></span>
						</div>
					</md-toolbar>
					
					<md-content	style=" padding: 5px;  height:41%">
						<!-- Going throuh ctrl.listaNew and making a list of metadata for every single dataset -->
						<div ng-repeat="dataset in ctrl.listaNew track by $index">
							<div style="width: 250px; float: left; padding: 5px;">
								<!-- Datset name-->
								<md-toolbar class="miniheadfedsmall"
									style="">
								<div class="md-toolbar-tools">
									<h2 class="md-flex">{{dataset.label | uppercase}}</h2>
								</div>
								</md-toolbar>
								<div  style=" height:85%;">
								<div ng-show="true" layout-padding>
									<angular-list
										layout-fill 
										id='{{dataset.label}}'
										ng-model="dataset.metadata.fieldsMeta" 
										item-name="name"
										highlights-selected-item=true
										selected-item="ctrl.myselectedvariable[dataset.label]"
										click-function="ctrl.selektuj(item, listId)"
										
									>
									</angular-list>
									
								</div>
								</div>
							</div>
						</div>
					</md-content>
				</div>
		
				<div ng-hide="ctrl.state" style="padding-top: 5px">
					<md-toolbar class="miniheadfedsmall"
						style="">
					<div class="md-toolbar-tools">
						<h2 class="md-flex">{{translate.load("sbi.federationdefinition.associationsList");}}</h2>
						<span flex=""></span><md-button class="md-fab md-ExtraMini createRelationButton" ng-click="ctrl.fillTheArray()"><md-tooltip md-delay=1500 md-direction="left">{{sbiModule_translate.load("sbi.federationdefinition.add.relationship");}}</md-tooltip><md-icon md-font-icon="fa fa-plus" style="position:absolute; left:5px; top:5px; right:5px; color:white"
							></md-icon></md-button> 
					</div>
		
		
					</md-toolbar>
					<md-content style=" height:40%">
		
						<div>
							
							<md-content >
							
								<md-list >
									<div ng-repeat="k in ctrl.multiArray track by $index">
										
										<md-list-item style="min-height:35px">
											
											<div ng-style="myStyle"  ng-click="ctrl.retrieveSelections(k)" ng-repeat="bla in k track by $index">
											<span>
												<span ng-if="$index==0">
												{{bla.sourceTable.name | uppercase }}.{{bla.sourceColumns[0]}}</span>={{bla.destinationTable.name | uppercase }}.{{bla.destinationColumns[0]}}
											</span>
											
											</div>
											<span flex=""></span>
											 
											 		<md-button aria-label="trash" class="md-fab md-ExtraMini trashcan-background">
														 <i class="fa fa-trash" ng-click="ctrl.deleteFromMultiArray(k)"></i>
													</md-button>
										
									</md-list-item>
									
									</div>
									
								</md-list>
													
							</md-content>
							
						</div>
					
					</md-content>
			</div>
			
				<div ng-show="ctrl.state">
					<md-button class="md-raised buttonR" aria-label="btn_next_step"
						style=" margin-top: 20px; float:right;" ng-click="ctrl.toggle();">{{translate.load("sbi.federationdefinition.button.nextStep");}}
					</md-button>
				</div>
			
				<div ng-hide="ctrl.state">
					<md-button  class="md-raised buttonL" aria-label="btn_back_to_first_page" ng-click="ctrl.toggleBack(); ctrl.ispisiSleektovane(); ctrl.clearSelections()">{{translate.load("sbi.federationdefinition.button.back");}}</md-button> 
					<md-button  class="md-raised buttonR" aria-label="btn_save_federation" ng-click="ctrl.showAdvanced($event)">{{translate.load("sbi.federationdefinition.button.saveFederation");}}</md-button>	
				</div>
			</md-content>
		</div>
	
	</body>

</html>
