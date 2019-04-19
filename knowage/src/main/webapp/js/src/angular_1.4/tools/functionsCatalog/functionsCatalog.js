var app = angular.module('functionsCatalogControllerModule', [ 'ngMaterial',
		'angular_list', 'angular_table', 'sbiModule', 'angular_2_col',
		'file_upload_base64', 'angular-list-detail', 'ui.codemirror',
		'ngWYSIWYG', 'ngSanitize' ]);

app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.filter("htmlSafe", [ '$sce', function($sce) {
	return function(htmlCode) {
		return $sce.trustAsHtml(htmlCode);
	};
} ]);

app.controller('functionsCatalogController', [ "sbiModule_config",
		"sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog",
		"$mdToast", "$log", "sbiModule_download", "sbiModule_messaging",
		"$sce", "$compile", "$angularListDetail", functionsCatalogFunction ]);

function functionsCatalogFunction(sbiModule_config, sbiModule_translate,
		sbiModule_restServices, $scope, $mdDialog, $mdToast, $log,
		sbiModule_download, sbiModule_messaging, $sce, $compile,
		$angularListDetail) {

	$scope.showDetail = false;
	$scope.shownFunction = {
		"language" : "Python",
		"owner" : $scope.ownerUserName,
		"keywords" : [],
		"remote" : false,
		"url" : ""
	};
	$scope.datasetLabelList = [];
	$scope.datasetNamesList = [];
	$scope.datasets = [];
	$scope.tableSelectedFunction = {};
	$scope.tableSelectedFunction.language = "Python";
	$scope.languages = [ "Python", "R" ];
	$scope.outputTypes = [ "Dataset", "Text", "Image", "File" ];
	$scope.inputTypes = [ "Simple Input", "Dataset Input" ];
	$scope.functionTypesList = [];
	$scope.simpleInputs = []; // =Input variables
	$scope.inputDatasets = [];
	$scope.inputFiles = [];
	$scope.varIndex = 0;
	$scope.datasetsIndex = 0;
	$scope.functionsList = [];
	$scope.emptyStr = " ";
	$scope.searchKeywords = [];
	$scope.selectedType = "All";
	$scope.missingFields = [];
	$scope.languageHidden = true;

	$scope.editorConfig = {
		sanitize : false
	};

	$scope.newFunction = {
		"id" : "",
		"name" : "",
		"inputDatasets" : [],
		"inputVariables" : [],
		"inputUrls" : [],
		"inputFiles" : [],
		"outputItems" : [],
		"language" : "Python",
		"script" : "",
		"description" : "",
		"owner" : $scope.ownerUserName,
		"keywords" : [],
		"label" : "",
		"keywords" : [], // keywords and type added
		"type" : "",
		"remote" : false,
		"url" : ""
	};
	$scope.cleanNewFunction = function() {
		$scope.newFunction = {
			"id" : "",
			"name" : "",
			"inputDatasets" : [],
			"inputVariables" : [],
			"inputUrls" : [],
			"inputFiles" : [],
			"outputItems" : [],
			"language" : "Python",
			"script" : "",
			"description" : "",
			"owner" : $scope.ownerUserName,
			"keywords" : [],
			"label" : "",
			"keywords" : [], // keywords and type added
			"type" : "",
			"remote" : false,
			"url" : ""
		};
	}
	$scope.datasetLabelsList = [];
	$scope.saveOrUpdateFlag = "";
	$scope.userId = "";
	$scope.isAdmin = "";

	// For CodeMirror
	$scope.editorOptions = {
		lineWrapping : true,
		lineNumbers : true,
		mode : $scope.shownFunction.language.toLowerCase(),
		autoRefresh : true
	};

	$scope.myF = function(text) {
		$scope.userId = text;
	}

	// Utility functions

	$scope.getBase64 = function(file) {
		var reader = new FileReader();
		reader.readAsDataURL(file);
		reader.onload = function() {
			console.log("File in Base64: ", reader.result);
		};
		reader.onerror = function(error) {
			console.log('Error: ', error);
		};
	}

	$scope.formatFile = function(body) { // convert file into BASE64 and put
											// content into inputFile.base64
		console.log("Format file body ", body);

		var newBody = angular.copy(body);
		newBody.inputFiles = [];

		for (var i = 0; i < body.inputFiles.length; i++) {
			if (body.inputFiles[i].file != undefined) {
				var inputFile = body.inputFiles[i];
				body.inputFiles[i].base64 = inputFile.file.base64;
				inputFile.file.base64 = "";
				newBody.inputFiles.push(body.inputFiles[i]);
			}

		}
		body = newBody;
		console.log("Format file body ", body);

	}

	$scope.showTabDialog = function(result, isDemoExecution) {
		$mdDialog.show({
			controller : functionCatalogResultsController,
			templateUrl : sbiModule_config.dynamicResourcesBasePath
					+ '/angular_1.4/tools/functionsCatalog/templates/'
					+ 'functionCatalogResults.jsp',
			preserveScope : true,
			locals : {
				results : result.data,
				logger : $log,
				translate : sbiModule_translate,
				isDemo : isDemoExecution
			},
			clickOutsideToClose : true
		});
	};

	$scope.showNewInputDialog = function(data, datasetList, isDemoFunction) {
		var executionResult = $mdDialog.show({
			controller : executeWithNewDataController,
			templateUrl : sbiModule_config.dynamicResourcesBasePath
					+ '/angular_1.4/tools/functionsCatalog/templates/'
					+ 'functionCatalogNewInputs.jsp',
			preserveScope : true,
			locals : {
				demoData : data,
				logger : $log,
				datasets : datasetList,
				userId : $scope.userId,
				translate : sbiModule_translate
			},
			clickOutsideToClose : true
		});
		executionResult.then(function(response) { // positive response, given
			// from $mdDialog.hide(..)
			$scope.showTabDialog(response, isDemoFunction);
		}, function(data) { // negative response, given from $mdDialog.close(..)

		});
		$scope.obtainDatasetLabelsRESTcall();

	};

	function isEmpty(obj) {

		var hasOwnProperty = Object.prototype.hasOwnProperty;

		// null and undefined are "empty"
		if (obj == null)
			return true;

		// Assume if it has a length property with a non-zero value
		// that that property is correct.
		if (obj.length > 0)
			return false;
		if (obj.length === 0)
			return true;

		// Otherwise, does it have any properties of its own?
		// Note that this doesn't handle
		// toString and valueOf enumeration bugs in IE < 9
		for ( var key in obj) {
			if (hasOwnProperty.call(obj, key))
				return false;
		}

		return true;
	}

	$scope.radioButtonRemoteLocalPush = function(localOrRemoteStr) {
		if (localOrRemoteStr == "local") {
			$scope.shownFunction.url = "";
			$scope.languageHidden = true;

		} else if (localOrRemoteStr == "remote") {
			$scope.shownFunction.script = "";
			$scope.languageHidden = false;
		}
	}

	$scope.obtainCatalogFunctionsRESTcall = function() {
		sbiModule_restServices.get("1.0/functions-catalog", "").then(
			function(result) {
				$scope.functionsList = result.data.functions;
				$scope.functionsList_bck = angular.copy(result.data.functions);
				$scope.keywordsList_bck = angular.copy(result.data.keywords);
				$scope.searchKeywords = result.data.keywords;
			}
		)
	}

	$scope.obtainFunctionTypesRESTcall = function() {
		sbiModule_restServices.get("2.0/domains","listByCode/" + "FUNCTION_TYPE").then(
			function(result) {
				for (var i = 0; i < result.data.length; i++) {
					if (result.data[i].valueCd != "All") {
						$scope.functionTypesList.push(result.data[i]);
					}
				}
			}
		);
	}

	$scope.obtainDatasetLabelsRESTcall = function() {
		sbiModule_restServices.get("2.0/datasets", "", "asPagedList=true").then(
			function(datasets) {
				$scope.datasets = datasets.data;
				$scope.datasetsList = [];
				$scope.datasetLabelsList = [];
				$scope.datasetNamesList = [];
				for (d in datasets.data.item) {
					$scope.datasetLabelsList.push(datasets.data.item[d].label);
					$scope.datasetNamesList.push(datasets.data.item[d].name);
				}
			}
		);
	}

	$scope.addFunction = function() {
		$scope.shownFunction = $scope.newFunction;
		$scope.newFunction.owner = $scope.ownerUserName;
		$scope.showDetail = true;
		$scope.saveOrUpdateFlag = "save"
		$angularListDetail.goToDetail();
	}

	$scope.saveFunction = function() {
		var body = {};

		if (!$scope.checkCorrectArguments()) {
			sbiModule_messaging.showAlertMessage(sbiModule_translate.load("sbi.functionscatalog.save.missingfield"), $scope.missingFields.join('<br>'));
		} else {
			if ($scope.saveOrUpdateFlag == "save") {
				body = $scope.shownFunction;

				sbiModule_restServices.post("1.0/functions-catalog", "insert", body).then(
					function(result) {
						if (result.data.errors != undefined) {
							var errorToDisplay = "";
							for (var i = 0; i < result.data.errors.length; i++) {
								errorToDisplay = errorToDisplay
									+ " , "
									+ result.data.errors[i].message;
							}
							if (errorToDisplay.length >= 3) {
								errorToDisplay = errorToDisplay.substring(3);
							}
							$mdToast.show($mdToast.simple()
								.textContent(errorToDisplay)
								.position("top left")
								.hideDelay(5000));
						} else {
							$scope.obtainCatalogFunctionsRESTcall();
							$scope.cleanNewFunction = function() {
								$scope.newFunction = {
									"id" : "",
									"name" : "",
									"inputDatasets" : [],
									"inputVariables" : [],
									"inputUrls" : [],
									"inputFiles" : [],
									"outputItems" : [],
									"language" : "Python",
									"script" : "",
									"description" : "",
									"owner" : $scope.ownerUserName,
									"keywords" : [],
									"label" : "",
									"remote" : false,
									"url" : ""
								};
							}
							$scope.shownFunction = $scope.newFunction;
							$mdToast.show($mdToast.simple()
								.textContent(sbiModule_translate.load("sbi.functionscatalog.save.success"))
								.position("top left")
								.hideDelay(5000));
						}
					},function(error) {
						$mdToast.show($mdToast.simple()
							.textContent(sbiModule_translate.load("sbi.functionscatalog.save.error"))
							.position("top left")
							.hideDelay(5000));
					}
				)
			} else if ($scope.saveOrUpdateFlag == "update") {
				body = $scope.shownFunction;
				functionId = $scope.shownFunction.id;

				sbiModule_restServices.put("1.0/functions-catalog", "update/" + functionId,body).then(
					function(result) {
						$scope.obtainCatalogFunctionsRESTcall();
						if (result.data.errors != undefined) {
							var errorToDisplay = "";
							for (var i = 0; i < result.data.errors.length; i++) {
								errorToDisplay = errorToDisplay
									+ " , "
									+ result.data.errors[i].message;
							}
							if (errorToDisplay.length >= 3) {
								errorToDisplay = errorToDisplay.substring(3);
							}
							$mdDialog.show($mdDialog
								.alert()
								.parent(angular.element(document.querySelector('#popupContainer')))
								.clickOutsideToClose(true)
								.title(errorToDisplay)
								.ok('OK'));
						} else {
							$mdToast.show($mdToast.simple()
								.textContent(sbiModule_translate.load("sbi.functionscatalog.save.success"))
								.position("top left")
								.hideDelay(5000));
						}
					},function(error) {
						$mdToast.show($mdToast.simple()
							.textContent(sbiModule_translate.load("sbi.functionscatalog.save.error"))
							.position("top left")
							.hideDelay(5000));
					}
				);
			}
		}

	}

	$scope.checkCorrectArguments = function() {
		var correctArguments = true;
		$scope.missingFields = [];

		for (var i = 0; i < $scope.shownFunction.inputDatasets.length; i++) {
			if ($scope.shownFunction.inputDatasets[i].label == undefined) {
				correctArguments = false;
				var index = i + 1;
				$scope.missingFields
						.push("Input dataset " + index + " missing");
			}
		}
		for (var i = 0; i < $scope.shownFunction.inputVariables.length; i++) {
			if ($scope.shownFunction.inputVariables[i].name == undefined
					|| $scope.shownFunction.inputVariables[i].value == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.inputVariables[i].name == undefined) {
					$scope.missingFields.push("Input variable  " + index + " name missing");
				}
				if ($scope.shownFunction.inputVariables[i].value == undefined) {
					$scope.missingFields.push("Input variable  " + index + " value missing");

				}
			}
		}

		for (var i = 0; i < $scope.shownFunction.inputFiles.length; i++) {
			if ($scope.shownFunction.inputFiles[i].filename == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.inputFiles[i].fileName == undefined) {
					$scope.missingFields.push("Input file  " + index + "file name missing");
				}

			}
		}

		for (var i = 0; i < $scope.shownFunction.outputItems.length; i++) {
			if ($scope.shownFunction.outputItems[i].label == undefined
					|| $scope.shownFunction.outputItems[i].type == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.outputItems[i].type == undefined) {
					$scope.missingFields.push("Output " + index + " type missing");
				}
				if ($scope.shownFunction.outputItems[i].label == undefined) {
					$scope.missingFields.push("Output " + index + " label missing");

				}
			}
		}
		if ($scope.shownFunction.description == ""
				|| $scope.shownFunction.description == "") {
			correctArguments = false;
			$scope.missingFields.push("Function description missing");

		}
		if ($scope.shownFunction.remote == true && ($scope.shownFunction.url == undefined || $scope.shownFunction.url == "")) {
			correctArguments = false;
			$scope.missingFields.push("Function url missing");
		} else if ($scope.shownFunction.remote == false && ($scope.shownFunction.script == undefined || $scope.shownFunction.script == "")) {
			correctArguments = false;
			$scope.missingFields.push("Function script missing");
		}
		return correctArguments;
	}

	$scope.acSpeedMenu = [ {
		label : sbiModule_translate.load("Execute Demo"),
		icon : 'fa fa-play-circle-o',
		action : function(item, event) {
			$scope.applyDemoItem(item, event);
		}
	}, {
		label : sbiModule_translate.load("Execute with new data"),
		icon : 'fa fa-play-circle',
		action : function(item, event) {
			$scope.applyItem(item, event);
		}
	}

	];

	var deleteIcon = {
		label : sbiModule_translate.load("Delete"),
		icon : 'fa fa-trash',
		action : function(item, event) {
			var confirm = $mdDialog.confirm().clickOutsideToClose(true).title()
					.textContent(sbiModule_translate.load("sbi.functionscatalog.suretodelete"))
					.ariaLabel('Alert Dialog Demo').ok('OK').cancel('Cancel');

			$mdDialog.show(confirm).then(
					function() {
						$scope.deleteFunction(item, event);
					},
					function() {
						console.log(sbiModule_translate.load("sbi.functionscatalog.deletecancelled"));
					});
		},
		visible : function(row, column) {
			if (row.owner == $scope.ownerUserName || isAdminGlobal) {
				return true;
			} else {
				return false;
			}
			return row.owner == $scope.ownerUserName ? true : false
		}
	};

	if (isAdminGlobal) {
		$scope.acSpeedMenu.push(deleteIcon);
	} else if (isDevGlobal) {
		$scope.acSpeedMenu.push(deleteIcon);
	}

	$scope.deleteFunction = function(item, event) {

		$scope.shownFunction = angular.copy(item);
		var functionId = $scope.shownFunction.id;

		sbiModule_restServices.get("1.0/functions-catalog","delete/" + functionId).then(function(result) {
			$scope.obtainCatalogFunctionsRESTcall();
			$scope.cleanNewFunction();
			$scope.shownFunction = $scope.newFunction;
			$scope.saveOrUpdateFlag = "save";
		});

	};

	$scope.applyDemoItem = function(item, event) {

		var functionId = item.id;

		$log.info("userId ", $scope.userId);
		sbiModule_restServices.get("1.0/functions-catalog/execute/sample",functionId).then(
			function(results) {
				for (var i = 0; i < results.data.length; i++) {
					if (results.data[i].resultType == "file"
							|| results.data[i].resultType == "File") {
						results.data[i].result = JSON.parse(results.data[i].result);
					}
				}
				var isDemo = true;
				$scope.showTabDialog(results, isDemo);

			});
	};

	$scope.applyItem = function(item, event) {
		demoData = item;
		var isDemo = false;
		$scope.showNewInputDialog(demoData, $scope.datasets, isDemo);
	};

	$scope.leftTableClick = function(item) {
		$scope.showDetail = true;
		$scope.shownFunction = angular.copy(item);
		$scope.cleanNewFunction();
		$log.info("ShownFunction: ", $scope.shownFunction);
		$scope.saveOrUpdateFlag = "update";
		$angularListDetail.goToDetail();

	}

	$scope.addInputDataset = function() {
		$scope.cleanNewFunction();
		var inputDataset = {};

		$scope.shownFunction.inputDatasets.push(inputDataset);
		$log
				.info("Added an input Dataset ",
						$scope.shownFunction.inputDatasets);
		return inputDataset;
	}

	$scope.addInputVariable = function() {
		$scope.cleanNewFunction();
		var inputVariable = {};

		$scope.shownFunction.inputVariables.push(inputVariable);
		$log.info("Added an input Variable ",
				$scope.shownFunction.inputVariables);
		return inputVariable;
	}

	$scope.addInputFile = function() {
		$scope.cleanNewFunction();
		var inputFile = {};

		$scope.shownFunction.inputFiles.push(inputFile);
		$log.info("Added an input File ", $scope.shownFunction.inputFiles);
		return inputFile;
	}

	$scope.removeInputDataset = function(inputDataset) {
		var index = $scope.shownFunction.inputDatasets.indexOf(inputDataset);
		$scope.shownFunction.inputDatasets.splice(index, 1);
		$log.info("Removed an input Dataset ",
				$scope.shownFunction.inputDatasets);
	}

	$scope.removeInputVariable = function(inputVariable) {
		var index = $scope.shownFunction.inputVariables.indexOf(inputVariable);
		$scope.shownFunction.inputVariables.splice(index, 1);
		$log.info("Removed an input Variable ",
				$scope.shownFunction.inputVariables);
	}

	$scope.removeInputFile = function(inputFile) {
		var index = $scope.shownFunction.inputFiles.indexOf(inputFile);
		$scope.shownFunction.inputFiles.splice(index, 1);
		$log.info("Removed an input File ", $scope.shownFunction.inputFile);
	}

	$scope.addOutputItem = function() {
		var output = {};
		$scope.shownFunction.outputItems.push(output);
		$log.info("Added an outputItem ", $scope.shownFunction.outputItems);
		return output;
	}

	$scope.removeOutputItem = function(output) {
		var index = $scope.shownFunction.outputItems.indexOf(output);
		$scope.shownFunction.outputItems.splice(index, 1);
		$log.info("Removed an output Item ", $scope.shownFunction.output);

	}

	$scope.datasetPreview = function(datasetLabel) {
		sbiModule_restServices.get("1.0/datasets", datasetLabel + "/content").then(function(dataset) {
			$scope.showDatasetPreviewDialog(dataset.data, datasetLabel);
		});
	}

	$scope.showDatasetPreviewDialog = function(datasetToSee, labelDS) {
		var executionResult = $mdDialog.show({
			controller : datasetPreviewController,
			templateUrl : sbiModule_config.dynamicResourcesBasePath
					+ '/angular_1.4/tools/functionsCatalog/templates/'
					+ 'datasetPreview.jsp',
			preserveScope : true,
			locals : {
				logger : $log,
				dataset : datasetToSee,
				datasetLabel : labelDS,
				translate : sbiModule_translate
			},
			clickOutsideToClose : true
		});

	};

	$scope.filterByType = function(typeObject) {
		var type = typeObject.valueCd;
		$scope.selectedChip = "";
		console.log("typeObject: ", typeObject);
		$scope.functionsToDisplay = [];
		$angularListDetail.goToList();
		$scope.selectedType = typeObject.valueCd;
		if (type != "All") {
			sbiModule_restServices.get("1.0/functions-catalog", type).then(
					function(result) {
						$scope.functionsList = result.data.functions;
						$scope.searchKeywords = result.data.keywords;
						return $scope.functionsToDisplay;
					});
		} else {
			$scope.functionsList = $scope.functionsList_bck;
			$scope.searchKeywords = $scope.keywordsList_bck;
			return $scope.functionsList_bck;
		}
	}

	// A REST service to obtain the functions already filtered by type is present in functionsCatalogResources.java
	$scope.chipFilter = function(keyword) {
		$scope.functionsToDisplay = [];
		for (var i = 0; i < $scope.functionsList_bck.length; i++) {
			if ($scope.selectedType == 'All') {
				if ($scope.functionsList_bck[i].keywords.indexOf(keyword) >= 0) { // if index >= 0, keyword is present
					$scope.functionsToDisplay.push($scope.functionsList_bck[i]);
				}
			} else {
				if ($scope.selectedType == $scope.functionsList_bck[i].type) {
					if ($scope.functionsList_bck[i].keywords.indexOf(keyword) >= 0) { // if index >= 0, keyword is present
						$scope.functionsToDisplay.push($scope.functionsList_bck[i]);
					}
				}
			}
		}
		$scope.selectedChip = keyword;
		$scope.functionsList = $scope.functionsToDisplay;
		return $scope.functionsToDisplay;
	}

	// --------------------------------------------Application
	// Logic---------------------------------------

	$scope.obtainDatasetLabelsRESTcall();
	$scope.obtainCatalogFunctionsRESTcall();
	$scope.obtainFunctionTypesRESTcall();

	// ----------------------------------------------Controllers-----------------------------------------------

	function functionCatalogResultsController($scope, $mdDialog, logger,
			results, translate, isDemo) {
		function isObject(obj) {
			return obj === Object(obj);
		}

		logger.info("received results: ", results);
		$scope.download = function(filename, base64) {
			var element = document.createElement('a');
			element.setAttribute('href',
					'data:application/octet-stream;base64,' + base64);

			element.setAttribute('download', filename);

			element.style.display = 'none';
			document.body.appendChild(element);

			element.click();

			document.body.removeChild(element);
		}

		$scope.numTab = results.length;
		$scope.results = results;
		$scope.translate = translate;
		$scope.isDemo = isDemo;
		$scope.truncate = false;
		$scope.error = "";
		$scope.dataset = {
			rows : []
		};
		for ( var res in $scope.results) {
			if ($scope.results.hasOwnProperty(res)) {
				if ($scope.results[res].resultType == "image"
						|| $scope.results[res].resultType == "Image") {
					$scope.results[res].imageString = "data:image/png;base64,"
							+ $scope.results[res].result;
				}
				if ($scope.results[res].resultType == "dataset"
						|| $scope.results[res].resultType == "Dataset"
						|| $scope.results[res].resultType == "spagobi_ds") {
					var datasetLabel = $scope.results[res].result;
					sbiModule_restServices.post("1.0/datasets", datasetLabel + "/content").then(function(result) {
						for (var i = 0; i < result.data.rows.length; i++) {
							delete result.data.rows[i].id;
						}

						$scope.dataset = result.data;
						$scope.datasetLabel = datasetLabel;
						if ($scope.dataset.rows.length > 10) {
							$scope.truncate = true;
						}
						$scope.dataset.rows = $scope.dataset.rows.slice(0, 9);

						$scope.headers = [];
						// in case first field is "recNO", skip
						// it
						if (!isObject($scope.dataset.metaData.fields[0])) {
							i = 1;
						} else {
							i = 0;
						}

						for (i; i < $scope.dataset.metaData.fields.length; i++) {
							if ($scope.dataset.metaData.fields[i].header != undefined && $scope.dataset.metaData.fields[i].header != "") {
								var colToInsert = {};
								colToInsert["label"] = $scope.dataset.metaData.fields[i].header;
								colToInsert["name"] = $scope.dataset.metaData.fields[i].name;
								$scope.headers.push(colToInsert);
							}

						}

					});
				}
				if (res == "errors") {
					$scope.error = $scope.results.errors[0].localizedMessage
							+ "\n" + $scope.results.errors[0].message;
				}
			}
		}

	}
	;

	function executeWithNewDataController($scope, $mdDialog, logger, demoData,
			datasets, userId, translate) {
		logger.info("received demo function data: ", demoData);
		$scope.demoData = demoData;
		$scope.datasets = datasets;
		$scope.functionId = demoData.id;
		$scope.userId = userId;
		logger.info("HAVING DATASETS: ", $scope.datasets);
		$scope.replacingDatasetList = {};
		$scope.replacingFileList = {};
		$scope.replacingVariableValues = {};
		$scope.replacingDatasetOutLabels = {};
		$scope.replacingTextOutLabels = {};
		$scope.replacingImageOutLabels = {};
		$scope.translate = translate;

		$scope.getDatasetNameByLabel = function(label, datasetList) {
			for ( var d in datasetList.item) {
				if (datasetList.item.hasOwnProperty(d)) {
					var datasetObj = datasetList.item[d];
					if (datasetObj.label == label)
						return datasetObj.name;
				}
			}
		}

		$scope.cancel = function() {
			$mdDialog.cancel();
		};

		$scope.isExecuteDisabled = function() {
			disabled = false;
			function isEmpty(obj) {

				// Speed up calls to hasOwnProperty
				var hasOwnProperty = Object.prototype.hasOwnProperty;

				// null and undefined are "empty"
				if (obj == null)
					return true;

				// Assume if it has a length property with a non-zero value
				// that that property is correct.
				if (obj.length > 0)
					return false;
				if (obj.length === 0)
					return true;

				// Otherwise, does it have any properties of its own?
				// Note that this doesn't handle
				// toString and valueOf enumeration bugs in IE < 9
				for ( var key in obj) {
					if (hasOwnProperty.call(obj, key))
						return false;
				}

				return true;
			}

			// The output are generic outputItems with a Type field that
			// distinguish them, in contrast with the input, that are divided in
			// inputDatasets and inputVariables
			// in future make 3 lists of outputs (outputImages, outputDatasets
			// and outputText) instead of outputItems (also in the jsp view!!)
			$scope.numDSout = 0;
			function classifyOutput() {
				for (var i = 0; i < $scope.demoData.outputItems.length; i++) {
					if (demoData.outputItems[i].type == "Dataset"
							|| demoData.outputItems[i].type == "dataset"
							|| demoData.outputItems[i].type == "spabobi_ds") {
						$scope.numDSout = $scope.numDSout + 1;
					}

				}
				return $scope.numDSout;
			}

			$scope.numDSout = classifyOutput();

			if ((isEmpty($scope.replacingDatasetList) && $scope.demoData.inputDatasets.length != 0)
					|| (isEmpty($scope.replacingVariableValues) && $scope.demoData.inputVariables.length != 0)
					|| (isEmpty($scope.replacingDatasetOutLabels) && $scope.numDSout != 0)) {
				disabled = true;
			} else {
				for ( var property in $scope.replacingDatasetList) {
					if ($scope.replacingDatasetList.hasOwnProperty(property)) {
						if (isEmpty($scope.replacingDatasetList[property])) {
							disabled = true;
						}
					}
				}
				for ( var property in $scope.replacingVariableValues) {
					if ($scope.replacingVariableValues.hasOwnProperty(property)) {
						if (isEmpty($scope.replacingVariableValues[property])) {
							disabled = true;
						}
					}
				}
				for ( var property in $scope.replacingDatasetOutLabels) {
					if ($scope.replacingDatasetOutLabels
							.hasOwnProperty(property)) {
						if (isEmpty($scope.replacingDatasetOutLabels[property])) {
							disabled = true;
						}
					}
				}

			}
			return disabled;
		}

		$scope.executeFunction = function() {
			var body = [];

			var variablesIn = {}, datasetsIn = {}, filesIn = {};
			variablesIn.type = "variablesIn";
			variablesIn.items = $scope.replacingVariableValues;
			body.push(variablesIn);

			datasetsIn.type = "datasetsIn";
			datasetsIn.items = $scope.replacingDatasetList;
			body.push(datasetsIn);

			filesIn.type = "filesIn"
			filesIn.items = $scope.replacingFileList;
			body.push(filesIn);

			logger.info("body: ", body);

			sbiModule_restServices.post("1.0/functions-catalog/execute/new",$scope.functionId, body).then(
				function(executionResult) {
					for (var i = 0; i < executionResult.data.length; i++) {
						if (executionResult.data[i].resultType == "file" || executionResult.data[i].resultType == "File") {
							executionResult.data[i].result = JSON.parse(executionResult.data[i].result);
						}
					}
					$mdDialog.hide(executionResult);
				}
			);
		}
	};

	$scope.cancelFunction = function() {
		$angularListDetail.goToList();
	}

	function datasetPreviewController($scope, $mdDialog, logger, dataset,
			datasetLabel, translate) {
		function isObject(obj) {
			return obj === Object(obj);
		}

		logger.info("Preview Controller, received dataset: ", dataset);
		for (var i = 0; i < dataset.rows.length; i++) {
			delete dataset.rows[i].id;
		}
		$scope.dataset = dataset;
		$scope.truncate = false;
		$scope.datasetLabel = datasetLabel;
		$scope.translate = translate;
		if ($scope.dataset.rows.length > 10) {
			$scope.truncate = true;
		}
		$scope.dataset.rows = $scope.dataset.rows.slice(0, 9);

		$scope.headers = [];
		// in case first field is "recNO", skip it
		if (!isObject($scope.dataset.metaData.fields[0])) {
			i = 1;
		} else {
			i = 0;
		}

		for (i; i < $scope.dataset.metaData.fields.length; i++) {
			if ($scope.dataset.metaData.fields[i].header != undefined
					&& $scope.dataset.metaData.fields[i].header != "") {
				var colToInsert = {};
				colToInsert["label"] = $scope.dataset.metaData.fields[i].header;
				colToInsert["name"] = $scope.dataset.metaData.fields[i].name;
				$scope.headers.push(colToInsert);
			}

		}
	}

};

