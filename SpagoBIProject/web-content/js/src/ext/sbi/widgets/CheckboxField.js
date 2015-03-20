/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * Sbi.widgets.CheckboxFiel
 * 
 * Authors
 *  - Andrea Gioia (mail)
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.CheckboxField = function(config) {

	var c = Ext.apply({
		//itemCls : 'x-check-group-alt',
		columns : 1,
		items : [ {
			boxLabel : 'Loading options...',
			name : 'loading-mask',
			value : 'mask'
		}]
	}, config || {});
	
	this.store = config.store;
	this.store.on('load', this.refreshOptions, this);
	this.autoLoad = (config.autoLoad === undefined || config.autoLoad === null)? true: config.autoLoad;
	Sbi.trace("[Sbi.CheckboxField.constructor] : autoLoad is equal to [" + this.autoLoad + "]");
	if(this.autoLoad === true) {
		this.store.load();
	}
	
	this.addEvents('change');
	
	// constructor
	Sbi.widgets.CheckboxField.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.widgets.CheckboxField, Ext.form.CheckboxGroup, {

	store: null
	, displayField:'label'
    , valueField:'value'
    , pendingRefreshOptions: false
    , bufferedValue: null
    , refreshed: false
    , fireChekedActive: true
	
    , afterRender : function(){
    	Sbi.widgets.CheckboxField.superclass.afterRender.call(this);
        if(this.bufferedValue && this.refreshed) {
        	this.doSetValue(this.bufferedValue);
        	delete this.bufferedValue;
        }
    }
    
	, refreshOptions: function() {		
		Sbi.trace('[CheckboxField.refreshOptions] : IN');
		var oldValue = this.getValue();
		
		// manage the case in which the store is loaded before the component is rendered
		if(this.rendered === false) {
			this.pendingRefreshOptions = true;
			this.on('render', this.refreshOptions, this);
			return;
		}			
		
		if(this.pendingRefreshOptions === true) {
			this.pendingRefreshOptions = false;
			this.un('render', this.refreshOptions);
		}
		
		// remove old options
		while(this.items.length > 0) {
			var item = this.items.removeAt(0);
			this.panel.items.get(0).remove(item);
			item.destroy();
		}
		this.panel.doLayout();
		
		// add new options
		var records = this.store.getRange();
		for(var i = 0; i < records.length; i++) {
			var label = records[i].get(this.displayField);
			var value = records[i].get(this.valueField);
			
			var optionItem = this.createOptionItem({
				boxLabel : label,
				name : this.parameterId,
				value : value
			});
			var colNo = this.items.getCount() % this.panel.items.getCount();
			var col = this.panel.items.get( colNo );
			//alert('Add option [' + value + '] to column [' + colNo + ']');
			this.items.add(optionItem);
			col.add(optionItem);
		}
		try {
			this.panel.doLayout();
		} catch(t) {
			// hide an internal non blocking exception
		}
		
		if(this.refreshed === false) {
			this.refreshed = true;
			if(this.bufferedValue) {
				this.doSetValue(this.bufferedValue);
	        	delete this.bufferedValue;
			}
		} else {
			this.doSetValue(oldValue);
		}
		
		Sbi.trace('[CheckboxField.refreshOptions] : OUT');
	}

	, createOptionItem: function(optionConfig) {
		var checkbox = new Ext.form.Checkbox(optionConfig);
		checkbox.on('check', this.fireChecked, this);
//		checkbox.on('check', function(){
//			this.fireEvent('change', this);
//		}, this);
		return checkbox;
	}
	
	, fireChecked: function(){
		Sbi.trace('[CheckboxField.fireChecked] : IN');
		
		if(this.fireChekedActive === false) return;
        var arr = [];
        if(this.isReady()) {
	        this.eachItem(function(item){
	            if(item.checked){
	                arr.push(item.value);
	            }
	        });
		} else {
			if(this.bufferedValue) {
				arr = this.bufferedValue;
        	}
		}
        this.fireEvent('change', this, arr);

        Sbi.trace('[CheckboxField.fireChecked] : OUT');
    }
	
	, suspendFireChecked: function() {
		this.fireChekedActive = false;
	}
	
	, resumeFireChecked: function() {
		this.fireChekedActive = true;
	}
	
	, eachItem : function(fn){
        if(this.items && this.items.each){
            this.items.each(fn, this);
        }
    }
	
	, isReady: function() {
		return (this.rendered && this.refreshed);
	}
	
	, reset : function(){
		Sbi.trace('[CheckboxField.reset] : IN');
		this.suspendFireChecked();
		if(this.bufferedValue) delete this.bufferedValue;
		Sbi.widgets.CheckboxField.superclass.reset.call(this);
		this.resumeFireChecked();
		Sbi.debug('[CheckboxField.reset] : fire checked');
		this.fireChecked();
		Sbi.debug('[CheckboxField.reset] : reseted');
		
		Sbi.trace('[CheckboxField.reset] : OUT');
    }
    
	, setValue: function(v){
		if (v == null) {
			Sbi.debug('Value in input is null');
			this.doSetValue([]);
			return;
		}
		if(typeof v == 'string' && v.trim() != '') {v = [v];}
		if(!Ext.isArray(v) || v.length == 0) {
			Sbi.warn('Impossible to set value ' + v + ' ' + (typeof v));
			return;
		} 
				
		this.suspendFireChecked();
		if(this.isReady()){
			Sbi.debug('[CheckboxField.setValue] : set value : >' + v + '< ');
			this.doSetValue(v);
		} else {
			this.bufferedValue = v;
			Sbi.debug('[CheckboxField.setValue] : buffer value : >' + v + '< ');
		}
		this.resumeFireChecked();
		this.fireChecked();
	}
	
	, doSetValue: function(v){
		this.suspendFireChecked();
	
		if(Ext.isArray(v)){
			this.eachItem(function(item){
				if(v.indexOf(item.value)> -1){
					Sbi.debug('[CheckboxField.doSetValue] : do set value ' + item.value);
					item.setValue(true);
		        } else {
		        	item.setValue(false);
		        }
		    }); 
		 } else {
			 Sbi.warn('[CheckboxField.doSetValue] : value [' + v + '] is not an array');
		 }
		 this.resumeFireChecked();
	}
	
	, getValue : function(){
        var out = [];
        if(this.isReady()) {
	        this.eachItem(function(item){
	            if(item.checked){
	                out.push(item.value);
	            }
	        });
        } else {
        	if(this.bufferedValue) {
        		out = this.bufferedValue;
        		Sbi.debug('[CheckboxField.getValue] : getBufferedValue ' + out + ' - ' + Ext.isArray(out));
        	}
        }
      
        Sbi.debug('[CheckboxField.getValue] : ' + out + ' - ' + Ext.isArray(out));
        return out;
    }
    
	, getValues : function() {
    	return this.getValue();
	}	
	
	, getRawValue : function() {
		return this.getValues().join();
	}
});