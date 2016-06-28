/**
 *
 */

function addBusinessViewController($scope,sbiModule_translate,physicalModel){
	$scope.translate=sbiModule_translate;
	$scope.physicalModel=physicalModel;

	$scope.tmpBnssView={physicalModels:[]};
	$scope.bvTableColumns=[{label:sbiModule_translate.load("sbi.generic.name"),name:"name"}];


	$scope.sourceTable;
	$scope.targetTable;

	$scope.dragOptionsFunct={
			dropEnd:function(ev,source,target){
			},
			beforeDrop:function(ev,source,target){
				return true;
			},
			accept:function(ev,target){
				return true;
			}

	}



	$scope.create = function() {
		 alert("save")
		    $mdDialog.hide();
		};
	  $scope.cancel = function() {
	    $mdDialog.cancel();
	  };
}