(function () {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
	angular.module('kpi-style', ['ngMaterial','sbiModule','color.picker'])
	.directive('kpiStyle', function() {
		return {
//			templateUrl: '/knowagekpiengine/js/angular_1.x/style/template/kpiStyle.html',
			templateUrl: currentScriptPath + 'template/kpiStyle.html',
			controller: kpiStyleController,
			scope: {
				ngModel:'=',
			},
			link: function (scope, elm, attrs) { 
	
			}
		};
	});
	
	function kpiStyleController($scope,$mdDialog,$q,$mdToast,$timeout,sbiModule_restServices,sbiModule_translate,sbiModule_config){
		$scope.translate=sbiModule_translate;
		
		$scope.measure = ["4px","6px","8px","10px","12px","14px","16px","20px","24px","28px","34px"];
		$scope.fontFamily = ['Roboto','Times New Roman','Georgia', 'Serif','Verdana'];
		$scope.fontWeight = ['normal','bold','bolder','lighter','number','initial','inherit'];
		if($scope.ngModel.fontWeight==undefined){
			$scope.ngModel.fontWeight ='normal';
		}
		if($scope.ngModel.size==undefined){
			$scope.ngModel.size ="10px";
		}
		if($scope.ngModel.fontFamily==undefined){
			$scope.ngModel.fontFamily ='Times New Roman';
		}
	};

})();