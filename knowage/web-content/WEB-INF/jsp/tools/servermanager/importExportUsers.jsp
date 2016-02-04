<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="impExpUsers">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
 
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/importexport/css/importExportStyle.css">
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/importExportUsers.js"></script>




</head>
<body class="bodyStyle">

<div ng-controller="Controller " layout="column" layout-wrap layout-fill>

	
	<md-toolbar  class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-exchange md-padding"></i>
			<h2 class="md-flex" >{{translate.load("sbi.impexpusers");}}</h2>
		</div>
	</md-toolbar>
	<md-content layout="column" layout-wrap flex>
		<md-tabs md-select="ImportExport" md-dynamic-height md-border-bottom> 
				<md-tab label="Export" md-on-select="setTab('Export')" md-active="isSelectedTab('Export')">
				<md-content layout="column" layout-wrap>
					<div layout="row" layout-wrap>
						<div flex>
							<md-input-container class="small counter"> <label>{{translate.load("sbi.impexpusers.nameexport")}}</label>
									<input class="input_class" ng-model="nameExport" required maxlength="100" ng-maxlength="100" md-maxlength="100" />
							</md-input-container>
						</div>
					 
					 <div layout="row" layout-wrap >
						<md-checkbox   style="line-height: 61px;"  ng-model="checkboxs.exportPersonalFolder" aria-label="Checkbox 1">{{translate.load("sbi.impexpusers.exportPersonalFolder")}}</md-checkbox>
						<md-checkbox ng-if="checkboxs.exportPersonalFolder"  style="line-height: 61px;"  ng-model="checkboxs.exportSubObj" aria-label="Checkbox 1">{{translate.load("SBISet.importexport.expSubView","component_impexp_messages");}}</md-checkbox>
						<md-checkbox ng-if="checkboxs.exportPersonalFolder"  style="line-height: 61px;"  ng-model="checkboxs.exportSnapshots" aria-label="Checkbox 1">{{translate.load("SBISet.importexport.expSnapshots","component_impexp_messages");}}</md-checkbox>
					 </div>
						<div>
							<md-input-container class="small counter"> 
								
								<md-button ng-show="!wait" ng-click="prepare($event)" aria-label="download Users"
									class="md-fab md-mini"  > <md-icon
									md-font-icon="fa fa-download fa-2x"  >
								</md-icon> </md-button>
								<div ng-show="wait">
								<i  class="fa fa-spinner fa-spin fa-4x"></i>
								</div>
								
								<!--  <md-progress-circular ng-show="wait" md-mode="indeterminate"></md-progress-circular>-->
							</md-input-container>
						</div>
					</div>
					 
					<div id="lista" style="background:#eceff1">
						<div layout="row" layout-wrap>
							<div >
							<md-checkbox  ng-checked="flagCheck" ng-click="selectAll()">{{translate.load("sbi.importusers.selectall");}}</md-checkbox>
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
				<md-tab	label="Import" md-on-select="setTab('Import')"
					md-active="isSelectedTab('Import')">
					<div  layout="column" layout-wrap>
					<div layout="row" layout-wrap>
						<div flex = 15 >
							<h3>{{translate.load("sbi.importusers.import");}}</h3>
						</div>
						
						<div flex=20 >
							<file-upload flex id="AssociationFileUploadImport" ng-model="importFile"></file-upload>
						</div>
						
						<div flex =10 >
						<md-input-container class="small counter"> 
							<md-button ng-click="upload($event)" aria-label="upload Users"
								class="md-fab md-mini"  > <md-icon
								md-font-icon="fa fa-upload fa-2x"  >
							</md-icon> </md-button>
						</md-input-container>
						</div>
						<span flex=20></span>
						
						<div flex =20>
							<md-radio-group layout="row" ng-model="typeSaveUser">
						      <md-radio-button value="Override" >{{translate.load("sbi.importusers.override");}}</md-radio-button>
						      <md-radio-button value="Missing">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
						    </md-radio-group>
						</div>
							<span flex=2></span>				
						<md-input-container class="small counter"> 
							<md-button ng-click="save($event)" aria-label="upload Users" >{{translate.load("sbi.importusers.startimport");}}</md-button>
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
				
			</md-tabs>
			</md-content>
			
			</div>
		
</body>


</html>