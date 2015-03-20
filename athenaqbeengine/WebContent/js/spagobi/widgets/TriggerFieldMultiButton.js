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


Ext.ns("Sbi.widgets");


Sbi.widgets.TriggerFieldMultiButton = function(config) {    
	
	
	var c = Ext.apply({}, config, {});   
	    
	Sbi.widgets.TriggerFieldMultiButton.superclass.constructor.call(this, c);  

};


Sbi.widgets.TriggerFieldMultiButton = Ext.extend(Ext.form.TwinTriggerField, {
    
	validationEvent:false,
	validateOnBlur:false,
	trigger1Class:'x-form-search-trigger',
	trigger2Class:'trigger-up',
	
	initComponent : function(){
		Sbi.widgets.TriggerFieldMultiButton.superclass.initComponent.call(this);       
    },  
    
    onRender : function(ct, position){
    	
    	Ext.form.TriggerField.superclass.onRender.call(this, ct, position);
        this.wrap = this.el.wrap({cls: "x-form-field-wrap"});
        this.trigger = this.wrap.createChild(this.triggerConfig ||
                {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: this.triggerClass});
        this.initTrigger();
    	
        if(!this.width){
            this.wrap.setWidth(this.el.getWidth()+this.trigger.getWidth());
        } else {
        }    	
    }

});
