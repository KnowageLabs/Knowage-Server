/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
(function () {

	agGrid.initialiseAgGridWithAngular1(angular);

	var app = angular.module(
		'lovsManagementModule',

		[
			'ngMaterial',
			'angular_list',
			'angular_table',
			'sbiModule',
			'angular-list-detail',
			'ui.codemirror',
			'ui.tree',
			'knTable',
			'agGrid',
			'ab-base64'
		]
	);

	app.config(['$mdThemingProvider', function ($mdThemingProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
	}]);

	app.controller(
		'lovsManagementController',

		[
			"sbiModule_translate",
			"sbiModule_restServices",
			"$scope",
			"$mdDialog",
			"$mdToast",
			"sbiModule_messaging",
			"sbiModule_config",
			"$timeout",
			"sbiModule_user",
			"sbiModule_device",
			"$filter",
			"$mdPanel",
			"base64",
			lovsManagementFunction
		]
	);

	function lovsManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, sbiModule_messaging, sbiModule_config, $timeout, sbiModule_user, sbiModule_device, $filter, $mdPanel,base64) {
		/**
		 * =====================
		 * ===== Variables =====
		 * =====================
		 */
		$scope.showMe = false;
		$scope.dirtyForm = false; // flag to check for modification
		$scope.enableTest = false;
		$scope.enablePreview = false;
		$scope.previewClicked = false;
		$scope.translate = sbiModule_translate;
		$scope.user = sbiModule_user;
		$scope.listOfLovs = [];
		$scope.previewLovModel = []
		$scope.testLovTreeModel = [];
		$scope.listOfInputTypes = [];
		$scope.listOfScriptTypes = [];
		$scope.listOfDatasources = [];
		$scope.listOfDatasets = [];
		$scope.listForFixLov = [];
		$scope.listOfProfileAttributes = [];
		$scope.listOfEmptyDependencies = [];
		$scope.selectedLov = {};
		$scope.toolbarTitle = "";
		$scope.infoTitle = "";
		$scope.selectedScriptType = {
			language: "",
			text: ""
		};
		$scope.selectedQuery = {
			datasource: "",
			query: ""
		};
		$scope.selectedFIXLov = {};
		$scope.selectedJavaClass = {
			name: ""
		};
		$scope.selectedDataset = {
			id: "",
			label: "",
			name: "",
			description: ""
		};
		$scope.lovItemEnum = {
			"SCRIPT": "SCRIPT",
			"QUERY": "QUERY",
			"FIX_LOV": "FIX_LOV",
			"JAVA_CLASS": "JAVA_CLASS",
			"DATASET": "DATASET"
		};
		var lovTypeEnum = {
			"MAIN": undefined,
			"SCRIPT": "script",
			"QUERY": "query",
			"DATASET": "dataset"
		};
		var lovProviderEnum = {
			"SCRIPT": "SCRIPTLOV",
			"QUERY": "QUERY",
			"FIX_LOV": "FIXLISTLOV",
			"JAVA_CLASS": "JAVACLASSLOV",
			"DATASET": "DATASET"
		};
		$scope.paginationObj = {
			"paginationStart": 0,
			"paginationLimit": 20,
			"paginationEnd": 20
		};
		$scope.dependenciesList = [];
		$scope.dependencyObj = {};
		$scope.testLovColumns = [{
				label: "Name",
				name: "name",
				size: "200px",
				hideTooltip: true,
			},
			{
				label: "Value",
				name: "",
				hideTooltip: true,
				transformer: function () {
					return " <md-checkbox ng-checked=scopeFunctions.getItem(row,'value') ng-click = scopeFunctions.setItem(row,'value')  aria-label='buttonValue'></md-checkbox>";
				}
			},
			{
				label: "Description",
				name: "",
				hideTooltip: true,
				transformer: function () {
					return " <md-checkbox ng-checked=scopeFunctions.getItem(row,'description') ng-click = scopeFunctions.setItem(row,'description') aria-label='buttonDescription'></md-checkbox>";
				}
			},
			{
				label: "Visible",
				name: "",
				hideTooltip: true,
				transformer: function () {
					return " <md-checkbox ng-checked=scopeFunctions.getItem(row,'visible') ng-click = scopeFunctions.setItem(row,'visible') aria-label='buttonVisible'></md-checkbox>";
				}
			}
		]


		$scope.testLovTreeRightColumns = [{
				label: "Level",
				name: "level",
				size: "200px",
				hideTooltip: true,
			},

			{
				label: "Value",
				name: "value",
				hideTooltip: true,
				transformer: function () {
					return '<md-select ng-model=row.value class="noMargin"><md-option ng-repeat="col in scopeFunctions.treeOptions()" value="{{col.name}}">{{col.name}}</md-option></md-select>';
				}
			},
			{
				label: "Description",
				name: "description",
				hideTooltip: true,
				transformer: function () {
					return '<md-select ng-model=row.description class="noMargin"><md-option ng-repeat="col in scopeFunctions.treeOptions()" value="{{col.name}}">{{col.name}}</md-option></md-select>';
				}
			}
		]


		$scope.TreeListType = [

			{
				"name": "Simple",
				"value": "simple"
			},
			{
				"name": "Tree",
				"value": "tree"
			},
			{
				"name": "Tree selectable inner nodes",
				"value": "treeinner"
			}

		]

		var addDataset = function () {
			var config = {
				attachTo: angular.element(document.body),
				templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/lovAddDataset.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen: true,
				locals: {
					listOfDatasets: $scope.listOfDatasets,
					saveDatasetFn: $scope.saveDataset
				},
				controller: addDatasetController,
				clickOutsideToClose: false,
				escapeToClose: true,
			};

			$mdPanel.open(config);
		}

		function addDatasetController($scope, mdPanelRef, sbiModule_translate, listOfDatasets, saveDatasetFn) {
			$scope.translate = sbiModule_translate;
			$scope.listOfDatasets = listOfDatasets;

			$scope.datasetSearchText = '';

			$scope.filterDataset = function () {
				var tempDatasetList = $filter('filter')($scope.listOfDatasets, $scope.datasetSearchText);
				$scope.datasetGrid.api.setRowData(tempDatasetList);
			}

			$scope.gridDatasetColumns = [{
					"headerName": sbiModule_translate.load('sbi.ds.label'),
					"field": "label"
				},
				{
					"headerName": sbiModule_translate.load('sbi.ds.name'),
					"field": "name"
				},
				{
					"headerName": sbiModule_translate.load('sbi.ds.description'),
					"field": "description"
				},
				{
					"headerName": sbiModule_translate.load('sbi.ds.owner'),
					"field": "owner"
				},
				{
					"headerName": sbiModule_translate.load('sbi.ds.scope'),
					"field": "scope"
				}
			];

			$scope.datasetGrid = {
				enableColResize: false,
				enableFilter: true,
				enableSorting: true,
				pagination: true,
				paginationAutoPageSize: true,
				rowSelection: 'single',
				rowMultiSelectWithClick: 'single',
				onGridSizeChanged: resizeColumns,
				columnDefs: $scope.gridDatasetColumns,
				rowData: $scope.listOfDatasets
			};

			function resizeColumns() {
				$scope.datasetGrid.api.sizeColumnsToFit();
			}

			$scope.closeDialog = function () {
				mdPanelRef.close();
				$scope.$destroy();
			}

			$scope.saveDataset = function () {
				var selectedDs = $scope.datasetGrid.api.getSelectedRows()[0];
				saveDatasetFn(selectedDs);
				mdPanelRef.close();
				$scope.$destroy();
			}
		}

		$scope.saveDataset = function (dataSetObject) {
			$scope.selectedDataset = dataSetObject;
		}

		$scope.formatedVisibleValues = [];
		$scope.formatedInvisibleValues = [];
		$scope.cmOption = {
			indentWithTabs: true,
			smartIndent: true,
			lineWrapping: true,
			matchBrackets: true,
			autofocus: true,
			theme: "eclipse",
			lineNumbers: true,
			onLoad: function (_cm) {

				// HACK to have the codemirror instance in the scope...
				$scope.modeChanged = function (type) {
					if (type == 'ECMAScript') {
						_cm.setOption("mode", 'text/javascript');
					} else {
						_cm.setOption("mode", 'text/x-groovy');
					}
				};
			}
		};


		/**
		 * Speed menu for handling the deleting action on one
		 * particular LOV item.
		 */
		$scope.lovsManagementSpeedMenu = [{
			label: sbiModule_translate.load("sbi.generic.delete"),
			icon: 'fa fa-trash-o',
			color: '#a3a5a6',
			action: function (item, event) {

				$scope.confirmDelete(item, event);
			}
		}];

		$scope.treeSpeedMenu = [{
			label: sbiModule_translate.load("sbi.generic.delete"),
			icon: 'fa fa-trash-o',
			color: '#a3a5a6',
			action: function (item, event) {

				deleteTreeLevel(item);
			}
		}];

		$scope.fixLovSpeedMenu = [{
				label: sbiModule_translate.load("sbi.generic.delete"),
				icon: 'fa fa-trash-o',
				color: '#a3a5a6',
				action: function (item, event) {

					$scope.confirmDelete(item, event);
				}
			},
			{
				label: sbiModule_translate.load("sbi.behavioural.lov.fixlov.up"),
				icon: 'fa fa-arrow-up',
				color: '#a3a5a6',
				action: function (item, event) {

					$scope.moveFixLovUp(item, event);
				},
				visible: function (row) {
					return checkArrowVisibility(row, 'up');
				}


			},
			{
				label: sbiModule_translate.load("sbi.behavioural.lov.fixlov.down"),
				icon: 'fa fa-arrow-down',
				color: '#a3a5a6',
				action: function (item, event) {

					$scope.moveFixLovDown(item, event);
				},
				visible: function (row) {
					return checkArrowVisibility(row, 'down');
				}
			}
		];


		$scope.lovTableColumns = [{
				"label": "Label",
				"name": "label",
				"type": "text"
			}, {
				"label": "Description",
				"name": "description",
				"type": "text"
			}, {
				"label": "Type",
				"name": "itypeCd",
				"type": "text"
			},
			{
				"label": sbiModule_translate.load("sbi.generic.delete"),
				"name": sbiModule_translate.load("sbi.generic.delete"),
				"type": "buttons",
				"buttons": [{
					"icon": "fa fa-trash-o",
					"action": function (item, event) {
						$scope.confirmDelete(item, event)
					}
				}]
			}
		];


		$scope.confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
			.content(
				sbiModule_translate
				.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('toast').ok(
				sbiModule_translate.load("sbi.general.continue")).cancel(
				sbiModule_translate.load("sbi.general.cancel"));

		$scope.confirmDelete = function (item, ev) {
			console.log(item);
			var confirm = $mdDialog.confirm()
				.title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
				.content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
				.ariaLabel("confirm_delete")
				.targetEvent(ev)
				.ok(sbiModule_translate.load("sbi.general.continue"))
				.cancel(sbiModule_translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm).then(function () {
				if (item.lovProvider != null) {
					deleteLovItem(item);
				} else {
					deleteFixedLovItem(item);
				}
			}, function () {

			});
		};
		/**
		 * =====================
		 * ===== Functions =====
		 * =====================
		 */

		angular.element(document).ready(function () { // on page load function
			$scope.getAllLovs();
			$scope.getInputTypes();
			$scope.getScriptTypes();
			$scope.getDatasources();
		});

		$scope.setDirty = function () {
			$scope.dirtyForm = true;
		}

		$scope.validateAndSetDirty = function () {

			var valid = true;
			if ($scope.selectedLov.hasOwnProperty("id")) {
				for (var i in $scope.listOfLovs) {
					if ($scope.selectedLov.id != $scope.listOfLovs[i].id &&
						angular.equals($scope.selectedLov.label, $scope.listOfLovs[i].label)) {
						valid = false;
						break;
					}
				}
			} else {
				for (var i in $scope.listOfLovs) {
					if (angular.equals($scope.selectedLov.label, $scope.listOfLovs[i].label)) {
						valid = false;
						break;
					}
				}
			}

			$scope.attributeForm.lovLbl.$setValidity("labelNotValid", valid);

			$scope.setDirty();
		}

		$scope.validateNameAndSetDirty = function () {
			var valid = true;
			if ($scope.selectedLov.hasOwnProperty("id")) {
				for (var i in $scope.listOfLovs) {
					if ($scope.selectedLov.id != $scope.listOfLovs[i].id &&
						angular.equals($scope.selectedLov.name, $scope.listOfLovs[i].name)) {
						valid = false;
						break;
					}
				}
			} else {
				for (var i in $scope.listOfLovs) {
					if (angular.equals($scope.selectedLov.name, $scope.listOfLovs[i].name)) {
						valid = false;
						break;
					}
				}
			}
			$scope.attributeForm.lovName.$setValidity("nameNotValid", valid);
			$scope.setDirty();
		}

		/**
		 * When clicking on plus button on the left panel, this function
		 * will be called and we should enable showing of the right panel
		 * on the main page for LOVs management (variable "showMe" will
		 * provide this functionality).	 *
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.createLov = function () {
			if ($scope.dirtyForm) {
				$mdDialog.show($scope.confirm).then(function () {
					$scope.dirtyForm = false;
					$scope.selectedLov = {};
					$scope.listForFixLov = [];
					$scope.showMe = true;
					$scope.enableTest = false;
					$scope.label = "";
					$scope.validateAndSetDirty();
					$scope.validateNameAndSetDirty();
				}, function () {

					$scope.showMe = true;
				});

			} else {

				$scope.selectedLov = {};
				$scope.showMe = true;
				$scope.listForFixLov = [];
				$scope.enableTest = false;

			}
		}
		/**
		 * Function that add additional fields depending on selection
		 * from combobox
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */

		$scope.changeLovType = function (item) {

			var propName = item;
			var prop = lovProviderEnum[propName];

			switch (item) {
				case $scope.lovItemEnum.SCRIPT:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.scriptWizard");
					$scope.infoTitle = sbiModule_translate.load("sbi.behavioural.lov.info.syntax")
					cleanSelections();

					break;
				case $scope.lovItemEnum.QUERY:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.queryWizard");
					$scope.infoTitle = sbiModule_translate.load("sbi.behavioural.lov.info.syntax")
					cleanSelections();

					break;
				case $scope.lovItemEnum.FIX_LOV:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.fixedListWizard");
					$scope.infoTitle = sbiModule_translate.load("sbi.behavioural.lov.info.rules")
					cleanSelections();

					break;
				case $scope.lovItemEnum.JAVA_CLASS:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.javaClassWizard");
					$scope.infoTitle = sbiModule_translate.load("sbi.behavioural.lov.info.rules")
					cleanSelections();

					break;
				case $scope.lovItemEnum.DATASET:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.datasetWizard");
					cleanSelections();

					break;
				default:
					break;
			}


			for (var i = 0; i < $scope.listOfInputTypes.length; i++) {
				if ($scope.listOfInputTypes[i].VALUE_CD == item) {
					$scope.selectedLov.itypeId = "" + $scope.listOfInputTypes[i].VALUE_ID;
				}
			}

			if ($scope.selectedLov.lovProvider && !$scope.selectedLov.lovProvider.hasOwnProperty(prop)) {

				formatForTest($scope.selectedLov, 'new');
			}
		}


		var cleanSelections = function () {
			$scope.enableTest = false;
			$scope.selectedScriptType = {};
			$scope.selectedQuery = {};
			$scope.selectedFIXLov = {};
			$scope.selectedJavaClass = {};
			$scope.selectedDataset = {};
			$scope.listForFixLov = [];
		}
		/**
		 * Function opens dialog with available
		 * profile attributes when clicked
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		$scope.openAttributesFromLOV = function () {

			sbiModule_restServices.promiseGet("2.0/attributes", '')
				.then(function (response) {
					$scope.listOfProfileAttributes = response.data;
					console.log($scope.listOfProfileAttributes);
				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));

				});
			$mdDialog
				.show({
					scope: $scope,
					preserveScope: true,
					parent: angular.element(document.body),
					controllerAs: 'LOVSctrl',
					templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/profileAttributes.html',
					clickOutsideToClose: false,
					hasBackdrop: false
				});
		}
		/**
		 * Function opens dialog with information
		 * about selection
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		$scope.openInfoFromLOV = function () {
			if ($scope.selectedLov.itypeCd != $scope.lovItemEnum.DATASET) {
				$mdDialog
					.show({
						scope: $scope,
						preserveScope: true,
						parent: angular.element(document.body),
						controllerAs: 'LOVSctrl',
						templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/Info.html',
						clickOutsideToClose: false,
						hasBackdrop: false
					});

			}

		}
		$scope.closeDialogFromLOV = function () {
			$scope.testLovTreeModel = [];
			$scope.formatedVisibleValues = [];
			$scope.formatedInvisibleValues = [];
			$scope.testLovModel = [];
			$scope.previewLovModel = [];
			$scope.paramsList = [];
			$scope.paramObj = {};

			$mdDialog.cancel();
			console.log(sbiModule_device.browser.name);
		}

		$scope.$watch('attributeForm.$invalid', function (newValue, oldValue) {


			switch (newValue) {

				case false:
					if ($scope.previewClicked)
						$scope.enableTest = true;
					break
				default:
					$scope.enableTest = false;
					break
			}
		})

		/**
		 * When clicking on Save button in the header of the right panel,
		 * this function will be called and the functionality for saving
		 * LOV into the DB will be run. 	 *
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.saveLov = function () { // this function is called when clicking on save button

			if (formatForSave()) {

				if ($scope.selectedLov.hasOwnProperty("id")) { // if item already exists do update PUT

					sbiModule_restServices.promisePut("2.0/lovs", "", $scope.selectedLov)
						.then(function (response) {
							$scope.listOfLovs = [];
							$timeout(function () {
								$scope.getAllLovs();
							}, 1000);
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
							$scope.selectedLov={};
							$scope.showMe = false;
							$scope.dirtyForm = false;
							$scope.closeDialogFromLOV();

						}, function (response) {
							if (response.status == 409) {
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.behavioural.lov.errorLovWithTheSameLabelExists"), sbiModule_translate.load("sbi.generic.toastr.title.error"));
								$scope.enableTest = false;
								$scope.closeDialogFromLOV();
							} else {
								sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
							}

						});

				} else {

					sbiModule_restServices.promisePost("2.0/lovs/save", "", $scope.selectedLov)
						.then(function (response) {
							var id = response.data;
							$scope.listOfLovs = [];
							$timeout(function () {
								$scope.getAllLovs().then(function (response) {
									$scope.itemOnClick($filter('filter')($scope.listOfLovs, {
										"id": id
									}, true)[0]);
								});
							}, 1000);
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
							$scope.selectedLov={};
							$scope.showMe = false;
							$scope.dirtyForm = false;
							$scope.closeDialogFromLOV();

						}, function (response) {
							if (response.status == 409) {
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.behavioural.lov.errorLovWithTheSameLabelExists"), sbiModule_translate.load("sbi.generic.toastr.title.error"));
								$scope.enableTest = false;
								$scope.closeDialogFromLOV();
							} else {
								sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
							}
						});
				}
			}

		}

		$scope.updateLovWithoutProvider = function () {
			var result = {};
			var propName = $scope.selectedLov.itypeCd;
			var prop = lovProviderEnum[propName];

			switch (prop) {
				case lovProviderEnum.QUERY:
					$scope.selectedLov.lovProvider[prop].CONNECTION = $scope.selectedQuery.datasource;
					$scope.selectedLov.lovProvider[prop].STMT = $scope.selectedQuery.query;

					break;
				case lovProviderEnum.SCRIPT:
					$scope.selectedLov.lovProvider[prop].LANGUAGE = $scope.selectedScriptType.language;
					$scope.selectedLov.lovProvider[prop].SCRIPT = $scope.selectedScriptType.text;
					break;
				case lovProviderEnum.FIX_LOV:

					if ($scope.listForFixLov != null && $scope.listForFixLov.length > 1) {
						$scope.selectedLov.lovProvider[prop].ROWS.ROW = $scope.listForFixLov;
					} else if ($scope.listForFixLov != null && $scope.listForFixLov.length == 1) {
						$scope.selectedLov.lovProvider[prop].ROWS.ROW = $scope.listForFixLov[0];
					}
					break;
				case lovProviderEnum.JAVA_CLASS:
					$scope.selectedLov.lovProvider[prop].JAVA_CLASS_NAME = $scope.selectedJavaClass.name;
					break;
				case lovProviderEnum.DATASET:
					$scope.selectedLov.lovProvider[prop].ID = $scope.selectedDataset.id;
					if ($scope.selectedDataset.id) {
						for (var i = 0; i < $scope.listOfDatasets.length; i++) {
							if ($scope.listOfDatasets[i].id == $scope.selectedDataset.id) {
								$scope.selectedLov.lovProvider[prop].LABEL = $scope.listOfDatasets[i].label;
							}
						}
					}
					break;
			}

			var tempObj = $scope.selectedLov.lovProvider[prop];
			result[prop] = tempObj
			var x2js = new X2JS();
			var xmlAsStr = x2js.json2xml_str(result);
			if (xmlAsStr.indexOf("'") != -1) {
				xmlAsStr = xmlAsStr.replace(/'/g, "'")
			}
			$scope.selectedLov.lovProvider = xmlAsStr;


			sbiModule_restServices.promisePut("2.0/lovs", "", $scope.selectedLov)
				.then(function (response) {
					$scope.listOfLovs = [];
					$timeout(function () {
						$scope.getAllLovs();
					}, 1000);
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
					$scope.selectedLov = {};
					$scope.showMe = false;
					$scope.dirtyForm = false;
					$scope.closeDialogFromLOV();

				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

				});
		}
		/**
		 * When clicking on Cancel button in the header of the right panel,
		 * this function will be called and the right panel will be hidden.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.cancel = function () {
			$scope.showMe = false;
			$scope.dirtyForm = false;
			$scope.selectedLov = {};
			$scope.selectedFIXLov = {};

		}

		/**
		 * Action that will happen when user clicks on the "Add" button that
		 * adds new pair (label, description) for current Fixed LOV item
		 * (second panel on the right side of the page).
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.addNewFixLOV = function () {
			if ($scope.listForFixLov.length > 0) {

				for (var i = 0; i < $scope.listForFixLov.length; i++) {
					if ($scope.selectedFIXLov.VALUE != $scope.listForFixLov[i].VALUE && $scope.selectedFIXLov.DESCRIPTION != $scope.listForFixLov[i].DESCRIPTION) {
						console.log("new one");
						$scope.listForFixLov.push($scope.selectedFIXLov);
						$scope.selectedFIXLov = {};
						break;
					} else {
						console.log("editing");
						var index = $scope.listForFixLov.indexOf($scope.listForFixLov[i]);
						$scope.listForFixLov.splice(index, 1);
						$scope.listForFixLov.push($scope.selectedFIXLov);
						$scope.selectedFIXLov = {};
						break;
					}
				}

			} else {
				$scope.listForFixLov.push($scope.selectedFIXLov);
				$scope.selectedFIXLov = {};
			}


		}

		var escapeXml = function (unsafe) {
			return unsafe.replace(/'/g, "'")
				.replace(/"/g, '"')
				.replace(/>/g, '>')
				.replace(/</g, '<')
				.replace(/&/g, '&')
				.replace(/&apos;/g, "'");
		}


		var decode = function (item) {
			try {
				if (item.lovProvider.SCRIPTLOV) {
					item.lovProvider.SCRIPTLOV.SCRIPT = base64.decode(item.lovProvider.SCRIPTLOV.SCRIPT);
					item.lovProvider.SCRIPTLOV.SCRIPT = escapeXml(item.lovProvider.SCRIPTLOV.SCRIPT);
				}
				if (item.lovProvider.QUERY) {
					item.lovProvider.QUERY.decoded_STMT = base64.decode(item.lovProvider.QUERY.STMT);
					item.lovProvider.QUERY.decoded_STMT = escapeXml(item.lovProvider.QUERY.decoded_STMT);
				}
			} catch (err) {
				console.log("Error during decoding of the script/statement: " + err);
			}

		};


		/**
		 * Function that handles what should be done when user clicks on the
		 * LOV item on the left side of the page (the one from the catalog).
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.itemOnClick = function (item) {

			item.lovProvider = angular.fromJson(item.lovProvider);
			decode(item);

			if ($scope.dirtyForm) {
				$mdDialog.show($scope.confirm).then(function () {
					$scope.selectLov(item);
					$scope.dirtyForm = false;
					$scope.showMe = true;
				}, function () {
					$scope.showMe = true;
				});
			} else {
				$scope.selectLov(item);
				$scope.showMe = true;
			}
		}

		$scope.selectLov = function(item) {
			$scope.selectedLov = angular.copy(item);
			console.log($scope.selectedLov);
			$scope.changeLovType($scope.selectedLov.itypeCd);
			if ($scope.dirtyForm) {
				$scope.validateAndSetDirty();
				$scope.validateNameAndSetDirty();
			}
			if ($scope.selectedLov.lovProvider.hasOwnProperty(lovProviderEnum.SCRIPT)) {
				if ($scope.selectedLov.lovProvider.SCRIPTLOV.LANGUAGE != null) {
					$scope.selectedScriptType.language = $scope.selectedLov.lovProvider.SCRIPTLOV.LANGUAGE;
				} else {
					$scope.selectedScriptType.language = 'groovy';
				}

				$scope.selectedScriptType.text = $scope.selectedLov.lovProvider.SCRIPTLOV.SCRIPT;

			} else if ($scope.selectedLov.lovProvider.hasOwnProperty(lovProviderEnum.QUERY)) {

				$scope.selectedQuery.datasource = $scope.selectedLov.lovProvider.QUERY.CONNECTION;
				$scope.selectedQuery.query = $scope.selectedLov.lovProvider.QUERY.decoded_STMT;

			} else if ($scope.selectedLov.lovProvider.hasOwnProperty(lovProviderEnum.FIX_LOV)) {

				if (Array === $scope.selectedLov.lovProvider.FIXLISTLOV.ROWS.ROW.constructor) {

					$scope.listForFixLov = [];
					$scope.listForFixLov = $scope.selectedLov.lovProvider.FIXLISTLOV.ROWS.ROW;
				} else {
					$scope.listForFixLov = [];
					$scope.listForFixLov.push($scope.selectedLov.lovProvider.FIXLISTLOV.ROWS.ROW);
				}

			} else if ($scope.selectedLov.lovProvider.hasOwnProperty(lovProviderEnum.JAVA_CLASS)) {

				$scope.selectedJavaClass.name = $scope.selectedLov.lovProvider.JAVACLASSLOV.JAVA_CLASS_NAME;

			} else if ($scope.selectedLov.lovProvider.hasOwnProperty(lovProviderEnum.DATASET)) {
				var dataSetId = $scope.selectedLov.lovProvider.DATASET.ID;

				sbiModule_restServices.promiseGet("1.0/datasets/dataset/id", dataSetId)
					.then(function (response) {
						var dataSet = response.data[0];
						$scope.selectedDataset.id = dataSet.id;
						$scope.selectedDataset.label = dataSet.label;
						$scope.selectedDataset.name = dataSet.name;
					}, function (response) {
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
					});
			}
		}

		/**
		 * Function that bind fixed lov item with model
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		$scope.itemOnClickFixLov = function (item) {
			$scope.selectedFIXLov = angular.copy(item);
		}
		/**
		 * Function that shows or hides arrows in table if
		 * if its first or last items
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		var checkArrowVisibility = function (row, direction) {
			var firstRow = $scope.listForFixLov[0];
			var lastRow = $scope.listForFixLov[$scope.listForFixLov.length - 1];
			if (direction == 'up') {

				if (row.VALUE == firstRow.VALUE) {
					return false;
				} else {
					return true;
				}

			} else if (direction == 'down') {
				if (row.VALUE == lastRow.VALUE) {
					return false;
				} else {
					return true;
				}
			}


		}
		/**
		 * Functions that moves items in table up or down
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		$scope.moveFixLovUp = function (item) {
			var index = $scope.listForFixLov.indexOf(item);
			var nextIndex = index - 1;
			var temp = $scope.listForFixLov[index];
			$scope.listForFixLov[index] = $scope.listForFixLov[nextIndex];
			$scope.listForFixLov[nextIndex] = temp;
		}
		$scope.moveFixLovDown = function (item) {
			var index = $scope.listForFixLov.indexOf(item);
			var nextIndex = index + 1;
			var temp = $scope.listForFixLov[index];
			$scope.listForFixLov[index] = $scope.listForFixLov[nextIndex];
			$scope.listForFixLov[nextIndex] = temp;
		}
		/**
		 * Function that delete fixlov
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		var deleteFixedLovItem = function (item) {
			var index = $scope.listForFixLov.indexOf(item);
			$scope.listForFixLov.splice(index, 1);
		}

		var deleteTreeLevel = function (item) {
			var index = $scope.testLovTreeModel.indexOf(item);
			$scope.testLovTreeModel.splice(index, 1);
		}
		/**
		 * Call all necessary services when getting all LOV items (all items
		 * in the LOV catalog for our page).
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.getAllLovs = function () { // service that gets list of drivers @GET
			var promise = sbiModule_restServices.promiseGet("2.0", "lovs/get/all");
			promise.then(function (response) {
				console.log(response);
				$scope.listOfLovs = response.data;
			}, function (response) {

				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));

			});

			return promise;
		}

		/**
		 * Get all input types for populating the GUI item that
		 * holds them when specifying the LOV item. This is used
		 * for specifying what kind (type) of LOV item user wants
		 * to define.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */

		$scope.getInputTypes = function () {
			sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType", "DOMAIN_TYPE=INPUT_TYPE")
				.then(function (response) {
					$scope.listOfInputTypes = response.data;
				}, function (response) {

					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));

				});
		}

		/**
		 * Get all script types from the DB in order to populate
		 * its GUI element so user can pick the script type he
		 * wants for the Script input type.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */

		$scope.getScriptTypes = function () {
			sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType", "DOMAIN_TYPE=SCRIPT_TYPE")
				.then(function (response) {
					$scope.listOfScriptTypes = response.data;
				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));

				});
		}


		/**
		 * Get datasources from the DB in order to populate the combo box
		 * that servers as a datasource picker for Query input type for
		 * LOV.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.getDatasources = function () {
			sbiModule_restServices.promiseGet("2.0/datasources", "")
				.then(function (response) {
					$scope.listOfDatasources = response.data;
				}, function (response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));

				});
		}

		/**
		 * Get datasets from the DB in order to populate the combo box
		 * that servers as a dataset picker for Dataset input type for
		 * LOV.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */

		$scope.getDatasets = function () {
			if ($scope.listOfDatasets.length == 0) {
				sbiModule_restServices.promiseGet("1.0/datasets/datasetsforlov", "")
					.then(function (response) {
						$scope.listOfDatasets = response.data;
						addDataset();
					}, function (response) {
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
					});
			} else {
				addDataset();
			}
		}

		$scope.testLov = function () {


			$scope.buildTestTable();
			console.log($scope.tableModelForTest)
			$mdDialog
				.show({
					scope: $scope,
					preserveScope: true,
					parent: angular.element(document.body),
					controllerAs: 'LOVSctrl',
					templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/lovTest.html',
					clickOutsideToClose: false,
					hasBackdrop: false
				});
		}


		$scope.indexInList = function (item, list) {

			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if (object == item.name) {
					return i;
				}
			}

			return -1;
		}

		$scope.tableFunction = {

			getItem: function (row, column) {
				if (column == 'description' && row.name == $scope.treeListTypeModel['DESCRIPTION-COLUMN']) {
					return true;
				}
				if (column == 'value' && row.name == $scope.treeListTypeModel['VALUE-COLUMN']) {
					return true;
				}
				if (column == 'visible') {

					for (var i = 0; i < $scope.formatedVisibleValues.length; i++) {
						if ($scope.formatedVisibleValues[i] == row.name) {
							return true;
						}
					}
				}
			},

			setItem: function (row, column) {
				if (column == 'description') {
					$scope.treeListTypeModel['DESCRIPTION-COLUMN'] = row.name;
				}
				if (column == 'value') {
					$scope.treeListTypeModel['VALUE-COLUMN'] = row.name;
				}
				if (column == 'visible') {

					var index = $scope.indexInList(row, $scope.formatedVisibleValues);

					if (index != -1) {
						$scope.formatedVisibleValues.splice(index, 1);
					} else {
						$scope.formatedVisibleValues.push(row.name);
					}


				}
			}
		}

		var deleteLovItem = function (item) {

			sbiModule_restServices
				.promiseDelete("2.0/lovs/delete", item.id)
				.then(
					function (response) {
						$scope.listOfLovs = [];
						$timeout(function () {
							$scope.getAllLovs();
						}, 1000);
						sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
						$scope.selectedLov = {};
						$scope.showMe = false;
						$scope.dirtyForm = false;
					},
					function (response) {
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					});
		}


		var formatForTest = function (item, state) {

			var propName = $scope.selectedLov.itypeCd;
			var prop = lovProviderEnum[propName];

			if (state == 'new') {
				$scope.selectedLov.lovProvider = {};
				$scope.selectedLov.lovProvider[prop] = {

					"LOVTYPE": "simple",
				};


				switch (prop) {
					case lovProviderEnum.QUERY:

						$scope.selectedLov.lovProvider[prop]['VISIBLE-COLUMNS'] = "";
						$scope.selectedLov.lovProvider[prop]['INVISIBLE-COLUMNS'] = "";

						$scope.selectedLov.lovProvider[prop]['DESCRIPTION-COLUMN'] = "";
						$scope.selectedLov.lovProvider[prop]['VALUE-COLUMN'] = "";

						break;
					case lovProviderEnum.SCRIPT:

						$scope.selectedLov.lovProvider[prop]['VISIBLE-COLUMNS'] = "";
						$scope.selectedLov.lovProvider[prop]['INVISIBLE-COLUMNS'] = "";

						$scope.selectedLov.lovProvider[prop]['DESCRIPTION-COLUMN'] = "";
						$scope.selectedLov.lovProvider[prop]['VALUE-COLUMN'] = "";

						$scope.selectedLov.lovProvider[prop]['TREE-LEVELS-COLUMNS'] = "";


						break;
					case lovProviderEnum.FIX_LOV:

						$scope.selectedLov.lovProvider[prop]['VISIBLE-COLUMNS'] = "DESCRIPTION";
						$scope.selectedLov.lovProvider[prop]['INVISIBLE-COLUMNS'] = "VALUE";

						$scope.selectedLov.lovProvider[prop]['DESCRIPTION-COLUMN'] = "DESCRIPTION";
						$scope.selectedLov.lovProvider[prop]['VALUE-COLUMN'] = "VALUE";

						$scope.selectedLov.lovProvider[prop]['TREE-LEVELS-COLUMNS'] = "";

						$scope.selectedLov.lovProvider[prop].ROWS = {};

						break;
					case lovProviderEnum.JAVA_CLASS:

						$scope.selectedLov.lovProvider[prop]['VISIBLE-COLUMNS'] = "VALUE";
						$scope.selectedLov.lovProvider[prop]['INVISIBLE-COLUMNS'] = "VALUE";

						$scope.selectedLov.lovProvider[prop]['DESCRIPTION-COLUMN'] = "VALUE";
						$scope.selectedLov.lovProvider[prop]['VALUE-COLUMN'] = "VALUE";

						$scope.selectedLov.lovProvider[prop]['TREE-LEVELS-COLUMNS'] = "";

						break;
					case lovProviderEnum.DATASET:

						$scope.selectedLov.lovProvider[prop]['VISIBLE-COLUMNS'] = "";
						$scope.selectedLov.lovProvider[prop]['INVISIBLE-COLUMNS'] = "";

						$scope.selectedLov.lovProvider[prop]['DESCRIPTION-COLUMN'] = "";
						$scope.selectedLov.lovProvider[prop]['VALUE-COLUMN'] = "";

						break;
				}

			}


			switch (prop) {
				case lovProviderEnum.QUERY:


					$scope.selectedLov.lovProvider[prop].CONNECTION = $scope.selectedQuery.datasource;
					$scope.selectedLov.lovProvider[prop].STMT = $scope.selectedQuery.query;

					break;
				case lovProviderEnum.SCRIPT:
					$scope.selectedLov.lovProvider[prop].LANGUAGE = $scope.selectedScriptType.language;
					$scope.selectedLov.lovProvider[prop].SCRIPT = $scope.selectedScriptType.text;
					break;
				case lovProviderEnum.FIX_LOV:

					if ($scope.listForFixLov != null && $scope.listForFixLov.length > 1) {
						$scope.selectedLov.lovProvider[prop].ROWS.ROW = $scope.listForFixLov;
					} else if ($scope.listForFixLov != null && $scope.listForFixLov.length == 1) {
						$scope.selectedLov.lovProvider[prop].ROWS.ROW = $scope.listForFixLov[0];
					}
					break;
				case lovProviderEnum.JAVA_CLASS:
					$scope.selectedLov.lovProvider[prop].JAVA_CLASS_NAME = $scope.selectedJavaClass.name;
					break;
				case lovProviderEnum.DATASET:
					$scope.selectedLov.lovProvider[prop].ID = $scope.selectedDataset.id;
					if ($scope.selectedDataset.id) {
						for (var i = 0; i < $scope.listOfDatasets.length; i++) {
							if ($scope.listOfDatasets[i].id == $scope.selectedDataset.id) {
								$scope.selectedLov.lovProvider[prop].LABEL = $scope.listOfDatasets[i].label;
							}
						}
					}


					break;
			}

		}

		var formatForSave = function () {
			console.log(sbiModule_device);
			var result = {}
			var propName = $scope.selectedLov.itypeCd;
			var prop = lovProviderEnum[propName];

			var tempObj = $scope.selectedLov.lovProvider[prop];


			if (!$scope.treeListTypeModel || $scope.treeListTypeModel.LOVTYPE == 'simple') {

				tempObj['DESCRIPTION-COLUMN'] = $scope.treeListTypeModel['DESCRIPTION-COLUMN'];
				tempObj['VALUE-COLUMN'] = $scope.treeListTypeModel['VALUE-COLUMN'];
				tempObj['VISIBLE-COLUMNS'] = $scope.formatedVisibleValues.join();
				for (var i = 0; i < $scope.testLovModel.length; i++) {
					if ($scope.formatedVisibleValues.indexOf($scope.testLovModel[i].name) === -1) {
						$scope.formatedInvisibleValues.push($scope.testLovModel[i].name);
					}
				}
				tempObj['INVISIBLE-COLUMNS'] = $scope.formatedInvisibleValues.join();


			} else {


				//tempObj['TREE-LEVELS-COLUMNS'] = "";
				delete tempObj['DESCRIPTION-COLUMN'];
				delete tempObj['VALUE-COLUMN'];
				$scope.formatedDescriptionColumns = [];
				$scope.formatedValueColumns = [];

				for (var i = 0; i < $scope.testLovTreeModel.length; i++) {
					$scope.formatedDescriptionColumns.push($scope.testLovTreeModel[i].description);
				}
				tempObj['DESCRIPTION-COLUMNS'] = $scope.formatedDescriptionColumns.join();

				for (var i = 0; i < $scope.testLovTreeModel.length; i++) {
					$scope.formatedValueColumns.push($scope.testLovTreeModel[i].value);
				}
				tempObj['VALUE-COLUMNS'] = $scope.formatedValueColumns.join();

				for (var i = 0; i < $scope.testLovModel.length; i++) {

					if ($scope.formatedValueColumns.indexOf($scope.testLovModel[i].name) === -1) {
						$scope.formatedInvisibleValues.push($scope.testLovModel[i].name);
					}

				}
				tempObj['INVISIBLE-COLUMNS'] = $scope.formatedInvisibleValues.join();
			}


			tempObj.LOVTYPE = $scope.treeListTypeModel.LOVTYPE;

			if (tempObj.LOVTYPE == "simple" && (tempObj['VALUE-COLUMN'] == "" || tempObj['DESCRIPTION-COLUMN'] == "")) {
				sbiModule_messaging.showErrorMessage("Value or description field is empty", sbiModule_translate.load("sbi.generic.toastr.title.error"));
				return false;
			}

			if (tempObj.LOVTYPE == "tree" && (tempObj['VALUE-COLUMNS'] == "" || tempObj['DESCRIPTION-COLUMNS'] == "")) {
				sbiModule_messaging.showErrorMessage("Tree is not defined", sbiModule_translate.load("sbi.generic.toastr.title.error"));
				return false;
			}

			result[prop] = tempObj
			var x2js = new X2JS();
			var xmlAsStr = x2js.json2xml_str(result);
			if (xmlAsStr.indexOf("'") != -1) {
				xmlAsStr = xmlAsStr.replace(/'/g, "'")
			}
			$scope.selectedLov.lovProvider = xmlAsStr;
			return true;

		}

		$scope.formatColumns = function (array) {
			var arr = [];
			var size = array.length;
			for (var i = 0; i < size; i++) {
				var obj = {};
				obj.label = array[i].name;
				obj.name = array[i].name;
				if (size <= 10) {
					obj.size = "60px"
				}
				arr.push(obj);
			}
			return arr;
		}

		$scope.checkForDependencies = function () {

			var toSend = {};
			var selectedLovForDependencyChecking = angular.copy($scope.selectedLov);
			var x2js = new X2JS();
			var xmlAsStr = x2js.json2xml_str(selectedLovForDependencyChecking.lovProvider);
			selectedLovForDependencyChecking.lovProvider = xmlAsStr;


			toSend.provider = selectedLovForDependencyChecking.lovProvider;

			sbiModule_restServices.promisePost("2.0", "lovs/checkdependecies", toSend)
				.then(function (response) {
					$scope.listOfEmptyDependencies = [];
					$scope.listOfEmptyDependencies = response.data;

					if ($scope.listOfEmptyDependencies.length > 0) {
						$scope.dependenciesList = [];
						for (var i = 0; i < $scope.listOfEmptyDependencies.length; i++) {
							$scope.dependencyObj = {};
							$scope.dependencyObj.name = $scope.listOfEmptyDependencies[i].name;
							$scope.dependencyObj.type = $scope.listOfEmptyDependencies[i].type;
							$scope.dependenciesList.push($scope.dependencyObj);
							$scope.dependencyObj = {};
						}

						$mdDialog
							.show({
								scope: $scope,
								preserveScope: true,
								parent: angular.element(document.body),
								controllerAs: 'LOVSctrl',
								templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/lovParams.html',
								clickOutsideToClose: false,
								hasBackdrop: false
							});


					} else {
						$scope.previewLov();
					}


				}, function (response) {

					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));

				});

			selectedLovForDependencyChecking = null;
		}


		$scope.openPreviewDialog = function () {

			$scope.paginationObj.paginationStart = 0;
			$scope.paginationObj.paginationLimit = 20;
			$scope.paginationObj.paginationEnd = 20;

			if (!$scope.selectedLov.hasOwnProperty('lovProvider')) {

				formatForTest($scope.selectedLov, 'new');
				console.log("new")
			} else {
				formatForTest($scope.selectedLov, 'edit');
				console.log("edit")
			}

			$scope.checkForDependencies();


		}

		$scope.previewLov = function (dependencies) {

			$scope.perviewClicked = true;
			var toSend = {};
			var selectedLovForPreview = angular.copy($scope.selectedLov);
			var x2js = new X2JS();
			var xmlAsStr = x2js.json2xml_str(selectedLovForPreview.lovProvider);
			selectedLovForPreview.lovProvider = xmlAsStr;


			toSend.data = selectedLovForPreview;
			toSend.pagination = $scope.paginationObj;
			if (dependencies != undefined) {
				toSend.dependencies = dependencies;
			}

			$scope.previewLovModel = [];

			sbiModule_restServices
				.promisePost("2.0", "lovs/preview", toSend)
				.then(
					function (response) {
						if (response.status == 204) {
							$scope.enableTest = false;
							sbiModule_messaging.showErrorMessage("Check your syntax", sbiModule_translate.load("sbi.generic.toastr.title.error"));

						} else {
							$scope.tableModelForTest = response.data.metaData.fields;
							$scope.previewLovColumns = $scope.formatColumns(response.data.metaData.fields);
							$scope.previewLovModel = response.data.root;
							$scope.paginationObj.size = response.data.results;


							$mdDialog
								.show({
									scope: $scope,
									preserveScope: true,
									parent: angular.element(document.body),
									controllerAs: 'LOVSctrl',
									templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/lovPreview.html',
									clickOutsideToClose: false,
									hasBackdrop: false
								});


							if (!$scope.attributeForm.$invalid) {
								$scope.enableTest = true;
							} else {
								$scope.enableTest = false;
							}

						}

					},
					function (response) {
						$scope.enableTest = false;
						sbiModule_messaging
							.showErrorMessage(
								"An error occured while getting properties for selected member",
								'Error');

					});
			selectedLovForPreview = null;
		}

		$scope.testTreeScopeFunctions = {

			treeOptions: function () {
				return $scope.tableModelForTest;
			},


		};

		$scope.doServerPagination = function () {
			var toSend = {};
			var selectedLovForPreview = angular.copy($scope.selectedLov);
			var x2js = new X2JS();
			var xmlAsStr = x2js.json2xml_str(selectedLovForPreview.lovProvider);
			selectedLovForPreview.lovProvider = xmlAsStr;
			toSend.data = selectedLovForPreview;
			toSend.pagination = $scope.paginationObj;

			sbiModule_restServices
				.promisePost("2.0", "lovs/preview", toSend)
				.then(
					function (response) {
						$scope.previewLovColumns = $scope.formatColumns(response.data.metaData.fields);
						$scope.previewLovModel = response.data.root;
						$scope.paginationObj.size = response.data.results;

					},
					function (response) {
						sbiModule_messaging
							.showErrorMessage(
								"An error occured while getting properties for selected member",
								'Error');

					});
			selectedLovForPreview = null;
		}

		$scope.getNextPreviewSet = function () {
			console.log("page up");
			$scope.paginationObj.paginationStart = $scope.paginationObj.paginationStart + $scope.paginationObj.paginationLimit;

			$scope.paginationObj.paginationEnd = $scope.paginationObj.paginationStart + $scope.paginationObj.paginationLimit;
			if ($scope.paginationObj.paginationEnd > $scope.paginationObj.size) {
				$scope.paginationObj.paginationEnd = $scope.paginationObj.size;
			}
			$scope.doServerPagination();
		}
		$scope.getBackPreviewSet = function () {
			console.log("page down");
			var temp = $scope.paginationObj.paginationStart;
			$scope.paginationObj.paginationStart = $scope.paginationObj.paginationStart - $scope.paginationObj.paginationLimit;
			$scope.paginationObj.paginationEnd = temp;

			$scope.doServerPagination();
		}

		$scope.checkArrows = function (type) {
			if ($scope.paginationObj.paginationStart == 0 && type == 'back') {
				return true;
			}
			if ($scope.previewLovModel.length != 20 && type == 'next') {
				return true;
			}
		}

		$scope.moveToTree = function (item) {

			for (var i = 0; i < $scope.testLovTreeModel.length; i++) {
				if ($scope.testLovTreeModel[i].level == item.name) {
					return;
				}
			}
			var defObj = {};
			defObj.level = item.name;
			defObj.value = item.name;
			defObj.description = item.name;
			$scope.testLovTreeModel.push(defObj);
		}

		$scope.buildTestTable = function () {


			if ($scope.selectedLov != null) {
				var propName = $scope.selectedLov.itypeCd;
				var prop = lovProviderEnum[propName];
				if ($scope.selectedLov.lovProvider[prop].LOVTYPE == "" || $scope.selectedLov.lovProvider[prop].LOVTYPE == undefined) {
					$scope.selectedLov.lovProvider[prop].LOVTYPE = "simple";
				}
				$scope.treeListTypeModel = {};

				$scope.treeListTypeModel = $scope.selectedLov.lovProvider[prop];
				if ($scope.selectedLov.id != undefined) {
					console.log("we have existing one")
					$scope.formatedVisibleValues = $scope.treeListTypeModel['VISIBLE-COLUMNS'].split(",");
					$scope.formatedInvisibleValues = [];
					if (!$scope.treeListTypeModel.LOVTYPE || $scope.treeListTypeModel.LOVTYPE == 'simple') {
						$scope.formatedValues = $scope.treeListTypeModel['VALUE-COLUMN'].split(",");
						$scope.formatedDescriptionValues = $scope.treeListTypeModel['DESCRIPTION-COLUMN'].split(",");
					} else {
						$scope.formatedValues = $scope.treeListTypeModel['VALUE-COLUMNS'].split(",");
						$scope.formatedDescriptionValues = $scope.treeListTypeModel['DESCRIPTION-COLUMNS'].split(",");
					}


				} else {
					console.log("we have new one")
					$scope.treeListTypeModel.LOVTYPE = 'simple';
				}
				if ($scope.treeListTypeModel && ($scope.treeListTypeModel.LOVTYPE != 'simple' && $scope.treeListTypeModel.LOVTYPE != '')) {
					$scope.testLovTreeModel = [];
					//$scope.formatedTreeValues = $scope.treeListTypeModel['TREE-LEVELS-COLUMNS'].split(",");
					for (var i = 0; i < $scope.formatedValues.length; i++) {

						var defObj = {};
						defObj.level = $scope.formatedValues[i];
						defObj.value = $scope.formatedValues[i];
						defObj.description = $scope.formatedDescriptionValues[i];

						$scope.testLovTreeModel.push(defObj);
					}
				}
			}
			$scope.testLovModel = $scope.tableModelForTest;
			var newformatedVisibleValues = [];
			for (var i = 0; i < $scope.formatedVisibleValues.length; i++) {
				for (var j = 0; j < $scope.testLovModel.length; j++) {
					if ($scope.formatedVisibleValues[i] == $scope.testLovModel[j].name) {
						newformatedVisibleValues.push($scope.testLovModel[j].name)
					}
				}
			}
			$scope.formatedVisibleValues = newformatedVisibleValues;

		}
	};

})();