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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.FormBuilderPage = function(config) {
	
	var defaultSettings = {
		//title: LN('sbi.formbuilder.formbuilderpage.title')
	};
		
	if(Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.formBuilderPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.formBuilderPage);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
		
	
	this.services = this.services || new Array();	
	this.services['saveFormState'] = this.services['saveFormState'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_FORM_ACTION'
		, baseParams: {}
	});
	
	//this.addEvents('customEvents');
		
		
	this.initQueryFieldsPanel();
	this.initTemplateEditorPanel();
		
	this.toolbar = new Ext.Toolbar({
		items: [
		    '->'
		    , {
				text: LN('sbi.formbuilder.formbuilderpage.toolbar.save'),
				handler: function() {
		    		this.validateTemplate(this.saveTemplate, function(errors) {
		        		var message = errors.join('<br>');
		        		Sbi.exception.ExceptionHandler.showErrorMessage(message, LN('sbi.formbuilder.formbuilderpage.validationerrors.title'));
		    		}, this);
		    	},
				scope: this
		    }
		  ]
	});
	
	c = Ext.apply(c, {
		title: this.title,
		border: true,
		//bodyStyle:'background:green',
		style:'padding:3px',
      	layout: 'border',
      	tbar: this.toolbar,
      	items: [this.queryFieldsPanel, this.templateEditorPanel]
	});

	// constructor
	Sbi.formbuilder.FormBuilderPage.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.FormBuilderPage, Ext.Panel, {
    
    services: null
    , queryFieldsPanel: null
    , templateEditorPanel: null
   
   
    // public methods
    
    , initQueryFieldsPanel: function() {
    	
		this.queryFieldsPanel = new Sbi.formbuilder.QueryFieldsPanel({
			region:'west',
			split: true, 
			layout:'fit',
			border: false,
	        width:250,
	        //margins: '5 5 5 5',
	        collapsible: true,
	        collapseFirst: false,
	        gridConfig: {ddGroup: 'formbuilderDDGroup'}
	        //bodyStyle:'background:red',
		});
    }
    
    , initTemplateEditorPanel: function() {
    	this.templateEditorPanel = new Sbi.formbuilder.TemplateEditorPanel({
    		region:'center',
	        autoScroll: true,
			containerScroll: true,
			layout: 'fit',
			border: false,
			template: this.template
    	});    	
    }
    
    , validateTemplate: function(successHandler, failureHandler, scope) {
    	var errors = [];
    	
    	errors = errors.concat(this.templateEditorPanel.groupingVariablesPanel.getErrors());
    	
		if (errors.length == 0 && successHandler !== undefined) {
			successHandler.call(scope || this);
		}
		if (errors.length > 0 && failureHandler !== undefined) {
			failureHandler.call(scope || this, errors);
		}
    }
    
    , saveTemplate: function() {
		var params = {
				"FORM_STATE": Sbi.commons.JSON.encode(this.templateEditorPanel.getContents())
		};
		Ext.Ajax.request({
		    url: this.services['saveFormState'],
		    success: function() {
	    		Ext.Msg.show({
 				   title: LN('sbi.formbuilder.formbuilderpage.templatesaved.title'),
 				   msg: LN('sbi.formbuilder.formbuilderpage.templatesaved.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
	 			});
			},
		    failure: Sbi.exception.ExceptionHandler.handleFailure,
		    scope: this,
		    params: params
		});  
    }
});