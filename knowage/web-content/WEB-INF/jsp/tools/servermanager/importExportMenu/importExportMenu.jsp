<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
    <%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
        <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
        <html>

        <head>
            <%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
                <link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/customStyle.css">
                <script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/menuImportExport/importExportMenuController.js"></script>
                <script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/menuImportExport/importStep0Controller.js"></script>
                <!-- 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/menuImportExport/importStep1Controller.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/menuImportExport/importStep2Controller.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/menuImportExport/importStep3Controller.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/servermanager/menuImportExport/importStep4Controller.js"></script>
-->
                <%-- breadCrumb --%>
                    <script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
                    
                    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
        </head>

        <body class="bodyStyle kn-importExportDocument" ng-app="importExportMenuModule">
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
                        <md-tab <%-- id="exportTab" --%>
                            >
                            <md-tab-label>{{translate.load("SBISet.export", "component_impexp_messages")}}</md-tab-label>
                            <md-tab-body>
                                <md-card>
                                    <md-content ng-controller="exportController">
                                        <div layout="column" layout-padding layout-wrap>
                                            <div layout="row" layout-wrap>
                                                <md-input-container flex class="md-block">
                                                    <label>{{translate.load('SBISet.importexport.nameExp', 'component_impexp_messages')}}</label>
                                                    <input type="text" ng-model="exportName" required>
                                                </md-input-container>
                                                <md-input-container class="small counter">
                                                    <md-button ng-if="!flags.waitExport" ng-click="exportFiles(selected)" aria-label="{{translate.load('SBISet.importexport.fileArchive', 'component_impexp_messages')}}" ng-disabled="exportName===undefined || exportName.length == 0" class="md-fab md-mini">
                                                        <md-icon md-font-icon="fa fa-download">
                                                        </md-icon>
                                                    </md-button>
                                                </md-input-container>
                                                <div ng-if="flags.waitExport">
                                                    <i class="fa fa-spinner fa-spin fa-4x"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </md-content>
                                </md-card>
                            </md-tab-body>
                        </md-tab>
                        <md-tab id="importTab">
                            <md-tab-label>{{translate.load("SBISet.import", "component_impexp_messages")}}</md-tab-label>
                            <md-tab-body>
                                <md-card>
                                    <md-content ng-cloak>
                                        <div layout="row" layout-wrap>
                                            <div flex=1 5>
                                                <h3>{{translate.load("sbi.importusers.import")}}</h3>
                                            </div>
                                            <div flex=20>
                                                <file-upload flex id="AssociationFileUploadImport" ng-model="importFile"></file-upload>
                                            </div>
                                            <div flex=10>
                                                <md-input-container class="small counter">
                                                    <md-button ng-click="upload($event)" aria-label="upload Menu" class="md-fab md-mini">
                                                        <md-icon md-font-icon="fa fa-upload">
                                                        </md-icon>
                                                    </md-button>
                                                </md-input-container>
                                            </div>
                                            <span flex=20></span>
                                            <div flex=20>
                                                <md-radio-group layout="row" ng-model="typeSaveMenu">
                                                    <md-radio-button value="Override" ng-click="reloadTree('Override')">{{translate.load("sbi.importusers.override");}}</md-radio-button>
                                                    <md-radio-button value="Missing" ng-click="reloadTree('Missing')">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
                                                </md-radio-group>
                                            </div>
                                            <div flex=10>
                                                <md-input-container>
                                                    <md-button ng-click="save($event)" aria-label="upload Menu">{{translate.load("sbi.importusers.startimport");}}</md-button>
                                                </md-input-container>
                                            </div>
                                        </div>
                                        <div layout="row" layout-wrap>
                                            <span flex></span>
                                            <div flex=30>
                                                <h4>{{translate.load("sbi.importusers.userimport");}}</h4>
                                                <component-tree ng-model="tree" subnode-key="children" text-to-show-key="name"> </component-tree>
                                            </div>
                                            <span flex=10>
							
								</span>
                                            <div flex=30>
                                                <h4>{{translate.load("sbi.importusers.userimporting");}}</h4>
                                                <component-tree ng-model="treeInTheDB" subnode-key="children" text-to-show-key="name"> </component-tree>
                                            </div>
                                            <span flex></span>
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
