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