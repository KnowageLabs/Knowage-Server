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
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.OperandsWindow = function(config) {

	var c = Ext.apply({}, config || {}, {
		title: LN('sbi.qbe.calculatedFields.operands.title.text')
		, width: 500
		, height: 300
		, hasBuddy: false	
		, modal: true
	});

	Ext.apply(this, c);
		
	this.initFormPanel();
	
	this.closeButton = new Ext.Button({
		text: LN('sbi.qbe.calculatedFields.buttons.text.cancel'),
		handler: function(){
        	this.destroy();
        }
        , scope: this
	});
	
	this.okButton = new Ext.Button({
		text: LN('sbi.qbe.calculatedFields.buttons.text.ok'),
		handler: function(){
        	//this.hide();			
        	this.fireEvent('click', this, this.getFormState());
        	this.close();
        }
        , scope: this
	});
	
	

	c = Ext.apply(c, {  	
		layout: 'fit'
	//,	closeAction:'hide'
	,	closeAction:'close'
	,	plain: true
	,	modal:true
	,	title: this.title
	,	buttonAlign : 'center'
	,	buttons: [this.okButton, this.closeButton]
	,	items: [this.formPanel]
	});

	// constructor
	Sbi.qbe.OperandsWindow.superclass.constructor.call(this, c);
	
	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	
	this.addEvents('click');
    
};

Ext.extend(Sbi.qbe.OperandsWindow, Ext.Window, {

    serviceName: null
    , formPanel: null
    
   , fieldMap: null
   , okButton: null
   , closeButton: null
   , dateFormat: null
    
    // public methods

    
    // private methods

    , initFormPanel: function() {	
		var fields = [];
		this.fieldMap = {};
		
		for(var i = 1, l = this.operands.length; i <= l; i++) { 
			var operand = this.operands[i-1];
			var tmpLabel = operand.label || "Parameter " + i;
			var tmpName = "op" + i;
			var tmpField = null;
			
			tmpField = this.createParameterField(tmpLabel, tmpName);
    		fields.push(tmpField);
			this.fieldMap[tmpName] = tmpField;			
        } //for
	
    	this.formPanel = new  Ext.FormPanel({
    		  //title:  LN('sbi.console.promptables.title'),
    		  margins: '50 50 50 50',
	          labelAlign: 'left',
	          bodyStyle:'padding:5px',
	          autoScroll:true,
	          width: 850,
	          height: 600,
	          labelWidth: 150,
	          layout: 'form',
	          trackResetOnLoad: true,
	          items: fields
	      }); 
    }

	, getFormState: function() {
    	var state = {};
    	for(f in this.fieldMap) {
			//sets the current value
			state[f] = this.fieldMap[f].getValue();
    	}
    	state = this.completeReturnedValue(state);
    	return state;
    }

	, createComboField: function(label, name){
		
		var tmpStore = null; 
		var tmpValueField = 'column_1';
		var tmpValueText = 'column_2';
		
		tmpStore = this.createStore(this.fields.childNodes);
				
		var field = new Ext.form.ComboBox({
		    fieldLabel: label,
		    name: name,
            width: 250,    	    
            store: tmpStore,
	        valueField: tmpValueField,	
	        displayField: tmpValueText,
	        mode : 'local',
	        typeAhead: true,
	        emptyText:'Select ...',
	        selectOnFocus:true,
	        triggerAction: 'all'
		 });        		 
		
		return field;
	}
	
	
	, createParameterField: function (label, name){
		var tmpField = null;
		//combobox     			
		tmpField = this.createComboField(label, name);    		
		return tmpField;
	}
	
	, createStore: function(fields) {
		var store;
		var data = [];
		for (var i=0, l = fields.length; i<l; i++){
			var tmpField = fields[i];
			var tmpData = [];
			tmpData.push(tmpField.attributes.text, tmpField.attributes.text);	
			data.push(tmpData);
		}
		//the store is created by fix values
		store = new Ext.data.SimpleStore({
              fields: ['column_1','column_2']
            , data: data
        });
		
		
		//store.on('exception', Sbi.exception.ExceptionHandler.onStoreLoadException, this);
		
		return store;
		
	}
	
	, completeReturnedValue: function(values){
		var returnValue = this.text;		
		for (var i=1, l=this.operands.length; i<=l; i++){
			returnValue = returnValue.replace('op'+i,values['op'+i]);
		}
		return returnValue;
	}
	
});