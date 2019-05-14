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
		.directive('cockpitAdvancedTableWidget',function(){
			return{
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/advancedTableWidget/templates/advancedTableWidgetTemplate.html',
				controller: cockpitAdvancedTableWidgetControllerFunction,
				compile: function (tElement, tAttrs, transclude) {
					return {
						pre: function preLink(scope, element, attrs, ctrl, transclud) {
							
						},
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							element.ready(function () {
	                    		scope.initWidget();
	                        });
						}
					};
				}
			}
		})
	function cockpitAdvancedTableWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdToast,
			$timeout,
			$mdPanel,
			$q,
			$sce,
			$filter,
			sbiModule_config,
			sbiModule_translate,
			sbiModule_restServices,
			cockpitModule_datasetServices,
			cockpitModule_generalOptions,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_analyticalDrivers,
			cockpitModule_properties){
		
		$scope.showGrid = true;
		$scope.bulkSelection = false;
		$scope.selectedCells = [];
		$scope.selectedRows = [];
		
		var _rowHeight;
		if(!$scope.ngModel.settings){
			$scope.ngModel.settings = {
				"pagination" : {
					'enabled': true,
					'itemsNumber': 10,
					'frontEnd': false
				},
				"page":1
			};
		}else $scope.ngModel.settings.page = 1;
		
		if(!$scope.ngModel.style) $scope.ngModel.style = {"th":{},"tr":{}}; 
		
		function getColumns(fields) {
			var columns = [];
			for(var c in $scope.ngModel.content.columnSelectedOfDataset){
				for(var f in fields){
					if(typeof fields[f] == 'object' && $scope.ngModel.content.columnSelectedOfDataset[c].alias === fields[f].header){
						var tempCol = {"headerName":$scope.ngModel.content.columnSelectedOfDataset[c].aliasToShow || $scope.ngModel.content.columnSelectedOfDataset[c].alias,"field":fields[f].name,"measure":$scope.ngModel.content.columnSelectedOfDataset[c].fieldType};
						tempCol.pinned = $scope.ngModel.content.columnSelectedOfDataset[c].pinned;
						if(!$scope.ngModel.content.columnSelectedOfDataset[c].hideTooltip) tempCol.tooltipField = fields[f].name;
						if($scope.ngModel.content.columnSelectedOfDataset[c].style) tempCol.style = $scope.ngModel.content.columnSelectedOfDataset[c].style;
						if($scope.ngModel.content.columnSelectedOfDataset[c].style && $scope.ngModel.content.columnSelectedOfDataset[c].style.hiddenColumn) tempCol.hide = true;
						if($scope.ngModel.settings.summary && $scope.ngModel.settings.summary.enabled) {
							tempCol.pinnedRowCellRenderer = SummaryRowRenderer,
							tempCol.pinnedRowCellRendererParams = {"title":$scope.ngModel.settings.summary.title, "style": $scope.ngModel.settings.summary.style};
						}
						if($scope.ngModel.content.columnSelectedOfDataset[c].style && $scope.ngModel.content.columnSelectedOfDataset[c].style.width) {
							tempCol.width = parseInt($scope.ngModel.content.columnSelectedOfDataset[c].style.width);
							tempCol.suppressSizeToFit = true;
						}
						if($scope.ngModel.content.columnSelectedOfDataset[c].ranges) tempCol.ranges = $scope.ngModel.content.columnSelectedOfDataset[c].ranges;
						tempCol.headerComponentParams = {template: headerTemplate()};
						
						tempCol.cellStyle = getCellStyle;
						
						tempCol.fieldType = cockpitModule_generalOptions.typesMap[$scope.ngModel.content.columnSelectedOfDataset[c].type].label;
						if($scope.ngModel.content.columnSelectedOfDataset[c].momentDateFormat) tempCol.dateFormat = $scope.ngModel.content.columnSelectedOfDataset[c].momentDateFormat;
						if(tempCol.fieldType == 'date') tempCol.valueFormatter = dateFormatter;
						if(tempCol.fieldType == 'timestamp') tempCol.valueFormatter = dateTimeFormatter;
						if(tempCol.fieldType == 'float' || tempCol.fieldType == 'integer' ) tempCol.valueFormatter = numberFormatter;
						
						tempCol.cellRenderer = cellRenderer;
						
						if($scope.ngModel.settings.autoRowsHeight) {
							tempCol.autoHeight = true;
							if(tempCol.style) tempCol.style['white-space'] = 'normal';
							else tempCol.style = {'white-space':'normal'};
						}else if(tempCol.style) tempCol.style['white-space'] = 'nowrap';
						tempCol.autoHeight = $scope.ngModel.settings.autoRowsHeight || false;
						if($scope.ngModel.content.columnSelectedOfDataset[c].visType) {
							tempCol.visType = $scope.ngModel.content.columnSelectedOfDataset[c].visType;
							if($scope.ngModel.content.columnSelectedOfDataset[c].visType.toLowerCase() == 'chart' || $scope.ngModel.content.columnSelectedOfDataset[c].visType.toLowerCase() == 'text & chart') tempCol.chart = $scope.ngModel.content.columnSelectedOfDataset[c].barchart;
						}
						columns.push(tempCol);
						break;
					}
				}
			}
			return columns
		}
		
		function getColumnName(colNum){
			for(var k in $scope.metadata.fields){
				if($scope.metadata.fields[k].dataIndex && $scope.metadata.fields[k].dataIndex == colNum) return $scope.metadata.fields[k].header;
			}
		}
		
		function headerTemplate() { 
			var cellClasses = 'cellContainer ';
			if($scope.ngModel.style && $scope.ngModel.style.th && $scope.ngModel.style.th.multiline) cellClasses = 'cellContainer multiLineHeader';
			return 	'<div class="ag-cell-label-container" role="presentation" style="background-color:'+$scope.ngModel.style.th["background-color"]+'">'+
					'	 <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>'+
					'    <div ref="eLabel" class="ag-header-cell-label" role="presentation" style="justify-content:'+$scope.ngModel.style.th["justify-content"]+'">'+
					'       <div class="'+cellClasses+'" style="justify-content:'+$scope.ngModel.style.th["justify-content"]+'">'+
					'			<span ref="eText" class="ag-header-cell-text" role="columnheader" style="color:'+$scope.ngModel.style.th.color+';font-style:'+$scope.ngModel.style.th["font-style"]+';font-size:'+$scope.ngModel.style.th["font-size"]+';font-weight:'+$scope.ngModel.style.th["font-weight"]+'"></span></div>'+
					'       <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>'+
					'       <span ref="eSortOrder" class="ag-header-icon ag-sort-order" ></span>'+
					'    	<span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>'+
					'   	<span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>'+
					'  		<span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>'+
					'	</div>'+
					'</div>';
		}
		
		function getCellStyle(params){
			var tempStyle = params.colDef.style || {};
			if(params.colDef.ranges && params.colDef.ranges.length > 0){
				for(var k in params.colDef.ranges){
					if (params.value!="" && eval(params.value + params.colDef.ranges[k].operator + params.colDef.ranges[k].value)) {
						tempStyle['background-color'] = params.colDef.ranges[k]['background-color'] || '';
						tempStyle['color'] = params.colDef.ranges[k]['color'] || '';
                        if (params.colDef.ranges[k].operator == '==') break;
                    }
				}
			}
			return tempStyle;
		}
		
		//VALUE FORMATTERS
		function dateFormatter(params){
			return isNaN(moment(params.value,'DD/MM/YYYY')) ? params.value : moment(params.value,'DD/MM/YYYY').locale(sbiModule_config.curr_language).format(params.colDef.dateFormat || 'LL');
		}
		
		function dateTimeFormatter(params){
			return isNaN(moment(params.value,'DD/MM/YYYY HH:mm:ss.SSS'))? params.value : moment(params.value,'DD/MM/YYYY HH:mm:ss.SSS').locale(sbiModule_config.curr_language).format(params.colDef.dateFormat || 'LLL');
		}
		
		function numberFormatter(params){
			if(!params.colDef.style || (params.colDef.style && !params.colDef.style.asString)) {
				var defaultPrecision = (params.colDef.fieldType == 'float') ? 2 : 0;
				return $filter('number')(params.value, (params.colDef.style && typeof params.colDef.style.precision != 'undefined') ? params.colDef.style.precision : defaultPrecision);
			}else return params.value;
		}
		
		//CELL RENDERERS
		function cellRenderer () {}
		
		cellRenderer.prototype.init = function(params){
			this.eGui = document.createElement('span');
			var tempValue = params.valueFormatted || params.value;
			if(!params.node.rowPinned){
				if(params.colDef.visType && (params.colDef.visType.toLowerCase() == 'chart' || params.colDef.visType.toLowerCase() == 'text & chart')){
					var percentage = Math.round((params.value - (params.colDef.chart.minValue || 0))/((params.colDef.chart.maxValue || 100) - (params.colDef.chart.minValue || 0))*100);
					if(percentage < 0) percentage = 0;
					if(percentage > 100) percentage = 100;
					this.eGui.innerHTML = '<div class="inner-chart-bar" style="justify-content:'+params.colDef.chart.style['justify-content']+'"><div class="bar" style="justify-content:'+params.colDef.chart.style['justify-content']+';background-color:'+params.colDef.chart.style['background-color']+';width:'+percentage+'%">'+(params.colDef.visType.toLowerCase() == 'text & chart' ? '<span style="color:'+params.colDef.chart.style.color+'">'+params.value+'</span>' : '')+'</div></div>';
				}
				if(params.colDef.ranges && params.colDef.ranges.length > 0){
					for(var k in params.colDef.ranges){
						if (params.value!="" && eval(params.value + params.colDef.ranges[k].operator + params.colDef.ranges[k].value)) {
							tempValue = '<i class="'+params.colDef.ranges[k].icon+'"></i>'
	                        if (params.colDef.ranges[k].operator == '==') break;
	                    }
					}
				}
				if(params.colDef.style && params.colDef.style.maxChars){
					tempValue = tempValue.toString().substring(0,params.colDef.style.maxChars);
				}
			}
			if(this.eGui.innerHTML == '') this.eGui.innerHTML = ((params.colDef.style && params.colDef.style.prefix) || '') + tempValue + ((params.colDef.style && params.colDef.style.suffix) || '');
		}
		
		cellRenderer.prototype.getGui = function() {
		    return this.eGui;
		};
		
		cellRenderer.prototype.refresh = function(params) {
			this.eGui.parentNode.style.backgroundColor = params.colDef.style && params.colDef.style['background-color'] || 'inherit';
			if($scope.bulkSelection){
				if(params.colDef.field == $scope.bulkSelection && $scope.selectedCells.indexOf(params.value) > -1){
					this.eGui.parentNode.style.backgroundColor = $scope.ngModel.settings.multiselectablecolor || '#ccc';
				}
			}
		}
		
		function SummaryRowRenderer () {}

		SummaryRowRenderer.prototype.init = function(params) {
		    this.eGui = document.createElement('div');
		    this.eGui.style.color = (params.style && params.style.color) || (params.colDef.style && params.colDef.style.color) || "";
		    this.eGui.style.backgroundColor = (params.style && params.style['background-color']) || (params.colDef.style && params.colDef.style['background-color']) || "";
		    this.eGui.style.justifyContent = (params.style && params.style['justify-content']) || (params.colDef.style && params.colDef.style['justify-content']) || "";
		    this.eGui.style.fontSize = (params.style && params.style['font-size']) || (params.colDef.style && params.colDef.style['font-size']) || "";
		    this.eGui.style.fontWeight = (params.style && params.style['font-weight']) || (params.colDef.style && params.colDef.style['font-weight']) || "";
		    this.eGui.style.fontStyle = (params.style && params.style['font-style']) || (params.colDef.style && params.colDef.style['font-style']) || "";
		    this.eGui.innerHTML = '';
		    if(params.style && params.style['pinnedOnly'] && params.column.pinned && params.column.lastLeftPinned) this.eGui.innerHTML ='<b style="margin-right: 4px;">'+params.title+'</b>';
		    if(params.valueFormatted || params.value){
		    	if(((!params.style || !params.style['pinnedOnly']) && params.title)) this.eGui.innerHTML ='<b style="margin-right: 4px;">'+params.title+'</b>';
			    this.eGui.innerHTML += params.valueFormatted || params.value;
		    }
		};

		SummaryRowRenderer.prototype.getGui = function() {
		    return this.eGui;
		};
		
		$scope.init=function(element,width,height){
			$scope.refreshWidget(null, 'init');
			$timeout(function(){
				$scope.widgetIsInit=true;
			},500);
		}
		
		$scope.reinit = function(){
			$scope.refreshWidget();
		}
		
		$scope.refresh = function(element,width,height, datasetRecords,nature) {
			$scope.showWidgetSpinner();
			
			if(datasetRecords){
				$scope.metadata = datasetRecords.metaData;
				$scope.totalRows = datasetRecords.results;
				if($scope.ngModel.style && $scope.ngModel.style.tr && $scope.ngModel.style.tr.height){
					_rowHeight = $scope.ngModel.style.tr.height;
					$scope.advancedTableGrid.api.resetRowHeights();
				}else delete _rowHeight;
				if($scope.ngModel.style && $scope.ngModel.style.th){
					if($scope.ngModel.style.th.enabled) $scope.advancedTableGrid.api.setHeaderHeight($scope.ngModel.style.th.height || 32);
					else $scope.advancedTableGrid.api.setHeaderHeight(0);
				}
				if(nature != 'sorting') $scope.advancedTableGrid.api.setColumnDefs(getColumns(datasetRecords.metaData.fields));
				if($scope.ngModel.settings.summary && $scope.ngModel.settings.summary.enabled) {
					$scope.advancedTableGrid.api.setRowData(datasetRecords.rows.slice(0,datasetRecords.rows.length-1));
					$scope.advancedTableGrid.api.setPinnedBottomRowData([datasetRecords.rows[datasetRecords.rows.length-1]]);
				}
				else {
					$scope.advancedTableGrid.api.setRowData(datasetRecords.rows);
					$scope.advancedTableGrid.api.setPinnedBottomRowData([]);
				}
				if($scope.ngModel.settings.pagination && $scope.ngModel.settings.pagination.enabled && !$scope.ngModel.settings.pagination.frontEnd){
					$scope.ngModel.settings.pagination.itemsNumber = $scope.ngModel.settings.pagination.itemsNumber || 15;
					$scope.totalPages = Math.ceil($scope.totalRows/$scope.ngModel.settings.pagination.itemsNumber) || 0;
				}
				if(!$scope.ngModel.settings.pagination.enabled) $scope.advancedTableGrid.api.paginationSetPageSize($scope.totalRows);
				if($scope.ngModel.settings.pagination.enabled && $scope.ngModel.settings.pagination.frontEnd && $scope.ngModel.settings.pagination.itemsNumber) $scope.advancedTableGrid.api.paginationSetPageSize($scope.ngModel.settings.pagination.itemsNumber);
				resizeColumns();
				$scope.hideWidgetSpinner();
			}
			
			if(nature == 'init'){
				$timeout(function(){
					$scope.widgetIsInit=true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				},500);
			}
		}
		
		$scope.getOptions = function(){
			var obj = {};
				obj["page"] = $scope.ngModel.settings.page ? $scope.ngModel.settings.page - 1 : 0;
				obj["itemPerPage"] = ($scope.ngModel.settings.pagination && $scope.ngModel.settings.pagination.enabled && !$scope.ngModel.settings.pagination.frontEnd) ? $scope.ngModel.settings.pagination.itemsNumber : -1;
				obj["type"] = 'table';
			return obj;
		}
		
		$scope.advancedTableGrid = {
				angularCompileRows: true,
				onGridSizeChanged: resizeColumns,
				onGridReady: resizeColumns,
				onSortChanged: changeSorting,
				enableSorting: true,
				pagination : true,
				onCellClicked: onCellClicked,
				defaultColDef: {
					resizable: cockpitModule_properties.EDIT_MODE,
				},
				onColumnResized: columnResized,
				getRowHeight: function(params){
					if(_rowHeight > 0) return parseInt(_rowHeight);
					else return 28;
				},
				getRowStyle: function(params) {
					if($scope.ngModel.settings.alternateRows && $scope.ngModel.settings.alternateRows.enabled){
					    if($scope.ngModel.settings.alternateRows.oddRowsColor && params.node.rowIndex % 2 === 0) {
					        return { background: $scope.ngModel.settings.alternateRows.oddRowsColor }
					    }
					    if($scope.ngModel.settings.alternateRows.evenRowsColor && params.node.rowIndex % 2 != 0){
					    	return { background: $scope.ngModel.settings.alternateRows.evenRowsColor }
					    }
					}
				}
		}
		function getRowHeight(params) {
			if(_rowHeight > 0) return _rowHeight;
			else return 28;
		}
		function changeSorting(){
			if($scope.ngModel.settings.pagination && $scope.ngModel.settings.pagination.enabled && !$scope.ngModel.settings.pagination.frontEnd){
				$scope.showWidgetSpinner()
				var sorting = $scope.advancedTableGrid.api.getSortModel();
				$scope.ngModel.settings.sortingColumn = sorting.length>0 ? getColumnName(sorting[0].colId) : '';
				$scope.ngModel.settings.sortingOrder = sorting.length>0 ? sorting[0]['sort'].toUpperCase() : '';
				$scope.refreshWidget(null, 'sorting');
			}
		}
		function columnResized(params){
			if(params.source != "sizeColumnsToFit"){
				if(params.finished){
					for(var c in $scope.ngModel.content.columnSelectedOfDataset){
						if($scope.ngModel.content.columnSelectedOfDataset[c].name == params.columns[0].colDef.headerName){
							if($scope.ngModel.content.columnSelectedOfDataset[c].style) $scope.ngModel.content.columnSelectedOfDataset[c].style.width = params.columns[0].actualWidth;
							else $scope.ngModel.content.columnSelectedOfDataset[c].style = {width : params.columns[0].actualWidth};	
							break;
						}
					}
				}
			}
		}
		
		function resizeColumns(){
			$scope.advancedTableGrid.api.sizeColumnsToFit();
		}
		
	  	$scope.maxPageNumber = function(){
			if($scope.ngModel.settings.page * $scope.ngModel.settings.pagination.itemsNumber < $scope.totalRows) return $scope.ngModel.settings.page*$scope.ngModel.settings.pagination.itemsNumber;
			else return $scope.totalRows;
	  	}
	  	
	  	$scope.disableFirst = function(){
	  		return $scope.ngModel.settings.page == 1;
	  	}
	  	
	  	$scope.disableLast = function(){
	  		return $scope.ngModel.settings.page == $scope.totalPages;
	  	}
	  	
	  	$scope.first = function(){
	  		$scope.ngModel.settings.page = 1;
	  		$scope.refreshWidget();
		}
		
	  	$scope.prev = function(){
	  		if($scope.ngModel.settings.page == 1) return;
	  		$scope.ngModel.settings.page = $scope.ngModel.settings.page - 1;
	  		$scope.refreshWidget();
		}
		
	  	$scope.next = function(){
	  		$scope.ngModel.settings.page = $scope.ngModel.settings.page + 1;
	  		$scope.refreshWidget();
		}
		
	  	$scope.last = function(){
	  		$scope.ngModel.settings.page = $scope.totalPages;
	  		$scope.refreshWidget();
		}
		
		
		function onCellClicked(node){
			if($scope.cliccable==false) return;
			if(node.rowPinned) return;
			if(!$scope.ngModel.settings.multiselectable || node.colDef.measure == "MEASURE") return;
			if($scope.ngModel.settings.multiselectable) {
				//first check to see it the column selected is the same, if not clear the past selections
				if(!$scope.bulkSelection || $scope.bulkSelection!=node.colDef.field){
					$scope.selectedCells.splice(0,$scope.selectedCells.length);
					$scope.selectedRows.splice(0,$scope.selectedRows.length);
					$scope.bulkSelection = node.colDef.field;
					$scope.$apply();
				}
				//check if the selected element exists in the selectedCells array, remove it in case.
				if(($scope.selectedCells.indexOf(node.data[node.colDef.field])==-1 && !$scope.ngModel.settings.modalSelectionColumn) || ($scope.ngModel.settings.modalSelectionColumn && $scope.selectedRows.indexOf(node.data)==-1)){
					$scope.selectedCells.push(node.data[node.colDef.field]);
					$scope.selectedRows.push(node.data);
				}else{
					$scope.selectedCells.splice($scope.selectedCells.indexOf(node.data[node.colDef.field]),1);
					$scope.selectedRows.splice($scope.selectedRows.indexOf(node.data),1);
					//if there are no selection left set bulk selection to false to avoid the selection button to show
					if($scope.selectedCells.length==0) $scope.bulkSelection=false;
				}
				$scope.advancedTableGrid.api.refreshCells({force:true});
			}else $scope.doSelection(node.column.colDef.headerName, node.value, $scope.ngModel.settings.modalSelectionColumn, null, node.data);
		}
		
		$scope.clickItem = function(e,row,column){
			$scope.advancedTableGrid.api.deselectAll();
			var newValue = undefined;
			
			function mapRow(rowData){
				var keyMap = {};
				for(var r in rowData){
					for(var f in $scope.metadata.fields){
						if(f != 0 && $scope.metadata.fields[f].dataIndex == r) keyMap[$scope.metadata.fields[f].header] = rowData[r];
					}
				}
				return keyMap;
			}
			
			column = getColumnName(column);

			var rows = [];
			newValue = [];
			var valuesArray = [];
			for(var r in row){
				rows.push(mapRow(row[r]));
			}
			for(var k in rows){
				newValue.push(rows[k][$scope.ngModel.settings.modalSelectionColumn || column]);
				valuesArray.push(rows[k][column]);
			}

			$scope.doSelection(column,valuesArray,$scope.ngModel.settings.modalSelectionColumn,newValue,rows);
			$scope.bulkSelection = false;
		}
		
		$scope.cancelBulkSelection = function(){
			$scope.bulkSelection = false;
			$scope.advancedTableGrid.api.refreshCells({force:true});
		}
		
		$scope.$watchCollection('ngModel.settings.pagination',function(newValue,oldValue){
			if(newValue != oldValue){
				$scope.showGrid = false;
				if(newValue && newValue.enabled && newValue.frontEnd){
					if(!newValue.itemsNumber) $scope.advancedTableGrid.paginationAutoPageSize = true;
					else $scope.advancedTableGrid.paginationAutoPageSize = false;
				}else if(newValue && !newValue.enabled){
					$scope.advancedTableGrid.paginationAutoPageSize = false;
					$scope.advancedTableGrid.paginationPageSize = angular.copy($scope.totalRows);
				}else {
					if(!newValue.itemsNumber) $scope.advancedTableGrid.paginationAutoPageSize = false;
					if(!newValue.itemsNumber) $scope.advancedTableGrid.paginationPageSize = 15;
				}
				$scope.showGrid = true;
				if(newValue && newValue.enabled != oldValue.enabled) $scope.refreshWidget();
			}
		})

		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: advancedTableWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/advancedTableWidget/templates/advancedTableWidgetEditPropertyTemplate.html',
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

	}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("advanced-table",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();