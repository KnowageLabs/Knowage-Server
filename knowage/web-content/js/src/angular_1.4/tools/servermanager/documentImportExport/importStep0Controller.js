angular.module('importExportDocumentModule').controller('importControllerStep0', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast",importStep0FuncController]);

function importStep0FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast) {
	
	
	
	$scope.importFile = function(item){

		console.log("step'0")
			var fd = new FormData();
				fd.append('exportedArchive', importExportDocumentModule_importConf.fileImport.file);
				fd.append('importAssociationKind', importExportDocumentModule_importConf.associations);
				if(importExportDocumentModule_importConf.associations!="noAssociations"){
					if(importExportDocumentModule_importConf.associationsFileImport.hasOwnProperty("file") && importExportDocumentModule_importConf.associationsFileImport.file!=undefined){
						fd.append('associationsFile', importExportDocumentModule_importConf.associationsFileImport.file);
					}else{
						fd.append('hidAssId', importExportDocumentModule_importConf.fileAssociation.name);
					}
				}

				sbiModule_restServices.post("1.0/serverManager/importExport/document", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
				.success(function(data, status, headers, config) {
					console.log("role--->",data)
					if(data.STATUS=="NON OK"){
						$mdToast.show($mdToast.simple().content("data.ERROR").position('top').action(
						'OK').highlightAction(false).hideDelay(5000));
					}
					else if(data.STATUS=="OK"){
						importExportDocumentModule_importConf.roles.currentRoles=data.currentRoles;
						importExportDocumentModule_importConf.roles.exportedRoles=data.exportedRoles;
						$scope.stepControl.insertBread({name:"step1"})
					}
					
					
				})
				.error(function(data, status, headers, config) {
					$mdToast.show($mdToast.simple().content("errore").position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
					
				});
	}
	
	
	$scope.isInvalidImportStep0Form=function(){
		if( importExportDocumentModule_importConf.fileImport.file===undefined || importExportDocumentModule_importConf.fileImport.fileName.length == 0) return true;
		if(importExportDocumentModule_importConf.associations!="noAssociations"){
			if((importExportDocumentModule_importConf.associationsFileImport.hasOwnProperty("file") && importExportDocumentModule_importConf.associationsFileImport.file==undefined) && importExportDocumentModule_importConf.fileAssociation==""){
				return true;
			}
		}
		
		return false;
	}
	
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
			importExportDocumentModule_importConf.fileAssociation = associationSelected;
		}, function(){

		});
	}

	$scope.dialogController =function ($scope, $mdDialog, translate,browser) {
		$scope.translate = translate;
		$scope.viewInsertForm=false;
		$scope.associationFile = {};
		//"[{"id":"quiiiii","name":"quiiiii","description":"assadsad","dateCreation":1452788706189}]"
		$scope.associations = [];
		$scope.associationSelected = {};
		$scope.form={"file":{}};
		$scope.isIE= (browser.name == 'internet explorer');

		
		sbiModule_restServices.get("1.0/serverManager/importExport/document", 'getAssociationsList')
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
