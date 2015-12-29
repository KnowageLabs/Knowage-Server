/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * It's the container of wizard accordion panels
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */


Ext.define('Sbi.olap.calculatedmembers.CalculatedMembersRightSide', {
	extend: 'Ext.panel.Panel',
	config:{
		bodyStyle: "background-color: white",	
		bodyPadding: 10,
		overflowY: 'scroll'
	},
	dimensions: null,
	calculatedOperationsBox: null,
	
	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.calculatedmembers && Sbi.settings.olap.calculatedmembers.CalculatedMembersRightSide) {
			Ext.apply(this, Sbi.settings.olap.calculatedmembers.CalculatedMembersRightSide);
		}
		this.callParent(arguments);
	},
	
	initComponent: function() {
		var me = this;
		var calculatedOperationPanel = Ext.create('Sbi.olap.calculatedmembers.CalculatedOperationPanel', {
			border: false
	    });
		var calculatedDataPanel = Ext.create('Sbi.olap.calculatedmembers.CalculatedDataPanel', {
			border: false,
			dimensions: me.dimensions,
			margin: '40 0 0 0'
			
	    });
		this.calculatedRightBox = Ext.create('Ext.panel.Panel', {
			bodyStyle: 'margin:0px 15px 0px 0px',
			border: false,
			items: [calculatedOperationPanel, calculatedDataPanel]
		});
	
		Ext.apply(this, {			
			items: [this.calculatedRightBox]
			
		});
		this.callParent();
	}
	
	
	
});