/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.execution.toolbar");

/**
 * Sbi.execution.toolbar.MetadataWindow
 * A popup window that shows document's metadata
 */
Sbi.execution.toolbar.MetadataWindow = function(config) {

	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	var defaultSettings = {
		id : 'win_metadata',
		title : LN('sbi.execution.metadata'),
		width : 650,
		height : 400,
		plain : true,
		modal: true,
		resizable: true,
		autoScroll : true	
	};
			
	if(Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.metadatawindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.metadatawindow);
	}		
			
	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);
	
	this.init();
    
    var buttons = [];
   if (Sbi.user.functionalities.contains('SaveMetadataFunctionality')) {
        buttons.push({
            text : LN('sbi.execution.metadata.savemeta')
            , scope : this
            , handler : this.saveMetadata
        });
    }
    
    c.buttons = this.buttons;
    c.items = this.generalMetadataPanel;
   
    this.buddy = undefined;
    
    // constructor
    Sbi.execution.toolbar.MetadataWindow.superclass.constructor.call(this, c);

    if (this.buddy === undefined) {
        this.buddy = new Sbi.commons.ComponentBuddy( {
            buddy : this
        });
    }

};

Ext.extend(Sbi.execution.toolbar.MetadataWindow, Ext.Window, {
	
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
    services : null
    , metadataStore : null
    , shortTextMetadataStore : null
    , longTextMetadataStore : null
    , generalMetadataStore: null
    , shortTextMetadataPanel : null
    , generalMetadataPanel : null
    
    // =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	/**
	 * @method 
	 * 
	 * Controls that the configuration object passed in to the class constructor contains all the compulsory properties. 
	 * If it is not the case an exception is thrown. Use it when there are properties necessary for the object
	 * construction for whom is not possible to find out a valid default value.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the config object received as input
	 */
	, validateConfigObject: function(config) {
		
	}

	/**
	 * @method 
	 * 
	 * Modify the configuration object passed in to the class constructor adding/removing properties. Use it for example to 
	 * rename a property or to filter out not necessary properties.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the modified version config object received as input
	 * 
	 */
	, adjustConfigObject: function(config) {
		
	}

	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	// ...
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	// ...

	//-----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function() {
		this.initServices();
		this.initStores();
	    this.initGeneralMetadataGridPanel();
	    this.initShortTextMetadataGridPanel();
	    this.initLongTextMetadataTabPanel();
	    this.initButtons();
	}
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - getMetadataService: read metadata from server
	 *    - saveMetadataService: write metadata to server
	 *    
	 */
	, initServices: function() {
		
		this.services = this.services || new Array();	
	
		var params = {
			LIGHT_NAVIGATOR_DISABLED : 'TRUE',
			OBJECT_ID: this.OBJECT_ID,
			SUBOBJECT_ID: this.SUBOBJECT_ID
		};
		
		this.services['getMetadataService'] = this.services['getMetadataService'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName : 'GET_METADATA_ACTION',
			baseParams : params
		});

		this.services['saveMetadataService'] = this.services['saveMetadataService'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName : 'SAVE_METADATA_ACTION',
			baseParams : params
		});
	}
	
	/**
	 * @method 
	 * 
	 * Initialize the metadata stores
	 */
	, initStores: function() {
		// store for general metadata
	    this.generalMetadataStore = new Ext.data.SimpleStore({
	        fields : ['meta_name', 'meta_content' ]
	    });

	    // store for short text metadata
	    this.shortTextMetadataStore = new Ext.data.SimpleStore({
	        fields : [ 'meta_id', 'meta_name', 'meta_content' ]
	    });
	    
	    // store for long text metadata
	    this.longTextMetadataStore = new Ext.data.SimpleStore({
	        fields : [ 'meta_id', 'meta_name', 'meta_content' ]
	    });
	    
	    // store for all metadata
	    this.metadataStore = new Ext.data.JsonStore({
	        autoLoad: false,
	        fields: [
	           'meta_id'
	           , 'biobject_id'
	           , 'subobject_id'
	           , 'meta_name'
	           , 'meta_type'
	           , 'meta_content'
	           , {name:'meta_creation_date', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}
	           , {name:'meta_change_date', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}
	        ]
	    	, url: this.services['getMetadataService']
	    });
	    this.metadataStore.on('load', this.onMetadataStoreLoaded, this);
	    this.metadataStore.load();
	}
	
	/**
	 * @method 
	 * 
	 * Refresh window content when metadata are loaded succesfully from server
	 */
    , onMetadataStoreLoaded: function () {
        
    	Sbi.trace("[MetadataWindow.onMetadataStoreLoaded] : IN");
    	
        for (var i = 0; i < this.metadataStore.getCount(); i++) {
        	
        	var record = this.metadataStore.getAt(i);
        	var metadataType = record.data.meta_type;
        	
        	if (metadataType == 'GENERAL_META') {
                this.generalMetadataStore.add(record);
            } else if (metadataType == 'SHORT_TEXT') {
                this.shortTextMetadataStore.add(record);
            } else if(metadataType == 'LONG_TEXT') {
            	
                this.longTextMetadataStore.add(record);
                var html = record.data.meta_content !== '' ? record.data.meta_content : '<br/>';
                
                var editablePanel = new Sbi.widgets.EditablePanel({
                    "title" : record.data.meta_name
                    , "html" : html
                    , "editable" : Sbi.user.functionalities.contains('SaveMetadataFunctionality')
                    , "fieldName" : record.data.meta_id
                });
                
                editablePanel.on('activate', function(panel) {
                    this.doLayout();
                }, this);
                editablePanel.on('changestate', function(panel) {
                	this.doLayout();
//                	var v = panel.htmlEditor.getValue();
//                	panel.collapse(false);
//                	panel.expand(false);
//                	panel.htmlEditor.setFocus();
//                	panel.htmlEditor.setValue(v);
                }, this);
                
                
                editablePanel.on('change', function(panel, newValue) {
                    var index = this.longTextMetadataStore.find('meta_id', panel.fieldName);
                    var record = this.longTextMetadataStore.getAt(index);
                    record.set('meta_content', newValue);
                }, this);
                
                this.longTextMetadataTabPanel.add(editablePanel);
            } else {
            	alert("Metadata type [" + metadataType + "] not recognized");
            }
        }
        
        if(this.shortTextMetadataStore.getCount() !== 0 ){
	    	this.add(this.shortTextMetadataPanel);
	    }
     
        if(this.longTextMetadataStore.getCount() !== 0 ){
    		 this.add(this.longTextMetadataPanel);
    	} 
	      
	    this.doLayout();
        
	    Sbi.trace("[MetadataWindow.onMetadataStoreLoaded] : OUT");
    }
    
    /**
	 * @method 
	 * 
	 * Initialize the panel containing technical metadata
	 */
    , initGeneralMetadataGridPanel : function() {
                     
        var generalMetadataGridPanel = new Ext.grid.GridPanel({
            store : this.generalMetadataStore,
            autoHeight : true,
            columns : [ {
                header : LN('sbi.execution.metadata.metaname'),
                width : 100,
                sortable : true,
                dataIndex : 'meta_name'
            }, {
                header : LN('sbi.execution.metadata.metavalue'),
                width : 540,
                sortable : true,
                dataIndex : 'meta_content',
                renderer: Ext.util.Format.htmlEncode
            } ],
            viewConfig : {
                forceFit : true,
                scrollOffset : 2
            }
        });

        this.generalMetadataPanel = new Ext.Panel({
            title : LN('sbi.execution.metadata.generalmetadata'),
            layout : 'fit',
            collapsible : true,
            collapsed : false,
            items : [ generalMetadataGridPanel ],
            autoWidth : true,
            autoHeight : true

        });
    }

    , initShortTextMetadataGridPanel : function() {
        
        var shortTextMetadataGridPanel = new Ext.grid.EditorGridPanel({
            store : this.shortTextMetadataStore,
            autoHeight : true,
            columns : [ {
                header : LN('sbi.execution.metadata.metaname'),
                width : 100,
                sortable : true,
                dataIndex : 'meta_name'
            }, {
                header : LN('sbi.execution.metadata.metavalue'),
                width : 540,
                sortable : true,
                dataIndex : 'meta_content',
                editor : Sbi.user.functionalities.contains('SaveMetadataFunctionality') ? new Ext.form.TextField({}) : undefined
            } ],
            viewConfig : {
                forceFit : true,
                scrollOffset : 2
            },
            singleSelect : true,
            clicksToEdit : 2
        });

        this.shortTextMetadataPanel = new Ext.Panel({
            title : LN('sbi.execution.metadata.shorttextmetadata'),
            layout : 'fit',
            collapsible : true,
            collapsed : false,
            items : [ shortTextMetadataGridPanel ],
            autoWidth : true,
            autoHeight : true
        });
    }
    
    , initLongTextMetadataTabPanel : function() {

        this.longTextMetadataTabPanel = new Ext.TabPanel({
            activeTab : 0
            //enableTabScroll : true
            //, autoScroll : true
            //, height: 210
            //, frame: false
        });

        this.longTextMetadataPanel = new Ext.Panel( {
            title : LN('sbi.execution.metadata.longtextmetadata'),
            layout : 'fit',
            collapsible : true,
            collapsed : false,
            items : [ this.longTextMetadataTabPanel ],
            height : 190
            //width: 620
            , animCollapse : false
        });
        
    }
    
    , initButtons: function() {
    	this.buttons = [];
    	if (Sbi.user.functionalities.contains('SaveMetadataFunctionality')) {
    		this.buttons.push({
    			text : LN('sbi.execution.metadata.savemeta')
    	        , scope : this
    	        , handler : this.saveMetadata
    	     });
    	}
    }

    , saveMetadata : function() {
        var modifiedRecords = new Array();
        modifiedRecords = modifiedRecords.concat(this.shortTextMetadataStore.getModifiedRecords());
        modifiedRecords = modifiedRecords.concat(this.longTextMetadataStore.getModifiedRecords());
        var modifiedMetadata = new Array();
        for (var i = 0; i < modifiedRecords.length; i++) {
            modifiedMetadata.push(modifiedRecords[i].data);
        }
        var params = {
            METADATA: Ext.util.JSON.encode(modifiedMetadata)
        };
        
        Ext.MessageBox.wait(LN('sbi.execution.metadata.waitmessage'), LN('sbi.execution.metadata.waittitle'));
        Ext.Ajax.request({
            url: this.services['saveMetadataService'],
            success: function(response, options) {
				if (response !== undefined) {
					Ext.MessageBox.hide();
					//this.shortTextMetadataStore.commitChanges();
					//var editablePanels = this.longTextMetadataTabPanel.items;
					//editablePanels.each(function() {this.commitChanges();});
					//for (var i = 0; i < editablePanels.getCount(); i++) {
					//	editablePanels.get(i).commitChanges();
					//}
					this.close();
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Error while saving Metadata', 'Service Error');
				}
            },
            failure: Sbi.exception.ExceptionHandler.handleFailure,    
            scope: this,
            params: params
        });
    }

});