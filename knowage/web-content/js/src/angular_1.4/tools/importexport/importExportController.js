var app = angular.module('importExportModule', ['ngMaterial','sbiModule','angular_table','document_tree','file_upload']);

app.controller('importExportController', ['$http','sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices",funcController]);

function funcController($http,sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices) {
	
	ctr = this;
	$scope.pathRest = {
			vers: '2.0'
			, folders : 'folders'
			, includeDocs : 'includeDocs=true'
			, urlTest : 'http://localhost:8080/knowage/servlet/AdapterHTTP'
	}
	
	sbiModule_translate.addMessageFile("component_impexp_messages");
	$scope.translate = sbiModule_translate;
	$scope.restServices = sbiModule_restServices;
	$scope.download = sbiModule_download;
	$scope.log = sbiModule_logger;
	$scope.selected =[] ;
	$scope.folders=[];
	$scope.fileImport= {};
	$scope.fileAssociation = {};
	$scope.flags = {
			waitExport : false,
			viewDownload : false
	}
	
	$scope.checkboxs={
		exportSubObj : false,
		exportSnapshots : false
	};
	
	$scope.restServices.get($scope.pathRest.vers, $scope.pathRest.folders,$scope.pathRest.includeDocs)
		.success(function(data){
			//if not errors in response, copy the data
			if (data.errors === undefined){
				$scope.folders=angular.copy(data);
			}else{
				$scope.folders=[];
			}
		})
		.error(function(data, status){
			$scope.folders=angular.copy(foldersJson);
			$scope.log.error('GET RESULT error of ' + data + ' with status :' + status);
		});
	
	$scope.exportFiles = function(){
		var strObjIds = "";
		for (var i =0 ; i < $scope.selected.length;i++){
			if ($scope.selected[i].type == "biObject"){
				strObjIds= strObjIds + $scope.selected[i].id + ";"
			}
		}
		var config = {};
		config.params = {
				PAGE:"ImportExportPage",
				MESSAGEDET:"Export",
				exportFileName: $scope.exportName,
				AF_MODULE_NAME: 'ImportExportModule',
				OBJECT_ID: strObjIds
		};
		if ($scope.checkboxs.exportSubObj){
			config.params.exportSubObj = "on";
		}
		if ($scope.checkboxs.exportSnapshots){
			config.params.exportSnapshots = "on";
		}
		var data= {};
		
		//TODO change link and use restService
		$scope.flags.waitExport=true;
		$http.post($scope.pathRest.urlTest,data, config)
			.success(function(data){
				var d=data;
				$scope.flags.waitExport=false;
				$scope.flags.viewDownload = true;
			})
			.error(function(){
				$scope.flags.waitExport=false;
				$scope.log.error('Export of file ' + $scope.exportName + ' with status :' + status);
			});
	}
	
	$scope.submitDownForm = function(form){
		$scope.flags.submitForm= true;
	}
	
	$scope.importFile = function(item){
		$scope.log.trace("here" + $scope.item);
	}
	
	$scope.toggleViewDownload = function(){
		$scope.flags.viewDownload = !$scope.flags.viewDownload;
	}
	
	$scope.downloadFile = function(){
		var config = {};
		config.params = {
				NEW_SESSION:"TRUE",
				user_id:"biadmin",
				ACTION_NAME:"DOWNLOAD_FILE_ACTION",
				OPERATION:"downloadExportFile",
				FILE_NAME: $scope.exportName
		};
		config.responseType='arraybuffer';
		var data={};
		
		$http.post($scope.pathRest.urlTest,data, config)
			.success(function(data){
				console.log("Hi");
				console.log(data);
				$scope.download.getBlob(data,$scope.exportName,'application/zip','zip');
				
				$scope.flags.viewDownload = false
			})
			.error(function(data, status, headers, config){
				$scope.flags.viewDownload = false;
				$scope.log.error('Export of file ' + $scope.exportName + ' with status :' + status);
			});
	}
	
	$scope.listAssociation = function(){
		$mdDialog.show({
			controller: $scope.dialogController ,
			templateUrl: '/knowage/js/src/angular_1.4/tools/importexport/templates/importExportListAssociation.html',
			parent: angular.element(document.body),
			locals : {
				translate : $scope.translate,
				browser : sbiModule_device.browser
			},
			preserveScope : true,
			clickOutsideToClose:true
		})
		.then(function(associationSelected){
			$scope.fileAssociation = associationSelected;
		}, function(){
			
		});
	}
	
	$scope.dialogController =function ($scope, $mdDialog, translate,browser) {
		$scope.translate = translate;
		$scope.viewInsertForm=false;
		$scope.associationFile = {};
		$scope.associations = [{name:'prova',description : 'desc', creationDate : 'data'}];
		$scope.associationSelected = {};
		$scope.form={};
		$scope.isIE= (browser.name == 'internet explorer');
		
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
	$scope.showAlert = function (title, message){
		$mdDialog.show( 
			$mdDialog.alert()
		        .parent(document.body)
		        .clickOutsideToClose(true)
		        .title(title)
		        .textContent(message) //FROM angular material 1.0 
		        .ok('Ok')
			);
	}
	$scope.debug= function(){
		//$scope.isEnabled = !$scope.isEnabled;
	}
}