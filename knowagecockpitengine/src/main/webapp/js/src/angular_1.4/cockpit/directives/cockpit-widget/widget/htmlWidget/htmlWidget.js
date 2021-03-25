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
	angular
		.module('cockpitModule')
		.directive('cockpitHtmlWidget',function(sbiModule_config){
			return{
				templateUrl: sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/directives/cockpit-widget/widget/htmlWidget/templates/htmlWidgetTemplate.html',
				controller: cockpitHtmlWidgetControllerFunction,
				compile: function (tElement, tAttrs, transclude) {
					return {
						pre: function preLink(scope, element, attrs, ctrl, transclud) {

						},
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							element.ready(function () {
								scope.initWidget();
							});
						}
					};
				}
			}
		})

		.directive('bindHtmlCompile', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                scope.$watch(function () {
                    return scope.$eval(attrs.bindHtmlCompile);
                }, function (value) {
                    element.html(value && value.toString());
                    var compileScope = scope;
                    if (attrs.bindHtmlScope) {
                        compileScope = scope.$eval(attrs.bindHtmlScope);
                    }
                    $compile(element.contents())(compileScope);
                });
            }
        };
    }])

	function cockpitHtmlWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdToast,
			$timeout,
			$mdPanel,
			$q,
			$sce,
			$filter,
			sbiModule_translate,
			sbiModule_restServices,
			cockpitModule_datasetServices,
			cockpitModule_generalServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_analyticalDrivers,
			cockpitModule_properties,
			cockpitModule_template){

		$scope.getTemplateUrl = function(template){
	  		return $sce.trustAsResourceUrl(cockpitModule_generalServices.getTemplateUrl('htmlWidget',template));
	  	}


		//Regular Expressions used
		$scope.widgetIdRegex = /\[kn-widget-id\]/g;
		$scope.activeSelectionsRegex = /(?:\[kn-active-selection(?:=\'([a-zA-Z0-9\_\-]+)\')?\s?\])/g;
		$scope.columnRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g;
		$scope.rowsRegex   = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d+)\'){1}(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g;
		$scope.noAggregationsExistRegex = /\[kn-column=\'[a-zA-Z0-9\_\-\s]+\'(?:\s+row=\'\d+\')?(?!\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')(?:\s+precision=\'(?:\d)\')?(?:\s+format)?\s?\]/g;
		$scope.limitRegex = /<[\s\w\=\"\'\-\[\]]*(?!limit=)"([\-\d]+)"[\s\w\=\"\'\-\[\]]*>/g;
		$scope.aggregationsRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?\])/g;
		$scope.aggregationRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?\])/;
		$scope.paramsRegex = /(?:\[kn-parameter=[\'\"]{1}([a-zA-Z0-9\_\-\s]+)[\'\"]{1}\])/g;
		$scope.calcRegex = /(?:\[kn-calc=\(([\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\)(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g;
		$scope.advancedCalcRegex = /(?:\[kn-calc=\{([\(\)\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\}(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g;
		$scope.repeatIndexRegex = /\[kn-repeat-index\]/g;
		$scope.variablesRegex = /(?:\[kn-variable=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+key=\'([a-zA-Z0-9\_\-\s]+)\')?\s?\])/g;
		$scope.i18nRegex = /(?:\[kn-i18n=\'([a-zA-Z0-9\_\-\s]+)\'\s?\])/g;
		$scope.gt = /(\<.*kn-.*=["].*)(>)(.*["].*\>)/g;
		$scope.lt = /(\<.*kn-.*=["].*)(<)(.*["].*\>)/g;

		if(!$scope.ngModel.cross) $scope.ngModel.cross = {};
		//dataset initializing and backward compatibilities checks
		if(!$scope.ngModel.dataset) $scope.ngModel.dataset = {};
		if($scope.ngModel.datasetId){
			$scope.ngModel.dataset.dsId = $scope.ngModel.datasetId;
			delete $scope.ngModel.datasetId;
		}

		$scope.showPreview = function(datasetLabel){
			var dataset = cockpitModule_datasetServices.getDatasetByLabel(datasetLabel);
			$scope.doSelection(null, null, null, null, null, null, dataset.id.dsId, null);
		}

		$scope.select = function(column,value){
			$scope.doSelection(column, value || $scope.htmlDataset.rows[0][$scope.getColumnFromName(column,$scope.htmlDataset).name], null, null, null, null, $scope.ngModel.dataset.dsId, null);
		}

		if(!$scope.ngModel.settings) $scope.ngModel.settings = {};

		$scope.refresh = function(element,width,height, datasetRecords,nature) {
			$scope.showWidgetSpinner();
			if(datasetRecords) $scope.htmlDataset = datasetRecords;
			$scope.manageHtml();
			if(nature == 'init'){
				$timeout(function(){
					$scope.widgetIsInit=true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				},500);
			}
		}

		$scope.init=function(element,width,height){
			$scope.showWidgetSpinner();
			if($scope.ngModel.htmlToRender.search($scope.noAggregationsExistRegex) == -1) $scope.refresh(element,width,height,null,'init')
			else $scope.refreshWidget(null, 'init');
		}

		$scope.getOptions = function(){
			var maxRows = $scope.maxRow();
			var obj = {};
				obj["page"] = 0;
				obj["itemPerPage"] = -1;
				obj["type"] = 'html';
			return obj;
		}

		/**
		 * Function to initialize the rendered html at the loading and after editing.
		 * If there is a selected dataset the function calls the data rest service.
		 */
		$scope.reinit = function(){
			$scope.showWidgetSpinner();
			if($scope.ngModel.dataset && $scope.ngModel.dataset.dsId && $scope.ngModel.htmlToRender.search($scope.noAggregationsExistRegex) != -1){
				sbiModule_restServices.restToRootProject();

				cockpitModule_datasetServices.loadDatasetRecordsById($scope.ngModel.dataset.dsId, 0, $scope.maxRow(), undefined, undefined, $scope.ngModel, undefined).then(
					function(data){
						$scope.htmlDataset = data;
						$scope.manageHtml();
					},function(error){
						$scope.hideWidgetSpinner();
					});
			}else if($scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
				$scope.refreshWidget();
			}else{
				$scope.manageHtml();
			}
		}

		$scope.maxRow = function(){
			var str = $scope.ngModel.cssToRender + $scope.ngModel.htmlToRender;
			var tempMaxRow = 1;
			var repeaters = str.replace($scope.limitRegex, function(match, p1){
				if(parseInt(p1) == -1) tempMaxRow = -1; 
				else if(p1>tempMaxRow) tempMaxRow = parseInt(p1)+1;
			})
			var occurrencies = str.replace($scope.rowsRegex,function(match,p1,p2){
				if(p2>=tempMaxRow) tempMaxRow = parseInt(p2)+1;
			});
			$scope.ngModel.limitRows = {enable:true,rows:tempMaxRow};
			return tempMaxRow;
		}

		//Core wrapper function to prepare css and styles to be parsed
		$scope.manageHtml = function(){
			if($scope.datasetLabel) delete $scope.datasetLabel;
			if($scope.ngModel.dataset.dsId) $scope.datasetLabel = cockpitModule_datasetServices.getDatasetLabelById($scope.ngModel.dataset.dsId);
			$scope.parseAggregations($scope.ngModel.cssToRender + $scope.ngModel.htmlToRender).then(function(resultHtml){
				if($scope.ngModel.cssToRender){
					$scope.checkPlaceholders($scope.ngModel.cssToRender).then(
							function(placeholderResultCss){
								placeholderResultCss = $scope.parseCalc(placeholderResultCss);
								$scope.trustedCss = $sce.trustAsHtml('<style>'+placeholderResultCss+'</style>');
							}
						)
				}
				if($scope.ngModel.htmlToRender){
					var wrappedHtmlToRender = "<div>" + $scope.ngModel.htmlToRender +" </div>";

					 //Escaping the illegal parsable characters < and >, or the parsing will throw an error
					wrappedHtmlToRender = wrappedHtmlToRender.replace($scope.gt, '$1&gt;$3');
					wrappedHtmlToRender = wrappedHtmlToRender.replace($scope.lt, '$1&lt;$3');

					$scope.parseHtmlFunctions(wrappedHtmlToRender).then(
						function(resultHtml){
							$scope.checkPlaceholders(resultHtml.firstChild.innerHTML).then(
								function(placeholderResultHtml){
									placeholderResultHtml = $scope.parseCalc(placeholderResultHtml);
									$scope.trustedHtml = $sce.trustAsHtml(placeholderResultHtml);
									$scope.hideWidgetSpinner();
								}
							)
						}
					)
				}else $scope.hideWidgetSpinner();
			},function(error){
				$scope.hideWidgetSpinner();
			})
		}

		//Get the dataset column name from the readable name. ie: 'column_1' for the name 'id'
		$scope.getColumnFromName = function(name,ds,aggregation){
			for(var i in ds.metaData.fields){
				if(typeof ds.metaData.fields[i].header != 'undefined' && ds.metaData.fields[i].header.toLowerCase() == (aggregation ? name+'_'+aggregation : name).toLowerCase()){
					return {'name':ds.metaData.fields[i].name,'type':ds.metaData.fields[i].type };
				}
			}
		}

		/**
		 * Promise to get the functions inside the html, returns the parsed html
		 */
		$scope.parseHtmlFunctions = function(rawHtml){
			return $q(function(resolve, reject) {
				var parser = new DOMParser()
				var parsedHtml = parser.parseFromString(rawHtml, "text/html");
				var allElements = parsedHtml.getElementsByTagName('*');
				allElements = $scope.parseRepeat(allElements);
				allElements = $scope.parseIf(allElements);
				allElements = $scope.parseAttrs(allElements);
				resolve(parsedHtml);
			})
		}

		$scope.parseAggregations = function(rawHtml){
			return $q(function(resolve, reject) {
				var aggregationsReg = rawHtml.match($scope.aggregationsRegex);
				if(aggregationsReg) {
					var tempModel = angular.copy($scope.ngModel);
					delete tempModel.settings;
					tempModel.content.columnSelectedOfDataset = [];
					var tempDataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId)
					for(var a in aggregationsReg){
						var aggregationReg = $scope.aggregationRegex.exec(aggregationsReg[a]);
						for(var m in tempDataset.metadata.fieldsMeta){
							if(aggregationReg && aggregationReg[1] && tempDataset.metadata.fieldsMeta[m].name == aggregationReg[1]){
								tempDataset.metadata.fieldsMeta[m].alias = aggregationReg[1]+'_'+aggregationReg[2];
								tempDataset.metadata.fieldsMeta[m].fieldType = 'MEASURE';
								tempDataset.metadata.fieldsMeta[m].aggregationSelected = aggregationReg[2];
								var exists = false;
								for(var c in tempModel.content.columnSelectedOfDataset){
									if(tempModel.content.columnSelectedOfDataset[c].alias == aggregationReg[1]+'_'+aggregationReg[2]) exists = true;
								}
								if(!exists) tempModel.content.columnSelectedOfDataset.push(angular.copy(tempDataset.metadata.fieldsMeta[m]));
							}
						}
					}

					cockpitModule_datasetServices.loadDatasetRecordsById($scope.ngModel.dataset.dsId, 0, -1, undefined, undefined, tempModel, undefined).then(
						function(data){
							$scope.aggregationDataset = data;
							resolve();
						},function(error){
							reject();
						});
				}else{
					resolve();
				}
			})
		}

		/**
		 * Function to control the kn-repeat attributes and iterations
		 * @KN-REPEAT condition to verify the repeat, works like a KN-IF
		 * @LIMIT (number) returns just the specified number of rows from the dataset
		 */
		$scope.parseRepeat = function(allElements) {
			var i=0;
			do {
				if(!allElements[i].innerHTML) allElements[i].innerHTML = ' ';
				if(allElements[i] && allElements[i].hasAttribute("kn-repeat")){
					if(eval($scope.checkAttributePlaceholders(allElements[i].getAttribute('kn-repeat')))){
						allElements[i].removeAttribute("kn-repeat");
						var limit = allElements[i].hasAttribute("limit") && (allElements[i].hasAttribute("limit") <= $scope.htmlDataset.rows.length) ? allElements[i].getAttribute('limit') : $scope.htmlDataset.rows.length;
						if(allElements[i].hasAttribute("limit") && allElements[i].getAttribute('limit') == -1) limit = $scope.htmlDataset.rows.length;
						if(allElements[i].hasAttribute("limit")) allElements[i].removeAttribute("limit");
				    	var repeatedElement = angular.copy(allElements[i]);
				    	var tempElement;
				    	for(var r = 0; r<limit; r++){
				    		var tempRow = angular.copy(repeatedElement);
				    		tempRow.innerHTML =  tempRow.innerHTML.replace($scope.columnRegex, function(match,c1,c2,c3, precision,format){
				    			var precisionPlaceholder = '';
				    			var formatPlaceholder = '';
				    			if(format) var formatPlaceholder = ' format';
				    			if(precision) precisionPlaceholder = " precision='" + precision + "'";
								return "[kn-column=\'"+c1+"\' row=\'"+(c2||r)+"\'" + precisionPlaceholder + formatPlaceholder + "]";
							});
				    		tempRow.innerHTML = tempRow.innerHTML.replace($scope.repeatIndexRegex, r);
				    		if(r==0){
				    			tempElement = tempRow.outerHTML;
				    		}else{
				    			tempElement += tempRow.outerHTML;
				    		}
						}
				    	allElements[i].outerHTML = tempElement;
					}else{
						allElements[i].outerHTML = "";
					}
			    } i++;
			} while (i<allElements.length);
			return allElements;
		}

		/**
		 * Function to show an element only if a condition is specified
		 * The eval works after a placeholder replacement so other tags like [kn-column] can be used inside the condition.
		 * @KN-IF condition to verify, if true the element will be show, else it will be deleted from the dom.
		 */
		$scope.parseIf = function(allElements) {
			var j = 0;
			var nodesNumber = allElements.length;
			do {
				  if (allElements[j] && allElements[j].hasAttribute("kn-if")){
				    	var condition = allElements[j].getAttribute("kn-if").replace($scope.columnRegex, $scope.ifConditionReplacer);
				    	condition = condition.replace($scope.activeSelectionsRegex, $scope.activeSelectionsReplacer);
				    	condition = condition.replace($scope.paramsRegex, $scope.ifConditionParamsReplacer);
				    	condition = condition.replace($scope.calcRegex, $scope.calcReplacer);
				    	condition = condition.replace($scope.variablesRegex, $scope.variablesReplacer);
				    	condition = condition.replace($scope.i18nRegex, $scope.i18nReplacer);
				    	if(eval(condition)){
				    		allElements[j].removeAttribute("kn-if");
				    	}else{
				    		allElements[j].parentNode.removeChild(allElements[j]);
				    		j--;
				    	}
				    }
				  j++;

			 } while (j<nodesNumber);
			return allElements;
		}

		$scope.parseAttrs = function(allElements) {
			var j = 0;
			var nodesNumber = allElements.length;
			do {
				  if (allElements[j] && allElements[j].hasAttribute("kn-preview")){
				    	var datasetPreviewLabel = allElements[j].getAttribute("kn-preview");
				    	allElements[j].setAttribute("ng-click", "showPreview('" + datasetPreviewLabel + "')");
				    }
				  if (allElements[j] && allElements[j].hasAttribute("kn-cross")){
				    	allElements[j].setAttribute("ng-click", "doSelection(null,'"+allElements[j].getAttribute("kn-cross")+"')");
				    }
				  if (allElements[j] && allElements[j].hasAttribute("kn-selection-column")){
					  	var columnSelectionLabel = allElements[j].getAttribute("kn-selection-column");
					  	var columnSelectionValue = allElements[j].getAttribute("kn-selection-value");
					  	if(columnSelectionValue.charAt(0) != "[") columnSelectionValue = "'"+columnSelectionValue+"'";
					  	allElements[j].setAttribute("ng-click", columnSelectionValue ? "select('"+ columnSelectionLabel +"',"+columnSelectionValue +")" : "select('"+ columnSelectionLabel +"')");
				    }
				  j++;

			 } while (j<nodesNumber);
			return allElements;
		}

		/**
		 * Function to replace kn-calc placeholders
		 */
		$scope.parseCalc = function(rawHtml) {
			rawHtml = rawHtml.replace($scope.advancedCalcRegex, $scope.calcReplacer);
			rawHtml = rawHtml.replace($scope.calcRegex, $scope.calcReplacer);
			return rawHtml;
		}

		/**
		 * Check the existence of placeholder inside the raw html.
		 * If there is a match the placeholder is replaced with the dataset value for that column.
		 * If the row is not specified the first one is returned.
		 */
		$scope.checkPlaceholders = function(rawHtml){
			return $q(function(resolve, reject) {
				var resultHtml = rawHtml;
				if($scope.datasetLabel) {
					resultHtml = resultHtml.replace($scope.columnRegex, $scope.replacer);
					resultHtml = resultHtml.replace($scope.activeSelectionsRegex, $scope.activeSelectionsReplacer);
				}
				resultHtml = resultHtml.replace($scope.widgetIdRegex, 'w'+$scope.ngModel.id);
				resultHtml = resultHtml.replace($scope.paramsRegex, $scope.paramsReplacer);
				resultHtml = resultHtml.replace($scope.variablesRegex, $scope.variablesReplacer);
				resultHtml = resultHtml.replace($scope.i18nRegex, $scope.i18nReplacer);
				resolve(resultHtml);
			})
		}

		$scope.checkParamsPlaceholders = function(rawHtml){
			return $q(function(resolve, reject) {
				var resultHtml = rawHtml.replace($scope.paramsRegex, function(match, p1) {
					p1 = cockpitModule_analyticalDrivers[p1] || null;
					return p1;
				});
				resolve(resultHtml);
			})
		}

		//Replacers
		$scope.activeSelectionsReplacer = function(match,column){
			if(cockpitModule_template.getSelections() && cockpitModule_template.getSelections().length > 0){
				var selections = cockpitModule_template.getSelections();
				for(var k in selections){
					if(selections[k].ds == $scope.datasetLabel && selections[k].columnName == column) return selections[k].value;
				}
			}
			return null;
		}

		$scope.calcReplacer = function(match,p1,min,max,precision,format){
			var result = eval(p1);
			if(min && result < min) result = min;
			if(max && result > max) result = max;
			if(format) return precision ? $filter('number')(result, precision) : $filter('number')(result);
			return (precision && !isNaN(result))? parseFloat(result).toFixed(precision) : result;
		}

		$scope.ifConditionReplacer = function(match, p1, row, aggr, precision){
			var columnInfo = $scope.getColumnFromName(p1,aggr ? $scope.aggregationDataset : $scope.htmlDataset ,aggr);
			if(!columnInfo) return p1;
			if(aggr){
				p1 = $scope.aggregationDataset && $scope.aggregationDataset.rows[0] && $scope.aggregationDataset.rows[0][columnInfo.name] !== "" && typeof($scope.aggregationDataset.rows[0][columnInfo.name])!='undefined' ? $scope.aggregationDataset.rows[0][columnInfo.name] : null;
			}
			else if($scope.htmlDataset && $scope.htmlDataset.rows[row||0] && typeof($scope.htmlDataset.rows[row||0][columnInfo.name])!='undefined' && $scope.htmlDataset.rows[row||0][columnInfo.name] !== ""){
				var columnValue = $scope.htmlDataset.rows[row||0][columnInfo.name];
				if(typeof columnValue == 'string') columnValue = columnValue.replace("'","\\'");
				p1 = columnInfo.type == 'string' ? '\''+columnValue+'\'' : columnValue;
			}else {
				p1 = null;
			}
			return (precision && !isNaN(p1))? parseFloat(p1).toFixed(precision) : p1;
		}

		$scope.ifConditionParamsReplacer = function(match, p1){
			var textToReturn = (cockpitModule_analyticalDrivers[p1] || null);
			if(typeof(cockpitModule_analyticalDrivers[p1]) == 'string'){
				textToReturn = '\''+cockpitModule_analyticalDrivers[p1].replace("'","\\'")+'\''
			}
			return textToReturn;
		}

		$scope.replacer = function(match, p1, row, aggr, precision,format) {
			var columnInfo = $scope.getColumnFromName(p1,aggr ? $scope.aggregationDataset : $scope.htmlDataset ,aggr);
			if(!columnInfo) return p1;
			if(aggr){
				p1 = $scope.aggregationDataset && $scope.aggregationDataset.rows[0] && $scope.aggregationDataset.rows[0][columnInfo.name] !== "" && typeof($scope.aggregationDataset.rows[0][columnInfo.name])!='undefined' ? $scope.aggregationDataset.rows[0][columnInfo.name] : null;
			}else if($scope.htmlDataset && $scope.htmlDataset.rows[row||0] && typeof($scope.htmlDataset.rows[row||0][columnInfo.name])!='undefined' && $scope.htmlDataset.rows[row||0][columnInfo.name] !== ""){
				p1 = $scope.htmlDataset.rows[row||0][columnInfo.name];
			}else {
				p1 = null;
			}
			if(p1 != null && columnInfo.type == 'int' || columnInfo.type == 'float'){
				if(format) p1 = precision ? $filter('number')(p1, precision) : $filter('number')(p1);
				else p1 = precision ? parseFloat(p1).toFixed(precision) : parseFloat(p1);
			}
			return p1;

		}
		$scope.paramsReplacer = function(match, p1){
			p1 = cockpitModule_analyticalDrivers[p1] || null;
			return p1;
		}

		$scope.variablesReplacer = function(match, p1, p2){
			if(!cockpitModule_properties.VARIABLES[p1]) return null;
			if(p2) return cockpitModule_properties.VARIABLES[p1][p2] || null;
			else return cockpitModule_properties.VARIABLES[p1] || null;
		}
		
		$scope.i18nReplacer = function(match, p1){
			return $filter('i18n')(p1);
		}

		$scope.checkAttributePlaceholders = function(rawAttribute){
			var resultAttribute = rawAttribute.replace($scope.columnRegex, $scope.replacer);
			resultAttribute = resultAttribute.replace($scope.paramsRegex, $scope.paramsReplacer);
			return resultAttribute;
		}

		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: htmlWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: $scope.getTemplateUrl('htmlWidgetEditPropertyTemplate'),
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: false,
					locals: {finishEdit:finishEdit,model:$scope.ngModel},
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}

	}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("html",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();
