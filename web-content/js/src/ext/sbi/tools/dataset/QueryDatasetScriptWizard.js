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

Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.QueryDatasetScriptWizard = function(config) {
	
	var c = Ext.apply({
		// set default values here
	}, config || {});
	
	var panels = this.initPanels(config);
	c = Ext.apply(c, {
		title: 'Edit script...',
        closable:true,
        width:600,
        height:350,
        //border:false,
        plain:true,
        layout: 'border',
        items: panels,
        buttons: [{
        	text:'Submit',
        	disabled:false,
        	handler: function(){
        		//alert(sField.getValue());
        		this.fireEvent('save', this, this.scriptField.getValue(), this.languageField.getValue());
        		this.close();
        	},
        	scope: this
        },{
        	text: 'Close',
        	disabled:false,
        	handler: function(){
        		this.close();
        	},
        	scope: this
        }]
    });
	
	// constructor
	Sbi.tools.dataset.QueryDatasetScriptWizard.superclass.constructor.call(this, c);
    
    this.addEvents('save');
};

Ext.extend(Sbi.tools.dataset.QueryDatasetScriptWizard, Ext.Window, {
    
	languagePanel: null
	, mainPanel: null
	, executionPanel: null
	, palettePanel: null
	
	, scriptLanguagesStore: null
	, languageField : null
	, scriptField: null
	, queryField: null
	
	, initPanels: function(config) {
		var panels = new Array();
		
		this.initLanguagePanel(config);
		panels.push(this.languagePanel);
		
		this.initMainPanel(config);
		panels.push(this.mainPanel);
		
		/*
		this.initExecutionPanel(config);
		panels.push(this.executionPanel);
		
		this.initPalettePanel(config);
		panels.push(this.palettePanel);
		*/
		
		return panels;
	}
	
	, initLanguagePanel: function(config) {
		this.scriptLanguagesStore = new Ext.data.SimpleStore({
			fields : [ 'scriptLanguage' ,'name'],
			data : config.languages,
			autoLoad : false
		});
		
		this.languageField = new Ext.form.ComboBox({
			store : this.scriptLanguagesStore,
			fieldLabel : 'Language',
			displayField : 'name', 
			valueField : 'scriptLanguage', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			editable : false,
			allowBlank : false,
			validationEvent : true,
			xtype : 'combo'
		});
		if(config.language)  this.languageField.setValue(config.language);
		
		this.languagePanel = new Ext.FormPanel({
			region: 'north',
			//labelWidth: 200, 
	        frame:false,
	        bodyStyle:'padding:3px 3px 0',
	        height: 32,
	        width: 300,
	        //layout: 'fit',
	        margins:'3 3 0 3', 
	        items: [this.languageField]
	    });
	}

	, initMainPanel: function(config) {
		
		this.scriptField = new Ext.form.TextArea({
			maxLength : 30000,
			xtype : 'textarea',
			validationEvent : true,
			allowBlank : true
		});
		if(config.script) this.scriptField.setValue(config.script);
		
		var scriptEditorPanel = new Ext.Panel({
            title: 'Script',
            layout: 'fit',
            items: [this.scriptField]
        });
		
		this.queryField = new Ext.form.TextArea({
			maxLength : 30000,
			xtype : 'textarea',
			validationEvent : true,
			allowBlank : true,
			readOnly: true
		});
		if(config.query) this.queryField.setValue(config.query);
	
		var queryEditorPanel = new Ext.Panel({
            title: 'Query',
            layout: 'fit',
            items: [this.queryField]
        });
	
        this.mainPanel = new Ext.TabPanel({
            region: 'center',
            margins:'3 3 3 0', 
            activeTab: 0,
            tabPosition: 'bottom',
            defaults:{autoScroll:true},

            items:[scriptEditorPanel, queryEditorPanel]
        });
	}
	
	, palettePanel: function(config) {
		
        this.palettePanel = new Ext.Panel({
            title: 'Palette',
            region: 'west',
            split: true,
            width: 200,
            collapsible: true,
            margins:'3 0 3 3',
            cmargins:'3 3 3 3'
        });
    }
	
	, executionPanel: function(config) {
	
        this.executionPanel = new Ext.Panel({
            title: 'Test',
            region: 'south',
            split: true,
            height: 100,
            collapsible: true,
            collapsed: true,
            margins:'0 3 3 3',
            cmargins:'3 3 3 3'
        });
    }
});