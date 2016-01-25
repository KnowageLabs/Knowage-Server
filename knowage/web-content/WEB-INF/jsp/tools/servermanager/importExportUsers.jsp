<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="impExpUsers">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/tree-style.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">


<link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css">
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/importExportUsers.js"></script>




</head>
<body class="bodyStyle">

<div ng-controller="Controller " layout="column" layout-wrap layout-fill>

	
	<md-toolbar  class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange fa-2x"></i>
			<h2 class="md-flex" >{{translate.load("sbi.impexpusers");}}</h2>
		</div>
	</md-toolbar>
	<md-content layout="column" layout-wrap flex>
		<md-tabs md-select="ImportExport" md-dynamic-height
					class="mozScroll hideTabs" md-border-bottom> 
				<md-tab	label="Import" md-on-select="setTab('Import')"
					md-active="isSelectedTab('Import')">
					<div  layout="column" layout-wrap>
					<div layout="row" layout-wrap>
						<div flex = 15 >
							<h3>Import a document</h3>
						</div>
						
						<div flex=20 >
							<file-upload flex id="AssociationFileUploadImport" ng-model="importFile"></file-upload>
						</div>
						
						<div flex =10 >
						<md-input-container class="small counter"> 
							<md-button ng-click="upload($event)" aria-label="upload Users"
								class="md-fab md-mini"  > <md-icon
								md-font-icon="fa fa-upload"  >
							</md-icon> </md-button>
						</md-input-container>
						</div>
						<span flex=30></span>
						
						<div flex =15>
							<md-radio-group layout="row" ng-model="typeSaveUser">
						      <md-radio-button value="Override" >Override</md-radio-button>
						      <md-radio-button value="Missing"> Add Missing </md-radio-button>
						    </md-radio-group>
						</div>
							<span flex=2></span>				
						<md-input-container class="small counter"> 
							<md-button ng-click="save($event)" aria-label="upload Users" > Start Import</md-button>
						</md-input-container>
					</div>

					<div layout="row" layout-fill ng-show="flagShowUser">
						<div flex=40 style="position: relative;" >
						<h4>{{translate.load("sbi.importusers.userimport");}}</h4>
							<angular-table ng-show="exportedUser.length!=0" id='layerlist' 
							ng-model=exportedUser
							columns='[{"label":"","name":"userId"}]'
							columnsSearch='["userId"]' 
							show-search-bar=true
							highlights-selected-item=true 
							
							menu-option=menuLayer 
							multi-select=true
							selected-item=selectedUser
							no-pagination=true
							scope-functions=tableFunction>
							</angular-table> 
							
						</div>
						<div flex = 10  >
						<md-input-container class="small counter"> 
							<md-button  class="md-fab md-mini"  ng-click="addUser()"><md-icon
								md-font-icon="fa fa-angle-right fa-2x"  >
							</md-icon></md-button>
						</md-input-container>
						<md-input-container class="small counter"> 
							<md-button  class="md-fab md-mini"  ng-click="removeUser()" ><md-icon
								md-font-icon="fa fa-angle-left fa-2x" >
							</md-icon></md-button>
						</md-input-container>
						<br>
						<br>
						<md-input-container class="small counter"> 
							<md-button  class="md-fab md-mini"  ng-click="addAllUser()" ><md-icon
								md-font-icon="fa fa-angle-double-right fa-2x"  >
							</md-icon></md-button>
						</md-input-container>
						<md-input-container class="small counter"> 
							<md-button class="md-fab md-mini"  ng-click="removeAllUser()"><md-icon
								md-font-icon="fa fa-angle-double-left fa-2x"  >
							</md-icon></md-button>
						</md-input-container>
						</div>
						<div flex=40 style="position: relative;" >
			
						<h4>{{translate.load("sbi.importusers.userimporting");}}</h4>
							<angular-table ng-show="exportingUser.length!=0" id='layerlist2' 
							ng-model=exportingUser
							columns='[{"label":"","name":"userId"}]'
							columnsSearch='["userId"]' 
							show-search-bar=true
							highlights-selected-item=true 
							menu-option=menuLayer 
							multi-select=true
							selected-item=selectedUser
							no-pagination=true
							scope-functions=tableFunction>
						
							
						</div>
					</div>
					
					</div>
				</md-tab>
				<md-tab label="Export" md-on-select="setTab('Export')"
					md-active="isSelectedTab('Export')">
					<div layout="row" layout-wrap>
					<div flex=35>
						<md-input-container class="small counter"> <label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
								<input class="input_class" ng-model="nameExport"
									required maxlength="100" ng-maxlength="100" md-maxlength="100" />
									
						</md-input-container>
					</div>
					<div flex =10 >
						<md-input-container class="small counter"> 
							<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download Users"
								class="md-fab md-ExtraMini"  > <md-icon
								md-font-icon="fa fa-download"  >
							</md-icon> </md-button>
							
							<md-progress-circular ng-show="wait" md-mode="indeterminate"></md-progress-circular>
						</md-input-container>
					</div>
					</div>
					<br>
					<div id="lista" style="background:#eceff1">
						<div layout="row" layout-wrap>
							<div >
							<md-checkbox  ng-checked="flagCheck" ng-click="selectAll()">Select All</md-checkbox>
							</div>
						</div>
						<div layout="row" layout-wrap flex>
							<div flex="90" ng-repeat="us in users">
								<md-checkbox ng-checked="exists(us, usersSelected)"
									ng-click="toggle(us, usersSelected)"> {{ us.userId }} </md-checkbox>
		
							</div>
						</div>
					</div>
			
					
				</md-tab>
			</md-tabs>
			</md-content>
			
			</div>
		
</body>


</html>