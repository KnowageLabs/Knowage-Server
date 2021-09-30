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

angular.module('qbe_custom_table', ['ngDraggable','exportModule','angularUtils.directives.dirPagination','formatModule'])
.directive('qbeCustomTable', function() {
    return {
        restrict: 'E',
        controller: qbeCustomTable,
        scope: {
            ngModel: '=',
        	expression: '=',
        	advancedFilters:'=',
        	distinct:'=',
            filters: '=',
            isTemporal: '='

        },
        templateUrl: currentScriptPath + 'custom-table.html',
        replace: true,
        transclude:true,
        link: function link(scope, element, attrs) {
        }
    };
})
.filter("orderedById",function(){
    return function(input,idIndex) {
        var ordered = [];
		for (var key in idIndex) {
			for(var obj in input){
				if(idIndex[key]==input[obj].id){
					ordered.push(input[obj]);
				}
			}

		}
        return ordered;
    };
})
.filter('format', function(formatter) {



	return function(item,field,row) {


		return formatter.format(item,field.format,row.dateFormatJava)

	};
});

function qbeCustomTable($scope, $rootScope, $mdDialog, sbiModule_translate, sbiModule_config, $mdPanel, query_service, $q, sbiModule_action,filters_service,expression_service,formatter){

	//$scope.smartPreview = query_service.smartView;
	$scope.query_service = query_service;
	$scope.completeResult = false;
	$scope.hideList = $scope.$parent.hideList;
	$scope.toggleEntitiesList = function(){
		$scope.hideList = !$scope.hideList;
		$scope.$parent.hideList = !$scope.$parent.hideList;
	}

	$scope.completeResultsColumns = [];

	$scope.previewModel = [];

	function CustomPinnedRowRenderer() {}
	CustomPinnedRowRenderer.prototype.init = function(params) {
		var fieldName = params.colDef.field;
		this.data = params.data[fieldName];

		this.eGui = document.createElement('div');
		this.eGui.classList.add("customFooter");

		var filterClass = '';
		if(params.value && params.value.filters && params.value.filters.length > 0){
			filterClass = 'filter-color';
		}
		this.eGui.innerHTML = '<md-icon class="fa fa-info info-button" title="'+$scope.translate.load('kn.qbe.custom.table.info')+'"></md-icon>'+
			'<md-icon class="fa fa-filter filter-button '+filterClass+'" title="'+$scope.translate.load('kn.qbe.custom.table.filters')+'"></md-icon>';

		var onInfoButtonClick   = this.infoColumn.bind(this);
		var onFilterButtonClick = this.filterColumn.bind(this);

		var infoIconEl   = this.eGui.querySelector(".info-button");
		infoIconEl.addEventListener("click", onInfoButtonClick);

		var filterIconEl = this.eGui.querySelector(".filter-button");
		filterIconEl.addEventListener("click", onFilterButtonClick);
	};
	CustomPinnedRowRenderer.prototype.getGui = function() {
		return this.eGui;
	};
	CustomPinnedRowRenderer.prototype.infoColumn = function() {
		$scope.showFilters(this.data);
	};
	CustomPinnedRowRenderer.prototype.filterColumn = function() {
		$scope.openFilters(this.data);
	};

	$scope.qbeTableGrid = {
		angularCompileRows: true,
		pagination : false,
		domLayout:'autoHeight',
		rowHeight: 20,
		defaultColDef: {
			resizable: true,
			pinnedRowCellRenderer: CustomPinnedRowRenderer
		},
		components: {
			agColumnHeader: CustomHeader
		},
		onDragStopped: function(data) {

			var currColsOrder = $scope.qbeTableGrid
				.columnApi
				.getAllGridColumns()
				.map(function(e) {
					return e.colDef.properties.id;
				});

			$rootScope.$broadcast("setOrder", currColsOrder);

		}
	}

	//VALUE FORMATTERS
	function dateTimeFormatter(params){
		return isNaN(moment(params.value,'DD/MM/YYYY HH:mm:ss.SSS'))? params.value : moment(params.value,'DD/MM/YYYY HH:mm:ss.SSS').locale(sbiModule_config.curr_language).format(params.colDef.properties.format || 'LLL');
	}

	function numberFormatter(params){
		switch(params.colDef.properties.format) {
		  case '#,###':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 0 }).format(params.value)
		  case '#,###.0':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 1 }).format(params.value)
		  case '#,###.00':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 2 }).format(params.value)
		  case '#,###.000':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 3 }).format(params.value)
		  case '#,###.0000':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 4 }).format(params.value)
		  case '#,###.00000':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 5 }).format(params.value)
		  case '€#.##0.00':
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 2 , style: 'currency', currency: 'EUR' }).format(params.value)
		  case "â¬#,##0.00":
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 2 , style: 'currency', currency: 'EUR' }).format(params.value)
		  case '$#,##0.00':
			  return new Intl.NumberFormat('en-US', { minimumFractionDigits: 2 , style: 'currency', currency: 'USD' }).format(params.value)
		  default:
			  return new Intl.NumberFormat(sbiModule_config.curr_language + '-' + sbiModule_config.curr_country, { minimumFractionDigits: 0 }).format(params.value)
		}
	}


	//CUSTOM RENDERERS
	function CustomHeader() {}
	CustomHeader.prototype.init = function(params) {

		this.params = params;
		this.properties = params.column.colDef.properties;

		this.eGui = document.createElement("div");
		this.eGui.classList.add("customHeader");
		this.eGui.innerHTML = '<div class="qbeCustomTopColor" style="background-color: ' + this.properties.color + '" title="'+ this.properties.entity +'"></div>' +
			'<div class="qbeHeaderContainer"><md-icon class="fa fa-sort sort-button" title="'+$scope.translate.load('kn.qbe.custom.table.sorting')+'"></md-icon>' +
			'<span class="flex truncated" title="'+params.displayName+'">' + params.displayName + '</span>' +
			'<md-icon class="fa fa-cog settings-button" title="'+$scope.translate.load('kn.qbe.custom.table.column.settings')+'"></md-icon>'+
			'<md-icon class="fa fa-times remove-button" title="'+$scope.translate.load('kn.qbe.custom.table.delete.column')+'"></md-icon></div>';

		this.onRemoveButtonClick = this.removeColumn.bind(this);
		this.onSortButtonClick = this.sortColumn.bind(this);
		this.onSettingsButtonClick = this.openColumnSettings.bind(this);

		this.removeButton = this.eGui.querySelector(".remove-button");
		this.removeButton.addEventListener("click", this.onRemoveButtonClick);

		this.sortButton = this.eGui.querySelector(".sort-button");
		this.sortButton.addEventListener("click", this.onSortButtonClick);

		this.settingsButton = this.eGui.querySelector(".settings-button");
		this.settingsButton.addEventListener("click", this.onSettingsButtonClick);


	}
	CustomHeader.prototype.getGui = function() {
		return this.eGui;
	}
	CustomHeader.prototype.removeColumn = function() {
		$scope.removeColumns([{"id" : this.properties.id,"entity" : this.properties.entity }]);
	}
	CustomHeader.prototype.sortColumn = function() {
		var realColumn = $scope.getColumnById(this.properties.id)
		$scope.toggleOrder(realColumn);
		$scope.$apply();
	}
	CustomHeader.prototype.openColumnSettings = function(event){
		for(var k in $scope.ngModel){
			if($scope.ngModel[k].id == this.properties.id) $scope.field = $scope.ngModel[k];
		}
		document.getElementById('ag-popup-child').style.left = event.clientX;
		document.getElementById('ag-popup-child').style.top = event.clientY;
		$scope.$apply();
		togglePopupVisibility();
	}

	function togglePopupVisibility(){
		var agPopUpEl = document.getElementById('ag-popup');
		var agPopUpCloseAreaEl = document.getElementById('ag-popup-close-area');

		agPopUpEl.style.display = agPopUpEl.style.display == 'block' ? 'none' : 'block';
		agPopUpCloseAreaEl.style.display = agPopUpEl.style.display;
	}

	function isTemporal(el){
		var type = el.dataType;
		var allowedTypes = ['oracle.sql.TIMESTAMP','java.sql.Timestamp','java.util.Date','java.sql.Date','java.sql.Time'];
		return allowedTypes.indexOf(type) > -1 || (el.type == "inline.calculated.field" && el.id.type == "DATE");
	}

	function isNumeric(type){
		if(["java.lang.Byte","java.lang.Long","java.lang.Short","java.lang.Integer","java.math.BigInteger","java.lang.Double","java.lang.Float","java.math.BigDecimal","java.math.Decimal" ].indexOf(type) > -1) return true;
		else return false;
	}

	function getAgGridColumns() {
		return $scope.ngModel
			.filter(function(el) {
				/*
				 * Skip invisible columns.
				 *
				 * See pinned row data function.
				 */
				return el.visible;
			})
			.filter(function(el) {
				/*
				 * Skip columns not in use.
				 */
				return el.hasOwnProperty("inUse") && typeof el.inUse != "undefined" ? el.inUse : true;
			})
			.map(function(el) {
				var tempObj = {"field": el.key,
					"tooltipField": el.key,
					"headerName":   el.alias,
					"hide": !el.visible,
					"properties": {
						"entity": el.entity,
						"id":     el.id,
						"color":  el.color
					}
				}
				if(isTemporal(el)) {
					tempObj.valueFormatter = dateTimeFormatter;
					tempObj.properties.format = el.format;
				}
				if(isNumeric(el.dataType)) {
					tempObj.valueFormatter = numberFormatter;
					tempObj.properties.format = el.format;
				}
				return tempObj;
			})
			.reduce(function(accumulator, currentValue, index) {
				/*
				 * In case of hidden cols, we have to fix the column name because a
				 * dataset always has consecutive column names.
				 */
				currentValue.field = currentValue.tooltipField = "column_" + (index + 1);
				accumulator.push(currentValue);
				return accumulator;
			}, []);
	}

	$scope.closePopup = function() {
		togglePopupVisibility();
	}

	$scope.getColumnById = function(id) {
		return $scope.ngModel
			.find(function(el) {
				return el.id == id;
			})
	}

	$scope.updateQbeTableGridColDef = function() {
		$scope.qbeTableGrid.api.setColumnDefs(getAgGridColumns());
	}

	$scope.updateQbeTableGridData = function() {
		$scope.qbeTableGrid.api.setRowData($scope.previewModel);

		var pinnedBottomRowData = $scope.ngModel
			.filter(function(el) {
				/*
				 * Skip invisible columns.
				 *
				 * See column defs function.
				 */
				return el.visible;
			})
			.filter(function(el) {
				/*
				 * Skip columns not in use.
				 */
				return el.hasOwnProperty("inUse") && typeof el.inUse != "undefined" ? el.inUse : true;
			})
			.reduce(function(accumulator, currentValue, index) {
				accumulator["column_" + (index + 1)] = currentValue;
				return accumulator;
			}, {});

		$scope.qbeTableGrid.api.setPinnedBottomRowData([pinnedBottomRowData]);
		if($scope.start + 1 > $scope.totalPages) $scope.start = $scope.totalPages - 1;
	}

	$scope.updateQbeTable = function() {
		$scope.updateQbeTableGridColDef();
		$scope.updateQbeTableGridData();
	}

	// PAGINATION METHODS
	$scope.pageChanged = function(newPageNumber){
		$rootScope.$broadcast('start',{"itemsPerPage":$scope.itemsPerPage, "currentPageNumber":newPageNumber});
	}

	$scope.maxPageNumber = function(){
		if(($scope.start + 1) * $scope.itemsPerPage < $scope.totalPages) return ($scope.start + 1) * $scope.itemsPerPage;
		else return $scope.results;
  	}

  	$scope.disableFirst = function(){
  		return $scope.start == 0;
  	}

  	$scope.disableLast = function(){
  		return ($scope.start + 1) == $scope.totalPages;
  	}

  	$scope.first = function(){
  		$scope.pageChanged(1);
	}

  	$scope.prev = function(){
  		$scope.pageChanged($scope.start);
	}

  	$scope.next = function(){
  		$scope.pageChanged($scope.start + 2);
	}

  	$scope.last = function(){
  		$scope.pageChanged($scope.totalPages);
	}
	$scope.start = 0;
	$scope.itemsPerPage = 25;

	$scope.firstExecution = true;

	$scope.$watch('query_service.smartView',function(newValue,oldValue){
		if(newValue !==oldValue){
			//query_service.setSmartView(newValue);
			$rootScope.$emit('smartView', $scope.ngModel);
		}

	},true)

	$scope.translate = sbiModule_translate;

	$scope.selectedVisualization = 'previewData';

	$scope.orderAsc = true;

	$scope.openMenu = function($mdOpenMenu, ev) {
		originatorEv = ev;
		$mdOpenMenu(ev);
	};

	$scope.setDistinct = function(distinct) {
		$scope.distinct = distinct;
	}

	$scope.aggFunctions = [ "NONE", "SUM", "MIN", "MAX", "AVG", "COUNT", "COUNT_DISTINCT" ];
	$scope.tmpFunctions = ["YTD", "LAST_YEAR", "PARALLEL_YEAR", "MTD", "LAST_MONTH"];

	$scope.deleteAllFilters = function(){
		for(var i = 0;i< $scope.filters.length;i++){
			filters_service.deleteFilter($scope.filters,$scope.filters[i],$scope.expression,$scope.advancedFilters);
			i--;
		}

		expression_service.generateExpressions($scope.filters,$scope.expression,$scope.advancedFilters)
	}

	$scope.deleteColumn = function(column){
		$scope.ngModel.splice($scope.ngModel.indexOf(column),1);
	}

	$scope.moveRight = function(currentOrder, column) {

		var newOrder = currentOrder + 1;
		var index = $scope.ngModel.indexOf(column);
		var indexOfNext = index + 1;

		if(index!=undefined && indexOfNext!=-1 && newOrder <= $scope.ngModel.length){
			$scope.ngModel[index] = $scope.ngModel[indexOfNext];
			$scope.ngModel[index].order = currentOrder;

			$scope.ngModel[indexOfNext] = column;
			$scope.ngModel[indexOfNext].order = newOrder;
		}

		$rootScope.$broadcast('move', {index:index,direction:+1});

	};

	$scope.moveLeft = function(currentOrder, column) {

		var newOrder = currentOrder - 1;
		var index = $scope.ngModel.indexOf(column);
		var indexOfBefore = index - 1;

		if(index!=undefined && indexOfBefore!=undefined && indexOfBefore!=-1){

			$scope.ngModel[index] = $scope.ngModel[indexOfBefore];
			$scope.ngModel[index].order = currentOrder;

			$scope.ngModel[indexOfBefore] = column;
			$scope.ngModel[indexOfBefore].order = newOrder;
		}

		$rootScope.$broadcast('move', {index:index,direction:-1});

	};

	$scope.changeAlias = function(field){
		$mdDialog.show({
            controller: function ($scope, $mdDialog) {

            	$scope.alias = field.alias;

                $scope.ok= function(){

                	field.index = $scope.ngModel.indexOf(field);
                	field.alias = $scope.alias;
                    $mdDialog.hide();
                }

                $scope.cancel = function(){
                	$mdDialog.hide();
                }
            },
            scope: $scope,
            locals :{field:field},
            preserveScope:true,
            templateUrl:  sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/alias.html',

            clickOutsideToClose:true
        })

	}

	$scope.applyFuntion = function(funct, id, entity) {
		$rootScope.$emit('applyFunction', {
			"funct" : funct,
			"fieldId" : id,
			"entity" : entity
		});
	};

	$scope.addTemporalParameter = function (field) {
		$rootScope.$broadcast('addTemporalParameter', field);
	}

	$scope.groupChanged = function(field) {
		if(field.group){
			field.funct = "NONE";
		}
	};
	$scope.modifyCalculatedField = function (row) {
		$rootScope.$broadcast('showCalculatedField',row);
	}

	$scope.removeColumns = function(fields,standardTable) {
		var toRemove = [];
		for(var i in fields){
			toRemove.push({"id" : fields[i].id,"entity" : fields[i].entity
			})
		}
		if(!standardTable) $scope.updateQbeTable();
		$rootScope.$emit('removeColumns', toRemove);
	};

	$scope.toggleOrder = function (data) {

		 switch(data.ordering) {
		 	case "NONE":
		    	data.ordering = "ASC";
		        break;
		    case "ASC":
		    	data.ordering = "DESC";
		        break;
		    case "DESC":
		    	data.ordering = "ASC";
		        break;
		    default:
		    	data.ordering = "NONE";
		}

	}

	$scope.openFiltersAdvanced = function (){
		$rootScope.$broadcast('openFiltersAdvanced', true);
	}

	$scope.executeRequest = function () {
		$scope.firstExecution = true;
		$rootScope.$broadcast('executeQuery', {"start":$scope.start, "itemsPerPage":$scope.itemsPerPage/*, "fields": $scope.ngModel*/});
	}

	$scope.$on('queryExecuted', function (event, data) {
		$scope.completeResult = true;
		angular.copy(data.columns, $scope.completeResultsColumns);
		angular.copy(data.data, $scope.previewModel);
		$scope.results = data.results;
		$scope.totalPages = Math.ceil($scope.results / $scope.itemsPerPage) || 0;
		if($scope.firstExecution&& !query_service.smartView){
			$scope.openPreviewTemplate(true, $scope.completeResultsColumns, $scope.previewModel, data.results);
			$scope.firstExecution = false;
		}
		$scope.updateQbeTable();
	});

	$scope.$on('start', function (event, data) {
		var start = 0;
		if(data.currentPageNumber>1){
			start = (data.currentPageNumber - 1) * data.itemsPerPage;
		}
		$scope.start = data.currentPageNumber == 0 ? 0 : (data.currentPageNumber - 1);
		//$scope.currentPage = (data.currentPageNumber - 1);
		$rootScope.$broadcast('executeQuery', {"start":start, "itemsPerPage":data.itemsPerPage});
	});

	$scope.openPreviewTemplate = function (completeResult,completeResultsColumns,previewModel,totalNumberOfItems){

		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/datasetPreviewDialogTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center().top("50%"),
				panelClass :"layout-row",
				fullscreen :true,
				controller: function($scope,mdPanelRef,sbiModule_translate,$mdDateLocale){

					var gridOptions = {
					        enableColResize: true,
					        enableSorting: false,
						    enableFilter: false,
						    pagination: false,
						    resizable: true,
						    onGridSizeChanged: resizeColumns,
						    suppressDragLeaveHidesColumns : true,
					        columnDefs :getColumns(completeResultsColumns),
					    	rowData: previewModel
						};



					    function getColumns(fields) {
							var columns = [];
							for(var f in fields){
								if(typeof fields[f] != 'object') continue;
								var tempCol = {
										"headerName":fields[f].label,
										"field":fields[f].name,
										"tooltipField":fields[f].name,
										"dataType":fields[f].dataType,
										"format":fields[f].format,
										"dateFormatJava":fields[f].dateFormatJava,
										"valueFormatter":function(params){
											return formatter.format(params.value,params.colDef.format,params.colDef.dateFormatJava)
											},
										};

								columns.push(tempCol);
							}
							return columns;
						}

					    function resizeColumns(){
							gridOptions.api.sizeColumnsToFit();
						}
					$scope.model ={"gridOptions":gridOptions, "completeresult": completeResult, "completeResultsColumns": completeResultsColumns, "previewModel": previewModel, "totalNumberOfItems": totalNumberOfItems, "mdPanelRef":mdPanelRef};
					$scope.$watch('model.previewModel',function(newValue,oldValue){
						console.log(newValue)
						gridOptions.api.setRowData(newValue);
						$scope.totalPages = Math.ceil($scope.model.totalNumberOfItems / $scope.itemsPerPage);
					},true)
					$scope.itemsPerPage = 20;

					$scope.currentPageNumber = 0;
					$scope.maxPageNumber = function(){
						if(($scope.currentPageNumber + 1) * $scope.itemsPerPage < $scope.model.totalNumberOfItems) return ($scope.currentPageNumber + 1) * $scope.itemsPerPage;
						else return $scope.model.totalNumberOfItems;
					}

				  	$scope.disableFirst = function(){
				  		return $scope.currentPageNumber == 0;
				  	}

				  	$scope.disableLast = function(){
				  		return ($scope.currentPageNumber + 1) == $scope.totalPages;
				  	}

				  	$scope.first = function(){
				  		$scope.currentPageNumber = 0;
				  		$scope.changeDatasetPage($scope.itemsPerPage,1);
					}

				  	$scope.prev = function(){
				  		$scope.currentPageNumber--;
				  		$scope.changeDatasetPage($scope.itemsPerPage,$scope.currentPageNumber);
					}

				  	$scope.next = function(){
				  		$scope.currentPageNumber++;
				  		$scope.changeDatasetPage($scope.itemsPerPage,$scope.currentPageNumber + 2);
					}

				  	$scope.last = function(){
				  		$scope.currentPageNumber = $scope.totalPages -1;
				  		$scope.changeDatasetPage($scope.itemsPerPage,$scope.totalPages -1);
					}
					$scope.changeDatasetPage=function(itemsPerPage,currentPageNumber){

							$rootScope.$broadcast('start',{"itemsPerPage":itemsPerPage, "currentPageNumber":currentPageNumber});

					}

					$scope.closePanel = function () {

						angular.copy(null,$scope.changeDatasetPage)
						mdPanelRef.close();
						mdPanelRef.destroy();
					}

					$scope.$on("$destroy",function(){
						mdPanelRef.close();
						mdPanelRef.destroy();
						})
					$scope.translate = sbiModule_translate;
				},
				locals: {completeresult: completeResult, completeResultsColumns: completeResultsColumns, previewModel: previewModel, totalNumberOfItems: totalNumberOfItems},
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;

	}

	$scope.openHavings = function(field) {
		$rootScope.$broadcast('openHavings', field);
	}

	$scope.openFilters = function (field){
		$rootScope.$broadcast('openFilters', {"field":field});
	}

	$scope.checkDescription = function (field){
		var desc = 0;

		for (var i = 0; i < $scope.filters.length; i++) {
			if($scope.filters[i].leftOperandDescription == field.entity+" : "+field.name){
				field.filters.push($scope.filters[i]);
				desc++;
			}
		}
		if(desc == 0) {
			return "No filters";
		} else {
			return desc + " filters";
		}
	}

    $scope.countFilters = function (field) {
    	var filt = 0;
    	var hav = 0;
    	for (var i = 0; i < field.filters.length; i++) {
			if(field.filters[i].leftOperandDescription == field.entity+" : "+field.name){
				filt++;
			}
		}

    	for (var i = 0; i < field.havings.length; i++) {
			if(field.havings[i].leftOperandDescription == field.entity+" : "+field.name){
				hav++;
			}
		}

    	var total = filt + hav;
    	if(total == 0) {
    		return "";
    	} else {
    		return total + " filter/s";
    	}
    }

	$scope.openDialogForParams = function (model){
		$rootScope.$broadcast('openDialogForParams');
	}

	$scope.openDialogJoinDefinitions = function (model){
		$rootScope.$broadcast('openDialogJoinDefinitions');
	}

	$scope.$watch('ngModel',function(newValue,oldValue){
		if(newValue[0]){
			$scope.isChecked = newValue[0].distinct;
		}
	},true);

	$scope.hiddenColumns = function() {
		for ( var field in $scope.ngModel) {
			if(!$scope.ngModel[field].visible) return true;
		}
		return false;
	}

	$scope.showHiddenColumns = function () {
		for ( var field in $scope.ngModel) {
			$scope.ngModel[field].visible = true;
		}
	}

	$scope.idIndex = Array.apply(null, {length: 25}).map(Number.call, Number);


	$scope.basicViewColumns = [
								{
	                            	"label":$scope.translate.load("kn.qbe.custom.table.entity"),
	                            	"name":"entity",
	                            	"size": "10%"
	                        	},
	                        	{
	                        		"label":$scope.translate.load("kn.qbe.general.field"),
	                            	"name":"name",
	                            	"size": "33%"
	                        	},{
	                        		"label":$scope.translate.load("kn.qbe.general.alias"),
	                            	"name":"alias",
	                            	transformer: function() {
	                   	    		 return '<md-input-container class="md-block" style="margin:0"><input  ng-model="row.alias" ng-click="scopeFunctions.setAlias(row)"></md-input-container>';
	                   	    	 	},
	                            	"size": "16%"
	                        	},
	                        	{
	                        		"label":$scope.translate.load("kn.qbe.custom.table.group"),
	                            	"name":"group",
	                            	transformer: function() {
	                            		return '<md-checkbox  ng-model="row.group"  ng-change="scopeFunctions.groupChanged(row)" aria-label="Checkbox"></md-checkbox>';
	                            	},
	                            	"size": "8%"
	                        	},
	                        	{
	                        		"label":$scope.translate.load("kn.qbe.selectgridpanel.headers.order"),
	                            	"name":"ordering",
	                            	transformer: function() {
	                            		return '<md-select  ng-model=row.ordering class="noMargin" ><md-option ng-repeat="col in scopeFunctions.orderingValues" value="{{col}}">{{col}}</md-option></md-select>';
	                            	},
	                            	"size": "9%"
	                        	},
	                        	{
	                        		"label":$scope.translate.load("kn.qbe.custom.table.aggregation.short"),
	                        		"tooltip":$scope.translate.load("kn.qbe.custom.table.aggregation"),
	                            	"name":"function",
	                            	transformer: function() {
	                            		return '<md-select ng-disabled="row.group" ng-model=row.funct class="noMargin" ><md-option ng-repeat="col in scopeFunctions.filterAggreagtionFunctions(row)" value="{{col}}">{{col}}</md-option></md-select>';
	                            	},
	                            	"size": "9%"
	                        	},
	                        	{
	                        		"label":$scope.translate.load("kn.qbe.custom.table.show.field.short"),
	                        		"tooltip":$scope.translate.load("kn.qbe.custom.table.show.field"),
	                            	"name":"visible",
	                            	transformer: function() {
	                            		return '<md-checkbox  ng-model="row.visible"  aria-label="Checkbox"></md-checkbox>';
	                            	},
	                            	"size": "9%"
	                        	},
	                        	{
	                        		"label":$scope.translate.load("kn.qbe.custom.table.inUse"),
	                            	"name":"inUse",
	                            	transformer: function() {
	                            		return '<md-checkbox  ng-model="row.inUse" aria-label="Checkbox"></md-checkbox>';
	                            	},
	                            	"size": "9%"
	                        	}
	]

	$scope.$watch('isTemporal',function(newValue,oldValue){
		if($scope.isTemporal){
			$scope.basicViewColumns.splice(5, 0, {
	    		"label":$scope.translate.load("kn.qbe.custom.table.function.temporal"),
	        	"name":"temporalOperand",
	        	hideTooltip:true,
	        	transformer: function() {
	        		return '<md-select ng-show="row.iconCls==measure" ng-model=row.temporalOperand class="noMargin" ><md-option ng-repeat="col in scopeFunctions.temporalFunctions" value="{{col}}">{{col}}</md-option></md-select>';
	        	}
	    	});
		}
	},true)

	$scope.treeSpeedMenu= [

        {
            label:sbiModule_translate.load("kn.qbe.custom.table.move.up"),
            icon:'fa fa-angle-up',
            color:'#a3a5a6',
            action:function(row,event){

            	$scope.basicViewScopeFunctions.moveUp(row)
            }
         },
         {
             label:sbiModule_translate.load("kn.qbe.custom.table.move.down"),
             icon:'fa fa-angle-down',
             color:'#a3a5a6',
             action:function(row,event){

            	 $scope.basicViewScopeFunctions.moveDown(row)
             }
          }
          ,
          {
      		"label":$scope.translate.load("kn.qbe.general.filters"),
      		icon:'fa fa-filter',
      		color:'#a3a5a6',
      		action:function(row,event){

      			$scope.basicViewScopeFunctions.openFilters(row);
            }
      	},
		{
			"label":$scope.translate.load("kn.qbe.general.havings"),
			icon:'fa fa-check-square',
			color:'#a3a5a6',
			action:function(row,event){

				$scope.basicViewScopeFunctions.openHavings(row);
			},
			visible: function (item) {
				return (item.funct && item.funct != '' && item.funct != "NONE" ? true : false)
					|| (item.type == "inline.calculated.field" && item.fieldType == "measure");
			}

		},
      	{
    		"label": sbiModule_translate.load("kn.qbe.custom.table.modified.field"),
    		"icon": "fa fa-calculator",
    		"visible": function (item){
    			if(item.id.alias && item.id.expressionSimple) return true;
    			else return false
    		},
    		"action": function(item, event) {
    			$scope.basicViewScopeFunctions.modifyCalculatedField(item);
    		}
    	},
        {
            label:sbiModule_translate.load("kn.qbe.custom.table.delete.field"),
            icon:'fa fa-trash',
            color:'#a3a5a6',
            action:function(row,event){

           	 $scope.basicViewScopeFunctions.deleteField(row)
            }
         }
       ];

	$scope.basicViewScopeFunctions = {
		filterAggreagtionFunctions: function (row){

			if(row.fieldType=='attribute') return [ "NONE", "MIN", "MAX", "COUNT", "COUNT_DISTINCT" ];
			else return [ "NONE", "SUM", "MIN", "MAX", "AVG", "COUNT", "COUNT_DISTINCT" ];
		},
		aggregationFunctions: $scope.aggFunctions,
		orderingValues: ["NONE", "ASC", "DESC"],
		temporalFunctions: $scope.tmpFunctions,
		deleteField : function (row) {
			$scope.removeColumns([row],true);
		},
		moveUp : function (row) {
			$scope.moveLeft(row.order, row);

		},
		moveDown : function (row) {
			$scope.moveRight(row.order, row);

		},
		openFilters : function (row) {
			$scope.openFilters(row);
		},
		openHavings : function (row) {
			$scope.openHavings(row);
		},
		isGrouped : function (row){
			for (var i = 0; i < $scope.ngModel.length; i++) {
				if($scope.ngModel[i].id==row.id && $scope.ngModel[i].group==true){
					return true;
				}
			}
		},
		groupChanged: function(row){
			$scope.groupChanged(row)
		},
		modifyCalculatedField : function (row){
			$scope.modifyCalculatedField(row);
		},
		setAlias : function (row){
			$scope.changeAlias(row);
		}
	};

	$scope.filtersListColumns = [
    	{"label": $scope.translate.load("kn.qbe.filters.condition"), "name": "operator"},
    	{"label": $scope.translate.load("kn.qbe.filters.target"), "name": "rightOperandDescription"}
    ]

	$scope.allFilters = [];

	$scope.showFilters = function(field) {

		$scope.allFilters = [];
		$scope.field = field;
		for (var i = 0; i < field.filters.length; i++) {
			$scope.filterObject = {
					"operator": field.filters[i].operator,
					"rightOperandDescription": field.filters[i].rightOperandDescription
			}

				$scope.allFilters.push($scope.filterObject);

		}

		for (var i = 0; i < field.havings.length; i++) {
			$scope.havingObject = {
				"operator": field.havings[i].operator,
				"rightOperandDescription": field.havings[i].rightOperandAggregator + "(" + field.havings[i].rightOperandDescription + ")"
			}
 			$scope.allFilters.push($scope.havingObject);
 		}

        if($scope.allFilters.length > 0) {
	    	$mdDialog.show({
	            controller: function ($scope, $mdDialog) {

	                $scope.ok= function(){
	                	console.log($scope)
	                    $mdDialog.hide();
	                }
	            },
	            scope: $scope,
	            preserveScope:true,
	            templateUrl:  sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/filtersInfo.html',

	            clickOutsideToClose:true
	        })
        } else {
        	$mdDialog.show(
        		$mdDialog.alert()
        		     .clickOutsideToClose(true)
        		     .title(field.entity + " : " + field.name)
        		     .textContent($scope.translate.load("kn.qbe.alert.nofilters"))
        		     .ok($scope.translate.load("kn.qbe.general.ok"))
            );
        }
	};

	$scope.showSQLQuery = function () {
		$rootScope.$broadcast('showSQLQuery', true);
	};

	$scope.showCalculatedField = function () {
		$rootScope.$broadcast('showCalculatedField');
	}

}
})();