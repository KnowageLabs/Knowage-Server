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
	angular.module("cockpitModule")
	.service("cockpitModule_backwardCompatibility", cockpitModule_backwardCompatibility);

	function cockpitModule_backwardCompatibility(cockpitModule_properties){
		var self=this;
		var currentVersion = cockpitModule_properties.CURRENT_KNOWAGE_VERSION;
		
		self.compareVersion = function(v1,v2){
			if(!v2) return false;
			
			function versionToArray(v){
				var snapshot = v.indexOf('-S');
			    if(snapshot != -1) v = v.substring(0,snapshot);
			    v = v.split('.').map(Number);
			    if(snapshot != -1) v.push('S');
			    return v;
			}
			
			v1 = versionToArray(v1);
			v2 = versionToArray(v2);
			
			for(var k in v1){
				if(v1[k]>v2[k]) {
					return false;
				}
			}
			if(!v1[3] && v2[3]) return false;
			return true;
		}
		
		self.updateCockpitModel = function(template){
			//to version 6.3
			if(!self.compareVersion("6.3.0",template.knowageVersion)){
				if(template.configuration && typeof(template.configuration.showScreenshot)=='undefined') template.configuration.showScreenshot = true;
			}
			template.knowageVersion = currentVersion;
			return template;
		}
		
		self.updateModel = function(model){
			//to version 6.3
			if(!self.compareVersion("6.3.0",model.knowageVersion)){
				if(model.type=='table'){
					if(model.content && model.content.columnSelectedOfDataset){
						for(k in model.content.columnSelectedOfDataset){
							if(model.content.columnSelectedOfDataset[k].style && model.content.columnSelectedOfDataset[k].style.td) {
								model.content.columnSelectedOfDataset[k].style['justify-content'] = model.content.columnSelectedOfDataset[k].style.td['justify-content'];
								delete model.content.columnSelectedOfDataset[k].style.td;
							}
						}
					}
				}
			}
			
			model.knowageVersion = currentVersion;
			return model;
		}
	}
})();