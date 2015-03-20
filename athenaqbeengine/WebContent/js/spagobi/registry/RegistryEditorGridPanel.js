/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * Object name 
  * 
  * [description]3
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
  * [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.registry");

Sbi.registry.RegistryEditorGridPanel = function(config) {
	
	var defaultSettings = {
	};
			
	if(Sbi.settings && Sbi.settings.registry && Sbi.settings.registry.registryEditorGridPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.registry.registryEditorGridPanel);
	}
			
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.services = this.services || new Array();	
	this.services['load'] = this.services['load'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'LOAD_REGISTRY_ACTION'
		, baseParams: new Object()
	});
	this.services['getFieldDistinctValues'] = this.services['getFieldDistinctValues'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FILTER_VALUES_ACTION'
		, baseParams: new Object()
	});
	this.services['update'] = this.services['update'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'UPDATE_RECORDS_ACTION'
		, baseParams: new Object()
	});
	this.services['delete'] = this.services['delete'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_RECORDS_ACTION'
		, baseParams: new Object()
	});
	
	
	this.init();
	

	if(this.registryConfiguration != undefined && this.registryConfiguration.pagination != 'false')
	{
		this.pagingTBar = new Ext.PagingToolbar({
	        pageSize: this.pageSize,
	        store: this.store,
	        displayInfo: true,
	//        displayMsg: LN('sbi.qbe.datastorepanel.grid.displaymsg'),
	//        emptyMsg: LN('sbi.qbe.datastorepanel.grid.emptymsg'),
	//        beforePageText: LN('sbi.qbe.datastorepanel.grid.beforepagetext'),
	//        afterPageText: LN('sbi.qbe.datastorepanel.grid.afterpagetext'),
	//        firstText: LN('sbi.qbe.datastorepanel.grid.firsttext'),
	//        prevText: LN('sbi.qbe.datastorepanel.grid.prevtext'),
	//        nextText: LN('sbi.qbe.datastorepanel.grid.nexttext'),
	//        lastText: LN('sbi.qbe.datastorepanel.grid.lasttext'),
	//        refreshText: LN('sbi.qbe.datastorepanel.grid.refreshtext'),
	        prependButtons: true
	    });
		this.pagingTBar.on('render', function() {
		}, this);
		
		this.pagingTBar.on('beforechange', function(cont, params) {
			var filtersValuesObject = this.getFiltersValues();
			
			this.start = params.start;
			this.limit = params.limit;
		
			Ext.apply(params, filtersValuesObject);
		}, this);
	}
	else 
	{
		this.pageSize = null;
	}
	
	
	var initialColumnModel = new Ext.grid.ColumnModel({
		columns:[
		new Ext.grid.RowNumberer(), 
		{
			header: "Data",
			dataIndex: 'data',
			width: 75
		}
		]

	});
	
	c = Ext.apply(c, {
		//height : 500
		autoScroll : true
		, id: 'RegistryEditorGridPanel'
    	, store : this.store
    	, tbar : this.gridToolbar
    	, sortable: false
        , cm : initialColumnModel
        , clicksToEdit : 1
        , style : 'padding:0px;'
        , frame : true
        , border : true
        , collapsible : false
        , columnLines : true
        , loadMask : true
        , enableHdMenu : false ///set true to eneable drob down menu on the header (which causes the loss of mouse control for resize column....)
        , viewConfig : {
            forceFit : false
            , autoFill : true
            , enableRowBody : true
//            ,getRowClass: function(record, rowIndex, rp, ds){ // rp = rowParams            	
//            	return 'x-grid3-row-mine';
//            	//return 'background-color: #FF0000;border: 0px none; margin: 0px;';
//            }
        }
		, bbar: this.pagingTBar
		, meta: null
		,listeners: {headerdblclick : function( grid, columnIndex, e ){
			this.showExpandPointer(grid, columnIndex);
		}
		}
		, cls: 'grid-row-span'
	});
	
	
	// constructor
	Sbi.formviewer.DataStorePanel.superclass.constructor.call(this, c);

	




};

Ext.extend(Sbi.registry.RegistryEditorGridPanel, Ext.grid.EditorGridPanel, {
    
    services: null
	, store: null
	, registryConfiguration : null
	, driversValues : null
	, gridToolbar : null
	, filters : null
	, columnName2columnHeader : null
	, columnHeader2columnName : null
	, columnHeader2color : null	
	, keyUpTimeoutId : null
	, mandatory: null
	, visibleColumns: []
	, columnsMaxSize: 15
	, pagingTBar: null
	, pageSize: 15
	, oldColumns: null
	, deletedRows: null
	, limit: null
	, start: null
	, indexColumnToMerge: null
	, previousValueEdit : null
	, saveMask: null
	, numberColumnIndex:null
    , lstMasterDependences: []

	// ---------------------------------------------------------------------------------------------------
    // public methods
	// ---------------------------------------------------------------------------------------------------
	,renderTooltip:function(val, cell, record) {
		 
		// get data
		var data = record.data;
		 
		// return markup
		return '<div ext:qtip="' + val +'" ext:qtitle="Valore:" ext:qwidth="300" ext:qdismissDelay="0" ext:closable="true">' + val + '</div>';
		
	},
	showExpandPointer: function(grid, columnIndex){
		grid.getColumnModel().setColumnWidth( columnIndex, this.columnsMaxSize, false ) 
	}
	,
	load:  function(requestParameters) {
		this.firstPage= true;
		requestParameters.start = 0;
		if(this.pageSize != undefined){
			requestParameters.limit = this.pageSize;
		}
		this.store.load({params: requestParameters});
	}
	,
	loadFrom:  function(requestParameters) {
		if(requestParameters.start && requestParameters.start!= null && requestParameters.start==0){
			this.firstPage= true;			
		}
		if(!requestParameters.start || requestParameters.start == null) requestParameters.start = 0;
		if(!requestParameters.limit || requestParameters.limit == null) requestParameters.limit = this.pageSize;

		this.store.load({params: requestParameters});
	}
  	
	// ---------------------------------------------------------------------------------------------------
	// private methods
	// ---------------------------------------------------------------------------------------------------
	
	,
	init : function () {
		this.initStore();
		
//		this.on('sortchange', function(context, sortInfo){
//			var c = 0;
//		});
		
		// THISS IS SORTING SYNTAX
		//		sortInfo:{
		 //   field:'fieldName',
		 //   direction:'ASC'// or 'DESC' (case sensitive for local sorting)
		//}
		
		
		
		
		this.initToolbar();
		Ext.QuickTips.init() ;
		Ext.apply(Ext.QuickTips.getQuickTip(), {
		    maxWidth: 200,
		    minWidth: 100,
		    showDelay: 50,
		    dismissDelay: 0,
		    closable: true,
		    title: 'Valore',
		    trackMouse: true
		});

	}

	,
	initStore: function() {
		
		var proxy = new Ext.data.HttpProxy({
	           url: this.services['load']
	           , timeout : 300000
	   			,success : 
				function(response, opts) {
	   			}
	   		   , failure: this.onDataStoreLoadException
	    });
		
		this.store = new Ext.data.Store({
	        proxy: proxy,
	        reader: new Ext.data.JsonReader(
	        ),
	        remoteSort: false
	    });
			
		this.store.on('load', function(store, records, options ){
			var numRec = this.store.getCount();
			
			//redefines the columns labels if they are dynamics
			var tmpMeta = this.getColumnModel();
			var fields = tmpMeta.config;
			var metaIsChanged = false;
			var fieldsMap = {};
			tmpMeta.fields = new Array(fields.length);

			for(var i = 0, len = fields.length; i < len; i++) {				
				
				for(k=0; k<numRec; k++){
					var tmpRec = this.store.getAt(k);
					if (tmpRec !== undefined) {
						
						var valorig =  tmpRec.json[fields[i].header];

						
						if(fields[i].type === 'int'){	
						
					    	if (valorig !== undefined){	
					    		if(valorig === ''){
					    			tmpRec.data[fields[i].header] = valorig;
					    			tmpRec.commit();
					    		}
					    		if(valorig === '0'){
					    			tmpRec.data[fields[i].header] = '0';
					    			tmpRec.commit();
					    		}
					    		
					    	}
						}
					}
			    }
			}
		
				this.updateRowSpan();
				this.colorTotalRows();
				
		
				
		}, this);
		
//		this.store.on('add', function(store, records, options ){
//			var numRec = this.store.getCount();
//			
//			//redefines the columns labels if they are dynamics
//			var tmpMeta = this.getColumnModel();
//			var fields = tmpMeta.config;
//			var metaIsChanged = false;
//			var fieldsMap = {};
//			tmpMeta.fields = new Array(fields.length);
//
//			for(var i = 0, len = fields.length; i < len; i++) {				
//				
//				for(k=0; k<numRec; k++){
//					var tmpRec = this.store.getAt(k);
//					if (tmpRec !== undefined) {
//						
//						var valorig;
//						if(tmpRec.json != undefined){
//							valorig	=  tmpRec.json[fields[i].header];
//						}
//						
//						if(fields[i].type === 'int'){	
//						
//					    	if (valorig !== undefined){	
//					    		if(valorig === ''){
//					    			tmpRec.data[fields[i].header] = valorig;
//					    			tmpRec.commit();
//					    		}
//					    		if(valorig === '0'){
//					    			tmpRec.data[fields[i].header] = '0';
//					    			tmpRec.commit();
//					    		}
//					    		
//					    	}
//						}
//					}
//			    }
//			}
//		}, this);
		
		
		
		
		
		
	

		
		this.store.on('metachange', function( store, meta ) {
			//alert('metachange');
			this.visibleColumns = [];
			
			this.meta = meta;
			
			
			// ORDER
			//The following code is used for keep the order of the columns after the execution of the query 
			meta.fields[0] = new Ext.grid.RowNumberer();
			this.alias2FieldMetaMap = {};
			var fields = meta.fields;
			var newColumns = new Array();
			newColumns.length = fields.length;
			
			
			//1) first of all we check if the list of fields is changed 
			if(this.firstPage){
				for(var i = 0; i <meta.fields.length ;i++) {
					if(meta.fields[i].header){
					newColumns[i]= meta.fields[i].header;
					}
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
			
			
	
			   
			// insert actual fields into oldCOlumns for next iteration
			   if(this.oldColumns == null){
					this.oldColumns = new Array();
					for(var i = 0; i <meta.fields.length ;i++) {
						this.oldColumns[i]= meta.fields[i].header;
					}
				}
			   
			   
			   // reassign meta.fields as stored in column Position
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
						  this.columnsWidth[i]= 200;
					  }
					  
				  	}
			
			// END ORDER
			
			
			   // Array of column Index to merge
			 this.indexColumnToMerge = new Array();
			 
			var arrayIndex = 0;
			 
			this.columnName2columnHeader = {};
			this.columnHeader2columnName = {};
			this.columnHeader2color = {};
			this.numberColumnIndex = [];
		
			// Set max size
			if(meta.maxSize != null && meta.maxSize !== undefined){
				this.columnsMaxSize = parseInt(meta.maxSize);
			}
			
			
			// ITERATE ON EACH FIELD                 0 is numberer
			for(var i = 0; i < meta.fields.length; i++) {
				
				// For i-field
				
				// map name to header and header to name
				this.columnName2columnHeader[meta.fields[i].name] = meta.fields[i].header;
				this.columnHeader2columnName[meta.fields[i].header] = meta.fields[i].name;
				
				// recalculate coumns size
				var col = meta.fields[i].name;
				for(var j = 0; j < meta.columnsInfos.length; j++) {
					if(meta.columnsInfos[j].sizeColumn !== undefined && meta.columnsInfos[j].sizeColumn == meta.fields[i].name){
						meta.fields[i].width = meta.columnsInfos[j].size;
						this.columnsWidth[i]= meta.columnsInfos[j].size;   // are these the same column?
					}
					if(meta.columnsInfos[j].unsigned !== undefined && meta.columnsInfos[j].sizeColumn == meta.fields[i].name){
						meta.fields[i].unsigned = meta.columnsInfos[j].unsigned;
					}
				}
				
				// set renderer based on field type

				if(meta.fields[i].type) {
				   var columnFromTemplate = this.registryConfiguration.columns[i-1]; // because 0 is row numberer
				   var formatToParse;
				   var t = meta.fields[i].type;
				   var s = meta.fields[i].subtype;
				   if (t ==='float') { // format is applied only to numbers
					   
					   //check if format is defined in template force it, else get it from model field
//					   var columnFromTemplate = this.registryConfiguration.columns[i-1]; // because 0 is row numberer					   
//					   var formatToParse;
					   if(columnFromTemplate.format){
						   formatToParse = columnFromTemplate.format;
					   }
					   else{
						   formatToParse = meta.fields[i].format;
					   }
					   		   
					   var format = Sbi.qbe.commons.Format.getFormatFromJavaPattern(formatToParse);
					   var f = Ext.apply( Sbi.locale.formats[t], format);
					   meta.fields[i].renderer = Sbi.qbe.commons.Format.floatRenderer(f);
					   this.numberColumnIndex.push(i);
					   
				   }else if(t ==='int'){
					   meta.fields[i].renderer = Sbi.locale.formatters['string']; 
					   this.numberColumnIndex.push(i);
				   }else if(t ==='date'){
					   if(columnFromTemplate.format){
						   formatToParse = columnFromTemplate.format;
					   }
					   else{
						   formatToParse = meta.fields[i].dateFormat;
					   }		
					   meta.fields[i].renderer = Ext.util.Format.dateRenderer(formatToParse);
				   }else{
					   //meta.fields[i].renderer = Sbi.locale.formatters[t];
					   meta.fields[i].renderer = this.renderTooltip.createDelegate(this);
				   }   
			   }
			   
			   // set if cells have to be merged
			
			//	var columnDef = this.registryConfiguration.columns[i-1];  // 0 is row numberer
				//	if(columnDef != undefined && columnDef.type == "merge"){
				//  this.indexColumnToMerge[arrayIndex] = i;
				//					arrayIndex++;
				//				}
				//				else{
				//				} 

				
				var columnDef = this.registryConfiguration.columns[i];
				if(columnDef != undefined && columnDef.type == "merge"){
					// index is plus one because there is numberer row at position zero of columns
									this.indexColumnToMerge[arrayIndex] = i+1;
									arrayIndex++;
				}
				else{
				} 
				
			   if(meta.fields[i].subtype && meta.fields[i].subtype === 'html') {
				   meta.fields[i].renderer  =  Sbi.locale.formatters['html'];
			   }
//			   if(meta.fields[i].subtype && meta.fields[i].subtype === 'timestamp') {
//				   meta.fields[i].renderer  =  Sbi.locale.formatters['timestamp'];
//			   }
			   

			   // set sortable flag			   
			   if(this.sortable === false) {
				   meta.fields[i].sortable = false;
			   } else {
				   if(meta.fields[i].sortable === undefined) { // keep server value if defined
					   meta.fields[i].sortable = true;
				   }
			   }
			   // set right editor
			   var editor = this.getEditor(meta.fields[i].name, meta.fields[i].type);   // not the header but the name
			   if (editor != null) {
				   meta.fields[i].editor = editor;
			   }
			   
				//define color
				color = undefined;
				if(i>0){		// i = 0 in fields is row numberer 
					var columnConf = this.registryConfiguration.columns[i-1];
					if(columnConf && columnConf.color){
						color = columnConf.color;
						this.columnHeader2color[meta.fields[i].name] = columnConf.color != undefined ? columnConf.color : '#FFFFFF';
					}
				}
				var MyColorRenderer = function(value, metaData, record)
				{
				   if (value === undefined || record === undefined || record.data === undefined){return null;}
				   metaData.style += 'background-color:' + this.color; //this is for background
				   retValue = value;
				   	//retValue = "<font style=' color:red;'>" + value + "</font>"; //this is for inc
				   	return retValue ;
				};
			   
			   // visible columns will contain all visible fields
			   var config = this.getColumnEditorConfig(meta.fields[i].header);
			   if(config.visible == false){
				  /* if(color){
					   meta.fields[i].color = color;
					   meta.fields[i].renderer = MyColorRenderer;
				   }*/
				  this.visibleColumns.push(meta.fields[i]);
				   meta.fields[i].hidden = true;
					continue;
					
			   }else{		    // IF VISIBLE
				   if(color){
					   meta.fields[i].color = color;
					   meta.fields[i].renderer = MyColorRenderer;
				   }
				   
				   this.visibleColumns.push(meta.fields[i]);
				   meta.fields[i].hidden = false;

			   }
			   
			   meta.fields[i].allowBlank = true;

		   } 
			// end cycling on meta.fields
			

			
			   // which columns are visible
//					 this.getColumnModel().setConfig(this.visibleColumns);
		   var columnmodel = this.getColumnModel();
		   columnmodel.setConfig(meta.fields);
		   
		   
		   //ORDER
					   	
		   for(var y=1; y<this.columnsWidth.length; y++){
					this.getColumnModel().setColumnWidth(y,this.columnsWidth[y]);
				}

		   this.firstPage = false;	  
		   //END ORDER
		   
		   this.mandatory = meta.mandatory;

			// set align column on the right (numberscolumn)
			for(var i = 0, len = this.numberColumnIndex.length; i < len; i++) {
				var index = this.numberColumnIndex[i];
				var column = this.getColumnModel().config[index];
				column.align = 'right';				
			}
		   
		   
		 this.on('beforeedit', function(e){			   
			   
			   /*
			    grid - This grid
			    record - The record being edited
			    field - The field name being edited
			    value - The value for the field being edited.
			    row - The grid row index
			    column - The grid column index
			    cancel - Set this to true to cancel the edit or return false from your handler.
				*/
			    var val = e.value;
			    
			    this.previousValueEdit = val;
			    
			    // check if is a total value than cannot be edited
			    for(var j = 0; j < this.meta.summaryCellsCoordinates.length; j++) {
			    	if(this.meta.summaryCellsCoordinates[j].row === e.row){
			    		return 	false
			    	}
			    }
			    
			    var valorig; 
			    if(e.record.json != undefined){
			    	valorig = e.record.json[e.field];
			    }
			    else{
			    	valorig = e.record.data[e.field];
			    }
			    
			    
			    var t = this.visibleColumns[e.column].type;
			    var st = this.visibleColumns[e.column].subtype;
			    if(Ext.isDate(val) ){	    	
//			    	if(st != null && st !== undefined && st === 'timestamp'){
//			    		e.record.data[e.field] = Sbi.qbe.commons.Format.date(val, Sbi.locale.formats['timestamp']);
//			    	}else{
//			    		e.record.data[e.field] = Sbi.qbe.commons.Format.date(val, Sbi.locale.formats['date']);
//			    	}
			       var formatDate = this.getFormatDate(e.column-1, st);				   
				   val = Sbi.qbe.commons.Format.date(val, formatDate);
				   e.record.data[e.field] = val;
			    }
			    else if(Ext.isNumber(val)){
			    	if(t === 'float'){
			    		e.record.data[e.field] = Sbi.qbe.commons.Format.number(val, Sbi.locale.formats['float']);
			    	}
			    	if(t === 'int' && valorig == ''){
			    		e.record.data[e.field] = valorig;
			    	}
			    }
			    return true;
		   }, this);
		   
		   this.on('afteredit', function(e) {
			      /*grid - This grid
				    record - The record being edited
				    field - The field name being edited
				    value - The value being set
				    originalValue - The original value for the field, before the edit.
				    row - The grid row index
				    column - The grid column index*/							   
			   
			   var t = this.visibleColumns[e.column].type;
			   var st = this.visibleColumns[e.column].subtype;
			   if (t === 'date' ) {
				   var val = e.value;
				   if (val == "" && e.originalValue !== ""){
					   //reset  value
					   val = null;
					   e.record.data[e.field] = val;
				   }else{
					   var formatDate = this.getFormatDate(e.column-1, st);
					   if(Ext.isDate(val) ){	   
						   val = val.dateFormat(formatDate); // format the date with correct format			  
					   }			
					   if (val == e.originalValue) return; //do nothing if the value doesn't changes
					   
					   var dt = new Date(Date.parseDate(val, formatDate));
					   e.record.data[e.field] = dt;		 
				   }
				   var view = this.getView();
				   var cell = view.getCell(e.row, e.column);
				   
		            var browser = this.detectBrowser();
		            if(browser != null && browser == 'IE'){
		            	 cell.innerText = val; 
		            }	
		            else{
		            	 cell.textContent = val; 
		            }
				   
			  	  
			   }
			   if (t === 'float') {
				   var dottedVal = '.00';
				   var pointval = null;
				   //replace , wirth . if there's a value
				   if(e.value)
					   pointval = e.value.replace(',', '.');
				   // check it is a number otherwise goes to zero
				   if (!isNaN(pointval))					   
					   dottedVal = pointval;
				   
				   var f = parseFloat(dottedVal);
				   e.record.data[e.field] = f;
			   }
			   
			   // update total rows
			   if(this.previousValueEdit != e.value){
				   this.updateTotalRow(e.row, e.column, e.value, this.previousValueEdit);
				   this.colorTotalRows();
				   this.previousValueEdit = e.value;
			   }
			   
			   
			   // refresh row span
			   this.updateRowSpan();
			   
			   
			 }, this);
		   
		   this.on('validateedit', function(e) {
	    	   var t = this.visibleColumns[e.column].type;
			   var st = this.visibleColumns[e.column].subtype;
			   
			   var unsigned = this.visibleColumns[e.column].unsigned;

			   if(t === 'float'){
				   var dottedVal = e.value.replace(',', '.');
				   var isfloat = isFloat(dottedVal);

				   if(!isfloat){
					   // if is not in float format but in integer then add .00
					   var isinteger = isInteger(dottedVal);

					   if(!isinteger){
						   if(e.value == ''){
							   //removed
							   //ORIG e.value = NaN;
							   e.value = '0';
							   return;
						   }
						   e.cancel = true;
						   Ext.MessageBox.show({
							   title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
							   msg : LN('sbi.registry.registryeditorgridpanel.validation'),
							   buttons : Ext.MessageBox.OK,
							   width : 300,
							   icon : Ext.MessageBox.INFO
						   }); 
						}
						else{
						   dottedVal+='.00';
						}
				   }
			   }

			   if(t === 'int'){
				   var isInt = isInteger(e.value);
				   
				   if(unsigned){
					   //only positive numbers
					   isInt = isUnsignedInteger(e.value);
					   
					   if(!isInt){
						   if(e.value == ''){
							   //removed
							 //ORIG e.value = NaN;
							   e.value = '0';
							   return;
						   }
						   e.cancel = true;
						   Ext.MessageBox.show({
								title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
								msg : LN('sbi.registry.registryeditorgridpanel.validation.unsigned'),
								buttons : Ext.MessageBox.OK,
								width : 300,
								icon : Ext.MessageBox.INFO
							});
						   return;
					   }
					   
				   }

				   if(!isInt){
					   if(e.value == ''){
						   //removed
						 //ORIG e.value = NaN;
						   e.value = '0';
						   return;
					   }
					   e.cancel = true;
					   Ext.MessageBox.show({
							title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
							msg : LN('sbi.registry.registryeditorgridpanel.validation'),
							buttons : Ext.MessageBox.OK,
							width : 300,
							icon : Ext.MessageBox.INFO
						}); 
				   }
			   }

		 }, this);

		   
		   
		   
		}, this);
		

	}

	,
	onDataStoreLoadException: function(response, options) {
		Sbi.exception.ExceptionHandler.handleFailure(response, options);
	}
	
	,
	getEditor : function (field, type) {
		var toReturn = null;
		var editorConfig = this.getColumnEditorConfig(field);

		if (editorConfig.editable == true) {
			if (editorConfig.editor == "COMBO") {
				
				if(type === 'boolean'){
					toReturn = this.createFieldBoolean(field);
				}else{
					toReturn = this.createFieldCombo(field);
				}
			} else if (editorConfig.editor == "PICKER") { 
				toReturn = this.createFieldDate(editorConfig);
			} else {
				toReturn = new Ext.form.TextField();
				toReturn.selectOnFocus = true;
			}
		}
		return toReturn;
	}

	,
	getColumnEditorConfig : function (field) {

		var columnsConf = this.getColumnsConfiguration();
		var toReturn = {  // default values
				editable : true
				, visible : true
		};
		for (var i = 0; i < columnsConf.length; i++) {
			if (columnsConf[i].field == field) {
				toReturn = Ext.apply(toReturn, columnsConf[i]);
				break;
			}
		}
		return toReturn;
	}
	
	,
	initToolbar : function () {
		var items = this.initFiltersToolbarItems();
		this.gridToolbar = new Ext.Toolbar(items);
	}
	
	,
	initFiltersToolbarItems : function () {
		var items = [];
		
		var enableButtons = this.getConfiguration('enableButtons');
		if(enableButtons && enableButtons == "true"){		
		items.push({
			iconCls: 'icon-add',
			handler : this.addNewRecord,
			scope : this
		});
		
		items.push({
			iconCls: 'icon-delete',
			handler : function(){
				Ext.MessageBox.confirm(
						LN('sbi.worksheet.designer.msg.deletetab.title'),
						LN('sbi.worksheet.designer.msg.deletetab.msg'),            
			            function(btn, text) {
			                if (btn=='yes') {
			    				this.deleteRecord();
			                }
			            },
			            this
					);
			},
			scope : this
		});
		
		}
		
		items.push({
			iconCls : 'icon-save',
			handler : this.save,
			scope : this
		});
//		items.push({
//			iconCls : 'icon-refresh',
//			handler : this.refresh,
//			scope : this
//		});
		items.push({xtype: 'tbspacer', width: 30});
		items.push({
			iconCls : 'icon-clear',
			handler : this.clearFilterForm,
			scope : this
		});
		this.filters = [];
		var filtersConf = this.getFiltersConfiguration();
		if (filtersConf.length > 0) {
			for (var i = 0 ; i < filtersConf.length ; i++) {
				var aFilter = filtersConf[i];
				if (aFilter.presentation != undefined && aFilter.presentation == "DRIVER") {}
				else{
					items.push({xtype: 'tbtext', text: aFilter.title, style: {'padding-left': 20}});
					var filterField = this.createFilterField(aFilter);
					items.push(filterField);
					this.filters.push(filterField); // save filters into local variable this.filters
				}
			}
	}
		
		items.push({xtype: 'tbspacer', width: 30});
		items.push({
			iconCls : 'icon-execute',
			handler : this.applyFilter,
			scope : this
		});

		//	items.push({xtype: 'tbspacer', width: 150});



		return items;
	}
	
	,
	getFiltersConfiguration : function () {
		var toReturn = [];
		if (this.registryConfiguration != undefined && this.registryConfiguration != null) {
			toReturn = this.registryConfiguration.filters;
		}
		return toReturn;
	}
	
	,
	getColumnsConfiguration : function () {
		var toReturn = [];
		if (this.registryConfiguration != undefined && this.registryConfiguration != null) {
			toReturn = this.registryConfiguration.columns;
		}
		return toReturn;
	}

	,
	getColumnConfiguration : function (field) {
		var columns = this.getColumnsConfiguration();
		for (var i = 0; i < columns.length; i++) {
			if (columns[i].field == field) {
				return columns[i];
			}
		}
		return null;
	}
	
	,
	getConfiguration : function (name) {
		if (this.registryConfiguration != undefined && this.registryConfiguration != null) {
			var confs = this.registryConfiguration.configurations;
			for (var i = 0; i < confs.length; i++) {
				if (confs[i].name == name) {
					return confs[i].value;
				}
			}
		
		}
		return;
	}

	,
	createFilterField : function (aFilter) {
		var filterField = null;
		if (aFilter.presentation != undefined && aFilter.presentation == "COMBO") {
			filterField = this.createFieldCombo(aFilter.field);
			filterField.type = "COMBO";
			filterField.section = "FILTER";
			//filterField.on('change', this.filterMainStore, this);
		} 
		else		
		{
			filterField = new Ext.form.TextField({
				name: aFilter.field
//				, enableKeyEvents : true
//				, listeners : {
//					keyup : this.setKeyUpTimeout
//					, scope: this
//				}
			});
			filterField.type = "TEXT";
		}
		return filterField;
	}
	,
	createFieldBoolean: function(field) {

		var combo = new Ext.form.ComboBox({
			name: field
            , editable : false
            , store: new Ext.data.SimpleStore({
            	fields: ['column_1'],
                data: [['true'], ['false']]
            })
	        , displayField: 'column_1'
	        , valueField: 'column_1'
	        , mode:'local'
	        , triggerAction: 'all'
        });
		
		return combo;
	}
	,
	createFieldCombo: function(field) {
		var store = new Ext.data.JsonStore({
			url: this.services['getFieldDistinctValues']
		});
		var temp = this.registryConfiguration.entity;
		var index = this.registryConfiguration.entity.indexOf('::');
		if (index != -1) {
			temp = this.registryConfiguration.entity.substring(0 , index);
		}
		var entityId = null;
		var column = this.getColumnConfiguration(field);
		if (column.subEntity) {
			entityId = temp + "::" + column.subEntity + "(" + column.foreignKey + ")" + ":" + field;
		} else {
			entityId = temp + ':' + field;
		}
		
		var orderBy = entityId;
		if(column.orderBy != null && column.orderBy != undefined){
			if (column.subEntity) {
				orderBy = temp + "::" + column.subEntity + "(" + column.foreignKey + ")" + ":" + column.orderBy;				
					}
			else{
				entityId = temp + ':' + column.orderBy;
			}
		}
		//if there are some dependences get columns informations about linked entity
		lstDependsFromRef = [];		
		if (column.dependsFrom){			
			var lstDependsFrom = column.dependsFrom.split(",");
			var lstDependsFromEntity = (column.dependsFromEntity)?column.dependsFromEntity.split(","):[];
			for (var i=0; i<lstDependsFrom.length; i++){					
				var name = (lstDependsFromEntity && lstDependsFromEntity[i] != null && lstDependsFromEntity[i] !== '')?lstDependsFromEntity[i]:temp;										
				var columnDepends = this.getColumnConfiguration(lstDependsFrom[i].trim());				 
				if (columnDepends && columnDepends.subEntity) {
					name +=  "::" + columnDepends.subEntity + "(" + columnDepends.foreignKey + ")" + ":" + lstDependsFrom[i].trim();				
				}
				else{
					 name += ':' + lstDependsFrom[i].trim();
				}
				var tmpFieldRef = {};
				tmpFieldRef.field = lstDependsFrom[i].trim();
				tmpFieldRef.entity = name;
				tmpFieldRef.title = column.title;
				lstDependsFromRef.push(tmpFieldRef);
				this.lstMasterDependences[column.field] = tmpFieldRef; //list of all dependences by master side				
			}
		}
		

		var baseParams = {
			'QUERY_TYPE': 'standard', 
			'ENTITY_ID': entityId, 
			'ORDER_ENTITY': orderBy, 
			'ORDER_TYPE': 'asc', 
			'QUERY_ROOT_ENTITY': true
		};
		store.baseParams = baseParams;
		store.on('loadexception', function(store, options, response, e) {
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
		});
		
		
		var combo = new Ext.form.ComboBox({
			name: field
            , editable : false
            , store: store
	        , displayField: 'column_1'
	        , valueField: 'column_1'
	        , triggerAction: 'all'
	        , lstDependsFromRef: lstDependsFromRef
        });
		
		var thisPanel = this;
		combo.on('change', function(el, newValue, oldValue){
			if (!el.section) {
				var isMasterField = false;
				var lstDependences = '';
				if (thisPanel.lstMasterDependences){
					for (el in thisPanel.lstMasterDependences){						
						var tmpRef = thisPanel.lstMasterDependences[el];
						if (tmpRef.field == column.field.trim()){
							isMasterField = true;
							lstDependences += tmpRef.title + '  ';
						}
					}
				}	
				if (isMasterField && oldValue != '' && oldValue != newValue){				
				   alert(LN('sbi.registry.registryeditorgridpanel.warningDependences.1') +'\'' + lstDependences + '\' ' +
						 LN('sbi.registry.registryeditorgridpanel.warningDependences.2') );
				}
			}
		});
		
		if (column.dependsFrom){
			combo.on('focus', function(el){				
				var lstDependsFrom = column.dependsFrom.split(",");
				dependences = '';
				for (var i=0; i<el.lstDependsFromRef.length; i++){		
					var name = '';
					var comma = (i < lstDependsFrom.length-1)?',':'';
					var filterValue;
					for (var j=0; j<el.lstDependsFromRef.length; j++){
						var tmpRef = el.lstDependsFromRef[j];
						if (tmpRef.field == lstDependsFrom[i].trim()){
							name = tmpRef.entity;
							for(var k=0; k < thisPanel.filters.length; k++){
								if (thisPanel.filters[k].name == tmpRef.field){
									filterValue = thisPanel.filters[k].value;			
									break;
								}
							}
							break;
						}
					}
					if (el.section  && el.section === "FILTER") {								
						dependences += (filterValue != undefined && filterValue !== "") ? name + '=' + filterValue + comma : "";
					}else{			
						dependences += (this.gridEditor.record.data[lstDependsFrom[i].trim()] != "")? name + '=' + this.gridEditor.record.data[lstDependsFrom[i].trim()] + comma : "";
					}						
				}
//				alert('dependences: '  + dependences);
				delete combo.store.baseParams.DEPENDENCES;
				combo.store.on('beforeload', function(s) {
					s.setBaseParam('DEPENDENCES', dependences);
				});
				combo.store.load();
			});
		}
		return combo;
	}	
	
	, createFieldDate: function( fieldConfig ) {

		var dtPicker = new Ext.form.DateField({name: fieldConfig.field
											 , format: fieldConfig.format });
		
		if(fieldConfig.value !== undefined && fieldConfig.value !== null) {	
			var dt = Sbi.commons.Format.date(fieldConfig.value, fieldConfig.format);
			dtPicker.setValue(fieldConfig.value);				
		}
		
		return dtPicker;
		
	}
	,
	setKeyUpTimeout : function () {
        clearTimeout(this.keyUpTimeoutId);
        this.keyUpTimeoutId = (function() {
	          this.keyUpTimeoutId = null;
	          this.filterMainStore();
	    }).defer(500, this);
	}
	
	,
	filterMainStore : function () {
		var filtersValuesObject = this.getFiltersValuesAndType();
		var filterFunction = this.createFilterFunction(filtersValuesObject);
		this.store.filterBy(filterFunction);
	}
	
	,
	getFiltersValues : function () {
		var filtersValuesObject = {};
		for (var i = 0 ; i < this.filters.length ; i++) {
			var aFilter = this.filters[i];
			//			filtersValuesObject[aFilter.getName()] = {
//				value : aFilter.getValue()
//			};
			filtersValuesObject[aFilter.getName()] = aFilter.getValue();
		}
		return filtersValuesObject;
	}
	,
	getFiltersValuesAndType : function () {
		var filtersValuesObject = {};
		for (var i = 0 ; i < this.filters.length ; i++) {
			var aFilter = this.filters[i];
			filtersValuesObject[aFilter.getName()] = {
				value : aFilter.getValue()
				, type : aFilter.type
			};
		}
		return filtersValuesObject;
	}	
	,
	createFilterFunction : function (filtersValuesObject) {
		var columnHeader2columnName = this.columnHeader2columnName;
		var filterFunction = function (record, recordId) {
			for (var aFilterName in filtersValuesObject) {
				var filterObject = filtersValuesObject[aFilterName];
				// filter name corresponds to the column header, so we retrieve the relevant column name
				var columnName = columnHeader2columnName[aFilterName];
				var fieldValue = record.get(columnName).toString();
				var filterType = filterObject.type;
				var filterValue = filterObject.value;
				var fieldCompareValue = null;
				if (filterType == 'COMBO') {
					fieldCompareValue = fieldValue;
					if (filterValue == '') {
						continue;
					}
				} else {
					filterValue = filterValue.toUpperCase();
					fieldCompareValue = fieldValue.substring(0, filterValue.length).toUpperCase();
				}
				if (filterValue != fieldCompareValue) {
					return false;
				}
			}
			return true;
		};
		return filterFunction;
	}
	
	,
	clearFilterForm: function () {
		for (var i = 0 ; i < this.filters.length ; i++) {
			var aFilter = this.filters[i];
			if (aFilter.type == "COMBO") {
				aFilter.clearValue();
			} else {
				aFilter.setValue('');
			}
		}
		this.store.clearFilter(false);
	}
	, hasMandatoryColumnRespected: function(aRecordData){
		var ok = '';
		for(i =0; i<this.mandatory.length; i++){
			var columnToCheck = this.mandatory[i].column;
			var col = aRecordData[columnToCheck];
			if(col === undefined || col == null || col === '' || isNaN(col)){
				var columnRef = this.mandatory[i].mandatoryColumn;
				var valueRef = this.mandatory[i].mandatoryValue;
				var value = aRecordData[columnRef];
				if(value !== undefined && value !== null && value === valueRef){
					return columnToCheck;					
				}
			}
		}
		return ok;
	}
	,
	saveSingleRecord : function (index, modifiedRecords) {
		var recordsData = [];

		if(index<modifiedRecords.length){

			var aRecordData = Ext.apply({}, modifiedRecords[index].data);
			delete aRecordData.recNo; // record number is not something to be persisted
			recordsData.push(aRecordData);
			var colMandatory= this.hasMandatoryColumnRespected(aRecordData);
			if(colMandatory !== ''){
				this.hideMask();
				Ext.MessageBox.show({
					title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
					msg : colMandatory +" "+LN('sbi.registry.registryeditorgridpanel.mandatory'),
					buttons : Ext.MessageBox.OK,
					width : 300,
					icon : Ext.MessageBox.INFO
				});

				return;
			}
			Ext.Ajax.request({
				url: this.services['update'],
				method: 'post',
				params: {"records" : Sbi.commons.JSON.encode(recordsData)},
				success : 
					function(response, opts) {
					try {
						var firstQuery = Ext.util.JSON.decode( response.responseText );
						var key = firstQuery.keyField;
						var id = firstQuery.ids[0];
						if(id){
							var record = modifiedRecords[index];
							record.set(key, id);
							this.doLayout();
						}
						//this.saveSingleRecord.createDelegate(this, [index + 1, modifiedRecords], false);
						this.saveSingleRecord(index + 1, modifiedRecords);
					} catch (err) {
						Sbi.exception.ExceptionHandler.handleFailure();
					}
				},
				failure: function(msg, title){
					for(var j=0; j<index;j++){
						modifiedRecords[0].commit();
					}
					this.hideMask();
					Ext.MessageBox.show({
						title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
						msg : LN('sbi.registry.registryeditorgridpanel.saveconfirm.message.ko'),
						buttons : Ext.MessageBox.OK,
						width : 300,
						icon : Ext.MessageBox.INFO
					});
				},
				scope: this
			});
		}else{
			this.updateSuccessHandler();
		}
	}
	
	,
	save: function () {
		this.showMask();
		var modifiedRecords = this.store.getModifiedRecords();
		this.saveSingleRecord(0,modifiedRecords);
		this.updateRowSpan();
	}
	,
	refresh: function () {
		this.view.refresh();
	}
	,
	applyFilter: function () {
		// read filters
		var filtersValuesObject = this.getFiltersValues();
		
		this.load(filtersValuesObject);
	}
	,
	addNewRecord: function () {

		var emptyRecord = new Object();
		
		for(var i = 1; i < this.visibleColumns.length; i++) {
			emptyRecord[this.visibleColumns[i].name] = ''; 
		}
		//meta.fields[0] = new Ext.grid.RowNumberer();
		var array = []
		for(var i = 1; i < this.visibleColumns.length; i++) {
			var obj = new Object()
			var col = this.visibleColumns[i];
			obj['name'] = col.name; 
			obj['type'] = col.type; 
			obj['allowBlank'] = this.visibleColumns[i].allowBlank; 
			obj['dataIndex'] = this.visibleColumns[i].dataIndex; 
			obj['header'] = this.visibleColumns[i].header; 
			obj['hidden'] = this.visibleColumns[i].hidden; 
			obj['id'] = this.visibleColumns[i].id; 
			obj['renderer'] = this.visibleColumns[i].renderer; 
			obj['sortable'] = this.visibleColumns[i].sortable; 
			obj['width'] = this.visibleColumns[i].width; 

			array.push(obj);		
		}
	
		var recordConstructor = Ext.data.Record.create(obj);
	
		var row = new recordConstructor(emptyRecord); 
		this.store.add([row]);
		//this.store.insert(0,[row]);
		

	}
	,
	deleteRecord: function () {

		var selectionModel = this.getSelectionModel();

		var cellsSelected = selectionModel.getSelectedCell(); 		

		var rowIndex = cellsSelected[0];
		
		rowIndex = rowIndex +1; 
		this.deletedRows = [rowIndex];
		
		var record = this.store.getById(rowIndex);

		var recordsArray =[];
		if(record){
				recordsArray = [record.json];
		}
		
		
		Ext.Ajax.request({
			url: this.services['delete'],
			method: 'post',
			params: {"records" : Sbi.commons.JSON.encode(recordsArray)},
			success : 
				function(response, opts) {
				try {
					for ( var indic = 0; indic < this.deletedRows.length; indic++) {
						var index = this.deletedRows[indic];
						this.store.removeAt(index);	
					}
					var params = {}
					if(this.start != null){
						params.start = this.start;
					}
					if(this.limit && this.limit!= null){
						params.limit = this.limit;
					}
					
					var filtersValuesObject = this.getFiltersValues();
					Ext.apply(params, filtersValuesObject);

					this.loadFrom(params);

				} catch (err) {
					Sbi.exception.ExceptionHandler.handleFailure();
				}
			},
			failure: function(msg, title){
				Ext.MessageBox.show({
					title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
					msg : LN('sbi.registry.registryeditorgridpanel.saveconfirm.message.ko'),
					buttons : Ext.MessageBox.OK,
					width : 300,
					icon : Ext.MessageBox.INFO
				});
			},
			scope: this
		});
	}

	,
	updateSuccessHandler : function () {
		this.hideMask();
		Ext.MessageBox.show({
			title : LN('sbi.registry.registryeditorgridpanel.saveconfirm.title'),
			msg : LN('sbi.registry.registryeditorgridpanel.saveconfirm.message'),
			buttons : Ext.MessageBox.OK,
			width : 300,
			icon : Ext.MessageBox.INFO
		});
		this.store.commitChanges();
		this.updateRowSpan();
	}

,
    onLayout : function(vw, vh) {
    	//alert('span');

    	this.updateRowSpan();
    	
    	this.colorTotalRows();
    	
    	
    },
    isNumeric: function(input) {
        var number = /^\-{0,1}(?:[0-9]+){0,1}(?:\.[0-9]+){0,1}$/i;
        var regex = RegExp(number);
        return regex.test(input) && input.length>0;
    },
//    onAfterEdit: function(o) {
////    	Ext.MessageBox.alert('onAfterEdit');
// alert('ciao');
//    }
//    ,
    colorTotalRows: function() {
    	if(this.meta){
    		var view = this.getView();
    	
            if(this.checkIfReady(this.getColumnModel().config)==false){
            	return;
            }

    		for(var j = 0; j < this.meta.summaryColorCellsCoordinates.length; j++) {
    			var coord = this.meta.summaryColorCellsCoordinates[j];
        		var cell = view.getCell(coord.row, coord.column+1);
        		var Ccell = Ext.get(cell);
        		var  cdcd= Ccell.down('*');
        		var innerCell = Ext.get(cdcd);
        		
        		//get summaryColor
        		var regConf = this.registryConfiguration;
        		
        		var colSum = '#FFFFFF';
        		if(regConf.summaryColor){
        			colSum = regConf.summaryColor;
        		}
        		
        		if(innerCell != null){
        			innerCell.setStyle('background-color', colSum);
        			innerCell.setStyle('fontWeight', 'bold');
        		}
        		Ccell.setStyle('background-color', colSum);
        		Ccell.setStyle('fontWeight', 'bold');
        		Ccell.setStyle('align', 'right');
        	
	            var browser = this.detectBrowser();
	            var IE = false;
	            if(browser != null && browser == 'IE'){
	            	IE = true;
	            }		
        		
        		if(IE == false){
        		if(cell.textContent && this.isNumeric(cell.textContent)){
        				cell.textContent = cell.textContent;
            			//Ccell.setStyle('padding-left', '20');
        				if(innerCell != null){
                			innerCell.setStyle('line-height', '10px');
                		}
        		}
        		}
        		else{
            		if(cell.innerText && this.isNumeric(cell.innerText)){
        				cell.innerText = cell.innerText;
            			//Ccell.setStyle('padding-left', '20');
        				if(innerCell != null){
                			innerCell.setStyle('line-height', '10px');
                		}
        		}
        		}
        		
        		// remove content and add simple value in order to remove the editor
        		
    		
    		}
    	}
    },
    updateTotalRow : function(row, column, newValue, previousValue) {
    	var view = this.getView();
  		
  		
  		// find first total cell that has same column as parameter column and that has row > parameter row
    	var foundTotal = false;
    	for(var j = 0; j < this.meta.summaryCellsCoordinates.length && foundTotal == false; j++) {
			var coord = this.meta.summaryCellsCoordinates[j];
			if((coord.column +1) == column && coord.row > row){
				// found cell to update
				var cell = view.getCell(coord.row, coord.column+1);
				
				
	            var browser = this.detectBrowser();
	            var IE = false;
	            if(browser != null && browser == 'IE'){
	            	IE = true;
	            }				
				
	            
	            
		  		var previousTotal; 
		  			
		  		if(IE == true){
		  			previousTotal = parseFloat(cell.innerText);
			  		// add to previous total the difference between new and old value
			  		var newTotal = previousTotal - previousValue + parseFloat(newValue);
			  		newTotal = newTotal.toFixed(2);
			  		cell.innerText = newTotal;
		  		}	
		  		else{
		  			previousTotal = parseFloat(cell.textContent);
			  		// add to previous total the difference between new and old value
			  		var newTotal = previousTotal - previousValue + parseFloat(newValue);
			  		newTotal = newTotal.toFixed(2);
			  		cell.textContent = newTotal;
		  		}
		  		
		  		
		  		foundTotal = true;
		  		
			}
		}
  		
    }
    , checkIfReady: function(columns) {
    	   // check if data is not still loaded
        if(this.indexColumnToMerge == null || this.indexColumnToMerge == undefined) 
 		{ 
 				return false;
 		}
         if(columns != undefined ) {
         	if(columns[1] != undefined) 
         		{
         		if(columns[1].dataIndex=="data")
         			{
 					return false;
         			} 
         		}
         } 
     return true;
    }
    ,
    updateRowSpan: function() {
//    	alert('updateRowSpan');
        var columns = this.getColumnModel().config,
        view = this.getView(),
        store = this.getStore(),
        rowCount = store.getCount();

        if(this.checkIfReady(this.getColumnModel().config)==false){
        	return;
        }
     
        // this array tells for each row if it was broken
        var rowIsBreakArray = new Array(rowCount);
        for (var r = 0; r < rowIsBreakArray.length; ++r) {
        	rowIsBreakArray[r] = false;
        }
        
        // col is the index of column to span among column indexes array
        for (var col = 0; col < this.indexColumnToMerge.length; ++col) {
        	// index of column to span
        	var colindex = this.indexColumnToMerge[col];
        	var column = columns[colindex];
        	// index name of the column to span
        	var dataIndex = column.dataIndex;
            
        	// store the previuous cell
        	var spanCell = null;
        	var spanCount = null;
        	var spanValue = null;
        	//var spanValueArray = new Array(this.indexColumnToMerge.length);
        
        	// for each row of the store
        	for (var row = 0; row < rowCount; ++row) {
        			var record = store.getAt(row);

        			// cell to be analyzed and current value 
        			var cell = view.getCell(row, colindex);
        			var value = record.get(dataIndex);
            
        			// set span if current value is different than previous or if that row was already broken 
        			if (spanValue != value || rowIsBreakArray[row] == true) {
        					if (spanCell !== null) {
        						this.setSpan(Ext.get(spanCell), spanCount, column);
        						// if spanned is set means that at row-1 there is a break point, set it
        						rowIsBreakArray[row] = true;
        					}
                
        					spanCell = cell;
        					spanCount = 1;
        					spanValue = value;
        					//if(cell){
        					//cell.on('afteredit', alert('prima'), this);
        					//}
        					} 
        			else {
        					spanCount++;
        			}
        	}
        
	        if (spanCell !== null) {
	            this.setSpan(Ext.get(spanCell), spanCount, column);
	        }
        }
    },
    
    detectBrowser: function() {
    	var brow = null;
    	
    	var val = navigator.userAgent.toLowerCase();
           
    	if(val.indexOf("firefox") > -1)
    	{
    		brow = 'firefox';
    	} 
    	else if(val.indexOf("opera") > -1)
    	{
    		brow = 'opera';
    	}
    	else if(val.indexOf("msie") > -1)
    	{
    		brow = 'IE';

    	} 
    	else if(val.indexOf("safari") > -1)
    	{
    		brow = 'safari';

    	} 
    	return brow;
    }
     ,setSpan: function(cell, count, column) {
        var view = this.getView();
        var cd = cell.down('*');
        var innerCell = Ext.get(cd);
        var height = cell.getHeight();
        var width = cell.getWidth();
        
        cell.setStyle('position', 'relative');
        if (count == 1) {
            innerCell.setStyle('position', '');
            innerCell.setStyle('height', '');
//            innerCell.setStyle('height', '');
            innerCell.setStyle('background-color', this.columnHeader2color[column.name]);
        } else {
            innerCell.setStyle('position', 'absolute');
            var lineHeight = height * (count);
            innerCell.setStyle('line-height', lineHeight+'px');     
            
            
            var marginHeight = 1.5;
            var browser = this.detectBrowser();

            if(browser != null && browser == 'IE'){
            	marginHeight = 3;
            }
            else if(browser != null && browser == 'firefox'){
            	marginHeight = 3;
            }
            
            var hei = (marginHeight*count);
        	innerCell.setStyle('height', (height * count)+hei- cell.getPadding('tb') - innerCell.getPadding('tb') + 'px');
            
/*            if(count >= 8){
            	
                var hei = (marginHeight*count);
            	innerCell.setStyle('height', (height * count)+hei + 'px');
            }


            if(count >= 5 && count < 8){
            	var hei = (marginHeight*count);
            	  if(browser != null && (browser == 'IE')){
            		  innerCell.setStyle('height', (height * count - cell.getPadding('tb')+hei) + 'px');
            	  }
            	  else{
            		  innerCell.setStyle('height', (height * count - cell.getPadding('tb')) + 'px');
            	  }
            	 }

            
//          if(count >= 8){
//          	innerCell.setStyle('height', ((height+1) * count) + 'px');
//          }
            
            //            if(count >= 5 && count < 8){
//            	innerCell.setStyle('height', (height * count - cell.getPadding('tb')) + 'px');
//            }
          
            if(count < 5){
            	innerCell.setStyle('height', (height * count - cell.getPadding('tb') - innerCell.getPadding('tb')) + 'px');
            } */
            
            innerCell.setStyle('width', (width - cell.getPadding('lr') - innerCell.getPadding('lr')) + 'px');

            innerCell.setStyle('background-color', this.columnHeader2color[column.name]);
  
        }
    }        
     
     , getFormatDate: function(idxcol, st){
    	   var columnFromTemplate = this.registryConfiguration.columns[idxcol];
		   var formatDate;
		   if(columnFromTemplate.format){
			  return columnFromTemplate.format;
		   }
		   //defaults getted from locale files:		   
		   if(st != null && st !== undefined && st === 'timestamp')
			   formatDate =  Sbi.locale.formats['timestamp'].dateFormat;
		   else
			   formatDate = Sbi.locale.formats['date'].dateFormat;
		   return formatDate;
     }
     
    
     /**
      * Opens the loading mask 
      */
     , showMask : function(){
     	if (this.saveMask == null) {
     		this.saveMask = new Ext.LoadMask('RegistryEditorGridPanel', {msg: "Saving.."});
     	}
     	this.saveMask.show();
     }

     /**
      * Closes the loading mask
      */
     , hideMask: function() {	
     	if (this.saveMask != null) {
     		this.saveMask.hide();
     	}
     }
	
});
function isInteger(s) {
	if (s.search) return (s.search(/^-?[0-9]+$/) == 0);
	else return  true;
}
function isUnsignedInteger(s) {
	if (s.search) return (s.search(/^[0-9]+$/) == 0);
	else return  true;
}
function isFloat(s){
	if (s.search) return (s.search(/^-?[0-9]*[.][0-9]+$/) == 0);
	else return  true;
}




