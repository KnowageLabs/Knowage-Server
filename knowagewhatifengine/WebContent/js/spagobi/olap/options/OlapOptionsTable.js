/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

