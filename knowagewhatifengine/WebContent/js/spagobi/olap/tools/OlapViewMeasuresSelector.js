/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Tool for the definition of the olap view.
 * It's the container of the dimensions
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.tools.OlapViewMeasuresSelector', {
	//class to extends
	extend: 'Ext.tree.Panel',


	config:{
		rootVisible: false,
		title:"Measures",
		autoHeight: true,
		border: false
	},

	constructor : function(config) {
		this.initConfig(config);
		this.callParent(arguments);
	},

	initComponent: function() {
		var store = Ext.create('Ext.data.TreeStore', {
			root: {
				text: "Measures",
				expanded: true,
				children: [
				           { text: "Strore Cost", leaf: true },
				           { text: "Store Sales", leaf: true },
				           { text: "Unit Sales", leaf: true }
				           ]
			}
		});

		Ext.apply(this, {
			store: store
		});
		this.callParent();
	}
});

