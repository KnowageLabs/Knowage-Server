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
	angular.module("chartBackwardCompatibilityModule")

	.service("chartBackwardCompatibilityService", function ($injector){
		var chartBackwardCompatibilityServices = []
		chartBackwardCompatibilityServices.push($injector.get('gaugeBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('scatterBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('sunburstBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('parallelBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('treemapBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('heatmapBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('barBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('lineBackwardCompatibilityService'));
		chartBackwardCompatibilityServices.push($injector.get('radarBackwardCompatibilityService'));

		var updateTemplate = function(chartTemplate,enterpriseEdition){
			for(var i in chartBackwardCompatibilityServices){
				chartTemplate = chartBackwardCompatibilityServices[i].updateTemplate.apply(null,arguments)
			}

			return chartTemplate;
		}
		return {
			updateTemplate:updateTemplate,

		}


	});


})();