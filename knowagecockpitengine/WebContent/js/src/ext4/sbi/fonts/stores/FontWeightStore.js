/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Store for font weight options
 *
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */

Ext.define('Sbi.fonts.stores.FontWeightModel', {
     extend: 'Ext.data.Model',
     fields: [
         {name: 'name', 		type: 'string'},
         {name: 'description',  type: 'string'}
     ]
 });

Ext.define('Sbi.fonts.stores.FontWeightStore', {
	
    model: 	'Sbi.fonts.stores.FontWeightModel',
	data:   [
	         {name:'normal', 	description: LN('sbi.cockpit.designer.fontConf.normalFontWeight')},
	         {name:'bold',		description: LN('sbi.cockpit.designer.fontConf.boldFontWeight')}
	        ]
});
	

