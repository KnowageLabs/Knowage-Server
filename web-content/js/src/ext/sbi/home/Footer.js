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
  * - Chiara Chiarelli (chiara.chiarelli@eng.it)
  */

Ext.ns("Sbi.home");

Sbi.home.Footer = function(config) {
	
		var itemsForFooter = [];
		
		if(showFooter){
		itemsForFooter.push({
				contentEl: 'Footer'
	             });
	    }
	
		var c = Ext.apply({}, config,  {
			region: 'south',
	        xtype: 'panel',
	        items: itemsForFooter,
	        border: false,
			collapseMode: 'mini',
	        autoHeight: true
		});   
		        
		Sbi.home.Footer.superclass.constructor.call(this, c);
    	
	
};

Ext.extend(Sbi.home.Footer, Ext.Panel, {

	// ---------------------------------------------------------------------------
    // public methods
	// ---------------------------------------------------------------------------

});