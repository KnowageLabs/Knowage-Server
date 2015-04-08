/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 *
 * This is a store "wrapper" that loads all the rows in the client memory and adds the capability to filter them live..
 * It extends the Ext.data.Store so it is general and its specialization derive from the configuration passed to the constructor.
 *
 *
 *     @example
 *     ...
 *		//define the store
 *   	this.storeConfig = Ext.apply({
 *   		parentGrid: this,
 *   		model: modelname,
 *   		filteredProperties: ["DATASOURCE_LABEL","DESCRIPTION"]
 *   	},this.storeConfig||{});
 *   	//create the store
 *   	this.store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);
 *   	//adds the pagination toolbar
 *		this.addPaging(config);
 *		//add the filter
 *		this.tbar = Ext.create('Sbi.widgets.toolbar.InLineFilterAndOrder',Ext.apply({store: this.store, additionalButtons:additionalButtons}));
 *     	...
 *
 *
 */
Ext.define('Sbi.widgets.store.InMemoryFilteredStore', {
    extend: 'Ext.data.Store'

    ,
    config: {
    	/**
    	 * The container of the items loaded from the store
    	 */
    	inMemoryData: null,
    	/**
    	 * The list of the properties that should be filtered
    	 */
    	filteredProperties: new Array(),
    	/**
    	 * Object with internal properties to filter
    	 */
    	filteredObjects: null,
    	/**
    	 * False to reload the wrapped store. It force the refresh of the in memory data
    	 */
    	useChache: true, //cache means that the values are cached
    	/**
    	 * The string used as filter
    	 */
    	filterString: null,
    	/**
    	 * Optional parameter for specify a filter only on a specific property
    	 */
    	filterSpecificProperty: null
    }

    /**
     * Creates the store.
     * @param {Object} config (optional) Config object. This is the normal configuration of a generic store
     */
    , constructor: function(config) {
    	this.initConfig(config);
    	this.callParent(arguments);
    	this.on("load", this.onLoadHandler, this);
    }

    ,
    onLoadHandler: function(store, records, successful, eOpts, sorterFn) {
		if (!this.inMemoryData) {
			this.inMemoryData = this.data.items.slice(0);//clone the items
		}
		var items = this.getFilteredItems(this.inMemoryData, this.filteredProperties, this.filterString, this.filteredObjects, this.filterSpecificProperty);
		this.suspendEvents(false);
		if (sorterFn) {
			items = Ext.Array.sort(items, sorterFn);
		}
		items = this.getPageItems(this.start, this.limit, items);
		this.removeAll();
		this.remoteSort = true; // this will prevent the add method to invoke the doSort method (an infinite loop will occur)
		this.add(items);
		this.resumeEvents();
		this.fireEvent('datachanged', this);
		//this.fireEvent('refresh', this);
	}

    ,
    doSort: function(sorterFn) {
    	this.onLoadHandler(this, null, null, null, sorterFn);
    }

    /**
     * @Override
     */
	, load: function(options) {
		if (!options) {
			options = {};
		}

		if (options.limit) {
			this.limit = options.limit;
		}
		if (options.start) {
			this.start = options.start;
		} else if (this.limit) {
			this.start = 0;
		}

		this.page = options.page;
		this.filterString = options.filterString;
		if (options.filterSpecificProperty) {
			this.filterSpecificProperty = options.filterSpecificProperty;
		}

		if (options.params || options.reset || !this.useChache ||  this.inMemoryData == null ||  this.inMemoryData == undefined) {
			//set null the paging configuration to load all the items from the store
			options.start = null;
			options.limit = null;
			options.page = null;
			delete this.inMemoryData;
			this.start = 0;
			this.removeAll(); // this is needed to reset the paging toolbar
		    this.currentPage = 1; // this is needed to reset the paging toolbar
		    this.callParent([options]);
		} else {
			this.fireEvent("load", this);
		}

	}

	,
	loadData : function( data ) {
		delete this.inMemoryData;
		this.start = 0;
		this.removeAll(); // this is needed to reset the paging toolbar
	    this.currentPage = 1; // this is needed to reset the paging toolbar
		Sbi.widgets.store.InMemoryFilteredStore.superclass.loadData.call(this, data);
		this.fireEvent("load", this);
	}

	/**
	 * Pages the in memory data.
     * @private
     * @param {Number} start The first element index
     * @param {Number} limit The number of items in the page
     * @param {Array} items The list of the items to get the page from
     */
	, getPageItems: function(start, limit, items){
		if(start!=null && start!=undefined && limit!=null && limit!=undefined){
			var pageItems = [];
			for(var i = start; (i< items.length && i<limit+start); i++){
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
	 * @param {Array} properties The list of properties to search
	 * @param {String} filterString string to find (apply a like)
	 * @param {Array} propertiesObject array of objects with fields objectName and filteredProperties for properties inside nested objects
	 * @param {String} filterSpecificProperty a single specific property to use as a filter (optional)
     */
	, getFilteredItems: function(items, properties, filterString, propertiesObject, filterSpecificProperty){

		//make a copy of the original values and work with them
		var copyProperties = properties.slice(); //array copy
		var copyObjectProperties = this.clone(propertiesObject);

		//Check if there is a specific property to use as single filter (has priority)
		if ((filterSpecificProperty != null) && (this.filterSpecificProperty !== undefined) && (this.filterSpecificProperty != "")){
			//clean properties object to filter only with filterSpecificProperty
			if (copyProperties.contains(filterSpecificProperty)){
				copyProperties = [filterSpecificProperty];
			} else {
				copyProperties = [];
			}
			for (var i=0; i<copyObjectProperties.length; i++){
				var objectFilteredProperties = copyObjectProperties[i].filteredProperties;
				if (objectFilteredProperties.contains(filterSpecificProperty)){
					copyObjectProperties[i].filteredProperties = [filterSpecificProperty];
				} else {
					copyObjectProperties[i].filteredProperties =  [];
				}
			}
		}
		//--------------------------

		var filteredCount = 0;
		if(filterString){
			filterString = filterString+"";
			var filteredItems = [];
			for(var i=0; i<items.length; i++){
				var item = items[i];
				for(var p in item.data){
					var bool = (copyProperties==null || copyProperties==undefined  ||
							((copyProperties.contains(p)) &&
									(((item.data[p].toLowerCase()).indexOf(filterString.toLowerCase()))>=0)));
					//for filtering properties of nested object in items
					if(( copyObjectProperties != null) && (copyObjectProperties !== undefined)){
						for (var j=0; j<copyObjectProperties.length; j++){
							var objectName = copyObjectProperties[j].objectName;
							var objectFilteredProperties = copyObjectProperties[j].filteredProperties;

							if (p == objectName){
								var object = item.data[p];
								if ((object != null) && (object !== undefined)){
									for (var y=0; y<object.length; y++){
										var aObject = object[y];
										for ( internalP in aObject ) {
											if (objectFilteredProperties.contains(internalP)){
												if(  (aObject[internalP].toLowerCase()).indexOf(filterString.toLowerCase())  == 0   ){
													bool = true;
													break;
												}
											}
										}
									}
								}
							}
						}
					}
					//-----------------------

					if(bool){
						filteredCount++;
						filteredItems.push(item);
						break;
					}
				}
			}


			this.totalCount = filteredCount;
			return filteredItems;
		}



		return items;
	}

	, getColumns: function(){
		return this.proxy.reader.jsonData.metaData.fields;
	}

	, getTotalCount: function(){
		if(this.inMemoryData){
			return this.inMemoryData.length;
		}
		return 0;
	}

	, clone: function(obj){
		{
		    // Handle the 3 simple types, and null or undefined
		    if (null == obj || "object" != typeof obj) return obj;

		    // Handle Date
		    if (obj instanceof Date) {
		        var copy = new Date();
		        copy.setTime(obj.getTime());
		        return copy;
		    }

		    // Handle Array
		    if (obj instanceof Array) {
		        var copy = [];
		        for (var i = 0, len = obj.length; i < len; i++) {
		            copy[i] = this.clone(obj[i]);
		        }
		        return copy;
		    }

		    // Handle Object
		    if (obj instanceof Object) {
		        var copy = {};
		        for (var attr in obj) {
		            if (obj.hasOwnProperty(attr)) copy[attr] = this.clone(obj[attr]);
		        }
		        return copy;
		    }

		    throw new Error("Unable to copy obj! Its type isn't supported.");
		}
	}




});