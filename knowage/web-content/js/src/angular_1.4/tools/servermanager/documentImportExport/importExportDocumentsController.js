var app = angular.module('importExportDocumentModule', ['ngMaterial','sbiModule','angular_table','document_tree','file_upload','bread_crumb']);

app.factory("importExportDocumentModule_importConf", function() {
	return {
		fileImport : {},
		associations : 'noAssociations',
		fileAssociation : '',
		roles : {
			currentRoles : [],
			exportedRoles : [],
			associatedRoles : {}
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
	};
});


app.controller('importExportController', ["$scope","sbiModule_translate","$mdToast",impExpFuncController]);
app.controller('exportController', ['$http','sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","$mdToast",exportFuncController]);
app.controller('importController', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","$mdToast","importExportDocumentModule_importConf",importFuncController]);

function impExpFuncController($scope,   sbiModule_translate ,$mdToast) {
	sbiModule_translate.addMessageFile("component_impexp_messages");
	$scope.translate = sbiModule_translate;
	
	$scope.showToast=function(text, time) {
		var timer = time == undefined ? 6000 : time;
		console.log(text)
		$mdToast.show($mdToast.simple().content(text).position('top').action(
		'OK').highlightAction(false).hideDelay(timer));
	}

}




function importFuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,$mdToast,importExportDocumentModule_importConf) {
	$scope.stepItem=[{name:"file upload"}];
	$scope.selectedStep=0;
	$scope.stepControl;
	$scope.IEDConf=importExportDocumentModule_importConf;
//	$scope.fileImport= {};
//	$scope.associationsFileImport={};
//	$scope.associations="noAssociations";

}
function exportFuncController($http,sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,$mdToast) {

	$scope.pathRest = {
			vers: '2.0'
				, folders : 'folders'
					, includeDocs : 'includeDocs=true'
						, urlTest : sbiModule_config.contextName+'/servlet/AdapterHTTP'
	}



	$scope.restServices = sbiModule_restServices;
	$scope.download = sbiModule_download;
	$scope.log = sbiModule_logger;
	$scope.selected =[] ;
	$scope.folders=[];

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
				$scope.showToast(data.errors[0].message,4000);
			}else if(data.hasOwnProperty("STATUS") && data.STATUS=="OK"){
				$scope.flags.viewDownload = true;
				$scope.downloadedFileName=$scope.exportName;
			}
			$scope.flags.waitExport=false;
		}).error(function(data, status, headers, config) {
			$scope.flags.waitExport=false;
			$scope.showToast("ERRORS "+status,4000);
		})
	}

	$scope.submitDownForm = function(form){
		$scope.flags.submitForm= true;
	}



	$scope.toggleViewDownload = function(){
		$scope.flags.viewDownload = !$scope.flags.viewDownload;
	}
	$scope.downloadFile= function(){
		var data={"FILE_NAME":$scope.downloadedFileName};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/document","downloadExportFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				$scope.showToast(data.errors[0].message,4000);
			}else if(status==200){
				$scope.download.getBlob(data,$scope.exportName,'application/zip','zip');
				$scope.flags.viewDownload = false
			}
		}).error(function(data, status, headers, config) {
			$scope.showToast("ERRORS "+status,4000);
		})
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