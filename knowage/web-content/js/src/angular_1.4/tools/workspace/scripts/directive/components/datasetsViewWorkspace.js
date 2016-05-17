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

function datasetsController($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,sbiModule_config,$window,$mdSidenav,sbiModule_user,sbiModule_helpOnLine,sbiModule_messaging,multipartForm){
	$scope.selectedDataset = undefined;
	//$scope.lastDocumentSelected = null;
	$scope.showDatasettInfo = false;
	$scope.currentTab = "myDataSet";
    $scope.previewDatasetModel=[];
    $scope.previewDatasetColumns=[];
    $scope.startPreviewIndex=0;
    $scope.endPreviewIndex=0;
    $scope.totalItemsInPreview=0;
    $scope.previewPaginationEnabled=true; 
    console.log(datasetParameters);
    $scope.itemsPerPage=15;
    $scope.datasetInPreview=undefined;
    
    
    $scope.datasetsInitial=[];  //all
	$scope.myDatasetsInitial= [];
	$scope.enterpriseDatasetsInitial=[];
	$scope.sharedDatasetsInitial=[];
    
    $scope.markNotDerived=function(datasets){
    	
    	for(i=0;i<datasets.length;i++){
    		
    		if($scope.notDerivedDatasets.indexOf(datasets[i].label)>-1){
    			datasets[i].derivated=false;
    			
    		}else{
    			datasets[i].derivated=true;		
    		}
    	}
    }
    
	/**
	 * load all datasets
	 */
	$scope.loadDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/mydata", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.datasets);
			$scope.markNotDerived($scope.datasets);
			angular.copy($scope.datasets,$scope.datasetsInitial);
			console.info("[LOAD END]: Loading of All datasets is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}

	$scope.loadMyDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/owned", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.myDatasets);
			$scope.markNotDerived($scope.myDatasets);
			angular.copy($scope.myDatasets,$scope.myDatasetsInitial);
			console.info("[LOAD END]: Loading of My datasets is finished.");
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadEnterpriseDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/enterprise", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.enterpriseDatasets);
			$scope.markNotDerived($scope.enterpriseDatasets);
			angular.copy($scope.enterpriseDatasets,$scope.enterpriseDatasetsInitial);
			console.info("[LOAD END]: Loading of Enterprised datasets is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadSharedDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/shared", "")
		.then(function(response) {
			angular.copy(response.data.root,$scope.sharedDatasets);
			$scope.markNotDerived($scope.sharedDatasets);
		    angular.copy($scope.sharedDatasets,$scope.sharedDatasetsInitial);
			console.info("[LOAD END]: Loading of Shared datasets is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadNotDerivedDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/listNotDerivedDataset", "")
		.then(function(response) {
			//angular.copy(response.data,$scope.notDerivedDatasets);
			$scope.extractNotDerivedLabels(response.data);
			console.info("[LOAD END]: Loading of Not derived datasets is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadNotDerivedDatasets();
	
	$scope.loadInitialForDatasets=function(){
		angular.copy($scope.datasets,$scope.datasetsInitial); 
		angular.copy($scope.myDatasets,$scope.myDatasetsInitial);
		angular.copy($scope.enterpriseDatasets,$scope.enterpriseDatasetsInitial);
		angular.copy($scope.sharedDatasets,$scope.sharedDatasetsInitial);
	};
	
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
    	console.log(dataset);
	//	var actionName= 'QBE_ENGINE_FROM_FEDERATION_START_ACTION';
		var label= dataset.label;
		
		var url= datasetParameters.qbeFromDataSetServiceUrl
		       +'&dataset_label='+label;
//		var url= sbiModule_config.engineUrls.worksheetServiceUrl
//		         +'&ACTION_NAME='+actionName
//		         +'&dataset_label='+label;
		 $window.location.href=url;
    }
    
    $scope.extractNotDerivedLabels= function(datasets){
    	for(i=0;i<datasets.length;i++){
    		$scope.notDerivedDatasets.push(datasets[i].label);
    	}
    		
    }
    
    $scope.creationDatasetEnabled= function(){
    	
    return datasetParameters.CAN_CREATE_DATASET_AS_FINAL_USER==="true";
    	
    }

    $scope.showHelpOnline= function(dataset){
    	
    	sbiModule_helpOnLine.show(dataset.label);
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
    	console.log(dataset);
    	$scope.datasetInPreview=dataset;
    	if(dataset.meta.dataset.length>0){
    	$scope.totalItemsInPreview=dataset.meta.dataset[0].pvalue;
    	$scope.previewPaginationEnabled=true;
    	}else{
    		$scope.previewPaginationEnabled=false;
    	}
    	$scope.getPreviewSet($scope.datasetInPreview);
        
    	if($scope.totalItemsInPreview < $scope.itemsPerPage){
    		 $scope.endPreviewIndex= $scope.totalItemsInPreview	
    	}else{
    		 $scope.endPreviewIndex = $scope.itemsPerPage;
    	}
    	
    	
     	$mdDialog.show({
			  scope:$scope,
			  preserveScope: true,
		      controller: DatasetPreviewController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetPreviewDialogTemplate.html',  
		      clickOutsideToClose:false,
		      escapeToClose :false,
		      //fullscreen: true,
		      locals:{
		    	 // previewDatasetModel:$scope.previewDatasetModel,
		         // previewDatasetColumns:$scope.previewDatasetColumns 
		      }
		    });
   
    	
    	
    }
    
    $scope.getPreviewSet= function(dataset){
    	

    	params={};
    	params.start=$scope.startPreviewIndex;
    	params.limit=$scope.itemsPerPage;
    	params.page=0;
    	params.dataSetParameters=null;
    	params.sort=null;
    	params.valueFilter=null;
    	params.columnsFilter=null;
    	params.columnsFilterDescription=null;
    	params.typeValueFilter=null;
    	params.typeFilter=null;
    	
    	config={};
    	config.params=params;
    	sbiModule_restServices.promiseGet("selfservicedataset/values", dataset.label,"",config)
		.then(function(response) {
			console.log(response.data);
		    angular.copy(response.data.rows,$scope.previewDatasetModel);
		    if( $scope.previewDatasetColumns.length==0){
			$scope.createColumnsForPreview(response.data.metaData.fields);
		    }
		
			
			//$scope.startPreviewIndex=$scope.startPreviewIndex=0+20;
			
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
   
    $scope.addNewFileDataset=function(){
    	
      console.log("new dataset");	
      
      
      $mdDialog.show({
		  scope:$scope,
		  preserveScope: true,
	      controller: DatasetCreateController,
	      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetCreateDialogTemplate.html',  
	      clickOutsideToClose:true,
	      escapeToClose :true,
	      //fullscreen: true,
	      locals:{
	    	 // previewDatasetModel:$scope.previewDatasetModel,
	         // previewDatasetColumns:$scope.previewDatasetColumns 
	      }
	    });
    }
    
    $scope.switchTab=function(currentTab){
    
    	$scope.currentTab=currentTab;
    	if($scope.selectedDataset !== undefined){
    		$scope.selectDataset(undefined);
         }
    	
    	if($scope.selectedModel !== undefined){
    		$scope.selectModel(undefined);
         }
    	
    }	
    
    $scope.getBackPreviewSet=function(){
    	 if($scope.startPreviewIndex-$scope.itemsPerPage < 0){
    		 $scope.startPreviewIndex=0; 
    		 $scope.endPreviewIndex=$scope.itemsPerPage;
    	 }else{
    		 $scope.endPreviewIndex=$scope.startPreviewIndex;
             $scope.startPreviewIndex= $scope.startPreviewIndex-$scope.itemsPerPage;
         
    	 }
    
    	 $scope.getPreviewSet($scope.datasetInPreview);
    	 
    	
    }
    
    $scope.getNextPreviewSet= function(){
    	 if($scope.startPreviewIndex+$scope.itemsPerPage > $scope.totalItemsInPreview){
    		 $scope.startPreviewIndex=$scope.totalItemsInPreview-($scope.totalItemsInPreview%$scope.itemsPerPage);
    		 $scope.endPreviewIndex=$scope.totalItemsInPreview;
    	 }else if($scope.startPreviewIndex+$scope.itemsPerPage == $scope.totalItemsInPreview){
    		 $scope.startPreviewIndex=$scope.totalItemsInPreview-$scope.itemsPerPage;
    		 $scope.endPreviewIndex=$scope.totalItemsInPreview;
    	 } else{
              $scope.startPreviewIndex= $scope.startPreviewIndex+$scope.itemsPerPage;
              $scope.endPreviewIndex=$scope.endPreviewIndex+$scope.itemsPerPage;
    	 }   
    	 
    	 
        	 $scope.getPreviewSet($scope.datasetInPreview);
        	 
    }
    
	function DatasetPreviewController($scope,$mdDialog){
		
		$scope.closeDatasetPreviewDialog=function(){
			 $scope.previewDatasetModel=[];
			 $scope.previewDatasetColumns=[];
			 $scope.startPreviewIndex=0;
			 $scope.endPreviewIndex=0;
			 $scope.totalItemsInPreview=0;
			 $scope.datasetInPreview=undefined;
			 
			 $mdDialog.cancel();	 
	    }
		
		
		
	}
	

	
	function DatasetCreateController($scope,$mdDialog,sbiModule_restServices,sbiModule_config,multipartForm){
		$scope.closeDatasetCreateDialog=function(){
			$mdDialog.cancel();
		}
		
		loadDatasetCategories= function(){
			sbiModule_restServices.promiseGet("domainsforfinaluser/listValueDescriptionByType", "")
			.then(function(response) {
				console.log(response.data);
				angular.copy(response.data,$scope.datasetCategories)
			},function(response){
				sbiModule_restServices.errorHandler(response.data,"faild to load categories");
			});
			
			
		}
		
	    loadDatasetCategories();
		$scope.fileObj={};
		
        $scope.uploadFileDataset=function(){
        	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedataset/fileupload",$scope.fileObj).success(

					function(data,status,headers,config){
						if(data.hasOwnProperty("errors")){						
							console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");		
							sbiModule_messaging.showErrorMessage("file upload failed"+":"+data.errors[0].message, 'Error');  

						}else{
							sbiModule_messaging.showSuccessMessage("success", 'Success!'); 
							console.log("[UPLOAD]: SUCCESS!");
							$scope.fileObj.fileName = "";
							$scope.fileObj = {};
						}
						//$scope.bmCWMImportingShow = false;

					}).error(function(data, status, headers, config) {
						console.log("[UPLOAD]: FAIL!"+status);
						sbiModule_messaging.showErrorMessage("errr", 'Error');
						//$scope.bmCWMImportingShow = false;
					});
        	
        }
		
		
	}
    
}