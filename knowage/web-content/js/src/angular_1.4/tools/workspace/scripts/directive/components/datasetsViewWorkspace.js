/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

function datasetsController($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,sbiModule_config,$window,$mdSidenav,sbiModule_user,sbiModule_helpOnLine,sbiModule_messaging,$qbeViewer){
	
	$scope.translate = sbiModule_translate;
	
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
    
    $scope.itemsPerPage=15;
    $scope.datasetInPreview=undefined;
    
    /**
     * Flag that will tell us if we are entering the Dataset wizard from the Editing or from Creating phase (changing or adding a new dataset, respectively).
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    $scope.editingDatasetFile = false;
    
    $scope.datasetsInitial=[];  //all
	$scope.myDatasetsInitial= [];
	$scope.enterpriseDatasetsInitial=[];
	$scope.sharedDatasetsInitial=[];
    
	/**
	 * STEP 3
	 */
	$scope.allHeadersForStep3Preview = [];
	$scope.allRowsForStep3Preview = [];
	
	/**
	 * The validation status after submitting the Step 2.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.validationStatus = false;
    
	/**
	 * An indicator if the user previously (already) uploaded an XLS/CSV file, in the case of re-browsing for a new one (this one needs to be uploaded, as well).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.prevUploadedFile = null;
	
	$scope.datasetSavedFromQbe = false;
	
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
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.load.error'));
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
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.load.error'));
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
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.load.error'));
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
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.load.error'));
		});
	}
	
	$scope.loadNotDerivedDatasets= function(){
		sbiModule_restServices.promiseGet("2.0/datasets/listNotDerivedDataset", "")
		.then(function(response) {
			//angular.copy(response.data,$scope.notDerivedDatasets);
			$scope.extractNotDerivedLabels(response.data);
			console.info("[LOAD END]: Loading of Not derived datasets is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.load.error'));
		});
	}
	
	$scope.loadNotDerivedDatasets();
	
	$scope.loadInitialForDatasets=function(){
		angular.copy($scope.datasets,$scope.datasetsInitial); 
		angular.copy($scope.myDatasets,$scope.myDatasetsInitial);
		angular.copy($scope.enterpriseDatasets,$scope.enterpriseDatasetsInitial);
		angular.copy($scope.sharedDatasets,$scope.sharedDatasetsInitial);
	};
	
	
	$scope.deleteDataset=function(dataset){
		var label= dataset.label;
		
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.federationdefinition.confirm.dialog"))
		.content(sbiModule_translate.load("sbi.workspace.dataset.delete.confirm"))
		.ariaLabel('delete Document') 
		.ok(sbiModule_translate.load("sbi.general.yes"))
		.cancel(sbiModule_translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
			
			sbiModule_restServices.promiseDelete("1.0/datasets",label)
			.then(function(response) {
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.workspace.dataset.delete.success'),sbiModule_translate.load('sbi.workspace.dataset.success'));
				$scope.reloadMyData();
				$scope.selectDataset(undefined);
			},function(response) {
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.delete.error'));
			});
		});
		
}
	
	
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
			          if(response.data.isPublic){
			          sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.workspace.dataset.share.success'),sbiModule_translate.load('sbi.workspace.dataset.success'));
			          }else{
			        	  
			            sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.workspace.dataset.unshare.success'),sbiModule_translate.load('sbi.workspace.dataset.success'));	  
			          }
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.fail'));
		});
    	
    }
    
    $scope.showQbeDataset= function(dataset){
    	console.log(dataset);

		var label= dataset.label;
		
		var url= datasetParameters.qbeFromDataSetServiceUrl
		       +'&dataset_label='+label;

		 //$window.location.href=url;
		$qbeViewer.openQbeInterface($scope,url);
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
			//console.log(response.data);
		    angular.copy(response.data.rows,$scope.previewDatasetModel);
		    if( $scope.previewDatasetColumns.length==0){
			$scope.createColumnsForPreview(response.data.metaData.fields);
		    }
		
			
			//$scope.startPreviewIndex=$scope.startPreviewIndex=0+20;
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.preview.error'));
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
    	
      console.info("[ADD NEW DATASET]: Opening the Dataset wizard for creation of a new Dataset in the Workspace.");	
      
      $scope.editingDatasetFile = false;
      
      /**
       * Initialize all the data needed for the 'dataset' object that we are sending towards the server when going to the Step 2 and ones that we are using
       * internally (such as 'limitPreviewChecked'). This initialization should be done whenever we are opening the Dataset wizard, since the behavior should 
       * be the reseting of all fields on the Step 1.
       * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
       */
      $scope.initializeDatasetWizard(undefined);
      
      $mdDialog.show({
		  scope:$scope,
		  preserveScope: true,
	      controller: DatasetCreateController,
	      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetCreateDialogTemplate.html',  
	      clickOutsideToClose: false,
	      escapeToClose :true,
	      //fullscreen: true,
	      locals:{
	    	 // previewDatasetModel:$scope.previewDatasetModel,
	         // previewDatasetColumns:$scope.previewDatasetColumns 
	      }
	    });
    }
    
  
    /**
	 * Set the currently active Datasets tab. Initially, the 'My Data Set' tab is selected (active). 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.currentDatasetsTab = "myDataSet";
	
    $scope.switchDatasetsTab = function(datasetsTab) {
    	
    	$scope.currentDatasetsTab = datasetsTab;
    
    	if($scope.selectedDataset !== undefined){
    		$scope.selectDataset(undefined);
         }
    	
    	if($scope.selectedCkan !== undefined){
    		$scope.selectCkan(undefined);
         }
    	
    	$scope.ckanDatasetsList=[];
    	$scope.selectedCkanRepo={};
    	$scope.ckanDatasetsListInitial=[];
    	
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
    
    /**
     * function that is called after adding new dataset, to syncronize model
     */
    $scope.reloadMyData=function(){
    	
    	$scope.loadNotDerivedDatasets();

    	if ($scope.datasetsDocumentsLoaded==true) {
    		$scope.loadDatasets();
        	$scope.loadMyDatasets();
    	}
    	
    }
  
    function parseCkanRepository(){
    	var ckanUrls= datasetParameters.CKAN_URLS;
    	var ckanUrlsSplitted= ckanUrls.split("|");
    	
    	var repos=[];
    	
    	for(i=0;i<ckanUrlsSplitted.length-1;i+=2){
    		repo={};
    		repo.url=ckanUrlsSplitted[i];
    		repo.name=ckanUrlsSplitted[i+1];
    		repos.push(repo);
    	}
    	
    	return repos;
    }
    //CKAN 
   
    $scope.ckanRepos=parseCkanRepository();
    $scope.selectedCkanRepo={};
    $scope.ckanDatasetsList=[];
    $scope.ckanDatasetsListInitial=[];
	$scope.loadCkanDatasets=function(){
		var repo=$scope.selectedCkanRepo;
		params={};
		params.isTech=false;
		params.showDerivedDataset=false;
		params.ckanDs=true;
		params.ckanFilter="NOFILTER";
		params.showOnlyOwner=true;
		params.ckanOffset=0;
		params.ckanRepository=repo.url;
		
		config={};
		config.params=params;
		sbiModule_restServices.promiseGet("certificateddatasets", "","",config)
		.then(function(response) {
			//console.log(response.data);
            angular.copy(response.data.root,$scope.ckanDatasetsList);
            angular.copy($scope.ckanDatasetsList,$scope.ckanDatasetsListInitial);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"error");
		});
		
		
	}
	
	$scope.showCkanDetails = function() {
		return $scope.showCkanInfo && $scope.isSelectedCkanValid();
	};
	
	
	$scope.isSelectedCkanValid = function() {
		return $scope.selectedCkan !== undefined;
	};
	
	$scope.setCkanDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('rightCkan').isLockedOpen() && !$mdSidenav('rightCkan').isOpen()) {
			$scope.toggleCkanDetail();
		}

		$scope.showCkanInfo = isOpen;
	};
	
	$scope.toggleCkanDetail = function() {
		$mdSidenav('rightCkan').toggle();
	};
	
	$scope.selectCkan= function ( dataset ) { 
		if (dataset !== undefined) {
			//$scope.lastDatasetSelected = dataset;
		}
		var alreadySelected = (dataset !== undefined && $scope.selectedCkan === dataset);
		$scope.selectedCkan = dataset;
		if (alreadySelected) {
			$scope.selectedCkan=undefined;
			$scope.setCkanDetailOpen(!$scope.showCkanDetail);
		} else {
			$scope.setCkanDetailOpen(dataset !== undefined);
		}
	};
	
	$scope.showDetailCkan=function(ckan){
		
		$mdDialog.show({
  		  scope:$scope,
			  preserveScope: true,
		      controller: DialogCkanController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/ckanDetailTemplate.html',  
		      clickOutsideToClose:false,
		      escapeToClose :false,
		      fullscreen: true,
		      locals:{ckan:ckan }
		    })
	}
	
	
	
	
	$scope.editCkan=function(ckan){
		//console.log(ckan);
		config={};
		params={};
        params.url=ckan.configuration.Resource.url;	
        params.format=ckan.configuration.Resource.format;
        params.id=ckan.configuration.ckanId;
        params.showOnlyOwner=true;
        params.showDerivedDataset=false;
        config.params=params;
		sbiModule_restServices.promiseGet("ckan-management/download", "","",config)
		.then(function(response) {
			//console.log(response.data);
			var data=response.data;
			var ckanObj=createCkanForWizard(data,ckan);
			$scope.initializeDatasetWizard(ckanObj);
			 $scope.fileObj={};
			 $scope.dataset.fileUploaded=true;
			 $scope.ckanInWizard=true;
			 $mdDialog.show({
	    		  scope:$scope,
	    		  preserveScope: true,
	    	      controller: DatasetCreateController,
	    	      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetCreateDialogTemplate.html',  
	    	      clickOutsideToClose: false,
	    	      escapeToClose :true,
	    	      //fullscreen: true,
	    	      locals:{
	    	    	
	    	      }
	    	    });
			
			
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"error");
		});
	}
	
	function createCkanForWizard(data, ckan){
		
		var toReturn={
			fileType: data.filetype,
		    fileName: data.filename,
		    csvEncoding:"UTF-8",
		    csvDelimiter:",",
		    csvQuote:"\"",
		    skipRows:0,
		    limitRows:0,
		    xslSheetNumber:0,
		    catTypeVn:'',
		    catTypeId:null,
		    id:'',
		    label:'',
		    name:'',
		    description:'',
		    meta:[]   	
		};
		
		return toReturn;
	}
    
    function DatasetPreviewController($scope,$mdDialog,$http){
		
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
	
    function DialogCkanController($scope,$mdDialog,ckan){
    	$scope.ckan=ckan;
    	$scope.closeCkanDetail=function(){
    		$mdDialog.cancel();
    	}
    	
    }

    $scope.editFileDataset = function (arg) {
    	
    	  $scope.initializeDatasetWizard(arg);
    	  
    	  // Set the flag for editing the current dataaset (file)
    	  $scope.editingDatasetFile = true;
    	 
//    	  $scope.dataset=arg;
//          $scope.dataset.xslSheetNumber=Number(arg.xslSheetNumber);
//          $scope.dataset.skipRows=Number(arg.skipRows);
//          $scope.dataset.limitRows=Number(arg.limitRows);
          //$scope.fileObj={};
         // $scope.fileObj.filename=arg.fileName;
          
          $mdDialog.show({
    		  scope:$scope,
    		  preserveScope: true,
    	      controller: DatasetCreateController,
    	      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetCreateDialogTemplate.html',  
    	      clickOutsideToClose: false,
    	      escapeToClose :true,
    	      //fullscreen: true,
    	      locals:{
    	    	 // previewDatasetModel:$scope.previewDatasetModel,
    	         // previewDatasetColumns:$scope.previewDatasetColumns 
    	      }
    	    });
    }
        
}