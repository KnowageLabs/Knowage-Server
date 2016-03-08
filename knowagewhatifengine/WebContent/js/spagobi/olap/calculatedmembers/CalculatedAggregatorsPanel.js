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
 * It's the container of the aggregation operators
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */


Ext.define('Sbi.olap.calculatedmembers.CalculatedAggregatorsPanel', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",
		title: 'Aggregators',
		border: false,
		bodyPadding: 10
	},
	initComponent: function() {
		var thisPanel = this;
		var aggregateButton = Ext.create('Ext.Button', {
		    text: 'AGGREGATE',
		    scale: 'medium',
		    margin: '10 10 0 0',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(set, numeric_expression)');
		    }
		});		
		var avgButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'AVG',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(set, numeric_expression)');
		    }
		});	
		var countButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'COUNT',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(set)');
		    }
		});	
		var sumButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'SUM',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(set, numeric_expression)');
		    }
		});	
		
		var medianButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'MEDIAN',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(set, numeric_expression)');
		    }
		});	
		
		
		Ext.apply(this, {
			items: [ 		
			        aggregateButton,
			        avgButton,
			        countButton,
			        sumButton,
			        medianButton			
			        ]
		});
		this.callParent();
	},
	
	setExpression: function(operator){
		Ext.getCmp('expTextAreaId').setValue(Ext.getCmp('expTextAreaId').getValue() + operator + ' '); 
	}

});


