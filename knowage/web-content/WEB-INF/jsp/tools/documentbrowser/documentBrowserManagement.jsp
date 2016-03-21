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

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
	<!-- Styles -->
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 

<!-- 	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js"></script> -->
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>
	<!-- 	breadCrumb -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
	
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/documentBrowser.js"></script>
	
	
	 
</head>

<body   ng-app="documentBrowserModule" id="ng-app" layout="column" ng-controller="documentBrowserController" ng-cloak class="kn-documentBrowser">
	  	
		<!-- Toolbar -->
		<md-toolbar class="documentBrowserToolbar" >
			<div class="md-toolbar-tools" layout="row" layout-align="center center">
				<!-- Folders button -->
				<md-button class="md-icon-button" title="Folders" aria-label="Folders"  hide-gt-md ng-hide="showSearchView" ng-click="toggleFolders()">
					  <md-icon md-font-icon="fa fa-bars"></md-icon>
				</md-button>
				
				<!-- Title -->
				<md-icon md-font-icon="fa  fa-folder-open-o fa-2x" layout-margin></md-icon>
				<h1 ng-hide="showSearchView">{{translate.load("sbi.browser.title")}}</h1>
				<h1 ng-show="showSearchView">{{translate.load("sbi.browser.document.searchDocuments")}}</h1>
				
				<span flex=""></span>
				
			    
			    <!-- Search input -->
			    <md-input-container ng-show="showSearchView" class="searchInput">
					<label>{{translate.load("sbi.generic.search.title")}}</label>
					<input   type="text" id="searchInput" ng-model="searchInput" ng-change="setSearchInput(searchInput)" focus-on="searchInput">
				</md-input-container>
				
				<!-- Search clear -->
				<md-button class="md-icon-button" title="Clear" aria-label="Clear" ng-show="showSearchView" ng-click="setSearchInput('')">
					   <md-icon md-font-icon="fa fa-times"></md-icon>
				</md-button>
				
				<!--  Search button -->
				<md-button class="md-icon-button" title="Search" aria-label="Search" ng-class="{'selectedButton':showSearchView}" ng-click="toggleSearchView()">
					  <md-icon md-font-icon="fa fa-search"></md-icon>
				</md-button>
				
				<!-- Document view button -->
				<md-button class="md-icon-button"  ng-click="toggleDocumentView()" title="{{showDocumentGridView?'List view':'Grid view'}}" aria-label="{{showDocumentGridView?'List view':'Grid view'}}" >
					 <md-icon md-font-icon="fa" ng-class="showDocumentGridView ? 'fa-th-list' : 'fa-th'" ></md-icon>
				</md-button>
				
				 <!-- Document Detail button-->
				<md-button class="md-icon-button"  ng-class="{'selectedButton':showDocumentDetail}" ng-click="setDetailOpen(!showDocumentDetail)" ng-disabled="!isSelectedDocumentValid()" title="Details" aria-label="Details">
					 <md-icon md-font-icon="fa fa-info-circle"></md-icon>
				 </md-button>
				
				<!-- Settings button-->
				<md-button class="md-icon-button" title="Settings" aria-label="Settings" ng-click="alert('Settings')">
				 	<md-icon md-font-icon="fa fa-cog"></md-icon>
				</md-button>
				
			</div>
		</md-toolbar>
	
	<md-content layout="row" flex>
		<md-content layout="row" flex ng-show="!showSearchView">
			
			<md-sidenav class="md-sidenav-left md-whiteframe-4dp" md-component-id="left" md-is-locked-open="$mdMedia('gt-md')" >
				<md-toolbar class=" secondaryToolbar">
	       			<h3 class="md-toolbar-tools">{{translate.load("sbi.browser.filtrpanel.filtergroup.opt.folders")}}</h3>
     			</md-toolbar>
     			<md-content layout-margin>
     				<document-tree ng-model="folders" create-tree="true"   click-function="setSelectedFolder(item)"  ></document-tree>
				</md-content>
			</md-sidenav>

			<md-content layout-margin flex layout="column"> 
				
				<bread-crumb ng-model=folderBread item-name='name' selected-item="selectedFolder" control='breadCrumbControl' move-to-callback=moveBreadCrumbToFolder(item,index)></bread-crumb>
				
				<h3 class="md-title" ng-show="folderDocuments.length==0" >{{translate.load("sbi.browser.document.noDocument")}}</h3> 
	 
				<!-- Document List View -->
				 	<angular-table  ng-hide="showDocumentGridView || folderDocuments.length==0 " flex 
						id='documentListTable' ng-model=folderDocuments
						columns='[{"label":"Type","name":"typeCode"},{"label":"Name","name":"name"},{"label":"Author","name":"creationUser"},{"label":"Date","name":"creationDate"}]'
						columnsSearch='["name"]' 
						show-search-bar=false
						speed-menu-option=documentTableButton 
						highlights-selected-item="true"
						selected-item=selectedDocument
						click-function="selectDocument(item);">
					</angular-table>
				 
				
				
				<!-- Document Grid View -->
				<div layout="row" layout-wrap ng-hide="!showDocumentGridView " >
				<md-card class="documentCard" ng-repeat="document in folderDocuments">
		        <md-card-title>
			          <md-card-title-text>
			            <p class=" ellipsis">{{document.name}}</p>
			             <md-tooltip md-delay="1500">
			              {{document.name}}
			            </md-tooltip>
			          </md-card-title-text>
			        </md-card-title>
			        <div class="md-card-image document_browser_image_{{document.typeCode}}"  ></div>
			        <md-card-actions layout="row" layout-align="end">
			          <md-button class="md-icon-button" aria-label="tag">
			            <md-icon md-font-icon="fa  fa-tag fa-2x"></md-icon>
			          </md-button>
			        	<span flex></span>
			          <md-button class="md-icon-button" aria-label="Favorite" ng-click="selectDocument(document);">
			            <md-icon md-font-icon="fa fa-info-circle fa-2x"></md-icon>
			          </md-button>
			          <md-button class="md-icon-button" aria-label="Settings">
			            <md-icon md-font-icon="fa fa-star fa-2x" ng-click="document.pref=!document.pref" ng-init="document.pref==false" ng-class="{'preferiteDocumentIcon': document.pref==true}"></md-icon>
			          </md-button>
			          <md-button class="md-icon-button" aria-label="Share">
			            <md-icon md-font-icon="fa fa-trash fa-2x"></md-icon>
			          </md-button>
			        </md-card-actions>
			      </md-card>
 
				</div> 
			</md-content>
		 
		</md-content>
		
		<md-content layout="column" flex ng-show="showSearchView">
			<h3 class="md-title" ng-show="searchInput.length==0" >{{translate.load("sbi.browser.document.noDocument")}}</h3>
			<h3 class="md-title" ng-show="searchInput.length>0">{{searchDocuments.length || 0}} {{translate.load("sbi.browser.document.found")}}</h3>
	 
		<!-- Document Search View -->
			<angular-table  ng-hide="showDocumentGridView || searchDocuments==undefined || searchDocuments.length==0" flex 
				id='documentSearchTable' ng-model=searchDocuments
				columns='[{"label":"Type","name":"typeCode"},{"label":"Name","name":"name"},{"label":"Author","name":"creationUser"},{"label":"Date","name":"creationDate"}]'
				speed-menu-option=documentTableButton 
				highlights-selected-item="true"
				selected-item=selectedDocument
				highlights-selected-item="true"
				click-function="selectDocument(item);">
			</angular-table>
		
		</md-content>
		
		<md-sidenav class="md-sidenav-right selectedDocumentSidenav md-whiteframe-4dp" md-component-id="right" md-is-locked-open="$mdMedia('gt-md')" ng-show="showDocumentDetails()">
				<md-toolbar class="secondaryToolbar">
<!-- 					<h1 class="md-toolbar-tools" style="text-align:center; display:inline;">{{selectedDocument.name | limitEllipses:28}}</h1> -->
					<div layout="row" layout-align="space-around center">
						<md-button title="Execute Document" aria-label="Execute Document" class="md-icon-button" ng-click="executeDocument(selectedDocument)">
								<md-icon md-font-icon="fa fa-play-circle" ></md-icon>
						</md-button>
						
						<md-button title="Edit Document" aria-label="Edit Document" class="md-icon-button" ng-click="alert('Editing '+selectedDocument.name+'...')">
								<md-icon md-font-icon="fa fa-pencil"></md-icon>
						</md-button>
						
						<md-button title="Clone Document" aria-label="Clone Document" class="md-icon-button" ng-click="alert('Cloning '+selectedDocument.name+'...')">
								<md-icon md-font-icon="fa fa-clone"></md-icon>
						</md-button>
						
						<md-button title="Delete Document" aria-label="Delete Document" class="md-icon-button" ng-click="alert('Deleting '+selectedDocument.name+'...')">
								<md-icon md-font-icon="fa fa-trash-o"></md-icon>
						</md-button>
					</div>
				</md-toolbar>
				<md-content layout-margin>
					<md-list>
						 <md-list-item class="md-2-line">
						          <div class="md-list-item-text">
						            <h3><b>{{translate.load("sbi.generic.descr")}}</b></h3>
						            <p>{{selectedDocument.description}}</p>
						          </div>
						 </md-list-item>
						 <md-list-item class="md-2-line">
						          <div class="md-list-item-text">
						            <h3><b>{{translate.load("sbi.generic.state")}}</b></h3>
						            <p>{{selectedDocument.stateCode}}</p>
						          </div>
						 </md-list-item>
						 <md-list-item class="md-2-line">
						          <div class="md-list-item-text">
						            <h3><b>{{translate.load("sbi.generic.type")}}</b></h3>
						            <p>{{selectedDocument.typeCode}}</p>
						          </div>
						 </md-list-item>
						 <md-list-item class="md-2-line">
						          <div class="md-list-item-text">
						            <h3><b>{{translate.load("sbi.generic.creationdate")}}</b></h3>
						            <p>{{selectedDocument.creationDate}}</p>
						          </div>
						 </md-list-item>
					</md-list>
				</md-content>
			</md-sidenav>
	</md-content>
	
</body>
</html>
