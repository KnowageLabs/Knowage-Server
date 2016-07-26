var app = angular.module('impExpDataset', [ 'ngMaterial', 'ui.tree',
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
app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",
                               "importExportDocumentModule_importConf", funzione ]);


//app.controller('catalogImportController', [ 'sbiModule_download',
//                                		'sbiModule_device', "$scope", "$mdDialog", "$timeout",
//                                		"sbiModule_logger", "sbiModule_translate", "sbiModule_restServices",
//                                		"sbiModule_config", "$mdToast",
//                                		"importExportDocumentModule_importConf", catalogImportFuncController ]);
//
//function catalogImportFuncController(sbiModule_download, sbiModule_device, $scope,
//		$mdDialog, $timeout, sbiModule_logger, sbiModule_translate,
//		sbiModule_restServices, sbiModule_config, $mdToast,
//		importExportDocumentModule_importConf) {
//	
//	
//	
//	
//
//}

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast,importExportDocumentModule_importConf) {
	$scope.translate = sbiModule_translate;
	$scope.dataset = [];
	$scope.datasetSelected = [];
	$scope.flagCheck=false;
	$scope.nameExport="";
	$scope.typeCatalog="Dataset";
	$scope.showDataset=false;
	$scope.showDatasetImported=false;
	$scope.listType=[];
	$scope.listTypeImported=[];
	$scope.listDestType=[];
	$scope.importFile = {};
	$scope.exportedDataset =[];
	$scope.download = sbiModule_download;
	$scope.flagUser = false;
	$scope.flagCategory = false;
	$scope.typeSaveMenu="Missing";
	$scope.filterDate;

	$scope.stepItem = [ {
		name : $scope.translate.load('sbi.ds.file.upload.button')
	} ];
	$scope.selectedStep = 0;
	$scope.stepControl;
	$scope.IEDConf = importExportDocumentModule_importConf;
	$scope.IEDConf.exportedKpis = []; // TODO: remove after debug

	$scope.finishImport = function() {
		if (importExportDocumentModule_importConf.hasOwnProperty("resetData")) {
			importExportDocumentModule_importConf.resetData();
		}
	}
	$scope.filterDataset = function(){
		if($scope.filterDate!=undefined){
			sbiModule_restServices.promiseGet("1.0/serverManager/importExport/catalog", 'getdataset',"dateFilter="+$scope.filterDate).then(
					function(response, status, headers, config) {
						$scope.dataset = response.data;
					},function(response, status, headers, config) {
						console.log("layer non Ottenuti " + status);
						sbiModule_restServices.errorHandler(response.data,"");
					})
					.error(function(data, status){
						$scope.log.error('GET RESULT error of ' + data + ' with status :' + status);
					});
		}else{
			$scope.loadAllDataset();
		}
	}
	$scope.stopImport = function(text, title) {
		var titleFin = title || "";
		var confirm = $mdDialog.confirm().title(titleFin).content(text)
				.ariaLabel('error import').ok('OK')
		$mdDialog.show(confirm).then(
				function() {
					$scope.stepControl.resetBreadCrumb();
					$scope.stepControl.insertBread({
						name : sbiModule_translate.load(
								'sbi.impexp.catalog.upload',
								'component_impexp_messages')
					});
					$scope.finishImport();
				});
	}
	
	

	//export utilities 
	$scope.loadAllDataset = function(){
		sbiModule_restServices.promiseGet("1.0/serverManager/importExport/catalog", 'getdataset').then(
				function(response, status, headers, config) {
					$scope.dataset = response.data;
				},function(response, status, headers, config) {
					console.log("layer non Ottenuti " + status);
					sbiModule_restServices.errorHandler(response.data,"");
				})
	}

	$scope.prepare = function(ev){
		$scope.wait = true;
		if($scope.datasetSelected.length == 0){
			$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.missingcheck"));
			$scope.wait = false;
		} else if($scope.nameExport==""){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingnamefile"));
			$scope.wait = false;
		}else{
			//modulo download zip

			var config={"DATASET_LIST":$scope.datasetSelected ,
					"EXPORT_FILE_NAME":$scope.nameExport,
					"EXPORT_SUB_OBJ":false,
					"EXPORT_SNAPSHOT":false};

			sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog", 'export',config).then(
					function(response, status, headers, config) {
						if(response.data.hasOwnProperty("STATUS") && response.data.STATUS=="OK"){
							$scope.downloadFile();
							//	$scope.viewDownload = true;
							//	$scope.downloadFile();
						}

					},function(response, status, headers, config) {
						sbiModule_restServices.errorHandler(response.data,"");					})
		}
	}



	$scope.downloadFile= function(){
		var data={"FILE_NAME":$scope.nameExport};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog","downloadExportFile",data,config)
		.then(function(response, status, headers, config) {
			 
				$scope.download.getBlob(response.data,$scope.nameExport,'application/zip','zip');
				$scope.showAction(sbiModule_translate.load("sbi.importusers.downloadOK"));
				$scope.wait=false;
			 
		},function(response, status, headers, config) {
			sbiModule_restServices.errorHandler(response.data,"");
			$scope.wait = false;
		})
	}


	$scope.toggle = function (item, list) {
		$scope.checkCategory(item);
		var index = $scope.indexInList(item, list);

		if(index != -1){
			list.splice(index,1);
		}else{
			list.push(item);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item, list)>-1;

	};

	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object==item){
				return i;
			}
		}

		return -1;
	};

	$scope.checkCategory= function(item){
		switch(item){
		case 'Dataset':
			if(!$scope.showDataset){
				$scope.showDataset=true;
				$scope.loadAllDataset();
			}else if($scope.showDataset){
				$scope.showDataset=false;
			}
			
			break;
		case 'DatasetImported':
			if(!$scope.showDatasetImported){
				$scope.showDatasetImported=true;
			}else if($scope.showDatasetImported){
				$scope.showDatasetImported=false;
			}
			break;


		}

	}
	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			$scope.datasetSelected=[];
			for(var i=0;i<$scope.dataset.length;i++){
				$scope.datasetSelected.push($scope.dataset[i]);
			}
		}else{
			$scope.flagCheck=false;
			$scope.datasetSelected=[];
		}


	}
	//import utilities
	$scope.upload = function(ev){
		$scope.exportedDataset =[];
		if($scope.importFile.fileName == "" || $scope.importFile.fileName == undefined){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"));
		}else{
			var fd = new FormData();

			fd.append('exportedArchive', $scope.importFile.file);
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
			.then(function(response, status, headers, config) {
				 

					$scope.datasetSelected = [];
					//	$scope.flagUser = data.flagUsers;
					$scope.flagCategory = response.data.flagDomain;
					if($scope.flagCategory){
						$scope.exportedDataset = response.data.exportedDataset;
					}else{

						if(!$scope.flagCategory){
							$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.missingcategory"));
						}
					}

			}, function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
			});
		}
	}

	$scope.save = function(ev){
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectmode"));
		}else if($scope.datasetSelected.length==0){
			$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectds"));
		}else{
			var config={
					"ds": $scope.datasetSelected,
					"type":$scope.typeSaveMenu

			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog","importCatalog",config)
			.then(function(response, status, headers, config) {
					$scope.showAction(sbiModule_translate.load("sbi.importusers.importuserok"));
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
				$scope.showAction("Error");
			})

		}
	}

	
	$scope.setTypeSaveMenu = function(type){
		$scope.typeSaveMenu = type;
	}
	
	
	$scope.associateddatasource = function(ev){
		if($scope.typeSaveMenu == ""){
			//if not selected a mode
			$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectmode"));
		}else if($scope.datasetSelected.length==0){
			$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.selectds"));
		}else{
			var config={
					"ds": $scope.datasetSelected,
					"type":$scope.typeSaveMenu

			}
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/catalog","associateDataSource",config)
			.then(function(response, status, headers, config) {
					
				
				
				if(response.data.STATUS=="OK"){
					importExportDocumentModule_importConf.datasources.currentDatasources=response.data.currentDatasources;
					importExportDocumentModule_importConf.datasources.exportedDatasources=response.data.exportedDatasources;
					importExportDocumentModule_importConf.datasources.associatedDatasources=response.data.associatedDatasources;
					$scope.stepControl.insertBread({name: $scope.translate.load('sbi.impexp.exportedDS')})
				}
					
					
					
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
				$scope.showAction("Error");
			})

		}
	}


	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}

	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}
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