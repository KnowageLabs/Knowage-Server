/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.DataStorePanel = function(config) {
	
	var defaultSettings = {
			title: LN('sbi.widgets.datastorepanel.title') ,
			displayInfo: true,
			pageSize: 25,
			sortable: false,
			sortMode: 'remote' // remote | local | auto
	};
			
	if(Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.dataStorePanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.widgets.dataStorePanel);
	}
			
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.services = this.services || new Array();	
	this.services['loadDataStore'] = this.services['loadDataStore'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'EXECUTE_FORM_QUERY_ACTION'
		, baseParams: this.baseParams || new Object()
	});
	
	/*
	this.services['exportDataStore'] = this.services['exportDataStore'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'EXPORT_RESULT_ACTION'
		, baseParams: this.baseParams || new Object()
	});
	*/
	
	this.initStore();
	this.initPanel();
	
	c = Ext.apply(c, {
		layout: 'fit',
		items: [this.grid]
	});
	
	// constructor
	Sbi.widgets.DataStorePanel.superclass.constructor.call(this, c);
	
	this.addEvents();
	
};

Ext.extend(Sbi.widgets.DataStorePanel, Ext.Panel, {
    
    services: null
	, store: null
	, paging: null
	, pageSize: null
	, pageNumber: null
    
	// ---------------------------------------------------------------------------------------------------
    // public methods
	// ---------------------------------------------------------------------------------------------------
	
	, execQuery:  function(baseParams) {  
		this.store.removeAll();
		this.store.baseParams = baseParams;
		var requestParameters = {start: 0, limit: this.pageSize };
		this.store.load({params: requestParameters});
	}

	/*
	, exportResult: function(mimeType) {
		var form = document.getElementById('export-form');
		if(!form) {
			var dh = Ext.DomHelper;
			form = dh.append(Ext.getBody(), {
			    id: 'export-form'
			    , tag: 'form'
			    , method: 'post'
			    , cls: 'export-form'
			});
		}
		
		form.action = this.services['exportDataStore'] + '&MIME_TYPE=' + mimeType +'&RESPONSE_TYPE=RESPONSE_TYPE_ATTACHMENT';
		form.submit();
	}
	*/
  	
	// ---------------------------------------------------------------------------------------------------
	// private methods
	// ---------------------------------------------------------------------------------------------------
	
	, initStore: function() {
		
		this.proxy = new Ext.data.HttpProxy({
	           url: this.services['loadDataStore']
	           , timeout : 300000
	   		   , failure: this.onDataStoreLoadException
	    });
		
		this.store = new Ext.data.Store({
	        proxy: this.proxy,
	        reader: new Ext.data.JsonReader(),
	        remoteSort: true
	    });
		
		this.store.on('metachange', function( store, meta ) {
		  
		   for(var i = 0; i < meta.fields.length; i++) {
			   if(meta.fields[i].type) {
				   var t = meta.fields[i].type;
				   //if(t === 'float' || t ==='int') t = 'number';
				   if (meta.fields[i].format) { // format is applied only to numbers
					   var format = Sbi.commons.Format.getFormatFromJavaPattern(meta.fields[i].format);
					   var f = Ext.apply( Sbi.locale.formats[t], format);
					   meta.fields[i].renderer = Sbi.commons.Format.numberRenderer(f);
				   } else {
					   meta.fields[i].renderer = Sbi.locale.formatters[t];
				   }			   
			   }
			   
			   if(meta.fields[i].subtype && meta.fields[i].subtype === 'html') {
				   meta.fields[i].renderer  =  Sbi.locale.formatters['html'];
			   }
			   if(meta.fields[i].subtype && meta.fields[i].subtype === 'timestamp') {
				   meta.fields[i].renderer  =  Sbi.locale.formatters['timestamp'];
			   }
			   
			   if(this.sortable === false) {
				   meta.fields[i].sortable = false;
			   } else {
				   if(meta.fields[i].sortable === undefined) { // keep server value if defined
					   meta.fields[i].sortable = true;
				   }
			   }
			   
		   }
		   meta.fields[0] = new Ext.grid.RowNumberer();
		   this.grid.getColumnModel().setConfig(meta.fields);
		}, this);
		
		this.store.on('load', this.onDataStoreLoaded, this);
		
	}

	, initPanel: function() {
		var cm = new Ext.grid.ColumnModel([
			new Ext.grid.RowNumberer(), 
			{
				header: "Data",
				dataIndex: 'data',
				width: 75
			}
		]);
		
		/*
		this.exportTBar = new Ext.Toolbar({
			items: [
			    new Ext.Toolbar.Button({
		            tooltip: LN('sbi.widgets.datastorepanel.button.tt.exportto') + ' pdf',
		            iconCls:'pdf',
		            handler: this.exportResult.createDelegate(this, ['application/pdf']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.widgets.datastorepanel.button.tt.exportto') + ' rtf',
		            iconCls:'rtf',
		            handler: this.exportResult.createDelegate(this, ['application/rtf']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.widgets.datastorepanel.button.tt.exportto') + ' xls',
		            iconCls:'xls',
		            handler: this.exportResult.createDelegate(this, ['application/vnd.ms-excel']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.widgets.datastorepanel.button.tt.exportto') + ' csv',
		            iconCls:'csv',
		            handler: this.exportResult.createDelegate(this, ['text/csv']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.widgets.datastorepanel.button.tt.exportto') + ' jrxml',
		            iconCls:'jrxml',
		            handler: this.exportResult.createDelegate(this, ['text/jrxml']),
		            scope: this
			    })
			]
		});
		*/
		
		this.warningMessageItem = new Ext.Toolbar.TextItem('<font color="red">' 
				+ LN('sbi.widgets.datastorepanel.grid.beforeoverflow') 
				+ ' [' + Sbi.config.queryLimit.maxRecords + '] '
				+ LN('sbi.widgets.datastorepanel.grid.afteroverflow') 
				+ '</font>');
		
		
		this.pagingTBar = new Ext.PagingToolbar({
            pageSize: this.pageSize,
            store: this.store,
            displayInfo: this.displayInfo,
            displayMsg: LN('sbi.widgets.datastorepanel.grid.displaymsg'),
            emptyMsg: LN('sbi.widgets.datastorepanel.grid.emptymsg'),
            beforePageText: LN('sbi.widgets.datastorepanel.grid.beforepagetext'),
            afterPageText: LN('sbi.widgets.datastorepanel.grid.afterpagetext'),
            firstText: LN('sbi.widgets.datastorepanel.grid.firsttext'),
            prevText: LN('sbi.widgets.datastorepanel.grid.prevtext'),
            nextText: LN('sbi.widgets.datastorepanel.grid.nexttext'),
            lastText: LN('sbi.widgets.datastorepanel.grid.lasttext'),
            refreshText: LN('sbi.widgets.datastorepanel.grid.refreshtext')
        });
		this.pagingTBar.on('render', function() {
			this.pagingTBar.addItem(this.warningMessageItem);
			this.warningMessageItem.setVisible(false);
			//this.pagingTBar.loading.setVisible(false); // it does not work with Ext 3.2.1
		}, this);
		
		// create the Grid
	    this.grid = new Ext.grid.GridPanel({
	    	store: this.store,
	        cm: cm,
	        clicksToEdit:1,
	        style:'padding:10px',
	        frame: true,
	        border:true,  	        
	        collapsible: false,
	        loadMask:false,// true,
	        viewConfig: {
	            forceFit:false,
	            autoFill: true,
	            enableRowBody:true,
	            showPreview:true
	        },
	        
	        //tbar:this.exportTBar,
	        bbar: this.pagingTBar
	    });   
	}

	, onDataStoreLoaded: function(store) {
		
		 var recordsNumber = store.getTotalCount();
       	 if(recordsNumber == 0) {
       		Ext.Msg.show({
				   title: LN('sbi.generic.info'),
				   msg: LN('sbi.widgets.datastorepanel.grid.emptywarningmsg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO,
				   modal: false
			});
       	 }
       	 
       	 if (Sbi.config.queryLimit.maxRecords !== undefined && recordsNumber > Sbi.config.queryLimit.maxRecords) {
       		if (Sbi.config.queryLimit.isBlocking) {
       			Sbi.exception.ExceptionHandler.showErrorMessage(this.warningMessageItem, LN('sbi.generic.error'));
       		} else {
       			this.warningMessageItem.show();
       		}
       	 } else {
       		this.warningMessageItem.hide();
       	 }
	}
	
	, onDataStoreLoadException: function(response, options) {
		Sbi.exception.ExceptionHandler.handleFailure(response, options);
	}

});