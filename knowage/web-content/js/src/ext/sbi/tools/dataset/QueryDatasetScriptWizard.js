/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
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