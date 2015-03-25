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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.SendToWindow = function(config) {
	
	this.sendToFormIframeUrl = config.url;
	this.buddy = undefined;
	
	var c = Ext.apply({}, config, {
		id:'win_sendTo',
		bodyCfg: {
			tag:'div',
			cls:'x-panel-body',
			children:[{
				tag:'iframe',
  				src: this.sendToFormIframeUrl,
  				frameBorder:0,
  				width:'100%',
  				height:'100%',
  				style: {overflow:'auto'}  
				}]
		},
		layout:'fit',
		width:650,
		height:400,
		//closeAction:'hide',
		plain: true,
		title: LN('sbi.execution.sendTo')
	});   
	
	// constructor
    Sbi.execution.toolbar.SendToWindow.superclass.constructor.call(this, c);
    
    if (this.buddy === undefined) {
    	this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
    }
    
};

Ext.extend(Sbi.execution.toolbar.SendToWindow, Ext.Window, {});