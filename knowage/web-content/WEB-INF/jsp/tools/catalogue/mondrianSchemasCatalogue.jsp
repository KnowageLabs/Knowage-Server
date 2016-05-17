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
<html ng-app="mondrianSchemasCatalogueModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<!-- <link rel="stylesheet" type="text/css" -->
<!-- 	href="/knowage/themes/glossary/css/generalStyle.css"> -->
<!-- <link rel="stylesheet" type="text/css" -->
<!--  href="/knowage/themes/catalogue/css/catalogue.css"> -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">


<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/mondrianSchemasCatalogue.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body class="bodyStyle kn-layerCatalogue"
	ng-controller="mondrianSchemasCatalogueController as ctrl">

		<angular-list-detail show-detail="showMe">
<!-- /////////////// LEFT SIDE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->
	<list label='translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue")' new-function="createMondrianSchema"> 
	
	
	
<!-- 		<md-toolbar class=" header"> -->
<!--  /////////////// LEFT SIDE TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ 	 -->
<!-- 			<div class="md-toolbar-tools"> -->
		
<!-- 				<div style="font-size: 24px;">{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue")}}</div> -->
 <!-- /////////////// ADD(PLUS) BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->		
<!-- 				<md-button 	class="md-fab md-ExtraMini addButton" -->
<!-- 							style="position:absolute; right:11px; top:0px;" -->
<!-- 							ng-click="createMondrianSchema()" -->
<!-- 							aria-label="create" -->
<!-- 							>  -->
							
<!-- 						<md-icon 	md-font-icon="fa fa-plus"  -->
<!-- 									style=" margin-top: 6px ; -->
<!-- 									color: white;"> -->
						
<!-- 						</md-icon> -->
						 
<!-- 				</md-button> -->
				

			
<!-- 			</div> -->
			
<!-- 		</md-toolbar> -->
		
		 	
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
		disable-save-button="false"
		show-save-button="showMe" show-cancel-button="showMe">
			
			<div layout-fill class="containerDiv">
			
			<form 	layout-fill class="detailBody md-whiteframe-z1" >
			
					
					
<!-- /////////////// RIGHT SIDE TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->				
<!-- 				<md-toolbar class= "header" > -->
				
<!--  /////////////// RIGHT SIDE TOOLBAR TOOLS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->					 
<!-- 					<div class="md-toolbar-tools h100"> -->
					
<!--  /////////////// RIGHT SIDE TOOLBAR TOOLS TITLE\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						 
<!-- 						<div 	style="text-align: center;  -->
<!-- 								font-size: 24px;"> -->
<!-- 								{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue");}}</div> -->
					
<!-- 						<div style="position: absolute; right: 0px" class="h100"> -->
						
<!--  /////////////// CANCEL BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						 
<!-- 						<md-button id="cancel" type="button" -->
<!-- 								aria-label="cancel" class="md-raised md-ExtraMini rightHeaderButtonBackground" -->
<!-- 								style=" margin-top: 2px;" -->
<!-- 								ng-click="cancel()"> -->
<!-- 								{{translate.load("sbi.generic.cancel");}}  -->
<!-- 						</md-button> -->

 <!-- /////////////// SAVE BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						 

<!-- 						<md-button  type="submit" -->
<!-- 							aria-label="save_constraint" class="md-raised md-ExtraMini rightHeaderButtonBackground" -->
<!-- 							style=" margin-top: 2px;" -->
<!-- 							ng-click="saveMondrianCatalogue()" -->
<!-- 							ng-disabled = "selectedMondrianSchema.modelLocked"> -->
<!-- 						{{translate.load("sbi.browser.defaultRole.save")}} -->
<!-- 						</md-button> -->
		
<!-- 					</div> -->
<!-- 				</div> -->
<!-- 				</md-toolbar> -->
			
<!-- /////////////// INPUT PART \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->								
				
				
				
<!-- /////////////// INPUT FIELD NAME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->			
					<div layout="row" layout-wrap>
      					<div flex>
		
				 			<md-input-container class="small counter"> 
				 
								<label>{{translate.load("sbi.ds.name");}}</label>
								<input 	ng-model="selectedMondrianSchema.name" 
										required
										ng-maxlength="100" 
										ng-disabled = "selectedMondrianSchema.modelLocker"
										> 
							</md-input-container> 
						</div>
     				</div>
				
				
<!-- /////////////// INPUT FIELD DESCRIPTION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div layout="row" layout-wrap >
      				<div flex>				
							<md-input-container class="small counter"> 
				
								<label>{{translate.load("sbi.ds.description");}}</label>
					
								<input 	ng-model="selectedMondrianSchema.description" 
										ng-maxlength="100" 
										ng-disabled = "selectedMondrianSchema.modelLocker"
										> 
							
								</md-input-container>
				</div>
     				</div>	
     				
<!-- /////////////// INPUT FILE UPLOAD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				
							
						<div layout="row" layout-wrap  >
							
								<div style="margin-top: 15px">
								<label >{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.fileUpload");}}</label>	
							</div>
							<div flex style="margin-right: 32px">	
       						<file-upload ng-model="file" id="myId" label='browse' ng-disabled = "selectedMondrianSchema.modelLocker" ></file-upload>
      					
							
							</div>
							
						</div>
       					
      				
					 		
      				
					
     			
     				
<!-- /////////////// LOCK UNLOCK BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div  layout-wrap >

      							<div  	style="line-height: 40px;margin: 2px;" 
      									ng-hide="selectedMondrianSchema.modelLocked">
      									
      								<md-button 	class="md-fab md-Mini "style="
											background-color: #3b678c;"
      										aria-label="unlock"
										> 
						
										<md-icon 	md-font-icon="fa fa-unlock fa-lg" 
											style=" margin-top: 6px ;
											color: white;">
						
										</md-icon>
						 
								</md-button>
       							
      						</div>
      							
      							
      							<div  style="line-height: 40px;margin: 2px;" ng-show="selectedMondrianSchema.modelLocked">
      								<md-button 	class="md-fab md-Mini 
      								"style="background-color: #3b678c;"
      								aria-label="lock"
      									
										ng-click="unlockModel()"> 
						
										<md-icon 	md-font-icon="fa fa-lock fa-lg" 
											style=" margin-top: 6px ;
											color: white;">
						
										</md-icon>
						 
								</md-button>
       							<label>{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.lockedBy")}}:</label>
       							<label>{{selectedMondrianSchema.modelLocker}}</label>
      						</div>
      						
     					</div>
     					
	
<!-- /////////////// SAVED VERSION TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	
							
				<md-toolbar class="header" >
					
					<label>{{translate.load("sbi.widgets.catalogueversionsgridpanel.title")}}</label>
						
				</md-toolbar>
				
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
						                flex
						                
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
			
				
				


				
	
				
				
					
			
			
			</form>	
			
		 </div>	
			
				
		</right-col> 
	
	</angular-list-detail>

</body>
</html>
