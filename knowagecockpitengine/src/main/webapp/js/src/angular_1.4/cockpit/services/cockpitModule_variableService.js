/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.

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
		.service("cockpitModule_variableService", cockpitModule_variableService)

	function cockpitModule_variableService(sbiModule_translate,sbiModule_user, cockpitModule_template,cockpitModule_properties,$q,cockpitModule_analyticalDrivers,cockpitModule_datasetServices, cockpitModule_widgetSelection){
		var self = this;

		function formatDataset(dataset){
			tempRows = {};
			dataset.rows.forEach(function(row,i){
				tempRows[row.column_1] = row.column_2;
			})
			return tempRows;
		}

		this.variablesInit = function(){
			if(cockpitModule_template.configuration && cockpitModule_template.configuration.variables){
				if(!cockpitModule_properties.VARIABLES) cockpitModule_properties.VARIABLES = {};
				cockpitModule_template.configuration.variables.forEach(function(variable){
					self.getVariableValue(variable).then(
							function(response){
								cockpitModule_properties.VARIABLES[variable.name] = response;
							},function(error){
								console.error('error during the variables recovery.')
							}
						)
				})
			}
		}

		this.getVariableValue = function(variable){
			return $q(function(resolve, reject) {
				if(variable.type == 'static') resolve( variable.value );
				if(variable.type == 'driver') resolve( self.parseOldStyleDriver(cockpitModule_analyticalDrivers[variable.driver]) );
				if(variable.type == 'profile') resolve( sbiModule_user.profileAttributes[variable.attribute] );
				if(variable.type == 'dataset') {
					var tempDataset = cockpitModule_datasetServices.getDatasetById(variable.dataset);
					if(tempDataset){
						var tempColumn = {content:{columnSelectedOfDataset:[]}}
						for(var j in tempDataset.metadata.fieldsMeta){
							if(variable.column && tempDataset.metadata.fieldsMeta[j].name == variable.column){
								tempColumn.content.columnSelectedOfDataset.push(tempDataset.metadata.fieldsMeta[j]);
								break;
							}else if(!variable.column){
								tempColumn.content.columnSelectedOfDataset.push(tempDataset.metadata.fieldsMeta[j]);
							}
						}
						cockpitModule_datasetServices.loadDatasetRecordsById(variable.dataset, 0, -1,undefined, undefined,tempColumn).then(
							function(response){
								if(variable.column) resolve(response.rows[0].column_1);
								else resolve(formatDataset(response));
							},function(error){
								reject(error)
						})
					}else reject(sbiModule_translate.load('kn.cockpit.dataset.error.notavailable'));
				}
			})
		}
		
		this.parseOldStyleDriver = function(oldDriver){
			var newDriver = oldDriver.replace(/\{([\;\,\_\s\-]{1})\{([^\}]*)\}([A-Z]*)\}/g,function(match, divider, params, type){
				newDriver = params.split(divider);
				return newDriver.join(",");
			})
			return newDriver;
		}

		this.getVariablePlaceholders = function(textToParse){
			var tempText = angular.copy(textToParse);
			if(tempText){
				tempText = tempText.replace(/\$V\{([a-zA-Z0-9\-\_]*){1}(?:.([a-zA-Z0-9\-\_]*){1})?\}/g,function(match,p1,p2){
					return p2 ? cockpitModule_properties.VARIABLES[p1][p2] : cockpitModule_properties.VARIABLES[p1];
				})
			}
			return tempText;
		}
	}
})();