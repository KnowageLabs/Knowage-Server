/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 







/**
 * Object name
 *
 * This grid must be used in combination with Sbi.widgets.store.InMemoryFilteredStore.
 * The aim is to have a grid with paging toolbar but all records to be loaded client-side.
 * The config object used by the constructor must contain a "store" property with an instance of Sbi.widgets.store.InMemoryFilteredStore.
 *
 * Public Properties
 *
 *  [list]
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

Ext.define('Sbi.widgets.grid.InMemoryPagingGridPanel', {
    extend: 'Ext.grid.Panel'

    ,
    config: {
    	stripeRows: true,
    	pagingToolbar: null
    }

	,
	constructor: function(config) {
		Sbi.debug('InMemoryPagingGridPanel costructor IN');

		Ext.apply(this, config);

      	this.columns = [];

      	this.store.on('load', this.updateGrid, this);
      	this.addPaging(config);

    	this.callParent(arguments);
    	this.bbar = this.pagingToolbar;
    	this.on('afterrender', this.loadStore, this);

    	Sbi.debug('InMemoryPagingGridPanel costructor OUT');
    }

	,
    addPaging: function(config){
		Sbi.debug('InMemoryPagingGridPanel add paging IN');
		var defaultPagingConfig={
			width: 400,
            store: this.store,
            displayInfo: true,
            displayMsg: LN('sbi.qbe.datastorepanel.grid.displaymsg'),
            emptyMsg: "No rows to display"
        };
		defaultPagingConfig = Ext.apply(defaultPagingConfig,config.pagingConfig );
		this.pagingToolbar = Ext.create('Ext.PagingToolbar', defaultPagingConfig);
		this.pagingToolbar.down('#refresh').hide();
		this.bbar = this.pagingToolbar;
		Sbi.debug('InMemoryPagingGridPanel add paging OUT');
    }

	,
    updateGrid: function() {
    	Sbi.debug('InMemoryPagingGridPanel updategrid IN');
    	//var columns = this.store.getColumns().slice(1); // we need to remove the first "recNo" column!!!
    	this.bbar.bindStore(this.store);
    	this.bbar.doLayout();
    	this.reconfigure(this.store); // , this.columnManager.columns);
    	
    	if(this.store.inMemoryData){
    		if(this.store.inMemoryData.length < this.store.limit){
				this.bbar.setVisible(false);
			} else {
				this.bbar.setVisible(true);
			}
		}
    	
    	this.getView().refresh();
    	Sbi.debug('InMemoryPagingGridPanel updategrid OUT');
    }

    ,
    loadStore: function(t) {
    	this.store.loadPage(1);
    }

//	/**
//	 * Opens the loading mask
//	 */
//    , showMask : function(){
//    	this.un('afterlayout',this.showMask,this);
//    	if (this.loadMask == null) {
//    		this.loadMask = new Ext.LoadMask('InMemoryPagingGridPanel', {msg: "Loading.."});
//    	}
//    	if (this.loadMask){
//    		this.loadMask.show();
//    	}
//    }
//
//	/**
//	 * Closes the loading mask
//	*/
//	, hideMask: function() {
//    	if (this.loadMask && this.loadMask != null) {
//    		this.loadMask.hide();
//    	}
//	}

});


