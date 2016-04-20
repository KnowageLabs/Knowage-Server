var app = angular.module('documentExecutionModule').controller('noteController', ['$scope', '$http', '$mdSidenav', '$mdDialog','$mdToast', 'sbiModule_translate', 'sbiModule_restServices', 
                                                                     			 'sbiModule_config', 'sbiModule_messaging', 'execProperties', 'documentExecuteFactories', 'sbiModule_helpOnLine',
                                                                    			 'documentExecuteServices','docExecute_urlViewPointService','docExecute_paramRolePanelService','infoMetadataService','sbiModule_download',
                                                                    			 noteControllerFunction ]);

function noteControllerFunction($scope, $http, $mdSidenav,$mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices, sbiModule_config,
		sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine,documentExecuteServices
		,docExecute_urlViewPointService,docExecute_paramRolePanelService,infoMetadataService,sbiModule_download) {

	
	$scope.saveNote = function(){
		var obj = {
				'nota' : $scope.contentNotes,
				'idObj': $scope.executionInstance.OBJECT_ID,
				'type' : $scope.typeNote
		}
		sbiModule_restServices.promisePost("documentnotes", 'saveNote',obj).then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						$scope.showAction("Saved");
						$scope.getList();
					}

				},function(response) {
					$scope.errorHandler(response.data,"");
				})

	}
	$scope.getNotesList = function(){
		
		if($scope.notesList.length==0){
			$scope.getList();
		}
	}
	$scope.getList = function(){
		var obj = {'id' : $scope.executionInstance.OBJECT_ID};
		sbiModule_restServices.promisePost("documentnotes", 'getListNotes',obj).then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						angular.copy(response.data,$scope.notesList)
						
					}

				},function(response) {
					$scope.errorHandler(response.data,"");
				})
	}
	$scope.deleteNote = function(nota){
		var obj ={
				"id":nota.biobjId,
				"execReq":nota.execReq,
				"owner":nota.owner
		}
		
		var confirm = $mdDialog.confirm()
				.title("Are you sure?")
				.ariaLabel('cancel metadata') 
				.ok($scope.translate.load("sbi.general.ok"))
				.cancel($scope.translate.load("sbi.general.cancel"));
				$mdDialog.show(confirm).then(function() {
					sbiModule_restServices.promisePost("documentnotes", 'deleteNote',obj).then(
							function(response) {
								if (response.data.hasOwnProperty("errors")) {
									$scope.showAction(response.data);
								} else {
									$scope.showAction("Nota deleted");
									$scope.getList();
								}

							},function(response) {
								$scope.errorHandler(response.data,"");
							})
				}, function() {
					return;
				});
	}
	
	$scope.editNote = function(nota){
		$scope.contentNotes=nota.content;
		$scope.noteLoaded.content = nota.content;
		$scope.noteLoaded.id = nota.id;
		$scope.noteLoaded.lastChangeDate = nota.lastChangeDate;
		$scope.noteLoaded.creationDate = nota.creationDate;
		$scope.noteLoaded.exeqReq = nota.exeqReq;
		$scope.noteLoaded.owner = nota.owner;
		$scope.selectedTab.tab=0;
	
	}
	
	$scope.exportNote = function(typeExport){
		var obj = {
				'idObj': $scope.executionInstance.OBJECT_ID,
				'type' : typeExport	
		}
		sbiModule_restServices.promisePost("documentnotes", 'getDownalNote',obj).then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						console.log(response);
						
						var arr = response.data.file;
						var byteArray = new Uint8Array(arr);
						sbiModule_download.getBlob(byteArray,$scope.executionInstance.OBJECT_LABEL,"application/"+typeExport,typeExport);

						
					}

				},function(response) {
					$scope.errorHandler(response.data,"");
				})
		
	}

	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}
	
	
	$scope.close = function(){
		$mdDialog.cancel();

	}
	$scope.apply = function(){
		$mdDialog.cancel();
	}
	$scope.showAction = function(text) {
		$scope.isClick=false;
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top');

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {

			}
		});
	};
	
}

app.filter("asDate", function () {
    return function (input) {
        return new Date(input);
    }
});