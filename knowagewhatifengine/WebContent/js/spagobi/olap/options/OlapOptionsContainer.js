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

