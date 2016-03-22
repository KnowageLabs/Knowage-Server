	<md-content ng-controller="documentBrowserController" layout="column"  ng-cloak layout-fill>
		<!-- Toolbar -->
		<md-toolbar class="documentBrowserToolbar" >
				<div class="md-toolbar-tools" layout="row" layout-align="center center">
					<!-- Folders button -->
					<md-button class="md-icon-button" title="Folders" aria-label="Folders"  hide-gt-md ng-hide="showSearchView" ng-click="toggleFolders()">
						  <md-icon md-font-icon="fa fa-bars"></md-icon>
					</md-button>
					
					<!-- Title -->
<!-- 					<md-icon md-font-icon="fa  fa-folder-open-o fa-2x" layout-margin></md-icon> -->
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
	     				<document-tree ng-model="folders" create-tree="true" selected-item="selectedFolder"  click-function="setSelectedFolder(item)"  ></document-tree>
					</md-content>
				</md-sidenav>
	
				<md-content layout-margin flex layout="column"> 
				
					<bread-crumb  item-name='name' selected-item="selectedFolder" control='breadCrumbControl' move-to-callback=moveBreadCrumbToFolder(item,index)></bread-crumb>
					<h3 class="md-title" ng-show="folderDocuments.length==0" >{{translate.load("sbi.browser.document.noDocument")}}</h3> 
		 
				 	<document-view flex ng-model="folderDocuments"
						show-grid-view="showDocumentGridView"
						table-speed-menu-option="documentTableButton"
						selected-document=selectedDocument
						select-document-action="selectDocument(doc);"
						edit-document-action="editDocument(doc)"
						clone-document-action="cloneDocument(doc)"
						delete-document-action="deleteDocument(doc)"
						execute-document-action="executeDocument(doc)"
				 	  ></document-view>
		   	</md-content>
			</md-content>
				<md-content layout="column" flex ng-show="showSearchView">
					<h3 class="md-title" ng-show="searchInput.length==0 || searchInput==undefined " >{{translate.load("sbi.browser.document.noDocument")}}</h3>
					<h3 class="md-title" ng-show="searchInput.length>0">{{searchDocuments.length || 0}} {{translate.load("sbi.browser.document.found")}}</h3>
		 
			 		<document-view  flex ng-model="searchDocuments"
				 	 show-grid-view="showDocumentGridView"
				 	  table-speed-menu-option="documentTableButton"
				 	  selected-document=selectedDocument
				 	  select-document-action="selectDocument(doc);"
				 	  ></document-view>
  
				</md-content>
			
			<md-sidenav class="md-sidenav-right selectedDocumentSidenav md-whiteframe-4dp" md-component-id="right" md-is-locked-open="$mdMedia('gt-md')" ng-show="showDocumentDetails()">
					<md-toolbar class="secondaryToolbar">
	<!-- 					<h1 class="md-toolbar-tools" style="text-align:center; display:inline;">{{selectedDocument.name | limitEllipses:28}}</h1> -->
						<div layout="row" layout-align="space-around center">
							<md-button title="Execute Document" aria-label="Execute Document" class="md-icon-button" ng-click="executeDocument(selectedDocument)">
									<md-icon md-font-icon="fa fa-play-circle" ></md-icon>
							</md-button>
							
							<md-button title="Edit Document" aria-label="Edit Document" class="md-icon-button" ng-click="editDocument(selectedDocument)">
									<md-icon md-font-icon="fa fa-pencil"></md-icon>
							</md-button>
							
							<md-button title="Clone Document" aria-label="Clone Document" class="md-icon-button" ng-click="cloneDocument(selectedDocument)">
									<md-icon md-font-icon="fa fa-clone"></md-icon>
							</md-button>
							
							<md-button title="Delete Document" aria-label="Delete Document" class="md-icon-button" ng-click="deleteDocument(selectedDocument)">
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
		
	
	</md-content>
	