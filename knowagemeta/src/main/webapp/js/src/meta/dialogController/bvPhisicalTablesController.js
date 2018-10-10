angular.module('metaManager').controller('bvPhisicalTablesController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_config','metaModelServices','sbiModule_restServices',bvPhisicalTablesControllerFunction ]);
function bvPhisicalTablesControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_config,metaModelServices,sbiModule_restServices ){
	$scope.bvPhisicalTablesTabColumns=[{
											label : sbiModule_translate.load("sbi.generic.name"),
											name : "physicalTableIndex",
											transformer : function(row) {
                              					return $scope.meta.physicalModels[row].name;
                              				}
										}
									];
	$scope.bvPhisicalTablesTabFunctions={
			translate: sbiModule_translate,
			addNewPhysicalTable:function(){
				$scope.addNewPhysicalTable();
			}
	}

	$scope.bvPhisicalTablesTabActionButton=[
	                            {
								label : 'delete',
								icon:'fa fa-trash' ,
								action : function(item,event) {
									var dataToSend={
										viewUniqueName:$scope.selectedBusinessModel.uniqueName,
										physicalTable:$scope.meta.physicalModels[item.physicalTableIndex].name
									};


									sbiModule_restServices.promisePost("1.0/metaWeb", "deletePhysicalColumnfromBusinessView",metaModelServices.createRequestRest(dataToSend))
									   .then(function(response){
											metaModelServices.applyPatch(response.data);
									   },function(response){
										   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
									   })



								 }
                    			}
						];


	$scope.addNewPhysicalTable = function(){
			$mdDialog.show({
				controller: bvAddPhisicalTableControllerFunction,
				preserveScope: true,
				locals: {physicalTables:$scope.meta.physicalModels,selectedBusinessModel:$scope.selectedBusinessModel},
				templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/bvAbbPhysicalTables.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true,
				});
	}

	function bvAddPhisicalTableControllerFunction($scope,sbiModule_restServices,physicalTables,$mdDialog,sbiModule_translate,selectedBusinessModel,metaModelServices){
		$scope.translate=sbiModule_translate;
		$scope.selectedPhysicalTables=[];
		$scope.ptColumns=[{
						label : sbiModule_translate.load("sbi.generic.name"),
						name : "name",
					}
				];
		$scope.physicalTables=[];

		var usedPT=[];
		for(var i=0;i<selectedBusinessModel.physicalTables.length;i++){
			usedPT.push(selectedBusinessModel.physicalTables[i].physicalTableIndex);
		}

		for(var p=0;p<physicalTables.length;p++){
			if(usedPT.indexOf(p)==-1){
				$scope.physicalTables.push(angular.copy(physicalTables[p]))
			}
		}

		for(var i=0;i<selectedBusinessModel.physicalTables.length;i++){
			var index=$scope.physicalTables.indexOf(selectedBusinessModel.physicalTables[i]);
			if(index!=-1){
				$scope.physicalTables.splice(index,1);
			}
		}
		$scope.save = function(){
			if($scope.selectedPhysicalTables.lenght!=0){

				var dataToSend={
						viewUniqueName:selectedBusinessModel.uniqueName,
						physicalTables:[]

				};

				for(var i=0;i<$scope.selectedPhysicalTables.length;i++){
					dataToSend.physicalTables.push($scope.selectedPhysicalTables[i].name);
				}



				sbiModule_restServices.promisePost("1.0/metaWeb", "addPhysicalColumnToBusinessView",metaModelServices.createRequestRest(dataToSend))
				   .then(function(response){
						metaModelServices.applyPatch(response.data);
				   },function(response){
					   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
				   })
			}


			 $mdDialog.hide();
		}
		$scope.cancel = function(){
			$mdDialog.cancel();
		}
	}
}
