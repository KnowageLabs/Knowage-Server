/**
 *
 */

function addBusinessViewController($scope,sbiModule_restServices,sbiModule_translate,originalPhysicalModel,metaModelServices,$mdDialog){
	$scope.translate=sbiModule_translate;
	$scope.physicalModel=[];
	angular.copy(originalPhysicalModel,$scope.physicalModel);
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
		for(var i=0;i<$scope.tmpBnssView.physicalModels.length;i++){
			for(var col=0;col<$scope.tmpBnssView.physicalModels[i].columns.length;col++){
				if($scope.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty("links") && $scope.tmpBnssView.physicalModels[i].columns[col].links.length>0){
					$scope.summary.push($scope.tmpBnssView.physicalModels[i].columns[col]);
				}
			}
		}
		if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
		    $scope.$apply();
		}
	}

	$scope.deleteRelationship=function(item,rel){

		item.links.splice(item.links.indexOf(rel),1);
		$scope.updateSummary();
	}


	$scope.create = function() {

	var tmpData={};
	tmpData.name=$scope.tmpBnssView.name;
	tmpData.description=$scope.tmpBnssView.description;
	tmpData.physicaltable=[];
	tmpData.relationships={};

	for(var i=0;i<$scope.tmpBnssView.physicalModels.length;i++){
		var tmpDataObj=$scope.tmpBnssView.physicalModels[i];
		tmpData.physicaltable.push(tmpDataObj.name);

		for(var col=0;col<$scope.tmpBnssView.physicalModels[i].columns.length;col++){
			if($scope.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty("links") && $scope.tmpBnssView.physicalModels[i].columns[col].links.length>0){
				//check if the table is present in the relationships object
				if(!tmpData.relationships.hasOwnProperty(tmpDataObj.name)){
					tmpData.relationships[tmpDataObj.name]={};
				}

				var tabObj=tmpData.relationships[tmpDataObj.name];

				var tmpColObj=$scope.tmpBnssView.physicalModels[i].columns[col];

				//check if table has column object
				if(!tabObj.hasOwnProperty(tmpColObj.name)){
					tabObj[tmpColObj.name]={};
				}

				var colObj=tabObj[tmpColObj.name];

				for(var rel=0;rel< tmpColObj.links.length;rel++){

					//check if column  has has target table object
					if(!colObj.hasOwnProperty(tmpColObj.links[rel].$parent.name)){
						colObj[tmpColObj.links[rel].$parent.name]=[];
					}

					var targetTableObj=colObj[tmpColObj.links[rel].$parent.name];
					targetTableObj.push(tmpColObj.links[rel].name);
				}
			}
		}
	}

		 var dataToSend=metaModelServices.createRequestRest(tmpData);

		 sbiModule_restServices.promisePost("1.0/metaWeb","addBusinessView",dataToSend)
			.then(function(response){
				metaModelServices.applyPatch(response.data);
				$mdDialog.hide();
			}
			,function(response){
				sbiModule_restServices.errorHandler(response.data,"Error while create Business view");
			})


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