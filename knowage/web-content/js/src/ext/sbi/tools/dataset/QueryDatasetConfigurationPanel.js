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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Chiara Chiarelli (chiara.chiarelli@eng.it)
 */
Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.QueryDatasetConfigurationPanel = function(config) {
	
	var c = Ext.apply({
		// set default values here
	}, config || {});
	this.scriptTypes = config.scriptTypes;
	
	this.initFormPanel(config);
	
	c.items = [this.formPanel];
	
	// constructor
	Sbi.tools.dataset.QueryDatasetConfigurationPanel.superclass.constructor.call(this, c);
    
    this.addEvents();
};

Ext.extend(Sbi.tools.dataset.QueryDatasetConfigurationPanel, Ext.Panel, {
   
	formPanel: null
	, datasourceField : null
	, queryField : null
	, script : null
	, language : null
	, scriptButton : null
   
    // public methods
	
	, getFormState: function() {
		var formState = {};
		formState.dataSource = this.datasourceField.getValue();
		formState.query = this.queryField.getValue();
		formState.queryScript = this.script;
		formState.queryScriptLanguage = this.language;
		return formState;
	}

	, setFormState: function(formState) {
		this.datasourceField.setValue(formState.dataSource);
		this.queryField.setValue(formState.query);
		this.script = formState.queryScript;
		this.language = formState.queryScriptLanguage;
		
	}
	
	// private methods
	
	, initFormPanel: function(config) { 
		this.initDatasourceField(config);
		this.initQueryField(config);
				
		this.scriptButton = new Ext.Button ({
            text:'Edit script',
            handler: this.onEditScript,
            scope: this
        });
		
		this.formPanel = new Ext.form.FieldSet({
			labelWidth : 100,
			defaults : {
				border : true
			},
			defaultType : 'textfield',
			autoHeight : true,
			autoScroll : true,
			border : true,
			style : {
				"margin-left" : "3px",
				"margin-top" : "0px",
				"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
						: "-5px")
						: "3px"
			},
			items : [ 
			    this.datasourceField,
				this.queryField,
				this.scriptButton
			]
		});
	}
	
	, initDatasourceField: function(config) {
		this.dataSourceStore = new Ext.data.SimpleStore({
			fields : [ 'dataSource','name' ],
			data : config.dataSourceLabels,
			autoLoad : false
		});
		
		this.datasourceField = new Ext.form.ComboBox({
			name : 'dataSource',
			store : this.dataSourceStore,
			width : 350, //180,
			fieldLabel : LN('sbi.ds.dataSource'),
			displayField : 'dataSource', 
			valueField : 'dataSource',
			typeAhead : true, 
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, editable : false,
			allowBlank : false, validationEvent : true
		});
	}

	, initQueryField: function(config) {
		this.queryField = new Ext.form.TextArea({
			maxLength : 30000,
			xtype : 'textarea',
			width : '100%', //350,
			height : 200,//110,			
			autoScroll: true,
			regexText : LN('sbi.roles.alfanumericString'),
			fieldLabel : LN('sbi.ds.query'),
			validationEvent : true,
			allowBlank : false,
			name : 'query'
		});
	}
	
	
	, onEditScript: function(button, e) {
		var win = new Sbi.tools.dataset.QueryDatasetScriptWizard({
			languages: this.scriptTypes,
			language: this.language,
			script: this.script,
			query: this.queryField.getValue()
		});
		win.on('save', function(win, script, language){
			this.script = script;
			this.language = language;
		}, this);
        win.show(this); 
	}
	
});