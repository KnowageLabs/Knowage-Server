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