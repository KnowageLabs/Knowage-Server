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

function datasetsController($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,sbiModule_config,$window,$mdSidenav){
	$scope.selectedDataset = undefined;
	//$scope.lastDocumentSelected = null;
	$scope.showDatasettInfo = false;
	
    //TODO move federations to separate controller
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
	
	/**
	 * load all datasets
	 */
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
	
	$scope.loadMyDatasets= function(){
		sbiModule_restServices.promiseGet("1.0/datasets/mydata", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.myDatasets);
			console.log($scope.myDatasets);
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadMyDatasets();
	
	$scope.loadEnterpriseDatasets= function(){
		sbiModule_restServices.promiseGet("1.0/datasets/enterprise", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.enterpriseDatasets);
			console.log("enterprise");
			console.log($scope.enterpriseDatasets);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadEnterpriseDatasets();
	
	$scope.loadSharedDatasets= function(){
		sbiModule_restServices.promiseGet("1.0/datasets/shared", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.sharedDatasets);
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadSharedDatasets();
	
	$scope.loadNotDerivedDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/listNotDerivedDataset", "")
		.then(function(response) {
			angular.copy(response.data,$scope.notDerivedDatasets);
			console.log("not derivated");
			console.log($scope.notDerivedDatasets);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadNotDerivedDatasets();
	
	
	$scope.showDatasetDetails = function() {
		return $scope.showDatasetInfo && $scope.isSelectedDatasetValid();
	};
	
	
	$scope.isSelectedDatasetValid = function() {
		return $scope.selectedDataset !== undefined;
	};
	
	$scope.setDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('right').isLockedOpen() && !$mdSidenav('right').isOpen()) {
			$scope.toggleDatasetDetail();
		}

		$scope.showDatasetInfo = isOpen;
	};
	
	$scope.toggleDatasetDetail = function() {
		$mdSidenav('right').toggle();
	};
	
	$scope.selectDataset= function ( dataset ) { 
		if (dataset !== undefined) {
			$scope.lastDatasetSelected = dataset;
		}
		var alreadySelected = (dataset !== undefined && $scope.selectedDataset === dataset);
		$scope.selectedDataset = dataset;
		if (alreadySelected) {
			$scope.selectedDataset=undefined;
			$scope.setDetailOpen(!$scope.showDatasetDetail);
		} else {
			$scope.setDetailOpen(dataset !== undefined);
		}
	};
	
	
	

	//TODO move business models to separate controller
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
		var id = federation.federation_id;
		var label = federation.label;
		//$window.location.href=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;

    	 $mdDialog.show({
		      controller: DialogEditFederationController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',  
		      clickOutsideToClose:true,
		      escapeToClose :true,
		      fullscreen: true,
		      locals:{federation:federation }
		    })
		
	}
	
	$scope.deleteFederation=function(federation){
		console.log("in delete");
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.browser.document.delete.ask.title"))
		.content(sbiModule_translate.load("sbi.browser.document.delete.ask"))
		.ariaLabel('delete Document') 
		.ok(sbiModule_translate.load("sbi.general.yes"))
		.cancel(sbiModule_translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
			
			sbiModule_restServices.promiseDelete("2.0/federateddataset",federation.federation_id)
			.then(function(response) {
			
				$scope.loadFederations();
				
			},function(response) {
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.delete.error'));
			});
		});
	}
	
	function DialogEditFederationController($scope,$mdDialog,sbiModule_config,federation){
		$scope.closeDialogFromExt=function(){
			 $mdDialog.cancel();	 
			 //$scope.loadFederations();
		}
		var id = federation.federation_id;
		var label = federation.label;
		$scope.iframeUrl=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;
	}
	
	
}