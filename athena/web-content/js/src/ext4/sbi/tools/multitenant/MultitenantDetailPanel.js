/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *  @author
 *  Luca Rossato (luca.rossato@eng.it)
 */


Ext.define('Sbi.tools.multitenant.MultitenantDetailPanel', {
	extend: 'Ext.tab.Panel'

	,config: {
		defaults: {
			width: 400
			,layout:'fit'
		},   
	
		fieldDefaults: {
			labelAlign: 'right',
			msgTarget: 'side'
		},
		border: false
	}

	, constructor: function(config) {
		this.initConfig(config);
		this.services = [];
		this.initServices();
		this.initTabs();
		this.items = [this.detailTab, this.engineTab, this.dsTab];

		this.addEvents('save');
		this.tbar = Sbi.widget.toolbar.StaticToolbarBuilder.buildToolbar({items:[{name:'->'},{name:'save'}]},this);
		this.tbar.on("save",function(){
			if(this.validateForm()){
				this.fireEvent("save", this.getAllTabValues());
			}else{
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.multitenant.validation.error'),LN('sbi.generic.validationError'));
			}
	
		},this);
		this.callParent(arguments);
		this.on("render",function(){this.hide()},this);	
	}
	
	, setValues: function(data){
		this.tenantId.setValue(data.MULTITENANT_ID);
		if(data.MULTITENANT_ID != null && data.MULTITENANT_ID !="")
			this.tenantName.disable();
		else
			this.tenantName.enable();
			
		this.tenantName.setValue(data.MULTITENANT_NAME);
		this.tenantTheme.setValue(data.MULTITENANT_THEME);
	}
	
	, initServices: function(baseParams){
		this.services["getThemes"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'multitenant/themes'
				, baseParams: baseParams
		});
		
		this.services["getEngines"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'multitenant/engines'
				, baseParams: baseParams
		});
		
		this.services["getDataSources"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'multitenant/datasources'
				, baseParams: baseParams
		});
	}
	
	, getValues: function(){
		var values = {
			MULTITENANT_ID: this.tenantId.getValue(),
			MULTITENANT_NAME: this.tenantName.getValue(),
			MULTITENANT_THEME: this.tenantTheme.getValue()
		};

		return values;
	}
	
	, getAllTabValues: function(){
		var values = this.getValues();
		var dsArray = this.dsList.getStore().getRange();
		var dsList = [];
		
		var dsCount = 0;
		for (var i = 0, len = dsArray.length; i < len; i++) {
			var dsRow = dsArray[i].getData();
			if(dsRow.CHECKED)
				dsList[dsCount++] = dsRow;
		};
		
		var engArray = this.enginesList.getStore().getRange();
		var engList = [];
		
		var engCount = 0;
		for (var i = 0, len = engArray.length; i < len; i++) {
			var engRow = engArray[i].getData();
			if(engRow.CHECKED)
				engList[engCount++] = engRow;
		};
		
		values.DS_LIST = dsList;	
		values.ENG_LIST = engList;

		return values;
	}
	
	, validateForm: function(){
		var valid = true;
		var v = this.getValues();

		valid = valid && (v.MULTITENANT_NAME!=null && v.MULTITENANT_NAME!=undefined && v.MULTITENANT_NAME!="" && v.MULTITENANT_NAME.length <= 20);	
		valid = valid && (v.MULTITENANT_THEME!=null && v.MULTITENANT_THEME!=undefined &&  v.MULTITENANT_THEME!="");
		
		return valid;
	}
	
	, initTabs: function(){
			
		this.tenantId = new Ext.form.field.Hidden({
			name: "MULTITENANT_ID"
		});
		
		this.tenantName = new Ext.form.field.Text({
			name: "MULTITENANT_NAME",
			width: 500,
			maxLengthText: 20,
			maxLength: 20,
			fieldLabel: LN('sbi.generic.name'),
			allowBlank: false
		});
		
		// Theme
		Ext.define("ThemeModel", {
    		extend: 'Ext.data.Model',
            fields: ["VALUE_CHECK"]
    	});
		
		var themeStore = Ext.create('Ext.data.Store',{
    		model: "ThemeModel",
    		proxy: {
    			type: 'ajax',
    			extraParams : {DOMAIN_TYPE:"DIALECT_HIB"},
    			url:  this.services['getThemes'],
    			reader: {
    				type:"json",
    				root: 'root'  					
    			}
    		}
    	});
		themeStore.load();		
		this.tenantTheme = new Ext.form.ComboBox({
			name: "MULTITENANT_THEME",
			fieldLabel: LN('sbi.multitenant.theme'),
			width: 400,
	        store: themeStore,
	        displayField:'VALUE_CHECK',
	        valueField:'VALUE_CHECK',
	        allowBlank: false
		});
		
		// Engine
		Ext.define("EngineModel", {
    		extend: 'Ext.data.Model',
            fields: ["ID", "NAME", "CHECKED"]
    	});
		
		this.enginesStore = Ext.create('Ext.data.Store', {
		        model: 'EngineModel',
		        proxy: {
		            type: 'ajax',
		            extraParams : {DOMAIN_TYPE:"DIALECT_HIB"},
	    			url:  this.services['getEngines'],
		            reader: {
		                type: 'json',
		                root: 'root'
		            },		         
		        }
		    });
//		this.enginesStore.load();
		
		this.enginesList = Ext.create('Ext.grid.Panel', {
	        store: this.enginesStore,
	       // autoScroll: true,
	        scroll: true,
	        layout: 'fit',
	        height: 750,
	        autoScroll: true,
	        style: 'overflow: hidden;',
	        columns: [{
	        	hidden: true,
	            dataIndex: 'ID'
	          }, {
	            text: LN('sbi.generic.name'),
	            flex: 1,
	            sortable: true,
	            dataIndex: 'NAME',
	            field: {
	                xtype: 'textfield'
	            }
	          }, {
	        	  xtype: 'checkcolumn',
	        	  header: '',
	        	  dataIndex: 'CHECKED',
	        	  sortable: false,
	        	  width: 40  
	          }]
	    });	
		
		// Data Source
		Ext.define("DSModel", {
    		extend: 'Ext.data.Model',
            fields: ["ID", "LABEL", "DESCRIPTION", "CHECKED"]
    	});
		
		this.dsStore = Ext.create('Ext.data.Store', {
		        model: 'DSModel',
		        proxy: {
		            type: 'ajax',
		            extraParams : {DOMAIN_TYPE:"DIALECT_HIB"},
	    			url:  this.services['getDataSources'],
		            reader: {
		                type: 'json',
		                root: 'root'
		            },		         
		        }
		    });
//		this.dsStore.load();
		
		this.dsList = Ext.create('Ext.grid.Panel', {
	        store: this.dsStore,
	        scroll: true,
	        autoHeight: true,
	        viewConfig: {
				autoFill: true,
				scrollOffset: 0,
				style: { overflow: 'auto', overflowX: 'hidden' }
	        },
	        columns: [{
	        	hidden: true,
	            dataIndex: 'ID'
	          }, {
	            text: LN('sbi.generic.label'),
	            flex: 1,
	            sortable: true,
	            dataIndex: 'LABEL',
	            field: {
	                xtype: 'textfield'
	            }
	          }, {
	            text: LN('sbi.generic.descr'),
	            flex: 1,
	            sortable: true,
	            dataIndex: 'DESCRIPTION',
	            field: {
	                xtype: 'textfield'
	            }
	          }   
	         , {
	        	  xtype: 'checkcolumn',
	        	  header: '',
	        	  dataIndex: 'CHECKED',
	        	  sortable: false,
	        	  width: 40 
	          }]
	    });	
		
		this.detailTab = new Ext.form.Panel({
			title: 'Detail',
			items: [this.tenantId, this.tenantName, this.tenantTheme],
			bodyBorder : false,
			bodyPadding : 10,
			border: false
		});
		
		this.engineTab = new Ext.form.Panel({
			title: 'Engines',
			items: [this.enginesList],
//			autoScroll: true,
			layout: 'fit',
			border: false
		});
		
		this.dsTab = new Ext.form.Panel({
			title: 'Data Source',
			items: [this.dsList],
//			autoScroll: true,
//			layout: 'fit',
			border: false
		});	
	}

});
