angular.module('impExpKpis').controller('importKpiControllerStep0',
		[ 'sbiModule_download', 'sbiModule_device', "$scope", "$mdDialog",
		  "$timeout", "sbiModule_logger", "sbiModule_translate",
		  "sbiModule_restServices", "sbiModule_config",
		  "importExportDocumentModule_importConf", "$mdToast",
		  importKpiStep0FuncController ]);
 

function importKpiStep0FuncController(sbiModule_download, sbiModule_device,
		$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config,
		importExportDocumentModule_importConf, $mdToast)
{
	$scope.upload = function(ev) {
		if ($scope.IEDConf.fileImport.fileName == "" || $scope.IEDConf.fileImport.fileName == undefined) {
			$scope.showAction(sbiModule_translate.load("sbi.impexpkpis.missinguploadfile"));
		} else {
			var fd = new FormData();
			fd.append('exportedArchive', $scope.IEDConf.fileImport.file);
			sbiModule_restServices.post("1.0/serverManager/importExport/kpis", 'import', fd, {transformRequest: angular.identity, headers: {'Content-Type': undefined}})
			.success(function(data, status, headers, config) {
				if (data.STATUS == "NON OK") {
					$mdToast.show($mdToast.simple().content("data.ERROR").position('top')
						.action('OK').highlightAction(false).hideDelay(5000));
				} else if (data.STATUS == "OK") {
					$scope.flagShowKpi = true;
					$scope.IEDConf.exportedKpis = data.exportedKpis;
					$scope.stepControl.insertBread({name: sbiModule_translate.load('sbi.impexpkpis.exportedKpis')});
				}
			}).error(function(data, status, headers, config) {
				$mdToast.show($mdToast.simple().content("errore").position('top')
					.action('OK').highlightAction(false).hideDelay(5000));
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
	