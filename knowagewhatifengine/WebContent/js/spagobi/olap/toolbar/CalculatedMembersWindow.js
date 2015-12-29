/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Window that performs calculated members operation
 *
 *
 */

Ext.define('Sbi.olap.toolbar.CalculatedMembersWindow', {
	extend: 'Ext.window.Window',
	config:{
		height: 400,
		width: 400,
		actualVersion: null,
		autoScroll: true,
		bodyStyle: "background-color: white",
		title: LN("sbi.olap.toolbar.calculatedmemberswindow.title")

	},

	grid: null,
	constructor : function(config) {

		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.toolbar && Sbi.settings.olap.toolbar.CalculatedMembersWindow) {
			Ext.apply(this, Sbi.settings.olap.toolbar.CalculatedMembersWindow);
		}
		var thisPanel = this;
		var service = Ext.create("Sbi.service.RestService", {
			url: "calculatedmembers",
			method: 'GET',
			async: true
		});
		var user=Ext.define('User', {
		    extend: 'Ext.data.Model',
		    fields: ['id', 'name']
		});

		var calculatedStore = Ext.create('Ext.data.Store', {

			model: user,
			//autoLoad: true,
		    autoSync: true,
			proxy: {
				type: 'rest',
				url: service.getRestUrlWithParameters(),
				extraParams: service.getRequestParams(),
				reader: {
		             type: 'json'
		         }
			}
		});

		calculatedStore.load({


			callback: function(datastore, records, successful, eOpts ){
		    },
		    scope: this
		});




		this.grid = Ext.create('Ext.grid.Panel', {
			store: calculatedStore,
			columns: [
			          { text: 'Name',  dataIndex: 'name', flex:1 }
			          ]
		});
		var editorName = Ext.create('Ext.form.field.TextArea', {
			flex: 2
		});

		var editorFormula = Ext.create('Ext.form.field.TextArea', {
			flex: 2
		});

		var button = Ext.create('Ext.Button', {
		    text: 'Submit expression',
		    flex: 1,
		    handler: function() {

		    	thisPanel.sendExpression(editorName.getValue(), editorFormula.getValue());
		    }
		});
		var elPanel = Ext.create('Ext.panel.Panel', {
			frame: false,
			layout: {
				align : 'stretch',
				type: 'vbox'
			},
			height: 300,
		   items: [editorName, editorFormula, button]
		});



		this.items= [elPanel];

		this.callParent(arguments);
	},

	sendExpression: function(name, formula){

		Sbi.olap.eventManager.executeCalculatedMemberExpression(name, formula);
		this.destroy();
	}

});
