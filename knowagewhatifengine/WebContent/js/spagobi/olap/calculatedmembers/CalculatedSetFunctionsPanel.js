/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**
 *
 * It's the container of set functions
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedSetFunctionsPanel', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",
		title: 'Set functions',
		border: false,
		bodyPadding: 10
	},
	initComponent: function() {
		var thisPanel = this;
		var membersBtn = Ext.create('Ext.Button', {
		    text: '.Members',
		    scale: 'medium',
		    margin: '10 10 0 0',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});		
		var childrenBtn = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '.Children',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		var descendantsBtn = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'Descendants',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(member, level)');
		    }
		});	
		var filterBtn = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'Filter',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+'(set, condition)');
		    }
		});	
	
		
		Ext.apply(this, {
			items: [ 		
			        membersBtn,
			        childrenBtn,
			        descendantsBtn,
			        filterBtn
			        ]
		});
		this.callParent();
	},
	
	setExpression: function(operator){
		Ext.getCmp('expTextAreaId').setValue(Ext.getCmp('expTextAreaId').getValue() + operator + ' '); 
	}

});


