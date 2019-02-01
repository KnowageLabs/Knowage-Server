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

	function NewsManagementController($scope, sbiModule_restServices, sbiModule_translate,$angularListDetail, $mdDialog, $mdToast, sbiModule_i18n, sbiModule_messaging,knModule_aggridLabels){
		$scope.translate = sbiModule_translate;
		$scope.loading = false;
	
		$scope.columns = [
			{"headerName":'Title',"field": 'title', "tooltipField":'title', "suppressMovable":true},
			{"headerName":'Description',"field": 'description', "tooltipField":'description',"suppressMovable":true},
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
            localeText : knModule_aggridLabels
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
			return moment(node.value).format()
		}
		
		function resizeColumns(grid){
			grid.api.sizeColumnsToFit();
		}
		
		function checkboxRenderer(params){
			return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;"><input type="checkbox" ng-model="tempPermissions['+params.rowIndex+'].active"/></div>';
		}
		
		function selectNews(){
			$scope.loading = true;
			sbiModule_restServices.promiseGet("2.0", "news/" + $scope.listGridOptions.api.getSelectedRows()[0].id)
			.then(function(response) {
				$scope.selectedNews = response.data;
				$scope.loading = false;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				$scope.loading = false;
			});
			$scope.tempPermissions = $scope.rolesList;
			for(var r in $scope.tempPermissions){
				for(var k in $scope.selectedNews.roles){
					if($scope.selectedNews.roles[k].id == $scope.tempPermissions[r].id){
						$scope.tempPermissions[r].active = true;
					}
				}
			}
			
			$scope.tempExpirationDate = new Date($scope.selectedNews.expirationDate);
			$scope.permissionGridOptions.api.setRowData($scope.tempPermissions);
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
			sbiModule_restServices.promiseDelete("2.0", "news/" + item ).then(function(){
				$scope.getNews();
			})
		}
		
		$scope.saveFunc = function(){
			$scope.selectedNews.roles = [];
			for(var r in $scope.tempPermissions){
				if($scope.tempPermissions[r].active) {
					$scope.selectedNews.roles.push({'id':$scope.tempPermissions[r].id,'name':$scope.tempPermissions[r].name});
				}
			}
			var test = {
					"id": 17,
				    "title": $scope.selectedNews.title,
				    "description": $scope.selectedNews.description,
				    "type": 1,
				    "html": $scope.selectedNews.html,
				    "expirationDate": moment($scope.selectedNews.expirationDate).valueOf(),
				    "roles": $scope.selectedNews.roles
				}
			sbiModule_restServices.promisePost("2.0", "news", test).then(function(){
				$scope.getNews();
				sbiModule_messaging.showSuccessMessage('The news has been saved', 'Success' ,5000);
			},function(response){
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			})
		}

	}

})();