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
  * - by Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.StaticCloseFilterGroupWizard = function(config) {
	
	var defaultSettings = {
		// set default values here
		title: LN('sbi.formbuilder.staticclosefiltergroupwizard.title')
		, autoScroll: true
		, width: 400
		, height: 380
		, baseState: {
			groupTitle: undefined,
			singleSelection: true,
			allowNoSelection: true,
			booleanConnector: 'AND'
		}
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticClosedXORFiltersWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticClosedXORFiltersWindow);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
		layout: 'fit',
		width: this.width,
		height: this.height,
		closeAction:'hide',
		plain: true,
		modal: true,
		resizable: true,
		title: this.title,
		items: [this.formPanel]
	});
	
	// constructor
    Sbi.formbuilder.StaticCloseFilterGroupWizard.superclass.constructor.call(this, c);
    
    if(this.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	
    this.addEvents('apply');
    
};

Ext.extend(Sbi.formbuilder.StaticCloseFilterGroupWizard, Ext.Window, {

	formPanel: null
	, groupTitleField: null
	, singleSelectionField: null
	, allowNoSelectionField: null
	, noSelectionTextField: null
	, booleanConnectorField: null
	, baseState: null
	, targetFilterGroup: null
	, hasBuddy: null
    , buddy: null
	
	
	// --------------------------------------------------------------------------
	// Public
	// --------------------------------------------------------------------------
	
	, getFormState : function () {
		var s = {};
		s.groupTitle = this.groupTitleField.getValue(s);
		s.singleSelection = this.singleSelectionField['true'].getValue()
		s.allowNoSelection = this.allowNoSelectionField['true'].getValue();  
		s.noSelectionText = this.noSelectionTextField.getValue();
		s.booleanConnector = this.booleanConnectorField['AND'].getValue() ? 'AND' : 'OR';
		return s;
	}

	, setFormState: function(s) {
		
		if(s.title !== undefined) {
			this.groupTitleField.setValue(s.title);
		} else {
			this.groupTitleField.setValue('');
			this.groupTitleField.clearInvalid();
		}
		
		if(s.singleSelection !== undefined) {
			this.singleSelectionField['true'].setValue(s.singleSelection === true);
			this.singleSelectionField['false'].setValue(s.singleSelection === false);
		} else {
			this.singleSelectionField['true'].setValue(true);
			this.singleSelectionField['false'].setValue(false);
		}
		
		if(s.allowNoSelection !== undefined) {
			this.allowNoSelectionField['true'].setValue(s.allowNoSelection === true);
			this.allowNoSelectionField['false'].setValue(s.allowNoSelection === false);
		} else {
			this.allowNoSelectionField['true'].setValue(true);
			this.allowNoSelectionField['false'].setValue(false);
		}
				
		if (s.noSelectionText !== undefined) {
			this.noSelectionTextField.setValue(s.noSelectionText);
		} else {
			this.noSelectionTextField.setValue('');
		}
		
		if(s.booleanConnector !== undefined) {
			this.booleanConnectorField['AND'].setValue(s.booleanConnector === 'AND');
			this.booleanConnectorField['OR'].setValue(s.booleanConnector === 'OR');
		} else {
			this.booleanConnectorField['AND'].setValue(true);
			this.booleanConnectorField['OR'].setValue(false);
		}
	}
	
	, resetFormState: function() {
		this.groupTitleField.reset();
		this.groupTitleField.clearInvalid();
		
		var s = Ext.apply(this.baseState || {}, {
			singleSelection: true
			, allowNoSelection: true
			, noSelectionText: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.noselectiontext')
			, booleanConnector: 'AND'
		})
		this.setFormState(s);
	}
	
	, setTarget: function(targetFilterGroup) {
		this.targetFilterGroup = targetFilterGroup;
		
		if(this.targetFilterGroup === null) {
			this.resetFormState();
		} else {
			this.setFormState(this.targetFilterGroup.getContents());
		}
	}
	
	, getTarget: function() {
		return this.targetFilterGroup;
	}
	
	
	// --------------------------------------------------------------------------
	// Private
	// --------------------------------------------------------------------------
	
	, init: function() {
		var items = [];
		
		this.groupTitleField = new Ext.form.TextField({
			fieldLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.grouptitle.label'),
    		name:'groupTitle',
    		value: this.baseState.groupTitle,
    		allowBlank: false, 
    		inputType:'text',
    		maxLength:150,
    		style:'margin-bottom:10px;'
    	});
    	items.push(this.groupTitleField);
    	
    	    	
    	this.singleSelectionField = {};
    	
    	this.singleSelectionField['true'] = new Ext.form.Radio({
    		fieldLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.enablesingleselection.label'),
			boxLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.enablesingleselection.yes'),
			name: 'singleSelection',
			checked: (this.baseState.singleSelection === true),
			inputValue: true
		});
    	//items.push(this.singleSelectionField['true']);
    	this.singleSelectionField['true'].on('check', function(cb, checked){
    		if(checked === true){
    			this.enableNoSelectionField(true);
    		}
    	}, this);
    	
    	
    	this.singleSelectionField['false'] = new Ext.form.Radio({
    		fieldLabel: '',
    		labelSeparator: '',
			boxLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.enablesingleselection.no'),
			name: 'singleSelection',
			checked: (this.baseState.singleSelection === false),
            inputValue: false
            //style:'margin-bottom:20px;'
		});
    	//items.push(this.singleSelectionField['fasle']);
    	this.singleSelectionField['false'].on('check', function(cb, checked){
    		if(checked === true){
    			this.enableNoSelectionField(false);
    		}
    	}, this);
    	
    	
    	this.allowNoSelectionField = {};
    	
    	this.allowNoSelectionField['true'] = new Ext.form.Radio({
    		fieldLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.allownoselection.label'),
			boxLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.allownoselection.yes'),
			name: 'allowNoSelectionField',
			checked: (this.baseState.allowNoSelection === true),
			inputValue: true,
			disabled: (this.baseState.singleSelection === false)
		});
    	//items.push(this.allowNoSelectionField['true']);
    	
    	this.allowNoSelectionField['false'] = new Ext.form.Radio({
    		fieldLabel: '',
    		labelSeparator: '',
			boxLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.allownoselection.no'),
			name: 'allowNoSelectionField',
			checked: (this.baseState.allowNoSelection === false),
            inputValue: false,
            disabled: (this.baseState.singleSelection === false)
		});
    	//items.push(this.allowNoSelectionField['false']);
    		
    	this.noSelectionTextField = new Ext.form.TextField({
			fieldLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.noselectionoptionlabel.label'),
    		name:'noSelectionTextField',
    		value: this.baseState.noSelectionTextField || LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.noselectiontext'),
    		allowBlank: true, 
    		inputType:'text',
    		maxLength:150
    		//, style:'margin-bottom:10px;'
    	});
    	    	
    	this.booleanConnectorField = {};
    	
    	this.booleanConnectorField['AND'] = new Ext.form.Radio({
    		fieldLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.booleanconnector.label'),
			boxLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.booleanconnector.and'),
			name: 'booleanConnector',
			checked: (this.baseState.booleanConnector === 'AND'),
			inputValue: true,
			disabled: (this.baseState.singleSelection === true)
		});
    	
    	this.booleanConnectorField['OR'] = new Ext.form.Radio({
    		fieldLabel: '',
    		labelSeparator: '',
			boxLabel: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.booleanconnector.or'),
			name: 'booleanConnector',
			checked: (this.baseState.booleanConnector === 'OR'),
            inputValue: false,
            disabled: (this.baseState.singleSelection === true)
		});
    	
    	var fieldSet = new Ext.form.FieldSet({
    		title: LN('sbi.formbuilder.staticclosefiltergroupwizard.fields.options'),
    		autoHeight: true,
    		autoWidth: true,
    		labelWidth: 150,
    		items: [
    		   this.singleSelectionField['true'], this.singleSelectionField['false'], 
    		   this.allowNoSelectionField['true'], this.allowNoSelectionField['false'],
    		   this.noSelectionTextField,
    		   this.booleanConnectorField['AND'], this.booleanConnectorField['OR']
    		]
    	});
    	items.push(fieldSet);
    	
    	this.formPanel = new Ext.form.FormPanel({
    		monitorValid:true,
    		frame:true,
    		labelWidth: 80,
    		defaults: {
    			width: 225
    		},
    	    bodyStyle:'padding:5px 5px 0',
    	    buttonAlign : 'center',
    	    items: items,
    	    buttons: [{
    			text: LN('sbi.formbuilder.staticclosefiltergroupwizard.buttons.apply'),
    			formBind:true,
    		    handler: function(){
    	    		this.fireEvent('apply', this, this.getTarget(), this.getFormState());
                	this.hide();
            	}
            	, scope: this
    	    },{
    		    text: LN('sbi.formbuilder.staticclosefiltergroupwizard.buttons.cancel'),
    		    handler: function(){
                	this.hide();
            	}
            	, scope: this
    		}]
    	 });
	}
	
	, enableNoSelectionField: function(enable) {
		if(enable === true) {
			this.allowNoSelectionField['true'].enable();
			this.allowNoSelectionField['false'].enable();
			this.noSelectionTextField.enable();
			this.booleanConnectorField['AND'].disable();
			this.booleanConnectorField['OR'].disable();
		} else {
			this.allowNoSelectionField['true'].disable();
			this.allowNoSelectionField['false'].disable();
			this.noSelectionTextField.disable();
			this.booleanConnectorField['AND'].enable();
			this.booleanConnectorField['OR'].enable();
		}
	}
});