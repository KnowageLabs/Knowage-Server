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
 * Object name
 * 
 * ManageConfig
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors
 * 
 * Monia Spinelli (monia.spinelli@eng.it)
 */

Ext.ns("Sbi.config");

Sbi.config.ToolbarConfig = function(config) {
	
	Ext.apply(this,config);
	//Creare domani
	var c = ( [ {
		iconCls : 'icon-domain-add',
		text : 'Add',
		handler : function() {
			var record = new this.Record();
			record.set('IS_ACTIVE', 'true');
			record.set('VALUE_TYPE', 'STRING');
			this.editor.stopEditing();
			this.store.insert(0, record);
			this.grid.getView().refresh();
			this.grid.getSelectionModel().selectRow(0);
			this.editor.startEditing(0);
		},
		scope : this
	}, {
		iconCls : 'icon-domain-delete',
		text : 'Delete',
		// disabled: true,
		handler : function() {
			this.editor.stopEditing();
			var s = this.grid.getSelectionModel().getSelections();
			for ( var i = 0, r; r = s[i]; i++) {
				var id = r.get('ID');
				if(id != undefined && id != null){
					var params = {
							ID: r.get('ID')
					};

					Ext.Ajax.request( {
						url : this.crudServices['deleteItemService'],
						params : params,
						// method: 'GET',
						success : function(response, options) {
						 	response = Ext.util.JSON.decode( response.responseText );
						    var index = this.store.find( "ID", response.ID );
						    var record =  this.store.getAt(  index ) ;
						    if(record) {
						    	this.store.remove(record);
								Ext.MessageBox.show({
						            title: LN('sbi.generic.info'),
						            msg: LN('sbi.config.manageconfig.delete'),
						            modal: false,
						            buttons: Ext.MessageBox.OK,
						            width:300,
						            icon: Ext.MessageBox.INFO,
						            animEl: 'root-menu'           
						           });
							}
						},
						failure : Sbi.exception.ExceptionHandler.handleFailure,
						scope : this
					});
				}
				else{
					this.store.remove(r);
				}
			}
		},
		scope : this
	},
	{
		xtype: 'tbspacer', width: 250
	},
	{
		iconCls : 'icon-domain-filter',
		xtype: 'splitbutton',
		text : 'Category',
		showText: true,
	    prependText: 'View as '
	},
		        // begin using the right-justified button container
		        '->', // same as {xtype: 'tbfill'}, // Ext.toolbar.Fill
	{
		xtype    : 'textfield',
		name     : 'LABEL',
		emptyText: 'enter search Label'
	},
	{
		xtype: 'tbspacer', width: 50
	},
	{
		xtype    : 'textfield',
		name     : 'NAME',
		emptyText: 'enter search Name'
	},
	{
		xtype: 'tbspacer', width: 50
	}
	] );
	
	// constructor
	Sbi.config.ToolbarConfig.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.config.ToolbarConfig, Ext.Toolbar, {
	initToolbar : function() {
		this.gridToolbar = new Ext.Toolbar( )
	}
});