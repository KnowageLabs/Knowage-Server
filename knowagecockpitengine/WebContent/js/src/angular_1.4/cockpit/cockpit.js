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

var scripts = document.getElementsByTagName("script");
var baseScriptPath  = scripts[scripts.length - 1].src;
baseScriptPath =baseScriptPath .substring(0, baseScriptPath .lastIndexOf('/'));

(function() {
	
var cockpitApp= angular.module("cockpitModule",['ngMaterial','sbiModule','gridster','file_upload','ngWYSIWYG','angular_table','color.picker','dndLists']);
cockpitApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);
 



 


cockpitApp.controller("cockpitMasterController",['$scope','cockpitModule_widgetServices','cockpitModule_template','cockpitModule_datasetServices','cockpitModule_realtimeServices','cockpitModule_properties','cockpitModule_templateServices','$rootScope',cockpitMasterControllerFunction]);
function cockpitMasterControllerFunction($scope,cockpitModule_widgetServices,cockpitModule_template,cockpitModule_datasetServices,cockpitModule_realtimeServices,cockpitModule_properties,cockpitModule_templateServices,$rootScope){
	$scope.cockpitModule_widgetServices=cockpitModule_widgetServices;
	$scope.cockpitModule_template=cockpitModule_template;
	//load the dataset list
	$scope.datasetLoaded=false;
	cockpitModule_datasetServices.loadDatasetList().then(function(){
		$scope.datasetLoaded=true;
		var dsNotInCache = cockpitModule_templateServices.getDatasetAssociatedNotUsedByWidget();
		if(dsNotInCache.length>0){
			cockpitModule_datasetServices.addDatasetInCache(dsNotInCache)
			.then(function(){
				$rootScope.$broadcast("WIDGET_INITIALIZED");
			});
			
			//WIDGET_INITIALIZED at the end
		}else{
			$rootScope.$broadcast("WIDGET_INITIALIZED");
		}
	},function(){
		console.error("error when load dataset list")
	});
	if(!cockpitModule_properties.EDIT_MODE){
		cockpitModule_realtimeServices.init();
	}
}

})();