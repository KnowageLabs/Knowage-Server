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
		},
		link: function(scope,elm,attrs){
			console.log("inizializzo geo-legend con id= "+scope.id);

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

	

	$scope.toggleLegend=function(ev){
		$scope.showLegend=!$scope.showLegend;
	}


	

	$scope.IndicatorFromCatalogueController=function($scope){
//		$scope.colorList=[{label:"000000" ,  class:"COL000000"},
//		{label:"993300" ,  class:"993300"},
//		{label:"333300" ,  class:"333300"},
//		{label:"003300" ,  class:"003300"},
//		{label:"003366" ,  class:"003366"},
//		{label:"000080" ,  class:"000080"},
//		]
	}

}