/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geoModule')
.directive('geoLegend',function(sbiModule_config ){
	return{
		restrict: "E",
//		replace: true,
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoLegend/templates/geoLegendTemplate.jspf',
		controller: geoLegendControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		}
	}
})

function geoLegendControllerFunction($scope,$mdDialog,geoModule_template,geoModule_thematizer,geoModule_layerServices){	
	$scope.showLegend=false;
	$scope.thematizer=geoModule_thematizer;
	$scope.legendItem=[];

	$scope.$watch(function() {
		return  geoModule_template.analysisConf.choropleth;
	}, function(newValue, oldValue) {
		if (newValue != oldValue) {
			geoModule_layerServices.updateTemplateLayer('choropleth');
	}
	}, true);
	
	$scope.$watch(function() {
		return  geoModule_template.analysisConf.proportionalSymbol;
	}, function(newValue, oldValue) {
		if (newValue != oldValue) {
			geoModule_layerServices.updateTemplateLayer('proportionalSymbol');
	}
	}, true);
	
	$scope.$watch(function() {
		return  geoModule_template.analysisConf.chart;
	}, function(newValue, oldValue) {
		if (newValue != oldValue) {
			geoModule_layerServices.updateTemplateLayer('chart');
	}
	}, true);

	$scope.toggleLegend=function(ev){
		$scope.showLegend=!$scope.showLegend;
	}


}