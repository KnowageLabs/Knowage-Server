/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * Object name
 * 
 * ManageDomains
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

Ext.ns("Sbi.domain");

Sbi.domain.ManageDomains = function(config) {
	
	var c = Ext.apply( {
		title : 'Domains',
		layout : 'fit'
	}, config || {});

	var paramsList = {
		MESSAGE_DET : "DOMAIN_LIST"
	};
	var paramsSave = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "DOMAIN_SAVE"
	};
	var paramsDel = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "DOMAIN_DELETE"
	};

	this.crudServices = {};

	this.crudServices['manageListService'] = Sbi.config.serviceRegistry
			.getServiceUrl( {
				serviceName : 'DOMAIN_ACTION',
				baseParams : paramsList
			});
	this.crudServices['saveItemService'] = Sbi.config.serviceRegistry
			.getServiceUrl( {
				serviceName : 'DOMAIN_ACTION',
				baseParams : paramsSave
			});
	this.crudServices['deleteItemService'] = Sbi.config.serviceRegistry
			.getServiceUrl( {
				serviceName : 'DOMAIN_ACTION',
				baseParams : paramsDel
			});

	this.initGrid();

	c.items = [ this.grid ];

	// constructor
	Sbi.domain.ManageDomains.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.domain.ManageDomains, Ext.Panel, {

	grid : null,
	columnModel : null,
	store : null,
	gridToolbar : null,
	Record : null,
	editor : null

	// public methods
	,
	initGrid : function() {

		this.editor = new Ext.ux.grid.RowEditor( {
			saveText : 'Update',
			listeners : {
				afteredit : {
					fn : this.saveDomain,
					scope : this
				}
			}

		});
		
		  var pagingToolbar = new Ext.PagingToolbar ({ 
				  store: this.store, 
				  pageSize:20,
				  displayInfo :true 
		   });
		 

		this.initStore();
		this.store.load({});
		this.initColumnModel();
		this.initToolbar();
		this.grid = new Ext.grid.GridPanel( {
			store : this.store,
			cm : this.columnModel,
			tbar : this.gridToolbar,
			sm : new Ext.grid.RowSelectionModel( {
				singleSelect : true
			}),
			plugins : [ this.editor ],
			// width: 600,
			height : 300,
			margins : '0 5 5 5',
			viewConfig : {
				forceFit : true
			}
		});
	}

	,
	initToolbar : function() {
		this.gridToolbar = new Ext.Toolbar( [ {
			iconCls : 'icon-domain-add',
			text : 'Add',
			handler : function() {
				var record = new this.Record();
				this.editor.stopEditing();
				this.store.insert(0, record);
				this.grid.getView().refresh();
				this.grid.getSelectionModel().selectRow(0);
				this.editor.startEditing(0);
			},
			scope : this
		}, {
			// ref: '../removeBtn',
			iconCls : 'icon-domain-delete',
			text : 'Delete',
			// disabled: true,
			handler : function() {
				this.editor.stopEditing();
				var s = this.grid.getSelectionModel().getSelections();
				for ( var i = 0, r; r = s[i]; i++) {
					var id = r.get('VALUE_ID');
					if(id != undefined && id != null){
						var params = {
								VALUE_ID: r.get('VALUE_ID')
						};

					Ext.Ajax.request( {
						url : this.crudServices['deleteItemService'],
						params : params,
						// method: 'GET',
						success : function(response, options) {
						 	response = Ext.util.JSON.decode( response.responseText ); 
						    var index = this.store.find( "VALUE_ID", response.VALUE_ID );
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
		} ])
	}

	,
	initColumnModel : function() {
		this.columnModel = new Ext.grid.ColumnModel( [ {
			header : LN('sbi.domain.managedomains.fields.valuecd'),
			dataIndex : 'VALUE_CD',
			// width: 220,
			sortable : true,
			editor : {
				xtype : 'textfield'
				// allowBlank : false,
				// maxLength : 100,
				// maxLengthText :
				// LN('sbi.domain.managedomains.validation.maxlengthtext')
			}
		}, {
			header : LN('sbi.domain.managedomains.fields.valuenm'),
			dataIndex : 'VALUE_NM',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 40,
				maxLengthText : LN('sbi.domain.managedomains.validation.maxlengthtext')
			}
		}, {
			header : LN('sbi.domain.managedomains.fields.domaincd'),
			dataIndex : 'DOMAIN_CD',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 20,
				maxLengthText : LN('sbi.domain.managedomains.validation.maxlengthtext')
			}
		}, {
			header : LN('sbi.domain.managedomains.fields.domainnm'),
			dataIndex : 'DOMAIN_NM',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 40,
				maxLengthText : LN('sbi.domain.managedomains.validation.maxlengthtext')
			}
		}, {
			header : LN('sbi.domain.managedomains.fields.valueds'),
			dataIndex : 'VALUE_DS',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 160,
				maxLengthText : LN('sbi.domain.managedomains.validation.maxlengthtext')
			}
		} ]);
	}

	,
	initStore : function() {

		var fields = [ {
			name : 'VALUE_ID'
		}, {
			name : 'VALUE_CD'
		}, {
			name : 'VALUE_NM'
		}, {
			name : 'DOMAIN_CD'
		}, {
			name : 'DOMAIN_NM'
		}, {
			name : 'VALUE_DS'
		} ];

		this.store = new Ext.data.JsonStore( {

			root : 'response',
			idProperty : 'VALUE_ID',
			fields : fields,
			url : this.crudServices['manageListService']

		});
		this.Record = Ext.data.Record.create(fields);
	}

	,
	saveDomain : function(rowEditor, obj, record, rowIndex) {

		var p = {};
		if (record.get('VALUE_ID') != undefined && record.get('VALUE_ID') != null && record.get('VALUE_ID') !== '') {
			p.VALUE_ID = record.get('VALUE_ID');
		}

		p.VALUE_CD = record.get('VALUE_CD');
		p.VALUE_NM = record.get('VALUE_NM');
		p.DOMAIN_CD = record.get('DOMAIN_CD');
		p.DOMAIN_NM = record.get('DOMAIN_NM');
		p.VALUE_DS = record.get('VALUE_DS');

		Ext.Ajax.request( {
			url : this.crudServices['saveItemService'],
			params : p,
			method : 'POST',
			success : this.successSave.createDelegate(this, [record], true),
			failure : Sbi.exception.ExceptionHandler.handleFailure,
			scope : this
		});
	}
	,successSave : function(response, options, record) {
		var jsonResponse = Ext.util.JSON.decode(response.responseText);
		record.set('VALUE_ID', jsonResponse.VALUE_ID);
		record.commit();
		
		Ext.MessageBox.show({
            title: LN('sbi.generic.info'),
            msg: LN('sbi.config.manageconfig.save'),
            modal: false,
            buttons: Ext.MessageBox.OK,
            width:300,
            icon: Ext.MessageBox.INFO,
            animEl: 'root-menu'           
           });
	}
});