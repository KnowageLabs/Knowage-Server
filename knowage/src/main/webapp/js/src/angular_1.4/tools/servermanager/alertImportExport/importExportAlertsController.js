// --- APP CONFIGURATION ----------------------------------------------

var app = angular.module('impExpAlerts', [ 'ngMaterial', 'ui.tree',
		'angularUtils.directives.dirPagination', 'ng-context-menu',
		'angular_list', 'angular_table', 'angular_list', 'sbiModule',
		'file_upload', 'bread_crumb', 'importExportDocumentModule' ]);

app.directive("fileread", [ function() {
	return {
		scope : {
			fileread : "="
		},
		link : function(scope, element, attributes) {
			element.bind("change", function(changeEvent) {
				var reader = new FileReader();
				reader.onload = function(loadEvent) {
					scope.$apply(function() {
						scope.fileread = loadEvent.target.result;
					});
				}
				reader.readAsDataURL(changeEvent.target.files[0]);
			});
		}
	}
} ]);

app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.factory("importExportDocumentModule_importConf", function() {
	var current_data = {};
	var default_values = {
		fileImport : {},
		importPersonalFolder : true,
		overwriteMode: false,
		overwriteKpis: false,
		targetsAndRelatedKpis: false,
		scorecardsAndRelatedKpis: false,
		schedulersAndRelatedKpis: false,
		typeSaveKpi : 'Missing',
		checkboxs : {
			exportSubObj : false,
			exportSnapshots : false,
			exportPersonalFolder : false
		},
		roles : {
			currentRoles : [],
			exportedRoles : [],
			selectedRoles : [],
			associatedRoles : [],
			exportedKpi : [],
			exportingKpi : [],
			selectedKpis : []
		},
		engines : {
			currentEngines : [],
			exportedEngines : [],
			associatedEngines : {}
		},
		datasources : {
			currentDatasources : [],
			exportedDatasources : [],
			associatedDatasources : {}
		},
		resetData : function() {
			current_data = angular.copy(default_values, current_data);
		}
	};
	default_values.resetData();
	return current_data;
});

app.controller('alertImportExportController', [ "sbiModule_download",
		"sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog",
		"$mdToast", alertImportExportFuncController ]);

app.controller('alertExportController', [ "sbiModule_download",
		"sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog",
		"$mdToast","sbiModule_messaging", alertExportFuncController ]);

app.controller('alertImportController', [ 'sbiModule_download',
		'sbiModule_device', "$scope", "$mdDialog", "$timeout",
		"sbiModule_logger", "sbiModule_translate", "sbiModule_restServices",
		"sbiModule_config", "$mdToast",
		"importExportDocumentModule_importConf", alertImportFuncController ]);


// --- CONTROLLERS --------------------------------------------------------------

function alertImportExportFuncController(sbiModule_download, sbiModule_translate,
		sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	// variables
	sbiModule_translate.addMessageFile("component_impexp_messages");
	$scope.translate = sbiModule_translate;
	$scope.alerts = [];
	$scope.alertsSelected = [];

	$scope.viewDownload = false;
	$scope.download = sbiModule_download;
	$scope.uploadProcessing = [];
	$scope.upload = [];
	$scope.wait = false;

	$scope.flagShowKpi = false;

	$scope.loadAllAlerts = function() {
		sbiModule_restServices.promiseGet("1.0/alert", "listAlert").then( 
				function(response) {
					angular.copy(response.data, $scope.kpiListOriginal);
					for (var i = 0; i < response.data.length; i++) {
						var obj = {};
						obj["id"] = response.data[i].id;
						obj["name"] = response.data[i].name;
						$scope.alerts.push(obj);
					}
				},
				function(response) {
					sbiModule_restServices
							.errorHandler(response.data, sbiModule_translate
									.load("sbi.kpi.list.load.error"));
				});
	};
	$scope.loadAllAlerts();

	$scope.showConfirm = function() {
		// Appending dialog to document.body to cover sidenav in docs app
		var confirm = $mdDialog.alert().title(
				sbiModule_translate.load("sbi.importkpis.importfailed"))
				.ariaLabel('Lucky day').ok('Ok')

		$mdDialog.show(confirm).then(function() {

		}, function() {

		});
	};

	$scope.indexInList = function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if (object.id == item.id) {
				return i;
			}
		}

		return -1;
	};

//	$scope.showAction = function(text) {
//		var toast = $mdToast.simple().content(text).action('OK')
//				.highlightAction(false).hideDelay(3000).position('top')
//
//		$mdToast.show(toast).then(function(response) {
//
//			if (response == 'ok') {
//
//			}
//		});
//	};

}

function alertExportFuncController(sbiModule_download, sbiModule_translate,
		sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging) {
	$scope.flagCheck = false;
	$scope.nameExport = "";
	$scope.overwriteKpis = false;
	$scope.targetsAndRelatedKpis = false;
	$scope.scorecardsAndRelatedKpis = false;
	$scope.schedulersAndRelatedKpis = false;
	$scope.exportCheckboxs = {};

	$scope.selectAll = function() {
		if (!$scope.flagCheck) {
			// if it was false then the kpi check
			$scope.flagCheck = true;
			$scope.alertsSelected = [];
			for (var i = 0; i < $scope.alerts.length; i++) {
				$scope.alertsSelected.push($scope.alerts[i]);
			}
		} else {
			$scope.flagCheck = false;
			$scope.alertsSelected = [];
		}
	};

	$scope.prepare = function(ev) {
		if ($scope.alertsSelected.length == 0) {
//			$scope.showAction(sbiModule_translate
//					.load("sbi.impexpkpis.missingcheck"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpkpis.missingcheck"),"");
		} else if ($scope.nameExport == "") {
//			$scope.showAction(sbiModule_translate
//					.load("sbi.impexpkpis.missingnamefile"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpkpis.missingnamefile"),"");
		} else {
			// Download ZIP archive
			var alertsIdVersionPairs = [];
			for (k = 0; k < $scope.alertsSelected.length; k++) {
				alertsIdVersionPairs.push({
					id : $scope.alertsSelected[k].id
					//version : $scope.alertsSelected[k].version
				});
			}
			var config = {
				"ALERTS_LIST" : alertsIdVersionPairs,
				"EXPORT_FILE_NAME" : $scope.nameExport
//				"TARGETS_AND_RELATED_KPIS": $scope.targetsAndRelatedKpis,
//				"SCORECARDS_AND_RELATED_KPIS": $scope.scorecardsAndRelatedKpis,
//				"SCHEDULERS_AND_RELATED_KPIS": $scope.schedulersAndRelatedKpis
			};
			$scope.wait = true;
			sbiModule_restServices.post("1.0/serverManager/importExport/alerts",
					'export', config).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("KPI Export Failure. Errors: "
									+ JSON.stringify(data.errors));
						} else {
							if (data.hasOwnProperty("STATUS")
									&& data.STATUS == "OK") {
								$scope.downloadFile();
							}
						}
						$scope.wait = false;
					}).error(function(data, status, headers, config) {
				console.log("KPI Export Failure. Status: " + status);
				$scope.wait = false;
			});
		}
	}

	$scope.downloadFile = function() {
		var data = {
			"FILE_NAME" : $scope.nameExport
		};
		var config = {
			"responseType" : "arraybuffer"
		};
		sbiModule_restServices.post("1.0/serverManager/importExport/kpis",
				"downloadArchive", data, config).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
						$scope.wait = false;
					} else if (status == 200) {
						$scope.download.getBlob(data, $scope.nameExport,
								'application/zip', 'zip');
						$scope.viewDownload = false;
						$scope.wait = false;
//						$scope.showAction(sbiModule_translate
//								.load("sbi.importkpis.downloadOK"));
						sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importkpis.downloadOK"),"");
					}
				}).error(function(data, status, headers, config) {
					sbiModule_restServices.errorHandler("ERRORS " + status,"sbi.generic.toastr.title.error");
			$scope.wait = false;
		});
	}

	// export utilities

	$scope.toggle = function(item, list) {
		var index = $scope.indexInList(item, list);
		if (index != -1) {
			$scope.alertsSelected.splice(index, 1);
		} else {
			$scope.alertsSelected.push(item);
		}
	};

	$scope.exists = function(item, list) {
		return $scope.indexInList(item, list) > -1;
	};
}

function alertImportFuncController(sbiModule_download, sbiModule_device, $scope,
		$mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config, $mdToast,
		importExportDocumentModule_importConf) {
	$scope.stepItem = [ {
		name : $scope.translate.load('sbi.ds.file.upload.button')
	} ];
	$scope.selectedStep = 0;
	$scope.stepControl;
	$scope.IEDConf = importExportDocumentModule_importConf;
	//$scope.IEDConf.exportedKpis = []; // TODO: remove after debug

	$scope.finishImport = function() {
		if (importExportDocumentModule_importConf.hasOwnProperty("resetData")) {
			importExportDocumentModule_importConf.resetData();
		}
	}

	$scope.stopImport = function(text, title) {
		var titleFin = title || "";
		var alert = $mdDialog.alert()
				.title(titleFin)
				.content(text)
				.ariaLabel('error import')
				.ok('OK');
		$mdDialog.show(alert).then(function() {
			$scope.stepControl.resetBreadCrumb();
			$scope.stepControl.insertBread({
				name : sbiModule_translate.load(
						'SBISet.impexp.exportedKpis',
						'component_impexp_messages')
			});
			$scope.finishImport();
		});
	}
}