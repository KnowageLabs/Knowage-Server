/**
 *
 */

function addBusinessViewController($scope,sbiModule_translate,originalPhysicalModel,metaModelServices,$mdDialog){
	$scope.translate=sbiModule_translate;
	$scope.physicalModel=angular.extend([],originalPhysicalModel);

	$scope.tmpBnssView={physicalModels:[]};
	$scope.bvTableColumns=[{label:sbiModule_translate.load("sbi.generic.name"),name:"name"}];
	$scope.summary=[];

	$scope.sourceTable;
	$scope.targetTable;

	$scope.steps={current:0};

	$scope.dragOptionsFunct={
			dropEnd:function(ev,source,target){
				$scope.updateSummary();
			}
	}
	$scope.afterClearItem=function(item){
		$scope.updateSummary();
	}
	$scope.updateSummary=function(){
		$scope.summary=[];
		for(var i=0;i<$scope.physicalModel.length;i++){
			for(var col=0;col<$scope.physicalModel[i].columns.length;col++){
				if($scope.physicalModel[i].columns[col].hasOwnProperty("links") && $scope.physicalModel[i].columns[col].links.length>0){
					$scope.summary.push($scope.physicalModel[i].columns[col]);
//					$scope.summary.push({source:$scope.physicalModel[i].columns[col].name, target: $scope.physicalModel[i].columns[col].links[0].name});
				}
			}
		}
		if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
		    $scope.$apply();
		}
	}

	$scope.deleteRelationship=function(item){
		item.links=[];
		$scope.updateSummary();
	}


	$scope.create = function() {
		 alert("save")
		 metaModelServices.createRequestRest({})
		    $mdDialog.hide();
		};
	  $scope.cancel = function() {
	    $mdDialog.cancel();
	  };
	  $scope.next = function() {
		  $scope.steps.current=1;
	  };
	  $scope.back = function() {
		  $scope.steps.current=0;	  };
}