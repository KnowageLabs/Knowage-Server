/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**
 *
 * It's the container of the members functions 
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedMembersFunctionsPanel', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",
		title: 'Members',
		border: false,
		bodyPadding: 10
	},
	initComponent: function() {
		var thisPanel = this;
		
		var currentMemberBtn = Ext.create('Ext.Button', {
		    text: '.CurrentMember',
		    scale: 'medium',
		    margin: '10 10 0 0',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});		
		var prevMemberBtn = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: '.PrevMember',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text);
		    }
		});	
		var parallelPeriodBtn = Ext.create('Ext.Button', {
			margin: '10 10 0 0',
			scale: 'medium',
		    text: 'ParallelPeriod',
		    renderTo: Ext.getBody(),
		    handler: function() {
		    	thisPanel.setExpression(this.text+ '(level, numeric_expression)');
		    }
		});	
	
		
		Ext.apply(this, {
			items: [ 		
			        currentMemberBtn,
			        prevMemberBtn,
			        parallelPeriodBtn			
			        ]
		});
		this.callParent();
	},
	
	setExpression: function(operator){
		Ext.getCmp('expTextAreaId').setValue(Ext.getCmp('expTextAreaId').getValue() + operator + ' '); 
	}

});


