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


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/servermanager/glossaryImportExport/importExportGlossaryController.js")%>"></script>



<%-- breadCrumb --%>
<script type="text/javascript" 
		src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

</head>
<body class="bodyStyle kn-importExportDocument" ng-app="impExpGlossary" >
<rest-loading></rest-loading>
	<div ng-controller="glossaryImportController " layout="column" layout-fill layout-wrap class="contentdemoBasicUsage">
	
	
		<md-toolbar class="miniheadimportexport" >
			<div class="md-toolbar-tools">
				<i class="fa fa-exchange fa-2x"></i>
				<h2 class="md-flex" >
					{{translate.load("sbi.impexpglossary.importexportglossary")}}
				</h2>
			</div>
		</md-toolbar>

		<md-content flex layout-wrap class="mainContainer">
			<md-tabs layout-fill class="absolute">
				<md-tab	>
					<md-tab-label>{{translate.load("sbi.impexpglossary.export")}}</md-tab-label>
					<md-tab-body>
					<md-card>
						<md-content>
						<div layout="row" layout-wrap >
							<div flex >
								<md-input-container class="md-block"> <label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport" required
									maxlength="100" ng-maxlength="100" md-maxlength="100" /> </md-input-container>
							</div>
							<div>
								<md-input-container class="small counter"> 
								<md-button	ng-show="!wait" ng-click="prepare($event)"
									aria-label="download Glossary" class="md-fab md-mini"> <md-icon
									md-font-icon="fa fa-download fa-2x"> </md-icon> </md-button>
								</md-input-container>
							</div>
						</div>
						<div  layout-padding layout-gt-sm="row"
								layout-align-gt-sm="start center" layout-sm="column">
								<h4>{{translate.load("sbi.impexpgloss.filtergloss")}}:</h4>
								<md-datepicker ng-model="filterDate" md-placeholder="Enter date"></md-datepicker>
								<md-button class="md-icon-button" ng-click="filterGlossary()">
							 		<md-icon md-font-icon="fa fa-filter" aria-label="Filter"></md-icon>
								 </md-button>
								  <md-button class="md-icon-button" ng-click=removeFilter()>
							 		<md-icon md-font-icon="fa fa-times" aria-label="Remove Filter"></md-icon>
								 </md-button>
							</div>
						<div id="lista">
							<div layout="row" layout-wrap>
								<div >
									<md-checkbox  ng-checked="flagCheck" ng-click="selectAll()"><h4>{{translate.load("sbi.importusers.selectall");}}</h4></md-checkbox>
								</div>
								</div>
								<div layout="row" layout-wrap flex>
									<div flex="90" ng-repeat="us in glossary">
										<md-checkbox ng-checked="exists(us, glossarySelected)"
										ng-click="toggle(us, glossarySelected)"> {{us.GLOSSARY_NM}} </md-checkbox>
									</div>
								</div>
						</div>
					
					</md-content>
				</md-card>
				</md-tab-body> 
			</md-tab> 
				
			<md-tab id="importTab" > 
				<md-tab-label>{{translate.load("sbi.impexpglossary.import")}}</md-tab-label>
					<md-tab-body> 
						<md-card>
						<div layout="row" layout-wrap >
								<file-upload flex id="AssociationFileUploadImport" ng-model="importFile" file-max-size="<%=importFileMaxSizeMB%>" ></file-upload>
								<md-button ng-click="upload($event)" aria-label="upload Menu"
								class="md-fab md-mini"  > <md-icon
								md-font-icon="fa fa-upload"  >
								</md-icon> </md-button>
						</div>
						<div layout="row" layout-wrap ng-show="glossaryImported.length!=0 || importingGlossary.length!=0">
								<md-radio-group layout="row" ng-model="typeSaveMenu">
								      <md-radio-button value="Override" >{{translate.load("sbi.importusers.override");}}</md-radio-button>
								      <md-radio-button value="Missing">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
								 </md-radio-group>
								
								<span flex></span>
								<md-button ng-disabled="importingGlossary.length==0" class="md-raised" ng-click="save($event)" aria-label="upload Menu" >{{translate.load("sbi.importusers.startimport");}}</md-button>
						</div>
							
						<div layout="row" flex>
							<div ng-show="glossaryImported.length!=0 || importingGlossary.length!=0" flex >
							<h4>{{translate.load("sbi.importusers.userimport");}}</h4>
								 <angular-table id='layerlist' 
									ng-model=glossaryImported
									columns='[{"label":"","name":"glossaryNm"}]'
									columnsSearch='["glossaryNm"]' 
									show-search-bar=true
									highlights-selected-item=true 
									hide-table-head=true
									menu-option=menuLayer 
									multi-select=true
									selected-item=selectGlossaryToImport
									no-pagination=true
									scope-functions=tableFunction>
									</angular-table> 
									
							</div>
							<div layout="column" layout-wrap  ng-show ="glossaryImported.length!=0 || importingGlossary.length!=0">
								<div flex></div>
									 	<md-button  class="md-fab md-mini"  ng-click="addGloss()"><md-icon
											md-font-icon="fa fa-angle-right fa-2x"  >
										</md-icon></md-button>
									 	<md-button  class="md-fab md-mini"  ng-click="removeGloss()" ><md-icon
											md-font-icon="fa fa-angle-left fa-2x" >
										</md-icon></md-button>
									<div flex></div>
									 	<md-button  class="md-fab md-mini"  ng-click="addAllGloss()" ><md-icon
											md-font-icon="fa fa-angle-double-right fa-2x"  >
										</md-icon></md-button>
									 	<md-button class="md-fab md-mini"  ng-click="removeAllGloss()"><md-icon
											md-font-icon="fa fa-angle-double-left fa-2x"  >
										</md-icon></md-button>
									<div flex></div>
								 </div>
								<div flex ng-show="glossaryImported.length!=0 || importingGlossary.length!=0">
									<h4>{{translate.load("sbi.importusers.userimporting");}}</h4>
									<angular-table  id='layerlist2' 
									ng-model=importingGlossary
									columns='[{"label":"","name":"glossaryNm"}]'
									columnsSearch='["glossaryNm"]' 
									show-search-bar=true
									highlights-selected-item=true 
									menu-option=menuLayer 
									multi-select=true
									selected-item=selectGlossaryToImport
									no-pagination=true
									scope-functions=tableFunction
									hide-table-head=true>
								
								
								</div>
							</div>
							
						</div>
						</md-card>
					</md-tab-body>
				</md-tab>
			</md-tabs> 
		</md-content>
	</div>
</body>
</html>
