angular.module('importExportDocumentModule').controller('importControllerStep0', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast","sbiModule_messaging",importStep0FuncController]);

function importStep0FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast,sbiModule_messaging) {
	
	
	
	$scope.importFile = function(item){

			var fd = new FormData();
				fd.append('exportedArchive', importExportDocumentModule_importConf.fileImport.file);
				fd.append('importAssociationKind', importExportDocumentModule_importConf.associations);
				if(importExportDocumentModule_importConf.associations!="noAssociations"){
					fd.append('hidAssId', importExportDocumentModule_importConf.fileAssociation.name);
					}

//				sbiModule_restServices.post("1.0/serverManager/importExport/document", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
//				.success(function(data, status, headers, config) {
//				 	if(data.STATUS=="NON OK"){
//						$scope.showToast(data.ERROR,4000);
//					}
//					else if(data.STATUS=="OK"){
//						importExportDocumentModule_importConf.roles.currentRoles=data.currentRoles;
//						importExportDocumentModule_importConf.roles.exportedRoles=data.exportedRoles;
//						importExportDocumentModule_importConf.roles.associatedRoles=data.associatedRoles;
//						
//						$scope.stepControl.insertBread({name: sbiModule_translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
//					} 
//				})
//				.error(function(data, status, headers, config) {
//					$scope.showToast(data,4000);
//					
//				});
				
				if(!$scope.flags) $scope.flags = {}
				
				sbiModule_restServices.promisePost("1.0/serverManager/importExport/document", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
				.then(function(response) {
					if(response.data.STATUS=="NON OK"){
						sbiModule_restServices.errorHandler("Error upload file" ,"sbi.generic.toastr.title.error");
					}else if(response.data.STATUS=="OK"){
							importExportDocumentModule_importConf.roles.currentRoles=response.data.currentRoles;
							importExportDocumentModule_importConf.roles.exportedRoles=response.data.exportedRoles;
							importExportDocumentModule_importConf.roles.associatedRoles=response.data.associatedRoles;
							importExportDocumentModule_importConf.objects.notImportable=response.data.notImportableObjs;
							$scope.stepControl.insertBread({name: sbiModule_translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
					}
				}, function(response) {
					$scope.flags.waitExport=false;
					sbiModule_restServices.errorHandler(response.data ,"sbi.generic.toastr.title.error");
				});
				
				
				
				
				
	}
	
	
	$scope.isInvalidImportStep0Form=function(){
		if( importExportDocumentModule_importConf.fileImport.file === undefined 
				|| importExportDocumentModule_importConf.fileImport === undefined
				|| importExportDocumentModule_importConf.fileImport.fileName === undefined
				|| importExportDocumentModule_importConf.fileImport.fileName.length == 0) return true;
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
			templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/servermanager/documentImportExport/templates/importExportListAssociation.html',
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
			sbiModule_restServices.errorHandler("Errore nel recuperare i file di associazione","sbi.generic.toastr.title.error");
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
					sbiModule_restServices.errorHandler(data.ERROR,"sbi.generic.toastr.title.error");
				}
				else if(data.STATUS=="OK"){
					$scope.associations.push(data.associationsFile);
					$scope.toogleViewInsertForm ();
					$scope.addAssociationForm={"file":{}};
				}
				
				
			})
			.error(function(data, status, headers, config) {
				sbiModule_restServices.errorHandler(data,"sbi.generic.toastr.title.error");
			});
			
			
		}

		$scope.downloadAssociationsFile=function(item){
			
			sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associationsList/download',{'id':item.name},{"responseType": "arraybuffer"})
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
				}else if (data.hasOwnProperty("NON OK")) {
					sbiModule_restServices.errorHandler(data.ERROR,"sbi.generic.toastr.title.error");
				}else if(status==200){
					sbiModule_download.getBlob(data,item.name,'application/xml','xml');
				
				}
			})
			.error(function(data, status, headers, config) {
				sbiModule_restServices.errorHandler(data,"sbi.generic.toastr.title.error");
				
			});
		}
		
$scope.deleteAssociationsFile=function(item){
	sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associationsList/delete',{'id':item.name})
		.success(function(data, status, headers, config) {
			console.log(data)
				$scope.associations.splice($scope.associations.indexOf(item),1);
			})
		.error(function(data, status, headers, config) {
			sbiModule_restServices.errorHandler(data,"sbi.generic.toastr.title.error");
			
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
				sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
			}else if(status==200){
				sbiModule_download.getBlob(data,importExportDocumentModule_importConf.associationsFileName,'application/xml','xml');
//				$scope.flags.viewDownload = false
			}
		}).error(function(data, status, headers, config) {
			sbiModule_restServices.errorHandler(data,"sbi.generic.toastr.title.error");
		})
	
	};
	
	$scope.downloadLogFile=function(){ 
		var data={"FILE_NAME":importExportDocumentModule_importConf.logFileName,
				"FOLDER_NAME":importExportDocumentModule_importConf.folderName};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/document","downloadLogFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
			}else if(status==200){
				sbiModule_download.getBlob(data,importExportDocumentModule_importConf.logFileName,'application/log','log');
//				$scope.flags.viewDownload = false
			}
		}).error(function(data, status, headers, config) {
			sbiModule_restServices.errorHandler("ERRORS","sbi.generic.toastr.title.error");
		}) 
	};
	
	$scope.saveAssociationsFile=function(){
		 $mdDialog.show({
		      controller: function($scope, $mdDialog, translate) { 
		          $scope.translate = translate;
		          $scope.closeDialog = function() {
		            $mdDialog.hide();
		          };
		          $scope.saveAssFile = function() {
		        	  
		      		var data={"FILE_NAME":importExportDocumentModule_importConf.logFileName,
		      				"FOLDER_NAME":importExportDocumentModule_importConf.folderName,
		      				"NAME":$scope.name,
		      				"DESCRIPTION":$scope.description}; 
		      		sbiModule_restServices.post("1.0/serverManager/importExport/document","associationsList/save",data)
		      		.success(function(data, status, headers, config) {
		      			if (data.hasOwnProperty("errors")) {
		      				sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
		      			}else if(status==200){
		      				$mdDialog.hide(); 
		      			}
		      		}).error(function(data, status, headers, config) {
		      			sbiModule_restServices.errorHandler("ERRORS","sbi.generic.toastr.title.error");
		      		}) 
		      	
			          }
		          
		        },
		      template:
		           '<md-dialog aria-label="List dialog">' +
		          ' <md-toolbar>' +
		         '  <div class="md-toolbar-tools">' +
		           '  <h2>{{translate.load("impexp.saveAss","component_impexp_messages");}}</h2>' +
		           '  <span flex></span>' +
		          ' </div>' +
		        ' </md-toolbar>' +
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
		          ' <md-button flex="40" ng-click="saveAssFile()" class="md-primary" ng-disabled="name==undefined || name==\'\'">' +
		           '     {{translate.load("sbi.generic.update");}}' +
		           '    </md-button>' +
		           '  </md-dialog-content>' + 
		           '</md-dialog>',
		      parent: angular.element(document.body),
		      locals: {
		           translate: $scope.translate
		         },
		      clickOutsideToClose:false
		    }) ;
		 
	};
}

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
