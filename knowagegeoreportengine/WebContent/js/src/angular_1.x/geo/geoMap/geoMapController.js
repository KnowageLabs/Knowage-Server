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

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geoModule')
.directive('geoMap',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoMap/templates/geoMapTemplate.jspf',
		controller: geoMapControllerFunction,
		transclude: true,
		scope: {
			mapId:"@"
		}
	}
});


//Dont'remove geoReport_saveTemplate from function because it initialize the factory to save the template
function geoMapControllerFunction($scope,geoModule_reportUtils,geoReport_saveTemplate,geoModule_layerServices){
	geoModule_reportUtils.getTargetDataset();
	$scope.openCrossNavMultiSelectFlag = false;
	$scope.closePopup=function(){
		geoModule_layerServices.removeSelectPopup();
	}
}