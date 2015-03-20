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
  * - name (mail)
  */


Ext.define('Sbi.widgets.grid.DynamicFilteringToolbar', {
    extend: 'Ext.toolbar.Toolbar'

    ,config: {
    	state: null
    	, columnNameStore: null
    	, columnNameCombo: null
    	, typeStore: null
    	, typeCombo: null
    	, filterStore: null
    	, filterCombo: null
    	, inputField: null
    	, additionalButtons: null//additional buttons to write in the top right part
    }

	, constructor: function(config) {
		this.callParent([config]);
		this.store.on('load', this.onStoreLoad,this);
	}


	, initComponent : function(){
		Sbi.widgets.grid.DynamicFilteringToolbar.superclass.initComponent.call(this);
	}
	
		
	, onRender : function(ct, position) {
	    
		Sbi.widgets.grid.DynamicFilteringToolbar.superclass.onRender.call(this, ct, position);
	    
		this.add(LN('sbi.lookup.ValueOfColumn'));	
		this.add({ xtype: 'tbspacer' });
		
		var columnNameStoreData = [{}];
		
		if(this.columnNamesStoreData){
			columnNameStoreData = this.columnNamesStoreData;
		}
		
		this.columnNameStore = Ext.create('Ext.data.Store', {
		    fields: ['header', 'name'],
		    data : columnNameStoreData
		});
		
		this.createColumnNameCombo();	    

	    this.add( this.columnNameCombo );	    
	    this.add({ xtype: 'tbspacer' });
	    	    
	    this.add(LN('sbi.lookup.asA'));
	    this.add({ xtype: 'tbspacer' });
	    
	    this.typeStore = Ext.create('Ext.data.Store', {
	        fields: ['value', 'label'],
	    	data : [
                  {value: 'string', label: LN('sbi.lookup.asString')}
                , {value: 'num', label: LN('sbi.lookup.asNumber')}
                , {value: 'date', label: LN('sbi.lookup.asDate')}
            ]
	    });	    
	    this.typeCombo = new Ext.create('Ext.form.ComboBox', {
	        store: this.typeStore,
	        width: 65,
	        displayField:'label',
	        valueField:'value',
	        typeAhead: true,
	        triggerAction: 'all',
	        emptyText:'...',
	        selectOnFocus:true,
	        mode: 'local'
	    });
	    this.add( this.typeCombo );
	    this.add({ xtype: 'tbspacer' });
	    	    
	    this.filterStore = Ext.create('Ext.data.Store', {
	        fields: ['value', 'label'],
	        data : [
	                  {value: 'contains', label: LN('sbi.lookup.Contains')}
	                , {value: 'start', label: LN('sbi.lookup.StartsWith')}
	                , {value: 'end', label: LN('sbi.lookup.EndsWith')}
	                , {value: 'equal', label: '='}
	                , {value: 'less', label: '<'}
	                , {value: 'lessequal', label: '<='}
	                , {value: 'greater', label: '>'}
	 	            , {value: 'greaterequal', label: '>='}
	        ]
	    });	    
	    this.filterCombo = Ext.create('Ext.form.ComboBox', {
	        store: this.filterStore,
	        width: 100,
	        displayField:'label',
	        valueField:'value',
	        typeAhead: true,
	        triggerAction: 'all',
	        emptyText:'...',
	        selectOnFocus:true,
	        mode: 'local'
	    });
	    this.add( this.filterCombo );   
	    this.add({ xtype: 'tbspacer' });
	    
	    this.valueField = Ext.create('Ext.form.TextField', {width: 70});
	    this.add( this.valueField ); 
	    	
		this.add({
            text: LN('sbi.behavioural.lov.filter.apply'),
            handler: this.applyFilter,
            scope: this
        });
		
		if(this.additionalButtons){
			this.add('->');
			for(var i=0; i<this.additionalButtons.length;i++){
				this.add(this.additionalButtons[i]);
			}
		}
	}
	
	, applyFilter: function(){
		var filterConfig = this.getValue();
		this.store.proxy.extraParams = filterConfig;
		this.store.loadPage(1);
	}
	
	
	
	, getValue: function(){
		var filterConfig = {};
		filterConfig.valueFilter = this.valueField.getValue();
		filterConfig.columnsFilter = this.columnNameCombo.getValue();
		filterConfig.typeValueFilter = this.typeCombo.getValue();
		filterConfig.typeFilter = this.filterCombo.getValue();
		return filterConfig;
	}
	
	, createColumnNameCombo: function(){
		// Create the combo box, attached to the states data store
		this.columnNameCombo = Ext.create('Ext.form.ComboBox', {
		    store: this.columnNameStore,
		    queryMode: 'local',
		    displayField: 'header',
		    valueField: 'name',
		    emptyText:'...',
	        selectOnFocus:true,
	        typeAhead: true,
	        triggerAction: 'all',
	        width: 100
		});	
	}
	
	, onStoreLoad: function(){
			if( this.store.proxy.reader.jsonData.metaData){//only if the metachanges, for the dynamicstore
				this.columnNameStore  = Ext.create('Ext.data.Store', {
				    fields: ['header', 'name'],
				    data : this.store.proxy.reader.jsonData.metaData.fields
				});
				
				if(this.columnNameCombo!=null && this.columnNameCombo!=undefined){
					this.columnNameCombo.destroy();
					this.createColumnNameCombo();
					this.insert(2,this.columnNameCombo );
				}
				
				
			}
	}

	, onClick: function() {
		var p = Ext.apply({}, this.store.baseParams);
		this.store.load({params: p});					
	}
	
	
});