
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
<html ng-app="UsersManagementModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<!-- Styles -->
<script type="text/javascript" src=" "></script>
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/usersManagement.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Users Management</title>
</head>
<body class="bodyStyle kn-usersManagement"
	ng-controller="UsersManagementController as ctrl">
   <angular-list-detail show-detail="showme">
	<list label='translate.load("sbi.users.usersList")' new-function="createUser"> 

		 <angular-table
		    flex
			id="usersList_id" ng-model="usersList"
			columns='[
						         {"label":"User ID","name":"userId"},
						         {"label":"Full Name","name":"fullName"}
						         ]'
			show-search-bar=true highlights-selected-item=true
			speed-menu-option="umSpeedMenu" click-function="loadUser(item)">
		</angular-table> 
	
	</list> 
	<detail label='selectedUser.userId==undefined? "" : selectedUser.userId'  save-function="saveUser"
		cancel-function="cancel"
		disable-save-button="!attributeForm.$valid || role.length== 0"
		show-save-button="showme" show-cancel-button="showme">
	<div layout-fill class="containerDiv">	
	<form name="attributeForm" layout-fill
		ng-submit="attributeForm.$valid && saveUser()"
		class="detailBody ">
			
			 <md-tabs  class="mozScroll hideTabs h100"
				md-dynamic-height md-selected="selectedTab" md-border-bottom="">
			<md-tab label='{{translate.load("sbi.generic.details");}}'>
			<md-content 
				class=" ToolbarBox miniToolbar noBorder mozTable ">
            <md-card layout-padding>
			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.users.userId")}}</label>
					<input name="id" ng-model="selectedUser.userId" ng-required="true"
						ng-maxlength="100" ng-change="setDirty()">

					<div ng-messages="attributeForm.id.$error"
						ng-show="selectedUser.userId== null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.users.fullName")}}</label>
					<input name="name" ng-model="selectedUser.fullName"
						ng-required="true" ng-maxlength="255" ng-change="setDirty()">

					<div ng-messages="attributeForm.name.$error"
						ng-show="selectedUser.fullName== null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.users.pwd")}}</label>
					<input data-ng-model="selectedUser.password" type="password"
						name="password" required ng-maxlength="100" ng-change="setDirty()">
					<div ng-messages="attributeForm.password.$error"
						ng-show="selectedUser.password== null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
			</div>

			<div layout="row" layout-wrap>
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.users.confPwd")}}</label>
					<input ng-model="selectedUser.confirm" type="password"
						name="confirm_password" required ng-maxlength="100" ng-change="setDirty()"
						nx-equal-ex="selectedUser.password">
					<div ng-messages="attributeForm.confirm_password.$error"
						ng-show="selectedUser.confirm== null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>	
					<div ng-messages="attributeForm.confirm_password.$error"
						ng-show="attributeForm.confirm_password.$error.nxEqualEx">
						<div ng-message="required">{{translate.load("sbi.users.pwdNotMatching");}}</div>
					</div>
					</md-input-container>
				</div>
			</div>
			</md-card>
			</md-content> 
			</md-tab> 
			<md-tab label='{{translate.load("sbi.users.roles");}}'> 
			<md-content
				flex 
				class="ToolbarBox noBorder">
				<md-card flex layout-padding>
				 <md-input-container class="small counter" >
				    <div  ng-messages="attributeForm.dropdown.$error" ng-show="role.length== 0">
					<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired.role");}}</div>
					</div>  
				</md-input-container>
				
				 <angular-table layout-fill
				  id="usersRoles_id" ng-model="usersRoles"
				columns='[
							{"label":"NAME","name":"name","size":"50px"},
							{"label":"VALUE","name":"description","size":"100px"},
							 ]'
				selected-item="role" highlights-selected-item="true"
				multi-select="true" >
				 </angular-table>
				 </md-card>
				 </md-content> 
				 </md-tab> 
				 <md-tab
				label='{{translate.load("sbi.users.attributes");}}'> 
				<md-content
				flex 
				class="ToolbarBox noBorder">
                <md-card layout-padding>
			<div layout="row" layout-wrap ng-repeat="attribute in tempAttributes">
				<div flex=100>
					<md-input-container class="small counter"> <label>{{attribute.name}}</label>
					<input name="attr" ng-model="attribute.value" ng-maxlength="100"
						ng-change="setDirty()"> </md-input-container>
				</div>


			</div>
           </md-card>
			</md-content> </md-tab> </md-tabs> 
		
	</form>
	</div>
	</detail>
</angular-list-detail>
</body>
</html>
