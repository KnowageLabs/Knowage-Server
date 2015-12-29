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


Ext.define('Sbi.olap.options.OlapOptionsTable', {
	extend: 'Ext.panel.Panel',

	config:{
		html: "Table options"
	},


	initComponent: function() {

		Ext.tip.QuickTipManager.init();  // enable tooltips
		var editor = Ext.create('Ext.form.field.TextArea', {
			height: 400,
			width: 500
		});

		var button = Ext.create('Ext.Button', {
		    text: 'Send MDX',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	Sbi.olap.eventManager.notifyMdxChanged(editor.getValue());
		    }
		});

		Ext.apply(this, {
			items: [editor, button]
		});
		this.callParent();
	}

});

