angular.module('importExportDocumentModule').controller('importControllerStep2', ["$scope","importExportDocumentModule_importConf","sbiModule_restServices",importStep2FuncController]);

function importStep2FuncController($scope,importExportDocumentModule_importConf,sbiModule_restServices) {

	function enginesInList(engine,list){
		for(var i in list){
			if(list[i].name==engine.name){
				return i;
			}
		}
		return -1;
	}


	$scope.currentEnginesIsSelectable=function(engine,expEngine){
		var engineinl=enginesInList(engine,importExportDocumentModule_importConf.engines.associatedEngines);
		if(engineinl!=-1 && engineinl!=expEngine.id){
			return false
		}
		return true;	
	}

	function getExportedEngines(){
		var expr=[];
		for(var key in importExportDocumentModule_importConf.engines.associatedEngines){
			expr.push({engineAssociateId:importExportDocumentModule_importConf.engines.associatedEngines[key].id,expEngineId:key});
		}
		return expr;
	}

	$scope.saveEngineAssociation=function(){
		sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associateEngines',getExportedEngines())
		.success(function(data, status, headers, config) {
			console.log("data--->",data)
			if(data.hasOwnProperty("errors")){
				$scope.stopImport(data.errors[0].message);	
			}else if(data.STATUS=="NON OK"){
				$scope.stopImport(data.ERROR);		
			}
			else if(data.STATUS=="OK"){
				importExportDocumentModule_importConf.datasources.currentDatasources=data.currentDatasources;
				importExportDocumentModule_importConf.datasources.exportedDatasources=data.exportedDatasources;
				importExportDocumentModule_importConf.datasources.associatedDatasources=data.associatedDatasources;
				$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedDS','component_impexp_messages')})
			}
		})
		.error(function(data, status, headers, config) {
			$scope.stopImport(data);		
		});
	}


}
