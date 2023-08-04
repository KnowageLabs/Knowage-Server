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
<%-- @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net) --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="FunctionalitiesManagementModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<!-- Styles -->
<script type="text/javascript" src=" "></script>


<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/functionalitiesManagement.js")%>"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title></title>
</head>
<body class="bodyStyle" ng-controller="FunctionalitiesManagementController as ctrl">

	<angular-list-detail show-detail="showme"> 
	<list  label='translate.load("sbi.folders.list")' new-function="createFolder" show-new-button="showPlusButton"
	style="max-width:30%">
		<md-content> 
			<document-tree ng-model="folders"
				highlights-selected-item="true" create-tree="true"
				click-function="loadFolder(item)"
				menu-option="functMenuOpt"
				translate="false"
				>
			</document-tree> 
		</md-content> 
	
	</list> 
	<detail label=' selectedFolder.code==undefined? "" : selectedFolder.code'  save-function="save" cancel-function="cancel"
		show-save-button="showme" show-cancel-button="showme"
		disable-save-button="!attributeForm.$valid"  >
		<div layout-fill class="containerDiv">
			<form name="attributeForm" 
				ng-submit="attributeForm.$valid && save()" class="detailBody ">
	
			 <md-card layout-padding ng-cloak  >
				<div flex=100>
					<md-input-container class="md-block"> <label>{{translate.load("sbi.folders.code")}}</label>
					<input name="code" ng-model="selectedFolder.code" ng-required="true"
						ng-maxlength="100" ng-change="setDirty()">
	
					<div ng-messages="attributeForm.id.$error"
						ng-show="selectedFolder.code == null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
				<div flex=100>
					<md-input-container class="md-block"> <label>{{translate.load("sbi.folders.name")}}</label>
					<input name="name" ng-model="selectedFolder.name"
						ng-required="true" ng-maxlength="255" ng-change="setDirty()">
	
					<div ng-messages="attributeForm.name.$error"
						ng-show="selectedFolder.name== null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
				<div flex=100>
					<md-input-container class="md-block"> <label>{{translate.load("sbi.folders.description")}}</label>
					<input data-ng-model="selectedFolder.description" 
						name="description" ng-maxlength="100" ng-change="setDirty()">
					</md-input-container>
				</div>
	
			
				</md-card>  
				<md-card>
					 <angular-table  
					 flex  
					 id="folderRoles_id" ng-model="roles"
					 columns='columnsArray'  no-pagination=false
					 highlights-selected-item="true" 
					 speed-menu-option="adSpeedMenu"
					 scope-functions = tableFunction
					 
					 
					>
				  
				 </md-card>
				
			</form>
			
		</div>
		
	
	</detail> </angular-list-detail>
</body>
</html>
