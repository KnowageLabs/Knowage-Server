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


