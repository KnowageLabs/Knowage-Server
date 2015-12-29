/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Container of the options for the table
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.options.OlapOptionsToolbar', {
	extend: 'Ext.panel.Panel',

	config:{
		html: "Toolbar options"
	},

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.options && Sbi.settings.olap.options.OlapOptionsToolbar) {
			Ext.apply(this, Sbi.settings.olap.options.OlapOptionsToolbar);
		}
		this.callParent(arguments);
	}
});

