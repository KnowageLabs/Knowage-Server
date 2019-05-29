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
	angular.module('advancedFiltersApp').directive('advanceFilterPanel', function(advancedFilterAppBasePath) {
		  return {
			  	scope:{
			  		filterTree:'='
			  	},
			    templateUrl: advancedFilterAppBasePath + '/directives/panels/advanceFilterPanel/advanceFilterPanel.html',
			    controller:function($scope,$injector){
			    	var advancedFiltersService = $injector.get('advancedFiltersService');
			    	$scope.selectedOperandService = $injector.get('selectedOperandService');
			    	$scope.filterTreeService = $injector.get('filterTreeService');
			    	
			    	$scope.filterTreeService.filterTree = $scope.filterTree ;
			    	$scope.group = function(){
			    		
			    		advancedFiltersService.group($scope.filterTreeService.filterTree,$scope.selectedOperandService.getSelected())
			    		$scope.selectedOperandService.unSelectAll();
			    		
			    	}
			    	
			    	$scope.ungroup = function(){
			    		
			    		advancedFiltersService.ungroup($scope.filterTreeService.filterTree,$scope.selectedOperandService.getSelected()[0])
			    		$scope.selectedOperandService.unSelectAll();
			    		
			    	}
			    	
			    }
			  };
			});
})()
