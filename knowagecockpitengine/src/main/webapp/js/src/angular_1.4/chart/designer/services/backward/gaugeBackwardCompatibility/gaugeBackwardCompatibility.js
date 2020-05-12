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

	.service("gaugeBackwardCompatibilityService", function (){

		var updateTemplate = function(chartTemplate){
			if(chartTemplate.type.toLowerCase()=='gauge'){
				//adding new
				if(!chartTemplate.AXES_LIST.AXIS[0].TARGET){
					chartTemplate.AXES_LIST.AXIS[0].TARGET = [{"color": "","dashStyle": "Solid","value":0,"width":2}]
				}
				if(!chartTemplate.TOOLTIP){
					chartTemplate.TOOLTIP = {"borderWidth": 0,"borderRadius":0}
				}
				if(chartTemplate.VALUES.SERIE[0].TOOLTIP){
					delete chartTemplate.VALUES.SERIE[0].TOOLTIP.borderWidth;
					delete chartTemplate.VALUES.SERIE[0].TOOLTIP.borderRadius;
					chartTemplate.VALUES.SERIE[0].TOOLTIP = {
							backgroundColor: "#D6D6D6",
							style: {align: "", color: "", fontFamily: "", fontWeight: "", fontSize: ""},
							align: "",
							color: "",
							fontFamily: "",
							fontSize: "",
							fontWeight: "",
					}
				}
			}

			return chartTemplate;
		};
		return {
			updateTemplate:updateTemplate
		}
	});


})();