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
 * Store for font family options
 *
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */

Ext.define('Sbi.fonts.stores.FontFamilyModel', {
     extend: 'Ext.data.Model',
     fields: [
         {name: 'name', 		type: 'string'},
         {name: 'description',  type: 'string'}
     ]
 });

Ext.define('Sbi.fonts.stores.FontFamilyStore', {
	model: 'Sbi.fonts.stores.FontFamilyModel',
	data:   	
		[
        	 {name:'Arial', 			description:'Arial'}, 
        	 {name:'Courier New',		description:'Courier New'}, 
        	 {name:'Tahoma',			description:'Tahoma'}, 
        	 {name:'Times New Roman',	description:'Times New Roman'},
        	 {name:'Verdana',			description:'Verdana'}
    	]
});
	

