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

angular.module('qbe_filter', ['ngMaterial','angular_table' ])
.directive('qbeFilter', function() {
	return {

		templateUrl:  currentScriptPath +'filter.html',
		controller: qbeFilter,

		scope: {
			 ngModel: '='
		},
		link: function (scope, elem, attrs) {

		}
	}
});

function qbeFilter($scope, filters_service ,sbiModule_translate, sbiModule_config,$mdPanel){

	$scope.filters=angular.copy($scope.ngModel.field.field.filters);
	$scope.field=$scope.ngModel.field.field;
	$scope.tree=$scope.ngModel.tree.entities;
	$scope.targetOption="default";
	$scope.openMenu = function($mdOpenMenu, ev) {
	      originatorEv = ev;
	      $mdOpenMenu(ev);
	};
	var i = 1;
	$scope.targetAF = {};
	$scope.addNewFilter= function (){
		console.log($scope.filters);
		$scope.showTable = false;
		$scope.targetOption = "default";
		var object = {
				"filterId": "Filter"+i,
				"filterDescripion": "Filter"+i,
				"promptable": false,
				"leftOperandValue": "",
				"leftOperandDescription": "",
				"leftOperandLongDescription": "",
				"leftOperandType": "Field Content",
				"leftOperandDefaultValue": null,
				"leftOperandLastValue": null,
				"leftOperandAlias": "",
				"leftOperandDataType": "",
				"operator": "",
				"rightOperandValue": [],
				"rightOperandDescription": "",
				"rightOperandLongDescription": "",
				"rightOperandType": "",
				"rightOperandDefaultValue": [],
				"rightOperandLastValue": [],
				"rightOperandAlias": null,
				"rightOperandDataType": "",
				"booleanConnector": "",
				"deleteButton": false
			}

		$scope.filters.push(object);
		i=i+1;
	}

	$scope.deleteFilter = function (filter){
		console.log(filter);
		for (var i = 0; i < $scope.filters.length; i++) {
			if($scope.filters[i].filterId==filter.filterId) {

				$scope.filters.splice(i, 1);
			}

		}

	}

	$scope.value =[];
	$scope.entitiesField=[];
	for (var i = 0; i < $scope.tree.length; i++) {
		if($scope.tree[i].qtip==$scope.field.entity  ){
			for (var j = 0; j < $scope.tree[i].children.length; j++) {
				if($scope.tree[i].children[j].iconCls!="relation")
					$scope.entitiesField.push($scope.tree[i].children[j])
			}

			break;
		}
	}


	console.log($scope.entitiesField)
	$scope.getConditionOptions = filters_service.getOperators;
	$scope.disableCombo = true;
	$scope.fillInput = function (filter, type, value){
		switch (value) {
		case "subquery":
			filter.rightOperandDescription = type.name;
			break;
		case "field":
			setRight(filter, type, value);


			break;
		default:

			break;
		}
	}
	var setRight = function (filter, type, value){


		if(value=='field'){
			filter.rightOperandValue=[];
			filter.rightOperandValue.push(type.id) ;
			filter.rightOperandType="Field Content";
			filter.rightOperandDescription=$scope.targetAF.text+" "+": "+type.text;
			filter.rightOperandLongDescription=$scope.targetAF.text+" "+": "+type.text;
			filter.rightOperandAlias=type.text;
		} else if(value=='subquery'){
			filter.rightOperandType="Subquery";
		} else {
			filter.rightOperandType="Static Content";

		}

	};
	$scope.changeTarget = function (option, filter){
		switch (option) {
		case "valueOfField":
			$scope.targetOption = option;
			$scope.filter = filter;
			$scope.value= [];
			$scope.disableCombo = true;
			openTableWithValues();
			break;
		case "anotherEntity":
			$scope.targetOption = option;
			$scope.disableCombo = false;
			break;
		case "subquery":
			$scope.targetOption = option;
			$scope.disableCombo = false;
			break;
		case "parameter":
			$scope.targetOption = option;
			$scope.disableCombo = true;
			break;
		default:

			break;
		}
	}
	$scope.showTable = false;
	$scope.listOfValues = [];
	var openTableWithValues = function (){

		$scope.showTable = true;
		filters_service.getFieldsValue($scope.left).then(function(response){
			$scope.listOfValues = response.data.rows;
		});

		console.log($scope.value)


	}

	$scope.$watch('value',function(newValue){
		$scope.forInput = '';
		for (var i = 0; i < newValue.length; i++) {
			$scope.forInput += newValue[i].column_1;
			if(i+1!=newValue.length) 	$scope.forInput += " ---- "

		}
		if($scope.filter) {
			$scope.filter.rightOperandDescription = angular.copy($scope.forInput);

			$scope.filter.rightOperandValue=[];
			$scope.filter.rightOperandValue.push($scope.filter.rightOperandDescription );
			$scope.filter.rightOperandType="Static Content";
			$scope.filter.rightOperandLongDescription=$scope.filter.rightOperandDescription;
		}
	}, true);


	$scope.left = null;
	$scope.setLeftValue= function (value,filter){
		$scope.left = value.id;
		filter.leftOperandValue = value.id;
		filter.leftOperandDescription = $scope.ngModel.field.field.entity+ " : "+value.text;
		filter.leftOperandLongDescription = $scope.ngModel.field.field.entity+ " : "+value.text;
		filter.leftOperandAlias = value.text;
	}
	$scope.selectChanged = function (entity){
		$scope.targetOption = "anotherField";
		$scope.targetAF = entity;
		$scope.entitiesChildren=[];
		for (var i = 0; i < $scope.targetAF.children.length; i++) {
			if($scope.targetAF.children[i].iconCls!="relation") {
				$scope.entitiesChildren.push($scope.targetAF.children[i])
			}

		}

	}

	$scope.saveFilters=function(){
		$scope.ngModel.field.field.filters = [];
		$scope.ngModel.field.field.filters = $scope.filters;
		$scope.ngModel.mdPanelRef.close();


	}

	$scope.closeFilters=function(){
		$scope.ngModel.mdPanelRef.close();

	}

}
})();