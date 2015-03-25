/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

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