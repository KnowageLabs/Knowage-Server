/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.FolderDetailPanel = function(config) {    
    
	// always declare exploited services first!
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
	this.services = new Array();
	this.services['loadFolderContentService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FOLDER_CONTENT_ACTION'
		, baseParams: params
	});
	this.services['searchContentService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SEARCH_CONTENT_ACTION'
		, baseParams: params
	});
	this.services['loadFolderPathService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FOLDER_PATH_ACTION'
		, baseParams: params
	});
	this.services['deleteDocument'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_OBJECT_ACTION'
		, baseParams: params
	});
	
	this.services['cloneDocument'] = Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'documents/clone'});
	
	
	this.services['createDocument'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DOCUMENT_USER_BROWSER_CREATE_DOCUMENT'
		, baseParams: {LIGHT_NAVIGATOR_DISABLED: 'FALSE', MESSAGEDET: 'DETAIL_SELECT'}
	});
	
	this.services['detailDocument'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceType: 'PAGE'
		, serviceName: 'DetailBIObjectPage'
		, baseParams: {LIGHT_NAVIGATOR_DISABLED: 'FALSE', MESSAGEDET: 'DETAIL_SELECT'}
	});
  
	this.services['newDocument'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceType: 'PAGE'
		, serviceName: 'DetailBIObjectPage'
		, baseParams: {LIGHT_NAVIGATOR_DISABLED: 'FALSE', MESSAGEDET: 'DETAIL_NEW'}
	});
	
	this.services['getCommunities'] =  Sbi.config.serviceRegistry.getRestServiceUrl({
		serviceName: 'community/user'
			, baseParams: {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE',
				EXT_VERSION: "3"
			}
	});

	
	// -- store -------------------------------------------------------
	this.store = new Ext.data.JsonStore({
		 url: this.services['loadFolderContentService']
		 , browseUrl: this.services['loadFolderContentService']
		 , searchUrl: this.services['searchContentService']
		 , root: 'folderContent'
		 , fields: ['title', 'icon', 'samples']
	});	

	this.store.on('loadexception', Sbi.exception.ExceptionHandler.handleStoreLoadException);
	this.store.on('beforeload', 
			function(){
				if(this.loadingMask) {
					this.loadingMask.show();
				}
			}, this);
	this.store.on('load', function(){if(this.loadingMask) {this.loadingMask.hide();}}, this);
	
	// -- folderView ----------------------------------------------------
    this.folderView = new Sbi.browser.FolderView({
    	store: this.store
        , listeners: {
        	'click': {
    			fn: this.onClick
    			, scope: this
    		}
    	}
    	, emptyText: LN('sbi.browser.folderdetailpanel.emptytext')
    	, metaFolder: (config.browserConfig)?config.browserConfig.metaFolder:null
        , metaDocument: (config.browserConfig)?config.browserConfig.metaDocument:null	
    });
    
    // -- toolbar -----------------------------------------------------------
    this.displayToolbar = false;   
    this.toolbar = new Ext.Toolbar({hidden:true});
    var toolbarItems = [];
    
    var ttbarTextItem = null;
    if (Sbi.settings.browser.showBreadCrumbs !== undefined && Sbi.settings.browser.showBreadCrumbs){
    	this.displayToolbar = true;
    	ttbarTextItem = new Ext.Toolbar.TextItem('> ?');  
	    ttbarTextItem.isBreadcrumb = true;	   
	    toolbarItems.push(ttbarTextItem);
    }
    
    this.communities = new Array();
    
    Ext.Ajax.request({
        url: this.services['getCommunities'],
        callback : function(options , success, response){
  	  	if(success && response !== undefined) {   
	      		if(response.responseText == undefined) {
	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}else{
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if(content !== undefined) {
	      				this.createBannerHtml(content);
	      				this.communities = content.root;
		      		} 
	      		}
  	  	}
        }
       , scope: this
       , failure: Sbi.exception.ExceptionHandler.handleFailure  
     });
    
		
	this.bannerPanel = new Ext.Panel({
		layout: 'fit',
		baseCls:'list-actions-container',
	   	autoScroll: false,
	   	style:"float: left; width:100%;"
	});

	
	if (this.displayToolbar){		
	    toolbarItems.push('->');	 
	    this.toolbar = new Ext.Toolbar({     
	      items: toolbarItems
	    });
	    this.toolbar.breadcrumbs = new Array();
	    this.toolbar.breadcrumbs.push(ttbarTextItem);
	    this.toolbar.setVisible(true);
	}

    // -- mainPanel -----------------------------------------------------------
    this.mainPanel = new Ext.Panel({
         id:'doc-details'
        , cls: 'group-view' 
//        , autoHeight: true
        , collapsible: true
        , frame: true
        , autoHeight: false
        , autoScroll:true

        , items:[this.bannerPanel, this.folderView]        
//    	, tbar: (this.displayToolbar)?this.toolbar:[]
        , tbar: this.toolbar
        , listeners: {
		    'render': {
            	fn: function() {
            		this.loadingMask = new Sbi.decorator.LoadMask(this.mainPanel.body, {msg:LN('sbi.browser.folderdetailpanel.waitmsg')}); 
            	},
            	scope: this
          	}
        }        
    });
    
    var c = Ext.apply({}, config, {
      items: [this.mainPanel]
    });   
    
    Sbi.browser.FolderDetailPanel.superclass.constructor.call(this, c);   
    
    this.addEvents("onfolderload", "ondocumentclick", "beforeperformactionondocument", "onfolderclick", "beforeperformactiononfolder");
    if (Sbi.settings.browser.showBreadCrumbs !== undefined && Sbi.settings.browser.showBreadCrumbs){
    	 this.addEvents("onbreadcrumbclick");
    }
    //this.store.load();  
    
	//for "custom" Document Browser we have a defaultFolder id
    if (config.defaultFolderId != null){
        this.loadFolder(config.defaultFolderId, null);

    } else {
        this.loadFolder(config.folderId, config.folderId);
    }
    
    if (config.engineUrls.georeportServiceUrl != null){
        this.georeportServiceUrl = config.engineUrls.georeportServiceUrl;
    }
    
    if (config.engineUrls.cockpitServiceUrl != null){
        this.cockpitServiceUrl = config.engineUrls.cockpitServiceUrl;
    }
    
    if (config.engineUrls.worksheetServiceUrl != null){
        this.worksheetServiceUrl = config.engineUrls.worksheetServiceUrl;
    }
};




Ext.extend(Sbi.browser.FolderDetailPanel, Ext.Panel, {
	//constants
      DETAIL_DOCUMENT: 'DocumentDetailManagement'
    , CREATE_DOCUMENT: 'CreateDocument'    	  
    //variables
	, services: null
	, displayToolbar: null
    , store: null
    , toolbar: null
    , folderView: null
    , loadingMask: null
    , folderId: null
    , georeportServiceUrl: null
    , cockpitServiceUrl: null
    , worksheetServiceUrl: null
    
    , id:'this'
    , communities: null
    , fId: null
    
	, addNewDocument: function(type) {
		var urlToCall = '';
		if (type == undefined || type==''){
		  urlToCall = this.services['newDocument'];
			if(this.folderId != null){
				urlToCall += '&FUNCT_ID='+this.folderId;
			}
		}else if(type =='georeport'){					
			urlToCall =  this.georeportServiceUrl;
		}else if(type =='creategeoreport'){
			//call the dataset list (mydata gui)
			urlToCall = Sbi.config.contextName + '/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=true&TYPE_DOC=GEO&SBI_ENVIRONMENT=DOCBROWSER';						
		}else if(type =='createcockpit'){
			urlToCall =  this.cockpitServiceUrl +  '&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=' + Sbi.config.isTechnicalUser + "&documentMode=EDIT";
		}
		
		if (Sbi.settings.browser.typeLayout !== undefined && Sbi.settings.browser.typeLayout == 'card'){
			// for TIS
			var urlToCallBase = urlToCall.split("?")[0];
			var urlToCallParams = urlToCall.split("?")[1];
			urlToCallParams = Ext.urlDecode(urlToCallParams);
			urlToCallParams = Ext.util.JSON.encode(urlToCallParams);
			
			var url = this.services['createDocument'];
			url += "&urlToCallBase=" + urlToCallBase;
			url += "&urlToCallParams=" + urlToCallParams;
			//alert("url to call: " + url);
			window.location.href=url;	
		} else{
			window.location.href=urlToCall;	
		}
		
		
	}

	, filterStore: function(value) {
		this.folderView.inMemoryFilter(value);
	}
	
	, sortStore: function(value) {    					
		var sortEls = Ext.get('sortList').dom.childNodes;
		//move the selected value to the first element
		for(var i=0; i< sortEls.length; i++){
			if (sortEls[i].id == value){					
				sortEls[i].className = 'active';
				break;
			} 
		}
		//append others elements
		for(var i=0; i< sortEls.length; i++){
			if (sortEls[i].id !== value){
				sortEls[i].className = '';				
			}
		}
		
		this.folderView.inMemorySort(value);
	}	

    , loadFolder: function(folderId, rootFolderId, what) {

      this.folderId = folderId;	
      
      var p = {};
      
      if(folderId) {
    	  p.folderId = folderId;
      }
      
      if(rootFolderId) {
    	  p.rootFolderId = rootFolderId;
      }
      if(what && what != null && what == 'ALL'){
    	  p.folderId = this.fId;
      }
      
      this.store.proxy.conn.url = this.store.browseUrl;
      this.store.baseParams = p || {};
      this.store.load();
     
     
      Ext.Ajax.request({
          url: this.services['loadFolderPathService'],
          params: p,
          callback : function(options , success, response){
    	  	if(success && response !== undefined) {   
	      		if(response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if(content !== undefined) {
	      				if (this.displayToolbar){
	      					this.setBreadcrumbs(content);
	      				}	      				      				
	      				this.fireEvent('onfolderload', this);
	      				if (what != null){	      					
	      					var oldComm = Ext.get('ul-community').dom.childNodes;
	    					//disactive old communities
	    					for(var i=0; i< oldComm.length; i++){
	    						if (oldComm[i].id == what){
	    							oldComm[i].className = 'active first';
	    						} else {
	    							oldComm[i].className = '';
	    						}
	    					}
	    					
//	    					
//		      				var newComm = Ext.get(what);
//		      				Ext.fly(newComm).addClass('active first');
	      				}	
	      			} 
	      		} else {
	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
    	  	}
          },
          scope: this,
  		  failure: Sbi.exception.ExceptionHandler.handleFailure      
       });
    }

    , searchFolder: function(params) {       
    	this.store.proxy.conn.url = this.store.searchUrl;
        this.store.baseParams = params || {};
        this.store.load();  
        if (this.displayToolbar){
        	this.setTitle('Query results ...');
        }
    }
    
   
    , setTitle: function(title) {
    	this.resetToolbar();
    	this.toolbar.add({
    		xtype: 'tbtext'
    		, text: title || ''
    	});
    	this.toolbar.add('->');
    }
    
    , setBreadcrumbs: function(breadcrumbs) {
    	this.resetToolbar();
    	
    	this.toolbar.addSpacer();
    	this.toolbar.addDom('<image width="12" height="12" src="../themes/sbi_default/img/analiticalmodel/browser/server16x16.png"></image>');
    	this.toolbar.addSpacer();
    	
    	
    	var titleLn = LN('sbi.browser.document.functionalities');
    	if(breadcrumbs[0]){
    			breadcrumbs[0].name = titleLn;
    	}
    	
    	
        for (var i = 0; i < breadcrumbs.length - 1; i++) {
        	this.toolbar.add({
        		text: breadcrumbs[i].name
        		, breadcrumb: breadcrumbs[i]
        		, listeners: {
        			'click': {
                  		fn: this.onBreadCrumbClick,
                  		scope: this
                	} 
        	}
        	});
        	 
        	this.toolbar.addSpacer();
        	this.toolbar.addDom('<image width="3" height="6" src="../themes/sbi_default/img/analiticalmodel/execution/c-sep.gif"></image>');
        	this.toolbar.addSpacer();
        }

        this.toolbar.add({
    		text: breadcrumbs[breadcrumbs.length-1].name
    		, breadcrumb: breadcrumbs[breadcrumbs.length-1]
    		, disabled: true
    		, cls: 'sbi-last-folder'
    	});
        
        this.toolbar.add('->');
    }
    
    , resetToolbar: function() {
    	this.toolbar.items.each(function(item){            
            this.items.remove(item);
            item.destroy();           
        }, this.toolbar.items); 
    }   
    
    , onBreadCrumbClick: function(b, e) {    	
    	this.fireEvent('onbreadcrumbclick', this, b.breadcrumb, e);
    }
    
    
    // private methods 
    
    , onClick: function(dataview, i, node, e) {
    	var actionDetail = e.getTarget('a[class=detail]', 10, true);
    	var actionMetaData = e.getTarget('a[class=showmetadata]', 10, true);
        var actionDelete = e.getTarget('a[class=delete]', 10, true);
        var actionClone = e.getTarget('a[class=clone]', 10, true);
        var actionFavourite = e.getTarget('span.icon', 10, true); //TBD
        var actionExport = e.getTarget('a[class=export]', 10, true);
        var actionSchedule = e.getTarget('a[class=schedule]', 10, true);
        var actionViewMore = e.getTarget('a[class=viewMore]', 10, true);
        var actionViewLess = e.getTarget('a[class=viewLess]', 10, true);
      
    	var action = null;

    	if (actionDetail != null){
	     	Sbi.debug('view detail ');        	
	     	action = actionDetail.dom.className;
		 }else if (actionDelete != null){
	     	Sbi.debug('delete');        	
	     	action = actionDelete.dom.className;
		 }else if (actionClone != null){
	     	Sbi.debug('clone');        	
	     	action = actionClone.dom.className;
		 }else if (actionMetaData != null){
	     	Sbi.debug('showMetadata');        	
	     	action = actionMetaData.dom.className;
		 }else if (actionFavourite != null){
	     	Sbi.debug('favourites');        	
	     	action = actionFavourite.dom.className;
	     	//temporary until favourites aren't managed:
	     	return;
		 }else if (actionExport != null){
		     	Sbi.debug('export massive');        	
		     	action = actionExport.dom.className;
		 }else if (actionSchedule != null){
		     	Sbi.debug('schedule');        	
		     	action = actionSchedule.dom.className;
		}else if (actionViewMore != null){
	     	Sbi.debug('View More');        	
	     	action = actionViewMore.dom.className;
		}else if (actionViewLess != null){
	     	Sbi.debug('View Less');        	
	     	action = actionViewLess.dom.className;
	}    
    	
		 var r; 
		 if (node.id && node.id !== "" && node.id.indexOf("ext-gen") < 0){
			 r = this.folderView.getRecord(node.id);
		 }else{			 
			 r = this.folderView.getRecord(i);
		 }
	 
    	if(r.engine) {
    		if(action !== null) {
    			this.performActionOnDocument(r, action, node);
    		} else {
    			this.fireEvent('ondocumentclick', this, r, e);
    		}
    	} else{
    		if(action !== null) {
    			this.performActionOnFolder(r, action);
    		} else {
    			this.fireEvent('onfolderclick', this, r, e);
    		}
    	}      
    }
   
    , sort : function(groupName, attributeName) {
    	if(this.loadingMask) this.loadingMask.show();
    	//alert('sort: ' + groupName + ' - ' + attributeName);
    	this.folderView.sort(groupName, attributeName);
    	if(this.loadingMask) this.loadingMask.hide();
    }
    
    , group : function(groupName, attributeName) {
    	if(this.loadingMask) this.loadingMask.show();
    	//alert('group: ' + groupName + ' - ' + attributeName);
    	this.folderView.group(groupName, attributeName);
    	if(this.loadingMask) this.loadingMask.hide();
    }
    
    , filter : function(type) {
    	if(this.loadingMask) this.loadingMask.show();
    	//alert('filter: ' + type);
    	this.folderView.filter(type);
    	if(this.loadingMask) this.loadingMask.hide();
    }
    
    , deleteDocument: function(docId) {
    	
    	var p = {};
        
        if(docId) {
      	  p.docId = docId;
      	  p.folderId = this.folderId;
        }
        
    	Ext.Ajax.request({
             url: this.services['deleteDocument'],
             params: p,
             callback : function(options , success, response){
    			 //alert(options.params.docId));
	       	  	 if(success && response !== undefined) {   
	   	      		if(response.responseText !== undefined) {
	   	      			Ext.MessageBox.show({
		      				title: 'Status',
		      				msg: LN('sbi.browser.document.delete.success'),
		      				modal: false,
		      				buttons: Ext.MessageBox.OK,
		      				width:300,
		      				icon: Ext.MessageBox.INFO 			
		      			});
	   	      			this.loadFolder(this.folderId);
	   	      		} else {
	   	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	   	      		}
	       	  	}
             },
             scope: this,
     		 failure: Sbi.exception.ExceptionHandler.handleFailure      
       });
    }
    
    , cloneDocument: function(docId) {
    	
    	var p = {};
        
        if(docId) {
      	  p.docId = docId;
      	  p.folderId = this.folderId;
        }
        
    	Ext.Ajax.request({
             url: this.services['cloneDocument'],
             params: p,
             method: "POST",
             callback : function(options , success, response){
	       	  	 if(success && response !== undefined) {   
	   	      		if(response.responseText !== undefined) {
	   	      			Ext.MessageBox.show({
		      				title: 'Status',
		      				msg: LN('sbi.browser.document.clone.success'),
		      				modal: false,
		      				buttons: Ext.MessageBox.OK,
		      				width:300,
		      				icon: Ext.MessageBox.INFO 			
		      			});
	   	      			this.loadFolder(this.folderId);
	   	      		} else {
	   	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	   	      		}
	       	  	}
             },
             scope: this,
     		 failure: Sbi.exception.ExceptionHandler.handleFailure      
       });
    }
    
     , showDocumentMetadata: function(docId) {
        this.win_metadata = new Sbi.execution.toolbar.MetadataWindow({'OBJECT_ID': docId});
		this.win_metadata.show();
    }
    
    
    , performActionOnDocument: function(docRecord, action, node) {
    	if(this.fireEvent('beforeperformactionondocument', this, docRecord, action) !== false){
    		if(action === 'delete') {
    			Ext.MessageBox.confirm(
    					LN('sbi.generic.pleaseConfirm')
    					, LN('sbi.generic.confirmDelete')
    		            , function(btn, text) {
    		                if ( btn == 'yes' ) {
    		                	this.deleteDocument(docRecord.id);
    		                }
    					}
    					, this
    				);
    			
    		} else if(action === 'clone') {
    			Ext.MessageBox.confirm(
    					LN('sbi.generic.pleaseConfirm')
    					, LN('sbi.generic.confirmClone')
    		            , function(btn, text) {
    		                if ( btn == 'yes' ) {
    		                	this.cloneDocument(docRecord.id);
    		                }
    					}
    					, this
    				);
    			
    		} else if(action === 'showmetadata' && Sbi.user.functionalities.contains('SeeMetadataFunctionality')) {
    			//alert(docRecord.id + '; ' + this.folderId);
    			this.showDocumentMetadata(docRecord.id);
    		} else if(action === 'detail') {

				var urlToCall = this.services['detailDocument'];
				urlToCall += '&OBJECT_ID='+docRecord.id;
				window.location.href=urlToCall;	
    		}  
    			else if(action === 'viewMore') {
						if(node){
    					var innerHTML = node.innerHTML;
    					innerHTML = innerHTML.replace('synthesis-box-visible', "synthesis-box-hidden");
    					innerHTML	 = innerHTML.replace('detail-box-hidden', "detail-box-visible");
    					innerHTML	 = innerHTML.replace('box-container-browser-synthesis', "box-container-browser-detail");
    					node.innerHTML = innerHTML;
    					}
    		}
    			else if(action === 'viewLess') {
				if(node){
					var innerHTML = node.innerHTML;
					innerHTML = innerHTML.replace('synthesis-box-hidden','synthesis-box-visible');
					innerHTML	 = innerHTML.replace('detail-box-visible', 'detail-box-hidden' );
					innerHTML	 = innerHTML.replace('box-container-browser-detail','box-container-browser-synthesis');
					node.innerHTML = innerHTML;
				}
		}

    	}
    }
    
    , performActionOnFolder: function(dirRecord, action) {
    	if(this.fireEvent('beforeperformactiononfolder', this, dirRecord, action) !== false){
    	
    		var services = [];
    		var serviceConf = {
    			baseParams: new Object()
    		};
    		
    		if(action === 'export' && Sbi.user.functionalities.contains('SeeMetadataFunctionality')) {
    			serviceConf.serviceName = 'START_MASSIVE_EXPORT_THREAD_ACTION';
    			services['performFinishAction'] = Sbi.config.serviceRegistry.getServiceUrl(serviceConf);
    			var popupWin = new Sbi.browser.mexport.MassiveExportWizard({
    				functId : dirRecord.id
    				, functCd : dirRecord.code
    				, wizardType: 'export'
    				, services: services
    			});
    			popupWin.show();
    			popupWin.doLayout();
    		
    		} else if(action === 'schedule' && Sbi.user.functionalities.contains('SeeMetadataFunctionality')) {
    			serviceConf.serviceName = 'START_MASSIVE_SCHEDULE_ACTION';
    			services['performFinishAction'] = Sbi.config.serviceRegistry.getServiceUrl(serviceConf);
    			var popupWin = new Sbi.browser.mexport.MassiveExportWizard({
    				functId : dirRecord.id
    				, functCd : dirRecord.code
    				, wizardType: 'schedule'
    				, services: services
    			});
    			popupWin.show();
    			popupWin.doLayout();
    		}
    	}
    }
    
    , isAbleToCreateDocument: function(){
    	var funcs = Sbi.user.functionalities;
    	if (funcs == null || funcs == undefined) return false;
    	
    	for (f in funcs){
    		if (funcs[f] == this.CREATE_DOCUMENT){
    			return true;
    			break;
    		}
    	}
    	
    	return false;
    }
    
    , isAbleToManageDocument: function(){		
		var funcs = Sbi.user.functionalities;
		if (funcs == null || funcs == undefined) return false;
		
		for (f in funcs){
			if (funcs[f] == this.DETAIL_DOCUMENT){	    	    			
				return true;
				break;
			}
		}
		
		return false;
	} 
    
    
    , createBannerHtml: function(communities){
    	var communityString = '';
    	if (communities.root != undefined) {
	        for(i=0; i< communities.root.length; i++){
	        	var funct = communities.root[i].functId;
	        	communityString += '<li  id="'+ communities.root[i].name +'" ><a href="#" onclick="javascript:Ext.getCmp(\'this\').loadFolder('+funct+', null, \''+communities.root[i].name+'\')">';
	        	communityString += communities.root[i].name;
	        	communityString +='</a></li>';
	        }
    	}
        
        var createButton = '';
        if (Sbi.settings.browser.showCreateButton == true){
        	 if (this.isAbleToManageDocument()){
//             	createButton += ' <a id="newDocument" href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'\')" class="btn-add"><span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.generic.document')+'<span class="plus">+</span></a> ';
        		 createButton += 
        		' 		<ul class="create" id="newDocument"> '+ '<span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.generic.document')+'<span class="plus">+</span>' +
//        		'	         <li id="WS"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'createworksheet\')">'+ 'Ad Hoc Worksheet' +'</a> </li> '+
         		'	         <li id="GEO"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'creategeoreport\')">'+ LN('sbi.generic.document.add.adhocGeoReport') +'</a></li> '+
         		'	         <li id="COCKPIT"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'createcockpit\')">'+ LN('sbi.generic.document.add.adhocCockpit')+'</a></li> '+
         		'	         <li id="STANDARD"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'\')">'+ LN('sbi.generic.document.add.traditional SpagoBI') +'</a></li> '+
        		'	    </ul> ';
             }
        } else {
            if (this.isAbleToManageDocument()){
//            	createButton += ' <a id="newDocument" href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'\')" class="btn-add"><span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.generic.document')+'<span class="plus">+</span></a> ';
            	createButton += 
             		' 		<ul class="create" id="newDocument"> '+ '<span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.generic.document')+'<span class="plus">+</span>' +
//             		'	         <li id="WS"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'createworksheet\')">'+ 'Ad Hoc Worksheet' +'</a> </li> '+
             		'	         <li id="GEO"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'creategeoreport\')">'+ LN('sbi.generic.document.add.adhocGeoReport') +'</a></li> '+
             		'	         <li id="COCKPIT"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'createcockpit\')">'+ LN('sbi.generic.document.add.adhocCockpit')+'</a></li> '+
             		'	         <li id="STANDARD"><a href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'\')">'+ LN('sbi.generic.document.add.traditional SpagoBI') +'</a></li> '+
             		'	    </ul> ';
            }else  if (this.isAbleToCreateDocument()){
            	createButton += ' <a id="newDocument" href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'georeport\')"" class="btn-add"><span class="highlighted">'+LN('sbi.generic.load')+'</span> '+LN('sbi.generic.map')+'<span class="plus">+</span></a> ';
            } 
        }

        var bannerHTML = ''+
     		'<div class="main-maps-list"> ';
     	if (communities.root && communities.root.length>0){
     			bannerHTML += 
     			'		<ul id="ul-community" class="list-tab"> '+
	    		'	    	<li  id="ALL" class="active first"><a href="#" onclick="javascript:Ext.getCmp(\'this\').loadFolder(null, null, \'ALL\')">'+LN('sbi.generic.all')+'</a></li> '+
	    					communityString+
	//    		'	        <li class="favourite last"><a href="#">'+LN('sbi.browser.document.favourites')+'</a></li> '+
	    		'		</ul> ';
     	}
        bannerHTML +='  <div class="list-actions"> '+
    					createButton +
    		'	        <form action="#" method="get" class="search-form"> '+
    		'	            <fieldset> '+
    		'	                <div class="field"> '+
    		'	                    <input type="text" name="search" id="search" onclick="this.value=\'\'" onkeyup="javascript:Ext.getCmp(\'this\').filterStore(this.value)" value="'+LN('sbi.browser.document.searchKeyword')+'" /> '+
    		'	                </div> '+
    		'	                <div class="submit"> '+
    		'	                    <input type="text" value="Cerca"> '+
    		'	                </div> '+
    		'	            </fieldset> '+
    		'	        </form> '+
    		'	        <ul class="order" id="sortList"> '+
    		'	            <li id="creationDate" class="active"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'creationDate\')">'+LN('sbi.ds.moreRecent')+'</a> </li> '+
//    		'	            <li id="label"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'label\')">'+LN('sbi.ds.label')+'</a></li> '+
    		'	            <li id="name"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'name\')">'+LN('sbi.ds.name')+'</a></li> '+
    		'	            <li id="creationUser"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'creationUser\')">'+LN('sbi.ds.owner')+'</a></li> '+
    		'	        </ul> '+   		
    		'	    </div> '+
    		'</div>' ;
        var dh = Ext.DomHelper;
        var b = this.bannerPanel.getEl().update(bannerHTML);

    }
});


