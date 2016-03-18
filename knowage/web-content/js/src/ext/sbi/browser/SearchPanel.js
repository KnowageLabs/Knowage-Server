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