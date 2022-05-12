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

	function cockpitModule_backwardCompatibility(
			cockpitModule_datasetServices,
			cockpitModule_properties,
			knModule_fontIconsService) {

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
				if(v1[k]>v2[k]) return false;
				else if(v1[k]==v2[k]) continue;
				else if(v1[k]<v2[k]) return true
			}
			//Check for literal versions
			//if(!v1[3] && v2[3]) return false;
			return true;
		}

		self.updateCockpitModel = function(template){
			//to version 6.3
			if(!self.compareVersion("6.3.0",template.knowageVersion)){
				if(template.configuration && typeof(template.configuration.showScreenshot)=='undefined') template.configuration.showScreenshot = true;
			}
			if(!self.compareVersion("7.3.0",template.knowageVersion)){
				if(template.configuration && typeof(template.configuration.showExcelExport)=='undefined') template.configuration.showExcelExport = true;
			}

			//Cycle trough all widgets
			for(var sheet in template.sheets){
				for(var widget in template.sheets[sheet].widgets){
					self.updateModel(template.sheets[sheet].widgets[widget],template.knowageVersion);
				}
			}

			template.knowageVersion = currentVersion;
			return template;
		}

		self.updateModel = function(model, version){
			//to version 6.3
			if(!self.compareVersion("6.3.0",version)){
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
				if(model.type=='static-pivot-table') {
					if (!model.content.style) {
						model.content.style = {};
					}
					if (!model.content.style.generic) {
						model.content.style.generic = {};
					}
					model.content.style.generic['layout'] = 'auto';
				}
			}
			//to version 6.4
			if(!self.compareVersion("6.4.3",version)){
				model.content.name = model.type + '_' + model.id;
			}
			if(!self.compareVersion("6.4.4",version)){
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
			if(!self.compareVersion("7.0.0",version)){
				if(model.type=='table'){
					if(model.cross && model.cross.cross && !model.cross.cross.crossType) model.cross.cross.crossType = 'allRow';
					if(model.style && model.style.tr && model.style.tr.height) model.style.tr.height = parseInt(model.style.tr.height.replace(/px|rem|em|pt/g,''));
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
					if(model.settings && model.settings.modalityValue == 'singleValue' && model.settings.modalityPresent == 'COMBOBOX') model.settings.modalityValue = 'dropdown';
				}
			}

			if(!self.compareVersion("7.2.1",version)){
				if(model.type=='table') {
					if(model.content && model.content.columnSelectedOfDataset){
						for(var k in model.content.columnSelectedOfDataset){
							if(model.content.columnSelectedOfDataset[k].isCalculated) {
								model.content.columnSelectedOfDataset[k].aggregationSelected = "NONE";
								if(!model.content.columnSelectedOfDataset[k].formula.match(/(AVG|SUM|MIN|MAX|COUNT|COUNT DISTINCT|TOTAL_SUM|TOTAL_AVG|TOTAL_MIN|TOTAL_MAX|TOTAL_COUNT)\(\"+[\D\+\-\*\/\w]*\)/gm)){
									model.content.columnSelectedOfDataset[k].formula = "SUM(" + model.content.columnSelectedOfDataset[k].formula + ")";
									model.content.columnSelectedOfDataset[k].formulaEditor = "SUM(" + model.content.columnSelectedOfDataset[k].formulaEditor + ")";
								}
							}
						}
					}
				}
				if(model.type=='chart') {
						if(model.dataset.dsId){
							model.content.dataset = model.dataset;
						}
						if(model.content.chartTemplate.CHART.LEGEND && model.content.chartTemplate.CHART.LEGEND.style && !model.content.chartTemplate.CHART.LEGEND.style.borderWidth) {
							model.content.chartTemplate.CHART.LEGEND.style.borderWidth = "";
						}
				}
			}

			if(!self.compareVersion("7.2.16",version)){
				if(model.type=='table') {
					if(typeof model.settings.autoRowsHeight != 'undefined') {
						delete model.settings.autoRowsHeight;
						if (model.style.tr && !model.style.tr.height)  model.style.tr.height = 25;
					}
					if(model.style.tr && model.style.tr.height && typeof model.style.tr.height != 'number') model.style.tr.height = parseInt(model.style.tr.height);
				}
				if(model.type=='chart') {
				 if (model.content.chartTemplate.CHART.AXES_LIST) {
					for (var k in model.content.chartTemplate.CHART.AXES_LIST.AXIS) {
						if (model.content.chartTemplate.CHART.AXES_LIST && model.content.chartTemplate.CHART.AXES_LIST.AXIS && model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].labels) {
							model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].LABELS = model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].labels;
							delete model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].labels;
						}
						if (model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].LABELS && model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].LABELS.rotation && model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].LABELS.rotation!='auto') {
							model.content.chartTemplate.CHART.AXES_LIST.AXIS[k].LABELS.rotationEnabled = true;
						}
					}
					if(model.content.chartTemplate.CHART.TOOLTIP && !model.content.chartTemplate.CHART.TOOLTIP.backgroundColor) {
							model.content.chartTemplate.CHART.TOOLTIP.backgroundColor= "#D6D6D6"
					}
				 }
				}
				if(model.type=='map') {

					// Ignore models that has zero elements in model.content.columnSelectedOfDataset
					if (Object.keys(model.content.columnSelectedOfDataset).length === 0) {
						delete model.content.columnSelectedOfDataset;
					}

					if (model.content.columnSelectedOfDataset) {
						var colsSelectedFromAllLayers = model.content.columnSelectedOfDataset;
						var layers = model.content.layers;
						// Add content attribute to every layer
						for (var j in layers) {
							var currLayer = layers[j];
							currLayer.content = {};
						}
						// Selected columns are moved into the respective layers
						for (var i in colsSelectedFromAllLayers) {
							var currColSelSet = colsSelectedFromAllLayers[i];

							/*
							 * If measure doesn't have showMap properties
							 * set it true by default.
							 */
							currColSelSet.forEach(function(el) {
								if (el.fieldType == "MEASURE") {
									if (!("shoMap" in el.properties)) {
										el.properties.showMap = true;
									}
								}
							});

							// Move aggregation function of all the measures outside properties
							for (var k in currColSelSet) {
								var currCol = currColSelSet[k];
								if ("MEASURE" == currCol.fieldType) {
									currCol.aggregationSelected = currCol.properties.aggregationSelected;
									delete currCol.properties.aggregationSelected;
								}
							}

							for (var j in layers) {
								var currLayer = layers[j];
								var currDsId = currLayer.dsId;
								if (currDsId == i) {
									currLayer.content.columnSelectedOfDataset = currColSelSet;
									break;
								}
							}
						}

						// Add dataset to every layer
						cockpitModule_datasetServices
							.loadDatasetsFromTemplate()
							.then(function() {
								for (var j in layers) {
									var currLayer = layers[j];
									var currDsId = currLayer.dsId;
									currLayer.dataset =
										cockpitModule_datasetServices.getDatasetById(currDsId);
									currLayer.name = currLayer.dataset.label;
									currLayer.dataset.dsId = currLayer.dataset.id.dsId;
								}
							});

						// Fix marker that use icons from Font Awesome
						for (var j in layers) {
							var currLayer = layers[j];
							if (currLayer.markerConf
									&& currLayer.markerConf.type == "icon") {

								var markerConf = currLayer.markerConf;
								var icon = markerConf.icon;
								var family = icon.family;
								var label = icon.label;

								var familyArr = undefined;

								for (var k in knModule_fontIconsService.icons) {
									var currFamily = knModule_fontIconsService.icons[k];
									if (currFamily.name == family) {
										familyArr = currFamily.icons;
										break;
									}
								}

								for (var k in familyArr) {
									var currIcon = familyArr[k];

									if (currIcon.label == label) {
										markerConf.icon = currIcon;
										break;
									}
								}

							}
						}

						// Cleanup
						delete model.content.columnSelectedOfDataset;
					}
				}
			}

			if(!self.compareVersion("7.3.1",version)){
				if(model.oldDatasetExecutions) delete model.oldDatasetExecutions;

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
				if(model.type == 'discovery'){
					if(model.settings.hideTextSearch) model.settings.textEnabled = !model.settings.hideTextSearch;
				}
				if(model.type=='static-pivot-table'){
					if(model.content.crosstabDefinition.measures){
						for(var k in model.content.crosstabDefinition.measures){
							if(model.content.crosstabDefinition.measures[k].scopeFunc && model.content.crosstabDefinition.measures[k].scopeFunc.condition){
								for(var c in model.content.crosstabDefinition.measures[k].scopeFunc.condition){
									if(model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].condition) {
										model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].operator = model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].condition;
										delete model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].condition;
										if(model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].iconColor) {
											model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].color = model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].iconColor;
											delete model.content.crosstabDefinition.measures[k].scopeFunc.condition[c].iconColor;
										}
									}
								}
								model.content.crosstabDefinition.measures[k].ranges = model.content.crosstabDefinition.measures[k].scopeFunc.condition;
								delete model.content.crosstabDefinition.measures[k].scopeFunc;
							}
							if(model.content.crosstabDefinition.measures[k].colorThresholdOptions){
								if(model.content.crosstabDefinition.measures[k].colorThresholdOptions.conditionValue){
									for(var j = 0; j<3 ; j++){
										if(model.content.crosstabDefinition.measures[k].colorThresholdOptions.conditionValue[j]){
											var tempObj = {
													"operator": model.content.crosstabDefinition.measures[k].colorThresholdOptions.condition[j],
													"value": model.content.crosstabDefinition.measures[k].colorThresholdOptions.conditionValue[j],
													"background-color": model.content.crosstabDefinition.measures[k].colorThresholdOptions.color[j]
											}

											if (model.content.crosstabDefinition.measures[k].ranges) model.content.crosstabDefinition.measures[k].ranges.push(tempObj);
											else model.content.crosstabDefinition.measures[k].ranges = [tempObj];
										}
									}
									delete model.content.crosstabDefinition.measures[k].colorThresholdOptions;
								}
							}
						}
					}
				}
			}

			if(!self.compareVersion("7.4.8",version)){
				if(model.type=='map'){
					if (!model.style.legend) model.style.legend = {};
				}
			}

			if(model.content.name.match(/new[a-zA-Z\s\-]*Widget/g)) model.content.name = model.type + '_' + model.id;

			return model;
		}
	}
})();