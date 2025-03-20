/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

(function() {

angular.module('datasetSaveModule')
	   .service('datasetSave_service', ['sbiModule_restServices', 
		   function(sbiModule_restServices){
			   
			   this.getDomainTypeScope = function() {
					return sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=DS_SCOPE");						
				}
			   
			   this.getDomainTypeCategory = function() {
					return sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=CATEGORY_TYPE");					
				}

			   this.persistDataSet = function(dataSet) {
				   return sbiModule_restServices.promisePost('1.0/datasets', '', angular.toJson(dataSet));
			   }
			   
			   this.getDatasetById = function(id) {
				   return sbiModule_restServices.promiseGet('1.0/datasets/dataset/id', id);
			   }
			   
	   }]);
	
})();