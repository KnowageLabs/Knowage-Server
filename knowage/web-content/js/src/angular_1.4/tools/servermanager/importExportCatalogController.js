var app = angular.module('impExpDataset', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'angular_list','sbiModule','file_upload', 'angular_2_col']);
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

app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	$scope.translate = sbiModule_translate;
	$scope.dataset = [];
	$scope.datasetSelected = [];
	$scope.flagCheck=false;
	$scope.nameExport="";
	$scope.typeCatalog="Dataset";
	$scope.showDataset=false;
	$scope.listType=[];
	$scope.download = sbiModule_download;
	//export utilities 
	$scope.loadAllDataset = function(){
		sbiModule_restServices.get("1.0/serverManager/importExport/catalog", 'getdataset').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.dataset = data;
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
	}
	
	$scope.prepare = function(ev){
		$scope.wait = true;
		if($scope.datasetSelected.length == 0){
			$scope.showAction(sbiModule_translate.load("sbi.importexportcatalog.missingcheck"));
			$scope.wait = false;
		} else if($scope.nameExport==""){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingnamefile"));
			$scope.wait = false;
		}else{
			//modulo download zip

			var config={"DATASET_LIST":$scope.datasetSelected ,
					"EXPORT_FILE_NAME":$scope.nameExport,
					"EXPORT_SUB_OBJ":false,
					"EXPORT_SNAPSHOT":false};

			sbiModule_restServices.post("1.0/serverManager/importExport/catalog", 'export',config).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
						} else {
							if(data.hasOwnProperty("STATUS") && data.STATUS=="OK"){
								$scope.downloadFile();
								//	$scope.viewDownload = true;
								//	$scope.downloadFile();
							}

						}

					}).error(function(data, status, headers, config) {
						console.log("layer non Ottenuti " + status);

					})
		}
	}



	$scope.downloadFile= function(){
		var data={"FILE_NAME":$scope.nameExport};
		var config={"responseType": "arraybuffer"};
		sbiModule_restServices.post("1.0/serverManager/importExport/catalog","downloadExportFile",data,config)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				showToast(data.errors[0].message,4000);
				$scope.wait = false;
			}else if(status==200){
				$scope.download.getBlob(data,$scope.nameExport,'application/zip','zip');
				$scope.showAction(sbiModule_translate.load("sbi.importusers.downloadOK"));
				$scope.wait=false;
			}
		}).error(function(data, status, headers, config) {
			showToast("ERRORS "+status,4000);
			$scope.wait = false;
		})
	}


	$scope.toggle = function (item, list) {
		$scope.checkCategory(item);
		var index = $scope.indexInList(item, list);

		if(index != -1){
			list.splice(index,1);
		}else{
			list.push(item);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item, list)>-1;

	};
	
	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object==item){
				return i;
			}
		}

		return -1;
	};
	
	$scope.checkCategory= function(item){
		switch(item){
		case 'Dataset':
			if(!$scope.showDataset){
				$scope.showDataset=true;
				$scope.loadAllDataset();
			}else{
				$scope.showDataset=false;
			}
		
		}
		
	}
	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			$scope.datasetSelected=[];
			for(var i=0;i<$scope.dataset.length;i++){
				$scope.datasetSelected.push($scope.dataset[i]);
			}
		}else{
			$scope.flagCheck=false;
			$scope.datasetSelected=[];
		}
		
		
	}

	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}
	
	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}
	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};
}