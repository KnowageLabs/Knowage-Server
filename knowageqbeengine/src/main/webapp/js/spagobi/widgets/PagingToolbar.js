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

Sbi.widgets.PagingToolbar = function(config) {
	
	// constructor
	Sbi.widgets.PagingToolbar.superclass.constructor.call(this, config)
};

Ext.extend(Sbi.widgets.PagingToolbar, Ext.PagingToolbar, {
    
	beforeLoad : function(store, o){
		Sbi.widgets.PagingToolbar.superclass.beforeLoad.call(this);
		
		 var pn = store.paramNames;
		 if(o.params[pn.start] === undefined && o.params[pn.limit] === undefined) {
			 // load has been forced not by paging toolbar
			 o.params[pn.start] = 0; //this.cursor;
			 o.params[pn.limit] = this.pageSize
		 }
		 
		// alert('PAGING_TOOLBAR\n' +  o.params.toSource());
	
		 return true;
	}
});