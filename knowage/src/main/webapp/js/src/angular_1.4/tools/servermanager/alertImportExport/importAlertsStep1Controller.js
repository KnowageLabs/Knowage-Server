angular.module('impExpAlerts').controller(
		'importAlertControllerStep1',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast","sbiModule_messaging",
				importAlertStep1FuncController ]);

function importAlertStep1FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast,sbiModule_messaging)
{
	$scope.save = function(ev) {
		console.log('step ds');
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
					"schedulersAndRelatedKpis": $scope.IEDConf.schedulersAndRelatedKpis
			}
			// Import
//			alert(JSON.stringify(data));
//			$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
			sbiModule_restServices
				.post("1.0/serverManager/importExport/alerts","associateDataSource", data)
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						$scope.stopImport(data.errors[0].message);
					}
//					else if (data.success == false) {
//						$scope.stopImport(data.submessage, $scope.translate.load(data.error,'component_impexp_messages'));
//					} else if (data.success == true) {
//						$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
//					}
					
					else if(data.STATUS=="OK"){
						importExportDocumentModule_importConf.datasources.currentDatasources=data.currentDatasources;
						importExportDocumentModule_importConf.datasources.exportedDatasources=data.exportedDatasources;
						importExportDocumentModule_importConf.datasources.associatedDatasources=data.associatedDatasources;
						$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedDS','component_impexp_messages')})
					}
					
					
				}).error(function(data, status, headers, config) {
					$scope.stopImport(data);
				})
		}
	}
}
	