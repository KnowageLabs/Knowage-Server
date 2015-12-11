/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
angular.module('geoModule')
.directive('geoMapMenu',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoMapMenu/templates/geoMapMenuTemplate.jspf',
		controller: geoMapMenuControllerFunction,
//		require: "^geoMap",
		scope: {
			id:"@"
		}
	}
});

function geoMapMenuControllerFunction(
		geoModule_layerServices, geoModule_dataset, $scope, $timeout, 
		$mdDialog, $map, geoModule_template, geoModule_filters, 
		geoModule_indicators, geo_interaction ,sbiModule_translate) {

	$scope.template = geoModule_template;
	$scope.dataset = geoModule_dataset;
	$scope.filters = geoModule_filters;
	$scope.indicators = geoModule_indicators;
	$scope.selectModeInteraction = geo_interaction;
	$scope.translate=sbiModule_translate;
	$scope.openRigthMenu = false;
	$scope.analysisTypeList = [
	                           {label:sbiModule_translate.load("gisengine.rigthMapMenu.analysisType.choropleth"),type:"choropleth",img:"fa  fa-area-chart "},
	                           {label:sbiModule_translate.load("gisengine.rigthMapMenu.analysisType.proportionalSymbol"),type:"proportionalSymbol",img:"fa fa-circle"},
	                           {label:sbiModule_translate.load("gisengine.rigthMapMenu.analysisType.chart"),type:"chart",img:"fa fa-bar-chart"}
	                           ];
	$scope.selectModeTypeList = [
	                             {label:sbiModule_translate.load("gisengine.rigthMapMenu.selectModeType.identify"), type:"identify"},
	                             {label:sbiModule_translate.load("gisengine.rigthMapMenu.selectModeType.cross"), type:"cross"}
	                             ];



	$scope.filterTypes = [
	                      {label: sbiModule_translate.load("gisengine.rigthMapMenu.spatialFilterType.near"), type:"near"},	
	                      {label: sbiModule_translate.load("gisengine.rigthMapMenu.spatialFilterType.intersect"), type:"intersect"},	
	                      {label: sbiModule_translate.load("gisengine.rigthMapMenu.spatialFilterType.inside"), type:"inside"}
	                      ];
	$scope.setSelectedFilterType = function(type) {
		//cambio geo_interaction con layer service
		//geo_interaction.selectedFilterType = type;
		geoModule_template.selectFilterType=type;
		geoModule_layerServices.setInteraction();
		if ($scope.$root.$$phase != '$apply') {
			$scope.$apply();
		}
	};

	$scope.isCrossRadioButtonDisabled = function(selectModeType) {
		var isCross = (selectModeType.toLowerCase() == "cross");
		var isCrossNavigationInTemplate = (geoModule_template.crossnav !== undefined);

		var isCrossRadioButtonOptionDisabled = (isCross && !isCrossNavigationInTemplate);

		return isCrossRadioButtonOptionDisabled;
	};

	if(!$scope.template.hasOwnProperty('analysisType')){
		$scope.template.analysisType = $scope.analysisTypeList[1].type;
	}

	$scope.updateMap = function(){
		$timeout(function() {
			geoModule_layerServices.updateTemplateLayer();
		}, 0);
	};

	$scope.indicatorIsSelected = function(item){
		return angular.equals(geoModule_template.selectedIndicator, item);
	};

	$scope.openIndicatorFromCatalogue = function(ev){
		$mdDialog.show({
			controller: $scope.IndicatorFromCatalogueController,
			templateUrl: 'indicatorFromCatalogueTemplate.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			clickOutsideToClose:true,
			openFrom: '#indicatorCatalogue',
			closeTo: '#indicatorCatalogue'
		})
		.then(function(answer) {
			console.log("then ok")
		}, function() {
			console.log("then cancel")
		});
	};

	$scope.IndicatorFromCatalogueController = function($scope, $mdDialog) {
		$scope.hide = function() {
			$mdDialog.hide();
		};
		$scope.cancel = function() {
			$mdDialog.cancel();
		};
		$scope.answer = function(answer) {
			$mdDialog.hide(answer);
		};
	};

	$scope.toggleIndicator = function (item){
		var index = $scope.indexInList(item, geoModule_template.selectedMultiIndicator);

		if(index == -1){
			geoModule_template.selectedMultiIndicator.push(item);
		}else{
			geoModule_template.selectedMultiIndicator.splice(index,1);
		}
		$scope.updateMap();
	};

	$scope.exist= function(item){
		return  $scope.indexInList(item, geoModule_template.selectedMultiIndicator)>-1;
	};

	$scope.indexInList = function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				//se nella lista è presente l'item è checked
				return i;
			}
		}
		return -1;
	};  
	

};

