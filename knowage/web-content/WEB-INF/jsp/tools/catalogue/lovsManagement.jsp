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


<!-- 
	The main JSP page for the management of the LOV catalog.
	
	Author: Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
-->

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html ng-app="lovsManagementModule">
	
	<head>
	
		<!-- HTML meta data -->
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		
		<!-- JSP files included -->
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		
		<!-- Style files included -->
		<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/generalStyle.css">
		
		<!-- Javascript files included -->
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/lovsManagement.js"></script>		
		
		<title>Insert title here</title>
		
	</head>
	
	<body 	class="bodyStyle" 
			ng-controller="lovsManagementController as ctrl" >
		
		<angular_2_col>		
		
			<!-- 
				Left column (page on the left side of the main LOV page) - 
				the LOV catalog (list of LOVs). 
			-->
			<left-col>					
				
				<div class="leftBox">									
					
					<md-toolbar class="md-blue minihead">
						
						<div class="md-toolbar-tools">							
							
							<!-- 
								The title of the left-side page of the main page (of the catalog) 
								inside of the toolbar on its top. 
							-->
							<div>
								{{translate.load("sbi.behavioural.lov.title");}}
							</div>	
							
							<!-- 
								The plus button inside of the toolbar on the top of the left-side 
								page of the main page (of the catalog). This button serves for adding
								new LOV items in the LOV catalog. It calls the "createLov" function
								inside of our controller.
							-->				
							<md-button 	class="md-fab md-ExtraMini addButton"
										style="position:absolute; right:11px; top:0px;"
										ng-click="createLov()"> 
								
								<md-icon	md-font-icon="fa fa-plus" 
											style="margin-top:6px ; color:white;">
								</md-icon>
								 							
							</md-button>
							
						</div>
						
					</md-toolbar>
					
					<!-- 
						The only content of the left-side page will be the table (catalog)
					 	with all LOVs present inside of the DB.
					 -->
					<md-content 	layout-padding 
									style="background-color: rgb(236,236,236);" 
									class="ToolbarBox miniToolbar noBorder leftListbox" >
						
						<!-- 
							ng-model: 
								Attach content of this table to the "listOfLovs" model (variable). 
							columns: 
								Define columns that our table (LOV container - catalog) will present.
							columns-search: 
								Which columns will be included when searching for certain content.
							click-function: 
								Which function inside of our controller is going to be called when 
								clicking on some LOV item. Forward to it the item (content) of that
								LOV.
							menu-option: 
								Which variable is going to be linked to this functionality. On the 
								right click we will offer deleting of the item on which this option
								is used (managed inside of our controller).
						-->
						<angular-table 	layout-fill
										id="listOfLovs"
										ng-model="listOfLovs"
										columns='[
													{"label":"Label", "name":"label"},
													{"label":"Description", "name":"description"}
												 ]',
										columns-search='["label","description"]'
										show-search-bar=true
										highlights-selected-item=true
										click-function=itemOnClick(item)
										menu-option=lovsManagementSpeedMenu >						
						</angular-table>						
								
				</div>	
							
			</left-col>
			
			<!-- Right column (page on the right side of the main LOV page) - the LOV form. -->
			<right-col>
						
				<!-- When the "Save" button is clicked, run "saveLov" function in our model. -->			
				<form 	layout-fill 
						ng-submit="saveLov()" 
						class="detailBody md-whiteframe-z1" 
						novalidate >
					
					<!-- When the "showMe" parameter is true, show the right side of the main LOV page. -->
					<div ng-show="showMe">
						
						<!-- The toolbar on the top of the right-side page of the main page. -->
						<md-toolbar class="md-blue minihead">
							
							<div class="md-toolbar-tools h100">
								
								<!-- The title of the right-side page of the main page. -->
								<div style="text-align:center; font-size:24px;">
									{{translate.load("sbi.behavioural.lov.title");}}
								</div>
								
								<!-- Buttons (commands) inside of the toolbar on the top of the right-side page. -->
								<div 	style="position:absolute; right:0px" 
										class="h100" >
									
									<!-- "Cancel" button. -->
									<md-button 	type="button" 
												tabindex="-1" 
												aria-label="cancel"
												class="md-raised md-ExtraMini " 
												style=" margin-top:2px;"
												ng-click="cancel()" >												
										{{translate.load("sbi.browser.defaultRole.cancel");}}
									</md-button>
									
									<!-- "Save" button. -->
									<md-button 	type="submit"
												aria-label="save layer" 
												class="md-raised md-ExtraMini"
												style="margin-top:2px;" >
										{{translate.load("sbi.browser.defaultRole.save");}} 
									</md-button>
									
								</div>
								
							</div>
							
						</md-toolbar>
						
						<!-- 
							Content of the right-side page of the main LOV page. It lies immediately 
							under the right-side toolbar (on its top).
						-->
						<md-content flex 
									style="margin-left:20px" 
									class="ToolbarBox miniToolbar noBorder" >
						
							<!-- 
								Height of our tabs that are placed inside of the right-side page's 
								content will be dynamic - it will follow the current dimensions of 
								the window. 
							-->
							<md-tabs md-dynamic-height>
							
								<!-- The left tab - the LOV form. Active by default. -->
								<md-tab label='{{translate.load("sbi.behavioural.lov.lovFormTabTitle")}}' 
										md-active=true >
								
									<!-- 
										The toolbar of the upper panel of the main page - 
										the title of the generic panel (the form common for 
										all LOV items and all LOV input types. 
									 -->
									<md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.behavioural.lov.details.wizardUpper");}}
									</md-toolbar>
									
									<md-content flex>
										
										<!-- Text field for the Label property. -->
										<div 	layout="row" 
												layout-wrap >
										
									      	<div flex=100>
									      	
									       		<md-input-container class="small counter">
									       		
										      		<label>
										      			{{translate.load("sbi.behavioural.lov.details.label");}}
									      			</label>
									      			
										       		<input 	ng-model="selectedLov.label" 
										       				required
										        			maxlength="100" 
										        			ng-maxlength="100" 
										        			md-maxlength="100" > 
										        			
							        			</md-input-container>
							        			
									      	</div>
									      	
									     </div>
									     
									     <!-- Text field for the Name property. -->
										<div 	layout="row" 
												layout-wrap >
												
									      	<div flex=100>
									      	
									       		<md-input-container class="small counter">
									       		
										      		<label>
										      			{{translate.load("sbi.behavioural.lov.details.name");}}
									      			</label>
									      			
										       		<input 	ng-model="selectedLov.name" 
										       				required
										        			maxlength="100" 
										        			ng-maxlength="100" 
										        			md-maxlength="100"> 
										        			
							        			</md-input-container>
							        			
									      	</div>
									      	
									     </div>
									     
									     <!-- Text field for the Description property. -->
										<div 	layout="row" 
												layout-wrap >
												
									      	<div flex=100>
									      	
									       		<md-input-container class="small counter">
									       		
										      		<label>
										      			{{translate.load("sbi.behavioural.lov.details.description");}}
									      			</label>
									      			
										       		<input 	ng-model="selectedLov.description" 
									       					required
										        			maxlength="100" 
										        			ng-maxlength="100" 
										        			md-maxlength="100">
										        			 
							        			</md-input-container>
							        			
									      	</div>
									      	
									     </div>
									     
									     <!-- Combo box for the Input type property. -->
									     <div 	layout="row" 
									     		layout-wrap >
								      		
								      		<div flex=100>
									       		
									       		<md-input-container class="small counter">
											       	
											       	<label>
										      			{{translate.load("sbi.behavioural.lov.details.inputType");}}
									      			</label>
									      			
											       	<md-select  aria-label="aria-label"
											        			ng-model="selectedLov.itypeId" > 
											        			
									        			<md-option	ng-repeat="it in listOfInputTypes" 
									        						value="{{it.VALUE_ID}}" > 
							        						{{it.VALUE_NM}} 
						        						</md-option>
						        						
											       </md-select> 
											       
										       </md-input-container>
										       
										      </div>
										      
									     </div>
						     
									</md-content>
									
									<!-- Toolbar and content for Query input type. -->
									<div ng-show="selectedLov.itypeId == 1">									
										
										<!-- 
											Toolbar (header) of the panel for the Query input type
											on the form page (right side of the main LOV catalog
											page).
										-->
										<md-toolbar class="md-blue minihead md-toolbar-tools" 
													style="margin-top:15px" >
											{{translate.load("sbi.behavioural.lov.details.queryWizard");}}
										</md-toolbar>
										
										<!-- 
											Content (body) of the panel for the Query input type
											on the form page (right side of the main LOV catalog
											page).
										-->
										<md-content>										
											
											<!-- Combo box for Data source label. -->
										     <div 	layout="row" 
										     		layout-wrap >
										     		
									      		<div flex=100>
									      		
										       		<md-input-container class="small counter">
												       
												       	<label>
											      			{{translate.load("sbi.behavioural.lov.details.dataSourceLabel");}}
										      			</label>
										      			
												       	<md-select  aria-label="aria-label"
												        			ng-model="selectedLov.dataSource"> 
										        			
										        			<md-option 	ng-repeat="ds in listOfDatasources"
										        						value="{{ds.DATASOURCE_ID}}">
								        						{{ds.DATASOURCE_LABEL}} 
							        						</md-option>
							        						
												       	</md-select> 
												       
											       	</md-input-container>
									      		</div>
									    	</div>
										     
									      	<!-- Text field for the Description parameter. -->
											<div 	layout="row" 
													layout-wrap >
													
										      	<div flex=100>
										      	
										       		<md-input-container class="small counter">
										       		
											      		<label>
											      			{{translate.load("sbi.behavioural.lov.details.queryDescription");}}
										      			</label>
										      			
											       		<textarea 	rows="4" 
											       					cols="50" >													
														</textarea>
														
								        			</md-input-container>
								        			
										      	</div>
										      	
										     </div>
										
										</md-content>
									
									</div>
									
									<!-- Toolbar and content for Script input type. -->	
									<div ng-show="selectedLov.itypeId == 2">		
									
										<!-- 
											Toolbar (header) of the panel for the Script input type
											on the form page (right side of the main LOV catalog
											page).
										-->								
										<md-toolbar class="md-blue minihead md-toolbar-tools" 
													style="margin-top:15px" >
											{{translate.load("sbi.behavioural.lov.details.scriptWizard");}}
										</md-toolbar>
										
										<!-- 
											Content (body) of the panel for the Script input type
											on the form page (right side of the main LOV catalog
											page).
										-->
										<md-content>
											
											<!-- Combo box for the Script type. -->
										     <div 	layout="row" 
										     		layout-wrap >
										     		
									      		<div flex=100>
									      		
										       		<md-input-container class="small counter">
												       
												       	<label>
											      			{{translate.load("sbi.behavioural.lov.details.dataSourceLabel");}}
										      			</label>
										      			
												       	<md-select  aria-label="aria-label"
												        			ng-model="selectedLov.scriptType"> 
										        			
										        			<md-option 	ng-repeat="st in listOfScriptTypes"
										        						value="{{st.VALUE_CD}}">
								        						{{st.VALUE_NM}} 
							        						</md-option>
							        						
												       	</md-select> 
												       
											       	</md-input-container>
									      		</div>
									    	</div>
										     
									      	<!-- Text field for the Description parameter. -->
											<div 	layout="row" 
													layout-wrap >
													
										      	<div flex=100>
										      	
										       		<md-input-container class="small counter">
										       		
											      		<label>
											      			{{translate.load("sbi.behavioural.lov.details.scriptDescription");}}
										      			</label>
										      			
											       		<textarea 	rows="4" 
											       					cols="50">													
														</textarea>
														
								        			</md-input-container>
								        			
										      	</div>
										      	
										     </div>
										     
										</md-content>
											
									</div>
									
									<!-- Toolbar and content for Fixed list of values input type. -->
									<div ng-show="selectedLov.itypeId == 3">	
									
										<!-- 
											Toolbar and content for Fix LOV wizard (second panel on the
											first tab panel of the right panel.
										-->																					
										<md-toolbar class="md-blue minihead md-toolbar-tools" 
													style="margin-top:15px" >
											{{translate.load("sbi.behavioural.lov.details.fixedListWizard");}}
										</md-toolbar>
										
										<md-content>
										
											<!-- 
												Toolbar and content for Fix LOV form sub-element in the
												second panel's first tab panel.
											-->											
											<div style="margin-top:15px; margin-right:20px; margin-left:20px;">
											
												<md-toolbar class="md-blue minihead md-toolbar-tools">
													{{translate.load("sbi.behavioural.lov.details.fixLovForm");}}
												</md-toolbar>
												
												<md-content>
												 	
												 	<!-- Text field for Label of the Fix LOV -->
													<div 	layout="row" 
															layout-wrap >
													
												      	<div flex=100>
												      	
												       		<md-input-container class="small counter">
												       		
													      		<label>
													      			{{translate.load("sbi.behavioural.lov.details.fixLovGridLabelColumn");}}
												      			</label>
												      			
													       		<input 	ng-model="selectedLov.fixLovGridLabelColumn" 
													       				required
													        			maxlength="100" 
													        			ng-maxlength="100" 
													        			md-maxlength="100" > 
													        			
										        			</md-input-container>
										        			
												      	</div>
												      	
											     	</div>
												     
											     	<!-- Text field for the Description parameter of the Fix LOV. -->
													<div 	layout="row" 
															layout-wrap >
															
												      	<div flex=100>
												      	
												       		<md-input-container class="small counter">
												       		
													      		<label>
													      			{{translate.load("sbi.behavioural.lov.details.fixLovGridDescriptionColumn");}}
												      			</label>
												      			
													       		<input 	ng-model="selectedLov.fixLovGridDescriptionColumn" 
													       				required
													        			maxlength="100" 
													        			ng-maxlength="100"
												        			 	md-maxlength="100" > 
													        			
										        			</md-input-container>
										        			
												      	</div>
												      	
											     	</div>
												     
											     	<!-- 
												     	Button for adding of the Fix LOV item into
												     	the Fix LOV items grid list.
										     		-->
											     	<div 	layout="row" 
											     			layout-wrap 
											     			style="float:right" >
											     	
											     		<md-button 	type="button" 
											     					ng-click="addFixLovItemIntoGrid()" > 
															{{translate.load("sbi.behavioural.lov.details.fixLovAddItemsButton");}}																																		
														</md-button>
														
											     	</div>
											     	
												</md-content>
												
											</div>
											
											
											<!-- 
												Toolbar and content for Information sub-element in the
												second panel's first tab panel.
											-->
											<div style="margin-top:15px; margin-right:20px; margin-left:20px;">
											
												<md-toolbar class="md-blue minihead md-toolbar-tools" 
															style="margin-top:15px" >
													{{translate.load("sbi.behavioural.lov.details.fixLovInfoPanelTitle");}}
												</md-toolbar>
												
												<md-content>
													{{translate.load("sbi.behavioural.lov.details.infoPanel");}}
												</md-content>
											</div>
											
											<!-- 
												Toolbar and content for Fix LOV grid panel sub-element in the
												second panel's first tab panel.
											-->
											<div style="margin-top:15px; margin-right:20px; margin-left:20px">
											
												<md-toolbar class="md-blue minihead md-toolbar-tools" 
															style="margin-top:15px" >
													{{translate.load("sbi.behavioural.lov.details.fixLovGridPanel");}}
												</md-toolbar>
												
												<md-content>
												
													<angular-table 	layout-fill
																	id="listForFixLov"
																	ng-model="listForFixLov"
																	show-search-bar=false
																	columns='["LOV_LABEL","LOV_DESCRIPTION"]'
																	highlights-selected-item=true >						
													</angular-table>
													
												</md-content>
												
											</div>
											
										</md-content>	
										
									</div>
									
									<!-- Toolbar and content for Java class input type. -->
									<div ng-show="selectedLov.itypeId == 4">										
										
										<md-toolbar class="md-blue minihead md-toolbar-tools" 
													style="margin-top:15px" >
											{{translate.load("sbi.behavioural.lov.details.javaClassWizard");}}
										</md-toolbar>
										
										<md-content>
										
											<!-- Text field for the Description parameter. -->
											<div 	layout="row" 
													layout-wrap >
											
										      	<div flex=100>
										      	
										       		<md-input-container class="small counter">
										       		
											      		<label>
											      			{{translate.load("sbi.behavioural.lov.details.javaClassName");}}
										      			</label>
										      			
											       		<input 	ng-model="selectedLov.javaClassName" 
											       				required
											        			maxlength="100" 
											        			ng-maxlength="100" 
											        			md-maxlength="100" > 
											        			
								        			</md-input-container>
								        			
										      	</div>
										      	
										     </div>
										     
										</md-content>	
										
									</div>
									
									<!-- Toolbar and content for Dataset input type. -->
									<div ng-show="selectedLov.itypeId == 5">
																			
										<md-toolbar class="md-blue minihead md-toolbar-tools" 
													style="margin-top:15px" >
											{{translate.load("sbi.behavioural.lov.details.datasetWizard");}}
										</md-toolbar>
										
										<md-content>
										
											<!-- Combo box for Datasets. -->
										     <div layout="row" layout-wrap>
										     
									      		<div flex=100>
									      		
										       		<md-input-container class="small counter">
												       
												       	<label>
											      			{{translate.load("sbi.behavioural.lov.datasetLovFormLabel");}}
										      			</label>
										      			
												       	<md-select  aria-label="aria-label"
												        			ng-model="selectedLov.dataset" > 
										        			
										        			<md-option 	ng-repeat="dset in listOfDatasets"
										        						value="{{dset.id}}" >
								        						{{dset.label}} 
							        						</md-option>
							        						
												       	</md-select> 
												       
											       	</md-input-container>
											       	
									      		</div>
									      		
									    	</div>
									    	
										</md-content>	
										
									</div>					     
						     
								</md-tab>
								
								<md-tab label='{{translate.load("sbi.behavioural.lov.lovTestResultTabTitle")}}'>
									
								</md-tab>
								
							</md-tabs>
							
						</md-content>
						
					</div>
					
				</form>
				
			</right-col>
			
		</angular_2_col>
		
	</body>
	
</html>
