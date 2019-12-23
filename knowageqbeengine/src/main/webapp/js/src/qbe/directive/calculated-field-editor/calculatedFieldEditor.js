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

angular.module('qbe_calculated_field_editor', ['ngSanitize', 'ui.codemirror'])

.filter('dateFormat', function($mdDateLocale) {



		return function(item) {


			var formatted = item;
			var date = new Date().toDateString();


				return $mdDateLocale.formatDate(new Date(date),item)


			return formatted;
		};
	})

.directive('qbeCalculatedFieldEditor', function($sce, $mdDialog, $mdMenu, $mdSidenav, $timeout,sbiModule_translate,formatDate) {
    return {
        restrict: 'E',
        scope: {
            entities: "=",
            fields: "=",
            functions: "=",
            selectedEntity: "=?",
            calculatedField: "=?"
        },
        templateUrl: currentScriptPath +'calculatedFieldEditor.html',
        replace: true,
        controller: function($scope, $attrs) {
        	$scope.translate = sbiModule_translate;
            $scope.availableFormulaTypes = [];
            $scope.availableTypes = ['NUMBER','STRING','DATE'];
            $scope.availableNatures = ['MEASURE','ATTRIBUTE'];
            $scope.dateFormats = formatDate.dateFormats;
            $scope.calculatedField.fieldType = "inline.calculated.field";

            //fix for codemirror to refresh when opened
            $timeout(function() {
                $scope.reloadCodemirror = true;
            }, 500)

            $scope.typeChanged = function(calculatedField){
            	if(calculatedField.type!=='DATE')
            		calculatedField.format = undefined;
            }

         //   if (!$scope.selectedEntity) $scope.selectedEntity = $scope.entities[0];

            angular.forEach($scope.functions, function(value, key) {
                if ($scope.availableFormulaTypes.indexOf(value.type) === -1) $scope.availableFormulaTypes.push(value.type);
            });

            //codemirror initializer
            $scope.codemirrorLoaded = function(_editor) {
                $scope._doc = _editor.getDoc();
                $scope._editor = _editor;
                _editor.focus();

                $scope._doc.markClean()

                _editor.on("beforeChange", function() {});
                _editor.on("change", function() {});
            };

            //codemirror options
            $scope.editorOptions = {
                theme: 'eclipse',
                lineWrapping: true,
                lineNumbers: true,
                mode: 'customMode',
                onLoad: $scope.codemirrorLoaded
            };

            $scope.addTextInCodemirror = function(text) {
                $scope._editor.focus();
                var position = $scope._editor.getCursor();
                var line = $scope._editor.getLine(position.line);
                $scope._editor.replaceRange(text, position);
            }

            //add text to the editor
            $scope.addField = function(field) {
                var text = field.html;
                text = '$F{' + field.field + '}';
                var suffix = "";
                var prefix = "";
                $scope._editor.focus();
                if ($scope._editor.somethingSelected()) {
                    $scope._editor.replaceSelection(prefix + text + suffix);
                    return
                }
                var position = $scope._editor.getCursor();
                var line = $scope._editor.getLine(position.line);
                if (line.charAt(position.ch - 1) == '}') {
                    prefix = ',';
                }
                $scope.addTextInCodemirror(prefix + text + suffix);
            }

            $scope.addFormula = function(formula) {
                if (Array.isArray(formula.arguments) && formula.arguments.length >1) {
                    $scope.toggleFunctionWizard();
                    $scope.selectedFunction = formula;
                    if ($scope.selectedFunction.temp) delete $scope.selectedFunction.temp;
                    return;
                }
                $scope.addTextInCodemirror(formula.body);
            }

            //add text to the editor with the function wizard
            $scope.addFromFunction = function(funct) {
                var str = funct.body;
                angular.forEach(funct.arguments, function(value, key) {
                	var regString = '\\$\\{('+value.placeholder+')\\}'
                    var regex = new RegExp(regString,"g");
					if(value.type && value.type.toLowerCase()=='field'){
						str = str.replace(regex, '$F{'+funct.temp[value.name]+'}');
					} else if(value.type && value.type.toLowerCase()=='string'){
						str = str.replace(regex, "'"+funct.temp[value.name]+"'");
					} else {
						str = str.replace(regex, funct.temp[value.name]);
					}

                });
                $scope.toggleFunctionWizard();
                $scope.addTextInCodemirror(str);
            }

            //open function wizard
            $scope.toggleFunctionWizard = function() {
                $mdSidenav('functionWizard').toggle();
            }

            //change selected entity for the fields list
          /*  $scope.changeSelectedEntity = function() {
                $scope.calculatedField.expression = "";
            }*/

            //watch the expression values to set id instead of fields names
            $scope.$watch('calculatedField.expression',function(newValue,oldValue){
            	if($scope.calculatedField && newValue!=oldValue){
            		if( $scope.calculatedField.formula != newValue){
            			$scope.calculatedField.formula = newValue;
            			//regex to get the number of fields occurrencies
            			var fullRegex = /(\$F\{[a-zA-Z0-9\s\-\>]*\}){1}/g;
            			//regex to get the single field
            			var regEx = /(\$F\{[a-zA-Z0-9\s\-\>]*\}){1}/;
            			//regex to divide the single fields match into groups
            			var regExGroups = /(\$F\{)([a-zA-Z0-9\s\-\>]*)(\}){1}/;
            			if($scope.calculatedField.formula.match(fullRegex)){
            				var fieldsNum = $scope.calculatedField.formula.match(fullRegex).length;
            			}
            			for(var i=0;i<fieldsNum;i++){
            				var tempReplace = "";
            				var match = regExGroups.exec($scope.calculatedField.formula);
            				//get the id of the field from the label
                			angular.forEach($scope.fields, function(value, key) {
                				if(value.alias==match[2]){
                					tempReplace = value.id;
                					return;
                				}
                			});
                			$scope.calculatedField.formula = $scope.calculatedField.formula.replace(regEx,tempReplace);
            			}
            		}
            	}
            });

        }
    };
})
})();