function generateDatamartOptionsController(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog,
		$mdToast,multipartForm,sbiModule_download,sbiModule_messaging,sbiModule_config,sbiModule_user,sbiModule_messaging,selectedBusinessModel,userId,catalogName,schemaName,parentController){
	
	//controller variables
	$scope.translate=sbiModule_translate;
	$scope.selectedBusinessModel=selectedBusinessModel;
	$scope.modelName=selectedBusinessModel.name;
	$scope.schemaName = schemaName;
	$scope.catalogName = catalogName;
	$scope.isGeneratedForRegistry = false;
	$scope.showAdvancedOptions = false;
	$scope.includeSources = false;

	
	//build the datamart
	$scope.buildDatamart = function(){

		sbiModule_restServices.alterContextPath(sbiModule_config.contextMetaName);
		var endOfTheURL = "buildModel/"+$scope.selectedBusinessModel.id
			+"?user_id=" + sbiModule_user.userId
			+"&model=" + encodeURIComponent($scope.modelName)
			+"&schema=" + $scope.schemaName
			+"&catalog=" + $scope.catalogName
			+"&registry=" + $scope.isGeneratedForRegistry
			+"&includeSources=" + $scope.includeSources;

		sbiModule_restServices.promiseGet("1.0/metaWeb", endOfTheURL)
		.then(
				function(response) {
					//ok case
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'check');
					parentController.getVersions($scope.selectedBusinessModel.id);
					$mdDialog.hide();
				},
				function(response) {
					//errors case
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.catalogues.generation.error"));
					parentController.getVersions($scope.selectedBusinessModel.id);
				}
		);

	}
	
	//cancel action
	$scope.cancel = function(){
		$mdDialog.cancel();
	};

}
