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

function DatasetCreateController($scope,$mdDialog,sbiModule_restServices,sbiModule_user,sbiModule_config,multipartForm,$http,sbiModule_messaging,sbiModule_translate) {

	var translate = sbiModule_translate;
	
	$scope.fileObj={};
	$scope.datasetWizardView=1;
	$scope.datasetCategories = [];
	$scope.dsGenMetaProperty = [];
	$scope.dsMetaProperty = [];
	$scope.dsMetaValue = [];
	$scope.category = null;
	$scope.datasetColumns=[];
	$scope.changingFile = false;
	$scope.categorySet = null;
	
	/**
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.submitStep1 = function() {
		
		var params = {};
		
		params.SBI_EXECUTION_ID = -1;
		params.isTech = false;
		params.showOnlyOwner = true;
		params.showDerivedDataset = false;		
		
		$scope.dataset.type = "File";
		$scope.dataset.persist = false;
		$scope.dataset.tablePrefix = datasetParameters.TABLE_NAME_PREFIX+sbiModule_user.userId+"_";
		$scope.dataset.tableName = "";
		
		/**
		 * If those three numeric fields are not provided, they will be NULL. For that reason, we redefined their values as an empty string, so they can be processed
		 * as a valid values (like in old implementation that could take an empty string as a numeric field value - consequence of the ExtJS framework).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.dataset.skipRows == null)
			$scope.dataset.skipRows = "";
		
		if ($scope.dataset.limitRows == null)
			$scope.dataset.limitRows = "";
		
		if ($scope.dataset.xslSheetNumber == null)
			$scope.dataset.xslSheetNumber = 1;
		
		$scope.dataset.meta = JSON.stringify($scope.dataset.meta);
		
		//console.log($scope.dataset);
		
		$http
		(
			{
				method: 'POST',
//				url: sbiModule_config.host+'/knowage/restful-services/selfservicedataset/testDataSet',
				url: sbiModule_config.host + sbiModule_config.contextName + '/restful-services/selfservicedataset/testDataSet',
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

				if (!response.data.errors) {
					
					console.info("[SUCCESS]: The Step 1 form is submitted successfully.");
					
					$scope.datasetWizardView = $scope.datasetWizardView + 1;
					
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
					console.info("[ERROR]: ",translate.load(response.data.errors[0].message));
					// Reset the meta after first unsuccessful try to go to Step 2 (danristo)
					$scope.dataset.meta = [];
					sbiModule_messaging.showErrorMessage(translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'));
				}
			}, 
				
			function errorCallback(response) {
			// called asynchronously if an error occurs
			// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				console.log(response);
				sbiModule_messaging.showErrorMessage("Failure!", sbiModule_translate.load('sbi.generic.failure'));
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
		
		/**
		 * If the editing of the existing Dataset is in progress, reset its label in order to save a new XLS/CSV file.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.editingDatasetFile==true && $scope.dataset.fileUploaded==true) {
			$scope.dataset.label = "";
		}
		
		//console.log($scope.dataset);
			
		var params = {};
			
		params.SBI_EXECUTION_ID = -1;
			
		$http
		(
			{
				method: 'POST',
//				url: sbiModule_config.host+'/knowage/restful-services/selfservicedataset/getDataStore',
				url: sbiModule_config.host + sbiModule_config.contextName + '/restful-services/selfservicedataset/getDataStore',
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
					console.log(response);
					angular.copy(response.data.metaData.fields,$scope.resultMetaDataStep2);
					// Take all results (pure data) for rows of the Angular table
					angular.copy(response.data.rows,$scope.resultRowsStep2);
					$scope.collectHeadersForStep3Preview();
					$scope.datasetWizardView = $scope.datasetWizardView +1;
				}
				else {
					console.info("[ERROR]: ",translate.load(response.data.errors[0].message));
					$scope.validationStatus = false;
					sbiModule_messaging.showErrorMessage(translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'));
				}
			}, 
			
			function errorCallback(response) {
			// called asynchronously if an error occurs
			// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				console.log(response);
				$scope.validationStatus = false;
				sbiModule_messaging.showErrorMessage("Failure!", sbiModule_translate.load('sbi.generic.failure'));
			}
		);
	}
	
	/**
	 * Final submit
	 */
	$scope.submitStep4 = function() {
		
		$scope.dataset.isPublicDS = false;
		$scope.dataset.meta = $scope.dataset.datasetMetadata;
		delete $scope.dataset['datasetMetadata'];
			
		var d = new Date();
		var label = 'ds__' + d.getTime()%10000000; 
		
		if($scope.dataset.label==='') {
			$scope.dataset.label = label;
		}		
	
		var params = {};
		params.showDerivedDataset=false;
		params.SBI_EXECUTION_ID = -1;
		params.isTech = false;
		params.showOnlyOwner=true;
		
		console.log($scope.dataset);
			
		/*sbiModule_restServices.promisePost("selfservicedataset","save?SBI_EXECUTION_ID=-1&isTech=false&showOnlyOwner=true&showDerivedDataset=false","",str)
			.then(function(response) {
				          console.log(response);
				          // binds changed value to object
//				          dataset.isPublic=response.data.isPublic;
//				          if(response.data.isPublic){
//				          sbiModule_messaging.showSuccessMessage(translate.load('sbi.workspace.dataset.share.success'),translate.load('sbi.workspace.dataset.success'));
//				          }else{
//				        	  
//				            sbiModule_messaging.showSuccessMessage(translate.load('sbi.workspace.dataset.unshare.success'),translate.load('sbi.workspace.dataset.success'));	  
//				          }
			},function(response){
				sbiModule_restServices.errorHandler(response.data,translate.load('sbi.workspace.dataset.fail'));
			});*/
		
		$http
		(
			{
				method: 'POST',
//				url: sbiModule_config.host+'/knowage/restful-services/selfservicedataset/save',
				url: sbiModule_config.host + sbiModule_config.contextName + '/restful-services/selfservicedataset/save',
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
				console.log(response);
				if (!response.data.errors) {
				
					console.info("[SUCCESS]: The Step 4 form is submitted successfully. The file dataset is saved");
					
					/**
					 * If some dataset is removed from the filtered set of datasets, clear the search input, since all datasets are refreshed.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					$scope.searchInput = "";
					
					/**
					 * After successful saving a file dataset, close the dialog (Dataset wizard). Also, reload necessary datasets after a new dataset 
					 * creation or a modification of an existing dataset).
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					$scope.closeDatasetCreateDialog();
					$scope.reloadMyData();
					
					/**
					 * If the ID value of the response data is negative (-1), that means that we edited (changed, modified) an existing file dataset.
					 * Otherwise, a new dataset is created and its ID is returned.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					if (response.data.id >= 0) {						
						sbiModule_messaging.showSuccessMessage(sbiModule_translate.format(sbiModule_translate.load('sbi.workspace.dataset.wizard.submit.success.save.msg'), 
								response.config.data.name), translate.load('sbi.generic.success'));						
					}
					else {
						sbiModule_messaging.showSuccessMessage(sbiModule_translate.format(sbiModule_translate.load('sbi.workspace.dataset.wizard.submit.success.update.msg'), 
								response.config.data.name), translate.load('sbi.generic.success'));
					}					
					
				}
				else {
					
					console.info("[ERROR]: ",translate.load(response.data.errors[0].message));
					sbiModule_messaging.showErrorMessage(translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'));
					
				}
			}, 
			
			function errorCallback(response) {
			// called asynchronously if an error occurs
			// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");			
				
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.workspace.dataset.wizard.submit.failure.msg'), 
						sbiModule_translate.load('sbi.generic.failure'));
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
	
	$scope.closeDatasetCreateDialog = function() {
		
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
	
	loadDatasetValues = function(a,b) {

		sbiModule_restServices.promiseGet(a,b)
		.then(function(response) {
			//console.log(response.data);
			if(b=="") {
			angular.copy(response.data,$scope.datasetCategories)
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
	//loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=CATEGORY_TYPE");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_GEN_META_PROPERTY");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_PROPERTY");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_VALUE");
    
	$scope.uploadFile= function(){
	
    	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedataset/fileupload",$scope.fileObj).success(

				function(data,status,headers,config){
					
					if(data.hasOwnProperty("errors")){						
						console.info("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load(data.errors[0].message), sbiModule_translate.load('sbi.generic.error'));
					}
					else {
						
						console.info("[UPLOAD]: SUCCESS!");
						
						sbiModule_messaging.showSuccessMessage(sbiModule_translate.format(sbiModule_translate.load('sbi.workspace.dataset.wizard.upload.success'), 
								$scope.fileObj.fileName), translate.load('sbi.generic.success'));
					
						$scope.file={};
						$scope.dataset.fileType = data.fileType;
						$scope.dataset.fileName = data.fileName;
						
						/**
						 * When user re-uploads a file, we should reset all fields that we have on the bottom panel of the Step 1, for both file types 
						 * (CSV and XLS), so the user can start from the scratch when defining new/modifying existing file dataset. 
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.dataset.csvEncoding = $scope.csvEncodingDefault;
						$scope.dataset.csvDelimiter = $scope.csvDelimiterDefault;
						$scope.dataset.csvQuote = $scope.csvQuoteDefault;
						$scope.dataset.skipRows = $scope.skipRowsDefault;
						$scope.dataset.limitRows = $scope.limitRowsDefault;
						$scope.dataset.xslSheetNumber = $scope.xslSheetNumberDefault;
						
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
					sbiModule_messaging.showErrorMessage($scope.fileObj.fileName+" could not be uploaded."+data.errors[0].message, sbiModule_translate.load('sbi.generic.failure'));
				});
    	
    }
		
	$scope.prepareMetaForView = function(item,index){				
		
		/**
		 * If the user just opens the Dataset wizard dialog and goes to the Step 2, the grid will be initialized with the saved (when updating/editing) or with the
		 * default (when creating a new File dataset) data. In that situation, the 'item' and 'index' will be undefined. These two values are defined only when user
		 * clicks on the Value column comboboxes for Field type of the particular column. They tell us the type of the Field type (ATTRIBUTE or MEASURE). So, this
		 * variable will be true only when just opening (entering) the Step 2.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var initialization = !item && !index;
		
		$scope.prepareMetaValue($scope.dataset.meta.columns,item,index);
				
		for(i=0; i< $scope.dataset.meta.columns.length;i++) {
							
			loc = $scope.dataset.meta.columns[i];
						
			var pname = loc.pname;

			loc.dsMetaValue=[];
			
			/**
			 * If initializing (entering) the Step 2, the expression (pname=="type") will indicate that we are dealing with the Type type of the Attribute column 
			 * (possible values of these combo boxes: String, Integer, Double). In that case, inspect the subsequent Field type type (ATTRIBUTE or MEASURE) and in
			 * the case it is a MEASURE, remove the String item from the current Type combobox, since the MEASURE can be only Integer/Double. Otherwise, if the
			 * Attribute column value is the Field type, just proceed with the filtering of metadata. 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
			 */			
			if (initialization) {	
				
				if (pname=="type") {
					loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined,$scope.dataset.meta.columns[i+1].pvalue);
				}					
				else {
					loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined);
				}
				
			}				
			// Click
			else  {
								
				if (index-1==i) {
					loc.dsMetaValue = $scope.filterMetaValue($scope.dataset.meta.columns[index-1].pname,item,i,index);	
				}
				else {
					
					if (pname=="type")
						loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined,$scope.dataset.meta.columns[i+1].pvalue);
					else
						loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined);
					
				}
			}
			
			/**
			 * danristo
			 */
			loc.indexOfRow = i;

			/**
			 * If user selects the MEASURE field type after having a field type of ATTRIBUTE and type String for a particular data column, the first
			 * item in the type combobox for a MEASURE field type will be selected (e.g. the Integer will be selected). This is implemented instead of
			 * having an empty combo for type when performing this scenario.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (i%2==0 && loc.dsMetaValue && item) {
				
				if (item.toUpperCase()=="MEASURE" && loc.pvalue.toLowerCase()=="string") {
					loc.pvalue = loc.dsMetaValue[0].VALUE_CD;					
				}				
				
			}
			
			/**
			 * Change the GUI element type for first two columns of the Step 2 of the Dataset wizard, from combo box ('md-select') to the label (fixed value).
			 * This is done as a temporary solution - besides this, these things are removed: 'Columns/Dataset' combo box, 'Add new row' button, 'Clear all' 
			 * button, delete row item in each row. The only dynamic behavior has the 'Value' column, since we can choose between Integer and Double for MEASURES
			 * and an additional String option for ATTRIBUTES, so for this reason we are keeping the combo box element.
			 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
//				loc.columnView='<md-select aria-label="column-view" ng-model=row.column class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumns" value="{{col.columnName}}">{{col.columnName}}</md-option></md-select>';
//				loc.pnameView='<md-select aria-label="pname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsMetaProperty" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.filterMetaValues(col.VALUE_CD,row)">{{col.VALUE_NM}}</md-option></md-select>';
		
			loc.columnView='<label>{{row.column}}</label>';
			loc.pnameView='<label>{{row.pname}}</label>';
			loc.pvalueView='<md-select aria-label="pvalue-view" ng-model=row.pvalue class="noMargin"><md-option ng-repeat="col in row.dsMetaValue" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.valueChanged(col,row.indexOfRow)">{{col.VALUE_NM}}</md-option></md-select>';
		
		}
	}	
	
	$scope.prepareDatasetForView = function() {
		var datasets = $scope.dataset.meta.dataset
		for (var i = 0; i < datasets.length; i++) {
			datasets[i].pnameView = '<md-select aria-label="dspname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsGenMetaProperty" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>';
			datasets[i].pvalueView = ' <input ng-model="row.pvalue"></input>';
		}
	}
	
	$scope.filterMetaValue = function(pname,item,i,index,myFieldType){
		
		var filteredMetaValues = [];
		
		/**
		 * A flag that will indicate if the "String" item in the Value column combobox for the belonging Field type row for the current column
		 * should be excluded. It does if one of two cases specified below are true.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
		var insertString = true;
		
		/**
		 * Cases in which the "String" item in the Field type should be excluded:
		 * 
		 * myFieldType=="MEASURE": 
		 * 		If initializing (entering) the Step 2, inspect the next Field type value and if it is a MEASURE, remove the "String" values 
		 * 		from the current Type row (since the column of the MEASURE field type cannot be a String). If it is not a MEASURE, i.e. if 
		 * 		it is an ATTRIBUTE, continue with initializing items in the Type combobox for particular row without any modification (include 
		 * 		the "String" item as well).
		 * 
		 * index && item=="MEASURE":
		 * 		If the user clicks on some Field type combobox (the combo in the Value column in the Step 2) and choose a MEASURE, its 
		 * 		belonging Type value (for the same column, e.g. Country) should exclude the "String" item. In that case the "index" input 
		 * 		parameter is defined.
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
		if (myFieldType=="MEASURE" || index && item=="MEASURE")
			insertString = false;
		
		if(pname.toLowerCase()==="type".toLowerCase()){
			
			for(j=0;j<$scope.dsMetaValue.length;j++){ 

				 if($scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="string".toLowerCase() && insertString || 
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
				typeValue = typeValue!=null ? typeValue.replace("java.lang.","") : null;
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
		
		var notValidStep1FileName = !$scope.dataset.fileName || $scope.prevUploadedFile!=$scope.fileObj.fileName;
		var notValidStep1SkipRows = $scope.dataset.skipRows==undefined  && isNaN(Number($scope.dataset.skipRows));
		var notValidStep1LimitRows = $scope.dataset.limitRows==undefined && isNaN(Number($scope.dataset.limitRows));
		var notValidStep1SheetNum = $scope.dataset.xslSheetNumber==undefined && isNaN(Number($scope.dataset.xslSheetNumber));
				
		if ($scope.datasetWizardView==1) {
			
			if (notValidStep1FileName) {
				
				if (!$scope.dataset.fileName) {
					return translate.load('sbi.workspace.dataset.wizard.nextbutton.uploadfile.tooltip');
				}	
				else if ($scope.prevUploadedFile!=$scope.fileObj.fileName) {
					return translate.load('sbi.workspace.dataset.wizard.nextbutton.uploadnewfile.tooltip');
				}
				
			}
			else if (notValidStep1SkipRows) {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.invalid.skiprows.tooltip');
			}
			else if (notValidStep1LimitRows) {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.invalid.limitrows.tooltip');
			}
			else if (notValidStep1SheetNum) {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.invalid.sheetnumber.tooltip');
			}
			else {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.valid.tooltip');
			}
			
		}
		else if ($scope.datasetWizardView==4) {
			
			if ($scope.dataset.name=='') {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.save.missing.name.tooltip');
			}
			else if ($scope.dataset.persist && $scope.dataset.tableName=='') {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.save.missing.tablename.tooltip');
			}
			else {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.save.valid.tooltip');
			}
			
		}
		else {
			return translate.load('sbi.workspace.dataset.wizard.nextbutton.valid.tooltip');
		}
		
	}
	
	/**
	 * Handle the title content (tooltip) of the 'Upload' button in the Step 1 of the Dataset wizard (dialog) according to the state of data that is necessary 
	 * for the creation of the Dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.datasetWizStep1UploadButtonTitle = function() {
		
		if (!$scope.fileObj.fileName) {
			return translate.load('sbi.workspace.dataset.wizard.uploadbutton.missing.file.tooltip');
		}
		else {
			return translate.load('sbi.workspace.dataset.wizard.uploadbutton.missing.browsedfile.tooltip');
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
		 * scenario2: If there is no uploaded file or the browsed file is not the same as the one that is uploaded previously. It is important that we are not in editing
		 * mode, in order for this code to work properly.
		 * scenario3: If on the Step 4, when finalizing the dataset creation user does not specify DS name or the name of the table if persisting.
		 * scenario4: If the number fields contain invalid values (values less than 0), disable the Next button.
		 */
		var step1 = $scope.datasetWizardView==1;
		var step4 = $scope.datasetWizardView==4;
			
		var changingFile = $scope.changingFile==true;
		var newUplFileDiffOldUpl = $scope.dataset.fileName!=$scope.fileObj.fileName;
		var nameOfDSOrTableNameNotDef = $scope.dataset.name=='' || $scope.dataset.persist && $scope.dataset.tableName=='';
		var fileNotUplOrNewOneNotUpl = !$scope.dataset.fileName || $scope.prevUploadedFile!=$scope.fileObj.fileName;
						
		var scenario1 = step1 && changingFile && newUplFileDiffOldUpl;
		var scenario2 = step1 && !$scope.editingDatasetFile && !changingFile && fileNotUplOrNewOneNotUpl;
		var scenario3 = step4 && nameOfDSOrTableNameNotDef;
		var scenario4 = step1 && ($scope.dataset.skipRows==undefined  && isNaN(Number($scope.dataset.skipRows))
									|| $scope.dataset.limitRows==undefined && isNaN(Number($scope.dataset.limitRows))
										|| $scope.dataset.xslSheetNumber==undefined && isNaN(Number($scope.dataset.xslSheetNumber)));
	
		return scenario1 || scenario2 || scenario3 || scenario4;
		
	}
	
	$scope.changeUploadedFile=function(){
		
		console.info("CHANGE FILE [IN]");
		
		$scope.changingFile = true;
		
		/**
		 * If we are about to change the uploaded file in editing mode, we should keep the data about the name of the previously uploaded
		 * file, in order to keep it in the line for the browsed file.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
		if ($scope.editingDatasetFile==true) {
			
			$scope.fileObj = 
			{
				file: { name:$scope.dataset.fileName }, 
				fileName: $scope.dataset.fileName
			};
			
		}
			
//		$scope.fileObj={};
//		$scope.fileObj.fileName='';
//		$scope.dataset.fileName='';
		
		console.info("CHANGE FILE [OUT]");
		
	}
	
}