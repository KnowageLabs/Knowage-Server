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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Chiara Chiarelli
 */
Ext.ns("Sbi.tools");

Sbi.tools.ManageDatasetVersions = function(config) { 
	
	this.severityStore = config.severityStore;

	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "DATASET_VERSION_DELETE"};
	var paramsClear = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "DATASET_ALL_VERSIONS_DELETE"};
	var paramsRes = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "DATASET_VERSION_RESTORE"};
	
	this.services = new Array();
	
	this.services['deleteAllDsVersionsService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_DATASETS_ACTION'
		, baseParams: paramsClear
	});
	
	this.services['deleteDsVersionService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_DATASETS_ACTION'
		, baseParams: paramsDel
	});
	
	this.services['restoreDsVersionService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_DATASETS_ACTION'
		, baseParams: paramsRes
	});

	// Let's pretend we rendered our grid-columns with meta-data from our ORM framework.
	//these are grid values for range type threshold value
	this.userColumns =  [
		{
		    name: 'dsId',
		    hidden: true
		}, {
		    name: 'versId',
		    hidden: true
		}, {
		    name: 'versNum',
		    hidden: true
		},{
	    	header: LN('sbi.ds.userIn'), 
	    	width: 180, 
			id:'userIn',
			sortable: true, 
			dataIndex: 'userIn' 
	    },{
	    	header: LN('sbi.generic.type'), 
	    	width: 110, 
			id:'type',
			sortable: true, 
			dataIndex: 'type' 
	    },{
	    	header: LN('sbi.ds.dateIn'), 
	    	width: 130, 
			id:'dateIn',
			sortable: true, 
			dataIndex: 'dateIn' 
	    }			
	];
    
	 var cm = new Ext.grid.ColumnModel({
	        columns: this.userColumns
	    });
	 
	 this.store = new Ext.data.JsonStore({
		    id : 'versId',
		    fields: ['dsId'
		            , 'versId'
		            , 'versNum'
		            , 'userIn'
     	            , 'type'
     	            , 'dateIn'
      	          ],
		    idIndex: 0,
		    data:{}
		});
	 
	 var tb = new Ext.Toolbar({
	    	buttonAlign : 'left',
	    	items:[new Ext.Toolbar.Button({
	            text: LN('sbi.ds.restore'),
	            iconCls: 'icon-restore',
	            handler: this.onRestore,
	            width: 30,
	            scope: this
	        }), '-', new Ext.Toolbar.Button({
	            text: LN('sbi.ds.deleteVersion'),
	            iconCls: 'icon-remove',
	            handler: this.onDelete,
	            width: 30,
	            scope: this
	        }), '-', new Ext.Toolbar.Button({
	            text: LN('sbi.ds.clearOldVersion'),
	            iconCls: 'icon-clear',
	            handler: this.onDeleteAll,
	            width: 30,
	            scope: this
	        })
	    	]
	    });
	 
		 var sm = new Ext.grid.RowSelectionModel({
	         singleSelect: true
	     });

	    // create the editor grid
	    var grid = {
	    	xtype: 'grid',
	        store: this.store,
	        layout: 'fit',
	        cm: cm,
	        sm: sm,
	        width: 370,
	        height: 300, //200,
	        frame: true,
	        autoscroll: true,
	        //clicksToEdit: 2,
	        tbar: tb
	    };

    var c = Ext.apply( {}, config, grid);

    // constructor
    Sbi.tools.ManageDatasetVersions.superclass.constructor.call(this, c);
    
    this.on('beforeedit', function(e) {
    	var t = Ext.apply({}, e);
		var col = t.column;
		this.currentRowRecordEdited = t.row;	 	
    }, this);
    
    this.on('afteredit', function(e) {   	
		var col = e.column;
		var row = e.row;	   	
    }, this);
    
    this.addEvents('verionrestored');	
    this.addEvents('verionsdeleted');	

};

Ext.extend(Sbi.tools.ManageDatasetVersions, Ext.grid.EditorGridPanel, {
  
	
	currentRowRecordEdited:null
  	,store:null
  	,userColumns:null

  	,loadItems: function(versions){
		this.store.loadData(versions);
	}

    ,onRestore: function () {
    	var rec = this.getSelectionModel().getSelected();
        var versNum = rec.get('versNum');
        var dsId =  rec.get('dsId');
        
        if (dsId != null && dsId!=undefined && dsId!=0 && versNum != null && versNum!=undefined && versNum!=0){       	
        	Ext.MessageBox.confirm(
    			LN('sbi.generic.pleaseConfirm'),
    			LN('sbi.generic.confirmRestore'),            
                function(btn, text) {
                    if (btn=='yes') {
                    	if (dsId != null) {	

    						Ext.Ajax.request({
    				            url: this.services['restoreDsVersionService'],
    				            params: {'dsId': dsId,'versNum':versNum},
    				            method: 'GET',
    				            success: function(response, options) {
    								if (response !== undefined) {
    									var content = Ext.util.JSON.decode(response.responseText);
    									var version = content.result;
    									this.fireEvent('verionrestored',version);
    									var deleteRow = this.getSelectionModel().getSelected();
    									this.store.remove(deleteRow);
    									this.store.commitChanges();
    								} else {
    									Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.deletingItemError'), LN('sbi.generic.serviceError'));
    								}
    				            },
    				            failure: function() {
    				                Ext.MessageBox.show({
    				                    title: LN('sbi.generic.error'),
    				                    msg: LN('sbi.generic.deletingItemError'),
    				                    width: 150,
    				                    buttons: Ext.MessageBox.OK
    				               });
    				            }
    				            ,scope: this
    			
    						});
    					} else {
    						Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.generic.error.msg'),LN('sbi.generic.warning'));
    					}
                    }
                },
                this
    		);
        }
    }
    
    ,onDelete: function() {   	
    	var rec = this.getSelectionModel().getSelected();
        var dsVersNum = rec.get('versNum');
        var dsId = rec.get('dsId');
        
        if (dsVersNum != null && dsVersNum!=undefined && dsVersNum!=0 ){       	
        	Ext.MessageBox.confirm(
    			LN('sbi.generic.pleaseConfirm'),
    			LN('sbi.generic.confirmDelete'),            
                function(btn, text) {
                    if (btn=='yes') {
						Ext.Ajax.request({
				            url: this.services['deleteDsVersionService'],
				            params: {'versNum': dsVersNum, 'dsId': dsId},
				            method: 'GET',
				            success: function(response, options) {
								if (response !== undefined) {
									var deleteRow = this.getSelectionModel().getSelected();
									this.store.remove(deleteRow);
									this.store.commitChanges();
									this.fireEvent('verionsdeleted');
								} else {
									Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.deletingItemError'), LN('sbi.generic.serviceError'));
								}
				            },
				            failure: function() {
				                Ext.MessageBox.show({
				                    title: LN('sbi.generic.error'),
				                    msg: LN('sbi.generic.deletingItemError'),
				                    width: 150,
				                    buttons: Ext.MessageBox.OK
				               });
				            }
				            ,scope: this
						});
                    }
                },
                this
    		);
        }
     }
    
    ,onDeleteAll: function() {   	
    	if(this.store.getAt(0)){
    		var itemId = this.store.getAt(0).get('dsId');
        
	        if (itemId != null && itemId!=undefined && itemId!=0){       	
	        	Ext.MessageBox.confirm(
	    			LN('sbi.generic.pleaseConfirm'),
	    			LN('sbi.generic.confirmDelete'),            
	                function(btn, text) {
	                    if (btn=='yes') {
	                    	if (itemId != null) {	
	
	    						Ext.Ajax.request({
	    				            url: this.services['deleteAllDsVersionsService'],
	    				            params: {'dsId': itemId},
	    				            method: 'GET',
	    				            success: function(response, options) {
	    								if (response !== undefined) {
	    									this.store.removeAll();
	    									this.store.commitChanges();
	    									this.fireEvent('verionsdeleted');
	    								} else {
	    									Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.deletingItemError'), LN('sbi.generic.serviceError'));
	    								}
	    				            },
	    				            failure: function() {
	    				                Ext.MessageBox.show({
	    				                    title: LN('sbi.generic.error'),
	    				                    msg: LN('sbi.generic.deletingItemError'),
	    				                    width: 150,
	    				                    buttons: Ext.MessageBox.OK
	    				               });
	    				            }
	    				            ,scope: this
	    			
	    						});
	    					} else {
	    						Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.generic.error.msg'),LN('sbi.generic.warning'));
	    					}
	                    }
	                },
	                this
	    		);
	        }else{
	        	this.store.removeAll();
	            this.store.commitChanges();
	        }
     }
   }
    
    ,getCurrentDsVersions: function(){
	    var arrayVers = new Array();
		var storeVers = this.getStore();
		var length = storeVers.getCount();
		for(var i = 0;i< length;i++){
			var item = storeVers.getAt(i);
			var data = item.data;
			arrayVers.push(data);
		}
		return arrayVers;
    }

});

