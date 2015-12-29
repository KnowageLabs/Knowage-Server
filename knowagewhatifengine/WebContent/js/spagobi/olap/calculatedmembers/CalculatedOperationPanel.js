/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * It's the container of wizard operations
 *
 *
 *  @author
 *  Maria Caterina Russo from Osmosit
 */

Ext.define('Sbi.olap.calculatedmembers.CalculatedOperationPanel', {
	extend: 'Ext.panel.Panel',
   layout: {
        type: 'accordion',
        titleCollapse: false,
        fill: false,
        animate: true
    },
	config:{
		title: 'Operations',
	   defaults: {
	        // applied to each contained panel
	        bodyStyle: 'padding:10px'
	    }
	},

	constructor: function(config) {
		this.initConfig(config);
		
		var fakePanel = Ext.create('Ext.panel.Panel', {
			hidden: true,
			collapsed: false
		});

		var operators = Ext.create('Sbi.olap.calculatedmembers.CalculatedOperatorsPanel', {
			 title: 'Operators'
		});
		var aggregators = Ext.create('Sbi.olap.calculatedmembers.CalculatedAggregatorsPanel', {
			title: 'Aggregators'
		});
		
		var memberFunctions = Ext.create('Sbi.olap.calculatedmembers.CalculatedMembersFunctionsPanel', {
			title: 'Member Functions'
		});
		
		var setFunctions = Ext.create('Sbi.olap.calculatedmembers.CalculatedSetFunctionsPanel', {
			title: 'Set Functions'
		});

		this.items=[fakePanel, operators, aggregators, memberFunctions, setFunctions];
		this.callParent(arguments);
	}
});


