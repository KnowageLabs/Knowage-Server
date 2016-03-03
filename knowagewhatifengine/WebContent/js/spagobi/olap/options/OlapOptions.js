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
 * Container of the options.
 * I has:
 * <ul>
 *		<li>Option toolbar</li>
 *		<li>Options container
 *		<ul>
 *			<li>Chart Options</li>
 *			<li>Table Options</li>
 *		</ul>
 *		</li>
 *	</ul>
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.options.OlapOptions', {
	extend: 'Ext.panel.Panel',
	layout: 'vbox',

	config:{
		width: "100%",
		collapsible: true,
		split: true,
		collapseMode: "mini",
		title: "Options"
	},

	/**
     * @property {Sbi.olap.options.OlapOptionsToolbar} olapOptionsToolbar
     *  Toolbar that allow to switch the visualization of the options
     */
	olapOptionsToolbar: null,

	/**
     * @property {Sbi.olap.options.OlapOptionsContainer} olapOptionsContainer
     *  Container of the chart options
     */
	olapOptionsContainer: null,

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.options && Sbi.settings.olap.options.OlapOptions) {
			Ext.apply(this, Sbi.settings.olap.options.OlapOptions||{});
		}
		this.callParent(arguments);
	},

	initComponent: function() {
		this.olapOptionsContainer = Ext.create('Sbi.olap.options.OlapOptionsContainer', {flex: 1, align:'stretch', width: "100%"});
		this.olapOptionsToolbar   = Ext.create('Sbi.olap.options.OlapOptionsToolbar',  {height: 100, align:'stretch', width: "100%"});

		Ext.apply(this, {
			items: [this.olapOptionsToolbar,this.olapOptionsContainer]
		});
		this.callParent();
	}
});

