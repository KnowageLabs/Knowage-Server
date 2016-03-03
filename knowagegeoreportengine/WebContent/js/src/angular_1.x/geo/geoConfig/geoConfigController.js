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
.directive('geoConfig',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoConfig/templates/geoConfigTemplate.jsp',
		controller: geoConfigControllerFunction,
//		require: "^geoMap",
		scope: {
			id:"@"
		}
	}
});

function geoConfigControllerFunction($scope,geoModule_template,sbiModule_translate,geoModule_indicators) {
	$scope.translate=sbiModule_translate;
	$scope.template = geoModule_template;
	$scope.indicators = geoModule_indicators;
	$scope.newArray=function(val){
		return new Array(val);
	}
	$scope.choroplethMethodTypeList = [
	                                   {label:sbiModule_translate.load("gisengine.rigthMapMenu.legend.choropleth.method.quantils"),value:"CLASSIFY_BY_QUANTILS"},
	                                   {label:sbiModule_translate.load("gisengine.rigthMapMenu.legend.choropleth.method.equalsIntervals"),value:"CLASSIFY_BY_EQUAL_INTERVALS"}
	                                   ];
};

