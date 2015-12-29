/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Container for the execution of the olap.
 * It contains:
 * <ul>
 *		<li>Toolbar</li>
 *		<li>Table</li>
 *		<li>Chart</li>
 *	</ul>
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.OlapExecutionPanel', {
	//class to extends
	extend: 'Ext.panel.Panel',
	layout: 'card',

	config:{
		border: false
	},

	/**
     * @property {Sbi.olap.execution.OlapExecutionChart} olapExecutionChart
     *  Container of the chart representation of the data
     */
	olapExecutionChart: null,

	/**
     * @property {Sbi.olap.execution.OlapExecutionPivot} olapExecutionPivot
     *  Container of the pivot representation of the data
     */
	olapExecutionPivot: null,

	/**
     * @property {Sbi.olap.toolbar.OlapToolbar} olapToolbar
     *  The toolbar
     */
	olapToolbar: null,

	/**
     * @property {Object} executionConfig
     *  The configuration of the model.. Example drill type, selected hierarchy of a dimension,...
     */
	executionConfig: {},

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.OlapExecutionPanel) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionPanel);
		}

		this.olapExecutionPivot = Ext.create('Sbi.olap.execution.table.OlapExecutionPivot', {});
		this.olapExecutionChart = Ext.create('Sbi.olap.execution.chart.OlapExecutionChart', {});
		this.olapToolbar  = Ext.create('Sbi.olap.toolbar.OlapToolbar', {});

		this.callParent(arguments);

		this.addEvents(
		        /**
		         * @event configChange
		         * The final user changes the configuration of the model
				 * @param {Object} configuration
		         */
		        'configChange'
				);
	},

	initComponent: function() {

		this.olapToolbar.on('configChange',this.applyConfigChanges,this);

		Ext.apply(this, {
			items: [this.olapExecutionPivot,this.olapExecutionChart],
			tbar: this.olapToolbar
		});
		this.callParent();
	},

	/**
	 * Updates the visualization after the execution of a a mdx query
	 * @param pivotModel {Sbi.olap.PivotModel} the model instance with the execution query result set
	 */
	updateAfterMDXExecution: function(pivotModel, modelConfig){
		this.olapExecutionPivot.updateAfterMDXExecution(pivotModel);
		this.olapToolbar.updateAfterMDXExecution(pivotModel, modelConfig);
	},

	applyConfigChanges: function(changes){
		this.executionConfig = Ext.apply(this.executionConfig,changes||{});
		this.fireEvent('configChange', this.executionConfig);
	}


});

