angular.module('impExpDataset').controller(
		'importCatalogControllerStep0',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast","sbiModule_messaging",
				importCatalogStep0FuncController ]);

function importCatalogStep0FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast,sbiModule_messaging)
{
	
	
	$scope.showDatasource = $scope.IEDConf.datasources.exportedDatasources.length>0;
	
	$scope.save = function(ev) {
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			//$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectmode"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectmode"),"");
		}else if($scope.catalogSelected.length==0){
			//$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectds"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectds"),"");
		}else{
			var config={
					"ds": $scope.catalogSelected,
					"type":$scope.typeSaveMenu,
					"associateDatasources" : $scope.IEDConf.datasources.associatedDatasources,
					"exportedDatasources" : $scope.IEDConf.datasources.exportedDatasources

			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog","importCatalog",config)
			.then(function(response, status, headers, config) {
				var resp = response.data;
				if (resp.STATUS == "OK")
					$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
				else
					$scope.stopImport(resp.ERROR);
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
				//$scope.showAction("Error");
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
	