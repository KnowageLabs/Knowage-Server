/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * SaveWindow - short description
  * 
  * Object documentation ...
  * 
  * by Andrea Gioia (andrea.gioia@eng.it)
  */

Sbi.widgets.SaveWindow = function(config) {	
	
	var c = Ext.apply({}, config || {}, {
		hasBuddy: false
	});
		
	this.initFormPanel(c);
	
	
	// constructor
	Sbi.widgets.SaveWindow.superclass.constructor.call(this, {
		layout:'fit',
		width:500,
		height:250,
		closeAction:'hide',
		plain: true,
		title: LN('sbi.execution.executionpage.toolbar.saveas'),
		items: [this.formPanel]
    });
    
	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	
	this.addEvents('save');
    
    
};

Ext.extend(Sbi.widgets.SaveWindow, Ext.Window, {
    
	nameField: null
	, descriptionField: null
	, scopeField: null
	, hasBuddy: null
    , buddy: null
   
   
    // public methods
	,getFormState : function() {      
    	
      	var formState = {};
      	formState.name= this.nameField.getValue();
      	formState.description= this.descriptionField.getValue();
      	formState.scope= this.scopeField.getValue();
      	
      	return formState;
    }

	//private methods
	, initFormPanel: function(config) {
    	this.nameField = new Ext.form.TextField({
    		name:'analysisName',
    		allowBlank:false, 
    		inputType:'text',
    		maxLength:50,
    		width:250,
    		fieldLabel:LN('sbi.execution.subobjects.name') 
    	});
    	    
    	this.descriptionField = new Ext.form.TextField({
    		name:'analysisDescription',
    		allowBlank:false, 
    		inputType:'text',
    		maxLength:50,
    		width:250,
    		fieldLabel: LN('sbi.execution.subobjects.description') 
    	});
    	    
    	    
    	   
    	var scopeComboBoxData = [
    		['PUBLIC',LN('sbi.execution.subobjects.visibility.public'), LN('sbi.execution.parametersselection.toolbar.save.public.description')],
    		['PRIVATE', LN('sbi.execution.subobjects.visibility.private'), LN('sbi.execution.parametersselection.toolbar.save.private.description')]
    	];
    		
    	var scopeComboBoxStore = new Ext.data.SimpleStore({
    		fields: ['value', 'field', 'description'],
    		data : scopeComboBoxData 
    	});
    		    
    		    
    	this.scopeField = new Ext.form.ComboBox({
    	   	tpl: '<tpl for="."><div ext:qtip="{field}: {description}" class="x-combo-list-item">{field}</div></tpl>',	
    	   	editable  : false,
    	   	fieldLabel : LN('sbi.execution.subobjects.visibility'),
    	   	forceSelection : true,
    	   	mode : 'local',
    	   	name : 'analysisScope',
    	   	store : scopeComboBoxStore,
    	   	displayField:'field',
    	    valueField:'value',
    	    emptyText:LN('sbi.execution.parametersselection.toolbar.save.scope.description'),
    	    typeAhead: true,
    	    triggerAction: 'all',
    	    selectOnFocus:true
    	});
    	
    	this.formPanel = new Ext.form.FormPanel({
    		frame:true,
    	    bodyStyle:'padding:5px 5px 0',
    	    buttonAlign : 'center',
    	    items: [this.nameField,this.descriptionField,this.scopeField],
    	    buttons: [{
    			text: LN('sbi.browser.defaultRole.save'),
    		    handler: function(){
    	    		this.fireEvent('save', this, this.getFormState());
                	this.hide();
            	}
            	, scope: this
    	    },{
    		    text: LN('sbi.browser.defaultRole.cancel'),
    		    handler: function(){
                	this.hide();
            	}
            	, scope: this
    		}]
    	 });
    }
});