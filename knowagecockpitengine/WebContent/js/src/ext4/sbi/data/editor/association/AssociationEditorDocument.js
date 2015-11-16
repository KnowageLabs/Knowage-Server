/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**

  * Authors
  *
  * - S.Lupo (salvatore.lupo@eng.it)
  */

Ext.define('Sbi.data.editor.association.AssociationEditorDocument', {
	  extend: 'Ext.Panel'
	, layout: 'fit'

	, config:{
		  border: true
		, dataset: null
	    , height : 225
		, width : 180

	}

	, gridConfig: null
	, style: {marginTop: '3px', marginRight: '5px', marginLeft:'5px'}
	, services: null
    , grid: null
    , displayRefreshButton: true  // if true, display the refresh button

	, constructor : function(config) {
		Sbi.trace("[AssociationEditorDocument.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		this.addEvents('addFieldToAssociation');
		Sbi.trace("[AssociationEditorDocument.constructor]: OUT");
	}

	, initComponent: function() {
	        Ext.apply(this, {
	            items: [this.grid]
	        });
	        this.callParent();
	}


    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    , refreshFieldsList: function(datasetLabel) {
    	Sbi.trace("[AssociationEditorDocument.refreshFieldsList]: IN");

    	Sbi.trace("[AssociationEditorDocument.refreshFieldsList]: input parameter datasetLabel is equal to [" + datasetLabel + "]");
/*
		if (datasetLabel) {
			this.dataset = datasetLabel;
			this.store.proxy.setUrl(Sbi.config.serviceReg.getServiceUrl("loadDataSetField", {
				pathParams: {datasetLabel: this.dataset}
			}), true);


			Sbi.trace("[AssociationEditorDocument.refreshFieldsList]: url: " + this.store.url);
		}*/
		this.store.load();

		Sbi.trace("[AssociationEditorDocument.refreshFieldsList]: OUT");
	}



    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		this.initGrid();
	}

	, initStore: function() {
		Sbi.trace("[AssociationEditorDocument.initStore]: IN");
		return Sbi.storeManager.getStoresById(this.dataset)[0];
		Sbi.trace("[AssociationEditorDocument.initStore]: OUT");
	}

    , initGrid: function() {
    	var gridStore = this.initStore();
    	gridStore.load();
    	this.grid = Ext.create('Ext.grid.Panel',  {
    		title: this.dataset,
        	margins: '5 5 5 0',
        	selModel: {selType: 'rowmodel', mode: 'SINGLE', allowDeselect: true},
	        store: gridStore,
	        columns: [
	            { header: LN('sbi.cockpit.association.editor.wizard.ds.columnColumn')
            	, width: 100
            	, sortable: true
            	, dataIndex: 'alias'
            	, flex: 1
            	}, {
        		  header: LN('sbi.cockpit.association.editor.wizard.ds.columnType')
            	, width: 75
            	, sortable: true
            	, dataIndex: 'colType'
            	}
	        ],
	        viewConfig: {
	        	stripeRows: true
	        }
	    });

    }

    // private methods



    // public methods
    , getFields : function () {
    	var fields = [];
    	var count = this.store.getCount();
    	for (var i = 0; i < count; i++) {
    		fields.push(this.store.getAt(i).data);
    	}
    	return fields;
    }

});