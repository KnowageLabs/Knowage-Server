/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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
	

