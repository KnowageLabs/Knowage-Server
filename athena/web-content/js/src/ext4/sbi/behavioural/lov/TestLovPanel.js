/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * Object name 
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
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.behavioural.lov.TestLovPanel', {
    extend: 'Ext.panel.Panel',
    lovTestConfiguration: null

    ,config: {
    	layout: 'border',
    	toolbarHeight: 30
    }

	, constructor: function(config) {
		
		var thisPanel = this;
		this.services = {};
		this.services.saveLovAction = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_LOV_ACTION',
			baseParams: {LIGHT_NAVIGATOR_DISABLED: 'TRUE'} 
		});
		this.treeLov = (config.lovConfig.lovType && (config.lovConfig.lovType=='tree'|| config.lovConfig.lovType=='treeinner'));
		
		var typeStoreValue = "simple";
		if(config.lovConfig.lovType){
			typeStoreValue = config.lovConfig.lovType;
		}
		
	    var typeStore = Ext.create('Ext.data.Store', {
	        fields: ['type','description'],
	        data : [{type:'simple', description:LN('sbi.behavioural.lov.type.simple')}, 
	                {type:'tree', description:LN('sbi.behavioural.lov.type.tree')},
	                {type:'treeinner', description:LN('sbi.behavioural.lov.type.treeinner')}]
	    });
	    
		this.comboType = Ext.create('Ext.form.ComboBox', {
	        store: typeStore,
	        displayField: 'description',
	        valueField: 'type',
	        queryMode: 'local',
	        value: typeStoreValue ,
	        triggerAction: 'all',
	        emptyText: LN('sbi.behavioural.lov.select.type'),
	        selectOnFocus:true,
	        width:135
	    });
	  
		var saveButton = {	
				handler: this.save,
				scope: this
		};

		var backButton = {
				handler: this.back,
				scope: this
		};
		
		if(Ext.isIE){
			saveButton.text = LN('sbi.behavioural.lov.save');
			backButton.text = LN('sbi.behavioural.lov.back');
		}else{
			saveButton.iconCls = 'icon-save';
			backButton.iconCls = 'icon-back';
		}

		this.dockedItems = [{
	        xtype: 'toolbar',
	        dock: 'top',
	        items: ['->',this.comboType,saveButton,backButton]
	    }]
		
		Ext.QuickTips.init();
		
		
		this.lovTestPreview = Ext.create('Sbi.behavioural.lov.TestLovResultPanel',{region: 'south',height:315, treeLov: this.treeLov}); //by alias
		//ConfigurationPanel(value, description)
		this.lovTestConfiguration = Ext.create('Sbi.behavioural.lov.TestLovConfigurationGridPanel',{lovConfig:config.lovConfig,  parentStore : this.lovTestPreview.store , lovType: typeStoreValue, flex: 1}); //by alias
		this.lovTestPreview.on('storeLoad',this.lovTestConfiguration.onParentStroreLoad,this.lovTestConfiguration);
		var lovConfigurationPanelItems = [this.lovTestConfiguration];
		
		if(this.treeLov){
			//Tree lov panel
			this.lovTestConfigurationTree = Ext.create('Sbi.behavioural.lov.TestLovTreePanel',{lovConfig:config.lovConfig, flex: 2, parentStore : this.lovTestPreview.store ,lovType: typeStoreValue});
			this.lovTestPreview.on('storeLoad',this.lovTestConfigurationTree.onParentStroreLoad,this.lovTestConfigurationTree);
			lovConfigurationPanelItems.push(this.lovTestConfigurationTree);
		}

		var lovConfigurationPanel = Ext.create('Ext.Panel', {
		      	layout: 'hbox',
		      	region: 'center',
		     	width: "100%",
		      	items: lovConfigurationPanelItems
		    });
		
		this.listeners = {
      		"render" : function(){
      			
    			var thisH = this.getHeight();
    			var previewH;
    			if(this.lovTestPreview.getEl()){
    				previewH = this.lovTestPreview.getHeight();
    			}else{
    				previewH = this.lovTestPreview.height;
    			}
    			this.lovTestConfiguration.setHeight(thisH-previewH-this.toolbarHeight);
    			if(this.treeLov){
    				this.lovTestConfigurationTree.setHeight(thisH-previewH-this.toolbarHeight);
    			}
    		},
    		"resize" : function(){
    			var thisH = this.getHeight();
    			var previewH;
    			if(this.lovTestPreview.getEl()){
    				previewH = this.lovTestPreview.getHeight();
    			}else{
    				previewH = this.lovTestPreview.height;
    			}
    			this.lovTestConfiguration.setHeight(thisH-previewH-this.toolbarHeight);
    			if(this.treeLov){
    				this.lovTestConfigurationTree.setHeight(thisH-previewH-this.toolbarHeight);
    			}
    		}
      	};
		
		Ext.apply(this,config||{});
		this.items = [lovConfigurationPanel,this.lovTestPreview];
    	this.callParent(arguments);
    	this.comboType.on('select',this.updateType,this);
    },
    
    save:  function(){
    	
    	var lovConfiguration;
    	if(this.lovTestConfiguration!=null && this.lovTestConfiguration!=undefined && !this.treeLov ){
    		lovConfiguration = this.lovTestConfiguration.getValues();
    	}else{
    		lovConfiguration = this.lovTestConfigurationTree.getValues();
    	}
    	if(!lovConfiguration) return;
    	
    	var callbackUrl = this.contextName+ "?PAGE=ListLovsPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
    	var callback = function(){window.location = callbackUrl};
    	var params ={};
    	params.LOV_CONFIGURATION = Ext.JSON.encode(lovConfiguration);
    	params.MESSAGEDET = this.modality;
    	params.RETURN_FROM_TEST_MSG = 'SAVE';
        Ext.Ajax.request({
            url: this.services.saveLovAction,
            params:  params,
            success: function(response, options) {
            	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.behavioural.lov.save.window.text'),LN('sbi.behavioural.lov.save.window.title'),{fn:callback});
            	
            },
            failure: Sbi.exception.ExceptionHandler.handleFailure
            ,scope: this
   		 });	
    	
    },
    
    back: function(){
    	var callbackUrl = this.contextName+ "?LIGHT_NAVIGATOR_DISABLED=true&PAGE=DetailModalitiesValuePage&lovProviderModified=false&MESSAGEDET="+ this.modality+"&modality=DETAIL_MOD&RETURN_FROM_TEST_MSG=DO_NOT_SAVE";
    	window.location = callbackUrl;
    }
    
    , updateType: function(combo, records,eOpt ){
    	var value = records[0].data.type;
    	
    	this.fireEvent('lovTypeChanged',value);
    }
});


