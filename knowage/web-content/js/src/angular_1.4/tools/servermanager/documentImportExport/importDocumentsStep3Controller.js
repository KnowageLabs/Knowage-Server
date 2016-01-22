angular.module('importExportDocumentModule').controller('importControllerStep3', ["$scope","importExportDocumentModule_importConf","sbiModule_restServices",importStep3FuncController]);

function importStep3FuncController($scope,importExportDocumentModule_importConf,sbiModule_restServices) {


	$scope.manageFirstAssociationsDatasources=function(){
		for(var i=0;i<importExportDocumentModule_importConf.datasources.exportedDatasources.length;i++){
			var index=datasourcesInList(importExportDocumentModule_importConf.datasources.exportedDatasources[i],importExportDocumentModule_importConf.datasources.currentDatasources);
			if(index!=-1){
				importExportDocumentModule_importConf.datasources.associatedDatasources[importExportDocumentModule_importConf.datasources.exportedDatasources[i].dsId]=importExportDocumentModule_importConf.datasources.currentDatasources[index];
				importExportDocumentModule_importConf.datasources.associatedDatasources[importExportDocumentModule_importConf.datasources.exportedDatasources[i].dsId].fixed=true;
			}else{
				importExportDocumentModule_importConf.datasources.associatedDatasources[importExportDocumentModule_importConf.datasources.exportedDatasources[i].dsId]={};
			}

		}
	}

	function datasourcesInList(datasource,list){
		for(var i in list){
			if(list[i].label==datasource.label){
				return i;
			}
		}
		return -1;
	}


	$scope.currentDatasourcesIsSelectable=function(datasource,expDatasource){
		var datasourceinl=datasourcesInList(datasource,importExportDocumentModule_importConf.datasources.associatedDatasources);
		if(datasourceinl!=-1 && datasourceinl!=expDatasource.dsId){
			return false
		}
		return true;	
	}

	function getExportedDatasources(){
		var expr=[];
		for(var key in importExportDocumentModule_importConf.datasources.associatedDatasources){
			expr.push({datasourceAssociateId:importExportDocumentModule_importConf.datasources.associatedDatasources[key].dsId,expDatasourceId:key});
		}
		return expr;
	}

	$scope.saveDatasourceAssociation=function(){
		sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associateDatasources',getExportedDatasources())
		.success(function(data, status, headers, config) {
			console.log("data--->",data)
			if(data.hasOwnProperty("errors")){
				$scope.showToast(data.errors[0].message,4000);
			}else if(data.STATUS=="NON OK"){
				$scope.showToast(data.ERROR,4000);
			}
			else if(data.STATUS=="OK"){
				importExportDocumentModule_importConf.datasources.currentDatasources=data.currentDatasources;
				importExportDocumentModule_importConf.datasources.exportedDatasources=data.exportedDatasources;

				$scope.stepControl.insertBread({name:"riepilogo"})
			}
		})
		.error(function(data, status, headers, config) {
			$scope.showToast(data,4000);
		});
	}



}
