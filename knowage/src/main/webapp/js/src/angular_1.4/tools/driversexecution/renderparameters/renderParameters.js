(function() {

	var driversExecutionModule = angular.module('driversExecutionModule');

		/*
		 * WARNING : You may initialize sbiModule_i18n in the main controller!
		 */
		driversExecutionModule.directive('renderParameters', ['sbiModule_config', 'sbiModule_i18n', function(sbiModule_config, sbiModule_i18n){

			return {
				restrict: 'E',
				templateUrl: sbiModule_config.dynamicResourcesBasePath
					+ '/angular_1.4/tools/driversexecution/renderparameters/template/renderParameters.html',
				controller: renderParametersController,
				scope: {
					param: '=',
					driverableObject: '=',
					haveAnalyticalDriversFn: '&?',
					analyticalDrivers: '=?',
				}
			}
		}]);

		var renderParametersController = function($scope, sbiModule_config, sbiModule_translate,driversExecutionService){

			$scope.sbiModule_translate = sbiModule_translate;
			$scope.driverableObject = $scope.driverableObject;

			// If param don't have value, use the default
			if (!$scope.param.value) {
				$scope.param.value = $scope.param.defaultValue;
			}

			 $scope.addParameter=function(driverUrl){
				 $scope.param.value = "$P{"+driverUrl+"}"
			 }
		}

	})();