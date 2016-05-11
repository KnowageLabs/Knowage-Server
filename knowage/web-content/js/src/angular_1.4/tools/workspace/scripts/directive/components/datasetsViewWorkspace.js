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

function datasetsController($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,sbiModule_config,$window,$mdSidenav,sbiModule_user,sbiModule_helpOnLine,sbiModule_messaging){
	$scope.selectedDataset = undefined;
	//$scope.lastDocumentSelected = null;
	$scope.showDatasettInfo = false;
	$scope.currentTab = "myDataSet";
    $scope.previewDatasetModel=[];
    $scope.previewDatasetColumns=[];
	
	/**
	 * load all datasets
	 */
	$scope.loadDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/mydata", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.datasets);
//			console.log($scope.datasets);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}

	$scope.loadDatasets();
	
	$scope.loadMyDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/owned", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.myDatasets);
//			console.log($scope.myDatasets);
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadMyDatasets();
	
	$scope.loadEnterpriseDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/enterprise", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.enterpriseDatasets);
//			console.log("enterprise");
//			console.log($scope.enterpriseDatasets);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadEnterpriseDatasets();
	
	$scope.loadSharedDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/shared", "")
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
//			console.log("not derivated");
//			console.log($scope.notDerivedDatasets);
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
		if (isOpen && !$mdSidenav('rightDs').isLockedOpen() && !$mdSidenav('rightDs').isOpen()) {
			$scope.toggleDatasetDetail();
		}

		$scope.showDatasetInfo = isOpen;
	};
	
	$scope.toggleDatasetDetail = function() {
		$mdSidenav('rightDs').toggle();
	};
	
	$scope.selectDataset= function ( dataset ) { 
		if (dataset !== undefined) {
			//$scope.lastDatasetSelected = dataset;
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
	
    $scope.shareDataset=function(dataset){
    	//console.log("in share");
//    	console.log(dataset);
    	var id=dataset.id;
//    	console.log(id);
        params={};
    	params.id=id;
    	config={};
    	config.params=params;
    	
    	sbiModule_restServices.promisePost("selfservicedataset/share","","",config)
		.then(function(response) {
//			          console.log(response);
			          // binds changed value to object
			          dataset.isPublic=response.data.isPublic;
			          sbiModule_messaging.showSuccessMessage("dataset is shared successfuly","shared success");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
    	
    }
    
    $scope.showQbeDataset= function(dataset){
		var actionName= 'QBE_ENGINE_FROM_FEDERATION_START_ACTION';
		var label= dataset.label;
		
		
		var url= sbiModule_config.engineUrls.worksheetServiceUrl
		         +'&ACTION_NAME='+actionName
		         +'&dataset_label='+label;
		        // +'&DATASOURCE_FOR_CACHE=knowage';
		 $window.location.href=url;
    }
    
    $scope.showHelpOnline= function(dataset){
    	
    	sbiModule_helpOnLine.show(dataset.id);
    }    
    
    $scope.isSharingEnabled=function(){
        return $scope.currentTab==="myDataSet"; 
    }
    
    $scope.exportDataset= function(dataset){
       var actionName='EXPORT_EXCEL_DATASET_ACTION';
       
       var id=dataset.id;
       if(isNaN(id)){
    	   id=id.dsId;
       }
       
       var url= sbiModule_config.adapterPath
               +'?ACTION_NAME='+actionName
               +'&SBI_EXECUTION_ID=-1'
               +'&LIGHT_NAVIGATOR_DISABLED=TRUE'
               +'&id='+id;
       
       $window.location.href=url;
    }
    
    $scope.previewDataset= function(dataset){
    	var loaded=false;
    	sbiModule_restServices.promiseGet("selfservicedataset/export", dataset.label)
		.then(function(response) {
			
		    angular.copy(response.data.rows,$scope.previewDatasetModel);
			$scope.createColumnsForPreview(response.data.metaData.fields);
//			console.log($scope.previewDatasetModel);
//			console.log($scope.previewDatasetColumns);
			$mdDialog.show({
				  scope:$scope,
				  preserveScope: true,
			      controller: DatasetPreviewController,
			      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetPreviewDialogTemplate.html',  
			      clickOutsideToClose:false,
			      escapeToClose :false,
			      fullscreen: true,
			      locals:{
			    	 // previewDatasetModel:$scope.previewDatasetModel,
			         // previewDatasetColumns:$scope.previewDatasetColumns 
			      }
			    });
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
        
    	
    
    	
    	
    }
    
    $scope.createColumnsForPreview=function(fields){
    
    	for(i=1;i<fields.length;i++){
    	 var column={};
    	 column.label=fields[i].header;
    	 column.name=fields[i].name;
    	 
    	 $scope.previewDatasetColumns.push(column);
    	}
    	
    }
    
    $scope.switchTab=function(currentTab){
    
    	$scope.currentTab=currentTab;
    	if($scope.selectedDataset !== undefined){
    		$scope.selectDataset(undefined);
         }
    	
    }	
    
	function DatasetPreviewController($scope,$mdDialog){
		
		$scope.closeDatasetPreviewDialog=function(){
			 $scope.previewDatasetModel=[];
			 $scope.previewDatasetColumns=[];
			 $mdDialog.cancel();	 
	    }
		
	}
    
}