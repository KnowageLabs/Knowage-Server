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
	var scripts = document.getElementsByTagName('script')
	var componentTreePath = scripts[scripts.length-1].src;
	componentTreePath = componentTreePath.substring(0, componentTreePath.lastIndexOf('/') + 1);

angular.module('table-containerModule').directive('tableContainer',['sbiModule_restServices','sbiModule_messaging',function(sbiModule_restServices,sbiModule_messaging){
	return{
		
		 scope:{
			 
			 tableDef:'=tabledef'
			 
		 },
		 transclude: true,
		 controller:function($scope,$rootScope){
			 
		       $scope.itemList;
		       $scope.selectedItem;
		       
		       $scope.itemSelected = function(item){
		    	   
		    	   $rootScope.$broadcast($scope.tableDef.selectedEvent,item)
		       }
		       
		       
		       $scope.refresh = function(){
		    	   $scope.getData($scope.tableDef.dataPath);
		    	   
		       }
		       
		       $scope.registerListener=function(){
		    	   angular.forEach($scope.tableDef.broadcasts, function(broadcast, key) {
		               $scope.$on(broadcast.event,broadcast.handler)
		           });
		       }
		       
		       $scope.getData = function(path){
		    	   $scope.selectedItem = {};
		    	   sbiModule_restServices.promiseGet(path,"")
		   		.then(function(response) {
		   			$scope.itemList =response.data;
		   		}, function(response) {
		   			sbiModule_messaging.showErrorMessage('ERROR','Error while loading '+$scope.tableDef.name+' table');
		   			
		   		});
		       }
		        },
		 restrict: 'E',
		 templateUrl:componentTreePath+'/template/table-container.html',
		
		link:function($scope){
			console.log('Hello from table-container directive');
			$scope.getData($scope.tableDef.dataPath);
			$scope.registerListener();
		}
	}
}])

})();