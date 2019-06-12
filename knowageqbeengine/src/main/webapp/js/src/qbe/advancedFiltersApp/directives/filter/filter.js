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
(function(){
	angular.module('advancedFiltersApp').directive('filter', function(advancedFilterAppBasePath,$rootScope) {
		  return {
			  	scope:{
			  		node:'='
			  	},
			  	link:function(scope, element, attrs){
			  		element.bind('mousedown', function(e) {

			  		   e.stopPropagation()
			  		   $rootScope.$broadcast('nodrag',scope.node)
			  		});
			  	},
			  	restrict:'E',
			    templateUrl: advancedFilterAppBasePath + '/directives/filter/filter.html',
			    controller:function($scope,$rootScope,advancedFiltersService,$injector){
			    	$scope.draggable = true;
			    	$scope.$on('nodrag',function(event,data){
			    		if(data!==$scope.node){
			    			$scope.draggable = false;

			    		}

			    	})
			    	var filterTreeService = $injector.get('filterTreeService');
			    	$scope.selectedOperandService = $injector.get('selectedOperandService');

			    	$scope.select = function(node){
			    		$scope.selectedOperandService.addOrRemove(node);

			    	}

			    	$scope.isSelected = function(){
			    		return $scope.selectedOperandService.contains($scope.node)
			    	}

			    	$scope.isSelectable = function(){
			    		return $scope.selectedOperandService.isSelectable($scope.node)
			    	}


			    	$scope.onDropComplete = function($data,$event){

			    		$event.event.stopImmediatePropagation();
			    		console.log($scope.node)
			    		console.log($data)
			    		if(!angular.equals($data.node,$scope.node)){
			    		advancedFiltersService.swap(filterTreeService.filterTree,$data.node,$scope.node)
			    		}


			    	}

			    	$scope.onDropMove = function($data,$event){
			    		 if($scope.selectedOperandService.isMovable($data.node)){
			    			 $event.event.stopImmediatePropagation();

					    		if(!angular.equals($data.node,$scope.node)){
					    		advancedFiltersService.move(filterTreeService.filterTree,$data.node,$scope.node)
					    		}
			    		 }



			    	}
			    }
			  };


			});
})()
