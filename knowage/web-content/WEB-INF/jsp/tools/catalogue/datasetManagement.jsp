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


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="datasetModule">
	
	<head>
	
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
			
		<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
		
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/catalogues/datasetManagement.js"></script>
		
		<!-- Codemirror -->
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
		<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/javascript/javascript.js"></script>
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/groovy/groovy.js"></script>
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/sql/sql.js"></script>
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js"></script>
		<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/display/autorefresh.js"></script>

		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		
		<title>Dataset Catalogue</title>
		
	</head>
	
	<body ng-controller="datasetController" class="bodyStyle kn-rolesManagement">
	
		<angular-list-detail>
	       
	       	<list label="translate.load('sbi.roles.datasets')"  new-function="createNewDataSet">
	       
		       	<angular-table
			     	flex
				 	id="datasetList_id" 
				 	ng-model="datasetsListTemp"
					columns=dataSetListColumns
					show-search-bar=true 
					highlights-selected-item=true
					click-function="loadDataSet(item)"
					selected-item="selectedDataSetInit" 
					speed-menu-option="manageDataset" >
				</angular-table> 
	        
	       	</list>
	       
      		<extra-button>
      		
				<md-fab-speed-dial md-open="false" md-direction="left"
	                         ng-class="'md-scale'">
			        
			        <md-fab-trigger>
			          	<md-button aria-label="menu" class="md-fab md-raised md-mini md-warn">
			            	<md-icon md-font-icon="fa fa-bars" class="fa fa-2x"></md-icon>
			          	</md-button>
			        </md-fab-trigger>
			
			        <md-fab-actions>
			            <md-icon md-font-icon="fa fa-eye" ng-click="previewDataset()"></md-icon>			            
			        </md-fab-actions>
			        
	      		</md-fab-speed-dial>
		      
			</extra-button>
	       
	       <!-- DATASET DETAIL PANEL -->
	       <detail save-function="saveDataSet" cancel-function="cancelDataSet">
	       
	       		<form name=datasetForm ng-show="selectedDataSet!=null" style="height:100%; overflow-y:hidden">
	       		
	       			 <!-- DATASET DETAIL PANEL TABS -->
	       			 <md-tabs md-selected="selectedTab" md-border-bottom="" style="min-height:100%">
						     
						 <!-- DATASET DETAIL PANEL "DETAIL" TAB -->            			
						 <md-tab label='{{translate.load("sbi.generic.details");}}' >
						 
						 	<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" >
								<md-card layout-padding>
									<div flex=100>
										<md-input-container class="md-block">
									    	<label>{{translate.load("sbi.ds.label")}}</label>
											<input ng-model="selectedDataSet.label">
										</md-input-container>
									</div>
									<div flex=100>
										<md-input-container class="md-block">
									    	<label>{{translate.load("sbi.ds.name")}}</label>
											<input ng-model="selectedDataSet.name">
										</md-input-container>
									</div>
									<div flex=100>
										<md-input-container class="md-block">
									    	<label>{{translate.load("sbi.ds.description")}}</label>
											<textarea ng-model="selectedDataSet.description" md-maxlength="150" rows="3" md-select-on-focus></textarea>
										</md-input-container>
									</div>
									<div flex=100>
								       <md-input-container class="md-block" > 
								       <label>{{translate.load("sbi.ds.scope")}}</label>
									       <md-select placeholder ="{{translate.load('sbi.ds.scope')}}"
									        ng-required = "true"
									        ng-model="selectedDataSet.scopeId">   
									        <md-option 
									        ng-repeat="l in scopeList" value="{{l.VALUE_ID}}">{{l.VALUE_CD}}
									        </md-option>
									       </md-select>  
									       	<div  ng-messages="datasetForm.lbl.$error" ng-show="!selectedDataSet.scopeId">
				       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
			       						 	</div>
								        </md-input-container>
								   </div>
								   <div flex=100>
								       <md-input-container class="md-block" > 
								       <label>{{translate.load("sbi.generic.category")}}</label>
								       <md-select placeholder ="{{translate.load('sbi.generic.category')}}"
								        ng-model="selectedDataSet.catTypeId">   
								        <md-option 
								        ng-repeat="l in categoryList" value="{{l.VALUE_ID}}">{{l.VALUE_CD}}
								        </md-option>
								       </md-select>  
								        </md-input-container>
								   </div>
								</md-card>
							</md-content>
							
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" layout-padding style="padding-top:0px;">
							
								<!-- TOOLBAR FOR THE CARD THAT HOLDS OLDER DATASET VERSIONS. (danristo) -->
						     	<md-toolbar class="secondaryToolbar" layout-padding>
						     	
						          	<div class="md-toolbar-tools">
							            
							            <h2>
							              <span>{{translate.load('sbi.ds.versionPanel')}}</span>
							            </h2>
							            
						         		<span flex></span>
							         											            
							            <md-button class="md-icon-button" aria-label="Clear all" ng-click="deleteAllDatasetVersions()" title="{{translate.load('sbi.ds.clearOldVersion')}}">
							              <md-icon md-font-icon="fa fa-eraser" class="fa fa-1x"></md-icon>
							            </md-button>
							         
						          	</div>
						          	
						        </md-toolbar>						         
							    
								<md-card layout-padding style="height:300px; margin:0px">
								
									<angular-table
										 flex
					 					 id="datasetVersionList_id" 
					 					 ng-model="selectedDataSet.dsVersions"
					 					 style="height:100%;"
					 					 click-function="selectDatasetVersion(item,index,a)"
										 columns='[
										         {"label":"Creation User","name":"userIn"},
										         {"label":"Type","name":"type"},
										         {"label":"Creation Date", "name":"dateIn"}
										         ]'
										show-search-bar=false
										highlights-selected-item=true
										speed-menu-option="manageVersion" >
									</angular-table>
									
								</md-card>								
								
							</md-content>
							
					     </md-tab>
					     
					      <!-- DATASET DETAIL PANEL "TYPE" TAB -->  
					     <md-tab label='{{translate.load("sbi.generic.type");}}'>
					     
					     	<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
								<md-card layout-padding>
									<div flex=100>
								       <md-input-container class="md-block" > 
									       <label>{{translate.load("sbi.ds.dsTypeCd")}}</label>
									       <md-select 	placeholder ="{{translate.load('sbi.ds.dsTypeCd')}}"
									       	 			ng-required = "true"
									        			ng-model="selectedDataSet.dsTypeCd">   
									        	<md-option ng-repeat="l in datasetTypeList" value="{{l.VALUE_CD}}">{{l.VALUE_CD}}</md-option>
									       </md-select>  
									       <div  ng-messages="datasetForm.lbl.$error" ng-show="!selectedDataSet.dsTypeCd">
				       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
			       						 	</div>
								        </md-input-container>
								   </div>
								</md-card>
							</md-content>
							
						<!-- ELEMENTS NEEDED FOR THE "FILE" DATASET TYPE -->
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='File'">
							
							<!-- UPLOADING AND CHANGING FILE AS A DATA SOURCE OF THE DATASET -->
							<md-card layout-padding  style="margin-top:0">
								
								<div layout="row" flex=100 layout-align="start center" ng-show="selectedDataSet.fileName=='' || changingFile">
					                  	
				                  	<label layout-align="center center">
				                  		{{translate.load("sbi.ds.wizard.selectFile")}}:
			                  		</label>
				                  	
				                  	<file-upload 	flex ng-model="fileObj" id="datasetFile" 
				                  					ng-click="fileChange();checkChange();fileObjTakeBackup()" 
					                  				title="{{translate.load('sbi.workspace.dataset.wizard.browsefile.tooltip')}}">
		                			</file-upload>
				                  	
				                  	<div class="">
					                    <md-button 	ng-click="uploadFile()" class="md-raised" 
					                     			ng-disabled="!fileObj.fileName || (changingFile &&selectedDataSet.fileName==fileObj.fileName)" 
					                     			title="{{datasetWizStep1UploadButtonTitle()}}">
			                     			{{translate.load("sbi.workspace.dataset.wizard.upload")}}
		             					</md-button>
				                  	</div>
				                  	
								</div>
									
								<div layout="row" flex=100 ng-if="selectedDataSet.fileName!='' && !changingFile">
							 		
							 		<label style="margin-top:14px; margin-bottom:8px">
							 			{{translate.load("sbi.workspace.dataset.wizard.file.uploaded")}}: <strong>{{selectedDataSet.fileName}}</strong>
						 			</label>
						 			
								 	<span flex></span>
								  
								  	<div class="">
								  	
									    <md-button 	ng-click="changeUploadedFile()" class="md-raised" 
									    			title="{{translate.load('sbi.workspace.dataset.wizard.file.change.tooltip')}}">
				                     			{{translate.load("sbi.workspace.dataset.wizard.file.change")}}
			             				</md-button>
		             				
		           					</div>
		             				
								</div>
								
							</md-card>
							
							<!-- ELEMENTS FOR SETTING THE 'XLS' FILE CONFIGURATION -->
							<md-card ng-if="selectedDataSet.fileType=='XLS'" layout="column" class="threeCombosThreeNumFields" style="padding:0 16 0 16;">          
				        
						        <div layout="row" class="threeCombosLayout">	
							        
							        <!-- XLS file is uploaded --> 
									<div layout="row" flex >
										
										<div layout="row" layout-wrap flex=30>
					                  		<div flex=90 layout-align="center center">
					                     		<md-input-container class="md-block">
					                        		<label>{{translate.load("sbi.ds.file.xsl.skiprows")}}</label> 
					                        		<input 	ng-model="selectedDataSet.skipRows" type="number" 
					                        				step="1" min="0" value="{{selectedDataSet.skipRows}}">
						                     	</md-input-container>
						                  	</div>
										</div>
					                 	
				                		<div layout="row" layout-wrap flex=30>
					                  		<div flex=90 layout-align="center center">
					                     		<md-input-container class="md-block">
					                        		<label>{{translate.load("sbi.ds.file.xsl.limitrows")}}</label> 
					                        		<input 	ng-model="selectedDataSet.limitRows" type="number" 
					                        				step="1" min="0" value="{{dataset.limitRows}}">
						                     	</md-input-container>
						                  	</div>
										</div>
										
										<div layout="row" layout-wrap flex=30>
					                  		<div flex=90 layout-align="center center">
					                     		<md-input-container class="md-block">
					                        		<label>{{translate.load("sbi.ds.file.xsl.sheetnumber")}}</label> 
					                        		<input 	ng-model="selectedDataSet.xslSheetNumber" type="number" 
					                        				step="1" min="1" value="{{selectedDataSet.xslSheetNumber}}">
						                     	</md-input-container>
						                  	</div>
										</div>
										
									</div>
										
								</div>					
								
				       	 	</md-card>
				       	 	
				       	 	<!-- ELEMENTS FOR SETTING THE 'CSV' FILE CONFIGURATION -->
				       	 	<md-card ng-if="selectedDataSet.fileType=='CSV'" layout="column" class="threeCombosThreeNumFields" style="padding:0 16 0 16;">         		
		         		
				         		<div layout="row" class="threeCombosLayout">								
							              
							        <!-- CSV file is uploaded --> 
									<div layout="row" flex >
						                 	
					                 	<div layout="row" layout-wrap flex=30>
					                  		<div flex=90 layout-align="center center">
					                     		<md-input-container class="md-block">
					                        		<label>{{translate.load("sbi.ds.file.csv.delimiter")}}</label> 
					                        		<md-select aria-label="aria-label" ng-model="selectedDataSet.csvDelimiter">
					                           			<md-option 	ng-repeat="csvDelimiterCharacterItem in csvDelimiterCharacterTypes" 
					                           						ng-click="chooseDelimiterCharacter(csvDelimiterCharacterItem)" 
					                           						value="{{csvDelimiterCharacterItem.name}}">
				                          						{{csvDelimiterCharacterItem.name}}
				                     						</md-option>
					                        		</md-select>
						                     	</md-input-container>
						                  	</div>
										</div>
					                 	
				                		<div layout="row" layout-wrap flex=30>
					                  		<div flex=90 layout-align="center center">
					                     		<md-input-container class="md-block">
					                        		<label>{{translate.load("sbi.ds.file.csv.quote")}}</label> 
					                        		<md-select aria-label="aria-label" ng-model="selectedDataSet.csvQuote">
					                           			<md-option 	ng-repeat="csvQuoteCharacterItem in csvQuoteCharacterTypes" 
					                           						ng-click="chooseQuoteCharacter(csvQuoteCharacterItem)" 
					                           						value="{{csvQuoteCharacterItem.name}}">
				                          						{{csvQuoteCharacterItem.name}}
				                     						</md-option>
					                        		</md-select>
						                     	</md-input-container>
						                  	</div>
										</div>
										
										<div layout="row" layout-wrap flex=30>
					                  		<div flex=90 layout-align="center center">
					                     		<md-input-container class="md-block">
					                        		<label>{{translate.load("sbi.workspace.dataset.wizard.csv.encoding")}}</label> 
					                        		<md-select aria-label="aria-label" ng-model="selectedDataSet.csvEncoding">
					                           			<md-option 	ng-repeat="csvEncodingItem in csvEncodingTypes" 
					                           						ng-click="chooseEncoding(csvEncodingItem)" 
					                           						value="{{csvEncodingItem.name}}">
				                          						{{csvEncodingItem.name}}
				                     						</md-option>
					                        		</md-select>
						                     	</md-input-container>
						                  	</div>
										</div>
											
									</div>					
																
						    	</div>
							    	
						    </md-card>					       	 	
							
						</md-content>
							
						<!-- QUERY -->
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Query'">
							
							<md-card layout-padding style="margin-top:0">
							
								<div flex=100>
							       <md-input-container class="md-block" > 
							       <label>{{translate.load("sbi.ds.dataSource")}}</label>
							       <md-select placeholder ="{{translate.load('sbi.ds.dataSource')}}"
							        ng-required = "selectedDataSet.dsTypeCd=='Query'"
							        ng-model="selectedDataSet.dataSource">   
							        <md-option 
							        ng-repeat="l in dataSourceList" value="{{l.label}}">{{l.label}}
							        </md-option>
							       </md-select>  
							       	<div  ng-messages="datasetForm.lbl.$error" ng-show="!selectedDataSet.dataSource">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
							        </md-input-container>
							   	</div>
							   	
							   	<md-input-container class="md-block">
								    	<label>{{translate.load("sbi.ds.query")}}</label>
									<textarea ng-model="selectedDataSet.query" ui-codemirror="{ onLoad : codemirrorLoaded }" ui-codemirror-opts="codemirrorOptions" rows="8" md-select-on-focus></textarea>
									</md-input-container>
								<md-button ng-click="openEditScriptDialog()" class="md-raised md-button md-knowage-theme md-ink-ripple">{{translate.load("sbi.ds.editScript")}}</md-button>
								
							</md-card>
							
						</md-content>
							
						<!-- JAVA CLASS -->
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Java Class'">
							<md-card layout-padding style="margin-top:0">
								<md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.jclassName")}}</label>
						           <input ng-model="">
						         </md-input-container>
							</md-card>
						</md-content>
						
						<!-- WEB SERVICE -->
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Web Service'">
							<md-card layout-padding style="margin-top:0">
								<md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.wsAddress")}}</label>
						           <input ng-model="">
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{sbi.ds.wsOperation")}}</label>
						           <input ng-model="">
						         </md-input-container>
							</md-card>
						</md-content>
							
							<!-- SCRIPT -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Script'">
								<md-card layout-padding style="margin-top:0">
							<md-input-container class="md-block" > 
						       <label>{{translate.load("sbi.functionscatalog.language")}}</label>
						       <md-select  aria-label="dropdown" placeholder ="{{translate.load('sbi.behavioural.lov.placeholder.script')}}"
						       	name ="scriptLanguageDropdown" 
						        ng-model="selectedDataSet.queryScriptLanguage"
						        ng-change="modeChanged(selectedDataSet.queryScriptLanguage)"
						        > <md-option 
						        ng-repeat="l in listOfScriptTypes track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_NM}} </md-option>
						       </md-select>   
					        </md-input-container>
								<md-input-container class="md-block">
							    	<label>{{translate.load("sbi.ds.query")}}</label>
									<textarea  ui-codemirror="cmOption" ng-model="selectedDataSet.queryScript" md-select-on-focus></textarea>
								</md-input-container>
							</md-card>
							</md-content>
							
							<!-- ELEMENTS NEEDED FOR THE "QBE" DATASET TYPE -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Qbe'">
								
								<md-card layout-padding style="margin-top:0">
									
									<div flex=100>
									
								       <md-input-container class="md-block" > 
								       
									       <label>{{translate.load("sbi.ds.dataSource")}}</label>
									       
									       <md-select 	placeholder ="{{translate.load('sbi.ds.dataSource')}}"
									        			ng-model="selectedDataSet.qbeDataSource">   
										        <md-option ng-repeat="l in dataSourceList" value="{{l.label}}">{{l.label}}</md-option>										        
									       </md-select>  
									       
								        </md-input-container>
								        
								  	 </div>
								  	 
								  	 <div flex=100>									
								       <md-input-container class="md-block" > 
								       								       
									       <label>{{translate.load("sbi.tools.managedatasets.datamartcombo.label")}}</label>
									       
									       <md-select 	placeholder ="{{translate.load('sbi.tools.managedatasets.datamartcombo.label')}}"
									        			ng-model="selectedDataSet.qbeDatamarts">   
										        <md-option ng-repeat="l in datamartList" value="{{l.name}}">{{l.name}}</md-option>										        
									       </md-select>  
									       
								        </md-input-container>
								        
								  	 </div>
								  	 
							  	  	<div flex=100 style="padding-left:0; padding-top:0;">
																				
										<md-button flex=20 class="md-raised" ng-click="viewQbe()">
											{{translate.load("sbi.ds.qbe.query.view.button")}}
										</md-button> 
										
										<!-- <div flex=30 style="float:right"> -->
										<md-button flex=20 class="md-raised" ng-click="openQbe()">
											{{translate.load("sbi.ds.qbe.query.open.button")}}
										</md-button> 								
										
									</div>
								  	 
								</md-card>
								
							</md-content>
							
						<!-- CUSTOM -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Custom'">
								<md-card layout-padding style="margin-top:0">
								<md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.jclassName")}}</label>
						           <input ng-model="">
						         </md-input-container>
								</md-card>
							</md-content>
							
						<!-- FLAT -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Flat'">
								<md-card layout-padding style="margin-top:0">
								<md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.persistTableName")}}</label>
						           <input ng-model="selectedDataSet.persistTableName">
						         </md-input-container>
						          <md-input-container class="md-block" > 
								       
									       <label>{{translate.load("sbi.ds.dataSource")}}</label>
									       
									       <md-select 	placeholder ="{{translate.load('sbi.ds.dataSource')}}"
									        			ng-model="selectedDataSet.qbeDataSource">   
										        <md-option ng-repeat="l in dataSourceList" value="{{l.label}}">{{l.label}}</md-option>										        
									       </md-select>  
									       
								        </md-input-container>
								</md-card>
							</md-content>
							
						<!-- CKAN -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Ckan'">
								<md-card layout-padding style="margin-top:0">
								<md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanFileType")}}</label>
						           <input ng-model="a" ng-required = "true">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!a">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanCsvDelimiter")}}</label>
						           <input ng-model="b" ng-required = "true">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!b">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanCsvQuote")}}</label>
						           <input ng-model="c" ng-required = "true">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!c">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanCsvEncoding")}}</label>
						           <input ng-model="d">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!d">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanSkipRows")}}</label>
						           <input ng-model="e">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!e">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanLimitRows")}}</label>
						           <input ng-model="f">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!f">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanXslSheetNumber")}}</label>
						           <input ng-model="g">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!g">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanId")}}</label>
						           <input ng-model="h" ng-required = "true">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!h">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
						         <md-input-container class="md-block" flex-gt-sm>
						           <label>{{translate.load("sbi.ds.ckanUrl")}}</label>
						           <input ng-model="j" ng-required = "true">
						           <div  ng-messages="datasetForm.lbl.$error" ng-show="!j">
		       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
	       						 	</div>
						         </md-input-container>
								</md-card>
							</md-content>
							
							<!-- ELEMENTS NEEDED FOR THE "FEDERATED" DATASET TYPE -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Federated'">
								
								<md-card layout-padding style="margin-top:0">
								
									<div flex=100 style="padding-left:0;">
																				
										<md-button flex=20 class="md-raised" ng-click="viewQbe()">
											{{translate.load("sbi.ds.qbe.query.view.button")}}
										</md-button> 
										
										<md-button flex=20 class="md-raised" ng-click="openQbe()">
											{{translate.load("sbi.ds.qbe.query.open.button")}}
										</md-button> 
										
									</div>
									
								</md-card>
								
							</md-content>
							
							<!-- ELEMENTS NEEDED FOR THE "REST" DATASET TYPE -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='REST'">
								
								<md-card layout-padding style="margin-top:0">
									
									<div flex=100>
										<md-input-container class="md-block">
									    	<label>Address</label>
											<input ng-model="selectedDataSet.restAddress" ng-required = "true">
											<div  ng-messages="datasetForm.lbl.$error" ng-show="!selectedDataSet.restAddress">
				       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
			       						 	</div>
										</md-input-container>
									</div>
									
									<div flex=100>
										<md-input-container class="md-block">
											<label>Request body</label>
									    	<textarea ng-model="selectedDataSet.restRequestBody" md-maxlength="150" rows="3" md-select-on-focus></textarea>
										</md-input-container>
									</div>
									
									<div flex=100 >
								       
								       <md-input-container class="md-block" > 
									       
									       	<label>HTTP methods</label>
									       	
									       	<md-select 	placeholder ="HTTP methods" ng-required = "true"
									        			ng-model="selectedDataSet.restHttpMethod">   
										        <md-option ng-repeat="l in httpMethods" value="{{l.value}}">
										        	{{l.name}}
										        </md-option>
									       	</md-select>  
											
											<div  ng-messages="datasetForm.lbl.$error" ng-show="selectedDataSet.restHttpMethod==null">
				       						 	<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
			       						 	</div>
			       						 									        
		       						 	</md-input-container>
			       						 
								   </div>
								   																	
								</md-card>
							
							</md-content>
								
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='REST'" style="padding:0 8 0 8">
								
								<!-- TOOLBAR FOR THE CARD THAT HOLDS ADD REQUEST HEADER BUTTON. (danristo) -->
						     	<md-toolbar class="secondaryToolbar" layout-padding>
						     	
						          	<div class="md-toolbar-tools">
							            
							            <h2>
							              <span>Request Headers</span>
							            </h2>
							            
						         		<span flex></span>
							         											            
							            <md-button class="md-icon-button" aria-label="Add request header" ng-click="requestHeaderAddItem()" title="{{translate.load('sbi.generic.add')}}">
							              	<md-icon md-font-icon="fa fa-plus-circle" class="fa fa-2x"></md-icon>
							            </md-button>
							         
						          	</div>
						          	
						        </md-toolbar>						         
							    
								<md-card layout-padding style="margin:0 0 8 0; padding:0px">
																   
								   <div>
								   
								   		<angular-table
												id="requestHeadersTable"
												ng-model=restRequestHeaders
												columns="requestHeadersTableColumns"
												show-search-bar=false
												scope-functions="metaScopeFunctions"
												no-pagination=false
												speed-menu-option="requestHeadersDelete" >
										</angular-table>
								   
								   </div>
									
								</md-card>
								
							</md-content>	
							
								
							<!-- ELEMENTS NEEDED FOR THE "REST" DATASET TYPE -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='REST'" style="padding: 0 8 0 8">
								
								<md-card layout-padding style="margin:0 0 8 0">
								
									<div flex=100 style="display:flex;">											
																				
										<md-input-container class="md-block" style="float:left; width:75%">
									    	<label>JSON Path Items</label>
											<input ng-model="selectedDataSet.label">
										</md-input-container>
										 
										<div style="width:25%">
											<md-button 	style="margin:16px 0 16px 0; float:right;" class="md-icon-button" 
														aria-label="Add request header" ng-click="showInfoForRestParams('jsonPathItems')" 
														title="{{translate.load('sbi.ds.help')}}">
								              	<md-icon md-font-icon="fa fa-info-circle" class="fa fa-2x"></md-icon>
								            </md-button>
							            </div>
										
									</div>
									
									<div flex=100 style="display:flex;">
										<div flex=50 layout="row" layout-align="start center">
							           	
					                  		<label>
					                  			Use directly JSON Attributes:
				                  			</label> 
					                  		
					                  		
					                  		<md-input-container class="small counter" style="padding-left:8px;">
					                     		<md-checkbox 	aria-label="Checkbox 2" 
						                     					ng-model="_ss_3" ng-checked="" >
												</md-checkbox>
					                  		</md-input-container>
					                  		
										</div>
										
										<div style="width:50%">
											<md-button 	style="margin:16px 0 16px 0; float:right;" class="md-icon-button" 
														aria-label="Add request header" ng-click="showInfoForRestParams('directJsonAttributes')" 
														title="{{translate.load('sbi.ds.help')}}">
								              	<md-icon md-font-icon="fa fa-info-circle" class="fa fa-2x"></md-icon>
								            </md-button>
							            </div>
							            
						            </div>
						            
						            <div flex=100 style="display:flex;">
						            
										<div flex=50 layout="row" layout-align="start center">
							           	
					                  		<label>
					                  			NGSI:
				                  			</label> 
					                  		
					                  		
					                  		<md-input-container class="small counter" style="padding-left:8px;">
					                     		<md-checkbox 	aria-label="Checkbox 2" 
						                     					ng-model="_ssw_" ng-checked="" >
												</md-checkbox>
					                  		</md-input-container>
					                  		
										</div>
										
										<div style="width:50%">
											<md-button 	style="margin:16px 0 16px 0; float:right;" class="md-icon-button" 
														aria-label="Add request header" ng-click="showInfoForRestParams('ngsi')" 
														title="{{translate.load('sbi.ds.help')}}">
								              	<md-icon md-font-icon="fa fa-info-circle" class="fa fa-2x"></md-icon>
								            </md-button>
							            </div>
							            
						            </div>
						            
						            <div flex=100 style="display:flex;" ng-show="false">
										<div flex=50 layout="row" layout-align="start center">
							           	
					                  		<label>
					                  			JSON Path Attributes:
				                  			</label> 
					                  		
					                  		
					                  		<md-input-container class="small counter" style="padding-left:8px;">
					                     		<md-checkbox 	aria-label="Checkbox 2" 
						                     					ng-model="_ss_" ng-checked="" >
												</md-checkbox>
					                  		</md-input-container>
					                  		
										</div>
										
										<div style="width:50%">
											<md-button 	style="margin:16px 0 16px 0; float:right;" class="md-icon-button" 
														aria-label="Add request header" ng-click="showInfoForRestParams('jsonPathAttributes')" 
														title="{{translate.load('sbi.ds.help')}}">
								              	<md-icon md-font-icon="fa fa-info-circle" class="fa fa-2x"></md-icon>
								            </md-button>
							            </div>
							            
						            </div>
									
								</md-card>
								
							</md-content>
							
							<!-- JSON path attributes grid  -->
							
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='REST'" style="padding:0 8 0 8">
								
								<!-- TOOLBAR FOR THE CARD THAT HOLDS ADD REQUEST HEADER BUTTON. (danristo) -->
						     	<md-toolbar class="secondaryToolbar" layout-padding>
						     	
						          	<div class="md-toolbar-tools">
							            
							            <h2>
							              <span>JSON path attributes</span>
							            </h2>
							            
						         		<span flex></span>
							         											            
							            <md-button class="md-icon-button" aria-label="Add JSON path attributes" ng-click="restJsonPathAttributesAddItem()" title="{{translate.load('sbi.generic.add')}}">
							              	<md-icon md-font-icon="fa fa-plus-circle" class="fa fa-2x"></md-icon>
							            </md-button>
							         
							         	 <md-button class="md-icon-button" aria-label="Help" ng-click="showInfoForRestParams('jsonPathAttributes')" title="{{translate.load('sbi.ds.help')}}">
							              	<md-icon md-font-icon="fa fa-info-circle" class="fa fa-2x"></md-icon>
							            </md-button>
							            
						          	</div>
						          	
						        </md-toolbar>						         
							    
								<md-card layout-padding style="margin:0 0 8 0; padding:0px">
																   
								   <div>
								   
								   		<angular-table
												id="jsonPathAttrTable"
												ng-model=restJsonPathAttributes
												columns="restJsonPathAttributesTableColumns"
												show-search-bar=false
												scope-functions="metaScopeFunctionsJsonPathAttr"
												no-pagination=false
												speed-menu-option="restJsonPathAttributesDelete" >
										</angular-table>
								   
								   </div>
									
								</md-card>
								
							</md-content>	
							
							<!-- ELEMENTS NEEDED FOR THE "REST" DATASET TYPE -->
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='REST'" style="padding: 0 8 0 8">
								
								<md-card layout-padding style="margin:0 0 8 0">
								
									<div flex=100>											
																				
										<md-input-container class="md-block">
									    	<label>Offset Param</label>
											<input ng-model="_oo_1">
										</md-input-container>
										
									</div>
									
									<div flex=100>											
																				
										<md-input-container class="md-block">
									    	<label>Fetch size Param</label>
											<input ng-model="_oo_2">
										</md-input-container>
										
									</div>
									
									<div flex=100>											
																				
										<md-input-container class="md-block">
									    	<label>Max Results Param</label>
											<input ng-model="_oo_3">
										</md-input-container>
										
									</div>
								
								</md-card>
								
							</md-content>
							
							<!-- ELEMENTS FOR SETTING THE DATASET PARAMETERS -->					
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-show="selectedDataSet.dsTypeCd && selectedDataSet.dsTypeCd.toLowerCase()!='file'" style="padding: 0 8 0 8">
								
								<md-card layout-padding style="margin:0">
									<md-toolbar class="secondaryToolbar" layout-padding>
									
										<div class="md-toolbar-tools">
									
											<h2>
											  <span>{{translate.load('sbi.execution.parametersselection.parameters')}}</span>
											</h2>
										
											<span flex></span>
										
											<md-button class="md-icon-button" aria-label="Clear all" ng-click="parametersAddItem()" title="{{translate.load('sbi.ds.clearOldVersion')}}">
											  <md-icon md-font-icon="fa fa-plus-circle" class="fa fa-1x"></md-icon>
											</md-button>
											
										</div>
													
									</md-toolbar>						         
													
									<md-card layout-padding style="margin:0px">
																			
					 						<div>
											      						
												<angular-table
														id="datasetParametersTable"
														ng-model=parameterItems
														columns="parametersColumns"
														show-search-bar=false
														scope-functions="paramScopeFunctions"
														no-pagination=false
														speed-menu-option="parameterDelete"
															>
												</angular-table>
										   
										</div>
										
									</md-card>
									
								</md-card>
								
							</md-content>
							
					     </md-tab>	
					     
					      <!-- DATASET DETAIL PANEL "ADVANCED" TAB -->  
					     <md-tab label='{{translate.load("sbi.ds.advancedTab");}}'>
							
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
								<md-card layout-padding>
								
								</md-card>
							</md-content>
							
							<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
								<md-card layout-padding>
								
								</md-card>
							</md-content>
							
					     </md-tab>						
						
					</md-tabs>
	       		
	       		</form>
	       
	       </detail>
	       
		</angular-view-detail>
	
	</body>
	
</html>