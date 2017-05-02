// STUB CODE

angular.module('impExpMetadata').controller('importMetadataControllerStep0',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
		  "$timeout", "sbiModule_logger", "sbiModule_translate",
		  "sbiModule_restServices", "sbiModule_config",
		  "importExportDocumentModule_importConf", "$mdToast","sbiModule_messaging",
		  importMetadataStep0FuncController ]);
 

function importMetadataStep0FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast,sbiModule_messaging)
{
	$scope.upload = function(ev) {
		if ($scope.IEDConf.fileImport.fileName == "" || $scope.IEDConf.fileImport.fileName == undefined) {
			//$scope.showAction(sbiModule_translate.load("sbi.impexpmetadata.missinguploadfile"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpmetadata.missinguploadfile"),"");
		} else {
			var fd = new FormData();
			fd.append('exportedArchive', $scope.IEDConf.fileImport.file);
			sbiModule_restServices.post("1.0/serverManager/importExport/metadata", 'uploadArchive', fd, {transformRequest: angular.identity, headers: {'Content-Type': undefined}})
			.success(function(data, status, headers, config) {
				if (data.STATUS == "NON OK") {
//					$mdToast.show($mdToast.simple().content("data.ERROR").position('top')
//						.action('OK').highlightAction(false).hideDelay(5000));
					sbiModule_restServices.errorHandler(data.ERROR,"sbi.generic.toastr.title.error");
				} else if (data.STATUS == "OK") {
					$scope.flagShowMetadata = true;
					//$scope.IEDConf.exportedMetadata = data.exportedMetadata;
					$scope.stepControl.insertBread({name: sbiModule_translate.load('sbi.impexpmetadata.exportedMetadatas')});
				}
			}).error(function(data, status, headers, config) {
				sbiModule_restServices.errorHandler("ERRORS " + status,"sbi.generic.toastr.title.error");
			});
		}
	};
	
	$scope.indexRoleInList=function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}
		return -1;
	};
	 
}
	