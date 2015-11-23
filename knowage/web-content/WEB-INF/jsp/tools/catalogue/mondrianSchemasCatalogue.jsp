
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
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/mondrianSchemasCatalogue.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body class="bodyStyle"
	ng-controller="mondrianSchemasCatalogueController as ctrl">

	<angular_2_col> 
<!-- /////////////// LEFT SIDE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->
	<left-col>
	
	<div class="leftBox">
	
		<md-toolbar class="md-blue minihead">
<!-- /////////////// LEFT SIDE TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->			
			<div class="md-toolbar-tools">
		
				<div>{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue")}}</div>
<!-- /////////////// ADD(PLUS) BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->		
				<md-button 	class="md-fab md-ExtraMini addButton"
							style="position:absolute; right:11px; top:0px;"
							ng-click="createDragan()"> 
						
						<md-icon 	md-font-icon="fa fa-plus" 
									style=" margin-top: 6px ;
									color: white;">
						
						</md-icon>
						 
				</md-button>
			
			</div>
		
		</md-toolbar>
		
		 	

				<md-content layout-padding
					style="background-color: rgb(236, 236, 236);"
					class="ToolbarBox miniToolbar noBorder leftListbox"> 
					
					
<!-- /////////////// CATALOGUE TABLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->					
					<angular-table 	layout-fill 
									id="Dragan" 
									ng-model="itemList"
									columns='[{"label":"NAME","name":"NAME"},{"label":"Description","name":"DESCRIPTION"}]'
									columns-search='["NAME","DESCRIPTION"]' 
									show-search-bar=true
									highlights-selected-item=true
									speed-menu-option ="catalogueSpeedOptions"
									click-function = "myAssociatedClickFunction(item)"
									
									
									> 
					</angular-table> 
			
				</md-content>

			</div>

		</left-col> 
<!-- /////////////// RIGHT SIDE     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->		
		<right-col  >
			
			<div ng-show="showMe"  >
			
			<form 	
					ng-submit="saveMondrianCatalogue()"
					class="detailBody md-whiteframe-z1" 
					novalidate>
					
<!-- /////////////// RIGHT SIDE TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->				
				<md-toolbar class= "md-blue minihead" >
				
<!-- /////////////// RIGHT SIDE TOOLBAR TOOLS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->					
					<div class="md-toolbar-tools h100">
					
<!-- /////////////// RIGHT SIDE TOOLBAR TOOLS TITLE\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						
						<div 	style="text-align: center; 
								font-size: 24px;">
								{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue");}}</div>
					
						<div style="position: absolute; right: 0px" class="h100">
						
<!-- /////////////// CANCEL BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						
						<md-button 	type="button" 
									tabindex="-1" 
									aria-label="cancel"
									class="md-raised md-ExtraMini " 
									style=" margin-top: 2px;"
									ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
								
						</md-button>

<!-- /////////////// SAVE BUTTON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						
						<md-button type="submit" 
									aria-label="save layer"
									class="md-raised md-ExtraMini " 
									style=" margin-top: 2px;">
									{{translate.load("sbi.browser.defaultRole.save");}} 
					
						</md-button>
					
					</div>
				</div>
				</md-toolbar>
			
<!-- /////////////// INPUT PART \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->								
				<md-content md-dynamic-height flex style="margin-left: 20px;" class="ToolbarBox miniToolbar noBorder">
				
<!-- /////////////// INPUT FIELD NAME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->			
					<div layout="row" layout-wrap>
      					<div flex=100>
		
				 			<md-input-container class="small counter"> 
				 
								<label>{{translate.load("sbi.ds.name");}}</label>
								<input 	ng-model="selectedMondrianSchema.NAME" 
										maxlength="100"
										ng-maxlength="100" 
										md-maxlength="100"> 
							</md-input-container> 
						</div>
     				</div>
				
				
<!-- /////////////// INPUT FIELD DESCRIPTION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div layout="row" layout-wrap>
      				<div flex=100>				
							<md-input-container class="small counter"> 
				
								<label>{{translate.load("sbi.ds.description");}}</label>
					
								<input 	ng-model="selectedMondrianSchema.DESCRIPTION" 
										maxlength="100"
										ng-maxlength="100" 
										md-maxlength="100"> 
							
								</md-input-container>
				</div>
     				</div>	
     				
<!-- /////////////// INPUT FILE UPLOAD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div flex=100>
      				<md-input-container class="small counter"> 
      				
       					<input id="mondarianSchemaFile" 
       							ng-model="selectedMondrianSchema.FILE" 
       							type="file"
       							fileread="selectedMondrianSchema.mondarianSchemaFile" 
       							accept=""> 
     				 </md-input-container>

     </div>
     				
<!-- /////////////// LOCK CHECKBOX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div layout="row" layout-wrap>
      						<div flex=3 style="line-height: 40px">
       							<label>{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.locked")}}:</label>
      						</div>
 
      						<md-input-container class="small counter"> 
      							<md-checkbox
       								ng-model="selectedBusinessModel.BUSINESSMODEL_LOCKED" 
       								aria-label="Locked"
       								disabled>
      							</md-checkbox> 
      						</md-input-container>
     					</div>
     					
     					<div layout="row" layout-wrap>
      						<div flex=3 style="line-height: 40px">
       							<label>{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.lockedBy")}}:</label>
      						</div>
 							<!-- Input label for locker -->
     					</div>
     				
<!-- /////////////// LOCK BUTTON  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div layout="row" layout-wrap>
      						<div flex=3 style="line-height: 40px">
       							<md-button type="button" class="md-raised ">
       								{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.lockModel")}}
       							</md-button>
      						</div>
     					</div>
     				
<!-- /////////////// UNLOCK BUTTON  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	

				<div layout="row" layout-wrap>
      						<div flex=3 style="line-height: 40px">
       							<md-button type="button" class="md-raised ">
       								{{translate.load("sbi.tools.catalogue.mondrianSchemasCatalogue.inputForm.unlockModel")}}
       							</md-button>
      						</div>
     					</div> 
     				
     								
<!-- /////////////// SAVED VERSION TOOLBAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->	
							
				<md-toolbar class="md-blue minihead md-toolbar-tools" >
					
					<label>{{translate.load("sbi.widgets.catalogueversionsgridpanel.title")}}</label>
						
				</md-toolbar>
				
<!-- /////////////// SCROLL FOR SAVED FILES TABLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->						
			
			
			<md-content layout-row  style = "background-color: rgb(236,236,236 );height:60%"  >
			
			
			
<!-- /////////////// SAVED FILES TABLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ -->					
						<angular-table 	layout-fill
										id="Dragan" 
										ng-model="selectedMondrianSchema.fileList"
										columns='["CREATOR","CREATION_DATE","FILE_NAME"]'
										highlights-selected-item=true
										
										> 
					
						</angular-table>
			
				
			
				
				</md-content>	
				
				</md-content>


				
	
				
				
					
			
			
			</form>	
			
			</div>
			
				
		</right-col> 
	
	</angular_2_col>

</body>
</html>