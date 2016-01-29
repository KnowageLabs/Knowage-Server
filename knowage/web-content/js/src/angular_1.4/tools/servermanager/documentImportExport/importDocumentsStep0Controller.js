angular.module('importExportDocumentModule').controller('importControllerStep0', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast",importStep0FuncController]);

function importStep0FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast) {
	
	
	
	$scope.importFile = function(item){

			var fd = new FormData();
				fd.append('exportedArchive', importExportDocumentModule_importConf.fileImport.file);
				fd.append('importAssociationKind', importExportDocumentModule_importConf.associations);
				if(importExportDocumentModule_importConf.associations!="noAssociations"){
					fd.append('hidAssId', importExportDocumentModule_importConf.fileAssociation.name);
					}

				sbiModule_restServices.post("1.0/serverManager/importExport/document", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
				.success(function(data, status, headers, config) {
					console.log("role--->",data)
					if(data.STATUS=="NON OK"){
						$scope.showToast(data.ERROR,4000);
					}
					else if(data.STATUS=="OK"){
						importExportDocumentModule_importConf.roles.currentRoles=data.currentRoles;
						importExportDocumentModule_importConf.roles.exportedRoles=data.exportedRoles;
						importExportDocumentModule_importConf.roles.associatedRoles=data.associatedRoles;
//						importExportDocumentModule_importConf.roles.exportedRoles.push({"id":123,"name":"/spagobi/lucY","description":"/spagobi/lucy","roleTypeCD":"ADMIN","code":"","roleTypeID":32,"organization":null,"defaultRole":false,"roleMetaModelCategories":null,"ableToSeeNotes":false,"ableToSaveRememberMe":false,"ableToEditWorksheet":false,"ableToSeeMetadata":false,"ableToSeeSubobjects":false,"ableToSeeViewpoints":false,"ableToBuildQbeQuery":false,"ableToSeeSnapshots":false,"ableToSaveMetadata":false,"ableToSendMail":false,"ableToSaveSubobjects":false,"ableToSeeMyData":false,"ableToEditAllKpiComm":false,"ableToCreateSocialAnalysis":false,"ableToEditMyKpiComm":false,"ableToSeeDocumentBrowser":false,"ableToSeeSubscriptions":false,"ableToSeeToDoList":false,"ableToDeleteKpiComm":false,"ableToViewSocialAnalysis":false,"ableToSeeFavourites":false,"ableToDoMassiveExport":false,"ableToCreateDocuments":false,"ableToHierarchiesManagement":false,"ableToManageUsers":false,"ableToEnableDatasetPersistence":false,"ableToManageGlossaryBusiness":false,"ableToManageGlossaryTechnical":false,"ableToSaveIntoPersonalFolder":false,"ableToEnableFederatedDataset":false});
						
						$scope.stepControl.insertBread({name: sbiModule_translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')})
					} 
				})
				.error(function(data, status, headers, config) {
					$scope.showToast(data,4000);
					
				});
	}
	
	
	$scope.isInvalidImportStep0Form=function(){
		if( importExportDocumentModule_importConf.fileImport.file===undefined || importExportDocumentModule_importConf.fileImport.fileName.length == 0) return true;
		if(importExportDocumentModule_importConf.associations!="noAssociations"){
			if(importExportDocumentModule_importConf.fileAssociation==""){
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
				browser : sbiModule_device.browser,
				showToast:$scope.showToast,
				sbiModule_download:sbiModule_download
			},
			preserveScope : true,
			clickOutsideToClose:true
		})
		.then(function(associationSelected){
			importExportDocumentModule_importConf.fileAssociation = associationSelected;
		}, function(){

		});
	}

	$scope.dialogController =function ($scope, $mdDialog, translate,browser,showToast,sbiModule_download) {
		$scope.translate = translate;
		$scope.showToast=showToast;
		$scope.viewInsertForm=false;
		$scope.associationFile = {};
		//"[{"id":"quiiiii","name":"quiiiii","description":"assadsad","dateCreation":1452788706189}]"
		$scope.associations = [];
		$scope.associationSelected = {};
		$scope.addAssociationForm={"file":{}};
		$scope.isIE= (browser.name == 'internet explorer');

		$scope.SpeedMenuOpt  = [
		                        {
				            		label : 'delete',
				            		 icon:'fa fa-minus-circle' ,  
				            		backgroundColor:'red',  
				            		 color:'black',		
				            		action : function(item,event) {
//				            				myFunction(event,item);
				            			$scope.deleteAssociationsFile(item);
				            		 }
				            	}, {
				            		label : 'download',
				            		 icon:'fa fa-download' ,  
				            		backgroundColor:'green',  
				            		 color:'black',		
				            		action : function(item,event) {
//				            				myFunction(event,item);
				            			$scope.downloadAssociationsFile(item);
				            		 }
				            	}
		            ];

		
		
		sbiModule_restServices.get("1.0/serverManager/importExport/document", 'associationsList/get')
		.success(function(data, status, headers, config) {
			$scope.associations=data.associationsList;
		})
		.error(function(data, status, headers, config) {
			$scope.showToast("Errore nel recuperare i file di associazione",4000);
			console.log()
		});
		
		
		
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

		$scope.saveListAssociationFile = function(){
			
			var fd = new FormData();
			fd.append('name', $scope.addAssociationForm.name);
			fd.append('description', $scope.addAssociationForm.description);
			fd.append('file', $scope.addAssociationForm.file.file);
			
			sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associationsList/upload', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
			.success(function(data, status, headers, config) {
				
				if(data.STATUS=="NON OK"){
					$scope.showToast(data.ERROR,4000);
					
				}
				else if(data.STATUS=="OK"){
					$scope.associations.push(data.associationsFile);
					$scope.toogleViewInsertForm ();
					$scope.addAssociationForm={"file":{}};
				}
				
				
			})
			.error(function(data, status, headers, config) {
				$scope.showToast(data,4000);
				
			});
			
			
		}

		$scope.downloadAssociationsFile=function(item){
			
			sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associationsList/download',{'id':item.name},{"responseType": "arraybuffer"})
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					$scope.showToast(data.errors[0].message,4000);
				}else if (data.hasOwnProperty("NON OK")) {
					$scope.showToast(data.ERROR,4000);
				}else if(status==200){
					sbiModule_download.getBlob(data,item.name,'application/xml','xml');
				
				}
			})
			.error(function(data, status, headers, config) {
				$scope.showToast(data,4000);
				
			});
		}
		
$scope.deleteAssociationsFile=function(item){
	sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associationsList/delete',{'id':item.name})
		.success(function(data, status, headers, config) {
			console.log(data)
				$scope.associations.splice($scope.associations.indexOf(item),1);
			})
		.error(function(data, status, headers, config) {
			$scope.showToast(data,4000);
			
		});
		}
		$scope.selectAssociation = function() {
			$mdDialog.hide($scope.associationSelected);
		};
	}

	
	$scope.downloadAssociationsFile=function(){

		var data={"FILE_NAME":importExportDocumentModule_importConf.associationsFileName,
				"FOLDER_NAME":importExportDocumentModule_importConf.folderName};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/document","downloadAssociationsFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				$scope.showToast(data.errors[0].message,4000);
			}else if(status==200){
				sbiModule_download.getBlob(data,importExportDocumentModule_importConf.associationsFileName,'application/xml','xml');
//				$scope.flags.viewDownload = false
			}
		}).error(function(data, status, headers, config) {
			$scope.showToast("ERRORS "+status,4000);
		})
	
	};
	
	$scope.downloadLogFile=function(){ 
		var data={"FILE_NAME":importExportDocumentModule_importConf.logFileName,
				"FOLDER_NAME":importExportDocumentModule_importConf.folderName};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/document","downloadLogFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				$scope.showToast(data.errors[0].message,4000);
			}else if(status==200){
				sbiModule_download.getBlob(data,importExportDocumentModule_importConf.logFileName,'application/log','log');
//				$scope.flags.viewDownload = false
			}
		}).error(function(data, status, headers, config) {
			$scope.showToast("ERRORS "+status,4000);
		}) 
	};
	
	$scope.saveAssociationsFile=function(){
		 $mdDialog.show({
		      controller: function($scope, $mdDialog, translate) { 
		          $scope.translate = translate;
		          $scope.closeDialog = function() {
		            $mdDialog.hide();
		          }
		        },
		      template:
		           '<md-dialog aria-label="List dialog">' +
		           '  <md-dialog-content>'+
		           '   <md-input-container>' +
		           '  <label> {{translate.load("sbi.generic.name");}}</label>' +
		           '  <input ng-model="name">' +
		           ' </md-input-container>' +
		           '    <md-input-container>' +
		           '  <label> {{translate.load("sbi.generic.descr");}}</label>' +
		           '    <input ng-model="description">' +
		           '  </md-input-container>' +
		           '<div layout="row" layout-wrap>'+
		           '    <md-button flex="40" ng-click="closeDialog()" class="md-primary">' +
		           '     {{translate.load("sbi.general.cancel");}}' +
		           '    </md-button>' + 
		          ' <md-button flex="40" ng-click="closeDialog()" class="md-primary">' +
		           '     {{translate.load("sbi.generic.update");}}' +
		           '    </md-button>' +
		           '  </md-dialog-content>' + 
		           '</md-dialog>',
		      parent: angular.element(document.body),
		      locals: {
		           translate: $scope.translate
		         },
		      clickOutsideToClose:false
		    })
		    .then(function(answer) {
		      $scope.status = 'You said the information was "' + answer + '".';
		    }, function() {
		      $scope.status = 'You cancelled the dialog.';
		    });
		 
	};
}
