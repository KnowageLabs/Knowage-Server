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

Ext.ns("Sbi.formtemplate");

Sbi.formtemplate.DocumentTemplateBuilder = function(config) {
	
	var defaultSettings = {
		
	};
	
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formtemplate.documentTemplateBuilder) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formtemplate.documentTemplateBuilder);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.services = this.services || new Array();	
	this.services['getDatasets'] = this.services['getDatasets'] || Sbi.config.remoteServiceRegistry.getRestServiceUrl(
			{serviceName: 'certificateddatasets/getflatdataset',
				isAbsolute: false,
				baseUrl:{restServicesPath: 'restful-services'}});
	
	this.services['execFormBuilderDataset'] = this.services['execFormBuilderDataset'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'FORM_ENGINE_FROM_DATASET_START_ACTION'
		, baseParams: {
			MODALITY: 'EDIT'
			, NEW_SESSION: 'TRUE'
			, formDocumentId: this.formDocumentId
			, IS_NEW_DOCUMENT: 'TRUE'  // this tells the service that the document is new and therefore the template is missing
		}
	});
	
	this.init();
	
	Ext.apply(c, {
		closable: false
		, border: false
		, items: [this.selectStart]
	});
	
	// constructor
    Sbi.formtemplate.DocumentTemplateBuilder.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.formtemplate.DocumentTemplateBuilder
 * @extends Ext.Panel
 * 
 * DocumentTemplateBuilder
 */
Ext.extend(Sbi.formtemplate.DocumentTemplateBuilder, Ext.Panel, {
    
	services: null
	, datasetSelectPage: null
	, selectStart: null
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	

	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, init: function() {
		this.initDatasetSelectPage();
		this.selectStart = new Ext.Panel({
			bodyStyle: 'padding:10px',
			layout: 'form',
			border: false,
			items: [this.datasetSelectPage]
		});
	}

	, initDatasetSelectPage: function() {

		var storeDS = new Ext.data.Store({
	        autoLoad: false,
	        url: this.services['getDatasets'],
		    reader: new Ext.data.JsonReader({
		    	    idProperty: 'label',
		    	    root: 'root',
		    	    totalProperty: 'results',
		    	    fields: [
		    	        {name: 'id', mapping: 'id'},
		    	        {name: 'name', mapping: 'name'},
		    	        {name: 'label', mapping: 'label'},
		    	        {name: 'description', mapping: 'description'}
		    	    ]
		    })
 
		});
	    this.datasetCombo = new Ext.form.ComboBox({
    	   	tpl: '<tpl for="."><div ext:qtip="{name}: {description}" class="x-combo-list-item">{name}</div></tpl>',	
    	   	editable  : false,
    	   	fieldLabel : LN('sbi.formtemplate.documenttemplatebuilder.datasetfield.label'),
    	   	forceSelection : true,
    	   	//mode : 'local',
    	   	name : 'scope',
    	   	store : storeDS,
    	   	displayField:'name',
    	    valueField:'label',
    	    emptyText: LN('sbi.formtemplate.documenttemplatebuilder.datasetfield.emptytext'),
    	    typeAhead: true,    	    
    	    triggerAction: 'all',
    	    selectOnFocus:true
    	});
	    
		
		this.submitDSBtn = new Ext.Button({
			text: LN('sbi.formtemplate.documenttemplatebuilder.startwithdataset'),
			disabled: false,
	        hidden: false,
	        handler: function() {			
				var datsetLabel = this.datasetCombo.getValue();
				this.openFormBuilderDS(datsetLabel);
			}, 
			scope: this
	    });
		
		this.datasetSelectPage = new Ext.Panel({
			bodyStyle: 'padding:10px;margin: 5px;',
			layout: 'form',
			items: [this.datasetCombo, this.submitDSBtn]
		});		

	}
	
	, openFormBuilderDS: function(datasetLabel) {
		var form = document.getElementById('submit-form');
		if(!form) {
			var dh = Ext.DomHelper;
			form = dh.append(Ext.getBody(), {
			    id: 'submit-form'
			    , tag: 'form'
			    , method: 'post'
			    , cls: 'submit-form'
			});
		}
		
		form.action = this.services['execFormBuilderDataset'] + '&dataset_label=' + datasetLabel;
		form.submit();
	}
  	
});