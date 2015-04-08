/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**

  * Authors
  *
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.define('Sbi.data.editor.association.AssociationEditorList', {
	extend: 'Ext.Panel'
	, layout: 'fit'
	, config:{
		  associations :null
		, height: 200
	}

	, services: null
	, grid: null
	, store: null
	, displayRefreshButton: null  // if true, display the refresh button
	, border: false
	, style: {marginTop: '3px', marginRight: '5px', marginLeft:'5px'}
	, autoScroll: true


	/**
	 * @property currentAss
	 * The current Association selected in the list
	 */
	, currentAss : null

	, constructor : function(config) {
		Sbi.trace("[AssociationEditorList.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		this.addEvents("addAssociation","removeAssociation","selectAssociation","updateIdentifier");
		Sbi.trace("[AssociationEditorList.constructor]: OUT");
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

    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		this.initStore();
		this.initGrid();
	}

	, initStore: function() {
	   Sbi.trace("[AssociationEditorDataset.initStore]: IN");
	   var initialData = [];

	   if (this.associations !== null ){
		   for (var i=0; i< this.associations.length; i++){
				if (Sbi.isValorized(this.associations[i].description)) {
					var el = [this.associations[i].id,  this.associations[i].description];
					initialData.push(el);
				}
			}
	   }
	   this.store = Ext.create('Ext.data.ArrayStore', {
	        fields: [
	           {name: 'id'},
	           {name: 'ass'}
	        ],
	        data: initialData
	    });

		Sbi.trace("[AssociationEditorDataset.initStore]: OUT");
	}

    , initGrid: function() {
    	var thisPanel = this;
    	var c = this.gridConfig;

    	// The add action
    	var title = new Ext.form.Label({text:LN('sbi.cockpit.association.editor.wizard.list.title'),  style: 'font-weight:bold;'});
        var actionAdd = new Ext.Action({
//            text: LN('sbi.cockpit.association.editor.wizard.list.add'),
            tooltip: LN('sbi.cockpit.association.editor.wizard.list.add.tooltip'),
            iconCls:'icon-add',
            handler: function(){
            	thisPanel.fireEvent("addAssociation",null);
            }
        });

        var actionModify = new Ext.Action({
//        	text: LN('sbi.cockpit.association.editor.wizard.list.modify'),
            tooltip: LN('sbi.cockpit.association.editor.wizard.list.modify.tooltip'),
            iconCls:'icon-edit',
            scope: this,
            handler: function(){
            	thisPanel.fireEvent("modifyAssociation", thisPanel);
            }
        });

        var actionAutoDetect = new Ext.Action({
        	text: LN('sbi.cockpit.association.editor.wizard.list.autodetect'),
            tooltip: LN('sbi.cockpit.association.editor.wizard.list.autodetect.tooltip'),
            handler: function(){
            	alert('Functionality not available!');
//            	thisPanel.fireEvent("autodetect", thisPanel);
            },
            disabled: true
        });


        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        });

        this.grid = Ext.create('Ext.grid.Panel', Ext.apply(c || {}, {
	        store: this.store,
	        tbar: new Ext.Toolbar({items:[title, '->', actionAdd, actionModify, actionAutoDetect]}),
	        selModel: {selType: 'rowmodel', mode: 'SINGLE', allowDeselect: true},
	        columns: [
	            { header: LN('sbi.cockpit.association.editor.wizard.list.columnId')
            	, width: 10
            	, sortable: true
            	, dataIndex: 'id'
            	, editor: {
                        allowBlank: false
                  }
            	, flex: 1
            	}, {
        		  header: LN('sbi.cockpit.association.editor.wizard.list.columnAssociation')
            	, width: 700
            	, sortable: true
            	, dataIndex: 'ass'
            	},{
                    xtype: 'actioncolumn',
                    width: 50,
                    items: [{
                        iconCls:'icon-delete',
                        tooltip: LN('sbi.cockpit.association.editor.wizard.list.delete.tooltip'),
                        handler: this.deleteAssociation ,
                        scope:this
                    }
                    ]
                }
	        ],
	        viewConfig: {
	        	stripeRows: true
	        },
	        plugins: [cellEditing]
	    }));
        this.grid.on('itemclick', this.onCellClick, this);
        this.grid.on('edit', function(editor, e) {
            // commit the changes right after editing finished
            e.record.commit();
            this.fireEvent('updateIdentifier', e);
        }, this);
    }


	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    /**
	 * @method
	 * Returns the fields of the store
	 *
	 */
    , getFields : function () {
    	var fields = [];
    	var count = this.store.getCount();
    	for (var i = 0; i < count; i++) {
    		fields.push(this.store.getAt(i).data);
    	}
    	return fields;
    }

    /**
	 * @method
	 * Returns the current Association object
	 *
	 */
    , getCurrentAss: function(){
    	return this.currentAss;
    }

    /**
	 * @method
	 * Add the Association to the grid's store
	 *
	 * @param {Object} Ass The Association
	 */
    , addAssociationToList: function(association){
    	association.ass = association.ass || association.description;
    	this.addAssToStore(association);
    }

    /**
	 * @method
	 * Remove the Association from the list and fire the event 'removeAssociation' for remove it
	 * from the associations too.
	 *
	 * @param {Object} r The Association
	 */
    , deleteAssociation: function(grid, rowIndex, colIndex) {
    	var rec = this.store.getAt(rowIndex);
    	var ass = rec.get('ass') ;
    	Ext.MessageBox.confirm(
    			LN('sbi.generic.pleaseConfirm')
    			, LN('sbi.cockpit.association.editor.msg.confirmDelete') +ass + ' ?'
                , function(btn, text) {
                    if ( btn == 'yes' ) {
                    	Sbi.trace("[AssociationEditorList.deleteAssociation]: Removed association  [ " +  ass + '] from Associations List');
                    	this.removeAssociationFromGrid(rec);
                        this.fireEvent('removeAssociation', ass);
                    }
    			}
    			, this
    		);
    }

    /**
	 * @method
	 * Remove fisically the Association from the grid's store
	 *
	 * @param {Object} r The record to delete
	 */
    , removeAssociationFromGrid: function(r){
    	 this.grid.store.remove(r);
    }

    /**
	 * @method
	 * Returns the record of the store.
	 *
	 * @param {Object} id The Association identifier
	 */
    , getAssociationById: function(id){
    	var recIdx = this.store.find('id', id);
    	var association = this.store.getAt(recIdx);
    	association.description = association.description || association.ass;
    	return this.store.getAt(recIdx);
    }

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
    /**
	 * @method
	 * Add the Association (getted by the cells selected) to the grid's store
	 *
	 * @param {Object} r The Association
	 */
    , addAssToStore: function(r){
    	if (r.id  == null || r.id == undefined)
    		r.id = '#'+ ((this.store.data.length !== undefined)?this.store.data.length:0);

    	var myData = [
		              [r.id, r.ass]
		             ];

		this.store.loadData(myData, true);
		this.doLayout();
    }
    /**
	 * @method (listener)
	 * Fire the selectAssociation event to select only the cells of the Association selected in the grid
	 *
	 */
    , onCellClick: function(grid, record, item, index, e, opt){
    	var association = {};
    	association.id = record.get('id');
    	association.description = record.get('ass');

        this.currentAss = association;
        this.fireEvent('selectAssociation', association);
    }

});