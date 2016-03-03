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
 * Store for font size store
 *
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */

Ext.define('Sbi.fonts.stores.FontSizeModel', {
     extend: 'Ext.data.Model',
     fields: [
         {name: 'name', 		type: 'int'},
         {name: 'description',  type: 'string'}
     ]
 });

Ext.define('Sbi.fonts.stores.FontSizeStore', {
	
    model: 	'Sbi.fonts.stores.FontSizeModel',
	data : [
		       {name:6,		description:"6"},
		       {name:8,		description:"8"},
		       {name:10,	description:"10"},
		       {name:12,	description:"12"},
		       {name:14,	description:"14"},
		       {name:16,	description:"16"},
		       {name:18,	description:"18"},
		       {name:22,	description:"22"},
		       {name:24,	description:"24"},
		       {name:28,	description:"28"},
		       {name:32,	description:"32"},
		       {name:36,	description:"36"},
		       {name:40,	description:"40"}
	       ]
});
	

