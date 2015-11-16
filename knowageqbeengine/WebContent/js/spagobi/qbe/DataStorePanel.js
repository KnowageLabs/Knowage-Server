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
	
	this.baseConfig = config;
	

	var c = Ext.apply({
		// set default values here
	}, config || {});
	
	this.services = this.services || new Array();
	var params = {};
	this.services['loadDataStore'] = this.services['loadDataStore'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'EXEC_QUERY_ACTION'
		, baseParams: params
	});
	this.services['exportDataStore'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'EXPORT_RESULT_ACTION'
		, baseParams: params
	});
	this.services['exportToExternalService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'INVOKE_EXTERNAL_SERVICE_ACTION'
		, baseParams: params
	});
		
	this.initStore();
	this.initPanel();
	
	c = Ext.apply(c, {
		//title: LN('sbi.qbe.datastorepanel.title'),  
		layout: 'fit',
		items: [this.grid]
	});
	
	// constructor
	Sbi.widgets.DataStorePanel.superclass.constructor.call(this, c);
    
    this.addEvents();
};

Ext.extend(Sbi.widgets.DataStorePanel, Ext.Panel, {
    
    services: null
	, baseConfig: null
    , store: null
	, paging: null
	, pageSize: null
	, pageNumber: null
	, oldColumns: null
	, notFirstPage : false
	, columnsPosition : null 
	, columnsWidth : null
   
	// columnsPosition is an array with the position of the columns respect the initial configuration id est:
	// Select clause fields:                                  A B C D E
	// DataStore fields order (after some user operation):    D C E A B
	// columnsPosition array:                                 3 4 1 0 2
	// Because the new position of A is 3, new position of B is 4 ....
	// With this array we can keep the order of the columns after a subsequent execution or after a page change 
	
	
	
	
   
	// ---------------------------------------------------------------------------------------------------
    // public methods
	// ---------------------------------------------------------------------------------------------------
	
	/*
	, execQuery:  function(query, freeFiltersForm, ambiguousFieldsPaths) {
		this.firstPage= true;
		this.store.removeAll();
		this.store.baseParams = Ext.apply({
			id : query.id
			, ambiguousFieldsPaths : Sbi.commons.JSON.encode(ambiguousFieldsPaths)
		}, freeFiltersForm || {});
		var requestParameters = {start: 0, limit: 25 };
		this.store.load({params: requestParameters});
	}
	*/
	
	, execQuery:  function(query, freeFiltersForm) {
		this.firstPage = true;
		this.store.removeAll();
		this.store.baseParams = Ext.apply({ id : query.id }, freeFiltersForm || {});
		var requestParameters = {start: 0, limit: 25 };
		this.store.load({params: requestParameters});
	}

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
  	
	// ---------------------------------------------------------------------------------------------------
	// private methods
	// ---------------------------------------------------------------------------------------------------
	
	, renderHtml: function(value, meta, record, row, col, store){
		
	}
	
	
	, initStore: function() {
		this.proxy = new Ext.data.HttpProxy({
	           url: this.services['loadDataStore']
	           , timeout : Sbi.config.queryExecutionTimeout || 300000
	   		   , failure: this.onDataStoreLoadException
	    });
		
		this.store = new Ext.data.Store({
	        proxy: this.proxy,
	        reader: new Ext.data.JsonReader(),
	        remoteSort: true
	    });
		
		this.store.on('metachange', function( store, meta ) {
			
			meta.fields[0] = new Ext.grid.RowNumberer();
			this.alias2FieldMetaMap = {};
			var fields = meta.fields;
			var newColumns = new Array();
			newColumns.length = fields.length;
			
			//The following code is used for keep the order of the columns after the execution of the query 
			
			//1) first of all we check if the list of fields is changed 
			if(this.firstPage){
				for(var i = 0; i <meta.fields.length ;i++) {
					newColumns[i]= meta.fields[i].header;
				}	
			}
			
			var val=true;
			if(this.oldColumns != null){
				if(this.oldColumns.length != newColumns.length){
					val=false;
				}else{
					for(var i = 1; i <this.oldColumns.length ;i++) {
						val = val && (this.oldColumns[i]==newColumns[i]);
					}	
				}
			}
			
			//2) if the list of fields is changed we should reload the columnsPosition and columnsWidth arrays
			if(this.oldColumns != null && this.firstPage && !val){

				
				
//				Suppose the columns in the select clause before the re executions are
//				A B C D E . After some operation the visualization in the data store panel is C B E D A
//				So we have the following arrays
//				oldColumns = A B C D E  
//				columnsPosition= 4 1 0 3 2
//				Now suppose the new fields are: A X D K. 
//				The new visualization should keeps the order and so should look like D A X K

				
//				The first step is calculate the array fieldsOrder that maps the new fields in the oldColumns array:
//				fieldsOrder: 0, , 3,    : A live in the position 0 in oldColumns, D in position 3, and the new fields have no position.

				var filedsOrder = new Array();
				var name;
				filedsOrder.length = fields.length;
				for(var i = 0; i <fields.length ;i++) {
					name = fields[i].header;
					for(var j = 0; j < this.oldColumns.length; j++) {
						if(name == this.oldColumns[j]){
							filedsOrder[i] = j;
							this.oldColumns[j]="";
							break;
						}			   
					}
				}

				
//				Now we change the indexes in fieldsOrder with previous position of the linked element. 
//				In code fieldsOrder[i] = columnsPosition[fieldsOrder[i]] and fieldsOrder: 4, , 3,    
//				Clean the array fieldsOrder filtering the empty spaces. 
//				The result is saved in the array cleanFreshPos = 4,3.

				
				var cleanFreshPos = new Array();
				var sortedCleanFreshPos = new Array();
				for(var i = 0; i <filedsOrder.length ;i++) {
					if(filedsOrder[i]!=null){
						cleanFreshPos.push(this.columnsPosition[filedsOrder[i]]);
						sortedCleanFreshPos.push(this.columnsPosition[filedsOrder[i]]);
						filedsOrder[i]=this.columnsPosition[filedsOrder[i]];
					}
				}
				
				var width = new Array();
				width.length = filedsOrder.length;

				sortedCleanFreshPos.sort();

//				Normalize the array cleanFreshPos: force the indexes to be an enumeration between 1 to cleanFreshPos.length. 
//				So normalizedCleanFreshPos = 2,1
//				We have to normalize the array because these values are the new position of the linked elements. 
				
				var normalizedCleanFreshPos = new Array();
				normalizedCleanFreshPos.length = sortedCleanFreshPos.length;

				for(var j = 0; j <cleanFreshPos.length ;j++) {
					for(var y=0; y<sortedCleanFreshPos.length; y++){
						if(sortedCleanFreshPos[y]==cleanFreshPos[j]){
							normalizedCleanFreshPos[j]=y+1;
							break;
						}
					}
				}
				
				
//				At the end we create the new array columnsPosition.
//				We take the fields we have also in the previous query and we save them at the beginning (with the normalizedCleanFreshPos array) 
//				of the array columnsPosition. 
//				Than we push the new fields in the tail of the array. 

				
				this.columnsPosition = new Array();
				this.columnsPosition.length = filedsOrder.length;
				this.columnsPosition[0]=0;//the position 0 is for the column with the row indexes
				
				var k=1;
				var m=0;
				for(var i = 1; i <filedsOrder.length ;i++) {
					if(filedsOrder[i]==null){//new fields
						this.columnsPosition[i]=k+cleanFreshPos.length;//in the tail
						width[k+cleanFreshPos.length] = 100;
						k++;
					}else{//old fields
						for(var j = 0; j <cleanFreshPos.length ;j++) {
							if(cleanFreshPos[j]==filedsOrder[i]){
								this.columnsPosition[i]=normalizedCleanFreshPos[m];
								width[normalizedCleanFreshPos[m]]=this.columnsWidth[filedsOrder[i]];
								m++;
							}
						}
					}
				}
				this.oldColumns=null;
				this.columnsWidth = width;
			}

	    	
			for(var i = 0; i < meta.fields.length; i++) {
			   if(meta.fields[i].type) {
				   var t = meta.fields[i].type;
				   //if(t === 'float' || t ==='int') t = 'number';
				   if (meta.fields[i].format) { // format is applied only to numbers
					   var format = Sbi.qbe.commons.Format.getFormatFromJavaPattern(meta.fields[i].format);
					   var f = {};
					   var baseFormat = Sbi.locale.formats[t];
					   for (var prop in baseFormat) { 
						   f[prop] = baseFormat[prop]; 
					   }
					   for (var prop in format) { 
						   f[prop] = format[prop]; 
					   }
					   meta.fields[i].renderer = Sbi.qbe.commons.Format.numberRenderer(f);
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
		   }

		   for(var i = 0, l = fields.length, f; i < l; i++) {
			   f = fields[i];
			   if( typeof f === 'string' ) {
				   f = {name: f};
			   }
			     
			   f.header = f.header || f.name;
			   this.alias2FieldMetaMap[f.header] = f;
		   }
		   
		   if(this.oldColumns == null){
				this.oldColumns = new Array();
				for(var i = 0; i <meta.fields.length ;i++) {
					this.oldColumns[i]= meta.fields[i].header;
				}
			}
		   
		   if(this.columnsPosition!=null){
			  var fields2 = new Array();
			  fields2.length = this.columnsPosition.length;
	
			  for(var i = 0; i<fields.length; i++) {
				  fields2[this.columnsPosition[i]] = fields[i];
			  }
			  
			  meta.fields = fields2;
		
		  	}else{
			  this.columnsPosition = new Array();
			  this.columnsWidth = new Array();
			  for(var i = 0; i <fields.length ;i++) {
				  this.columnsPosition[i]= i;
			  }
			  this.columnsWidth[0]=23;
			  for(var i = 1; i <fields.length ;i++) {
				  this.columnsWidth[i]= 100;
			  }
				
		  	}
		   	this.grid.getColumnModel().setConfig(meta.fields);
		   	
		    for(var y=1; y<this.columnsWidth.length; y++){
		    	this.grid.getColumnModel().setColumnWidth(y,this.columnsWidth[y]);
		   	}

			this.firstPage = false;


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
		
		this.exportTBar = new Ext.Toolbar({
			items: [
			    new Ext.Toolbar.Button({
		            tooltip: LN('sbi.qbe.datastorepanel.button.tt.exportto') + ' pdf',
		            iconCls:'pdf',
		            handler: this.exportResult.createDelegate(this, ['application/pdf']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.qbe.datastorepanel.button.tt.exportto') + ' rtf',
		            iconCls:'rtf',
		            handler: this.exportResult.createDelegate(this, ['application/rtf']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.qbe.datastorepanel.button.tt.exportto') + ' xls',
		            iconCls:'xls',
		            handler: this.exportResult.createDelegate(this, ['application/vnd.ms-excel']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.qbe.datastorepanel.button.tt.exportto') + ' csv',
		            iconCls:'csv',
		            handler: this.exportResult.createDelegate(this, ['text/csv']),
		            scope: this
			    }),
			    new Ext.Toolbar.Button({
		            tooltip:LN('sbi.qbe.datastorepanel.button.tt.exportto') + ' jrxml',
		            iconCls:'jrxml',
		            handler: this.exportResult.createDelegate(this, ['text/jrxml']),
		            scope: this
			    })
			]
		});
		
		this.warningMessageItem = new Ext.Toolbar.TextItem('<font color="red">' 
				+ LN('sbi.qbe.datastorepanel.grid.beforeoverflow') 
				+ ' [' + Sbi.config.queryLimit.maxRecords + '] '
				+ LN('sbi.qbe.datastorepanel.grid.afteroverflow') 
				+ '</font>');
		
		
		
		
		this.pagingTBar = new Ext.PagingToolbar({
            pageSize: 25,
            store: this.store,
            displayInfo: true,
            displayMsg: LN('sbi.qbe.datastorepanel.grid.displaymsg'),
            emptyMsg: LN('sbi.qbe.datastorepanel.grid.emptymsg'),
            beforePageText: LN('sbi.qbe.datastorepanel.grid.beforepagetext'),
            afterPageText: LN('sbi.qbe.datastorepanel.grid.afterpagetext'),
            firstText: LN('sbi.qbe.datastorepanel.grid.firsttext'),
            prevText: LN('sbi.qbe.datastorepanel.grid.prevtext'),
            nextText: LN('sbi.qbe.datastorepanel.grid.nexttext'),
            lastText: LN('sbi.qbe.datastorepanel.grid.lasttext'),
            refreshText: LN('sbi.qbe.datastorepanel.grid.refreshtext'),
            //  prependButtons: true,
          	//buttonAlign  :"center",

            
    	    buttons: [{
    	    	//pressed: true,
    	    	style: "margin-left: 30px;",
    			text: LN('sbi.qbe.datastore.refreshgrid'),
    		    handler: function(){
    	    	
    	    	for(var y=1; y<this.columnsWidth.length; y++){
    	    		this.columnsPosition[y] = y;
    	    		this.columnsWidth[y] = 100;
    	    	}
    	    	
    			var requestParameters = Ext.apply({start: 0, limit: 25 } || {});
    			this.store.load({params: requestParameters});

            	}
            	, scope: this
    	    }]

        });
		this.pagingTBar.on('render', function() {
			this.pagingTBar.addItem(this.warningMessageItem);
			this.warningMessageItem.setVisible(false);
		}, this);
		
		// create the Grid
	    this.grid = new Ext.grid.GridPanel({
	    	store: this.store,
	        cm: cm,
	        clicksToEdit:1,
	        style:'padding:10px',
	        frame: true,
	        border:true,  	        
	        collapsible:false,
	        loadMask: true,
	        viewConfig: {
	            forceFit:false,
	            autoFill: false,
	            enableRowBody:true,
	            showPreview:true
	        },
	        
	        //tbar:this.exportTBar,
	        bbar: this.pagingTBar
	    });
	    
	    this.grid.on('columnresize', function(columnIndex, newSize ){
//	    	for(var y=0; y<this.columnsWidth.length; y++){
//	    		this.columnsWidth[y] = this.grid.getColumnModel().getColumnWidth(y);
//	    	}
	    	this.columnsWidth[columnIndex] = newSize;
	    } , this);
	    
	    
	    
	    this.grid.on('columnmove', function(oldIndex, newIndex ){
		
	    	//update the array of the widths
	    	for(var y=0; y<this.columnsWidth.length; y++){
	    		this.columnsWidth[y] = this.grid.getColumnModel().getColumnWidth(y);
	    	}

	    	
	    	//update the array of the columns positions
	    	var tempHeaders = new Array();
	    	tempHeaders.length = this.columnsPosition.length;
	    	
	    	for(var t=0; t<this.columnsPosition.length; t++){
	    		tempHeaders[t]=this.grid.getColumnModel().getColumnHeader(t);
	    	}
	    	
	    	for(var i=0; i<this.oldColumns.length; i++){
	    		for(var t=0; t<tempHeaders.length; t++){
	    			if(this.oldColumns[i]==tempHeaders[t]){
	    				this.columnsPosition[i]=t;
	    				tempHeaders[t]="";
	    				break;
	    			}
	    		}
	    	}
	    	
			
	    } , this);
	    
	    // START CONTEXT MENU FOR EXTERNAL SERVICES INTEGRATION
	    if (this.baseConfig.externalServicesConfig && this.baseConfig.externalServicesConfig.length > 0) {
	    	
			// the row context menu
		    var externalServicesMenuItems = [];
		    for (var counter = 0; counter < this.baseConfig.externalServicesConfig.length; counter++) {
		    	externalServicesMenuItems.push({
		    		id: this.baseConfig.externalServicesConfig[counter].id,
					text: this.baseConfig.externalServicesConfig[counter].description,
					scope: this,
					handler: function(item) {
						var selectedRecords = new Array();
						for (var i = 0; i < menu.selectedRecords.length; i++) {
							var record = menu.selectedRecords[i];
							var myRecord = this.adjustRecordHeaders(record.data);
							selectedRecords.push(myRecord);
						}
						var params = {
								"id": item.id
								, "records": Sbi.commons.JSON.encode(selectedRecords)
						};
						this.callExternalService(params);
					}
		    	});
		    }
		    
		   	var menu = 
				new Ext.menu.Menu({
					items: externalServicesMenuItems
			});
		    
		    this.grid.on(
				'rowcontextmenu', 
				function(grid, rowIndex, e) {
					var sm = grid.getSelectionModel();
					if (!sm.isSelected(rowIndex)) {
						sm.clearSelections();
						sm.selectRow(rowIndex, true);
					}
					var records = sm.getSelections();
					e.stopEvent();
					menu.selectedRecords = records;
					menu.showAt(e.getXY());
				}
		    );
	    
	    } 
	    // END CONTEXT MENU FOR EXTERNAL SERVICES INTEGRATION
	    
	}
	
	
	/**
	 * Utility method that returns the JSON object associated to a grid record data:
	 * the JSON object has this structure:
	 * {
	 * 		"header_column_1": "row_cell_1"
	 * 		, "header_column_2": "row_cell_2"
	 * 		, ....
	 * }
	 */
	, adjustRecordHeaders: function(data) {
		var toReturn = {};
		for (header in this.alias2FieldMetaMap) {
			if (header !== undefined) {
				var field = this.alias2FieldMetaMap[header];
				toReturn[header] = data[field.name];
			}
		}
		return toReturn;
	}
	
	/**
	 * This method calls the action for external services integration and shows service response.
	 */
	, callExternalService: function(params) {
		Ext.MessageBox.wait('Please wait...', 'Processing');
		Ext.Ajax.request({
		    url: this.services['exportToExternalService'],
		    success: function(response, options) {
				var content = Ext.util.JSON.decode( response.responseText );
				if (content.missingcolumns) {
		    		Ext.Msg.show({
	 				   title: LN('sbi.qbe.datastorepanel.externalservices.errors.title'),
	 				   msg: LN('sbi.qbe.datastorepanel.externalservices.errors.missingcolumns') + ' ' + content.missingcolumns,
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
			 		});
				} else {
		    		Ext.Msg.show({
	 				   title: LN('sbi.qbe.datastorepanel.externalservices.title'),
	 				   msg: LN('sbi.qbe.datastorepanel.externalservices.serviceresponse') + ' ' + content.serviceresponse,
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.INFO
		 			});
				}
			},
		    failure: Sbi.exception.ExceptionHandler.handleFailure,
		    scope: this,
		    params: params
		});
	}
	
	, onDataStoreLoaded: function(store) {
		 var recordsNumber = store.getTotalCount();
       	 if(recordsNumber == 0) {
       		Ext.Msg.show({
				   title: LN('sbi.qbe.messagewin.info.title'),
				   msg: LN('sbi.qbe.datastorepanel.grid.emptywarningmsg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
			});
       	 }
       	 
       	 if (Sbi.config.queryLimit.maxRecords !== undefined && recordsNumber > Sbi.config.queryLimit.maxRecords) {
       		if (Sbi.config.queryLimit.isBlocking) {
       			Sbi.exception.ExceptionHandler.showErrorMessage(this.warningMessageItem, LN('sbi.qbe.messagewin.error.title'));
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