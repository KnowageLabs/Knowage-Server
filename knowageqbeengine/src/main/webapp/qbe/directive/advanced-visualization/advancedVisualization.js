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

angular.module('qbe_advanced_visualization', ['dndLists'])
.directive('qbeAdvancedVisualization', function() {
	return {
		templateUrl:  currentScriptPath +'advanced-visualization.html',
		controller:advancedVisualizationControllerFunction,
		priority: 10,
		scope: {
			ngModel:"="
		},
		link: function (scope, elem, attrs) {

		}
	}
});

function advancedVisualizationControllerFunction($scope,sbiModule_translate, sbiModule_config, filters_service, $element, $mdDialog){

	$scope.models = {
		        selected: null,
		        templates: [

		            {type: "group", id: 1, columns: [[]], connector:"AND"}
		        ],
		        dropzones:{
		        	  "Filter visualisation": $scope.ngModel.advancedFilters,
		        	    }

	  };

	$scope.booleanConnectors = filters_service.booleanConnectors;

	$scope.$watch('models.dropzones', function(model) {
        $scope.modelAsJson = angular.toJson(model, true);
        console.log("model"+angular.toJson(model));
    }, true);



	$scope.saveFiltersAdvanced = function(){
		var advanceFiltersSaved = angular.copy($scope.ngModel.advancedFilters);
		generateAdvancedExpression(advanceFiltersSaved);
		$scope.ngModel.mdPanelRef.close();
	}

	$scope.filtersGroup = [];

	$scope.openMenu = function($mdOpenMenu, ev) {
		originatorEv = ev;
		$mdOpenMenu(ev);
	};

	$scope.linkFunction = function (booleanConnector,id) {
		for ( var index in $scope.ngModel.childNodes) {
			if($scope.ngModel.childNodes[index].id==id){
				$scope.ngModel.childNodes[index].booleanConnector = booleanConnector;
			}
		}
	}

	$scope.sbiModule_config = sbiModule_config;
	$scope.translate = sbiModule_translate;

	$scope.clickDocument=function(item){
		 $scope.selectDocumentAction({doc: item});
	}

	$scope.closeFiltersAdvanced = function () {
		$scope.ngModel.mdPanelRef.close();
	}

	$scope.removeFilter = function () {
		for ( var index in $scope.ngModel.childNodes) {
			if($scope.ngModel.childNodes[index].selected==true){
				$scope.ngModel.childNodes.splice(index,1);
			}
		}
	}

	$scope.removeGroup = function (item, index) {
		$scope.ngModel.advancedFilters.splice(index,1);
		var subset = item.columns[0];
		for (var i = 0; i < subset.length; i++) {
			$scope.ngModel.advancedFilters.push(subset[i])
		}

	}

	var generateAdvancedExpression = function (advancedFiltersAndGroups){

		var finalExpression = {};

		var nop = {};
		nop.value = "";
		nop.type = "NODE_OP";
		nop.childNodes = [];
		var nopForInsert = {};
		for (var i = advancedFiltersAndGroups.length-1; i >= 0 ; i--) {

			if(i-1==-1 && advancedFiltersAndGroups[i].type=="group"&& advancedFiltersAndGroups.length==1) {
				angular.copy(createGroupNode(advancedFiltersAndGroups[i].columns[0]) ,finalExpression);
			} else {
				if (i-1==-1 || (advancedFiltersAndGroups[i].type==advancedFiltersAndGroups[i-1].type&&advancedFiltersAndGroups[i].connector!=advancedFiltersAndGroups[i-1].connecto&&advancedFiltersAndGroups.length!=i+1)) {
					nop.value = advancedFiltersAndGroups[i].connector;
					if(advancedFiltersAndGroups[i].type=="item"){
						var nodeConstObj = {};
						nodeConstObj.value = '$F{' + advancedFiltersAndGroups[i].name + '}';
						nodeConstObj.type = "NODE_CONST";
						nodeConstObj.childNodes = [];
						nop.childNodes.unshift(nodeConstObj);
					} else if (advancedFiltersAndGroups[i].type=="group") {
						var group = {};
						angular.copy(createGroupNode(advancedFiltersAndGroups[i].columns[0]) ,group);
						nop.childNodes.unshift(group);
					}
					if(nopForInsert.value){
						nop.childNodes.unshift(nopForInsert);
					}
					nopForInsert = angular.copy(nop);
					nop.value = "";
					nop.type = "NODE_OP";
					nop.childNodes.length = 0;
				} else {
					if(advancedFiltersAndGroups[i].type=="item"){
						var nodeConstObj = {};
						nodeConstObj.value = '$F{' + advancedFiltersAndGroups[i].name + '}';
						nodeConstObj.type = "NODE_CONST";
						nodeConstObj.childNodes = [];
						nop.childNodes.unshift(nodeConstObj);
					} else if (advancedFiltersAndGroups[i].type=="group"){
						nop.childNodes.unshift(createGroupNode(advancedFiltersAndGroups[i].columns[0]));
					}
				}
				angular.copy(nopForInsert,finalExpression);
			}
		}
		angular.copy(finalExpression, $scope.ngModel.expression);
		console.log($scope.ngModel);
	}

	function createGroupNode(arr){
		var nodeParObj = {};
		nodeParObj.value = "PAR";
		nodeParObj.type = "NODE_OP";
		nodeParObj.childNodes = [];
	    for(var i = 0; i < arr.length; i++){
	        if(arr[i].type=="group"){
	        	for(var j = 0; j < arr[i].columns.length; j++){
	                if(arr[i].columns[j] instanceof Array){
	                	createGroupNode(arr[i].columns[j]);
	                }else{
	                    console.log(arr[i]);
	                }
	            }
	        }else{
	            console.log(arr[i]);
	        }
	    }
	    nodeParObj.childNodes.push(generateFiltersExpression(arr));

	    return nodeParObj;
	}

	var generateFiltersExpression = function (filters){

		var finalExpression = {};

		var nop = {};
		nop.value = "";
		nop.type = "NODE_OP";
		nop.childNodes = [];
		var nopForInsert = {};
		for (var i = filters.length-1; i >= 0 ; i--) {

			if (i-1==-1 || filters[i].connector!=filters[i-1].connecto&&filters.length!=i+1) {

				nop.value = filters[i].connector;
				var nodeConstObj = {};
				nodeConstObj.value = '$F{' + filters[i].name + '}';
				nodeConstObj.type = "NODE_CONST";
				nodeConstObj.childNodes = [];
				nop.childNodes.unshift(nodeConstObj);

				if(nopForInsert.value){
					nop.childNodes.unshift(nopForInsert);
				}

				nopForInsert = angular.copy(nop);
				nop.value = "";
				nop.type = "NODE_OP";
				nop.childNodes.length = 0;
			} else {
				var nodeConstObj = {};
				nodeConstObj.value = '$F{' + filters[i].name + '}';
				nodeConstObj.type = "NODE_CONST";
				nodeConstObj.childNodes = [];
				nop.childNodes.unshift(nodeConstObj);

			}
			angular.copy(nopForInsert,finalExpression);

		}

		return finalExpression;

	}

	$scope.filterColumnsAV = [
		{
        	"label":"Name",
        	"name":"filterId"
    	},
    	{
        	"label":"Description",
        	"name":"description",
        	transformer: function() {
        		return '{{scopeFunctions.checkDescription(row)}}';
        	}
    	},
    	{
    		"label":"Function",
        	"name":"booleanConnector",
        	hideTooltip:true,
        	transformer: function() {
        		return '<md-select ng-model=row.booleanConnector class="noMargin" ><md-option ng-repeat="connector in scopeFunctions.booleanConnectors" ng-click="scopeFunctions.setBooleanConnector(connector, row)" value="{{connector}}">{{connector}}</md-option></md-select>';
        	}
    	}
    ]

	$scope.speedMenu =  [ {
		label :  "Move filter up",
		icon : 'fa fa-arrow-up',
		color : '#153E7E',
		action : function(item) {
			$scope.moveFilter(item,'up');
		},
		visible : function (item){
			return $scope.showMoveUp(item);
		}
	},{

		label :  "Move filter down",
		icon : 'fa fa-arrow-down',
		color : '#153E7E',
		action : function(item) {
			$scope.moveFilter(item,'down');
		},
		visible : function (item){
			return $scope.showMoveDown(item);
		}
	}
	 ];

	$scope.showMoveUp = function (item){
		if($scope.ngModel.filters[0].filterId == item.filterId){
			return false;
		}
	}

	$scope.showMoveDown = function (item){
		if($scope.ngModel.filters[$scope.ngModel.filters.length-1].filterId == item.filterId){
			return false;
		}
	}

	$scope.moveFilter = function (item, direction) {

		var oldIndex = findWithAttr($scope.ngModel.filters, 'filterId', item.filterId);
		var newIndex = direction == 'up' ? oldIndex-1 : oldIndex+1;

		var filterToSwitch = {};
		angular.copy($scope.ngModel.filters[newIndex], filterToSwitch);

		$scope.ngModel.filters[newIndex] = item;
		$scope.ngModel.filters[oldIndex] = filterToSwitch;

	};

	var findWithAttr = function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}


	$scope.groupFilters = function () {
		for (var i = 0; i < $scope.advancedFilters.length; i++) {
			for (var j = 0; j < $scope.arrayForGroup.length; j++) {
				if($scope.advancedFilters[i].name==$scope.arrayForGroup[j].name) {

					$scope.advancedFilters.splice(i, 1);

				}

			}

		}
		$scope.advancedFilters.push(angular.copy($scope.arrayForGroup))
		$scope.arrayForGroup.length=0;
		console.log($scope.advancedFilters)
	}

	$scope.$on('groupFilters', function (event, data) {
		console.log(data);
		console.log($scope.advancedFilters);

		$scope.indexes = [];
		//event.stopPropagation();
	 // drop single filter on single filter
		if(data.droppedField.hasOwnProperty("connector") && data.draggedField.hasOwnProperty("connector")){
			$scope.filtersGroup.push(data.droppedField);
			$scope.filtersGroup.push(data.draggedField);
		}
	 // drop single filter on group
		else if (!data.droppedField.hasOwnProperty("connector") && data.draggedField.hasOwnProperty("connector")){
			data.droppedField.push(data.draggedField);
		}
	 // drop group on group
		else if (!data.droppedField.hasOwnProperty("connector") && !data.draggedField.hasOwnProperty("connector")){
			data.droppedField.push(data.draggedField);
		}
	 // drop group on field
		else if (data.droppedField.hasOwnProperty("connector") && !data.draggedField.hasOwnProperty("connector")){
			data.droppedField.push(data.draggedField);
		}

		$scope.spliceSelectedFiltersFromParent($scope.advancedFilters, $scope.filtersGroup);


		for (var i = $scope.indexes.length-1; i >= 0; i--) {
			$scope.advancedFilters.splice($scope.indexes[i], 1);
		}

		$scope.advancedFilters.push(angular.copy($scope.filtersGroup))
		$scope.filtersGroup.length=0;
		$scope.indexes.length=0;
	});

	$scope.spliceSelectedFiltersFromParent = function(parent, group, parentIndex){
		for (var i = 0; i <parent.length; i++) {
			if(parent[i].hasOwnProperty("connector")){
				for (var j = 0; j < group.length; j++) {
					if(parent[i].name==group[j].name) {
						$scope.indexes.push(i);
					}
				}
			} else {
				$scope.spliceSelectedFiltersFromParent(parent[i], group);
			}

		}
	}

	$scope.arrayForGroup = [];
	$scope.addToArray = function (filter) {

		$scope.arrayForGroup.push(filter);

	}

	$scope.advancedVisualizationScopeFunctions = {
		booleanConnectors: $scope.booleanConnectors,
		setBooleanConnector: function (connector, row) {
			row.booleanConnector = connector;
		},
		checkDescription: function (row){
			return row.leftOperandDescription + " " + row.operator + " " +row.rightOperandDescription ;
		}
	}
}
})();