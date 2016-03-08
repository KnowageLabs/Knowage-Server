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