(function() {

	var app = angular.module('geoTemplateBuild', [ 'ngMaterial',
			'angular_table', 'sbiModule', 'expander-box' ]);
	app.config([ '$mdThemingProvider', function($mdThemingProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
	} ]);

	app.controller('geoTemplateBuildController',
			[ '$scope', 'sbiModule_translate', 'sbiModule_restServices',
					'sbiModule_config', "$mdDialog",'sbiModule_messaging',
					geoTemplateBuildControllerFunction ]);

	function geoTemplateBuildControllerFunction($scope, sbiModule_translate,
			sbiModule_restServices, sbiModule_config, $mdDialog, sbiModule_messaging) {
		
		$scope.template=angular.fromJson(docTemplate);
		console.log($scope.template);
		$scope.docLabel=documentLabel;
      console.log($scope.docLabel);
      
      
        
		$scope.translate = sbiModule_translate;
		$scope.layerCatalogs = [];
		$scope.selectedLayer = [];
		$scope.selectedFilters = [];
		$scope.allFilters = [];

		// dataset variables
		$scope.selectedDatasetLabel = dataset;
		$scope.isDatasetChosen = $scope.selectedDatasetLabel != '';
		$scope.datasetFields = [];
		$scope.datasetJoinColumns = [];

		// indicators
		$scope.datasetIndicators = [];
		$scope.measureFields = [];
       
		// if there is no template at all
		$scope.editDisabled = $scope.template.mapName==undefined; 
		
		$scope.loadLayers = function() {
			sbiModule_restServices
					.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/layers", "")
					.then(
							function(response) {
								console.log(response.data.root);
								angular.copy(response.data.root,
										$scope.layerCatalogs);
								initializeSelectedLayer();
							},
							function(response) {
								sbiModule_restServices.errorHandler(
										response.data, "error loading layers");
							});
		}
		
		$scope.loadLayers();
		
		$scope.saveTemplate = function() {
			console.log("IN save template");
			var template = buildTemplate();
			if (template.error) {
				
//				var confirm = $mdDialog.confirm()
//				.title(sbiModule_translate.load("gisengine.designer.tempate.error"))
//				.content(template.error)
//				.ariaLabel('gisTemplateError') 
//				.ok(sbiModule_translate.load("gisengine.info.message.yes"));
//				
//				$mdDialog.show(confirm).then(function(){
//					$mdDialog.cancel();
//				});
             
				sbiModule_messaging.showWarningMessage(template.error,sbiModule_translate.load('gisengine.designer.tempate.error'));
	
				
			}else{
				// call service that will save the template
				//then redirect to gis document for configuring style
				var temp={};
				temp.TEMPLATE=template;
				temp.DOCUMENT_LABEL= $scope.docLabel;
				sbiModule_restServices
				.alterContextPath(sbiModule_config.externalBasePath);
		sbiModule_restServices.promisePost("restful-services/1.0/documents",
				"saveGeoReportTemplate", temp).then(
				function(response) {
					$scope.template=template;
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('gisengine.designer.tempate.save.message'),sbiModule_translate.load('gisengine.designer.tempate.save.success'));
				},
				function(response) {
					sbiModule_restServices.errorHandler(response.data,
							"error saving template");
				});
			}

			
		}

		$scope.editMap= function(){
			console.log("IN EDIT");
			
		}

		$scope.tableFunctionSingleLayer = {
			translate : sbiModule_translate,
			loadListLayers : function(item, evt) {
				
				$scope.newLayerCatalog = undefined;
				$mdDialog
						.show({
							controller : DialogControllerLayerList,
							templateUrl : '/knowagegeoreportengine/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateLayerList.html',
							clickOutsideToClose : false,
							preserveScope : true,
							scope : $scope,
							locals : {
								multiSelect : false
							}

						});
			}
		};

		$scope.tableFunctionMultiLayer = {
			translate : sbiModule_translate,
			loadListLayers : function(item, evt) {
				
				$scope.newLayerCatalog = [];
				$mdDialog
						.show({
							controller : DialogControllerLayerList,
							templateUrl : '/knowagegeoreportengine/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateLayerList.html',
							clickOutsideToClose : false,
							preserveScope : true,
							scope : $scope,
							locals : {
								multiSelect : true
							}
						});
			}
		};

		$scope.multipleLayerSpeedMenu = [ {
			label : 'remove',
			icon : 'fa fa-trash',
			action : function(item) {
				$scope.removeFromSelected(item);
				$scope.selectedFilters = [];
			}
		} ];

		$scope.removeFromSelected = function(item) {
			var index = null;
			for (var i = 0; i < $scope.selectedLayer.length; i++) {

				if (item.layerId == $scope.selectedLayer[i].layerId)
					index = i;
			}
			if (index != null) {
				$scope.selectedLayer.splice(index, 1);
			}

		}

		loadLayerFilters = function(layerId) {
			sbiModule_restServices
					.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/layers",
					"getFilter", "id=" + layerId).then(
					function(response) {

						for (var i = 0; i < response.data.length; i++) {
							$scope.allFilters.push(response.data[i]);
						}

					},
					function(response) {
						sbiModule_restServices.errorHandler(response.data,
								"error loading layer filters");
					});
		}

		$scope.loadAllFilters = function() {
			$scope.allFilters = [];
			for (var i = 0; i < $scope.selectedLayer.length; i++) {
				loadLayerFilters($scope.selectedLayer[i].layerId);
			}
		}

		$scope.tableFunctionFilters = {
			translate : sbiModule_translate,
			loadFilters : function(item, evt) {
				$scope.loadAllFilters();

				$scope.newFilter = [];
				$mdDialog
						.show({
							controller : DialogControllerFilter,
							templateUrl : '/knowagegeoreportengine/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateFilterList.html',
							clickOutsideToClose : false,
							preserveScope : true,
							scope : $scope
						});

			}
		};

		$scope.filtersSpeedMenu = [ {
			label : 'remove',
			icon : 'fa fa-trash',
			action : function(item) {
				$scope.removeFilterFromSelected(item);
			}
		} ];

		$scope.removeFilterFromSelected = function(item) {
			var index = null;
			for (var i = 0; i < $scope.selectedFilters.length; i++) {

				if (item.property == $scope.selectedFilters[i].property)
					index = i;
			}
			if (index != null) {
				$scope.selectedFilters.splice(index, 1);
			}
		}

		$scope.loadDatasetColumns = function(label) {
			sbiModule_restServices
					.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/1.0/datasets",
					label + "/fields").then(
					function(response) {
						console.log(response.data);
						angular.copy(response.data.results,
								$scope.datasetFields);
						$scope.loadMeasures();

					},
					function(response) {
						sbiModule_restServices.errorHandler(response.data,
								"error loading layer dataset columns");
					});
		}

		$scope.loadMeasures = function() {

			for (var i = 0; i < $scope.datasetFields.length; i++) {
				if ($scope.datasetFields[i].nature === "measure") {
					$scope.measureFields.push($scope.datasetFields[i]);
				}
			}
		}

		if ($scope.isDatasetChosen) {
			$scope.loadDatasetColumns($scope.selectedDatasetLabel);
			// $scope.loadMeasures();
		}

		$scope.tableFunctionsJoin = {
			translate : sbiModule_translate,
			datasetColumnsStore : $scope.datasetFields
		};

		$scope.tableFunctionsJoin.addJoinColumn = function() {
			var newRow = {
				datasetColumn : '',
				layerColumn : '',
				datasetColumnView : '<md-select ng-model=row.datasetColumn class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumnsStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
				layerColumnView : '<md-input-container class="md-block"><label>layer join column</label><input type="text" ng-model="row.layerColumn" required></md-input-container>'
			};

			$scope.datasetJoinColumns.push(newRow);

		}

		$scope.datasetJoinSpeedMenu = [ {
			label : 'remove',
			icon : 'fa fa-trash',
			action : function(item) {
				$scope.removeJoinFromSelected(item);
			}
		} ];

		$scope.removeJoinFromSelected = function(item) {
			var index = $scope.datasetJoinColumns.indexOf(item);
			if (index > -1) {
				$scope.datasetJoinColumns.splice(index, 1);
			}
		}

		// INDICATORS
		$scope.tableFunctionIndicator = {
			translate : sbiModule_translate,
			datasetMeasuresStore : $scope.measureFields
		};

		$scope.tableFunctionIndicator.addIndicator = function() {
			console.log($scope.tableFunctionIndicator.datasetMeasuresStore);
			var newRow = {
				indicatorName : '',
				indicatorLabel : '',
				indicatorNameView : '<md-select ng-model=row.indicatorName class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetMeasuresStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
				indicatorLabelView : '<md-input-container class="md-block"><label>indicator label</label><input type="text" ng-model="row.indicatorLabel"></md-input-container>'
			};

			$scope.datasetIndicators.push(newRow);

		}

		$scope.indicatorsSpeedMenu = [ {
			label : 'remove',
			icon : 'fa fa-trash',
			action : function(item) {
				$scope.removeIndicatorFromSelected(item);
			}
		} ];

		$scope.removeIndicatorFromSelected = function(item) {
			var index = $scope.datasetIndicators.indexOf(item);
			if (index > -1) {
				$scope.datasetIndicators.splice(index, 1);
			}
		}

		function buildTemplate() {
			var template ={};
			angular.copy($scope.template,template);

			if ($scope.mapName == undefined || $scope.mapName == '') {
				template.error = sbiModule_translate
						.load('gisengine.designer.tempate.noMapName');
				return template;
			} else {
				template.mapName = $scope.mapName;
			}

			if ($scope.isDatasetChosen) {
				// template building when dataset is selected
				if ($scope.selectedLayer.length == 0) {
					template.error = sbiModule_translate
							.load('gisengine.designer.tempate.nolayer');
					return template;
				} else {
					// from interface no more than one layer can be selected
					template.targetLayerConf=[];
					var layerConf = {};
					layerConf.label=$scope.selectedLayer[0].name;
					template.targetLayerConf.push (layerConf);
				}
				
				if($scope.datasetJoinColumns.length==0){
					template.error = sbiModule_translate
					.load('gisengine.designer.tempate.noJoinColumns');
					return template;
				}else{
					template.datasetJoinColumns="";
					template.layerJoinColumns="";
					for (var i = 0; i < $scope.datasetJoinColumns.length; i++) {
						template.datasetJoinColumns+=$scope.datasetJoinColumns[i].datasetColumn;
						template.layerJoinColumns += $scope.datasetJoinColumns[i].layerColumn;
						if(i != $scope.datasetJoinColumns.length-1){
							template.datasetJoinColumns+=",";
							template.layerJoinColumns += ",";
						}
					}
					
				}
				
				if($scope.datasetIndicators.length==0){
					template.error = sbiModule_translate
					.load('gisengine.designer.tempate.noIndicators');
					return template;
				}else{
					template.indicators=[];
					for (var i = 0; i < $scope.datasetIndicators.length; i++) {
						if($scope.datasetIndicators[i].indicatorName != '' && $scope.datasetIndicators[i].indicatorLabel != ''){
						var indicator={};
						indicator.name=$scope.datasetIndicators[i].indicatorName;
						indicator.label=$scope.datasetIndicators[i].indicatorLabel;
						template.indicators.push(indicator);
						}
						if(template.indicators.length==0){
							template.error = sbiModule_translate
							.load('gisengine.designer.tempate.noIndicators');
							return template;
						}
					}
				}
				
				

			} else {
				// template building when dataset is not selected
				if ($scope.selectedLayer.length == 0) {
					template.error = sbiModule_translate
							.load('gisengine.designer.tempate.nolayer');
					return template;
				} else {
					// from interface no more than one layer can be selected
					template.targetLayerConf = [];
					for (var i = 0; i < $scope.selectedLayer.length; i++) {
						var layer = {};
						layer.label = $scope.selectedLayer[i].name;
						template.targetLayerConf.push(layer);
					}

				}
				
				
				if($scope.selectedFilters.length==0){
					template.error = sbiModule_translate
					.load('gisengine.designer.tempate.nofilters');
					return template;
				}else{
					template.analitycalFilter=[];
					for (var i = 0; i < $scope.selectedFilters.length; i++) {
						template.analitycalFilter.push($scope.selectedFilters[i].property);
					}
				}
			}

			return template;
		}
		
		
		function initializeFromTemplate(){
			if($scope.template.mapName){
			$scope.mapName=$scope.template.mapName;
			}
			initializeDatasetJoinColumns();
			initializeIndicators();
			initializeLayerFilters();
			}

		function initializeSelectedLayer(){
	    	if($scope.isDatasetChosen){
				if($scope.template.targetLayerConf){
				for (var i = 0; i < $scope.layerCatalogs.length; i++) {
					if($scope.layerCatalogs[i].name === $scope.template.targetLayerConf[0].label){
						$scope.selectedLayer.push($scope.layerCatalogs[i]);
					}
				}
				}
			}else{
				if($scope.template.targetLayerConf){
					for (var i = 0; i < $scope.layerCatalogs.length; i++) {
						for (var j = 0; j < $scope.template.targetLayerConf.length; j++) {
							
						
						if($scope.layerCatalogs[i].name === $scope.template.targetLayerConf[j].label){
							$scope.selectedLayer.push($scope.layerCatalogs[i]);
						}
						}
					}
					}
			}
	    }
		function initializeDatasetJoinColumns(){
			if($scope.template.datasetJoinColumns && $scope.template.layerJoinColumns ){
				var dsJoinCols= $scope.template.datasetJoinColumns.split(',');
				var layerJoinCols= $scope.template.layerJoinColumns.split(',');
				
				for (var i = 0; i < dsJoinCols.length; i++) {
					var newRow = {
							datasetColumn : dsJoinCols[i],
							layerColumn :layerJoinCols[i],
							datasetColumnView : '<md-select ng-model=row.datasetColumn class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumnsStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
							layerColumnView : '<md-input-container class="md-block"><label>layer join column</label><input type="text" ng-model="row.layerColumn" required></md-input-container>'
						};

						$scope.datasetJoinColumns.push(newRow);
				}
			}
		}
		
		function initializeIndicators(){
			if($scope.template.indicators){
				for (var i = 0; i < $scope.template.indicators.length; i++) {
					var newRow = {
							indicatorName : $scope.template.indicators[i].name,
							indicatorLabel :$scope.template.indicators[i].label,
							indicatorNameView : '<md-select ng-model=row.indicatorName class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetMeasuresStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
							indicatorLabelView : '<md-input-container class="md-block"><label>indicator label</label><input type="text" ng-model="row.indicatorLabel"></md-input-container>'						
					};
					

					$scope.datasetIndicators.push(newRow);
			}
		}
		}
        
		function initializeLayerFilters(){
			if($scope.template.analitycalFilter){
				for (var i = 0; i < $scope.template.analitycalFilter.length; i++) {
					var filter={};
					filter.property=$scope.template.analitycalFilter[i];
					$scope.selectedFilters.push(filter);
				}
			}
		}
		
		initializeFromTemplate();
	}
	// dialog controllers
	function DialogControllerLayerList($scope, $mdDialog, multiSelect) {
		$scope.multiSelect = multiSelect;
		if (multiSelect == true) {
			$scope.newLayerCatalog = [];
		}
		$scope.closeDialog = function() {
			$scope.newLayerCatalog = undefined;
			$mdDialog.cancel();
		}

		$scope.changeSelectedLayer = function() {
			if ($scope.multiSelect == true) {
				
				for (var i = 0; i < $scope.newLayerCatalog.length; i++) {

					if (!checkIfExists($scope.newLayerCatalog[i])) {
						$scope.selectedLayer.push($scope.newLayerCatalog[i]);
						/**
						 * every time selected layers are changed selected filters will
						 * be cleared prevent case that selected filter can be related
						 * to unselected layer
						 */
						
						$scope.selectedFilters = [];
					}
				}
			
				
			} else {
				$scope.selectedLayer = [];
				$scope.selectedLayer.push($scope.newLayerCatalog);
			}

			$mdDialog.cancel();
		}

		function checkIfExists(elem) {
			for (var i = 0; i < $scope.selectedLayer.length; i++) {
				if ($scope.selectedLayer[i].layerId==elem.layerId){
					return true;
				}
			}
			return false;
		}

	}

	function DialogControllerFilter($scope, $mdDialog) {
		$scope.closeFilterDialog = function() {
			$scope.newFilter = [];
			$mdDialog.cancel();
		}

		$scope.changeSelectedFilters = function() {
			for (var i = 0; i < $scope.newFilter.length; i++) {

				if (!checkIfSelected($scope.newFilter[i])) {
					$scope.selectedFilters.push($scope.newFilter[i]);
				}
			}
			$mdDialog.cancel();
		}

		function checkIfSelected(elem) {
			for (var i = 0; i < $scope.selectedFilters.length; i++) {
				if (elem.property == $scope.selectedFilters[i].property)
					return true;
			}
			return false;
		}

	}

})();