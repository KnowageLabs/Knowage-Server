/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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

