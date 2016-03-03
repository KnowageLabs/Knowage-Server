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