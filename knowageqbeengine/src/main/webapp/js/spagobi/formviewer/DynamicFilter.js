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

Ext.ns("Sbi.formviewer");

Sbi.formviewer.DynamicFilter = function(dynamicFilter, config) {
	
	var defaultSettings = {
		// set default values here
		id: dynamicFilter.id
		, autoScroll: true
		, autoWidth: true
        , layout: 'column'
    	, layoutConfig: {
	        columns: dynamicFilter.operator.toUpperCase() === 'BETWEEN' ? 4 : 3
	    }
	};
	if (Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.dynamicFilter) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.dynamicFilter);
	}
	var c = Ext.apply(defaultSettings, config || {});

	this.baseConfig = c;
	
	this.init(dynamicFilter);
	
	Ext.apply(c, {
  		items: this.fields
	});
	
	// constructor
    Sbi.formviewer.DynamicFilter.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.formviewer.DynamicFilter, Ext.form.FormPanel, {
    
	services: null
	, combo: null
	, valuesInputs: null
	, fields: null
	, hiddenField: null
	   
	// private methods
	   
	, init: function(dynamicFilter) {
		this.fields = [];
		
		var aPanel = null;
		if ( this.hasOnlyOneField( dynamicFilter ) ) {
			this.hiddenField = this.getFirstField( dynamicFilter );
			aPanel = new Ext.Panel({
				html: this.getDynamicFilterTitle( dynamicFilter ) + ":"
				, width: 300
				, cls: 'x-form-item'  // to get the same font as the other inputs with options displayed by the combobox
			});
		} else {
			this.combo = this.createFieldCombo( dynamicFilter );
			aPanel = new Ext.Panel({
				items: [this.combo]
				, layout: 'form' // form layout required: input field labels are displayed only with this layout
				, width: 300
			});
		}
		this.fields.push(aPanel);
		
		this.valuesInputs = this.createFieldValuesInput( dynamicFilter );
		if (this.valuesInputs.length == 1) {
			this.fields.push(new Ext.Panel({
				items: [this.valuesInputs[0]]
				, layout: 'form' // form layout required: input field labels are displayed only with this layout
				, width: 300
			}));
		} else {
			this.fields.push(new Ext.Panel({
				items: [this.valuesInputs[0]]
				, layout: 'form' // form layout required: input field labels are displayed only with this layout
				, width: 150
				, labelWidth : 34
			}));
			this.fields.push(new Ext.Panel({
				items: [this.valuesInputs[1]]
				, layout: 'form' // form layout required: input field labels are displayed only with this layout
				, width: 150
				, labelWidth : 34
			}));
		}
		var clearButtonPanel = new Ext.Panel({
			items: [new Ext.Button({
				iconCls: 'icon-clear'
				, tooltip: LN('sbi.formviewer.dynamicfilterspanel.clear.tt')
				, scope: this
				, handler: this.clear
			})]
			, width: 30
		});
		this.fields.push(clearButtonPanel);
	}

	, createFieldCombo: function(dynamicFilter) {
		
		var store = new Ext.data.JsonStore({
			data: [{'field': '', 'text': ''}].concat(dynamicFilter.admissibleFields),
		    fields: ['field', 'text']
		});
		
		var combo = new Ext.form.ComboBox({
			name: 'field'
            , editable: false
            , fieldLabel: this.getDynamicFilterTitle(dynamicFilter)
		    , forceSelection: false
		    , store: store
		    , mode : 'local'
		    , triggerAction: 'all'
		    , displayField: 'text'
		    , valueField: 'field'
		    , emptyText: ''
		});

		return combo;
	}
	
	,
	getDynamicFilterTitle : function ( dynamicFilter ) {
		var fieldLabel = (dynamicFilter.title !== undefined && dynamicFilter.title !== '') ? 
				dynamicFilter.title :
					LN('sbi.formviewer.dynamicfilterspanel.variable');
		return fieldLabel;
	}
	
	, createFieldValuesInput: function(dynamicFilter) {
		var valuesInput = [];
		if (dynamicFilter.operator.toUpperCase() === 'BETWEEN') {
			valuesInput[0] = new Ext.form.TextField({
				fieldLabel: LN('sbi.formviewer.dynamicfilterspanel.fromvalue')
			   , name : 'fromvalue'
			   , allowBlank: true
			   , width: 100
			});
			valuesInput[1] = new Ext.form.TextField({
				fieldLabel: LN('sbi.formviewer.dynamicfilterspanel.tovalue')
			   , name : 'tovalue'
			   , allowBlank: true
			   , width: 100
			});
		} else {
			valuesInput[0] = new Ext.form.TextField({
				fieldLabel: LN('sbi.formviewer.dynamicfilterspanel.value')
			   , name : 'value'
			   , allowBlank: true
			   , width: 290
			   , hideLabel : true
			});
		}
		return valuesInput;
	}

	,
	hasOnlyOneField : function (dynamicFilter) {
		return dynamicFilter.admissibleFields.length == 1;
	}
	
	,
	getFirstField : function (dynamicFilter) {
		return dynamicFilter.admissibleFields[0].field;
	}
	   
	// public methods
	
	, clear: function() {
		if (this.combo != null) {
			this.combo.setValue('');
		}
		for (var i = 0; i < this.valuesInputs.length; i++) {
			var aValueInput = this.valuesInputs[i];
			aValueInput.setValue('');
		}
	}
	
	, getFormState: function() {
		var state = {};
		if (this.combo != null) {
			state.field = this.combo.getValue();
			for (var i = 0; i < this.valuesInputs.length; i++) {
				var aValueInput = this.valuesInputs[i];
				state[aValueInput.name] = aValueInput.getValue();
			}
		} else {
			var atLeastOneValue = false;
			for (var i = 0; i < this.valuesInputs.length; i++) {
				var aValueInput = this.valuesInputs[i];
				var aValue = aValueInput.getValue();
				if (aValue !== null && aValue.trim() !== '') {
					atLeastOneValue = true;
				}
				state[aValueInput.name] = aValueInput.getValue();
			}
			if (atLeastOneValue) {
				state.field = this.hiddenField;
			} else {
				state.field = ''; // in case the filter has no values filled by the user, return an empty field (no values means no filter); 
				// case when the filter should consider an empty string as a valid value is ignored.
			}
		}
		return state;
	}
	
	, setFormState: function(value) {
		if (this.combo != null) {
			this.combo.setValue(value.field);
		}
		this.getForm().setValues(value);
	}
	
    , isValid: function() {
    	if (this.combo != null) {
            if(this.combo.getValue() === '') {
            	return true;
            }
    		for (var i = 0; i < this.valuesInputs.length; i++) {
    			var aValueInput = this.valuesInputs[i];
    			if (aValueInput.getValue() !== null && aValueInput.getValue().trim() === '') {
    				return false;
    			}
    		}
    	}
        return true;
    }
    
    , getValidationErrors: function() {
    	var errors = new Array();
    	if (this.isValid()) {
    		return errors;
    	} else {
    		var error = String.format(LN('sbi.formviewer.dynamicfilter.validation.missingvalue'), this.combo.getRawValue());
    		errors.push(error);
    	}
    	return errors;
    }
  	
});