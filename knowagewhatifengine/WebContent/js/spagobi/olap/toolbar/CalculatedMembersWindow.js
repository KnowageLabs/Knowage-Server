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
