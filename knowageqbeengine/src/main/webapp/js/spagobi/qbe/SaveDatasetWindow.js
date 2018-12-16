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
	, queryCataloguePanel : null
	, services: null
	, persistPanel : null
	, metadataPanel : null
	,
	initServices: function() {
		this.services = new Array();

		var params = {
			LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		};

		var scopeParams = {
				DOMAIN_TYPE : 'DS_SCOPE',
				EXT_VERSION : '3',
				LIGHT_NAVIGATOR_DISABLED : params.LIGHT_NAVIGATOR_DISABLED
		};
		if(this.queryCataloguePanel.dataset){
			this.services['saveDatasetService'] = Sbi.config.remoteServiceRegistry.getRestServiceUrl({
				serviceName: '1.0/datasets'
				, isAbsolute : false
			});
		} else {
			this.services['saveDatasetService'] = Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'SAVE_DATASET_USER_ACTION'
				, baseParams: params
			});
		}

		this.services["getScopeCd"]= Sbi.config.remoteServiceRegistry.getRestServiceUrl({
			serviceName: 'domainsforfinaluser/listValueDescriptionByType',
			baseParams: scopeParams
			, isAbsolute : false
		});
		this.services["getDsCategories"]= Sbi.config.remoteServiceRegistry.getRestServiceUrl({
			serviceName: 'domainsforfinaluser/ds-categories',
			baseParams: params
			, isAbsolute : false
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
			fieldLabel: LN('sbi.generic.label'),
			readOnly: this.queryCataloguePanel.dataset ? true : false,
			value: this.queryCataloguePanel.dataset ? this.queryCataloguePanel.dataset.label : undefined
		});
		this.nameField = new Ext.form.TextField({
			id: 'name',
			name: 'name',
			allowBlank: false,
			maxLength: 50,
			autoCreate: {tag: 'input', type: 'text', autocomplete: 'off', maxlength: '50'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.name'),
			value: this.queryCataloguePanel.dataset ? this.queryCataloguePanel.dataset.name : undefined
		});
		this.descriptionField = new Ext.form.TextArea({
			id: 'description',
			name: 'description',
			allowBlank: true,
			maxLength: 160,
			autoCreate: {tag: 'textarea ', autocomplete: 'off', maxlength: '160'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.descr'),
			value: this.queryCataloguePanel.dataset ? this.queryCataloguePanel.dataset.description : undefined
		});

		var hideCombos = !Sbi.config.isTechnicalUser;

		this.initPersistPanel();
		this.initMetadataPanel();
		this.categoriesStore = this.createCategoriesStore();
		this.scopeCdStore = this.createScopeCdStore();

		this.categoriesCombo = new Ext.form.ComboBox({
			id : 'catTypeVn',
			name : 'catTypeVn',
			fieldLabel: LN('sbi.ds.catType'),
		    typeAhead: true,
		    hidden : hideCombos,
		    hideLabel : hideCombos,
		    triggerAction: 'all',
		    allowBlank : true,
		    editable : false,
		    lazyRender:true,
		    mode: 'local',
		    width : 300,
		    store: this.categoriesStore,
		    valueField: 'VALUE_ID',
		    displayField : 'VALUE_CD'
		});

		this.scopeCdCombo = new Ext.form.ComboBox({
			id: 'scopeCd',
			name : 'scopeCd',
			fieldLabel: LN('sbi.ds.scope'),
			typeAhead: true,
			hidden : hideCombos,
			hideLabel : hideCombos,
			triggerAction: 'all',
			lazyRender:true,
			editable : false,
			allowBlank : true,
			mode: 'local',
			width : 300,
			store: this.scopeCdStore,
			valueField: 'VALUE_ID',
			displayField : 'VALUE_CD'
		});

		this.scopeCdCombo.addListener('select', function() {
			var selected = this.getRawValue();
			if (selected != null || selected != undefined){
				selected = selected.toUpperCase();
				var catTypeCombo = Ext.getCmp('catTypeVn');
				if (selected == 'ENTERPRISE' || selected == 'TECHNICAL'){
					catTypeCombo.allowBlank = false;
				}else{
					catTypeCombo.allowBlank = true;
				}
				catTypeCombo.validate();
			}
		});

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
	             , items :  [ this.labelField, this.nameField, this.descriptionField, this.scopeCdCombo, this.categoriesCombo]

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
	, createCategoriesStore: function(){
		var categoriesStore = new Ext.data.JsonStore({
			url: this.services['getDsCategories'],
			fields: ["VALUE_NM","VALUE_DS","VALUE_ID","VALUE_CD"]
		});

		categoriesStore.on('loadexception', function(store, options, response, e) {
			var msg = '';
			var content = Ext.util.JSON.decode( response.responseText );
  			if(content !== undefined) {
  				msg += content.serviceName + ' : ' + content.message;
  			} else {
  				msg += 'Server response is empty';
  			}

			Sbi.exception.ExceptionHandler.showErrorMessage(msg, response.statusText);
		});

    	categoriesStore.load();

    	return categoriesStore;
	}
	, createScopeCdStore: function(){

		var scopeStore = new Ext.data.JsonStore({
			url: this.services['getScopeCd'],
			root : 'domains',
			fields: ["VALUE_NM","VALUE_DS","VALUE_ID","VALUE_CD"],
			listeners: {
			    load : function() {
			    	var combo = Ext.getCmp('scopeCd');
		            combo.setValue(this.getAt(0).data.VALUE_ID);
			    }
			}
		});

		scopeStore.on('loadexception', function(store, options, response, e) {
			var msg = '';
			var content = Ext.util.JSON.decode( response.responseText );
  			if(content !== undefined) {
  				msg += content.serviceName + ' : ' + content.message;
  			} else {
  				msg += 'Server response is empty';
  			}

			Sbi.exception.ExceptionHandler.showErrorMessage(msg, response.statusText);
		});

		scopeStore.load();

    	return scopeStore;
	},
	getFormState : function() {
      	var formState = {};
      	formState.label = this.labelField.getValue();
      	formState.name = this.nameField.getValue();
      	formState.description = this.descriptionField.getValue();

      	formState.isPersisted = this.persistPanel.isPersisted.getValue();
      	formState.isScheduled = this.persistPanel.isScheduled.getValue();
      	formState.persistTable = this.persistPanel.persistTableName.getValue();
      	formState.startDateField = this.persistPanel.startDateField.getValue();
      	formState.endDateField = this.persistPanel.endDateField.getValue();
      	formState.schedulingCronLine = this.persistPanel.schedulingCronLine.getValue();

      	if (this.scopeCdCombo.getRawValue() != null && this.scopeCdCombo.getRawValue().length > 0){
      		formState.scopeId = this.scopeCdCombo.getValue();
      		formState.scopeCd = this.scopeCdCombo.getRawValue();
      	}
     	if (this.categoriesCombo.getRawValue() != null && this.categoriesCombo.getRawValue().length > 0){
	      	formState.categoryId = this.categoriesCombo.getValue();
	      	formState.categoryCd = this.categoriesCombo.getRawValue();
     	}
    	var meta = this.metadataPanel.getValues()

		if(this.queryCataloguePanel.dataset){
			formState.meta = meta
		} else {
	    	formState.meta = Ext.util.JSON.encode(meta);
		}

      	return formState;
    }

	,
	saveDatasetHandler: function () {

		this.persistPanel.setSchedulingCronLine();

		var params = this.getInfoToBeSentToServer();


		var currentQuery = this.queryCataloguePanel.getSelectedQuery();
		var ambiguousFields = [];
		var ambiguousRoles = [];
		if (currentQuery) {
			ambiguousFields = this.queryCataloguePanel.getStoredAmbiguousFields();
			ambiguousRoles = this.queryCataloguePanel.getStoredRoles();
		}
		params.catalogue = Ext.util.JSON.encode(this.queries.catalogue.queries) ;
		params.currentQueryId = (currentQuery) ? currentQuery.id : '' ;
		params.ambiguousFieldsPaths = Ext.util.JSON.encode(ambiguousFields) ;
		params.ambiguousRoles = Ext.util.JSON.encode(ambiguousRoles) ;
		var config = {
		        url : this.services['saveDatasetService'],
		       // success : this.datasetSavedSuccessHandler,
		        scope : this,
		        failure : Sbi.exception.ExceptionHandler.handleFailure
		}
		if(this.queryCataloguePanel.dataset){
			this.queryCataloguePanel.dataset.description = params.description;
			//this.queryCataloguePanel.dataset.endDateField = params.endDateField;
			//this.queryCataloguePanel.dataset.isFlatDataset = params.isFlatDataset;
			this.queryCataloguePanel.dataset.isPersisted = params.isPersisted;
			this.queryCataloguePanel.dataset.isScheduled = params.isScheduled;
			this.queryCataloguePanel.dataset.label = params.label;
			this.queryCataloguePanel.dataset.name = params.name;
			this.queryCataloguePanel.dataset.meta = params.meta;
			this.queryCataloguePanel.dataset.qbeJSONQuery = params.qbeJSONQuery;
			//this.queryCataloguePanel.dataset.schedulingCronLine = params.schedulingCronLine;
			this.queryCataloguePanel.dataset.scopeCd = params.scopeCd;
			this.queryCataloguePanel.dataset.scopeId = params.scopeId;
			//this.queryCataloguePanel.dataset.startDateField = params.startDateField;
			config.method="POST";
			config.success = this.datasetUpdateSuccessHandler,
			config.headers = {
	            'Content-Type': 'application/json'
	        };
			config.jsonData = this.queryCataloguePanel.dataset
		} else {
			success : this.datasetSavedSuccessHandler,
			config.params = params
		}
		Ext.MessageBox.wait(LN('sbi.generic.wait'));
		Ext.Ajax.request(config);

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
	},
	datasetUpdateSuccessHandler : function (response , options) {
  		if (response !== undefined && response.responseText !== undefined) {
  			var content = Ext.util.JSON.decode( response.responseText );
  			if ( content.success == false) {
                Ext.MessageBox.show({
                    title: LN('sbi.generic.error'),
                    msg: content,
                    width: 150,
                    buttons: Ext.MessageBox.OK
               });
      		} else {
      			var theWindow = this;
      			this.queryCataloguePanel.dataset.meta = content.meta;
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
	initPersistPanel : function () {
		this.persistPanel = new Sbi.qbe.PersistOptions({});
	}
	,
	initMetadataPanel : function () {
		var c = {};
		this.metadataPanel = new Sbi.qbe.ManageDatasetFieldMetadata(c);
		var query = this.queries.catalogue.queries[0];
		var fields = query.fields;
		var fieldsMetadata = new Array();
		if(this.queryCataloguePanel.dataset && this.queryCataloguePanel.dataset.meta.columns){
			var object = {};

			for(var item in this.queryCataloguePanel.dataset.meta.columns){
			     var element = object[this.queryCataloguePanel.dataset.meta.columns[item].column];
			     if(!element){
			    	 element = {};
			         object[this.queryCataloguePanel.dataset.meta.columns[item].column] = element;
			         element["column"] = this.queryCataloguePanel.dataset.meta.columns[item].column;
			     }
			     element[this.queryCataloguePanel.dataset.meta.columns[item].pname] = this.queryCataloguePanel.dataset.meta.columns[item].pvalue;
			}


			for (item in object) {
				fieldsMetadata.push(object[item]);
			}

		}
		// create object in order to reuse code
		var metadata = new Array();
		for (var i = 0; i < fields.length; i++) {
			metadata[i]={};
			metadata[i].column = fields[i].field;
			metadata[i].pname = '';
			metadata[i].alias = fields[i].alias;
			metadata[i].entity = fields[i].entity;
			metadata[i].field = fields[i].field;
			metadata[i].group = fields[i].group;
			metadata[i].funct = fields[i].funct;
			metadata[i].id = fields[i].id;
			metadata[i].type = fields[i].type;
			metadata[i].include = fields[i].include;
			metadata[i].visible = fields[i].visible;

			// If it has aggregation function and is not grouped by is assumed to be a measure
			//alert (metadata[i].group + " ---- "+ metadata[i].funct );
			if(metadata[i].group != true && metadata[i].funct != '' && metadata[i].funct != 'NONE'){
				metadata[i].pname = 'fieldType';
				metadata[i].pvalue = 'MEASURE';
			}
			else{
				metadata[i].pname = 'fieldType';
				metadata[i].pvalue = 'ATTRIBUTE';
			}

			if(this.queryCataloguePanel.dataset && this.queryCataloguePanel.dataset.meta.columns){
				for (var j = 0; j < fieldsMetadata.length; j++) {
					if(metadata[i].id==fieldsMetadata[j].uniqueName) {
						metadata[i].pvalue = fieldsMetadata[j].fieldType;
					}
				}

			}
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