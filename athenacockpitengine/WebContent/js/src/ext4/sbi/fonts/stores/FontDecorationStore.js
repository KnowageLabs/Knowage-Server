/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Store for font decoration options
 *
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */

Ext.define('Sbi.fonts.stores.FontDecorationModel', {
     extend: 'Ext.data.Model',
     fields: [
         {name: 'name', 		type: 'string'},
         {name: 'description',  type: 'string'}
     ]
 });

Ext.define('Sbi.fonts.stores.FontDecorationStore', {
	
    model: 		'Sbi.fonts.stores.FontDecorationModel',
	data:   	
		[
        	 {name:'none',			description: LN('sbi.cockpit.designer.fontConf.noneFontDecoration')}, 
		     {name:'overline',		description: LN('sbi.cockpit.designer.fontConf.overlineFontDecoration')},
		     {name:'line-through',	description: LN('sbi.cockpit.designer.fontConf.linethroughFontDecoration')},
		     {name:'underline',		description: LN('sbi.cockpit.designer.fontConf.underlineFontDecoration')}
    	 ]
});
	

