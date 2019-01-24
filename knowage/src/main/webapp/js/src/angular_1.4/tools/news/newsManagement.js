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

(function() {
	agGrid.initialiseAgGridWithAngular1(angular);
	angular
		.module('newsManagement', ['ngMaterial','sbiModule','angular-list-detail','agGrid'])
		.config(function($mdThemingProvider) {
		    $mdThemingProvider.theme('knowage');
		    $mdThemingProvider.setDefaultTheme('knowage');
		})
		.controller('newsManagementController', NewsManagementController)

	function NewsManagementController($scope, sbiModule_restServices, sbiModule_translate,$angularListDetail, $mdDialog, $mdToast, sbiModule_i18n, sbiModule_messaging){
		$scope.translate = sbiModule_translate;
		$scope.stubbedNews = [
			{'title':'My first news','author':'biadmin','creationDate':new Date()},
			{'title':'My Second news','author':'biadmin','creationDate':new Date()},
			{'title':'My Third news','author':'biadmin','creationDate':new Date()},
			{'title':'My Fourth news','author':'biuser','creationDate':new Date()},
			{'title':'My Fifth news','author':'biadmin','creationDate':new Date()}
		]
		
		$scope.columns = [
			{"headerName":'Title',"field": 'title', "tooltipField":'title'},
			{"headerName":'Author',"field": 'author', "tooltipField":'author'},
			{"headerName":'Creation Date',"field": 'creationDate', "tooltipField":'creationDate',cellRenderer: dateFormat}
		]
		
		function dateFormat(node){
			return moment(node.createdAt).format('MM/DD/YYYY HH:mm')
		}
		
		function resizeColumns(){
			$scope.listGridOptions.api.sizeColumnsToFit();
		}
		
		$scope.listGridOptions = {
			enableColResize: false,
            enableSorting: true,
            pagination: true,
            paginationAutoPageSize: true,
            rowSelection: 'single',
            onGridSizeChanged: resizeColumns,
            onRowSelected: selectNews,
            columnDefs: $scope.columns,
            rowData: $scope.stubbedNews
		}
		
		function selectNews(node){
			$scope.selectedNews = node.data;
			$scope.$apply();
		}
		
	}

})();