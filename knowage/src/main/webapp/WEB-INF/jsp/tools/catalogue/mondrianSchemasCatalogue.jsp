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


<%@ page language="java" pageEncoding="UTF-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="mondrianSchemasCatalogueModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">


<%-- 
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/mondrianSchemasCatalogue.js"></script> 
--%>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script>

<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/mondrianSchemasCatalogue.js")%>"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body class="bodyStyle kn-layerCatalogue"
	ng-controller="mondrianSchemasCatalogueController as ctrl">

		<angular-list-detail show-detail="showMe">
	<list label='translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue")' new-function="createMondrianSchema"> 
	
		 	
					<div layout-align="space-around" layout="row" style="height:100%" ng-show="catalogLoadingShow" >
					
						<md-progress-circular 
							
					 	 	class=" md-hue-4"
					 		md-mode="indeterminate" 
					 		md-diameter="70"
					 	
					 	style="height:100%;">
						</md-progress-circular>
					
					</div>
					
					
<!-- /////////////// CATALOGUE TABLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->					
					<angular-table 	flex
									id="catalog" 
									ng-model="itemList"
									columns='[{"label":"NAME","name":"name"},{"label":"DESCRIPTION","name":"description"}]'
									columns-search='["name","description"]'
									show-search-bar=true
									highlights-selected-item=true
									speed-menu-option ="catalogueSpeedOptions"
									click-function = "catalogueClickFunction(item)"
									no-pagination=false
									ng-show="showCatalogs"
									
									> 
					</angular-table> 
			

			

		</list>
<!-- /////////////// RIGHT SIDE     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->		
		<detail label='selectedMondrianSchema.name==undefined? "" : selectedMondrianSchema.name'  save-function="saveMondrianCatalogue"
		cancel-function="cancel"
		disable-save-button="isDisabled()"
		show-save-button="showMe" show-cancel-button="showMe">
			
		
		<md-tabs md-dynamic-height md-border-bottom="">
		<md-tab label='{{translate.load("sbi.generic.details");}}'>
		<md-content>	
		<form name="attributeForm" ng-submit="attributeForm.$valid">
			<md-card>
		    	<md-card-content>					
	      			<div flex="100">
	      				<md-input-container class="small counter"> 
							<label>{{translate.load("sbi.ds.name");}}</label>
							<input 	ng-model="selectedMondrianSchema.name" 
									required 
									name="name"
									ng-maxlength="100"
									ng-disabled = "selectedMondrianSchema.modelLocker" 
									ng-change = "changeApplied()" 
									ng-pattern="regex.extendedAlphanumeric">
							<div ng-messages="attributeForm.name.$error" role="alert" ng-messages-multiple>
								<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
								<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
	  						</div>
						</md-input-container> 
					</div>
					
	      			<div flex="100">				
						<md-input-container class="small counter"> 
							<label>{{translate.load("sbi.ds.description");}}</label>
							<input 	ng-model="selectedMondrianSchema.description" 
									ng-maxlength="500"
									name = "description" 
									ng-disabled = "selectedMondrianSchema.modelLocker"
									ng-change = "changeApplied()"
									ng-pattern="regex.extendedAlphanumeric"> 
							<div ng-messages="attributeForm.description.$error" role="alert" ng-messages-multiple>
								<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
								<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 500</div>
	  						</div>
						</md-input-container>
					</div>
	
					<div layout="row" layout-wrap  layout-align="start center">
						<label  flex class="buttonLabel">{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.fileUpload")}}:</label>
	      				<file-upload  flex ng-model="file" id="myId" ng-disabled = "selectedMondrianSchema.modelLocker" ng-change = "changeApplied()" flex></file-upload>
					</div>				
	      		</md-card-content>
		    </md-card>	
	      				
	     	<md-card>
	     	<!-- /////////////// SAVED VERSION TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	
					
					<md-toolbar class="secondaryToolbar">
			      <div class="md-toolbar-tools">
			        <h2>
			          <span>{{translate.load("sbi.widgets.catalogueversionsgridpanel.title")}}</span>
			        </h2>
			   
			      </div>
			    </md-toolbar>
		     <md-card-content>					
						 		
	     					
		
	
					
	<!-- /////////////// SCROLL FOR SAVED FILES TABLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						
				
				
				
				
					<div layout-align="space-around" layout="row" style="height:100%" ng-show="versionLoadingShow" >
						
							<md-progress-circular 
								
						 	 	class=" md-hue-4"
						 		md-mode="indeterminate" 
						 		md-diameter="70"
						 	
						 		style="height:100%;">
							</md-progress-circular>
						
						</div>
				
	<!-- /////////////// SAVED FILES TABLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	
						<md-radio-group ng-model="selectedMondrianSchema.currentContentId" >
							<angular-table 	
							                layout-fill
							                
											id="versions" 
											ng-model="fileList"
											no-pagination=false
											
											columns='[
											{"label":"ACTIVE","name":"actives","size":"50px"},
											{"label":"FILE NAME","name":"fileName"},
											{"label":"CREATOR","name":"creationUser"},
											{"label":"CREATION DATE","name":"creationDate"},
											
											
											]'
											highlights-selected-item=true
											show-search-bar=true
											speed-menu-option ="versionsSpeedOptions"
											click-function = "versionClickFunction(item)"
											ng-show="showVersions"
											> 
											
							</angular-table>
				
				
					</md-radio-group>
				
					
					
				</md-card-content>
		      </md-card>

			</form>
			</md-content>
	     	</md-tab>
	     		<md-tab ng-if="!disableWorkFlow" label='{{translate.load("sbi.generic.workflow");}}'>
	     		
	     			<md-content layout="row">
	     				<md-card flex>
	     					<md-toolbar class="md-hue-2">
		     					<div class="md-toolbar-tools">
									<h2 class="md-flex" >Available users</h2>
									<span flex=""></span>					
								</div>
	     					</md-toolbar>
	     					
	     					<md-content  layout-padding>
								<angular-table
									layout-fill
									id="availableUsers" 
									ng-model="availableUsersList" 
									columns='[
										{"label":"Username","name":"userId"},
										{"label":"Name","name":"fullName"}
									]'
									show-search-bar=true
									click-function="moveToWorkflow(item,true)"
									style="overflow:hidden"	
								>					
								</angular-table>
							</md-content>
	     				</md-card>

	     				<md-card flex>
	     					<md-toolbar class="md-hue-2">
	     						<div class="md-toolbar-tools" layout="row">
										<h2 class="md-flex">Users work flow</h2>
	     							<div flex></div>
										<md-button class="md-icon-button mondrian-start-btn"  ng-show="isStartVisible()" ng-click="startWorkflow(selectedMondrianSchema.id)">
										<md-tooltip md-direction="bottom">
								          Start workflow
								        </md-tooltip>
          								<md-icon md-font-icon="fa-play-circle" class="fa fa-2x"></md-icon>
        							</md-button>										
								</div>
								
	     					</md-toolbar>
	     					
	     					<md-content  layout-padding>
								<angular-table
								layout-fill
								id="selectedUsers" 
								ng-model="wfSelectedUserList" 
								columns='[
									{"label":"Username","name":"userId"},
									{"label":"Name","name":"fullName"}
								]'
								show-search-bar=true
								speed-menu-option ="!isStartedWf ? workflowSpeedMenu : workflowSpeedMenuSt"
								style="overflow:hidden"	
								>					
								</angular-table>
							</md-content>
	     				</md-card>
	     			</md-contet>
	     		</md-tab>
			</md-tabs>
			</detail>	
	</angular-list-detail>

</body>
</html>
