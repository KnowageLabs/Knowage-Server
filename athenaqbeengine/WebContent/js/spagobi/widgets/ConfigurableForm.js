/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * Sbi.widgets.ConfigurableForm
  * 
  * This class is a configurable form: you can define which inputs should be displayed.
  * An example of constructor object:
  * 
  * 		    var formConfig = {
		    	title : 'Test'
		    	, configuredItems : 
		    		[
		    		 {
		    			 type : 'manual'
		    			 , name : 'manual'
		    			 , label : 'Manual'
		    		 }
		    		 , {
		    			 type : 'combobox'
		    			 , name : 'combobox'
		    			 , label : 'Combobox'
	    			     , data : [
	    			 	        	['value1', 'description 1'],
	    			 	       		['value2', 'description 2'],
	    			 	      		['value3', 'description 3'],
	    			 	     		['value4', 'description 4'],
	    			 	    		['value5', 'description 5'],
	    			 	   			['value6', 'description 6']
	    			 	     ] 
		    		 }
		    		 , {
		    			 type : 'checkbox'
		    			 , name : 'checkbox'
		    			 , label : 'Checkbox'
		    		 }
		    		 , {
		    			 type : 'radiogroup'
		    			 , name : 'radiogroup'
		    			 , label : 'Radiogroup'
		    			 , items: [
	    		                {boxLabel: 'Item 1', inputValue: "1"},
	    		                {boxLabel: 'Item 2', inputValue: "2"},
	    		                {boxLabel: 'Item 3', inputValue: "3"}
		    			     ]
		    		 }
		    		 , {
		    			 type : 'checkboxgroup'
		    			 , name : 'checkboxgroup'
		    			 , label : 'Checkboxgroup'
		    			 , items: [
	    		                {boxLabel: 'Item 1', inputValue: "1"},
	    		                {boxLabel: 'Item 2', inputValue: "2"},
	    		                {boxLabel: 'Item 3', inputValue: "3"}
		    			     ]
		    		 }
		    		]
		    };
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

Ext.ns("Sbi.widgets");

Sbi.widgets.ConfigurableForm = function(config) {

	var defaultSettings = {
		superBoxSelectValueDelimiter: '--!;;;;!--' // values delimiter for SuperBoxSelect
		, fieldLabelWidth: 200
	};
		
	if (Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.configurableForm) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.widgets.configurableForm);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	this.baseConfig = c;
	
	this.init();
	
	c = Ext.apply(c, {
		items : this.formItems
		, labelWidth : this.baseConfig.fieldLabelWidth + 'px;' // THIS DOES NOT WORK!!!
	});
	
	// constructor
    Sbi.widgets.ConfigurableForm.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.widgets.ConfigurableForm, Ext.form.FormPanel, {

	configuredItems : null 	// this contains the configuration of the inputs (type, name, values ....)
	, formItems : null			// contains the ExtJS objects corresponding to the form input
	, superBoxSelectValueDelimiter : null // values delimiter for SuperBoxSelect
	
	,
	init : function () {
		this.formItems = [];
		for (var i = 0; i < this.configuredItems.length; i++) {
			var aConfiguredItem = this.configuredItems[i];
			var anItem = null;
			switch (aConfiguredItem.type) {
				case "manual" : 
					anItem = this.createManualInput(aConfiguredItem);
					break;
				case "combobox" :
					anItem = this.createCombobox(aConfiguredItem);
					break;
				case "radiogroup" :
					anItem = this.createRadioGroup(aConfiguredItem);
					break;
				case "checkbox" :
					anItem = this.createCheckbox(aConfiguredItem);
					break;
				case "checkboxgroup" :
					anItem = this.createCheckboxGroup(aConfiguredItem);
					break;
			}
			this.formItems.push(anItem);
		}
	}
	
	/*
	 * The combination of the following methods does not properly work:
	 * - for single check boxes: if the check box is initially not checked and it is leaved unchanged, 
	 * the get method does NOT return {checkBoxName : 'off'} in the returned object, therefore when using the set method with the same object, 
	 * if the check box is checked, it is leaved unchanged (in checked state);
	 * - for check box groups the set method does not work properly.
	,
	getFormState : function () {
		var state = this.getForm().getValues();
		return state;
	}
	,
	setFormState : function (state) {
		this.getForm().setValues(state);
	}
	*/
	
	/*
	 * Returns the form state. Example of state:
	 * {manual:"", combobox:[], checkbox:false, radiogroup:null, checkboxgroup:[]}
	 * {manual:"hello", combobox:["value1", "value4"], checkbox:true, radiogroup:"2", checkboxgroup:["1", "3"]}
	 */
	,
	getFormState : function () {
		var state = {};
		for (var i = 0; i < this.formItems.length; i++) {
			var aFormItem = this.formItems[i];
			var name = aFormItem.getName();
			var value = null;
			if (aFormItem instanceof Ext.ux.form.SuperBoxSelect) {
				var concatenatedValues = aFormItem.getValue();
				if (concatenatedValues == '') {
					value = [];
				} else {
					value = concatenatedValues.split(this.superBoxSelectValueDelimiter);
				}
			} else if (aFormItem instanceof Ext.form.RadioGroup) {
				var theRadio = aFormItem.getValue();
				if (theRadio != null) {
					value = theRadio.getGroupValue();
				}
			} else if (aFormItem instanceof Ext.form.CheckboxGroup) {
				var checkboxes = aFormItem.getValue();
				value = [];
				for (var j = 0; j < checkboxes.length; j++) {
					value.push(checkboxes[j].getEl().getValue());
				}
			} else {
				value = aFormItem.getValue();
			}
			state[name] = value;
		}
		return state;
	}

	/*
	 * Sets the form state. Example of input state:
	 * {manual:"", combobox:[], checkbox:false, radiogroup:null, checkboxgroup:[]}
	 * {manual:"hello", combobox:["value1", "value4"], checkbox:true, radiogroup:"2", checkboxgroup:["1", "3"]}
	 * Each value of a CheckboxGroup cannot contain ",".
	 */
	,
	setFormState : function (state) {
		for (var i = 0; i < this.formItems.length; i++) {
			var aFormItem = this.formItems[i];
			var name = aFormItem.getName();
			var value = state[name];
			if (aFormItem instanceof Ext.form.RadioGroup) {
				aFormItem.setValue(value); // needed since Ext.form.CheckboxGroup extends Ext.form.RadioGroup
			} else if (aFormItem instanceof Ext.form.CheckboxGroup) {
				aFormItem.reset();
				var concatenatedValues = value.join(',');
				aFormItem.setValue(concatenatedValues);
			} else {
				aFormItem.setValue(value);
			}
		}
	}
	
	,
	createManualInput : function (aConfiguredItem) {
		return new Ext.form.TextField({
			fieldLabel : LN(aConfiguredItem.label)
			, name : aConfiguredItem.name
			, allowBlank : true
		});
	}
	
	,
	createCombobox : function (aConfiguredItem) {
		var store = new Ext.data.SimpleStore({
			 fields: ['value', 'description']
		     , data :  aConfiguredItem.data
		 });
		
		return new Ext.ux.form.SuperBoxSelect({
			fieldLabel: LN(aConfiguredItem.label)
			, name : aConfiguredItem.name
		    , store: store
		    , displayField: 'description'
		    , valueField: 'value'
		    , mode : 'local'
		    , maxSelection: aConfiguredItem.maxSelectionNumber
		    , valueDelimiter: this.superBoxSelectValueDelimiter
		});
		
	}
	
	,
	createRadioGroup : function (aConfiguredItem) {
		var items = [];
		for (var i = 0; i < aConfiguredItem.items.length; i++) {
			var anItem = aConfiguredItem.items[i];
			var anItemWithName = Ext.apply({name : aConfiguredItem.name}, anItem);
			items.push(anItemWithName);
		}
		
		return new Ext.form.RadioGroup({
			name : aConfiguredItem.name
			, fieldLabel: aConfiguredItem.label
            , columns: 1
            , items: items
		});
	}
	
	,
	createCheckbox : function (aConfiguredItem) {
		return new Ext.form.Checkbox({
			fieldLabel: LN(aConfiguredItem.label)
			, name : aConfiguredItem.name
		});
	}

	,
	createCheckboxGroup : function (aConfiguredItem) {
		var items = [];
		for (var i = 0; i < aConfiguredItem.items.length; i++) {
			var anItem = aConfiguredItem.items[i];
			var anItemWithName = Ext.apply({name : aConfiguredItem.name}, anItem);
			items.push(anItemWithName);
		}
		
		return new Ext.form.CheckboxGroup({
			name : aConfiguredItem.name
			, fieldLabel: aConfiguredItem.label
            , columns: 1
            , items: items
		});
	}

	,
	debug : function () {
		var win = new Ext.Window({
			html : this.getFormState().toSource()
		});
		win.show();
	}
	
});