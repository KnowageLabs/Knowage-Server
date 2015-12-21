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
				        ng-model="selectedRole.roleTypeCD"> <md-option 
				        ng-repeat="l in listType track by $index" ng-click="FieldsCheck(l)" value="{{l.VALUE_CD}}">{{l.VALUE_NM}} </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedRole.roleTypeCD== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
			</md-content> </md-tab> <md-tab label='{{translate.load("sbi.roles.authorizations");}}'> <md-content
				flex style="margin-left:20px; overflow:hidden"
				class="md-padding ToolbarBox noBorder"> <angular-table
				layout-fill id="authList_id" ng-model="authList"
				columns='[
							{"label":"NAME","name":"name","size":"50px"},
							 ]'
				selected-item="auth" highlights-selected-item=true
				multi-select="true"> </angular-table> </md-content> </md-tab> 
				<md-tab
				label='{{translate.load("sbi.roles.businessModels");}}'> <md-content
				flex style="margin-left:20px; overflow:hidden"
				class="md-padding ToolbarBox noBorder">

			
			</md-content> </md-tab> </md-tabs> </md-content>
		</div>
	</form>
	</right-col> </angular_2_col>
</body>
</html>