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
(function(){

	angular.module('cockpitModule').directive('calculatedField',function(){
		return{
			template:   '<button class="md-button md-knowage-theme" ng-click="addNewCalculatedField()" ng-class="{\'md-icon-button\':selectedItem && !insideMenu}">'+
						'	<md-icon md-font-icon="fa fa-calculator" ng-if="selectedItem"></md-icon>'+
						'	<span ng-if="!selectedItem">{{::translate.load("sbi.cockpit.widgets.table.calculatedFields.add")}}</span>'+
						'	<span ng-if="selectedItem && insideMenu">{{::translate.load("sbi.cockpit.widgets.table.calculatedFields.edit")}}</span>'+
						'	<md-tooltip ng-if="selectedItem && !insideMenu" md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.calculatedFields.edit")}}</md-tooltip>'+
						'</button>',
			replace: true,
			scope:{
				ngModel: "=",
				selectedItem : "=?",
				callbackUpdateGrid : "&?",
				callbackUpdateAlias : "&?",
				insideMenu : "=?",
				additionalInfo: "=?",
				// A function with no params that return the list
				// of available features
				measuresListFunc : "&?",
				// A function with the new CF to add to the list
				// of fields
				callbackAddTo : "&?"
			},
			controller: calculatedFieldController,
		}
	});

	function calculatedFieldController($scope,sbiModule_translate,$q,$mdDialog,cockpitModule_datasetServices,$mdToast){

		$scope.translate = sbiModule_translate;
		if($scope.selectedItem){

			if ($scope.measuresListFunc != undefined) {
//				var tmpList = $scope.measuresListFunc();
//				$scope.currentRow = tmpList[$scope.selectedItem];
				$scope.currentRow = $scope.selectedItem;

			} else if ( $scope.ngModel.content == undefined) {  // case when coming from chart widget
				$scope.currentRow = $scope.ngModel.columnSelectedOfDatasetAggregations[$scope.selectedItem];
			} else {
				$scope.currentRow = $scope.ngModel.content.columnSelectedOfDataset[$scope.selectedItem]
			}


		}
		$scope.addNewCalculatedField = function(){

			var deferred = $q.defer();
			var promise ;
			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitCalculatedFieldTemplate.html',
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {
					promise: deferred,
					model:$scope.ngModel,
					actualItem : $scope.currentRow,
					callbackUpdateGrid: $scope.callbackUpdateGrid,
					callbackUpdateAlias: $scope.callbackUpdateAlias,
					additionalInfo: $scope.additionalInfo,
					measuresListFunc: $scope.measuresListFunc,
					callbackAddTo: $scope.callbackAddTo
				},
				//fullscreen: true,
				controller: calculatedFieldDialogController
			}).then(function() {
				deferred.promise.then(function(result){
					if($scope.currentRow != undefined){
						if($scope.callbackUpdateAlias) {
							$scope.callbackUpdateAlias({newAlias: result.alias, oldAlias: $scope.currentRow.alias});
						}
						$scope.currentRow.name = result.alias;
						$scope.currentRow.aliasToShow = result.alias;
						$scope.currentRow.formula = result.formula;
						$scope.currentRow.formulaEditor = result.formulaEditor;
						$scope.currentRow.aggregationSelected = result.aggregationSelected;
						$scope.currentRow.funcSummary = result.funcSummary;
						$scope.currentRow.alias = result.alias;
					}else{
						if ($scope.callbackAddTo != undefined) {
							$scope.callbackAddTo({newItem: result});
						} else if ($scope.ngModel.content == undefined) {
							$scope.ngModel.columnSelectedOfDatasetAggregations.push(result);
						} else {
							$scope.ngModel.content.columnSelectedOfDataset.push(result);
						}
					}
					if($scope.callbackUpdateGrid){
						$scope.callbackUpdateGrid();
					}
				});
			}, function() {
			});
			promise =  deferred.promise;

		}
	}

	function calculatedFieldDialogController($scope,sbiModule_translate,cockpitModule_template,sbiModule_restServices,$mdDialog,$q,promise,model,actualItem,callbackUpdateGrid,callbackUpdateAlias,additionalInfo,measuresListFunc,callbackAddTo,cockpitModule_datasetServices,cockpitModule_generalOptions,$timeout, cockpitModule_properties){
		$scope.translate=sbiModule_translate;
		$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
		$scope.model = model;
		$scope.callbackUpdateGrid = callbackUpdateGrid;
		$scope.callbackUpdateAlias = callbackUpdateAlias;
		$scope.callbackAddTo = callbackAddTo;
		$scope.localDataset = {};
		$scope.calculatedField = actualItem ? angular.copy(actualItem) : {};
		if(!$scope.calculatedField.aggregationSelected) $scope.calculatedField.aggregationSelected = 'NONE';
		if($scope.calculatedField.formula && !$scope.calculatedField.formulaEditor) $scope.calculatedField.formulaEditor = $scope.calculatedField.formula;

		$scope.setVariableFunction = function(variable){
			return {
			      "syntax":"$V{ "+variable.name+" }",
			      "description":variable.name,
			      "body":"$V{"+variable.name+"}",
			      "name": variable.name,
			      "output":"Number",
			      "type":"variables"
			   };
		}

		//premade functions for codemirror menu bar
		$scope.functions = angular.copy(cockpitModule_generalOptions.calculatedFieldsFunctions);
		angular.forEach(cockpitModule_template.configuration.variables, function(value, key) {
			$scope.functions.push($scope.setVariableFunction(value));
		})

		$scope.availableFormulaTypes = [];
		
		$scope.checkFormulaAvailability = function(formula){
				
			if(formula.exclude && formula.exclude.indexOf(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId).type) != -1) return false;				
				
			if(formula.type == $scope.translate.load("kn.cockpit.functions.type.functions") && additionalInfo){
				if(additionalInfo.availableFunctions.lenght > 0 && additionalInfo.availableFunctions.indexOf(formula.name) === -1) return false;
			}
			return true;
		}

		if(additionalInfo && additionalInfo.nullifFunction && additionalInfo.nullifFunction.length > 0) $scope.nullifWarningLabel = additionalInfo.nullifFunction[0];

		angular.forEach($scope.functions, function(value, key) {
			if(value.type == $scope.translate.load("kn.cockpit.functions.type.functions")){
				if(additionalInfo && additionalInfo.availableFunctions && additionalInfo.availableFunctions.length != 0){
					if ($scope.availableFormulaTypes.indexOf(value.type) === -1 && $scope.checkFormulaAvailability(value)) {
							$scope.availableFormulaTypes.push(value.type);
						}
				}
			} else if ($scope.availableFormulaTypes.indexOf(value.type) === -1 && $scope.checkFormulaAvailability(value)) {
					$scope.availableFormulaTypes.push(value.type);
				}
		});

		
		//codemirror initializer
		$scope.reloadCodemirror = false;
		$scope.codemirrorLoaded = function(_editor) {
			$scope._doc = _editor.getDoc();
			$scope._editor = _editor;
			_editor.focus();
			$scope._doc.markClean()
			_editor.on("beforeChange", function() {});
			_editor.on("change", function() {});
		};

		$scope.$watch('calculatedField.formulaEditor', function(newValue,oldValue){
			if(newValue && newValue.match("/") && $scope.nullifWarningLabel){
				$scope.showWarning = $scope.translate.load('kn.cockpit.calculatedfield.validation.division').replace("{0}", $scope.nullifWarningLabel);
			}else {
				$scope.showWarning = false;
			}
		})

		//codemirror options
		$scope.editorOptions = {
				theme: 'eclipse',
				lineWrapping: true,
				lineNumbers: true,
				mode: 'calculatedFieldMode',
				onLoad: $scope.codemirrorLoaded
		};

		$scope.addTextInCodemirror = function(text) {
			$scope._editor.focus();
			var position = $scope._editor.getCursor();
			var line = $scope._editor.getLine(position.line);
			$scope._editor.replaceRange(text, position);
		}

		if($scope.calculatedField.formulaEditor) {
			$timeout(function(){
				$scope.reloadCodemirror = true;
			},0)
		}

		$scope.addFormula = function(formula) {
			$scope.addTextInCodemirror(formula.body);
		}

		$scope.toastifyMsg = function(type,msg){
			Toastify({
				text: msg,
				duration: 10000,
				close: true,
				className: 'kn-' + type + 'Toast',
				stopOnFocus: true
			}).showToast();
		}

		$scope.validateFormula = function(save) {
			return $q(function(resolve, reject) {
				$scope.calculatedField.formula =  $scope.calculatedField.formulaEditor.replace(/\$V\{([a-zA-Z0-9\-\_]*){1}(?:.([a-zA-Z0-9\-\_]*){1})?\}/g,function(match,p1,p2){
					return p2 ? cockpitModule_properties.VARIABLES[p1][p2] : cockpitModule_properties.VARIABLES[p1];
				})
				if(!$scope.calculatedField.formulaEditor) {
					if(!save) $scope.toastifyMsg('warning',$scope.translate.load("kn.cockpit.calculatedfield.validation.error.noformula"));
					reject();
					return;
				}
				if($scope.model && $scope.model.type == "static-pivot-table") {
					if(!containsAggregation($scope.calculatedField.formula)) {
						$scope.toastifyMsg('warning',$scope.translate.load("kn.cockpit.calculatedfield.validation.error.crosstab.noaggregations"));
						reject();
						return;
					}
				}
				$scope.formulaLoading = true;
				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.promisePost('2.0/datasets','validateFormula',{
					"formula": $scope.calculatedField.formula.trim(),
					"measuresList" : $scope.measuresList
				})
				.then(function(response){
					if(!save) $scope.toastifyMsg('success',$scope.translate.load("kn.cockpit.calculatedfield.validation.success"));
					$scope.formulaLoading = false;
					resolve();
				},function(response){
					if(!save) $scope.toastifyMsg('warning',$scope.translate.load(response.data.errors[0].message));
					$scope.formulaLoading = false;
					reject(response.data.errors[0].message);
				})
			})
		}

		containsAggregation = function(formula) {
			aggregations = cockpitModule_generalOptions.aggregationFunctions;
			var aggregationFound = false;
			for (var i=0; i<aggregations.length; i++) {
				var aggr = aggregations[i].value;
				if(formula.indexOf(aggr) !== -1) {
					aggregationFound = true;
					break;
				}
			}
			return aggregationFound;
		}

		$scope.addMeasures = function(field) {
			var text = field.name;

			var	prefix = '"';
			var	suffix = '"';


			$scope._editor.focus();
			if ($scope._editor.somethingSelected()) {
				$scope._editor.replaceSelection(prefix + text + suffix);
				return
			}
			var position = $scope._editor.getCursor();
			$scope.addTextInCodemirror(prefix + text + suffix);
		}

		if($scope.model.dataset.dsId != undefined){
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
		}

		$scope.measuresList = [];
		$scope.datasetColumnsList = [];

		for(var i=0;i<$scope.localDataset.metadata.fieldsMeta.length;i++){
			var obj = $scope.localDataset.metadata.fieldsMeta[i];
			if(obj.fieldType == 'MEASURE' && !obj.isCalculated){
				$scope.datasetColumnsList.push(obj);
			}
		}

		if (measuresListFunc != undefined) {

			var tmpList = measuresListFunc();
			for (var i in tmpList) {
				var obj = tmpList[i];
				if(obj.fieldType == 'MEASURE' && !obj.isCalculated){
					$scope.measuresList.push(obj);
				}
			}
		} else if ($scope.model.content == undefined) {

			for(var i in $scope.model.columnSelectedOfDatasetAggregations){
				var obj = $scope.model.columnSelectedOfDatasetAggregations[i];
				if(obj.fieldType == 'MEASURE' && !obj.isCalculated){
					$scope.measuresList.push(obj);
				}
			}
		} else {
			for(var i in $scope.model.content.columnSelectedOfDataset){
				var obj = $scope.model.content.columnSelectedOfDataset[i];
				if(obj.fieldType == 'MEASURE' && !obj.isCalculated){
					$scope.measuresList.push(obj);
				}
			}
		}
		$scope.saveColumnConfiguration=function(){
			if($scope.aliasForm.alias.$valid){
				$scope.validateFormula(true)
				.then(function(success){
					if(!$scope.calculatedField.alias){
						$scope.toastifyMsg('warning',$scope.translate.load("kn.cockpit.calculatedfield.validation.error.noalias"));
						return;
					}
					$scope.result = angular.copy($scope.calculatedField);
					if(!$scope.result.aggregationSelected) $scope.result.aggregationSelected = 'NONE';
					$scope.result.funcSummary = $scope.result.aggregationSelected == 'NONE' ? 'SUM' : $scope.result.aggregationSelected;
					$scope.result.aliasToShow = $scope.result.alias;
					$scope.result.name = $scope.result.alias;
					$scope.result.fieldType = 'MEASURE';
					$scope.result.isCalculated = true;
					$scope.result.type = "java.lang.Double";
					promise.resolve($scope.result);
					$mdDialog.hide();

				},function(error){
					$scope.toastifyMsg('warning',$scope.translate.load(error));
					return;
				})
			}else $scope.toastifyMsg('warning',$scope.translate.load("kn.cockpit.calculatedfield.validation.error.invalidalias"));
		}
		$scope.cancelConfiguration=function(){
			$mdDialog.cancel();
		}

		$scope.resetFormula = function(){
			$scope.calculatedField.formulaEditor = '';
			$scope.calculatedField.aggregationSelected = 'NONE';
		}

		$scope.isSolrDataset = function() {
			if($scope.model.dataset.dsId != undefined) {
				if (cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId).type == "SbiSolrDataSet") {
					return true;
				}

			}
			return false;
		}
	}

})();