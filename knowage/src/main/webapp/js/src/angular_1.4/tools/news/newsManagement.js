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
		.module('newsManagement', ['ngMaterial','sbiModule','angular-list-detail','agGrid','summernote'])
		.config(function($mdThemingProvider) {
		    $mdThemingProvider.theme('knowage');
		    $mdThemingProvider.setDefaultTheme('knowage');
		})
		.controller('newsManagementController', NewsManagementController)

	function NewsManagementController($scope, sbiModule_restServices, sbiModule_translate,$angularListDetail, $mdDialog, $mdToast, sbiModule_i18n, sbiModule_messaging){
		$scope.translate = sbiModule_translate;
	
		$scope.columns = [
			{"headerName":'Title',"field": 'title', "tooltipField":'title', "suppressMovable":true, filter: 'agTextColumnFilter'},
			{"headerName":'Expiration Date',"field": 'expirationDate', "tooltipField":'expirationDate',cellRenderer: dateFormat,"suppressMovable":true, filter:'agDateColumnFilter'},
			{"headerName":"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"flex-end","border":"none"},
				suppressSorting:true,suppressFilter:true,width: 50,suppressSizeToFit:true, tooltip: false, "suppressMovable":true}
		]
		
		$scope.listGridOptions = {
			angularCompileRows: true,
			enableColResize: false,
            enableSorting: true,
            enableFilter: true,
            pagination: true,
            paginationAutoPageSize: true,
            rowSelection: 'single',
            onGridSizeChanged: resizeColumns,
            onSelectionChanged: selectNews,
            columnDefs: $scope.columns,
            localeText : {noRowsToShow: $scope.translate.load('kn.table.norows')}
		}
		
		$scope.permissionGridOptions = {
			angularCompileRows: true,
            onGridSizeChanged: resizeColumns,
            columnDefs: [{"headerName":'Role',"field": 'name', "tooltipField":'name', filter: 'agTextColumnFilter'},{"headerName":'',"field": '', "tooltipField":'', cellRenderer:checkboxRenderer,width: 50}]
		}
		
		function buttonRenderer(params){
			return 	'<md-button class="md-icon-button" ng-click="deleteRow($event,\''+params.data.id+'\')"><md-icon md-font-icon="fa fa-trash"></md-icon></md-button>';
		}
		
		function dateFormat(node){
			return moment(node.value).format('MM/DD/YYYY HH:mm')
		}
		
		function resizeColumns(grid){
			grid.api.sizeColumnsToFit();
		}
		
		function checkboxRenderer(params){
			return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;"><input type="checkbox" ng-model="selectedNews.roles['+params.rowIndex+'].active"/></div>';
		}
		
		function selectNews(){
			sbiModule_restServices.promiseGet("2.0", "news/"+$scope.listGridOptions.api.getSelectedRows()[0].id)
			.then(function(response) {
				$scope.selectedNews
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
			$scope.selectedNews = $scope.listGridOptions.api.getSelectedRows()[0];
			$scope.tempExpirationDate = new Date($scope.selectedNews.expirationDate);
			$scope.permissionGridOptions.api.setRowData($scope.rolesList);
			$scope.$apply();
		}

		$scope.wysiwygOptions = {
		    height: 300,
		    toolbar: [
		        ['style', ['bold', 'italic', 'underline', 'clear']],
		        ['font', ['strikethrough', 'superscript', 'subscript']],
		        ['fontname', ['fontname']],
		        ['fontsize', ['fontsize']],
		        ['color', ['color']],
		        ['para', ['ul', 'ol', 'paragraph']],
		        ['insert',['picture','link','hr']]
		      ],
		    fontNames: ['Roboto','Comic Sans MS','Sacramento'],
		    fontNamesIgnoreCheck : ['Roboto','Comic Sans MS','Sacramento']
		};
		
		$scope.newNews = function(){
			$scope.selectedNews = {};
			$scope.permissionGridOptions.api.setRowData($scope.rolesList);
		}
		
		$scope.getNews = function(){
			sbiModule_restServices.promiseGet("2.0", "news")
			.then(function(response) {
				if(response.data.length == 0) $scope.listGridOptions.api.showNoRowsOverlay();
				else $scope.listGridOptions.api.setRowData(response.data);
			}, function(response) {
				$scope.listGridOptions.api.showNoRowsOverlay();
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}
		$scope.getNews();
		
		$scope.getRoles = function () {
			sbiModule_restServices.promiseGet("2.0", "roles")
			.then(function(response) {
				$scope.rolesList = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}
		$scope.getRoles();
		
		$scope.deleteRow = function(e, item){
			e.preventDefault();
			e.stopImmediatePropagation();
			sbiModule_restServices.promiseDelete("2.0", "news/delete/"+item ).then(function(){

			})
		}
		
		$scope.saveFunc = function(){
			sbiModule_restServices.promisePost("2.0", "news", $scope.selectedNews).then(function(){

			},function(response){
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			})
		}

	}

})();