/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.SearchPanel = function(config) { 
	 
	this.services = new Array();
	this.services['getCategoriesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_CATEGORIES_ACTION'
		, baseParams: {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		}
	});
	
	var c = Ext.apply({}, config, {
		title: LN('sbi.browser.searchpanel.title')
        , border:true
		, bodyStyle:'padding:6px 6px 6px 6px; background-color:#FFFFFF'
    	, items: [
    	          new Ext.form.Checkbox({id: 'temp'})
    	  ]
    	, attributes: 'ALL'
	});   
	
    this.attributesStore = new Ext.data.JsonStore({
    	autoLoad: false    	
    	,fields: ['label', 'name', 'description']
		, url: this.services['getCategoriesService']
		
    });
    this.attributesStore.on('load', this.onLoad, this);
    
    this.attributesStore.load();
	Sbi.browser.SearchPanel.superclass.constructor.call(this, c);  
	
	this.addEvents("onsearch", "onreset");
	
	var attributeLabel = LN('sbi.browser.searchpanel.attribute');
	
	this.addListener('afterlayout', function(el){
		
		if(this.searchField) return;		
			
		this.remove('temp');
		
		this.searchField =  new Sbi.browser.SearchField({
	    	hideLabel: true 
	    	, width:220
	    });
		
		this.searchField.addListener('onsearch', this.onSearch, this);
		
		this.searchField.addListener('onreset', function(){
			this.setFormState({valueFilter: null});
			this.fireEvent('onreset', this, this.getFormState());
		}, this);


	

		this.attributesComboBox = new Ext.form.ComboBox({
			tpl: '<tpl for="."><div ext:qtip="{name}: {name}" class="x-combo-list-item">{name}</div></tpl>',	
		    editable  : false,
		    fieldLabel : attributeLabel,
		    forceSelection : true,
		    mode : 'local',
		    name : 'attributes',
		    store : this.attributesStore,
		    displayField:'name',
		    valueField:'name',
		    emptyText:'Select an attribute...',
		    typeAhead: true,
		    triggerAction: 'all',
		    width: 100,
		    listWidth: 200,
		    selectOnFocus:true,
		    listeners: {
		   		'select': {
		        	fn: function(){}
		            , scope: this
				}
		    }
		});	
		
		this.similCheckbox =  new Ext.form.Checkbox({
	    	boxLabel: LN('sbi.browser.searchpanel.similar')
	    	,hideLabel: true 
	    	, width:220
	    });	
		this.add({
	        xtype:'fieldset',
	        title: LN('sbi.browser.searchpanel.query'),
	        collapsible: false,
	        autoHeight:true,
	        //defaults: {width: 10},
	        defaultType: 'textfield',
	        items :[this.searchField]
	       });
		this.add({
	        xtype:'fieldset',
	        title: LN('sbi.browser.searchpanel.attributes'),
	        collapsible: true,
	        autoHeight:true,
	        //defaults: {width: 10},
	        defaultType: 'textfield',
	        items :[this.attributesComboBox]
	       });
		this.add({
            xtype:'fieldset',
            title: LN('sbi.browser.searchpanel.advanced'),
            collapsible: true,
            autoHeight:true,
            //defaults: {width: 10},
            defaultType: 'textfield',
            items :[this.similCheckbox]
           });
	
		this.setFormState(c);
		
	}, this);
};

Ext.extend(Sbi.browser.SearchPanel, Ext.FormPanel, {
    services: null
	,searchField: null
	, similCheckbox: null
	, attributesComboBox: null
	
	, getFormState: function() {
	
		var formState = {};
	    
		formState.valueFilter = this.searchField.getRawValue();
		formState.attributes = this.attributesComboBox.getValue();
		formState.similar = this.similCheckbox.getValue();
	    	   
	    return formState;
	}

	, setFormState: function(formState) {
		
		if(formState.valueFilter !== undefined) {
			this.searchField.setValue( formState.valueFilter );
		}
		
		if(formState.attributes) {
			this.attributesComboBox.setValue(formState.attributes);
		} 
		if(formState.similar) {
			this.similCheckbox.setValue( formState.similar );
		}
	}
	
	
	, onSearch: function(field, query) {		
		this.fireEvent('onsearch', this, this.getFormState());
		
	}
    , onLoad : function() {
    	var recordsToAdd = [];
    	recordsToAdd[0]=new Ext.data.Record({name:'ALL', label:LN('sbi.browser.searchpanel.attributes.all'), description:LN('sbi.browser.searchpanel.attributes.all')});
    	recordsToAdd[1]=new Ext.data.Record({name:'LABEL', label:LN('sbi.browser.searchpanel.attributes.label'), description:LN('sbi.browser.searchpanel.attributes.label')});
    	recordsToAdd[2]=new Ext.data.Record({name:'NAME', label:LN('sbi.browser.searchpanel.attributes.name'), description:LN('sbi.browser.searchpanel.attributes.name')});
    	recordsToAdd[3]=new Ext.data.Record({name:'DESCRIPTION', label:LN('sbi.browser.searchpanel.attributes.description'), description:LN('sbi.browser.searchpanel.attributes.description')});
    	this.attributesStore.add(recordsToAdd);


    }
});

Ext.reg('searchpanel', Sbi.browser.SearchPanel);