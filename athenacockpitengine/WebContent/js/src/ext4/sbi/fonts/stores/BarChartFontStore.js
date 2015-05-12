/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Store for a bar chart font options model
 *
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */

Ext.define('Sbi.fonts.models.BarChartFontModel', {
	
	extend: 'Ext.data.Model',
	
	fields: [
	 		{name: 'bcFontType', 				type:	'string'},
	 		{name: 'bcFontSize', 				type:	'int'},
	 		{name: 'bcLegendFontSize', 			type:	'int'},
	 		{name: 'bcAxisTitleFontSize', 		type:	'int'},
	 		{name: 'bcTooltipLabelFontSize', 	type:	'int'},
	 		{name: 'bcAxisLabelsFontSize', 		type:	'int'}
	 	]    
	
});

Ext.define('Sbi.fonts.stores.BarChartFontStore', {
	
    storeId: 	'barChartFontStore'		
	
});