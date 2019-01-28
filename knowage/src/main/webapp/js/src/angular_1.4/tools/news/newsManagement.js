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
		
		$scope.stubbedNews = [
			{'title':'My first news','author':'biadmin','creationDate':new Date()},
			{'title':'My Second news','author':'biadmin','creationDate':new Date()},
			{'title':'My Third news','author':'biadmin','creationDate':new Date()},
			{'title':'My Fourth news','author':'biuser','creationDate':new Date()},
			{'title':'My Fifth news','author':'biadmin','creationDate':new Date(),'html':'<h1>Test</h1><p>ciao</p>',roles:[{'role':'administrator','active':true}]}
		]
		
		$scope.columns = [
			{"headerName":'Title',"field": 'title', "tooltipField":'title'},
			{"headerName":'Author',"field": 'author', "tooltipField":'author'},
			{"headerName":'Creation Date',"field": 'creationDate', "tooltipField":'creationDate',cellRenderer: dateFormat},
			{"headerName":"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"flex-end","border":"none"},
				suppressSorting:true,suppressFilter:true,width: 50,suppressSizeToFit:true, tooltip: false}
		]
		
		$scope.listGridOptions = {
			angularCompileRows: true,
			enableColResize: false,
            enableSorting: true,
            pagination: true,
            paginationAutoPageSize: true,
            rowSelection: 'single',
            onGridSizeChanged: resizeColumns,
            onSelectionChanged: selectNews,
            columnDefs: $scope.columns,
            rowData: $scope.stubbedNews
		}
		
		$scope.premissionGridOptions = {
			angularCompileRows: true,
            onGridSizeChanged: resizeColumns,
            columnDefs: [{"headerName":'Role',"field": 'name', "tooltipField":'name'},{"headerName":'',"field": '', "tooltipField":'', cellRenderer:checkboxRenderer,width: 50}]
		}
		
		function buttonRenderer(params){
			return 	'<md-button class="md-icon-button" ng-click="deleteRow($event,\''+params.data.valueId+'\')"><md-icon md-font-icon="fa fa-trash"></md-icon></md-button>';
		}
		
		function dateFormat(node){
			return moment(node.createdAt).format('DD/MM/YYYY HH:mm')
		}
		
		function resizeColumns(grid){
			grid.api.sizeColumnsToFit();
		}
		
		function checkboxRenderer(params){
			return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;"><input type="checkbox" ng-model="selectedNews.roles['+params.rowIndex+'].active"/></div>';
		}
		
		function selectNews(node){
			$scope.selectedNews = $scope.listGridOptions.api.getSelectedRows()[0];
			$scope.premissionGridOptions.api.setRowData($scope.rolesList);
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
		
		$scope.getRoles = function () {
			sbiModule_restServices.promiseGet("2.0", "roles")
			.then(function(response) {
				$scope.rolesList = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}
		$scope.getRoles();
		
		$scope.deleteRow = function(e){
			e.preventDefault();
			e.stopImmediatePropagation();
		}
		
		$scope.saveFunc = function(){
			debugger;
		}

	}

})();