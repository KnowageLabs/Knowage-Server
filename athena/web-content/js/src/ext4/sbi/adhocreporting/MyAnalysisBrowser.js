/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Marco Cortella (marco.cortella@eng.it)
 */

Ext.define('Sbi.adhocreporting.MyAnalysisBrowser', {
	extend : 'Ext.Panel'
	
	,
	config : {
		//id:'this',
		modelName : "Sbi.adhocreporting.MyAnalysisModel", 
		dataView : null,
		user : '',
		myAnalysisServicePath: '',
		autoScroll:true,
		displayToolbar: true,
		PUBLIC_USER: 'public_user',	    
	    qbeEditDatasetUrl : '',
	    treePanel : null,
	    docCommunity : null,
	    useCockpitEngine: null,
	    useWSEngine: null,
	    useQbeEngine: null,
	    useGeoEngine: null
	}

	,
	constructor : function(config) {

		this.initConfig(config);
		if (this.enginesAreAvailable()){
			this.initServices();
			this.initStore();		
			this.initToolbar();
			this.initViewPanel();
			this.items = [this.bannerPanel
			              ,this.viewPanel
			              ];
		}else{
			alert(LN('sbi.myanalysis.noEngines'));
		}
		this.callParent(arguments);
		
		this.addEvents('order');
		this.addEvents('openMyDataForReport');
		this.addEvents('openMyDataForGeo');
		this.addEvents('openCockpitDesigner');
	}
	
	/*
	 * Initialization Functions
	 */
	
	,initDefaultFilter: function(){
		if (Sbi.settings.myanalysis.defaultFilter != undefined){
			var defaultFilter = Sbi.settings.myanalysis.defaultFilter;
			this.activateFilter(defaultFilter);
		} 
	}
	
	
	,
	initServices : function(baseParams) {
		this.services = [];
		
		if(baseParams == undefined){
			baseParams ={};
		}
		
		baseParams.user = this.user;

		this.initDefaultFilter(); //initialize the correct 'list' service depending on the default filter set
		
		this.services['cloneDocument'] = Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'documents/clone'});
		this.services['shareDocument'] = Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'documents/share'});
		
		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
		this.services['deleteDocument'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'DELETE_OBJECT_ACTION'
			, baseParams: params
		});
		
		this.services['getCommunities'] =  Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'community/user'
				, baseParams: {
					LIGHT_NAVIGATOR_DISABLED: 'TRUE',
					EXT_VERSION: "3"
				}
		});

	}
	
	,
	initStore : function(baseParams) {
		Sbi.debug('MyAnalysis Browser bulding the store...');
		
		this.filteredProperties = [ "label","name","description","creationUser" ];
				
		this.sorters = [{property : 'creationDate', direction: 'DESC', description: LN('sbi.ds.moreRecent')}, 
		                {property : 'label', direction: 'ASC', description:  LN('sbi.ds.label')}, 
		                {property : 'name', direction: 'ASC', description: LN('sbi.ds.name')}, 				
						{property : 'creationUser', direction: 'ASC', description: LN('sbi.ds.owner')}];
		

		this.storeConfig = Ext.apply({
			model : this.getModelName(),
			filteredProperties : this.filteredProperties, 
			sorters: [],
			proxy: {
		        type: 'ajax'
		        , url: this.services["list"]
	         	, reader : {
	        		type : 'json',
	        		root : 'root'
	        	}
		     }
		}, {});

		// creates and returns the store
		Sbi.debug('MyAnalysis Browser store built.');

		this.store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);				
		this.store.load({});
		
		this.sortersCombo = this.createSortersStore({sorters: this.sorters});		
		
	}
	
	, initToolbar: function() {
		
		if (this.displayToolbar) {
			
			var bannerHTML = this.createBannerHtml({});
			this.bannerPanel = new Ext.Panel({
				height: 105,
				border:0,
			   	autoScroll: false,
			   	html: bannerHTML
			});			
		}
	}
	
	, initViewPanel: function() {
		var config = {};
		config.services = this.services;
		config.store = this.store;
		config.actions = this.actions;
		config.user = this.user;
		this.viewPanel = Ext.create('Sbi.adhocreporting.MyAnalysisView', config); 
		this.viewPanel.on('detail', this.modifyDocument, this);
		this.viewPanel.on('delete', this.deleteDocument, this);
		this.viewPanel.on('clone', this.cloneDocument, this);
		this.viewPanel.on('showMetadata', this.showMetadataDocument, this);
		this.viewPanel.on('share', this.shareDocument, this);
		this.viewPanel.on('executeDocument',function(docType, inputType,  record){
			this.fireEvent('executeDocument',docType, inputType,  record);
		},this);
	}
	
	
	/*
	 * ----------------------------------------------------------------------
	 * Private methods
	 * ----------------------------------------------------------------------
	 */
	
	, modifyDocument: function(rec){
		//TODO
		Sbi.exception.ExceptionHandler.showInfoMessage('TODO: Modify Document');
	}
	
	, deleteDocument: function(rec){
		var thisPanel = this;
		Ext.MessageBox.show({    
		    title: LN('sbi.generic.pleaseConfirm'),
		    msg: LN('sbi.generic.confirmDelete'),
		    buttons: Ext.Msg.YESNOCANCEL,
		    buttonText: {
		        yes: LN('sbi.myanalysis.delete.personalfolder'),
		        no: LN('sbi.myanalysis.delete.everywhere'),
		        cancel: LN('sbi.myanalysis.delete.cancel')
		    },
			fn: function(btn){
				
				if (btn != 'cancel'){
                	var p = {};
                    
                    if(rec.id) {
                  	  p.docId = rec.id;
                  	  p.folderId = rec.functionalities[0]; 
                  	  p.fromMyAnalysis = true;
                    }
                    
    				if ( btn == 'yes' ){
    					// delete only from Personal Folder of user
    					p.deleteOnlyFromPersonalFolder = true;
    				} else if ( btn == 'no' ) {
    					// delete everywhere
    					p.deleteOnlyFromPersonalFolder = false;
    				}
                    
                	Ext.Ajax.request({
                         url: thisPanel.services['deleteDocument'],
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
            	   	      			thisPanel.store.load({reset:true});										
            	   	      			thisPanel.viewPanel.refresh();
            	   	      		} else {
            	   	      			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.serviceResponseEmpty'), LN('sbi.generic.serviceError'));
            	   	      		}
            	       	  	}
                         },
                         scope: thisPanel,
                 		 failure: Sbi.exception.ExceptionHandler.handleFailure      
                   });
				}

			}
		});	
	}
	
	, cloneDocument: function(rec){		
		Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm')
				, LN('sbi.generic.confirmClone')
	            , function(btn, text) {
	                if ( btn == 'yes' ) {
	                	var p = {};
	                    
	                    if(rec.id) {
	                  	  p.docId = rec.id;
	                  	  p.folderId = rec.functionalities[0]; 
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
										this.store.load({reset:true});										
	            	   	      			this.viewPanel.refresh();
	            	   	      		} else {
	            	   	      			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.serviceResponseEmpty'), LN('sbi.generic.serviceError'));
	            	   	      		}
	            	       	  	}
	                         },
	                         scope: this,
	                 		 failure: Sbi.exception.ExceptionHandler.handleFailure      
	                   });
	                }
				}
				, this
			);

	}
	
	, showMetadataDocument: function(rec){
		var docId = rec.id;
		this.win_metadata = new Sbi.adhocreporting.MyAnalysisMetadataWindow({'OBJECT_ID': docId});
		this.win_metadata.show();
	}
	
	, shareDocument: function(rec){
		if (!rec.isPublic){			
			//share
		    this.treePanel =  Ext.create("Sbi.browser.DocumentsTree",{
		    	  columnWidth: 0.4,
		          border: false,
		          drawUncheckedChecks: true,
		          title: LN('sbi.browser.document.share.win.titleDetail')		         
		    });
		    
		 // The data store holding the communities
			var storeComm = Ext.create('Ext.data.Store', {
				proxy:{
					type: 'rest',
					url : this.services['getCommunities'],
					reader: {
						type: 'json',
						root: 'root'
					}
				},

				fields: [
				         "communityId",
				         "name",
				         "description",
				         "owner",
				         "functCode"]
			});
			storeComm.on("load", function(store) {
				var defaultData = {};
				store.insert(0, defaultData);
			}, this);
			
			storeComm.load();

			this.docCommunity = Ext.create('Ext.form.ComboBox', {
			    fieldLabel: LN('sbi.community.title'),
			    queryMode: 'local',
			    store: storeComm,
			    displayField: 'name',
			    valueField: 'functCode',
			    width: 300,
			    allowBlank: true,
			    editable: false,
			    padding: '10 0 0 0'
			});
		    
		    if(this.shareWindow != null){			
				this.shareWindow.destroy();
				this.shareWindow.close();
			}
			
			var shareWindowPanel = new Ext.form.FormPanel({
				layout: 'anchor', //'form',
				autoScroll: true,
				bodyStyle: 'padding:5px',
				defaults: {
		            xtype: 'textfield'
		        },

		        items: [this.treePanel, this.docCommunity]
			});
			
			this.shareWindow = new Ext.Window({
				modal		: true,
	            layout      : 'fit',
		        width		: 550,
		        height		: 310,
	            closeAction :'destroy',
	            plain       : true,
	            title		: LN('sbi.browser.document.share.win.title'),
	            items       : [shareWindowPanel],
	            buttons		: [{
				            	text    : LN('sbi.browser.document.share.win.btn')
				    			, tooltip : LN('sbi.browser.document.share.win.tooltip')
							    , scope : this
							    , handler : function() { this.makeDocumentShared(rec); }
				    	    }]
			});
			
			this.shareWindow.show();
	
		} else {
			//unshare
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm')
					, LN('sbi.generic.confirmUnshare')
		            , function(btn, text) {
		                if ( btn == 'yes' ) {
		                	var p = {};
		                    
		                    if(rec.id) {
		                  	  p.docId = rec.id
		                    }
		                    p.isShare = "false";  //is an unshare operation
		                    
		                    Ext.Ajax.request({
		                         url: this.services['shareDocument'],
		                         params: p,
		                         method: "POST",
		                         callback : function(options , success, response){
		            	       	  	 if(success && response !== undefined) {   
		            	   	      		if(response.responseText !== undefined) {		            	   	      			
											this.store.load({reset:true});										
		            	   	      			this.viewPanel.refresh();
			            	   	      		Ext.MessageBox.show({
		            		      				title: 'Status',
		            		      				msg: LN('sbi.browser.document.unshare.success'),
		            		      				modal: false,
		            		      				buttons: Ext.MessageBox.OK,
		            		      				width:300,
		            		      				icon: Ext.MessageBox.INFO 			
		            		      			});
		            	   	      		} else {
		            	   	      			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.serviceResponseEmpty'), LN('sbi.generic.serviceError'));
		            	   	      		}
		            	       	  	}
		                         },
		                         scope: this,
		                 		 failure: Sbi.exception.ExceptionHandler.handleFailure      
		                   });
		                }
					}
					, this
				);				
		}
	}
	
	, makeDocumentShared: function(rec) {
        if ((!this.treePanel.returnCheckedIdNodesArray() || this.treePanel.returnCheckedIdNodesArray().length == 0) &&
        		(!this.docCommunity || ! this.docCommunity.value || this.docCommunity.value == '' )){
        	Ext.MessageBox.show({
                title: LN('sbi.generic.warning'),
                msg:  LN('sbi.browser.document.functsMandatory'),
                width: 180,
                buttons: Ext.MessageBox.OK
           });
        	return;
        }
        
		Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm')
				, LN('sbi.generic.confirmShare')
	            , function(btn, text) {
	                if ( btn == 'yes' ) {
	                	var p = {};
	                    
	                    if(rec.id) {
	                  	  p.docId = rec.id;
	                    }
	                    p.isShare = "true"; //is a share operation
	                    p.functs =  Ext.JSON.encode(this.treePanel.returnCheckedIdNodesArray());
	                    p.communityId = this.docCommunity.getValue();
	                    
	                	Ext.Ajax.request({
	                         url: this.services['shareDocument'],
	                         params: p,
	                         method: "POST",
	                         callback : function(options , success, response){
	            	       	  	 if(success && response !== undefined) {   
	            	   	      		if(response.responseText !== undefined) {	            	   	      			        	   	      		
										this.store.load({reset:true});										
	            	   	      			this.viewPanel.refresh();
	            	   	      			this.shareWindow.close();
		            	   	      		Ext.MessageBox.show({
	            		      				title: 'Status',
	            		      				msg: LN('sbi.browser.document.share.success'),
	            		      				modal: false,
	            		      				buttons: Ext.MessageBox.OK,
	            		      				width:300,
	            		      				icon: Ext.MessageBox.INFO 			
	            		      			});	    
	            	   	      		} else {
	            	   	      			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.serviceResponseEmpty'), LN('sbi.generic.serviceError'));
	            	   	      		}
	            	       	  	}
	                         },
	                         scope: this,
	                 		 failure: Sbi.exception.ExceptionHandler.handleFailure      
	                   });
	                }
				}
				, this
			);
	}
	
	
	, createSortersStore: function(config){		
		var ordersStore = Ext.create('Ext.data.Store', {
		    fields: ["property","direction","description"],
		    data : config.sorters
		});
    	
		ordersStore.load();
    	
    	return ordersStore;
	}
	
	, createBannerHtml: function(communities){
    	var communityString = '';
  
        var createButton = '';    	
        if (this.user !== '' && this.user !== this.PUBLIC_USER ){
        	createButton += ' <a id="newDocument" href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDocument(\'\')" class="btn-add"><span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.myanalysis.analysis')+'<span class="plus">+</span></a> ';
        }
        
   
        var activeClass = '';
        var bannerHTML = ''+
     		'<div class="main-datasets-list"> '+
    		'    <div class="list-actions-container"> '+ //set into the container panel
    		'		<ul class="list-tab" id="list-tab"> ';
        if (Sbi.settings.myanalysis.showReportFilter &&  (this.useWSEngine == true && this.useQbeEngine == true)){	
        	if (Sbi.settings.myanalysis.defaultFilter == 'Report'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
        	bannerHTML = bannerHTML+	
        	'	    	<li class="first '+activeClass+'" id="Report"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDocument( \'Report\')">'+LN('sbi.myanalysis.report')+'</a></li> '; 
        }	
        if (Sbi.settings.myanalysis.showCockpitFilter &&  this.useCockpitEngine == true){
        	if (Sbi.settings.myanalysis.defaultFilter == 'Cockpit'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
        	bannerHTML = bannerHTML+	
    		'	    	<li class="'+activeClass+'" id="Cockpit"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDocument( \'Cockpit\')">'+LN('sbi.myanalysis.cockpit')+'</a></li> ';    
        }
         if (Sbi.settings.myanalysis.showMapFilter  &&  this.useGeoEngine == true){
         	if (Sbi.settings.myanalysis.defaultFilter == 'Map'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
         	bannerHTML = bannerHTML+	
     		'	    	<li class="'+activeClass+'" id="Map"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDocument( \'Map\')">'+LN('sbi.myanalysis.map')+'</a></li> ';    	
         }
         if (Sbi.settings.myanalysis.showAllFilter){
          	if (Sbi.settings.myanalysis.defaultFilter == 'All'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
          	bannerHTML = bannerHTML+	
    		'	    	<li id="All" class="last '+activeClass+'"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDocument( \'All\')">'+LN('sbi.myanalysis.all')+'</a></li> ';    		    		    		    		        	 
         }

        bannerHTML = bannerHTML+
            '		</ul> '+
    		'	    <div id="list-actions" class="list-actions"> '+
    					createButton +
    		'	        <form action="#" method="get" class="search-form"> '+
    		'	            <fieldset> '+
    		'	                <div class="field"> '+
    		'	                    <label for="search">'+LN('sbi.browser.document.searchDatasets')+'</label> '+
    		'	                    <input type="text" name="search" id="search" onclick="this.value=\'\'" onkeyup="javascript:Ext.getCmp(\'this\').filterStore(this.value)" value="'+LN('sbi.browser.document.searchKeyword')+'" /> '+
    		'	                </div> '+
    		'	                <div class="submit"> '+
    		'	                    <input type="text" value="Cerca" /> '+
    		'	                </div> '+
    		'	            </fieldset> '+
    		'	        </form> '+
    		'	         <ul class="order" id="sortList">'+
    		'	            <li id="dateIn" class="active"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'creationDate\')">'+LN('sbi.ds.moreRecent')+'</a> </li> '+
    		'	            <li id="name"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'name\')">'+LN('sbi.ds.name')+'</a></li> '+
    		'	            <li id="owner"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'creationUser\')">'+LN('sbi.ds.owner')+'</a></li> '+
    		'	        </ul> '+
    		'	    </div> '+
    		'	</div> '+
    		'</div>' ;


        return bannerHTML;
    }
	
	
	, addNewDocument : function() {		 
		var config =  {};
		config.user = this.user;
		config.useCockpitEngine = this.useCockpitEngine;
		config.useWSEngine = this.useWSEngine;
		config.useQbeEngine = this.useQbeEngine;
		config.useGeoEngine = this.useGeoEngine;
		config.isNew = true;
	
		this.wizardWin =  Ext.create('Sbi.adhocreporting.MyAnalysisWizard',config);	
		//Event for Opening MyData Page only for creating a Report( WS or Qbe)
		this.wizardWin.on('openMyDataForReport',function(){
			this.fireEvent('openMyDataForReport');
		},this);
		//Event for Opening MyData Page only for creating a Geo Document
		this.wizardWin.on('openMyDataForGeo',function(){
			this.fireEvent('openMyDataForGeo');
		},this);
		//Event for Opening Cockpit designer only for creating a Cockpit
		this.wizardWin.on('openCockpitDesigner',function(){
			this.fireEvent('openCockpitDesigner');
		},this);
    	this.wizardWin.show();
	}
	
	/*
	 * 	Show only the document of the passed type
	 */
	, showDocument: function(documentType) {
		//alert('Show Document of type '+documentType);
		
		var tabEls = Ext.get('list-tab').dom.childNodes;
		
		//Change active dataset type on toolbar
		for(var i=0; i< tabEls.length; i++){
			//nodeType == 1 is  Node.ELEMENT_NODE
			if (tabEls[i].nodeType == 1){
				if (tabEls[i].id == documentType){					
					tabEls[i].className += ' active '; //append class name to existing others
				} else {
					tabEls[i].className = tabEls[i].className.replace( /(?:^|\s)active(?!\S)/g , '' ); //remove active class
				}
			}
		}
		//Change content of DatasetView
		this.activateFilter(documentType);
		
		this.storeConfig = Ext.apply({
			model : this.getModelName(),
			filteredProperties : this.filteredProperties, 
			sorters: [],
			proxy: {
		        type: 'ajax'
		        , url: this.services["list"]
	         	, reader : {
	        		type : 'json',
	        		root : 'root'
	        	}
		     }
		}, {});

		this.store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);				
		
		//load store and refresh datasets view
		this.store.load(function(records, operation, success) {
		    //console.log('***MYANALYSIS BROWSER loaded records***');
		});
		this.viewPanel.bindStore(this.store);
		this.viewPanel.refresh();
		
	}
	
	, activateFilter: function(documentType){
		if (documentType == 'All'){			
			baseParams ={};
			baseParams.user = this.user;
			baseParams.docType = 'All';

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : this.myAnalysisServicePath,
				baseParams : baseParams
			});
			
			
		} else if (documentType == 'Map'){			
			baseParams ={};
			baseParams.user = this.user;
			baseParams.docType = 'Map';

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : this.myAnalysisServicePath,
				baseParams : baseParams
			});
	
			
		} else if (documentType == 'Cockpit'){
			baseParams ={};
			baseParams.user = this.user;
			baseParams.docType = 'Cockpit';

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : this.myAnalysisServicePath,
				baseParams : baseParams
			});
		
			
		} else if (documentType == 'Report'){
			baseParams ={};
			baseParams.user = this.user;
			baseParams.docType = 'Report';

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : this.myAnalysisServicePath,
				baseParams : baseParams
			});
		
		}
	}
	
	/**
	 * Opens the loading mask 
	*/
    , showMask : function(){
    	this.un('afterlayout',this.showMask,this);
    	if (this.loadMask == null) {    		
    		this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "  Wait...  "});
    	}
    	if (this.loadMask){
    		this.loadMask.show();
    	}
    }

	/**
	 * Closes the loading mask
	*/
	, hideMask: function() {
    	if (this.loadMask && this.loadMask != null) {	
    		this.loadMask.hide();
    	}
	} 
	
	, filterStore: function(filterString) {
		this.store.load({filterString: filterString});
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
		

		for (sort in this.sorters){
			var s = this.sorters[sort];
			if (s.property == value){
				this.store.sort(s.property, s.direction);
				break;
			}
		}
		
		this.viewPanel.refresh();
	}	

	, enginesAreAvailable: function(){
		return (this.useCockpitEngine == true || this.useWSEngine == true || 
				 this.useGeoEngine == true)
	}
	
});	