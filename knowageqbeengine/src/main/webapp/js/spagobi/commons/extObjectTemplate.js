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
  * - name (mail)
  */

Ext.ns("Sbi.xxx");

Sbi.xxx.Xxxx = function(config) {
	
	var defaultSettings = {
			title: LN('sbi.qbe.queryeditor.title'),
		};
		
		if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.queryBuilderPanel) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.queryBuilderPanel);
		}
		
		var c = Ext.apply(defaultSettings, config || {});
		
		Ext.apply(this, c);
		
		
		this.services = this.services || new Array();	
		this.services['doThat'] = this.services['doThat'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'DO_THAT_ACTION'
			, baseParams: new Object()
		});
		
		this.addEvents('customEvents');
		
		this.initThis(c.westConfig || {});
		this.initThat(c.westConfig || {});
	
		c = Ext.apply(c, {
	      	layout: 'border',      	
	      	items: [this.thisPanel, this.thatPanel]
		});

		// constructor
		Sbi.xxx.Xxxx.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.xxx.Xxxx, Ext.util.Observable, {
    
    services: null
   
   
    // public methods
});