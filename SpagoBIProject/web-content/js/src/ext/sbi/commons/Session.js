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
  * -  Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.commons");

Sbi.commons.Session = new (function(){
	
	this.services = new Array();
	this.services['extendSession'] = Sbi.config.contextName + '/servlet/ExtendSession';
	
	this.extend = function(){
    	Ext.Ajax.request({
	        url: this.services['extendSession']
	   });
    };
       
})();