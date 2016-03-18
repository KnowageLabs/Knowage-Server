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
 
  

Ext.define('Sbi.tools.dataset.FederatedDatasetModel', {
    extend: 'Ext.data.Model',
    fields: ['id', 'name', 'description','data_source_label', "label", "relationships", "type", "cache_data_source"],
    proxy: {
        type: 'ajax',
        url : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_FEDERATED_DATASET_FOR_FINAL_USER_ACTION'
		}),
        reader: {
            type: 'json',
            root: 'rows'
        }
    }
});