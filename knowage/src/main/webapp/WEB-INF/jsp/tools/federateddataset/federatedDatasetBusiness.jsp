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
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>{{translate.load("sbi.federationdefinition.title");}}</title>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%-- 
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/federateddataset/federatedDataset.js"></script> 
--%>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/federateddataset/federatedDataset.js")%>"></script>
		<!-- 
		<link rel="stylesheet" type="text/css" href="/knowage/themes/federateddataset/css/federateddatasetStyle.css">
		<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/generalStyle.css">
		--> 
<%-- 
		<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css">
--%>
		
		
		<!-- Retrieveing datasets used in creating a federation definition, as well as the whole relationships column -->
		<script> var listaNewEditMode = [];
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
						int counter = 0;
						for(IDataSet ds : datasets){%>
							listaNewEditMode[<%= counter++ %>] = "<%=ds.getLabel() %>";
						<%
						}
					}
					lisOfDSL = listOfDatasetLabels.toString();
					relString = obj.getRelationships();
				}
			}
		%>
		</script>
		
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
		<div ng-controller="federationDefinitionCTRL as ctrl" layout-fill class="contentdemoBasicUsage" id="federationDefinition" layout="column">					
			<md-toolbar class="primaryToolbar" >
				<div class="md-toolbar-tools" layout="row">
					<h2 class="md-flex" >{{::translate.load("sbi.federationdefinition.title");}}</h2>	
				</div>
			</md-toolbar>
	
			<md-content class="mainContainer" flex>
			
				<!-- Wrapping content that will be shown when ctrl.state is true -->
				<div ng-show="ctrl.state" layout="row" flex layout-align="center start">
					
					<!-- Available datasets -->
					  <md-card flex>	
						<md-toolbar class="secondaryToolbar">
							<div class="md-toolbar-tools">
								<h2>{{::translate.load("sbi.federationdefinition.datasets.avaliable");}}</h2>				
							</div>
						</md-toolbar>
					
					
						<div layout="row" layout-align="center center">
							<md-input-container md-no-float flex="80">
						      <md-icon md-font-icon="fa fa-search"></md-icon>
						      <input ng-model="dsSearch" type="text" placeholder="Search" style="margin-left: 32px;"/>
						    </md-input-container>
						</div>
							
						<md-card-content class="noPadding federationDsContainer">
							<md-subheader>Click to add to federation</md-subheader>
							
							<md-list>
							  <md-list-item class="secondary-button-padding" ng-repeat="item in ctrl.list | filter:{'name':dsSearch}" ng-click="ctrl.moveToListNew(item)">
							    <p> {{ item.name }} </p>
							    <md-button class="md-secondary md-icon-button" ng-click="ctrl.showDSDetails(item)">
							    	<md-icon md-font-icon="fa fa-info-circle"></md-icon>
							    </md-button>
							  </md-list-item>
						  </md-list>
						</md-card-content>
					 </md-card>	
				
					<!-- Selected datasets -->
					 <md-card flex>
						<md-toolbar class="secondaryToolbar">
							<div class="md-toolbar-tools">
								<h2>{{::translate.load("sbi.federationdefinition.datasets.selected");}}</h2>				
							</div>
						</md-toolbar>
					
						<md-card-content class="noPadding federationDsContainer">
							<md-subheader class="md-no-sticky">Click to remove from federation</md-subheader>
							<md-list>
							  <md-list-item ng-repeat="item in ctrl.listaNew" ng-click="ctrl.kickOutFromListNew(item)">
							    <p> {{ item.name }} </p>
							  </md-list-item>
						  </md-list>
						</md-card-content>
					</md-card>
				
				</div>
			
				<!-- Wrapping content that will be shown when ctrl.state is false -->
				<div ng-hide="ctrl.state">
					
					<!-- Associations editor -->
				 <md-card>	
					<md-toolbar class="secondaryToolbar">
						<div class="md-toolbar-tools">
							<h2>{{::translate.load("sbi.federationdefinition.associationsEditor");}}</h2>
						</div>
					</md-toolbar>
					
					<md-content	class="associationsBox">
						<!-- Going throuh ctrl.listaNew and making a list of metadata for every single dataset -->
						<div ng-repeat="dataset in ctrl.listaNew track by $index">
							<div class="datasetInAssociationBox">
								<md-card class="fedAssociationsBoxCard">
									<!-- Datset name-->
									<md-toolbar class="ternaryToolbar">
										<div class="md-toolbar-tools">
											<h2 class="truncated">{{dataset.name | uppercase}}</h2>
										</div>
									</md-toolbar>
									
									<md-content class="listBox">
										<angular-list
											layout-fill
											id='{{dataset.label}}'
											ng-model="dataset.metadata.fieldsMeta" 
											item-name="alias"
											highlights-selected-item="isDSCountained(dataset.label)"
											selected-item="ctrl.myselectedvariable[dataset.label]"
											click-function="ctrl.selectDeselect(item, listId)"
											class="noScrol"
											no-pagination="true"									
										>
										</angular-list-->
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
						<span flex=""></span>
						<md-button ng-if="ctrl.showSmartDetection" ng-click="ctrl.autodetect()">{{translate.load("sbi.federationdefinition.autodetect")}}</md-button>
						<md-button class="md-fab associationListBtn" ng-click="ctrl.addSingleRelation()"><md-tooltip md-delay=1500 md-direction="left">{{translate.load("sbi.federationdefinition.add.relationship");}}</md-tooltip><md-icon md-font-icon="fa fa-plus">
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
												 <i class="fa fa-times" ></i>
											</md-button>
										
									</md-list-item>
									
									</div>
									
								</md-list>
													
							</md-content>
							
						</div>
					
					</md-content>
			 </md-card>
			</div>

			</md-content>
		 
		 	<md-actions layout="row">
		      <span flex></span>
		      <md-button ng-if="!ctrl.state" ng-click="ctrl.toggleBack(); ctrl.ispisiSleektovane(); ctrl.clearSelections()" class="md-raised">
		       	{{::translate.load("sbi.federationdefinition.button.back");}}
		      </md-button>
		      <md-button ng-if="ctrl.state" ng-click="ctrl.toggle()" class="md-raised md-primary">
		        {{::translate.load("sbi.federationdefinition.button.nextStep");}}
		      </md-button>
		      <md-button  ng-if="!ctrl.state" ng-click="ctrl.showAdvanced($event)" class="md-raised md-primary">
		        {{::translate.load("sbi.federationdefinition.button.saveFederation");}}
		      </md-button>
		    </md-actions>

		</div>
	
	</body>

</html>
