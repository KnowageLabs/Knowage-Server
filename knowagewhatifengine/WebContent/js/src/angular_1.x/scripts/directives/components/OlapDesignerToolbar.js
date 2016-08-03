/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Directive used in OLAP template build.
 * @author Nikola Simovic (nsimovic, nikola.simovic@mht.net)
 * @author Stefan Petrovic (spetrovic, stefan.petrovic@mht.net)
 */
angular.module('olap_designer_toolbar', ['sbiModule'])
		.directive(
				'olapDesignerToolbar',
				function(sbiModule_config) {
					return {
						restrict : "E",
						replace : 'true',
						templateUrl : function() {
						return sbiModule_config.contextName + '/html/template/right/edit/olapDesignerToolbar.html'	
						}, 
						controller : olapDesignerToolbarController
					}
				});


function olapDesignerToolbarController($scope, $timeout, $window, $mdDialog, $http, $sce,
		sbiModule_messaging, sbiModule_restServices, sbiModule_translate,
		toastr, $cookies, sbiModule_docInfo, sbiModule_config) {
	
	$scope.cubeList = [];
	/**
	 * SCENARIO is the temporary object that will be bind to olapTemplate if the scenario is defined.
	 */
	$scope.SCENARIO = {
			name: "scenario",
			editCube : ""
	};		
	/**
	 * Array that hold cross navigation types
	 */
	$scope.crossNavType = null;
	$scope.crossNavTypeList = [{
	   	 "value": "cell",
		  "name": "From Cell"	 
	}, 
	{
		 "value": "member",
		  "name": "From Member"	 
	}];
	
	/**
	 * Loads cubes and opens a new what-if scenario dialog.
	 */
	$scope.runScenarioWizard = function(){
		$scope.getVersions();
		$scope.getCubes();
	}
	
	/**
	 * Object that holds Nav from member
	 */
	$scope.crossNavfromMemberObj = {
			"uniqueName" : "",
			"clickParameter": {
				"name": "",
				"value":"{0}"
			}
	};
	
	$scope.crossNavfromCellObj = {
			 "name":"",
             "scope":"",
             "dimension":"",
             "hierarchy":"",
             "level":""
			
	};
	
	/**
	 * Opens a new dialog for what-if scenario.
	 */
	$scope.openScenarioWizard = function(){
		$mdDialog
		.show({
			scope : $scope,
			preserveScope : true,
			parent : angular.element(document.body),
			controllerAs : 'olapDesignerCtrl',
			templateUrl : sbiModule_config.contextName + '/html/template/right/edit/scenarioWizard.html',
			clickOutsideToClose : false,
			hasBackdrop : false
		});
	}
	
	/**
	 * Loads schema cubes.
	 */
	$scope.getCubes = function(){
		sbiModule_restServices.promiseGet("1.0/designer/cubes/"+schemaID,"?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			$scope.cubeList = response.data;
			$scope.showCubes = true;
			$scope.openScenarioWizard();
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');	
		});	
		
	};
	
	/**
	 * Opens a new dialog for crossnav definition.
	 */
	$scope.openCrossNavWizard = function() {
		
		 $mdDialog
		  .show({
		   scope : $scope,
		   preserveScope : true,
		   parent : angular.element(document.body),
		   controllerAs : 'olapDesignerCtrl',
		   templateUrl : sbiModule_config.contextName + '/html/template/right/edit/crossNavWizardS1.html',
		   clickOutsideToClose : false,
		   hasBackdrop : false
		  });
		
	};
	
	/**
	 * Opens a new dialog for crossnav definition.
	 */
	$scope.nextCNStep = function() {
		
		 $mdDialog
		  .show({
		   scope : $scope,
		   preserveScope : true,
		   parent : angular.element(document.body),
		   controllerAs : 'olapDesignerCtrl',
		   templateUrl : sbiModule_config.contextName + '/html/template/right/edit/crossNavWizardS2.html',
		   clickOutsideToClose : false,
		   hasBackdrop : false
		  });
		
	};
	/**
	 * Function that dynamically assign ng-model
	 */
	$scope.changeNgModel = function(type,input) {
		console.info(type);
		if(type == 'member' && input == 'value'){
			return "crossNavfromMemberObj.uniqueName";
		}else if (type == 'member' && input == 'name'){
			return "crossNavfromMemberObj.clickParameter.name";
		} 
	};
	
	/**
	 * Binds temporary scenario object to olapTemplate and after that sets the scenario to initial value.
	 */
	$scope.saveScenario = function() {
		console.log($scope.SCENARIO);
		$scope.SCENARIO = {
				name: "scenario",
				editCube : ""
		};
	}

};

