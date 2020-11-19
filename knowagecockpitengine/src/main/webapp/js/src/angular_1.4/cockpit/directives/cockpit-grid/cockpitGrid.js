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
(function(){
	var cockpitToolbarPath = '/angular_1.4/cockpit';
angular.module('cockpitModule').directive('cockpitGrid',function($compile,cockpitModule_widgetServices,cockpitModule_properties, sbiModule_config){
	   return{
		   templateUrl: sbiModule_config.dynamicResourcesEnginePath+ cockpitToolbarPath+ '/directives/cockpit-grid/templates/cockpitGrid.html',
		   transclude: true,
		   controller: cockpitGridControllerFunction,
		   priority: 1000,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("layout");
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	 transclude(scope, function (clone, scope) {
                             angular.element(element[0].querySelector("#gridsterContainer>ul")).append(clone);
                             $compile(angular.element(element[0].querySelector("#gridsterContainer>ul")))(scope);
                         });

                    	 angular.element(document).ready(function () {
                    		 scope.$on('gridster-mobile-changed', function(gridster,sizes) {
                    			 if(cockpitModule_properties.all_widget_initialized!=true){
                    				 return
                    			 }
                    				angular.element(document.getElementsByTagName('md-card-content')).removeClass('fadeIn');
                    				angular.element(document.getElementsByTagName('md-card-content')).addClass('fadeOut');
                    				for(var i=0;i<gridster.currentScope.sheet.widgets.length;i++){
                    					var widEle=document.getElementById(gridster.currentScope.sheet.widgets[i].id);
                    					if(widEle!=undefined && widEle.firstElementChild!=undefined){
                    						var options =angular.element(widEle).scope().getOptions == undefined? {} :  angular.element(widEle).scope().getOptions();
                    						cockpitModule_widgetServices.refreshWidget(angular.element(widEle.firstElementChild),gridster.currentScope.sheet.widgets[i],'gridster-resized',options);
                    					}

                    				}
                    				angular.element(document.getElementsByTagName('md-card-content')).removeClass('fadeOut');
                    				angular.element(document.getElementsByTagName('md-card-content')).addClass('fadeIn');
                    			});
                    	 });
                    }
                };
		   	}
	   }
});

function cockpitGridControllerFunction($rootScope, $scope,cockpitModule_gridsterOptions,cockpitModule_widgetServices,cockpitModule_properties,cockpitModule_template,$timeout){
	$scope.cockpitModule_gridsterOptions=cockpitModule_gridsterOptions;
	$scope.cockpitModule_template = cockpitModule_template;
	$scope.cockpitModule_properties=cockpitModule_properties;
	cockpitModule_properties.cockpitSpinner = false;

	if($scope.cockpitModule_template.configuration && $scope.cockpitModule_template.configuration.style){
		var tempBgStyle = $scope.cockpitModule_template.configuration.style;
		$scope.backgroundStyle={
				"background-size": tempBgStyle.imageBackgroundSize || '100%',
				"background-position": "center",
				"background-repeat": "no-repeat",
			    "min-height": "100%"

		};
		if(!cockpitModule_properties.EDIT_MODE){
			if(tempBgStyle.imageBackgroundUrl) $scope.backgroundStyle['background-image'] = 'url('+tempBgStyle.imageBackgroundUrl+')';
			if(tempBgStyle.sheetsBackgroundColor) $scope.backgroundStyle['background-color'] = tempBgStyle.sheetsBackgroundColor;
			$scope.backgroundStyle['background-size'] = tempBgStyle.imageBackgroundSize || 'contain';
		}
		

		$rootScope.showCockpitSpinner = function(){
			$timeout(function(){
				cockpitModule_properties.cockpitSpinner = true;
			},0)
		}

		$rootScope.hideCockpitSpinner = function(){
			$timeout(function(){
				cockpitModule_properties.cockpitSpinner = false;
			},0)
		}
	}
};

})();