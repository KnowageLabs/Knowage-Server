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
	$scope.scenario = {
			name: "scenario",
			editCube : "",
			measures: []
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
			$scope.getAllMeasures();
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');	
		});		
	};
	
	/**
	 * Loads all measures.
	 */
	$scope.getAllMeasures = function(){
		sbiModule_restServices.promiseGet("1.0/designer/measures/"+schemaID + "/"+ cubeName,"?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			$scope.measuresList = [];
			
			for (var i = 0; i < response.data.length; i++) {
				var measuresListItem = { name: ""};
				measuresListItem.name = response.data[i];
				$scope.measuresList.push(measuresListItem);
			}
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
		if(type == 'member' && input == 'value'){
			return "crossNavfromMemberObj.uniqueName";
		}else if (type == 'member' && input == 'name'){
			return "crossNavfromMemberObj.clickParameter.name";
		} 
	};
		
	/**
	 * Binds temporary scenario object to olap template object via service after validation check.
	 */
	$scope.saveScenario = function() {
		
	    if($scope.scenario.editCube==""&&$scope.scenario.measures.length==0){
			sbiModule_messaging.showErrorMessage("Selecting a cube and a measure is mandatory. ", 'Validation error');
			console.log($scope.scenario)
		}
	    else if($scope.scenario.editCube==""){
			sbiModule_messaging.showErrorMessage("You didn't select a cube. Selecting a cube is mandatory. ", 'Validation error');	
		}
		else if($scope.scenario.measures.length==0){
			sbiModule_messaging.showErrorMessage("You didn't select a measure. Selecting a measure is mandatory. ", 'Validation error');	
		}
	    else {
			console.log($scope.scenario)
			$mdDialog.hide();
			$scope.scenario = {
					name: "scenario",
					editCube : "",
					measures: []
			};
		}	
	}
	
	/**
	 * Changes the state of the selected checkbox item. [editable measures]
	 */
	$scope.toggle = function (item) {
	   var idx = $scope.scenario.measures.indexOf(item);
	   if (idx > -1) {
		   $scope.scenario.measures.splice(idx, 1);
	   }
	   else {
	    $scope.scenario.measures.push(item);
	   }
	 };
	 
	 /**
	  * Used for ng-change directive on single checkbox item. 
	  * If this expression evaluates as truthy, the 'md-checked' css class is added to the checkbox and it will appear checked.[editable measures]
	  */
	 $scope.exists = function (item) {
	   return $scope.scenario.measures.indexOf(item) > -1;
	 };

	 /**
	  * Used for md-indeterminate 
	  */
	 $scope.isIndeterminate = function() {
	   return ($scope.scenario.measures.length !== 0 &&
	       $scope.scenario.measures.length !== $scope.measuresList.length);
	 };

	 /**
	  * Used for Select All check box to determine if all check boxes are checked.
	  */
	 $scope.isChecked = function() {
	   return $scope.scenario.measures.length === $scope.measuresList.length;
     };

     /**
	  * Selects or un-selects all measures check boxes.
	  */
	 $scope.toggleAll = function() {
	   if ($scope.scenario.measures.length === $scope.measuresList.length) {
		   $scope.scenario.measures = [];
	   } else if ($scope.scenario.measures.length === 0 || $scope.scenario.measures.length > 0) {
		   $scope.scenario.measures = $scope.measuresList.slice(0);
	   }
	 };
	 
	 /**
	  * Closes dialog in olap designer.
	  */
	 $scope.closeDialogOlapDesigner = function() {
		 $mdDialog.hide();
	 }
	 
	 /**
	  * Adds variable map(name,value) object to scenario property array variable.
	  */
	 $scope.addVariable=function(){ 
			var variable={};
			if($scope.scenario.variables==undefined){
				$scope.scenario.variables = [];
			}			
			$scope.scenario.variables.push(variable);
			console.log($scope.scenario.variables);
			return variable;
	 }
	 
	 /**
	  * Removes selected variable from array property of $scope.scenario object.
	  */
	 $scope.removeVariable=function(inputVariable){
			var index=$scope.scenario.variables.indexOf(inputVariable);		
			$scope.scenario.variables.splice(index, 1);
			console.log($scope.scenario.variables);
		}
	
	

};

