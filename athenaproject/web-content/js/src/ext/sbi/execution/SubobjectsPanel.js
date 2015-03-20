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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.execution");

Sbi.execution.SubobjectsPanel = function(config, doc) {
	
	var c = Ext.apply({
		// defaults
		showTitle: true
	}, config || {});
	
	// always declare exploited services first!
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
	this.services = new Array();
	this.services['getSubObjectsService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_SUBOBJECTS_ACTION'
		, baseParams: params
	});
	this.services['deleteSubObjectsService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_SUBOBJECTS_ACTION'
		, baseParams: params
	});
	//this.subobjectPreference = c.subobject;
	this.executionInstance = null;
	//this.selectedSubObjectId = null;
	
    this.subObjectsStore = new Ext.data.JsonStore({
        root: 'results'
        , idProperty: 'id'
        , fields: ['id', 'name', 'description', 'owner', 
                   {name:'creationDate', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}, 
                   {name:'lastModificationDate', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}, 
                   'visibility']
		, url: this.services['getSubObjectsService']
    });
    
    // need to put subobject id
    this.idGlob; 
    
     this.executeMetadataColumn = new Ext.grid.ButtonColumn({
	       header:  '',
	       dataIndex: 'metadata',
	       iconCls: 'icon-metadata',
	       clickHandler: function(e, t) {
	          var index = this.grid.getView().findRowIndex(t);
	          var selectedRecord = this.grid.subObjectsStore.getAt(index);
	          var subObjectId = selectedRecord.get('id');
	          this.grid.fireEvent('showmetadatarequest', subObjectId);
	       },
	       width: 25,
	       renderer : function(v, p, record){
	           return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
	       }
	});
    
    this.executeColumn = new Ext.grid.ButtonColumn({
	       header:  '',
	       dataIndex: 'delete',
	       iconCls: 'icon-execute',
	       clickHandler: function(e, t) {
	          var index = this.grid.getView().findRowIndex(t);
	          var selectedRecord = this.grid.subObjectsStore.getAt(index);
	          var subObjectId = selectedRecord.get('id');
	          this.grid.fireEvent('executionrequest', subObjectId);
	       },
	       width: 25,
	       renderer : function(v, p, record){
	           return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
	       }
	});
    
    var isUserAbleToDeleteSubObjects = 
    	Sbi.user.functionalities.contains('SaveSubobjectFunctionality') &&
    	(doc.typeCode != 'DATAMART' || Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality'));
    
    
    
    var columns = [
       {id: "id", header: "Id", sortable: true, dataIndex: 'id',  hidden: true}
       , {header: LN('sbi.execution.subobjects.name'), sortable: true, dataIndex: 'name', renderer: Ext.util.Format.htmlEncode}
       , {header: LN('sbi.execution.subobjects.description'), sortable: true, dataIndex: 'description', renderer: Ext.util.Format.htmlEncode}
       , {header: LN('sbi.execution.subobjects.owner'), sortable: true, dataIndex: 'owner'}
       , {header: LN('sbi.execution.subobjects.creationDate'), sortable: true, dataIndex: 'creationDate', renderer: Ext.util.Format.dateRenderer(Sbi.config.localizedDateFormat)} 
       , {header: LN('sbi.execution.subobjects.lastModificationDate'), sortable: true, dataIndex: 'lastModificationDate', renderer: Ext.util.Format.dateRenderer(Sbi.config.localizedDateFormat)} 
       , {
       	header: LN('sbi.execution.subobjects.visibility')
       	, sortable: true
       	, dataIndex: 'visibility'
       	, renderer: function(val) {
       		return val? LN('sbi.execution.subobjects.visibility.public'): LN('sbi.execution.subobjects.visibility.private')
       	}
       }
       , this.executeColumn
    ];
    
    if(Sbi.user.functionalities.contains('SeeMetadataFunctionality')){
       columns.push(this.executeMetadataColumn);
    }
    
    var tbar = undefined;
    
    if (isUserAbleToDeleteSubObjects) {
    	this.sm = new Ext.grid.CheckboxSelectionModel();
    	columns.push(this.sm);
    	tbar = [
		    '->'
		    , {
		 	   text: LN('sbi.execution.subobjects.deleteSelected')
		 	   , tooltip: LN('sbi.execution.subobjects.deleteSelectedTooltip')
		 	   , iconCls:'icon-remove'
		 	   , scope: this
		 	   , handler : this.deleteSelectedSubObjects
		    }
    	];
    } else {
    	this.sm = new Ext.grid.RowSelectionModel({singleSelect:false});
    }
    
    this.shortcutsHiddenPreference = config.shortcutsHidden !== undefined ? config.shortcutsHidden : false;
    
    this.isHidden = !Sbi.user.functionalities.contains('SeeSubobjectsFunctionality')
        			||
    				this.shortcutsHiddenPreference;
    
	c = Ext.apply({}, c, {
        store: this.subObjectsStore
        , columns: columns
        , plugins: Sbi.user.functionalities.contains('SeeMetadataFunctionality') ? [this.executeColumn,this.executeMetadataColumn] : this.executeColumn
		, viewConfig: {
        	forceFit: true
        	, emptyText: LN('sbi.execution.subobjects.emptyText')
		}
        , tbar: tbar
        , collapsible: false
        , title: c.showTitle === true? LN('sbi.execution.subobjects.title'): undefined
        , autoScroll: true
        , sm : this.sm
        , height: 200
        , hidden: this.isHidden
	});
	
	// constructor
    Sbi.execution.SubobjectsPanel.superclass.constructor.call(this, c);
    
    this.on('rowdblclick', this.onRowDblClick, this);
    
    this.addEvents('executionrequest', 'ready', 'showmetadatarequest');
    
};

Ext.extend(Sbi.execution.SubobjectsPanel, Ext.grid.GridPanel, {
	
	services: null
	, subObjectsStore: null
	, sm: null
	, executionInstance: null
	//, selectedSubObjectId: null
	   
    // public methods
	
	, synchronize: function( executionInstance ) {	
		this.executionInstance = executionInstance;
		if (this.isHidden === false) {
			this.subObjectsStore.on(
				'load', 
				function() {
				this.fireEvent('ready');
				},
				this
			);
			this.subObjectsStore.load({params: executionInstance});
		} else {
			// must fire 'ready' event to inform that the panel is ready (see ParametersSelectionPage.js)
			this.fireEvent('ready');
		}		
	}
	// called when saving a subobject, set listener when datastore loading is end to open metadata window
	, openMetadataWindowAfterSaving: function( id, executionInstance ) {
		this.idGlob = id;
			this.on(
				'ready', 
				this.openMetadataWindowAfterSavingFunction,
				this
			);

	}
	// opens metadata window by calling the metadata event on the just inserted subobject id
	, openMetadataWindowAfterSavingFunction: function( id, executionInstance ) {
				// erase listener
			this.un(
				'ready', 
				this.openMetadataWindowAfterSavingFunction,
				this
			);
	
			// search for the index of record with right id	
			var index = this.subObjectsStore.find('id', this.idGlob );
			var record = this.subObjectsStore.getAt(index);
	        var subObjectId = record.get('id');
			// call the metadata event on the just inserted subobject id
	        this.fireEvent('showmetadatarequest', subObjectId);
	}

	, deleteSelectedSubObjects: function() {
		var recordsSelected = this.getSelectionModel().getSelections();
		if (recordsSelected && recordsSelected.length > 0) {
			var ids = new Array();
			for (var count = 0; count < recordsSelected.length; count++) {
				// check if user is able to delete subobject
				var aRecord = recordsSelected[count];
				if (!Sbi.user.functionalities.contains('DocumentAdminManagement') && aRecord.get('owner') != Sbi.user.userId) {
					Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.execution.subobjects.cannotDeleteSubObject') + ' \'' + aRecord.get('name') + '\'', 'Warning');
					return;
				}
				ids[count] = aRecord.get('id');
			}
			var idsJoined = ids.join(',');
			
			Ext.Ajax.request({
		        url: this.services['deleteSubObjectsService'],
		        params: {'SBI_EXECUTION_ID': this.executionInstance.SBI_EXECUTION_ID, 'id': idsJoined},
		        success : function(response, options) {
		      		if (response && response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if (content !== undefined && content.result == 'OK') {
			  	  			// removes the subobjects from the store
			  	  			for (var count = 0; count < recordsSelected.length; count++) {
			  	  				this.subObjectsStore.remove(recordsSelected[count]);
			  	  			}
		      			} else {
			      			Sbi.exception.ExceptionHandler.showErrorMessage('Error while deleting customized views', 'Service Error');
			      		}
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Error while deleting customized views', 'Service Error');
		      		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure      
			});
		} else {
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.execution.subobjects.noSubObjectsSelected'), 'Warning');
		}
	}
	
	// private methods
	
	, onRowDblClick: function(grid, rowIndex, event) {
    	var selectedRecord =  grid.getStore().getAt(rowIndex);
    	var subObjectId = selectedRecord.get('id');
    	this.fireEvent('executionrequest', subObjectId);
    }
	
});