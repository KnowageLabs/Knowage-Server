/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * 
 * This is a grid panel linked to a Store.. It builds the model and the associated store. 
 * It adds the widgets to the grid rows and the buttons in the toolbar according to the configuration.
 * It use the REST services to connect to server. The connection properties are in the definition of the model 
 * 
 * 		@example
 * 		...
 *		var FixedGridPanelConf= {
 *			pagingConfig:{},
 *			storeConfig:{ 
 *				pageSize: 5
 *			},
 *			columnWidth: 2/5,
 *			buttonToolbarConfig:{
 *					newButton: true,
 *					cloneButton: true
 *			},
 *			buttonColumnsConfig:{
 *				deletebutton:true,
 *				selectbutton: true
 *			},
 *			modelName: "ModelName",
 *			columns: this.columns
 *		};
 *		
 *		Ext.apply(this,config||{});
 *		
 *		this.grid=Ext.create('Sbi.widgets.grid.FixedGridPanel',FixedGridPanelConf);
 *		... 
 * 
 * @author
 * Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.widgets.grid.FixedGridPanel', {
    extend: 'Ext.grid.Panel'

    ,config: {
    	/**
    	 * Stripe rows
    	 */
    	stripeRows: true,
    	/**
    	 * The paging toolbar
    	 */
    	pagingToolbar: null,
    	/**
    	 * The name of the Model
    	 */
    	modelName:null,
    	/**
    	 * The optional configuration for the store
    	 */
    	storeConfig: null,
    	/**
    	 * The optional configuration for the paging toolbar. Null to hide the toolbar
    	 */
    	pagingConfig:null,
    	/**
    	 * Configuration object for the widgets buttons to add in every row of the grid
    	 */
    	buttonColumnsConfig:null,
    	/**
    	 * Configuration object for the buttons to add in the toolbar. {@link Sbi.widget.grid.StaticGridDecorator#StaticGridDecorator}
    	 */
    	buttonToolbarConfig: null,
    	/**
    	 * The definition of the columns of the grid. {@link Sbi.widgets.store.InMemoryFilteredStore#InMemoryFilteredStore}
    	 */
    	columns: [],
    	/**
    	 * If true force the grid to fit the width of the container. Default true.
    	 */
    	adjustWidth: true,
    	/**
    	 * The list of the properties that should be filtered 
    	 */
    	filteredProperties: new Array(),
    	/**
    	 * Object with internal properties to filter
    	 */
    	filteredObjects: null
    }

	/**
	 * The constructor:
	 * 1) builds the store. You can change the behavior using the configuration storeConfig
	 * 2) add pagination and the additional button to the toolbar
	 */
	, constructor: function(config) {
		this.initConfig(config);
		Sbi.debug('FixedGridPanel costructor IN');
		Ext.apply(this,config||{});
		
		if(!this.store){
			this.store = this.buildStore(this.modelName);	
		}
		
    	this.addPaging();
      	
      	if(this.pagingConfig!=undefined && this.pagingConfig!=null){
      		Sbi.debug('FixedGridPanel load first page');
      		this.store.loadPage(1);
      	}else{
      		Sbi.debug('this.fields load store');
      		this.store.load();
      	}
      	

    	//Add the widgets to the rows
      	Sbi.widget.grid.StaticGridDecorator.addButtonColumns(this.buttonColumnsConfig, this.columns, this);
      
      	
      	this.addToolbar();

      	this.callParent(arguments);
    	
      	//resize the grid to fit the width of the container
      	if(this.adjustWidth==undefined || this.adjustWidth==null || this.adjustWidth){
    		this.on("resize",this.adjustColumnsWidth,this);
    	}

    	Sbi.debug('FixedGridPanel costructor OUT');
    },
    
    /**
     * @private
     * Add the paging toolbar to the grid
     */
    addPaging: function(){
    	
    	if(this.pagingConfig!=undefined && this.pagingConfig!=null){
    		Sbi.debug('FixedGridPanel add paging IN');
    		var defaultPagingConfig={
                store: this.store,
                displayInfo: true,
                displayMsg: 'Displaying  {0} - {1} of {2}',
                emptyMsg: "No rows to display"
            }
    		defaultPagingConfig = Ext.apply(defaultPagingConfig,this.pagingConfig );
    		this.pagingToolbar = Ext.create('Ext.PagingToolbar',defaultPagingConfig);
    		this.bbar = this.pagingToolbar;
    		Sbi.debug('FixedGridPanel add paging OUT');
    	}
    },
    
    /**
     * fit the columns non decorated to the width of the grid
     * @private
     * Set the width of the columns to force the panel width to fit the container width
     */
    adjustColumnsWidth: function(){
    	var columns = this.columns;
    	var thisw = this.getWidth();
    	var decoratedColumnsWidth = 0;
    	var decoratedColumns = 0;
    	
    	//Search the columns that contains a widget. They have a fixed width
    	for(var i=0; i<columns.length; i++){
    		if(columns[i].columnType == "decorated"){
    			decoratedColumnsWidth = columns[i].width+decoratedColumnsWidth;
    			decoratedColumns++;
    		}
    	}
    	
    	var nondecoratedColumns = columns.length-decoratedColumns;
    	if(thisw && nondecoratedColumns>0){
    		var nondecoratedWidth = ((thisw-decoratedColumnsWidth)/nondecoratedColumns)-1;
        	for(var i=0; i<columns.length; i++){
        		if(columns[i].columnType ==null || columns[i].columnType == undefined || columns[i].columnType != "decorated"){
        			columns[i].setWidth(nondecoratedWidth);
        		}
        	}
    	}
    },
        
    /**
     * Set the size of the page to the store and reloads the first page
     * @param rthe size of the page
     */
    setPageSize: function(size){
    	this.store.pageSize = size;
    	this.store.loadPage(1);
    },

    /**
     * @private
     * Add the toolbar to the grid
     */
    addToolbar: function(){
    	Sbi.debug('FixedGridPanel adding the toolbar..');
      	//Adds the additional buttons to the toolbar
      	this.additionalButtons = Sbi.widget.grid.StaticGridDecorator.getAdditionalToolbarButtons(this.buttonToolbarConfig, this);
      	//if the toolbar contains some button we create it
      	if(this.additionalButtons){
      		this.tbar = Ext.create('Ext.toolbar.Toolbar',{items: this.additionalButtons});
      	}
      	Sbi.debug('FixedGridPanel toolbar added.');
    },

    /**
     * Builds the store starting from the model
     * @param {String} modelname the name of the model 
     */
    buildStore: function(modelname){
		//BUILD THE STORE
    	Sbi.debug('FixedGridPanel bulding the store...');
    	
    	this.storeConfig = Ext.apply({
    		parentGrid: this,
    		model: modelname
    	},this.storeConfig||{});
    	Sbi.debug('FixedGridPanel store built.');
    	return Ext.create('Ext.data.Store', this.storeConfig);
    }

});


