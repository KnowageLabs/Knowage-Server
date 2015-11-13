/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geo_module')

.directive('geoRigthMenu',function(sbiModule_config){
	return{
		 restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoRigthMenu/templates/geoRigthMenuTemplate.jspf',
		controller: geoRigthMenuControllerFunction,
//		require: "^geoMap",
		scope: {
			id:"@"
		},
		link: function(scope,elm,attrs){
			console.log("inizializzo geo-rigth-menu con id= "+scope.id);
			
		}
	}
});

function geoRigthMenuControllerFunction(layerServices,geo_dataset,$scope,$mdSidenav,$timeout,$mdDialog,$map,geo_template,geo_dataset,geo_filters,geo_indicators){	
	$scope.template=geo_template;
	$scope.dataset=geo_dataset;
	$scope.filters=geo_filters;
	$scope.indicators=geo_indicators;
	console.log("indicators",$scope.indicators);
	console.log("template",$scope.template);
	$scope.openRigthMenu=false;
	$scope.analysisTypeList=[{label:"Map point",type:"ProportionalSymbol",img:"fa fa-circle"},{label:"Map zone",type:"choropleth",img:"fa  fa-area-chart "}];
	
	if(!$scope.template.hasOwnProperty('analysisType')){
		$scope.template.analysisType=$scope.analysisTypeList[1].type;
	}
	
	
	
//	$scope.selectedIndicator={};
	
	$scope.toggleMenu=function(){
		$scope.openRigthMenu=!$scope.openRigthMenu;
		$timeout(function() {
			$map.updateSize();
		}, 500);
		
	}
    $scope.changeIndicator=function(item){
    	geo_template.selectedIndicator=item;
    	layerServices.updateTemplateLayer();
    }
    
    $scope.updateMap=function(){
    	console.log("updateMap",geo_template)
    	layerServices.updateTemplateLayer();
    }
    
	    
    $scope.indicatorIsSelected=function(item){
    	return angular.equals(geo_template.selectedIndicator, item);
    }
    
    $scope.openIndicatorFromCatalogue=function(ev){
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
    }
    
    $scope.IndicatorFromCatalogueController=function($scope, $mdDialog) {
    	  $scope.hide = function() {
    	    $mdDialog.hide();
    	  };
    	  $scope.cancel = function() {
    	    $mdDialog.cancel();
    	  };
    	  $scope.answer = function(answer) {
    	    $mdDialog.hide(answer);
    	  };
    	}
    
}