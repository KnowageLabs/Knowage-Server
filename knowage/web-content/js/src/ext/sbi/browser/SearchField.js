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

