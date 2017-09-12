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

function advancedVisualizationControllerFunction($scope,sbiModule_translate, sbiModule_config, filters_service, $mdDialog){
	$scope.advancedFilters = [{
		"type": "group",
		"id": 3,
		"columns": [[{
			"type": "item",
			"id": "12",
			"columns": [[],
			[]],
			"name": "Filter12",
			"connector": "AND"
		},
		{
			"type": "item",
			"id": "13",
			"columns": [[],
			[]],
			"name": "Filter13",
			"connector": "AND"
		},
		],
		[]],
		connector:"AND"
	},
	{
		"type": "group",
		"id": 1,
		"columns": [[{
			"type": "item",
			"id": "14",
			"columns": [[],
			[]],
			"name": "Filter14",
			"connector": "AND"
		},
		{
			"type": "item",
			"id": "15",
			"columns": [[],
			[]],
			"name": "Filter15",
			"connector": "AND"
		},
		],
		[]],
		connector:"AND"
	},
	{
		"type": "item",
		"id": "1",
		"columns": [[],
		[]],
		"name": "Filter1",
		"connector": "AND"
	},
	{
		"type": "item",
		"id": "2",
		"columns": [[],
		[]],
		"name": "Filter2",
		"connector": "AND"
	},
	{
		"type": "item",
		"id": "3",
		"columns": [[],
		[]],
		"name": "Filter3",
		"connector": "AND"
	},
	{
		"type": "item",
		"id": "4",
		"columns": [[],
		[]],
		"name": "Filter4",
		"connector": "AND"
	}];

	$scope.models = {
		        selected: null,
		        templates: [

		            {type: "group", id: 1, columns: [[], []], connector:"AND"}
		        ],
		        dropzones:{
		        	  "Filter visualisation": $scope.advancedFilters,
		        	    }

	  };
	$scope.$watch('models.dropzones', function(model) {
        $scope.modelAsJson = angular.toJson(model, true);
    }, true);


	$scope.saveFiltersAdvanced = function(){
		var advanceFiltersSaved = angular.copy($scope.advancedFilters);

		generateExpressions (advanceFiltersSaved);

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

	var generateExpressions = function (advanceFiltersSaved){

		console.log(advanceFiltersSaved);
	};

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
        		return '<md-select ng-model=row.booleanConnector class="noMargin" ><md-option ng-repeat="connector in scopeFunctions.logicalOperators" ng-click="scopeFunctions.setBooleanConnector(connector, row)" value="{{connector}}">{{connector}}</md-option></md-select>';
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
	$scope.logicalOperators = filters_service.getBooleanConnectors;
	
	$scope.advancedVisualizationScopeFunctions = {
		logicalOperators: $scope.logicalOperators,
		setBooleanConnector: function (connector, row) {
			row.booleanConnector = connector;
		},
		checkDescription: function (row){
			return row.leftOperandDescription + " " + row.operator + " " +row.rightOperandDescription ;
		}
	}
	$scope.radaf = [{"conn":4, "sss":"das"}, {"conn":3, "sss":"das"}, [{"conn":656, "sss":55},{"conn":654666, "sss":"das"},{"conn":"fsdfs", "sss":"das"},[{"conn":11, "sss":55},{"conn":22, "sss":"das"},{"conn":"22", "sss":"das"}]]];
}
})();