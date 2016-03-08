/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**
 *
 * It's the container of calculated operators
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedOperatorsPanel', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",
		title: 'Operators',
		border: false,
		bodyPadding: 10
	},
	initComponent: function() {
		var thisPanel = this;
		var plusButton = Ext.create('Ext.Button', {
		    text: '+',
		    scale: 'medium',
		    margin: '10 10 0 0',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});		
		var minusButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '-',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		var obelusButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '/',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		var timesButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '*',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var lessThanButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '<',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var lessThanOrEqualToButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '<=',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var greaterThanButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '>',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var greaterThanOrEqualToButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '>=',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var inequalityButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '<>',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var equalButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '=',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var andButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'AND',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});
		
		var orButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'OR',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var notButton = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'NOT',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		
		
		var leftParentheses = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '(',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		var rightParentheses = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: ')',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		
		Ext.apply(this, {
			items: [ 		
			        plusButton,
			        minusButton,
			        obelusButton,
			        timesButton,
			        lessThanButton,
			        lessThanOrEqualToButton,
			        greaterThanButton,
			        greaterThanOrEqualToButton,
			        inequalityButton,		        
			        andButton,
			        orButton,
			        notButton,
			        equalButton,
			        leftParentheses,
			        rightParentheses			        
			        ]
		});
		this.callParent();
	},
	
	setExpression: function(operator){
		Ext.getCmp('expTextAreaId').setValue(Ext.getCmp('expTextAreaId').getValue() + operator + ' '); 
	}

});


