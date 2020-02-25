/**
 *
 */

function addBusinessViewController($scope,sbiModule_restServices,sbiModule_translate,originalPhysicalModel,metaModelServices,$mdDialog,selectedBusinessModel,editMode){
	$scope.translate=sbiModule_translate;
//	$scope.businessModel=businessModel;
	$scope.physicalModel=[];
	angular.copy(originalPhysicalModel,$scope.physicalModel);
	$scope.tmpBnssView={physicalModels:[]};
	$scope.bvTableColumns=[{label:sbiModule_translate.load("sbi.generic.name"),name:"name"}];
	$scope.summary=[];
	$scope.editMode=editMode;
	$scope.sourceTable;
	$scope.targetTable;
	$scope.steps={current:0};

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

	$scope.getItemIndex=function(list,name){
		for(var i=0;i<list.length;i++){
			if(angular.equals(list[i].name,name)){
				return i;
			}
		}
		return -1;
	}

	if(editMode==true){
		$scope.steps.current=1;
		//copy the physical tables
		for(var pti=0;pti<selectedBusinessModel.physicalTables.length;pti++){
			var tmppt={};
			angular.copy(originalPhysicalModel[selectedBusinessModel.physicalTables[pti].physicalTableIndex],tmppt);
			$scope.tmpBnssView.physicalModels.push(tmppt);
		}


		for(var x=0;x<$scope.tmpBnssView.physicalModels.length;x++){
			for(var y=0;y<$scope.tmpBnssView.physicalModels[x].columns.length;y++){
				$scope.tmpBnssView.physicalModels[x].columns[y].$parent=$scope.tmpBnssView.physicalModels[x];
			}
		}


		for(var i=0;i<selectedBusinessModel.joinRelationships.length;i++){
			var rel=selectedBusinessModel.joinRelationships[i];
			var destTab=$scope.tmpBnssView.physicalModels[$scope.getItemIndex($scope.tmpBnssView.physicalModels,rel.destinationTable.name)];
			var sourceTab=$scope.tmpBnssView.physicalModels[$scope.getItemIndex($scope.tmpBnssView.physicalModels,rel.sourceTable.name)];
			for(var dc=0;dc<rel.destinationColumns.length;dc++){
				var destCol= destTab.columns[$scope.getItemIndex(destTab.columns,rel.destinationColumns[dc].name)];
				var sourceCol= sourceTab.columns[$scope.getItemIndex(sourceTab.columns,rel.sourceColumns[dc].name)];
				if(!destCol.hasOwnProperty("links")){
					destCol.links=[];
				}
				destCol.links.push(sourceCol);
			}

		}

		$scope.updateSummary();
	}


	$scope.dragOptionsFunct={
			dropEnd:function(ev,source,target){
				$scope.updateSummary();
			},
			beforeDrop:function(ev,source,target){
				if(target.links){
					for(var i=0;i<target.links.length;i++){
						if(angular.equals(target.links[i].tableName,source.tableName)){
							return false;
						}
					}
				}
				return true
			}
	}


	$scope.deleteRelationship=function(item,rel){

		item.links.splice(item.links.indexOf(rel),1);
		$scope.updateSummary();
	}


	$scope.create = function() {
	var tmpData={};
	if(editMode){
		tmpData.viewUniqueName=selectedBusinessModel.uniqueName;
	}else{
		tmpData.name=$scope.tmpBnssView.name;
		tmpData.description=$scope.tmpBnssView.description;
		tmpData.sourceBusinessClass=$scope.tmpBnssView.sourceBusinessClass;
		tmpData.physicaltable=[];
	}
	tmpData.relationships={};

	for(var i=0;i<$scope.tmpBnssView.physicalModels.length;i++){
		var tmpDataObj=$scope.tmpBnssView.physicalModels[i];
		if(!editMode){
			tmpData.physicaltable.push(tmpDataObj.name);
		}
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
					if(!colObj.hasOwnProperty(tmpColObj.links[rel].tableName)){
						colObj[tmpColObj.links[rel].tableName]=[];
					}

					var targetTableObj=colObj[tmpColObj.links[rel].tableName];
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
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.businessview.create.error"));
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