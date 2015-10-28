/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geo_module')
.directive('geoRigthMenu',function(){
	return{
		 restrict: "E",
		templateUrl:'/athena/js/src/angular_1.4/tools/commons/geo/geoRigthMenu/templates/geoRigthMenuTemplate.jspf',
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

function geoRigthMenuControllerFunction($scope,$mdSidenav,$timeout,$mdDialog,$map){	
	
	$scope.openRigthMenu=false;
	$scope.mapTypeList=[{label:"point",type:"point",img:"fa fa-area-chart"},{label:"zone",type:"zone",img:"fa fa-circle"}];
	$scope.selectedIndicator={};
	
	
	
	$scope.toggleMenu=function(){
		$scope.openRigthMenu=!$scope.openRigthMenu;
		$timeout(function() {
			$map.updateSize();
		}, 500);
		
	}
    $scope.changeIndicator=function(item){
    	$scope.selectedIndicator=item;
    }
	    
    $scope.indicatorIsSelected=function(item){
    	return angular.equals($scope.selectedIndicator, item);
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