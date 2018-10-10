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

angular.module('qbe_expander_list', ['ngDraggable'])
.directive('qbeExpanderList', function($sce) {
        return {
            restrict: 'E',
            scope: {
                ngModel: '=',
                childrenName: '@?',
                displayPropertyName: '@?',
                colors: '=?',
                fontIcons: '@?',
                dragAction: '&?',
                entitiesActions: '=?',
                fieldsActions: '=?'
            },
            transclude: true,
            templateUrl: currentScriptPath + 'expander-list.html',
            replace: true,
            link: function link(scope, element, attrs) {
                scope.usedColorIndex = 0;
                scope.dragEnabled = scope.dragAction ? true : false;
                scope.childrenName = (scope.childrenName == undefined) ? "children" : scope.childrenName;
                scope.displayPropertyName = (scope.displayPropertyName == undefined) ? "text" : scope.displayPropertyName;

                //optional colorizing function to create the colored squares on the view. If no colors are given the blocks disappear.
                scope.colorize = function() {
                    for (var k in scope.ngModel) {
                        if (scope.ngModel.hasOwnProperty(k)) {
                            for (var j in scope.ngModel[k]) {
                                var color = scope.colors[scope.usedColorIndex];
                                scope.usedColorIndex++;
                                scope.ngModel[k][j].color = color;
                                if (scope.ngModel[k][j].children) {
                                    for (var y in scope.ngModel[k][j].children) {
                                        scope.ngModel[k][j].children[y].color = color;
                                    }
                                }
                            }
                        }
                    }
                }

                //function to expand or contract the entities. Uses angular hashkey so avoid using the $index in the view
                scope.toggleExpander = function(row) {
                    for (var k in scope.ngModel) {
                        if (scope.ngModel.hasOwnProperty(k)) {
                            for (var j in scope.ngModel[k]) {
                                if (scope.ngModel[k][j].$$hashKey == row.$$hashKey) {
                                    scope.ngModel[k][j].expanded = scope.ngModel[k][j].expanded ? false : true;
                                    return;
                                }
                            }
                        }
                    }
                }

                //drag function to export data from the list to the detail
                scope.dragLi = function(data, ev) {
                    if (scope.dragAction) {
                        ev.event.preventDefault();
                        scope.dragAction({ "data": data });
                    }
                }

                scope.checkDescription = function (field){
            		var desc = "";
            		for (var i = 0; i < scope.$parent.filters.length; i++) {
            			if(scope.$parent.filters[i].leftOperandDescription == field.attributes.entity+" : "+field.text){
            				desc =desc.concat(" "
            				+scope.$parent.filters[i].operator + " " +scope.$parent.filters[i].rightOperandDescription + "\n") ;
            			}
            		}
            		if(desc==""){
            			return "No filters"
            		} else {

            			return desc;

            		}
                }

                scope.$watch("ngModel", function(newValue, oldValue) {
                    if (scope.colors && scope.ngModel) {
                        scope.colorize();
                    }
                });

            }
        };
    })

})();