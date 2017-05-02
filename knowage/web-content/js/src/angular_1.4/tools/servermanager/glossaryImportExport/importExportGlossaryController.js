var app = angular.module('impExpGlossary', 
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
app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
app.controller('glossaryImportController', 
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
		 'sbiModule_messaging',
		 glossaryImportControllerFunc]);

function glossaryImportControllerFunc(sbiModule_download,sbiModule_device,$scope,$mdDialog,	$timeout,sbiModule_logger,sbiModule_translate,
		sbiModule_restServices,sbiModule_config,$mdToast,sbiModule_messaging) {
	$scope.translate = sbiModule_translate;
	$scope.nameExport = "";
	$scope.wait = false;
	$scope.flagCheck = false;
	$scope.glossarySelected= [];
	$scope.glossary = [];
	$scope.download = sbiModule_download;
	$scope.glossaryImported = [];
	$scope.glossaryPresentIdDB  =[];
	//import variables
	$scope.importFile = {};
	$scope.typeSaveMenu = "Missing";
	$scope.selectGlossaryToImport = [];
	$scope.importingGlossary = [];
	$scope.filterDate;
	//export glossary
	$scope.loadGlossaryList = function(){
		
		sbiModule_restServices.promiseGet("1.0/glossary","listGlossary")
		.then(function(response){ 
			angular.copy(response.data,$scope.glossary);

		},function(response){
		});

	}
	
	$scope.loadGlossaryList();
	
	
	$scope.filterGlossary = function(){
		if($scope.filterDate!=undefined){
			sbiModule_restServices.promiseGet("1.0/glossary","listGlossary","dateFilter="+$scope.filterDate)
			.then(function(response){ 
				angular.copy(response.data,$scope.glossary);

			},function(response){
			});
		}else{
			$scope.removeFilter();
		}
	}
	
	$scope.removeFilter = function(){
		sbiModule_restServices.promiseGet("1.0/glossary","listGlossary")
		.then(function(response){ 
			angular.copy(response.data,$scope.glossary);

		},function(response){
		});
	}
	$scope.prepare = function(ev){
		
		if($scope.glossarySelected.length == 0){
			//$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingcheck"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missingcheck"),"");
			 
		} else if($scope.nameExport==""){
			//$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingnamefile"));
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missingnamefile"),"");
		}else{
			// download zip	
			$scope.wait = true;
			var config={"GLOSSARY_LIST":$scope.glossarySelected ,
					"EXPORT_FILE_NAME":$scope.nameExport,
					};
			sbiModule_restServices.promisePost("1.0/serverManager/importExport/glossary","export",config)
			.then(function(response){ 
				$scope.downloadFile();

			},function(response){
			});
		}
	}
	
	$scope.downloadFile = function(){
		var data={"FILE_NAME":$scope.nameExport};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/users","downloadExportFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				showToast(data.errors[0].message,4000);
				$scope.wait = false;
			}else if(status==200){
				$scope.download.getBlob(data,$scope.nameExport,'application/zip','zip');
				$scope.viewDownload = false;
				$scope.wait = false;
				//$scope.showAction(sbiModule_translate.load("sbi.importusers.downloadOK"));
				sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.importusers.downloadOK"),"");
			}
		}).error(function(data, status, headers, config) {
			showToast("ERRORS "+status,4000);
			$scope.wait = false;
		})
	}
	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			$scope.glossarySelected=[];
			for(var i=0;i<$scope.glossary.length;i++){
				$scope.glossarySelected.push($scope.glossary[i]);
			}
		}else{
			$scope.flagCheck=false;
			$scope.glossarySelected=[];
		} 
	};
	
	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);

		if(index != -1){
			$scope.glossarySelected.splice(index,1);
		}else{
			$scope.glossarySelected.push(item);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item, list)>-1;

	}; 
	
	 $scope.indexInList=function(item, list) {

			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.GLOSSARY_ID==item.GLOSSARY_ID){
					return i;
				}
			}

			return -1;
		};
		
		$scope.indexGlossInList=function(item, list) {

			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.glossaryId==item.glossaryId){
					return i;
				}
			}

			return -1;
		};
		
		
	//import glossary
		$scope.upload = function(ev){
			$scope.importingGlossary = [];
			$scope.glossaryImported = [];
			$scope.glossaryPresentIdDB  =[];
			if($scope.importFile.fileName == "" || $scope.importFile.fileName == undefined){
				//$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"));
				sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"),"");
			}else{
				var fd = new FormData();
			
				fd.append('exportedArchive', $scope.importFile.file);
				$scope.glossaryImported = [];
				$scope.glossaryPresentIdDB  =[];
				sbiModule_restServices.post("1.0/serverManager/importExport/glossary", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("ERROR")){
							$mdToast.show($mdToast.simple().content(data.ERROR).position('top').action(
							'OK').highlightAction(false).hideDelay(5000));
					}
					if(data.STATUS=="NON OK"){
						$mdToast.show($mdToast.simple().content(data.ERROR).position('top').action(
						'OK').highlightAction(false).hideDelay(5000));
					}
					else if(data.STATUS=="OK"){
					
						console.log(data);
						$scope.glossaryImported = data.glossaryListFromFile;
						$scope.glossaryPresentIdDB  =data.glossaryListPresentInDb;
						
					}
				})
				.error(function(data, status, headers, config) {
					$mdToast.show($mdToast.simple().content(data.ERROR).position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
					
				});
			}
		};

		$scope.addGloss = function(){

			for(var i=0;i<$scope.selectGlossaryToImport.length;i++){

				//add inf exportig user
				var index = $scope.indexGlossInList($scope.selectGlossaryToImport[i],$scope.importingGlossary);
				if(index!=-1){
					//if present nothing action
				}else{
					//if not present add
					$scope.importingGlossary.push($scope.selectGlossaryToImport[i]);

				}
				//remove from IEDConf.exportedUser
				var index = $scope.indexGlossInList($scope.selectGlossaryToImport[i],$scope.glossaryImported);
				if(index!=-1){
					//if present
					$scope.glossaryImported.splice(index,1);
				}else{
					//if not present add nothing action

				}
			}


			$scope.selectGlossaryToImport=[];

		}
		$scope.removeGloss = function(){

			for(var i=0;i<$scope.selectGlossaryToImport.length;i++){
				//add inf exportig user
				var index = $scope.indexGlossInList($scope.selectGlossaryToImport[i],$scope.glossaryImported);
				if(index!=-1){
					//if present nothing action
				}else{
					//if not present add
					$scope.glossaryImported.push($scope.selectGlossaryToImport[i]);

				}
				//remove from IEDConf.roles.exportedUser
				var index = $scope.indexGlossInList($scope.selectGlossaryToImport[i],$scope.importingGlossary);
				if(index!=-1){
					//if present
					$scope.importingGlossary.splice(index,1);
				}else{
					//if not present add nothing action

				}
			}
			$scope.selectGlossaryToImport=[];

		}

		$scope.addAllGloss = function(){
			$scope.selectGlossaryToImport=[];
			if($scope.glossaryImported.length!=0){
				for(var i=0;i<$scope.glossaryImported.length;i++){
					$scope.importingGlossary.push($scope.glossaryImported[i]);
				}
			}else{
				$scope.importingGlossary = $scope.glossaryImported;
			}

			$scope.glossaryImported=[];
		}

		$scope.removeAllGloss = function(){
			$scope.selectGlossaryToImport=[];
			if($scope.glossaryImported.length!=0){
				for(var i=0;i<$scope.importingGlossary.length;i++){
					$scope.glossaryImported.push($scope.importingGlossary[i]);
				}
			}else{
				$scope.glossaryImported = $scope.importingGlossary;
			}

			$scope.importingGlossary=[];
		};
		$scope.save = function(ev){
			
			if($scope.importingGlossary.length==0){
				//$scope.showAction(sbiModule_translate.load("sbi.impexpglossary.nothingtoimport"));
				sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.impexpglossary.nothingtoimport"),"");
			}else{ 
				var obj = {
						"type": $scope.typeSaveMenu,
						"glossaryList": $scope.importingGlossary
				};
				sbiModule_restServices.promisePost("1.0/serverManager/importExport/glossary", 'importGlossaryintoDB', obj)
				.then(function(response){ 
					if(response.data.STATUS=="OK"){
						$scope.showConfirm(sbiModule_translate.load("sbi.importusers.importuserok"));
					}else{
						//$scope.showAction(response.data.ERROR);
						sbiModule_restServices.errorHandler(response.data.ERROR,"sbi.generic.toastr.title.error");
					}

				},function(response){
					sbiModule_restServices.errorHandler(response.data,"sbi.generic.toastr.title.error");
				});
				
		};
		}
		
		$scope.showConfirm = function(text) {
		    // Appending dialog to document.body to cover sidenav in docs app
		    var confirm = $mdDialog.alert()
		          .title(text)
		          .ariaLabel('Lucky day')
		          .ok('Ok')
		         
		    $mdDialog.show(confirm).then(function() {

		    }, function() {
		      
		    });
		  };
};



