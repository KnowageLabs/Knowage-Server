/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.widgets.store");

Sbi.widgets.store.InMemoryFilteredStore = function(config) {
	
	Sbi.widgets.store.InMemoryFilteredStore.superclass.constructor.call(this, config);
	
	this.on("load", function(theStore, records, options) {
		if (!this.inMemoryData) {
			this.inMemoryData = this.data.items.slice(0);//clone the items
		}
		var items = this.getFilteredItems(this.inMemoryData, this.filter);
		// we need to set the totalLength, since removeAll doesn't clear the totalLength counter
		this.totalLength = items.length;
		items = this.getPageItems(this.start, this.limit, items);
		this.removeAll();
		for (var i = 0; i < items.length; i++) {
			this.add(items[i]);
		}
	}, this);
	
};

Ext.extend(Sbi.widgets.store.InMemoryFilteredStore, Ext.data.Store, {
	
	/**
	 * The container of the items loaded from the store
	 */
	inMemoryData: null


	/**
	 * False to reload the wrapped store. It force the refresh of the in memory data
	 */
	, useChache: false //cache means that the values are cached
	
	/**
	 * Object containing filtering value and columns to be filtered: {filterString : '....', columnsToFilter : ['label', 'name', ...]}
	 */
	, filter: null
	
    /**
     * @Override
     */
	, load: function(options) {
		this.limit = (options && options.params && options.params.limit != undefined) ? options.params.limit : this.limit;
		this.start = (options && options.params && options.params.start != undefined) ? options.params.start : this.start;
		this.filter = (options && options.params && options.params.filter) ? options.params.filter : this.filter;
		var forceRefresh = (options && options.forceRefresh) ? options.forceRefresh : false;
		
		if (this.useChache || this.inMemoryData == undefined || this.inMemoryData == null || forceRefresh) {
			//set null the paging configuration to load all the items from the store
			Ext.apply(options, {params : {}});
			this.inMemoryData = null;
			Sbi.widgets.store.InMemoryFilteredStore.superclass.load.call(this, options);
		} else {
			// this is needed for the paging toolbar
			this.fireEvent("load", this, null, options);
		}
		
		return true;
	}

	/**
	 * Pages the in memory data.
	 * @private
	 * @param {Number} start The first element index
	 * @param {Number} limit The number of items in the page
	 * @param {Array} items The list of the items to get the page from
	 */
	, getPageItems: function(start, limit, items) {
		if (start != undefined && start != null && limit != undefined && limit != null){
			var pageItems = [];
			for (var i = start; ( i < items.length && i < limit + start); i++) {
				pageItems.push(items[i]);
			}
			return pageItems;
		}
		return items;
	}

	/**
	 * Filters the data in memory.
	 * @private
	 * @param {Array} items The list of the items to filter
	 * @param {Object} filters Object containing filtering value and columns to be filtered: {filterString : '....', columnsToFilter : ['label', 'name', ...]}
	 */
	, getFilteredItems: function(items, filter) {
		if (filter && filter.columnsToFilter && filter.filterString) {
			var columnsToFilter = filter.columnsToFilter;
			var filterString = filter.filterString;
			var filteredCount = 0;
			var filteredItems = [];
			for (var i = 0; i < items.length; i++) {
				var item = items[i];
				for (var j = 0; j < columnsToFilter.length; j++) {
					var columnToFilter = columnsToFilter[j];
					if (item.data[columnToFilter]) {
						var value = item.data[columnToFilter];
						var bool = value.toLowerCase().indexOf(filterString.toLowerCase()) >= 0;
						if (bool) {
							filteredCount++;
							filteredItems.push(item);
							break;
						}
					}
				}
			}
			return filteredItems;
		}

		return items;
	}
	
});
