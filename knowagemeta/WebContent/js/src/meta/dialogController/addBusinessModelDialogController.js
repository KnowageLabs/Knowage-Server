/**
 *
 */

function addBusinessModelController($scope,$mdDialog,sbiModule_translate,businessModel,physicalModel){
	$scope.translate=sbiModule_translate;
	$scope.physicalModel=physicalModel;
	$scope.tmpBnssModel={physicalModel:{columns:[]},selectedColumns:[]};
	$scope.changePhYModel=function(){
		$scope.tmpBnssModel.selectedColumns=[];
	}
	$scope.bmTableColumns=[{label:sbiModule_translate.load("sbi.generic.name"),name:"name"}];


	 $scope.create = function() {
		 alert("save")
		    $mdDialog.hide();
		};
	  $scope.cancel = function() {
	    $mdDialog.cancel();
	  };
}