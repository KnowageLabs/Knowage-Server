angular.module('impExpModule').controller(
		'importAnalyticalDriversControllerStep1',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast","sbiModule_messaging",
				importAnalyticalDriversStep1FuncController ]);

function importAnalyticalDriversStep1FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast,sbiModule_messaging)
{
	$scope.showDatasource = $scope.IEDConf.datasources.exportedDatasources.length>0;
	
	$scope.save = function(ev) {
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectmode"),"");
		}else if($scope.catalogSelected.length==0){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectds"),"");
		}else{
			var config={
					"drivers": $scope.catalogSelected,
					"type":$scope.typeSaveMenu,
					"associateRoles" : $scope.IEDConf.roles.associatedRoles,
					"exportedRoles" : $scope.IEDConf.roles.exportedRoles,
					"associateDatasources" : $scope.IEDConf.datasources.associatedDatasources,
					"exportedDatasources" : $scope.IEDConf.datasources.exportedDatasources

			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/analyticaldrivers","importDrivers",config)
			.then(function(response, status, headers, config) {
				var resp = response.data;
				if (resp.STATUS == "OK")
					$scope.stopImport($scope.translate.load("sbi.importexportcatalog.generic.ok"));
				else
					$scope.stopImport(resp.ERROR);
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
			})

		}
	}
	
	
	$scope.checkDatasourceAssociated = function() {
		
		var associatedDS = $scope.IEDConf.datasources.associatedDatasources;
		var exportedDS = $scope.IEDConf.datasources.exportedDatasources;
		var countAssociatedDS = Object.keys(associatedDS).length;
		
		if(exportedDS.length == countAssociatedDS){
			return false;
		}else{
			return true;
		}
		
	}	
	
}
	