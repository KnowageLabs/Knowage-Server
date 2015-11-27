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
		
		<!-- JSP -->
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		
		<!-- Style -->
		<link rel="stylesheet" type="text/css"	href="/knowage/themes/glossary/css/generalStyle.css">
		
		<!-- Javascript -->
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/lovsManagement.js"></script>		
		
		<title>Insert title here</title>
		
	</head>
	
	<body class="bodyStyle" ng-controller="lovsManagementController as ctrl">
		
		<angular_2_col>		
		
			<!-- Left column (left panel) -->
			<left-col>					
				<div class="leftBox">									
					<md-toolbar class="md-blue minihead">
						<div class="md-toolbar-tools">							
							<div>
								{{translate.load("sbi.behavioural.lov.title");}}
							</div>	
													
							<md-button 
								class="md-fab md-ExtraMini addButton"
								style="position:absolute; right:11px; top:0px;"
								ng-click="createLov()"> 
								
								<md-icon
									md-font-icon="fa fa-plus" 
									style=" margin-top: 6px ; color: white;">
								</md-icon> 							
							</md-button>
						</div>
					</md-toolbar>
					
					<md-content layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox">
						<angular-table 
							layout-fill
							id="listOfLovs"
							ng-model="listOfLovs"
							columns='["LOV_LABEL","LOV_DESCRIPTION"]'
							columns-search='["LOV_LABEL","LOV_DESCRIPTION"]'
							show-search-bar=true
							highlights-selected-item=true
							menu-option = lovsManagementSpeedMenu		
						>						
						</angular-table>
					</md-content>									
				</div>				
			</left-col>
			
			<!-- Right column (right panel) -->
			<right-col>
				<form layout-fill ng-submit="saveLov()" class="detailBody md-whiteframe-z1" novalidate>
					<div ng-show="showMe">
						<md-toolbar class="md-blue minihead">
							<div class="md-toolbar-tools h100">
								<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.behavioural.lov.title");}}</div>
								
								<div style="position: absolute; right: 0px" class="h100">
									<md-button type="button" tabindex="-1" aria-label="cancel"
										class="md-raised md-ExtraMini " style=" margin-top: 2px;"
										ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
									</md-button>
									
									<md-button 	type="submit"
												aria-label="save layer" 
												class="md-raised md-ExtraMini "
												style="margin-top: 2px;"
									>
										{{translate.load("sbi.browser.defaultRole.save");}} 
									</md-button>
								</div>
							</div>
						</md-toolbar>
						
						<md-content flex style="margin-left: 20px;" class="ToolbarBox miniToolbar noBorder">
						
							<md-tabs md-dynamic-height>
							
								<md-tab label='{{translate.load("sbi.behavioural.lov.lovFormTabTitle")}}' md-active=true>
								
									<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
										{{translate.load("sbi.behavioural.lov.details.wizardUpper");}}
									</md-toolbar>
									
									<md-content flex>
										
										<!-- Text field for Label -->
										<div layout="row" layout-wrap>
									      	<div flex=100>
									       		<md-input-container class="small counter">
										      		<label>
										      			{{translate.load("sbi.behavioural.lov.details.label");}}
									      			</label>
										       		<input 	ng-model="selectedLov.label" required
										        			maxlength="100" ng-maxlength="100" md-maxlength="100"> 
							        			</md-input-container>
									      	</div>
									     </div>
									     
									     <!-- Text field for Name -->
										<div layout="row" layout-wrap>
									      	<div flex=100>
									       		<md-input-container class="small counter">
										      		<label>
										      			{{translate.load("sbi.behavioural.lov.details.name");}}
									      			</label>
										       		<input 	ng-model="selectedLov.name" required
										        			maxlength="100" ng-maxlength="100" md-maxlength="100"> 
							        			</md-input-container>
									      	</div>
									     </div>
									     
									     <!-- Text field for Description -->
										<div layout="row" layout-wrap>
									      	<div flex=100>
									       		<md-input-container class="small counter">
										      		<label>
										      			{{translate.load("sbi.behavioural.lov.details.description");}}
									      			</label>
										       		<input 	ng-model="selectedLov.description" required
										        			maxlength="100" ng-maxlength="100" md-maxlength="100"> 
							        			</md-input-container>
									      	</div>
									     </div>
									     
									     <!-- Combo box for Input type -->
									     <div layout="row" layout-wrap>
								      		<div flex=100>
									       		<md-input-container class="small counter">
											       	<label>
										      			{{translate.load("sbi.behavioural.lov.details.inputType");}}
									      			</label>
											       	<md-select  aria-label="aria-label"
											        			ng-model="selectedLov.inputTypeId"> <md-option
											        			ng-repeat="it in listOfInputTypes" value="{{it.VALUE_ID}}">{{it.VALUE_NM}} </md-option>
											       </md-select> 
										       </md-input-container>
										      </div>
									     </div>
						     
									</md-content>
									
									<!-- Toolbar and content for Query input type -->
									<div ng-show="selectedLov.inputTypeId==1">									
										
										<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
											{{translate.load("sbi.behavioural.lov.details.queryWizard");}}
										</md-toolbar>
										
										<md-content>										
											<!-- Combo box for Data source label -->
										     <div layout="row" layout-wrap>
									      		<div flex=100>
										       		<md-input-container class="small counter">
												       
												       	<label>
											      			{{translate.load("sbi.behavioural.lov.details.dataSourceLabel");}}
										      			</label>
										      			
												       	<md-select  aria-label="aria-label"
												        			ng-model="selectedLov.dataSource"> 
										        			
										        			<md-option 	ng-repeat="ds in listOfDatasources"
										        						value="{{ds.DATASOURCE_ID}}">{{ds.DATASOURCE_LABEL}} 
							        						</md-option>
							        						
												       	</md-select> 
												       
											       	</md-input-container>
									      		</div>
									    	</div>
										     
									      	<!-- Text field for Description -->
											<div layout="row" layout-wrap>
										      	<div flex=100>
										       		<md-input-container class="small counter">
											      		<label>
											      			{{translate.load("sbi.behavioural.lov.details.queryDescription");}}
										      			</label>
											       		<textarea rows="4" cols="50">													
														</textarea>
								        			</md-input-container>
										      	</div>
										     </div>
										
										</md-content>
									
									</div>
									
									<!-- Toolbar and content for Script input type -->	
									<div ng-show="selectedLov.inputTypeId==2">										
										<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
											{{translate.load("sbi.behavioural.lov.details.scriptWizard");}}
										</md-toolbar>
										
										<md-content>
											<!-- Combo box for Script type -->
										     <div layout="row" layout-wrap>
									      		<div flex=100>
										       		<md-input-container class="small counter">
												       
												       	<label>
											      			{{translate.load("sbi.behavioural.lov.details.dataSourceLabel");}}
										      			</label>
										      			
												       	<md-select  aria-label="aria-label"
												        			ng-model="selectedLov.scriptType"> 
										        			
										        			<md-option 	ng-repeat="st in listOfScriptTypes"
										        						value="{{st.VALUE_CD}}">{{st.VALUE_NM}} 
							        						</md-option>
							        						
												       	</md-select> 
												       
											       	</md-input-container>
									      		</div>
									    	</div>
										     
									      	<!-- Text field for Description -->
											<div layout="row" layout-wrap>
										      	<div flex=100>
										       		<md-input-container class="small counter">
											      		<label>
											      			{{translate.load("sbi.behavioural.lov.details.scriptDescription");}}
										      			</label>
											       		<textarea rows="4" cols="50">													
														</textarea>
								        			</md-input-container>
										      	</div>
										     </div>
										</md-content>	
									</div>
									
									<!-- Toolbar and content for Fixed list of values input type -->
									<div ng-show="selectedLov.inputTypeId==3">	
									
										<!-- 
											Toolbar and content for Fix LOV wizard (second panel on the
											first tab panel of the right panel.
										-->
												
										<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
											{{translate.load("sbi.behavioural.lov.details.fixedListWizard");}}
										</md-toolbar>
										
										<md-content>
										
											<!-- 
												Toolbar and content for Fix LOV form sub-element in the
												second panel's first tab panel.
											-->
											
											<div style="margin-top:15px;margin-right:20px;margin-left:20px;">
											
												<md-toolbar class="md-blue minihead md-toolbar-tools">
													{{translate.load("sbi.behavioural.lov.details.fixLovForm");}}
												</md-toolbar>
												
												<md-content>
												 	<!-- Text field for Label of the Fix LOV -->
													<div layout="row" layout-wrap>
												      	<div flex=100>
												       		<md-input-container class="small counter">
													      		<label>
													      			{{translate.load("sbi.behavioural.lov.details.fixLovGridLabelColumn");}}
												      			</label>
													       		<input 	ng-model="selectedLov.fixLovGridLabelColumn" required
													        			maxlength="100" ng-maxlength="100" md-maxlength="100"> 
										        			</md-input-container>
												      	</div>
											     	</div>
												     
											     	<!-- Text field for Description of the Fix LOV -->
													<div layout="row" layout-wrap>
												      	<div flex=100>
												       		<md-input-container class="small counter">
													      		<label>
													      			{{translate.load("sbi.behavioural.lov.details.fixLovGridDescriptionColumn");}}
												      			</label>
													       		<input 	ng-model="selectedLov.fixLovGridDescriptionColumn" required
													        			maxlength="100" ng-maxlength="100" md-maxlength="100"> 
										        			</md-input-container>
												      	</div>
											     	</div>
												     
											     	<!-- 
												     	Button for adding of the Fix LOV item into
												     	the Fix LOV items grid list.
										     		-->
											     	<div layout="row" layout-wrap style="float:right">
											     		<md-button type="button" ng-click="addFixLovItemIntoGrid()" > 
															{{translate.load("sbi.behavioural.lov.details.fixLovAddItemsButton");}}																																		
														</md-button>
											     	</div>
												</md-content>
											</div>
											
											
											<!-- 
												Toolbar and content for Information sub-element in the
												second panel's first tab panel.
											-->
											<div style="margin-top:15px;margin-right:20px;margin-left:20px;">
											
												<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
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
											<div style="margin-top:15px;margin-right:20px;margin-left:20px">
												<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
													{{translate.load("sbi.behavioural.lov.details.fixLovGridPanel");}}
												</md-toolbar>
												
												<md-content>
													<angular-table 
														layout-fill
														id="listForFixLov"
														ng-model="listForFixLov"
														show-search-bar=false
														columns='["LOV_LABEL","LOV_DESCRIPTION"]'
														highlights-selected-item=true
													>						
													</angular-table>
												</md-content>
											</div>
											
										</md-content>	
									</div>
									
									<!-- Toolbar and content for Java class input type -->
									<div ng-show="selectedLov.inputTypeId==4">										
										<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
											{{translate.load("sbi.behavioural.lov.details.javaClassWizard");}}
										</md-toolbar>
										
										<md-content>
											<!-- Text field for Description -->
											<div layout="row" layout-wrap>
										      	<div flex=100>
										       		<md-input-container class="small counter">
											      		<label>
											      			{{translate.load("sbi.behavioural.lov.details.javaClassName");}}
										      			</label>
											       		<input 	ng-model="selectedLov.javaClassName" required
											        			maxlength="100" ng-maxlength="100" md-maxlength="100"> 
								        			</md-input-container>
										      	</div>
										     </div>
										</md-content>	
									</div>
									
									<!-- Toolbar and content for Dataset input type -->
									<div ng-show="selectedLov.inputTypeId==5">										
										<md-toolbar class="md-blue minihead md-toolbar-tools" style="margin-top:15px;">
											{{translate.load("sbi.behavioural.lov.details.datasetWizard");}}
										</md-toolbar>
										
										<md-content>
											<!-- Combo box for Datasets -->
										     <div layout="row" layout-wrap>
									      		<div flex=100>
										       		<md-input-container class="small counter">
												       
												       	<label>
											      			{{translate.load("sbi.behavioural.lov.datasetLovFormLabel");}}
										      			</label>
										      			
												       	<md-select  aria-label="aria-label"
												        			ng-model="selectedLov.dataset"> 
										        			
										        			<md-option 	ng-repeat="dset in listOfDatasets"
										        						value="{{dset.id}}">{{dset.label}} 
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