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