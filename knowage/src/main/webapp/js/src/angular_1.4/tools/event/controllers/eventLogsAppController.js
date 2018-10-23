/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
(function(){
	var app = angular.module("eventModule")
	.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	}])
	.controller("eventController", eventControllerFunction)
	
	function eventControllerFunction($scope,eventService,sbiModule_messaging){
		
		$scope.eventsGrid = {
			enableColResize: false,
            enableSorting: false,
            pagination: true,
            paginationAutoPageSize: true,
            columnDefs : [{"headerName":"User","field":"user"},{"headerName":"Date","field":"formattedDate"},{"headerName":"Type","field":"type"}],
            onGridReady : function(params){
            	params.api.sizeColumnsToFit();
           },
           rowSelection:'single',
           onSelectionChanged: onSelectionChanged,
		};
		
		function onSelectionChanged(){
			var item = $scope.eventsGrid.api.getSelectedRows()
			$scope.loadDetail(item[0]);
		}
		
		$scope.eventSelectModel = ["SCHEDULER", "ETL", "COMMONJ", "DATA_MINING"]
		$scope.pageChangedFun = function(itemsPerPage, currentPageNumber) {
	
			$scope.fetchsize = itemsPerPage;
	
			$scope.offset = 0;
	
			if(currentPageNumber > 1) {
				$scope.offset = (currentPageNumber -1) * $scope.fetchsize;
			}
	
			$scope.filter = {
				offset: $scope.offset,
				fetchsize: itemsPerPage
			}
	
			if($scope.startDate != undefined) {
				$scope.filter.startDate = $scope.startDate;
			}
	
			if($scope.endDate != undefined) {
				$scope.filter.endDate = $scope.endDate;
			}
	
			if($scope.type != undefined) {
				$scope.filter.type = $scope.type;
			}
	
			eventService.getAllEvents($scope.filter).then(function(response) {
				$scope.events = response.data.results;
				$scope.totalItemCountt = response.data.total;
				$scope.eventsGrid.api.setRowData($scope.events);
	
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, "Error");
	
			});
	
		}
	
		$scope.showDetail = false;
		$scope.selectedDetail = {};
	
		$scope.loadDetail = function(item){
			$scope.showDetail = true;
			$scope.selectedDetail = angular.copy(item);
		}
		
		
	
		$scope.searchEvents = function (){
	
			if($scope.startDate){
				var startDateFormat = moment($scope.startDate).format("YYYY-MM-DD HH:mm:ss");
				$scope.filter.startDate = startDateFormat;
			}
	
			if($scope.endDate){
				var endDateFormat = moment($scope.endDate).format("YYYY-MM-DD HH:mm:ss");
				$scope.filter.endDate = endDateFormat;
			}
	
			if($scope.type){
				var type = $scope.type;
				$scope.filter.type = type;
	
			}
	
			eventService.getAllEvents($scope.filter)
				.then(function(response){
					$scope.events=response.data.results;
					$scope.events = [{"user":"Davide","formattedDate":"16-10-1985","type":"SCHEDULER"},{"user":"Davide","formattedDate":"16-10-1985","type":"SCHEDULER"}];
					$scope.eventsGrid.api.setRowData($scope.events);
					$scope.eventsGrid.api.sizeColumnsToFit();
				},	  function(response){
		
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, "Error");
		
				});
	
		}
	
	}
	
}())