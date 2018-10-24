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
	angular
		.module('cockpitModule')
		.directive('cockpitDiscoveryWidget',function(){
			return{
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/discoveryWidget/templates/discoveryWidgetTemplate.html',
				controller: cockpitDiscoveryWidgetControllerFunction,
				compile: function (tElement, tAttrs, transclude) {
					return {
						pre: function preLink(scope, element, attrs, ctrl, transclud) {},
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							element.ready(function () {
								scope.initWidget();
								scope.showWidgetSpinner();
							});
						}
					};
				}
			}
		})

	function cockpitDiscoveryWidgetControllerFunction(
			$scope,
			$rootScope,
			$timeout,
			$mdPanel,
			$q,
			$filter,
			sbiModule_translate,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetSelection,
			cockpitModule_generalServices,
			cockpitModule_template){
		
		$scope.template = cockpitModule_template;
		
		$scope.getTemplateUrl = function(template){
	  		return cockpitModule_generalServices.getTemplateUrl('discoveryWidget',template);
	  	}		
		
		$scope.selectedItems = {};
		if(!$scope.ngModel.style) $scope.ngModel.style = {"th":{},"tr":{}}; 
		if(!$scope.ngModel.settings){
			$scope.ngModel.settings = {
				"pagination" : {
					'enabled': true,
					'itemsNumber': 10,
					'frontEnd': false
				},
				"page":1,
				"table" : {
					"enabled" : true
				},
				"facets" : {
					"selection" : true
				}
			};
		}else $scope.ngModel.settings.page = 1;
		
			
		if($scope.ngModel.settings)
		
		$scope.getOptions = function(){
			var obj = {};
				obj["page"] = $scope.ngModel.settings.page ? $scope.ngModel.settings.page - 1 : 0;
				obj["itemPerPage"] = $scope.ngModel.settings.pagination ? $scope.ngModel.settings.pagination.itemsNumber : -1;
				obj["type"] = $scope.ngModel.type;
			return obj;
		}
		
		$scope.ngModel.search = {};
		$scope.facets = [];
		
		$scope.gridOptions = {
            enableColResize: true,
            enableSorting: true,
            onGridReady: resizeColumns,
            onGridSizeChanged: resizeColumns,
            onSortChanged: changeSorting,
            getRowHeight: rowHeight,
            onCellClicked: handleClick
		};
		
		function changeSorting(){
			$scope.showWidgetSpinner()
			var sorting = $scope.gridOptions.api.getSortModel();
			$scope.ngModel.settings.sortingColumn = sorting.length>0 ? $scope.getColumnName(sorting[0].colId) : '';
			$scope.ngModel.settings.sortingOrder = sorting.length>0 ? sorting[0]['sort'].toUpperCase() : '';
			$scope.refreshWidget();
		}
		
		function resizeColumns(){
			$scope.gridOptions.api.sizeColumnsToFit();
		}
		
		function rowHeight(){
			return parseInt($scope.ngModel.style.tr && $scope.ngModel.style.tr.height) || 25;
		}
		
		function handleClick(node){
	  		$scope.doSelection(node.colDef.headerName,node.value,null,null,node.data, null);
	  	}
		
		$scope.init = function(element,width,height){
			$scope.element = element[0];
		}
		
		$scope.reinit = function(){
			$scope.refreshWidget(null, 'init');
		}
		
		$scope.refresh = function(element,width,height, datasetRecords,nature) {
			if(datasetRecords){
				$scope.facets = datasetRecords.facets;
				$scope.metaData = datasetRecords.metaData;
				if($scope.ngModel.settings.table && $scope.ngModel.settings.table.enabled){
					$scope.gridOptions.headerHeight = !$scope.ngModel.style.th.enabled && 0;
					if(nature == 'init'){
						$scope.columns = $scope.getColumns(datasetRecords.metaData.fields);
						$scope.gridOptions.api.setColumnDefs($scope.columns);
						$scope.gridOptions.api.resetRowHeights();
					}
					$scope.gridOptions.api.setRowData(datasetRecords.rows);
					resizeColumns();
				}
				$scope.totalResults = datasetRecords.results;
				$scope.hideWidgetSpinner();
			}
		}
		
		$scope.getColumns = function(fields) {
			var columns = [];
			$scope.ngModel.search.columns = [];
			for(var c in $scope.ngModel.content.columnSelectedOfDataset){
				
				if($scope.ngModel.content.columnSelectedOfDataset[c].fullTextSearch){
					$scope.ngModel.search.columns.push($scope.ngModel.content.columnSelectedOfDataset[c].name);
				}
				for(var f in fields){
					if(typeof fields[f] == 'object' && $scope.ngModel.content.columnSelectedOfDataset[c].name === fields[f].header){
						var tempCol = {"headerName":$scope.ngModel.content.columnSelectedOfDataset[c].alias,"field":fields[f].name, "tooltipField":fields[f].name};
						if(!$scope.ngModel.content.columnSelectedOfDataset[c].visible) tempCol.hide = true;
						if($scope.ngModel.content.columnSelectedOfDataset[c].style) tempCol.style = $scope.ngModel.content.columnSelectedOfDataset[c].style;
						tempCol.headerComponentParams = {template: headerTemplate()};
						tempCol.cellStyle = getCellStyle;
						columns.push(tempCol);
						break;
					}
				}
			}
			return columns
		}
		
		function headerTemplate() { 
			return 	'<div class="ag-cell-label-container" role="presentation" style="background-color:'+$scope.ngModel.style.th["background-color"]+'">'+
					'	 <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>'+
					'    <div ref="eLabel" class="ag-header-cell-label" role="presentation"  style="justify-content:'+$scope.ngModel.style.th["justify-content"]+'">'+
					'       <span ref="eText" class="ag-header-cell-text" role="columnheader" style="color:'+$scope.ngModel.style.th.color+';font-size:'+$scope.ngModel.style.th["font-size"]+';font-weight:'+$scope.ngModel.style.th["font-weight"]+'"></span>'+
					'       <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>'+
					'       <span ref="eSortOrder" class="ag-header-icon ag-sort-order" ></span>'+
					'    	<span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>'+
					'   	<span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>'+
					'  		<span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>'+
					'	</div>'+
					'</div>';
		}
		
		function getCellStyle(params){
			var tempStyle = angular.copy(params.colDef.style);
			return tempStyle;
		}
		
		$scope.getColumnName = function(colNum){
			for(var k in $scope.metaData.fields){
				if($scope.metaData.fields[k].dataIndex && $scope.metaData.fields[k].dataIndex == colNum) return $scope.metaData.fields[k].header;
			}
		}
		
		$scope.deleteFilterSelection = function(group, value){
			var item = {};
			item.aggregated=false;
			item.columnName=group;
			item.columnAlias=group;
			item.value = value;
			item.ds=$scope.ngModel.dataset.label;
			delete cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label][group];
			$rootScope.$broadcast('DELETE_SELECTION',item);
		}
		
		$scope.selectItem = function(group, item){
			if($scope.dimensions && $scope.dimensions.width<600){
				$scope.toggleMenu();
			}
//			if($scope.ngModel.settings.facets.selection){
				if(cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label] && cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label][group]==item.column_1){
					$scope.deleteFilterSelection(group, item.column_1);
				}else{
					$scope.doSelection(group, item.column_1, null, null, item, null, undefined, !$scope.ngModel.settings.facets.selection);
				}
//			}else{
//				$scope.showWidgetSpinner();
//				var tempFilter = {
//					"colAlias":group,
//					"colName":group,
//					"dataset":$scope.ngModel.dataset.label,
//					filterOperator : "=",
//					filterVals : [item.column_1],
//					type:"java.lang.String"
//				};
//				if($scope.ngModel.filters && $scope.ngModel.filters.length>0){
//					for(var k in $scope.ngModel.filters){
//						if($scope.ngModel.filters[k].colName == group && $scope.ngModel.filters[k].filterVals.indexOf(item.column_1)!=-1) {
//							$scope.ngModel.filters.splice(k,1);
//						}else {
//							$scope.ngModel.filters.push(tempFilter);
//						}
//					}
//				}else {
//					$scope.ngModel.filters = [tempFilter];
//				}
//				$scope.refreshWidget();
//			}
		}
		
		$scope.first = function(){
			$scope.ngModel.settings.page = 1;
			$scope.refreshWidget();
		}
		
		$scope.prev = function(){
			$scope.ngModel.settings.page = $scope.ngModel.settings.page - 1;
			$scope.refreshWidget();
		}
		
		$scope.next = function(){
			$scope.ngModel.settings.page = $scope.ngModel.settings.page + 1;
			$scope.refreshWidget();
		}
		
		$scope.last = function(){
			$scope.ngModel.settings.page = $scope.totalPageNumber();
			$scope.refreshWidget();
		}
		
		$scope.maxPageNumber = function(){
			if($scope.ngModel.settings.page*$scope.ngModel.settings.pagination.itemsNumber < $scope.totalResults) return $scope.ngModel.settings.page*$scope.ngModel.settings.pagination.itemsNumber;
			else return $scope.totalResults;
		}
		
		$scope.totalPageNumber = function(){
			return Math.ceil($scope.totalResults/$scope.ngModel.settings.pagination.itemsNumber);
		}
		
		$scope.isFacetVisible = function(facet){
			for(var k in $scope.ngModel.content.columnSelectedOfDataset){
				if($scope.ngModel.content.columnSelectedOfDataset[k].name == facet && $scope.ngModel.content.columnSelectedOfDataset[k].facet) return true
			}
			return false;
		}

		$scope.hasFacets = function() {
		    for(var k in $scope.ngModel.content.columnSelectedOfDataset){
            	if($scope.ngModel.content.columnSelectedOfDataset[k].facet) {
            	    return true;
            	}
            }
            return false;
		}

		$scope.toggleMenu = function() {
			$scope.sidemenuOpened = !$scope.sidemenuOpened;
		}
		
		var timer;
		$scope.searchWatcher = $scope.$watch('ngModel.search.text', function(newValue, oldValue){
			if(oldValue != newValue){
				if(timer){
					$timeout.cancel(timer)
				}  
				timer = $timeout(function(){
					$scope.showWidgetSpinner();
					$scope.refreshWidget();
				},500)
			}
		});		
		
		$scope.facetSettingsWatcher = $scope.$watch('ngModel.settings.facet', function(newValue,oldValue){
			if(newValue && oldValue != newValue){
				$scope.ngModel.filters
			}
		})
		
		$scope.dimensionWatcher = $scope.$watch('element.clientWidth',function(newValue, oldValue){
			if(newValue) $scope.dimensions = {"width": newValue};
		})
		
		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: discoveryWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: $scope.getTemplateUrl('discoveryWidgetEditPropertyTemplate'),
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: false,
					locals: {finishEdit:finishEdit,model:$scope.ngModel},
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}
		
		$scope.reinit();
		
	}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("discovery",{'initialDimension':{'width':15, 'height':10},'updateble':true,'cliccable':true});

})();