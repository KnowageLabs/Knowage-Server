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
 * 
 * @author Marco Cortella (marco.cortella@eng.it)
 */

 Ext.define('Sbi.adhocreporting.MyAnalysisModel', {
    extend: 'Ext.data.Model',
    proxy:{
    	type : 'rest',
    	url : Sbi.config.serviceRegistry.getRestServiceUrl({
    		serviceName : 'documents/myAnalysisDocsList' 
    	}),
    	reader : {
    		type : 'json',
    		root : 'root'
    		
    	}
    },
    fields: 
    	[	    	 
    	 	"id",
    	 	"label",
    	 	"name",
    	 	"description",
    	 	"typeCode",
    	 	"typeId",
    	 	"encrypt",
    	 	"visible",
    	 	"engine",
    	 	"engineId",
    	 	"dataset",
    	 	"stateCode",
    	 	"stateId",
    	 	"functionalities",
    	 	"creationDate",
    	 	"creationUser",
    	 	"refreshSeconds",
    	 	"isPublic",
    	 	"actions",
    	 	"exporters",
    	 	"decorators",
    	 	"previewFile"

        ]
}); 