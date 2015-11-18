/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.qbe");

Sbi.qbe.SaveDatasetWindow = function(config) {

	// init properties...
	var defaultSettings = {
		// public
		title : LN('sbi.qbe.savedatasetwindow.title')
		, layout : 'fit'
		, width : 680
		, height : 460
		, closeAction : 'close'
		, frame : true
		// private
	};

	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.savedatasetwindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.savedatasetwindow);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	this.addEvents('save');
	this.addEvents('returnToMyAnalysis');
	
	this.initServices();
	this.initScopeStore();
	this.initForm();
	
	var c = Ext.apply({}, config, {
		buttons : [{ 
			  iconCls: 'icon-save' 	
			, handler: this.saveDatasetHandler
			, scope: this
			, text: LN('sbi.generic.actions.save')
           }]
		, items : [this.datasetForm]
	});   
	
    Sbi.qbe.SaveDatasetWindow.superclass.constructor.call(this, c);
    
};

/**
 * @class Sbi.qbe.SaveDatasetWindow
 * @extends Ext.Window
 * 
 * The popup window to save a Qbe dataset.
 */
Ext.extend(Sbi.qbe.SaveDatasetWindow, Ext.Window, {
	
	datasetForm : null
	, queries : null
	, services: null
	, persistPanel : null
	, metadataPanel : null
	,
	initServices: function() {
		this.services = new Array();
		
		var params = {
			LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		};

		this.services['saveDatasetService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_DATASET_USER_ACTION'
			, baseParams: params
		});
		
	}
	
	,
	initForm: function () {
		
		this.labelField = new Ext.form.TextField({
			id: 'label',
			name: 'label',
			allowBlank: false,
			maxLength: 50,
			autoCreate: {tag: 'input', type: 'text', autocomplete: 'off', maxlength: '50'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.label')
		});
		this.nameField = new Ext.form.TextField({
			id: 'name',
			name: 'name',
			allowBlank: false, 
			maxLength: 50,
			autoCreate: {tag: 'input', type: 'text', autocomplete: 'off', maxlength: '50'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.name') 
		});
		this.descriptionField = new Ext.form.TextArea({
			id: 'description',
			name: 'description',
			allowBlank: true, 
			maxLength: 160,
			autoCreate: {tag: 'textarea ', autocomplete: 'off', maxlength: '160'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.descr') 
		});
		this.scopeField = new Ext.form.ComboBox({
			id: 'scopeCombo',
		    fieldLabel: LN('sbi.generic.scope') ,
		    selectOnFocus:true,
		    mode : 'local',
		    triggerAction: 'all',
		    queryMode:'local',
		    store: this.scopesStore,
		    displayField: 'description',
		    valueField: 'value',
		    allowBlank: false,
    	   	editable: false,
    	   	forceSelection : true
		});
		
		
		this.initPersistPanel();
		
		this.initMetadataPanel();
		
		//default value
		this.scopeField.setValue(this.scopesStore.getAt(0).get('value'));
	    
	    var  genericForm = new Ext.Panel({
	        columnWidth: 0.6
	        , height : 300
	        //, frame : true
	    	, autoScroll : true
	    	, title : LN('sbi.qbe.savedatasetwindow.generic')
	        , items: {
	 		   	 columnWidth : 0.4
	             , xtype : 'fieldset'
	             , labelWidth : 80
	             //, defaults : { border : false }
	             , defaultType : 'textfield'
	             , autoHeight : true
	             , autoScroll : true
	             , bodyStyle : Ext.isIE ? 'padding:0 0 5px 5px;' : 'padding:0px 5px;'
	             , border : false
	             , style : {
	                 //"margin-left": "4px",
	                 //"margin-top": "10px"
	             }
	             , items :  [ this.labelField, this.nameField, this.descriptionField, this.scopeField]

	        }
	    });

	    var persistForm = new Ext.Panel({
	    	columnWidth: 0.6
	    	//, frame : true
	    	, autoScroll : true
	    	, title : LN('sbi.qbe.savedatasetwindow.persistence')
	        , items: {
	 		   	 columnWidth : 0.4
	             , xtype : 'fieldset'
	             , labelWidth : 80
	             //, defaults : { border : false }
	             , defaultType : 'textfield'
	             , autoHeight : true
	             , autoScroll : true
	             , bodyStyle : Ext.isIE ? 'padding:0 0 5px 5px;' : 'padding:0px 5px;'
	             , border : false
	             , style : {
	                 //"margin-left": "4px",
	                 //"margin-top": "10px"
	             }
	             , items :  [ this.persistPanel ]
	        }
	    });


	    var metadataForm = new Ext.Panel({
	    	columnWidth: 0.6
	    	//, frame : true
	    	, autoScroll : true
	    	, height: 400
	    	, title : LN('sbi.qbe.savedatasetwindow.metadata')
	        , items: {
	 		   	 columnWidth : 0.4
	             , xtype : 'fieldset'
	             , labelWidth : 80
	             //, defaults : { border : false }
	             , defaultType : 'textfield'
	             , autoHeight : true
	             , autoScroll : true
	             , bodyStyle : Ext.isIE ? 'padding:0 0 5px 5px;' : 'padding:0px 5px;'
	             , border : false
	             , style : {
	                 //"margin-left": "4px",
	                 //"margin-top": "10px"
	             }
	             , items :  [this.metadataPanel ]
	        }
	    });
	    
	    
	    var tabPanel = new Ext.TabPanel({
	        activeTab         : 0,
	        id                : 'myTPanel',
	        enableTabScroll   : true,
	        items             : [
	            genericForm,
	            persistForm,
	            metadataForm

	        ]
	    });
	    
	    
	    this.datasetForm = new Ext.FormPanel({
	        //columnWidth: 0.6
	        //, 
	        frame : true
	        , autoScroll : true
	    	, items :  [ tabPanel]
	    });
	    
	}
	
	,
	getFormState : function() {
      	var formState = {};
      	formState.label = this.labelField.getValue();
      	formState.name = this.nameField.getValue();
      	formState.description = this.descriptionField.getValue();
      	formState.isPublic = this.scopeField.getValue() === 'true';
      	

      	formState.isPersisted = this.persistPanel.isPersisted.getValue();
      	formState.isScheduled = this.persistPanel.isScheduled.getValue();
      	formState.persistTable = this.persistPanel.persistTableName.getValue();
      	formState.startDateField = this.persistPanel.startDateField.getValue();
      	formState.endDateField = this.persistPanel.endDateField.getValue();
      	formState.schedulingCronLine = this.persistPanel.schedulingCronLine.getValue();
      	
    	var meta = this.metadataPanel.getValues()
      	
    	formState.meta = Ext.util.JSON.encode(meta);
    	
      	return formState;
    }
	
	,
	saveDatasetHandler: function () {

		this.persistPanel.setSchedulingCronLine();
		
		var params = this.getInfoToBeSentToServer();
		Ext.MessageBox.wait(LN('sbi.generic.wait'));
		Ext.Ajax.request({
	        url : this.services['saveDatasetService']
	        , params : params
	        , success : this.datasetSavedSuccessHandler
	        , scope : this
			, failure : Sbi.exception.ExceptionHandler.handleFailure      
		});

	}
	
	,
	getInfoToBeSentToServer : function () {
		var formState = this.getFormState();
		formState.qbeJSONQuery = Ext.util.JSON.encode(this.queries);
		formState.qbeDataSource = this.datasourceLabel;
		//formState.isPersisted = true;
		formState.isFlatDataset = false;
		formState.sourceDatasetLabel = this.sourceDatasetLabel;
		return formState;
	}
	
	,
	datasetSavedSuccessHandler : function (response , options) {
  		if (response !== undefined && response.responseText !== undefined) {
  			var content = Ext.util.JSON.decode( response.responseText );
  			if (content.success !== 'true') {
                Ext.MessageBox.show({
                    title: LN('sbi.generic.error'),
                    msg: content,
                    width: 150,
                    buttons: Ext.MessageBox.OK
               });
      		} else {			 
      			var theWindow = this;
      			Ext.MessageBox.show({
                        title: LN('sbi.generic.success'),
                        msg: LN('sbi.generic.operationSucceded'),
                        width: 200,
                       buttons: Ext.MessageBox.OK

                });
      			this.fireEvent('save', this, this.getFormState());
      		}  
  		} else {
  			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
  		}  	
	}
	
	,
	initScopeStore : function () {
    	var scopeComboBoxData = [
    		['true',LN('sbi.generic.scope.public')],
    		['false',LN('sbi.generic.scope.private')]
    	];
    		
    	this.scopesStore = new Ext.data.SimpleStore({
    		fields: ['value', 'description'],
    		data : scopeComboBoxData 
    	}); 
	}
	,
	initPersistPanel : function () {
		this.persistPanel = new Sbi.qbe.PersistOptions({});
	}
	,
	initMetadataPanel : function () {
		var c = {};
		this.metadataPanel = new Sbi.qbe.ManageDatasetFieldMetadata(c);
		var query = this.queries.catalogue.queries[0];
		var fields = query.fields;
		
		// create object in order to reuse code
		var metadata = new Array();
		for (var i = 0; i < fields.length; i++) {
			metadata[i]={};
			metadata[i].column = fields[i].field;
			metadata[i].pname = '';
			
		}

		this.metadataPanel.loadItems({columns: metadata});

	
	}
	,
	getSelection : function(name) {
		var chosen;
		if (Ext.get(name + "-every").dom.checked) {
			chosen = '*';
		} else {
			chosen = Ext.getCmp(name + 'sMultiselect')
					.getValue();
			if (!chosen.length) {
				chosen = '*';
			}
		}
		return chosen;
	}


	
	
});