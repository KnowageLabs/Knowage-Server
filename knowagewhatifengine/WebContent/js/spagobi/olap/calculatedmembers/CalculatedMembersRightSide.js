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