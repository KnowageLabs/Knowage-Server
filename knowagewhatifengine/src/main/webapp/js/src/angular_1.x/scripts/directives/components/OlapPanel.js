/*
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
		.module('olap_panel', ['sbiModule'])
		.directive(
				'olapPanel',
				function(sbiModule_config) {
					return {
						restrict : "E",
						replace : 'true',
						templateUrl : function(){
						return sbiModule_config.contextName + '/html/template/main/olap/olapPanel.html'
						},
						controller : olapPanelController
					}
				});

var downlf;
function olapPanelController($scope, $rootScope,$timeout, $window, $mdDialog, $http, $sce, $location,
		sbiModule_messaging, sbiModule_restServices, sbiModule_translate,
		toastr, $cookies,$localStorage, sbiModule_docInfo, sbiModule_config,sbiModule_download) {

	downlf = function(type) {
		$scope.exportOlap(type);
	}

	$scope.exportOlap = function(type) {
		var encoded = encodeURI('knowagewhatifengine/restful-services/1.0/model/export/excel?SBI_EXECUTION_ID='
				+ JSsbiExecutionID);
		if (type == "PDF") {
			encoded = encodeURI('knowagewhatifengine/restful-services/1.0/model/export/pdf?SBI_EXECUTION_ID='
					+ JSsbiExecutionID);
		}

		var protocol = $location.protocol();
		var host = $location.host();
		var port = $location.port();
		window.open(protocol + "://" + host + ":" + port + "/" + encoded);

	}

	$scope.drillDown = function(axis, position, member, uniqueName,
			positionUniqueName) {

		var data = JSON.stringify({
			memberUniqueName : uniqueName,
			positionUniqueName : positionUniqueName
		});

		var encoded = encodeURI('/member/drilldown/' + axis + '/' + position
				+ '/' + member + '/' + '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promisePost("1.0", encoded, data).then(
				function(response) {

					$scope.handleResponse(response);

				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.drillDown.error'), 'Error');

				});
	}

	$scope.drillUp = function(axis, position, memberPosition, memberUniqueName,
			positionUniqueName) {
		var toSend = {};
		toSend.axis = axis;
		toSend.position = position;
		toSend.memberPosition = memberPosition;
		toSend.positionUniqueName = positionUniqueName;
		toSend.memberUniqueName = memberUniqueName;

		var encoded = encodeURI('/member/drillup?SBI_EXECUTION_ID='
				+ JSsbiExecutionID);
		sbiModule_restServices.promisePost("1.0", encoded, toSend).then(
				function(response) {

					$scope.handleResponse(response);


				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.drillUp.error'), 'Error');

				});
	}

	$scope.swapAxis = function() {

		var encoded = encodeURI("1.0/axis/swap?SBI_EXECUTION_ID="
				+ JSsbiExecutionID);
		sbiModule_restServices.promisePost(encoded, "").then(
				function(response) {
					//var row  = $scope.modelConfig.startColumn;
					//var column = $scope.modelConfig.startRow;

					$scope.handleResponse(response);
					//$scope.modelConfig.startColumn = row;
					//$scope.modelConfig.startRow = column;
					//$scope.scrollTo(row,column);
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.swapAxis.error'), 'Error');

				});
	}

	$scope.exportDynamic = function() {


		if($scope.selectedVersion == null){
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.versionSlice.error'), 'Error');

		}else{
			var encoded = encodeURI("1.0/model/exceledit?SBI_EXECUTION_ID="+ JSsbiExecutionID);
			sbiModule_restServices.promiseGet(encoded,"").then(
					function(){
						sbiModule_download.getLink("/restful-services/"+encoded);
						},
					function(response){
							var errorResponse = response.data;
							if(errorResponse.errors){
								var errorMessagesObjects = errorResponse.errors;
								var errorMessage = "";
								for(var i in errorMessagesObjects){
									errorMessage += " " + errorMessagesObjects[i].localizedMessage;
									console.error(errorMessagesObjects[i])
								}
								sbiModule_messaging.showErrorMessage(errorMessage, sbiModule_translate.load('sbi.common.error'));
							}

						})

		}
	}


	$scope.getCollections = function() {

		var toSend = {};
		toSend.filters = angular.toJson($scope.filterCardList);

		var encoded = encodeURI('/member/drilltrough/levels/?SBI_EXECUTION_ID='
				+ JSsbiExecutionID);
		sbiModule_restServices.promisePost("1.0", encoded, toSend).then(
				function(response) {
					$scope.dtTree = response.data;
					setTimeout(function() {
						$scope.checkDtLevels();
					}, 500)

				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.dtLevels.error'), 'Error');

				});
	}
	$scope.checkDtLevels = function() {
		var tempArr = [];
		if ($scope.dtTree != null && $scope.formateddtColumns != null) {
			for (var i = 0; i < $scope.dtTree.length; i++) {
				for (var j = 0; j < $scope.dtTree[i].children.length; j++) {
					for (var k = 0; k < $scope.formateddtColumns.length; k++) {
						if ($scope.formateddtColumns[k].label == $scope.dtTree[i].children[j].caption
								.toUpperCase())
							tempArr.push($scope.dtTree[i].children[j]);

					}
				}
			}
		}

		else {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.dtLevelsChecking.error'), 'Error');
		}

		for (var i = 0; i < tempArr.length; i++) {
			$scope.checkCheckboxes(tempArr[i], $scope.dtAssociatedLevels);
		}
	}

	$scope.clearLevels = function() {
		$scope.dtAssociatedLevels.length = 0;
	}

	$scope.enableDisableDrillThrough = function(){
		$scope.modelConfig.enableDrillThrough = !$scope.modelConfig.enableDrillThrough;
		$scope.sendModelConfig($scope.modelConfig);
	}

	$scope.drillThrough = function(ordinal){
		if(ordinal){
			$scope.usedOrdinal = ordinal;
		}


if ($scope.dtAssociatedLevels.length == 0 && $scope.dtMaxRows == 0) {




			if($scope.showWarningDT){
				sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.dt.warning'), 'Warning');

			}

			var toSend = {};
			toSend.ordinal = ordinal;
			if (toSend.ordinal != undefined) {

				var encoded = encodeURI('/member/drilltrough?SBI_EXECUTION_ID='
						+ JSsbiExecutionID);
				sbiModule_restServices
						.promisePost("1.0", encoded, toSend)
						.then(
								function(response) {


									$scope.dtData = [];
									$scope.dtColumns = [];

									$scope.dtData = response.data;
									for ( var key in response.data[0]) {

										$scope.dtColumns.push(key);
									}

									$scope.formateddtColumns = $scope
											.formatColumns($scope.dtColumns);
									$scope.getCollections();

									$scope.openDtDialog();

								},
								function(response) {
									sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.dt.error'), 'Error');
								});
			} else {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.selectCell.error'), 'Error');
			}
		} else {


			console.log("from dialog");
			console.log($scope.usedOrdinal)
			var toSend = {};
			toSend.ordinal = $scope.usedOrdinal;
			toSend.levels = angular.toJson($scope.dtAssociatedLevels);
			toSend.max = $scope.dtMaxRows;
			var encoded = encodeURI('/member/drilltrough/full?SBI_EXECUTION_ID='
					+ JSsbiExecutionID);
			sbiModule_restServices.promisePost("1.0", encoded, toSend).then(
					function(response, ev) {
						$scope.dtData = [];
						$scope.dtColumns = [];
						$scope.dtData = angular.copy(response.data);
						for ( var key in response.data[0]) {

							$scope.dtColumns.push(key);
						}
						$scope.formateddtColumns = $scope
								.formatColumns($scope.dtColumns);
					},
					function(response) {
						sbiModule_messaging.showErrorMessage(
								response.data.errors[0].localizedMessage,
								'Error');

					});

		}


	}

	$scope.exportDrill = function(JSONData, ReportTitle, ShowLabel) {

		JSONData = angular.toJson(JSONData);

		// If JSONData is not an object then JSON.parse will parse the JSON
		// string in an Object
		var arrData = typeof JSONData != 'object' ? JSON.parse(JSONData)
				: JSONData;

		var CSV = '';
		// Set Report title in first row or line

		CSV += ReportTitle + '\r\n\n';

		// This condition will generate the Label/Header
		if (ShowLabel) {
			var row = "";

			// This loop will extract the label from 1st index of on array
			for ( var index in arrData[0]) {

				// Now convert each value to string and comma-seprated
				row += index + ',';
			}

			row = row.slice(0, -1);

			// append Label row with line break
			CSV += row + '\r\n';
		}

		// 1st loop is to extract each row
		for (var i = 0; i < arrData.length; i++) {
			var row = "";

			// 2nd loop will extract each column and convert it in string
			// comma-seprated
			for ( var index in arrData[i]) {
				row += '"' + arrData[i][index] + '",';
			}

			row.slice(0, row.length - 1);

			// add a line break after each row
			CSV += row + '\r\n';
		}

		if (CSV == '') {
			alert("Invalid data");
			return;
		}

		// Generate a file name
		var fileName = "MyReport_";
		// this will remove the blank-spaces from the title and replace it with
		// an underscore
		fileName += ReportTitle.replace(/ /g, "_");

		// Initialize file format you want csv or xls
		var uri = 'data:text/csv;charset=utf-8,' + escape(CSV);

		// Now the little tricky part.
		// you can use either>> window.open(uri);
		// but this will not work in some browsers
		// or you will not get the correct file extension

		// this trick will generate a temp <a /> tag
		var link = document.createElement("a");
		link.href = uri;

		// set the visibility hidden so it will not effect on your web-layout
		link.style = "visibility:hidden";
		link.download = fileName + ".csv";

		// this part will append the anchor tag and remove it after automatic
		// click
		document.body.appendChild(link);
		link.click();
		document.body.removeChild(link);
	}

	$scope.getProps = function(memberUniqueName) {

		var toSend = {};
		toSend.memberUniqueName = memberUniqueName;
		var encoded = encodeURI('/member/properties?SBI_EXECUTION_ID='
				+ JSsbiExecutionID);
		sbiModule_restServices
				.promisePost("1.0", encoded, toSend)
				.then(
						function(response) {
							console.log(response.data);
							$scope.propertiesArray = response.data;
							$mdDialog
									.show({
										scope : $scope,
										preserveScope : true,
										parent : angular.element(document.body),
										controllerAs : 'olapCtrl',
//										templateUrl : '/knowagewhatifengine/html/template/main/toolbar/properties.html',
										templateUrl : sbiModule_config.contextName + '/html/template/main/toolbar/properties.html',
										clickOutsideToClose : false,
										hasBackdrop : false
									});

						},
						function(response) {
							sbiModule_messaging
									.showErrorMessage(sbiModule_translate.load('sbi.olap.properties.error'), 'Error');

						});
	}

	$scope.formatColumns = function(array) {
		var arr = [];
		for (var i = 0; i < array.length; i++) {
			var obj = {};
			obj.label = array[i].toUpperCase();
			obj.name = array[i];
			obj.size = "100px";
			arr.push(obj);
		}
		console.log(arr);
		return arr;

	}

	$scope.switchPosition = function(data) {

		$scope.moveHierarchies(data.axis, data.selectedHierarchyUniqueName,
				data.positionInAxis + 1, 1, data);
		if (data.axis == 0) {
			var pom = $scope.columns[data.positionInAxis];
			var pia = data.positionInAxis;

			$scope.columns[pia].positionInAxis = pia + 1;
			$scope.columns[pia + 1].positionInAxis = pia;

			$scope.columns[pia] = $scope.columns[pia + 1];
			$scope.columns[pia + 1] = pom;

		} else if (data.axis == 1) {
			var pom = $scope.rows[data.positionInAxis];
			var pia = data.positionInAxis;

			$scope.rows[pia].positionInAxis = pia + 1;
			$scope.rows[pia + 1].positionInAxis = pia;

			$scope.rows[pia] = $scope.rows[pia + 1];
			$scope.rows[pia + 1] = pom;

		}

	};

	$scope.checkCheckboxes = function(item, list) {
		if (item.hasOwnProperty("caption")) {
			var index = $scope.indexInList(item, list);

			if (index != -1) {
				$scope.dtAssociatedLevels.splice(index, 1);
			} else {

				$scope.dtAssociatedLevels.push(item);
			}
		}
	};
	$scope.getCheckboxes = function(item, list) {

		return $scope.indexInList(item, list) > -1;
	}
	$scope.indexInList = function(item, list) {
		if (item.hasOwnProperty("caption")) {
			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if (object.caption == item.caption) {
					return i;
				}
			}
		}
		return -1;
	}

	/** dragan */
	/* writeback funtionality */
	/**
	 * @property {String} lastEditedFormula the last edited formula. To restore
	 *           the formula
	 */
	$scope.lastEditedFormula = null;

	/**
	 * @property {String} lastEditedCell the last edited formula. To restore the
	 *           formula
	 */
	$scope.lastEditedCell = null,

	$scope.cellValue = "";

	$scope.makeEditable = function(id, measureName) {

		var unformattedValue = "";
		var modelStatus = null;

		modelStatus = $scope.modelConfig.status;

		if (modelStatus == 'locked_by_other' || modelStatus == 'unlocked') {
			sbiModule_messaging.showErrorMessage(sbiModule_translate
					.load('sbi.olap.writeback.edit.no.locked'), 'Error');

			return;
		}
		if ($scope.selectedVersion == null){
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.versionSlice.error'), 'Error');
			return;

		}
		if ($scope.modelConfig && $scope.isMeasureEditable(measureName)) {

			var cell = angular.element(document.querySelector("[id='" + id
					+ "']"));

			// check if the user is editing the same cell twice. If so we present
			// again the last formula
			if (!String.prototype.startsWith) {
				  String.prototype.startsWith = function(searchString, position) {
				    position = position || 0;
				    return this.indexOf(searchString, position) === position;
				  };
				}
			var originalValue = "";
			originalValue = (cell[0].childNodes[0].data).trim();
			if ($scope.lastEditedFormula && $scope.lastEditedCell
					&& id.startsWith($scope.lastEditedCell)) {
				unformattedValue = $scope.lastEditedFormula;
				$scope.cellValue = $scope.lastEditedFormula
			} else {
				var type = "float";



				if (originalValue == '') { // in case the cell was empty, we type 0
					unformattedValue = 0;
				} else {
					unformattedValue = parseFloat(originalValue.replace(',', '.'));// Sbi.whatif.commons.Format.cleanFormattedNumber(originalValue,
					$scope.cellValue = "";														// Sbi.locale.formats[type]);
					console.log(originalValue);
					console.log(unformattedValue);
				}

				// Sbi.error("Error loading the value of the cell to edit" + err);

				// it's not possible to edit a cell with value 0
				if (unformattedValue == 0) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate
							.load('sbi.olap.writeback.edit.no.zero'), 'Error');
					return;
				}



			}
			$scope.showEditCell(cell, id, originalValue);

		}else{
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.measureEditable.error'), 'Error');
		}




	}

	/**
	 * checks if measure is editable
	 *
	 * @param measureName
	 *            the name of the measure to check
	 * @returns {Boolean} return if the measure is editable
	 */
	$scope.isMeasureEditable = function(measureName) {
		if ($scope.modelConfig && $scope.modelConfig.writeBackConf) {
			if ($scope.modelConfig.writeBackConf.editableMeasures == null
					|| $scope.modelConfig.writeBackConf.editableMeasures.length == 0) {
				return true;
			} else {
				var measures = ($scope.modelConfig.writeBackConf.editableMeasures);

			/*	for (measureNameCheck in measures) {
					if (measureNameCheck === measureName)
						;
					var contained = measureName;
					return contained;
				}*/

				for (var i = 0;i<measures.length;i++) {

					if (measures[i] === measureName){
						return true;
					}

				}

			}
			return false;
		}

	}
	$scope.writeBackCell = function(id, value, startValue, originalValue) {
		console.log("writeBackCell");
		var type = "float";
		if(value!==""){

			if($scope.isItStartWithOperator(value)){
				$scope.originalValue=$scope.originalValue.replace(/,/g , "");
				value =$scope.originalValue+value;
			}else if($scope.isItStarsWithEqual(value)){
				value = value.slice(1);
			}

			if (startValue) {
				startValue = parseFloat(startValue);// Sbi.whatif.commons.Format.cleanFormattedNumber(startValue,
													// Sbi.locale.formats[type]);
			}
			if (value != startValue) {
				var position = "";
				var unformattedValue = value;

				if (id) {
					var endPositionIndex = id.indexOf("!");
					position = id.substring(0, endPositionIndex);
				}

				if (!isNaN(value)) {
					// Value is a number
					unformattedValue = parseFloat(value);// Sbi.whatif.commons.Format.formatInJavaDouble(value,
															// Sbi.locale.formats[type]);
				} else {
					// Value is a string/expression
					unformattedValue = value;
				}

				// update the last edited values
				this.lastEditedFormula = unformattedValue;
				var separatorIndex = id.lastIndexOf('!');
				this.lastEditedCell = id.substring(0, separatorIndex);

				$scope.sendWriteBackCellService(position, unformattedValue);
			} else {
				/*
				 * Sbi.debug("The new value is the same as the old one"); var cell =
				 * Ext.get(id); cell.dom.childNodes[0].data = originalValue;
				 */
				console.log(originalValue);
			}
		}

	}

	$scope.showEditCell = function(cell, id, originalValue) {
		console.log(cell[0]);
		cell[0].style.setProperty('position', 'fixed', 'important');

		var textLength = (cell[0].childNodes[0].data).trim().length;
		var startVaue = cell[0].childNodes[0].data.trim();
		var textFontSize = cell[0].style.fontSize;
		console.log(textFontSize);
		var cellWidth = 250 + 12 * textLength;
		cell[0].style.setProperty('z-index', '500');
		cell.css('width', cellWidth);
		cell.css('transform', 'translatey(-14px)');
		$scope.id = id;
		$scope.startVaue = startVaue;
		$scope.originalValue = originalValue;
		$mdDialog
				.show({
					scope : $scope,
					parent : cell,
					preserveScope : true,
					controller : function DialogController($scope, $mdDialog) {
						$scope.closeDialog = function(e) {
							if (e.keyCode === 13) {
								$mdDialog.hide();

								$scope.writeBackCell($scope.id, $scope.cellValue,
										$scope.startVaue, $scope.originalValue);
							}

						}

					},
					template : "<md-dialog style='min-height: 30px;position: absolute;left: 0;top:0'><input md-autofocus ng-model='cellValue' type='text' style='width: 190px;transform:translateX(50px);' ng-keypress='closeDialog($event)'><input type='button'  ng-click='showFormulaDialog()' style='position:absolute;left:0px;top:0px' value='f(x)'></md-dialog>",
					//'/knowagewhatifengine/html/template/main/toolbar/writeBackCellSmall.html'
					onRemoving : function() {
						cell.css('width', 'inherit');
						cell[0].style.setProperty('position', 'relative',
								'important');
						cell[0].style.setProperty('z-index', '1');
						cell.css('transform', 'translatey(0px)');


					},
					clickOutsideToClose : true,
					autoWrap : false
				});




	}
	$scope.showFormulaDialog = function() {

		$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					controller : function DialogController($scope, $mdDialog) {
						$scope.sendFormula = function() {
							$scope.writeBackCell($scope.id, $scope.cellValue,
									$scope.startVaue, $scope.originalValue);

							$mdDialog.hide();
						}

						$scope.closeFormulaDialog = function(){
							$mdDialog.hide();
						}



					},
					controllerAs : 'olapCtrl',
//					templateUrl : '/knowagewhatifengine/html/template/main/toolbar/writeBackCell.html',
					templateUrl :  sbiModule_config.contextName + '/html/template/main/toolbar/writeBackCell.html',
					// targetEvent : ev,
					clickOutsideToClose : false,
					hasBackdrop : false,
					//locals:{originalValue:originalValue}
				});
	}

	$scope.sendWriteBackCellService = function(ordinal, expression) {

		var path = '/model/setValue/' + ordinal + '?SBI_EXECUTION_ID='
				+ JSsbiExecutionID;

		var st = {
			'expression' : expression
		};

		sbiModule_restServices.promisePost(
				"1.0/model/setValue/" + ordinal + "?SBI_EXECUTION_ID="
						+ JSsbiExecutionID, "", st).then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');

		});
	}

	$scope.isItStartWithOperator = function(value){
		value = value.toString();
		var firstChar = value.charAt(0);
		return firstChar === '+'||firstChar === '-'||firstChar === '*'||firstChar === '/';
	}

	$scope.isItStarsWithEqual = function(value){
		value = value.toString();
		var firstChar = value.charAt(0);
		return firstChar === '='
	}
	/******************************************************************************/

	$scope.dimensionShift = function(direction, isPanel) {
		if (direction == 'left') {
			if(isPanel) element= document.querySelector('.filter-panel');
			else element= document.querySelector('.top-axis-container');
			element.scrollLeft = element.scrollLeft - 50;
		}
		if (direction == 'right') {
			if(isPanel) element= document.querySelector('.filter-panel');
			else element= document.querySelector('.top-axis-container');
			element.scrollLeft = element.scrollLeft + 50;
		}
		if (direction == 'up') {
			element= document.querySelector('.left-axis');
			element.scrollTop = element.scrollTop - 50;
		}
		if (direction == 'down') {
			element= document.querySelector('.left-axis');
			element.scrollTop = element.scrollTop + 50;
		}

	}

	$scope.openDtDialog = function(ev) {
		$scope.dtAssociatedLevels = [];
		$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					controllerAs : 'olapCtrl',
//					templateUrl : '/knowagewhatifengine/html/template/main/toolbar/drillThrough.html',
					templateUrl : sbiModule_config.contextName + '/html/template/main/toolbar/drillThrough.html',
					targetEvent : ev,
					clickOutsideToClose : true

				});
	};
	$scope.closeDialog = function(ev) {


		$scope.dtData = [];
		$scope.dtAssociatedLevels = [];
		$scope.formateddtColumns = [];
		$scope.dtTree = [];
		var elem = document.getElementById("dtData_id");
		if(elem != undefined){
			elem.remove();
		}
		$scope.selectedCrossNavigation = null;
		cleanCC();
		$mdDialog.hide();
	};

	$scope.showCCWizard = function() {


		formulasSplit();
		cleanCC();

		$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					parent : angular.element(document.body),
					controllerAs : 'olapCtrl',
//					templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/calculatedFields.html',
					templateUrl : sbiModule_config.contextName + '/html/template/main/calculatedfields/calculatedFields.html',
					clickOutsideToClose : false,
					hasBackdrop : false
				});


	}

	$scope.checkValidity = function() {

		if ($scope.selectedMDXFunction.type == null
				|| $scope.selectedMDXFunctionName == "") {
			return true;
		} else {

			return false;
		}
	}

	$scope.cellClickCreateCrossNavigationMenu = function(ordinal) {
		var encoded = encodeURI('/crossnavigation/getCrossNavigationUrl/'
				+ ordinal + '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promisePost("1.0", encoded).then(
				function(response) {
					try {
						eval(response.data);
					} catch (e) {
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');
					}

				}, function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');
				});
	}

	$scope.checkValidityCrossNav = function() {

		if ($scope.selectedCrossNavigationDocument == null) {
			return true;
		} else {
			if ($scope.selectedCrossNavigationDocument.title == "") {
				return true;
			} else {
				return false;
			}
		}
	}

	$scope.selectMDXFunction = function(obj) {
		$scope.selectedMDXFunction = obj;
		console.log($scope.selectedMDXFunction);
	}

	$scope.openArgumentsdialog = function() {

		$scope.editArguments = $scope.allowEditingCC;
		$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					parent : angular.element(document.body),
					controllerAs : 'olapCtrl',
//					templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/argumentsDialog.html',
					templateUrl : sbiModule_config.contextName + '/html/template/main/calculatedfields/argumentsDialog.html',
					clickOutsideToClose : false,
					hasBackdrop : false
				});

	}

	$scope.openSavedSets = function() {

		$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					parent : angular.element(document.body),
					controllerAs : 'olapCtrl',
//					templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/savedSets.html',
					templateUrl : sbiModule_config.contextName + '/html/template/main/calculatedfields/savedSets.html',
					clickOutsideToClose : true,
					hasBackdrop : false
				});

	}

	$scope.formatValues = function(index, obj) {
		var value = null;
		if (obj.expected_value == "Set_Expression") {
			value = "{";
			if ($scope.members.length >= 1) {
				value += $scope.members[0].uniqueName;
			}
			for (var i = 1; i < $scope.members.length; i++) {
				value += "," + $scope.members[i].uniqueName;
			}
			value += "}"
			$scope.selectedMDXFunction.argument[index].default_value = value;

		} else if (obj.expected_value == "Level_Expression") {
			for (var i = 0; i < $scope.members.length; i++) {
				value = $scope.members[i].level;
			}
			$scope.selectedMDXFunction.argument[index].default_value = value;

		} else {
			for (var i = 0; i < $scope.members.length; i++) {
				value = $scope.members[i].uniqueName;
			}
			$scope.selectedMDXFunction.argument[index].default_value = value;
		}
		$scope.members = [];
		$scope.sendModelConfig($scope.modelConfig);
		return value;

	}

	$scope.enterSelectMode = function(index, obj) {
		$scope.selectedAgument = obj;
		$mdDialog.hide();
		toastr
				.info(
						'Click ok to finish selection<br /><br /><md-button class="md-raised">OK</md-button>',
						{
							allowHtml : true,
							timeOut : 0,
							extendedTimeOut : 0,

							onTap : function() {
								$scope.valuesArray.push($scope.formatValues(
										index, obj));

								$scope.openArgumentsdialog();
								toastr.clear();

							}

						});

	}

	$scope.hideNameInputCC = function(option) {
		if(option == "select"){
			$scope.hideName = true;
		}else{
			$scope.hideName = false;
		}

	}

	var formulasSplit = function() {

		$scope.aggregationArray = [];
		$scope.arithmeticArray = [];
		$scope.temporalArray = [];
		$scope.customArray = [];

		for (var i = 0; i < $scope.formulasData.length; i++) {

			switch ($scope.formulasData[i].type) {
			case "aggregation":
				$scope.aggregationArray.push($scope.formulasData[i]);
				break;
			case "arithmetic":
				$scope.arithmeticArray.push($scope.formulasData[i]);
				break;
			case "temporal":
				$scope.temporalArray.push($scope.formulasData[i]);
				break;
			case "custom":
				$scope.customArray.push($scope.formulasData[i]);
				break;
			default:
				break;
			}

		}
	}

	var formatFormulaforSending = function() {

		String.prototype.replaceAll = function(search, replace) {
		    if (replace === undefined) {
		        return this.toString();
		    }
		    return this.split(search).join(replace);
		}

		var tempString =  $scope.selectedMDXFunction.body;
		var finString = tempString;

			for (var i = 0; i < $scope.selectedMDXFunction.argument.length; i++) {

			if ($scope.selectedMDXFunction.argument[i].default_value != undefined) {

				finString = finString.replaceAll($scope.selectedMDXFunction.argument[i].name,$scope.selectedMDXFunction.argument[i].default_value);

			}
		}

		$scope.finalFormula = finString;

	}

	var cleanCC = function() {

		$scope.selectedMDXFunction = {};
		$scope.selectedMDXFunctionName = "";
		$scope.selectedTab = 0;
		$scope.members = [];
	}


	var doMemberSaving = function() {

		var toSend = {};
		toSend.calculatedFieldName = $scope.selectedMDXFunctionName;
		toSend.calculatedFieldFormula = $scope.finalFormula;
		toSend.parentMemberUniqueName = $scope.selectedMember.parentMember;
		toSend.axisOrdinal = $scope.selectedMember.axisOrdinal;
		toSend.hierarchyUniqueName = $scope.selectedMember.hierarchyUniqueName;
		toSend.formula = $scope.selectedMDXFunction;
		var encoded = encodeURI('/calculatedmembers?SBI_EXECUTION_ID='
				+ JSsbiExecutionID);
		sbiModule_restServices.promisePost("1.0", encoded, toSend).then(
				function(response) {
					$scope.handleResponse(response);

					var namedMember = {
						'docName' : sbiModule_docInfo.label,
						'name' : $scope.selectedMDXFunctionName,
						'value' : $scope.finalFormula,
						'type' : 'Member',
						'parentMemberUniqueName':$scope.selectedMember.parentMember,
						'formula' : $scope.selectedMDXFunction,
						'img' : sbiModule_config.contextName + "/img/m.png"
					}


					cleanCC();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.memberSave.success'), 'Success');

				},
				function(response) {
					cleanCC();
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.cc.error'), 'Error');

				});

	}

	var doSetSaving = function() {

		var namedSet = {
			'docName' : sbiModule_docInfo.label,
			'name' : $scope.selectedMDXFunctionName,
			'value' : $scope.finalFormula,
			'type' : 'Set',
			'formula' : $scope.selectedMDXFunction,
			'img' : sbiModule_config.contextName + "/img/s.png"

		}

		checkForDuplicates(namedSet);

		$scope.cookieArray.push(namedSet);

		cleanCC();
		sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.setSave.success'), 'Success');

	}

	$scope.sendCC = function() {

		formatFormulaforSending();

		if ($scope.selectedMDXFunction.output != "Set") {

			doMemberSaving();

		}

		else if ($scope.selectedMDXFunction.output == "Set") {

			doSetSaving();
		}

		$mdDialog.hide();

	}


	$scope.setIt = function(index, set) {

		$scope.selectedMDXFunction.argument[0].default_value = set.value;
		$scope.openArgumentsdialog();

	}

	/* function to call pop-up for editing saved members/sets */
	$scope.edit = function(item) {
		console.log("editing...");

		for (var i = 0; i < $scope.cookieArray.length; i++) {
			if (item.name === $scope.cookieArray[i].name) {
				console.log("same one in editing")
				$scope.selectedMDXFunction = item.formula;
				$scope.selectedMDXFunctionName = item.name;

				console.log($scope.selectedMDXFunction);
			}
		}

		$scope.openArgumentsdialog();
	}

	$scope.deleteFromCookie = function(index, item) {


		$scope.cookieArray.splice(index, 1);

		if (item.name != null) {
			$scope.deleteCC(item.name);
			$scope.selectedMDXFunction = {};
			sbiModule_messaging
					.showSuccessMessage(sbiModule_translate.load('sbi.common.delete.success'), 'Success');
		} else {
			console.log("cant delete name is null");
		}

	}

	$scope.deleteCC = function(calculateMemberName) {

		var toSend = {};
		toSend.calculatedFieldName = calculateMemberName;
		sbiModule_restServices.promisePost(
				"1.0",
				'/calculatedmembers/delete?SBI_EXECUTION_ID='
						+ JSsbiExecutionID, toSend).then(
				function(response) {

					$scope.handleResponse(response);
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.common.delete.error'), 'Error');

				});
	};

	$scope.hideSwitchIcon = function(position, axis) {
		var max = axis == 1 ? $scope.maxRows : $scope.maxCols;
		var last = axis == 1 ? $scope.maxRows + $scope.leftStart - 1
				: $scope.maxCols + $scope.topStart - 1
		var length = axis == 1 ? $scope.rows.length : $scope.columns.length;

		if (position == length - 1)
			return true;
		if (length > max && last == position)
			return true;

		return false;

	}



};
