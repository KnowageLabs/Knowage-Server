/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * RangeDefinitionWindow - short description
  * 
  * Object documentation ...
  * 
  * by Monica Franceschini
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.RangeDefinitionWindow = function(config) {	
	
	var c = Ext.apply({}, config || {}, {
		title: LN('sbi.qbe.bands.range.title')
		, width: 400
		, height: 150
		, hasBuddy: false	
		
	});

	Ext.apply(this, c);
	
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
	this.services = new Array();
	
	this.services['getValuesForQbeFilterLookupService'] = Sbi.config.serviceRegistry.getServiceUrl({
		  serviceName: 'GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION'
		, baseParams: params
	});
	
	this.initMainPanel(c);	
	this.initfields(c.toedit);
	
	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	Sbi.qbe.RangeDefinitionWindow.superclass.constructor.call(this, c);  
	this.add(this.mainPanel);
  
};

Ext.extend(Sbi.qbe.RangeDefinitionWindow, Ext.Window, {
	
	hasBuddy: null
    , buddy: null
    , slotPanel : null
   
    , mainPanel: null
    , rangeFrom: null
    , rangeTo: null
    , rangeToSave: {}
	, fieldId: null
    , expression: null
    , lookupStore: null
    , lookupFieldFrom: null
    , lookupFieldTo: null
    , record: null
	
    , initfields: function(toedit){
		if(toedit !== undefined && toedit !== null){
			if(toedit.includeTo){
				this.rangeFrom.value = '<=';
			}else{
				this.rangeFrom.value = '<';
			}
			this.rangeToValue.value = toedit.to;
			this.rangeFromValue.value = toedit.from;
			if(toedit.includeFrom){
				this.rangeFrom.value = '>=';
			}else{
				this.rangeFrom.value = '>';
			}
		}
		
	}
	, initMainPanel: function(c) {
		
		this.slotPanel = c.slotPanel;
		this.fieldId = c.id;
		this.expression = c.expression;
		
		this.record = c.record;
		
		var btnFinish = new Ext.Button({
	        text: LN('sbi.qbe.bands.save.btn'),
	        disabled: false,
	        scope: this,
	        handler : this.save.createDelegate(this, [this.record, c.idx])
		});
		
	      var valuesFrom = new Ext.data.SimpleStore({
	          fields: ['id', 'value'],
	          data : [['1','>'],['2','>='], []]
	      });
	      var valuesTo = new Ext.data.SimpleStore({
	          fields: ['id', 'value'],
	          data : [['3','<'],['4','<='], []]
	      });
	      
		this.rangeFrom = new Ext.form.ComboBox({
		    allowBlank: true,
		    width: 50,
		    triggerAction: 'all',
		    lazyRender:true,
		    mode: 'local',
		    store: valuesFrom,
		    value: '2',
		    valueField: 'id',
		    displayField: 'value'
		});
		
		this.rangeFromValue = new Ext.form.TriggerField({
			  width: 100
            , allowBlank: true
            , triggerClass: 'x-form-search-trigger'
	    });
		this.rangeFromValue.onTriggerClick = this.openLookupFrom.createDelegate(this);
		
		this.rangeTo = new Ext.form.ComboBox({
		    allowBlank: true,
		    width: 50,
		    triggerAction: 'all',
		    lazyRender:true,
		    mode: 'local',
		    store: valuesTo,
		    valueField: 'id',
		    displayField: 'value',
		    value: '3'
		});

		this.rangeToValue = new Ext.form.TriggerField({
			  width: 100
			, allowBlank: true
            , triggerClass: 'x-form-search-trigger'
		});
		this.rangeToValue.onTriggerClick = this.openLookupTo.createDelegate(this);
		
		this.mainPanel = new Ext.form.FormPanel({  
			    layout: 'column',  
			    scope: this,
				width: 385,
				height: 120,
				
			    defaults: {border:false},  
			    bbar: ['->',
			           btnFinish
			    ], 
			    items: [{
			    	//rang from
			        xtype:'fieldset',
			        columnWidth: 0.48,
			        layout: 'hbox',   
			        title: 'MIN',
			        labelWidth: 0,
			        //autoHeight:true,
			        style: 'margin: 2px; border: 1px solid silver; float: left;',
			        defaultType: 'textfield',
			        padding:5,
			        items :[
			                this.rangeFrom, this.rangeFromValue
			        ]
			    } , {
			    	//rang to
			        xtype:'fieldset',
			        columnWidth: 0.48,
			        layout: 'hbox',   
			        labelWidth: 0,
			        style: 'margin: 2px; border: 1px solid silver; float: left;',
			        title: 'MAX',
			        //autoHeight:true,
			        padding:5,
			        defaultType: 'textfield',
			        items :[
			                this.rangeTo, this.rangeToValue
			        ]
			    } ]
		});  

		this.mainPanel.doLayout();

    }
	, save: function(rec, idx){
		
		this.rangeToSave.from ={};
		this.rangeToSave.to ={};
		this.rangeToSave.desc ={};
		
		this.rangeToSave.from.operand = this.rangeFrom.value;
		this.rangeToSave.from.value = this.rangeFromValue.getValue();
		this.rangeToSave.to.operand = this.rangeTo.value;
		this.rangeToSave.to.value = this.rangeToValue.getValue();
		if((this.rangeToSave.from.operand == null || this.rangeToSave.from.operand === undefined
			||this.rangeToSave.from.value == null || this.rangeToSave.from.value === undefined || this.rangeToSave.from.value == '')
			||(this.rangeToSave.to.operand == null || this.rangeToSave.to.operand === undefined
			||this.rangeToSave.to.value == null || this.rangeToSave.to.value === undefined || this.rangeToSave.to.value == '')){
			alert(LN('sbi.qbe.bands.range.invalid'));
		}else{
			this.slotPanel.addRange(this.rangeToSave, rec, idx);
			this.close();
		}

	}
	
	, openLookupFrom: function() {	
		this.lookupStore = this.createLookupStore();
		this.lookupStore.load()
		
		var baseConfig = {
	       store: this.lookupStore
	     , singleSelect: true
	     , valuesSeparator: Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator
		};
		
		this.lookupFieldFrom = new Sbi.widgets.FilterLookupPopupWindow(baseConfig);	
		this.lookupFieldFrom.on('selectionmade', function(xselection) {
			this.rangeFromValue.setValue(xselection.xselection.Values);			
			this.lookupFieldFrom.close();
		}, this);
		this.lookupFieldFrom.show();
	}
	
	, openLookupTo: function() {	
		this.lookupStore = this.createLookupStore();
		this.lookupStore.load()
		var baseConfig = {
	       store: this.lookupStore
	     , singleSelect: true
	     , valuesSeparator: Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator
		};
		
		this.lookupFieldTo = new Sbi.widgets.FilterLookupPopupWindow(baseConfig);	
		this.lookupFieldTo.on('selectionmade', function(xselection) {
			this.rangeToValue.setValue(xselection.xselection.Values);			
			this.lookupFieldTo.close();
		}, this);
		this.lookupFieldTo.show();
	}
	
	, createLookupStore: function() {
		var store = null;
		var params = {};
		var createStoreUrl = this.services['getValuesForQbeFilterLookupService'];
		if (this.fieldId !== null) createStoreUrl +=  '&ENTITY_ID=' + this.fieldId;
		if (this.expression !== null) {
			params.fieldDescriptor = Ext.util.JSON.encode({expression: this.expression});
		}
		
		store = new Ext.data.JsonStore({
			url: createStoreUrl
		});
		
		store.on('beforeload', function(store, options) {
			options =  Ext.apply(options.params, params);
		});
		
		store.on('loadexception', function(store, options, response, e) {
			var msg = '';
			var content = Ext.util.JSON.decode( response.responseText );
  			if(content !== undefined) {
  				msg += content.serviceName + ' : ' + content.message;
  			} else {
  				msg += 'Server response is empty';
  			}
	
			Sbi.exception.ExceptionHandler.showErrorMessage(msg, response.statusText);
		});
		
		return store;
	}
	
});