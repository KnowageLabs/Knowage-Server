angular
	.module('models_view_workspace', [])


	/**
	 * The HTML content of the Recent view (recent documents).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('modelsViewWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/modelsViewWorkspace.html',
		      controller: modelsController
		  };	  
	});

function modelsController($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,sbiModule_config,$window,$mdSidenav){
	//console.log("aaaa");
	//console.log(datasetParameters);
	
	$scope.selectedModel = undefined;

	$scope.showModelInfo = false;
	
	
	$scope.loadFederations=function(){
		sbiModule_restServices.promiseGet("2.0/federateddataset", "")
		.then(function(response) {
			angular.copy(response.data,$scope.federationDefinitions);
//			console.log($scope.federationDefinitions);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadFederations();
	
	
	//TODO move business models to separate controller
    $scope.loadBusinessModels= function(){
    	sbiModule_restServices.promiseGet("2.0/businessmodels", "")
		.then(function(response) {
			angular.copy(response.data,$scope.businessModels);
//			console.log($scope.businessModels);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadBusinessModels();
	
	$scope.showModelDetails = function() {
		return $scope.showModelInfo && $scope.isSelectedModelValid();
	};
	
	
	$scope.isSelectedModelValid = function() {
		return $scope.selectedModel !== undefined;
	};
	
	$scope.setDetailOpenModel = function(isOpen) {
		if (isOpen && !$mdSidenav('rightModel').isLockedOpen() && !$mdSidenav('rightModel').isOpen()) {
			$scope.toggleModelDetail();
		}

		$scope.showModelInfo = isOpen;
	};
	
	$scope.toggleModelDetail = function() {
		$mdSidenav('rightModel').toggle();
	};
	
	$scope.selectModel= function ( model ) { 
		if (model !== undefined) {
			//$scope.lastDatasetSelected = dataset;
		}
		var alreadySelected = (model !== undefined && $scope.selectedModel === model);
		$scope.selectedModel = model;
		if (alreadySelected) {
			$scope.selectedModel=undefined;
			$scope.setDetailOpenModel(!$scope.showModelDetail);
		} else {
			$scope.setDetailOpenModel(model !== undefined);
		}
	};
	
	$scope.showQbeModel=function(model){
		
		if($scope.currentTab=='federations'){
			$scope.showQbeFederation(model);
		}else if($scope.currentTab=='businessModels'){
			$scope.showQbeFromBM(model);
		}
	}
	
	$scope.showQbeFederation= function(federation){
//		console.log(federation);
//		console.log(sbiModule_config.contextName);
//		console.log(sbiModule_config.adapterPath);
		
	//	var actionName= 'QBE_ENGINE_FROM_FEDERATION_START_ACTION';
		var federationId= federation.federation_id;
		
		var url=datasetParameters.qbeEditFederationServiceUrl
		       +'&FEDERATION_ID='+federationId;
		
//		var url= sbiModule_config.engineUrls.worksheetServiceUrl
//		         +'&ACTION_NAME='+actionName
//		         +'&FEDERATION_ID='+federationId
//		         + '&label='+federation.label;
//		        // +'&DATASOURCE_FOR_CACHE=knowage';
		 $window.location.href=url;
		

		
	}
	
	$scope.editFederation=function(federation){
//		console.log(federation);
		var id = federation.federation_id;
		var label = federation.label;
		//$window.location.href=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;

    	 $mdDialog.show({
    		  scope:$scope,
			  preserveScope: true,
		      controller: DialogEditFederationController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',  
		      clickOutsideToClose:true,
		      escapeToClose :true,
		      fullscreen: true,
		      locals:{federation:federation }
		    })
		
	}
	
	$scope.deleteFederation=function(federation){
//		console.log("in delete");
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
				$scope.selectModel(undefined);
			},function(response) {
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.delete.error'));
			});
		});
	}
	
	$scope.createFederation=function(){
		
		$mdDialog.show({
			  scope:$scope,
			  preserveScope: true,
		      controller: DialogEditFederationController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',  
		      clickOutsideToClose:true,
		      escapeToClose :true,
		      fullscreen: true,
		      locals:{federation:undefined}
		    })
	}
	
	$scope.showQbeFromBM=function(businessModel){
		console.log(businessModel);
		//var actionName= 'QBE_ENGINE_START_ACTION_FROM_BM';
		var modelName= businessModel.name;
		var dataSource=businessModel.dataSourceLabel;
		var url= datasetParameters.qbeFromBMServiceUrl
		        +'&isWorksheetEnabled='+datasetParameters.IS_WORKSHEET_ENABLED
		        +'&MODEL_NAME='+modelName
		        +'&DATA_SOURCE_LABEL='+ dataSource;
		
//		var url= sbiModule_config.engineUrls.worksheetServiceUrl
//		         +'&ACTION_NAME='+actionName
//		         +'&MODEL_NAME='+modelName
//		         +'&isWorksheetEnabled=true'
//		         +'&DATA_SOURCE_LABEL='+ dataSource;
		       
		 $window.location.href=url;
	}
	
	function DialogEditFederationController($scope,$mdDialog,sbiModule_config,federation){
	
		$scope.closeFederationDialog=function(){
			 $mdDialog.cancel();	 
			 $scope.loadFederations();
		}
		
		if(federation!==undefined){
		var id =federation.federation_id;
		var label = federation.label;
		$scope.iframeUrl=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;
		}else{
			
		$scope.iframeUrl=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp";	
		}
		
		}
}