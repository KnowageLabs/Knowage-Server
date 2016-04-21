angular.module('kpi-style', ['ngMaterial','sbiModule','color.picker'])
.directive('kpiStyle', function() {
	return {
		templateUrl: '/knowagekpiengine/js/angular_1.x/style/template/kpiStyle.html',
		controller: kpiStyleController,
		scope: {
			ngModel:'=',
		},
		link: function (scope, elm, attrs) { 

		}
	}
});

function kpiStyleController($scope,$mdDialog,$q,$mdToast,$timeout,sbiModule_restServices,sbiModule_translate,sbiModule_config){
	$scope.translate=sbiModule_translate;
	
	$scope.measure = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1];
	$scope.fontFamily = ['Times New Roman','Georgia', 'Serif'];
	$scope.fontWeight = ['normal','bold','bolder','lighter','number','initial','inherit'];
	
	if($scope.ngModel.size==undefined){
		$scope.ngModel.size =1;
	}
	if($scope.ngModel.fontFamily==undefined){
		$scope.ngModel.fontFamily ='Times New Roman';
	}

}