// STUB CODE

angular.module('impExpMetadata').controller(
		'importMetadataControllerStep1',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
				"$timeout", "sbiModule_logger", "sbiModule_translate",
				"sbiModule_restServices", "sbiModule_config",
				"importExportDocumentModule_importConf", "$mdToast",
				importMetadataStep1FuncController ]);

function importMetadataStep1FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast)
{
	$scope.save = function(ev) {
		/* if ($scope.IEDConf.roles.selectedMetadata.length == 0) {
			// No METADATA selected
			$scope.showAction(sbiModule_translate.load("sbi.importmetadata.anymetadatachecked"));
		} else */ {
			// Import data
			var data = {
					//"metadata": $scope.IEDConf.roles.selectedMetadata,
					"conflictsAction": $scope.IEDConf.conflictsAction
			}
			// Import
//			alert(JSON.stringify(data));
//			$scope.stopImport($scope.translate.load("sbi.importmetadata.importmetadataok"));
			sbiModule_restServices
				.post("1.0/serverManager/importExport/metadata","import", data)
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						$scope.stopImport(data.errors[0].message);
					} else if (data.success == false) {
						$scope.stopImport(data.submessage, $scope.translate.load(data.error,'component_impexp_messages'));
					} else if (data.success == true) {
						$scope.stopImport($scope.translate.load("sbi.importmetadata.importmetadataok"));
					}
				}).error(function(data, status, headers, config) {
					$scope.stopImport(data);
				})
		}
	}
}
	