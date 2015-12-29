/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * It's the container of the dimensions 
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedDimensionsPanel', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",
		title: 'Dimensions',
		border: false,
		bodyPadding: 10,
		dimStore: null		
	},
	dimensions: null,
	dimensionsPanel: null,
	constructor : function(config) {
		this.initConfig(config);
		this.dimStore = Ext.create('Ext.data.Store', {
		    model: 'Sbi.olap.DimensionModel'
		});
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.calculatedmembers && Sbi.settings.olap.calculatedmembers.CalculatedDimensionsPanel) {
			Ext.apply(this, Sbi.settings.olap.calculatedmembers.CalculatedDimensionsPanel);
		}
		this.callParent(arguments);
	},
	initComponent: function() {
		var thisPanel = this;
		thisPanel.dimensionsPanel = Ext.create('Ext.form.Panel', {
	        renderTo: Ext.getBody(),
	        border: false,
	        id : 'dimButtonsPanelId'
	    });		
		var dimensionsArray = Ext.decode(thisPanel.dimensions);
		this.dimStore.removeAll();
		if(dimensionsArray){
			for(var i=0; i<dimensionsArray.length; i++){
				this.dimStore.add(Ext.create("Sbi.olap.DimensionModel", dimensionsArray[i]));
			}
		}
		 //create the buttons          
	     	this.dimStore.each(function(rec){
	            var button = {
	                xtype : 'button',
	                text : rec.raw.caption,
	                margin: '5 5 5 5',
	                renderTo: Ext.getBody(),
	                listeners : {
	                    click : function(){
	                    	thisPanel.createFilterWindow(rec);
	                    }
	                }
	            };
	            thisPanel.dimensionsPanel.insert(button);
	        });//each                                           


	    Ext.apply(this, {
			items: [
			        thisPanel.dimensionsPanel     
			        ]
		});
		this.callParent();
	},
	
	setExpression: function(operator){
		Ext.getCmp('expTextAreaId').setValue(Ext.getCmp('expTextAreaId').getValue() + operator + ' '); 
	},
	
	createFilterWindow: function(record){
		var win =   Ext.create("Sbi.olap.execution.table.OlapExecutionFilterTree",{
			   title: LN('sbi.olap.execution.table.filter.dimension.title'),
			   dimension: record,
			   multiSelection: false
		   });
		win.show();
		win.on("select", function(member){
			   this.setFilterValue(member);
		   },this);
	},
	
	/**
	 * Sets the value of the filter
	 * @param {Sbi.olap.MemberModel} member the value of the filter
	 */
	setFilterValue: function(members){
		if(members && members.length){
			for(var i=0; i<members.length; i++){
				var member = members[i];
				if(member){	
					this.selectedMember = member;
					var name =  this.selectedMember.uniqueName;
					this.setExpression(name);
				}
			}
		}
	}
	
});