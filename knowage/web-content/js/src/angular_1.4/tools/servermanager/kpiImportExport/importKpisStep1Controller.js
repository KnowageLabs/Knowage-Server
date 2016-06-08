angular.module('impExpKpis').controller(
		'importKpiControllerStep1',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast",
				importKpiStep1FuncController ]);

function importKpiStep1FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast)
{
	$scope.save = function(ev) {
		if ($scope.IEDConf.roles.selectedKpis.length == 0) {
			// No KPI selected
			$scope.showAction(sbiModule_translate.load("sbi.importkpis.anykpichecked"));
		} else {
			// Import data
			var data = {
				"kpis": $scope.IEDConf.roles.selectedKpis
			}
			// Import
			alert(JSON.stringify(data));
			$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
		/*	sbiModule_restServices
				.post("1.0/serverManager/importExport/kpis","importKpis", data)
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						$scope.stopImport(data.errors[0].message);
					} else if (data.success == false) {
						$scope.stopImport(data.SUBMESSAGE, $scope.translate.load(data.ERROR,'component_impexp_messages'));
					} else if (data.success == true) {
						$scope.stopImport($scope.translate.load("sbi.importkpis.importkpiok"));
					}
				}).error(function(data, status, headers, config) {
					$scope.stopImport(data);
				}) */
		}
	}
}
	