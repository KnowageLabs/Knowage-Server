/**
 *
 */

function addBusinessViewController($scope,sbiModule_translate,physicalModel){
	$scope.translate=sbiModule_translate;
	$scope.physicalModel=physicalModel;


	 $scope.create = function() {
		 alert("save")
		    $mdDialog.hide();
		};
	  $scope.cancel = function() {
	    $mdDialog.cancel();
	  };
}