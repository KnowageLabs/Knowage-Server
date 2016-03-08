/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**
 *
 * It's the container of wizard text field and text area
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedMembersLeftSide', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",	
		bodyPadding: 10
	},

	calculatedMemberNameText: null,
	
	calculatedExpressionTextArea: null,
	
	calculatedMemberLabelLeft: null,
	
	calculatedMemberLabelRight: null,
	
	calculatedMembersNameBox: null,
	
	
	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.calculatedmembers && Sbi.settings.olap.calculatedmembers.CalculatedMembersLeftSide) {
			Ext.apply(this, Sbi.settings.olap.calculatedmembers.CalculatedMembersLeftSide);
		}
		this.callParent(arguments);
	},
	
	initComponent: function() {
		
		this.calculatedMemberNameText = Ext.create('Ext.form.field.Text', {
	        fieldLabel: '',
	        hideLabel: true,
	        name: 'expressionTextField',
	        itemId: 'calculatedMemberTextField',
	        width: 200,
	        emptyText: 'calculated member name',
	        id : 'expFieldTextId'
	    });
		
		this.calculatedMemberLabelLeft = Ext.create('Ext.form.Label', {
	        forId: 'calculatedLeftLabelId',
	        id : 'calculatedNameLeftId'
	    });
		
		this.calculatedMemberLabelRight = Ext.create('Ext.form.Label', {
	        forId: 'calculatedRightLabelId',
	        text: ']',
	        id : 'calculatedNameRightId'
	    });
		
		this.calculatedMembersNameBox = Ext.create('Ext.Panel', {
			border: false,
		    layout: {
		        type: 'hbox',
		        align: 'middle'
		    },
		    renderTo: Ext.getBody(),
		    items: [this.calculatedMemberLabelLeft, this.calculatedMemberNameText, this.calculatedMemberLabelRight]
		});
		
		
		this.calculatedExpressionTextArea = Ext.create('Ext.form.field.TextArea', {
	        name      : 'expressionTextArea',
	        fieldLabel: 'Expression value',
	        labelAlign: 'top',
	        height    : 150,
	        width     : 400,
	        autoScroll: true,
	        id : 'expTextAreaId',
	        margin: '40 0 0 0'
			});
		
		
		Ext.apply(this, {
			items: [this.calculatedMembersNameBox,this.calculatedExpressionTextArea]
			
		});
		this.callParent();
	}
	
	
	
});

