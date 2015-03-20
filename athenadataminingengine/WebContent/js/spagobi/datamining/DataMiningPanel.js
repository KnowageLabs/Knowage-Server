/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *     
 *  @author
 *  Monica Franceschini
 */
 
  
Ext.define('Sbi.datamining.DataMiningPanel', {
	extend: 'Ext.panel.Panel',
	layout: {
        type: 'fit',
        align: 'left'
    },
	
	config:{
		padding: 5
		, border: 0
	},
	
	
	commandsTabPanel: null,
	
	constructor : function(config) {
		this.initConfig(config||{});
		
		this.commandsTabPanel = Ext.create('Sbi.datamining.CommandsTabPanel',{
			itsParent: this, 
			listeners: {
	            'tabchange': function (tabPanel, tab) {

	               tabPanel.setCommandAutoMode(tab.commandName);
	               
	            }
	        }

		}); 
		
		this.callParent(arguments);
	},

	initComponent: function() {
		Ext.apply(this, {
			items: [this.commandsTabPanel]
		});

		this.callParent();
	}
	
	
});