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
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.widgets");

Sbi.widgets.CatalogueVersionsGridPanel = function(config) {

	var defaultSettings = {
		title: LN('sbi.widgets.catalogueversionsgridpanel.title')
	};

	if (Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.catalogueversionsgridpanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.widgets.catalogueversionsgridpanel);
	}

	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
        store 			: this.store
        , cm 			: this.cm
        , sm 			: this.sm
        , frame 		: true
        , autoscroll 	: true
        , tbar			: this.tb
        , view			: this.gridView
        , plugins		: [ this.downloadColumn ]
	}); 

    // constructor
    Sbi.widgets.CatalogueVersionsGridPanel.superclass.constructor.call(this, c);
    
    this.on('rowdblclick', this.rowDbClickHanlder, this);

};

Ext.extend(Sbi.widgets.CatalogueVersionsGridPanel, Ext.grid.GridPanel, {
  
	services : null
	, downloadColumn : null
	, cm : null
	, store: null
	, sm : null
	, tb : null

	,
	init : function () {
		this.initStore();
		this.initSm();
		this.initCm();
		this.initTb();
		this.initGridView();
	}
	
	,
	initGridView : function () {
		this.gridView = new Ext.grid.GridView({
			forceFit : true
			, getRowClass : function(row, index) {
				var cls = '';
				var data = row.data;
				if (data.active == true) {
					cls = 'green-row'
				}
				return cls;
			}
		});
	}

  	,
  	initCm : function () {
  		
        this.downloadColumn = new Ext.grid.ButtonColumn({
			header 					:  ' '
			, iconCls 				: 'icon-download'
			, clickHandler 			: this.downloadHandler
			, scope 				: this  
			, width 				: 25
			, renderer : function(v, p, record) {
			       return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
			}
        });
        
        if (this.sm == null) {
        	this.initSm();
        }
  		
  		this.cm = new Ext.grid.ColumnModel({
  			columns : [
	       		{
	       		    name			: 'id'
	       		    , hidden		: true
	       		},{
	       	    	header			: LN('sbi.widgets.catalogueversionsgridpanel.userIn')
	       	    	//, width			: 60
	       			, id			: 'creationUser'
	       			, sortable		: true
	       			, dataIndex		: 'creationUser' 
	       	    },{
	       	    	header			: LN('sbi.widgets.catalogueversionsgridpanel.dateIn')
	       	    	//, width			: 60
	       			, id			: 'creationDate'
	       			, sortable		: true
	       			, dataIndex		: 'creationDate'
	       			, renderer		: Ext.util.Format.dateRenderer(Sbi.config.localizedTimestampFormat)
	       	    },{
	       	    	header			: LN('sbi.widgets.catalogueversionsgridpanel.fileName')
	       	    	//, width			: 60
	       			, id			: 'fileName'
	       			, sortable		: true
	       			, dataIndex		: 'fileName' 
	       	    }
	       	    , this.downloadColumn
	       	    , this.sm
  			]
  		});
  	}
  	
  	,
  	initStore : function () {
  		
  	    this.store = new Ext.data.JsonStore({
  	        root: 'results'
  	        , id : 'id'
  	        , fields: ['id', 'creationUser', 'fileName', 'dimension', 'active'
  	                   	, {name:'creationDate', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}
  	                   ]
  			, url: this.services['getVersionsService']
  	    });
  	    
  	}
  	
  	,
  	initSm : function () {
		this.sm = new Ext.grid.CheckboxSelectionModel({
	        singleSelect		: false
	        , checkOnly			: true
	    });
  	}
  	
  	,
  	initTb : function () {
		this.tb = new Ext.Toolbar({
			items : [
			    '->'
			    /*, new Ext.Toolbar.Button({
					text: LN('sbi.widgets.catalogueversionsgridpanel.deleteNonActive')
					, iconCls: 'icon-clear'
					, handler: this.onDelete
					, width: 30
					, scope: this
				})*/
			    , new Ext.Toolbar.Button({
					text: LN('sbi.generic.delete')
					, iconCls: 'icon-remove'
					, handler: this.deleteHandler
					, width: 30
					, scope: this
				})

			]
		});
  	}
    
    ,
    downloadHandler: function(e, t) {
    	var baseUrl = this.grid.services['downloadVersionService'];
        var index = this.grid.getView().findRowIndex(t);
        var selectedRecord = this.grid.store.getAt(index);
        var versionId = selectedRecord.get('id');
    	window.location.href = baseUrl + '&id=' + versionId;
    }
    
	,
	deleteHandler: function() {
		var recordsSelected = this.getSelectionModel().getSelections();
		if (recordsSelected && recordsSelected.length > 0) {
			Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm')
				, LN('sbi.generic.confirmDelete')
				, function (btn, text) {
					if ( btn == 'yes' ) {
						this.doDelete(recordsSelected);
					}
				}
				, this
			);
		} else {
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.generic.noItemsSelected'), 'Warning');
		}
	}
	
	, doDelete : function(recordsSelected) {
		var ids = new Array();
		for (var count = 0; count < recordsSelected.length; count++) {
			var aRecord = recordsSelected[count];
			ids[count] = aRecord.get('id');
		}
		var idsJoined = ids.join(',');
			
		Ext.Ajax.request({
	        url: this.services['deleteVersionsService'],
	        params: { 'id' : idsJoined },
	        success : function(response, options) {
	      		if (response && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if (content !== undefined && content.result == 'OK') {
		  	  			// removes the subobjects from the store
		  	  			for (var count = 0; count < recordsSelected.length; count++) {
		  	  				this.store.remove(recordsSelected[count]);
		  	  			}
	      			} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Error while deleting item versions', 'Service Error');
		      		}
	      		} else {
	      			Sbi.exception.ExceptionHandler.showErrorMessage('Error while deleting item versions', 'Service Error');
	      		}
	        },
	        scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}	
    
    ,
    getSelectedVersionId: function() {
    	var selectedRecord = this.getSelectionModel().getSelected();
    	return selectedRecord.get('id');
    }
    
    ,
    rowDbClickHanlder : function ( theGrid, rowIndex, e ) {
    	e.stopEvent();
    	
    	var currentActiveRecord = this.getCurrentActiveRecord();
    	currentActiveRecord.set('active', false);
    	
        var record = this.store.getAt(rowIndex);
        record.set('active', true);
        
        this.getView().refresh();
    }
    
    ,
    getCurrentActiveRecord : function () {
    	var currentActiveRecordIndex = this.store.find('active', 'true');
    	if (currentActiveRecordIndex == -1) {
    		return null;
    	}
    	var currentActiveRecord = this.store.getAt(currentActiveRecordIndex);
    	return currentActiveRecord;
    }

});