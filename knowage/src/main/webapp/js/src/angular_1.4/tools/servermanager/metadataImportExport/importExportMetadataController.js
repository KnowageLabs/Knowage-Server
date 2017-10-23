// STUB CODE

// --- APP CONFIGURATION ----------------------------------------------

var app = angular.module('impExpMetadata', [ 'ngMaterial', 'ui.tree',
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
		conflictsAction: "keep",
		typeSaveMetadata : 'Missing',
		checkboxs : {
			exportSubObj : false,
			exportSnapshots : false,
			exportPersonalFolder : false
		},
		roles : {
			currentRoles : [],
			exportedRoles : [],
			selectedRoles : [],
			associatedRoles : []
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

app.controller('metadataImportExportController', [ "sbiModule_download",
		"sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog",
		"$mdToast", metadataImportExportFuncController ]);

app.controller('metadataExportController', [ "sbiModule_download",
		"sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog",
		"$mdToast","sbiModule_messaging", metadataExportFuncController ]);

app.controller('metadataImportController', [ 'sbiModule_download',
		'sbiModule_device', "$scope", "$mdDialog", "$timeout",
		"sbiModule_logger", "sbiModule_translate", "sbiModule_restServices",
		"sbiModule_config", "$mdToast",
		"importExportDocumentModule_importConf", metadataImportFuncController ]);


// --- CONTROLLERS --------------------------------------------------------------

function metadataImportExportFuncController(sbiModule_download, sbiModule_translate,
		sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	// variables
	sbiModule_translate.addMessageFile("component_impexp_messages");
	$scope.translate = sbiModule_translate;

	$scope.viewDownload = false;
	$scope.download = sbiModule_download;
	$scope.uploadProcessing = [];
	$scope.upload = [];
	$scope.wait = false;

	$scope.flagShowMetadata = false;

/*	$scope.loadAllMetadatas = function() {
		sbiModule_restServices.promiseGet("1.0/metadata", "listMetadata").then(
				function(response) {
					angular.copy(response.data, $scope.metadataListOriginal);
					for (var i = 0; i < response.data.length; i++) {
						var obj = {};
						obj["id"] = response.data[i].id;
						obj["version"] = response.data[i].version;
						obj["name"] = response.data[i].name;
						$scope.metadatas.push(obj);
					}
				},
				function(response) {
					sbiModule_restServices
							.errorHandler(response.data, sbiModule_translate
									.load("sbi.metadata.list.load.error"));
				});
	};
	$scope.loadAllMetadatas(); */

	$scope.showConfirm = function() {
		// Appending dialog to document.body to cover sidenav in docs app
		var confirm = $mdDialog.alert().title(
				sbiModule_translate.load("sbi.importmetadata.importfailed"))
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

function metadataExportFuncController(sbiModule_download, sbiModule_translate,
		sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging) {
	$scope.flagCheck = false;
	$scope.nameExport = "";
	$scope.conflictsAction = "keep";
	$scope.exportCheckboxs = {};

	$scope.selectAll = function() {
	/*	if (!$scope.flagCheck) {
			// if it was false then the metadata check
			$scope.flagCheck = true;
			$scope.metadatasSelected = [];
			for (var i = 0; i < $scope.metadatas.length; i++) {
				$scope.metadatasSelected.push($scope.metadatas[i]);
			}
		} else {
			$scope.flagCheck = false;
			$scope.metadatasSelected = [];
		} */
	};

	$scope.prepare = function(ev) {
		if ($scope.nameExport == "") {
//			$scope.showAction(sbiModule_translate
//					.load("sbi.impexpmetadata.missingnamefile"));			
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpmetadata.missingnamefile"),"");
		} else {
			// Download ZIP archive
			var config = {
				"EXPORT_FILE_NAME" : $scope.nameExport,
				"CONFLICTS_ACTION": $scope.conflictsAction
			};
			$scope.wait = true;
			sbiModule_restServices.post("1.0/serverManager/importExport/metadata",
					'export', config).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("METADATA Export Failure. Errors: "
									+ JSON.stringify(data.errors));
						} else {
							if (data.hasOwnProperty("STATUS")
									&& data.STATUS == "OK") {
								$scope.downloadFile();
							}
						}
						$scope.wait = false;
					}).error(function(data, status, headers, config) {
				console.log("METADATA Export Failure. Status: " + status);
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
		sbiModule_restServices.post("1.0/serverManager/importExport/metadata",
				"downloadArchive", data, config).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						showToast(data.errors[0].message, 4000);
						$scope.wait = false;
					} else if (status == 200) {
						$scope.download.getBlob(data, $scope.nameExport,
								'application/zip', 'zip');
						$scope.viewDownload = false;
						$scope.wait = false;
//						$scope.showAction(sbiModule_translate
//								.load("sbi.importmetadata.downloadOK"));
						sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importmetadata.downloadOK"),"");
					}
				}).error(function(data, status, headers, config) {
			sbiModule_restServices.errorHandler("ERRORS " + status,"sbi.generic.toastr.title.error");
			$scope.wait = false;
		});
	}

	// export utilities

	$scope.toggle = function(item, list) {
	/*	var index = $scope.indexInList(item, list);
		if (index != -1) {
			$scope.metadatasSelected.splice(index, 1);
		} else {
			$scope.metadatasSelected.push(item);
		} */
	};

	$scope.exists = function(item, list) {
		return $scope.indexInList(item, list) > -1;
	};
}

function metadataImportFuncController(sbiModule_download, sbiModule_device, $scope,
		$mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
		sbiModule_restServices, sbiModule_config, $mdToast,
		importExportDocumentModule_importConf) {
	$scope.stepItem = [ {
		name : $scope.translate.load('sbi.ds.file.upload.button')
	} ];
	$scope.selectedStep = 0;
	$scope.stepControl;
	$scope.IEDConf = importExportDocumentModule_importConf;
	//$scope.IEDConf.exportedMetadatas = []; // TODO: remove after debug

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
		$mdDialog.show(alert).then(
				function() {
					$scope.stepControl.resetBreadCrumb();
					$scope.stepControl.insertBread({
						name : sbiModule_translate.load(
								'sbi.ds.file.upload.button')
					});
					$scope.finishImport();
				});
	}
	// $scope.currentRoles=[];
	// $scope.exportedRoles=[];
	// $scope.exportedMetadata=[];
	// $scope.exportingMetadata = [];
	// $scope.selectedMetadatas = [];

}