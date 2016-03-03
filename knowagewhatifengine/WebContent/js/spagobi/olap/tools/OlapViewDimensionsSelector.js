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
 * Tool for the definition of the olap view.
 * It's the container of the dimensions
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.tools.OlapViewDimensionsSelector', {
	//class to extends
	extend: 'Ext.tree.Panel',

	config:{
		rootVisible: false,
		title: "Dimensions",
		border: false
	},

	constructor : function(config) {
		this.initConfig(config);
		this.callParent(arguments);
	},

	initComponent: function() {
		var store = Ext.create('Ext.data.TreeStore', {
			root: {
				text: "Dimensions",
				expanded: true,
				children: [
				           { text: "Store", leaf: true },
				           { text: "Product", leaf: true },
				           { text: "Customer", leaf: true },
				           { text: "Time", leaf: true }
				           ]
			}
		});

		Ext.apply(this, {
			store: store
		});
		this.callParent();
	}
});

