/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * This is a simple window with an html content.
 *     
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.widgets.Help', {
	extend: 'Ext.panel.Panel',
	
	config:{
		frame: true,
		width: 400,
		height: 500
	},

	/**
     * @property {String} content
     *  The content on the help window
     */
	content: "",
	
	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.Help) {
			Ext.apply(this, Sbi.settings.widgets.Help);
		}
	},
	
	initComponent: function() {

		Ext.apply(this, {
			html: content
		});
		this.callParent();
	}
	

	
});

