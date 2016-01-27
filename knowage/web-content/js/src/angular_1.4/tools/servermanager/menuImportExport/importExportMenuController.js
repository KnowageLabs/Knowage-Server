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
	
	$scope.exportFiles = function(){
		$scope.flags.waitExport = true;
		
		sbiModule_restServices.post('1.0/serverManager/importExport/menu', 'export', 
			{'EXPORT_FILE_NAME': $scope.exportName}, 
			{'responseType': 'arraybuffer'}
			)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty('errors')) {
				showToast(data.errors[0].message,4000);
			
			}else if(status==200){
				$scope.flags.enableDownload = true;
				$scope.downloadedFileName = $scope.exportName;
				
				$scope.download.getBlob(data, $scope.exportName, 'application/zip', 'zip');
				$scope.flags.enableDownload = false
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
