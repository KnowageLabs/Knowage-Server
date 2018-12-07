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
		.directive('cockpitHtmlWidget',function(){
			return{
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/htmlWidget/templates/htmlWidgetTemplate.html',
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
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_analyticalDrivers,
			cockpitModule_properties){
		
		//Regular Expressions used
		$scope.columnRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?\s?\])/g;
		$scope.aggregationRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-]+)[\']{1}(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?\])/g;
		$scope.paramsRegex = /(?:\[kn-parameter=[\'\"]{1}([a-zA-Z0-9\_\-]+)[\'\"]{1}\])/g;
		$scope.calcRegex = /(?:\[kn-calc=\(([\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\)(?:\s+precision=\'(\d)\')?\])/g;
		$scope.repeatIndexRegex = /\[kn-repeat-index\]/g;
		$scope.gt = /(\<.*kn-.*=["].*)(>)(.*["].*\>)/g;
		$scope.lt = /(\<.*kn-.*=["].*)(<)(.*["].*\>)/g;
				
		//dataset initializing and backward compatibilities checks
		if(!$scope.ngModel.dataset){$scope.ngModel.dataset = {}};
		if($scope.ngModel.datasetId){
			$scope.ngModel.dataset.dsId = $scope.ngModel.datasetId;
			delete $scope.ngModel.datasetId;
		}
		
		if(!$scope.ngModel.settings) $scope.ngModel.settings = {};
		
		$scope.refresh = function(element,width,height, datasetRecords,nature) {
			$scope.showWidgetSpinner();
			if(datasetRecords) $scope.htmlDataset = datasetRecords;
			$scope.manageHtml();
			$scope.hideWidgetSpinner();
		}
		
		/**
		 * Function to initialize the rendered html at the loading and after editing.
		 * If there is a selected dataset the function calls the data rest service.
		 */
		$scope.reinit = function(){
			$scope.showWidgetSpinner();
			if($scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
				sbiModule_restServices.restToRootProject();
				var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
				$scope.ngModel.content.columnSelectedOfDataset = dataset.metadata.fieldsMeta;
				//getting dataset parameters if available
//				$scope.params = cockpitModule_datasetServices.getDatasetParameters($scope.ngModel.dataset.dsId);
//				for(var p in $scope.params){
//					if($scope.params[p].length == 1){
//						$scope.params[p] = $scope.params[p][0];
//					}
//				}
				cockpitModule_datasetServices.loadDatasetRecordsById($scope.ngModel.dataset.dsId, 0, -1, undefined, undefined, $scope.ngModel, undefined).then(
					function(data){
						$scope.htmlDataset = data;
						$scope.manageHtml();
					},function(error){
						$scope.hideWidgetSpinner();
					});
			}else {
				$scope.trustedCss = $sce.trustAsHtml('<style>'+$scope.ngModel.cssToRender+'</style>');
				if($scope.ngModel.htmlToRender){
					$scope.checkParamsPlaceholders($scope.ngModel.htmlToRender).then(function(placeholderResultHtml){
						$scope.trustedHtml = $sce.trustAsHtml($scope.parseCalc("<div>" + placeholderResultHtml +" </div>"));
					})
				}
				$scope.hideWidgetSpinner();
			}
		}

		//Core wrapper function to prepare css and styles to be parsed
		$scope.manageHtml = function(){
			if($scope.ngModel.cssToRender){
				$scope.checkPlaceholders($scope.ngModel.cssToRender).then(
						function(placeholderResultCss){
							placeholderResultCss = $scope.parseCalc(placeholderResultCss);
							$scope.trustedCss = $sce.trustAsHtml('<style>'+placeholderResultCss+'</style>');
						}
					)
			}
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
		}

		//Get the dataset column name from the readable name. ie: 'column_1' for the name 'id'
		$scope.getColumnFromName = function(name,ds,aggregation){
			for(var i in ds.metaData.fields){
				if(typeof ds.metaData.fields[i].header != 'undefined' && ds.metaData.fields[i].header == (aggregation ? name+'_'+aggregation : name)){
					return ds.metaData.fields[i].name;
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
				var aggregationsReg = rawHtml.match($scope.aggregationRegex);
				if(aggregationsReg) {
					var tempModel = angular.copy($scope.ngModel);
					delete tempModel.settings;
					tempModel.content.columnSelectedOfDataset = [];
					var tempDataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId)
					for(var a in aggregationsReg){
						var aggRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-]+)[\']{1}(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1})?(?:\s+precision=\'(\d)\')?\])/;
						var aggregationReg = aggRegex.exec(aggregationsReg[a]);
						for(var m in tempDataset.metadata.fieldsMeta){
							if(tempDataset.metadata.fieldsMeta[m].name == aggregationReg[1]){
								tempDataset.metadata.fieldsMeta[m].alias = aggregationReg[1]+'_'+aggregationReg[2];
								tempDataset.metadata.fieldsMeta[m].aggregationSelected = aggregationReg[2];
								tempModel.content.columnSelectedOfDataset.push(angular.copy(tempDataset.metadata.fieldsMeta[m]));
							}
						}
					}
					
					cockpitModule_datasetServices.loadDatasetRecordsById($scope.ngModel.dataset.dsId, 0, -1, undefined, undefined, tempModel, undefined).then(
							function(data){
								$scope.aggregationDataset = data;
								allElements = $scope.parseRepeat(allElements);
								allElements = $scope.parseIf(allElements);
								resolve(parsedHtml);
							},function(error){
								$scope.hideWidgetSpinner();
								reject(error);
							});
				}else{
					allElements = $scope.parseRepeat(allElements);
					allElements = $scope.parseIf(allElements);
					resolve(parsedHtml);
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
				if (allElements[i] && allElements[i].hasAttribute("kn-repeat")){
					if(eval($scope.checkAttributePlaceholders(allElements[i].getAttribute('kn-repeat')))){
						allElements[i].removeAttribute("kn-repeat");
						var limit = allElements[i].hasAttribute("limit") && (allElements[i].hasAttribute("limit") <= $scope.htmlDataset.rows.length) ? allElements[i].getAttribute('limit') : $scope.htmlDataset.rows.length;
				    	var repeatedElement = angular.copy(allElements[i]);
				    	var tempElement;
				    	for(var r = 0; r<limit; r++){
				    		var tempRow = angular.copy(repeatedElement);
				    		tempRow.innerHTML =  tempRow.innerHTML.replace($scope.columnRegex, function(match,c1,c2,c3, precision){
				    			var precisionPlaceholder = '';
				    			if(precision) precisionPlaceholder = " precision='"+precision+"'";
								return "[kn-column=\'"+c1+"\' row=\'"+(c2||r)+"\'" + precisionPlaceholder + "]";
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
				    	condition = condition.replace($scope.paramsRegex, $scope.paramsReplacer);
				    	condition = condition.replace($scope.calcRegex, $scope.calcReplacer);
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
		
		/**
		 * Function to replace kn-calc placeholders
		 */
		$scope.parseCalc = function(rawHtml) {
			return rawHtml.replace($scope.calcRegex, $scope.calcReplacer);
		}
		
		/**
		 * Check the existence of placeholder inside the raw html.
		 * If there is a match the placeholder is replaced with the dataset value for that column.
		 * If the row is not specified the first one is returned.
		 */
		$scope.checkPlaceholders = function(rawHtml){
			return $q(function(resolve, reject) {
				var resultHtml = rawHtml.replace($scope.columnRegex, $scope.replacer);
				resultHtml = resultHtml.replace($scope.paramsRegex, $scope.paramsReplacer);
				resolve(resultHtml);
			})
		}
		
		$scope.checkParamsPlaceholders = function(rawHtml){
			return $q(function(resolve, reject) {
				var resultHtml = rawHtml.replace($scope.paramsRegex, function(match, p1) {
					p1=cockpitModule_analyticalDrivers[p1];
					return p1;
				});
				resolve(resultHtml);
			})
		}
		
		//Replacers
		$scope.calcReplacer = function(match,p1,precision){
			return (precision && !isNaN(eval(p1)))? parseFloat(eval(p1)).toFixed(precision) : eval(p1);
		}
		
		$scope.ifConditionReplacer = function(match, p1, p2, aggr, precision){
			if(aggr){
				p1=$scope.aggregationDataset && $scope.aggregationDataset.rows[0] && typeof($scope.aggregationDataset.rows[0][$scope.getColumnFromName(p1,$scope.aggregationDataset,aggr)])!='undefined' ? $scope.aggregationDataset.rows[0][$scope.getColumnFromName(p1,$scope.aggregationDataset,aggr)] : 'null';
			}
			else if($scope.htmlDataset.rows[p2||0] && $scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1,$scope.htmlDataset)]){
				p1 = typeof($scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1,$scope.htmlDataset)]) == 'string' ? '\''+$scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1,$scope.htmlDataset)]+'\'' : $scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1,$scope.htmlDataset)];
			}else {
				p1 = 'null';
			}
			return (precision && !isNaN(p1))? parseFloat(p1).toFixed(precision) : p1;
		}
		
		$scope.replacer = function(match, p1, p2, p3, precision) {
			if(p3){
				p1=$scope.aggregationDataset && $scope.aggregationDataset.rows[0] && typeof($scope.aggregationDataset.rows[0][$scope.getColumnFromName(p1,$scope.aggregationDataset,p3)])!='undefined' ? $scope.aggregationDataset.rows[0][$scope.getColumnFromName(p1,$scope.aggregationDataset,p3)] : 'null';
			}else{
				p1=$scope.htmlDataset.rows[p2||0] && typeof($scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1,$scope.htmlDataset)])!='undefined' ? $scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1,$scope.htmlDataset)] : 'null';
			}
			return (precision && !isNaN(p1))? parseFloat(p1).toFixed(precision) : p1;
			
		}
		$scope.paramsReplacer = function(match, p1){
			p1=cockpitModule_analyticalDrivers[p1];
			return p1;
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
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/htmlWidget/templates/htmlWidgetEditPropertyTemplate.html',
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

		$scope.reinit();

	}

	function htmlWidgetEditControllerFunction($scope,finishEdit,model,sbiModule_translate,$mdDialog,mdPanelRef,$mdToast,$timeout){
		$scope.translate=sbiModule_translate;
		$scope.newModel = angular.copy(model);

		if($scope.newModel.cssOpened) $scope.newModel.cssOpened = false;

		$scope.toggleCss = function() {
			$scope.newModel.cssOpened = !$scope.newModel.cssOpened;
		}

        //codemirror initializer
        $scope.codemirrorLoaded = function(_editor) {
            $scope._doc = _editor.getDoc();
            $scope._editor = _editor;
            _editor.focus();
            $scope._doc.markClean()
            _editor.on("beforeChange", function() {});
            _editor.on("change", function() {});
        };

        //codemirror options
        $scope.editorOptionsCss = {
            theme: 'eclipse',
            lineWrapping: true,
            lineNumbers: true,
            mode: {name:'css'},
            onLoad: $scope.codemirrorLoaded
        };
        $scope.editorOptionsHtml = {
            theme: 'eclipse',
            lineWrapping: true,
            lineNumbers: true,
            mode: {name: "xml", htmlMode: true},
            onLoad: $scope.codemirrorLoaded
        };

		$scope.saveConfiguration=function(){
			 mdPanelRef.close();
			 angular.copy($scope.newModel,model);
			 finishEdit.resolve();
   	  	}
   	  	$scope.cancelConfiguration=function(){
   	  		mdPanelRef.close();
   	  		finishEdit.reject();
   	  	}

	}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("html",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();