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
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.tools.dataset.federation.FederationDefinition"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="businessModelCatalogueModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<!-- <link rel="stylesheet" type="text/css"	href="/knowage/themes/glossary/css/generalStyle.css"> -->

<!-- <link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css"> -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/businessModelCatalogue.js"></script>

		<!-- Retrieveing datasets used in creating a federation definition, as well as the whole relationships column -->
		<%
			String user = "";
			if(userName!=null){
				user = userName;
			}
		%>
		
		<!-- Making lisOfDSL and relString avaliable for use in federatedDataset.js -->
		<script>
			var valueUser = '<%= user  %>';
		</script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body  class="bodyStyle businessModelCatalog" ng-controller="businessModelCatalogueController as ctrl">
	<angular-list-detail show-detail="showMe">
		<list label='translate.load("sbi.tools.catalogue.metaModelsCatalogue")' new-function="createBusinessModel"> 
			
<!-- 				<md-toolbar class="header"> -->
<!-- 					<div class="md-toolbar-tools"> -->
<!-- 						<div style="font-size: 24px;">{{translate.load("sbi.tools.catalogue.metaModelsCatalogue");}}</div> -->
						
<!-- 						<md-button  -->
<!--     						ng-disabled=false -->
<!--     						class="md-fab md-ExtraMini" -->
<!--     						style="position:absolute; right:26px; top:0px; background-color:#E91E63" -->
<!--     						ng-click="deleteBusinessModels()">  -->
    						
<!--     						<md-icon -->
<!--         						md-font-icon="fa fa-trash"  -->
<!--         						style=" margin-top: 6px ; color: white;" > -->
<!--        						</md-icon>  -->
<!-- 						</md-button> -->
						
<!-- 						<md-button  -->
<!-- 							class="md-fab md-ExtraMini addButton" -->
<!-- 							style="position:absolute; right:11px; top:0px;" -->
<!-- 							ng-click="createBusinessModel()" -->
<!-- 							aria-label="create"> -->
<!-- 							<md-icon -->
<!-- 								md-font-icon="fa fa-plus"  -->
<!-- 								style=" margin-top: 6px ; color: white;"> -->
<!-- 							</md-icon>  -->
<!-- 						</md-button> -->
<!-- 					</div> -->
<!-- 				</md-toolbar> -->
				<div layout-align="space-around" layout="row" style="height:100%" ng-show="bmLoadingShow">
     				<md-progress-circular 
        	 			class=" md-hue-4"
        				md-mode="indeterminate" 
        				md-diameter="70"       
        				style="height:100%;">
      				</md-progress-circular>
      			</div> 
				

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
					>						
					</angular-table>


				
		
		</list>
		
		<detail label='selectedBusinessModel.name==undefined? "" : selectedBusinessModel.name'  save-function="saveBusinessModel"
		cancel-function="cancel"
		disable-save-button="!isDirty"
		show-save-button="showMe" show-cancel-button="showMe">
		
		
		
          <form id="businessModelForm">
          <md-card>
	        <md-card-content>
		        <md-input-container class="small counter">
					<label>{{translate.load("sbi.ds.name")}}</label>
					<input ng-change="checkChange()" ng-model="selectedBusinessModel.name" required
						 ng-maxlength="100"> 
				</md-input-container>
				
				<md-input-container class="small counter">
					<label>{{translate.load("sbi.ds.description")}}</label>
					<input ng-model="selectedBusinessModel.description"
						ng-maxlength="100" ng-change="checkChange()"> 
				</md-input-container>
				
				<md-input-container class="small counter"> 
					<label>{{translate.load("sbi.ds.catType")}}</label>
				   <md-select  aria-label="aria-label"
				    ng-model="selectedBusinessModel.category" ng-change="checkChange()"> <md-option
				    ng-repeat="c in listOfCategories" value="{{c.VALUE_ID}}">{{c.VALUE_NM}} </md-option>
				   </md-select> 
				</md-input-container>
				
				<md-input-container class="small counter"> 
					<label>{{translate.load("sbi.ds.dataSource")}}</label>
				       <md-select  aria-label="aria-label"
				        ng-model="selectedBusinessModel.dataSourceLabel" ng-change="checkChange()"> <md-option
				        ng-repeat="d in listOfDatasources" value="{{d.DATASOURCE_LABEL}}">{{d.DATASOURCE_LABEL}} </md-option>
				       </md-select> 
				</md-input-container>
				
				<div layout="row" layout-wrap>
						<label layout-align="center center" class="buttonLabel">{{translate.load("sbi.ds.file.upload.button")}}:</label>
      				<file-upload  ng-model="fileObj" id="businessModelFile" ng-click="fileChange();checkChange()" flex></file-upload>
      				
				</div>
				
			</md-card-content>
	      </md-card>
	      <md-card>
	      <md-card-title>
	      	<md-card-title-text>
	      	<md-toolbar class="secondaryToolbar">
		      <div class="md-toolbar-tools">
		        <h2>
		          <span>Metadata</span>
		        </h2>
		   
		      </div>
		    </md-toolbar>
          	</md-card-title-text>
	      </md-card-title>
	      <md-card-content>
	      	<div layout="column" layout-margin>
		      	<div  layout="row">
					<md-button ng-click="importMetadata(selectedBusinessModel.id)"  ng-disabled="bmImportingShow" class="md-raised md-ExtraMini">{{translate.load("sbi.tools.catalogue.metaModelsCatalogue.import.metadata");}}</md-button>
					<md-button ng-click="downloadCWMFile(selectedBusinessModel.id)" ng-disabled="bmCWMProcessingShow" class="md-raised" >{{translate.load("sbi.metadata.cwm.export.button")}}</md-button></div>
				</div>
				<div   ng-show="bmImportingShow" >
    				<md-progress-linear md-mode="indeterminate"></md-progress-linear>
    				<div class="bottom-block">
				      <span>Importing metadata</span>
				    </div>
      			</div> 
      			<div   ng-show="bmCWMProcessingShow" >
    				<md-progress-linear md-mode="indeterminate"></md-progress-linear>
    				<div class="bottom-block">
				      <span>Exporting CWM</span>
				    </div>
      			</div> 
	      	</div>
	      	 
	      	 <md-divider layout-margin></md-divider>
	      	<div layout="row" layout-wrap>
				<label flex="20" layout-align="center center" class="buttonLabel">{{translate.load("sbi.metadata.cwm.import.file.upload")}}:</label>
		        <file-upload flex="50" ng-model="fileObjCWM" id="cwmFile" ng-click="fileCWMChange();checkCWMChange()"></file-upload>
            	<div flex="30">
               		 <md-button  ng-click="importCWMFile(selectedBusinessModel.id)" ng-disabled="bmCWMImportingShow" class="md-raised">{{translate.load("sbi.metadata.cwm.import.button")}}</md-button>
            	</div>
               	

                                     						 
      		</div>
      		<div   ng-show="bmCWMImportingShow" >
    				<md-progress-linear md-mode="indeterminate"></md-progress-linear>
    				<div class="bottom-block">
				      <span>Importing CWM</span>
				    </div>
      			</div> 
	      </md-card-content>
	      </md-card>
	      
	      
	      <md-card layout="column">
	      <md-card-title>
	      	<md-card-title-text>
	      	<md-toolbar class="secondaryToolbar">
		      <div class="md-toolbar-tools">
		        <h2>
		          <span>{{translate.load("sbi.widgets.catalogueversionsgridpanel.title")}}</span>
		        </h2>
		   
		      </div>
		    </md-toolbar>
          	</md-card-title-text>
	      </md-card-title>
	      <md-card-content layout="column">
	      	
				<md-radio-group ng-model="bmVersionsActive" ng-change="checkChange()">
				
				<angular-table
					ng-show="!versionLoadingShow"
					id="bmVersions_id"
					ng-model="bmVersions"
					columns='[
						{"label":"ACTIVE","name":"ACTION", "size":"60px"},
						{"label":"FILE NAME","name":"fileName"},
						{"label":"CREATOR","name":"creationUser"},
						{"label":"CREATION DATE","name":"creationDate"}
						]'
					columns-search='["creationUser","creationDate"]'
					show-search-bar=false
					selected-item="selectedVersions"
					highlights-selected-item=true
					speed-menu-option="bmSpeedMenu2"	
					no-pagination=false	
					click-function="clickRightTable(item)"								
				>						
				</angular-table>
				</md-radio-group>
						
	      </md-card-content>
	      </md-card>
	      </form>
     					

        

			
				
			
		</detail>
	</angular-list-detail>
</body>
</html>
