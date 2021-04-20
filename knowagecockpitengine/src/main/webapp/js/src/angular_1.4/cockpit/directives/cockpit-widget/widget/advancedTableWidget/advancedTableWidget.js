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
		.directive('cockpitTableWidget',function(sbiModule_config){
			return{
				templateUrl: sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/directives/cockpit-widget/widget/advancedTableWidget/templates/advancedTableWidgetTemplate.html',
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
			cockpitModule_datasetServices,
			cockpitModule_generalOptions,
			cockpitModule_generalServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_analyticalDrivers,
			cockpitModule_properties,
			cockpitModule_defaultTheme,
			knModule_aggridLabels){

		$scope.showGrid = true;
		$scope.bulkSelection = false;
		$scope.selectedCells = [];
		$scope.selectedRows = [];

		$scope.getTemplateUrl = function(template){
	  		return cockpitModule_generalServices.getTemplateUrl('advancedTableWidget',template);
	  	}

		function escapeIfString(value){
			if(typeof value != 'number') return "\""+value+"\"";
			else return value;
		}

		function crossHasType(type){
			if($scope.interaction && $scope.interaction.crossType && $scope.interaction.crossType === type) return true;
			if($scope.interaction && $scope.interaction.previewType && $scope.interaction.previewType === type) return true;
			if($scope.interaction && $scope.interaction.links){
				for(var l in $scope.interaction.links) {
					if($scope.interaction.links[l].interactionType === type) return true;
				}
			}
			return false;
		}

		function returnCrossIcon(){
			if($scope.interaction.icon) return $scope.interaction.icon;
			if($scope.interaction.links){
				for(var l in $scope.interaction.links) {
					if($scope.interaction.links[l].interactionType == 'icon') return $scope.interaction.links[l].icon;
				}
			}
		}

		var _rowHeight;
		if(!$scope.ngModel.settings){
			$scope.ngModel.settings = cockpitModule_defaultTheme.table.settings;
		}else $scope.ngModel.settings.page = 1;

		if($scope.ngModel.settings.summary && $scope.ngModel.settings.summary.enabled) {
			if(!$scope.ngModel.settings.summary.list) $scope.ngModel.settings.summary.list = [{"label":$scope.ngModel.settings.summary.title}];
		}
		if(!$scope.ngModel.style) $scope.ngModel.style = cockpitModule_defaultTheme.table.style;
		function getColumns(fields,sortedDefault) {
			var crossEnabled = $scope.ngModel.cross && $scope.ngModel.cross.cross && $scope.ngModel.cross.cross.enable;
			if($scope.ngModel.cross) {
				for(var c in $scope.ngModel.cross){
					if($scope.ngModel.cross[c].enable) {
						$scope.interaction = angular.copy($scope.ngModel.cross[c]);
						$scope.interaction.type = c;
					}
				}
			}
			var columns = [];

			$scope.columnsNameArray = [];
			var dataset = cockpitModule_datasetServices.getAvaiableDatasetById($scope.ngModel.dataset.dsId);
			var columnGroups = {};
			if($scope.ngModel.settings.indexColumn){
				columns.push({headerName:"",field:"",cellRenderer:indexCellRenderer,sortable:false,filter:false,width: 50,suppressSizeToFit:true, tooltipValueGetter: false, pinned:'left'});
			}
			for(var c in $scope.ngModel.content.columnSelectedOfDataset){
				for(var f in fields){
					var thisColumn = $scope.ngModel.content.columnSelectedOfDataset[c];
					if(typeof fields[f] == 'object' && (dataset.type == "SbiSolrDataSet" && thisColumn.name.toLowerCase() === fields[f].header || ((thisColumn.aliasToShow || thisColumn.alias).toLowerCase() === fields[f].header.toLowerCase()))  ){
						$scope.columnsNameArray.push(fields[f].name);
						var tempCol = {"headerName":$scope.ngModel.content.columnSelectedOfDataset[c].aliasToShow || $scope.ngModel.content.columnSelectedOfDataset[c].alias,
								"field":fields[f].name,"measure":$scope.ngModel.content.columnSelectedOfDataset[c].fieldType};
						tempCol.headerTooltip = $scope.ngModel.content.columnSelectedOfDataset[c].aliasToShow || $scope.ngModel.content.columnSelectedOfDataset[c].alias;
						tempCol.pinned = $scope.ngModel.content.columnSelectedOfDataset[c].pinned;

						if ($scope.ngModel.content.columnSelectedOfDataset[c].isCalculated){
							tempCol.isCalculated = $scope.ngModel.content.columnSelectedOfDataset[c].isCalculated;
						}

						if(sortedDefault && sortedDefault[0].colId == fields[f].name){
							tempCol.sort = sortedDefault[0].sort;
						}

						//ROWSPAN MANAGEMENT
						if($scope.ngModel.content.columnSelectedOfDataset[c].rowSpan){
							tempCol.rowSpanDimensions = {};
							var previousValue;
							var previousIndex;
							for(var r in $scope.tempRows){
								if(previousValue != $scope.tempRows[r][fields[f].name]){
									previousValue = $scope.tempRows[r][fields[f].name];
									previousIndex = r;
									$scope.tempRows[r].span = 1;
								}else {
									$scope.tempRows[previousIndex].span ++;
								}
							}
							tempCol.rowSpan = RowSpanCalculator;
							tempCol.cellClassRules = {
								'cell-span': function(params) {
									return $scope.tempRows[params.rowIndex].span > 1
								}
					        }
						}

						//VARIABLES MANAGEMENT
						if($scope.ngModel.content.columnSelectedOfDataset[c].variables && $scope.ngModel.content.columnSelectedOfDataset[c].variables.length>0){
							for(var k in $scope.ngModel.content.columnSelectedOfDataset[c].variables){
								var variableUsage = $scope.ngModel.content.columnSelectedOfDataset[c].variables[k];
								var variableValue = cockpitModule_properties.VARIABLES[variableUsage.variable];
								if(typeof cockpitModule_properties.VARIABLES[variableUsage.variable] == 'object' && typeof cockpitModule_properties.VARIABLES[variableUsage.variable][variableUsage.key] != 'undefined') {
									variableValue = cockpitModule_properties.VARIABLES[variableUsage.variable][variableUsage.key];
								}
								if(variableUsage.action == 'hide' && eval(escapeIfString(variableValue) + variableUsage.condition + escapeIfString(variableUsage.value))) tempCol.hide = true;
								if(variableUsage.action == 'header' && variableValue) {
									tempCol.headerName = variableValue;
									tempCol.headerTooltip = tempCol.headerName;
								}
							}
						}

						if(!$scope.ngModel.content.columnSelectedOfDataset[c].hideTooltip) {
							tempCol.tooltipValueGetter = TooltipValue;
						}
						if($scope.interaction && $scope.interaction.column == $scope.ngModel.content.columnSelectedOfDataset[c].name && crossHasType('singleColumn')) {
							tempCol.cellClass = 'cross-cell';
							delete tempCol.tooltipField;
							tempCol.tooltipValueGetter = function(params) {
								return $scope.translate.load('sbi.cockpit.table.cross.tooltip');
							};
						}
						if($scope.interaction && $scope.interaction.links && $scope.interaction.links.length > 0){
							for(var link in $scope.interaction.links){
								if($scope.interaction.links[link].interactionType == 'singleColumn' && $scope.interaction.links[link].column == $scope.ngModel.content.columnSelectedOfDataset[c].name){
									tempCol.cellClass = 'cross-cell';
									delete tempCol.tooltipField;
									tempCol.tooltipValueGetter = function(params) {
										return $scope.translate.load('sbi.cockpit.table.cross.tooltip');
									};
								}
							}
						}
						if($scope.ngModel.content.columnSelectedOfDataset[c].style) {
							tempCol.style = $scope.ngModel.content.columnSelectedOfDataset[c].style;
							if($scope.ngModel.content.columnSelectedOfDataset[c].style.hiddenColumn) tempCol.hide = true;
							if($scope.ngModel.content.columnSelectedOfDataset[c].style.hideHeader) delete tempCol.headerTooltip;
						}
						if($scope.ngModel.settings.summary && $scope.ngModel.settings.summary.enabled) {
							tempCol.pinnedRowCellRenderer = SummaryRowRenderer,
							tempCol.pinnedRowCellRendererParams = {"summaryRows":$scope.ngModel.settings.summary.list, "style": $scope.ngModel.settings.summary.style};
						}
						if($scope.ngModel.content.columnSelectedOfDataset[c].style && $scope.ngModel.content.columnSelectedOfDataset[c].style.width) {
							tempCol.width = parseInt($scope.ngModel.content.columnSelectedOfDataset[c].style.width);
							tempCol.suppressSizeToFit = true;
						}
						if($scope.ngModel.content.columnSelectedOfDataset[c].ranges) tempCol.ranges = $scope.ngModel.content.columnSelectedOfDataset[c].ranges;

						tempCol.cellStyle = $scope.ngModel.content.columnSelectedOfDataset[c].style || {};

						tempCol.fieldType = cockpitModule_generalOptions.typesMap[$scope.ngModel.content.columnSelectedOfDataset[c].type || ($scope.ngModel.content.columnSelectedOfDataset[c].fieldType == 'ATTRIBUTE'? 'java.lang.String': 'java.lang.Float')].label;
						if($scope.ngModel.content.columnSelectedOfDataset[c].dateFormat) tempCol.dateFormat = $scope.ngModel.content.columnSelectedOfDataset[c].dateFormat;
						if(tempCol.fieldType == 'date') tempCol.valueFormatter = dateFormatter;
						if(tempCol.fieldType == 'timestamp') tempCol.valueFormatter = dateTimeFormatter;
						if(tempCol.fieldType == 'float' || tempCol.fieldType == 'integer' ) {
							tempCol.valueFormatter = numberFormatter;
							// When server-side pagination is disabled
							tempCol.comparator = function (valueA, valueB, nodeA, nodeB, isInverted) {
								return valueA - valueB;
							}
						}

						if(fields[f].multiValue) {
							tempCol.cellClass = 'multiCell';
							tempCol.cellRenderer = cellMultiRenderer;
						}
						else tempCol.cellRenderer = cellRenderer;

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

						tempCol.headerName = $filter('i18n')(tempCol.headerName);
						tempCol.headerTooltip = $filter('i18n')(tempCol.headerTooltip);

						//Columns group managament
						var group = getColumnGroup($scope.ngModel.content.columnSelectedOfDataset[c]);
						if(group) {
							if(typeof columnGroups[group.id] != 'undefined') {
								columns[columnGroups[group.id]].children.push(tempCol);
							}else {
								columnGroups[group.id] = columns.length;
								columns.push({
							        headerName: group.name,
							        headerGroupComponent: CustomHeaderGroupRenderer,
							        headerParams: group,
							        children: [tempCol]
							    });
							}
						}
						else columns.push(tempCol);
						break;
					}
				}
			}
			if($scope.interaction && crossHasType('icon')){
				columns.push({headerName:"",field:(crossEnabled && $scope.ngModel.cross.cross.column) || "",
					crossIcon: returnCrossIcon(),
					cellRenderer:crossIconRenderer,"cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"center","border":"none"},
					sortable:false,filter:false,width: 50,suppressSizeToFit:true, tooltipValueGetter: false});
			}
			return columns
		}

		function getColumnName(colNum){
			for(var k in $scope.metadata.fields){
				if($scope.metadata.fields[k].dataIndex && $scope.metadata.fields[k].dataIndex == colNum) {
					for(var c in $scope.ngModel.content.columnSelectedOfDataset){
						if($scope.ngModel.content.columnSelectedOfDataset[c].aliasToShow == $scope.metadata.fields[k].header || $scope.ngModel.content.columnSelectedOfDataset[c].aliasToShow == $scope.metadata.fields[k].header) {
							return $scope.ngModel.content.columnSelectedOfDataset[c].name;
						}
					}
				}
			}
		}

		function getColumnAlias(colNum){
			for(var k in $scope.metadata.fields){
				if($scope.metadata.fields[k].dataIndex && $scope.metadata.fields[k].dataIndex == colNum) return $scope.metadata.fields[k].header;
			}
		}

		function getColumnGroup(col){
			if(col.group && $scope.ngModel.groups && $scope.ngModel.groups.length > 0){
				for(var k in $scope.ngModel.groups){
					var groupKey = $scope.ngModel.groups[k].id ? $scope.ngModel.groups[k].id : $scope.ngModel.groups[k].name;
					if(groupKey == col.group){
						return $scope.ngModel.groups[k];
					}
				}
			}else return false;
		}

		//CUSTOM HEADER TEMPLATE RENDERER
		function CustomHeader() {}

		CustomHeader.prototype.init = function (params) {
			var cellClasses = "cellcontainer ";
			this.agParams = params;
			this.agParams.enableSorting = false;
			var headerStyle = {};
			var headerText = (params.column.colDef.style && params.column.colDef.style.hideHeader) ? '' : params.displayName;
			if($scope.ngModel.style && $scope.ngModel.style.th) headerStyle = angular.copy($scope.ngModel.style.th);
			if(headerStyle && headerStyle.multiline) cellClasses = 'cellContainer multiLineHeader';
			if(params.column.colDef.style) {
				var properties = ["justify-content","color","background-color","font-style","font-weight","font-size"];
				properties.forEach(function(property){
					if(!headerStyle[property] && params.column.colDef.style[property]) headerStyle[property] = params.column.colDef.style[property];
					if(property == "justify-content" && params.column.colDef.style.overrideHeader) headerStyle[property] = params.column.colDef.style[property];
				})
			}
		    this.eGui = document.createElement('div');
		    this.eGui.style.width = '100%';
		    this.eGui.style.height = '100%';
		    this.eGui.innerHTML = '<div class="ag-cell-label-container customHeaderTemplate" role="presentation" style="background-color:'+headerStyle["background-color"]+'">'+
								'    <div ref="eLabel" class="ag-header-cell-label" role="presentation" style="color:'+headerStyle.color+';justify-content:'+headerStyle["justify-content"]+'">'+
								'       <div class="'+cellClasses+'" style="justify-content:'+headerStyle["justify-content"]+'">'+
								'			<span ref="eText" class="ag-header-cell-text" role="columnheader" style="font-style:'+headerStyle["font-style"]+';font-size:'+headerStyle["font-size"]+';font-weight:'+headerStyle["font-weight"]+'">'+headerText+'</span></div>'+
								'    	<span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon ag-hidden" ><span class="ag-icon ag-icon-asc"></span></span>'+
								'   	<span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon ag-hidden"><span class="ag-icon ag-icon-desc"></span></span>'+
								'	</div>'+
								'</div>';

		    if(params.column.colDef.sortable){
		    	this.mySortAscButton = this.eGui.querySelector(".ag-cell-label-container");
			    this.eSortDownButton = this.eGui.querySelector(".ag-sort-descending-icon");
			    this.eSortUpButton = this.eGui.querySelector(".ag-sort-ascending-icon");

			    this.onSortChangedListener = this.onSortChanged.bind(this);
		        this.agParams.column.addEventListener('sortChanged', this.onSortChangedListener);
		        this.onSortChanged();

			    this.mySortAscButton.addEventListener('click', function(event) {
			    	if(params.column.sort == '') params.setSort('asc');
			    	else params.setSort(params.column.sort == 'asc' ? 'desc' : 'asc');
			    });
		    }
		};

		CustomHeader.prototype.onSortChanged = function () {
			if (this.agParams.column.isSortAscending()) {
	    		this.eSortUpButton.classList.add('ag-hidden');
	    		this.eSortDownButton.classList.remove('ag-hidden');
		    } else if (this.agParams.column.isSortDescending()) {
		    	this.eSortUpButton.classList.remove('ag-hidden');
		    	this.eSortDownButton.classList.add('ag-hidden');
		    } else {
		    	this.eSortUpButton.classList.add('ag-hidden');
		    	this.eSortDownButton.classList.add('ag-hidden');
		    }
		}

		CustomHeader.prototype.getGui = function () {
		    return this.eGui;
		};

		//CUSTOM HEADER GROUP TEMPLATE RENDERER
		function CustomHeaderGroupRenderer() {}

		CustomHeaderGroupRenderer.prototype.init = function (params) {
			this.eGui = document.createElement('div');
			this.eGui.classList.add('customHeaderTemplate');
			for(var k in params.columnGroup.originalColumnGroup.colGroupDef.headerParams){
				if(k != 'name') this.eGui.style[k] = params.columnGroup.originalColumnGroup.colGroupDef.headerParams[k];
			}
			this.eGui.innerHTML = params.displayName;
		}

		CustomHeaderGroupRenderer.prototype.getGui = function () {
		    return this.eGui;
		};

		function getCellStyle(params){
			var tempStyle = params.colDef.style || {};
			if(params.colDef.ranges && params.colDef.ranges.length > 0){
				for(var k in params.colDef.ranges){
					if (params.value!="" && eval(params.value + params.colDef.ranges[k].operator + params.colDef.ranges[k].value)) {
						tempStyle['background-color'] = params.colDef.ranges[k]['background-color'] || (tempStyle['background-color'] || '');
						tempStyle['color'] = params.colDef.ranges[k]['color'] || (tempStyle['color'] || '');
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

		/*
		 * The number formatter prepares the data to show the number correctly in the user locale format.
		 * If a precision is set will use it, otherwise will set the precision to 2 if float, 0 if int.
		 * In case of a returning empty string that one will be displayed.
		 */
		function numberFormatter(params){
			if(params.value != "" && (!params.colDef.style || (params.colDef.style && !params.colDef.style.asString))) {
				var defaultPrecision = (params.colDef.fieldType == 'float') ? 2 : 0;
				return $filter('number')(params.value, (params.colDef.style && typeof params.colDef.style.precision != 'undefined') ? params.colDef.style.precision : defaultPrecision);
			}else return params.value;
		}

		$scope.showHiddenValues = function(e,values){
			e.stopImmediatePropagation();
			e.preventDefault();
		    $mdDialog.show({
		      controller: function($scope, listValues, sbiModule_translate){
		    	  $scope.translate = sbiModule_translate;
		    	  $scope.listValues = listValues;
		    	  $scope.close = function(){
		    		  $mdDialog.hide();
		    	  }
		      },
		      templateUrl: $scope.getTemplateUrl('multiCellListDialogTemplate'),
		      parent: angular.element(document.body),
		      locals: {listValues:values},
		      clickOutsideToClose:true
		    })
		}

		//CELL RENDERERS
		function cellRenderer () {}

		cellRenderer.prototype.init = function(params){
			this.eGui = document.createElement('span');
			var tempValue = "";
			tempValue = params.valueFormatted || params.value;
			if(!params.node.rowPinned){
				if(params.colDef.visType && (params.colDef.visType.toLowerCase() == 'chart' || params.colDef.visType.toLowerCase() == 'text & chart')){
					var minValue = (params.colDef.chart && params.colDef.chart.minValue) ? params.colDef.chart.minValue : 0;
					var maxValue = (params.colDef.chart && params.colDef.chart.maxValue) ? params.colDef.chart.maxValue : 100;
					var percentage = Math.round(((params.value - minValue)/(maxValue - minValue))*100);
					if(percentage < 0) percentage = 0;
					if(percentage > 100) percentage = 100;
					var tempStyle = params.colDef.chart && params.colDef.chart.style ? params.colDef.chart.style : {"background-color":"#3B678C","color":"#ffffff"};
					this.eGui.innerHTML = '<div class="inner-chart-bar" style="justify-content:'+tempStyle['justify-content']+'"><div class="bar" style="justify-content:'+tempStyle['justify-content']+';background-color:'+tempStyle['background-color']+';width:'+percentage+'%">'+(params.colDef.visType.toLowerCase() == 'text & chart' ? '<span style="color:'+tempStyle.color+'">'+tempValue+'</span>' : '')+'</div></div>';
				}
				if(params.colDef.ranges && params.colDef.ranges.length > 0){
					for(var k in params.colDef.ranges){
						if (typeof params.value != "undefined" && eval(params.value + params.colDef.ranges[k].operator + params.colDef.ranges[k].value)) {
							if(params.colDef.ranges[k]['background-color']) {
								if(params.colDef.visType && (params.colDef.visType.toLowerCase() == 'chart' || params.colDef.visType.toLowerCase() == 'text & chart')) {
									this.eGui.innerHTML = this.eGui.innerHTML.replace(/background-color:([\#a-z0-9\(\)\,]+);/g,function(match,p1){
										return 'background-color:'+params.colDef.ranges[k]['background-color']+';';
									})
								}else params.eParentOfValue.style.backgroundColor = params.colDef.ranges[k]['background-color'];
							}
							if(params.colDef.ranges[k]['color']) params.eParentOfValue.style.color = params.colDef.ranges[k]['color'];
							if(params.colDef.visType && params.colDef.visType.toLowerCase() == 'icon only') tempValue = '<i class="'+params.colDef.ranges[k].icon+'"></i>';
							else tempValue += '<i class="'+params.colDef.ranges[k].icon+'"></i>';
	                        if (params.colDef.ranges[k].operator == '==') break;
	                    }
					}
				}
				if(params.colDef.style && params.colDef.style.maxChars){
					tempValue = tempValue.toString().substring(0,params.colDef.style.maxChars);
				}
			}
			if(params.eParentOfValue.style.backgroundColor == "") params.eParentOfValue.style.backgroundColor = (params.colDef.style && params.colDef.style['background-color']) || 'inherit';
			if($scope.bulkSelection){
				this.manageMultiSelection(params);
			}
			if(typeof tempValue != "undefined" && this.eGui.innerHTML == '') this.eGui.innerHTML = ((params.colDef.style && params.colDef.style.prefix) || '') + tempValue + ((params.colDef.style && params.colDef.style.suffix) || '');
		}

		cellRenderer.prototype.getGui = function() {
		    return this.eGui;
		};

		cellRenderer.prototype.refresh = function(params) {
			if($scope.bulkSelection || $scope.resetSelections){
				this.manageMultiSelection(params);
			}
		}

		cellRenderer.prototype.manageMultiSelection = function(params) {
			params.eParentOfValue.style.backgroundColor = (params.colDef.style && params.colDef.style['background-color']) || 'inherit';
			if($scope.interaction && ($scope.interaction.crossType == 'allRow' || $scope.interaction.interactionType == 'allRow')){
				if($scope.selectedCells.indexOf(params.data[$scope.bulkSelection]) > -1) {
					if(this.eGui.parentNode) this.eGui.parentNode.style.backgroundColor = $scope.ngModel.settings.multiselectablecolor || '#ccc';
					else params.eParentOfValue.style.backgroundColor = $scope.ngModel.settings.multiselectablecolor || '#ccc';
				}
			}else if(params.colDef.field == $scope.bulkSelection && $scope.selectedCells.indexOf(params.value) > -1){
				if(this.eGui.parentNode) this.eGui.parentNode.style.backgroundColor = $scope.ngModel.settings.multiselectablecolor || '#ccc';
				else params.eParentOfValue.style.backgroundColor = $scope.ngModel.settings.multiselectablecolor || '#ccc';
			}
		}

		function crossIconRenderer(params){
			return '<md-button class="md-icon-button" ng-click=""><md-icon md-font-icon="'+params.colDef.crossIcon+'"></md-icon></md-button>';
		}

		function cellMultiRenderer () {}

		cellMultiRenderer.prototype.init = function(params){
			this.eGui = document.createElement('div');
			var self = this.eGui;
			if(Array.isArray(params.value)){
				params.value.slice(0,(params.colDef.style && params.colDef.style.maxChars) || params.value.length).forEach(function(item){
					self.innerHTML += '<span class="miniChip">'+item+'</span>';
				})
				if(params.colDef.style && params.colDef.style.maxChars && params.value.length > params.colDef.style.maxChars) {
					this.eGui.innerHTML += '<i class="fa fa-search maxcharsButton"></i>';
					this.eButton = this.eGui.querySelector('.maxcharsButton');
					this.eventListener = function(e) {
				        $scope.showHiddenValues(e,params.value);
				    };
				    this.eButton.addEventListener('click', this.eventListener);
				}
			}
		}

		cellMultiRenderer.prototype.getGui = function() {
		    return this.eGui;
		};

		function SummaryRowRenderer () {}

		SummaryRowRenderer.prototype.init = function(params) {
		    this.eGui = document.createElement('div');
		    this.eGui.style.color = (params.style && params.style.color) || (params.colDef.style && params.colDef.style.color) || "";
		    this.eGui.style.backgroundColor = (params.style && params.style['background-color']) || (params.colDef.style && params.colDef.style['background-color']) || "";
		    this.eGui.style.justifyContent = (params.style && params.style['justify-content']) || (params.colDef.style && params.colDef.style['justify-content']) || "";
		    this.eGui.style.fontSize = (params.style && params.style['font-size']) || (params.colDef.style && params.colDef.style['font-size']) || "";
		    this.eGui.style.fontWeight = (params.style && params.style['font-weight']) || (params.colDef.style && params.colDef.style['font-weight']) || "";
		    this.eGui.style.fontStyle = (params.style && params.style['font-style']) || (params.colDef.style && params.colDef.style['font-style']) || "";
            if(params.colDef.style && params.colDef.style.hideSummary) this.eGui.innerHTML = '';
            else {
            	var title = $filter('i18n')(params.summaryRows[params.rowIndex].label);
            	if(title && params.style && params.style['pinnedOnly'] && params.column.pinned) this.eGui.innerHTML ='<b style="margin-right: 4px;">'+title+'</b>';
            	if(params.valueFormatted || params.value){
            		if (params.rowIndex == 0 || !params.colDef.isCalculated) {
	            		if(params.summaryRows[params.rowIndex].aggregation == 'COUNT' || params.summaryRows[params.rowIndex].aggregation == 'COUNT_DISTINCT') {
	            			var tempValue = $filter('number')(params.value,0);
	            		}else var tempValue = params.valueFormatted || params.value;
	            		if((!params.style || !params.style['pinnedOnly']) && title) this.eGui.innerHTML ='<b style="margin-right: 4px;">'+title+'</b>';
	            		if(params.colDef.cellStyle && params.colDef.cellStyle.prefix) this.eGui.innerHTML += params.colDef.cellStyle.prefix;
	            		this.eGui.innerHTML += tempValue;
	            		if(params.colDef.cellStyle && params.colDef.cellStyle.suffix) this.eGui.innerHTML += params.colDef.cellStyle.suffix;
            		}
	    		}
            }
		};

		SummaryRowRenderer.prototype.getGui = function() {
		    return this.eGui;
		};

		function indexCellRenderer(params) {
			return (($scope.ngModel.settings.page - 1) * $scope.ngModel.settings.pagination.itemsNumber) + (params.rowIndex + 1);
		}

		function TooltipValue(params) {
			if (params.colDef.style && params.colDef.style.hideSummary && params.node.rowPinned) {
				return ""
			}
			if(params.colDef.style && params.colDef.style.tooltip) {
				var tempValue = params.valueFormatted || params.value;
				if(typeof params.colDef.style.tooltip.precision != 'undefined'){
					tempValue = $filter('number')(params.value, params.colDef.style.tooltip.precision);
				}
				return (params.colDef.style.tooltip.prefix || '') + tempValue + (params.colDef.style.tooltip.suffix || '');
			}
			return params.valueFormatted || params.value;
		}

		function RowSpanCalculator(params) {
			if(params.data.span > 1){
				return params.data.span;
			}else return 1;
        };

		$scope.init=function(element,width,height){
			for(var k in $scope.ngModel.content.columnSelectedOfDataset){
				if($scope.ngModel.content.columnSelectedOfDataset[k].isCalculated && $scope.ngModel.content.columnSelectedOfDataset[k].formulaEditor){
					$scope.ngModel.content.columnSelectedOfDataset[k].formula = $scope.ngModel.content.columnSelectedOfDataset[k].formulaEditor.replace(/(\$V\{)([a-zA-Z0-9\-\_\s]*)(\})/g, function(match,p1,p2){
						return cockpitModule_properties.VARIABLES[p2];
					});
				}
			}
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
				$scope.tempRows = datasetRecords.rows;
				if($scope.ngModel.style && $scope.ngModel.style.tr && $scope.ngModel.style.tr.height){
					_rowHeight = $scope.ngModel.style.tr.height;
					$scope.advancedTableGrid.api.resetRowHeights();
				}else {
					_rowHeight = 0;
					$scope.advancedTableGrid.api.resetRowHeights();
				}
				if($scope.ngModel.style && $scope.ngModel.style.th){
					if($scope.ngModel.style.th.enabled) $scope.advancedTableGrid.api.setHeaderHeight($scope.ngModel.style.th.height || 32);
					else $scope.advancedTableGrid.api.setHeaderHeight(0);
				}
				$scope.advancedTableGrid.api.setRowData([])
				if(nature == 'sorting') $scope.advancedTableGrid.api.setColumnDefs(getColumns(datasetRecords.metaData.fields,$scope.advancedTableGrid.api.getSortModel()));
				else $scope.advancedTableGrid.api.setColumnDefs(getColumns(datasetRecords.metaData.fields));
				if($scope.ngModel.settings.summary && $scope.ngModel.settings.summary.enabled) {
					var rowsNumber = 1;
					if($scope.ngModel.settings.summary.list) rowsNumber = $scope.ngModel.settings.summary.list.length;
					$scope.advancedTableGrid.api.setRowData(datasetRecords.rows.slice(0,datasetRecords.rows.length - rowsNumber));
					$scope.advancedTableGrid.api.setPinnedBottomRowData(datasetRecords.rows.slice( -rowsNumber));
				}
				else {
					$scope.advancedTableGrid.api.setRowData(datasetRecords.rows);
					$scope.advancedTableGrid.api.setPinnedBottomRowData([]);
				}

				if($scope.ngModel.settings.pagination && $scope.ngModel.settings.pagination.enabled && !$scope.ngModel.settings.pagination.frontEnd){
					$scope.ngModel.settings.pagination.itemsNumber = $scope.ngModel.settings.pagination.itemsNumber || 15;
					$scope.totalPages = Math.ceil($scope.totalRows/$scope.ngModel.settings.pagination.itemsNumber) || 0;
					$scope.hidePagination = $scope.totalPages < 2 ? true : false;
					if($scope.totalRows > 0 && $scope.ngModel.settings.page > Math.ceil($scope.totalRows / $scope.ngModel.settings.pagination.itemsNumber)){
						$scope.ngModel.settings.page = Math.ceil($scope.totalRows/$scope.ngModel.settings.pagination.itemsNumber);
						$scope.refreshWidget();
						return;
					};
				}
				if($scope.ngModel.settings.norows){
					if($scope.ngModel.settings.norows.hide) $scope.advancedTableGrid.api.hideOverlay();
					if($scope.ngModel.settings.norows.message) $scope.advancedTableGrid.localeText.noRowsToShow = $scope.ngModel.settings.norows.message;
				}
				if($scope.ngModel.settings.pagination.enabled && $scope.ngModel.settings.pagination.frontEnd && $scope.ngModel.settings.pagination.itemsNumber) $scope.advancedTableGrid.api.paginationSetPageSize($scope.ngModel.settings.pagination.itemsNumber);
				resizeColumns();
				$scope.hideWidgetSpinner();
			}else $scope.hideWidgetSpinner();

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
				onGridReady: readyResizeColumns,
				onSortChanged: changeSorting,
				pagination : true,
				suppressRowTransform: true,
				onCellClicked: onCellClicked,
				defaultColDef: {
					resizable: cockpitModule_properties.EDIT_MODE,
					suppressMovable: true,
					sortable: true
				},
				components: {
			        agColumnHeader: CustomHeader
			    },
				onColumnResized: columnResized,
				getRowHeight: getRowHeight,
				getRowStyle: function(params) {
					// TODO : make this a CSS rule with a custom class
					if($scope.ngModel.settings.alternateRows && $scope.ngModel.settings.alternateRows.enabled){
					    if($scope.ngModel.settings.alternateRows.oddRowsColor && params.node.rowIndex % 2 === 0) {
					        return { background: $scope.ngModel.settings.alternateRows.oddRowsColor }
					    }
					    if($scope.ngModel.settings.alternateRows.evenRowsColor && params.node.rowIndex % 2 != 0){
					    	return { background: $scope.ngModel.settings.alternateRows.evenRowsColor }
					    }
					}
				},
				localeText : knModule_aggridLabels
		}

		if($scope.ngModel.settings.norows && $scope.ngModel.settings.norows.message) $scope.advancedTableGrid.localeText.noRowsToShow = $filter('i18n')($scope.ngModel.settings.norows.message);

		function getRowHeight(params) {
			if(_rowHeight > 0) return _rowHeight;
			else return 28;
		}
		function changeSorting(){
			if($scope.ngModel.settings.pagination && $scope.ngModel.settings.pagination.enabled && !$scope.ngModel.settings.pagination.frontEnd){
				$scope.showWidgetSpinner();
				var sorting = $scope.advancedTableGrid.api.getSortModel();
				if(sorting[0]) sorting[0].colId = sorting[0].colId.replace(/(column_[0-9]+)(?:_[0-9]+)/gm, '$1');
				$scope.ngModel.settings.sortingColumn = sorting.length>0 ? getColumnName(sorting[0].colId) : '';
				$scope.ngModel.settings.sortingOrder = sorting.length>0 ? sorting[0]['sort'].toUpperCase() : '';
				$scope.refreshWidget(null, 'sorting');
			}
			// Re-apply styles to all rows
			// TODO : it's too heavy!
			//$scope.advancedTableGrid.api.redrawRows();
		}
		function columnResized(params){
			if(params.source != "sizeColumnsToFit"){
				if(params.finished){
					for(var c in $scope.ngModel.content.columnSelectedOfDataset){
						if($scope.ngModel.content.columnSelectedOfDataset[c].aliasToShow == params.column.colDef.headerName){
							if($scope.ngModel.content.columnSelectedOfDataset[c].style) $scope.ngModel.content.columnSelectedOfDataset[c].style.width = params.column.actualWidth;
							else $scope.ngModel.content.columnSelectedOfDataset[c].style = {width : params.column.actualWidth};
							break;
						}
					}
				}
			}
		}

		var resizeTimer = false;
		function readyResizeColumns(){
			$scope.advancedTableGrid.api.sizeColumnsToFit();
			if(resizeTimer) clearTimeout(resizeTimer);
			else if($scope.advancedTableGrid.api) resizeTimer = setTimeout(function(){ $scope.advancedTableGrid.api.sizeColumnsToFit(); }, 5000);
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

	  	function mapRow(rowData){
			var keyMap = {};
			for(var r in rowData){
				for(var f in $scope.metadata.fields){
					if(f != 0 && $scope.metadata.fields[f].dataIndex == r) keyMap[$scope.metadata.fields[f].header] = rowData[r];
				}
			}
			return keyMap;
		}

	  	function previewDataset(row, column) {
			if ($scope.ngModel.cross.preview.parameters &&
				    (angular.isArray($scope.ngModel.cross.preview.parameters) && $scope.ngModel.cross.preview.parameters.length > 0)) {
				newValue = $scope.ngModel.cross.preview.parameters;
				$scope.doSelection(column, row[column], newValue, undefined, row);
			} else if ($scope.ngModel.cross.preview.column && $scope.ngModel.cross.preview.column != "") {
				// if modal column is selected
				newValue = row[$scope.ngModel.cross.preview.column];
				$scope.doSelection(column, row[column], $scope.ngModel.cross.preview.column, newValue, row);
			} else {
				// previewing common Dataset, without parameters
				$scope.doSelection(column, row[column], undefined, undefined, row);
			}
		}

		function getColumnNameFromTableMetadata(colAlias, colId){
			if(colId){
				for(var m in $scope.metadata.fields){
					if($scope.metadata.fields[m].name && $scope.metadata.fields[m].name == colId) return $scope.metadata.fields[m].header;
				}
			}
			for(var k in $scope.ngModel.content.columnSelectedOfDataset){
				if($scope.ngModel.content.columnSelectedOfDataset[k].aliasToShow && $scope.ngModel.content.columnSelectedOfDataset[k].aliasToShow == colAlias) return $scope.ngModel.content.columnSelectedOfDataset[k].name;
			}
		}

		function onCellClicked(node){
			var interactionType = $scope.interaction && ($scope.interaction.crossType || $scope.interaction.previewType || $scope.interaction.interactionType);
			if($scope.cliccable==false) return;
			if(node.colDef.measure=='MEASURE' && !$scope.ngModel.settings.modalSelectionColumn && !crossHasType('allRow')) return;
			if(!crossHasType('icon') && (node.value == "" || node.value == undefined)) return;
			if(node.rowPinned) return;
			if(crossHasType('icon') && node.colDef.crossIcon) {
				$scope.doSelection(node.colDef.field || null, null, null, null, mapRow(node.data));
				return;
			}
			if ($scope.interaction && $scope.interaction.previewType && $scope.interaction.enable) {
				switch (interactionType) {
				case 'allRow':
					previewDataset(mapRow(node.data), node.colDef.headerName);
					return;
					break;
				case 'singleColumn':
					if (node.colDef.headerName == $scope.ngModel.cross.preview.column) {
						previewDataset(mapRow(node.data), node.colDef.headerName);
						return;
					}
					break;
				case 'icon':
					if (node.colDef.headerName == "") {
						previewDataset(mapRow(node.data), node.colDef.headerName);
						return;
					}
					break;
				}
			}

			if($scope.ngModel.settings.multiselectable) {
				//first check to see it the column selected is the same, if not clear the past selections
				if(!$scope.bulkSelection || ($scope.bulkSelection!=node.colDef.field && interactionType != 'allRow')){
					$scope.selectedCells.splice(0,$scope.selectedCells.length);
					$scope.selectedRows.splice(0,$scope.selectedRows.length);
					if($scope.interaction && interactionType == 'allRow'){
						for(var f in $scope.metadata.fields){
							if($scope.metadata.fields[f].header && $scope.metadata.fields[f].header == $scope.interaction.column){
								$scope.bulkSelection = $scope.metadata.fields[f].name;
								break;
							}
						}
					}else $scope.bulkSelection = node.colDef.field;
					$scope.$apply();
				}
				if($scope.interaction && interactionType == 'allRow'){
					if(($scope.selectedCells.indexOf(node.data[$scope.bulkSelection])==-1)){
						$scope.selectedCells.push(node.data[$scope.bulkSelection]);
						$scope.selectedRows.push(node.data);
					}else{
						$scope.selectedCells.splice($scope.selectedCells.indexOf(node.data[$scope.bulkSelection]),1);
						$scope.selectedRows.splice($scope.selectedRows.indexOf(node.data),1);
						//if there are no selection left set bulk selection to false to avoid the selection button to show
						if($scope.selectedCells.length==0) $scope.bulkSelection=false;
					}
				}else{
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
				}
				$scope.advancedTableGrid.api.refreshCells({columns:$scope.columnsNameArray,force:true});
			}else {
				newValue = [];
				if ($scope.ngModel.settings.modalSelectionColumn!= undefined) {

					var rows = [];
					rows.push(mapRow(node.data));

					for(var k in rows){
						newValue.push(rows[k][$scope.ngModel.settings.modalSelectionColumn]);
					}
				}
				else {
					newValue = null;
				}
				$scope.doSelection(getColumnNameFromTableMetadata(node.colDef.headerName, node.colDef.field), node.value, $scope.ngModel.settings.modalSelectionColumn, newValue, mapRow(node.data));
			}
		}


		$scope.clickItem = function(e,row,column){
			$scope.advancedTableGrid.api.deselectAll();
			var newValue = undefined;

			column = getColumnAlias(column);

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

			$scope.doSelection(column,valuesArray,$scope.ngModel.settings.modalSelectionColumn || null,newValue,rows);
			$scope.bulkSelection = false;
		}

		$scope.cancelBulkSelection = function(){
			$scope.bulkSelection = false;
			$scope.resetSelections = true;
			$scope.advancedTableGrid.api.refreshCells({columns:$scope.columnsNameArray,force:true});
			$timeout(function(){$scope.resetSelections = false},1000)

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
					templateUrl: sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/directives/cockpit-widget/widget/advancedTableWidget/templates/advancedTableWidgetEditPropertyTemplate.html',
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
	addWidgetFunctionality("table",{'initialDimension':{'width':10, 'height':7},'updateble':true,'cliccable':true});

})();
