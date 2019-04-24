/**
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
(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('qbe_having', ['ngMaterial'])
.directive('qbeHaving', function() {
	return {
		templateUrl: currentScriptPath + 'having.html',
		controller: qbeHaving,

		scope: {
			ngModel: '='
		},
		link: function(scope, elem, attrs) {

		}
	}
});

function qbeHaving($scope, $rootScope, filters_service, sbiModule_translate) {

	$scope.havings = angular.copy($scope.ngModel.havings);
	$scope.field = $scope.ngModel.field;
	$scope.tree = $scope.ngModel.tree.entities;
	$scope.translate = sbiModule_translate;
	$scope.subqueries = $scope.ngModel.subqueries;
	$scope.selFields = $scope.ngModel.selectedFields

	var checkForIndex = function(){
		var arrayOfIndex = [];
		if($scope.havings.length==0){
			return 1;
		} else {

			for (var m = 0; m < $scope.havings.length; m++) {
				var num = $scope.havings[m].filterId.substring(6);
				arrayOfIndex.push(parseInt(num));
			}
			function sortNumber(a,b) {
			    return a - b;
			}

		    arrayOfIndex.sort(sortNumber);
		    return arrayOfIndex[arrayOfIndex.length-1]+1;
		}
	}

	$scope.addNewHaving = function (){
		$scope.havingIndex = checkForIndex();
		$scope.showTable = false;
		$scope.targetOption = "default";
		var object = {
				"filterId": "having"+$scope.havingIndex,
				"filterDescripion": "having"+$scope.havingIndex,
				"filterInd": $scope.havingIndex,
				"promptable": false,
				"leftOperandAggregator": $scope.field.funct,
				"leftOperandValue": $scope.ngModel.field.id,
				"leftOperandDescription": $scope.ngModel.field.entity+ ":" + " " + $scope.field.funct + " (" + $scope.ngModel.field.name + ")",
				"leftOperandLongDescription": $scope.ngModel.field.entity+ ":" + " " + $scope.field.funct + " (" + $scope.ngModel.field.name + ")",
				"leftOperandType": $scope.field.id.expression ? "inline.calculated.field" : "Field Content",
				"leftOperandDefaultValue": null,
				"leftOperandLastValue": null,
				"operator": "EQUALS TO",
				"rightOperandAggregator": "",
				"rightOperandValue": [],
				"rightOperandDescription": "",
				"rightOperandLongDescription": "",
				"rightOperandType": "",
				"rightType" : "",
				"rightOperandDefaultValue": [""],
				"rightOperandLastValue": [""],
				"booleanConnector": "AND",
				"deleteButton": false,
				"color": $scope.ngModel.field.color,
				"entity": $scope.ngModel.field.entity
			}
		$scope.havings.push(object);
	}

	if($scope.havings.length == 0) {
		$scope.addNewHaving();
	}

	$scope.openMenu = function($mdOpenMenu, ev) {
		originatorEv = ev;
		$mdOpenMenu(ev);
	};

	$scope.fieldHaving = function (item) {
		if(item.leftOperandValue==$scope.field.id)
			return item;
	}

	$scope.getConditionOptions = filters_service.getSpecialOperators;
	$scope.getFunctions = filters_service.aggFunctions;
	$scope.booleanConnectors = filters_service.booleanConnectors;
	$scope.getHavingTargetTypes = filters_service.getHavingTargetTypes;

	$scope.changeTarget = function(option, having) {
		switch(option) {
		case "anotherEntity":
			having.rightOperandType = "Field Content";
			$scope.showTable = false;
			break;
		case "subquery":
			having.rightOperandType = "Subquery";
			$scope.showTable = false;
			break;
		default:
			having.rightOperandType = "Static Content";
			break;
		}
	}

	$scope.fillInput = function(having, type, value) {
		switch(value) {
		case "subquery":
			$scope.setRight(having, type, value);
			break;
		case "anotherEntity":
			$scope.setRight(having, type, value);
			break;
		default:
				break;
		}
	}

	$scope.setRight = function(having, type, value) {
		if(value=='anotherEntity'){
			having.rightOperandValue=[];
			having.rightOperandValue.push(type.id) ;
			having.rightOperandType="Field Content";
			//having.rightOperandDescription=type.attributes.entity+" "+": "+type.text;
			having.rightOperandDescription = type.field;
			//having.rightOperandLongDescription = type.attributes.entity+" "+": "+type.text;
			having.rightOperandLongDescription = type.field;
			//having.rightOperandAlias=type.text;
			having.rightOperandAlias = type.field;
		} else if(value =='subquery') {
			having.rightOperandValue.push(type.id);
			having.rightOperandDescription = type.name;
			having.rightOperandLongDescription = "Subquery "+type.name;
			having.rightOperandType = "Subquery";
		} else {
			having.rightOperandType = "Static Content";
		}
	};

	$scope.edit = function(having) {
		having.rightOperandValue = [];
		having.rightOperandValue.push(having.rightOperandDescription);
		having.rightOperandType = "Static Content";
	}


	$scope.saveHavings=function() {
		$scope.ngModel.havings.length = 0;
		Array.prototype.push.apply($scope.ngModel.havings, $scope.havings);
		$scope.ngModel.mdPanelRef.close();
	}

	$scope.closeHavings=function(){
		$scope.ngModel.mdPanelRef.close();
	}

	$scope.deleteHaving = function (having){
		console.log(having);
		for (var i = 0; i < $scope.havings.length; i++) {
			if($scope.havings[i].filterId==having.filterId) {

				$scope.havings.splice(i, 1);
			}

		}

	}
}

})();