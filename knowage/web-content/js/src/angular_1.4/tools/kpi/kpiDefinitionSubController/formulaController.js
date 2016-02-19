var app = angular.module('kpiDefinitionManager').controller('formulaController', [ '$scope','sbiModule_translate' ,KPIDefinitionFormulaControllerFunction ]);

function KPIDefinitionFormulaControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.measureFormula="";
	$scope.currentKPI ={
			"formula": ""
	}
	
	$scope.codemirrorOptions = {
		mode: 'text/x-mathematica',
		indentWithTabs: true,
		smartIndent: true,
        lineWrapping : true,
        matchBrackets : true,
        autofocus: true,
        theme:"eclipse",
        lineNumbers: true, 
        extraKeys: {  "Ctrl-Space":   function(cm){  $scope.keyAssistFunc(cm) }},
        hintOptions: {}
		};
	
	
	$scope.keyAssistFunc=function(cm){
		
        	CodeMirror.showHint(cm, CodeMirror.hint.autocomplete);
	}
	
	
}
 