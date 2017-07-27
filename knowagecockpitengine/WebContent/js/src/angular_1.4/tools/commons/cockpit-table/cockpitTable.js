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
        .directive('cockpitTable', function() {
            return {
                restrict: "E",
                scope: {
                    columns: "=",
                    model: "=",
                    settings: "=?",
                    clickFunction: "&?"
                },
                templateUrl: currentScriptPath+'/templates/cockpitTable.tpl.html',
                link: function(scope, elem, attr) {
                    scope.loading = true; 			//initializing directive with the loading active
                    scope.sortingCol = false; 		//no initial sorting
                    scope.settings.page = 1; 		//initial page number

                    //checking the presence of the rows model to stop the loading. deregistering the watcher after
                    var loadingWatcher = scope.$watch('model', function(newValue, oldValue) {
                        if (newValue != undefined) {
                            scope.loading = false;
                            loadingWatcher();
                        }
                    });

                    //function bind to the parent element. Passing mouse event, row and column data.
                    scope.selectCell = function(event, row, column) {
                        scope.clickFunction({
                            "e": event,
                            "row": row,
                            "column": column
                        });
                    }

                    //sorting function
                    scope.sort = function(col, e) {
                        if (col.sortable) {
                            if (scope.orderCol == col.name) {
                                scope.sortingCol = scope.sortingCol ? false : true;
                            }
                            scope.orderCol = col.name;
                            scope.orderColFilter = "'"+col.name+"'";
                        }
                    }

                    //barchart fill percentage
                    scope.getBarChartFill = function(value, maxValue) {
                        return (value / maxValue) * 100 + '%';
                    }

                    //icon ranges recognition
                    scope.getDynamicIcon = function(column, value) {
                    	var ranges = column.icon.ranges;
                        var icon = "";
                        for (var k in ranges) {
                            if (value!="" && eval(value + ranges[k].operator + ranges[k].value)) {
                                icon = {
                                    "iconClass": ranges[k].icon,
                                    "iconColor": ranges[k].color
                                };
                                if (ranges[k].operator == '==') break;
                            }
                        }
                        return icon;
                    };
                }
            }
        })

    //Pagination directive
    .directive('cockpitTablePagination', function() {
        return {
            restrict: "E",
            templateUrl: currentScriptPath+'/templates/cockpitTablePagination.tpl.html',
            replace: true,
            scope: {
            	model:"=",
            	settings:"="
            },
            link: function(scope, elem, attr) {

                scope.limit = scope.settings.pagination.itemsNumber;
                scope.total = scope.model.length;
                
                
                scope.getTotalPages = function() {
                	return new Array(Math.ceil(scope.model.length / scope.settings.pagination.itemsNumber));
                } 

                scope.getNumber = function(num) {
                    return new Array(num);
                }

                scope.hasPrevious = function() {
                    return scope.settings.page > 1;
                };

                scope.hasNext = function() {
                    return scope.settings.page * scope.settings.pagination.itemsNumber < scope.model.length;
                };

                scope.min = function() {
                    return scope.model.length > 0 ? scope.settings.page * scope.settings.pagination.itemsNumber - scope.settings.pagination.itemsNumber + 1 : 0;
                };

                scope.max = function() {
                    return scope.hasNext() ? (scope.settings.page * scope.settings.pagination.itemsNumber) : scope.model.length;
                };

                scope.next = function() {
                    scope.hasNext() && scope.settings.page++;
                }

                scope.previous = function() {
                    scope.hasPrevious() && scope.settings.page--;
                }
            }
        };
    });
}());