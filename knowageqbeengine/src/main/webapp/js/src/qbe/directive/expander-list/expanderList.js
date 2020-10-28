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
.directive('qbeExpanderList', function($sce, $rootScope, sbiModule_action, sbiModule_inputParams, sbiModule_messaging, sbiModule_translate ) {
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

                scope.dragEnabled = scope.dragAction ? true : false;
                scope.childrenName = (scope.childrenName == undefined) ? "children" : scope.childrenName;
                scope.displayPropertyName = (scope.displayPropertyName == undefined) ? "text" : scope.displayPropertyName;
                scope.translate = sbiModule_translate;
                //optional colorizing function to create the colored squares on the view. If no colors are given the blocks disappear.
                scope.colorize = function() {
                	scope.usedColorIndex = 0;
                    for (var k in scope.ngModel) {
                        if (scope.ngModel.hasOwnProperty(k)) {
                            for (var j in scope.ngModel[k]) {
                                if(!scope.colors[scope.usedColorIndex]) scope.usedColorIndex = 0;
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

                scope.countFilters = function (field) {
                	var filt = 0;
                	for (var i = 0; i < scope.$parent.filters.length; i++) {
            			if(scope.$parent.filters[i].leftOperandDescription == field.attributes.entity+" : "+field.text){
            				filt++;
            			}
            		}
                	return filt;
                }

                scope.getIconCls = function (field){
                	var icon = scope.fontIcons + ' ';
                	switch (field.iconCls) {
                	  case 'measure':
                		icon += 'fa-ruler';
                	    break;
                	  case 'cube':
                		icon += 'fa-cube';
                		break;
                	  case 'calculation':
                  		icon += 'fa-calculator';
                  		break;
                	  case 'dimension':
                  		icon += 'fa-ruler-horizontal';
                  		break;
                	  case 'geographic dimension':
                		  icon += 'fa-map-marked-alt';
                		  break;
                	  case 'attribute':
                		icon += 'fa-font';
                		break;
                	  case 'generic':
                		icon += 'fa-layer-group';
                		break;
                	  default:
                	    icon += 'fa-cube';
                	    break;
                	}
                	return icon;
                }

                scope.countHavings = function (field) {
                	var hav = 0;
                	for (var i = 0; i < scope.$parent.havings.length; i++) {
            			if(scope.$parent.havings[i].leftOperandDescription == field.attributes.entity+" : "+field.text){
            				hav++;
            			}
            		}
                	return hav;
                }

                scope.countAll = function (field) {
                	var filt = scope.countFilters(field);
                	var hav = scope.countHavings(field);
                	var total = filt + hav;

                	return total;
                }

                scope.isThereAFilter = function (entity) {
                	if(entity.children){
                		for (var i = 0; i < entity.children.length; i++) {
                    		if(scope.countFilters(entity.children[i])>0){
                				return true;
                			} else {
                				return false;
                			}
                		}
                	}

                }



                //broadcast field to root scope
                scope.moveFieldByClick = function (item) {
                	if(item.hasOwnProperty('iconCls')){
                		$rootScope.$broadcast('addFieldOnClick', item);
                	} else {
                		item.expanded=!item.expanded;
                	}

                }

                scope.passEntityColorToTemporalFields = function (entity) {
                	for (var i = 0; i < entity.children.length; i++) {
						if(!entity.children[i].leaf){
							for (var j = 0; j < entity.children[i].children.length; j++) {
								if(entity.children[i].children[j].leaf) {
									if(!entity.children[i].children[j].hasOwnProperty('color')){
										entity.children[i].children[j].color=entity.color;
									}
									if(!entity.children[i].children[j].hasOwnProperty('temporal')){
										entity.children[i].children[j].temporal=true;
									}
									if(!entity.children[i].children[j].attributes.hasOwnProperty('longDescription')){
										entity.children[i].children[j].attributes.longDescription=""+entity.children[i].text+" : "+ entity.children[i].children[j].text+"";
									}
								}
							}
						}
					}
                }

                scope.setDefaultHierarchy = function (field, entityId) {
                	if(!field.isDefault) {


                    	item = {};
                    	item.fieldId = field.id;
                    	item.entityId = entityId;

                    	queryParam = {};

                		conf = {};
                		conf.headers = {'Content-Type': 'application/x-www-form-urlencoded'},

        				conf.transformRequest = function(obj) {

        					var str = [];

        					for(var p in obj)
        						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));

        					return str.join("&");

        				}

                		sbiModule_action.promisePost('SET_DEFAULT_HIERARCHY_ACTION',queryParam,item, conf)
            			.then(function(response) {
            				console.log("[POST]: SUCCESS!");
            				var entities = scope.ngModel.entities;
            				for (var i = 0; i < entities.length; i++) {
								if(entities[i].id==entityId) {
									for (var j = 0; j < entities[i].children.length; j++) {
										if(entities[i].children[j].hasOwnProperty('cls')){
											delete entities[i].children[j].cls;
											if(entities[i].children[j].attributes.isdefault==true){
												entities[i].children[j].attributes.isdefault=false;
											}
										}
									}
								}
							}

            				field.cls='default_hierarchy';
            				if(!field.attributes.isdefault) {
            					field.attributes.isdefault=true;
            				}

            				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("kn.qbe.hierarchies.setdefault.success"), 'Success!');
            			}, function(response) {
            				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

            			});
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