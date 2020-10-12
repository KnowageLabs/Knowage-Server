/*
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

var olapMod = angular.module('olap.controllers', [ 'olap.configuration',
		'olap.directives', 'olap.settings', 'olap.services'])

olapMod.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

olapMod.config(['$locationProvider', function($locationProvider) {
	$locationProvider.html5Mode({ enabled: true, requireBase: false }); }]
)

olapMod.controller("olapController", [ "$scope", '$rootScope',"$timeout", "$window",
		"$mdDialog", "$http", '$sce', '$mdToast', '$mdSidenav', 'sbiModule_config',
		'sbiModule_messaging', 'sbiModule_restServices', 'sbiModule_translate','sbiModule_docInfo',
		'olapSharedSettings', 'indexChangingService','FiltersService', olapFunction ]);

function olapFunction($scope, $rootScope,$timeout, $window, $mdDialog, $http, $sce,
		$mdToast, $mdSidenav, sbiModule_config, sbiModule_messaging, sbiModule_restServices,
		sbiModule_translate,sbiModule_docInfo, olapSharedSettings, indexChangingService,FiltersService) {

	//VARIABLES
	var firstLoad = true;
	$scope.translate = sbiModule_translate;
	//selected members
	$scope.members = [];
	$scope.selectedMember = {};

	//templateRoot = "/knowagewhatifengine/html/template";
	templateRoot = sbiModule_config.contextName + "/html/template";
	$scope.sendMdxDial = "/main/toolbar/sendMdx.html";
	$scope.saveSubObjectDial = "/main/savesubobject/saving_subobject_dialog.html";
	$scope.showMdxDial = "/main/toolbar/showMdx.html";
	$scope.sortSetDial = "/main/toolbar/sortingSettings.html";
	$scope.filterDial = "/main/filter/filterDialog.html";
	$scope.saveAsNew = "/main/toolbar/saveAsNew.html";
	$scope.deleteVersionDialog = "/main/toolbar/deleteVersion.html";
	$scope.allocationAlgDialog = "/main/toolbar/allocationAlg.html";

	$scope.filterDialogWidth = olapSharedSettings.getSettings().filterDialogWidth;
	$scope.filterDialogHeight = olapSharedSettings.getSettings().filterDialogHeight;
	$scope.allowEditingCC = olapSharedSettings.getSettings().disableManualEditingCC;
	$scope.showWarningDT = olapSharedSettings.getSettings().showDTWarning;

	$scope.minNumOfLetters = olapSharedSettings.getSettings().minSearchLength;
	$scope.searchText = "";
	$scope.searchSucessText="";
	$scope.showSearchInput = false;

	$scope.rows=[];
	$scope.maxRows = 3;
	$scope.topSliderNeeded;
	$scope.topStart = 0;
	$scope.tableSubsets ={};
	$scope.columns=[];
	$scope.maxCols = 5;
	$scope.leftSliderNeeded;
	$scope.leftStart = 0;

	$scope.olapToolbarButtons = [];
	$scope.whatifToolbarButtons = [];
	$scope.tableToolbarButtons = [];

	$scope.filterCardList = [];
	$scope.filterSelected = [];
	$scope.usedOrdinal = "";
	$scope.dtData = [];
	$scope.dtTree = [];
	$scope.dtMaxRows = 0;
	$scope.dtAssociatedLevels = [];
	$scope.formulasData = [];
	$scope.valuesArray = [];
	$scope.selectedMDXFunction = {};
	$scope.selectedMDXFunctionName = "";
	$scope.selectedTab = 0;
	$scope.olapDocName = sbiModule_docInfo.label;
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
	$scope.index = 0;
	$scope.numVisibleFilters = 5;
	$scope.indexChangingSer = indexChangingService;
	$scope.shiftNeeded;

	$scope.modelConfig;
	$scope.modelConfigBuffer = [];
	$scope.filterDialogToolbarName;
	// flag for showing olap designer specific stuff
	$scope.olapMode = false;

	$scope.showSiblings = false;
	$scope.selectview = false;
	$scope.sortingSetting;
	$scope.ready = true;
	$scope.sortingEnabled = false;
	$scope.crossNavigationEnabled = false;
	$scope.sortingModes = [
	{
		'label' : 'no sorting',
		'value' : 'no sorting'
	},
	{
		'label' : 'basic',
		'value' : 'basic'
	},
	{
		'label' : 'breaking',
		'value' : 'breaking'
	},
	{
		'label' : 'count',
		'value' : 'count'
	} ];
	$scope.selectedSortingMode = 'no sorting';
	$scope.sortingCount = 10;
	$scope.saveSortingSettings = function() {
		if($scope.sortingCount<1||!$scope.sortingCount){
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.sortingSetting.count.error'), 'Error');

		}else{

			$mdDialog.hide();
			//$scope.sortDisable();
			switch($scope.selectedSortingMode) {
		    case 'no sorting':
		        if($scope.modelConfig.sortingEnabled){
		        	$scope.sortDisable();
		        }
		        break;
		    case 'basic':
		    case 'breaking':
		    case 'count':
		    	if($scope.modelConfig.sortingEnabled!=true){
		        	$scope.sortDisable();
		        }

		        break;
		    default:

		}
		}

	}
	$scope.loadingNodes = false;
	$scope.activeaxis;

	$scope.member;
	$scope.selecetedMultiHierUN;
	$scope.selectedVersion = null;

	$scope.buffer = null;
	$scope.max =0;

	$scope.handleResponse = function(response) {




		source = response.data;
		handleCalculatedFields(source);
		if($scope.modelConfig&&$scope.modelConfig.pagination){
			$scope.tableSubsets=source.tables;
			$scope.tableSubsets=null;
			$scope.buffer = {};

			$scope.tableSubsets=response.data.tables;
			for(var x in $scope.tableSubsets){
				$scope.buffer[x]=$scope.tableSubsets[x];
				var intx = parseInt(x);
				if(intx>$scope.max){
					$scope.max = intx;
				}
			}
			var startRow = $scope.modelConfig.startRow;
			if(startRow!=undefined&&!isNaN(startRow)){
				response.data.modelConfig.startRow  = startRow;
			}
			$scope.table = $sce.trustAsHtml($scope.buffer[$scope.modelConfig.startRow]);
		}else{
			$scope.table = $sce.trustAsHtml(source.table);
		}

		$scope.modelConfig = source.modelConfig;

		//$scope.table = {};

		//angular.copy($sce.trustAsHtml($scope.buffer[$scope.modelConfig.startRow]),$scope.table);

		$scope.columns = source.columns;
		$scope.rows = source.rows;
		$scope.columnsAxisOrdinal = source.columnsAxisOrdinal;
		$scope.filterCardList = source.filters;
		$scope.hasPendingTransformations = source.hasPendingTransformations;

		$scope.rowsAxisOrdinal = source.rowsAxisOrdinal;
		$scope.showMdxVar = source.mdxFormatted;
		
		//codemirror initializer
	    $scope.codemirrorLoaded = function(_editor) {
	        $scope._doc = _editor.getDoc();
	        $scope._editor = _editor;
	        _editor.focus();
	        $scope._doc.markClean()
	        _editor.on("beforeChange", function() {});
	        _editor.on("change", function() {});
	    };

	    //codemirror options
	    $scope.editorOptionsMdx = {
	        theme: 'eclipse',
	        lineWrapping: true,
	        readOnly: true,
	        lineNumbers: true,
	        mode: "text/x-sql",
	        onLoad: $scope.codemirrorLoaded
	    };
	    
	    $scope.MDXWithoutCF = source.MDXWITHOUTCF;
		$scope.formulasData = source.formulas;
		$scope.ready = false;

		$scope.selectedVersion = source.modelConfig.actualVersion;
		handleSlicers(source.filters);
		$scope.wiGridNeeded = response.data.modelConfig.whatIfScenario; //arsenije
		if(firstLoad && $scope.modelConfig != undefined){
			if(mode == 'full'){
				$scope.executeClicks();
				$scope.ready = true;
			}

			firstLoad = false;
		}
		source = null;
		$scope.ready = true;
	}

	var handleCalculatedFields = function(data){
		$scope.cookieArray.length = 0;
		$scope.calculatedFields = data.CALCULATED_FIELDS;


		for(index in data.CALCULATED_FIELDS){

			for(i in data.formulas){
				var formula = angular.copy(data.formulas[i])
				if(data.CALCULATED_FIELDS[index].formula.name === formula.name){

					for(j in formula.argument){
						formula.argument[j].default_value = data.CALCULATED_FIELDS[index].formula.arguments[j].defaultValue;
					}

					data.CALCULATED_FIELDS[index].formula = formula;
				}

			}
			data.CALCULATED_FIELDS[index].img = sbiModule_config.contextName + "/img/m.png";
			$scope.cookieArray.push(data.CALCULATED_FIELDS[index])
		}



	}

	handleSlicers = function(filters){
		$scope.filterSelected = [];
		for(var i=0; i<filters.length;i++){
			var hier = filters[i].hierarchies;
			var selPos = filters[i].selectedHierarchyPosition;
			var posInAx = filters[i].positionInAxis;
			var obj ={
					caption:"...",
					uniqueName:"",
					visible:false
					};
			if(hier[selPos].slicers.length > 0){
				obj.caption = hier[selPos].slicers[0].name;
				obj.uniqueName = hier[selPos].slicers[0].uniqueName;
				obj.visible = true;

				$scope.filterSelected[posInAx] = obj
			}
			else{
				$scope.filterSelected[posInAx] = obj;
			}

		}

		FiltersService.setFilters(filters);
	}



	$scope.sendModelConfig = function(modelConfig,noloading) {
		console.log("Sending model config" + " "+ modelConfig);


		if($scope.tableSubsets){
			$scope.tableSubsets.length = 0;
		}

		var sentStartRow = $scope.modelConfig.startRow;
		if ($scope.ready) {
			$scope.ready = false;
			sbiModule_restServices.promisePost("1.0/modelconfig?SBI_EXECUTION_ID=" + JSsbiExecutionID+"&NOLOADING="+noloading, "",
					modelConfig).then(

					function(response) {



						if(!$scope.buffer){
							$scope.buffer ={};
						}

						$scope.tableSubsets=response.data.tables;
						for(var x in $scope.tableSubsets){
							$scope.buffer[x]=$scope.tableSubsets[x];
							var intx = parseInt(x);
							if(intx>$scope.max){
								$scope.max = intx;
							}
						}
						if($scope.modelConfig&&$scope.modelConfig.pagination){
							var startRow = $scope.modelConfig.startRow;
							if(startRow!=undefined&&!isNaN(startRow)){
								response.data.modelConfig.startRow  = startRow;
							}

							angular.copy(response.data.modelConfig,$scope.modelConfig)
							if($scope.table){
								angular.copy($sce.trustAsHtml($scope.buffer[$scope.modelConfig.startRow]),$scope.table);
							}else{
								$scope.table = $sce.trustAsHtml($scope.buffer[$scope.modelConfig.startRow]);
							}
						}else{
							if($scope.table){
								$scope.table = $sce.trustAsHtml(response.data.table);
							}

						}





						//$scope.table = $sce.trustAsHtml(response.data.table);
//						if(!angular.equals(modelConfigTest,$scope.modelConfig)){
//							$scope.ready = true;
//							$scope.sendModelConfig($scope.modelConfig,true)
//							return;
//
//						}else{
//							angular.copy(modelConfigTest,$scope.modelConfig );
//						}
						//$scope.modelConfigBuffer.length = 0;

						$scope.ready = true;
						$scope.isScrolling = false;

					},
					function(response) {
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.modelConfig.error'), 'Error');
						$scope.ready = true;
					});

		}

	}

	$scope.$watch('modelConfig.startRow',function(newValue,oldValue){

		if(newValue!=undefined&&!isNaN(newValue)) {

			if($scope.buffer!=null&&
    		   		$scope.buffer[$scope.modelConfig.startRow]!=undefined&&
    		   		$scope.buffer[$scope.modelConfig.startRow]!=null){
    		   		var obj ={};
    		   		obj.text = 2;
    		   		if($scope.table){
						angular.copy($sce.trustAsHtml($scope.buffer[$scope.modelConfig.startRow]),$scope.table);
					}else{
						$scope.table = $sce.trustAsHtml($scope.buffer[$scope.modelConfig.startRow]);
					}
    		   	}else{
    		   		$scope.sendModelConfig($scope.modelConfig,false);
    		   	}

			if($scope.modelConfig.rowCount>$scope.max+1 && $scope.max<$scope.modelConfig.startRow+2*$scope.modelConfig.pageSize &&
        			$scope.modelConfig.startRow+2*$scope.modelConfig.pageSize<$scope.modelConfig.rowCount){
		   		$scope.sendModelConfig($scope.modelConfig, true);
        	}
		}


	},true)

	$scope.$watch('modelConfig.rowsSet',function(newValue,oldValue){
		if(newValue!=undefined&&!isNaN(newValue)&&newValue!=0) {


		   		$scope.sendModelConfig($scope.modelConfig, $scope.modelConfig.rowsSet===50);

		}


	},true)

		$scope.$watch('modelConfigBuffer',function(newValue,oldValue){

				if($scope.modelConfigBuffer.length>0){
					$scope.sendModelConfig($scope.modelConfigBuffer[0],true);
				}


	},true);
	$scope.startFrom = function(start) {
		if ($scope.ready) {
			$scope.ready = false;

			sbiModule_restServices.promiseGet(
					"1.0",
					'/member/start/1/' + start + '?SBI_EXECUTION_ID='
							+ JSsbiExecutionID).then(function(response) {

				angular.copy($sce.trustAsHtml(response.data.table),scope.table);
				$scope.ready = true;
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');

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
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');
				});
	};

}