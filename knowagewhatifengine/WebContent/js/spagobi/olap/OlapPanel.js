/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Container of all the UI of the olap engine.<br>
 * It contains:
 * <ul>
 *		<li>View definition tools</li>
 *		<li>Table/Chart</li>
 *		<li>Options</li>
 *	</ul>
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.OlapPanel', {
	extend: 'Ext.panel.Panel',
	layout: {
        type: 'border'
    },

	config:{

	},

	/**
     * @property {Sbi.olap.tools.OlapViewDefinitionTools} definitionTools
     *  Tools for the view definition
     */
	definitionTools: null,

	/**
     * @property {Sbi.olap.execution.OlapExecutionPanel} executionPanel
     *  Panel that contains the pivot and the chart
     */
	executionPanel: null,

	/**
     * @property {Sbi.olap.options.OlapOptions} optionsPanel
     *  Panel that contains the options of the chart
     */
	optionsPanel: null,

	/**
     * @property {Sbi.olap.control.EventManager} eventManager
     *  Manager of all the events fired by the UI
     */
	eventManager: null,

	/**
     * @property {Sbi.olap.PivotModel} pivotModel
     *  The pivot model
     */
	pivotModel: null,

	/**
     * @property {Object} modelConfig
     *  The configuration of the model.. Example drill type, selected hierarchy of a dimension,...
     */
	modelConfig:{},



	constructor : function(config) {
		this.initConfig(config||{});

		this.definitionTools = Ext.create('Sbi.olap.tools.OlapViewDefinitionTools', {region:"west",width: '15%'});
		this.executionPanel = Ext.create('Sbi.olap.execution.OlapExecutionPanel', {region:"center",width: '45%'});
		this.optionsPanel = Ext.create('Sbi.olap.options.OlapOptions', {region:"east",width: '10%'});
		Sbi.olap.eventManager = Ext.create('Sbi.olap.control.EventManager', {olapPanel: this});


		this.callParent(arguments);

		this.initEvents();
	},

	initComponent: function() {


		Ext.apply(this, {
			items: [this.definitionTools, this.executionPanel, this.optionsPanel]
		});

		this.executionPanel.on('configChange',this.appyConfigChanges,this);

		this.callParent();
	},


	updateAfterMDXExecution: function(pivot, modelConfig){
		this.pivotModel = pivot;
		this.modelConfig = modelConfig;
		this.executionPanel.updateAfterMDXExecution(pivot, modelConfig);
	},

	appyConfigChanges: function(changes){
		this.modelConfig = Ext.apply(this.modelConfig,changes||{});
		Sbi.olap.eventManager.setModelConfig(this.modelConfig);
	},
	
	
	// perform the save action
	saveSubObject: function(name, description, scope){
		Sbi.olap.eventManager.saveSubObject(name, description, scope);				
	}



});