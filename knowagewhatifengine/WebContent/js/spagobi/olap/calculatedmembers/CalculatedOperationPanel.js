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


