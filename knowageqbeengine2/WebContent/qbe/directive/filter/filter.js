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

function qbeFilter($scope,$rootScope, filters_service ,sbiModule_translate, sbiModule_config,$mdPanel){

	$scope.filters=angular.copy($scope.ngModel.queryFilters);
	$scope.field=$scope.ngModel.field.field;
	$scope.tree=$scope.ngModel.tree.entities;
	$scope.pars=angular.copy($scope.ngModel.pars);
	$scope.targetOption="default";
	$scope.openMenu = function($mdOpenMenu, ev) {
	      originatorEv = ev;
	      $mdOpenMenu(ev);
	};
	$scope.fieldFilter = function (item) {
		if(item.leftOperandValue==$scope.field.id) return item
	}

	$scope.params=false;
	$scope.targetAF = {};
	var checkForIndex = function(){
		var niz = [];
		if($scope.filters.length==0){
			return 1;
		} else {

			for (var m = 0; m < $scope.filters.length; m++) {
				var num = $scope.filters[m].filterId.substring(6);
				niz.push(parseInt(num));
			}
			function sortNumber(a,b) {
			    return a - b;
			}

		    niz.sort(sortNumber);
		    return niz[niz.length-1]+1;
		}
	}
	$scope.addNewFilter= function (){
		$scope.filterIndex = checkForIndex();
		$scope.showTable = false;
		$scope.targetOption = "default";
		var object = {
				"filterId": "Filter"+$scope.filterIndex,
				"filterDescripion": "Filter"+$scope.filterIndex,
				"filterInd": $scope.filterIndex,
				"promptable": false,
				"leftOperandValue": $scope.ngModel.field.field.id,
				"leftOperandDescription": $scope.ngModel.field.field.entity+ " : " + $scope.ngModel.field.field.name,
				"leftOperandLongDescription": $scope.ngModel.field.field.entity+ " : " + $scope.ngModel.field.field.name,
				"leftOperandType": "Field Content",
				"leftOperandDefaultValue": null,
				"leftOperandLastValue": null,
				"leftOperandAlias": $scope.ngModel.field.field.name,
				"leftOperandDataType": "",
				"operator": "EQUALS TO",
				"rightOperandValue": [],
				"rightOperandDescription": "",
				"rightOperandLongDescription": "",
				"rightOperandType": "",
				"rightOperandDefaultValue": [],
				"rightOperandLastValue": [],
				"rightOperandAlias": null,
				"rightOperandDataType": "",
				"booleanConnector": "AND",
				"deleteButton": false
			}
		if($scope.filters.length ==0){
			$scope.filters.push(object);
		} else {
			for (var j = 0; j <  $scope.filters.length; j++) {
				if($scope.filters[j+1]){
					if($scope.filters[j].leftOperandValue==$scope.filters[j+1].leftOperandValue){

					} else {
						if($scope.filters[j].leftOperandValue==object.leftOperandValue){
							$scope.filters.splice(j+1,0,object);
							break;
						}
					}
				} else {
					$scope.filters.push(object);
					break;
				}
			}
		}
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

	$scope.getBooleanConnectors = filters_service.getBooleanConnectors;
	$scope.getConditionOptions = filters_service.getOperators;
	$scope.disableCombo = true;
	$scope.fillInput = function (filter, type, value){
		switch (value) {
		case "subquery":
			setRight(filter, type, value);
			break;
		case "field":
			setRight(filter, type, value);
			break;
		case "parameter":
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
			filter.rightOperandValue.push(type.id);
			filter.rightOperandDescription=type.text;
			filter.rightOperandLongDescription="Subquery "+type.text;
			filter.rightOperandType="Subquery";
		} else if(value=='parameter'){
			filter.hasParam = type.name
			filter.rightOperandDescription=type.name;
			filter.rightOperandLongDescription="Parameter "+type.name;
			filter.rightOperandType="Parameter";
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
			$scope.disableCombo = false;
			break;
		default:
			break;
		}
	}
	$scope.showTable = false;
	$scope.listOfValues = [];
	var openTableWithValues = function (){
		$scope.showTable = true;
		filters_service.getFieldsValue($scope.ngModel.field.field.id).then(function(response){
			$scope.listOfValues = response.data.rows;
		});
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
			for (var i = 0; i < newValue.length; i++) {
				$scope.filter.rightOperandValue.push(newValue[i].column_1)
			}
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

	$scope.edit = function (filter){
		filter.rightOperandValue=[];
		filter.rightOperandValue.push(filter.rightOperandDescription );
		filter.rightOperandType="Static Content";
	}
	$scope.saveFilters=function(){
		var countParam = 0;
		for (var i = 0; i < $scope.filters.length; i++) {
			if($scope.filters[i].rightOperandType=="Parameter"){
				countParam++;
			}
		}

		if($scope.pars.length>0 && countParam>0){
			$scope.params=true;
		} else {
			$scope.ngModel.queryFilters = [];
			$scope.ngModel.queryFilters = $scope.filters;
			//$scope.ngModel.field.field.expression = generateExpressions ($scope.filters);
			$scope.applyFuntion($scope.filters, $scope.pars/*, $scope.ngModel.field.field.expression*/)
			$scope.ngModel.mdPanelRef.close();
		}
	}

	$scope.applyFuntion = function(filters/*, expression*/, pars) {
		console.log($scope)
		$rootScope.$broadcast('applyFunction', {
			"fieldId" : $scope.ngModel.field.field.id,
			"entity" : $scope.ngModel.field.field.entity,

	"filters" : filters,		/*"expression" : expression,*/
			"pars" : pars,

		});
	};
	$scope.closeFilters=function(){
		$scope.ngModel.mdPanelRef.close();
	}
	var generateExpressions = function (filters){
		var expression = {};
		var childNodes = [];
		if(filters.length==1){
			expression =
			{
				"type": "NODE_CONST",
				"value": "$F{" + filters[0].filterId+"}",
				"childNodes": []
			}
		} else if (filters.length>=1){
			for (var i = 0; i < filters.length; i++) {
				var node = {
					"type": "NODE_CONST",
					"value": "$F{" + filters[i].filterId+"}",
					"childNodes": []
				}
				childNodes.push(node)
			}
			expression =
			{
				"type": "NODE_OP",
				"value": "AND",
				"childNodes": childNodes
			}
		}
		return expression;

	};
	$scope.parametersPreviewColumns = [

	                                   {
	                                   	"label":"NAME",
	                                   	"name":"name",
	                                   	hideTooltip:true,
	                                   	transformer: function() {
	                                   		return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name"></md-input-container>';
	                                   	}
	                               	},

	                               	{
	                            		"label":"VALUE",
	                            		"name":"defaultValue",
	                            		hideTooltip:true,

	                                   	transformer: function() {
	                                   		return '<md-input-container class="md-block" style="margin:0"><input placeholder="If not set, parameter will have default value." ng-model="row.value"></md-input-container>';
	                                   	}
	                            	}

	                               ];
	$scope.applyValueOfParameterAndContinuePreviewExecution = function (){
		$scope.ngModel.queryFilters = [];
		for (var i = 0; i < $scope.filters.length; i++) {
			if($scope.filters[i].rightOperandType=="Parameter"){
				for (var j = 0; j < $scope.pars.length; j++) {
					if($scope.filters[i].hasParam==$scope.pars[j].name){
						$scope.filters[i].rightOperandValue.push($scope.pars[j].value);
						$scope.filters[i].rightOperandLongDescription="Parameter "+$scope.pars[j].value;
					}
				}
			}
		}
		$scope.ngModel.queryFilters  = $scope.filters;
	//	$scope.ngModel.field.field.expression = generateExpressions ($scope.filters);
		$scope.applyFuntion($scope.filters/*, $scope.ngModel.field.field.expression*/, $scope.pars)
		$scope.ngModel.mdPanelRef.close();

	}

}
})();