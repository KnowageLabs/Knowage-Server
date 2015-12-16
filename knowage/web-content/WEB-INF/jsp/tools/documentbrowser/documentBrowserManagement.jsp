<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
	<!-- Styles -->
	<link rel="stylesheet" type="text/css"	href="/knowage/themes/glossary/css/generalStyle.css">
	<link rel="stylesheet" type="text/css" href="/knowage/themes/documentbrowser/css/md-data-table.min.css">
	<link rel="stylesheet" type="text/css" href="/knowage/themes/documentbrowser/css/documentBrowser.css">
	
	<!--
	<link rel="stylesheet" type="text/css" href="/knowage/themes/documentbrowser/css/style.css">
	<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons"/>
	-->
	
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js"></script>
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/documentBrowser.js"></script>
	
	<title>Document Browser</title>
</head>

<body class="bodyStyle" ng-app="documentBrowserModule" id="ng-app">
	<div layout="column" ng-controller="documentBrowserController as ctrl" ng-cloak>
		<div>folderDocuments.length = {{folderDocuments.length}}</div>
		<div>searchDocuments.length = {{searchDocuments.length}}</div>
		<div>showSearchResultHeader() = {{showSearchResultHeader()}}</div>
		<div>showDefaultHeader() = {{showDefaultHeader()}}</div>
		<div>showBreadcrumbHeader() = {{showBreadcrumbHeader()}}</div>
		
		<!-- Toolbar -->
		<md-toolbar class="header">
			<div class="md-toolbar-tools" layout="row" layout-align="center center">
			
				<!-- Folders button -->
				<md-button class="toolbar-button-custom" aria-label="Folders" style="min-width: 40px;" hide-gt-md ng-click="toggleFolders()">
					<i class="fa fa-bars" style="color:white"></i>
				</md-button>
				
				<!-- Title -->
				<h2 ng-hide="showSearchView">Document Browser</h2>
				
				<!-- Search back -->
				<md-button class="md-icon-button" aria-label="Back" style="margin-right:0px;" ng-show="showSearchView" ng-click="showSearchView=!showSearchView">
					<i class="fa fa-arrow-left" style="color:white"></i>
				</md-button>
				
				<!-- Search input -->
				<md-input-container md-no-float style="padding-bottom:0px;" flex ng-show="showSearchView">
					<input class="header" type="text" ng-model="searchInput" placeholder="Search documents" style="margin-left:5px;" ng-change="setSearchInput(searchInput)" key-enter="setSearchInput(searchInput)">
				</md-input-container>
				
				<!-- Search clear -->
				<md-button class="md-icon-button" aria-label="Clear" ng-show="showSearchView" ng-click="setSearchInput('')">
					<i class="fa fa-times" style="color:white"></i>
				</md-button>
				
				<span flex=""></span>
				
				<!-- Document view button -->
				<md-button title="{{showDocumentGridView?'List view':'Grid view'}}" class="toolbar-button-custom" ng-hide="showSearchView" ng-click="toggleDocumentView()">
					<i class="fa fa-th-list" style="color:white" ng-show="showDocumentGridView"></i>
					<i class="fa fa-th" style="color:white" ng-hide="showDocumentGridView"></i>
				</md-button>
				
				<!-- Search button -->
				<md-button title="Search" class="toolbar-button-custom" ng-hide="showSearchView" ng-click="showSearchView=!showSearchView">
					<i class="fa fa-search" style="color:white"></i>
				</md-button>
				
				<!-- Document Detail button-->
				<md-button title="Details" ng-class="{'md-raised':showDocumentDetail}" ng-click="setDetailOpen(!showDocumentDetail)" class="toolbar-button-custom" ng-disabled="!isSelectedDocumentValid()">
					<i class="fa fa-info-circle" style="color:white"></i>
				</md-button>
				
				<!-- Settings button-->
				<md-button title="Settings" class="toolbar-button-custom" ng-click="alert('Settings')">
					<i class="fa fa-cog header"></i>
				</md-button>
			</div>
		</md-toolbar>

		<section layout="row" flex>
			
			<md-sidenav class="md-sidenav-left" md-component-id="left" md-is-locked-open="$mdMedia('gt-md')" ng-hide="showSearchView">
				<md-toolbar class="header">
        			<h3 class="md-toolbar-tools">Folders</h3>
      			</md-toolbar>
      			<document-tree ng-model="folders" create-tree="true" click-function="setSelectedFolder(item)" multi-select="false"></document-tree>
			</md-sidenav>
			
			<md-content layout="column" flex>
				<div ng-include="'search_result_header.html'" ng-show="showSearchResultHeader()"></div>
				<div layout="row" ng-include="'breadcrumbs_header.html'" ></div>
				<div ng-include="'no_documents.html'" ng-show="showDefaultHeader()"></div>
				
				<!-- Document List View -->
				<div layout="column" ng-hide="showDocumentGridView" ng-class="{'doc-list-border': folderDocuments.length>0}" flex> 
					<md-data-table-container ng-show="folderDocuments.length>0">
						<table md-data-table>
							<thead md-order="documentsOrderProperty" style="height: 75px;">
								<tr>
									<th name="Type" order-by="typeCode"></th>
									<th  name="Name" order-by="name"></th>
									<th  name="Author" order-by="creationUser"></th>
									<th  name="Date"  order-by="creationDate"></th>
								</tr>
							</thead>
							<tbody>
								<tr md-auto-select ng-repeat="document in folderDocuments | orderBy: documentsOrderProperty" ng-click="selectDocument(document)" ng-dblclick="alert('Executing '+document.name+'...')" ng-class="{'selected-doc':wasSelected(document)}">
									<td>{{document.typeCode}}</td>
									<td>{{document.name}}</td>
									<td>{{document.creationUser}}</td>
									<td>{{document.creationDate | limitTo: 10}}</td>
								</tr>
							</tbody>
						</table>
					</md-data-table-container>
				</div>
				
				<!-- Document Grid View -->
				<div layout="column" ng-show="showDocumentGridView" flex>
					<div layout="row" layout-padding layout-wrap layout-fill style="padding-bottom: 32px;">
						<md-whiteframe flex="25" layout layout-align="center center" ng-repeat="document in folderDocuments">
							<md-card style="width: 150px;" ng-click="selectDocument(document)" ng-dblclick="alert(document.name)" ng-class="{'selected-doc':wasSelected(document)}">
								<!-- <img src="preview-images/{{document.PREVIEW_FILE}}" class="md-card-image" alt="{{document.name}}" style="width: 150px; height: 126px;"></img> -->
								<md-card-content style="padding:0px; padding-left:2px; text-align:center;">
									<div>{{document.name | limitEllipses:20}}</div>
									<div>{{document.creationUser | limitEllipses:20}}</div>
									<div>{{document.creationDate}}</div>
								</md-card-content>
							</md-card>
						</md-whiteframe>
					</div>
				</div>
			</md-content>
			
			<!-- Document Search View -->
				<div layout="column" ng-show="showSearchView" ng-class="{'doc-list-border': searchDocuments.length>0}" flex> 
					<md-data-table-container ng-show="searchDocuments.length>0">
						<table md-data-table>
							<thead md-order="documentsOrderProperty" style="height: 75px;">
								<tr>
									<th name="Type" order-by="typeCode"></th>
									<th  name="Name" order-by="name"></th>
									<th  name="Author" order-by="creationUser"></th>
									<th  name="Date"  order-by="creationDate"></th>
								</tr>
							</thead>
							<tbody>
								<tr md-auto-select ng-repeat="document in searchDocuments | orderBy: documentsOrderProperty" ng-click="selectDocument(document)" ng-dblclick="alert('Executing '+document.name+'...')" ng-class="{'selected-doc':wasSelected(document)}">
									<td>{{document.typeCode}}</td>
									<td>{{document.name}}</td>
									<td>{{document.creationUser}}</td>
									<td>{{document.creationDate | limitTo: 10}}</td>
								</tr>
							</tbody>
						</table>
					</md-data-table-container>
				</div>
		
			<md-sidenav class="md-sidenav-right selected-doc" md-component-id="right" md-is-locked-open="$mdMedia('gt-md')" ng-show="showDocumentDetails()">
				<md-toolbar class="header" style="height: 75px;">
					<h1 class="md-toolbar-tools" style="text-align:center; display:inline;">{{selectedDocument.name | limitEllipses:28}}</h1>
					<div layout="row" layout-align="center center">
						<md-button title="Execute Document" class="toolbar-button-custom" ng-click="alert('Executing '+selectedDocument.name+'...')">
							<i class="fa fa-play-circle" style="color:white"></i>
						</md-button>
						
						<md-button title="Edit Document" class="toolbar-button-custom" ng-click="alert('Editing '+selectedDocument.name+'...')">
							<i class="fa fa-pencil" style="color:white"></i>
						</md-button>
						
						<md-button title="Clone Document" class="toolbar-button-custom" ng-click="alert('Cloning '+selectedDocument.name+'...')">
							<i class="fa fa-clone" style="color:white"></i>
						</md-button>
						
						<md-button title="Delete Document" class="toolbar-button-custom" ng-click="alert('Deleting '+selectedDocument.name+'...')">
							<i class="fa fa-trash-o" style="color:white"></i>
						</md-button>
					</div>
				</md-toolbar>
				
				<md-list>
					<md-list-item layout="row">
						<span flex="40"><b>Description:</b></span>
						<span flex="60">{{selectedDocument.description}}</span>
					</md-list-item>
					<md-list-item layout="row">
						<span flex="40"><b>State:</b></span>
						<span flex="60">{{selectedDocument.stateCode}}</span>
					</md-list-item>
					<md-list-item layout="row">
						<span flex="40"><b>Type:</b></span>
						<span flex="60">{{selectedDocument.typeCode}}</span>
					</md-list-item>
					<md-list-item layout="row">
						<span flex="40"><b>Author:</b></span>
						<span flex="60">{{selectedDocument.creationDate}}</span>
					</md-list-item>
				</md-list>
			</md-sidenav>
			
		</section>
	</div>
	
	<script type="text/ng-template" id="folders_renderer.html">
		<div ng-click="setSelectedFolder(folder)" class="customTreeNode" ui-tree-handle>
			<md-button  ng-class="{'md-primary':folder.selected,'md-raised':folder.selected}">
			<md-icon>{{folder.subfolders.length>0?(folder.showSubfolders?"&#xE313;":"&#xE315;"):""}}</md-icon>
			<md-icon>&#xE2C7;</md-icon>&nbsp;{{folder.NAME}}
			</md-button>
		</div>
		<ol ui-tree-nodes="" ng-model="folder.subfolders" ng-show="folder.showSubfolders">
			<li ng-repeat="folder in folder.subfolders" ui-tree-node ng-include="'folders_renderer.html'"></li>
		</ol>
	</script>
	
	<script type="text/ng-template" id="search_result_header.html">
		<h3 class="md-title">{{searchDocuments.length}} documents found.</h3>
	</script>

	<script type="text/ng-template" id="no_documents.html">
		<h3 class="md-title">No documents to display.</h3>
	</script>

	<!-- Should be included in a div with layout row-->
	<script type="text/ng-template" id="breadcrumbs_header.html">
		<div ng-repeat="folderChild in getFolderAncestors(selectedFolder)">
			<span ng-hide="$first">&gt;</span>
			<md-button ng-click="setSelectedFolder(folderChild)">{{folderChild.NAME}}</md-button>
		</div>
 	</script>
</body>
</html>