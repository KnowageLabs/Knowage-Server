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
 * 
 * Public Properties
 * 
  * MANDATORY PARAMETERS: serviceUrl: the url for the ajax request
  * OPTIONAL:
  * 	pagingConfig:{} Object. If this object is defined the paging toollbar will be displayed
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
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.widgets.grid.DynamicGridPanel', {
    extend: 'Ext.grid.Panel'

    ,config: {
    	stripeRows: true,
    	
    	/**
    	 * The paging toolbar
    	 */
    	pagingToolbar: null
    	
//    ,	loadMask: null
    }

	, constructor: function(config) {		
		Sbi.debug('DynamicGridPanel costructor IN');
		
		Ext.apply(this,config);
	
//		if (config.parentGrid)
//			this = parentGrid;
		
    	Sbi.debug('DynamicGridPanel build store');
    	config.storeConfig = Ext.apply(config.storeConfig||{},{serviceUrl: config.serviceUrl});
    	if ((config.usePost != null) && (config.usePost != undefined ) ){
        	config.storeConfig.params = config.params;
        	config.storeConfig.usePost = config.usePost;
    	}
    	config.storeConfig.isDynamicStore = true;
    	var store;
    	if (config.storeConfig.storeType && config.storeConfig.storeType == 'InMemoryFilteredStore'){    		
    		store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', config.storeConfig ||{});	
    	}else{
    		store = Ext.create('Sbi.widgets.store.DynamicStore', config.storeConfig ||{});	
    	}
    	
      	this.store = store;
      	
      	this.columns = [];

      	this.store.on('load', this.updateGrid, this);
      	this.addPaging(config);
      	
//      	if(config.pagingConfig!=undefined && config.pagingConfig!=null){
//      		Sbi.debug('DynamicGridPanel load first page');
//      		this.store.loadPage(1);
//      	}else{
//      		Sbi.debug('DynamicGridPanel load store');
//      		this.store.load();
//      	}
      	
      	var additionalButtons = Sbi.widget.grid.StaticGridDecorator.getAdditionalToolbarButtons(this.decorators);
      	
      	if(this.filterConfig!=undefined && this.filterConfig!=null){
      		this.tbar = Ext.create('Sbi.widgets.grid.DynamicFilteringToolbar',Ext.apply(config.filterConfig||{},{store: this.store, additionalButtons:additionalButtons}));
      	}
      	
    	this.callParent(arguments);
    	this.bbar = this.pagingToolbar;
    	this.on('afterrender', this.loadStore, this);
      	
    	Sbi.debug('DynamicGridPanel costructor OUT');
    },
    
    addPaging: function(config){    	
    	if(config.pagingConfig!=undefined && config.pagingConfig!=null){
    		
    		Sbi.debug('DynamicGridPanel add paging IN');
    		var defaultPagingConfig={
    				width: 400,
                store: this.store,
                displayInfo: true,
                displayMsg: 'Displaying  {0} - {1} of {2}',
                emptyMsg: "No rows to display"
            }
    		defaultPagingConfig = Ext.apply(defaultPagingConfig,config.pagingConfig );
    		this.pagingToolbar = Ext.create('Ext.PagingToolbar',defaultPagingConfig);
    		this.bbar = this.pagingToolbar;
    		Sbi.debug('DynamicGridPanel add paging OUT');
    	}
    },
    
    updateGrid: function(){
    	Sbi.debug('DynamicGridPanel updategrid IN');
    	var columns = this.store.getColumns();
    	Sbi.widget.grid.StaticGridDecorator.addButtonColumns(this.decorators, this.columns, this);
//    	if((this.bbar == undefined || this.bbar==null) && this.pagingToolbar){
//    		this.bbar = this.pagingToolbar;
//    	}
    	if(this.bbar!=undefined && this.bbar!=null){     
    		this.bbar.bindStore(this.store);    		
    		this.bbar.doLayout();
    	}
    	this.reconfigure(this.store, columns);
    }
    
    , loadStore: function(t){
//    	this.showMask();
    	if(t.pagingConfig!=undefined && t.pagingConfig!=null){
      		Sbi.debug('DynamicGridPanel load first page');      		
      		this.store.loadPage(1);
      	}else{
      		Sbi.debug('DynamicGridPanel load store');
      		this.store.load();
      	}
//    	this.hideMask.defer(2000, this);
    }

//	/**
//	 * Opens the loading mask 
//	 */
//    , showMask : function(){
//    	this.un('afterlayout',this.showMask,this);
//    	if (this.loadMask == null) {    		
//    		this.loadMask = new Ext.LoadMask('DynamicGridPanel', {msg: "Loading.."});
//    	}
//    	if (this.loadMask){
//    		this.loadMask.show();
//    	}
//    }
//
//	/**
//	 * Closes the loading mask
//	*/
//	, hideMask: function() {
//    	if (this.loadMask && this.loadMask != null) {	
//    		this.loadMask.hide();
//    	}
//	} 

});


