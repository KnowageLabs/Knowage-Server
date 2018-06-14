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
			cockpitModule_properties){
		
		//Regular Expressions used
		$scope.columnRegex = /(?:\[kn-column=[\'\"]{1}([a-zA-Z0-9\_\-\s\(\)]+)[\'\"]{1}(?:\s+row=[\'\"]{1}(\d*)[\'\"]{1})?\])/g;
		$scope.paramsRegex = /(?:\[kn-parameter=[\'\"]{1}([a-zA-Z0-9\_\-]+)[\'\"]{1}\])/g;
		$scope.repeatIndexRegex = /\[kn-repeat-index\]/g;
		$scope.gt = /(\<.*kn-.*=["].*)(>)(.*["].*\>)/g;
		$scope.lt = /(\<.*kn-.*=["].*)(<)(.*["].*\>)/g;
		
		//dataset initializing and backward compatibilities checks
		if(!$scope.ngModel.dataset){$scope.ngModel.dataset = ''};
		if($scope.ngModel.datasetId){
			$scope.ngModel.dataset.dsId = $scope.ngModel.datasetId;
			delete $scope.ngModel.datasetId;
		}
		
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
			if($scope.ngModel.dataset){
				sbiModule_restServices.restToRootProject();
				var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
				
				//getting dataset parameters if available
				$scope.params = cockpitModule_datasetServices.getDatasetParameters($scope.ngModel.dataset.dsId);
				for(var p in $scope.params){
					if($scope.params[p].length == 1){
						$scope.params[p] = $scope.params[p][0];
					}
				}
				sbiModule_restServices.promisePost("2.0/datasets", encodeURIComponent(dataset.label) + "/data?nearRealtime=" + !dataset.useCache,$scope.params && JSON.stringify({"parameters": $scope.params})).then(function(data){
					$scope.htmlDataset = data.data;
					$scope.manageHtml();

				},function(error){
					$scope.hideWidgetSpinner();
				});
			}else {
				$scope.trustedCss = $sce.trustAsHtml('<style>'+$scope.ngModel.cssToRender+'</style>');
				$scope.trustedHtml = $sce.trustAsHtml($scope.ngModel.htmlToRender);
				$scope.hideWidgetSpinner();
			}
		}

		//Core wrapper function to prepare css and styles to be parsed
		$scope.manageHtml = function(){
			if($scope.ngModel.cssToRender){
				$scope.checkPlaceholders($scope.ngModel.cssToRender).then(
						function(placeholderResultCss){
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
								$scope.trustedHtml = $sce.trustAsHtml(placeholderResultHtml);
								$scope.hideWidgetSpinner();
							}
						)
					}
				)
		}

		//Get the dataset column name from the readable name. ie: 'column_1' for the name 'id'
		$scope.getColumnFromName = function(name){
			for(var i in $scope.htmlDataset.metaData.fields){
				if($scope.htmlDataset.metaData.fields[i].header && $scope.htmlDataset.metaData.fields[i].header == name){
					return $scope.htmlDataset.metaData.fields[i].name;
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
				
				$scope.parseRepeat(allElements);
				
				$scope.parseIf(allElements);
				
				resolve(parsedHtml)
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
				    		tempRow.innerHTML =  tempRow.innerHTML.replace($scope.columnRegex, function(match,c1,c2){
								return "[kn-column='"+c1+"' row='"+(c2||r)+"']";
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
				    	if(eval(condition)){
				    		allElements[j].removeAttribute("kn-if");
				    	}else{
				    		allElements[j].parentNode.removeChild(allElements[j]);
				    		j--;
				    	}
				    }
				  j++;
				  
			 } while (j<nodesNumber);
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
					p1=$scope.params[p1];
					return p1;
				});
				resolve(resultHtml);
			})
		}
		
		//Replacers
		$scope.ifConditionReplacer = function(match, p1, p2){
			if($scope.htmlDataset.rows[p2||0] && $scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)]){
				p1 = typeof($scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)]) == 'string' ? '\''+$scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)]+'\'' : $scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)];
			}else {
				p1 = 'null';
			}
			return p1;
		}
		
		$scope.replacer = function(match, p1, p2) {
			p1=$scope.htmlDataset.rows[p2||0] && typeof($scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)])!='undefined' ? $scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)] : 'null';
			return p1;
		}
		$scope.paramsReplacer = function(match, p1){
			p1=$scope.params[p1];
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