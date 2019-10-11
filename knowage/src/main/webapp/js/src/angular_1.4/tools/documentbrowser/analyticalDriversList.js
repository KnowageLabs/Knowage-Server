/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	var app = angular.module("analyticalDriversListModule",["ngMaterial","angular_table","sbiModule"])
	app.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	 }]);
	app.controller("analyticalDriversListCTRL",analyticalDriversListFunction);
	analyticalDriversListFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging"];
	function analyticalDriversListFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging){

		//VARIABLES
		$scope.translate = sbiModule_translate;
		$scope.adList = [];
		$scope.selectedAD = {};

		//FUNCTIONS
		$scope.getDrivers = function(){ // service that gets list of drivers @GET
			sbiModule_restServices.promiseGet("2.0", "analyticalDrivers")
			.then(function(response) {
				$scope.adList = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
		}
		$scope.getDrivers();

		$scope.selectAD = function(item){
			$scope.selectedAD = angular.copy(item);
		}

		$scope.goBackandSave = function(){
			var tempSaveUrl = angular.copy(saveUrl);
			tempSaveUrl += "&ADName="+$scope.selectedAD.label;
			tempSaveUrl += "&ADId="+$scope.selectedAD.id;
			$scope.selectedAD = {};
			document.location.href = tempSaveUrl;
			$scope.$destroy();
		}

		$scope.close = function(){
			$scope.selectedAD = {};
			document.location.href = closeUrl;
			$scope.$destroy();
		}
	};
})();