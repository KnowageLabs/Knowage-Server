angular
	.module('datasets_view_workspace', [])

	/**
	 * The HTML content of the Recent view (recent documents).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('datasetsViewWorkspace', function () {
	 	return {
	      	restrict: 'E',
	      	replace: 'true',
	      	templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/datasetsViewWorkspace.html',
	      	controller: datasetsController
	  	};
	})

function datasetsController($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,sbiModule_config){
	
	$scope.loadFederations=function(){
		sbiModule_restServices.promiseGet("2.0/federateddataset", "")
		.then(function(response) {
			angular.copy(response.data,$scope.federationDefinitions);
			console.log($scope.federationDefinitions);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadFederations();
	
	$scope.loadDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets", "")
		.then(function(response) {
			angular.copy(response.data,$scope.datasets);
			console.log($scope.datasets);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadDatasets();
	
    $scope.loadBusinessModels= function(){
    	sbiModule_restServices.promiseGet("2.0/businessmodels", "")
		.then(function(response) {
			angular.copy(response.data,$scope.businessModels);
			console.log($scope.businessModels);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadBusinessModels();
	
	$scope.showQbeFederation= function(){
		
	}
	
	$scope.editFederation=function(federation){
		console.log(federation);
    	 $mdDialog.show({
		      controller: DialogEditFederationController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',  
		      clickOutsideToClose:true,
		      escapeToClose :true,
		      fullscreen: true,
		      locals:{federation:federation }
		    })
		
	}
	
	function DialogEditFederationController($scope,$mdDialog,sbiModule_config,federation){
//		$scope.closeDialogFromExt=function(){
//			 $mdDialog.cancel();
//		}
		var id = federation.federation_id;
		var label = federation.label;
		$scope.iframeUrl=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;
	}
	
	
}