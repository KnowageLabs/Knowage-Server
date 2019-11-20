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
                    $scope.currentRow.formulaArray = result.formulaArray;
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

function calculatedFieldDialogController($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,promise,model,actualItem,cockpitModule_datasetServices,$mdToast,cockpitModule_generalOptions){
	$scope.translate=sbiModule_translate;
	$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
	$scope.model = model;
	$scope.localDataset = {};
	$scope.formula = "";
	$scope.formulaElement = [];

	$scope.functions = cockpitModule_generalOptions.calculatedFieldsFunctions;
	/*$scope.functions.push({
	      "syntax":"VariabileTest",
	      "description":"no description.",
	      "body":"$V{variabileTest}",
	      "name":"VariabileTest",
	      "output":"Number",
	      "type":"variable"
	   })
	*/
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

    $scope.addFormula = function(formula) {
        $scope.addTextInCodemirror(formula.body);
    }
    $scope.validateFormula = function() {
    	sbiModule_restServices.restToRootProject();
    	sbiModule_restServices.promisePost('2.0/datasets','validateFormula',{
    		"formula": $scope.formula.trim()
    	})
    	.then(function(response){

    	},function(error){

    	})
    }

    $scope.addMeasures = function(field) {
        var text = field.name;
        var suffix = $scope.column.datasetOrTableFlag ? ' ' : ') ';
        var prefix = $scope.column.datasetOrTableFlag ? '' : field.aggregationSelected+'(';
        $scope._editor.focus();
        if ($scope._editor.somethingSelected()) {
            $scope._editor.replaceSelection(prefix + text + suffix);
            return
        }
        var position = $scope._editor.getCursor();
//        var line = $scope._editor.getLine(position.line);
//        if (line.charAt(position.ch - 1) == '}') {
//            prefix = ' ';
//        }
        $scope.addTextInCodemirror(prefix + text + suffix);
    }

	if($scope.model.dataset.dsId != undefined){
		angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
	}

	$scope.column = {};
	$scope.measuresList = [];
    $scope.datasetColumnsList = [];
	//$scope.operators = ['+','-','*','/'];
	//$scope.brackets = ['(',')'];

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


	/*$scope.checkInput=function(event){
		console.log(event);
		if(event.key == "Backspace"){
			event.preventDefault();
			$scope.deleteLast();
		}
		else if(event.key=="+" || event.key=="-" || event.key=="/" ||  event.key=="*" ){
			event.preventDefault();
			$scope.addOperator(event.key);
		}else if (event.char=="+" || event.char=="-" || event.char=="/" ||  event.char=="*"){
			//internet explorer
			event.preventDefault();
			$scope.addOperator(event.char);
		}
		else if(event.key=="(" || event.key==")" ){
			event.preventDefault()
			$scope.addBracket(event.key);
		}else if(event.char=="(" || event.char==")"){
			event.preventDefault()
			$scope.addBracket(event.char);
		}

		var reg = new RegExp("[0-9\.\,]+");
		if(reg.test(event.key)){
			if($scope.formulaElement.length>0){
				var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
				if(lastObj.type=='measure'){
					$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula3'));
					event.preventDefault();
					return;
				}
			}
			var obj = {};
			obj.type = 'number';
			obj.value = event.key;
			$scope.formulaElement.push(obj);
			$scope.formula = $scope.formula +""+event.key+"";
			event.preventDefault();
		} else {
			event.preventDefault();
		}
	}*/

	$scope.reloadValue = function(){
		$scope.formulaElement = angular.copy(actualItem.formulaArray);
		$scope.column.alias = angular.copy(actualItem.aliasToShow);
		$scope.column.aggregationSelected = actualItem.aggregationSelected && angular.copy(actualItem.aggregationSelected);
		$scope.column.datasetOrTableFlag = actualItem.datasetOrTableFlag ? angular.copy(actualItem.datasetOrTableFlag) : false;
		//$scope.redrawFormula();
	}

	$scope.checkAggregation = function(obj){
		for(var i in $scope.measuresList){
			if($scope.measuresList[i].alias == obj.value) {
				obj.aggregation = $scope.measuresList[i].aggregationSelected;
				return $scope.measuresList[i].aggregationSelected;
			}
		}
	}

	$scope.saveColumnConfiguration=function(){
		if($scope.formulaElement.length>0){
			var obj = $scope.formulaElement[$scope.formulaElement.length-1];
			if(obj.type=='operator'){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula1'));
				return;
			}
		}
		else{
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula0'));
			return;
		}
		if(!$scope.checkBrackets()){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula5'));
			return;
		}

		$scope.result = {};
		$scope.result.alias = $scope.column.alias != undefined ? $scope.column.alias : "NewCalculatedField";
		$scope.result.formulaArray = $scope.formulaElement;
		$scope.result.formula = $scope.formula;
		$scope.result.aggregationSelected = $scope.column.aggregationSelected || 'NONE';
		$scope.result.funcSummary = $scope.result.aggregationSelected == 'NONE' ? 'SUM' : $scope.result.aggregationSelected;
		$scope.result.datasetOrTableFlag = $scope.column.datasetOrTableFlag;
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
		//$scope.formulaElement = [];
		$scope.column.aggregationSelected = $scope.column.datasetOrTableFlag ? 'SUM' : 'NONE';
	}

	/*$scope.checkBrackets = function(){
		var countOpenBrackets = 0;
		var countCloseBrackets = 0;
		for(var i=0;i<$scope.formulaElement.length;i++){
			var obj = $scope.formulaElement[i];
			if(obj.type == 'bracket'){
				if(obj.value == '('){
					countOpenBrackets++;
				} else {
					countCloseBrackets++;
				}
			}
		}

		if(countOpenBrackets != countCloseBrackets){
			return false;
		}
		return true;
	}
	$scope.addOperator= function(op){
		if($scope.formulaElement.length==0){
			$scope.showAction('Select a measure before.');
			return;
		}
		var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
		if(lastObj.type=='operator'){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula2'));
			return;
		}
		var obj = {};
		obj.type = 'operator';
		obj.value = op;
		$scope.formulaElement.push(obj);
		$scope.formula += op+" ";
	}
	$scope.addBracket= function(br){

		var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
		if(lastObj !=undefined && lastObj.type=='measure' && br == '('){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula4'));
			return;
		}
		if(lastObj !=undefined && lastObj.type=='operator' && br == ')'){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula4'));
			return;
		}
		var obj = {};
		obj.type = 'bracket';
		obj.value = br;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +" "+br+" ";
	}
	$scope.addMeasures =function(meas){
		if($scope.formulaElement.length>0){
			var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
			if(lastObj.type=='measure' || lastObj.type=='number'){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula3'));
				return;
			}
		}
		var obj = {};
		obj.type = 'measure';
		obj.value = meas.alias;
		$scope.formulaElement.push(obj);
		if(meas.aggregationSelected) obj.aggregation = meas.aggregationSelected;
		if(!$scope.column.datasetOrTableFlag && meas.aggregationSelected != 'NONE') $scope.formula += meas.aggregationSelected+'("'+meas.alias+'") ';
		else $scope.formula += '"'+meas.alias+'" ';
	}
	$scope.deleteLast = function(){
		if($scope.formulaElement.length>0){
			$scope.formulaElement.pop();
			$scope.redrawFormula();
		}
	}
	$scope.redrawFormula = function(){
		$scope.formula = "";
		for(var i=0;i<$scope.formulaElement.length;i++){
			var obj = $scope.formulaElement[i];
			if(obj.type=="number"){
				$scope.formula += obj.value + " ";
			}else if(obj.type=="measure"){
				if(!$scope.column.datasetOrTableFlag && obj.aggregation && $scope.checkAggregation(obj) != 'NONE') {
				   	$scope.formula += $scope.checkAggregation(obj) +'("'+obj.value+'") ';
				}
				else $scope.formula += '"'+obj.value+'" ';
			}else{
				$scope.formula += obj.value+" ";
			}

		}
	}*/
	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	}
	if(actualItem !=undefined){
		$scope.reloadValue();
	}
}

})();