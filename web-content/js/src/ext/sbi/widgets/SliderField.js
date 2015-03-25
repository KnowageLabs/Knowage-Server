/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * Sbi.widgets.SliderField
 * 
 * Authors
 *  - Andrea Gioia (mail)
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.SliderField = function(config) {

	this.store = config.store;
	this.store.on('load', this.refreshOptions, this);
	
	this.autoLoad = (config.autoLoad === undefined || config.autoLoad === null)? true: config.autoLoad;
	Sbi.trace("[Sbi.SliderField.constructor] : autoLoad is equal to [" + this.autoLoad + "]");
	if(this.autoLoad === true) { 
		this.store.load();
	}
	
	this.addEvents('change');
	
	// constructor
	Sbi.widgets.SliderField.superclass.constructor.call(this, config);
	
};

Ext.extend(Sbi.widgets.SliderField, Ext.form.SliderField , {
	
	 /**
     * @cfg {Boolean} multiSelect
     * True to have two thumbs instead that only one in order to allow the user to select a range 
     * and not only e punctual value. Defaults to <tt>false</tt>.
     */
	multiSelect: false,
	
	originalIndex: null,
	thumbRendered: false,
	storeLoaded: false,
	
	/**
     * @cfg {Boolean} reseted
     * <tt>true</tt> if no value has been set for the input field after the last reset, <tt>false</tt>
     * otherwise. Defaults to <tt>true</tt>.
     */
	reseted: true,
	
	/**
     * @cfg {Boolean} useUndefinedWhenReseted
     * <tt>true</tt> if the method #getValues must return <tt>undefined</tt> when the input field is reseted
     * (i.e. property reseted equals to <tt>true</tt>),  <tt>false</tt> otherwise. Defaults 
     * to <tt>false</tt>.
     */
	useUndefinedWhenReseted: false,
	
	  /**
     * Initialize the component.
     * @private
     */
    initComponent : function() { 	
    	Sbi.trace("[Sbi.SliderField.initComponent] : [" + this.name + "] : IN");
    	this.sliderCfgProperties.push('values');
    	this.sliderCfgProperties.push('value');
       
    	Sbi.widgets.SliderField.superclass.initComponent.call(this);
    	Sbi.trace("[Sbi.SliderField.initComponent] : [" + this.name + "] : OUT");
    }, 
    
    initSlider : function(cfg) {
    	Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] : IN");
    	if(this.colspan){
    		cfg.colspan = this.colspan;
    	}
    	if(this.thickPerc){
    		cfg.thickPerc = this.thickPerc;
    	}
    	
    	this.slider = new Ext.slider.MultiSlider(cfg);
    	this.slider.store = this.store;
    	Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] :  OUT");
    	return this.slider;
    }, 
    
    afterRender : function(){
    	Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] : IN");
    	Sbi.widgets.SliderField.superclass.afterRender.call(this);
        if(this.multiSelect === true) {
        	// the add an extra tab
        	this.slider.addThumb();
        	Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] : second thumb added succesfully to the multiselect slider");
        } else {
        	Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] : the slider is not multiselect so there is no need to add the second thumb");
        }
        this.originalIndex = this.multiSelect===true? [0,this.slider.maxValue]: [0];
        Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] : originalIndex set equal to " + this.originalIndex);
        
        this.thumbRendered = true;
        Sbi.trace("[Sbi.SliderField.afterRender] : [" + this.name + "] : OUT");
    },
    
    refreshOptions : function() {
    	Sbi.trace("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : IN");
    	var recordNo = this.store.getTotalCount();
    	Sbi.debug("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : loaded store contains [" + recordNo + "] records");
    	Sbi.debug("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : slider alredy rendered [" + this.slider.rendered + "]");
    	//this.slider.setMaxValue(recordNo-1); // first record index is 0, last is recordNo-1
    	this.slider.maxValue = recordNo-1;
    	Sbi.trace("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : maxIndex set equal to: " + (recordNo-1));
    	this.originalIndex = this.multiSelect===true? [0,this.slider.maxValue]: [0]; 
    	Sbi.trace("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : originalIndex set equal to: " + this.originalIndex);
    	this.storeLoaded = true;
    	
    	if(this.bufferedValues) {
    		Sbi.trace("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : set buffered value: [" + this.bufferedValues + "]");
    		this.doSetValue(this.bufferedValues, false);
    		delete this.bufferedValues;
    	} else {
    		Sbi.trace("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : indexes (pre): " + this.getIndexes());
        	this.setIndexes(this.originalIndex, false);
        	Sbi.trace("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : indexes (post): " + this.getIndexes());    
    	}
    	
    	Sbi.debug("[Sbi.SliderField.refreshOptions] : [" + this.name + "] : OUT");
    },
    
    reset : function (){
    	Sbi.trace("[Sbi.SliderField.reset] : [" + this.name + "] :  IN");
    	if(this.bufferedValues) delete this.bufferedValues;
        this.setIndexes(this.originalIndex, false);
        this.clearInvalid();
        this.reseted = true;
        Sbi.trace("[Sbi.SliderField.reset] : [" + this.name + "] : OUT");
    },
    
    
   
    
	/**
     * Sets the value for this field.
     * @param {Number} v The new value.
     * @param {Boolean} animate (optional) Whether to animate the transition. If not specified, it will default to the animate config.
     * @return {Ext.form.SliderField} this
     */
    setIndexes : function(v, animate, silent){
    	Sbi.trace("[Sbi.SliderField.setIndexes] : [" + this.name + "] :  IN");
    	
    	Sbi.debug("[Sbi.SliderField.setIndex] : [" + this.name + "] :  set value to [" + v + "]");
    	
    	if(v === "" || v === undefined) { // it's a reset...
        	v = [this.slider.minValue];
        	if(this.multiSelect == true){
        		v.push(this.slider.maxValue);
        	}
        }        
        if(!Ext.isArray(v)) {
        	v = [v];
        }
        
        // silent is used if the setValue method is invoked by the slider
        // which means we don't need to set the value on the slider.
        if(!silent){
        	this.slider.setValues(v, animate);
        }
        
    	Sbi.trace("[Sbi.SliderField.setIndexes] : [" + this.name + "] : OUT");
    	return this;
    },
    
    
    // private
    normalizeValue: function(v) {
    	if(v === "" || v === undefined) { // it's a reset...
        	v = [this.slider.minValue];
        	if(this.multiSelect == true){
        		v.push(this.slider.maxValue);
        	}
        }        
        //if(!Ext.isArray(v)) {
    	//v = [v];
        //}
        
    	var isArray = Ext.isArray(v);
   
        
    	if(!isArray){
    	 	var isNumber = !isNaN(v);
    		if(isNumber){
    			v = [v];
    		}
    		else {
    			// try to convert
    			var array = v.split(",");
    			if(!Ext.isArray(array)){
    				v = [v];
    			}
    			else{
    				v = array;
    			}
    		}
    	}
        
        return v;
    },
    
    isReset: function(v) {
    	return (v === undefined || v === null);
    },
    
    /**
     * Sets the value for this field.
     * @param {Number} v The new value.
     * @param {Boolean} animate (optional) Whether to animate the transition. If not specified, it will default to the animate config.
     * @return {Ext.form.SliderField} this
     */
    setValue : function(v, animate, silent){
    	Sbi.trace("[Sbi.SliderField.setValue] : [" + this.name + "] : IN");
    	
    	if(this.isReset(v) === true) {
    		Sbi.debug("[Sbi.SliderField.setValue] : [" + this.name + "] : set field to value [" + v + "] is equal to do a field reset");
    		this.reset();
    		return;
    	}
    	Sbi.debug("[Sbi.SliderField.setValue] : [" + this.name + "] : set value to [" + v + "]");
    	
    	v = this.normalizeValue(v);
         
    	// silent is used if the setValue method is invoked by the slider
        // which means we don't need to set the value on the slider.
        if(!silent){	
        	if(!this.storeLoaded) {
        		Sbi.warn("[Sbi.SliderField.setValue] : [" + this.name + "] : datastore not yet loaded. The valu will be buffered");
        		this.bufferedValues = v;
        	} else {
        		this.doSetValue(v);
        	} 		
        	
        } else {
        	v[0] = this.store.getAt(v[0]);
        	if(this.multiSelect == true) {
        		v[v.length-1] = this.store.getAt(v[v.length-1]);
        	}
        }
       
        this.reseted = false;
        this.fireEvent('change', this, v);
        
    	Sbi.trace("[Sbi.SliderField.setValue] : [" + this.name + "] : OUT");
    	
        return this;
    },
    
    doSetValue : function(v, animate) {
    	
    	this.slider.fireChangeEvent = false;
    	
    	var index;
        index = this.store.find(this.valueField, v[0]);
        if(index === -1) {
        	Sbi.warn("[Sbi.SliderField.doSetValue] : [" + this.name + "] : value [" + v[0] + "] is not contained in the dataset (store loaded: " + this.storeLoaded + "; store size: " + (this.storeLoaded?this.store.getTotalCount(): "-") + " )");
        } else {
        	Sbi.trace("[Sbi.SliderField.doSetValue] : [" + this.name + "] : index of value [" + v[0] + "] is equal to [" + index + "]");
        	this.slider.setValue(0, index, animate);
        }
        
        if(this.multiSelect == true) {
        	index = this.store.find(this.valueField, v[v.length-1]);
        	if(index === -1) {
        		Sbi.warn("[Sbi.SliderField.doSetValue] : [" + this.name + "] : value [" + v[v.length-1] + "] is not contained in the dataset (store loaded: " + this.storeLoaded + "; store size: " + (this.storeLoaded?this.store.getTotalCount(): "-")+ " )");
        		        } else {
            	Sbi.trace("[Sbi.SliderField.doSetValue] : [" + this.name + "] : index of value [" + v[v.length-1] + "] is equal to [" + index + "]");
            	this.slider.setValue(1, index, animate);
            }
        }
        
        this.slider.fireChangeEvent = true;
    },
    
    /**
     * Gets the current value for this field.
     * @return {Number} The current value.
     */
    getIndexes : function(){
        return this.slider.getValues();    
    },
       
    /**
     * Gets the current value for this field.
     * @return {Number} The current value.
     */
    getValue : function(){
    	Sbi.trace("[Sbi.SliderField.getValue] : [" + this.name + "] : IN");
    	var values = this.getValues();
    	var value = undefined;
    	if(values) {
    		value = this.multiSelect == true? values: values[0];
    	}
    	Sbi.trace("[Sbi.SliderField.getValue] : [" + this.name + "] : value is equal to " + value + "");
        
    	Sbi.trace("[Sbi.SliderField.getValue] : [" + this.name + "] : OUT");
        return value;
    },
    
    getValues : function() {
    	Sbi.trace("[Sbi.SliderField.getValues] : [" + this.name + "] : IN");
    	
    	if(this.reseted && this.useUndefinedWhenReseted ) return undefined;
    	
    	if(this.storeLoaded == false) {
    		if(this.bufferedValues) {
    			Sbi.trace("[Sbi.SliderField.getValues] : Store has not be loaded yet. The buffered value [" + this.bufferedValues + "] will be returned");
    			return this.bufferedValues;
    		} else {
    			Sbi.trace("[Sbi.SliderField.getValues] : Store has not be loaded yet and there is no buffered value set to return");
    			return undefined;
    		}
    	}
    	
    	var records;
    	var indexes = this.getIndexes();
    	if(this.multiSelect == true) {
    		records = this.store.getRange(indexes[0], indexes[1]);
    	} else {
    		records = [this.store.getAt(indexes[0])];
    	}
    	Sbi.trace("[Sbi.SliderField.getValues] : [" + this.name + "] : Selected values numeber is equal to [" + records.length + "]");
    	
    	var values = [];
    	for(var i  = 0; i < records.length; i++) {
    		var record = records[i];
    		values.push( record.get(this.valueField) );
    	}
	
    	Sbi.trace("[Sbi.SliderField.getValues] : [" + this.name + "] : OUT");
    	
    	return values;
    },
    
    getRawValue : function() {
    	var values = this.getValues();
		return values? values.join() : values;
	}

});
