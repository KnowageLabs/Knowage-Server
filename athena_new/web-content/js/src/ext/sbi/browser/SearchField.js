/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.SearchField = function(config) {    
	
	
	var c = Ext.apply({}, config, {
	});   
	    
	Sbi.browser.SearchField.superclass.constructor.call(this, c);  
	
	this.addEvents("onsearch", "onreset");
};

Sbi.browser.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
    
	validationEvent:false,
	validateOnBlur:false,
	trigger1Class:'x-form-clear-trigger',
	trigger2Class:'x-form-search-trigger',
	hideTrigger1:true,
	//width:244,
	hasSearch : false,
	paramName : 'query',
	
	initComponent : function(){
		Sbi.browser.SearchField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger2Click();
            }
        }, this);
        
    },  
    
    onRender : function(ct, position){
    	
    	Ext.form.TriggerField.superclass.onRender.call(this, ct, position);
        this.wrap = this.el.wrap({cls: "x-form-field-wrap"});
        this.trigger = this.wrap.createChild(this.triggerConfig ||
                {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.triggerClass});
        if(this.hideTrigger){
            this.trigger.setDisplayed(false);
        }
        this.initTrigger();
    	
        if(!this.width){
        	//alert('A ');
            this.wrap.setWidth(this.el.getWidth()+this.trigger.getWidth());
        } else {
        	//alert('B ');
        	//this.wrap.setWidth(200);
        }    	
    },

    onTrigger1Click : function(){
        if(this.hasSearch){ 
        	this.triggers[0].hide();
            this.hasSearch = false;   
            this.fireEvent('onreset', this);
        }        
    },

    onTrigger2Click : function(){
        var v = this.getRawValue();
        if(v.length < 1){
            this.onTrigger1Click();
            return;
        }
        
        this.hasSearch = true;
        this.triggers[0].show();
        this.fireEvent('onsearch', this, v);
    }
});

