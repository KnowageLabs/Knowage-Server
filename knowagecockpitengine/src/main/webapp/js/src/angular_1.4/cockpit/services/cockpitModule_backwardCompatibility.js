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
				var releaseCandidate = v.indexOf('-RC');
			    if(snapshot != -1) v = v.substring(0,snapshot);
			    if(releaseCandidate != -1) v = v.substring(0,releaseCandidate);
			    v = v.split('.').map(Number);
			    if(snapshot != -1) v.push('S');
			    if(releaseCandidate != -1) v.push('RC');
			    return v;
			}

			v1 = versionToArray(v1);
			v2 = versionToArray(v2);

			for(var k in v1){
				if(v1[k]>v2[k]) {
					return false;
				}
			}
			//Check for literal versions
			//if(!v1[3] && v2[3]) return false;
			return true;
		}

		self.updateCockpitModel = function(template){
			//to version 6.3
			if(!self.compareVersion("6.3.0",template.knowageVersion)){
				if(template.configuration && typeof(template.configuration.showScreenshot)=='undefined') template.configuration.showScreenshot = true;
				template.knowageVersion = currentVersion;
			}
			return template;
		}

		self.updateModel = function(model){
			//to version 6.3
			if(!self.compareVersion("6.3.0",model.knowageVersion)){
				if(model.type=='table'){
					if(model.content && model.content.columnSelectedOfDataset){
						for(var k in model.content.columnSelectedOfDataset){
							if(model.content.columnSelectedOfDataset[k].style && model.content.columnSelectedOfDataset[k].style.td) {
								model.content.columnSelectedOfDataset[k].style['justify-content'] = model.content.columnSelectedOfDataset[k].style.td['justify-content'];
								delete model.content.columnSelectedOfDataset[k].style.td;
							}
						}
					}
				}
			}
			//to version 6.4
			if(!self.compareVersion("6.4.3",model.knowageVersion)){
				model.content.name = model.type + '_' + model.id;
			}
			if(!self.compareVersion("6.4.4",model.knowageVersion)){
				if(model.type=='table'){
					if(model.content && model.content.columnSelectedOfDataset){
						for(var k in model.content.columnSelectedOfDataset){
							if (model.content.columnSelectedOfDataset[k].isCalculated) {
								if(!model.content.columnSelectedOfDataset[k].funcSummary){
									model.content.columnSelectedOfDataset[k].funcSummary = model.content.columnSelectedOfDataset[k].aggregationSelected == 'NONE' ? 'SUM' : model.content.columnSelectedOfDataset[k].aggregationSelected;
								}
								model.content.columnSelectedOfDataset[k].datasetOrTableFlag = true;
							}
						}
					}
				}
			}
			//to version 7.0
			if(!self.compareVersion("7.0.0",model.knowageVersion)){
				if(model.type=='table'){
					if(model.cross && model.cross.cross && !model.cross.cross.crossType) model.cross.cross.crossType = 'allRow';
					if(model.content && model.content.columnSelectedOfDataset){
						for(var k in model.content.columnSelectedOfDataset){
							if(model.content.columnSelectedOfDataset[k].fieldType == "ATTRIBUTE" && model.content.columnSelectedOfDataset[k].funcSummary) {
								delete model.content.columnSelectedOfDataset[k].funcSummary;
							}
							if(model.content.columnSelectedOfDataset[k].style && model.content.columnSelectedOfDataset[k].style.width) delete model.content.columnSelectedOfDataset[k].style.width;
						}
					}
				}
				if(model.type=='selector'){
					if(model.settings && model.settings.modalityValue == 'multiValue' && model.settings.modalityPresent == 'COMBOBOX') model.settings.modalityValue = 'multiDropdown';
				}
			}

			if(!self.compareVersion("7.3.0",model.knowageVersion)){
				if(model.type=='table' || model.type=='discovery'){
					for(var k in model.content.columnSelectedOfDataset){
						if(model.content.columnSelectedOfDataset[k].momentDateFormat){
							model.content.columnSelectedOfDataset[k].dateFormat = model.content.columnSelectedOfDataset[k].momentDateFormat;
							delete model.content.columnSelectedOfDataset[k].momentDateFormat;
						}
						if(model.content.columnSelectedOfDataset[k].dateFormat) {
							model.content.columnSelectedOfDataset[k].dateFormat.replace(/DD\/MM\/YYYY HH:MM:SS/g,'DD/MM/YYYY HH:mm:SS');
							model.content.columnSelectedOfDataset[k].dateFormat.replace(/DD\/MM\/YYYY HH:MM/g,'DD/MM/YYYY HH:mm');
						}
					}
				}
			}

			if(model.content.name.match(/new[a-zA-Z\s\-]*Widget/g)) model.content.name = model.type + '_' + model.id;

			model.knowageVersion = currentVersion;
			return model;
		}
	}
})();