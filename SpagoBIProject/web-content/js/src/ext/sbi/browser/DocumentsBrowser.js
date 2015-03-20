/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.DocumentsBrowser = function(config) {    
  
	// sub-components   
	
	this.containerBrowser = config.parentTab;
	this.rootFolderId = config.rootFolderId || null;
	this.selectedFolderId = this.rootFolderId;
	this.defaultFolderId = null;

	//for "custom" Document Browser we have a defaultFolder id
	if ((config.defaultFolderId != null) && (config.defaultFolderId != undefined )){
		this.defaultFolderId = config.defaultFolderId ;
	} else {
		this.defaultFolderId = this.getLastVisitedFolderId();
	}
	
	
	this.detailPanel = new Sbi.browser.FolderDetailPanel({ 
		layout: 'fit'
        , metaFolder: config.browserConfig.metaFolder
        , metaDocument: config.browserConfig.metaDocument	
        , engineUrls: config.engineUrls	
        , folderId: this.selectedFolderId
        , defaultFolderId: this.defaultFolderId
    });

	this.centerContainerPanel = new Ext.Panel({
		 region: 'center'
//		 , enableTabScroll:true
//		 , defaults: {autoScroll:true}	

		 , items: [this.detailPanel]
		 , layout: 'fit'
	});
	config.baseLayout = config.baseLayout || {}; 	
	
	var c = Ext.apply({}, config.baseLayout, {
		layout: 'border',
	    border: false,
	   // title:'',//'Document browser',
	    items: [ 
	            // CENTER REGION ---------------------------------------------------------
	            this.centerContainerPanel
	           
	            // NORTH HREGION -----------------------------------------------------------
	            /*
	          	,new Sbi.browser.Toolbar({
	            	region: 'north',
	            	margins: '3 3 3 3',
	            	autoScroll: false,
	            	height: 30,
	            	layout: 'fit'
	          	})
	          	*/
	        ]
	});   
	
	if (Sbi.settings.browser.showTitle !== undefined && Sbi.settings.browser.showTitle){
		c.title = 'Document browser';
		
	}else{
		c.title='';
		c.header = false;
		c.headerAsText=false;
	}
	 
	if (Sbi.settings.browser.showLeftPanels !== undefined && Sbi.settings.browser.showLeftPanels){
		 // WEST REGION -----------------------------------------------------------
		this.treePanel = new Sbi.browser.DocumentsTree({
	        border: true
	        , rootNodeId: this.selectedFolderId 
	    });
	    this.treePanel.addListener('click', this.onTreeNodeClick, this);
		
		this.filterPanel = new Sbi.browser.FilterPanel({
	        title: LN('sbi.browser.filtrpanel.title')
	        , border:true
	        , metaFolder: config.browserConfig.metaFolder
	        , metaDocument: config.browserConfig.metaDocument	
	    });
		this.filterPanel.addListener('onsort', this.onSort, this);
	    this.filterPanel.addListener('ongroup', this.onGroup, this);
	    this.filterPanel.addListener('onfilter', this.onFilter, this);
		    
		if (Sbi.user.functionalities.contains('DoMassiveExportFunctionality')) {
			this.progressPanel = new Sbi.browser.ProgressPanel({
				title: LN('sbi.browser.progresspanel.title')
				, border:true
				, metaFolder: config.browserConfig.metaFolder
				, metaDocument: config.browserConfig.metaDocument	
			});
			this.progressPanel.addListener('click', this.onTreeNodeClick, this);
		}		
		
		this.searchPanel = new Sbi.browser.SearchPanel({
	        title: LN('sbi.browser.searchpanel.title')
	        , border:true
	        , metaDocument: config.browserConfig.metaDocument	
	    });
	    this.searchPanel.addListener('onsearch', this.onSearch, this);
	    this.searchPanel.addListener('onreset', this.onReset, this);

			
		this.westRegionContainer = new Ext.Panel({
		       id:'westRegionContainer',
		       split:true,
		       border:true,
		       frame:true,
		       collapsible: true,
		       //margins:'0 0 0 15',
		       layout:'accordion',
		       layoutConfig:{
		          animate:true
		       },
		       items: [
		               this.treePanel
		               , this.filterPanel
		               , this.searchPanel
		       ]
		});
	
		if(this.progressPanel){
		// defined and added only if user has massive export functionality	
			this.westRegionContainer.add(this.progressPanel);
		}
		
		var westRegion = 
            new Ext.Panel({               
                region: 'west',
                border: false,
                frame: false,
                //margins: '0 0 3 3',
                collapsible: true,
                collapsed: false,
                hideCollapseTool: true,
                titleCollapse: true,
                collapseMode: 'mini',
                split: true,
                autoScroll: false,
                width: 280,
                minWidth: 280,
                layout: 'fit',
                items: [this.westRegionContainer]
              });
		c.items.push(westRegion);
	}
	config.baseLayout = config.baseLayout || {}; 	
   
    Sbi.browser.DocumentsBrowser.superclass.constructor.call(this, c);

    this.detailPanel.addListener('onfolderload', this.onFolderLoad, this);
    this.detailPanel.addListener('ondocumentclick', this.onDocumentClick, this);

    this.detailPanel.addListener('onfolderclick', this.onFolderClick, this);
    if (Sbi.settings.browser.showBreadCrumbs !== undefined && Sbi.settings.browser.showBreadCrumbs){
    	this.detailPanel.addListener('onbreadcrumbclick', this.onBreadCrumbClick, this);
    }
    
    this.addEvents('closeDocument');
};


Ext.extend(Sbi.browser.DocumentsBrowser, Ext.Panel, {
    	
	rootFolderId: null
    , selectedFolderId: null
    
	, westRegionContainer: null
    , treePanel: null
    , filterPanel: null
    , searchPanel: null
    , progressPanel: null
    
    , centerRegionContainer: null
    , detailPanel: null
    , executionPanel: null

    
    , selectFolder: function(folderId) {
		this.detailPanel.loadFolder(folderId, this.rootFolderId);
		this.selectedFolderId = folderId;
		if (this.searchPanel) 
			this.searchPanel.selectedFolderId = folderId;
		if (this.detailPanel) 
			this.detailPanel.fId = folderId;
	}
	
	, onFolderLoad: function(panel) {
		
		var value = panel.folderId;
		if (value == null) {  // in case the root node is loaded, panel.folderId is null
			value = 'rootNode';
		}
		this.setLastVisitedFolderId(value);
		
//		if(this.brSheet.getActiveTab() != this.detailPanel) {
//			this.brSheet.setActiveTab(this.detailPanel);
//			
//		}
//		this.detailPanel.show();
	}
    
    
    , onTreeNodeClick: function(node, e) {
		this.selectFolder(node.id);
	}
    
    , onOpenFavourite: function(doc){
    	var maxNumberOfExecutionTabs = Sbi.settings.browser.maxNumberOfExecutionTabs || 0;
		var numOfExecutionDocs =  this.containerBrowser.brSheet.items.length-1;
		if (maxNumberOfExecutionTabs > 1 && numOfExecutionDocs >= maxNumberOfExecutionTabs){
			alert(LN('sbi.execution.executionpage.tabs.overMaxNum'));
			return
		}
		
    	var executionPanel = new Sbi.execution.ExecutionPanel({
			title: doc.title !== undefined ? doc.title : doc.name
			, closable: true
		}, doc);
		executionPanel.tabType = 'document';
		
		executionPanel.addListener('crossnavigationonothertab', this.onCrossNavigation, this);
		executionPanel.addListener('openfavourite', this.onOpenFavourite, this);		
		this.addPanelToSheet(executionPanel);
		
		executionPanel.execute();
	}
    
	, onCrossNavigation: function(config){
		this.onCrossNavigationDocumentClick(config);
		return false;
	}
	
	, onCrossNavigationDocumentClick: function(r) {

		var config = Ext.apply({
			//title: (r.document.title !== undefined || r.document.title != null)? r.document.title : r.document.name, 
			closable: true
		}, r);
		
		var name = r.document.name;
		var title = r.document.title;
		if(title !== undefined){
			config.title = title;
		}else{
			config.title = name;
		}
		var maxNumberOfExecutionTabs = Sbi.settings.browser.maxNumberOfExecutionTabs || 0;
		var numOfExecutionDocs =  this.containerBrowser.brSheet.items.length-1;
		if (maxNumberOfExecutionTabs > 1 && numOfExecutionDocs >= maxNumberOfExecutionTabs){
			alert(LN('sbi.execution.executionpage.tabs.overMaxNum'));
			return
		}
		
		var executionPanel = new Sbi.execution.ExecutionPanel(config, r.document);
		executionPanel.tabType = 'document';
		
		executionPanel.addListener('crossnavigationonothertab', this.onCrossNavigation, this);
		executionPanel.addListener('openfavourite', this.onOpenFavourite, this);
		this.addPanelToSheet(executionPanel);
		
		executionPanel.execute();
	}

	, onDocumentClick: function(panel, doc) {
		var maxNumberOfExecutionTabs = Sbi.settings.browser.maxNumberOfExecutionTabs || 0;
		var numOfExecutionDocs =  this.containerBrowser.brSheet.items.length-1;
		if (maxNumberOfExecutionTabs > 1 && numOfExecutionDocs >= maxNumberOfExecutionTabs){
			alert(LN('sbi.execution.executionpage.tabs.overMaxNum'));
			return
		}
		
		var executionPanel = new Sbi.execution.ExecutionPanel({
			title: doc.title !== undefined ? doc.title : doc.name
			, closable: true
		}, doc);
		executionPanel.tabType = 'document';
		
		executionPanel.addListener('crossnavigationonothertab', this.onCrossNavigation, this);
		executionPanel.addListener('openfavourite', this.onOpenFavourite, this);
		executionPanel.addListener('closeDocument', this.closeDocument,this);
		
		this.addPanelToSheet(executionPanel);
		executionPanel.execute();
	}
	
	, onFolderClick: function(panel, r) {
		this.selectFolder(r.id);
	}
	
	
	, onBreadCrumbClick: function(panel, b) {
		this.selectFolder(b.id);
	}
	
	, onSearch: function(panel, q) {
		if(this.rootFolderId) {
			q.rootFolderId = this.rootFolderId;
		}
		this.detailPanel.searchFolder(q);
	}
	
	, onSort: function(panel, cb) {
		this.detailPanel.sort('Documents', cb.inputValue);
	}
	
	, onReset: function(panel, cb) {
		this.selectFolder(this.selectedFolderId);
	}
	
	, onGroup: function(panel, cb) {
		this.detailPanel.group('Documents', cb.inputValue);
	}
	
	, onFilter: function(panel, cb) {
		this.detailPanel.filter(cb.inputValue);
	}
	
	,enableBBarButtons: function(nCard){
		if (Sbi.settings.geobi.browser.hideGoBackToolbar && !Sbi.settings.geobi.browser.hideGoBackToolbar){
			switch (nCard)
			{
			case 0:
				Ext.getCmp('close').setVisible(false);
				Ext.getCmp('close-all').setDisabled(true);
			  break;
			case 1:
				Ext.getCmp('close').setVisible(false);
				Ext.getCmp('close-all').setDisabled(false);
			  break;		
			default:
				Ext.getCmp('close').setVisible(true);
				Ext.getCmp('close-all').setDisabled(false);
			}
		}
	}
	
	, addPanelToSheet: function(panel){
		var maxNumberOfExecutionTabs = Sbi.settings.browser.maxNumberOfExecutionTabs || 0;
		var numOfExecutionDocs =  this.containerBrowser.brSheet.items.length-1;

		if (maxNumberOfExecutionTabs == 1 && maxNumberOfExecutionTabs == numOfExecutionDocs){
			//replace the first document panel
			var lastTab = this.containerBrowser.brSheet.getComponent(1);
			if (lastTab !== undefined && lastTab !== null){ 
				this.containerBrowser.brSheet.remove(lastTab, true);
			}
		}
		if (Sbi.settings.browser.typeLayout !== undefined && Sbi.settings.browser.typeLayout == 'card'){
			var newExecDoc = this.containerBrowser.brSheet.add(panel);	
			this.containerBrowser.brSheet.getLayout().setActiveItem(newExecDoc);		
			newExecDoc.getEl().slideIn('r', {
		         easing: 'easeOut'
//		       , duration: 1000
		     });			
			this.enableBBarButtons(this.containerBrowser.brSheet.items.length-1);
			
		}else{
			this.containerBrowser.brSheet.add(panel).show();
		}
	}
	
	, closeDocument: function(){
		   this.fireEvent('closeDocument');
		   this.detailPanel.folderView.store.reload();
	}
	
	,
	setInCookies : function (key, value) {
		var expiration = 24*60*60*1000; // 1 day
		Sbi.cookies.Cookies.setCookie(key, value, expiration);
	}
	
	,
	getFromCookies : function (key) {
		var value = Sbi.cookies.Cookies.getCookie(key);
		return value;
	}
	
	,
	getLastVisitedFolderId : function () {
		var key = this.getLastVisitedFolderIdKey();
		return this.getFromCookies(key);
	}
	
	,
	setLastVisitedFolderId : function (lastVisitedFolderId) {
		var key = this.getLastVisitedFolderIdKey();
		this.setInCookies(key, lastVisitedFolderId);
	}
	
	,
	getLastVisitedFolderIdKey : function () {
		return "SBI_" + Sbi.user.userId + "_LF";
	}
	
});