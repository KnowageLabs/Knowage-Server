<%--
	Knowage, Open Source Business Intelligence suite
	Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
	
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
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/src/angular_1.4/tools/servermanager/menuImportExport/importExportMenuController.js ")%>"></script>
      <%-- breadCrumb --%>
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/src/angular_1.4/tools/commons/BreadCrumb.js ")%>"></script>
      <link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, " themes/commons/css/customStyle.css ")%>">
   </head>
   <body class="bodyStyle kn-importExportDocument" ng-app="importExportMenuModule">
      <rest-loading></rest-loading>
      <div ng-controller="importExportMenuController " layout="column" layout-fill layout-wrap class="contentdemoBasicUsage">
         <md-toolbar class="miniheadimportexport">
            <div class="md-toolbar-tools">
               <i class="fa fa-exchange fa-2x"></i>
               <h2 class="md-flex">
                  {{translate.load("SBISet.impexp.menu.title", "component_impexp_messages")}}
               </h2>
            </div>
         </md-toolbar>
         <md-content flex layout-wrap class="mainContainer">
            <md-tabs layout-fill class="absolute">
               <md-tab>
                  <md-tab-label>{{translate.load("SBISet.export", "component_impexp_messages")}}</md-tab-label>
                  <md-tab-body>
                     <md-card>
                        <md-content ng-controller="exportController">
                           <div layout="column" layout-padding layout-wrap>
                              <div layout="row" layout-align="center center">
                                 <md-input-container flex="90" class="md-block">
                                    <label>{{translate.load('SBISet.importexport.nameExp', 'component_impexp_messages')}}</label>
                                    <input type="text" ng-model="exportName" required>
                                 </md-input-container>
                                 <md-button ng-if="!flags.waitExport" ng-click="exportFiles(selected)" aria-label="{{translate.load('SBISet.importexport.fileArchive', 'component_impexp_messages')}}" ng-disabled="exportName===undefined || exportName.length == 0" class="md-fab md-mini">
                                    <md-icon md-font-icon="fa fa-download">
                                    </md-icon>
                                 </md-button>
                              </div>
                           </div>
						   <div layout="row" layout-wrap >
							   <span flex></span>
							   <md-button class="md-raised" ng-if="tree.length > 0" ng-click="selectAll()" aria-label="upload Menu" >{{translate.load('SBISet.importexport.selectall','component_impexp_messages');}}</md-button>
							   <md-button class="md-raised" ng-if="tree.length > 0" ng-click="deselectAll()" aria-label="upload Menu" >{{translate.load('SBISet.importexport.deselectall','component_impexp_messages');}}</md-button>
						   </div>
                           <div layout-padding>
      						<h4>{{translate.load("SBISet.importexport.currentDatabase","component_impexp_messages");}}</h4>
                           <treecontrol 
								class="tree-classic knowage-theme" 
								tree-model="tree"
								expanded-nodes="expandedNodes"
								disabled-nodes="disabledNodes"
								selected-nodes="selectedNodes"
								options="treeOptions"
								on-selection="showSelected(node, selected)">
								{{node.name}}
					      </treecontrol>
                           </div>
                        </md-content>
                     </md-card>
                  </md-tab-body>
               </md-tab>
               <md-tab id="importTab">
                  <md-tab-label>{{translate.load("SBISet.import","component_impexp_messages");}}</md-tab-label>
                  <md-tab-body ng-controller="importController">
                    <md-toolbar class="ternaryToolbar" flex="nogrow">
                           <div class="md-toolbar-tools noPadding" layout="row">
                              <bread-crumb flex ng-model='stepItem' item-name='name' selected-index='selectedStep' control='stepControl'>
                              </bread-crumb>
                           </div>
                     </md-toolbar>
                     <md-card ng-if="selectedStep == 0">
                        <div layout="row" layout-wrap layout-align="center center">
                           <file-upload flex id="AssociationFileUploadImport" ng-model="importFile" file-max-size="<%=importFileMaxSizeMB%>"></file-upload>
                           <md-button ng-click="upload($event)" aria-label="upload Menu" class="md-fab md-mini">
                              <md-icon md-font-icon="fa fa-upload">
                              </md-icon>
                           </md-button>
                        </div>
                        <!-- 2020/02/05 Temporary disabled because not expected. -->
                        <div ng-if="false" layout-padding class="associations-container">
                           <md-radio-group ng-model="IEDConf.associations">
                              <md-radio-button value="noAssociations " class="md-primary">{{translate.load("impexp.withoutAss","component_impexp_messages");}}</md-radio-button>
                              <!--  <md-radio-button value="mandatoryAssociations">{{translate.load("impexp.mandatoryAss","component_impexp_messages");}}</md-radio-button>-->
                              <md-radio-button value="defaultAssociations">{{translate.load("impexp.defaultAss","component_impexp_messages");}}</md-radio-button>
                           </md-radio-group>
                           <div layout-padding layout="column" layout-wrap ng-if="IEDConf.associations != 'noAssociations' ">
                              <div layout-xs="column" layout-align-xs="center stretch" layout="row" layout-align="start center">
                                 <md-input-container flex class="md-block">
                                    <label>{{translate.load("impexp.savedAss","component_impexp_messages");}}</label>
                                    <input type="text" ng-model="IEDConf.fileAssociation.name" ng-disabled="true" aria-label="{{translate.load('impexp.savedAss','component_impexp_messages');}}">
                                 </md-input-container>
                                 <md-button class="md-fab md-mini" ng-click="listAssociation()" aria-label="{{translate.load('impexp.listAssFile','component_impexp_messages')}}">
                                    <md-icon class="fa fa-search fa-2x"></md-icon>
                                 </md-button>
                              </div>
                           </div>
                           <div class="downloadDiv">
                              <h3 ng-if="IEDConf.associationsFileName!=''" class="md-body-2">
                                 <span>{{translate.load("Sbi.downloadAss","component_impexp_messages");}} {{IEDConf.associationsFileName}}.xml </span>
                                 <md-button class="md-fab md-mini"   aria-label="download" ng-click="downloadAssociationsFile()">
                                    <md-icon class="fa fa-download fa-2x"></md-icon>
                                 </md-button>
                              </h3>
                              <h3 ng-if="IEDConf.associationsFileName!=''" class="md-body-2">
                                 <span>{{translate.load("impexp.saveAss","component_impexp_messages");}}</span>
                                 <md-button class="md-fab md-mini"   aria-label="download" ng-click="saveAssociationsFile()">
                                    <md-icon class="fa fa-save fa-2x"></md-icon>
                                 </md-button>
                              </h3>
                              <h3 ng-if="IEDConf.logFileName!=''" class="md-body-2">
                                 <span>{{translate.load("Sbi.downloadLog","component_impexp_messages");}} {{IEDConf.logFileName}}.log</span>
                                 <md-button class="md-fab md-mini"   aria-label="download" ng-click="downloadLogFile()">
                                    <md-icon class="fa fa-download fa-2x"></md-icon>
                                 </md-button>
                              </h3>
                           </div>
                        </div>
                     </md-card>
                     <md-card>
                        <md-content ng-cloak>
                           <div class="importSteps" flex ng-if="selectedStep == 1">
                              <%@include	file="./importMenuSteps/importMenuStep1.jsp"%>
                           </div>
                           <div class="importSteps" flex ng-if="selectedStep == 2">
                              <%@include	file="./importMenuSteps/importMenuStep2.jsp"%>
                           </div>
                        </md-content>
                     </md-card>
                  </md-tab-body>
               </md-tab>
            </md-tabs>
         </md-content>
      </div>
   </body>
</html>