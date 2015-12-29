/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * It's the container of the dimensions 
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */


Ext.define('Sbi.olap.calculatedmembers.CalculatedDataPanel', {
	extend: 'Ext.panel.Panel',
	 layout: {
	        type: 'accordion',
	        titleCollapse: false,
	        fill: false,
	        animate: true        
	    },
		config:{
			title: 'Data',
		    defaults: {
		        bodyStyle: 'padding:10px'
		    }
		},
		dimensions: null,
		constructor: function(config) {
			this.initConfig(config);
			if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.calculatedmembers && Sbi.settings.olap.calculatedmembers.CalculatedDataPanel) {
				Ext.apply(this, Sbi.settings.olap.calculatedmembers.CalculatedDataPanel);
			}
			this.callParent(arguments);
		},
		
		initComponent: function() {
			var thisPanel= this;
			var fakePanel = Ext.create('Ext.panel.Panel', {
				hidden: true,
				collapsed: false
			});
			var dimensionsPanel = Ext.create('Sbi.olap.calculatedmembers.CalculatedDimensionsPanel', {
				 title: 'Dimensions',
				 dimensions : thisPanel.dimensions
			});

			Ext.apply(this, {
				items: [ 		
	            fakePanel,
	            dimensionsPanel
	            ]
			});
			this.callParent();
		}
		
});


