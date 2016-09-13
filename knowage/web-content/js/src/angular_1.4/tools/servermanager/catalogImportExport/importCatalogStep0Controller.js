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
	
	
	
	$scope.save = function(ev) {
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			//$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectmode"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectmode"),"");
		}else if($scope.datasetSelected.length==0){
			//$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectds"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectds"),"");
		}else{
			var config={
					"ds": $scope.datasetSelected,
					"type":$scope.typeSaveMenu,
					"associateDatasources" : $scope.IEDConf.datasources.associatedDatasources,
					"exportedDatasources" : $scope.IEDConf.datasources.exportedDatasources

			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog","importCatalog",config)
			.then(function(response, status, headers, config) {
				$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
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
	