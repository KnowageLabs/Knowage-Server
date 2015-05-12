/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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
	

