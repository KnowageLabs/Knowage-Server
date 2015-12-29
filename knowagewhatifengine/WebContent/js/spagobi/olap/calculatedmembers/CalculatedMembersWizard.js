/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * The wizard 
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedMembersWizard', {
	extend: 'Ext.window.Window',

	config:{
		bodyStyle: "background-color: white",
		actualVersion: null,
		title: "Calculated Members Wizard"
	},
	calculatedMembersBox: null,
    dimensions: null,	
	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.calculatedmembers && Sbi.settings.olap.calculatedmembers.CalculatedMembersWizard) {
			Ext.apply(this, Sbi.settings.olap.calculatedmembers.CalculatedMembersWizard);
		}		
		this.callParent(arguments);
	},

	initComponent: function() {
		var thisPanel = this;

		var innerLeftPanel = Ext.create('Sbi.olap.calculatedmembers.CalculatedMembersLeftSide', {
			width: 425
		});
		var innerRightPanel = Ext.create('Sbi.olap.calculatedmembers.CalculatedMembersRightSide', {
			width: 375,
			dimensions: thisPanel.dimensions
		});
		this.calculatedMembersBox = Ext.create('Ext.Panel', {
		    width: 800,
		    height: 450,
		    layout: {
		        type: 'hbox',
		        align: 'stretch'
		    },
		    anchor    : '100%',
		    renderTo: Ext.getBody(),
		    items: [innerLeftPanel, innerRightPanel]
		});
		

		Ext.apply(this, {
			items: [this.calculatedMembersBox],
			bbar:[			        
		             '->',  
		             {
		            	 text: LN('sbi.common.cancel'),
		            	 handler: function(){
		            		 thisPanel.destroy();
		            	 }
		             },
		             {
		            	 text: LN('sbi.common.ok'),
		            	 handler: function(){
		            		 thisPanel.sendExpression(Ext.getCmp('expFieldTextId').getValue(), Ext.getCmp('expTextAreaId').getValue());
		            		 thisPanel.destroy();
		            	 }
		             }
		         ]
		});
		this.callParent();
	},
	
		sendExpression: function(name, formula){
			Sbi.olap.eventManager.executeCalculatedMemberExpression(name, formula);		
			this.destroy();
		}
});