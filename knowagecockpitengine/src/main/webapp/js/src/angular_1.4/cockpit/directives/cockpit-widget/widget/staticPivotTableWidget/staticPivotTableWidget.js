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

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 *
 */
(function() {
angular.module('cockpitModule')
.directive('cockpitStaticPivotTableWidget',function(cockpitModule_widgetServices){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/staticPivotTableWidget/templates/staticPivotTableWidgetTemplate.html',
		   controller: cockpitStaticPivotTableWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
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
}).run(function() {
	//adds methods for IE11
	if (!String.prototype.startsWith) {
	    String.prototype.startsWith = function(searchString, position){
	      position = position || 0;
	      return this.substr(position, searchString.length) === searchString;
	  };
	}

	if (!String.prototype.endsWith) {
		  String.prototype.endsWith = function(searchString, position) {
		      var subjectString = this.toString();
		      if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
		        position = subjectString.length;
		      }
		      position -= searchString.length;
		      var lastIndex = subjectString.lastIndexOf(searchString, position);
		      return lastIndex !== -1 && lastIndex === position;
		  };
		}

});

function cockpitStaticPivotTableWidgetControllerFunction(
		$scope,
		cockpitModule_widgetConfigurator,
		$q,
		$mdPanel,
		sbiModule_restServices,
		$compile,
		cockpitModule_generalOptions,
		$mdDialog,
		sbiModule_device,
		sbiModule_i18n,
		$timeout,
		cockpitModule_properties,
		cockpitModule_defaultTheme){

	var _EMPTYFIELDPLACEHOLDER = 'empty_field';
	$scope.init=function(element,width,height){
		$scope.refreshWidget(null, 'init');
	};

	$scope.cleanProperties = function(config, obj, admitObject) {
		var toReturn = {};
		for (var c in config){
			if (c == obj){
				if (typeof config[c] == 'object'){
					var objProp = config[c];
					var propToReturn = {};
					for (p in objProp){
						if (!admitObject && p.startsWith("{\"")){
							continue;	//skip the object element. ONLY attributes are added
						}
						propToReturn[p] = objProp[p];
					}
					toReturn[c] = propToReturn;
				}
			}else
				toReturn[c] = config[c];
		}
		return toReturn;
	}

	$scope.addDynamicWidthClass = function(elem){
		elem.classList.add('crosstab-fill-width');
		elem.style['table-layout'] = 'auto';
	}

	$scope.addFixedWidthClass = function(elem){
		elem.classList.add('crosstab-fill-width');
		elem.style['table-layout'] = 'fixed';
	}

	$scope.evaluatePivotedCells = function(elem){
		var offsetArray = [];
		var staticThead = elem.querySelectorAll("thead tr");
		for(var j = 0; j < staticThead.length; j++){
			if(!staticThead[j].style.backgroundColor){
				for(var c = 0 ; c < staticThead[j].children.length; c++){
					if(!staticThead[j].children[c].style.backgroundColor) staticThead[j].children[c].style.backgroundColor = 'white';
				}
			}
		}

		var pivotedHeaders = elem.querySelectorAll("td[pivot*='header']");
		for(var h = 0; h < pivotedHeaders.length; h++){
			pivotedHeaders[h].style.left = "";
			var headerName = pivotedHeaders[h].getAttribute('pivot');
			headerIndex = headerName.substr(headerName.length - 1);
			if(!offsetArray[headerIndex]){
				var leftPadding = 0;
				if($scope.ngModel.style && $scope.ngModel.style.padding && $scope.ngModel.style.padding.enabled){
					leftPadding = parseInt($scope.ngModel.style.padding["padding-left"]) || 0;
				}
				offsetArray.splice(headerIndex, 0 , pivotedHeaders[h].offsetLeft - leftPadding);
			}
			pivotedHeaders[h].style.left = offsetArray[headerIndex];
		}
		var pivotedCells = elem.querySelectorAll('td[pivot]');
		if(pivotedCells.length > 0){
			for(var k in pivotedCells){
				if(pivotedCells[k] && pivotedCells[k].style){
					pivotedCells[k].style.left = offsetArray[pivotedCells[k].getAttribute('pivot')];
					if(!pivotedCells[k].style.backgroundColor && pivotedCells[k].getAttribute('pivot')[0]!= 'h') pivotedCells[k].style.backgroundColor = 'white';
				}
			}
		}
	}

	$scope.removeDynamicWidthClass = function(elem){
		elem.classList.remove("crosstab-fill-width");
	}

	$scope.refresh=function(element,width,height, datasetRecords,nature){
		if(nature == 'resize' || nature == 'gridster-resized' || nature == 'fullExpand'){
			$scope.evaluatePivotedCells(element[0]);
		}
		if(datasetRecords==undefined){
			return;
		}
		if(nature == 'resize' || nature == 'gridster-resized' || nature == 'fullExpand'){
			var fatherElement = angular.element($scope.subCockpitWidget);
			if($scope.ngModel.content.style.generic.layout == 'auto') {
				$scope.addDynamicWidthClass(fatherElement[0].children[0]);
			} else {
				$scope.addFixedWidthClass(fatherElement[0].children[0]);
			}
			return;
		}
		$scope.showWidgetSpinner();

		var dataToSend={
				 config: {
				        type: "pivot"
				    },
				metadata: datasetRecords.metaData,
				jsonData: datasetRecords.rows,
				sortOptions: $scope.getSortOptions($scope.ngModel.content.crosstabDefinition.columns, $scope.ngModel.content.crosstabDefinition.rows) || {} //initialization with template version
		};

		$scope.ngModel.content.sortOptions = $scope.manageSortCompatibility($scope.ngModel.content.sortOptions);
		angular.merge(dataToSend,$scope.ngModel.content); //(dest, src)
		$scope.ngModel.content.sortOptions = $scope.manageSortCompatibility(dataToSend.sortOptions);

		if( dataToSend.crosstabDefinition==undefined ||
			dataToSend.crosstabDefinition.measures==undefined||dataToSend.crosstabDefinition.measures.length==0 ||
			((dataToSend.crosstabDefinition.rows==undefined||dataToSend.crosstabDefinition.rows.length==0) &&
			(dataToSend.crosstabDefinition.columns==undefined||dataToSend.crosstabDefinition.columns.length==0)) ){
			console.log("crossTab non configured")
			$scope.hideWidgetSpinner();
			return;
		}

		dataToSend.crosstabDefinition.measures = $scope.initializeStyleFormat(dataToSend.crosstabDefinition.measures);
		dataToSend.crosstabDefinition.rows = $scope.initializeStyleFormat(dataToSend.crosstabDefinition.rows);
		dataToSend.crosstabDefinition.columns = $scope.initializeStyleFormat(dataToSend.crosstabDefinition.columns);
		if(cockpitModule_properties.VARIABLES) dataToSend.variables = cockpitModule_properties.VARIABLES;

		dataToSend.crosstabDefinition.measures = $scope.cleanObjectConfiguration(dataToSend.crosstabDefinition.measures, 'style', false);
		dataToSend.crosstabDefinition.rows = $scope.cleanObjectConfiguration(dataToSend.crosstabDefinition.rows, 'style', false);
		dataToSend.crosstabDefinition.columns = $scope.cleanObjectConfiguration(dataToSend.crosstabDefinition.columns, 'style', false);

		$scope.sentData = dataToSend;	//used for expand/collapse all

		$scope.applyI18N(dataToSend);
		$scope.options = dataToSend;

		$scope.oldUpdateExecutions = $scope.oldUpdateExecutions || [];

		var newUpdateExecution = sbiModule_restServices.promisePost("1.0/crosstab","update",dataToSend);
		$q.all($scope.oldUpdateExecutions).then(function() {
			newUpdateExecution.then(
				function(response){
					$scope.isExpanded = true;
					var fatherElement = angular.element($scope.subCockpitWidget);
					$scope.subCockpitWidget.html(response.data.htmlTable);
					$scope.addPivotTableStyle();
					$scope.hideWidgetSpinner();
					$compile(fatherElement.contents())($scope);
					if(fatherElement[0].children[0] && (fatherElement[0].children[0].clientWidth < fatherElement[0].clientWidth)) {
						$scope.addDynamicWidthClass(fatherElement[0].children[0]);
					}
					$scope.evaluatePivotedCells(element[0]);
				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,"Pivot Table Error")
					$scope.hideWidgetSpinner();
				}
			);
		});

		$scope.oldUpdateExecutions.push(newUpdateExecution)

		if(nature == 'init'){
			$timeout(function(){
				$scope.widgetIsInit=true;
				cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
			},500);
		}
	}

	// returns the internationalized crosstab definition
	$scope.applyI18N = function(crosstabDataRequestData) {
		// looks for all "alias" properties and apply I18N to them
		crosstabDataRequestData.crosstabDefinition = $scope.getI18NJSON(crosstabDataRequestData.crosstabDefinition, "alias");
		// looks for all "totals" and "subtotal" properties and apply I18N to them
		crosstabDataRequestData.crosstabDefinition.config = $scope.getI18NtotalsAndSubtotals(crosstabDataRequestData.crosstabDefinition.config);
		// looks for all "header" properties and apply I18N to them
		crosstabDataRequestData.metadata = $scope.getI18NJSON(crosstabDataRequestData.metadata, "header");
	}

	$scope.getI18NJSON = function (jsonTemplate, attributeName) {
    	var clone = angular.copy(jsonTemplate);

    	// looks for all "attributeName" properties and apply I18N to them
    	var func = function (key, object) {
    		if (object.hasOwnProperty(attributeName)) {
    			object[attributeName] = sbiModule_i18n.getI18n(object[attributeName]);
	        }
    	}

    	this.traverse(clone, func);
    	return clone;
	}

	$scope.getI18NtotalsAndSubtotals = function (jsonTemplate) {
		var clone = angular.copy(jsonTemplate);
		clone.columnsubtotalLabel = sbiModule_i18n.getI18n(clone.columnsubtotalLabel);
		clone.columntotalLabel = sbiModule_i18n.getI18n(clone.columntotalLabel);
		clone.rowsubtotalLabel = sbiModule_i18n.getI18n(clone.rowsubtotalLabel);
		clone.rowtotalLabel = sbiModule_i18n.getI18n(clone.rowtotalLabel);
		return clone;
	}

	$scope.escapeHtmlString = function(customString){
		 var map = cockpitModule_generalOptions.htmlEscapes;
		 return customString.replace(cockpitModule_generalOptions.htmlRegex, function(m) {
			 return map[m];
			 });
	}

	$scope.getParentKey = function(key) {
		var values = [];
		for (i in $scope.sentData.metadata.fields) {
			var field = $scope.sentData.metadata.fields[i];
			if (field.header == key) {
				return field.name;
			}
		}
		return null;
	}

	$scope.getParentValues = function(key) {
		var values = [];
		for (i in $scope.sentData.metadata.fields) {
			var field = $scope.sentData.metadata.fields[i];
			if (field.header == key) {
				var colName = field.name;
				break;
			}
		}
		for (i in $scope.sentData.jsonData) {
			var value = $scope.sentData.jsonData[i][colName];
			if (values.indexOf($scope.escapeHtmlString(value)) == -1) values.push($scope.escapeHtmlString(value));
		}
		return values;
	}

	$scope.collapseAll = function(e) {
		e.stopImmediatePropagation();
		e.preventDefault();
		var alias = $scope.sentData.crosstabDefinition.rows[0].alias;
		var parentKey = $scope.getParentKey(alias);
		var parentValues = $scope.getParentValues(alias);
		var widgetEl = document.getElementById($scope.ngModel.id);

		for (p in parentValues) {
			var parent = parentValues[p];
			if(parent == '') parent = _EMPTYFIELDPLACEHOLDER;
			//suffix to avoid issues with same name in different column levels
			parent = parent + '1';
			//hide all rows
			var rowsToHideQuery = "tr[" + parentKey + "='" + parent + "']";
			var rowsToHide = widgetEl.querySelectorAll(rowsToHideQuery);
			rowsToHide.forEach(function(row, index){
				row.style.display = 'none';
			});
			//show only subtotal row
			var subtotalHiddenCellQuery = "td[id='" + parent + "']";
			var subtotalHiddenCell = widgetEl.querySelectorAll(subtotalHiddenCellQuery);
			if(subtotalHiddenCell[1]){
				subtotalHiddenCell[1].parentElement.style.display = "table-row";
				//show hidden cell in subtotal row
				subtotalHiddenCell[1].classList.remove('hidden');
			}
		}
		$scope.evaluatePivotedCells(widgetEl);
		$scope.isExpanded = false;
	}

	$scope.expandAll = function(e) {
		e.stopImmediatePropagation();
		e.preventDefault();
		var alias = $scope.sentData.crosstabDefinition.rows[0].alias;
		var parentKey = $scope.getParentKey(alias);
		var parentValues = $scope.getParentValues(alias);
		var widgetEl = document.getElementById($scope.ngModel.id);

		//show all rows
		var rowsToShowQuery = "tr";
		var rowsToShow = widgetEl.querySelectorAll(rowsToShowQuery);
		rowsToShow.forEach(function(row, index){
			row.style.display = 'table-row';
		});
		for (p in parentValues) {
			var parent = parentValues[p];
			if(parent == '') parent = _EMPTYFIELDPLACEHOLDER;
			//suffix to avoid issues with same name in different column levels
			parent = parent + '1';
			//hide cell in subtotal row
			var subtotalHiddenCellQuery = "td[id='" + parent + "']";
			var subtotalHiddenCell = widgetEl.querySelectorAll(subtotalHiddenCellQuery);
			if(subtotalHiddenCell[1]) subtotalHiddenCell[1].classList.add('hidden');
			//hide all children hidden cells
			var childrenHiddenCellsQuery = "td.cell-visible";
			var childrenHiddenCells = widgetEl.querySelectorAll(childrenHiddenCellsQuery);
			childrenHiddenCells.forEach(function(cell, index){
				cell.classList.remove("cell-visible");
				cell.classList.add('hidden');
			});
			var collapsedCellsQuery = "td[style*='display: none;']";
			var collapsedCells = widgetEl.querySelectorAll(collapsedCellsQuery);
			collapsedCells.forEach(function(cell, index){
				cell.style.display = 'table-cell';
			});
			//reset rowspans
			$scope.resetParentsRowspan(parentKey, parent, widgetEl);
		}
		$scope.evaluatePivotedCells(widgetEl);
		$scope.isExpanded = true;
	}

	$scope.getCorrectElement = function(allElements, parent) {
		for (var i=0; i<allElements.length; i++) {
			var rightElement = true;
			for (var key in parent) {
				var val = parent[key];
				var attrVal = allElements[i].parentElement.getAttribute(key);
				if (!attrVal || attrVal != val) {
					rightElement = false;
					break;
				}
			}
			if (rightElement) {
				var element = allElements[i];
				break;
			}
		}
		return element;
	}

	$scope.getCorrectRows = function(allElements, parent) {
		var subtotals = [];
		if (Object.keys(parent).length > 0) { //filter only rows related to current parent, don't hide rows of other parents
			for (var i=0; i<allElements.length; i++) {
				var valid = false;
				for (var key in parent) {
					var val = parent[key];
					var attrVal = allElements[i].getAttribute(key);
					if(attrVal && attrVal == val) valid = true;
					else {
						valid = false;
						break;
					}
				}
				if (valid) {
					subtotals.push(allElements[i]);
				}
			}
			return subtotals;
		} else return allElements;
	}

	$scope.collapse = function(e, column, value, parent) {
		e.stopImmediatePropagation();
		e.preventDefault();
		var widgetEl = document.getElementById($scope.ngModel.id);
		var rowQuery = "tr[" + column + "='" + value + "']";
		var hiddenRows = 0;
		var rowsToHide = $scope.getCorrectRows(widgetEl.querySelectorAll(rowQuery), parent);
		rowsToHide.forEach(function(row, index){
			if (row.style.display == 'none') hiddenRows++; // count rows that have already been hidden
			if (index > 0) {
				row.style.display = 'none';
			}
			else { // first row
				if (row.children[0] == e.currentTarget.parentElement) { // hide first cell to show hidden one
					row.style.display = 'none';
				}
				else {
					var finished = false;
					e.currentTarget.parentElement.setAttribute('firstrow', true);
					for (var c=0; c < row.children.length && !finished; c++) { // loop on all row elements
						if (row.children[c] == e.currentTarget.parentElement ) { // children elements
							for (var i=c; i<row.children.length; i++) {
								row.children[i].style.display = 'none';
							}
							finished = true;
						} else { // parent elements in the same row
							var rowspan = row.children[c].getAttribute('rowspan');
							row.children[c].setAttribute('normalizedRowspan', true);
						}
					}
				}
			}
		});
		var subTotalQuery = "tr[" + column + "='" + value + "'][SubTotal]";
		var allSubTotals = widgetEl.querySelectorAll(subTotalQuery);
		var subTotals = $scope.getCorrectRows(allSubTotals, parent);
		subTotals[subTotals.length - 1].style.display = "table-row";
//		widgetEl.querySelectorAll(subTotalQuery)[allSubTotals.length - 1].style.display = "table-row";
		var cellQuery = "tr[" + column + "='" + value + "'][SubTotal] td[id='"+ value +"'].hidden";
		var allCells = widgetEl.querySelectorAll(cellQuery);
		var cell = $scope.getCorrectElement(allCells, parent);
		cell.classList.add('cell-visible');
		cell.classList.remove('hidden');
		//if not the first level change the parent rowspan to avoid fat rows
		if (parent) {
			var tempParents = {};
			var orderedParents = {};
			Object.keys(parent).sort().forEach(function(key) {
				orderedParents[key] = parent[key];
				});
			for (var p in orderedParents) { // loop on all parents
				tempParents[p] = orderedParents[p];
				var parentQuery = "tr[" + p + "='" + parent[p] + "']";
				var parentRows = widgetEl.querySelectorAll(parentQuery);
				var correctParentRows = $scope.getCorrectRows(parentRows, tempParents);
				for (var k in correctParentRows[0].querySelectorAll('td')) { // loop on all parent cells
					var currentParentEl = correctParentRows[0].querySelectorAll('td')[k];
					if (currentParentEl.id == parent[p]) { // apply rowspan normalization only to current cell
						var rowspan = currentParentEl.getAttribute('rowspan');

						if (currentParentEl.getAttribute('normalizedRowspan')) { // check if normalization has been performed
							currentParentEl.setAttribute('rowspan', parseInt(rowspan) - (rowsToHide.length - hiddenRows) + 2);
							currentParentEl.removeAttribute('normalizedRowspan');
						}
						else if(e.currentTarget.parentElement.getAttribute('firstrow')){ // first record has empty row
							currentParentEl.setAttribute('rowspan', parseInt(rowspan) - (rowsToHide.length - hiddenRows) + 2);
							currentParentEl.removeAttribute('firstrow');
						}else {
							currentParentEl.setAttribute('rowspan', parseInt(rowspan) - (rowsToHide.length - hiddenRows) + 1);
						}
					}
				}
			}
		}
		$scope.evaluatePivotedCells(widgetEl);
	}

	$scope.expand = function(e, column, value, parent) {
		e.stopImmediatePropagation();
		e.preventDefault();
		var widgetEl = document.getElementById($scope.ngModel.id);
		var rowQuery = "tr[" + column + "='" + value + "']";
		var rowsToShow = $scope.getCorrectRows(widgetEl.querySelectorAll(rowQuery), parent);
		var rowSpanModifier = -1;
		rowsToShow.forEach(function(row, index){
			if(index == 0 && row.children[0].id != value) {
				var modifyRowSpan = true;
				for(var c in row.children){
					if(row.children[c].id == value && row.children[c].getAttribute('start-span')) {
						row.children[c].setAttribute('rowspan', row.children[c].getAttribute('start-span'));
						//rowSpanModifier = - (row.children[c].getAttribute('start-span') - row.children[c].getAttribute('rowspan'));
						//modifyRowSpan = false;
					}
				}
				if(modifyRowSpan){
					rowSpanModifier--;
				}

			} else {
				if(index == 0) {
					if(row.children[0].getAttribute('start-span')) {
						row.children[0].setAttribute('rowspan', row.children[0].getAttribute('start-span'));
					}
					else row.children[0].setAttribute('rowspan', rowsToShow.length);
				}
			}
			row.style.display = "table-row";
			row.querySelectorAll('td').forEach(function(cell) {
				if (cell.classList.contains("cell-visible")) {
					cell.classList.remove("cell-visible");
					cell.classList.add('hidden');
					rowSpanModifier++;
				}
				else cell.style.display = "table-cell";
			});
		});
		var subTotalQuery = "tr[" + column + "='" + value + "'][SubTotal]";
		var allSubTotals = widgetEl.querySelectorAll(subTotalQuery);
		e.currentTarget.parentElement.classList.add('hidden');
		//if not the first level change the parent rowspan to avoid fat rows
		if (parent && Object.keys(parent).length > 0) {
			var tempParents = {};
			var orderedParents = {};
			Object.keys(parent).sort().forEach(function(key) {
				orderedParents[key] = parent[key];
				});
			for (var p in orderedParents) { // loop on all parents
				tempParents[p] = orderedParents[p];
				var parentQuery = "tr[" + p + "='" + parent[p] + "']";
				var parentRows = widgetEl.querySelectorAll(parentQuery);
				var correctParentRows = $scope.getCorrectRows(parentRows, tempParents);

				//rowSpanModifier = rowSpanModifier + (Object.keys(tempParents).length - Object.keys(parent).length);

				for (var k in correctParentRows[0].querySelectorAll('td')) { // loop on all cells
					if (correctParentRows[0].querySelectorAll('td')[k].id == parent[p]) { // if current cell
						var rowspan = correctParentRows[0].querySelectorAll('td')[k].getAttribute('rowspan');
						var currentCell = correctParentRows[0].querySelectorAll('td')[k];
						currentCell.setAttribute('rowspan', parseInt(rowspan) + rowsToShow.length - 1 + rowSpanModifier);
						// if the rowspan is bigger that start-span it is normalized
						if(currentCell.getAttribute('start-span') && (parseInt(currentCell.getAttribute('rowspan')) > parseInt(currentCell.getAttribute('start-span')))) currentCell.setAttribute('rowspan', currentCell.getAttribute('start-span'));
						break;
					}
				}
			}
		}
		$scope.evaluatePivotedCells(widgetEl);
	}

	$scope.resetParentsRowspan = function(key, value, widgetEl) {
		//reset all parents rowspan to initial value
		var allParentsQuery = "tr[" + key + "='" + value + "'] td[start-span]";
		var allParents = widgetEl.querySelectorAll(allParentsQuery);
		allParents.forEach(function(element){
			var originalRowspan = element.getAttribute('start-span');
			element.setAttribute('rowspan', originalRowspan);
		});
	}

	$scope.traverse = function(o, func) {
	    for (var i in o) {
	        if (o[i] !== null && typeof(o[i])=="object") {
	        	func.apply(this, [i, o[i]]);
	            //going one step down in the object tree!!
    	        this.traverse(o[i], func);
	        }
	    }
	};


	$scope.initializeStyleFormat = function(config){
		//add an empty style format on the config objects if it's not found
		if (Array.isArray(config)){
			var toReturnArray = [];
			for (var i=0; i < config.length; i++){
				var configElement = config[i];
				if (!configElement.hasOwnProperty('style')){
					//style not defined, we add it as an empty obj
					configElement.style = new Object();
					configElement.style.format = "";
				}
				toReturnArray.push(configElement);
			}
			return toReturnArray;
		} else {
			if (!config.hasOwnProperty('style')){
				//style not defined, we add it as an empty obj
				config.style = new Object();
				configElement.style.format = "";
			}
			return config;
		}

	}


	$scope.selectRow=function(columnName,columnValue){
		$scope.doSelection(columnName,columnValue);
	};

	$scope.getSortOptions = function(columns, rows){
		var toReturn = {};
		var rowsSortKeys = [];
		var columnsSortKeys = [];

		for (var c=0; c < columns.length; c++){
			var cObj = columns[c];
			if (cObj.sorting){
				var cSort = {};
				cSort.column = c;
				cSort.direction = (cObj.sortingOrder == "ASC")? 1 : -1;
				columnsSortKeys.push(cSort);
			}
		}
		if (columnsSortKeys.length > 0)
			toReturn.columnsSortKeys = columnsSortKeys;

		for (var c=0; c < rows.length; c++){
			var rObj = rows[c];
			if (rObj.sorting){
				var rSort = {};
				rSort.column = c;
				rSort.direction = (rObj.sortingOrder == "ASC")? 1 : -1;
				rowsSortKeys.push(rSort);
			}
		}
		if (rowsSortKeys.length > 0)
			toReturn.rowsSortKeys = rowsSortKeys;

		return toReturn;
	}

	$scope.manageSortCompatibility = function(oldSortOptions){
		var toReturn={};
		var rowsSortKeys = [];
		var columnsSortKeys = [];
		var measuresSortKeys = [];

		if (!oldSortOptions) return toReturn;

		if (oldSortOptions.columnsSortKeys && !Array.isArray(oldSortOptions.columnsSortKeys)){
			for (var c in oldSortOptions.columnsSortKeys){
				var cSort = {};
				cSort.column = c;
				cSort.direction = oldSortOptions.columnsSortKeys[c];
				columnsSortKeys.push(cSort);

			}
			if (columnsSortKeys.length > 0)
				toReturn.columnsSortKeys = columnsSortKeys;
		}

		if (oldSortOptions.rowsSortKeys && !Array.isArray(oldSortOptions.rowsSortKeys)){
			for (var r in oldSortOptions.rowsSortKeys){
				var rSort = {};
				rSort.column = r;
				rSort.direction = oldSortOptions.rowsSortKeys[r];
				rowsSortKeys.push(rSort);
			}
			if (rowsSortKeys.length > 0)
				toReturn.rowsSortKeys = rowsSortKeys;
		}

		if (oldSortOptions.measuresSortKeys && !Array.isArray(oldSortOptions.measuresSortKeys)){
			for (var m in oldSortOptions.measuresSortKeys){
				var mSort = {};
				mSort.column = m;
				mSort.direction = oldSortOptions.measuresSortKeys[m];
				mSort.measureLabel = oldSortOptions.measuresSortKeys.measureLabel;
				mSort.parentValue = oldSortOptions.measuresSortKeys.parentValue;
				measuresSortKeys.push(mSort);
				break;
			}
			if (measuresSortKeys.length > 0)
				toReturn.measuresSortKeys = measuresSortKeys;
		}

		if (JSON.stringify(toReturn) === JSON.stringify({}))
			toReturn = oldSortOptions; //syntax is already correct

		return toReturn;
	}

	$scope.cleanObjectConfiguration = function(config, obj, admitObject){

		if (Array.isArray(config)){
			var toReturnArray = [];
			for (var e=0; e<config.length; e++){
				var elem = config[e];
				toReturnArray.push($scope.cleanProperties(elem, obj, admitObject));
			}
			return toReturnArray;
		}else{
			var toReturn = {};
			toReturn = $scope.cleanProperties(config, obj, admitObject);
			return toReturn;
		}
	}


	$scope.selectMeasure=function(rowHeaders, rowsValues, columnsHeaders, columnValues, measureRef){
		var lstHeaders = []; //list of all headers (columns and rows)
		var lstValues = []; //list of all values (columns and rows)
		if (rowsValues != ""){
			//adds all selection references about the row side
			var rowsHeads = rowHeaders.split("_S_");
			var rowsVals = rowsValues.split("_S_");
			for (var c=0; c < rowsHeads.length; c++){
				if (rowsHeads[c] == "" || !rowsVals[c] || rowsVals[c] == "") continue;
				var rowName = rowsHeads[c];
				var rowValue = rowsVals[c];
				lstHeaders.push(rowName);
				lstValues.push(rowValue);
			}
		}

		if (columnValues != ""){
			//adds all selection references about the column side
			var columnHeads = columnsHeaders.split("_S_");
			var columnVals = columnValues.split("_S_");
			for (var c=0; c < columnHeads.length; c++){
				if (columnHeads[c] == "" || !columnVals[c] || columnVals[c] == "") continue;
				var columnName = columnHeads[c];
				var columnValue = columnVals[c];
				lstHeaders.push(columnName);
				lstValues.push(columnValue);
			}
		}

		var measureId = '';
		if (measureRef != ""){
			var measureHeads = measureRef.split("_S_");
			if (measureHeads[1] && measureHeads[1] != ""){
				measureId = measureHeads[1];
			}
		}

		if (lstHeaders.length>0)
			$scope.doSelection(lstHeaders,lstValues,measureId); //call selection method passing all headers and values (unique time)

	};

	$scope.enableAlternate = function(){
		$scope.colorPickerProperty['disabled'] = $scope.ngModel.content.style.showAlternateRows;
	}

	$scope.enableGrid =  function(){
		$scope.colorPickerPropertyGrid['disabled'] = $scope.ngModel.content.style.showGrid;
	}



	$scope.addPivotTableStyle=function(){
		if($scope.ngModel.content.style!=undefined){
			var totalsItem;
			var subtotalsItem;
			var dataItem;
			var memberItem;
			var measureHeaderItem;
			var crossItem;
			//generic
			if($scope.ngModel.content.style.generic!=undefined && Object.keys($scope.ngModel.content.style.generic).length>0 ){
				totalsItem 			= angular.element($scope.subCockpitWidget[0].querySelectorAll(".totals"));
				subtotalsItem 		= angular.element($scope.subCockpitWidget[0].querySelectorAll(".partialsum"));
				dataItem 			= angular.element($scope.subCockpitWidget[0].querySelectorAll(".data"));
				dataItemNoStd 		= angular.element($scope.subCockpitWidget[0].querySelectorAll(".dataNoStandardStyle"));
				memberItem 			= angular.element($scope.subCockpitWidget[0].querySelectorAll(".member"));
				measureHeaderItem 	= angular.element($scope.subCockpitWidget[0].querySelectorAll(".measures-header-text"));
				crossItem 			= angular.element($scope.subCockpitWidget[0].querySelectorAll(".crosstab-header-text"));
				for(var prop in $scope.ngModel.content.style.generic){
					if ($scope.ngModel.content.style.generic[prop]!=""){
						totalsItem.css(prop,$scope.ngModel.content.style.generic[prop]);
						subtotalsItem.css(prop,$scope.ngModel.content.style.generic[prop]);
						dataItem.css(prop,$scope.ngModel.content.style.generic[prop]);
						dataItemNoStd.css(prop,$scope.ngModel.content.style.generic[prop]);
						memberItem.css(prop,$scope.ngModel.content.style.generic[prop]);
						crossItem.css(prop,$scope.ngModel.content.style.generic[prop]);
					}
				}
			}

			//altrnateRow & grid border
			if($scope.ngModel.content.style.showGrid ||
			   ($scope.ngModel.content.style.showAlternateRows &&
			    $scope.ngModel.content.style.measuresRow!=undefined &&
			    Object.keys($scope.ngModel.content.style.measuresRow).length>0)){
				var rowList=angular.element($scope.subCockpitWidget[0].querySelectorAll("tr"));
				var tmpOddRow=false;
				angular.forEach(rowList,function(row,index){
					//apply borders on member class
					var dataColumnList=row.querySelectorAll(".member");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
//					dataColumnList=row.querySelectorAll(".measures-header-text");
//					if(dataColumnList.length>0){
//						$scope.applyBorderStyle(dataColumnList);
//					}
					dataColumnList=row.querySelectorAll(".memberNoStandardStyle");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on level class
					dataColumnList=row.querySelectorAll(".level");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on 'na' class
					dataColumnList=row.querySelectorAll(".na");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on 'na' class
					dataColumnList=row.querySelectorAll(".naNoStandardStyle");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on 'empty' class
					dataColumnList=row.querySelectorAll(".empty");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on 'total' class
					dataColumnList=row.querySelectorAll(".totals");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on 'subtotal' class
					dataColumnList=row.querySelectorAll(".partialsum");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply borders on 'crosstab-header-text' class
					dataColumnList=row.querySelectorAll("td.crosstab-header-text");
					if(dataColumnList.length>0){
						$scope.applyBorderStyle(dataColumnList);
					}
					//apply styles on data (values)
					dataColumnList=row.querySelectorAll(".data");
					if(dataColumnList.length>0){
						// alternateRow only if there are not thresholds
						if ($scope.ngModel.content.style.showAlternateRows
								&& angular.element(dataColumnList).css("background-color") == "") {
							if (tmpOddRow
							    && $scope.ngModel.content.style.measuresRow["odd-background-color"] != "") {
								angular.element(dataColumnList).css("background-color",$scope.ngModel.content.style.measuresRow["odd-background-color"])
							} else if ($scope.ngModel.content.style.measuresRow["even-background-color"] != "") {
								angular.element(dataColumnList).css("background-color",$scope.ngModel.content.style.measuresRow["even-background-color"])
							}
							tmpOddRow=!tmpOddRow;
						}

						//border cell style
						$scope.applyBorderStyle(dataColumnList);
					}else{
						tmpOddRow=false;
					}

					dataNoStandardColumnList=row.querySelectorAll(".dataNoStandardStyle"); //personal user settings
					if(dataNoStandardColumnList.length>0){
						//border cell style
						$scope.applyBorderStyle(dataNoStandardColumnList);
					}
				});
			}

			//measures
			if($scope.ngModel.content.style.measures!=undefined && Object.keys($scope.ngModel.content.style.measures).length>0 ){
				if(dataItem==undefined){
					dataItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".data"));
				}
				for(var prop in $scope.ngModel.content.style.measures){
					if(angular.equals("background-color",prop) && $scope.ngModel.content.style.measures[prop]!= ""){
						dataItem.parent().parent().css(prop,$scope.ngModel.content.style.measures[prop])
					}else if ($scope.ngModel.content.style.measures[prop] != "")
						dataItem.css(prop,$scope.ngModel.content.style.measures[prop])
				}
			}

			//measuresHeaders
			if($scope.ngModel.content.style.measuresHeaders!=undefined && Object.keys($scope.ngModel.content.style.measuresHeaders).length>0 ){
				if(measureHeaderItem==undefined){
//					memberItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".member"));
					measureHeaderItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".measures-header-text"));
				}
				for(var prop in $scope.ngModel.content.style.measuresHeaders){
					if(angular.equals("background-color",prop) && $scope.ngModel.content.style.measuresHeaders[prop]!= ""){
						if ($scope.ngModel.content.crosstabDefinition.config.measureson == "columns")
							measureHeaderItem.parent().parent().css(prop,$scope.ngModel.content.style.measuresHeaders[prop])
						else
							measureHeaderItem.css(prop,$scope.ngModel.content.style.measuresHeaders[prop])
					}else if ($scope.ngModel.content.style.measuresHeaders[prop]!= "")
						measureHeaderItem.css(prop,$scope.ngModel.content.style.measuresHeaders[prop])
				}
			}

			//crossTabHeaders
			if($scope.ngModel.content.style.crossTabHeaders!=undefined && Object.keys($scope.ngModel.content.style.crossTabHeaders).length>0 ){
				if(crossItem==undefined){
					var crossEmptyItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".empty"));
					crossItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".crosstab-header-text"));
					Array.prototype.push.apply(crossItem, crossEmptyItem);
				}
				for(var prop in $scope.ngModel.content.style.crossTabHeaders){
					if(angular.equals("background-color",prop) && $scope.ngModel.content.style.crossTabHeaders[prop]!= ""){
						crossItem.parent().css(prop,$scope.ngModel.content.style.crossTabHeaders[prop])
					}else if ($scope.ngModel.content.style.crossTabHeaders[prop]!=""){
						crossItem.css(prop,$scope.ngModel.content.style.crossTabHeaders[prop])
					}
				}

				//memebers are managed with crosstab-header-text too
				if(memberItem==undefined){
					memberItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".member"));
				}
				for(var prop in $scope.ngModel.content.style.crossTabHeaders){
					if ($scope.ngModel.content.style.crossTabHeaders[prop]!=""){
						memberItem.css(prop,$scope.ngModel.content.style.crossTabHeaders[prop])
					}
				}
			}

			//totals
			if($scope.ngModel.content.style.totals!=undefined && Object.keys($scope.ngModel.content.style.totals).length>0 ){
				if(totalsItem==undefined){
					totalsItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".totals"));
				}
				for(var prop in $scope.ngModel.content.style.totals){
					if ($scope.ngModel.content.style.totals[prop]!= "")
						totalsItem.css(prop,$scope.ngModel.content.style.totals[prop])
				}
			}
			//subTotals
			if($scope.ngModel.content.style.subTotals!=undefined && Object.keys($scope.ngModel.content.style.subTotals).length>0 ){
				if(subtotalsItem==undefined){
					subtotalsItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".partialsum"));
				}
				for(var prop in $scope.ngModel.content.style.subTotals){
					if ($scope.ngModel.content.style.subTotals[prop] != "")
						subtotalsItem.css(prop,$scope.ngModel.content.style.subTotals[prop])
				}
			}
		}else {
			$scope.ngModel.content.style = cockpitModule_defaultTheme.pivotTable.style;
		}

	};

	//border cell style
	$scope.applyBorderStyle = function(dataColumnList){
		if ($scope.ngModel.content.style.showGrid){
			if($scope.ngModel.content.style.measuresRow["border-width"]!= ""){
				angular.element(dataColumnList).css("border-width",$scope.ngModel.content.style.measuresRow["border-width"])
			}
			if ($scope.ngModel.content.style.measuresRow["border-color"]!= ""){
				angular.element(dataColumnList).css("border-color",$scope.ngModel.content.style.measuresRow["border-color"])
			}
			if ($scope.ngModel.content.style.measuresRow["border-style"]!= ""){
				angular.element(dataColumnList).css("border-style",$scope.ngModel.content.style.measuresRow["border-style"])
			}
		}
	}

	$scope.editWidget=function(index){

		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: function($scope,finishEdit,sbiModule_translate,model,fnOrder,mdPanelRef,cockpitModule_datasetServices,cockpitModule_template,cockpitModule_generalOptions,$mdDialog,$mdToast,sbiModule_device){
			    	  $scope.translate=sbiModule_translate;
			    	  $scope.sbiModule_device=sbiModule_device;
			    	  $scope.localModel={};
			    	  $scope.currentDataset={};
			    	  $scope.originalCurrentDataset={};
			    	  $scope.dragUtils={dragObjectType:undefined};
			    	  $scope.tabsUtils={selectedIndex:0};
			    	  $scope.sorterByColumns=[];

			    	  $scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
			    	  $scope.bordersSize=[{
			  	    	label:$scope.translate.load("sbi.cockpit.style.borders.solid"),
			  	    	value:'solid',
			  	    	exampleClass:"borderExampleSolid"
			  	    },
			  	    {
			  	    	label:$scope.translate.load("sbi.cockpit.style.borders.dashed"),
			  	    	value:'dashed',
			  	    	exampleClass:"borderExampleDashed"
			  	    },
			  	    {
			  	    	label:$scope.translate.load("sbi.cockpit.style.borders.dotted"),
			  	    	value:'dotted',
			  	    	exampleClass:"borderExampleDotted"
			  	    }
			  		];
			  		$scope.bordersWidth=[{
			  		    	label:$scope.translate.load("sbi.cockpit.style.small"),
			  		    	value:"0.1em"
			  		    },
			  		    {
			  		    	label:$scope.translate.load("sbi.cockpit.style.medium"),
			  		    	value:"0.3em"
			  		    },
			  		    {
			  		    	label:$scope.translate.load("sbi.cockpit.style.large"),
			  		    	value:"0.7em"
			  		    },
			  		    {
			  		    	label:$scope.translate.load("sbi.cockpit.style.extralarge"),
			  		    	value:"1em"
			  		    },
			  		];
			    	  angular.copy(model,$scope.localModel); //src, dest


			    	  if($scope.localModel.content==undefined){
		    			  $scope.localModel.content={};
		    		  }
			    	  if($scope.localModel.content.crosstabDefinition==undefined){
		    			  $scope.localModel.content.crosstabDefinition={};
		    		  }
			    	  if($scope.localModel.content.crosstabDefinition.measures==undefined){
		    			  $scope.localModel.content.crosstabDefinition.measures=[];
		    		  }
			    	  if($scope.localModel.content.crosstabDefinition.rows==undefined){
			    		  $scope.localModel.content.crosstabDefinition.rows=[];
			    	  }
			    	  if($scope.localModel.content.crosstabDefinition.columns==undefined){
			    		  $scope.localModel.content.crosstabDefinition.columns=[];
			    	  }
			    	  if($scope.localModel.content.crosstabDefinition.config==undefined){
			    		  $scope.localModel.content.crosstabDefinition.config={};
			    	  }
			    	  if($scope.localModel.content.crosstabDefinition.config.measureson==undefined){
			    		  $scope.localModel.content.crosstabDefinition.config.measureson="columns";
			    	  }
			    	  if($scope.localModel.content.crosstabDefinition.config.percenton==undefined){
			    		  $scope.localModel.content.crosstabDefinition.config.percenton="no";
			    	  }

			    	  $scope.colorPickerProperty={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb'};
			    	  $scope.colorPickerPropertyGrid={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:$scope.localModel.content.style && $scope.localModel.content.style.showGrid ? false : true};
			    	  $scope.colorPickerAlternateGrid={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:$scope.localModel.content.style && $scope.localModel.content.style.showAlternateRows ? false : true};

			    		$scope.$watch('localModel.content.crosstabDefinition.config.expandCollapseRows',function(newValue,oldValue){
			    			if (newValue) {
			    				$scope.localModel.content.crosstabDefinition.config.calculatesubtotalsoncolumns = true;
			    			}
			    		})

			    		$scope.$watch('localModel.content.crosstabDefinition.config.calculatesubtotalsoncolumns',function(newValue,oldValue){
			    			if (!newValue) {
			    				$scope.localModel.content.crosstabDefinition.config.expandCollapseRows = false;
			    			}
			    		})

			    	  $scope.enableAlternate = function() {
			    		  $scope.colorPickerAlternateGrid.disabled=!$scope.localModel.content.style.showAlternateRows;
		    		  }
			    	  $scope.enableGrid = function() {
			    		  $scope.colorPickerPropertyGrid.disabled=!$scope.localModel.content.style.showGrid;
			    	  }

			    	  $scope.changeDatasetFunction=function(dsId,noReset){
			    		  $scope.currentDataset= cockpitModule_datasetServices.getDatasetById( dsId);
			    		  $scope.originalCurrentDataset=angular.copy( $scope.currentDataset);
			    		  if(noReset!=true){
			    			  $scope.localModel.content.crosstabDefinition.measures=[];
			    			  $scope.localModel.content.crosstabDefinition.rows=[];
			    			  $scope.localModel.content.crosstabDefinition.columns=[];
			    		  }
			    		  $scope.getDatasetAdditionalInfo(dsId);
			    	  }

			    	  $scope.getDatasetAdditionalInfo = function(dsId){
			    	        for(var k in cockpitModule_template.configuration.datasets){
			    	        	if(cockpitModule_template.configuration.datasets[k].dsId == dsId) {
			    	        		$scope.localDataset = cockpitModule_template.configuration.datasets[k];
			    	        		break;
			    	        	}

			    	        }
			    	        sbiModule_restServices.restToRootProject();
			    	        sbiModule_restServices.promiseGet('2.0/datasets', 'availableFunctions/' + dsId, "useCache=" + $scope.localDataset.useCache).then(function(response){
			    	        	$scope.datasetAdditionalInfos = response.data;
			    	        }, function(response) {
			    	        	if(response.data && response.data.errors && response.data.errors[0]) $scope.showAction(response.data.errors[0].message);
			    	        	else $scope.showAction($scope.translate.load('sbi.generic.error'));
			    	        });
			    		}
			    		if($scope.localModel.dataset && $scope.localModel.dataset.dsId) $scope.getDatasetAdditionalInfo($scope.localModel.dataset.dsId);

			    	  if($scope.localModel.dataset!=undefined && $scope.localModel.dataset.dsId!=undefined){
			    		  $scope.changeDatasetFunction($scope.localModel.dataset.dsId,true)
			    	  }

			    	  //default labels
			    	  if (!$scope.localModel.content.crosstabDefinition.config.rowsubtotalLabel || $scope.localModel.content.crosstabDefinition.config.rowsubtotalLabel == ''){
			    		  $scope.localModel.content.crosstabDefinition.config.rowsubtotalLabel = sbiModule_translate.load('sbi.crosstab.crosstabdetailswizard.defaultSubtotalLabel') ;
			    	  }
			    	  if (!$scope.localModel.content.crosstabDefinition.config.rowtotalLabel || $scope.localModel.content.crosstabDefinition.config.rowtotalLabel == ''){
			    		  $scope.localModel.content.crosstabDefinition.config.rowtotalLabel = sbiModule_translate.load('sbi.crosstab.crosstabdetailswizard.defaultTotalLabel') ;
			    	  }
			    	  if (!$scope.localModel.content.crosstabDefinition.config.columnsubtotalLabel || $scope.localModel.content.crosstabDefinition.config.columnsubtotalLabel == ''){
			    		  $scope.localModel.content.crosstabDefinition.config.columnsubtotalLabel = sbiModule_translate.load('sbi.crosstab.crosstabdetailswizard.defaultSubtotalLabel') ;
			    	  }
			    	  if (!$scope.localModel.content.crosstabDefinition.config.columntotalLabel || $scope.localModel.content.crosstabDefinition.config.columntotalLabel == ''){
			    		  $scope.localModel.content.crosstabDefinition.config.columntotalLabel = sbiModule_translate.load('sbi.crosstab.crosstabdetailswizard.defaultTotalLabel') ;
			    	  }

			    	  //remove used measure and attribute
			    	 $scope.clearUsedMeasureAndAttribute=function(){
			    		 if($scope.currentDataset.metadata==undefined){
			    			 return;
			    		 }

			    		 var arrObje=["measures","rows","columns"];
			    		 var present=[];
			    		 for(var meas=0;meas<arrObje.length;meas++){
			    			 for(var i=0;i<$scope.localModel.content.crosstabDefinition[arrObje[meas]].length;i++){
			    				 present.push($scope.localModel.content.crosstabDefinition[arrObje[meas]][i].id);
			    			 }
			    		 }

			    		 for(var i=0;i<$scope.currentDataset.metadata.fieldsMeta.length;i++){
			    			 if(present.indexOf($scope.currentDataset.metadata.fieldsMeta[i].name)!=-1){
			    				 $scope.currentDataset.metadata.fieldsMeta.splice(i,1);
			    				 i--;
			    			 }
			    		 }

			    	 }
			    	 $scope.clearUsedMeasureAndAttribute();

			    	 $scope.dropCallback=function(event, index, list,item, external, type, containerType){

			    		  if(angular.equals(type,containerType)){
			    			  var eleIndex=-1;
			    			  angular.forEach(list,function(ele,ind){
			    				  var tmp=angular.copy(ele);
			    				  delete tmp.$$hashKey;
			    				  if(angular.equals(tmp,item)){
			    					  eleIndex=ind;
			    				  }
			    			  });

			    			  list.splice(eleIndex,1)
			    			  list.splice(index,0,item)
			    			  return false
			    		  }else{
			    			  var tmpItem;
			    			  if(["MEASURE-PT","COLUMNS","ROWS"].indexOf(containerType) != -1){

			    				  if( (angular.equals(containerType,"COLUMNS") &&  angular.equals(type,"ROWS")) || (angular.equals(containerType,"ROWS") &&  angular.equals(type,"COLUMNS"))){
			    					  tmpItem=item;
			    				  }else{
			    					  //convert item in specific format
			    					   tmpItem={
			    							  id: item.name,
			    							  alias: item.alias,
			    							  containerType : containerType,
			    							  iconCls: item.fieldType.toLowerCase(),
			    							  nature: item.fieldType.toLowerCase(),
			    							  values: "[]",
//			    							  sortable: false,
			    							  width: 0
			    					  };
			    					   if(angular.equals(containerType,"MEASURE-PT")){
			    						   tmpItem.funct="SUM";
			    					   }

			    				  }
			    			  }else{
			    				  //containerType == MEASURE or ATTRIBUTE
			    				  //load element from dataset field
			    				  for(var i=0;i<$scope.originalCurrentDataset.metadata.fieldsMeta.length;i++){
			    					  if(angular.equals($scope.originalCurrentDataset.metadata.fieldsMeta[i].name,item.id)){
			    						  tmpItem=angular.copy($scope.originalCurrentDataset.metadata.fieldsMeta[i]);
			    						  break;
			    					  }
			    				  }
			    			  }
			    			  list.splice(index,0,tmpItem)
			    			  return true;
			    		  }

			    	  }

			    	 $scope.getMeasureType = function(item){
			    		 return item.nature == 'calculated_field' ? "'CALCULATED-FIELD'":"'MEASURE-PT'";
			    	 }

			    	 $scope.addCalculatedField = function(item) {

							item.nature = "calculated_field";
							item.id = item.alias;
							item.iconCls = "measure";
							item.funct = "NONE";

							$scope.localModel.content.crosstabDefinition.measures.push(item);

						}

						$scope.getAvailableMeasures = function() {
							var ret = [];
							for(var i in $scope.originalCurrentDataset.metadata.fieldsMeta) {
								if($scope.originalCurrentDataset.metadata.fieldsMeta[i].fieldType == "MEASURE") {
									var tmpField = angular.copy($scope.originalCurrentDataset.metadata.fieldsMeta[i]);
									tmpField.aliasToShow = tmpField.alias;
									ret.push(tmpField);
								}
							}
							return ret;
						}

						$scope.deleteCalculatedField = function(index) {

							$scope.localModel
								.content
								.crosstabDefinition
								.measures
								.splice(index, 1);
						}

						$scope.deleteCatalogFunction = function(localModel) {
							//clean columns
							colsToRemove = [];
							for (var i=0; i<$scope.localModel.content.crosstabDefinition.columns.length; i++) {
								var col = $scope.localModel.content.crosstabDefinition.columns[i];
								if (col.isFunction)
									colsToRemove.push(col);
							}
							for (var j=0; j<colsToRemove.length; j++) {
								var index=$scope.localModel.content.crosstabDefinition.columns.indexOf(colsToRemove[j]);
								$scope.localModel.content.crosstabDefinition.columns.splice(index,1);
							}
							//clean rows
							rowsToRemove = [];
							for (var i=0; i<$scope.localModel.content.crosstabDefinition.rows.length; i++) {
								var row = $scope.localModel.content.crosstabDefinition.rows[i];
								if (row.isFunction)
									rowsToRemove.push(row);
							}
							for (var j=0; j<rowsToRemove.length; j++) {
								var index=$scope.localModel.content.crosstabDefinition.rows.indexOf(rowsToRemove[j]);
								$scope.localModel.content.crosstabDefinition.rows.splice(index,1);
							}
							//clean measures
							measToRemove = [];
							for (var i=0; i<$scope.localModel.content.crosstabDefinition.measures.length; i++) {
								var meas = $scope.localModel.content.crosstabDefinition.measures[i];
								if (meas.isFunction)
									measToRemove.push(meas);
							}
							for (var j=0; j<measToRemove.length; j++) {
								var index=$scope.localModel.content.crosstabDefinition.measures.indexOf(measToRemove[j]);
								$scope.localModel.content.crosstabDefinition.measures.splice(index,1);
							}
						}

					$scope.checkAggregation = function(){
						var isAggregated;
						var firstColumn = $scope.localModel.content.crosstabDefinition.measures[0];
						if(firstColumn.funct != 'NONE') {
							isAggregated = true;
						} else {
							isAggregated = false;
						}
						for(var i in $scope.localModel.content.crosstabDefinition.measures){
							var column = $scope.localModel.content.crosstabDefinition.measures[i];
							if (!isAggregated && column.funct != "NONE") return false;
							if (isAggregated && column.funct == "NONE") return false;
						}
						return true;
					}

		    	    $scope.saveConfiguration=function(){
		    		  if($scope.localModel.dataset == undefined){
		  				$scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
		    			return;
		    		  }

		    		  if($scope.localModel.content.crosstabDefinition.config && $scope.localModel.content.crosstabDefinition.config.expandCollapseRows){
		    			 var exclusionCounter = 0;
		    			 for(var k in $scope.localModel.content.crosstabDefinition.measures){
		    				if($scope.localModel.content.crosstabDefinition.measures[k].excludeFromTotalAndSubtotal) exclusionCounter++;
		    			 }
		    			 if(exclusionCounter == $scope.localModel.content.crosstabDefinition.measures.length) {
		    				 $scope.showAction($scope.translate.load('sbi.cockpit.widgets.staticpivot.missingSubtotals'))
		    				 return;
		    			 }
		    		  }

		    		  if($scope.localModel.content.crosstabDefinition.measures.length == 0 ||
		    			($scope.localModel.content.crosstabDefinition.rows.length == 0 &&
		    			$scope.localModel.content.crosstabDefinition.columns.length ==0)
		    		  ){
		    			  $scope.showAction($scope.translate.load('sbi.cockpit.widgets.staticpivot.missingfield'));
		    			  return;
		    		  }
		    		  if ($scope.localModel.content.crosstabDefinition)
		    			  $scope.localModel.content.sortOptions =  fnOrder($scope.localModel.content.crosstabDefinition.columns, $scope.localModel.content.crosstabDefinition.rows) || {}; //update sorting
		    		  angular.copy($scope.localModel,model);//src, dest
		    		  mdPanelRef.close();
		    		  $scope.$destroy();
		    		  finishEdit.resolve();

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
			    		  $scope.$destroy();
			    		  finishEdit.reject();
			    	}

			    	$scope.getSorterByColumns = function(dataset){
			      		var sorterByColumns = [];
			      		sorterByColumns.push("");
			      		 for (m in dataset.metadata.fieldsMeta){
			      			 if (dataset.metadata.fieldsMeta[m].fieldType == 'ATTRIBUTE' && !$scope.isSelectedAttribute(dataset.metadata.fieldsMeta[m].name)){
			      				 sorterByColumns.push(dataset.metadata.fieldsMeta[m].name);
			      			 }
			      		 }
			      		 return sorterByColumns;
			      	 }

			    	$scope.isSelectedAttribute=function(attrName){
			    		for (c in $scope.localModel.content.crosstabDefinition.columns){
			    			if ($scope.localModel.content.crosstabDefinition.columns[c].id == attrName){
			    				return true;
			    			}
			    		}

			    		for (r in $scope.localModel.content.crosstabDefinition.rows){
			    			if ($scope.localModel.content.crosstabDefinition.rows[r].id == attrName){
			    				return true;
			    			}
			    		}
			    		return false;
			    	}

			    	 $scope.editFieldsProperty=function(selectedColumn){
			    		  selectedColumn.sorterByColumns = $scope.getSorterByColumns($scope.currentDataset); //load all columns to manage external sort column
			    		  if (model.content.crosstabDefinition){
				    		  var guiSortOptions = fnOrder(model.content.crosstabDefinition.columns, model.content.crosstabDefinition.rows);
				    		  var templateSortOptions = model.content.sortOptions;
				    		  if (!angular.equals(guiSortOptions, templateSortOptions)){
				    			  selectedColumn.showSortingAlert = true;
				    			  console.log("ATTENTION: The user had save sortings manually that are different by the configuration set. The system are using the first ones.");
				    			  console.log("templateSortOptions:", templateSortOptions);
				    			  console.log("guiSortOptions:", guiSortOptions);
				    		  }else{
				    				selectedColumn.showSortingAlert = false;
				    		  }
			    		  }
			    		  selectedColumn.fnOrder = fnOrder;
//			    		  selectedColumn.funct = "SUM";
			    		  $mdDialog.show({
								templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/staticPivotTableWidget/templates/staticPivotTableColumnStyle.html',
								parent : angular.element(document.body),
								clickOutsideToClose:true,
								escapeToClose :true,
								preserveScope: true,
								autoWrap:false,
								fullscreen: true,
								locals:{model:$scope.localModel, selectedColumn:selectedColumn},
								controller: cockpitStyleColumnFunction

							}).then(function(answer) {
								console.log("Selected column:", $scope.selectedColumn);

							}, function() {
								console.log("Selected column:", $scope.selectedColumn);
							});
					}

				},
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/staticPivotTableWidget/templates/staticPivotTableWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {finishEdit:finishEdit,model:$scope.ngModel,fnOrder:$scope.getSortOptions},
				onRemoving :function(){
					$scope.refreshWidget();
				}
		};

		$mdPanel.open(config);
		return finishEdit.promise;

	}


	function cockpitStyleColumnFunction($scope,sbiModule_translate,$mdDialog,model,selectedColumn,cockpitModule_datasetServices,cockpitModule_generalOptions,cockpitModule_properties, $mdToast,sbiModule_messaging, knModule_fontIconsService, cockpitModule_generalServices){
		$scope.translate=sbiModule_translate;
//		$scope.localModel = angular.copy(model);
		$scope.selectedColumn = angular.copy(selectedColumn);
		$scope.selectedColumn.fieldType = selectedColumn.nature.toUpperCase();
		$scope.selectedColumn.widgetType = "staticPivotTable";
		$scope.selectedColumn.showHeader = (selectedColumn.showHeader==undefined)?true:selectedColumn.showHeader;
		$scope.selectedColumn.excludeFromTotalAndSubtotal = (selectedColumn.excludeFromTotalAndSubtotal==undefined)?false:selectedColumn.excludeFromTotalAndSubtotal;
		if(cockpitModule_properties.VARIABLES &&  Object.keys(cockpitModule_properties.VARIABLES).length > 0) $scope.variables = cockpitModule_properties.VARIABLES;

		$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
		$scope.formatPattern = ['','#.###,##','#,###.##'];
		$scope.colorPickerProperty={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb'}
		$scope.visTypes=[{label:$scope.translate.load('kn.generic.visualization.text'), value:"Text"},
			{label:$scope.translate.load('kn.generic.visualization.texticon'),value: "Text/Icon"},
			{label:$scope.translate.load('kn.generic.visualization.icon'),value:"Icon only"}];
		function setChunks(array, dimension){
			var newArray = [];
			for(var f in array){
				var familyArray = {"name":array[f].name,"className":array[f].className,icons:[]};
				var iterator = 0;
				for(var k in array[f].icons){
					if (iterator == 0) var tempArray = [];
					if (iterator < dimension) {
						tempArray.push(array[f].icons[k]);
						iterator ++;
					}
					if (iterator == dimension) {
						familyArray.icons.push(tempArray);
						iterator = 0;
					}
				}
				newArray.push(familyArray);
			}

			return newArray;
		}

		$scope.availableAggregationFunctions = $scope.cockpitModule_generalOptions.aggregationFunctions.filter(function(el) {
			return el.value != "COUNT_DISTINCT";
		});
		$scope.availableIcons = setChunks(knModule_fontIconsService.icons,4);
		$scope.conditions=['>','<','==','>=','<=','!='];

		$scope.getTemplateUrl = function(template){
			return cockpitModule_generalServices.getTemplateUrl('tableWidget',template)
		}

		$scope.selectedColumn.disableShowHeader = false; //default is enabled: only for measures force disable if there are many measures
		if ($scope.selectedColumn.containerType && $scope.selectedColumn.containerType == 'MEASURE-PT'){
			if (model.content.crosstabDefinition.measures.length==1)
				$scope.selectedColumn.disableShowHeader  = false;
			else{
				$scope.selectedColumn.disableShowHeader  = true;
				$scope.selectedColumn.showHeader = true;
			}
		}

		if(!$scope.selectedColumn.hasOwnProperty('colorThresholdOptions'))
		{
			$scope.selectedColumn.colorThresholdOptions={};
			$scope.selectedColumn.colorThresholdOptions.condition=[];
			$scope.selectedColumn.colorThresholdOptions.condition2=[];
			for(var i=0;i<3;i++)
			{
				$scope.selectedColumn.colorThresholdOptions.condition[i]="none";
				$scope.selectedColumn.colorThresholdOptions.condition2[i]="none";
			}
		}
		//retrocompatibility check for old version
		if(!$scope.selectedColumn.colorThresholdOptions.hasOwnProperty('condition2')){
			$scope.selectedColumn.colorThresholdOptions.condition2=[];
			for(var i=0;i<3;i++){
				$scope.selectedColumn.colorThresholdOptions.condition2[i]="none";
			}
		}


		if($scope.selectedColumn.visType==undefined)
		{
			$scope.selectedColumn.visType="Text";
		}
		if($scope.selectedColumn.minValue==undefined||$scope.selectedColumn.minValue===''||$scope.selectedColumn.maxValue==undefined||$scope.selectedColumn.maxValue==='')
		{
			$scope.selectedColumn.minValue=0;
			$scope.selectedColumn.maxValue=100;
		}
		if($scope.selectedColumn.chartColor==undefined||$scope.selectedColumn.chartColor==='')
		{
			$scope.selectedColumn.chartColor="rgb(19, 30, 137)";
		}
		if($scope.selectedColumn.chartLength==undefined||$scope.selectedColumn.chartLength==='')
		{
			$scope.selectedColumn.chartLength=200;
		}

		//Ranges Management

		$scope.addRange = function(){
			if(!$scope.selectedColumn.ranges) $scope.selectedColumn.ranges = [];
			$scope.selectedColumn.ranges.push({});
		}

		$scope.deleteRange = function(hashkey){
			for(var i in $scope.selectedColumn.ranges){
				if($scope.selectedColumn.ranges[i].$$hashKey == hashkey){
					$scope.selectedColumn.ranges.splice(i,1);
					break;
				}
			}
		}

		$scope.openFamily = function(familyName){
			if($scope.iconFamily == familyName) $scope.iconFamily = "";
			else $scope.iconFamily = familyName;
		}

		$scope.chooseIcon = function(range) {
			$scope.tempVar = !$scope.tempVar;
			$scope.currentRange=range;
			$scope.iconFamily = $scope.availableIcons[0].name;

	  	}
		$scope.setIcon = function(family,icon){
			$scope.currentRange.icon = family.className+' '+icon.className;
			$scope.tempVar = !$scope.tempVar;
		}


		$scope.cleanStyleColumn = function(){
			$scope.selectedColumn.style = undefined;
		}

		$scope.checkPrecision = function(){
			if($scope.selectedColumn.style!=undefined && $scope.selectedColumn.style.format!=undefined && $scope.selectedColumn.style.precision!=undefined && !$scope.isPrecisionEnabled()){
				$scope.selectedColumn.style.precision = null;
			}
		}

		$scope.isPrecisionEnabled = function(){
			return $scope.selectedColumn.style;
		}

		$scope.saveColumnStyleConfiguration = function(){
			if($scope.selectedColumn.style!=undefined && $scope.selectedColumn.style.precision!=undefined && $scope.selectedColumn.style.format==undefined){
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.chartengine.structure.serieStyleConfig.dataLabels.format.emptyText'), sbiModule_translate.load('sbi.generic.error'));
				return;
			}
			$scope.checkPrecision();
			$scope.selectedColumn = $scope.cleanObjectConfiguration($scope.selectedColumn, 'style', false);
			$scope.selectedColumn = $scope.cleanObjectConfigurationForSaving($scope.selectedColumn);
			angular.copy($scope.selectedColumn,selectedColumn);
//			angular.copy($scope.localModel,model);

			$mdDialog.cancel();
		}

		$scope.cancelcolumnStyleConfiguration = function(){
			$mdDialog.cancel();
		}

		$scope.cleanObjectConfiguration = function(config, obj, admitObject){
			var toReturn = {};
			for (var c in config){
				if (c == obj){
					if (typeof config[c] == 'object'){
						var objProp = config[c];
						var propToReturn = {};
						for (p in objProp){
							if (!admitObject && p.startsWith("{\"")){
								continue;	//skip the object element. ONLY attribute are added
							}
							propToReturn[p] = objProp[p];
						}
						toReturn[c] = propToReturn;
					}
				}else
					toReturn[c] = config[c];
			}
			return toReturn;
		}

		$scope.cleanObjectConfigurationForSaving = function (config){
			var toReturn;
			//add all necessaries deletes for obj that don't want save (ex.. array of all columns for sorting by value, ...)
			if (config.sorterByColumns) delete config.sorterByColumns;

			toReturn = config;
			return toReturn;
		}


		$scope.checkIfDisable = function(){

			if($scope.selectedColumn.selectThreshold==true)
			{
				if($scope.selectedColumn.threshold==undefined||$scope.selectedColumn.threshold=="")
				{
					return true;
				}
			}

			if($scope.selectedColumn.maxValue==undefined || $scope.selectedColumn.minValue==undefined || $scope.selectedColumn.maxValue==="" || $scope.selectedColumn.minValue==="")
			{
				return true;
			}

//			for(var i=0;i<$scope.selectedColumn.scopeFunc.condition.length;i++)
//			{
//				if($scope.selectedColumn.scopeFunc.condition[i].condition!=undefined && $scope.selectedColumn.scopeFunc.condition[i].condition!="none")
//				{
//					if($scope.selectedColumn.scopeFunc.condition[i].value==="" || $scope.selectedColumn.scopeFunc.condition[i].value==undefined)
//					{
//						return true;
//					}
//				}
//			}
			return false;
		}

		 $scope.resetTemplateSortOptions=function(){
			 model.content.sortOptions=$scope.selectedColumn.fnOrder(model.content.crosstabDefinition.columns,model.content.crosstabDefinition.rows) || {};
	    		$scope.selectedColumn.showSortingAlert = false;
	     }

		 $scope.updateSortOptions=function(item){
			 console.log(item);
			 if (item.containerType == "ROWS"){
				 for (r in  model.content.crosstabDefinition.rows){
					 if (model.content.crosstabDefinition.rows[r].id == item.id){
						 model.content.crosstabDefinition.rows[r] = item;
						 break;
					 }
				 }
			 }

			 if (item.containerType == "COLUMNS"){
				 for (c in  model.content.crosstabDefinition.columns){
					 if (model.content.crosstabDefinition.columns[c].id == item.id){
						 model.content.crosstabDefinition.columns[c] = item;
						 break;
					 }
				 }
			 }
		 }
	}


	$scope.orderPivotTable=function(column, axis, globalId, measureLabel, parentValue){
		if($scope.ngModel.content.sortOptions==undefined){
			$scope.ngModel.content.sortOptions = $scope.getSortOptions($scope.ngModel.content.crosstabDefinition.columns, $scope.ngModel.content.crosstabDefinition.rows) || {} //initialization with template version
		}
		var axisConfig;
		if(axis==1){
			if (measureLabel){
				var previousSelection = $scope.getPreviousSelection($scope.ngModel.content.sortOptions.measuresSortKeys);
				if($scope.ngModel.content.sortOptions.measuresSortKeys==undefined || previousSelection != column){
					$scope.ngModel.content.sortOptions.measuresSortKeys = [];
				}
				var colDef = {};
				colDef.column = column;
				colDef.parentValue = parentValue;
				colDef.measureLabel = measureLabel;
				var exists = false;
                for(var i in $scope.ngModel.content.sortOptions.measuresSortKeys){
                    if($scope.ngModel.content.sortOptions.measuresSortKeys[i].column == column){
                        exists = true;
                        break;
                    }
                }
				if (!exists) $scope.ngModel.content.sortOptions.measuresSortKeys.push(colDef);
				axisConfig = $scope.ngModel.content.sortOptions.measuresSortKeys;
			}else{
				if($scope.ngModel.content.sortOptions.columnsSortKeys==undefined){
					$scope.ngModel.content.sortOptions.columnsSortKeys=[];
				}
				//reset measure ordering
				if($scope.ngModel.content.sortOptions.measuresSortKeys!=undefined ){
					$scope.ngModel.content.sortOptions.measuresSortKeys=undefined;
				}
				var exists = false;
                for(var i in $scope.ngModel.content.sortOptions.columnsSortKeys){
                    if($scope.ngModel.content.sortOptions.columnsSortKeys[i].column == column){
                        exists = true;
                        break;
                    }
                }
				if (!exists) $scope.ngModel.content.sortOptions.columnsSortKeys.push({'column': column});
				axisConfig = $scope.ngModel.content.sortOptions.columnsSortKeys;
			}
		}else{
			if (measureLabel){
				var previousSelection = $scope.getPreviousSelection($scope.ngModel.content.sortOptions.measuresSortKeys);
				if($scope.ngModel.content.sortOptions.measuresSortKeys==undefined || previousSelection != column){
					$scope.ngModel.content.sortOptions.measuresSortKeys=[];
				}
				var colDef = {};
				colDef.column = column;
				colDef.parentValue = parentValue;
				colDef.measureLabel = measureLabel;
				var exists = false;
                for(var i in $scope.ngModel.content.sortOptions.measuresSortKeys){
                    if($scope.ngModel.content.sortOptions.measuresSortKeys[i].column == column){
                        exists = true;
                        break;
                    }
                }
				if (!exists) $scope.ngModel.content.sortOptions.measuresSortKeys.push(colDef);
				axisConfig = $scope.ngModel.content.sortOptions.measuresSortKeys;
			}else{
				if($scope.ngModel.content.sortOptions.rowsSortKeys==undefined){
					$scope.ngModel.content.sortOptions.rowsSortKeys=[];
				}
				//reset measure ordering
				if($scope.ngModel.content.sortOptions.measuresSortKeys!=undefined ){
					$scope.ngModel.content.sortOptions.measuresSortKeys=undefined;
				}
				var exists = false;
				for(var i in $scope.ngModel.content.sortOptions.rowsSortKeys){
                    if($scope.ngModel.content.sortOptions.rowsSortKeys[i].column == column){
                        exists = true;
                        break;
                    }
                }
				if (!exists) $scope.ngModel.content.sortOptions.rowsSortKeys.push({'column': column});
				axisConfig = $scope.ngModel.content.sortOptions.rowsSortKeys;
			}
		}

		var direction = $scope.getItemDirection(axisConfig, column);
		if(!direction){
			direction = 1;
		}
		direction = direction*(-1);
		$scope.setItemDirection(axisConfig, column, direction);

		$scope.refreshWidget();
	}

	$scope.getPreviousSelection = function (keys){
		var toReturn = undefined;

		if(keys!=undefined){
			for (var m in keys){
				toReturn = keys[m].column;
				break;
			}
		}

		return toReturn;
	}

	$scope.getItemDirection = function(axisConf, idx){
		for (c in axisConf){
			if (axisConf[c].column == idx)
				return axisConf[c].direction;
		}
		return null;
	}

	$scope.setItemDirection = function(axisConf, idx, direction){
		for (c in axisConf){
			if (axisConf[c].column == idx)
				axisConf[c].direction = direction;
		}
	}

};


//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("static-pivot-table",{'initialDimension':{'width':10, 'height':10},'updateble':true,'cliccable':true});

})();