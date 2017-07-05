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

angular.module('qbe_custom_table', ['ngDraggable'])
.directive('qbeCustomTable', function() {
    return {
        restrict: 'E',
        controller: qbeCustomTable,
        scope: {
            ngModel: '='
        },
        templateUrl: currentScriptPath + 'custom-table.html',
        replace: true,
        link: function link(scope, element, attrs) {
           
        }
    };
});
function qbeCustomTable($scope, $rootScope, $mdDialog, sbiModule_translate){
	$scope.openMenu = function($mdOpenMenu, ev) {
	      originatorEv = ev;
	      $mdOpenMenu(ev);
  };
  $scope.aggFunctionList = ["SUM","BLA","BLA"];
  $scope.aggFunction = "";
  $scope.translate = sbiModule_translate;
  
  $scope.moveRight = function (currentOrder, column) {
	  
	  var newOrder = currentOrder+1;
	  var index = $scope.ngModel.indexOf(column);
	  var indexOfNext = index+1;
	  
	  $scope.ngModel[index] = $scope.ngModel[indexOfNext];
	  $scope.ngModel[index].order = currentOrder;
	  
	  $scope.ngModel[indexOfNext]= column;
	  $scope.ngModel[indexOfNext].order = newOrder;
  }
  
$scope.moveLeft = function (currentOrder, column) {
	  
	  var newOrder = currentOrder-1;
	  var index = $scope.ngModel.indexOf(column);
	  var indexOfBefore = index-1;
	  
	  $scope.ngModel[index] = $scope.ngModel[indexOfBefore];
	  $scope.ngModel[index].order = currentOrder;
	  
	  $scope.ngModel[indexOfBefore]= column;
	  $scope.ngModel[indexOfBefore].order = newOrder;
  }

$scope.applyFuntion = function (funct,id, entity) {
	
	$rootScope.$emit('applyFunction', {"funct":funct,"fieldId":id,"entity":entity});
}
}
})();