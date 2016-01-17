<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="RolesManagementModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/catalogue/css/catalogue.css">
<!-- Styles -->
<script type="text/javascript" src=" "></script>
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/roleManagement.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Roles Management</title>
</head>
<body class="bodyStyle"
	ng-controller="RolesManagementController as ctrl">

	<angular_2_col> <left-col>
	<div class="leftBox">
		<md-toolbar class="header">
		<div class="md-toolbar-tools">
			<div style="font-size: 24px;">{{translate.load("sbi.roles.rolesList");}}</div>

			<md-button class="md-fab md-ExtraMini addButton" aria-label="create"
				style="position:absolute; right:11px; top:0px;"
				ng-click="createRole()"> <md-icon
				md-font-icon="fa fa-plus" style=" margin-top: 6px ; color: white;">
			</md-icon> </md-button>
		</div>
		</md-toolbar>
		<md-content layout-padding
			style="background-color: rgb(236, 236, 236);"
			class="ToolbarBox miniToolbar noBorder leftListbox"> <angular-table
			layout-fill id="rolesList_id" ng-model="rolesList"
			columns='[
						         {"label":"Name","name":"name"},
						         {"label":"Description","name":"description"}
						         ]'
			show-search-bar=true highlights-selected-item=true
			speed-menu-option="rmSpeedMenu" click-function="loadRole(item)">
		</angular-table> </md-content>
	</div>
	</left-col> <right-col>
	<form name="attributeForm" layout-fill
		ng-submit="attributeForm.$valid && saveRole()"
		class="detailBody md-whiteframe-z1">

		<div ng-show="showme">
			<md-toolbar class="header">
			<div class="md-toolbar-tools h100">
				<div style="text-align: center; font-size: 24px;"></div>
				<div style="position: absolute; right: 0px" class="h100">
					<md-button type="button" tabindex="-1" aria-label="cancel"
						class="md-raised md-ExtraMini rightHeaderButtonBackground"
						style=" margin-top: 2px;" ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
					</md-button>
					<md-button type="submit" aria-label="save_role"
						class="md-raised md-ExtraMini rightHeaderButtonBackground"
						style=" margin-top: 2px;" ng-disabled="!attributeForm.$valid">
					{{translate.load("sbi.browser.defaultRole.save")}} </md-button>
				</div>
			</div>
			</md-toolbar>
			<md-content flex style="margin-left:20px;"
				class="ToolbarBox miniToolbar noBorder"> <md-tabs
				md-dynamic-height md-selected="selectedTab" md-border-bottom="">
			<md-tab label='{{translate.load("sbi.generic.details");}}'>
			<md-content flex style="margin-left:20px; overflow:hidden"
				class="md-padding ToolbarBox noBorder">

			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.roles.headerName")}}</label>
					<input name="name" ng-model="selectedRole.name" ng-required="true"
						ng-maxlength="100" ng-change="setDirty()">

					<div ng-messages="attributeForm.name.$error"
						ng-show="selectedRole.name== null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.roles.headerCode")}}</label>
					<input name="code" ng-model="selectedRole.code"
					 ng-maxlength="255" ng-change="setDirty()">

					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.roles.headerDescr")}}</label>
					<input name="code" ng-model="selectedRole.description"
					 ng-maxlength="255" ng-change="setDirty()">

					</md-input-container>
				</div>
			</div>

			<div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="small counter" > 
				       <label>{{translate.load("sbi.roles.headerRoleType")}}</label>
				       <md-select  aria-label="dropdown" placeholder ="Role Type"
				       	name ="dropdown" 
				        ng-required = "true"
				        ng-model="selectedRole.roleTypeCD"
				        ng-change="changeType(selectedRole.roleTypeCD)">    
				        <md-option 
				        ng-repeat="l in listType track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_TR}}
				        </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedRole.roleTypeCD== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
			</md-content> </md-tab> <md-tab label='{{translate.load("sbi.roles.authorizations");}}'> <md-content
				flex style="margin-left:20px;"
				class="md-padding ToolbarBox noBorder">
				<div style="display: flex;">
				<div style="width: 50%; margin-right:5px;">

				<md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.save");}}
				</md-toolbar>
				
				<div layout="row" layout-wrap>
				<md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveIntoPersonalFolder" aria-label="check" name ="savePersonalFolder">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.savePersonalFolder")}}</label>
			       </div>
			       
			   </div>
				<div layout="row" layout-wrap>
				<md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveMetadata" aria-label="check" name="saveMeta">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.saveMeta")}}</label>
			       </div>
			   </div>
			   <div layout="row" layout-wrap>
			   <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveRememberMe" aria-label="check" name="saveRemember">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.saveRemember")}}</label>
			       </div>
			   </div>
			   <div layout="row" layout-wrap>
			   <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSaveSubobjects" aria-label="check" name="saveSubobj">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.saveSubobj")}}</label>
			       </div> 
			   </div>
			   <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.see");}}
				</md-toolbar>
				
				
				<div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeMetadata" aria-label="check" name="seeMeta">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeMeta")}}</label>
			       </div>
			   </div>
				<div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeNotes" aria-label="check" name="seeNotes">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeNotes")}}</label>
			       </div>
			   </div>
			   <div layout="row" layout-wrap>
			   <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeSnapshots" aria-label="check" name="seeSnapshot">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeSnapshot")}}</label>
			       </div>
			   </div>
			   <div layout="row" layout-wrap>
			    <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeSubobjects" aria-label="check" name="seeSubobj">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeSubobj")}}</label>
			       </div>
			   </div>
			   <div layout="row" layout-wrap>
			    <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeViewpoints" aria-label="check" name="seeViewpoints">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeViewpoints")}}</label>
			       </div>
			   </div>
			   
			    <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.send");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSendMail" aria-label="check" name="sendMail">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.sendMail")}}</label>
			       </div>
			   </div>
			    <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.build");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToBuildQbeQuery" aria-label="check" name="buildQbe">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.buildQbe")}}</label>
			       </div>
			   </div>
			    <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.export");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				   <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToDoMassiveExport" aria-label="check" name="doMassiveExport">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.doMassiveExport")}}</label>
			       </div>
			   </div>
			  
			   

				</div>
				<!-- right column -->
				<div style="flex-grow: 1; margin-left:5px; ">
				
				 <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.manage");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageUsers" aria-label="check" name="manageUsers">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.manageUsers")}}</label>
			       </div>
			   </div>
			    <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageGlossaryBusiness" aria-label="check" name="manageGlossaryBusiness">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.manageGlossaryBusiness")}}</label>
			       </div>
			   </div>
			    <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToManageGlossaryTechnical" aria-label="check" name="manageGlossaryTechnical">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.manageGlossaryTechnical")}}</label>
			       </div> 
			   </div>
				
				<md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.edit");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToEditWorksheet" aria-label="check" name="editWorksheet">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.worksheet")}}</label>
			       </div> 
			   </div>
			    <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.enable");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToEnableDatasetPersistence" aria-label="check" name="enableDatasetPersistence">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.enableDatasetPersistence")}}</label>
			       </div> 
			   </div>
			   <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.view");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToEnableFederatedDataset" aria-label="check" name="enableFederatedDataset">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.enableFederatedDataset")}}</label>
			       </div> 
			   </div>
				
				 <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.finalUserCan");}}
				</md-toolbar>
				 <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeDocumentBrowser" aria-label="check" ng-disabled="disable" name="seeDocBrowser">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeDocumentBrowser")}}</label>
			       </div> 
			   </div>
				 <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeMyData" aria-label="check" ng-disabled="disable" name="seeMyData">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeMyData")}}</label>
			       </div> 
			   </div>
				<div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeFavourites" aria-label="check" ng-disabled="disable" name="seeFavourites">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeFavourites")}}</label>
			       </div> 
			   </div>
				 <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeSubscriptions" aria-label="check" ng-disabled="disable" name="seeSubscriptions">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeSubscriptions")}}</label>
			       </div> 
			   </div>
			   <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToSeeToDoList" aria-label="check" ng-disabled="disable" name="seeToDoList">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.seeToDoList")}}</label>
			       </div> 
			   </div>
				 <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToCreateDocuments" aria-label="check" ng-disabled="disable" name="createDocument">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.createDocument")}}</label>
			       </div> 
			   </div>
			   <div layout="row" layout-wrap>
				 <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToCreateSocialAnalysis" aria-label="check" ng-disabled="disable" name="createSocialAnalysis">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.createSocialAnalysis")}}</label>
			       </div> 
			   </div>
				 <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToViewSocialAnalysis" aria-label="check" ng-disabled="disable" name="viewSocialAnalysis">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.viewSocialAnalysis")}}</label>
			       </div> 
			   </div>
			   <div layout="row" layout-wrap>
				  <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedRole.ableToHierarchiesManagement" aria-label="check" ng-disabled="disable" name="hierarchiesManagement">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.roles.hierarchiesManagement")}}</label>
			       </div> 
			   </div>
				
				</div>
				</div>
				 </md-content> </md-tab> 
				<md-tab
				label='{{translate.load("sbi.roles.businessModels");}}'> <md-content
				flex style="margin-left:20px; overflow:hidden"
				class="md-padding ToolbarBox noBorder">
				
				<md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.roles.businessModels.categories");}}
				</md-toolbar>
				
				<angular-table
				layout-fill id="rolesCategories_id" ng-model="roleMetaModelCategories"
				columns='[
							{"label":"NAME","name":"VALUE_NM","size":"50px"}
							 ]'
				selected-item="category" highlights-selected-item=true
				multi-select="true"> </angular-table>
				
			</md-content> </md-tab> </md-tabs> </md-content>
		</div>
	</form>
	</right-col> </angular_2_col>
</body>
</html>