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
 * Window that allows the management of versions
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */



Ext.define('Sbi.olap.toolbar.VersionManagerWindow', {
	extend: 'Ext.window.Window',

	config:{
		height: 400,
		width: 300,
		actualVersion: null,
		autoScroll: true,
		bodyStyle: "background-color: white",
		title: LN("sbi.olap.toolbar.versionmanagerwindow.version.title")
	},

	/**
     * @property {Ext.grid.Panel} grid
     *  The grid panel with the versions
     */
	grid: null,

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.toolbar && Sbi.settings.olap.toolbar.VersionManagerWindow) {
			Ext.apply(this, Sbi.settings.olap.toolbar.VersionManagerWindow);
		}

		var service = Ext.create("Sbi.service.RestService",{
			url: "version"
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Sbi.olap.VersionModel',
			proxy: {
				type: 'rest',
				url: service.getRestUrlWithParameters(),
				extraParams: service.getRequestParams()
			},
			autoLoad: true
		});

		var selectionModel = Ext.create('Ext.selection.CheckboxModel');
		this.grid = Ext.create('Ext.grid.Panel', {
			store: store,
			selModel: selectionModel,
			selection: "checkboxmodel",
			columns: [
			          { text: 'Id',  dataIndex: 'id', flex:1 }
			         , { text: LN('sbi.common.name'), dataIndex: 'name', flex: 2 }
			         , { text: LN('sbi.common.description'), dataIndex: 'description', flex: 5 }
			          ]
		});

		var thisPanel = this;
		this.items= [this.grid];
		this.bbar = [
		             '->',    {
		            	 text: LN('sbi.common.cancel'),
		            	 handler: function(){
		            		 thisPanel.destroy();
		            	 }
		             },    {
		            	 text: LN('sbi.common.select'),
		            	 tooltip: LN('sbi.olap.toolbar.versionmanagerwindow.version.select.warning'),
		            	 handler: function(){
		            		 var selected = thisPanel.grid.getSelectionModel( ).getSelection();
		            		 var itemsToDelete = "";
		            		 if(selected && selected.length>0){

		            			 if(selected.length  == thisPanel.grid.getStore().getCount( )){
		            				 Sbi.exception.ExceptionHandler.showWarningMessage(LN("sbi.olap.toolbar.versionmanagerwindow.version.no.cancel.all"));
		            				 return;
		            			 }

		            			 for(var i=0; i<selected.length; i++){
		            				 var id = selected[i].get("id");
		            				 if(id ==thisPanel.actualVersion){
		            					 Sbi.exception.ExceptionHandler.showWarningMessage(LN("sbi.olap.toolbar.versionmanagerwindow.version.no.cancel.current"));
		            					 return;
		            				 }
		            				 itemsToDelete = itemsToDelete+ ","+ id;
		            			 }
		            		 }

		            		 if(itemsToDelete.length>1){
		            			 itemsToDelete = itemsToDelete.substring(1);
		            			 Sbi.olap.eventManager.deleteVersions(itemsToDelete);
		            		 }

		            		 thisPanel.destroy();
		            	 }
		             }];

		this.callParent(arguments);
	}




});
