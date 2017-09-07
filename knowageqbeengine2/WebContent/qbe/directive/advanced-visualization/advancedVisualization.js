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

angular.module('qbe_advanced_visualization', ['ngDraggable'])
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

	$scope.advancedFilters = angular.copy($scope.ngModel.advancedFilters);
	
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