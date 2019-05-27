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

(function() {
	angular.module('cockpitModule')
	.directive('cockpitTableWidget',function(cockpitModule_widgetServices,$mdDialog){
		return{
			templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/tableWidget/templates/tableWidgetTemplate.html',
			controller: cockpitTableWidgetControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
						element[0].classList.add("flex");
						element[0].classList.add("layout-column");
						element[0].classList.add("layout-fill");
					},
					post: function postLink(scope, element, attrs, ctrl, transclud) {
						//init the widget
						element.ready(function () {
							scope.initWidget();
						});
					}
				};
			}
		}
	})

	function cockpitTableWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdToast,
			$timeout,
			$mdPanel,
			$q,
			$filter,
			sbiModule_user,
			sbiModule_translate,
			sbiModule_restServices,
			cockpitModule_datasetServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties,
			accessibility_preferences){

		$scope.accessibilityModeEnabled = accessibility_preferences.accessibilityModeEnabled;
		if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
			$scope.ngModel.dataset.isRealtime = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).isRealtime;
		}

		$scope.selectedTab = {'tab' : 0};
		$scope.widgetIsInit=false;
		$scope.totalCount = 0;
		$scope.translate = sbiModule_translate;
		$scope.user = sbiModule_user;
		$scope.datasetRecords = {};

		var scope = $scope;

		$scope.realTimeSelections = cockpitModule_widgetServices.realtimeSelections;
		//set a watcher on a variable that can contains the associative selections for realtime dataset
		var realtimeSelectionsWatcher = $scope.$watchCollection('realTimeSelections',function(newValue,oldValue,scope){
			if(scope.ngModel && scope.ngModel.dataset && scope.ngModel.dataset.dsId){
				var dataset = cockpitModule_datasetServices.getDatasetById(scope.ngModel.dataset.dsId);
				if(dataset.isRealtime && dataset.useCache){
					if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1 ){
		                cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
		            }
					if(newValue != oldValue && newValue.length > 0){
						scope.itemList = scope.filterDataset(angular.copy(scope.savedRows),scope.reformatSelections(newValue));
					}else{
						angular.copy(scope.savedRows, scope.itemList);
					}
				}
			}
		});

		if(!$scope.ngModel.settings){
			$scope.ngModel.settings = {};
		}

		if(!$scope.ngModel.settings.backendTotalRows){
			$scope.ngModel.settings.backendTotalRows = 0;
		}

		if(!$scope.ngModel.settings.alternateRows){
			$scope.ngModel.settings.alternateRows = {
				'enabled' : false,
				'evenRowsColor':'',
				'oddRowsColor': ''
			};
		}

		if(!$scope.ngModel.settings.showGrid){
			$scope.ngModel.settings.showGrid = false;
		}

		$scope.ngModel.settings.page = 1;
		$scope.ngModel.settings.rowsCount = 0;

		if(!$scope.ngModel.settings.pagination){
			$scope.ngModel.settings.pagination = {
				'enabled': true,
				'itemsNumber': 10,
				'frontEnd': false
			};
		}

		if($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.isRealtime){
			$scope.ngModel.settings.pagination.frontEnd = true;
		}

		if($scope.ngModel.settings.multiselectable==undefined){
			$scope.ngModel.settings.multiselectable=false;
		}

		if(!$scope.ngModel.settings.summary){
			$scope.ngModel.settings.summary={
					'enabled': false,
					'forceDisabled': false,
					'style': {}
			};
		}

		if(!$scope.ngModel.style){
			$scope.ngModel.style={};
		}

		if(!$scope.ngModel.cross){
			$scope.ngModel.cross={};
		}

		$scope.getKeyByValue = function( obj,value ) {
		    for( var prop in obj ) {
		        if( obj.hasOwnProperty( prop ) ) {
		             if( obj[ prop ] === value ){
		                 return prop;
		             }
		        }
		    }
		}

		$scope.tableFunction = {
				widgetStyle: $scope.ngModel.style
		}

		//checking the model version. If is a previous version I create the newer settings.
		$scope.retroCompatibilityCheckToVersion1 = function() {
			if($scope.ngModel.version == undefined){
				var columns = $scope.ngModel.content.columnSelectedOfDataset;
				for(var k in columns){
					if(!columns[k].style) columns[k].style = {};
					$scope.moveProperty(columns[k],"style.fontSize","style.font-size");
					$scope.moveProperty(columns[k],"style.fontWeight","style.font-weight");
					if(!columns[k].text) columns[k].text = {"enabled":true};
					$scope.moveProperty(columns[k], "style.size","style.width");
					if(columns[k].style.textAlign == "Left" ) columns[k].style.td = {'justify-content' : "flex-start"};
					if(columns[k].style.textAlign == "Center" ) columns[k].style.td = {'justify-content' : "center"};
					if(columns[k].style.textAlign == "Right" )columns[k].style.td = {'justify-content' : "flex-end"};
					if(columns[k].style.scopefunc && columns[k].scopefunc.condition){
						columns[k].ranges = [];
						for(var j in columns[k].scopefunc.condition){
							var range = {
									"operator" 	: columns[k].scopefunc.condition[j].condition,
									"value"		: columns[k].scopefunc.condition[j].value,
									"icon"		: columns[k].scopefunc.condition[j].icon,
									"iconColor"	: columns[k].scopefunc.condition[j].color
							};
							columns[k].ranges.push(range);

						}
					}
					if(columns[k].visType=='Chart'|| columns[k].visType=='Chart & Text' || columns[k].visType== 'Text & Chart'){
						columns[k].barchart = {
							"minValue": columns[k].minValue,
							"maxValue": columns[k].maxValue,
							"style": {
								"background-color": columns[k].chartColor
							}
						}
					}
				}

				$scope.deleteProperty($scope.ngModel, "content.currentPageNumber");
				$scope.deleteProperty($scope.ngModel, "style.disableShowSummary");

				if($scope.getPropertyValue($scope.ngModel, "content.fixedRow") != undefined){
					$scope.deleteProperty($scope.ngModel, "content.fixedRow");
					$scope.setPropertyValue($scope.ngModel, "settings.pagination.enabled", true);
				}

				$scope.moveProperty($scope.ngModel, "style.alternateRows", "settings.alternateRows");
				$scope.moveProperty($scope.ngModel, "style.showAlternateRows", "settings.alternateRows.enabled");
				$scope.moveProperty($scope.ngModel, "style.autoRowsHeight", "settings.autoRowsHeight");
				$scope.moveProperty($scope.ngModel, "content.modalselectioncolumn", "settings.modalSelectionColumn");
				$scope.moveProperty($scope.ngModel, "multiselectable", "settings.multiselectable");
				$scope.moveProperty($scope.ngModel, "content.maxRowsNumber", "settings.pagination.itemsNumber");
				var itemsNumber = $scope.getPropertyValue($scope.ngModel, "settings.pagination.itemsNumber");
				if(itemsNumber != undefined && itemsNumber < 1){
					$scope.setPropertyValue($scope.ngModel, "settings.pagination.itemsNumber", 10);
				}
				$scope.moveProperty($scope.ngModel, "style.showGrid", "settings.showGrid");
				$scope.moveProperty($scope.ngModel, "sortingColumn", "settings.sortingColumn");
				$scope.moveProperty($scope.ngModel, "sortingOrder", "settings.sortingOrder");
				$scope.moveProperty($scope.ngModel, "style.showSummary", "settings.summary.enabled");
				$scope.moveProperty($scope.ngModel, "style.rowsHeight", "style.tr.height");

				$scope.moveProperty($scope.ngModel, "style.grid", "style.td");
				$scope.moveProperty($scope.ngModel, "style.headerStyle.fontfamily", "style.th.font-family");
				$scope.moveProperty($scope.ngModel, "style.headerStyle.fontsize", "style.th.font-size");
				$scope.moveProperty($scope.ngModel, "style.headerStyle.fontweight", "style.th.font-weight");
				$scope.moveProperty($scope.ngModel, "style.headerStyle.color", "style.th.color");
				$scope.moveProperty($scope.ngModel, "style.headerStyle.background", "style.th.background-color");
				$scope.moveProperty($scope.ngModel, "style.headerStyle.textalign", "style.th.text-align");
				$scope.deleteProperty($scope.ngModel, "style.headerStyle");
				$scope.moveProperty($scope.ngModel, "style.summary.background", "style.summary.background-color");


				$scope.ngModel.version = 1;
			}
		}

		$scope.retroCompatibilityCheckToVersion1();

		$scope.selectRow=function(row,column,evt){
			cockpitModule_widgetSelection.setWidgetOfType("table");
			
			var newValue = undefined;
			
			// Dataset preview
			if ($scope.ngModel.cross && $scope.ngModel.cross.preview && $scope.ngModel.cross.preview.enable) {
				switch ($scope.ngModel.cross.preview.previewType) {
				case 'allRow':
					previewDataset(row, column);
					break;
				case 'singleColumn':
					if (column.aliasToShow == $scope.ngModel.cross.preview.column)
						previewDataset(row, column);
					break;
				case 'icon':
					previewDataset(row, column);
					break;
				}
				return;				
			}
			
			for(var i=0;i<$scope.ngModel.content.columnSelectedOfDataset.length;i++){
				if($scope.ngModel.settings.modalSelectionColumn!=undefined)	{
					if($scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow==$scope.ngModel.settings.modalSelectionColumn)	{
						if(Object.prototype.toString.call( row ) === '[object Array]'){
							newValue = [];
							for(var k in row){
								newValue.push(row[k][$scope.ngModel.settings.modalSelectionColumn]);
							}
						}else{
							newValue=row[$scope.ngModel.settings.modalSelectionColumn];
						}
						break;
					}
				}

				if(!$scope.ngModel.cross.cross && angular.equals($scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow,column.aliasToShow)){
					if($scope.ngModel.content.columnSelectedOfDataset[i].fieldType=="MEASURE"
							|| $scope.ngModel.content.columnSelectedOfDataset[i].isCalculated){
						return;
					}
					//break;
				}
			}

			if(Object.prototype.toString.call( row ) === '[object Array]'){
				var valuesArray = [];
				for(var k in row){
					valuesArray.push(row[k][column.aliasToShow]);
				}
				$scope.doSelection(column.aliasToShow, valuesArray, $scope.ngModel.settings.modalSelectionColumn, newValue, row);
			}else{
				$scope.doSelection(column.aliasToShow, row[column.aliasToShow], $scope.ngModel.settings.modalSelectionColumn, newValue, row);
			}
		}
		
		var previewDataset = function(row, column) {
			if ($scope.ngModel.cross.preview.parameters && 
				    (angular.isArray($scope.ngModel.cross.preview.parameters) && $scope.ngModel.cross.preview.parameters.length > 0)) {
				newValue = $scope.ngModel.cross.preview.parameters;
				$scope.doSelection(column.aliasToShow, row[column.aliasToShow], newValue, undefined, row);
			} else if ($scope.ngModel.cross.preview.column && $scope.ngModel.cross.preview.column != "") {
				// if modal column is selected
				newValue = row[$scope.ngModel.cross.preview.column];
				$scope.doSelection(column.aliasToShow, row[column.aliasToShow], $scope.ngModel.cross.preview.column, newValue, row);
			} else {
				// previewing common Dataset, without parameters
				$scope.doSelection(column.aliasToShow, row[column.aliasToShow], undefined, undefined, row);
			}	
		}

		$scope.calculatedRow = function(row,column,alias){

			var index = $scope.indexInList(alias, $scope.ngModel.content.columnSelectedOfDataset);

			if(index != -1){
				var formulaArray = $scope.ngModel.content.columnSelectedOfDataset[index].formula;
				var value = "";

				for(var i=0;i<formulaArray.length;i++){
					var obj = formulaArray[i];
					if(obj.type == 'measure'){
						value = value+column[obj.value];
					} else{
						value = value + obj.value;
					}
				}
				column[alias] = ""+$scope.$eval(value);
				row = column[alias];
				return row;
			}
		}

		$scope.indexInList=function(item, list) {
			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.aliasToShow==item){
					return i;
				}
			}
			return -1;
		};

		$scope.lastChangePageConf={};

		$scope.changeDocPage = function(searchValue, itemsPerPage, currentPageNumber, columnsSearch, columnOrdering, reverseOrdering){
			if($scope.gridsterItem.hasClass("gridster-item-resizing")
					|| !$scope.widgetIsInit
					|| itemsPerPage == 0){
				return;
			}
			if($scope.ngModel
					&& $scope.ngModel.settings.page == currentPageNumber
					&& $scope.datasetRecords
					&& $scope.datasetRecords.rows
					&& $scope.datasetRecords.rows.length == itemsPerPage
					&& $scope.columnOrdering == columnOrdering
					&& $scope.reverseOrdering == reverseOrdering){
				return;
			}

			var time=(new Date()).getTime();
			$scope.lastChangePageConf=time;
			$timeout(function(){
				if(angular.equals(time,	$scope.lastChangePageConf)){

					if(searchValue==undefined || searchValue.trim().lenght==0 ){
						searchValue='';
					}

					var numberOfElement = angular.copy(itemsPerPage);
					if($scope.ngModel.content.maxRowsNumber != undefined){
						numberOfElement = angular.copy($scope.ngModel.content.maxRowsNumber)
					}

					if($scope.ngModel.settings.summary.enabled){
						numberOfElement--;
					}

					currentPageNumber--;
					$scope.ngModel.settings.page = currentPageNumber;

					$scope.columnOrdering = columnOrdering;
					$scope.reverseOrdering = reverseOrdering;

					var options = {
						page: currentPageNumber,
						itemPerPage: numberOfElement,
						columnOrdering: columnOrdering,
						reverseOrdering: reverseOrdering,
						type: $scope.ngModel.type
					};
					$scope.refreshWidget(options);
				}
			},1000);

		};

		$scope.isMobile = {
		    Android: function() {
		        return navigator.userAgent.match(/Android/i);
		    },
		    BlackBerry: function() {
		        return navigator.userAgent.match(/BlackBerry/i);
		    },
		    iOS: function() {
		        return navigator.userAgent.match(/iPhone|iPad|iPod/i);
		    },
		    Opera: function() {
		        return navigator.userAgent.match(/Opera Mini/i);
		    },
		    Windows: function() {
		        return navigator.userAgent.match(/IEMobile/i);
		    },
		    any: function() {
		        return ($scope.isMobile.Android() || $scope.isMobile.BlackBerry() || $scope.isMobile.iOS() || $scope.isMobile.Opera() || $scope.isMobile.Windows());
		    }
		};

		$scope.canSeeColumnByMobile = function(obj){
			if(obj!=undefined && (obj.hiddenColumn == true || ($scope.isMobile.any()!=null && obj.hideonMobile == true))){
				return false;
			}
			return true;
		}


		$scope.freeValueFromPrefixAndSuffix=function(value,currentColumn){
			if(currentColumn.hasOwnProperty("style") && value!=undefined){
				if(currentColumn.style.hasOwnProperty("suffix")){
					value=value.replace(" "+currentColumn.style.suffix, "");
				}
				if(currentColumn.style.hasOwnProperty("prefix")){
					value=value.replace(currentColumn.style.prefix+" ", "");
				}
			}
			return parseFloat(value);
		}



		$scope.refresh=function(element,width,height, datasetRecords,nature){
			if(nature == 'gridster-resized' || nature == 'fullExpand' || nature == 'resize'){
				return;
			}
			$scope.columnsToShow = [];
			$scope.columnToshowinIndex = [];
			$scope.tableFunction.widgetStyle = $scope.ngModel.style;
			$scope.datasetRecords = datasetRecords;
			if($scope.columnWatcher){$scope.columnWatcher};

			$scope.columnWatcher = $scope.$watchCollection('ngModel.content.columnSelectedOfDataset',function(newValue,oldValue){
				$scope.getColumns(newValue);
			})
			var calculateScaleValue=function(minVal, maxVal, val)
			{
				if(maxVal!=minVal)
				{
					return ((val-minVal)/(maxVal-minVal))*100;
				}
				else
				{
					return 0;
				}
			}

			$scope.rgbToHex=function(rgbColor)
			{
				var a = rgbColor.split("(")[1].split(")")[0];
				a = a.split(",");
				var b = a.map(function(x){             //For each array element
				    x = parseInt(x).toString(16);      //Convert to a base16 string
				    return (x.length==1) ? "0"+x : x;  //Add zero if we get only one character
				})
				b = "0x"+b.join("");
				return b;
			}

			$scope.itemList = $scope.getRows($scope.datasetRecords);

			if($scope.datasetRecords){
				$scope.ngModel.settings.backendTotalRows = $scope.datasetRecords.results;
			}
			if(nature == 'init'){
				$timeout(function(){
					$scope.widgetIsInit=true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				},500);
			}
		}

		// reformatting the filter object to have an easier access on it
		$scope.reformatFilters = function(){
			var filters = {};
			for(var f in $scope.ngModel.filters){
				if($scope.ngModel.filters[f].filterVals.length > 0){
					var columnObject = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,$scope.ngModel.filters[f].colName);
					var aliasToShow = columnObject.aliasToShow;
					filters[aliasToShow] = {
						"type":columnObject.fieldType,
						"values":$scope.ngModel.filters[f].filterVals,
						"operator":$scope.ngModel.filters[f].filterOperator
					};
				}
			}
			return filters;
		};

		// filtering the table for realtime dataset
		$scope.filterDataset = function(dataset,selection){
			//using the reformatted filters
			var filters = selection ? selection : $scope.reformatFilters();
			for(var f in filters){

				for(var d = dataset.length - 1; d >= 0; d--){
					//if the column is an attribute check in filter
					if (filters[f].type == 'ATTRIBUTE'){
						var value = dataset[d][f];
						if(typeof value == "number"){
							value = String(value);
						}
						if (filters[f].values.indexOf(value)==-1){
							dataset.splice(d,1);
						}
					//if the column is a measure cast it to number and check in filter
					} else if (filters[f].type == 'MEASURE'){
						var columnValue = Number(dataset[d][f]);
						var filterValue = filters[f].values.map(function (x) {
						    return Number(x);
						});
						//check operator
						var operator = String(filters[f].operator);
						if (operator == "="){
							operator = "==";
						}
						var leftOperand = String(columnValue);
						var rightOperand = String(filterValue[0]);
						var expression =  leftOperand + operator + rightOperand;


						//if (filterValue.indexOf(columnValue)==-1){
						if (eval(expression) == false){
							dataset.splice(d,1);
						}
					}else if (filters[f].type == 'SPATIAL_ATTRIBUTE'){
						var value = dataset[d][f];

						if (filters[f].values.indexOf(value)==-1){
							dataset.splice(d,1);
						}
					}
				}
			}
			return dataset;
		}

		//reformatting the selections to have the same model of the filters
		$scope.reformatSelections = function(realTimeSelections){
			if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
				var widgetDatasetId = $scope.ngModel.dataset.dsId;
				var widgetDataset = cockpitModule_datasetServices.getDatasetById(widgetDatasetId)

				for (var i=0; i< realTimeSelections.length; i++){
					//search if there are selection on the widget's dataset
					if (realTimeSelections[i].datasetId == widgetDatasetId){
						var selections = realTimeSelections[i].selections;
						var formattedSelection = {};
						var datasetSelection = selections[widgetDataset.label];
						for(var s in datasetSelection){
							var columnObject = scope.getColumnObjectFromName(scope.ngModel.content.columnSelectedOfDataset,s);
							if (!columnObject){
								columnObject = scope.getColumnObjectFromName(widgetDataset.metadata.fieldsMeta,s);
							}

							formattedSelection[columnObject.aliasToShow || columnObject.alias] = {"values":[], "type": columnObject.fieldType};
							for(var k in datasetSelection[s]){
								// clean the value from the parenthesis ( )
								if (columnObject.fieldType == "SPATIAL_ATTRIBUTE") {
									//for spatial attribute doens't split value
									var x = datasetSelection[s][k].replace(/[()]/g, '').replace(/['']/g, '');
									formattedSelection[columnObject.aliasToShow || columnObject.alias].values.push(x);
								}else{
									var x = datasetSelection[s][k].replace(/[()]/g, '').replace(/['']/g, '').split(/[,]/g);
									for(var i=0; i<x.length; i++){
										formattedSelection[columnObject.aliasToShow || columnObject.alias].values.push(x[i]);
									}
								}
							}
						}
					}
				}
				return formattedSelection;
			}
		}

		//function to get the rows of the table. Is used on every refresh
		$scope.getRows = function(values){
			var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			var table = [];
			if (values != undefined){
				var headerMap = {};
				for(var k=0; k<values.metaData.fields.length; k++){
					var field = values.metaData.fields[k];

					//changing the column naming to aliasToShow in case of realtime dataset
					if(dataset.isRealtime === true){
						for(var n in $scope.ngModel.content.columnSelectedOfDataset){
							if($scope.ngModel.content.columnSelectedOfDataset[n].name == field.header){
								field.header = $scope.ngModel.content.columnSelectedOfDataset[n].aliasToShow;
								break;
							}
						}
					}
					if(field.dataIndex){
						headerMap[field.dataIndex] = field.header;
					}
				}

				for(var j in values.rows){
					var obj = {};
					for(var i in values.rows[j]){
						var header = headerMap[i];
						if(header){
							obj[header] = values.rows[j][i];
						}
					}
					table.push(obj);
				}

				// deleting last row to move the summary row outsise in its footer container.
				if($scope.ngModel.settings.summary.enabled){
					var lastIndex = values.rows.length-1;
					$scope.ngModel.settings.summary.row = table[lastIndex];
					table.splice(lastIndex,1);
				}

				// realtime dataset filtering
				if(dataset.isRealtime === true){
					table = $scope.filterDataset(table);
					$scope.savedRows = [];
					angular.copy(table,$scope.savedRows);

					if ($scope.realTimeSelections.length > 0 ) {
						table = $scope.filterDataset(table,scope.reformatSelections($scope.realTimeSelections));
					}

				}
			}
			return table;
		}

		/**
		 * Returns the column object that satisfy the original name (not aliasToShow) passed as argument
		 */
		$scope.getColumnObjectFromName = function(columnSelectedOfDataset, originalName){
			for (i = 0; i < columnSelectedOfDataset.length; i++){
				if (columnSelectedOfDataset[i].name === originalName){
					return columnSelectedOfDataset[i];
				}
			}
		}

		$scope.presentInTable = function(table, obj){
			for(var i=0;i<table.length;i++){
				var objT = table[i];
				if(objT == obj){
					return true
				}
			}
			return false;
		}
		$scope.tableColumns=$scope.ngModel.content.columnSelectedOfDataset;
		$scope.itemList=[];

		$scope.showAction = function(text) {
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top')

			$mdToast.show(toast).then(function(response) {

				if ( response == 'ok' ) {


				}
			});
		}

		$scope.init=function(element,width,height){
			$scope.refreshWidget(null, 'init');

		}

		$scope.getColumns =function(newValues){
			var columns = newValues ? newValues : $scope.ngModel.content.columnSelectedOfDataset;
			for(var k in columns){
				if(columns[k].visType == "Text" || !columns[k].visType) {
					delete columns[k].barchart;
					if(!columns[k].text || !columns[k].text.enabled){
						columns[k].text = {"enabled":true};
					}
				}else if(columns[k].visType == "Chart") {
					if(!columns[k].barchart || !columns[k].barchart.enabled) columns[k].barchart = {'enabled':true};
					columns[k].barchart.maxValue = columns[k].barchart.maxValue ? columns[k].barchart.maxValue : 100;
					delete columns[k].text;
				}else if(columns[k].visType == "Text & Chart") {
					if(!columns[k].barchart || !columns[k].barchart.enabled) columns[k].barchart = {'enabled':true};
					columns[k].barchart.maxValue = columns[k].barchart.maxValue ? columns[k].barchart.maxValue : 100;
					if(!columns[k].text || !columns[k].text.enabled) columns[k].text = {"enabled":true};
				}
			}
		}



		$scope.getOptions =function(){
			var obj = {};

			if(!$scope.ngModel.settings.pagination.enabled || $scope.ngModel.settings.pagination.frontEnd){
				obj["page"] = -1;
				obj["itemPerPage"] = -1;
			}else{
				obj["page"] = $scope.ngModel.settings.page ? $scope.ngModel.settings.page - 1 : 0;
				obj["itemPerPage"] = $scope.ngModel.settings.pagination ? $scope.ngModel.settings.pagination.itemsNumber : -1;
			}

			obj["columnOrdering"] = { name: $scope.ngModel.settings.sortingColumn };
			obj["reverseOrdering"] = ($scope.ngModel.settings.sortingOrder == 'ASC');

			obj["type"] = $scope.ngModel.type;

			return obj;

		}

		$scope.exportCsv=function(obj){
			var deferred = obj.def;
			var csv = '';
			var metas = this.ngModel.content.columnSelectedOfDataset;
			for(var k = 0; k < metas.length; k++){
				csv += metas[k].aliasToShow + ';';
			}
			csv += '\n';
			var datasetId = this.ngModel.dataset.dsId;
			var model = {content: {columnSelectedOfDataset: this.ngModel.content.columnSelectedOfDataset}};
			cockpitModule_datasetServices.loadDatasetRecordsById(datasetId, undefined, undefined, undefined, undefined, model).then(function(allDatasetRecords){
				obj.csvData = {};
				var allRows = $scope.getRows($scope.columnToshowinIndex, allDatasetRecords);
				var rows = allDatasetRecords.rows;
				allDatasetRecords = null;
				var numRecs = rows.length;
				for(var recIndex = 0; recIndex < numRecs; recIndex++){
					for(var col = 0; col < $scope.columnToshowinIndex.length; col++){
						csv += rows[recIndex][$scope.columnToshowinIndex[col]] + ';'
					}
					csv += '\n';
				}
				obj.csvData = csv;
				deferred.resolve(obj);
			},function(error){
				deferred.reject(error);
			});
		}

		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: tableWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/tableWidget/templates/tableWidgetEditPropertyTemplate.html',
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: true,
					autoWrap:false,
					locals: {finishEdit: finishEdit, originalModel: $scope.ngModel, getMetadata: $scope.getMetadata, scopeFather: $scope},

			};

			$mdPanel.open(config);

			return finishEdit.promise;
		}

		$scope.$watch('ngModel.settings.pagination.frontEnd', function(newValue, oldValue) {
			if(newValue != undefined){
				if(newValue){ // deregistering backend watchers
					if($scope.watchPagingForBackend){
						$scope.watchPagingForBackend();
					}

					if($scope.watchSortingForBackend){
						$scope.watchSortingForBackend();
					}
				}else{ // registering backend watchers
					$scope.watchPagingForBackend = $scope.$watch('ngModel.settings.pagination.enabled + "," + ngModel.settings.pagination.itemsNumber + "," + ngModel.settings.page', function(newValue, oldValue) {
						if(newValue != undefined && newValue != oldValue){
							$scope.refreshWidget();
						}
					});

					$scope.watchSortingForBackend = $scope.$watch('ngModel.settings.sortingColumn + "," + ngModel.settings.sortingOrder', function(newValue, oldValue) {
						if(newValue != undefined && newValue != oldValue && newValue.indexOf(",") > 0 && newValue.indexOf(",") < newValue.length - 1){
							$scope.refreshWidget();
						}
					});
				}
			}
		});
	};

	function tableWidgetEditControllerFunction($scope,finishEdit,sbiModule_translate,$mdDialog,originalModel,mdPanelRef,getMetadata,scopeFather,$mdToast, sbiModule_user){
		$scope.translate=sbiModule_translate;

		$scope.fontFamily = ["Inherit","Roboto","Arial","Times New Roman","Tahoma","Verdana","Impact","Calibri","Cambria","Georgia","Gungsuh"],

		$scope.fontWeight = ['normal','bold','bolder','lighter','initial','inherit'];

		$scope.textAlign = ['left','right','center'];

		$scope.getMetadata = getMetadata;

		$scope.user = sbiModule_user;

		$scope.model = {};
		angular.copy(originalModel,$scope.model);

		$scope.toggleTh = function(){
			$scope.colorPickerPropertyTh.disabled = $scope.model.style.th.enabled;
		}

		$scope.toggleSummary = function(){
			$scope.summaryColorPickerProperty.disabled = $scope.model.settings.summary.enabled;
		}
		
		$scope.colorPickerPropertyTh = {format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select'), disabled:($scope.model.style.th && $scope.model.style.th.enabled === false)}

		$scope.colorPickerProperty={format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select')};
		$scope.summaryColorPickerProperty = {placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:!$scope.model.settings.summary.enabled};

		$scope.colorPickerPropertyEvenOddRows = {placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:!$scope.model.settings.alternateRows.enabled};

		$scope.colorPickerPropertyGrid = {placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:!$scope.model.settings.showGrid};

		$scope.saveConfiguration=function(){
			if($scope.model.dataset == undefined || $scope.model.dataset.dsId == undefined ){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
				return;
			}
			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
				return;
			}
			if(!$scope.checkAggregation()){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.erroraggregation'));
				return;
			}
			if(!$scope.checkAliases()){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.erroraliases'));
				return;
			}
//			if(!$scope.checkFilters()){
//				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorfilters'));
//				return;
//			}

			angular.copy($scope.model,originalModel);
			mdPanelRef.close();
			mdPanelRef.destroy();

			if(!scopeFather.ngModel.isNew){
				scopeFather.refreshWidget();
			}
			$scope.$destroy();
			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
			}
			$scope.watchColumnSelectedOfDataset();
			finishEdit.resolve();
		}

		$scope.enableAlternate = function(){
			$scope.colorPickerPropertyEvenOddRows['disabled'] = $scope.model.settings.alternateRows.enabled;
		}

		$scope.enableGrid = function(){
			if($scope.model.settings.showGrid){
				delete $scope.model.style.td['border-color'];
				delete $scope.model.style.td['border-width'];
				delete $scope.model.style.td['border-style'];
			}
			$scope.colorPickerPropertyGrid['disabled'] = $scope.model.settings.showGrid;

		}

		$scope.checkAggregation = function(){
			var measures =0;
			var noneAggr =0;
			for(var i=0;i<$scope.model.content.columnSelectedOfDataset.length;i++){
				var column = $scope.model.content.columnSelectedOfDataset[i];
				if(column.fieldType == 'MEASURE'){
					measures++;
					if(column.aggregationSelected == 'NONE'){
						noneAggr++;
					}
				}
			}
			if(noneAggr!=0){
				if(noneAggr != measures){
					return false;
				}
			}
			return true;
		}

		$scope.checkAliases = function(){
			var columns = $scope.model.content.columnSelectedOfDataset;
			for(var i = 0; i < columns.length - 1; i++){
				for(var j = i + 1; j < columns.length; j++){
					if(columns[i].aliasToShow == columns[j].aliasToShow){
						return false;
					}
				}
			}
			return true;
		}



//		check if right number of operands have been specified depending on operator type
		$scope.checkFilters = function(){
			var filters = $scope.model.filters;

			var oneOperandOperator = ['=','!=','like','<','>','<=','>='];
			var twoOperandOperator = ['range'];
			var zeroOperandOperator = ['is null','is not null','min','max'];

			for(var i = 0; i < filters.length - 1; i++){
				var filter = filters[i];
				var operator = filter.filterOperator;
				var values = filter.filterVals;

				if(oneOperandOperator.indexOf(operator) > -1){
					if(values.length!=1){
						return false;
					}
					else{
						if(values[0] == ''){
							return false;
						}
					}

				}
				else if(twoOperandOperator.indexOf(operator) > -1){
					if(values.length!=2){
						return false;
					}
					else{
						if(values[0] == '' || values[1] == ''){
							return false;
						}
					}
				}
				else if(zeroOperandOperator.indexOf(operator) > -1){
					if(values.length!=0){
						return false;
					}
				}
			}
			return true;
		}



		$scope.showAction = function(text) {
			var toast = $mdToast.simple()
					.content(text)
					.action('OK')
					.highlightAction(false)
					.hideDelay(3000)
					.position('top')

			$mdToast.show(toast).then(function(response) {
				if ( response == 'ok' ) {
				}
			});
		}

		$scope.cancelConfiguration=function(){
			mdPanelRef.close();
			mdPanelRef.destroy();
			$scope.$destroy();
			finishEdit.reject();
		}

		$scope.canSeeGrid = function(){
			if($scope.model!=undefined){
				return $scope.model.settings.showGrid;
			}else{
				return false;
			}
		}

		$scope.canSeeSummary = function(){
			if($scope.model && $scope.model.settings && $scope.model.settings.summary){
				return $scope.model.settings.summary.enabled;
			}else{
				return false;
			}
		}
		$scope.watchColumnSelectedOfDataset = $scope.$watchCollection('model.content.columnSelectedOfDataset', function(newColumns, oldColumns) {
			var disableShowSummary = true;
			if(newColumns){
				for(var i=0; i<newColumns.length; i++){
					if(newColumns[i].fieldType == "MEASURE"){
						disableShowSummary = false;
						break;
					}
				}
			}
			$scope.model.settings.summary.forceDisabled = disableShowSummary;
			if(disableShowSummary){
				$scope.model.settings.summary.enabled = false;
			}
		});
	}

	// this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("old-table",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();