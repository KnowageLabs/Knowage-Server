var app = angular.module('impExpUsers', [ 'ngMaterial', 'ui.tree',
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
		 resetData: function() {  
		    	 current_data = angular.copy( default_values,current_data); 
		     } 
	};
	 default_values.resetData();
	  return current_data;
});

app.controller('userImportExportController', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);
app.controller('userExportController', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","sbiModule_messaging", userExportFuncController ]);
app.controller('userImportController', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","$mdToast","importExportDocumentModule_importConf",userImportFuncController]);


function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	//variables
	sbiModule_translate.addMessageFile("component_impexp_messages");
	$scope.translate=sbiModule_translate;
	$scope.users = [];
	$scope.usersSelected = [];
	
	
	$scope.viewDownload =false;
	$scope.download = sbiModule_download;
	$scope.uploadProcessing = [];
	$scope.upload = [];
	$scope.wait = false;

	
	$scope.flagShowUser=false;
	
	
	
	$scope.loadAllUsers = function(){
		sbiModule_restServices.promiseGet("2.0", 'users').then(
				function(response, status, headers, config) {
						$scope.users = response.data;
				},function(response, status, headers, config) {
					sbiModule_restServices.errorHandler(response.data,"");
				})
	};
	$scope.loadAllUsers();
	
	
	
	
	
	$scope.showConfirm = function() {
	    // Appending dialog to document.body to cover sidenav in docs app
	    var confirm = $mdDialog.alert()
	          .title(sbiModule_translate.load("sbi.importusers.importfailed"))
	          .ariaLabel('Lucky day')
	          .ok('Ok')
	         
	    $mdDialog.show(confirm).then(function() {

	    }, function() {
	      
	    });
	  };
	
	
	  $scope.indexInList=function(item, list) {

			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.userId==item.userId){
					return i;
				}
			}

			return -1;
		};
	 
//	$scope.showAction = function(text) {
//		var toast = $mdToast.simple()
//		.content(text)
//		.action('OK')
//		.highlightAction(false)
//		.hideDelay(3000)
//		.position('top')
//
//		$mdToast.show(toast).then(function(response) {
//
//			if ( response == 'ok' ) {
//
//
//			}
//		});
//	};
	
}


function userExportFuncController(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging) {
	$scope.flagCheck=false;
	$scope.nameExport="";
	$scope.exportCheckboxs={};
	$scope.filterDate;
	
	$scope.filterUsers = function(){
		if($scope.filterDate!=undefined){
			sbiModule_restServices.promiseGet("2.0", 'users', "dateFilter="+$scope.filterDate).then(
					function(response, status, headers, config) {
							$scope.users = response.data;
					},function(response, status, headers, config) {
						sbiModule_restServices.errorHandler(response.data,"");
					})
		
		}else{
			$scope.removeFilter();
		}
	}
	
	$scope.removeFilter = function(){
		sbiModule_restServices.promiseGet("2.0", 'users').then(
				function(response, status, headers, config) {
						$scope.users = response.data;
				},function(response, status, headers, config) {
					sbiModule_restServices.errorHandler(response.data,"");
				})
	}
	
	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			$scope.usersSelected=[];
			for(var i=0;i<$scope.users.length;i++){
				$scope.usersSelected.push($scope.users[i]);
			}
		}else{
			$scope.flagCheck=false;
			$scope.usersSelected=[];
		} 
	};
	
	$scope.prepare = function(ev){
		
		if($scope.usersSelected.length == 0){
			//$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingcheck"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missingcheck"),"");
		} else if($scope.nameExport==""){
			//$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingnamefile"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missingnamefile"),"");
		}else{
			// download zip
			var config={"USERS_LIST":$scope.usersSelected ,
					"EXPORT_FILE_NAME":$scope.nameExport,
					"EXPORT_SUB_OBJ":$scope.exportCheckboxs.exportSubObj,
					"EXPORT_SNAPSHOT":$scope.exportCheckboxs.exportSnapshots,
					"EXPORT_PERSONAL_FOLDER":$scope.exportCheckboxs.exportPersonalFolder};
			$scope.wait = true;
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/users", 'export',config).then(
					function(response, status, headers, config) {
						 
						if(response.data.hasOwnProperty("STATUS") && response.data.STATUS=="OK"){
							//da usare poi 
							$scope.downloadFile();
						}
						 
						$scope.wait = false;
					},function(response, status, headers, config) {
						sbiModule_restServices.errorHandler(response.data,"");
						$scope.wait = false;
					})
		}
		
	}
	
		
	$scope.downloadFile= function(){
			var data={"FILE_NAME":$scope.nameExport};
			var config={"responseType": "arraybuffer"};
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/users","downloadExportFile",data,config)
			.then(function(response, status, headers, config) {
				 
					$scope.download.getBlob(response.data,$scope.nameExport,'application/zip','zip');
					$scope.viewDownload = false;
					$scope.wait = false;
					//$scope.showAction(sbiModule_translate.load("sbi.importusers.downloadOK"));
					sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importusers.downloadOK"),"");
				 
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"");
				$scope.wait = false;
			})
		}
	
	 
//export utilities
	
	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);

		if(index != -1){
			$scope.usersSelected.splice(index,1);
		}else{
			$scope.usersSelected.push(item);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item, list)>-1;

	}; 
	
}
function userImportFuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,$mdToast,importExportDocumentModule_importConf) {
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
		var alert = $mdDialog.alert()
				.title(titleFin)
				.content(text)
				.ariaLabel('error import') 
				.ok('OK');
		$mdDialog.show(alert).then(function() {
			$scope.stepControl.resetBreadCrumb();
			$scope.stepControl.insertBread({name: sbiModule_translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')});
			$scope.finishImport();
		} ); 
	}
//	$scope.currentRoles=[];
//	$scope.exportedRoles=[];
//	$scope.exportedUser=[];
//	$scope.exportingUser = [];
//	$scope.selectedUser = [];
	
	
	
}