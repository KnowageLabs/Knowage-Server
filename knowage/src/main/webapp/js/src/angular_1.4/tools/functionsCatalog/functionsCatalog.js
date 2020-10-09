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
	$scope.tableSelectedFunction = {};
	$scope.tableSelectedFunction.language = "Python";
	$scope.languages = [ "Python", "R" ];
	$scope.inputColumnTypes = ['STRING', 'DATE', 'NUMBER', 'LIST'];
	$scope.inputVariableTypes = ['STRING', 'DATE', 'NUMBER'];
	$scope.outputColumnTypes = ['STRING', 'DATE', 'NUMBER'];
	$scope.functionTypesList = [];
	$scope.inputColumns = [];
	$scope.varIndex = 0;
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
		"inputColumns" : [],
		"inputVariables" : [],
		"outputColumns" : [],
		"language" : "Python",
		"onlineScript" : "",
		"offlineScriptTrainModel" : "",
		"offlineScriptUseModel" : "",
		"description" : "",
		"benchmarks" : "",
		"owner" : $scope.ownerUserName,
		"keywords" : [],
		"label" : "",
		"type" : "",
		"functionFamily": "online"
	};
	$scope.cleanNewFunction = function() {
		$scope.newFunction = {
			"id" : "",
			"name" : "",
			"inputColumns" : [],
			"inputVariables" : [],
			"outputColumns" : [],
			"language" : "Python",
			"onlineScript" : "",
			"offlineScriptTrainModel" : "",
			"offlineScriptUseModel" : "",
			"description" : "",
			"benchmarks" : "",
			"owner" : $scope.ownerUserName,
			"keywords" : [],
			"label" : "",
			"type" : "",
			"functionFamily": "online"
		};
	}
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

	$scope.radioButtonOnlineOfflinePush = function(onlineOrOfflineStr) {
		$scope.shownFunction.functionFamily = onlineOrOfflineStr;
	}

	$scope.obtainCatalogFunctionsRESTcall = function() {
		sbiModule_restServices.get("2.0/functions-catalog", "").then(
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

				sbiModule_restServices.post("2.0/functions-catalog", "insert", body).then(
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
									"inputColumns" : [],
									"inputVariables" : [],
									"outputColumns" : [],
									"language" : "Python",
									"onlineScript" : "",
									"offlineScriptTrainModel" : "",
									"offlineScriptUseModel" : "",
									"description" : "",
									"benchmarks" : "",
									"owner" : $scope.ownerUserName,
									"keywords" : [],
									"label" : "",
									"type" : "",
									"functionFamily": "online"
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

				sbiModule_restServices.put("2.0/functions-catalog", "update/" + functionId,body).then(
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

		for (var i = 0; i < $scope.shownFunction.inputColumns.length; i++) {
			if ($scope.shownFunction.inputColumns[i].name == undefined
					|| $scope.shownFunction.inputColumns[i].type == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.inputColumns[i].name == undefined) {
					$scope.missingFields.push("Input variable  " + index + " name missing");
				}
				if ($scope.shownFunction.inputColumns[i].type == undefined) {
					$scope.missingFields.push("Input variable  " + index + " type missing");

				}
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

		for (var i = 0; i < $scope.shownFunction.outputColumns.length; i++) {
			if ($scope.shownFunction.outputColumns[i].name == undefined
					|| $scope.shownFunction.outputColumns[i].type == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.outputColumns[i].type == undefined) {
					$scope.missingFields.push("Output column " + index + " type missing");
				}
				if ($scope.shownFunction.outputColumns[i].name == undefined) {
					$scope.missingFields.push("Output column " + index + " name missing");

				}
			}
		}
		if ($scope.shownFunction.description == ""
				|| $scope.shownFunction.description == "") {
			correctArguments = false;
			$scope.missingFields.push("Function description missing");

		}
		if ($scope.shownFunction.functionFamily == "online" && (!$scope.shownFunction.onlineScript || $scope.shownFunction.onlineScript == "")) {
			correctArguments = false;
			$scope.missingFields.push("Online script missing");
		} else if ($scope.shownFunction.functionFamily == "offline") {
			if (!$scope.shownFunction.offlineScriptTrainModel || $scope.shownFunction.offlineScriptTrainModel == "") {
				correctArguments = false;
				$scope.missingFields.push("Offline train script missing");
			}
			if (!$scope.shownFunction.offlineScriptUseModel || $scope.shownFunction.offlineScriptUseModel == "") {
				correctArguments = false;
				$scope.missingFields.push("Offline use script missing");
			}
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

		sbiModule_restServices.get("2.0/functions-catalog","delete/" + functionId).then(function(result) {
			$scope.obtainCatalogFunctionsRESTcall();
			$scope.cleanNewFunction();
			$scope.shownFunction = $scope.newFunction;
			$scope.saveOrUpdateFlag = "save";
		});

	};

	$scope.applyDemoItem = function(item, event) {

		var functionId = item.id;

		$log.info("userId ", $scope.userId);
		sbiModule_restServices.get("2.0/functions-catalog/execute/sample",functionId).then(
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

	$scope.addInputColumn = function() {
		$scope.cleanNewFunction();
		var inputColumn = {};

		$scope.shownFunction.inputColumns.push(inputColumn);
		$log
				.info("Added an input Column ",
						$scope.shownFunction.inputColumns);
		return inputColumn;
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

	$scope.removeInputColumn = function(inputColumn) {
		var index = $scope.shownFunction.inputColumns.indexOf(inputColumn);
		$scope.shownFunction.inputColumns.splice(index, 1);
		$log.info("Removed an input Column ",
				$scope.shownFunction.inputColumns);
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

	$scope.addOutputColumn = function() {
		var outputColumn = {};
		$scope.shownFunction.outputColumns.push(outputColumn);
		$log.info("Added an output Column ", $scope.shownFunction.outputColumns);
		return outputColumn;
	}

	$scope.removeOutputColumn = function(outputColumn) {
		var index = $scope.shownFunction.outputColumns.indexOf(outputColumn);
		$scope.shownFunction.outputColumns.splice(index, 1);
		$log.info("Removed an output Column ", $scope.shownFunction.outputColumns);

	}

	$scope.filterByType = function(typeObject) {
		var type = typeObject.valueCd;
		$scope.selectedChip = "";
		console.log("typeObject: ", typeObject);
		$scope.functionsToDisplay = [];
		$angularListDetail.goToList();
		$scope.selectedType = typeObject.valueCd;
		if (type != "All") {
			sbiModule_restServices.get("2.0/functions-catalog", type).then(
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
		for ( var res in $scope.results) {
			if ($scope.results.hasOwnProperty(res)) {
				if ($scope.results[res].resultType == "image"
						|| $scope.results[res].resultType == "Image") {
					$scope.results[res].imageString = "data:image/png;base64,"
							+ $scope.results[res].result;
				}
				if (res == "errors") {
					$scope.error = $scope.results.errors[0].localizedMessage
							+ "\n" + $scope.results.errors[0].message;
				}
			}
		}

	};

	$scope.cancelFunction = function() {
		$angularListDetail.goToList();
	}

};

