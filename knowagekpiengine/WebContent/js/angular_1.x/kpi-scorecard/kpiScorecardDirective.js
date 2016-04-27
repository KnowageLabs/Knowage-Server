(function() {

var scripts = document.getElementsByTagName("script");
var currentScriptPath = scripts[scripts.length - 1].src;
currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

var kpiScorecardApp = angular.module('kpiScorecardModule', ['ngMaterial','expander-box','sbiModule','kpi_semaphore_indicator']);
kpiScorecardApp.directive("kpiScorecard",function(){
		return {
			//restrict: 'E',
			templateUrl: currentScriptPath + 'template/kpi-scorecard.jsp',
			controller: kpiScorecardController,
			scope: {
				scorecard:"="
			}
		};
	})
	;
function kpiScorecardController($scope, sbiModule_translate){
	$scope.translate = sbiModule_translate;
//	do{}
//	while ($scope.scorecard.scorecard == undefined)
	$scope.scorecardTarget = $scope.scorecard;
};

})();