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
'use strict';
(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

    angular.module('cockpitTable', [])
        .directive('cockpitTable', ['$mdDialog', 'sbiModule_translate', 'cockpitModule_generalServices', function($mdDialog, sbiModule_translate, cockpitModule_generalServices) {

            return {
                restrict: "E",
                scope: {
                    columns:	"=",
                    model:		"=",
                    settings:	"=?",
                    styledata:	"=?",
                    clickFunction: "&?"
                },
                templateUrl: currentScriptPath+'/templates/cockpitTable.tpl.html',
                link: function(scope, elem, attr) {
                	scope.translate = sbiModule_translate;
                	scope.generalServices = cockpitModule_generalServices;
                    scope.loading = true; 			//initializing directive with the loading active
                    scope.settings.page = 1; 		//initial page number
                    scope.settings.rowsCount = 0;	//initial rows count
                    scope.bulkSelection = false; 	//initializing bulk selection
                    scope.selectedCells = []; 		//initializing selected cells array, used for bulk selection
                    scope.selectedRows = [];		//initializing selected rows array, used for bulk selection
                    scope.alignMap = {
	            		"flex-start":"left",
	            	 	 "center":"center",
	            	  	"flex-end":"right"
                	};

                    scope.tableId = Math.ceil(Math.random()*1000).toString();

                    //returning the column name for the sorting
                    scope.getSortingColumnFilter = function(){
                    	if(scope.settings.pagination.frontEnd && scope.settings.sortingColumn){
                    		return "'" + scope.settings.sortingColumn + "'";
                    	}
                		return "";
                    }    
                    
                    scope.datify = function(dateString,dateFormat){
                    	var returningDate = moment(dateString, dateFormat).valueOf();
                    	if(isNaN(returningDate)){
                    		returningDate = dateString;
                    	}
                    	return  returningDate;
                    }

                    //returning if the order is ASC or DESC
                    scope.getSortingOrderAsBoolean = function(){
                    	if(scope.settings.pagination.frontEnd && scope.settings.sortingOrder){
                    		return scope.settings.sortingOrder.toUpperCase() == 'DESC';
                    	}
                    	return false;
                    }

                    //checking the presence of the rows model to stop the loading. deregistering the watcher after
                    var loadingWatcher = scope.$watch('model', function(newValue, oldValue) {
                        if (newValue != undefined) {
                            scope.loading = false;
                            loadingWatcher();
                        }
                    });

                    //returning the style of the row, changing if alternate rows are enabled
                    scope.getRowStyle = function(even){
                    	var style = scope.styledata.tr ? scope.styledata.tr: {};
                    	if(scope.settings.autoRowsHeight) style.height = 0;
                    	if(scope.settings.alternateRows && scope.settings.alternateRows.enabled){
                    		style['background-color'] = even ? scope.settings.alternateRows.evenRowsColor : scope.settings.alternateRows.oddRowsColor;
                    	}
                    	return style;
                    }
                    
                    scope.checkPrecision = function(column){
                    	return (column.style && typeof column.style.precision != 'undefined') ? column.style.precision : 2;
                    }

                    //changing the flex align to text-align for the table cells


                    scope.flexToAlign = function(map, key, obj){obj["text-align"] = map[key] ? map[key] :"left";}

                    //returning the style of the header cell
                    scope.getThStyle = function(thStyle,column){
                    	var style = angular.copy(thStyle?thStyle:{})
                    	if(column.style && column.style.width) {
                    		style['max-width'] = column.style.width;
                    		style.width = column.style.width;
                		}else{
                			delete(style['max-width']);
                    		delete(style.width);
                		}
                    	if(column.style && column.style['justify-content']) scope.flexToAlign(scope.alignMap,column.style['justify-content'],style);
                    	return style;
                    }

                    scope.getSummaryStyle = function(column){
                    	var summarystyle = angular.copy(scope.styledata.summary?scope.styledata.summary:{});
                    	if(column.style && column.style.width) summarystyle['max-width'] = column.style.width;
                    	if(column.style && column.style.width) summarystyle.width = column.style.width;
                    	if(column.style && column.style.td) scope.flexToAlign(scope.alignMap,column.style.td['justify-content'],summarystyle);
                    	return summarystyle;
                    }

                    //returning the style of the cell
                    scope.getCellStyle = function(column,value){
                    	var style= {};
                    	if(!scope.settings.showGrid) style['border-width'] = 0;
                    	if(scope.styledata.td) angular.merge(style,scope.styledata.td);
                    	if(column.style) angular.merge(style,column.style);


                    	if(column.ranges && column.ranges.length >0){
                    		var ranges = column.ranges;
                    		for (var k in ranges) {
                                if (value!="" && eval(value + ranges[k].operator + ranges[k].value)) {
                                	style['background-color'] = ranges[k]['background-color'] || '';
                                	style['color'] = ranges[k]['color'] || '';
                                    if (ranges[k].operator == '==') break;
                                }
                            }
                    	}
                    	return style;
                    }

                    //function bind to the parent element. Passing mouse event, row and column data.
                    scope.selectCell = function(event, row, column) {
                    	event.stopImmediatePropagation();
                    	event.preventDefault();
						 if(!scope.settings.multiselectable || column.fieldType == "MEASURE"){
							 scope.clickItem(event,row,column);
							 return;
						 }
						 if(scope.$parent.ngModel.cliccable==false){
							console.log("widget is not cliccable")
							return;
						}
						//first check to see it the column selected is the same, if not clear the past selections
						if(!scope.bulkSelection || scope.bulkSelection.aliasToShow!=column.aliasToShow){
							scope.selectedCells.splice(0,scope.selectedCells.length);
							scope.selectedRows.splice(0,scope.selectedRows.length);
							scope.bulkSelection = column;
						}

						//check if the selected element exists in the selectedCells array, remove it in case.
						if((scope.selectedCells.indexOf(row[column.aliasToShow])==-1 && !scope.settings.modalSelectionColumn) || (scope.settings.modalSelectionColumn && scope.selectedRows.indexOf(row)==-1)){
							scope.selectedCells.push(row[column.aliasToShow]);
							scope.selectedRows.push(row);

						}else{
							scope.selectedCells.splice(scope.selectedCells.indexOf(row[column.aliasToShow]),1);
							scope.selectedRows.splice(scope.selectedRows.indexOf(row),1);
							//if there are no selection left set bulk selection to false to avoid the selection button to show
							if(scope.selectedCells.length==0) scope.bulkSelection=false;
						}

                    }

                    //cancel all active selection and exit bulk selection mode
                	scope.cancelBulkSelection = function(){
                		scope.selectedCells.splice(0,scope.selectedCells.length);
                		scope.bulkSelection = false;
                	}

                	//check if the element is selected to highlight the element
                	scope.isCellSelected = function(row,column){
                		if(scope.bulkSelection){
                			if(scope.settings.modalSelectionColumn){
                    			return (scope.settings.modalSelectionColumn == column.name && scope.selectedRows.indexOf(row)!=-1)?true:false;
                    		}else{
                    			return (column.aliasToShow == scope.bulkSelection.aliasToShow && scope.selectedCells.indexOf(row[scope.bulkSelection.aliasToShow])!=-1)?true:false;
                    		}
                		}
                	}

//                	scope.getContainerStyle = function(column){
//                		var style = {};
//                		if(column.style && column.style.td && column.style.td['justify-content']) style['justify-content'] = column.style.td['justify-content'];
//                		return style;
//                	}

                	//function passed to parent on click
                    scope.clickItem = function (event,row,column) {
                    	event.preventDefault();
                    	scope.bulkSelection = false;

                    	scope.clickFunction({
					        "evt": event,
						    "row": row,
						    "column": column
					    });
                    }

                    //sorting function
                    scope.sort = function(col, e) {
                        if (scope.settings.sortingColumn && scope.settings.sortingColumn == col.aliasToShow) {
                        	scope.settings.sortingOrder = scope.settings.sortingOrder.toUpperCase() == 'DESC' ? 'ASC' : 'DESC';
                        }else{
                        	scope.settings.sortingOrder = 'ASC';
                        }

                        scope.settings.sortingColumn = col.aliasToShow;
                    }

                    //barchart fill percentage
                    scope.getBarChartFill = function(value, minValue, maxValue) {
                    	if(value){
                    		return ((((value<minValue)?minValue:value) - minValue) / (maxValue-minValue)) * 100 + '%';
                    	}else{
                    		return '0%';
                    	}
                    }
                    
                    scope.getBarchartColor = function(column,value){
                		for(var r in column.ranges){
                			if(eval(value+column.ranges[r].operator+column.ranges[r].value)){
                				return column.ranges[r].color;
                			}
                		}
                		return column.barchart.style['background-color'];
                    }

                    //icon ranges recognition
                    scope.getDynamicIcon = function(column, value) {
                    	var ranges = column.ranges;
                        var icon = "";
                        for (var k in ranges) {
                            if (typeof value != "undefined" && eval(value + ranges[k].operator + ranges[k].value)) {
                                icon = {
                                    "iconClass": ranges[k].icon,
                                    "iconColor": ranges[k].color
                                };
                                if (ranges[k].operator == '==') break;
                            }
                        }
                        return icon;
                    };
                    
                    scope.dynamicIcon = function() {
                    	if (!scope.$parent.ngModel && !scope.$parent.ngModel.cross) {
                    		return;
                    	}
                    	if (scope.$parent.ngModel.cross.cross && scope.$parent.ngModel.cross.cross.enable && scope.$parent.ngModel.cross.cross.crossType == 'icon') {
                    		return scope.$parent.ngModel.cross.cross.icon;
                    	} else if (scope.$parent.ngModel.cross.preview && scope.$parent.ngModel.cross.preview.enable && scope.$parent.ngModel.cross.preview.previewType == 'icon') {
                    		return scope.$parent.ngModel.cross.preview.icon;
                    	}                    	
                    }
                    
                    scope.hasPrecision = function(column){
                    	if(column.style && column.style.asString) return false;
                    	return scope.generalServices.isNumericColumn(column);
                    }
                    
                    scope.isNumber = function(value){
                         return value != undefined && value.trim().length > 0 && !isNaN(value);
                    }
                    
                    //conditional value formatting
                    scope.formatValue = function (value, column){
            			var output = value;
            			var precision = 2;
            			if (column.style && column.style.precision >= 0) precision =  column.style.precision;

            			//setting the number precision when format is not present
            			if (column.style && column.style.precision && !column.style.format) {
            				output = value.toFixed(column.style.precision);
            			}

            			if (column.style && column.style.format){
            		    	switch (column.style.format) {
            		    	case "#.###":
            		    		output = scope.numberFormat(value, 0, ',', '.');
            		    	break;
            		    	case "#,###":
            		    		output = scope.numberFormat(value, 0, '.', ',');
            		    	break;
            		    	case "#.###,##":
            		    		output = scope.numberFormat(value, precision, ',', '.');
            		    	break;
            		    	case "#,###.##":
            		    		output = scope.numberFormat(value, precision, '.', ',');
            		    		break;
            		    	default:
            		    		break;
            		    	}
            			}
            	    	return output;
            		}

                    //formatting function with the given parameters
            		scope.numberFormat = function (value, dec, dsep, tsep) {

              		  if (isNaN(value) || value == null) return value;

              		  value = parseFloat(value).toFixed(~~dec);
              		  tsep = typeof tsep == 'string' ? tsep : ',';

              		  var parts = value.split('.'), fnums = parts[0],
              		    decimals = parts[1] ? (dsep || '.') + parts[1] : '';


              		  return fnums.replace(/(\d)(?=(?:\d{3})+$)/g, '$1' + tsep) + decimals;
            		}

            		//popup to show cell content on click when the cell is not wide enough
            		scope.showFullContent = function (e,value){
            			e.stopPropagation();
            			$mdDialog.show(
            		      $mdDialog.alert()
            		        .parent(angular.element(document.body))
            		        .clickOutsideToClose(true)
            		        .textContent(value)
            		        .ok('Close')
            		        .targetEvent(e)
            		    );
            		}

                }
            }
        }])

        // for I18N of custom messages
        .filter('i18n', function(sbiModule_i18n) {
			return function(label) {
				return sbiModule_i18n.getI18n(label);
			}
		})

    //Pagination directive
    .directive('cockpitTablePagination', ['sbiModule_translate', function(sbiModule_translate) {
        return {
            restrict: "E",
            templateUrl: currentScriptPath+'/templates/cockpitTablePagination.tpl.html',
            replace: true,
            scope: {
            	model:"=",
            	settings:"="
            },
            link: function(scope, elem, attr) {
            	scope.translate=sbiModule_translate;
            	scope.Math = window.Math;

            	scope.widgetWidth = elem[0].clientWidth;
            	scope.settings.page = scope.settings.page != 0 ? scope.settings.page : 1;

            	scope.maxPageCount = 1;

            	scope.$watch('settings.pagination.frontEnd', function(newValue, oldValue) {
        			if(newValue != undefined){
        				if(newValue){
        					if(scope.watchRowsCountForBackend){
        						scope.watchRowsCountForBackend();
        					}

        					scope.watchRowsCountForFrontend = scope.$watch('model.length + "," + settings.pagination.itemsNumber', function(newValue, oldValue) {
        						if(newValue != undefined){
        							scope.updateTotalRows();
        						}
        					});
        				}else{
        					if(scope.watchRowsCountForFrontend){
        						scope.watchRowsCountForFrontend();
        					}

        					scope.watchRowsCountForBackend = scope.$watch('settings.backendTotalRows + "," + settings.pagination.itemsNumber', function(newValue, oldValue) {
        						if(newValue != undefined){
        							scope.updateTotalRows();
        						}
        					});
        				}
        			}
        		});

            	scope.updateTotalRows = function() {
            		scope.settings.rowsCount = scope.settings.pagination.frontEnd ? scope.model.length: scope.settings.backendTotalRows;

            		scope.maxPageCount = scope.settings.pagination.enabled ? Math.ceil(scope.settings.rowsCount/scope.settings.pagination.itemsNumber) : 1;
            		if(scope.maxPageCount < 1){
            			scope.maxPageCount = 1;
            		}

            		scope.settings.page = scope.settings.page > scope.maxPageCount ? 1 : scope.settings.page;
                }

                scope.getTotalPages = function() {
                	return new Array(Math.ceil(scope.settings.rowsCount / scope.settings.pagination.itemsNumber));
                }

                scope.getNumber = function(num) {
                    return new Array(num);
                }

                scope.hasPrevious = function() {
                    return scope.settings.page > 1;
                };

                scope.hasNext = function() {
                    return scope.settings.page * scope.settings.pagination.itemsNumber < scope.settings.rowsCount;
                };

                scope.min = function() {
                    return scope.settings.rowsCount > 0 ? (scope.settings.page - 1) * scope.settings.pagination.itemsNumber + 1 : 0;
                };

                scope.max = function() {
                    return scope.hasNext() ? (scope.settings.page * scope.settings.pagination.itemsNumber) : scope.settings.rowsCount;
                };

                scope.next = function() {
                	scope.hasNext() && scope.settings.page++;
                }

                scope.previous = function() {
                	scope.hasPrevious() && scope.settings.page--;
                }
            }
        };
    }])

    .directive("onScroll", function ($window) {
	    return {
	    	restrict: "A",
            link: function(scope, elem, attr) {
		        elem.bind("scroll", function() {
		        	scope.unit = "px";
		        	angular.element(document.querySelector( '#'+attr.onScroll ))[0].style.left = -(this.scrollLeft)+scope.unit;
		        });
            }
	    }
	})

}());