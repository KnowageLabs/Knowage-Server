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
		.service("cockpitModule_catalogFunctionService", cockpitModule_catalogFunctionService)

	function cockpitModule_catalogFunctionService(sbiModule_translate, sbiModule_restServices){
		var self = this;
		this.allCatalogFunctions = {};
		this.rEnvironments = [];
		this.pythonEnvironments = [];

		this.buildEnvironments = function (data) {
			toReturn = []
			for (i=0; i<data.length; i++) {
				key = data[i].label;
				val = data[i].valueCheck;
				toReturn[i] = {"label": key, "value": val};
			}
			return toReturn;
		}

		// PYTHON ENVIRONMENTS CONFIG
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet('2.0/configs/category', 'PYTHON_CONFIGURATION')
		.then(function(response){
			self.pythonEnvironments = self.buildEnvironments(response.data);
		});

		// R ENVIRONMENTS CONFIG
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet('2.0/configs/category', 'R_CONFIGURATION')
		.then(function(response){
			self.rEnvironments = self.buildEnvironments(response.data);
		});

		// GET ALL CATALOG FUNCTIONS
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.get("2.0/functions-catalog", "")
		.then(function(result) {
			self.allCatalogFunctions = result.data.functions;
		});

	}
})();