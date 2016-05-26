var app = angular.module('impExpKpis', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'angular_list','sbiModule','file_upload', 'bread_crumb','importExportDocumentModule']);
app.directive("fileread", [function () {
	return {
		scope: {
			fileread: "="
		},
		link: function (scope, element, attributes) {
			element.bind("change", function (changeEvent) {
				var reader = new FileReader();
				reader.onload = function (loadEvent) {
					scope.$apply(function () {
						scope.fileread = loadEvent.target.result;
					});
				}
				reader.readAsDataURL(changeEvent.target.files[0]);
			});
		}
	}
}]);

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
app.factory("importExportDocumentModule_importConf", function() {
	 var current_data = {}; 
	 var default_values = {
		fileImport : {},
		importPersonalFolder : true,
		typeSaveKpi : 'Missing',
		checkboxs:{
				exportSubObj : false,
				exportSnapshots : false,
				exportPersonalFolder: false
		},
		roles : {
			currentRoles : [],
			exportedRoles : [],
			selectedRoles : [],
			associatedRoles:[],
			exportedKpi:[],
			exportingKpi : [],
			selectedKpi : [],
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
		 resetData: function() {  
		    	 current_data = angular.copy( default_values,current_data); 
		     } 
	};
	 default_values.resetData();
	  return current_data;
});

app.controller('kpiImportExportController', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);
app.controller('kpiExportController', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", kpiExportFuncController ]);
app.controller('kpiImportController', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","$mdToast","importExportDocumentModule_importConf",kpiImportFuncController]);


function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	//variables
	sbiModule_translate.addMessageFile("component_impexp_messages");
	$scope.translate=sbiModule_translate;
	$scope.kpis = [];
	$scope.kpisSelected = [];
	
	
	$scope.viewDownload =false;
	$scope.download = sbiModule_download;
	$scope.uploadProcessing = [];
	$scope.upload = [];
	$scope.wait = false;

	
	$scope.flagShowKpi=false;
	
	
	
	$scope.loadAllKpis = function() {
		sbiModule_restServices.promiseGet("1.0/kpi", "listKpi")
			.then(function(response) {
				angular.copy(response.data, $scope.kpiListOriginal);
				for (var i = 0; i < response.data.length; i++) {
					var obj = {};
					obj["id"] = response.data[i].id;
					obj["version"] = response.data[i].version;
					obj["name"] = response.data[i].name;
					$scope.kpis.push(obj);
				}
			}, function(response) {
				sbiModule_restServices.errorHandler(response.data, sbiModule_translate.load("sbi.kpi.list.load.error"));
			});
	};
	$scope.loadAllKpis();
	
	
	
	$scope.showConfirm = function() {
	    // Appending dialog to document.body to cover sidenav in docs app
	    var confirm = $mdDialog.alert()
	          .title(sbiModule_translate.load("sbi.importkpis.importfailed"))
	          .ariaLabel('Lucky day')
	          .ok('Ok')
	         
	    $mdDialog.show(confirm).then(function() {

	    }, function() {
	      
	    });
	  };
	
	
	  $scope.indexInList=function(item, list) {

			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.id==item.id){
					return i;
				}
			}

			return -1;
		};
	 
	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};
	
}


function kpiExportFuncController(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	$scope.flagCheck = false;
	$scope.nameExport = "";
	$scope.exportCheckboxs = {};
	
	$scope.selectAll = function() {
		if (!$scope.flagCheck) {
			//if it was false then the kpi check 
			$scope.flagCheck = true;
			$scope.kpisSelected = [];
			for (var i = 0; i < $scope.kpis.length; i++) {
				$scope.kpisSelected.push($scope.kpis[i]);
			}
		} else {
			$scope.flagCheck = false;
			$scope.kpisSelected = [];
		} 
	};
	
	$scope.prepare = function(ev) {
		if ($scope.kpisSelected.length == 0) {
			$scope.showAction(sbiModule_translate.load("sbi.impexpkpis.missingcheck"));
		} else if($scope.nameExport=="") {
			$scope.showAction(sbiModule_translate.load("sbi.impexpkpis.missingnamefile"));
		} else {
			// Download ZIP archive
			var kpisIdVersionPairs = [];
			for (k = 0; k < $scope.kpisSelected.length; k++) {
				kpisIdVersionPairs.push({
					id: $scope.kpisSelected[k].id,
					version: $scope.kpisSelected[k].version
				});
			}
			var config = {"KPIS_LIST": kpisIdVersionPairs,
					"EXPORT_FILE_NAME": $scope.nameExport,
					"EXPORT_SUB_OBJ": $scope.exportCheckboxs.exportSubObj,
					"EXPORT_SNAPSHOT": $scope.exportCheckboxs.exportSnapshots /*,
					"EXPORT_PERSONAL_FOLDER": $scope.exportCheckboxs.exportPersonalFolder */};
			$scope.wait = true;
			sbiModule_restServices.post("1.0/serverManager/importExport/kpis", 'export', config)
				.success( function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("KPI Export Failure. Errors: " + JSON.stringify(data.errors));
					} else {
						if (data.hasOwnProperty("STATUS") && data.STATUS == "OK") {
							$scope.downloadFile();
						}
					}
					$scope.wait = false;
				}).error( function(data, status, headers, config) {
					console.log("KPI Export Failure. Status: " + status);
					$scope.wait = false;
				});
		}
	}
	
	$scope.downloadFile = function() {
		var data = {"FILE_NAME": $scope.nameExport};
		var config = {"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/kpis", "downloadExportFile", data, config)
			.success( function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					showToast(data.errors[0].message, 4000);
					$scope.wait = false;
				} else if (status == 200) {
					$scope.download.getBlob(data, $scope.nameExport, 'application/zip', 'zip');
					$scope.viewDownload = false;
					$scope.wait = false;
					$scope.showAction(sbiModule_translate.load("sbi.importkpis.downloadOK"));
				}
			}).error( function(data, status, headers, config) {
				showToast("ERRORS " + status, 4000);
				$scope.wait = false;
			});
	}
	
	// export utilities
	
	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);
		if (index != -1) {
			$scope.kpisSelected.splice(index,1);
		} else {
			$scope.kpisSelected.push(item);
		}
	};
	
	$scope.exists = function (item, list) {
		return  $scope.indexInList(item, list) > -1;
	}; 
}

function kpiImportFuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,$mdToast,importExportDocumentModule_importConf) {
	$scope.stepItem=[{name: $scope.translate.load('sbi.ds.file.upload.button')}];
	$scope.selectedStep=0;
	$scope.stepControl;
	$scope.IEDConf=importExportDocumentModule_importConf;
	
	$scope.finishImport=function(){
		if(importExportDocumentModule_importConf.hasOwnProperty("resetData")){
			importExportDocumentModule_importConf.resetData();
		}
	}
	
	$scope.stopImport=function(text,title){
		var titleFin=title || "";
		 var confirm = $mdDialog.confirm()
		.title(titleFin)
		.content(text)
		.ariaLabel('error import') 
		.ok('OK') 
		$mdDialog.show(confirm).then(function() {
			$scope.stepControl.resetBreadCrumb();
			$scope.stepControl.insertBread({name: sbiModule_translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
			$scope.finishImport();
		} ); 
	}
//	$scope.currentRoles=[];
//	$scope.exportedRoles=[];
//	$scope.exportedKpi=[];
//	$scope.exportingKpi = [];
//	$scope.selectedKpi = [];
	
	
	
}