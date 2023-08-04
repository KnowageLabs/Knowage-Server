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
<html ng-app="MenuConfigurationModule">
<head>
<style>
	.hoveredElement {
		background-color: rgba(144, 144, 144, 0.5);
	}
	
	.selectedElement {
		background-color: rgb(169, 195, 219);
	}
	.line-container{
		font-family: Roboto,"Helvetica Neue",sans-serif;
		font-size: 14px;
		color: rgba(0, 0, 0, 0.87);
		font-weight: normal;
		text-overflow: ellipsis;
		white-space: nowrap;
		overflow: hidden;
		max-width: 20rem;
		outline: none;
	}
</style>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<!-- Styles -->
<script type="text/javascript" src=" "></script>


<script type="text/javascript"
	src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/menuConfiguration.js")%>"></script>

<script type="text/javascript" 
	src="<%=urlBuilder.getResourceLink(request, "/js/src/angular_1.4/tools/commons/services/knModule.js")%>"></script>
	
<script type="text/javascript" 
	src="<%=urlBuilder.getResourceLink(request, "/js/src/angular_1.4/tools/commons/services/knModule_fontIconsService.js")%>"></script>
	
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title></title>
</head>
<body class="bodyStyle" ng-controller="MenuConfigurationController as ctrl">
<script type="text/ng-template" id="dialog1.tmpl.html">
<md-dialog aria-label="{{translate.load('sbi.crossnavigation.selectDocument')}}" ng-cloak layout="column" style="height: 90%;    width: 40%;">

	<md-toolbar>
		<div class="md-toolbar-tools">
			<h1>{{translate.load('sbi.crossnavigation.selectDocument')}}</h1>
			<span flex></span>
			<md-button ng-click="closeDialog()"> {{translate.load('sbi.general.cancel')}} </md-button>
		</div>
	</md-toolbar>
	 
	<md-content  flex layout>
		<angular-table flex	id="docList" 
			ng-model="listDoc" item-name="DOCUMENT_NAME"
			show-item-tooltip="false" highlights-selected-item="true"
			columns='[{"label":"Label","name":"DOCUMENT_LABEL"}, {"label":"Name","name":"DOCUMENT_NAME"}]'
			show-search-bar="true"
			no-pagination=false
			columns-search='["DOCUMENT_LABEL","DOCUMENT_NAME"]'
			total-item-count = totalCount
			scope-functions = tableFunction 
			page-changed-function="changeDocPage(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering)"
			search-function="changeDocPage(searchValue, itemsPerPage, 0, columnsSearch, columnOrdering, reverseOrdering)"
			click-function="clickOnSelectedDoc(item,listId,closeDialog)">
		</angular-table>
	</md-content>	 
	
	 

<div ng-show="loading" class="loadingSpinner">
    <i class="fa fa-spinner fa-pulse fa-4x"></i> 
</div>
</md-dialog>
</script>
	<angular-list-detail show-detail="showme"> 
	<list  label='translate.load("sbi.menu.list")' new-function="createMenu" show-new-button="true"
	style="max-width:30%">
		<md-content style= "height: 100%">
	<script type="text/ng-template" id="nodes_renderer.html">
  

<div layout = "row" layout-align="start center" class="customTreeNode" ui-tree-handle 
	ng-class="{'hoveredElement':(node.menuId==nodeTemp.menuId && node.menuId!=null), 'selectedElement':(nodeTempT.menuId==node.menuId && node.menuId!=null)}" ng-mouseenter="mouseenter(node)"
 	ng-mouseleave="mouseleave()"  >
	<div layout = "row" layout-align="start center" >
		<md-icon style="color:#3b678c" ng-if="node.menuId==null" md-font-icon=" fa fa-desktop">
		</md-icon>	
		<md-icon ng-click='tooggle(this)' ng-if="node.lstChildren && node.lstChildren.length > 0"    
        	 ng-class="{
          	'fa fa-plus-square': collapsed,
          	'fa fa-minus-square': !collapsed 
        		}"></md-icon>
		<md-icon  ng-if="node.lstChildren.length ==0" md-font-icon=" fa fa-square">
		</md-icon>
  		<div ng-style="{'color' : (node.menuId==null) ? '#3b678c' : ''}" ng-click="showSelectedMenu(node)" class="line-container">
					<span> &nbsp;{{node.name}}</span> 
		</div>

    </div>
	<div  class="menu-container" layout="row" layout-align="end center" style="flex:1">

		<div class="icon-container" ng-show="node.menuId==nodeTemp.menuId" >
			<md-icon  ng-if="canBeMovedUp(node)" ng-click= "moveUp(node)" md-font-icon="fa fa-arrow-up">
			</md-icon>   
			<md-icon   ng-if="canBeMovedDown(node)" ng-click= "moveDown(node)" md-font-icon="fa fa-arrow-down">
			</md-icon>  
			<md-icon   ng-if="canBeChangedWithFather(node)" ng-click= "changeWithFather(node)" md-font-icon="fa fa-random" ">
			</md-icon>
			<md-icon ng-click="deleteMenu(node)"  ng-if="node.lstChildren.length==0  && node.menuId!=null" md-font-icon="fa fa-trash">
			</md-icon>    
		</div>
	</div>
</div>


      <ol ui-tree-nodes="" ng-model="node.lstChildren" ng-class="{hidden: collapsed}">
        <li data-nodrag ng-repeat="node in node.lstChildren" ui-tree-node ng-include="'nodes_renderer.html'">
        </li>
      </ol>
	</script>
		 
		
		
		<div class="row">
      <div class="col-sm-6">
        <div ui-tree id="tree-root">
          <ol ui-tree-nodes ng-model="listOfMenu_copy">
            <li data-nodrag ng-repeat="node in listOfMenu_copy" ui-tree-node ng-include="'nodes_renderer.html'"></li>
          </ol>
        </div>
      </div>
	</div>
	</md-content> 

	</list> 
	<detail label=' selectedMenu.name==undefined? "" : selectedMenu.name'  save-function="save" cancel-function="cancel"
		show-save-button="showme" show-cancel-button="showme"
		disable-save-button="!attributeForm.$valid"  >
		<div layout-fill class="containerDiv">
		
			<form name="attributeForm" 
				ng-submit="attributeForm.$valid && save()" class="detailBody ">
	
			 <md-card layout-padding ng-cloak  >
				<div flex=100>
				
					<md-input-container class="md-block"> <label>{{translate.load("sbi.menu.name")}}</label>
					<input name="code" ng-model="selectedMenu.name" ng-required="true"
						ng-maxlength="100" ng-change="setDirty()">
	
					<div ng-messages="attributeForm.id.$error"
						ng-show="selectedMenu.name == null">
						<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					</div>
					</md-input-container>
				</div>
				<div layout="row">
					<div flex=50>
						<md-input-container class="md-block"> <label>{{translate.load("sbi.menu.description")}}</label>
						<input required data-ng-model="selectedMenu.descr" 
							name="description" ng-maxlength="100" ng-change="setDirty()">
							<div class="hint">{{translate.load("sbi.menu.descriptionHint")}}</div>
						</md-input-container>
					</div>
					<div flex=50>
						<div ng-if="selectedMenu.level == 1">
							<div layout="row" layout-align="center center" class="kn-buttonBar">
								<md-button class="md-raised md-button-empty kn-primaryButton" ng-click="chooseMenuIcon($event)">
										<img ng-if="selectedMenu.custIcon" class="icon" ng-src="{{selectedMenu.custIcon.src}}" width="26" height="26"/>
										<i ng-if="!selectedMenu.custIcon && selectedMenu.icon" class="icon" ng-class="[selectedMenu.icon.className]"></i>
										<span ng-if="!selectedMenu.custIcon && !selectedMenu.icon">{{translate.load("sbi.menu.chooseIcon")}}</span>
								</md-button>
							
						        <md-button ng-click="removeIcon()" class="toolbar-button-custom md-raised kn-functionButton">
						        	<md-icon md-font-icon="fa fa-trash"></md-icon>
						        </md-button>
					      </div>
					        <!-- <input type="file" id="files" name="files" onchange="angular.element(this).scope().insertMenu(this)"/> -->
					        
						</div>
					</div>
				</div>		
			<div flex=100>
					<md-input-container class="md-block"> 
				<label>{{translate.load("sbi.menu.menuNodeContent")}}:</label>
				<md-select  ng-required="true" aria-label="aria-label" ng-model=selectedMenuItem.typeId ng-change="setFormDirty();setPropertiesForCombo()"> 
					<md-option ng-value="type.id" ng-repeat="type in allTypes" >{{type.label}}</md-option>
				</md-select>
			</md-input-container></div>
			<div flex=100 ng-if="selectedMenuItem.typeId==3">
				<md-input-container class="md-block"> <label>{{translate.load("sbi.menu.HTMLPage")}}:</label>
				<md-select aria-label="aria-label" ng-model=selectedMenuItem.page
					ng-change="setFormDirty()"> <md-option
					ng-repeat="type in files" ng-value="type.name" >{{type.name}}</md-option>
				</md-select></md-input-container>
			</div>


			<div flex=100 ng-if="selectedMenuItem.typeId==2">
				<md-input-container class="md-block"> <label>{{translate.load("sbi.menu.appUrl")}}</label>
				<input data-ng-model="selectedMenu.externalApplicationUrl"
					name="externalApplicationUrl" md-maxlength="1000"
					ng-change="setDirty()"> </md-input-container>
			</div>
			<div flex=100 ng-if="selectedMenuItem.typeId==1">
				<md-input-container flex class="md-block"> <label>{{translate.load("sbi.menu.document")}}</label>
				<input maxlength="100" type="text"
					ng-model="selectedMenu.document.fromDoc" readonly> <md-icon
					ng-click="listAllDocuments()" class="fa fa-search"></md-icon> </md-input-container>
			</div>
			
			
			
			<div flex=100 ng-if="selectedMenuItem.typeId==1">
					<md-input-container class="md-block"> <label>{{translate.load("sbi.menu.docParam")}}</label>
					<input data-ng-model="selectedMenu.objParameters" 
						name="objParameters"   md-maxlength="1000" ng-change="setDirty()">
					</md-input-container>
			</div>
			
		  <div flex=100 ng-if="selectedMenuItem.typeId==4">
					<md-input-container class="md-block"> 
				<label>{{translate.load("sbi.menu.chooseFunctionality")}}:</label>
				<md-select  ng-required="true" aria-label="aria-label" ng-model=selectedMenu.functionality ng-change="setFormDirty();"> 
					<md-option ng-value="func.code" value="func.name" ng-repeat="func in allFunctionalities" >{{func.name}}</md-option>
				</md-select>
			</md-input-container></div>
			
			<!--  INITAL PATH FOLDER -->
	 <div ng-if="selectedMenu.functionality == 'WorkspaceManagement'">
	 		<md-input-container class="md-block"> 
				<label>{{translate.load("sbi.menu.ws.initialPath")}}:</label>
				<md-select  ng-required="false" aria-label="aria-label" ng-model=selectedMenu.initialPath ng-change="setFormDirty();"> 
					<md-option ng-value="path.code" value="path.name" ng-repeat="path in allWorkspacePaths" >{{path.name}}</md-option>
				</md-select>
			</md-input-container>
	 </div>

			
	 <div ng-if="selectedMenu.functionality == 'DocumentUserBrowser'">
				<img style="margin: 0 0 -5px -6px;" src="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/img/treebase.gif")%>" alt="" /> 
				
				<span>{{translate.load("sbi.menu.folders")}}</span>
				
				<div id="docTree" ui-tree="" data-drag-enabled="false"
						data-drag-delay="false" data-empty-placeholder-enabled="false">
					
					<script type="text/ng-template" id="lowFunctionalityTreeNodeTemplate">						
						<div ui-tree-handle layout="row">
							<div class="indicator-child "></div>
							<span class="fa fa-folder-open-o" style="color: turquoise;"></span>
							<md-checkbox md-no-ink style="margin: -3px 0 0 5px;" aria-label="Checkbox 1" 
									ng-click="toggleDocFunct(selectedMenu, elementToIterate.path);"
									ng-checked="isChecked(elementToIterate.path, selectedMenu.initialPath, true)">
								{{elementToIterate.name}}
							</md-checkbox>
						</div>
						<ol ui-tree-nodes ng-model="elementToIterate" ng-if="elementToIterate.childs">
							<li ng-repeat="elementToIterate in elementToIterate.childs" 
									ui-tree-node class="figlioVisibile" 
									ng-include="'lowFunctionalityTreeNodeTemplate'"></li>
						</ol>
					</script>
					
					<ol id="olchiproot" ui-tree-nodes ng-model="folders">
						<li ng-repeat="elementToIterate in folders" ui-tree-node 
								ng-include="'lowFunctionalityTreeNodeTemplate'"></li>
					</ol>
					
				</div>
			</div>
			
			
			</md-card>  
		
			 <angular-table layout-fill id="menuRoles_id" ng-model="roles" columns='columnsArray'  no-pagination=true
					selected-item="role" highlights-selected-item="true"  scope-functions = tableFunction>
				
		</md-card>  
			</form>
			
		</div>
		
	
	</detail> </angular-list-detail>
</body>
</html>
