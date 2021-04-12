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

	angular.module('dataset_view', ['ngMaterial'])
	.filter('i18n', function(sbiModule_i18n) {
		return function(label) {
			return sbiModule_i18n.getI18n(label);
		}
	})
	.directive('datasetView', function() {
		return {
			templateUrl: currentScriptPath + 'dataset-view.html',
			controller: datasetViewControllerFunction,
			 priority: 10,
			scope: {
				ngModel:"=",
				showGridView:"=?",
				tableSpeedMenuOption:"=?",
				selectedDataset:"=?",
				selectDatasetAction:"&",
				shareDatasetAction:"&",
				shareDatasetEnabled:"=?",
				previewDatasetAction:"&",
				cloneDatasetAction:"&",
				showQbeDatasetAction:"&",
				editQbeDatasetAction:"&",
				showQbeEnabled:"=?",
				showDetailDatasetAction:"&",
				showDetailDatasetEnabled:"=?",
				//executeDocumentAction:"&",
				orderingDatasetCards:"=?",
				deleteDatasetAction:"&",
				tableColumns:"=?"
			},

			link: function (scope, elem, attrs) {

				elem.css("position","static")
				 if(!attrs.tableSpeedMenuOption){
					 scope.tableSpeedMenuOption=[];
				 }

			}
		}
	});

	function datasetViewControllerFunction($scope,sbiModule_user,sbiModule_translate,sbiModule_i18n){

		$scope.i18n = sbiModule_i18n;

		$scope.i18n.loadI18nMap();

		$scope.clickDataset=function(item){
			 $scope.selectDatasetAction({ds: item});
		}

		$scope.canLoadData = function(dataset) {
			for (var i = 0; i < dataset.actions.length; i++) {
				var action = dataset.actions[i];
				if (action.name == 'loaddata') {
					return true;
				}
			}
			return false;
		}

		$scope.cockpitDatasetColumn = [
			{"headerName": sbiModule_translate.load('sbi.workspace.dataset.label'),"field":"label"},
			{"headerName": sbiModule_translate.load('sbi.workspace.dataset.name'),"field":"name"},
			{"headerName": sbiModule_translate.load('sbi.workspace.dataset.type'),"field":"dsTypeCd",width: 250,suppressSizeToFit:true,suppressMovable:true},
			{"headerName": "Tags","field":"tags", cellRenderer:tagsRenderer},
			{"headerName": sbiModule_translate.load('sbi.workspace.dataset.hasParameters'),"field":"pars","cellStyle":{"display":"inline-flex","justify-content":"center", "align-items": "center"},cellRenderer:hasParametersRenderer,suppressSorting:true,suppressFilter:true,width: 150,suppressSizeToFit:true,suppressMovable:true},
			{"headerName": sbiModule_translate.load('sbi.workspace.dataset.hasDrivers'),"field":"drivers","cellStyle":{"display":"inline-flex","justify-content":"center", "align-items": "center"},cellRenderer:hasDriversRenderer,suppressSorting:true,suppressFilter:true,width: 150,suppressSizeToFit:true,suppressMovable:true}
		];

		$scope.workspaceDatasetViewGrid = {
	        enableColResize: false,
	        enableFilter: true,
	        enableSorting: true,
	        pagination: true,
	        paginationAutoPageSize: true,
	        onGridReady: resizeColumns,
	        onGridSizeChanged: resizeColumns,
	        onRowClicked: clickDataset,
	        columnDefs : $scope.cockpitDatasetColumn,
	        rowData: $scope.ngModel,
	        getRowClass: rowClassRenderer
		};

		$scope.$watchCollection('ngModel',function(newValue,oldValue){
			if($scope.workspaceDatasetViewGrid.api){
				$scope.workspaceDatasetViewGrid.api.setRowData($scope.ngModel);
			}
		})

		function resizeColumns(){
			$scope.workspaceDatasetViewGrid.api.sizeColumnsToFit();
		}

		function clickDataset(param){
			$scope.selectDatasetAction({ds: param.data});
			$scope.selectedDataset = param.data;
		}

		function hasParametersRenderer(row){
			return (row.data.pars && row.data.pars.length > 0) ? '<i class="fa fa-check"></i>' : '';
		}

		function hasDriversRenderer(row){
			return (row.data.drivers && row.data.drivers.length > 0) ? '<i class="fa fa-check"></i>' : '';
		}

		function tagsRenderer(params){
			if(params.value && params.value.length > 0) {
				var cell = '';
				for(var i in params.value){
					cell += '<span class="miniChip">'+params.value[i].name+'</span>';
				}
				return cell;
			}
		}

		function rowClassRenderer(params) {
			if (!$scope.canLoadData(params.data)) {
				return "disabled";
			}
		}

		$scope.sbiUser = sbiModule_user;
		$scope.translate=sbiModule_translate;

	}
})();