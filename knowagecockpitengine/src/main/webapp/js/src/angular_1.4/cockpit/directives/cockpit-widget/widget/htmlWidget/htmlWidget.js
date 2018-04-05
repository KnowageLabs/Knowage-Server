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
		
		$scope.$watch('ngModel.cssToRender',function(newValue,oldValue){
			$scope.trustedCss = $sce.trustAsHtml('<style>'+newValue+'</style>');
		})
		$scope.$watch('ngModel.htmlToRender',function(newValue,oldValue){
			$scope.trustedHtml = $sce.trustAsHtml(newValue);
		})
		
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

	// this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("html",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});

})();