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
(function() {
	

	angular.module('filter_card',['sbiModule'])
	.directive('filterCard', function (sbiModule_config) {
		 return {
		      restrict: 'E',
		      replace: 'true',
//		      templateUrl: '/knowagewhatifengine/html/template/main/filter/filterCard.html',
		      templateUrl: function(){
			    	 return sbiModule_config.contextName+'/html/template/main/filter/filterCard.html';
			    	  
		      },
		      controller:filterCardController
		  };
	});

function filterCardController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
	$scope.showMultiHierDialog = function(ev,f){
		/*if($scope.member != undefined){
			$scope.member.hierarchies = [];
		}*/			
		$scope.member = f;
		$scope.selecetedMultiHierUN = $scope.member.hierarchies[$scope.member.selectedHierarchyPosition].uniqueName;
		$scope.showDialog(ev,"/main/filter/multiHierarchyDialog.html");
		console.log($scope.member);
	};
	
	$scope.updateHierarchie = function(ev){
		var axis = $scope.member.axis;
		var oldHier = $scope.member.hierarchies[$scope.member.selectedHierarchyPosition].uniqueName;
		var newHier = $scope.selecetedMultiHierUN;
		var pia = $scope.member.positionInAxis;
		
		if(oldHier != newHier)
			updateHierService(axis, oldHier, newHier, pia);
		$scope.closeDialog(ev);
	};
	
	updateHierService = function(ax, oldH, newH, pia){
		var toSend = {
				'axis':ax,
				'newHierarchyUniqueName':newH,
				'oldHierarchyUniqueName':oldH,
				'hierarchyPosition':pia
		}

		var encoded = encodeURI("1.0/axis/updateHierarchyOnDimension?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		sbiModule_restServices.promisePost(encoded,"",toSend)
			.then(function(response){
				$scope.table = $sce.trustAsHtml(response.data.table);
				$scope.handleResponse(response);
			},function(response){
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.hierarchy.error'), 'Error');
		});
	};
};
})();