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
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.ns("Sbi.engines.chart.data");

Sbi.engines.chart.data.StoreManager = function(config) {
		var defaultSettings = {
			
		};
		
		if(Sbi.settings && Sbi.settings.chart && Sbi.engines.chart.data && Sbi.engines.chart.data.storeManager) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.engines.chart.data.storeManager);
		}
		
		var c = Ext.apply(defaultSettings, config || {});
		Ext.apply(this, c);
		
		this.init(c.datasetsConfig);
		
		// constructor
		Sbi.engines.chart.data.StoreManager.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.engines.chart.data.StoreManager, Ext.util.Observable, {
    
	stores: null
   
	//  -- public methods ---------------------------------------------------------
    
	, addStore: function(s) {
		s.ready = s.ready || false;
		s.storeType = s.storeType || 'ext';
	//	s.filterPlugin = new Sbi.console.StorePlugin({store: s});
		
		this.stores.add(s);
				
		
		if(s.refreshTime) {
			var task = {
				run: function(){
					//if the console is hidden doesn't refresh the datastore
					if(s.stopped) return;
					
					// if store is paging...
					if(s.lastParams) {
						// ...force remote reload
						delete s.lastParams;
					}
					
					s.load({
						params: s.pagingParams || {}, 
						callback: function(){this.ready = true;}, 
						scope: s 
						//,add: false
					});
				},
				interval: s.refreshTime * 1000 //1 second
			}
			Ext.TaskMgr.start(task);
		}
	}

	, getStore: function(storeId) {
		return this.stores.get(storeId);
	}
	
	, stopRefresh: function(value){
		for(var i = 0, l = this.stores.length, s; i < l; i++) {
			var s = this.stores.get(i);
			s.stopped = value;
		}
		 
	}
	
	//refresh All stores of the store manager managed
	, forceRefresh: function(){
		for(var i = 0, l = this.stores.length; i < l; i++) {
			var s = this.getStore(i);
			//s.stopped = false; 
			if (s !== undefined && s.dsLabel !== undefined && s.dsLabel !== 'testStore' && !s.stopped){
				
				s.load({
					params: s.pagingParams || {},
					callback: function(){this.ready = true;}, 
					scope: s, 
					add: false
				});
			}
		}
	}

	
	
	//  -- private methods ---------------------------------------------------------
    
    , init: function(c) {
		c = c || [];
	
		this.stores = new Ext.util.MixedCollection();
		this.stores.getKey = function(o){
            return o.storeId;
        };
		
		for(var i = 0, l = c.length, s; i < l; i++) {
			s = new Sbi.engines.chart.data.Store({
				storeId: c[i].id
				, datasetLabel: c[i].label
				, dsTypeCd: c[i].dsTypeCd
				, dsPars: c[i].pars
				, dsTransformerType: c[i].dsTransformerType
				, refreshTime: c[i].refreshTime
				, autoLoad: false
			}); 
		
			s.ready = c[i].ready || false;
			s.storeType = 'sbi';
			
			this.addStore(s);
		}
	
		// for easy debug purpose
		var testStore = new Ext.data.JsonStore({
			id: 'testStore'
			, fields:['name', 'visits', 'views']
	        , data: [
	            {name:'Jul 07', visits: 245000, views: 3000000},
	            {name:'Aug 07', visits: 240000, views: 3500000},
	            {name:'Sep 07', visits: 355000, views: 4000000},
	            {name:'Oct 07', visits: 375000, views: 4200000},
	            {name:'Nov 07', visits: 490000, views: 4500000},
	            {name:'Dec 07', visits: 495000, views: 5800000},
	            {name:'Jan 08', visits: 520000, views: 6000000},
	            {name:'Feb 08', visits: 620000, views: 7500000}
	        ]
	    });
		
		testStore.ready = true;
		testStore.storeType = 'ext';
		
		this.addStore(testStore);
		
	}
    
    
});