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
			template: '<span><button ng-if="!selectedItem" class="md-button md-knowage-theme" ng-click="addNewCalculatedField()">{{translate.load("sbi.cockpit.widgets.table.calculatedFields.add")}}</button>'+
			'<md-button ng-if="selectedItem" class="md-icon-button" ng-click="addNewCalculatedField()">'+
			'<md-icon md-font-icon="fa fa-calculator"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.inlineCalculatedFields.title")}}</md-tooltip></md-button><span>',
			replace: true,
			scope:{
				ngModel:"=",
				selectedItem : "=?"
			},
			controller: calculatedFieldController,
		}
	});

	function calculatedFieldController($scope,sbiModule_translate,$q,$mdDialog,cockpitModule_datasetServices,$mdToast){

		$scope.translate = sbiModule_translate;
		if($scope.selectedItem){$scope.currentRow = $scope.ngModel.content.columnSelectedOfDataset[$scope.selectedItem]}
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
					actualItem : $scope.currentRow
				},
				//fullscreen: true,
				controller: calculatedFieldDialogController
			}).then(function() {
				deferred.promise.then(function(result){
					if($scope.currentRow != undefined){
						$scope.currentRow.aliasToShow = result.alias;
						$scope.currentRow.formula = result.formula;
						$scope.currentRow.formulaEditor = result.formulaEditor;
						$scope.currentRow.aggregationSelected = result.aggregationSelected;
						$scope.currentRow.funcSummary = result.funcSummary;
						$scope.currentRow.datasetOrTableFlag = result.datasetOrTableFlag;
						$scope.currentRow.alias = result.alias;
					}else{
						$scope.ngModel.content.columnSelectedOfDataset.push(result);

					}
				});
			}, function() {
			});
			promise =  deferred.promise;

		}
	}

	function calculatedFieldDialogController($scope,sbiModule_translate,cockpitModule_template,sbiModule_restServices,$mdDialog,$q,promise,model,actualItem,cockpitModule_datasetServices,cockpitModule_generalOptions,$timeout, cockpitModule_properties){
		$scope.translate=sbiModule_translate;
		$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
		$scope.model = model;
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
		angular.forEach($scope.functions, function(value, key) {
			if ($scope.availableFormulaTypes.indexOf(value.type) === -1) $scope.availableFormulaTypes.push(value.type);
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
				$scope.calculatedField.formula =  $scope.calculatedField.formulaEditor.replace(/(\$V\{)([a-zA-Z0-9\-\_\s]*)(\})/g,function(match,p1,p2){
					return cockpitModule_properties.VARIABLES[p2];
				})
				if(!$scope.calculatedField.formulaEditor) {
					$scope.toastifyMsg('warning',$scope.translate.load("kn.cockpit.calculatedfield.validation.error.noformula"));
					reject();
					return;
				}
				$scope.formulaLoading = true;
				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.promisePost('2.0/datasets','validateFormula',{
					"formula": $scope.calculatedField.formula.trim()
				})
				.then(function(response){
					if(!save) $scope.toastifyMsg('success',$scope.translate.load("kn.cockpit.calculatedfield.validation.success"));
					$scope.formulaLoading = false;
					resolve();
				},function(response){
					$scope.toastifyMsg('warning',$scope.translate.load(response.data.errors[0].message));
					$scope.formulaLoading = false;
					reject(response.data.errors[0].message);
				})
			})
		}

		$scope.addMeasures = function(field) {
			var text = field.name;

			var prefix = $scope.calculatedField.datasetOrTableFlag  ? '"' : field.aggregationSelected+'("';
			var suffix = $scope.calculatedField.datasetOrTableFlag  ? '"' : '") ';

			if ($scope.isSolrDataset()) {
				prefix = '"';
				suffix = '"';
			}

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

		for(var i in $scope.model.content.columnSelectedOfDataset){
			var obj = $scope.model.content.columnSelectedOfDataset[i];
			if(obj.fieldType == 'MEASURE' && !obj.isCalculated){
				$scope.measuresList.push(obj);
			}
		}

		$scope.saveColumnConfiguration=function(){
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
				$scope.result.fieldType = 'MEASURE';
				$scope.result.isCalculated = true;
				$scope.result.type = "java.lang.Double";
				promise.resolve($scope.result);
				$mdDialog.hide();
			},function(error){
				$scope.toastifyMsg('warning',error);
				return;
			})

		}
		$scope.cancelConfiguration=function(){
			$mdDialog.cancel();
		}

		$scope.resetFormula = function(){
			$scope.calculatedField.formulaEditor = '';
			$scope.calculatedField.aggregationSelected = $scope.calculatedField.datasetOrTableFlag ? 'SUM' : 'NONE';
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