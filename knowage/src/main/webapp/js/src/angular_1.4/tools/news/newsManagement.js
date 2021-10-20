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

(function () {
	agGrid.initialiseAgGridWithAngular1(angular);
	angular
		.module('newsManagement', ['ngMaterial', 'sbiModule', 'angular-list-detail', 'agGrid', 'ngWYSIWYG'])
		.config(function ($mdThemingProvider) {
			$mdThemingProvider.theme('knowage');
			$mdThemingProvider.setDefaultTheme('knowage');
		})
		.controller('newsManagementController', NewsManagementController)

	function NewsManagementController($scope, sbiModule_restServices, sbiModule_translate, $angularListDetail, $mdDialog, $mdToast, sbiModule_i18n, sbiModule_messaging, knModule_aggridLabels, sbiModule_config) {
		$scope.translate = sbiModule_translate;
		$scope.loading = false;


		$scope.typeMapping = [{
			"id": 1,
			"value": "News"
		}, {
			"id": 2,
			"value": "Notification"
		}, {
			"id": 3,
			"value": "Warning"
		}];

		$scope.columns = [{
				"headerName": $scope.translate.load('sbi.news.title'),
				"field": 'title',
				"tooltipField": 'title',
				"suppressMovable": true
			},
			{
				"headerName": $scope.translate.load('sbi.news.description'),
				"field": 'description',
				"tooltipField": 'description',
				"suppressMovable": true
			},
			{
				"headerName": "",
				cellRenderer: buttonRenderer,
				"field": "valueId",
				"cellStyle": {
					"text-align": "right",
					"display": "inline-flex",
					"justify-content": "flex-end",
					"border": "none"
				},
				suppressSorting: true,
				suppressFilter: true,
				width: 50,
				suppressSizeToFit: true,
				tooltip: false,
				"suppressMovable": true
			}
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
			localeText: knModule_aggridLabels
		}

		$scope.permissionGridOptions = {
			onGridSizeChanged: resizeColumns,
			rowSelection: 'multiple',
			rowMultiSelectWithClick: true,
			columnDefs: [{
					"headerName": $scope.translate.load('sbi.news.role'),
					"field": 'name',
					"tooltipField": 'name',
					filter: 'agTextColumnFilter'
				},
				{
					"headerName": '',
					"field": '',
					"tooltipField": '',
					width: 50,
					headerCheckboxSelection: true,
					headerCheckboxSelectionFilteredOnly: false,
					checkboxSelection: true
				}
			]
		}

		function buttonRenderer(params) {
			return '<md-button class="md-icon-button" ng-click="deleteRow($event,\'' + params.data.id + '\')"><md-icon md-font-icon="fa fa-trash"></md-icon></md-button>';
		}

		function dateFormat(node) {
			return moment(node.value).format()
		}

		function resizeColumns(grid) {
			grid.api.sizeColumnsToFit();
		}

		function selectNews(params) {
			$scope.loading = true;
			sbiModule_restServices.promiseGet("2.0", "news/" + (params.id || $scope.listGridOptions.api.getSelectedRows()[0].id) + "?isTechnical=true")
				.then(function (response) {
					$scope.selectedNews = response.data;
					$scope.remapRoles();
					$scope.tempExpirationDate = new Date($scope.selectedNews.expirationDate);
					$scope.loading = false;
				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load('sbi.general.error'));
					$scope.loading = false;
				});
		}

		$scope.remapRoles = function () {
			var tempSelectedRoles = angular.copy($scope.selectedNews.roles);
			$scope.permissionGridOptions.api.setRowData($scope.rolesList);
			$scope.permissionGridOptions.api.forEachNode(function (rowNode, index) {
				for (var r in tempSelectedRoles) {
					if (rowNode.data.id == tempSelectedRoles[r].id) {
						rowNode.setSelected(true);
						continue;
					}
				}
			});
		}

		$scope.editorConfig = {
			sanitize: false,
			toolbar: [{
					name: 'basicStyling',
					items: ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript', '-', 'leftAlign', 'centerAlign', 'rightAlign', 'blockJustify', '-']
				},
				{
					name: 'paragraph',
					items: ['orderedList', 'unorderedList', 'outdent', 'indent', '-']
				},
				{
					name: 'colors',
					items: ['fontColor', 'backgroundColor', '-']
				},
				{
					name: 'styling',
					items: ['font', 'size', 'format']
				}
			]
		};


		$scope.newNews = function () {
			$scope.selectedNews = {};
			$scope.remapRoles();
			$scope.permissionGridOptions.api.setRowData($scope.rolesList);
		}

		$scope.getNews = function () {
			sbiModule_restServices.promiseGet("2.0", "news")
				.then(function (response) {
					if (response.data.length == 0) $scope.listGridOptions.api.showNoRowsOverlay();
					$scope.listGridOptions.api.setRowData(response.data);
				}, function (response) {
					$scope.listGridOptions.api.showNoRowsOverlay();
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load('sbi.general.error'));
				});
		}
		$scope.getNews();

		$scope.getRoles = function () {
			sbiModule_restServices.promiseGet("2.0", "roles")
				.then(function (response) {
					$scope.rolesList = response.data;
				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load('sbi.general.error'));

				});
		}
		$scope.getRoles();

		$scope.deleteRow = function (e, item) {
			e.preventDefault();
			e.stopImmediatePropagation();

			var confirm = $mdDialog.confirm()
				.title($scope.translate.load('sbi.news.delete.title'))
				.targetEvent(event)
				.textContent($scope.translate.load('sbi.news.delete.text'))
				.ok($scope.translate.load("sbi.general.yes"))
				.cancel($scope.translate.load("sbi.general.No"));

			$mdDialog
				.show(confirm)
				.then(function () {
						sbiModule_restServices.promiseDelete("2.0", "news/" + item).then(function () {
							if ($scope.selectedNews && item == $scope.selectedNews.id) delete $scope.selectedNews;
							$scope.getNews();
						})
					},
					function () {})

		}

		$scope.saveFunc = function () {
			if ($scope.newsForm.$valid) {
				$scope.selectedNews.roles = [];
				$scope.selectedNews.expirationDate = moment($scope.tempExpirationDate).valueOf();
				var tempPermissions = $scope.permissionGridOptions.api.getSelectedRows();
				for (var r in tempPermissions) {
					$scope.selectedNews.roles.push({
						'id': tempPermissions[r].id,
						'name': tempPermissions[r].name
					});
				}
				sbiModule_restServices.promisePost("2.0", "news", $scope.selectedNews).then(function (response) {
					$scope.getNews();

					if (!$scope.selectedNews.id) {
						selectNews({
							id: response.data
						});
					}
					
					$scope.socket = new WebSocket('ws://'+sbiModule_config.contextName+'/webSocket');
					$scope.socket.send(JSON.stringify(message));

					sbiModule_messaging.showSuccessMessage($scope.translate.load('sbi.news.success.text'), $scope.translate.load('sbi.general.success'), 5000);
				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load('sbi.general.error'));
				})
			} else {
				sbiModule_messaging.showErrorMessage($scope.translate.load('sbi.news.error.missing'), $scope.translate.load('sbi.general.error'));
			}
		}

	}

})();