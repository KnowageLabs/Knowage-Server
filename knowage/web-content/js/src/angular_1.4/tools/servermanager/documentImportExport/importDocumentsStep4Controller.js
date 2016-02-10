angular.module('importExportDocumentModule').controller('importControllerStep4', ["$scope","importExportDocumentModule_importConf","sbiModule_restServices",importStep4FuncController]);

function importStep4FuncController($scope,importExportDocumentModule_importConf,sbiModule_restServices) {
	
	
	
	$scope.saveMetaDataAssociation=function(){
		var data={
				"overwrite":$scope.overwriteMetaData, 
			}
		if($scope.importType){
			data.importType=$scope.importType;
		}
			sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associateMetadata',data)
			.success(function(data, status, headers, config) {
			if(data.hasOwnProperty("errors")){
				$scope.errorImport(data.errors[0].message);	
			}else if(data.STATUS=="NON OK"){
				$scope.errorImport(data.ERROR);		
			}
			else if(data.STATUS=="OK"){
				importExportDocumentModule_importConf.associationsFileName=data.associationsName;
				importExportDocumentModule_importConf.logFileName=data.logFileName;
				importExportDocumentModule_importConf.folderName=data.folderName;
				$scope.stepControl.resetBreadCrumb();
				$scope.stepControl.insertBread({name:$scope.translate.load('sbi.ds.file.upload.button')})
			
			if($scope.finishImport){
				$scope.finishImport();
			}
			}
		})
		.error(function(data, status, headers, config) {
			$scope.errorImport(data);		
		});
	}
	
}
