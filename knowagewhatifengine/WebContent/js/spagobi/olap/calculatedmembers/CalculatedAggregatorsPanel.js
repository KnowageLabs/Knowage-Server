/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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


