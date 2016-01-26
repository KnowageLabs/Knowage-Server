angular.module('importExportMenuModule').controller('importMenuStep0', 
		['sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 'importExportMenuModule_importConf',
		 '$mdToast',
		 importStep0FuncController]);

function importStep0FuncController(
		sbiModule_download,
		sbiModule_device,
		$scope,
		$mdDialog,
		$timeout, 
		sbiModule_logger, 
		sbiModule_translate, 
		sbiModule_restServices,
		sbiModule_config,
		importExportMenuModule_importConf,
		$mdToast) {
	
	$scope.importFile = function(item){

//		console.log("step'0");
		var fd = new FormData();
		fd.append('exportedArchive', importExportMenuModule_importConf.fileImport.file);
		fd.append('importAssociationKind', importExportMenuModule_importConf.associations);
		if(importExportMenuModule_importConf.associations!='noAssociations'){
			if(importExportMenuModule_importConf.associationsFileImport.hasOwnProperty('file') && importExportMenuModule_importConf.associationsFileImport.file!=undefined){
				fd.append('associationsFile', importExportMenuModule_importConf.associationsFileImport.file);
			}else{
				fd.append('hidAssId', importExportMenuModule_importConf.fileAssociation.name);
			}
		}

		sbiModule_restServices.post('1.0/serverManager/importExport/document', 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
		.success(function(data, status, headers, config) {
//				console.log('role--->',data);
			if(data.STATUS == 'NON OK'){
				$mdToast.show(
						$mdToast.simple()
						.content('data.ERROR')
						.position('top')
						.action('OK')
						.highlightAction(false)
						.hideDelay(5000));
			}
			else if(data.STATUS=='OK'){
				importExportMenuModule_importConf.roles.currentRoles = data.currentRoles;
				importExportMenuModule_importConf.roles.exportedRoles = data.exportedRoles;
				$scope.stepControl.insertBread({name:'step1'})
			}
		})
		.error(function(data, status, headers, config) {
			$mdToast.show(
					$mdToast.simple()
					.content('errore')
					.position('top')
					.action('OK')
					.highlightAction(false)
					.hideDelay(5000));
			
		});
	};
	
	$scope.isInvalidImportStep0Form=function(){
		if(importExportMenuModule_importConf.fileImport.file === undefined 
				|| importExportMenuModule_importConf.fileImport.fileName.length == 0) {
			return true;
		}
		
		if(importExportMenuModule_importConf.associations != 'noAssociations'){
			if((importExportMenuModule_importConf.associationsFileImport.hasOwnProperty('file') && importExportMenuModule_importConf.associationsFileImport.file==undefined) && importExportMenuModule_importConf.fileAssociation==''){
				return true;
			}
		}
		return false;
	};
	
	$scope.listAssociation = function(){
		$mdDialog.show({
			controller: $scope.dialogController ,
			templateUrl: '/knowage/js/src/angular_1.4/tools/servermanager/documentImportExport/templates/importExportListAssociation.html',
			parent: angular.element(document.body),
			locals : {
				translate : $scope.translate,
				browser : sbiModule_device.browser
			},
			preserveScope : true,
			clickOutsideToClose:true
		})
		.then(function(associationSelected){
			importExportMenuModule_importConf.fileAssociation = associationSelected;
		}, function(){

		});
	}

	$scope.dialogController =function ($scope, $mdDialog, translate,browser) {
		$scope.translate = translate;
		$scope.viewInsertForm=false;
		$scope.associationFile = {};
		$scope.associations = [];
		$scope.associationSelected = {};
		$scope.form={'file':{}};
		$scope.isIE= (browser.name == 'internet explorer');

		
		sbiModule_restServices.get('1.0/serverManager/importExport/document', 'getAssociationsList')
		.success(function(data, status, headers, config) {
			$scope.associations=data.associationsList;
			
		})
		.error(function(data, status, headers, config) {});
		
		
		
		$scope.toogleViewInsertForm = function (){
			$scope.viewInsertForm = !$scope.viewInsertForm;
			if (!$scope.viewInsertForm){
				//reset values
				$scope.associationSelected = {};
				$scope.associationFile = {};
			}
		}
		$scope.cancel = function() {
			$mdDialog.cancel();
		};

		$scope.save = function(){
			$scope.toogleViewInsertForm ();
		}

		$scope.selectAssociation = function() {
			$mdDialog.hide($scope.associationSelected);
		};
	}

}
