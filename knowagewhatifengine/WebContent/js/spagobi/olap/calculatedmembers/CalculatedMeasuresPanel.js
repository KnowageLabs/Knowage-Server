/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.define('Sbi.olap.calculatedmembers.CalculatedMeasuresPanel', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",
		title: 'Measures',
		border: false,
		bodyPadding: 10,
		store: null
		
	},
	measures: null,
	measuresPanel: null,
	constructor : function(config) {
		this.initConfig(config);
		this.store = Ext.create('Ext.data.Store', {
		    model: 'Sbi.olap.MemberModel'
		});
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.calculatedmembers && Sbi.settings.olap.calculatedmembers.CalculatedMeasuresPanel) {
			Ext.apply(this, Sbi.settings.olap.calculatedmembers.CalculatedMeasuresPanel);
		}
		this.callParent(arguments);
	},
	initComponent: function() {
		var thisPanel = this;
		thisPanel.measuresPanel = Ext.create('Ext.form.Panel', {
	        renderTo: Ext.getBody(),
	        border: false,
	        id : 'measButtonsPanelId'
	    });
		var measuresArray = Ext.decode(thisPanel.measures);
		this.store.removeAll();
		if(measuresArray){
			for(var i=0; i<measuresArray.length; i++){
				this.store.add(Ext.create("Sbi.olap.MemberModel", measuresArray[i]));
			}
		}
		 //create the buttons          
	        this.store.each(function(rec){
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
	            thisPanel.measuresPanel.insert(button);
	        });//each                                           

	    Ext.apply(this, {
			items: [
			        thisPanel.measuresPanel     
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
			   multiSelection: true
		   });
		win.show();
	}
	
});