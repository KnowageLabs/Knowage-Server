var olapMod = angular.module('olap.controllers', [ 'olap.configuration',
		'olap.directives', 'olap.settings' ])

olapMod.controller("olapController", [ "$scope", "$timeout", "$window",
		"$mdDialog", "$http", '$sce', '$mdToast', '$mdSidenav',
		'sbiModule_messaging', 'sbiModule_restServices', 'sbiModule_translate',
		'olapSharedSettings', olapFunction ]);

function olapFunction($scope, $timeout, $window, $mdDialog, $http, $sce,
		$mdToast, $mdSidenav, sbiModule_messaging, sbiModule_restServices,
		sbiModule_translate, olapSharedSettings) {

	//VARIABLES

	$scope.translate = sbiModule_translate;
	//selected members
	$scope.members = [];
	$scope.selectedMember = {};

	templateRoot = "/knowagewhatifengine/html/template";
	$scope.sendMdxDial = "/main/toolbar/sendMdx.html";
	$scope.showMdxDial = "/main/toolbar/showMdx.html";
	$scope.sortSetDial = "/main/toolbar/sortingSettings.html";
	$scope.filterDial = "/main/filter/filterDialog.html"

	$scope.minNumOfLetters = olapSharedSettings.getSettings().minSearchLength;
	$scope.searchText = "";
	$scope.searchSucessText;
	$scope.showSearchInput = false;

	$scope.rows;
	$scope.maxRows = 3;
	$scope.topSliderNeeded;
	$scope.topStart = 0;

	$scope.columns;
	$scope.maxCols = 5;
	$scope.leftSliderNeeded;
	$scope.leftStart = 0;

	$scope.olapToolbarButtons = [];
	$scope.whatifToolbarButtons = [];
	$scope.tableToolbarButtons = [];

	$scope.filterCardList = [];
	$scope.filterSelected = [];
	$scope.dtData = [];
	$scope.dtTree = [];
	$scope.dtMaxRows = 0;
	$scope.dtAssociatedLevels = [];
	$scope.formulasData = [];
	$scope.valuesArray = [];
	$scope.selectedMDXFunction = null;
	$scope.selectedCrossNavigationDocument = null;
	$scope.cookieArray = [];
	$scope.propertiesArray = [];

	$scope.finalFormula = null;
	$scope.isFilterSelected = false;
	$scope.filterAxisPosition;
	$scope.showMdxVar = "";

	$scope.draggedFrom = "";
	$scope.dragIndex;

	$scope.doneonce = false;
	$scope.level;
	$scope.data = [];
	$scope.loadedData = [];
	$scope.dataPointers = [];
	$scope.numVisibleFilters = 5;
	$scope.shiftNeeded;

	$scope.modelConfig;
	$scope.filterDialogToolbarName;

	$scope.showSiblings = true;
	$scope.sortingSetting;
	$scope.ready = true;
	$scope.sortingEnabled = false;
	$scope.crossNavigationEnabled = false;
	$scope.sortingModes = [ {
		'label' : 'basic',
		'value' : 'basic'
	}, {
		'label' : 'breaking',
		'value' : 'breaking'
	}, {
		'label' : 'count',
		'value' : 'count'
	} ];
	$scope.selectedSortingMode = 'basic';
	$scope.sortingCount = 10;
	$scope.saveSortingSettings = function() {
		$mdDialog.hide();
		$scope.sortDisable();
	}
	$scope.loadingNodes = false;
	$scope.activeaxis;

	$scope.member;
	$scope.selecetedMultiHierUN;

	$scope.handleResponse = function(response) {
		source = response.data;
		$scope.modelConfig = source.modelConfig;
		console.log($scope.modelConfig);
		$scope.table = $sce.trustAsHtml(source.table)
		$scope.columns = source.columns;
		$scope.rows = source.rows;
		$scope.columnsAxisOrdinal = source.columnsAxisOrdinal;
		$scope.filterCardList = source.filters;
		$scope.hasPendingTransformations = source.hasPendingTransformations;

		$scope.rowsAxisOrdinal = source.rowsAxisOrdinal;
		$scope.showMdxVar = source.mdxFormatted;
		$scope.formulasData = source.formulas;
		$scope.ready = true;

	}

	$scope.sendModelConfig = function(modelConfig) {
		if ($scope.ready) {
			$scope.ready = false;
			sbiModule_restServices.promisePost(
					"1.0/modelconfig?SBI_EXECUTION_ID=" + JSsbiExecutionID, "",
					modelConfig).then(
					function(response) {
						$scope.table = $sce.trustAsHtml(response.data.table);
						$scope.modelConfig = response.data.modelConfig;
						$scope.ready = true;
						$scope.scrollTo($scope.modelConfig.startRow,
								$scope.modelConfig.startColumn);

					},
					function(response) {
						sbiModule_messaging.showErrorMessage(
								"An error occured while sending model config",
								'Error');
						$scope.ready = true;
					});

		}

	}

	$scope.startFrom = function(start) {
		if ($scope.ready) {
			$scope.ready = false;

			sbiModule_restServices.promiseGet(
					"1.0",
					'/member/start/1/' + start + '?SBI_EXECUTION_ID='
							+ JSsbiExecutionID).then(function(response) {
				$scope.table = $sce.trustAsHtml(response.data.table);
				$scope.ready = true;
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("error", 'Error');

			});
		}
	}

	/**
	 *Function for opening dialogs
	 **/
	$scope.showDialog = function(ev, path) {
		$mdDialog.show({
			scope : $scope,
			preserveScope : true,
			controllerAs : 'olapCtrl',
			templateUrl : templateRoot + path,
			targetEvent : ev,
			clickOutsideToClose : false
		});
	};

	$scope.closeDialog = function(e) {
		$mdDialog.hide();
	};

	$scope.getVersions = function() {
		sbiModule_restServices.promiseGet("1.0",
				'/version/?SBI_EXECUTION_ID=' + JSsbiExecutionID).then(
				function(response) {
					console.log(response);
					$scope.outputVersions = response.data;
				},
				function(response) {
					sbiModule_messaging.showErrorMessage("An error occured ",
							'Error');
				});
	};
}