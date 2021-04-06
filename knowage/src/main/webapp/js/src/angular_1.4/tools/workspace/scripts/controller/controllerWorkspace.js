/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Dependencies for the Workspace main controller:
 * 		- document_viewer: Directive that provides possibility to execute a document in separate
 * 		iframe (window) that has a button for closing the executed document. When user do that,
 * 		the iframe closes and we are having the initial page (the one from which we wished to
 * 		execute the document).
 */
angular
	.module('workspace.controller', ['workspace.directive','workspace.configuration'])
	.controller('workspaceController', ["$scope","$http","$mdDialog","$timeout","$mdSidenav","$documentViewer","sbiModule_translate","sbiModule_restServices","sbiModule_config","sbiModule_user","sbiModule_messaging","$qbeViewer","toastr",workspaceFunction])
    .service('multipartForm',['$http',function($http){

		this.post = function(uploadUrl,data){

			var formData = new FormData();
    		for(var key in data){
    				formData.append(key,data[key]);
    			}
			return	$http.post(uploadUrl,formData,{
					transformRequest:angular.identity,
					headers:{'Content-Type': undefined}
				})
		}

	}]);


function workspaceFunction($scope, $http, $mdDialog, $timeout, $mdSidenav, $documentViewer, sbiModule_translate,
			sbiModule_restServices, sbiModule_config, sbiModule_user, sbiModule_messaging, toastr) {

	$scope.allDocuments = [];
	$scope.federationDefinitions=[];
	$scope.businessModels=[];
	$scope.favoriteDocumentsList = [];
	$scope.recentDocumentsList = [];

	/**
	 * variables for data management
	 */
	$scope.datasets=[];  //all
	$scope.myDatasets= [];
	$scope.enterpriseDatasets=[];
	$scope.sharedDatasets=[];
	$scope.notDerivedDatasets=[];

	/**
	 * smart filters
	 */
	$scope.smartFiltersList=[];

	$scope.searchInput = "";

	/**
	 * Variables for Analysis view of the Workspace (option):
	 * 		allAnalysisDocs - array of all Analysis documents available in user's workspace (Cockpit, Geo and Ad hoc)
	 * 		cockpitAnalysisDocs - array of all Analysis documents of type Cockpit available in user's workspace
	 * 		adhocReportAnalysisDocs - array of all Analysis documents of type Ad hoc available in user's workspace
	 * 		geoAnalysisDocs - array of all Analysis documents of type Geo available in user's workspace
	 */
	$scope.allAnalysisDocs = [];
	$scope.cockpitAnalysisDocs = [];
	$scope.adhocReportAnalysisDocs = [];
	$scope.geoAnalysisDocs = [];
	$scope.allAnaylticsDocsTypes = ["MAP","DOCUMENT_COMPOSITE"];

	/**
	 * currentOptionMainMenu - 	which of all available perspectives (options) from the left menu is selected (picked)
	 * 							by the user.
	 * activeTabAnalysis - 		which of all three available tabs for Analysis perspective (option) is active at the moment
	 * 							(ALL, COCKPIT, GEO).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	(whereAreWeComingFrom == "NewCockpit") ? $scope.currentOptionMainMenu = "analysis" : $scope.currentOptionMainMenu = "recent";

	if(initialOptionMainMenu != undefined && initialOptionMainMenu != ''){
		$scope.currentOptionMainMenu = initialOptionMainMenu;
	}


	$scope.resetOption = "recent";

	$scope.isDocumentFavorite = false;

	/**
	 * Flag that servers as indicator for toggling between grid and list view of documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	if(sbiModule_config.workspaceShowGrid != undefined &&
			sbiModule_config.workspaceShowGrid == "false")
		$scope.showGridView = false;
	else
		$scope.showGridView = true;

	// @author Davide Vernassa toggle navbar
		$scope.toggleLeftNav = function(){
			$mdSidenav('leftWorkspaceSideNav').toggle();
		}


	/**
	 * Flag is rised if user is searching for a document in the Organizer. Needed as indicator for handling the
	 * right-side navigation panel appearance and for the breadcrumb appearance control.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.organizerSearch = false;

	var recentDocumentsLoaded = false;
	var favoritesDocumentsLoaded = false;
	var recentDocumentsLoaded = false;
	var organizerDocumentsLoaded = false;
	$scope.datasetsDocumentsLoaded = false;
	var modelsDocumentsLoaded = false;
	var smartFiltersDocumentsLoaded = false;

	$scope.reloadMyData = false;

	$scope.clearSearch = false;
	$scope.resetSearchedData = true;

	/**
	 * Attached to the scope because we need this information inside the controller for managing the Analysis documents and interface after coming to the Workspace
	 * interface (web page) from the interface for the creation of a new Cockpit document.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.analysisDocumentsLoaded = false;

	$scope.translate = sbiModule_translate;

	/**
	 * Scope variables needed for showing details about the currently selected document in
	 * the Workspace. Details will be shown inside the right side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var selectedDocument = undefined;
	var showDocumentInfo = false;

	/**
	 * TODO: For Dataset wizard
	 */
	$scope.dataset = {};
	$scope.resultMetaDataStep2 = [];
	$scope.resultRowsStep2 = [];

	/**
	 * For the STEP 2: all available meta-data types and the inititially selected one (Columns).
	 * @editedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.metadataTypes=
	[
	 	{name:"Columns",value:"1"},
	 	{name:"Dataset",value:"2"}
 	];

	$scope.metadataType = $scope.metadataTypes[0];
	$scope.metadataId = 1;

	$scope.csvEncodingDefault = "UTF-8";
	$scope.csvDelimiterDefault = ",";
	$scope.csvQuoteDefault = "\"";
	$scope.skipRowsDefault = 0;
	$scope.limitRowsDefault = null;
	$scope.xslSheetNumberDefault = 1;
	$scope.dateFormatDefault = "dd/MM/yyyy";
	$scope.timestampFormatDefault = "dd/MM/yyyy HH:mm:ss";

	// The configuration for the message displayed inside the toaster. (danristo)
	$scope.toasterConfig = {
		timeOut: 3500,
		closeButton: true
	};

	/**
     * Initialize all the data needed for the 'dataset' object that we are sending towards the server when going to the Step 2 and ones that we are using
     * internally (such as 'limitPreviewChecked'). This initialization should be done whenever we are opening the Dataset wizard, since the behavior should
     * be the reseting of all fields on the Step 1.
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
	$scope.initializeDatasetWizard = function(dataset) {

		$scope.dataset.fileType = dataset!=undefined ? dataset.fileType : "";
		$scope.dataset.fileName = dataset!=undefined ? dataset.fileName : "";
		$scope.datasetInitialFileName = $scope.dataset.fileName;

		$scope.limitPreviewChecked = true;

		$scope.dataset.csvEncoding = dataset!=undefined ? dataset.csvEncoding : $scope.csvEncodingDefault;
		$scope.dataset.csvDelimiter = dataset!=undefined ? dataset.csvDelimiter : $scope.csvDelimiterDefault;
		$scope.dataset.csvQuote = dataset!=undefined ? dataset.csvQuote : $scope.csvQuoteDefault;

		$scope.dataset.skipRows = dataset!=undefined ? Number(dataset.skipRows) : Number($scope.skipRowsDefault);
		$scope.dataset.dateFormat = (dataset!=undefined && dataset.dateFormat!=undefined) ? dataset.dateFormat : $scope.dateFormatDefault;
		$scope.dataset.timestampFormat = (dataset!=undefined && dataset.timestampFormat!=undefined) ? dataset.timestampFormat : $scope.timestampFormatDefault;

		/**
		 * Handle the limitRows property value deserialization (special case: it can be of a value NULL).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (dataset!=undefined) {

			if (dataset.limitRows!=null && dataset.limitRows!="") {
				$scope.dataset.limitRows = Number(dataset.limitRows);
			}
			else {
				$scope.dataset.limitRows = dataset.limitRows;
			}
		}
		else {
			$scope.dataset.limitRows = $scope.limitRowsDefault;
		}

		$scope.dataset.xslSheetNumber = dataset!=undefined ? Number(dataset.xslSheetNumber) : Number($scope.xslSheetNumberDefault);

		$scope.dataset.catTypeVn = dataset!=undefined ? dataset.catTypeVn : "";
		$scope.dataset.catTypeId = dataset!=undefined ? Number(dataset.catTypeId) : null;

		$scope.dataset.id = dataset!=undefined ? dataset.id : "";
		$scope.dataset.label = dataset!=undefined ? dataset.label : "";
		$scope.dataset.name = dataset!=undefined ? dataset.name : "";
		$scope.dataset.description = dataset!=undefined ? dataset.description : "";
		$scope.dataset.meta = dataset!=undefined ? dataset.meta : [];

		$scope.dataset.persist =  (dataset != undefined && dataset.hasOwnProperty('isPersisted')) ? dataset.isPersisted : false;
		$scope.dataset.tableName = (dataset != undefined && dataset.persistTableName) ? dataset.persistTableName : "";

		$scope.dataset.fileUploaded = false;

	}

	$scope.initializeDatasetWizard();

	/**
	 * Static (fixed) values for three comboboxes that appear when the CSV file is uploaded.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.csvEncodingTypes =
	[
	 	{value:"windows-1252",name:"windows-1252"},
	 	{value:"UTF-8",name:"UTF-8"},
	 	{value:"UTF-16",name:"UTF-16"},
	 	{value:"US-ASCII",name:"US-ASCII"},
	 	{value:"ISO-8859-1",name:"ISO-8859-1"}
 	];

	$scope.csvDelimiterCharacterTypes =
	[
	 	{value:",",name:","},
	 	{value:";",name:";"},
	 	{value:"\\t",name:"\\t"},
	 	{value:"\|",name:"\|"}
 	];

	$scope.csvQuoteCharacterTypes =
	[
	 	{value:"\"",name:"\""},
	 	{value:"\'",name:"\'"}
 	];

	$scope.dateFormatTypes =
	[
	 	{value:"dd/MM/yyyy",name:"dd/MM/yyyy"},
	 	{value:"MM/dd/yyyy",name:"MM/dd/yyyy"},
	 	{value:"dd-MM-yyyy",name:"dd-MM-yyyy"},
	 	{value:"MM-dd-yyyy",name:"MM-dd-yyyy"},
		{value:"yyyy-MM-dd",name:"yyyy-MM-dd"},
	 	{value:"yyyy:MM:dd",name:"yyyy:MM:dd"},
	 	{value:"dd.MM.yyyy",name:"dd.MM.yyyy"},
	 	{value:"MM.dd.yyyy",name:"MM.dd.yyyy"}

	];

	$scope.timestampFormatTypes = [
		{ value:"dd/MM/yyyy HH:mm:ss", name:"dd/MM/yyyy HH:mm:ss" },
	 	{ value:"MM/dd/yyyy hh:mm:ss a", name:"MM/dd/yyyy hh:mm:ss a" },
	 	{ value:"dd-MM-yyyy hh:mm:ss a", name:"dd-MM-yyyy hh:mm:ss a" },
	 	{ value:"MM-dd-yyyy hh:mm:ss a", name:"MM-dd-yyyy hh:mm:ss a" },
		{ value:"yyyy-MM-dd hh:mm:ss a", name:"yyyy-MM-dd hh:mm:ss a" },
	 	{ value:"yyyy:MM:dd hh:mm:ss a", name:"yyyy:MM:dd hh:mm:ss a" },
	 	{ value:"dd.MM.yyyy HH:mm:ss", name:"dd.MM.yyyy HH:mm:ss" },
	 	{ value:"MM.dd.yyyy HH:mm:ss", name:"MM.dd.yyyy HH:mm:ss" }
	];


	/**
	 * Keep and change the values for three comboboxes that appear when user uploads a CSV file when creating a new Dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.chooseDelimiterCharacter = function(delimiterCharacterObj) {
		$scope.dataset.csvDelimiter = delimiterCharacterObj;
	}

	$scope.chooseQuoteCharacter = function(quoteCharacterObj) {
		$scope.dataset.csvQuote = quoteCharacterObj;
	}

	$scope.chooseEncoding = function(encodingObj) {
		$scope.dataset.csvEncoding = encodingObj;
	}

	$scope.chooseDateFormat = function(dateFormat) {
		$scope.dataset.dateFormat = dateFormat;
	}

	$scope.chooseTimestampFormat = function(timestampFormat) {
		$scope.dataset.timestampFormat = timestampFormat;
	}

	/**
	 * Function for toggling the state of the checkbox for the 'Limit preview' option (Step 1).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.toggleLimitPreview = function() {
		$scope.limitPreviewChecked = !$scope.limitPreviewChecked;
		return $scope.limitPreviewChecked;
	}

	/**
	 * On-click listener function for the left main menu of the Workspace web page.
	 * We will keep the lastly chosen option from this menu inside scope variable.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMenuItemPicked = function(item, reloadAnyway) {

		var newLeftMenuItemPicked = item.name.toLowerCase();

		/**
		 * If user picks the same option from the left menu as the one that is already selected, do nothing. The right detail
		 * panel (that contains documents, datasets and folders - depending on the option that is picked) will remain in the
		 * same state as before clicking.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.currentOptionMainMenu!=newLeftMenuItemPicked || reloadAnyway == true) {

			/**
			 * If user moves between the options in the left menu (changes option), the selected document/dataset/model should
			 * be deselected and the right side navigation panel should be hidden (removed). This means, that if user e.g. selects
			 * one document in the Analysis option and then goes to e.g. Models, the selected document will be deselected (will not
			 * be highlight) and the right navigation detail panel will not appear when returning back to Analysis option.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			$scope.selectedDocument = null;
			if($scope.selectDocument) $scope.selectDocument(undefined);

			$scope.showOrganizerDocumentInfo = null;
			if($scope.selectOrganizerDocument) $scope.selectOrganizerDocument(undefined);

			$scope.showDatasetInfo = null;
			if($scope.selectDataset) $scope.selectDataset(undefined);

			$scope.showModelInfo = null;
			if($scope.selectModel) $scope.selectModel(undefined);

			/**
			 * Handle the situation of clicking on the left menu option, keeping track of the state of the search input field. In the if-block
			 * we should reload the data - re-initialize (after clearing the search field).
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if ($scope.searchInput!="") {
				$scope.resetSearchedData = true;
				$scope.resetOption = ($scope.currentOptionMainMenu==item.name.toLowerCase()) ? item.name.toLowerCase() : $scope.currentOptionMainMenu;
			}
			else {

				/**
				 * Needed for reseting of the searched data when changing options in the left menu.
				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				$scope.resetSearchedData = true;
				$scope.resetOption = item.name.toLowerCase();
			}

			$scope.currentOptionMainMenu = item.name.toLowerCase();
			$scope.selectMenuItem(item);

			/**
			 * If the previously selected item from the left main menu was one of three suboptions of the 'Data' option (Datasets, Models, SmartFilters) and the newly selected
			 * item is not among those three, whilst the Data option is collapsed, unselect the Data option.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var isSuboptionActiveTemp = $scope.currentOptionMainMenu=="models" || $scope.currentOptionMainMenu=="datasets" || $scope.currentOptionMainMenu=="smartfilters";

			if (!isSuboptionActiveTemp && $scope.isSuboptionActive) {
				$scope.suboptionActive = false;
			}

			if (searchedBefore) {
				$scope.searchInput = "";
				$scope.setSearchInput($scope.searchInput);
			}

			/**
			 * Unselect the already selected document when changing the option in the left menu.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			$scope.selectedDocument = undefined;

			/**
			 * Provide loading for different Workspace options from the left main menu only when it is chosen (clicked)
			 * for the first time (after loading the main page). By default, the Recent documents are displayed first.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			switch($scope.currentOptionMainMenu) {

				case "analysis":

					if ($scope.analysisDocumentsLoaded==false) {
						console.info("[LOAD START]: Loading of Analysis documents is started.");
						$scope.loadAllMyAnalysisDocuments();
						$scope.analysisDocumentsLoaded = true;
					}

					break;

			case "schedulation":

					$scope.loadSchedulations();

					break;

				case "favorites":

					if (favoritesDocumentsLoaded==false) {
						console.info("[LOAD START]: Loading of Favorites documents is started.");
						$scope.loadFavoriteDocumentExecutionsForUser();
						favoritesDocumentsLoaded = true;
					}

					break;

				case "documents":

					if (organizerDocumentsLoaded==false) {
						console.info("[LOAD START]: Loading of Organizer documents is started.");
						$scope.loadAllFolders();
						organizerDocumentsLoaded = true;
					}

					break;

				case "datasets":

					if ($scope.datasetsDocumentsLoaded==false) {

						console.info("[LOAD START]: Loading of Datasets is started.");

//						$scope.loadDatasets();
						$scope.loadMyDatasets();
//						$scope.loadEnterpriseDatasets();
//						$scope.loadSharedDatasets();

						$scope.datasetsDocumentsLoaded = true;
						$scope.reloadMyData = false;

					}
					else {
						if ($scope.reloadMyData==true) {
							console.info("[LOAD]: My Datasets, because of reloading of MyData.");
//							$scope.loadDatasets();
							$scope.myDatasets = [];
							$scope.datasets = [];
				        	$scope.loadMyDatasets();

				        	$scope.reloadMyData = false;
						}
					}

					break;

				case "models":

					if (modelsDocumentsLoaded==false) {

						console.info("[LOAD START]: Loading of Models is started.");

						$scope.loadFederations();
						$scope.loadBusinessModels();

						modelsDocumentsLoaded = true;
					}

					break;

				case "smartfilters":

					if (smartFiltersDocumentsLoaded==false) {

						console.info("[LOAD START]: Loading of Smart filters is started.");
						/**
						 * TODO: Add functionality for loading all smart filters.
						 */
						$scope.loadSmartFilters();
						smartFiltersDocumentsLoaded = true;
					}

					break;

			}

		}

	}

	/**
	 * Function for toggling grid/list view by changing the responsible flag (showGridView).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.toogleGridListViewOfDocs = function() {
		$scope.showGridView = !$scope.showGridView;
	}

	/**
	 * Filter the sent collection of data (documents, analysis, datasets, etc.)
	 * according to the searching term (sequence) user entered, 'newSearchInput'.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var filterThroughCollection = function(newSearchInput,inputCollection,propertyName) {

		/**
		 * Resulting collection to return.
		 */
		var filteredCollection = [];

		if (inputCollection!=null) {

			var item = null;

			for (i=0; i<inputCollection.length; i++) {

				item = inputCollection[i];

				/**
				 * NOTE: If we want to search just according to the starting sequence of the name
				 * of a document, change this expression to this criteria: ... == 0).
				 */
				if (item[propertyName].toLowerCase().indexOf(newSearchInput.toLowerCase()) >= 0) {
					filteredCollection.push(item);
				}

			}

		}

		/**
		 * Set the flag for displaying a circular loading animation to true, so it can be shown
		 * when searching (filtering) is in progress.
		 */
		$scope.searching = false;

		return filteredCollection;
	}

	/**
	 * Flag to indicate if we need to reload data when changing between options on the left menu.
	 * For example, if user searched through the collection for one left menu option (e.g. Analysis)
	 * and after that goes to another option (e.g. Datasets), he need to clear the searching input
	 * field, as well as to reload all analysis documents that were filtered just before option change.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var searchedBefore = false;

	var searchCleanAndReload = function() {

		switch($scope.currentOptionMainMenu!=$scope.resetOption ? $scope.currentOptionMainMenu : $scope.resetOption) {

			/**
			 * SEARCH FOR ANALYSIS
			 */
			case "analysis":
				angular.copy($scope.cockpitAnalysisDocsInitial,$scope.cockpitAnalysisDocs);
				$scope.clearSearch = false;
				$scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
				$scope.organizerSearch = false;
				break;

			/**
			 * SEARCH FOR SCHEDULATION
			 */
			case "schedulation":
				angular.copy($scope.schedulationListInitial,$scope.schedulationList);
				$scope.clearSearch = false;
				$scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
				$scope.searchInput="";
				$scope.organizerSearch = false;
				break;

			/**
			 * SEARCH FOR DATASETS
			 */
			case "datasets":
				angular.copy($scope.datasetsInitial,$scope.datasets);
				angular.copy($scope.myDatasetsInitial,$scope.myDatasets);
				angular.copy($scope.enterpriseDatasetsInitial,$scope.enterpriseDatasets);
				angular.copy($scope.sharedDatasetsInitial,$scope.sharedDatasets);
				angular.copy($scope.ckanDatasetsListInitial,$scope.ckanDatasetsList);
				$scope.clearSearch = false;
				$scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
				$scope.organizerSearch = false;
				break;

			/**
			 * SEARCH FOR FAVORITES
			 */
			case "favorites":
				$scope.favoriteDocumentsList = $scope.favoriteDocumentsInitial;
				$scope.clearSearch = false;
				$scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
				$scope.organizerSearch = false;
				break;


			/**
			 * SEARCH IN SELECTED FOLDER IN THE ORGANIZER
			 */
			case "documents":
				$scope.clearSearch = false;
			    $scope.organizerSearch = false;
			    $scope.documentsOfSelectedFolder = $scope.documentsOfSelectedFolderInitial;
				$scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
	            break;

			/**
			 * SEARCH FOR RECENT
			 */
			case "recent":
				$scope.organizerSearch = false;
				$scope.clearSearch = false;
				$scope.recentDocumentsList = $scope.recentDocumentsInitial;
				$scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
				break;

			case "models":
				angular.copy($scope.federationDefinitionsInitial,$scope.federationDefinitions);
			    angular.copy($scope.businessModelsInitial,$scope.businessModels);
			    $scope.clearSearch = false;
			    $scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
			    $scope.organizerSearch = false;
			    break;

			case "smartfilters":
				  angular.copy($scope.smartFiltersListInitial,$scope.smartFiltersList);
				  $scope.clearSearch = false;
				  $scope.currentOptionMainMenu==$scope.resetOption ? $scope.resetSearchedData = false : null;
				  $scope.organizerSearch = false;
				break;
		}

	};

	/**
	 * Function for hiding a right-side navigation (detail) panel for all left menu options (wherever we are).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.hideRightSidePanel = function() {

		$scope.selectedDocument = null;
		$scope.selectDocument(undefined);

		$scope.showOrganizerDocumentInfo = null;
		$scope.selectOrganizerDocument(undefined);

		$scope.showDatasetInfo = null;
		$scope.selectDataset(undefined);

		$scope.showModelInfo = null;
		$scope.selectModel(undefined);

		$scope.showCkanInfo = null;
		$scope.selectCkan(undefined);

	}

	/**
	 * Function that is called when user is starting a search among some document collection (dataset,
	 * analysis, documents, etc.).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.setSearchInput = function(newSearchInput) {

		($scope.searchInput!="") ? $scope.resetSearchedData = true : null;

		var currentOptionMainMenu = $scope.currentOptionMainMenu;

		/**
		 * Collection through which we will search for diverse documents.
		 */
		var allAnalysisDocsTemp = null;
		var cockpitAnalysisDocsTemp = null;
		var geoAnalysisDocsTemp = null;

		var filteredCollection = [];

		var collectionForFiltering = null;

		// COMMENTED BY: danristo (DEPRECATED: no need to timout, since the 'debounce' is used over the model in the HTML)
//		$timeout
//		(
//			function() {

				if (newSearchInput=="") {

					/**
					 * If the search field is cleared (previously it had some content), unselect potentially selected document
					 * and close the right-side navigation panel. Do this for all documents, datasets and models in the Workspace
					 * (for all available options from the left menu).
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					if ($scope.searchInput!=""){

		//				$scope.selectedDocument = null;
		//				$scope.selectDocument(undefined);
		//
		//				$scope.showOrganizerDocumentInfo = null;
		//				$scope.selectOrganizerDocument(undefined);
		//
		//				$scope.showDatasetInfo = null;
		//				$scope.selectDataset(undefined);
		//
		//				$scope.showModelInfo = null;
		//				$scope.selectModel(undefined);

						$scope.hideRightSidePanel();

					}

					if ($scope.resetSearchedData==true) {
						$scope.clearSearch = true;
						searchCleanAndReload();
					}

				}
				else {

					$scope.searching = true;
					$scope.clearSearch = false;

					/**
					 * If the search is started, unselect potentially selected document and close the right-side navigation panel. Do this for all documents,
					 * datasets and models in the Workspace (for all available options from the left menu).
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					$scope.selectedDocument = null;
					$scope.selectDocument(undefined);

					$scope.showOrganizerDocumentInfo = null;
					$scope.selectOrganizerDocument(undefined);

					$scope.showDatasetInfo = null;
					$scope.selectDataset(undefined);

					$scope.showModelInfo = null;
					$scope.selectModel(undefined);

					!searchedBefore ? searchedBefore = true : null;

					switch(currentOptionMainMenu) {

						/**
						 * SEARCH FOR ANALYSIS
						 */
						case "analysis":
							$scope.cockpitAnalysisDocs = filterThroughCollection(newSearchInput,$scope.cockpitAnalysisDocsInitial,"name");
							$scope.searching = false;
							break;

						/**
						 * SEARCH FOR SCHEDULATION
						 */
						case "schedulation":
							$scope.schedulationList = filterThroughCollection(newSearchInput,$scope.schedulationListInitial,"jobName");
							$scope.searching = false;
							break;

						/**
						 * SEARCH FOR RECENT
						 */
						case "recent":
							$scope.recentDocumentsList = filterThroughCollection(newSearchInput,$scope.recentDocumentsInitial,"documentName");
							$scope.searching = false;
							break;

						/**
						 * SEARCH FOR FAVORITES
						 */
						case "favorites":
							$scope.favoriteDocumentsList = filterThroughCollection(newSearchInput,$scope.favoriteDocumentsInitial,"name");
							$scope.searching = false;
							break;

						case "documents":
							$scope.documentsOfSelectedFolder = filterThroughCollection(newSearchInput,$scope.documentsFromAllFolders,"documentName");
							$scope.searching = false;
							// Rise the flag that user is searching inside the Organizer. (author: danristo)
							$scope.organizerSearch = true;
							break;

						/**
						 * SEARCH FOR DATASETS
						 */
						case "datasets":
							$scope.datasets = filterThroughCollection(newSearchInput,$scope.datasetsInitial,"name");
							$scope.myDatasets = filterThroughCollection(newSearchInput,$scope.myDatasetsInitial,"name");
							$scope.enterpriseDatasets = filterThroughCollection(newSearchInput,$scope.enterpriseDatasetsInitial,"name");
							$scope.sharedDatasets = filterThroughCollection(newSearchInput,$scope.sharedDatasetsInitial,"name");
							$scope.ckanDatasetsList = filterThroughCollection(newSearchInput,$scope.ckanDatasetsListInitial,"name");
							$scope.searching = false;
							break;

						/**
						 * SEARCH FOR MODELS
						 */
						case "models":
							if($scope.currentModelsTab == 'federations') $scope.federationDefinitions = filterThroughCollection(newSearchInput,$scope.federationDefinitionsInitial,"name");
							if($scope.currentModelsTab != 'federations') $scope.businessModels = filterThroughCollection(newSearchInput,$scope.businessModelsInitial,"name");
							$scope.searching = false;
							break;


						case "smartfilters":
							$scope.smartFiltersList = filterThroughCollection(newSearchInput,$scope.smartFiltersListInitial,"name");
							$scope.searching = false;
							break;
					}

				}

				// COMMENTED BY: danristo (DEPRECATED: no need to timout, since the 'debounce' is used over the model in the HTML)
				//				}, 1000
				//			);

		/**
		 * Set the current search content to the new one. We are doing this on the end of the function, in order to have the
		 * correct information about the previous search sequence when comparing to the new one (ar the beginning).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.searchInput = newSearchInput;

	}

	/**
	 * Preview (execute) a particular document.
	 */
	$scope.executeDocument = function(document) {

		console.info("[EXECUTION]: Execution of document with the label '" + document.label + "' is started.");
		$documentViewer.openDocument(document.id, document.label, document.name, $scope);

		/**
		 * After opening (executing) a document listen for the 'documentClosed' event that will be fired from the 'documentViewer.js', i.e. the controller that the
		 * 'openDocument' function is referring to. The event will be fired when user closes an executed document. This information will be used to re-call the GET
		 * method towards the REST service that collects the last (recently) executed documents. This way the Workspace's RECENT view will be up-to-date.
		 *
		 * Also, load all folders and their content (document) for the DOCUMENTS (Organizer) view, because user could add an executed document to the Workspace.
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.$on(
						"documentClosed",

						function() {

							/**
							 * 'loadRecentDocumentExecutionsForUser'
							 * 		- reload all recent documents (since the new one is executed)
							 * 'loadAllFolders'
							 * 		- reload all folders (the content of the entire Organizer), since user might add
							 * 		the executed document (cockpit) to the Workspace, i.e. to its Organizer.
							 * 'loadAllMyAnalysisDocuments'
							 * 		- reload all Analysis documents (cockpits), since the executed document
							 * 		could be edited or the new one could be created according to the one that is executed.
							 *
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							$scope.loadRecentDocumentExecutionsForUser();
							$scope.loadAllFolders();
							$scope.loadAllMyAnalysisDocuments();
							$scope.hideRightSidePanel();

						}
					);

	}

	/**
	 * [START] Block of functions responsible for showing the details for
	 * currently selected document. Details will be shown inside the right
	 * side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var showDocumentDetails = function() {
		return showDocumentInfo && isSelectedDocumentValid();
	};

	isSelectedDocumentValid = function() {
		return selectedDocument !== undefined;
	};

	setDocumentDetailOpen = function(isOpen) {

		if (isOpen && !$mdSidenav('rightDoc').isLockedOpen() && !$mdSidenav('rightDoc').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		showDocumentInfo = isOpen;
	};

	$scope.toggleDocumentDetail = function() {
		$mdSidenav('rightDoc').toggle();
	};

	$scope.selectDocument= function ( document ) {

//		if (document !== undefined) {
//			$scope.lastDocumentSelected = document;
//		}

		var alreadySelected = (document !== undefined && selectedDocument === document);

		selectedDocument = document;

		if (alreadySelected) {
			selectedDocument=undefined;
			setDocumentDetailOpen(!showDocumentDetails);
		} else {
			setDocumentDetailOpen(document !== undefined);
		}
	};




	/**
	 * [END] Block of functions responsible for showing the details for
	 * currently selected document. Details will be shown inside the right
	 * side navigation panel.
	 */
}