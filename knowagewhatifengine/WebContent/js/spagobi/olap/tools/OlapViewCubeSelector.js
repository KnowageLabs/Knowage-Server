/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Tool for the definition of the olap view.
 * It's the container of the dimensions
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.tools.OlapViewCubeSelector', {
	//class to extends
	extend: 'Ext.panel.Panel',

	config:{
		title: 'Cubes',
		border: false
	},

	constructor: function(config) {
		this.initConfig(config);

		// The data store containing the list of states
	    var cubes = Ext.create('Ext.data.Store', {
	        fields: ['id','name'],
	        data : [{id:'1', name:'Sales'},
	                {id:'2', name:'Inventory'}]
	    });

		var cubeStore = Ext.create('Ext.form.ComboBox', {
	        store: cubes,
	        displayField: 'name',
	        valueField: 'id',
	        queryMode: 'local',
	        triggerAction: 'all',
	        emptyText:"Select a Cube"
	    });

		this.items=[cubeStore];
		this.callParent(arguments);
	}
});

