agGrid.initialiseAgGridWithAngular1(angular);
var app = angular.module('functionsCatalogControllerModule', [ 'ngMaterial',
		'angular_list', 'angular_table', 'sbiModule', 'angular_2_col',
		'file_upload_base64', 'angular-list-detail', 'ui.codemirror',
		'ngWYSIWYG', 'ngSanitize', 'agGrid' ]);

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
	$scope.languages = ["Python"];
	$scope.inputColumnTypes = ['STRING', 'DATE', 'NUMBER'];
	$scope.inputVariableTypes = ['STRING', 'DATE', 'NUMBER'];
	$scope.outputColumnFieldTypes = ['MEASURE', 'ATTRIBUTE'];
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
				if (!body.id || body.id == "") body.id = -1;

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
				// Yes, I'm doing something bad and I know it
				// I don't know why I have "family" and not "functionFamily" field, so I am overwriting it on the fly
				if (body.family) {
					body.functionFamily = body.family;
					delete body.family;
				}
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

	$scope.resetType=function(col) {
		col.type = '';
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
			if ($scope.shownFunction.inputVariables[i].name == undefined || $scope.shownFunction.inputVariables[i].type == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.inputVariables[i].name == undefined) {
					$scope.missingFields.push("Input variable  " + index + " name missing");
				}
				if ($scope.shownFunction.inputVariables[i].type == undefined) {
					$scope.missingFields.push("Input variable  " + index + " type missing");

				}
			}
		}

		//there must be at least one output column
		if ($scope.shownFunction.outputColumns.length == 0) {
			correctArguments = false;
			$scope.missingFields.push("Insert at least one output column");
		}

		for (var i = 0; i < $scope.shownFunction.outputColumns.length; i++) {
			if ($scope.shownFunction.outputColumns[i].name == undefined
					|| $scope.shownFunction.outputColumns[i].type == undefined || $scope.shownFunction.outputColumns[i].fieldType == undefined) {
				correctArguments = false;
				var index = i + 1;

				if ($scope.shownFunction.outputColumns[i].type == undefined) {
					$scope.missingFields.push("Output column " + index + " type missing");
				}
				if ($scope.shownFunction.outputColumns[i].name == undefined) {
					$scope.missingFields.push("Output column " + index + " name missing");
				}
				if ($scope.shownFunction.outputColumns[i].fieldType == undefined) {
					$scope.missingFields.push("Output column " + index + " fieldtype missing");
				}
			}
//			if ($scope.shownFunction.outputColumns[i].fieldType == "MEASURE" && $scope.shownFunction.outputColumns[i].type != "NUMBER") {
//				correctArguments = false;
//				$scope.missingFields.push("Field type and type not matching");
//			}
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

	$scope.acSpeedMenu = [{
		label : sbiModule_translate.load("sbi.functionscatalog.executepreview"),
		icon : 'fa fa-play-circle-o',
		action : function(item, event) {
			$scope.applyPreviewItem(item, event);
		}
	}];

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
			if (result && result.data && result.data.FunctionInUseException) {
				sbiModule_messaging.showErrorMessage(result.data.FunctionInUseException,"Delete Error");
			}
			$scope.obtainCatalogFunctionsRESTcall();
			$scope.cleanNewFunction();
			$scope.shownFunction = $scope.newFunction;
			$scope.saveOrUpdateFlag = "save";
		});

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

	$scope.applyPreviewItem = function(item, event) {
		$mdDialog.show({
			templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/functionsCatalog/templates/functionCatalogPreviewTemplate.html',
			parent : angular.element(document.body),
			clickOutsideToClose:true,
			escapeToClose :true,
			autoWrap:false,
			locals: {
				selectedFunction: item,
			},
			fullscreen: true,
			controller: functionCatalogPreviewController
		})
	};

	// --------------------------------------------Application
	// Logic---------------------------------------

	$scope.obtainCatalogFunctionsRESTcall();
	$scope.obtainFunctionTypesRESTcall();

	// ----------------------------------------------Controllers-----------------------------------------------

	function functionCatalogPreviewController($scope,sbiModule_restServices,sbiModule_translate,sbiModule_messaging,$mdDialog,selectedFunction) {
		$scope.translate=sbiModule_translate;
		$scope.selectedFunction = angular.copy(selectedFunction);
		$scope.disablePreview = true;
		var style = {'display': 'inline-flex', 'justify-content':'center', 'align-items':'center'};
		var typesMap = {'STRING': "fa fa-quote-right", 'NUMBER': "fa fa-hashtag", 'DATE': 'fa fa-calendar'};

		// PYTHON ENVIRONMENTS CONFIG
		sbiModule_restServices.promiseGet('2.0/configs/category', 'PYTHON_CONFIGURATION')
		.then(function(response){
			$scope.pythonEnvironments = buildEnvironments(response.data);
		});

		// R ENVIRONMENTS CONFIG
		sbiModule_restServices.promiseGet('2.0/configs/category', 'R_CONFIGURATION')
		.then(function(response){
			$scope.rEnvironments = buildEnvironments(response.data);
		});

		buildEnvironments = function (data) {
			toReturn = []
			for (i=0; i<data.length; i++) {
				key = data[i].label;
				val = data[i].valueCheck;
				toReturn[i] = {"label": key, "value": val};
			}
			return toReturn;
		}

		$scope.datasetsGrid = {
		        enableColResize: false,
		        enableFilter: true,
		        enableSorting: true,
		        onGridReady: initDatasets,
		        onGridSizeChanged: resizeDatasets,
		        rowSelection: "single",
		        onRowClicked: selectDataset,
		        pagination: true,
		        paginationAutoPageSize: true,
		        columnDefs: [
		        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.dataset'), field:'label'},
		        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.datasettype'), field:'dsTypeCd'}],
		        rowData: $scope.datasetList
		}

		function initDatasets(){
			sbiModule_restServices.promiseGet('1.0/datasets','')
			.then(function(response){
				$scope.datasetList = response.data.root;
				$scope.datasetsGrid.api.setRowData($scope.datasetList);
				resizeDatasets();
			}, function(error){
			});
		}

		function resizeDatasets(){
			if ($scope.datasetsGrid.api) $scope.datasetsGrid.api.sizeColumnsToFit();
		}

		function selectDataset(props){
			$scope.selectedDataset = props.api.getSelectedRows()[0];
			$scope.selectedDatasetColumns = getDatasetColumns($scope.selectedDataset);
			$scope.selectedFunction = angular.copy(selectedFunction);
			$scope.$apply();
			if ($scope.columnsGrid.api) {
				$scope.columnDefs[2].cellEditorParams.values = $scope.selectedDatasetColumns;
				$scope.columnsGrid.api.setColumnDefs($scope.columnDefs);
				$scope.columnsGrid.api.setRowData($scope.selectedFunction.inputColumns);
			}
		}

		function getDatasetColumns(ds){
			var toReturn = [];
			var allColumns = ds.meta.columns;
			for (var i=0; i<allColumns.length; i=i+3) {
//				var type = allColumns[i].pvalue;
//				var fieldType = allColumns[i+1].pvalue;
				var alias = allColumns[i+2].pvalue;
				toReturn.push(alias);
			}
			return toReturn;
		}

		$scope.columnDefs = [
        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.inputColumn.name'), field:'name'},
        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.inputColumn.type'), field:'type', cellRenderer:typeRenderer},
        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.inputColumn.datasetColumn'), field:'dsColumn', editable:true, cellRenderer:editableCell, cellEditor:"agSelectCellEditor", cellEditorParams: {values: $scope.selectedDatasetColumns}}];

		$scope.columnsGrid = {
				angularCompileRows: true,
				domLayout :'autoHeight',
		        enableColResize: false,
		        enableFilter: false,
		        enableSorting: false,
		        onGridReady : resizeColumns,
		        onGridSizeChanged: resizeColumns,
		        onCellEditingStopped: refreshRowForColumns,
		        singleClickEdit: true,
		        columnDefs: $scope.columnDefs,
		        rowData: $scope.selectedFunction.inputColumns
		}

		function refreshRowForColumns(cell){
			$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		function resizeColumns(){
			$scope.columnsGrid.api.sizeColumnsToFit();
		}

		function editableCell(params){
			var editButton = '<i class="fa fa-edit"></i> <i>';
			if (typeof(params.value) !== 'undefined')
				return editButton + params.value;
			else return editButton;
		}

		function typeRenderer(params){
			var typeIcon = '<i class="' + typesMap[params.value] + '"></i> <i>'
			return typeIcon + params.value;
		}

		$scope.variablesGrid = {
				angularCompileRows: true,
				domLayout :'autoHeight',
		        enableColResize: false,
		        enableFilter: false,
		        enableSorting: false,
		        onGridReady : resizeVariables,
		        onGridSizeChanged: resizeVariables,
		        onCellEditingStopped: refreshRowForVariables,
		        singleClickEdit: true,
		        columnDefs: [
		        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.inputVariable.name'), field:'name'},
		        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.inputVariable.type'), field:'type', cellRenderer: typeRenderer},
		        	{headerName: $scope.translate.load('sbi.functionscatalog.functionpreview.inputVariable.value'), field:'value', editable: true, cellRenderer: editableCell}],
		        rowData: $scope.selectedFunction.inputVariables
		}

		function refreshRowForVariables(cell){
			$scope.variablesGrid.api.redrawRows({rowNodes: [$scope.variablesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		function resizeVariables(){
			$scope.variablesGrid.api.sizeColumnsToFit();
		}

		$scope.librariesGrid = {
		        enableColResize: false,
		        enableFilter: true,
		        enableSorting: true,
		        onGridReady: resizeLibraries,
		        onGridSizeChanged: resizeLibraries,
		        pagination: true,
		        paginationAutoPageSize: true,
		        columnDefs: [
		        	{headerName: "Library", field:'name'},
		        	{headerName: "Version", field:'version'}]
		}

		function resizeLibraries(){
			$scope.librariesGrid.api.sizeColumnsToFit();
		}

		function refreshRowForLibraries(cell){
			$scope.librariesGrid.api.redrawRows({rowNodes: [$scope.librariesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		$scope.setLibraries=function() {
			var endpoint = $scope.selectedFunction.language == "Python" ? "python" : "RWidget";
			sbiModule_restServices.promiseGet('2.0/backendservices/widgets/'+ endpoint +'/libraries', JSON.parse($scope.selectedFunction.environment).label)
			.then(function(response){
				$scope.selectedFunction.libraries = [];
				var librariesArray = JSON.parse((response.data.result));
				for (idx in librariesArray) {
					lib = librariesArray[idx];
					$scope.selectedFunction.libraries.push({"name": lib.name, "version": lib.version})
				}
				$scope.librariesGrid.api.setRowData($scope.selectedFunction.libraries);
			}, function(error){
			});
		}

		$scope.cancelPreview=function(){
			$scope.selectedDataset = undefined;
			$scope.selectedFunction = angular.copy(selectedFunction);
			$mdDialog.cancel();
		}

		$scope.resultDataGrid = {
		        enableColResize: false,
		        enableFilter: true,
		        enableSorting: true,
		        onGridReady: resizeResultData,
		        onGridSizeChanged: resizeResultData,
		        pagination: true,
		        paginationAutoPageSize: true
		}

		function resizeResultData(){
			if ($scope.resultDataGrid.api) $scope.resultDataGrid.api.sizeColumnsToFit();
		}

		$scope.goToPreview=function(){
			if (!checkColumnsConfiguration($scope.selectedFunction.inputColumns))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.functionscatalog.functionpreview.function.error.datasetColumns"));
			else if (!checkVariablesConfiguration($scope.selectedFunction.inputVariables))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.functionscatalog.functionpreview.function.error.inputVariables"));
			else if (!checkEnvironmentConfiguration($scope.selectedFunction.environment))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.functionscatalog.functionpreview.function.error.environment"));
			else {
				$scope.disablePreview = false;
				$scope.selectedIndex = 1;
				executePreview();
			}
		}

		executePreview=function(){
			body = buildDataServiceBody();
			sbiModule_restServices.promisePost('2.0/datasets/'+ $scope.selectedDataset.label, 'data', body)
			.then(function(response){
				//display results table
				var resultColumnDefs = [];
				for (var i=1; i<response.data.metaData.fields.length; i++) {
					var header = response.data.metaData.fields[i];
					var colDef = {headerName: header.header, field: header.name};
					resultColumnDefs.push(colDef);
				}
				$scope.resultDataGrid.api.setColumnDefs(resultColumnDefs);
				$scope.resultDataGrid.api.setRowData(response.data.rows);
			}, function(error){
				sbiModule_messaging.showErrorMessage("Error during dataset execution","Data service error");
			});
		}

		buildDataServiceBody=function(){
			var body = {};
			body.aggregations = buildBodyAggregations();
			body.parameters = buildBodyParameters();
			body.selections = {};
			body.indexes = [];
			return body;
		}

		buildBodyParameters=function(){
			var parameters = {}
			var allParameters = $scope.selectedDataset.pars;
			for (var i=0; i< allParameters.length; i++) {
				var currPar = allParameters[i];
				parameters[currPar.name] = currPar.value;
			}
			return parameters;
		}

		buildBodyAggregations=function(){
			var aggregations = {};
			var measures = [];
			var categories = [];
			//traditional columns
			var allColumns = $scope.selectedDataset.meta.columns;
			for (var i=0; i<allColumns.length; i=i+3) {
				var name = allColumns[i].column;
				var fieldType = allColumns[i+1].pvalue;
				var alias = allColumns[i+2].pvalue;
				var obj = {};
				obj.id = name;
				obj.alias = alias;
				obj.columnName = name;
				obj.funct = "NONE";
				if (fieldType == "MEASURE") {
					obj.orderColumn = name;
					measures.push(obj);
				} else {
					obj.orderType = "";
					categories.push(obj);
				}
			}
			//catalog function columns
			var functionConfig = {};
			functionConfig.inputColumns = $scope.selectedFunction.inputColumns;
			functionConfig.inputVariables = $scope.selectedFunction.inputVariables;
			functionConfig.outputColumns = $scope.selectedFunction.outputColumns;
			functionConfig.environment = JSON.parse($scope.selectedFunction.environment).label;
			for (var i=0; i<$scope.selectedFunction.outputColumns.length; i++) {
				var outputCol = $scope.selectedFunction.outputColumns[i];
				var obj = {};
				obj.id = outputCol.name;
				obj.alias = outputCol.name;
				obj.catalogFunctionId = $scope.selectedFunction.id;
				obj.catalogFunctionConfig = functionConfig;
				obj.columnName = outputCol.name;
				obj.funct = "NONE";
				if (fieldType == "MEASURE") {
					obj.orderColumn = outputCol.name;
					measures.push(obj);
				} else {
					obj.orderType = "";
					categories.push(obj);
				}
			}
			aggregations.measures = measures;
			aggregations.categories = categories;
			aggregations.dataset = $scope.selectedDataset.label;
			return aggregations;
		}

		$scope.goToConfigurator=function(){
			$scope.disablePreview = true;
			$scope.selectedIndex = 0;
		}

		checkColumnsConfiguration=function(columns){
			for (var i=0; i<columns.length; i++) {
				if (!columns[i].dsColumn)
					return false;
			}
			return true;
		}

		checkVariablesConfiguration=function(variables){
			for (var i=0; i<variables.length; i++) {
				if (!variables[i].value || variables[i].value == '')
					return false;
			}
			return true;
		}

		checkEnvironmentConfiguration=function(environment){
			if (!environment)
				return false;
			return true;
		}

		$scope.toastifyMsg = function(type,msg){
			Toastify({
				text: msg,
				duration: 10000,
				close: true,
				className: 'kn-' + type + 'Toast',
				stopOnFocus: true
			}).showToast();
		}

	};

	$scope.cancelFunction = function() {
		$angularListDetail.goToList();
	}

};

