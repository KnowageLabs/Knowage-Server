/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 

/**
  * Sbi.widgets.Cache 
  * 
  * Simple cache implementation. It can be extended with events...
  * 
  * Public Properties
  * 
  * [list]
  * 
  * Public Methods
  * 
  * - put: puts a value into the cache with the given key
  * 
  * - get: gets a value from the cache with the given key
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.Cache = function(config) {
	
	var defaultSettings = {
			// defaults here
	};
	
	if (Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.cache) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.widgets.cache);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	
	// constructor
	Sbi.widgets.Cache.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.widgets.Cache, Ext.util.Observable, {
	
	cache : null

	,
	init : function () {
		cache = {};
	}
	
	,
	put : function (key, value) {
		cache[key] = value; 
	}
	
	,
	get : function (key) {
		return cache[key];
	}

});