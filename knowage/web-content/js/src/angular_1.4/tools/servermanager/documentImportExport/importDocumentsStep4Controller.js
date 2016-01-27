angular.module('importExportDocumentModule').controller('importControllerStep4', ["$scope","importExportDocumentModule_importConf","sbiModule_restServices",importStep4FuncController]);

function importStep4FuncController($scope,importExportDocumentModule_importConf,sbiModule_restServices) {
	$scope.nextStep = function(){
		alert("FINITO")
		$scope.stepControl.resetBreadCrumb();
		$scope.stepControl.insertBread({name:"step1"})
	}
	
	
	$scope.saveMetaDataAssociation=function(){
		sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associateMetadata',{"overwrite":$scope.overwriteMetaData})
		.success(function(data, status, headers, config) {
			console.log("data--->",data)
			if(data.hasOwnProperty("errors")){
				$scope.showToast(data.errors[0].message,4000);
			}else if(data.STATUS=="NON OK"){
				$scope.showToast(data.ERROR,4000);
			}
			else if(data.STATUS=="OK"){
				importExportDocumentModule_importConf.associationsFileName=data.associationsName;
				importExportDocumentModule_importConf.logFileName=data.logFileName;
				importExportDocumentModule_importConf.folderName=data.folderName;
				$scope.stepControl.resetBreadCrumb();
				$scope.stepControl.insertBread({name:$scope.translate.load('sbi.ds.file.upload.button')})
			}
		})
		.error(function(data, status, headers, config) {
			$scope.showToast(data,4000);
		});
	}
	
}
