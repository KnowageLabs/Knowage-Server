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


<%@ page language="java" pageEncoding="UTF-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="RolesManagementModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<!-- <link rel="stylesheet" type="text/css" -->
<!-- 	href="/knowage/themes/glossary/css/generalStyle.css"> -->
<!-- <link rel="stylesheet" type="text/css" -->
<!-- 	href="/knowage/themes/catalogue/css/catalogue.css"> -->
	
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
	
<!-- Styles -->
<script type="text/javascript" src=" "></script>
<%-- 
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/rolesManagement.js"></script> 
<script type="text/javascript"	src="/knowage/js/src/angular_1.4/tools/catalogues/RoleManagementSubController/kpiCategoryController.js"></script>
--%>
<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/rolesManagement.js")%>"></script>
<script type="text/javascript"	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/RoleManagementSubController/kpiCategoryController.js")%>"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Roles Management</title>
</head>
<body class="bodyStyle kn-rolesManagement"
	ng-controller="RolesManagementController as ctrl">

	<angular-list-detail show-detail="showme">
	<list label='translate.load("sbi.roles.rolesList");' new-function="createRole"> 
	
		 <angular-table
		     flex
			 id="rolesList_id" ng-model="rolesList"
			columns='[
						         {"label":"Name","name":"name"},
						         {"label":"Description","name":"description"}
						         ]'
			columns-search='["name","description"]'
			show-search-bar=true highlights-selected-item=true
			speed-menu-option="rmSpeedMenu" click-function="loadRole(item)">
		</angular-table> 
	
	</list>
	 <detail label='selectedRole.name==undefined? "" : selectedRole.name'  save-function="saveRole"
		cancel-function="cancel"
		disable-save-button="!attributeForm.$valid"
		show-save-button="showme" show-cancel-button="showme">
	<div layout-fill class="containerDiv">	
	<form name="attributeForm"
		ng-submit="attributeForm.$valid && saveRole()">

		
			 <md-tabs
				md-dynamic-height md-selected="selectedTab" md-border-bottom="">
			<md-tab label='{{translate.load("sbi.generic.details");}}'>
			<md-content flex 
				class="ToolbarBox miniToolbar noBorder mozTable">
            <md-card layout-padding>

				<div flex=100>
					<md-input-container class="md-block"> <label>{{translate.load("sbi.roles.headerName")}}</label>
						<input name="name" ng-model="selectedRole.name" ng-required="true" ng-maxlength="100" ng-change="setDirty()" ng-pattern="regex.extendedAlphanumeric">
						<div ng-messages="attributeForm.name.$error" role="alert" ng-messages-multiple>
							<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
							<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 100</div>
  						</div>	
					</md-input-container>
				</div>
				<div flex=100>
					<md-input-container class="md-block"> <label>{{translate.load("sbi.roles.headerCode")}}</label>
						<input name="code" ng-model="selectedRole.code" ng-maxlength="20" ng-change="setDirty()"  ng-pattern="regex.extendedAlphanumeric">
						<div ng-messages="attributeForm.code.$error" role="alert" ng-messages-multiple>
							<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
							<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 20</div>
  						</div>
					</md-input-container>
				</div>
				<div flex=100>
					<md-input-container class="md-block"> <label>{{translate.load("sbi.roles.headerDescr")}}</label>
						<input name="description" ng-model="selectedRole.description" ng-maxlength="255" ng-change="setDirty()"  ng-pattern="regex.extendedAlphanumeric">
						<div ng-messages="attributeForm.description.$error" role="alert" ng-messages-multiple>
							<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
							<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 160</div>
  						</div>
					</md-input-container>
				</div>
      			<div flex=100>
					<md-input-container class="md-block" > 
				    	<label>{{translate.load("sbi.roles.headerRoleType")}}</label>
				       	<md-select  aria-label="dropdown" placeholder ="Role Type" name ="dropdown" required ng-model="selectedRole.roleTypeCD" ng-change="changeType(selectedRole.roleTypeCD)">    
				        	<md-option ng-repeat="l in listType track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_TR}}</md-option>
				    	</md-select>   
					</md-input-container>
				</div>
				<div layout="row" flex="100" class="kn-checkInput">
					<label flex="15">{{translate.load("sbi.roles.isPublicRole")}}</label>
					<md-input-container> 
			        	<md-checkbox flex="85" 
			         		ng-change="setDirty()"  
			         		ng-model="selectedRole.isPublic" aria-label="check" name ="isPublic">
			        	</md-checkbox> 
			    	</md-input-container>  
				</div>
			</md-card>
			</md-content>
			 </md-tab> 
			 <md-tab label='{{translate.load("sbi.roles.authorizations");}}'>
			  <md-content
				flex 
				class="ToolbarBox miniToolbar noBorder mozTable authorizationList">
				<md-card>
				

				<md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar"  ng-if="isToolbarVisible('SAVE')"
												 >
										{{translate.load("sbi.roles.save");}}
				</md-toolbar>
				
				<div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('savePersonalFolder')">
				<md-input-container> 
			        <md-checkbox 
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveIntoPersonalFolder" aria-label="check" name ="savePersonalFolder">
			        </md-checkbox> 
			       </md-input-container>
			       <label flex="90">{{translate.load("sbi.roles.savePersonalFolder")}}</label>
			        
			       
			   </div>
				<div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('saveMeta')">
				<md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveMetadata" aria-label="check" name="saveMeta">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.saveMeta")}}</label>
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('saveRemember')">
			   <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveRememberMe" aria-label="check" name="saveRemember">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.saveRemember")}}</label>
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('saveSubobj')">
			   <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveSubobjects" aria-label="check" name="saveSubobj">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.saveSubobj")}}</label>
			   </div>
			   <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('SEE')">
								{{translate.load("sbi.roles.see");}}
				</md-toolbar>
				
				
				<div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeMeta')">
				 <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeMetadata" aria-label="check" name="seeMeta">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.seeMeta")}}</label>
			   </div>
				<div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeNotes')">
				 <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeNotes" aria-label="check" name="seeNotes">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.seeNotes")}}</label>
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeSnapshot')">
			   <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeSnapshots" aria-label="check" name="seeSnapshot">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.seeSnapshot")}}</label>
			   </div> 
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('runSnapshot')">
			   <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToRunSnapshots" aria-label="check" name="runSnapshot">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.runSnapshot")}}</label>
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeSubobj')">
			    <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeSubobjects" aria-label="check" name="seeSubobj">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.seeSubobj")}}</label>
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeViewpoints')">
			    <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeViewpoints" aria-label="check" name="seeViewpoints">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.seeViewpoints")}}</label>
			   </div>			
			   
			    <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('SEND')"
												 >
										{{translate.load("sbi.roles.send");}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('sendMail')">
				  <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSendMail" aria-label="check" name="sendMail">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.sendMail")}}</label>
			   </div>
			    <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('BUILD')"
												 >
										{{translate.load("sbi.roles.build");}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('buildQbe')">
				 <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToBuildQbeQuery" aria-label="check" name="buildQbe">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.buildQbe")}}</label>
			   </div>
			    <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('EXPORT')"
												 >
										{{translate.load("sbi.roles.export");}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('doMassiveExport')">
				   <md-input-container> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToDoMassiveExport" aria-label="check" name="doMassiveExport">
			        </md-checkbox> 
			       </md-input-container>
			        <label flex="90">{{translate.load("sbi.roles.doMassiveExport")}}</label>

			   </div>

				 <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('MANAGE')"
												 >
										{{translate.load("sbi.roles.manage");}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('manageUsers')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageUsers" aria-label="check" name="manageUsers">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.manageUsers")}}</label>
			       </div>
			   </div>
			    <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('manageGlossaryBusiness')">
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageGlossaryBusiness" aria-label="check" name="manageGlossaryBusiness">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.manageGlossaryBusiness")}}</label>
			       </div>
			   </div>
			    <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('manageGlossaryTechnical')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageGlossaryTechnical" aria-label="check" name="manageGlossaryTechnical">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.manageGlossaryTechnical")}}</label>
			       </div> 
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('manageKpiValue')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageKpiValue" aria-label="check" name="manageKpiValue">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.manageKpiValue")}}</label>
			       </div> 
			   </div>
				<div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('manageCalendar')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageCalendar" aria-label="check" name="manageCalendar">
			        </md-checkbox> 
			       </md-input-container>
			       <div>
			        <label>{{translate.load("sbi.roles.manageCalendar")}}</label>
			       </div> 
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('manageInternationalization')">
				   <md-input-container class="small counter"> 
			         <md-checkbox
			           ng-change="setDirty()"  ng-model="selectedRole.ableToManageInternationalization" aria-label="check" name="manageInternationalization">
		        	 </md-checkbox> 
			       </md-input-container>
			       <div>
			       	 <label>{{translate.load("sbi.roles.manageInternationalization")}}</label>
			       </div>			        
			   </div>
			  
			  
				<md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('EDIT')"
												 >
										{{translate.load("sbi.roles.edit");}}
				</md-toolbar>
				 
			    <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" 
												 >
										{{translate.load("sbi.roles.enable");}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('enableDatasetPersistence')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToEnableDatasetPersistence" aria-label="check" name="enableDatasetPersistence">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.enableDatasetPersistence")}}</label>
			       </div> 
			   </div>
			   <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('VIEW')"
												 >
										{{translate.load("sbi.roles.view");}}
				</md-toolbar>
				
				
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('enableFederatedDataset')">
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToEnableFederatedDataset" aria-label="check" name="enableFederatedDataset">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.enableFederatedDataset")}}</label>
			       </div> 
			   </div>
			   
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('enableToRate')">
                 <md-input-container class="small counter"> 
                    <md-checkbox
                     ng-change="setDirty()"  ng-model="selectedRole.ableToEnableRate" aria-label="check" name="enableToRate">
                    </md-checkbox> 
                   </md-input-container>
                   <div >
                    <label>{{translate.load("sbi.roles.enableToRate")}}</label>
                   </div> 
               </div>
               
               <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('enableToPrint')">
                 <md-input-container class="small counter"> 
                    <md-checkbox
                     ng-change="setDirty()"  ng-model="selectedRole.ableToEnablePrint" aria-label="check" name="enableToPrint">
                    </md-checkbox> 
                   </md-input-container>
                   <div >
                    <label>{{translate.load("sbi.roles.enableToPrint")}}</label>
                   </div> 
               </div>
               
               <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('enableToCopyAndEmbed')">
                 <md-input-container class="small counter"> 
                    <md-checkbox
                     ng-change="setDirty()"  ng-model="selectedRole.ableToEnableCopyAndEmbed" aria-label="check" name="enableToCopyAndEmbed">
                    </md-checkbox> 
                   </md-input-container>
                   <div >
                    <label>{{translate.load("sbi.roles.enableToCopyAndEmbed")}}</label>
                   </div> 
               </div>
               
               <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('ENABLE')"
												 >
										{{translate.load("sbi.roles.enableWidgets")}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('editPythonScripts')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToEditPythonScripts" aria-label="check" name="editPythonScripts">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.editPythonScripts")}}</label>
			       </div> 
			   </div>
				<div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('createCustomChart')">
					<md-input-container class="small counter"> 
						<md-checkbox
							ng-change="setDirty()"  ng-model="selectedRole.ableToCreateCustomChart" aria-label="check" name="createCustomChart">
						</md-checkbox> 
					</md-input-container>
					<div >
						<label>{{translate.load("sbi.roles.createCustomChart")}}</label>
					</div> 
				</div>
				 <md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" ng-if="isToolbarVisible('ITEMS')"
												 >
										{{translate.load("sbi.roles.finalUserCan");}}
				</md-toolbar>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeDocBrowser')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeDocumentBrowser" aria-label="check" ng-disabled="disable" name="seeDocBrowser">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.seeDocumentBrowser")}}</label>
			       </div> 
			   </div>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeMyData')">
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeMyData" aria-label="check" ng-disabled="disable" name="seeMyData">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.seeMyData")}}</label>
			       </div> 
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeMyWorkspace')">
                  <md-input-container class="small counter"> 
                    <md-checkbox
                     ng-change="setDirty()"  ng-model="selectedRole.ableToSeeMyWorkspace" aria-label="check" ng-disabled="disable" name="seeMyWorkspace">
                    </md-checkbox> 
                   </md-input-container>
                   <div >
                    <label>{{translate.load("sbi.roles.seeMyWorkspace")}}</label>
                   </div> 
               </div>
			   
			   
			   <!-- 
			   		"Add to favorites" option is not used, so it should not be visible in the Authorization tab
			   		for the Role.
			   		@modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			    -->
				<!-- <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeFavourites')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeFavourites" aria-label="check" ng-disabled="disable" name="seeFavourites">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.seeFavourites")}}</label>
			       </div> 
			   </div> -->
			   
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeSubscriptions')">
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeSubscriptions" aria-label="check" ng-disabled="disable" name="seeSubscriptions">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.seeSubscriptions")}}</label>
			       </div> 
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('seeToDoList')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeToDoList" aria-label="check" ng-disabled="disable" name="seeToDoList">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.seeToDoList")}}</label>
			       </div> 
			   </div>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('createDocument')">
                      <md-input-container class="small counter">
                        <md-checkbox
                         ng-change="setDirty()"  ng-model="selectedRole.ableToCreateDocuments" aria-label="check" name="createDocument">
                        </md-checkbox>
                       </md-input-container>
                       <div >
                        <label>{{translate.load("sbi.roles.createDocument")}}</label>
                       </div>
			     </div>
			     <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('createSelfSelviceCockpit')">
                     <md-input-container class="small counter">
                          <md-checkbox ng-change="setDirty()"  ng-model="selectedRole.ableToCreateSelfServiceCockpit" aria-label="check" name="createSelfSelviceCockpit">
                          </md-checkbox>
                     </md-input-container>
                     <div >
                        <label>{{translate.load("sbi.roles.createSelfSelviceCockpit")}}</label>
                     </div>
                 </div>
                 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('createSelfSelviceGeoreport')">
                     <md-input-container class="small counter">
                          <md-checkbox ng-change="setDirty()"  ng-model="selectedRole.ableToCreateSelfServiceGeoreport" aria-label="check" name="createSelfSelviceGeoreport">
                          </md-checkbox>
                     </md-input-container>
                     <div >
                        <label>{{translate.load("sbi.roles.createSelfSelviceGeoreport")}}</label>
                     </div>
                 </div>
                 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('createSelfSelviceKpi')">
                     <md-input-container class="small counter">
                          <md-checkbox ng-change="setDirty()"  ng-model="selectedRole.ableToCreateSelfServiceKpi" aria-label="check" name="createSelfSelviceKpi">
                          </md-checkbox>
                     </md-input-container>
                     <div >
                        <label>{{translate.load("sbi.roles.createSelfSelviceKpi")}}</label>
                     </div>
                 </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('createSocialAnalysis')">
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToCreateSocialAnalysis" aria-label="check" ng-disabled="disable" name="createSocialAnalysis">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.createSocialAnalysis")}}</label>
			       </div> 
			   </div>
				 <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('viewSocialAnalysis')">
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToViewSocialAnalysis" aria-label="check" ng-disabled="disable" name="viewSocialAnalysis">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.viewSocialAnalysis")}}</label>
			       </div> 
			   </div>
			   <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('hierarchiesManagement')">
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToHierarchiesManagement" aria-label="check" ng-disabled="disable" name="hierarchiesManagement">
			        </md-checkbox> 
			       </md-input-container>
			       <div >
			        <label>{{translate.load("sbi.roles.hierarchiesManagement")}}</label>
			       </div> 
			   </div>
				
               <div layout="row" class="kn-checkInput" layout-padding ng-if="isVisible('functionsCatalogUsage')">
                 <md-input-container class="small counter"> 
                    <md-checkbox
                     ng-change="setDirty()"  ng-model="selectedRole.ableToUseFunctionsCatalog" aria-label="check" name="functionsCatalogUsage">
                    </md-checkbox> 
                   </md-input-container>
                   <div >
                    <label>{{translate.load("sbi.roles.functionsCatalogUsage")}}</label>
                   </div> 
               </div>
               
			
				</md-card>
				 </md-content> </md-tab> 
				<md-tab
				label='{{translate.load("sbi.roles.businessModels");}}'> 
				<md-content
				flex  
				class="ToolbarBox miniToolbar noBorder mozTable">
				 <md-card>
				<md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" 
												 >
										{{translate.load("sbi.roles.businessModels.categories");}}
				</md-toolbar>
				<md-card-content>
				<angular-table
				layout-fill id="rolesMetaModelCategories_id" ng-model="roleMetaModelCategories"
				columns='[
							{"label":"NAME","name":"VALUE_NM","size":"50px"}
							 ]'
				selected-item="category" highlights-selected-item=true
				multi-select="true"> </angular-table>
				</md-card-content>
		    </md-card>		
			</md-content> </md-tab>
			<md-tab
				label='{{translate.load("sbi.roles.datasets");}}'> 
				<md-content
				flex  
				class="ToolbarBox miniToolbar noBorder mozTable">
				 <md-card>
				<md-toolbar class="md-blue minihead md-toolbar-tools secondaryToolbar" 
												 >
										{{translate.load("sbi.roles.datasets.categories");}}
				</md-toolbar>
				
				<angular-table
				layout-fill id="rolesDatasetCategories_id" ng-model="roleDataSetCategories"
				columns='[
							{"label":"NAME","name":"VALUE_NM","size":"50px"}
							 ]'
				selected-item="ds_category" highlights-selected-item=true
				no-pagination=false
				multi-select="true"> </angular-table>
		    </md-card>		
			</md-content> </md-tab>
			<!-- Add associations role categoryKpi -->
			<md-tab id="tabKpi" >
       				<md-tab-label>{{translate.load("sbi.roles.listcategory")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./rolesManagementTemplates/kpiCategory.jsp"%>
					</md-tab-body>
				</md-tab>
			</md-tabs> 
		
	</form>
	</div>
	</detail>
	</angular-list-detail>
</body>
</html>
