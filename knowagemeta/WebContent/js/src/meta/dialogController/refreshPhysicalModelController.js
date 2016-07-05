
function refreshPhysicalModelController($scope,sbiModule_restServices,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.changedItem={};
	$scope.updateObj={selectedtable:[]};
	sbiModule_restServices.promiseGet("1.0/metaWeb","updatePhysicalModel")
	.then(
			function(response){
				angular.copy(response.data,$scope.changedItem);
				$scope.convertTables();
			},
			function(response){
				sbiModule_restServices.errorHandler(response.data,"Error while attempt to refresh physical model");
			});

	$scope.convertTables=function(){
		var missingTablesObj=[];
		for(var i=0;i<$scope.changedItem.missingTables.length;i++){
			missingTablesObj.push({name:$scope.changedItem.missingTables[i]});
		}
		angular.copy(missingTablesObj,$scope.changedItem.missingTables);

		var missingColumnsObj=[];
		for(var i=0;i<$scope.changedItem.missingColumns.length;i++){
			missingColumnsObj.push({name:$scope.changedItem.missingColumns[i]});
		}
		angular.copy(missingColumnsObj,$scope.changedItem.missingColumns);

		var removingItemsObj=[];
		for(var i=0;i<$scope.changedItem.removingItems.length;i++){
			removingItemsObj.push({name:$scope.changedItem.removingItems[i]});
		}
		angular.copy(removingItemsObj,$scope.changedItem.removingItems);
	}

	$scope.saveChange=function(){
		var dataToSend={tables:[]};
		for(var i=0;i<$scope.updateObj.selectedtable.length;i++){
			dataToSend.tables.push($scope.updateObj.selectedtable[i].name);
		}
		sbiModule_restServices.promisePost("1.0/metaWeb","updatePhysicalModel",dataToSend)
		.then(
				function(response){

				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,"Error while attempt to refresh physical model");
				});
	}
}