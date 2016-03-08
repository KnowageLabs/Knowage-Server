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