function DatasetCreateController($scope,$mdDialog,sbiModule_restServices,sbiModule_user,sbiModule_config,multipartForm,$http,sbiModule_messaging,sbiModule_translate ){
	$scope.fileObj={};
	$scope.datasetWizardView=1;
	$scope.datasetCategories = [];
	$scope.datasetCategoryType = [];
	$scope.dsGenMetaProperty = [];
	$scope.dsMetaProperty = [];
	$scope.dsMetaValue = [];
	$scope.category = null;
	$scope.datasetColumns=[];
	$scope.changingFile = false;
	
	/**
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.submitStep1 = function() {
		
		var params = {};
		
		params.SBI_EXECUTION_ID = -1;
		params.isTech = false;
		params.showOnlyOwner = true;
		params.showDerivedDataset = false;
	
		
		
//			$scope.dataset.id = "";
		$scope.dataset.type = "File";
//			$scope.dataset.label = "";
//			$scope.dataset.name = ""
//			$scope.dataset.description = "";
		$scope.dataset.persist = false;
		$scope.dataset.tablePrefix = datasetParameters.TABLE_NAME_PREFIX+sbiModule_user.userId+"_";
		$scope.dataset.tableName = "";
//		$scope.dataset.fileUploaded = false;
		
		if ($scope.dataset.limitRows == null)
			$scope.dataset.limitRows = "";
		
		$scope.dataset.meta = JSON.stringify($scope.dataset.meta);
		
		//console.log($scope.dataset);
		
		$http
		(
			{
				method: 'POST',
				url: sbiModule_config.host+'/knowage/restful-services/selfservicedataset/testDataSet',
				data: $scope.dataset,
				params:params,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},
				
				transformRequest: function(obj) {
					
					var str = [];
					
					for(var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					
					return str.join("&");
					
				},
			}
		)
		.then
		(
			function successCallback(response) {

				if (!response.data.errors) {
					console.info("[SUCCESS]: The Step 1 form is submitted successfully.");
					$scope.datasetWizardView = $scope.datasetWizardView +1;
					console.log(response.data);
					//set meta to empty
					$scope.dataset.meta=[];
					angular.copy(response.data.meta,$scope.dataset.meta);
					angular.copy(response.data.datasetColumns,$scope.datasetColumns);
					$scope.prepareMetaForView();
					$scope.prepareDatasetForView();
					
					// Set a flag to indicate that we are not changing uploaded file any more (the 'Change file' button will appear when returning back to the Step 1.
					$scope.changingFile = false;
				}
				else {
					console.info("[ERROR]: ",sbiModule_translate.load(response.data.errors[0].message));
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error!');
				}
			}, 
				
			function errorCallback(response) {
			// called asynchronously if an error occurs
			// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				console.log(response);
				sbiModule_messaging.showErrorMessage("Failure!", 'Error!');
			}
		);
	}
	
	$scope.submitStep2 = function() {
		
		$scope.dataset.isPublicDS = false;
		$scope.dataset.datasetMetadata={};
		$scope.dataset.datasetMetadata.version=1;
		$scope.dataset.datasetMetadata.dataset=[];
		$scope.dataset.datasetMetadata.columns=[];
		angular.copy($scope.dataset.meta.dataset,$scope.dataset.datasetMetadata.dataset);
		angular.copy($scope.dataset.meta.columns,$scope.dataset.datasetMetadata.columns);
  
		c=$scope.dataset.datasetMetadata.columns;
		for (var i = 0; i < c.length; i++) {
			delete c[i].columnView;
			delete c[i].pvalueView;
			delete c[i].pnameView;
			delete c[i].dsMetaValue;
		}
		
		d=$scope.dataset.datasetMetadata.dataset;
		for (var i = 0; i < d.length; i++) {
			delete d[i].pvalueView;
			delete d[i].pnameView;
		}
		
		$scope.dataset.datasetMetadata = JSON.stringify($scope.dataset.datasetMetadata);
		$scope.dataset.limitPreview = $scope.limitPreviewChecked;
		$scope.dataset.page = 1;
		$scope.dataset.start = "";
		$scope.dataset.page = 10;
		
		console.log($scope.dataset);
			
		var params = {};
			
		params.SBI_EXECUTION_ID = -1;
			
		$http
		(
			{
				method: 'POST',
				url: sbiModule_config.host+'/knowage/restful-services/selfservicedataset/getDataStore',
				data: $scope.dataset,
				params: params,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},
				
				transformRequest: function(obj) {
					
					var str = [];
					
					for(var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					
					return str.join("&");
					
				},
			}
		)
		.then
		(
			function successCallback(response) {

				console.log("SUCCESS");					
				console.log(response.data);
				
				if (!response.data.errors) {
					console.info("[SUCCESS]: The Step 2 form is submitted successfully.");
					// Take the meta data from resulting JSON object (needed for the table's header)
					$scope.validationStatus = true;
					$scope.resultMetaDataStep2 = [];
					$scope.resultRowsStep2 = [];
					angular.copy(response.data.metaData.fields,$scope.resultMetaDataStep2);
					// Take all results (pure data) for rows of the Angular table
					angular.copy(response.data.rows,$scope.resultRowsStep2);
					$scope.collectHeadersForStep3Preview();
					$scope.datasetWizardView = $scope.datasetWizardView +1;
				}
				else {
					console.info("[ERROR]: ",sbiModule_translate.load(response.data.errors[0].message));
					$scope.validationStatus = false;
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error!');
				}
			}, 
			
			function errorCallback(response) {
			// called asynchronously if an error occurs
			// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				console.log(response);
				$scope.validationStatus = false;
				sbiModule_messaging.showErrorMessage("Failure!", 'Error!');
			}
		);
	}
	
	/**
	 * Final submit
	 */
	$scope.submitStep4 = function() {
		
		$scope.dataset.isPublicDS = false;
//			$scope.dataset.meta = '{"version":1,"dataset":[],"columns":[{"column":"country","pname":"Type","pvalue":"String"},{"column":"country","pname":"fieldType","pvalue":"ATTRIBUTE"},{"column":"region","pname":"Type","pvalue":"String"},{"column":"region","pname":"fieldType","pvalue":"ATTRIBUTE"},{"column":"province","pname":"Type","pvalue":"String"},{"column":"province","pname":"fieldType","pvalue":"ATTRIBUTE"},{"column":"store_sales_promo_2012","pname":"Type","pvalue":"Double"},{"column":"store_sales_promo_2012","pname":"fieldType","pvalue":"MEASURE"},{"column":"unit_sales_promo_2012","pname":"Type","pvalue":"Double"},{"column":"unit_sales_promo_2012","pname":"fieldType","pvalue":"MEASURE"},{"column":"store_sales_promo_2013","pname":"Type","pvalue":"Double"},{"column":"store_sales_promo_2013","pname":"fieldType","pvalue":"MEASURE"},{"column":"unit_sales_promo_2013","pname":"Type","pvalue":"Double"},{"column":"unit_sales_promo_2013","pname":"fieldType","pvalue":"MEASURE"}]}';
		$scope.dataset.meta = $scope.dataset.datasetMetadata;
		delete $scope.dataset['datasetMetadata'];
		//console.log("meta",$scope.dataset.meta);
			
		var d = new Date();
		var label = 'ds__' + d.getTime()%10000000; 
		
		if($scope.dataset.label===''){
		$scope.dataset.label = label;
		}
		console.log($scope.dataset);
		
		var params = {};
		params.showDerivedDataset=false;
		params.SBI_EXECUTION_ID = -1;
		params.isTech = false;
		params.showOnlyOwner=true;
			
		/*sbiModule_restServices.promisePost("selfservicedataset","save?SBI_EXECUTION_ID=-1&isTech=false&showOnlyOwner=true&showDerivedDataset=false","",str)
			.then(function(response) {
				          console.log(response);
				          // binds changed value to object
//				          dataset.isPublic=response.data.isPublic;
//				          if(response.data.isPublic){
//				          sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.workspace.dataset.share.success'),sbiModule_translate.load('sbi.workspace.dataset.success'));
//				          }else{
//				        	  
//				            sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.workspace.dataset.unshare.success'),sbiModule_translate.load('sbi.workspace.dataset.success'));	  
//				          }
			},function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.workspace.dataset.fail'));
			});*/
		
		$http
		(
			{
				method: 'POST',
				url: sbiModule_config.host+'/knowage/restful-services/selfservicedataset/save',
				data: $scope.dataset,
				params: params,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},
				
				transformRequest: function(obj) {
					
					var str = [];
					
					for(var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					
					console.log(str.join("&"));
					return str.join("&");
					
				},
			}
		)
		.then
		(
			function successCallback(response) {

				console.log("SUCCESS aaaaa");					
				console.log(response.data);
				
				if (!response.data.errors) {
					console.info("[SUCCESS]: The Step 2 form is submitted successfully.");
					// Take the meta data from resulting JSON object (needed for the table's header)
					$scope.validationStatus = true;
					$scope.resultMetaDataStep2 = [];
					$scope.resultRowsStep2 = [];
					angular.copy(response.data.metaData.fields,$scope.resultMetaDataStep2);
					// Take all results (pure data) for rows of the Angular table
					angular.copy(response.data.rows,$scope.resultRowsStep2);
					$scope.collectHeadersForStep3Preview();
					$scope.datasetWizardView = $scope.datasetWizardView +1;
				}
				else {
					console.info("[ERROR]: ",sbiModule_translate.load(response.data.errors[0].message));
					$scope.validationStatus = false;
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error!');
				}
			}, 
			
			function errorCallback(response) {
			// called asynchronously if an error occurs
			// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				console.log(response);
				
				/**
				 * NOTE: Temporary solution!
				 * WORKAROUND
				 * TODO
				 * @author Danilo Ristovski
				 */
				$scope.closeDatasetCreateDialog();
				$scope.loadMyDatasets();
				$scope.loadDatasets();
				
//					$scope.validationStatus = false;
//					sbiModule_messaging.showErrorMessage("Failure!", 'Error!');
			}
		);
		
	}
	
	$scope.toggleDWVNext = function() {
	
//			console.log($scope.dataset);
		
		/**
		 * Remember the lastly chosen metadata type from the combo box on the Step 2 when leaving it.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)  
		 */
		$scope.metadataId = $scope.metadataType.value;
		
		/**
		 * Call this service only when submitting the form data from the Step 1.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		switch($scope.datasetWizardView) {
			case 1: $scope.submitStep1(); break;
			case 2: $scope.submitStep2(); break;
			case 4: $scope.submitStep4(); break;
		}			
		
		/**
		 * Bigger then 1, because for the Step 1 we will move to the next step in the 'submitStep1()', according to the state of success of the service call.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
		if($scope.datasetWizardView>2 && $scope.datasetWizardView<4){
			$scope.datasetWizardView = $scope.datasetWizardView +1;
		}
	}
	
	$scope.toggleDWVBack = function() {
		
		/**
		 * Remember the lastly chosen metadata type from the combo box on the Step 2 when leaving it.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)  
		 */
		$scope.metadataId = $scope.metadataType.value;
		
		if($scope.datasetWizardView>1&&$scope.datasetWizardView<5){ 
			$scope.datasetWizardView = $scope.datasetWizardView -1;
		}
	}
		
	/**
	 * Collect all table headers so we can preview results in the Step 3.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.collectHeadersForStep3Preview = function() {			
		
		$scope.allHeadersForStep3Preview = [];
		
		for (i=0; i<$scope.resultMetaDataStep2.length; i++) {
			
			var temp = {};
			
			temp['label'] = $scope.resultMetaDataStep2[i].header;
			temp['name'] = 'column_' + (i+1);
			
			$scope.allHeadersForStep3Preview.push(temp);
			
	}
	
	}
	
	$scope.closeDatasetCreateDialog=function(){
		$mdDialog.cancel();
		$scope.datasetWizardView=1;
		
		/**
		 * Empty the object that was potentially uploaded previously when closing the dialog box. Re-initialize the variables that are linked to the 
		 * previously closed dataset configuration.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.dataset = {};
		$scope.limitPreviewChecked = false;
		$scope.category = {};
		
		/**
		 * Reset the metadata type when closing the dialog (dataset wizard). Next time we open the wizard, the first item from the metadata types 
		 * collection will be selected automatically (initially).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)  
		 */
		$scope.metadataId = 1;
		$scope.metadataType.value = 1;
		
		/**
		 * this is set when wizard is open from ckan 
		 */
		$scope.ckanInWizard=false;
	}
	
	loadDatasetValues= function(a,b){
		sbiModule_restServices.promiseGet(a,b)
		.then(function(response) {
			console.log(response.data);
			if(b=="") {
			angular.copy(response.data,$scope.datasetCategories)
			} else if(b=="?DOMAIN_TYPE=CATEGORY_TYPE"){
				angular.copy(response.data,$scope.datasetCategoryType)
				/**
				 * Initialize the category type for the new Dataset when the Dataset wizard appears in the Workspace. The initial value should be the first one in an
				 * array of all category types that are available (i.e. 'Cat1').
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				$scope.chooseCategory($scope.datasetCategoryType[0]);
			} else if(b=="?DOMAIN_TYPE=DS_GEN_META_PROPERTY"){
				angular.copy(response.data,$scope.dsGenMetaProperty)
			} else if(b=="?DOMAIN_TYPE=DS_META_PROPERTY"){
				angular.copy(response.data,$scope.dsMetaProperty)
			} else if(b=="?DOMAIN_TYPE=DS_META_VALUE"){
				angular.copy(response.data,$scope.dsMetaValue)
			}
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"faild to load data for"+b);
		});
	}
		
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=CATEGORY_TYPE");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_GEN_META_PROPERTY");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_PROPERTY");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_VALUE");
    
	$scope.uploadFile= function(){
	
    	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedataset/fileupload",$scope.fileObj).success(

				function(data,status,headers,config){
					if(data.hasOwnProperty("errors")){						
						console.info("[UPLOAD]: DATA HAS ERRORS PROPERTY!");		
						sbiModule_messaging.showErrorMessage($scope.fileObj.fileName+" could not be uploaded."+data.errors[0].message, 'Error!');
					}
					else {
					
						console.info("[UPLOAD]: SUCCESS!");
					sbiModule_messaging.showSuccessMessage($scope.fileObj.fileName+" successfully uploaded", 'Success!');
					
					$scope.file={};
						$scope.dataset.fileType = data.fileType;
						$scope.dataset.fileName = data.fileName;
						
						/**
						 * Whenever we upload a file, keep the track of its name, in order to indicate when the new one is browsed but not uploaded.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.prevUploadedFile = $scope.dataset.fileName;
						$scope.dataset.fileUploaded=true;
						$scope.changingFile = false;
						
					}
				}).error(function(data, status, headers, config) {
					console.info("[UPLOAD]: FAIL! Status: "+status);
					sbiModule_messaging.showErrorMessage($scope.fileObj.fileName+" could not be uploaded."+data.errors[0].message, 'Error!');
				});
    	
    }
	
	$scope.prepareMetaForView=function(){
		$scope.prepareMetaValue($scope.dataset.meta.columns);
		for(i=0; i< $scope.dataset.meta.columns.length;i++){
			loc = $scope.dataset.meta.columns[i];
		var pname = loc.pname;
		loc.dsMetaValue=[];
		loc.dsMetaValue=$scope.filterMetaValue(pname);	
		loc.columnView='<md-select aria-label="column-view" ng-model=row.column class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumns" value="{{col.columnName}}">{{col.columnName}}</md-option></md-select>';
		loc.pnameView='<md-select aria-label="pname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsMetaProperty" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.filterMetaValues(col.VALUE_CD,row)">{{col.VALUE_NM}}</md-option></md-select>';
		loc.pvalueView='<md-select aria-label="pvalue-view"ng-model=row.pvalue class="noMargin"><md-option ng-repeat="col in row.dsMetaValue" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>';	
		}	
	}
	
	$scope.prepareDatasetForView = function() {
		var datasets = $scope.dataset.meta.dataset
		for (var i = 0; i < datasets.length; i++) {
			datasets[i].pnameView = '<md-select aria-label="dspname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsGenMetaProperty" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>';
			datasets[i].pvalueView = ' <input ng-model="row.pvalue"></input>';
		}
	}
	
	$scope.filterMetaValue = function(pname){
		var filteredMetaValues = [];
		if(pname.toLowerCase()==="type".toLowerCase()){
			for(j=0;j<$scope.dsMetaValue.length;j++){
			 if($scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="string".toLowerCase()||
					 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="double".toLowerCase()||
					 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="integer".toLowerCase()){
				 filteredMetaValues.push($scope.dsMetaValue[j]);
			 }    			
		}
			
			
		}else if(pname.toLowerCase()==="fieldType".toLowerCase()){
			for(j=0;j<$scope.dsMetaValue.length;j++){
   			 if($scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="attribute".toLowerCase()||
   			    $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="measure".toLowerCase()){
   				filteredMetaValues.push($scope.dsMetaValue[j]);
   			 }
   				
   			
   			}
			
		}else{
			
			angular.copy($scope.dsMetaValue,filteredMetaValues);
			
		}
		return filteredMetaValues;
	}
	
	$scope.prepareMetaValue=function(values){
		
		for(i=0;i<values.length;i++){
			
			if (values[i].pname.toUpperCase() == 'type'.toUpperCase()){
				values[i].pname=values[i].pname.toLowerCase();
				typeValue = values[i].pvalue;
				typeValue = typeValue.replace("java.lang.","");
				values[i].pvalue = typeValue;
			}
		}
	}
	
	/**
	 * Handle the title content (tooltip) of the 'Next' button in the Step 1 and the Step 4 of the Dataset wizard (dialog) according to the state of data 
	 * that is necessary for the creation of the Dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.datasetWizStep1NextButtonTitle = function() {
		
		var notValidStep1 = !$scope.dataset.fileName || $scope.prevUploadedFile!=$scope.fileObj.fileName;
		
		if ($scope.datasetWizardView==1 && notValidStep1) {
			if (!$scope.dataset.fileName) {
				return 'Please upload XLS or CSV file in order to proceed with the dataset creation';
			}
			else if ($scope.prevUploadedFile!=$scope.fileObj.fileName) {
				return 'Please upload newly browsed XLS or CSV file in order to proceed with the dataset creation';
			}
		}
		else if ($scope.datasetWizardView==4) {
			if ($scope.dataset.name=='') {
				return 'Please provide the name of the dataset you want to save';
			}
			else if ($scope.dataset.persist && $scope.dataset.tableName=='') {
				return 'Please provide the table name of the dataset you want to save';
			}
			else {
				return 'Save the dataset';
			}
		}
		else {
			return 'Proceed to the next step';
		}
		
	}
	
	/**
	 * Handle the title content (tooltip) of the 'Upload' button in the Step 1 of the Dataset wizard (dialog) according to the state of data that is necessary 
	 * for the creation of the Dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.datasetWizStep1UploadButtonTitle = function() {
		
		if (!$scope.fileObj.fileName) {
			return 'Please browse for the XLS or CSV file in order to proceed with the dataset creation';
		}
		else {
			return 'Upload the browsed file';
		}
		
	}		
	
	/**
	 * Returns boolean condition if the Next/Save button should be disabled.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.nextOrSaveButtonCondition = function() {
		
		/**
		 * scenario1: If there is already an uploaded file, but user desides to change it and then browse for another one (different from the already uploaded one). In 
		 * combination with other scenario (criteria) it prevents going to Next step if the newly browsed file is not uploaded as well.
		 * scenario2: If there is no uploaded file or the browsed file is not the same as the one that is uploaded previously.
		 * scenario3: If on the Step 4, when finalizing the dataset creation user does not specify DS name or the name of the table if persisting.
		 */
		var step1 = $scope.datasetWizardView==1;
		var step4 = $scope.datasetWizardView==4;
		
		var changingFile = $scope.changingFile==true;
		var newUplFileDiffOldUpl = $scope.dataset.fileName!=$scope.fileObj.fileName;
		var nameOfDSOrTableNameNotDef = $scope.dataset.name=='' || $scope.dataset.persist && $scope.dataset.tableName=='';
		var fileNotUplOrNewOneNotUpl = !$scope.dataset.fileName || $scope.prevUploadedFile!=$scope.fileObj.fileName;
		
		var scenario1 = step1 && changingFile && newUplFileDiffOldUpl;
		var scenario2 = step1 && !changingFile && fileNotUplOrNewOneNotUpl;
		var scenario3 = step4 && nameOfDSOrTableNameNotDef;
		
		return scenario1 || scenario2 || scenario3;
		
	}
	
	$scope.changeUploadedFile=function(){
		console.info("CHANGE FILE [IN]");
		$scope.changingFile = true;
//		$scope.fileObj={};
//		$scope.fileObj.fileName='';
//		$scope.dataset.fileName='';
		console.info("CHANGE FILE [OUT]");
	}
	
}