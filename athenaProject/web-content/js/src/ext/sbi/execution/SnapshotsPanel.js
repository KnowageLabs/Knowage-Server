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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.execution");

Sbi.execution.SnapshotsPanel = function(config, doc) {
	
	var c = Ext.apply({
		showTitle: true
	}, config || {});
	
	// always declare exploited services first!
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
	this.services = new Array();
	this.services['getSnapshotsService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_SNAPSHOTS_ACTION'
		, baseParams: params
	});
	this.services['deleteSnapshotsService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_SNAPSHOTS_ACTION'
		, baseParams: params
	});
	
	this.snapshotPreference = c.snapshot;
	
	this.selectedSnapshotId = null;
	
    this.snapshotsStore = new Ext.data.JsonStore({
        root: 'results'
        , idProperty: 'id'
        , fields: ['id', 'name', 'description', {name:'creationDate', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}]
		, url: this.services['getSnapshotsService']
    });
    
    this.executeColumn = new Ext.grid.ButtonColumn({
	       header:  '',
	       dataIndex: 'delete',
	       iconCls: 'icon-execute',
	       clickHandler: function(e, t) {
	          var index = this.grid.getView().findRowIndex(t);
	          var selectedRecord = this.grid.snapshotsStore.getAt(index);
	          var snapshotId = selectedRecord.get('id');
	          this.grid.fireEvent('executionrequest', snapshotId);
	       },
	       width: 25,
	       renderer : function(v, p, record){
	           return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
	       }
	});
    
    this.sm = new Ext.grid.CheckboxSelectionModel();
    
    this.tbar = null;
    
    if (Sbi.user.functionalities.contains('DocumentAdminManagement')) {
    	this.tbar = [
         '->'
         , {
      	   text: LN('sbi.execution.snapshots.deleteSelected')
      	   , tooltip: LN('sbi.execution.snapshots.deleteSelectedTooltip')
      	   , iconCls:'icon-remove'
      	   , scope: this
      	   , handler : this.deleteSelectedSnapshots
         }
      ];
    }
    
    this.shortcutsHiddenPreference = config.shortcutsHidden !== undefined ? config.shortcutsHidden : false;
    
    this.isHidden = !Sbi.user.functionalities.contains('SeeSnapshotsFunctionality')
    				||
    				this.shortcutsHiddenPreference;
    				
	c = Ext.apply({}, c, {
        store: this.snapshotsStore
        , columns: [
            {id: "id", header: "Id", sortable: true, width: 50, dataIndex: 'id',  hidden: true}
            , {header: LN('sbi.execution.snapshots.name'), sortable: true,  width: 50, dataIndex: 'name'}
            , {header: LN('sbi.execution.snapshots.description'), sortable: true,  width: 50, dataIndex: 'description'}
            , {header: LN('sbi.execution.snapshots.creationDate'), sortable: true,  width: 50,dataIndex: 'creationDate', renderer: Ext.util.Format.dateRenderer(Sbi.config.localizedDateFormat)} 
            , this.executeColumn
            , this.sm
        ]
        , plugins: this.executeColumn
		, viewConfig: {
        	forceFit: true
        	, emptyText: LN('sbi.execution.snapshots.emptyText')
		}
		, tbar: this.tbar
        , collapsible: false
        , title: c.showTitle === true? LN('sbi.execution.snapshots.title') : undefined
        , autoScroll: true
        , sm : this.sm
        , height: 200
        , hidden: this.isHidden
	});
	
	// constructor
    Sbi.execution.SnapshotsPanel.superclass.constructor.call(this, c);
    
    this.on('rowdblclick', this.onRowDblClick, this);
    
    this.addEvents('executionrequest', 'ready');
    
};

Ext.extend(Sbi.execution.SnapshotsPanel, Ext.grid.GridPanel, {
	
	services: null
	, snapshotsStore: null
	, executionInstance: null
	, selectedSnapshotId: null
	   
    // public methods
	
	, synchronize: function( executionInstance ) {
		
		this.executionInstance = executionInstance;
		if (this.isHidden === false) {
			this.snapshotsStore.load({params: executionInstance});
			// when snapshots are loaded, must check if there a preference for a snapshot execution
			this.snapshotsStore.on(
				'load', 
				this.checkPreferences,
				this
			);
		} else {
			// must fire 'ready' event to inform that the panel is ready (see listeners in ParametersSelectionPage.js)
			this.fireEvent('ready', null);
		}

	}

	

	, deleteSelectedSnapshots: function() {
		var recordsSelected = this.getSelectionModel().getSelections();
		if (recordsSelected && recordsSelected.length > 0) {
			var ids = new Array();
			for (var count = 0; count < recordsSelected.length; count++) {
				ids[count] = recordsSelected[count].get('id');
			}
			var idsJoined = ids.join(',');
	
			Ext.Ajax.request({
		        url: this.services['deleteSnapshotsService'],
		        params: {'SBI_EXECUTION_ID': this.executionInstance.SBI_EXECUTION_ID, 'id': idsJoined},
		        callback : function(options , success, response) {
		  	  		if(success) {
			      		if (response && response.responseText !== undefined) {
			      			var content = Ext.util.JSON.decode( response.responseText );
			      			if (content !== undefined && content.result == 'OK') {
				  	  			// removes the snapshots from the store
				  	  			for (var count = 0; count < recordsSelected.length; count++) {
				  	  				this.snapshotsStore.remove(recordsSelected[count]);
				  	  			}
			      			} else {
				      			Sbi.exception.ExceptionHandler.showErrorMessage('Error while deleting scheduled executions', 'Service Error');
				      		}
			      		} else {
			      			Sbi.exception.ExceptionHandler.showErrorMessage('Error while deleting scheduled executions', 'Service Error');
			      		}
		  	  		} else {
		  	  			Sbi.exception.ExceptionHandler.showErrorMessage('Cannot detele scheduled executions', 'Service Error');
		  	  		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure      
			});
		} else {
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.execution.snapshots.noSnapshotsSelected'), 'Warning');
		}
	}
	
	, findByNameAndHistoryNumber: function(name, historyNumber) {
		var historyCounter = -1;
		for (var count = 0; count < this.snapshotsStore.getCount(); count++) {
			var aRecord = this.snapshotsStore.getAt(count);
			if (aRecord.get('name') == name) {
				historyCounter++;
				if (historyCounter == historyNumber) {
					return aRecord;
				}
			}
		}
		return null;
	}
	
	//private methods
	
	, onRowDblClick: function(grid, rowIndex, event) {
		var selectedRecord =  grid.getStore().getAt(rowIndex);
		var snapshotId = selectedRecord.get('id');
		this.fireEvent('executionrequest', snapshotId);
	}
	
	, checkPreferences: function () {
		var snapshotId = null;
		if (this.snapshotPreference !== undefined && this.snapshotPreference.name !== undefined) {
			// get the required snapshot from the store
			var record = this.findByNameAndHistoryNumber(this.snapshotPreference.name, this.snapshotPreference.historyNumber);
			if (record != null) {
				snapshotId = record.get('id');
		    	//this.fireEvent('executionrequest', snapshotId);
			} else {
				Sbi.exception.ExceptionHandler.showErrorMessage('Scheduled execution \'' + this.snapshotPreference.name + '\' not found ', 'Configuration Error');
			}
			// reset preference variable
			delete this.snapshotPreference;
		}
		// remove the listener
		this.snapshotsStore.un(
				'load', 
				this.checkPreferences,
				this
		);
		// tells that the snapshots panel has been loaded and (eventually) the snapshot specified by the preferences
		this.fireEvent('ready', snapshotId);
	}
	
});