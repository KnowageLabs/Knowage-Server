var app = angular.module('importExportMenuModule', 
		['ngMaterial', 'sbiModule', 'angular_table',
		 'document_tree', 'componentTreeModule', 'file_upload', 'bread_crumb']);

app.factory('importExportMenuModule_importConf', function() {
	return {
		fileImport : {},
		associationsFileImport : {},
		associations : 'noAssociations',
		fileAssociation : '',
		roles : {
			currentRoles : [],
			exportedRoles : [],
			associationsRole : {}
		}
	};
});

app.controller('importExportMenuController', 
		['sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 '$mdToast',
		 impExpFuncController]);

function impExpFuncController(
		sbiModule_download,
		sbiModule_device,
		$scope,
		$mdDialog,
		$timeout,
		sbiModule_logger,
		sbiModule_translate,
		sbiModule_restServices,
		sbiModule_config,
		$mdToast) {
	
	sbiModule_translate.addMessageFile('component_impexp_messages');
	$scope.translate = sbiModule_translate;
};

app.controller('exportController', 
		['$http',
		 'sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 '$mdToast',
		 exportFuncController]);

function exportFuncController(
		$http,
		sbiModule_download,
		sbiModule_device,
		$scope, 
		$mdDialog, 
		$timeout, 
		sbiModule_logger, 
		sbiModule_translate, 
		sbiModule_restServices,
		sbiModule_config,
		$mdToast) {
	
//	$scope.pathRest = {
//			vers: '2.0',
//			folders : 'folders',
//			includeDocs : 'includeDocs=true',
//			urlTest : sbiModule_config.contextName + '/servlet/AdapterHTTP'
//	};
	
	$scope.pathRest = { // /restful-services/1.0/menu/enduser
		restfulServices : '1.0',
		menuPath : 'menu/enduser',
	};
	
	$scope.restServices = sbiModule_restServices;
	$scope.download = sbiModule_download;
	$scope.log = sbiModule_logger;
	$scope.selected = [] ;
	$scope.customs = [];
	
	$scope.fileAssociation = {};
	$scope.flags = {
		waitExport : false,
		enableDownload : false
	};
	
	$scope.checkboxs = {
		exportSubObj : false,
		exportSnapshots : false
	};
	
//	$scope.restServices.get($scope.pathRest.menuPath)
//	sbiModule_restServices.get(
//			$scope.pathRest.restfulServices,
//			$scope.pathRest.menuPath)
//	.success(function(data, status, headers, config){
//		if (data.errors === undefined){
//			$scope.customs = data.customMenu[0].menu || [];
//		}else{
//			$scope.customs = [];
//		}
//	})
//	.error(function(data, status){
//		$scope.customs = data;
//		$scope.log.error('GET RESULT error of ' + data + ' with status :' + status);
//	});
	
	$scope.exportFiles = function(){
		var config = {
//			'DOCUMENT_ID_LIST':[],
			'EXPORT_FILE_NAME': $scope.exportName,
//			'FILE_NAME': $scope.exportName,
//			'EXPORT_SUB_OBJ': $scope.checkboxs.exportSubObj,
//			'EXPORT_SNAPSHOT': $scope.checkboxs.exportSnapshots
		};
		
//		for (var i = 0; i < $scope.selected.length; i++){
//			if ($scope.selected[i].type == 'biObject'){
//				config.DOCUMENT_ID_LIST.push('' + $scope.selected[i].id);
//			}
//		}
//		
		$scope.flags.waitExport = true;
		
		sbiModule_restServices.post('1.0/serverManager/importExport/menu', 'export', config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty('errors')) {
				showToast(data.errors[0].message,4000);
			}else if(data.hasOwnProperty('STATUS') && data.STATUS=='OK'){
				$scope.flags.enableDownload = true;
				$scope.downloadedFileName = $scope.exportName;
			}
			$scope.flags.waitExport = false;
		}).error(function(data, status, headers, config) {
			$scope.flags.waitExport = false;
			showToast('ERRORS ' + status,4000);
		})
	};
	
	$scope.submitDownForm = function(form){
		$scope.flags.submitForm= true;
	};
	
	$scope.toggleEnableDownload = function(){
		$scope.flags.enableDownload = !$scope.flags.enableDownload;
	};
	
	$scope.downloadFile= function(){
		var data = {'FILE_NAME': $scope.downloadedFileName };
		var config = {'responseType': 'arraybuffer'};
		sbiModule_restServices.post('1.0/serverManager/importExport/document','downloadExportFile',data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty('errors')) {
				showToast(data.errors[0].message,4000);
			}else if(status==200){
				$scope.download.getBlob(data,$scope.exportName,'application/zip','zip');
				$scope.flags.enableDownload = false
			}
		}).error(function(data, status, headers, config) {
			showToast('ERRORS ' + status,4000);
		})
	};
	
	$scope.showAlert = function (title, message){
		$mdDialog.show( 
				$mdDialog.alert()
				.parent(document.body)
				.clickOutsideToClose(true)
				.title(title)
				.textContent(message) //FROM angular material 1.0 
				.ok('Ok')
		);
	};
	
	$scope.debug= function(){
	};
	
	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time;
		
		$mdToast.show(
				$mdToast
				.simple()
				.content(text)
				.position('top')
				.action('OK')
				.highlightAction(false)
				.hideDelay(timer)
		);
	};
};

app.controller('importController', 
		['sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 '$mdToast',
		 'importExportMenuModule_importConf',
		 importFuncController]);

function importFuncController(
		sbiModule_download,
		sbiModule_device,
		$scope,
		$mdDialog,
		$timeout,
		sbiModule_logger,
		sbiModule_translate,
		sbiModule_restServices,
		sbiModule_config,
		$mdToast,
		importExportMenuModule_importConf) {
	
	$scope.stepItem = [{name:'step0'}];
	$scope.selectedStep = 0;
	$scope.stepControl;
	$scope.IEDConf = importExportMenuModule_importConf;
};
