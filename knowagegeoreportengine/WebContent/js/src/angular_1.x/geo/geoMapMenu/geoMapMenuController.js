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
		},
		link: function(scope,elm,attrs){
			console.log("inizializzo geo-map-menu con id= " + scope.id);

		}
	}
});

function geoMapMenuControllerFunction(
		geoModule_layerServices, geoModule_dataset, $scope, $mdSidenav, $timeout, 
		$mdDialog, $map, geoModule_template, geoModule_dataset, geoModule_filters, 
		geoModule_indicators, geo_interaction ) {
	
	$scope.template = geoModule_template;
	$scope.dataset = geoModule_dataset;
	$scope.filters = geoModule_filters;
	$scope.indicators = geoModule_indicators;
	$scope.selectModeInteraction = geo_interaction;
	$scope.openRigthMenu = false;
	$scope.analysisTypeList = [
	                         {label:"Map point",type:"ProportionalSymbol",img:"fa fa-circle"},
	                         {label:"Map zone",type:"choropleth",img:"fa  fa-area-chart "}
	                         ];
	$scope.selectModeTypeList = [
	                           {label:"Identify", type:"identify"},
	                           {label:"Cross navigation", type:"cross"}
	                           ];
	
	if(!$scope.template.hasOwnProperty('analysisType')){
		$scope.template.analysisType = $scope.analysisTypeList[1].type;
	}

//	$scope.selectedIndicator = {};

//	$scope.changeIndicator = function(item){
////	geoModule_template.selectedIndicator = item;
//	geoModule_layerServices.updateTemplateLayer();
//	}

	$scope.updateMap = function(){
		console.log("updateMap",geoModule_template)
		geoModule_layerServices.updateTemplateLayer();
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
}