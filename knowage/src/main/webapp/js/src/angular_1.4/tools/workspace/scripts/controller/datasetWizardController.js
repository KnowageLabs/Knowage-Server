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

function DatasetCreateController($scope, $mdDialog, sbiModule_restServices, sbiModule_user, sbiModule_config, multipartForm,
			$http, sbiModule_translate, toastr, $filter, $mdSidenav) {

	var translate = sbiModule_translate;
	$scope.showExportHDFS = sbiModule_user.functionalities.indexOf("DataSourceBigData")>-1;
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
	$scope.showErrors = false;
	$scope.hideErors = true;
	$scope.displayInvalidColumns = { value: false };
	var isDirty = false;
	var metaColumnsTemp = [];
	var gridForPreview;

	/**
	 * 'step2ValidationErrors' - contains the validation result. If there is not error after the validation, the property will
	 * not be present in the retrieved JSON got after the validating process and this scope variable will be of a value NULL.
	 * Otherwise, the 'validationErrors' property appears as a part of the returning JSON (result of calling the validation).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.step2ValidationErrors = null;
	/**
	 * The metadata of the already uploaded file. It will contain up-to-date state of the dataset metadata, namely it will keep
	 * all changes that the user provided for the metadata of the current dataset (whether it change them or not). When the new
	 * file is uploaded for the current (already existing) dataset, this variable will be refreshed with the new content (the
	 * default one for that file).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.metaDataCopy = null;
	$scope.csvConfChanged = false;
	/**
	 * The name of the newly uploaded file for the current dataset. It will server for comparing with the old file name in order
	 * to keep or reset the metadata of the previous file that was uploaded. Namely, when the file is changed (a new one is uploaded
	 * the metadata should be reset (the service should be called and the new metadata should be returned and applied).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.changedFileName = null;
	/**
	 * Set the flag if the dataset wizard is opened for the first time and if so, take these necessary configuration parameters
	 * for the dataset that is constructed on the base of the CSV file. These parameters are used for parsing the CSV file that
	 * is uploaded and are of great importance for setting the state on the Step 2 (Definition data), that represents the metadata
	 * of that CSV file, i.e. the file dataset that we are editing/creating.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.datasetInitial = true;
	/**
	 * Simple scope variables that will memorize the delimiter and quote sign, as well as encoding type for parsing the CSV file
	 * that is uploaded and will serve for comparing if at least one of them is changed at any time of Dataset wizard dialog life.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.csvDelimiter = "";
	$scope.csvQuoteChar = "";
	$scope.csvEncoding= "";
	$scope.dateFormat ="";
	$scope.timestampFormat = "";
	/**
	 * If the type of the file that is uploaded for the file dataset is CSV and the dataset is opened in the wizard for the first
	 * time, collect those mentioned CSV configuration parameters that are needed for the managing of the Step 2 (the metadata).
	 * At the same, time reset the state of "datasetInitial" scope variable (setting it to false), to avoid this if-statement, until
	 * next opening of the dataset wizard (for the same dataset or for some other).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	if ($scope.dataset.fileType.toUpperCase()=="CSV" && $scope.datasetInitial==true) {
		$scope.datasetInitial = false;
		$scope.csvDelimiter = $scope.dataset.csvDelimiter;
		$scope.csvQuoteChar = $scope.dataset.csvQuote;
		$scope.csvEncoding= $scope.dataset.csvEncoding;
		$scope.dateFormat = $scope.dataset.dateFormat;
		$scope.timestampFormat = $scope.dataset.timestampFormat;
	}
	/**
	 * These two scope variables represent the current state of the validation - if it is successful or not (passed or error,
	 * respectively). They will server as a flag for setting the validation status for respective metadata item (for particular
	 * row in the metadata Step 2). If the validation is passed or it recorder an error (cannot move to the Step 3), provide
	 * correct or invalid state on the Step 2.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.validationPassed = false;
	$scope.validationError = false;
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
		$scope.dataset.exportToHdfs = false;
		$scope.dataset.tablePrefix = datasetParameters.TABLE_NAME_PREFIX+sbiModule_user.userId+"_";
		if (!$scope.dataset.hasOwnProperty('persist'))
			$scope.dataset.persist = false;
		if (!$scope.dataset.hasOwnProperty('tableName'))
			$scope.dataset.tableName = "";
		/**
		 * This if-statement is almost the same as the one before, except we use this one for later usage, not when the
		 * dataset wizard is opened for the first time. According to the previous state of mentioned CSV configuration
		 * parameters (three counted here) and the new one, if there is any (at least one) change, set the scope variable
		 * "csvConfChanged" to boolean value of TRUE. This flag will be used for managing the metadata on the Step 2, i.e.
		 * if the metadata is going to be updated (refreshed, reloaded) or kept (if there is no change among these
		 * parameters at all). Afterwards, set the new state for these CSV configuration parameters to those scope variables
		 * so we can keep the current (actual) state.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.dataset.fileType.toUpperCase()=="CSV") {
			// If at least one of those parameters is changed, signal to re-parse the file and get new metadata.
			$scope.csvConfChanged = ($scope.csvDelimiter != $scope.dataset.csvDelimiter
										|| $scope.csvQuoteChar != $scope.dataset.csvQuote
											|| $scope.csvEncoding != $scope.dataset.csvEncoding || $scope.dateFormat != $scope.dataset.dateFormat
												|| $scope.timestampFormat != $scope.dataset.timestampFormat) ?  true : false;
			/**
			 * If the CSV configuration is changed on the Step 1, set these indicators to false in order to reset the
			 * validation status on Step 2 (when coming from the Step 1).
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if ($scope.csvConfChanged==true) {
				$scope.validationPassed = false;
				$scope.validationError = false;
			}
			$scope.csvDelimiter = $scope.dataset.csvDelimiter;
			$scope.csvQuoteChar = $scope.dataset.csvQuote;
			$scope.csvEncoding= $scope.dataset.csvEncoding;
			$scope.dateFormat = $scope.dataset.dateFormat;
		}
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
		/**
		 * Make a copy of a metadata of the dataset, so we can retrieve this data when moving back to the Step 1
		 * from the Step 2 and then again forward to the Step 2. Remember the metadata that user set last time.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.dataset.meta!="[]" && !Array.isArray($scope.dataset.meta)) {
			$scope.metaDataCopy = angular.copy($scope.dataset.meta);
		}
		/**
		 * If we open the dataset wizard for the current dataset for the first time, set the dataset file name keeper
		 * to the name of the just opened dataset.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.changedFileName == null) {
			$scope.changedFileName = $scope.dataset.fileName;
		}
		$scope.dataset.meta = JSON.stringify($scope.dataset.meta);

		$http({
			method: 'POST',
			url: sbiModule_config.contextName + '/restful-services/selfservicedataset/testDataSet',
			data: $scope.dataset,
			params: params,
			headers: {'Content-Type': 'application/x-www-form-urlencoded'},

			transformRequest: function(obj) {
				var str = [];
				for(var p in obj)
					str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
				return str.join("&");
			},

		}).then(function successCallback(response) {
				if (!response.data.errors) {
					console.info("[SUCCESS]: The Step 1 form is submitted successfully.");
					gridForPreview = response.data.gridForPreview;
					$scope.datasetWizardView = $scope.datasetWizardView + 1;
					/**
					 * If there was a non-empty metadata before submitting of the Step 1 (previously we were on the Step 2),
					 * retrieve the metadata.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					var isMetaDataCopyArray = Array.isArray($scope.metaDataCopy);
					// Reset the metadata
					$scope.dataset.meta = {};
					$scope.dataset.meta = angular.copy(response.data.meta);
					$scope.changedFileName = $scope.dataset.fileName;
					// Set the status to FALSE since the new metadata is collected and set. (danristo)
					$scope.validationPassed = false;
					angular.copy(response.data.datasetColumns,$scope.datasetColumns);

					$scope.prepareMetaForView();
					$scope.prepareDatasetForView();
					// Set a flag to indicate that we are not changing uploaded file any more (the 'Change file' button will appear when returning back to the Step 1.
					$scope.changingFile = false;
					// Reset the value for this flag, since Step 1 CSV paramters are not changed now (their previous change is applied). (danristo)
					$scope.csvConfChanged = false;
				} else {
					console.info("[ERROR]: ",translate.load(response.data.errors[0].message));
					// Reset the meta after first unsuccessful try to go to Step 2 (danristo)
					$scope.dataset.meta = [];
					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);
				}
			}, function errorCallback(response) {
				// called asynchronously if an error occurs
				// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				console.log(response);
				// Take the toaster duration set inside the main controller of the Workspace. (danristo)
				toastr.error(sbiModule_translate.load('sbi.ds.wizard.form.submit.failure.msg'), sbiModule_translate.load('sbi.generic.failure'), $scope.toasterConfig);
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
		var params = {};
		params.SBI_EXECUTION_ID = -1;
		// If User changes provided DataTypes on UI, go for Validation of Dataset, otherwise go for Preview of Dataset
		if (isDirty) {
			// Validation

			$http({
				method: 'POST',
				url: sbiModule_config.contextName + '/restful-services/selfservicedataset/getDataStore',
				data: $scope.dataset,
				params: params,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},

				transformRequest: function(obj) {
					var str = [];
					for(var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					return str.join("&");
				},
			}).then(function successCallback(response) {
					/**
					 * If the response does not contain the validation errors after submitting the Step 2 for particular
					 * file dataset, then we are dealing with the valid metadata configuration (formatting).
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					if (!response.data.validationErrors) {
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.success(sbiModule_translate.load("sbi.workspace.dataset.wizard.metadata.validation.success.msg"),
								sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);
						//If no errors go to preview - step 3
						previewDataSet();
					}
					/**
					 * If the response however contains the property that indicated that there are some validation problems
					 * (errors), handle the situation and inform user about it.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					else if(response.data.validationErrors) {
						console.warn("[VALIDATION ERRORS]: Validation failed!");
						$scope.validationStatus = false;
						/**
						 * Now, since we are having validation errors when submitting the Step 3, change the value of the
						 * scope variable that contains these data.
						 */
						$scope.step2ValidationErrors = response.data.validationErrors;
						/**
						 * Since the validation of the Step 2 went wrong (metadata are not valid), set flags that indicate that
						 * the validation did not passed, that the CSV configration parameters are not changed (anymore) and that
						 * there were validation errors.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.validationPassed = false;
						$scope.csvConfChanged = false;
						$scope.validationError = true;
						// "Refresh" the Step 2 table, in order to where the validation error appears.
						$scope.prepareMetaForView();
						$scope.prepareDatasetForView();
						// Inform the user about the validation error.
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.error(sbiModule_translate.load("sbi.workspace.dataset.wizard.metadata.validation.error.msg"), sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);
					} else {
						console.info("[ERROR]: ",translate.load(response.data.errors[0].message));
						$scope.validationStatus = false;
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.error(translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);
						$scope.step2ValidationErrors = null;
						/**
						 * Since the validation of the Step 2 went wrong (some failure happened), set flags that indicate that
						 * the validation did not passed, that the CSV configration parameters are not changed (anymore) and that
						 * there were validation errors.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.validationError = true;
						$scope.validationPassed = false;
						$scope.csvConfChanged = false;
					}
				}, function errorCallback(response) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					console.info("[FAILURE]: The form cannot be submitted because of some failure.");
					console.log(response);
					$scope.validationStatus = false;
					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(sbiModule_translate.load("sbi.ds.wizard.form.submit.failure.msg"), sbiModule_translate.load('sbi.generic.failure'), $scope.toasterConfig);
				});
	  } else {
		previewDataSet();
	  }
	}

	var previewDataSet = function() {
		console.info("[SUCCESS]: The Step 2 form is validated and submitted successfully!");
		$scope.resultMetaDataStep2 = [];
		$scope.resultRowsStep2 = [];
		angular.copy(gridForPreview.metaData.fields,$scope.resultMetaDataStep2);
		// Take all results (pure data) for rows of the Angular table
		angular.copy(gridForPreview.rows,$scope.resultRowsStep2);
		$scope.collectHeadersForStep3Preview();
		// Move to the next step (Step 3)
		$scope.datasetWizardView = $scope.datasetWizardView + 1;
		$scope.step2ValidationErrors = null;
		$scope.validationStatus = true;
		$scope.validationPassed = true;
		$scope.csvConfChanged = false;
		$scope.validationError = false;
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

		$http({
			method: 'POST',
			url: sbiModule_config.contextName + '/restful-services/selfservicedataset/save',
			data: $scope.dataset,
			params: params,
			headers: {'Content-Type': 'application/x-www-form-urlencoded'},

			transformRequest: function(obj) {
				var str = [];
				for(var p in obj)
					str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
				return str.join("&");
			},
		})
		.then(function successCallback(response) {
				if (!response.data.errors) {
					console.info("[SUCCESS]: The Step 4 form is submitted successfully. The file dataset is saved");
					if($scope.dataset.exportToHdfs) {
						sbiModule_restServices.promisePost('1.0/hdfs',response.data.id)
						.then(
							function(responseHDFS) {
								sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.ds.hdfs.request.work"), 'Success!');
							},
							function(responseHDFS) {
								sbiModule_messaging.showErrorMessage(responseHDFS.data.errors[0].message, 'Error');
							}
						);
					}
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
					$scope.reloadMyDataFn();
					/**
					 * If the ID value of the response data is negative (-1), that means that we edited (changed, modified) an existing file dataset.
					 * Otherwise, a new dataset is created and its ID is returned.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					if (response.data.id >= 0) {
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.success(sbiModule_translate.format(sbiModule_translate.load('sbi.workspace.dataset.wizard.submit.success.save.msg'),
							response.config.data.name), translate.load('sbi.generic.success'), $scope.toasterConfig);
					} else {
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.success(sbiModule_translate.format(sbiModule_translate.load('sbi.workspace.dataset.wizard.submit.success.update.msg'),
							response.config.data.name), translate.load('sbi.generic.success'), $scope.toasterConfig);
					}
					$scope.hideRightSidePanel();
				} else {
					console.info("[ERROR]: ",translate.load(response.data.errors[0].message));
					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);
				}
			}, function errorCallback(response) {
				// called asynchronously if an error occurs
				// or server returns response with an error status.
				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
				// Take the toaster duration set inside the main controller of the Workspace. (danristo)
				toastr.error(sbiModule_translate.load('sbi.workspace.dataset.wizard.submit.failure.msg'),
					sbiModule_translate.load('sbi.generic.failure'), $scope.toasterConfig);
			}
		);
	}

	$scope.toggleDWVNext = function() {
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
			case 3: $scope.datasetWizardView = $scope.datasetWizardView +1; break;
			case 4: $scope.submitStep4(); break;
		}
	}

	$scope.toggleDWVBack = function() {
		/**
		 * Remember the lastly chosen metadata type from the combo box on the Step 2 when leaving it.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.metadataId = $scope.metadataType.value;
		if ($scope.datasetWizardView==3) {
			$scope.validationPassed = true;
		}
		if($scope.datasetWizardView>1&&$scope.datasetWizardView<5){
			$scope.datasetWizardView = $scope.datasetWizardView -1;
		}
		/**
		 * Since the validation is performed, when coming to the Step 3 and then going back to the Step 2 (previously maybe it had wrong validation - some metadata
		 * columns did not pass the validation (red X icon in the Valid column), re-prepare the metadata, so we can refresh the Step 2 metadata columns and their
		 * 'Valid' column in particular. This way, those rows (metadata columns, such as 'country') that previously did not pass the validation, now will be updated
		 * with their "valid" state as passed (green check icon).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if ($scope.datasetWizardView==2) {
			$scope.prepareMetaForView();
			$scope.prepareDatasetForView();
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
//		/**
//		 * Reset the indicator, so the next time Dataset wizard is opened, the Step 2 "Valid" column items (icons) will be set to their
//		 * initial values - pending for validation.
//		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
//		 */
		$mdDialog.cancel();
	}

	loadDatasetValues = function(a,b) {
		sbiModule_restServices.promiseGet(a,b)
		.then(function(response) {
			if(b=="?DOMAIN_TYPE=DS_GEN_META_PROPERTY"){
				angular.copy(response.data,$scope.dsGenMetaProperty)
			} else if(b=="?DOMAIN_TYPE=DS_META_PROPERTY"){
				angular.copy(response.data,$scope.dsMetaProperty)
			} else if(b=="?DOMAIN_TYPE=DS_META_VALUE"){
				angular.copy(response.data,$scope.dsMetaValue)
			}
		},function(response){
			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data,"failed to load data for"+b, $scope.toasterConfig);
		});
	}

	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_GEN_META_PROPERTY");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_PROPERTY");
	loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_VALUE");

	$scope.uploadFile= function(){
    	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedatasetupload/fileupload",$scope.fileObj).success(

			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					console.info("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(sbiModule_translate.load(data.errors[0].message), sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);
				} else {
					console.info("[UPLOAD]: SUCCESS!");
					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.success(sbiModule_translate.format(sbiModule_translate.load('sbi.workspace.dataset.wizard.upload.success'),
						$scope.fileObj.fileName), translate.load('sbi.generic.success'), $scope.toasterConfig);
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
					$scope.dataset.dateFormat = $scope.dateFormatDefault;
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
				// Take the toaster duration set inside the main controller of the Workspace. (danristo)
				toastr.error($scope.fileObj.fileName+" could not be uploaded."+data.errors[0].message, sbiModule_translate.load('sbi.generic.failure'), $scope.toasterConfig);
			});
    }

	/**
	 * Local function that is used for filtering rows (metadata) for all columns available in the file dataset.
	 * It will pass only the 'type' and 'fieldType' rows, whilst others will be ignored (filtered).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var filterMetadataRows = function() {
		/**
		 * The final (filtered) array of all rows in the Step 2
		 */
		var finalFilteredRows = [];
		var pname = "";
		for(i=0; i< $scope.dataset.meta.columns.length;i++) {
			loc = $scope.dataset.meta.columns[i];
			pname = loc.pname;
			if (pname=="type" || pname=="fieldType") {
				finalFilteredRows.push(loc);
			}
		}
		return finalFilteredRows;
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
		/**
		 * Overwrite the existing metadata (the array of all rows for the metadata on the Step 2 of the Dataset wizard)
		 * with the filtered array - the one that passes only the 'type' and 'fieldType' rows for each column in the
		 * file dataset. Other will be filtered (they will not pass).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.dataset.meta.columns = filterMetadataRows($scope.dataset.meta.columns);
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
			if (pname=="type" || pname=="fieldType") {
				if (initialization) {
					if (pname=="type") {
						loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined,$scope.dataset.meta.columns[i+1].pvalue);
					} else {
						loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined);
					}
				} else {
					if (index-1==i) {
						loc.dsMetaValue = $scope.filterMetaValue($scope.dataset.meta.columns[index-1].pname,item,i,index);
					} else {
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
				loc.columnView='<label>{{row.column}}</label>';
				loc.pnameView='<label>{{row.pname}}</label>';
				loc.pvalueView='<md-select aria-label="pvalue-view" ng-model="row.pvalue" ng-change="scopeFunctions.setFormDirty()" class="noMargin" style=styleString><md-option ng-repeat="col in row.dsMetaValue" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.valueChanged(col,row.indexOfRow)">{{col.VALUE_NM}}</md-option></md-select>';
				var msg = '';
				/**
				 * Manage the Step 2 "Valid" column state according to the validation after submitting the Step 2.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if ($scope.step2ValidationErrors != null && ($scope.validationPassed==true || $scope.validationError==true) && $scope.csvConfChanged==false && $scope.changedFileName==$scope.dataset.fileName) {
					var columnName = loc.column;
					var invalidColumns = $filter('filter')($scope.step2ValidationErrors, {columnName: columnName}, true);
					var invalidType = false;
					if (invalidColumns.length > 0 && invalidColumns[0]['column_' + i] != undefined) {
						msg = invalidColumns[0]['column_' + i];
						invalidType = true;
						loc.columnErrorDetails = {
							errors: invalidColumns,
							skipRows: $scope.dataset.skipRows,
							index: i
						};
					} else {
						msg = "sbi.workspace.dataset.wizard.metadata.validation.success.title";
					}
					var invalidColumnValidContent = '<md-content class="metadataValidationColumn metadataInvalidColumn" ng-click="scopeFunctions.showErrorDetails(row.columnErrorDetails)"><div><div class="leftInavlidIcon"><md-icon md-font-icon="fa fa-times fa-1x" class="invalidTypeMetadata" title="' + eval("sbiModule_translate.load(msg)") + '" style="float:right;"></div></md-icon><div class="rightInvalidIcon"><md-icon md-font-icon="fa fa-info fa-1x" class="invalidTypeMetadata"></md-icon></div></div></md-content>';
					var validColumnValidContent = '<md-content class="metadataValidationColumn metadataValidColumn"><md-icon md-font-icon="fa fa-check fa-1x" class="validTypeMetadata" title="' + eval("sbiModule_translate.load(msg)") + '"></md-icon></md-content>';
					// Set the content of the "Valid" column for the current row to an appropriate state (passed/failed validation).
					loc.metaValid = (invalidType) ? invalidColumnValidContent : validColumnValidContent;
				} else {
					msg = "sbi.workspace.dataset.wizard.metadata.validation.pending.title";
					loc.metaValid = '<md-content class="metadataValidationColumn metadataDefaultColumn"><md-icon md-font-icon="fa fa-question fa-1x" class="defaultStateValidType" title="' + eval("sbiModule_translate.load(msg)") + '"></md-icon></md-content>';
				}
			}
		}
		if ($scope.validationError)
			angular.copy($scope.dataset.meta.columns, metaColumnsTemp);
	}

	$scope.prepareDatasetForView = function() {
		var datasets = $scope.dataset.meta.dataset;
		for (var i = 0; i < datasets.length; i++) {
			datasets[i].pnameView = '<md-select aria-label="dspname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsGenMetaProperty" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>';
			datasets[i].pvalueView = '<input ng-model="row.pvalue"></input>';
		}
	}

	$scope.filterInvalidColumns = function() {
		if ($scope.displayInvalidColumns.value) {
			$scope.dataset.meta.columns = $scope.dataset.meta.columns.filter(function(element){
				if ($scope.step2ValidationErrors.find(function(target){
					return element.column == target.columnName;
				})) return true;
				else return false;
			});
		} else {
			$scope.dataset.meta.columns = metaColumnsTemp;
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
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="double".toLowerCase() ||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="long".toLowerCase()||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="integer".toLowerCase() ||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="date".toLowerCase() ||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="timestamp".toLowerCase()){
					 filteredMetaValues.push($scope.dsMetaValue[j]);
				 }
			}
		}else if(pname.toLowerCase()==="fieldType".toLowerCase()){
			for(j=0;j<$scope.dsMetaValue.length;j++){
				if($scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="attribute".toLowerCase() || $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="spatial_attribute".toLowerCase() ||
					$scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="measure".toLowerCase()){
					filteredMetaValues.push($scope.dsMetaValue[j]);
   				}
   			}
		} else {
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
				typeValue = typeValue!=null ? typeValue.replace("java.util.","") : null;
				typeValue = typeValue!=null ? typeValue.replace("java.sql.","") : null;
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
				} else if ($scope.prevUploadedFile!=$scope.fileObj.fileName) {
					return translate.load('sbi.workspace.dataset.wizard.nextbutton.uploadnewfile.tooltip');
				}
			} else if (notValidStep1SkipRows) {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.invalid.skiprows.tooltip');
			} else if (notValidStep1LimitRows) {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.invalid.limitrows.tooltip');
			} else if (notValidStep1SheetNum) {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.invalid.sheetnumber.tooltip');
			} else {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.valid.tooltip');
			}
		} else if ($scope.datasetWizardView==4) {
			if ($scope.dataset.name=='') {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.save.missing.name.tooltip');
			} else if ($scope.dataset.persist && $scope.dataset.tableName=='') {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.save.missing.tablename.tooltip');
			} else {
				return translate.load('sbi.workspace.dataset.wizard.nextbutton.save.valid.tooltip');
			}
		} else {
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
		} else {
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
			$scope.fileObj = {
				file: { name:$scope.dataset.fileName },
				fileName: $scope.dataset.fileName
			};
		}
		console.info("CHANGE FILE [OUT]");
	}

	// ------------------------ //
	//   METADATA VALIDATION    //
	// ------------------------ //

	$scope.metadataTypes =	[
		 	{name:"Columns",value:1},
		 	{name:"Dataset",value:2}
	];

	$scope.markSelectedOptMetadataType = function(md) {
		for (var i = 0; i < $scope.metadataTypes.length; i++) {
			if ($scope.metadataTypes[i].value == md) {
				$scope.metadataType=$scope.metadataTypes[i];
			}
		}
		console.log($scope.metadataType);
	}

    $scope.tableColumns = [
	     {
	      name:"columnView",
	      label:"Column",
	      hideTooltip:true
	     },
	     {
	         name:"pnameView",
	         label:"Attribute",
	         hideTooltip:true
	     },
	     {
	         name:"pvalueView",
	         label:"Value",
	         hideTooltip:true
	     },
	     /**
	      * A new column on the Step 2 of the Dataset wizard. It contains a graphic description of a validation state
	      * for all metadata column separately.
	      * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	      */
	     {
	    	 name: "metaValid",
	    	 label: "Valid",
	    	 hideTooltip: true
	     }
     ];

    $scope.tableDataset = [
	 	{
	        name:"pnameView",
	        label:"Attribute",
	        hideTooltip:true
	    },
	    {
	        name:"pvalueView",
	        label:"Value",
	        hideTooltip:true
	    }
    ];

	$scope.table=[];

    $scope.metaScopeFunctions={
    	translate: sbiModule_translate,
    	datasetColumns:$scope.datasetColumns,
    	dsMetaProperty:$scope.dsMetaProperty,
    	dsMetaValue   :$scope.dsMetaValue,
    	filterMetaValues: function(value,row){
    		console.log(row);
    		row.dsMetaValue=[];
    		if(value.toLowerCase()==="type".toLowerCase()){
    			for(i=0;i<this.dsMetaValue.length;i++){
    			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="string".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="double".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="long".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="integer".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="date".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="timestamp".toLowerCase())
    				 row.dsMetaValue.push(this.dsMetaValue[i]);
    			}
    		}else if(value.toLowerCase()==="fieldType".toLowerCase()){
    			for(i=0;i<this.dsMetaValue.length;i++){
       			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="attribute".toLowerCase()||
       			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="measure".toLowerCase())
       				 row.dsMetaValue.push(this.dsMetaValue[i]);
       			}
    		}else{
    			angular.copy(this.dsMetaValue,row.dsMetaValue);
    		}
    	}
    };

	$scope.metaScopeFunctions.setFormDirty = function () {
		isDirty = true;
	}

    /**
     * A click-listener function that will take care when user clicks on the Value combo that is aligned with the particular
     * Field type row, in order to indicate that we are changing this value. This information will be useful when user picks
     * e.g. a MEASURE, in which case the previous row of type Type (it belonging column, e.g. city) will remove its "String"
     * item, since the MEASURE cannot be if type String.
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    $scope.metaScopeFunctions.valueChanged = function(item,index) {
    	if (item.VALUE_CD=="MEASURE" || item.VALUE_CD=="ATTRIBUTE") {
    		$scope.prepareMetaForView(item.VALUE_CD,index);
    	}
    }

    /**
     * Showing Excel cells that can't be casted to chosen Type (Value column: Integer, Double, Long...)
     */
    $scope.metaScopeFunctions.showErrorDetails = function(columnErrorDetails) {
    	$scope.showErrors = true;
    	$scope.hideErrors = false;
    	$scope.columnErrorDetails = columnErrorDetails;
		$scope.columnString = 'column_';
		$scope.index = $scope.columnErrorDetails.index;
    	$scope.limit = 10;
    	$scope.errorsCount = $scope.columnErrorDetails.errors.length;

    	$scope.showMoreErrorsButton = function() {
    		return $scope.errorsCount > $scope.limit;
    	}

    	$scope.remainingErros = function() {
    		return $scope.errorsCount - $scope.limit;
    	}

    	$scope.extandErrorList = function() {
    		if($scope.showMoreErrorsButton()) {
    			$scope.limit += $scope.limit;
    		} else {
    			$scope.limit = $scope.remainingErros();
    		}
    	}
	}

    $scope.closeErrorDetails = function() {
    	$scope.showErrors = false;
    	$scope.hideErrors = true;
    	$scope.columnErrorDetails = {};
    }

    $scope.deleteMetaColumn=function(item){
    	var index=$scope.dataset.meta.columns.indexOf(item);
    	if(index>-1){
    		$scope.dataset.meta.columns.splice(index,1);
    	}
    }

    $scope.metaScopeFunctions.addNewMetaRow = function() {
    	var newRow = {
    			column:"",
    			pname:"",
    			pvalue:"",
    			dsMetaValue: [],
    			columnView:'<md-select ng-model=row.column class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumns" value="{{col.columnName}}">{{col.columnName}}</md-option></md-select>',
    			pnameView:'<md-select ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsMetaProperty" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.filterMetaValues(col.VALUE_CD,row)">{{col.VALUE_NM}}</md-option></md-select>',
    			pvalueView:'<md-select ng-model=row.pvalue class="noMargin"><md-option ng-repeat="col in row.dsMetaValue" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>'
    	}
    	angular.copy($scope.dsMetaValue,newRow.dsMetaValue);
    	$scope.dataset.meta.columns.push(newRow);
    }

    $scope.metaScopeFunctions.clearAllMeta = function() {
    	$scope.dataset.meta.columns = [];
    }

	$scope.metaScopeFunctions.dsGenMetaProperty = $scope.dsGenMetaProperty;

    $scope.metaScopeFunctions.addNewDatasetRow = function() {
    	var newRow = {
    			pname:"",
    			pvalue:"",
    			pnameView:'<md-select ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsGenMetaProperty" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>',
    			pvalueView:'<div><md-input-container"><input type="text" ng-model="row.pvalue">	</md-input-container></div>'
    	}
    	$scope.dataset.meta.dataset.push(newRow);
    }

    $scope.metaScopeFunctions.clearAllDatasets = function() {
    	$scope.dataset.meta.dataset = [];
    }

    $scope.deleteMeta=[{
    	label:'delete',
    	icon: 'fa fa-trash',
    	action:function(item){
    		 $scope.deleteMetaColumn(item);
    	}
    }];
}