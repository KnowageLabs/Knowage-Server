var app = angular.module('impExpModule', [ 'ngMaterial', 'ui.tree',
                                            'angularUtils.directives.dirPagination', 'ng-context-menu',
                                            'angular_list', 'angular_table' ,'angular_list','sbiModule','file_upload', 'angular_2_col','bread_crumb', 'importExportDocumentModule' ]);
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
		typeSaveUser : 'Missing',
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
			exportedUser:[],
			exportingUser : [],
			selectedUser : [],
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
		exportedCatalog : [],
		showDriversImported : false,
		resetData: function() {  
	    	 current_data = angular.copy(default_values,current_data); 
	    } 
	};
	 
	default_values.resetData();
	return current_data;
});

app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","sbiModule_messaging",
                               "importExportDocumentModule_importConf", funzione ]);

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging,importExportDocumentModule_importConf) {
	$scope.translate = sbiModule_translate;
	$scope.showDrivers=true;
	$scope.catalogDrivers=[];
	
	$scope.catalogSelected=[];

	$scope.flagCheck=false;
	$scope.nameExport="";
	$scope.listType=[];
	$scope.listTypeImported=[];
	$scope.listDestType=[];
	$scope.importFile = {};
	$scope.exportedDataset =[];
	$scope.download = sbiModule_download;
	$scope.flagUser = false;
	$scope.typeSaveMenu="Missing";
	$scope.filterDate;
	
	
	$scope.stepItem = [ {
		name : $scope.translate.load('sbi.ds.file.upload.button')
	} ];
	$scope.selectedStep = 0;
	$scope.stepControl;
	$scope.IEDConf = importExportDocumentModule_importConf;	

	$scope.finishImport = function() {
		if (importExportDocumentModule_importConf.hasOwnProperty("resetData")) {
			importExportDocumentModule_importConf.resetData();
		}
	}

	$scope.stopImport = function(text, title) {
		var titleFin = title || "";
//		var confirm = $mdDialog.confirm().title(titleFin).content(text)
//				.ariaLabel('error import').ok('OK').cancel(
//						sbiModule_translate.load("sbi.general.cancel"));
//		
		var alert = $mdDialog.alert()
				.title(titleFin)
				.textContent(text)
				.ariaLabel('error import')
				.ok('OK');
		$mdDialog.show(alert).then(function() {
			$scope.stepControl.resetBreadCrumb();
			$scope.stepControl.insertBread({
				name : sbiModule_translate.load(
						'sbi.ds.file.upload.button')
			});
			$scope.finishImport();
		});
	}
	
	

	//export utilities 
	$scope.prepare = function(ev){
		$scope.wait = true;
		if($scope.catalogSelected.length == 0){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.missingcheck"),"");
			$scope.wait = false;
		} else if($scope.nameExport==""){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missingnamefile"),"");
			$scope.wait = false;
		}else{
			var driversList = $scope.getCatalogForCategory($scope.catalogSelected, 'AnalyticalDrivers');			
			//module download zip			
//			var config={"DRIVERS_LIST":driversList ,					
//						"EXPORT_FILE_NAME":$scope.nameExport,
//						"EXPORT_SUB_OBJ":false,
//						"EXPORT_SNAPSHOT":false};
//			
			var config={"DRIVERS_LIST":driversList ,					
					    "EXPORT_FILE_NAME":$scope.nameExport};

			sbiModule_restServices.promisePost("1.0/serverManager/importExport/analyticaldrivers", 'export',config).then(
					function(response, status, headers, config) {
						if(response.data.hasOwnProperty("STATUS") && response.data.STATUS=="OK"){
							$scope.downloadFile();
						}
					},function(response, status, headers, config) {
						$scope.wait = false;
						sbiModule_restServices.errorHandler(response.data,"");					
					})
		}
	}

	$scope.getCatalogForCategory = function(selectedList, type){
		var toReturn = [];
		
		if(selectedList.length == 0) return selectedList;
		
		for (var s=0; s < selectedList.length; s++){
			var selObj = selectedList[s];
			if (selObj.catalogType == type){
				toReturn.push(selObj);
			}
		}
		
		return toReturn;
	}
	
	
	$scope.downloadFile= function(){
		var data={"FILE_NAME":$scope.nameExport};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.promisePost("1.0/serverManager/importExport/wizard","downloadExportFile",data,config)
		.then(function(response, status, headers, config) {
				$scope.download.getBlob(response.data,$scope.nameExport,'application/zip','zip');
				sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importusers.downloadOK"),"");
				$scope.wait=false;
		},function(response, status, headers, config) {
			sbiModule_restServices.errorHandler(response.data,"");
			$scope.wait = false;
		})
	}


	//import utilities
	$scope.upload = function(ev){
		$scope.exportedCatalog =[];
		if($scope.IEDConf.fileImport.fileName == "" || $scope.IEDConf.fileImport.fileName == undefined){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"),"");
		}else{
			var fd = new FormData();
			fd.append('exportedArchive', $scope.IEDConf.fileImport.file);
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/analyticaldrivers", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
			.then(function(response, status, headers, config) {
//				$scope.catalogSelected = [];
				$scope.catalogSelected = angular.copy(response.data.exportedCatalog, $scope.catalogSelected);  //for default all elements are checked
				$scope.exportedCatalog = response.data.exportedCatalog;
				//open 
				$scope.IEDConf.showDriversImported = true;

			}, function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
			});
		}
	}

	$scope.save = function(ev){
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectmode"),"");
		}else if($scope.catalogSelected.length==0){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectds"),"");
		}else{
			var config={
					"ds": $scope.catalogSelected,
					"type":$scope.typeSaveMenu

			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/analyticaldrivers","importAnalyticalDrivers",config)
			.then(function(response, status, headers, config) {
				sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importusers.importuserok"),"");
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
			})

		}
	}

	
	$scope.setTypeSaveMenu = function(type){
		$scope.typeSaveMenu = type;
	}
	
	$scope.associatedrole= function(ev){
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importexportcatalog.selectmode"),"");
		}else if($scope.catalogSelected.length==0){
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexp.selectad"),"");
		}else{
			var config={
//					"ds": $scope.catalogSelected,
//					"type":$scope.typeSaveMenu
			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/wizard","associateRoles",config)
			.then(function(response, status, headers, config) {
				if(response.data.STATUS=="OK"){
					importExportDocumentModule_importConf.roles.currentRoles=response.data.currentRoles;
					importExportDocumentModule_importConf.roles.exportedRoles=response.data.exportedRoles;
					importExportDocumentModule_importConf.roles.associatedRoles=response.data.associatedRoles;
					$scope.stepControl.insertBread({name: $scope.translate.load('sbi.impexp.exportedRole')})
				}
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
			})
		}
	}

	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}

	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}

}