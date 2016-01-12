var app = angular.module('importExportModule', ['ngMaterial','sbiModule','angular_table','document_tree','file_upload']);

app.controller('importExportController', ['$http','sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","$mdToast",funcController]);

function funcController($http,sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,$mdToast) {

	ctr = this;
	$scope.pathRest = {
			vers: '2.0'
				, folders : 'folders'
					, includeDocs : 'includeDocs=true'
						, urlTest : sbiModule_config.contextName+'/servlet/AdapterHTTP'
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

	$scope.exportFiles= function(){
		var config={"DOCUMENT_ID_LIST":[],
				"EXPORT_FILE_NAME":$scope.exportName,
				"EXPORT_SUB_OBJ":$scope.checkboxs.exportSubObj,
				"EXPORT_SNAPSHOT":$scope.checkboxs.exportSnapshots};
		for (var i =0 ; i < $scope.selected.length;i++){
			if ($scope.selected[i].type == "biObject"){
				config.DOCUMENT_ID_LIST.push(""+$scope.selected[i].id);
			}
		}
		
		$scope.flags.waitExport=true;
		sbiModule_restServices.post("1.0/serverManager/importExport/document","export",config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				showToast(data.errors[0].message,4000);
			}else if(data.hasOwnProperty("STATUS") && data.STATUS=="OK"){
				$scope.flags.viewDownload = true;
			}
			$scope.flags.waitExport=false;
		}).error(function(data, status, headers, config) {
			$scope.flags.waitExport=false;
			showToast("ERRORS "+status,4000);
		})
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
	$scope.downloadFile= function(){
		var data={"FILE_NAME":$scope.exportName};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/document","downloadExportFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				showToast(data.errors[0].message,4000);
			}else if(status==200){
				$scope.download.getBlob(data,$scope.exportName,'application/zip','zip');
				$scope.flags.viewDownload = false
			}
		}).error(function(data, status, headers, config) {
		showToast("ERRORS "+status,4000);
		})
	}

	$scope.downloadFileOLD = function(){
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
	
	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time;

		console.log(text)
		$mdToast.show($mdToast.simple().content(text).position('top').action(
		'OK').highlightAction(false).hideDelay(timer));
	}
	
}