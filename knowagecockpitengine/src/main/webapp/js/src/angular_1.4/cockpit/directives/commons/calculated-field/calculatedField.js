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

	function calculatedFieldDialogController($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,promise,model,actualItem,cockpitModule_datasetServices,cockpitModule_generalOptions,$timeout){
		$scope.translate=sbiModule_translate;
		$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
		$scope.model = model;
		$scope.localDataset = {};
		$scope.alias = actualItem && actualItem.alias;
		$scope.formula = (actualItem && actualItem.formula) || "";
		$scope.datasetOrTableFlag = actualItem && actualItem.datasetOrTableFlag;
		$scope.aggregationSelected = (actualItem && actualItem.aggregationSelected) || "NONE";
		$scope.reloadCodemirror = false;

		$scope.functions = cockpitModule_generalOptions.calculatedFieldsFunctions;
		$scope.availableFormulaTypes = [];
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
	        mode: 'calculatedFieldMode',
	        onLoad: $scope.codemirrorLoaded
	    };

	    $scope.addTextInCodemirror = function(text) {
	        $scope._editor.focus();
	        var position = $scope._editor.getCursor();
	        var line = $scope._editor.getLine(position.line);
	        $scope._editor.replaceRange(text, position);
	    }

	    if($scope.formula) {
	    	$timeout(function(){
	    		$scope.reloadCodemirror = true;
	    	},0)
	    }

	    $scope.addFormula = function(formula) {
	        $scope.addTextInCodemirror(formula.body);
	    }
	    $scope.validateFormula = function() {
	    	sbiModule_restServices.restToRootProject();
	    	sbiModule_restServices.promisePost('2.0/datasets','validateFormula',{
	    		"formula": $scope.formula.trim()
	    	})
	    	.then(function(response){
	    			Toastify({
						text: "Validation Successful",
						duration: 10000,
						close: true,
						className: 'kn-successToast',
						stopOnFocus: true
					}).showToast();
	    	},function(response){
	    		Toastify({
					text: response.data.errors[0].message,
					duration: 10000,
					close: true,
					className: 'kn-warningToast',
					stopOnFocus: true
				}).showToast();
	    	})
	    }

	    $scope.addMeasures = function(field) {
	        var text = field.name;
	        var suffix = $scope.datasetOrTableFlag ? '" ' : '") ';
	        var prefix = $scope.datasetOrTableFlag ? '"' : field.aggregationSelected+'("';
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
			$scope.validateFormula();
			if($scope.formula == ""){
				Toastify({
					text: $scope.translate.load('sbi.cockpit.table.errorformula0'),
					duration: 10000,
					close: true,
					className: 'kn-warningToast',
					stopOnFocus: true
				}).showToast();
				return;
			}
			if(!$scope.alias){
				Toastify({
					text: $scope.translate.load('Enter a valid alias name'),
					duration: 10000,
					close: true,
					className: 'kn-warningToast',
					stopOnFocus: true
				}).showToast();
				return;
			}

			$scope.result = {};
			$scope.result.alias = $scope.alias != undefined ? $scope.alias : "NewCalculatedField";
			$scope.result.formula = $scope.formula;
			$scope.result.aggregationSelected = $scope.aggregationSelected || 'NONE';
			$scope.result.funcSummary = $scope.result.aggregationSelected == 'NONE' ? 'SUM' : $scope.result.aggregationSelected;
			$scope.result.datasetOrTableFlag = $scope.datasetOrTableFlag;
			$scope.result.aliasToShow = $scope.result.alias;
			$scope.result.fieldType = 'MEASURE';
			$scope.result.isCalculated = true;
			$scope.result.type = "java.lang.Integer";
			promise.resolve($scope.result);
			$mdDialog.hide();
		}
		$scope.cancelConfiguration=function(){
			$mdDialog.cancel();
		}

		$scope.resetFormula = function(){
			$scope.formula = '';
			$scope.aggregationSelected = $scope.datasetOrTableFlag ? 'SUM' : 'NONE';
		}
	}

})();