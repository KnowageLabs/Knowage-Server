/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 


/**
 * 
 * @author MOnica Franceschini (monica.franceschini@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.DocBrowserContainer = function(config) {    
	
	config.baseLayout = config.baseLayout || {}; 	
	config.parentTab = this;
    var browser = new Sbi.browser.DocumentsBrowser(config);
    browser.addListener('closeDocument', this.closeDocument, this);
    
    //The type of layout is configurable trought the property 'typeLayout'. 
    //Possible values are 'tab' or 'card'. the first shows a tabPanel, the second a panel with a card layout.
    if (Sbi.settings.browser.typeLayout !== undefined && Sbi.settings.browser.typeLayout == 'card'){
    	 var navHandler = function(el){
    		 var lastSheetId = this.brSheet.items.length-1;
    		 
    		 if (el.getId && el.getId() == 'close-all'){
    			 //removes all cards and back to the document browser
	    		 for (var i=lastSheetId; i>0; i--){
		    		 var lastActiveItem = this.brSheet.getComponent(i);
		    		 if (i==1){
		    			 lastActiveItem.getEl().slideOut('l', {
					         easing: 'easeOut',
					         duration: 1000,
					         remove: true,
					         useDisplay: false
					     });
		    		 }
		    		 this.brSheet.remove(lastActiveItem, true);		    		 		    		 
	    		 }
	    		 var newActiveItem = this.brSheet.getComponent(0);
	    		 this.brSheet.getLayout().setActiveItem(newActiveItem);
//	    		 newActiveItem.getEl().slideIn('l', {
//			         easing: 'easeOut'
//			     });
	    		 lastSheetId = 0; //reset value
    		 }else{
    			 //removes the last card and back to card-1
    			 var newActiveItemId = lastSheetId-1;
	    		 if (newActiveItemId >= 0){    			
		    		 var lastActiveItem = this.brSheet.getComponent(this.brSheet.items.length-1);
		    		 lastActiveItem.getEl().slideOut('l', {
				         easing: 'easeOut',
				         duration: 1000,
				         remove: true,
				         useDisplay: false
				     });
		    		 this.brSheet.remove(lastActiveItem, true);
		    		 var newActiveItem = this.brSheet.getComponent(newActiveItemId);
		    		 this.brSheet.getLayout().setActiveItem(newActiveItem);	
		    		
		    		 lastSheetId -= 1;
	    		 }    			 
    		 }
    		 if (lastSheetId == 0) {
    			 Ext.getCmp('close').setVisible(false);
    			 Ext.getCmp('close-all').setDisabled(true);
    		 }else if (lastSheetId == 1) {
    			 Ext.getCmp('close').setVisible(false);
    			 Ext.getCmp('close-all').setDisabled(false);
    		 }else if (lastSheetId > 1) {
    			 Ext.getCmp('close').setVisible(true);
    			 Ext.getCmp('close-all').setDisabled(false);
    		 }
         };
         
         var hideGoBackLink = (Sbi.settings.geobi.browser.hideGoBackToolbar)? Sbi.settings.geobi.browser.hideGoBackToolbar:false;
         var btmbar = [];
         if (!hideGoBackLink){
        	 btmbar =  ['->', 
	     	    	      {
				            id: 'close-all',
				            xtype:'button',
				            text: LN('sbi.browser.goTo'),
				            handler: navHandler.createDelegate(this),
				            disabled: true,
				            hidden: hideGoBackLink
					      },
					      {
					         id: 'close',
					         xtype:'button',
					         text: LN('sbi.general.close'),
					         handler: navHandler.createDelegate(this),
					         hidden: true
					      }	    	        
		    ];
         }
         
         var brSheetConf = {
     	 	layout:'card',
     	   	activeItem: 0,
     	   	layoutConfig: { animate: true }, 
     	    items: [browser]
     	 };
         if(btmbar.length > 0 ) brSheetConf.bbar = btmbar;
    	 this.brSheet = new Ext.Panel(brSheetConf);	   
    }else{ 
    	//default type layout is 'tab'
    	this.brSheet = new Ext.TabPanel({
	    	activeTab: 0,	    	
		    items: [browser]
	    });
    }
	
	var c = {
			title: 'Browser'
			,layout:'fit'
	        ,items: [this.brSheet]

	};        
    Sbi.browser.DocBrowserContainer.superclass.constructor.call(this, c);
	// if browser is IE, re-inject parent.execCrossNavigation function in order to solve parent variable conflict that occurs when 
	// more iframes are built and the same function in injected: it is a workaround that let cross navigation work properly
	if (Ext.isIE) {
		this.brSheet.on(
				'tabchange',
				function  ( thisTabPanel, anActiveTab ) {
					var act = thisTabPanel.getActiveTab();
					try {
						if (act !== undefined && act.getActiveDocument()) {
							
								var documentPage = act.getActiveDocument().getDocumentExecutionPage();
								if (documentPage.isVisible()) {
									documentPage.injectCrossNavigationFunction();
								}
	
						}
					} catch (e) {}
				}
				, this
		);
	}
	//send messages about enable or disable datastore refresh action (for console engine) 
	this.brSheet.on(
	   'beforetabchange',
	   function (tabPanel, newTab, currentTab ) {
		   if(currentTab && currentTab.tabType === 'document' && currentTab.getActiveDocument() && currentTab.getActiveDocument().getDocumentExecutionPage()) {
			   currentTab.getActiveDocument().getDocumentExecutionPage().getDocumentPage().sendMessage('Disable datastore', 'hide');
		   }
		   if(newTab.tabType === 'document' && newTab.getActiveDocument() && newTab.getActiveDocument().getDocumentExecutionPage()){
			   newTab.getActiveDocument().getDocumentExecutionPage().getDocumentPage().sendMessage('Enable datastore', 'show');
		   }
	   }
	   , this
	);
	
};


Ext.extend(Sbi.browser.DocBrowserContainer, Ext.Panel, {
	brSheet: null
	
	, executeGeoreport: function(inputType, record){
		if(inputType == "DATASET"){
			var datasetLabel = record.data.label;
			var dataSourceLabel = record.data.dataSource;
			
			var url =  this.georeportEngineBaseUrl+ '&dataset_label=' + datasetLabel ;
			if(dataSourceLabel || dataSourceLabel!=""){
				url = url+ '&datasource_label=' + dataSourceLabel;
			}
			this.documentexecution.load(url);
			this.documentexecution.datasetLabel = datasetLabel;
		}
	}

	,
	getActiveExecutionPanel : function () {
		if (this.brSheet instanceof Ext.TabPanel) {
			var activeTab = this.brSheet.getActiveTab();
			if (activeTab instanceof Sbi.execution.ExecutionPanel) {
				return activeTab;
			} else {
				return null;
			}
		} else {
			var activePanel = this.brSheet.activeItem;
			if (activeTab instanceof Sbi.execution.ExecutionPanel) {
				return activeTab;
			} else {
				return null;
			}
		}
	}
	
	, closeDocument: function(){
		var el = this.brSheet.getActiveTab();
		this.brSheet.remove(el, true);	
	}
});