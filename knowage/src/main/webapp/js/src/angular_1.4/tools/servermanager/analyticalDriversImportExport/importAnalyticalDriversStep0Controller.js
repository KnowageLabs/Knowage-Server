angular.module('impExpModule').controller(
		'importAnalyticalDriversControllerStep0',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast","sbiModule_messaging",
				importAnalyticalDriversStep0FuncController ]);

function importAnalyticalDriversStep0FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast,sbiModule_messaging)
{
	
	//ROLES
	$scope.showRoles = $scope.IEDConf.roles.exportedRoles.length>0;
	
	$scope.associateddatasource = function(ev){
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectmode"),"");
		}else if($scope.catalogSelected.length==0){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectds"),"");
		}else{
			var config={
//					"ds": $scope.catalogSelected,
//					"type":$scope.typeSaveMenu
			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/wizard","associateDataSource",config)
			.then(function(response, status, headers, config) {
				if(response.data.STATUS=="OK"){
					importExportDocumentModule_importConf.datasources.currentDatasources=response.data.currentDatasources;
					importExportDocumentModule_importConf.datasources.exportedDatasources=response.data.exportedDatasources;
					importExportDocumentModule_importConf.datasources.associatedDatasources=response.data.associatedDatasources;
					$scope.stepControl.insertBread({name: $scope.translate.load('sbi.impexp.exportedDS')})
				}
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
			})
		}
	} 
	
	$scope.checkRolesAssociated = function() {
		
		var associatedRoles = $scope.IEDConf.roles.associatedRoles;
		var exportedRoles = $scope.IEDConf.roles.exportedRoles;
		var countAssociatedRoles = Object.keys(associatedRoles).length;
		
		if(exportedRoles.length == countAssociatedRoles){
			return false;
		}else{
			return true;
		}
		
	}	
}
	