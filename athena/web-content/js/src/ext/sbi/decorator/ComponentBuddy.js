/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * ComponentBuddy 
  * 
  * follows the twin element making it always rendered 
  * on top of otherones in the page
  * 
  * 
  * Public Properties
  * 
  * - buddy: the twin element
  * 
  * 
  * Authors
  * 
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.commons.ComponentBuddy");

Sbi.commons.ComponentBuddy = function(config) {
    
    this.buddy = undefined;
    
    Ext.apply(this, config);
	
	//this.addEvents();	
    
    // sub-components    
   
    var el =  Ext.get(Ext.DomHelper.append( Ext.getBody(), {
                            tag : 'iframe',
                            frameborder : 0,
                            html : 'Inline frames are NOT enabled\/supported by your browser.',
                            src : (Ext.isIE && Ext.isSecure)? Ext.SSL_SECURE_URL: 'about:blank'}));
                    
    el.applyStyles({
      position: 'absolute',
      'background-color': 'white',
      'z-index': 1000,
      width: '400px',
	  height: '400px',
      opacity: 1
      
    });
    
    Sbi.commons.ComponentBuddy.superclass.constructor.call(this, el.dom.id, true);
    
    this.buddy.fixEl = this;
        
    this.buddy.addListener('render', function(){this.keepInTouchWithBuddy('render');}, this);
    this.buddy.addListener('show', function(){this.keepInTouchWithBuddy('show');}, this);
    this.buddy.addListener('move', function(){this.keepInTouchWithBuddy('move');}, this);
    this.buddy.addListener('hide', function(){this.keepInTouchWithBuddy('hide');}, this);
    
    
    
    if(this.buddy.dd) {
    	this.buddy.dd.onDrag = function(e) {
        	this.buddy.dd.alignElWithMouse(this.buddy.dd.proxy, e.getPageX(), e.getPageY());
            this.buddy.dd.alignElWithMouse(this, e.getPageX()-1, e.getPageY());
        };
    }
    
     
}




Ext.extend(Sbi.commons.ComponentBuddy, Ext.Element, {
    
    // static contens and methods definitions
    keepInTouchWithBuddy : function(eventName){
      
      var box = this.buddy.getBox();
      box.width += 3;
      box.height += 3;
      this.setBox( box );
      
      if(eventName === 'hide') {
      	this.hide();
      } else if (eventName === 'show') {
      	this.show();
      	if(this.buddy.dd) {
	    	this.buddy.dd.fixEl = this;
	    	this.buddy.dd.onDrag = function(e) {
	        	this.alignElWithMouse(this.proxy, e.getPageX(), e.getPageY());
	            this.alignElWithMouse(this.fixEl, e.getPageX()-1, e.getPageY());
	        };
    	  }
      } else if (eventName === 'render') {
      	 if(this.buddy.dd) {
	    	this.buddy.dd.onDrag = function(e) {
	        	this.buddy.dd.alignElWithMouse(this.buddy.dd.proxy, e.getPageX(), e.getPageY());
	            this.buddy.dd.alignElWithMouse(this, e.getPageX()-1, e.getPageY());
	        };
    	  }
      }
    }
    
});

