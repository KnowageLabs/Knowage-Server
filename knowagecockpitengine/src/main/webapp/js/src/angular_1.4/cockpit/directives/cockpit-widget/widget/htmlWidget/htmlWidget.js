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
						pre: function preLink(scope, element, attrs, ctrl, transclud) {},
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

		$scope.refresh = function(element,width,height, datasetRecords,nature) {

		}

		/**
		 * Function to initialize the rendered html at the loading and after editing.
		 * If there is a selected dataset the function calls the data rest service.
		 */
		$scope.reinit = function(){
			if($scope.ngModel.datasetId){
				sbiModule_restServices.restToRootProject();
				var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.datasetId);
				sbiModule_restServices.promisePost("2.0/datasets", encodeURIComponent(dataset.label) + "/data?nearRealtime=" + !dataset.useCache).then(function(data){
					$scope.htmlDataset = data.data;
					if($scope.ngModel.cssToRender){
						$scope.checkPlaceholders($scope.ngModel.cssToRender).then(
								function(placeholderResultCss){
									$scope.trustedCss = $sce.trustAsHtml('<style>'+placeholderResultCss+'</style>');
								}
							)
					}
					$scope.checkCustomFunctions($scope.ngModel.htmlToRender).then(
						function(resultHtml){
							$scope.checkPlaceholders(resultHtml).then(
								function(placeholderResultHtml){
									$scope.trustedHtml = $sce.trustAsHtml(placeholderResultHtml);
								}
							)
						}
					)

				},function(error){

				});
			}else {
				$scope.trustedCss = $sce.trustAsHtml('<style>'+$scope.ngModel.cssToRender+'</style>');
				$scope.trustedHtml = $sce.trustAsHtml($scope.ngModel.htmlToRender);
			}
		}

		/**
		 * Get the dataset column name from the readable name. ie: 'column_1' for the name 'id'
		 */
		$scope.getColumnFromName = function(name){
			for(var i in $scope.htmlDataset.metaData.fields){
				if($scope.htmlDataset.metaData.fields[i].header && $scope.htmlDataset.metaData.fields[i].header == name){
					return $scope.htmlDataset.metaData.fields[i].name;
				}
			}
		}

		/**
		 * Regular Expressions used in the placeholder functions
		 */
		$scope.columnRegex = /(?:\[kn-column=[\'\"]{1}([a-zA-Z0-9\_\-]+)[\'\"]{1}(?:\s+row=[\'\"]{1}(\d*)[\'\"]{1})?\])/g;
		$scope.functionsRegex = /(?:(?:\[kn-((?!column)[a-zA-Z0-9]+)\s*([\w\=\'\""\s]+)?\]([\s\S]*)(?:\[\/kn-[a-zA-Z0-9]+\])))/g;
		$scope.repeatIndexRegex = /\[kn-repeat-index\]/g;

		/**
		 * Check the existence of placeholder inside the raw html.
		 * If there is a match the placeholder is replaced with the dataset value for that column.
		 * If the row is not specified the first one is returned.
		 */
		$scope.checkPlaceholders = function(rawHtml){
			return $q(function(resolve, reject) {
				function replacer(match, p1, p2) {
					p1=$scope.htmlDataset.rows[p2||0][$scope.getColumnFromName(p1)]
					return p1;
				}
				var resultHtml = rawHtml.replace($scope.columnRegex, replacer);
				resolve(resultHtml);
			})
		}

		/**
		 * Check the existence of custom functions inside the raw html.
		 * If there is a match the placeholder changes the html using the function selected.
		 */
		$scope.checkCustomFunctions = function(rawHtml){
			return $q(function(resolve, reject) {
				function replacer(match, functionType, attrs, html) {
					var str = '';
					if(functionType=='repeat'){
						for(var r in $scope.htmlDataset.rows){
							str += html.replace($scope.columnRegex, function(match,c1,c2){
								return '[kn-column="'+c1+'" row="'+(c2||r)+'"]';
							});
							str = str.replace($scope.repeatIndexRegex, r);
						}
					}
					return str;
				}
				var resultHtml = rawHtml.replace($scope.functionsRegex, replacer);
				resolve(resultHtml);
			})
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