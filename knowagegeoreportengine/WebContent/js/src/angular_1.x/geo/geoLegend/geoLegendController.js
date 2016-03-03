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
.directive('geoLegend',function(sbiModule_config ){
	return{
		restrict: "E",
//		replace: true,
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoLegend/templates/geoLegendTemplate.jspf',
		controller: geoLegendControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		}
	}
})

function geoLegendControllerFunction($scope,$mdDialog,geoModule_template,geoModule_thematizer,geoModule_layerServices,sbiModule_translate){	
	$scope.showLegend=false;
	$scope.thematizer=geoModule_thematizer;
	$scope.template=geoModule_template;
	$scope.translate=sbiModule_translate;
	$scope.legendItem=[];

	$scope.$watch(function() {
		return  geoModule_template.analysisConf.choropleth;
	}, function(newValue, oldValue) {
		if (newValue != oldValue) {
			geoModule_layerServices.updateTemplateLayer('choropleth');
	}
	}, true);
	
	$scope.$watch(function() {
		return  geoModule_template.analysisConf.proportionalSymbol;
	}, function(newValue, oldValue) {
		if (newValue != oldValue) {
			geoModule_layerServices.updateTemplateLayer('proportionalSymbol');
	}
	}, true);
	
	$scope.$watch(function() {
		return  geoModule_template.analysisConf.chart;
	}, function(newValue, oldValue) {
		if (newValue != oldValue) {
			geoModule_layerServices.updateTemplateLayer('chart');
	}
	}, true);

	$scope.toggleLegend=function(ev){
		$scope.showLegend=!$scope.showLegend;
	}


}