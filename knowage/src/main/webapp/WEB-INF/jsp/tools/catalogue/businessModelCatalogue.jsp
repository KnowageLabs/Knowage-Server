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


<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.tools.dataset.federation.FederationDefinition"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<html ng-app="businessModelCatalogueModule">
	<head>
	
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		
		<%-- <%@include file="/WEB-INF/jsp/analiticalmodel/document/documentDetailsImport.jsp"%> --%>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/resourceService.js")%>"></script>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/modules/driversModule.js")%>"></script>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/drivers.js")%>"></script>
		
		<%-- <script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/businessModelCatalogue.js"></script> --%>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/businessModelCatalogue.js")%>"></script>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/generateDatamartOptionsController.js")%>"></script>
	
	
	
			<!-- Retrieveing datasets used in creating a federation definition, as well as the whole relationships column -->
			<%
				String user = "";
				if(userName!=null){
					user = userName;
				}
			%>
			
			<!-- check if use is admin or tecnical user -->
			
			<%
			boolean isAdmin=UserUtilities.isAdministrator(userProfile);
			boolean isTec=UserUtilities.isTechnicalUser(userProfile);
			
			%>
			<!-- Making lisOfDSL and relString avaliable for use in federatedDataset.js -->
			<script>
				var valueUser = '<%= user  %>';
			</script>
	
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Business Model Catalogue</title>
	</head>
	<body  class="bodyStyle businessModelCatalog" ng-controller="businessModelCatalogueController as ctrl">
		<rest-loading></rest-loading>
		<angular-list-detail show-detail="showMe">
			<list label='translate.load("sbi.tools.catalogue.metaModelsCatalogue")' new-function="createBusinessModel"> 
	
						<angular-table 
							flex
							id="businessModelList_id"
							ng-show="!bmLoadingShow"
							ng-model="businessModelList"
							columns='[{"label":"Name","name":"name"},{"label":"Description","name":"description"}]' 
							columns-search='["name","description"]'
							show-search-bar=true
							highlights-selected-item=true
							click-function ="leftTableClick(item)"
							selected-item="selectedBusinessModels"
							speed-menu-option="bmSpeedMenu"					
						></angular-table>
	
			</list>
			
			<detail label='selectedBusinessModel.name==undefined? "" : selectedBusinessModel.name'  
					save-function="saveBusinessModel"
					cancel-function="cancel"
					disable-save-button="!businessModelForm.$valid"
					show-save-button="showMe" show-cancel-button="showMe">
			   <form name="businessModelForm">
			
				<md-tabs md-border-bottom="" md-dynamic-height>				     	         			
			  		<md-tab label='{{translate.load("sbi.generic.details");}}'>
		          		<md-card>
			        		<md-card-content layout="column">
				        		<md-input-container class="md-block" >
									<label>{{translate.load("sbi.ds.name")}}</label>
									<input ng-disabled="isEdit(selectedBusinessModel)"  ng-model="selectedBusinessModel.name" name="name" required ng-maxlength="100" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="businessModelForm.name.$error" role="alert" ng-messages-multiple>
										<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
										<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
		  							</div>
								</md-input-container>
						
								<md-input-container class="md-block">
									<label>{{translate.load("sbi.ds.description")}}</label>
									<input ng-model="selectedBusinessModel.description"	ng-maxlength="500" name="description" ng-pattern="regex.extendedAlphanumeric">
									<div ng-messages="businessModelForm.description.$error" role="alert" ng-messages-multiple>
										<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
										<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
		  							</div>
								</md-input-container>
						
								<md-input-container class="md-block"> 
									<label>{{translate.load("sbi.ds.catType")}}</label>
				   					<md-select  aria-label="aria-label" required name="cat" ng-model="selectedBusinessModel.category" >
					    				<md-option  ng-repeat="c in listOfCategories" value="{{c.VALUE_ID}}">{{c.VALUE_NM}} </md-option>
					   				</md-select> 
								</md-input-container>
						
								<md-input-container class="md-block"> 
									<label>{{translate.load("sbi.ds.dataSource")}}</label>
							       	<md-select  aria-label="aria-label" required name="ds" ng-model="selectedBusinessModel.dataSourceLabel"> 
								        <md-option ng-repeat="d in listOfDatasources" value="{{d.label}}">{{d.label}} </md-option>
							       	</md-select>     
								</md-input-container>

								<div layout="row" layout-wrap layout-align="start center">
									<div ng-if="!metaWebFunctionality" flex layout="row" layout-align="start center">
										<label class="buttonLabel">{{translate.load("sbi.ds.file.upload.button")}}:</label>
					      				<file-upload flex ng-model="fileObj" id="businessModelFile" flex></file-upload>
				      				</div>
				      				<% if(isAdmin || isTec){ %>
				      				<div ng-if="metaWebFunctionality" flex layout="row" layout-align="start center">
					      				<md-button ng-if="metaWebFunctionality" class="md-raised" aria-label="Profile" ng-click="createBusinessModels()" ng-disabled="selectedBusinessModel.dataSourceLabel==undefined">
											{{translate.load("sbi.bm.metaweb")}}
										</md-button>
										<md-button ng-if="metaWebFunctionality && togenerate" class="md-raised" aria-label="Profile" ng-click="openGenerateDatamartDialog()" ng-disabled="selectedBusinessModel.dataSourceLabel==undefined">
											{{translate.load("sbi.bm.generate")}}
										</md-button>
									</div>
									<%} %>
									
									<div flex layout="row" layout-wrap layout-align="start center">
										<% if(isAdmin || isTec){ %>
										<md-input-container ng-show="selectedBusinessModel.id!=undefined" flex="50" class="noMargin">
								          <md-switch class="lowMarginSwitch" ng-model="metaWebFunctionality" ng-change="resetLikeConditions()">{{translate.load("sbi.bm.metaweb.enable")}}</md-switch>
								        </md-input-container>
								        <%} %>
									        
										
					      				
					      				<!-- ng-click="fileChange();checkChange()"  -->
					      				<%
										if (userProfile.isAbleToExecuteAction(SpagoBIConstants.META_MODEL_LIFECYCLE_MANAGEMENT)) {%>
					      				<md-input-container flex="50" class="noMargin">
								          <md-switch class="lowMarginSwitch" ng-model="selectedBusinessModel.modelLocked" ng-change="businessModelLock()">{{ selectedBusinessModel.modelLocked ? translate.load("sbi.bm.unlockModel") : translate.load("sbi.bm.lockModel")}}</md-switch>
								        </md-input-container>
										<%} %>
										<md-input-container flex="100" class="noMargin">
											<md-switch class="lowMarginSwitch" ng-model="selectedBusinessModel.smartView" aria-label="Switch smart preview">
												{{selectedBusinessModel.smartView ? translate.load("sbi.bm.smart.view") : translate.load("sbi.bm.advanced.view")}}	
												<md-tooltip>{{::translate.load("sbi.bm.smart.view.tooltip")}}</md-tooltip>								   
											</md-switch>
										</md-input-container>
									</div>
								</div>
								<md-card-content ng-if="metaWebFunctionality" layout="column">
						      		<md-toolbar class="secondaryToolbar">
								      <div class="md-toolbar-tools">
								          <span>{{translate.load("sbi.bm.metaweb.configurationTablePrefixTitle")}}</span>
								          <span flex></span>
								          	<md-icon md-font-icon="fa fa-info-circle">
									        		<md-tooltip ng-if="metaWebFunctionality" md-delay="500">{{translate.load("sbi.bm.metaweb.configurationTablePrefixTooltip")}}</md-tooltip>
									        </md-icon>
								      </div>
							    	</md-toolbar>
				        			<div layout="row" layout-align="start center">
								        <md-input-container class="md-block" flex>
											<label>{{translate.load("sbi.bm.metaweb.tablePrefixLike")}}
												<md-tooltip ng-if="metaWebFunctionality" md-delay="500">{{translate.load("sbi.bm.metaweb.tablePrefixLikeExampleTooltip")}}</md-tooltip>
											</label>
								        	
											<input ng-model="selectedBusinessModel.tablePrefixLike" ng-disabled="!metaWebFunctionality" ng-maxlength="500" id="tablePrefixLike" name="tablePrefixLike" ng-pattern="regex.extendedAlphanumeric" ng-onload="updateTablePrefixLikeValue()">
		 									<div ng-messages="businessModelForm.tablePrefixLike.$error" role="alert" ng-messages-multiple>
												<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
												<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
				  							</div>
	 									</md-input-container>
	 									<md-input-container class="md-block" flex>
				  							<label>{{translate.load("sbi.bm.metaweb.tablePrefixNotLike")}}
				  								<md-tooltip ng-if="metaWebFunctionality" md-delay="500">{{translate.load("sbi.bm.metaweb.tablePrefixNotLikeExampleTooltip")}}</md-tooltip>
											</label>
											<input ng-model="selectedBusinessModel.tablePrefixNotLike" ng-disabled="!metaWebFunctionality" ng-maxlength="500" id="tablePrefixNotLike" name="tablePrefixNotLike" ng-pattern="regex.extendedAlphanumeric" ng-onload="updateTablePrefixNotLikeValue()">
											<div ng-messages="businessModelForm.tablePrefixNotLike.$error" role="alert" ng-messages-multiple>
												<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
												<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
				  							</div>
										</md-input-container>
										
									</div>
				        		</md-card-content>
							</md-card-content>
			      		</md-card>
			      </md-tab>
			      
			      <md-tab label='{{translate.load("sbi.catalogues.generic.title.metadata");}}'>		      
			      
			      <% if( (userProfile.isAbleToExecuteAction(SpagoBIConstants.META_MODEL_SAVING_TO_RDBMS)) || (userProfile.isAbleToExecuteAction(SpagoBIConstants.META_MODEL_CWM_EXPORTING)) ) {%>
			      
			      <md-card ng-if="bmVersions!=undefined && bmVersions.length>0">
			      	<md-toolbar class="secondaryToolbar">
				      <div class="md-toolbar-tools">
				        <h2>
				          <span>{{translate.load("sbi.catalogues.generic.title.metadata")}}</span>
				        </h2>
				      </div>
				    </md-toolbar>
		
			      <md-card-content>
			      	<div layout="column" layout-margin>
				      	<div  layout="row">
				      		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.META_MODEL_SAVING_TO_RDBMS)) {%>
								<md-button ng-click="importMetadata(selectedBusinessModel.id)"  ng-disabled="bmImportingShow" class="md-raised md-ExtraMini" style="min-width:15rem;">{{translate.load("sbi.tools.catalogue.metaModelsCatalogue.import.metadata");}}</md-button>
							<%} %>
							<%-- if (userProfile.isAbleToExecuteAction(SpagoBIConstants.META_MODEL_CWM_EXPORTING)) {%>	
								<md-button ng-click="downloadCWMFile(selectedBusinessModel.id)" ng-disabled="bmCWMProcessingShow" class="md-raised" >{{translate.load("sbi.metadata.cwm.export.button")}}</md-button></div>
							<%} --%>
						</div>
						<div ng-show="bmImportingShow" >
		    				<md-progress-linear md-mode="indeterminate"></md-progress-linear>
		    				<div class="bottom-block">
						      <span>{{translate.load("sbi.catalogues.generic.import.progress")}}</span>
						    </div>
		      			</div> 
		      			<!-- 
		      			<div   ng-show="bmCWMProcessingShow" >
		    				<md-progress-linear md-mode="indeterminate"></md-progress-linear>
		    				<div class="bottom-block">
						      <span>{{translate.load("sbi.metadata.cwm.export.progress")}}</span>
						    </div>
		      			</div>
		      			--> 
			      	</div>
					<%-- if (userProfile.isAbleToExecuteAction(SpagoBIConstants.META_MODEL_CWM_EXPORTING)) {%>		      	 
			      	<md-divider layout-margin></md-divider>
			      	<div layout="row" layout-wrap layout-align="start center">
						<label flex  class="buttonLabel">{{translate.load("sbi.metadata.cwm.import.file.upload")}}:</label>
				        <file-upload flex ng-model="fileObjCWM" id="cwmFile" ng-click="fileCWMChange();checkCWMChange()"></file-upload>
		            	<div flex>
		               		 <md-button  ng-click="importCWMFile(selectedBusinessModel.id)" ng-disabled="bmCWMDisableImportButton" class="md-raised">{{translate.load("sbi.metadata.cwm.import.button")}}</md-button>
		            	</div>                  						 
		      		</div>
		      		<%} --%>
		      		<!--
		      		<div ng-show="bmCWMImportingShow" >
		    			<md-progress-linear md-mode="indeterminate"></md-progress-linear>
	   					<div class="bottom-block">
				      		<span>{{translate.load("sbi.metadata.cwm.import.progress")}}</span>
					    </div>
	      			</div>
	      			--> 
			      </md-card-content>
		      	</md-card>
			      
		 		<%} %>   
			      </md-tab>			      
			      
			      <md-tab label='{{translate.load("sbi.widgets.catalogueversionsgridpanel.title");}}'>
			      <md-card layout="column">
			      	  <md-toolbar class="secondaryToolbar">
					      <div class="md-toolbar-tools">
						      <h2>
						      	<span>{{translate.load("sbi.widgets.catalogueversionsgridpanel.title")}}</span>
						      </h2>			   
					      </div>
				      </md-toolbar>				
					      
					<md-content layout="column">
			            <div ng-if="!bmVersions || bmVersions==0">
			                <div class="kn-noItems" layout-align="center center ">
			                    {{translate.load("sbi.bm.versions.noversions")}}
			                </div>
			            </div>
			            <div class="kn-custom-list h42" class="tabContainer">
			                <div class="kn-list-item selectable" ng-repeat="version in bmVersions track by $index" ng-class="{'selected':version.$$hashKey == selectedVersions}" ng-style="{'background-color': version.active ? '#cddcea' : ''}">
			                    <md-icon ng-if="version.active == false" md-font-icon="kn-list-preicon fa fa-history"></md-icon>
			                    <md-icon ng-if="version.active == true" md-font-icon="kn-list-preicon fa fa-check"></md-icon>
			                    <div layout="row" flex>
				                    <div class="kn-list-text" layout="column">                        
				                        <h3>{{ version.fileName }}</h3>
				                        <p>{{ version.creationDate | date:'yyyy-MM-dd HH:mm:ss Z'}}</p>	                        
				                    </div>
				                    <div class="kn-list-text">  
				                    	<h3>{{version.creationUser}}</h3>
				                    </div>
			                    </div>                    	
			                    <md-menu class="kn-list-menu-button">
			                    	<md-button class="kn-list-action-button md-icon-button" ng-click="openMenu($mdOpenMenu,$event)" aria-label="open menu" >
			                        	<md-icon md-menu-origin md-font-icon="fa fa-ellipsis-v"></md-icon>
			                    	</md-button>
			                    	<md-menu-content width="4">
			                    	
			                    		<md-menu-item ng-if="!version.active"> 
			                    			<md-button ng-click="clickRightTable(version)">
						                  		<div layout="row" flex>
						                  			<md-icon md-menu-align-target md-font-icon="fa fa-check-circle" class="md-secondary md-icon-button"  aria-label="set active version"></md-icon>
							                    	<p flex>{{translate.load("sbi.bm.versions.setactive")}}</p>
							                  	</div>
							              	</md-button>          
			                        	</md-menu-item>
			                        	
			                    		<md-menu-item ng-if="version.hasContent && !version.hasLog"> 
			                    			<md-button ng-click="downloadFile(version,event,'JAR')">
						                  		<div layout="row" flex>
						                  			<md-icon md-menu-align-target md-font-icon="fa fa-file-archive-o" class="md-secondary md-icon-button"  aria-label="download jar"></md-icon>
							                    	<p flex>{{translate.load("sbi.bm.download.jar")}}</p>
							                  	</div>
							              	</md-button>
							            </md-menu-item>
							            
							            <md-menu-item ng-if="version.hasLog"> 
			                    			<md-button ng-click="downloadFile(version,event,'LOG')">
						                  		<div layout="row" flex>
						                  			<md-icon md-menu-align-target md-font-icon="fa fa-file-text-o" class="md-secondary md-icon-button"  aria-label="download log"></md-icon>
							                    	<p flex>{{translate.load("sbi.bm.download.log")}}</p>
							                  	</div>
							              	</md-button>
							            </md-menu-item>
			                        	
			                        	<md-menu-item ng-if="version.hasFileModel"> 
			                    			<md-button ng-click="downloadFile(version,event,'SBIMODEL')">
						                  		<div layout="row" flex>
						                  			<md-icon md-menu-align-target md-font-icon="fa fa-file-code-o" class="md-secondary md-icon-button"  aria-label="download sbimodel"></md-icon>
							                    	<p flex>{{translate.load("sbi.bm.download.model")}}</p>
							                  	</div>
							              	</md-button>
							            </md-menu-item>
			                        	
			                    		<md-menu-item>      
			                    			<md-button ng-click="deleteItemVersion(version,event)">
						                  		<div layout="row" flex>
						                  			<md-icon md-menu-align-target md-font-icon="fa fa-trash" class="md-secondary md-icon-button"  aria-label="delete version"></md-icon>
							                    	<p flex>{{translate.load("sbi.generic.delete")}}</p>
							                  	</div>
							              	</md-button>              			
			                        	</md-menu-item>
			                    		
			                    	</md-menu-content>
			                    </md-menu>
			                </div>
			            </div>
			            
			        </md-content>
					       
			      </md-card>
			      </md-tab>
			      
			      
			      <md-tab label="Drivers" ng-if="selectedBusinessModel.id">			  	
				  	<md-tab-body>
				  		<ng-include src="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/drivers.html")%>'" />
				  	</md-tab-body>
				  </md-tab>
	
				</md-tabs>
		      </form>
			</detail>
		</angular-list-detail>
	</body>
</html>
