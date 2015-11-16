/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * A test panel to check that extjs 4 is properly working
 *
 *
 *  @author
 *  Abdrea Gioia (andrea.gioia@eng.it)
 */

Ext.define('Sbi.test.HelloWorldPanel', {
	extend: 'Ext.panel.Panel',
	layout: {
        type: 'border'
    },

	config:{

	},

	constructor : function(config) {
		this.initConfig(config||{});
		this.callParent(arguments);
	},

	initComponent: function() {
		Ext.apply(this, {
			items: [{html:"Hello World!"}]
		});
		this.callParent();
	}
});