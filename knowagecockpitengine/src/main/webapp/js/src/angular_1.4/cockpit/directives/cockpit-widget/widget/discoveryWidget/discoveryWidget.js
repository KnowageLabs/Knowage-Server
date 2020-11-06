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
								//scope.showWidgetSpinner();
								scope.initWidget();
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
			$mdDialog,
			$q,
			$filter,
			sbiModule_config,
			sbiModule_translate,
			cockpitModule_analyticalDrivers,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetSelection,
			cockpitModule_generalServices,
			cockpitModule_generalOptions,
			cockpitModule_properties,
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
					"selection" : true,
					"enabled" : true
				},
				"textEnabled" : true
			};
		}else $scope.ngModel.settings.page = 1;

		$scope.getOptions = function(){
			var obj = {};
				obj["page"] = $scope.ngModel.settings.page ? $scope.ngModel.settings.page - 1 : 0;
				obj["itemPerPage"] = $scope.ngModel.settings.pagination ? $scope.ngModel.settings.pagination.itemsNumber : -1;
				obj["type"] = $scope.ngModel.type;
			return obj;
		}

		if(!$scope.ngModel.search){
			$scope.ngModel.search = {};
		}

		function setDefaultTextSearch() {
		if($scope.ngModel.settings && $scope.ngModel.settings.defaultTextSearch){
			if($scope.ngModel.settings.defaultTextSearchType == 'static') $scope.ngModel.search.text = $scope.ngModel.settings.defaultTextSearchValue;
			if($scope.ngModel.settings.defaultTextSearchType == 'driver') $scope.ngModel.search.text = cockpitModule_analyticalDrivers[$scope.ngModel.settings.defaultTextSearchValue];
			if(!$scope.ngModel.search.columns){
				for(var c in $scope.ngModel.content.columnSelectedOfDataset){

					if($scope.ngModel.content.columnSelectedOfDataset[c].fullTextSearch){
						$scope.ngModel.search.columns.push($scope.ngModel.content.columnSelectedOfDataset[c].name);
					}
				}
			}
		}
		}
		
		setDefaultTextSearch();
		
		$scope.facets = [];

		$scope.gridOptions = {
			angularCompileRows: true,
            enableColResize: true,
            enableSorting: true,
            onGridReady: resizeColumns,
            onGridSizeChanged: resizeColumns,
            onSortChanged: changeSorting,
            onCellClicked: handleClick,
            onColumnResized: columnResized,
            getRowHeight: getRowHeight
		};

		function getRowHeight(params){
			if($scope.ngModel.style.tr && $scope.ngModel.style.tr.height) return parseInt($scope.ngModel.style.tr && $scope.ngModel.style.tr.height) || 25;
			else{
				var maxLength = 0;
				for(var r in params.data){
					if(params.data[r].length > maxLength) maxLength = params.data[r].length;
				}
		        return !params.node.rowPinned ? 28 * Math.min((Math.floor(maxLength / 80) + 1),3) : 28;
			}
		}

		function columnResized(params){
			if(params.source != "sizeColumnsToFit"){
				if(params.finished){
					for(var c in $scope.ngModel.content.columnSelectedOfDataset){
						if($scope.ngModel.content.columnSelectedOfDataset[c].alias == params.column.colDef.headerName){
							if($scope.ngModel.content.columnSelectedOfDataset[c].style) $scope.ngModel.content.columnSelectedOfDataset[c].style.width = params.column.actualWidth;
							else $scope.ngModel.content.columnSelectedOfDataset[c].style = {width : params.column.actualWidth};
							break;
						}
					}
				}
			}
		}

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

		function mapRow(rowData){
			var keyMap = {};
			for(var r in rowData){
				for(var f in $scope.metaData.fields){
					if(f != 0 && $scope.metaData.fields[f].dataIndex == r) keyMap[$scope.metaData.fields[f].header] = rowData[r];
				}
			}
			return keyMap;
		}

		function handleClick(node){
			if(node.colDef.paramType == 'text'){
				$mdDialog.show({
					template: 	'<md-dialog class="textContainerDialog">'+
									'<md-dialog-content>'+
										'<p ng-bind-html="dialogContent"></p>'+
									'</md-dialog-content>'+
									'<md-dialog-actions>'+
										'<md-button class="md-primary md-button" ng-click="hide()">Close</button>'+
									'</md-dialog-actions>'+
								'</md-dialog>' ,
					parent : angular.element(document.body),
					clickOutsideToClose:true,
					escapeToClose: true,
					preserveScope: false,
					locals:{value:node.value},
					controller: function(scope,$mdDialog,value){
						scope.dialogContent = value;
						scope.hide = function(){
							$mdDialog.hide();
						}
					}
				});
				return;
			};
	  		$scope.doSelection(getColumnNameFromTableMetadata(node.colDef.headerName),node.value,null,null,mapRow(node.data), null);
	  	}

		function getColumnNameFromTableMetadata(colAlias){
			for(var k in $scope.ngModel.content.columnSelectedOfDataset){
				if($scope.ngModel.content.columnSelectedOfDataset[k].alias && $scope.ngModel.content.columnSelectedOfDataset[k].alias == colAlias) return $scope.ngModel.content.columnSelectedOfDataset[k].name;
			}
		}


		$scope.init = function(element,width,height){
			$scope.element = element[0];
			$scope.reinit();
		}

		$scope.reinit = function(){
			setDefaultTextSearch();
			$scope.refreshWidget(null, 'init');
		}

		$scope.orderFacets = function(facets){
			var orderedFacets = {};
			for(var k in $scope.ngModel.content.columnSelectedOfDataset){
				for(var j in facets){
					if($scope.ngModel.content.columnSelectedOfDataset[k].name == j){
						orderedFacets[j] = facets[j];
						break;
					}
				}
			}
			return orderedFacets;
		}

		$scope.setFacetAsClosed = function(facets){
			for(var k in $scope.ngModel.content.columnSelectedOfDataset){
				for(var j in facets){
					if(!$scope.isFacetGroupSelected(j)) facets[j].closed = true;
				}
			}
		}

		$scope.refresh = function(element,width,height, datasetRecords,nature) {
			$scope.showWidgetSpinner();
			if(datasetRecords){
				$scope.facets = $scope.orderFacets(datasetRecords.facets);
				$scope.metaData = datasetRecords.metaData;
				if($scope.ngModel.settings.table && $scope.ngModel.settings.table.enabled){
					$scope.gridOptions.headerHeight = !$scope.ngModel.style.th.enabled && 0;
					if(nature == 'init'){
						$scope.columns = $scope.getColumns(datasetRecords.metaData.fields);
						$scope.updateDates();
						$scope.gridOptions.api.setColumnDefs($scope.columns);
						$scope.gridOptions.api.resetRowHeights();
					}else $scope.updateDates();
					$scope.gridOptions.api.setRowData(datasetRecords.rows);
					resizeColumns();
				}
				if($scope.ngModel.settings.facets.closed) $scope.setFacetAsClosed($scope.facets);
				$scope.totalResults = datasetRecords.results;
				$scope.hideWidgetSpinner();
			}
			$scope.hideWidgetSpinner();
			if(nature == 'init'){
				$timeout(function(){
					$scope.widgetIsInit=true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				},500);
			}
		}

		$scope.getFacetAlias = function(name){
			for(var c in $scope.ngModel.content.columnSelectedOfDataset){
				if($scope.ngModel.content.columnSelectedOfDataset[c].name == name) return $scope.ngModel.content.columnSelectedOfDataset[c].alias;
			}
		}

		function dateTimeFormatter(params){
			return isNaN(moment(params.value,cockpitModule_generalOptions.defaultValues.dateTime))? params.value : moment(params.value,cockpitModule_generalOptions.defaultValues.dateTime).locale(sbiModule_config.curr_language).format(params.colDef.dateFormat || 'LLL');
		}

		$scope.setTimeFormat = function(date, format){
			return isNaN(moment(date,cockpitModule_generalOptions.defaultValues.facetDateTime))? date : moment(date,cockpitModule_generalOptions.defaultValues.facetDateTime).locale(sbiModule_config.curr_language).format(format || 'LLL');
		}

		$scope.updateDates = function (){
			for(var c in $scope.ngModel.content.columnSelectedOfDataset){
				if(cockpitModule_generalOptions.typesMap[$scope.ngModel.content.columnSelectedOfDataset[c].type].label == 'date' || cockpitModule_generalOptions.typesMap[$scope.ngModel.content.columnSelectedOfDataset[c].type].label == 'timestamp'){
					if($scope.ngModel.content.columnSelectedOfDataset[c].dateFormat) {
						if($scope.facets && $scope.facets[$scope.ngModel.content.columnSelectedOfDataset[c].name]) $scope.facets[$scope.ngModel.content.columnSelectedOfDataset[c].name].metaData.dateFormat = $scope.ngModel.content.columnSelectedOfDataset[c].dateFormat;
					}
					if($scope.facets && $scope.facets[$scope.ngModel.content.columnSelectedOfDataset[c].name]) $scope.facets[$scope.ngModel.content.columnSelectedOfDataset[c].name].metaData.type = 'date';
				}
			}
		}

		$scope.getColumns = function (fields) {
			var columns = [];
			$scope.ngModel.search.columns = [];
			for(var c in $scope.ngModel.content.columnSelectedOfDataset){

				if($scope.ngModel.content.columnSelectedOfDataset[c].fullTextSearch){
					$scope.ngModel.search.columns.push($scope.ngModel.content.columnSelectedOfDataset[c].name);
				}
				for(var f in fields){
					if(typeof fields[f] == 'object' && $scope.ngModel.content.columnSelectedOfDataset[c].name === fields[f].header){
						var tempCol = {
								"headerName":$filter('i18n')($scope.ngModel.content.columnSelectedOfDataset[c].alias),
								"field":fields[f].name, 
								"tooltipField":$filter('i18n')(fields[f].name)};
						tempCol.paramType = fields[f].type;
						if(fields[f].type == 'text') {
							tempCol.cellRenderer = textCellRenderer;
							tempCol.cellClass = 'textCell';
						}
						if(!$scope.ngModel.content.columnSelectedOfDataset[c].visible) tempCol.hide = true;
						if($scope.ngModel.content.columnSelectedOfDataset[c].style) {
							tempCol.style = $scope.ngModel.content.columnSelectedOfDataset[c].style;
							if($scope.ngModel.content.columnSelectedOfDataset[c].style.width) {
								tempCol.width = $scope.ngModel.content.columnSelectedOfDataset[c].style.width;
								tempCol.suppressSizeToFit = true;
							}
						}
						if($scope.ngModel.content.columnSelectedOfDataset[c].dateFormat) {
							tempCol.dateFormat = $scope.ngModel.content.columnSelectedOfDataset[c].dateFormat;
						}
						if(tempCol.paramType == 'date' || tempCol.paramType == 'timestamp'){
							tempCol.valueFormatter = dateTimeFormatter;
						}
						tempCol.headerComponentParams = {template: headerTemplate()};
						tempCol.cellStyle = getCellStyle;
						columns.push(tempCol);
						break;
					}
				}
			}
			return columns
		}

		function textCellRenderer () {}
		textCellRenderer.prototype.init = function(params) {
		    this.eGui = document.createElement('div');
		    this.eGui.innerHTML = params.value;
		};
		textCellRenderer.prototype.getGui = function() {
		    return this.eGui;
		};

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

	    $scope.deleteSelections = function(item){
	    	var reloadAss=false;
	    	var reloadFilt=[];

	    	if(item.aggregated){
				var key = item.ds + "." + item.columnName;

				for(var i=0; i<cockpitModule_template.configuration.aggregations.length; i++){
					if(cockpitModule_template.configuration.aggregations[i].datasets.indexOf(item.ds) !=-1){
						var selection = cockpitModule_template.configuration.aggregations[i].selection;
						if(selection){
							delete selection[key];
							reloadAss=true;
						}
					}
				}
			}else{
				if(cockpitModule_template.configuration.filters){
					if(cockpitModule_template.configuration.filters[item.ds]){
						delete cockpitModule_template.configuration.filters[item.ds][item.columnName];

						if(Object.keys(cockpitModule_template.configuration.filters[item.ds]).length==0){
							delete cockpitModule_template.configuration.filters[item.ds];
						}

						reloadFilt.push(item.ds);
					}
				}
			}

			if(reloadAss){
				cockpitModule_widgetSelection.getAssociations(true);
			}

			if(!reloadAss && reloadFilt.length!=0){
				$scope.cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(reloadFilt);
			}

			var hs=false;
			for(var i=0; i<cockpitModule_template.configuration.aggregations.length; i++){
				if(Object.keys(cockpitModule_template.configuration.aggregations[i].selection).length>0){
					hs= true;
					break;
				}
			}

			if(hs==false && Object.keys(cockpitModule_template.configuration.filters).length==0){
				cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=false;
			}
	    }

		$scope.deleteFilterSelection = function(group, value){
			var item = {};
			item.aggregated= cockpitModule_template.configuration.aggregations ? true : false;
			item.columnName=group;
			item.columnAlias=group;
			item.value = value;
			item.ds=$scope.ngModel.dataset.label;
			if(cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label]) delete cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label][group];
			if(cockpitModule_template.configuration.aggregations[0] && cockpitModule_template.configuration.aggregations[0].selection[$scope.ngModel.dataset.label+'.'+group]) delete cockpitModule_template.configuration.aggregations[0].selection[$scope.ngModel.dataset.label+'.'+group];
			if(cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label] && Object.keys(cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label]).length==0){
				delete cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label];
			}
			if(Object.keys(cockpitModule_template.configuration.filters).length==0) cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=false;
			if(cockpitModule_template.configuration.aggregations[0] && Object.keys(cockpitModule_template.configuration.aggregations[0].selection).length==0) cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=false;
			$rootScope.$broadcast('DELETE_SELECTION',item);
			$scope.deleteSelections(item);
			if(cockpitModule_template.configuration.aggregations.length == 0) $scope.refreshWidget();
		}

		$scope.selectItem = function(group, item){
			if(item.column_2==0) return;
			if($scope.dimensions && $scope.dimensions.width<600){
				$scope.toggleMenu();
			}
			if($scope.ngModel.settings.facets.selection == true){
				$scope.ngModel.search.facets = {};
				if($scope.template.configuration
						&& (cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label] && cockpitModule_template.configuration.filters[$scope.ngModel.dataset.label][group]==item.column_1)
						|| ($scope.template.configuration.aggregations &&  $scope.template.configuration.aggregations.length > 0 && $scope.template.configuration.aggregations[0].selection && $scope.template.configuration.aggregations[0].selection[$scope.ngModel.dataset.label+'.'+group] == item.column_1)){
					$scope.deleteFilterSelection(group, item.column_1);
				}else{
					$scope.doSelection(group, item.column_1, null, null, item, null, undefined, !$scope.ngModel.settings.facets.selection,'selection');
				}
			}else{
				if(!$scope.ngModel.search.facets) $scope.ngModel.search.facets = {};
				if($scope.ngModel.search.facets[group] && $scope.ngModel.search.facets[group].filterVals.length>0){
					for(var k in $scope.ngModel.search.facets[group].filterVals){
						if($scope.ngModel.search.facets[group].filterVals.indexOf(item.column_1)!=-1) {
							$scope.ngModel.search.facets[group].filterVals.splice(k,1);
							if($scope.ngModel.search.facets[group].filterVals.length==0) delete $scope.ngModel.search.facets[group];
						}else {
							$scope.ngModel.search.facets[group].filterVals.push(item.column_1);
						}
					}
				}else {
					$scope.ngModel.search.facets[group] = { filterOperator: "=", filterVals: [item.column_1]} ;
				}
				$scope.refreshWidget();
			}
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

		$scope.customFacetWidth = function(){
			if($scope.ngModel.settings.facets && $scope.ngModel.settings.facets.width) {
				return {'width':$scope.ngModel.settings.facets.width};
			}return false;
		}
		
		$scope.customFacetPrecision = function(){
			if($scope.ngModel.settings.facets && $scope.ngModel.settings.facets.precision != null) {
				return $scope.ngModel.settings.facets.precision;
			}
			return 2;
		}

		$scope.isFacetSelected = function(group,item){
			if($scope.template.configuration.filters && $scope.template.configuration.filters[$scope.ngModel.dataset.label] && $scope.template.configuration.filters[$scope.ngModel.dataset.label][group] == item.column_1) return true;
			if($scope.ngModel.search.facets && $scope.ngModel.search.facets[group] && $scope.ngModel.search.facets[group].filterVals.indexOf(item.column_1)!=-1) return true;
			if($scope.template.configuration.aggregations){
			    for(var i in $scope.template.configuration.aggregations){
			        if($scope.template.configuration.aggregations[i].selection && $scope.template.configuration.aggregations[i].selection[$scope.ngModel.dataset.label+'.'+group] == item.column_1)
			            return true;
			    }
            }
			return false;
		}

		$scope.isFacetGroupSelected = function(group){
			if($scope.template.configuration.filters && $scope.template.configuration.filters[$scope.ngModel.dataset.label] && $scope.template.configuration.filters[$scope.ngModel.dataset.label][group]) return true;
			if($scope.ngModel.search.facets && $scope.ngModel.search.facets[group]) return true;
			if($scope.template.configuration.aggregations){
			    for(var i in $scope.template.configuration.aggregations){
			        if($scope.template.configuration.aggregations[i].selection && $scope.template.configuration.aggregations[i].selection[$scope.ngModel.dataset.label+'.'+group])
			            return true;
			    }
            }
			return false;
		}

		$scope.hasFacets = function() {
			if($scope.ngModel.settings.facets.enabled || typeof $scope.ngModel.settings.facets.enabled == 'undefined'){
				for(var k in $scope.ngModel.content.columnSelectedOfDataset){
	            	if($scope.ngModel.content.columnSelectedOfDataset[k].facet) {
	            	    return true;
	            	}
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
				},1000)
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
		
	
	$scope.getDecimalPlaces = function(colName, itemValue, metadataFields){
            var decimalPlaces = 0;
            var floatColumns = $filter('filter')(metadataFields, {type: 'float'}, true);
            floatColumns.forEach(function(col){
                if(col.name == colName) {
                	decimalPlaces = $scope.customFacetPrecision();
                }
            });
            
            return decimalPlaces;
        };

	}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("discovery",{'initialDimension':{'width':15, 'height':10},'updateble':true,'cliccable':true});

})();