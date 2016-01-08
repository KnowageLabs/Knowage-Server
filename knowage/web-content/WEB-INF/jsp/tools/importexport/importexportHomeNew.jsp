<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.tools.importexport.ImportExportConstants" %>
<%@page import="it.eng.spagobi.tools.importexport.ImportResultInfo"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.tools.importexport.bo.AssociationFile"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%  
	String exportFilePath = (String)aServiceRequest.getAttribute(ImportExportConstants.EXPORT_FILE_PATH);
	ImportResultInfo iri = (ImportResultInfo)aServiceRequest.getAttribute(ImportExportConstants.IMPORT_RESULT_INFO);

   	Map backUrlPars = new HashMap();
	backUrlPars.put("ACTION_NAME", "START_ACTION");
	backUrlPars.put("PUBLISHER_NAME", "LoginSBIToolsPublisher");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "true");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
   
	Map formExportUrlPars = new HashMap();
	String formExportUrl = urlBuilder.getUrl(request, formExportUrlPars);
   
	Map formImportUrlPars = new HashMap();
	String formImportUrl = urlBuilder.getUrl(request, formImportUrlPars);
  
	String downloadUrl = GeneralUtilities.getSpagoBIProfileBaseUrl(userId);
	downloadUrl += "&ACTION_NAME=DOWNLOAD_FILE_ACTION";
	if((exportFilePath!=null) && !exportFilePath.trim().equalsIgnoreCase("") ) {
		downloadUrl += "&OPERATION=downloadExportFile&FILE_NAME="+  exportFilePath;
	}
   
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/importexport/importExportController.js"></script>
	<link rel="stylesheet" type="text/css" href="/knowage/themes/importexport/css/importExportStyle.css">
	<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/generalStyle.css">
	<title>{{translate.load("SBISet.importexport","component_impexp_messages");}}</title>
</head>
<body class="bodyStyle" ng-app="importExportModule" id="ng-app">
<!-- TODO using correct message -->
	<div ng-controller="importExportController as ctrl" layout="column" style="width: 100%; height: 100%;" class="contentdemoBasicUsage" >		

		<md-toolbar ng-click="debug()" class="miniheadimportexport" style="height:4%">
			<div class="md-toolbar-tools">
				<i class="fa fa-exchange fa-2x"></i>
				<h2 class="md-flex" style="padding-left: 14px">{{translate.load("SBISet.importexport","component_impexp_messages");}}</h2>
			</div>
		</md-toolbar>
		
		<md-content layout-padding style="height: 96%; padding: 20px;">
			<md-content layout="row" layout-sm="column" layout-wrap >
				<!-- Import -->
				<div class="div-container" flex flex-sm="100" style="margin-right: 20px;" >
					<md-toolbar class="miniheadimexsmall" style="border-bottom: 2px solid grey;" >
						<div class="md-toolbar-tools">
							<i class="fa fa-arrow-left "></i>
							<h2 class="md-flex" style="padding-left: 14px">{{translate.load("SBISet.import","component_impexp_messages");}}</h2>
							<span flex=""></span>					
						</div>
					</md-toolbar>
					
					<md-content layout-padding layout="column" layout-align="start stretch">
						<md-content layout="row" layout-align="space-around center">
							<div flex="75">
								<file-upload id="fileUploadImport" ng-model="fileImport"></file-upload>
							</div>
							<div flex="15">
								<md-button class="md-fab md-fab-mini"  ng-disabled="fileImport.file===undefined || fileImport.fileName.length == 0" aria-label="{{translate.load('SBISet.import','component_impexp_messages');}} {{translate.load('sbi.ds.wizard.file');}}" ng-click="importFile()">
			           				 <!-- <md-tooltip md-direction="bottom" >
				          				{{translate.load("SBISet.import","component_impexp_messages");}} {{translate.load("sbi.ds.wizard.file");}}
				        			 </md-tooltip>
				        			  -->
			           				 <md-icon class="fa fa-download center-ico"></md-icon>
			       				 </md-button>
		       				 </div>
						</md-content>
						<md-content layout-padding class="associations-container">
							<md-toolbar class="miniheadassociations" >
								<div class="md-toolbar-tools" style="margin-top: 5px;">	
									{{translate.load("impexp.Associations","component_impexp_messages");}}
								</div>
							</md-toolbar>
							<md-radio-group ng-model="associations">
						    	<md-radio-button value="noAssociations " class="md-primary">{{translate.load("impexp.withoutAss","component_impexp_messages");}}</md-radio-button>
						    	<md-radio-button value="mandatoryAssociations">{{translate.load("impexp.mandatoryAss","component_impexp_messages");}}</md-radio-button>
						     	<md-radio-button value="defaultAssociations">{{translate.load("impexp.defaultAss","component_impexp_messages");}}</md-radio-button>
						    </md-radio-group>
						    <md-content layout-padding ng-if = "associations == 'mandatoryAssociations' || associations == 'defaultAssociations'">
						    	<md-content layout-xs="column" layout-align-xs="center stretch" layout="row"  layout-align="start center">
						    		<div flex="20" flex-xs="90">
						    			{{translate.load("impexp.savedAss","component_impexp_messages");}}
						    		</div>
							    	<md-input-container flex='70' flex-xs="90">
										<input ng-model="fileAssociation.name" aria-label="{{translate.load('impexp.savedAss','component_impexp_messages');}}" ng-disabled="true" type="text">
									</md-input-container>
									<md-button class="md-fab md-fab-mini" ng-click="listAssociation()" aria-label="{{translate.load('impexp.listAssFile','component_impexp_messages')}}" >
				           				<md-icon class="fa fa-search center-ico"></md-icon>
				       				</md-button>
								</md-content>
						    </md-content>
						</md-content>
					</md-content>
					
				</div>
				<!-- Export -->
				<div  class="div-container" flex flex-sm="100">
					<md-toolbar class="miniheadimexsmall" style="border-bottom: 2px solid grey;" >
						<div class="md-toolbar-tools">
							<i class="fa fa-arrow-right"></i>
							<h2 class="md-flex" style="padding-left: 14px">{{translate.load("SBISet.export","component_impexp_messages");}}</h2>
							<span flex=""></span>					
						</div>
					</md-toolbar>
					<md-content ng-if="flags.waitExport" layout="column" layout-padding layout-align="space-around center">
						<md-content>
							<h3 class="md-subhead">{{translate.load("SBISet.importexport.opProg","component_impexp_messages");}}</h3>
						</md-content>
						<md-content style="height: 6rem;width: 6rem;margin-top: 1rem;">
							<i class="fa fa-spinner fa-spin fa-5x"></i>
						</md-content>
					</md-content>
					<md-content layout-margin ng-if="flags.viewDownload">
						<form method='POST' action='<%=downloadUrl%>' id='downForm' name='downForm'>
							<h3 class="md-body-2">
								<md-button aria-label="Close" ng-click="toggleViewDownload()" style="min-width: 24px;min-height:24px;width:24px;height:24px">
									<md-icon class="fa fa-times" style="margin-top: 0.3rem;margin-left: -0.3rem;"></md-icon>
								</md-button>
								<span >{{translate.load("SBISet.importexport.opComplete","component_impexp_messages");}}</span> 
								<md-button ng-click="downloadFile()">{{translate.load("Sbi.download","component_impexp_messages");}}</md-button>
								<br>
								<span>{{translate.load("SBISet.importexport.exportCompleteResourcesWarning","component_impexp_messages");}}</span>
							</h3>
						</form>
					</md-content>
					<md-content flex layout="column" layout-padding layout-align = "start stretch">
						<md-content layout="row" layout-align="space-around center" style="bottom: 5px;">
							<md-input-container flex="75"  md-no-float class="md-block">
								<label>{{translate.load("SBISet.importexport.nameExp","component_impexp_messages");}}</label>
								<input type="text" ng-model="exportName" required>
							</md-input-container>
							<div flex="15">
								<md-button class="md-fab md-fab-mini" ng-click="exportFiles(selected)" ng-disabled="selected.length==0 || exportName===undefined || exportName.length == 0" aria-label="{{translate.load('SBISet.importexport.fileArchive','component_impexp_messages')}}">
			           				 <md-icon class="fa fa-upload center-ico"></md-icon>
			           				 <!-- <md-tooltip md-direction="bottom" >
			          					{{translate.load("SBISet.importexport.fileArchive","component_impexp_messages");}}
			        				</md-tooltip>
			        				 -->
			       				 </md-button>
		       				 </div>
	       				</md-content>
	       				<md-content layout-padding layout-gt-sm="row" layout-align-gt-sm="start center" layout-sm="column" layuout-align-sm ="start start">
	       				 	<md-checkbox class="little-check" ng-model="checkboxs.exportSubObj" aria-label="Export sub views">{{translate.load("SBISet.importexport.expSubView","component_impexp_messages");}}</md-checkbox>
	       				 	<md-checkbox class="little-check" ng-model="checkboxs.exportSnapshots" aria-label="Export snapshots">{{translate.load("SBISet.importexport.expSnapshots","component_impexp_messages");}}</md-checkbox>
       				 	</md-content>
						<md-content layout-padding>
							<document-tree ng-model="folders" id="impExpTree" create-tree="true" selected-item="selected"  multi-select="true" show-files="true" >
							</document-tree>
						</md-content>
					</md-content>
				</div>
			</md-content>
		</md-content>
	</div>		
</body>
</html>