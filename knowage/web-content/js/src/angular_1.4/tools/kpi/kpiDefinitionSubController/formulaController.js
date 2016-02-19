var app = angular.module('kpiDefinitionManager').controller('formulaController', [ '$scope','sbiModule_translate' ,KPIDefinitionFormulaControllerFunction ]);

function KPIDefinitionFormulaControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.measureFormula="";
	
	$scope.codemirrorOptions = {
			mode: 'text/x-mysql',
			indentWithTabs: true,
		    smartIndent: true,
        lineWrapping : true,
        matchBrackets : true,
        autofocus: true,
        theme:"eclipse",
        lineNumbers: true, 
        extraKeys: {  "Ctrl-Space":   function(cm){  $scope.keyAssistFunc(cm) }},
        hintOptions: {tables:$scope.dataSourceTable}
		};
}
 