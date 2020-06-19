angular.module('importExportDocumentModule').controller('importControllerStep4', ["$scope","importExportDocumentModule_importConf","sbiModule_restServices","$mdDialog",importStep4FuncController]);

function importStep4FuncController($scope,importExportDocumentModule_importConf,sbiModule_restServices,$mdDialog) {
	$scope.showCircular = false;

	$scope.saveMetaDataAssociation=function(){
		$scope.showCircular = true;

		var data={
				"overwrite":true,
		}
		
		if($scope.importType){
			data.importType=$scope.importType;
		}
		
		sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associateMetadata',data)
			.then(function(response) {
				if(response.data.hasOwnProperty("errors")){
					$scope.stopImport(response.data.errors[0].message);
					
				}else if(response.data.STATUS=="NON OK"){
					$scope.stopImport($scope.translate.load(response.data.ERROR,'component_impexp_messages'));
	
				}
				else if(response.data.STATUS=="OK"){
					$scope.showCircular = false;
	
					importExportDocumentModule_importConf.associationsFileName=response.data.associationsName;
					importExportDocumentModule_importConf.logFileName=response.data.logFileName;
					importExportDocumentModule_importConf.folderName=response.data.folderName;
					var warnings = response.data.warnings;
					
					if (warnings && warnings.length > 0) {
						
						var text = "";
						var tmpType = "";
						warnings.sort(function(a, b){
							return a.TYPE.localeCompare(b.TYPE);
						});
						
						var count = 0;
						for (var i in warnings) {
							if (tmpType.localeCompare(warnings[i].TYPE) != 0){
								text += "<h4 class='noMargin' style='margin-bottom:4px;'>" + warnings[i].TYPE + "</h4>";
								tmpType = warnings[i].TYPE;
								count = 0;
							}
							if (count > 0) text += "</br>";
							text += warnings[i].MESSAGE;
							count++;
						}
						
						var confirm = $mdDialog.alert()
							.title($scope.translate.load("sbi.importusers.warnings"))
							.htmlContent(text)
							.ariaLabel('Alert Dialog')
							.ok('ok');
						
						$mdDialog.show(confirm).then(function() {
							$scope.stopImportWithDownloadAss($scope.translate.load("sbi.importusers.importuserokdownloadass"),response.data.folderName, response.data.associationsName);
						});
					} else {
						$scope.stopImportWithDownloadAss($scope.translate.load("sbi.importusers.importuserokdownloadass"),response.data.folderName, response.data.associationsName);
					}
					
					if($scope.finishImport){
						$scope.finishImport();
					}
				}
			},
			function(response) {
				$scope.stopImport(response.data);
			}
		);
	}
	
	$scope.stopImportWithDownloadAss=function(text, folderAss, fileAss){
		var folder = folderAss;
		var file = fileAss;

		var confirm = $mdDialog.confirm()
		.title('')
		.content(text)
		.ariaLabel('error import')
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));

		$mdDialog.show(confirm).then(function() {
			// choose to download association xml
			var data={"FILE_NAME":file, "FOLDER_NAME":folder};
			var config={"responseType": "arraybuffer"};

			sbiModule_restServices.promisePost("1.0/serverManager/importExport/document","downloadAssociationsFile",data,config)
			.then(function(response) {
				if (response.data.hasOwnProperty("errors")) {
				sbiModule_restServices.errorHandler(response.data.errors[0].message,"sbi.generic.toastr.title.error");

				$scope.stepControl.resetBreadCrumb();
				$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
				$scope.finishImport();
			}else {
				// download association file
				$scope.download.getBlob(response.data,file,'text/xml','xml');

				$scope.stepControl.resetBreadCrumb();
				$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
				$scope.finishImport();

			}
	}, function(response) {
		sbiModule_restServices.errorHandler(response.data.errors[0].message,"sbi.generic.toastr.title.error");
	});




},
	function() {
	$scope.stepControl.resetBreadCrumb();
		$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
		$scope.finishImport();
});
	
	}


}
