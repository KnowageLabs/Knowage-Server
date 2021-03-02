angular.module('impExpAlerts').controller(
		'importAlertControllerStep2',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast","sbiModule_messaging",
				importAlertStep2FuncController ]);

function importAlertStep2FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast,sbiModule_messaging)
{
	$scope.save = function(ev) {
		if ($scope.IEDConf.roles.selectedKpis.length == 0) {
			// No KPI selected
			//$scope.showAction(sbiModule_translate.load("sbi.importkpis.anykpichecked"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importkpis.anykpichecked"),"");
		} else {
			// Import data
			var data = {
					"alerts": $scope.IEDConf.roles.selectedKpis,
					"overwrite": $scope.IEDConf.overwriteMode,
					"overwriteKpis": $scope.IEDConf.overwriteKpis,
					"targetsAndRelatedKpis": $scope.IEDConf.targetsAndRelatedKpis,
					"scorecardsAndRelatedKpis": $scope.IEDConf.scorecardsAndRelatedKpis,
					"schedulersAndRelatedKpis": $scope.IEDConf.schedulersAndRelatedKpis,
					"associateDatasources" : $scope.IEDConf.datasources.associatedDatasources,
					"exportedDatasources" : $scope.IEDConf.datasources.exportedDatasources
			}
			// Import
//			alert(JSON.stringify(data));
//			$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
			sbiModule_restServices
				.post("1.0/serverManager/importExport/alerts","import", data)
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						$scope.stopImport(data.errors[0].message);
					} else if (data.success == false) {
						$scope.stopImport(data.submessage, $scope.translate.load(data.error,'component_impexp_messages'));
					} else if (data.success == true) {
						$scope.stopImport($scope.translate.load("sbi.importexportcatalog.generic.ok"));
					}
				}).error(function(data, status, headers, config) {
					$scope.stopImport(data);
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
	