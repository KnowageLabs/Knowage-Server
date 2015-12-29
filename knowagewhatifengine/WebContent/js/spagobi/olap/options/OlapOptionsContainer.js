/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Container of the options for the chart and the table
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.options.OlapOptionsContainer', {
	extend: 'Ext.panel.Panel',
	layout: 'card',

	config:{

	},

	/**
     * @property {Sbi.olap.options.OlapOptionsTable} olapOptionsTable
     *  Container of the table options
     */
	olapOptionsTable: null,

	/**
     * @property {Sbi.olap.options.OlapOptionsChart} olapOptionsChart
     *  Container of the chart options
     */
	olapOptionsChart: null,

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.options && Sbi.settings.olap.options.OlapOptionsContainer) {
			Ext.apply(this, Sbi.settings.olap.options.OlapOptionsContainer);
		}
		this.callParent(arguments);
	},

	initComponent: function() {
		this.olapOptionsTable = Ext.create('Sbi.olap.options.OlapOptionsTable', {});
		this.olapOptionsChart = Ext.create('Sbi.olap.options.OlapOptionsChart', {});

		Ext.apply(this, {
			items: [this.olapOptionsTable,this.olapOptionsChart]
		});
		this.callParent();
	}
});

