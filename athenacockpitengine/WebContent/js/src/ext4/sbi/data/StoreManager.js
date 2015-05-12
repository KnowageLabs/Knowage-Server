/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.data");

/**
 * @class Sbi.data.StoreManager
 * @extends Ext.util.Observable
 *
 * This class manages a group of stores shared by different component. It can be instantiated
 * at any level of the application classes hierarchy depending of the components that need to use it.
 * For example it can be instantiated within a panel whose child need to share different stores.
 */

/**
 * @cfg {Object} config The configuration object passed to the constructor
 */
Sbi.data.StoreManager = function(config) {
	Sbi.trace("[StoreManager.constructor]: IN");

	// init properties...
	var defaultSettings = {
		autoDestroy: true
	};

	var settings = Sbi.getObjectSettings('Sbi.data.StoreManager', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	//check if exist a crosstab in order to create a crosstab store
	if(c && c.storesConf && c.storesConf.stores){
		var stores = c.storesConf.stores;
		var widgets = c.template.widgetsConf.widgets;

		for(var i=0; i<widgets.length; i++){
			var aWidget = widgets[i];
			var aStore = stores[i];
			if(aWidget.wtype == "crosstab"){
				aStore.stype = "crosstab";
			}
		}
	}


	this.setConfiguration(c.storesConf);

	// constructor
	Sbi.data.StoreManager.superclass.constructor.call(this, c);

	Sbi.trace("[StoreManager.constructor]: OUT");
};

Ext.extend(Sbi.data.StoreManager, Ext.util.Observable, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	/**
     * @property {Ext.util.MixedCollection()} stores
     * The list of registered stores managed by this manager
     */
	stores: null

	/**
     * @property {Ext.util.MixedCollection()} associations
     * The list of registered associations between datasets managed by this manager
     */
	, associations: null

	, associationGroups: null

	/**
     * @property {Ext.util.MixedCollection()} fonts
     * The list of registered fonts managed by this manager
     */
	, fonts: null
	
	/**
     * @property {Ext.util.MixedCollection()} layout
     * The list of registered layouts managed by this manager
     */
	, layouts: null
	
	/**
     * @property {Ext.util.MixedCollection()} parameters
     * The list of registered parameters managed by this manager
     */
	, parameters: null

	, widgetConfig: null


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // configuration methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * Sets the configuration of this manage
	 *
	 * @param {Object} conf The configuration object
	 */
	, setConfiguration: function(conf) {
		Sbi.trace("[StoreManager.setConfiguration]: IN");

		conf = conf || {};

		var stores = conf.stores || [];
		this.setStoreConfigurations(stores);

		var associations = conf.associations || [];
		this.setAssociationConfigurations(associations);
		
		var fonts = conf.fonts || [];
		this.setFontConfigurations(fonts);
		
		var layouts = conf.layouts || [];
		this.setLayoutConfigurations(layouts);

		var parameters = conf.parameters || [];
		this.setParameterConfigurations(parameters);

		Sbi.trace("[StoreManager.setConfiguration]: OUT");
	}

	/**
	 * Removes all stores registered to this manager.
	 *
	 * @param {Boolean} autoDestroy (optional) True to automatically also destry the each store after removal.
	 * Defaults to the value of this Manager's {@link #autoDestroy} config.
	 */
	, resetConfiguration: function(autoDestroy) {
		Sbi.trace("[StoreManager.resetConfiguration]: IN");

		this.resetStoreConfigurations(autoDestroy);
		this.resetAssociationConfigurations(autoDestroy);

	    Sbi.trace("[StoreManager.resetConfiguration]: OUT");
	}

	/**
	 * @method
	 * Gets the store configuration object. This object can be passed to #setConfiguration method
	 * at any time to roll back to the current configuration. It can also be passed to the constructor
	 * of this class to create a clone of this instance of store manager.
	 *
	 * <b>WARNING: </b> what stated above is true only for store whose storeType is equal to "sbi". In other words
	 * it s true only for stores created using the method #createStore. Other stores managed by this manager are not included
	 * in the configuration object and so will be lost. This is due to the fact that the method #getStoreConfiguration is not able
	 * to extract configuration for a general Ext.data.Store object.
	 *
	 * @return {Object} The configuration object
	 */
	, getConfiguration: function() {
		Sbi.trace("[StoreManager.getConfiguration]: IN");
		var config = {};
		config.stores = this.getStoreConfigurations();
		config.associations = this.getAssociationConfigurations();
		config.parameters = this.getParameterConfigurations();
		config.fonts = this.getFontConfigurations();
		config.layouts = this.getLayoutConfigurations();
		Sbi.trace("[StoreManager.getConfiguration]: OUT");
		return config;
	}

	//STORE CONFIGS

	/**
	 * @method
	 * Sets stores' configuration
	 *
	 * @param {Object[]} conf The configuration object
	 */
	, setStoreConfigurations: function(conf){
		Sbi.trace("[StoreManager.setStoreConfigurations]: IN");
		this.resetStoreConfigurations();
		Sbi.trace("[StoreManager.setStoreConfigurations]: Input parameter [conf] is equal to [" + Sbi.toSource(conf) + "]");
		conf = conf || [];
		for(var i = 0; i < conf.length; i++) {
			var store = this.createStore(conf[i]);
			this.addStore(store);
		}

		// for easy debug purpose
		//	var testStore = this.createTestStore();
		//	Sbi.trace("[StoreManager.init]: adding test store whose type is equal to [" + testStore.storeType + "]");
		//	this.addStore(testStore);
		Sbi.trace("[StoreManager.setStoreConfigurations]: OUT");
	}

	, resetStoreConfigurations: function(autoDestroy) {
		Sbi.trace("[StoreManager.resetStoreConfigurations]: IN");
		if(Sbi.isValorized(this.stores)) {
			Sbi.trace("[StoreManager.resetConfiguration]: There are [" + this.stores.getCount() + "] store(s) to remove");
			autoDestroy = autoDestroy || this.autoDestroy;
			this.stores.each(function(registeredStore, index, length) {
				Sbi.trace("[StoreManager.resetConfiguration]: Removing  [" + registeredStore.aggregatedVersions.length + "] store(s) " +
						"associated with id [" + registeredStore.id + "]...");
				for(var i = 0; i < registeredStore.aggregatedVersions.length; i++) {
					this.removeStore(registeredStore.aggregatedVersions[i], autoDestroy);
				}
				Sbi.trace("[StoreManager.resetConfiguration]: Store(s) associated with id [" + registeredStore.id + "] have been succesfully removed");
			}, this);
		} else {
			Sbi.trace("[StoreManager.resetConfiguration]: There are no store(s) to remove");
		}

		this.stores = new Ext.util.MixedCollection();
		this.stores.getKey = function(o){
	        return o.id;
	    };
	    Sbi.trace("[StoreManager.resetStoreConfigurations]: OUT");
	}

	/**
	 * @method
	 * Gets the configuration of all stores whose type is equal to "sbi" managed by this store manager.
	 *
	 * @return {Object[]} The stores' configuration
	 */
	, getStoreConfigurations: function() {
		var confs = [];
		this.stores.each(function(registeredStore, index, length) {
			for(var i = 0; i < registeredStore.aggregatedVersions.length; i++) {
				var c = this.getStoreConfiguration(registeredStore.aggregatedVersions[i]);
				if(Sbi.isValorized(c)) {
					confs.push(c);
				}
			}
		}, this);
		return confs;
	}

	/**
	 * @method
	 * Gets the configuration of the store whose id is equal to #storeId if it is managed by this
	 * manager and its type is equal to "sbi", null otherwise.
	 *
	 * @param {String} store the store
	 *
	 * @return {Object[]} The store's configuration
	 */
	, getStoreConfiguration: function(store) {

		Sbi.trace("[StoreManager.getStoreConfiguration]: IN");

		if(Ext.isString(store)) {
			Sbi.error("[StoreManager.getStoreConfiguration]: Input parameter [store] must be of type Ext.data.Store");
			alert("[StoreManager.getStoreConfiguration]: Input parameter [store] must be of type Ext.data.Store");
			return;
		}

		var storeConf = null;

		if(Sbi.isValorized(store)) {
			if(store.storeType === "sbi") {
				Sbi.trace("[StoreManager.getStoreConfiguration]: conf of store [" + store.storeId + "] of type [" + store.storeType + "] " +
						"is equal to [" + Sbi.toSource(store.storeConf, true)+ "]");

				storeConf = Ext.apply({}, store.storeConf);
			} else {
				Sbi.warn("[StoreManager.getStoreConfiguration]: impossible to extract configuration from store of type different from [sbi]");
			}
		} else {
			Sbi.warn("[StoreManager.getStoreConfiguration]: impossible to find store [" + store.storeId + "]");
		}

		Sbi.trace("[StoreManager.getStoreConfiguration]: OUT");

		return storeConf;
	}

	// ASSOCIATION CONFIGS

	/**
	 * @method
	 * Sets associations configuration
	 *
	 * @param {Object[]} conf The configuration object
	 */
	, setAssociationConfigurations: function(conf) {
		Sbi.trace("[StoreManager.setAssociationConfigurations]: IN");
		this.resetAssociationConfigurations();
		Sbi.debug("[StoreManager.setAssociationConfigurations]: parameter [conf] is equal to [" + Sbi.toSource(conf)+ "]");
		conf = conf || [];
		for(var i = 0; i < conf.length; i++) {
			this.addAssociation(conf[i]);
		}
		this.refreshAssociationGroups();
		Sbi.trace("[StoreManager.setAssociationConfigurations]: OUT");
	}

	, resetAssociationConfigurations: function(autoDestroy) {
		if(Sbi.isValorized(this.associations)) {
			Sbi.trace("[StoreManager.resetAssociationConfigurations]: There are [" + this.associations.getCount() + "] association(s) to remove");
			autoDestroy = autoDestroy || this.autoDestroy;
			this.associations.each(function(association, index, length) {
				this.removeAssociation(association, autoDestroy);
			}, this);
		}

		this.associations = new Ext.util.MixedCollection();
		this.associations.getKey = function(o){
	        return o.id;
	    };
	}

	/**
	 * @method
	 * Gets the configuration of all associations defined in this store manager.
	 *
	 * @return {Object[]} The associations' configuration
	 */
	, getAssociationConfigurations: function() {
		Sbi.trace("[StoreManager.getAssociationConfigurations]: IN");
		var confs = [];
		this.associations.each(function(association, index, length) {
			var c = this.getAssociationConfiguration(association.id);
			if(Sbi.isValorized(c)) {
				confs.push(c);
			}
		}, this);
		Sbi.trace("[StoreManager.getAssociationConfigurations]: OUT");
		return confs;
	}

	/**
	 * @method
	 * Gets the configuration of the association whose id is equal to #associationId if it is defined in this
	 * manager, null otherwise.
	 *
	 * @param {String} associationId the association id
	 *
	 * @return {Object[]} The association's configuration
	 */
	, getAssociationConfiguration: function(associationId) {
		Sbi.trace("[StoreManager.getAssociationConfiguration]: IN");

		var association = this.getAssociation(associationId);
		var associationConf = null;

		if(Sbi.isValorized(association)) {
			associationConf = Ext.apply({}, association);
			Sbi.trace("[StoreManager.getAssociationConfiguration]: conf of store [" + associationId + "] is equal to [" + Sbi.toSource(associationConf, true)+ "]");
		} else {
			Sbi.warn("[StoreManager.getAssociationConfiguration]: impossible to find association [" + associationId + "]");
		}

		Sbi.trace("[StoreManager.getAssociationConfiguration]: OUT");

		return associationConf;
	}

	, refreshAssociationGroups: function() {
		// mask container (non direttamente ma tramite lancio di evento onRefresh group)
		// call refresh service on server
		// implement a callback that
		//	- save the response of the server
		//	- unmask container (non direttamente ma tramite lancio di evento afterRefresh group)
		Ext.Ajax.request({
		    url: Sbi.config.serviceReg.getServiceUrl('setAssociations'),
		    method: 'POST',
		    params: {
		        requestParam: 'notInRequestBody'
		    },
		    jsonData: Ext.JSON.encode(this.associations),
		    success : this.onAssociationGroupRefreshed,
			failure: Sbi.exception.ExceptionHandler.handleFailure,
			scope: this
		});
	}
	, getAssociationGroupByAssociationId: function(associationId) {
		for(var i = 0; i < this.associationGroups.length; i++) {
			var associationGroup = this.associationGroups[i];
			for(var j = 0; j < associationGroup.associations.length; j++) {
				if(associationGroup.associations[j].id == associationId) {
					return associationGroup;
				}
			}

		}
		return null;
	}
	, getAssociationGroupByStore: function(store) {
		for(var i = 0; i < this.associationGroups.length; i++) {
			var associationGroup = this.associationGroups[i];
			if( Ext.Array.contains(associationGroup.datasets, store.storeId) ) {
				return associationGroup;
			}
		}
		return null;
	}

	/**
	 * @method
	 *
	 * Returns the stores belonging to this association group. The association group refers the stores by id. This method
	 * return all the stores (at all aggregation level) for each id referentiated in association group.
	 *
	 * @param {Object} associationGroup The association group
	 */
	, getStoresInAssociationGroup: function(associationGroup) {
		Sbi.trace("[StoreManager.getStoresInAssociationGroup]: IN");
		var stores = [];
		Sbi.trace("[StoreManager.getStoresInAssociationGroup]: There are [" + associationGroup.datasets.length + "] dataset(s) in the input association group");
		for(var i = 0; i < associationGroup.datasets.length; i++) {
			var storeId = associationGroup.datasets[i];
			Ext.Array.push(stores, this.getStoresById(storeId));
		}
		Sbi.trace("[StoreManager.getStoresInAssociationGroup]: There are [" + stores.length + "] store(s) related to the input association group ");
		Sbi.trace("[StoreManager.getStoresInAssociationGroup]: OUT");
		return stores;
	}
	
	// FONTS CONFIGS
	
	/**
	 * @method
	 * Sets fonts configuration
	 *
	 * @param {Object[]} conf The configuration object
	 */
	, setFontConfigurations: function(conf) {
		Sbi.trace("[StoreManager.setFontConfigurations]: IN");
		this.resetFontConfigurations();
		Sbi.debug("[StoreManager.setFontConfigurations]: parameter [conf] is equal to [" + Sbi.toSource(conf)+ "]");
		conf = conf || [];
		for(var i = 0; i < conf.length; i++) {
			this.addFont(conf[i]);
		}
		Sbi.trace("[StoreManager.setFontConfigurations]: OUT");
	}

	, resetFontConfigurations: function(autoDestroy) {
		if(Sbi.isValorized(this.fonts)) {
			Sbi.trace("[StoreManager.resetFontConfigurations]: There are [" + this.fonts.getCount() + "] parameter(s) to remove");
			autoDestroy = autoDestroy || this.autoDestroy;
			this.fonts.each(function(font, index, length) {
				this.removeFont(font, autoDestroy);
			}, this);
		}

		this.fonts = new Ext.util.MixedCollection();
		this.fonts.getKey = function(o){
	        return o.id;
	    };
	}

	/**
	 * @method
	 * Gets the configuration of all fonts defined in this store manager.
	 *
	 * @return {Object[]} The fonts' configuration
	 */
	, getFontConfigurations: function() {
		Sbi.trace("[StoreManager.getFontConfigurations]: IN");
		var confs = [];
		this.fonts.each(function(font, index, length) {
			//var c = this.getFontConfiguration(font);
			if(Sbi.isValorized(font)) {
				confs.push(font);
			}
		}, this);
		Sbi.trace("[StoreManager.getFontConfigurations]: OUT");
		return confs;
	}
	
	// LAYOUT CONFIGS
	
	/**
	 * @method
	 * Sets layout configuration
	 *
	 * @param {Object[]} conf The configuration object
	 */
	, setLayoutConfigurations: function(conf) {
		Sbi.trace("[StoreManager.setLayoutConfigurations]: IN");
		this.resetLayoutConfigurations();
		Sbi.debug("[StoreManager.setLayoutConfigurations]: parameter [conf] is equal to [" + Sbi.toSource(conf)+ "]");
		conf = conf || [];
		for(var i = 0; i < conf.length; i++) {
			this.addLayout(conf[i]);
		}
		Sbi.trace("[StoreManager.setLayoutConfigurations]: OUT");
	}

	, resetLayoutConfigurations: function(autoDestroy) {
		if(Sbi.isValorized(this.layouts)) {
			Sbi.trace("[StoreManager.resetLayoutConfigurations]: There are [" + this.layouts.getCount() + "] parameter(s) to remove");
			autoDestroy = autoDestroy || this.autoDestroy;
			this.layouts.each(function(layout, index, length) {
				this.removeLayout(layout, autoDestroy);
			}, this);
		}

		this.layouts = new Ext.util.MixedCollection();
		this.layouts.getKey = function(o){
	        return o.id;
	    };
	}

	/**
	 * @method
	 * Gets the configuration of layout defined in this store manager.
	 *
	 * @return {Object[]} The layout configuration
	 */
	, getLayoutConfigurations: function() {
		Sbi.trace("[StoreManager.getLayoutConfigurations]: IN");
		var confs = [];
		this.layouts.each(function(layout, index, length) {
			//var c = this.getFontConfiguration(font);
			if(Sbi.isValorized(layout)) {
				confs.push(layout);
			}
		}, this);
		Sbi.trace("[StoreManager.getLayoutConfigurations]: OUT");
		return confs;
	}
	

	// FILTERS CONFIGS

	/**
	 * @method
	 * Sets parameters configuration
	 *
	 * @param {Object[]} conf The configuration object
	 */
	, setParameterConfigurations: function(conf) {
		Sbi.trace("[StoreManager.setParameterConfigurations]: IN");
		this.resetParameterConfigurations();
		Sbi.debug("[StoreManager.setParameterConfigurations]: parameter [conf] is equal to [" + Sbi.toSource(conf)+ "]");
		conf = conf || [];
		for(var i = 0; i < conf.length; i++) {
			this.addParameter(conf[i]);
		}
		Sbi.trace("[StoreManager.setParameterConfigurations]: OUT");
	}

	, resetParameterConfigurations: function(autoDestroy) {
		if(Sbi.isValorized(this.parameters)) {
			Sbi.trace("[StoreManager.resetParameterConfigurations]: There are [" + this.parameters.getCount() + "] parameter(s) to remove");
			autoDestroy = autoDestroy || this.autoDestroy;
			this.parameters.each(function(parameter, index, length) {
				this.removeParameter(parameter, autoDestroy);
			}, this);
		}

		this.parameters = new Ext.util.MixedCollection();
		this.parameters.getKey = function(o){
	        return o.id;
	    };
	}

	/**
	 * @method
	 * Gets the configuration of all parameters defined in this store manager.
	 *
	 * @return {Object[]} The parameters' configuration
	 */
	, getParameterConfigurations: function() {
		Sbi.trace("[StoreManager.getParameterConfigurations]: IN");
		var confs = [];
		this.parameters.each(function(parameter, index, length) {
			var c = this.getParameterConfiguration(parameter.id);
			if(Sbi.isValorized(c)) {
				confs.push(c);
			}
		}, this);
		Sbi.trace("[StoreManager.getParameterConfigurations]: OUT");
		return confs;
	}

	/**
	 * @method
	 * Gets the configuration of the parameter whose id is equal to the one passed in as arguments
	 * manager, null otherwise.
	 *
	 * @param {String} parameterId the parameter id
	 *
	 * @return {Object[]} The parameter's configuration
	 */
	, getParameterConfiguration: function(parameterId) {
		Sbi.trace("[StoreManager.getParameterConfiguration]: IN");

		var parameter = this.getParameter(parameterId);
		var parameterConf = null;

		if(Sbi.isValorized(parameter)) {
			parameterConf = Ext.apply({}, parameter);
			Sbi.trace("[StoreManager.getParameterConfiguration]: conf of parameter [" + parameterId + "] is equal to [" + Sbi.toSource(parameterConf, true)+ "]");
		} else {
			Sbi.warn("[StoreManager.getParameterConfiguration]: impossible to find parameter [" + parameterId + "]");
		}

		Sbi.trace("[StoreManager.getParameterConfiguration]: OUT");

		return parameterConf;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // store methods
	// -----------------------------------------------------------------------------------------------------------------


	/**
	 * @method
	 *
	 * Adds a new store to the ones already managed by this manager.
	 *
	 * @param {Ext.data.Store} store The store to add.
	 * @param {String} store.storeId The store identifier. For store related to a SpagoBI's dataset it is equal to the dataset' label
	 * @param {boolean} store.raedy true if the store has been already loaded, false otherwise. The default is false.
	 * @param {String} store.storeType The type of the store. It can be equal to "ext" if the store is a standrad extjs store "sbi" if
	 * the store is an extension provided by SpagoBI. The default is "ext".
	 * @param {Numeric} store.refreshTime The refresh time of the store in seconds. The default is 0.
	 * @param {Object} store.aggregations The aggregations defined on store ({categories: [...], measures: [...]}).
	 */
	, addStore: function(store) {
		Sbi.trace("[StoreManager.addStore]: IN");

		if(Sbi.isNotValorized(store)) {
			Sbi.warn("[StoreManager.addStore]: Input parameter [s] is not defined");
			Sbi.trace("[StoreManager.addStore]: OUT");
		}

		if(Ext.isArray(store)) {
			Sbi.trace("[StoreManager.addStore]: Input parameter [s] is of type [Array]");
			for(var i = 0; i < store.length; i++) {
				this.addStore(store[i]);
			}
		} else if(Ext.isString(store)) {
			Sbi.trace("[StoreManager.addStore]: Input parameter [s] is of type [String]");
			this.addStore({storeId: store});
		} else if(Sbi.isNotExtObject(store)) {
			Sbi.trace("[StoreManager.addStore]: Input parameter [s] is of type [Object]");
			store = this.createStore(store);
		} else if((store instanceof Ext.data.Store) === true) {
			Sbi.trace("[StoreManager.addStore]: Input parameter [s] is of type [Store]");
			// nothing to do here
		} else {
			Sbi.error("[StoreManager.addStore]: Input parameter [s] of type [" + (typeof store) + "] is not valid");
		}


		if (store.storeId !== undefined){ //TODO this is valid only for store of type sbi. Generalize!
			Sbi.trace("[StoreManager.addStore]: Adding store [" + store.storeId + "] of type [" + store.storeType + "] to manager");
			store.ready = store.ready || false;
			store.storeType = store.storeType || 'ext';
			//s.filterPlugin = new Sbi.data.StorePlugin({store: s});


			if(this.containsStore(this.getStoreId(store), this.getAggregationOnStore(store)) === false) {
				Sbi.trace("[StoreManager.addStore]: There isn't yet a store with id  [" + store.storeId + "] and aggregated at specified level into manager");

				var registeredStore = this.stores.get(this.getStoreId(store));
				if(!registeredStore) {
					Sbi.trace("[StoreManager.addStore]: No store is alredy registered with id  [" + store.storeId + "]");
					registeredStore = {
						id: this.getStoreId(store),
						aggregatedVersions: []
					};
					this.stores.add(registeredStore);
				} else {
					Sbi.trace("[StoreManager.addStore]: There are alredy [" + registeredStore.aggregatedVersions.length + "] stores registered with id  [" + registeredStore.id + "]");
				}
				registeredStore.aggregatedVersions.push(store);

				Sbi.trace("[StoreManager.addStore]: Store added. Now there are  [" + registeredStore.aggregatedVersions.length + "] store registered for id [" + registeredStore.id + "]");

				registeredStore = this.stores.get(this.getStoreId(store));
				Sbi.trace("[StoreManager.addStore.debug]: Store added. Now there are  [" + registeredStore.aggregatedVersions.length + "] store registered for id [" + registeredStore.id + "]");


				if(store.refreshTime) {
					var task = {
						run: function(){
							//if the console is hidden doesn't refresh the datastore
							if(store.stopped) {
								return;
							}

							// if store is paging...
							if(store.lastParams) {
								// ...force remote reload
								delete store.lastParams;
							}
							store.load({
								params: store.pagingParams || {},
								callback: function(){this.ready = true;},
								scope: store,
								add: false
							});
						},
						interval: store.refreshTime * 1000 //1 second
					};
					Ext.TaskMgr.start(task);
				}
			} else {
				Sbi.trace("[StoreManager.addStore]: There is yet a store with id  [" + store.storeId + "] and aggregated at specified level into manager");
			}
		}

		Sbi.trace("[StoreManager.addStore]: OUT");
	}

	/**
	 * @method
	 *
	 * @param {Ext.data.Store/String} store The store to rmove or its id.
	 * @param {Boolean} autoDestroy (optional) True to automatically also destroy the store after removal.
	 * Defaults to the value of this Manager's {@link #autoDestroy} config.
	 *
	 * @return {Ext.data.Store} the store removed. False if it was impossible to remove the store. null if the store after removal
	 * has been destroyed (see autoDestroy parameter).
	 */
	, removeStore: function(store, autoDestroy) {

		Sbi.trace("[StoreManager.removeStore]: IN");

		if(Sbi.isNotValorized(store)) {
			Sbi.trace("[StoreManager.removeStore]: Parameter [store] is not valorized");
			Sbi.trace("[StoreManager.removeStore]: OUT");
			return false;
		}

		if(Ext.isString(store)) {
			Sbi.error("[StoreManager.removeStore]: Input parameter [store] must be of type Ext.data.Store");
			return false;
		}

		var storeId = this.getStoreId(store);
		var storeAggregation = this.getAggregationOnStore(store);

		Sbi.trace("[StoreManager.removeStore]: removing store [" + storeId + "] at aggregation level [" + storeAggregation + "]");
		var registeredStore = this.stores.get(storeId);
		var newAggregatedVersions = [];

		if(Sbi.isValorized(registeredStore)) {
			Sbi.trace("[StoreManager.removeStore]: There are [" + registeredStore.aggregatedVersions.length + "] store(s) with id equal to [" + storeId + "] registered");
			for(var i = 0; i < registeredStore.aggregatedVersions.length; i++) {
				var agg = this.getAggregationOnStore(registeredStore.aggregatedVersions[i]);
				if(this.isSameAggregationLevel(storeAggregation, agg) == false ){
					newAggregatedVersions.push(registeredStore.aggregatedVersions[i]);
				}
			}
			registeredStore.aggregatedVersions = newAggregatedVersions;
			Sbi.trace("[StoreManager.removeStore]: There should be [" + registeredStore.aggregatedVersions.length + "] store(s) with id equal to [" + storeId + "] registered after deletion");

			registeredStore = this.stores.get(storeId);
			Sbi.trace("[StoreManager.removeStore]: There are [" + registeredStore.aggregatedVersions.length + "] store(s) with id equal to [" + storeId + "] registered after deletion");

			if(registeredStore.aggregatedVersions.length === 0) {
				Sbi.trace("[StoreManager.removeStore]: There are no more entry for store id [" + storeId+ "]");
				this.stores.remove(registeredStore);
				var registeredStore = this.stores.get(storeId);
				if(Sbi.isValorized(registeredStore)) {
					alert("Huston abbiamo un problema!!!");
				}
			}

			Sbi.trace("[StoreManager.removeStore]: registered store ids after deletion are [" + this.getStoreIds() + "]");
		} else {
			Sbi.warn("[StoreManager.removeStore]: There are no store with id equal to [" + storeId + "] registered");
		}


		autoDestroy = autoDestroy || this.autoDestroy;
		if(autoDestroy) {
			store.destroy();
			store = null;
		}

		Sbi.trace("[StoreManager.removeStore]: OUT");

		return store;
	}

	, containsStore: function(storeId, aggregations) {
		var containsStore;
		Sbi.trace("[StoreManager.containsStore]: IN");

		if(Ext.isString(storeId) == false) {
			Sbi.error("[StoreManager.getStore]: parameter storeId must be a string");
			return;
		}
		Sbi.trace("[StoreManager.containsStore]: store id is equal to [" + storeId +"]");
		//Sbi.trace("[StoreManager.containsStore]: store aggregations is equal to [" + Sbi.toSource(aggregations,true) +"]");

		var store = this.getStore(storeId, aggregations);
		Sbi.trace("[StoreManager.containsStore]: store is equal to [" + store +"]");
		containsStore = (store != null);
		Sbi.trace("[StoreManager.containsStore]: manager contains store [" + storeId +"] with specified aggregation level [" + containsStore + "]");
		Sbi.trace("[StoreManager.containsStore]: OUT");
		return containsStore;
	}

	/**
	 * @method
	 *
	 * @param {Ext.data.Store/String} store
	 * @return the store id
	 */
	, getStoreId: function(store) {
		var storeId;

		if(Sbi.isNotValorized(store)) {
			storeId = null;
			Sbi.warn("[StoreManager.getStoreId]: Input parameter [store] is not defined");
		} else if(Ext.isString(store)) {
			storeId = store;
		} else {
			storeId = store.storeId;
		}

		return storeId;
	}

	, getStoreIds: function() {
		var ids = [];
		this.stores.each(function(registeredStore, index, length) {
			ids.push(registeredStore.id);
		}, this);
		return ids;
	}

	/**
	 * @methods
	 *
	 * @returns {Integer} the number of stores managed by this store manager
	 */
	, getStoresCount: function() {
		return getStores().length;
	}

	, getStoresCountById: function(storeId) {
		return getStoresById(storeId).length;
	}

	/**
	 * @methods
	 *  @return {Ext.data.Store[]} all the stores managed by this store manager
	 */
	, getStores: function() {
		var stores = [];
		var registeredStores =  this.stores.getRange();
		for(var i = 0; i < registeredStores.length; i++) {
			Ext.Array.push(stores, registeredStores[i].aggregatedVersions);
		}
		return stores;
	}

	/**
	 * @methods
	 *  @return {Ext.data.Store[]} all the stores managed by this store manager with the specified id
	 */
	, getStoresById: function(storeId) {
		Sbi.trace("[StoreManager.getStoresById]: IN");

		var stores = [];
		if(Ext.isString(storeId) == false) {
			Sbi.error("[StoreManager.getStoresById]: parameter storeId must be a string");
			return;
		}
		Sbi.trace("[StoreManager.getStoresById]: store id is equal to [" + storeId +"]");

		var registeredStore = this.stores.get(storeId);
		if(Sbi.isValorized(registeredStore)) {
			Ext.Array.push(stores, registeredStore.aggregatedVersions);
		} else {
			Sbi.warn("[StoreManager.getStoresById]: There is no store associated to id [" + storeId + "]");
		}
		Sbi.trace("[StoreManager.getStoresById]: Found [" + stores.length + "] stores associated to id [" + storeId + "]");

		Sbi.trace("[StoreManager.getStoresById]: OUT");

		return stores;
	}

	/**
	 * @method
	 *
	 * @returns {Ext.data.Store} the store with the specified id and aggregation level
	 */
	, getStore: function(storeId, aggregations) {
		var store = null;

		Sbi.trace("[StoreManager.getStore]: IN");

		if(Ext.isString(storeId) == false) {
			Sbi.error("[StoreManager.getStore]: parameter storeId must be a string!!!");
			return;
		}
		Sbi.trace("[StoreManager.getStore]: store id is equal to [" + storeId +"]");
		//Sbi.trace("[StoreManager.getStore]: store aggregations is equal to [" + Sbi.toSource(aggregations, true) + "]");

		var registeredStore = this.stores.get(storeId);
		aggregations = aggregations ||  null;
		if(registeredStore) {
			Sbi.trace("[StoreManager.getStore]: There are [" + registeredStore.aggregatedVersions.length + "] stores registered with id [" + storeId + "]");
			for(var i = 0; i < registeredStore.aggregatedVersions.length; i++) {

				var aggregationOnRegisteredStore = this.getAggregationOnStore(registeredStore.aggregatedVersions[i]);

				if( this.isSameAggregationLevel(aggregations, aggregationOnRegisteredStore) ) {
					//Sbi.trace("[StoreManager.getStore]: there is already a store for id [" + storeId +"]");
					store = registeredStore.aggregatedVersions[i];
				}
			}
		} else {
			//Sbi.trace("[StoreManager.getStore]: There is no store registered with id [" + storeId + "]");
		}

		if(store === null) {
			//Sbi.trace("[StoreManager.getStore]: no store found with id [" + storeId + "] and specified aggregation level");
		}

		Sbi.trace("[StoreManager.getStore]: OUT");

		return store;
	}

	/**
	 * @method
	 *
	 * @param {Ext.data.Store} store
	 * @return the store's fields
	 */
	, getStoreFields: function(store) {
		var fields = [];
		for(var fieldHeader in store.fieldsMeta) {
			fields.push(store.fieldsMeta[fieldHeader]);
		}
		return fields;
	}

	/**
	 * @method
	* @deprecated used in method extractSelectionsFromRecord of TableWidget
	 */
	, getRecordMeta: function(r){
		var toReturn = {};
		var fields = r.fields.items;
		for(var i = 0; i < fields.length; i++) {
			var field = fields[i];
			if( Ext.isString(field) ) {
				field = {name: field};
			}
			field.header = field.header || field.name;
			toReturn[field.header] = field;
		}

		return toReturn;
	}

	/**
	 * @method
	 * @deprecated used in method extractSelectionsFromRecord of TableWidget
	 */
	, getFieldHeaderByName: function(meta, name){
	   	for (fieldHeader in meta) {
	   		var field = meta[fieldHeader];
			if(field.name === name) {
				return field.header;
			}
	   	}
	   	return null;
	}

	/**
	 * @method
	 *
	 * @param {Ext.data.Store} store
	 * @param {Object} aggregation
	 */
	, setAggregationOnStore: function(store, aggregations) {
		if(store.getProxy()) {
			aggregations.dataset = aggregations.dataset || store.storeId;
			store.getProxy().extraParams = store.getProxy().extraParams || {};
			store.getProxy().extraParams.aggregations = Ext.JSON.encode(aggregations);
		}
	}

	/**
	 * @method
	 *
	 * @param {Ext.data.Store} store
	 * @param {Object} aggregation
	 */
	, getAggregationOnStore: function(store) {
		var aggregations = null;
		if(Sbi.isNotValorized(store)) {
			aggregations = null;
			Sbi.warn("[StoreManager.getAggregationOnStore]: Input prameter [store] is undfined");
		} else if(store.getProxy() && store.getProxy().extraParams && store.getProxy().extraParams.aggregations) {
			aggregations = Ext.JSON.decode(store.getProxy().extraParams.aggregations);
		}
		return aggregations;
	}

	/**
	 * @method
	 *
	 * @param {Ext.data.Store} store
	 */
	, isStoreAggregated: function(store) {
		var aggregations = this.getAggregationOnStore(store);
		return (aggregations !== null);
	}

	, isSameAggregationLevel: function(agg1, agg2) {

		if(Sbi.isNotValorized(agg1) && Sbi.isNotValorized(agg2)) {
			//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are the same (both empty)");
			return true;
		} else if( (Sbi.isValorized(agg1) && Sbi.isNotValorized(agg2)) || (Sbi.isNotValorized(agg1) && Sbi.isValorized(agg2))) {
			//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (one of the two is not valorized)");
			return false;
		} else {
			if( (Sbi.isValorized(agg1.measures) && Sbi.isNotValorized(agg2.measures))
					|| (Sbi.isNotValorized(agg1.measures) && Sbi.isValorized(agg2.measures))) {
				//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (one of the two have no measures)");
				return false;
			}
			if(agg1.measures.length != agg2.measures.length) {
				//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (the number of mesures is different)");
				return false;
			}
			for(var i = 0; i < agg1.measures.length; i++) {
				if(agg1.measures[i].id != agg2.measures[i].id
				|| agg1.measures[i].alias != agg2.measures[i].alias
				|| agg1.measures[i].funct != agg2.measures[i].funct
				|| agg1.measures[i].nature != agg2.measures[i].nature) {
					//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (measures are not equals)");
					return false;
				}
			}

			if( (Sbi.isValorized(agg1.categories) && Sbi.isNotValorized(agg2.categories))
					|| (Sbi.isNotValorized(agg1.categories) && Sbi.isValorized(agg2.categories))) {
				//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (one of the two have no categories)");
				return false;
			}
			if(agg1.categories.length != agg2.categories.length) {
				//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (the number of categories is different)");
				return false;
			}
			for(var i = 0; i < agg1.categories.length; i++) {

//				Sbi.trace("[StoreManager.isSameAggregationLevel]: comapring category[" + i + "].1: " + Sbi.toSource(agg1.categories[i]));
//				Sbi.trace("[StoreManager.isSameAggregationLevel]: comapring category[" + i + "].2: " + Sbi.toSource(agg2.categories[i]));
				if(agg1.categories[i].id != agg2.categories[i].id) {
//					Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (category[" + i + "] are not equals. " +
//							"[id] is different [" + agg1.categories[i].id + ", " + agg2.categories[i].id + "])");
					return false;
				}
				if(agg1.categories[i].alias != agg2.categories[i].alias) {
//					Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (category[" + i + "] are not equals. " +
//							"[alias] is different [" + agg1.categories[i].alias + ", " + agg2.categories[i].alias + "])");
					return false;
				}
				if(agg1.categories[i].nature != agg2.categories[i].nature) {
//					Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are not the same (category[" + i + "] are not equals. " +
//							"[nature] is different [" + agg1.categories[i].nature + ", " + agg2.categories[i].nature + "])");
					return false;
				}
			}
		}

		//Sbi.trace("[StoreManager.isSameAggregationLevel]: aggregations are the same");

		return true;
	}



	, loadAllStores: function(){
		var stores = this.getStores();
		for(var i = 0; i < stores.length; i++) {
			this.loadStore(stores[i]);
		}
	}
	/**
	 * @method
	 *
	 * @param {Ext.data.Store} store The store to load or its id.
	 * @param {Object} params parameters to pass to load method of the store
	 *
	 * has been destroyed (see autoDestroy parameter).
	 */
	, loadStore: function(store, selections, params){

		Sbi.trace("[StoreManager.loadStore]: IN");

		if(Sbi.isNotValorized(store)) {
			Sbi.error("[StoreManager.loadStore]: Parameter [store] is not valorized");
			Sbi.trace("[StoreManager.loadStore]: OUT");
			return false;
		}

		if(Ext.isString(store)) {
			Sbi.error("[StoreManager.loadStore]: Parameter [store] is a string and not a valid Ext.Store");
			Sbi.trace("[StoreManager.loadStore]: OUT");
			return false;
		}

		var storeId = this.getStoreId(store);
		params = params || {};

		// add pareameters to params
		var p = this.getStoreParametersValues(storeId);
		//alert("[StoreManager.loadStore]: store [" + storeId + "] parameters are equal to [" + Sbi.toSource(p) + "]");
		params.parameters = Ext.JSON.encode( p );

		// add selections to params
		if(Sbi.isValorized(selections)) {
			//alert("load [" + store.storeId + "] with selections");
			params.selections = Ext.JSON.encode( selections );
		} else {
			//alert("load [" + store.storeId + "] without selections");
		}

		try {
			Sbi.trace("[StoreManager.loadStore]: Start loading store [" + storeId + "] ...");
			store.load({params: params});
		} catch(e){
			Sbi.exception.ExceptionHandler.showErrorMessage(e, LN('sbi.qbe.datastorepanel.grid.emptywarningmsg'));
		}

		Sbi.trace("[StoreManager.loadStore]: OUT");
	}

	, loadStoresByAggregations: function(storeId, selections) {
		Sbi.trace("[StoreManager.loadStoresByAggregations]: IN");
		if(Ext.isString(storeId) === false) {
			Sbi.error("[StoreManager.loadStoresByAggregations]: input parameter [storeId] must be of type string");
			alert("[StoreManager.loadStoresByAggregations]: input parameter [storeId] must be of type string");
		}
		var registeredStore = this.stores.get(storeId);
		if(Sbi.isValorized(registeredStore)) {
			if(registeredStore.aggregatedVersions && registeredStore.aggregatedVersions.length > 1) {
				for(var i = 0; i < registeredStore.aggregatedVersions.length; i++) {
					this.loadStore(registeredStore.aggregatedVersions[i], selections);
				}
			}
		}
		Sbi.trace("[StoreManager.loadStoresByAggregations]: OUT");
	}

	/**
	 * reload all the store connected by an association group. Apply the selctions to filter
	 * the results in an associative way. Selection object have this form:
	 *
	 * 	{
	 * 		cityAssociation: ['Milan', 'Turin']
	 * 		, customerAssociation: ['Andrea', 'Sofia', 'Lucio']
	 * 	}
	 */
	, loadStoresByAssociations: function(associationGroup, selections) {

		Sbi.trace("[StoreManager.loadStoresByAssociations]: IN");

		var storesAggregations = [];
		var stores = this.getStoresInAssociationGroup(associationGroup);

		Sbi.trace("[StoreManager.loadStoresByAssociations]: store in assocition group are [" + stores.length + "]");

		var storesNotAggregated = [];
		var storesAggregated = [];
		for(var i = 0; i < stores.length; i++) {
			var store = stores[i];
			store.fireEvent('beforeassociation', store, associationGroup, selections);

			if(this.isStoreAggregated(store)) {
				storesAggregations.push(this.getAggregationOnStore(store));
				storesAggregated.push(this.getStoreId(store));
			} else {
				storesNotAggregated.push(this.getStoreId(store));
			}
		}

		Sbi.trace("[StoreManager.loadStoresByAssociations]: not agrregated stores are [" + storesNotAggregated.length + "][" + storesNotAggregated+ "]");
		Sbi.trace("[StoreManager.loadStoresByAssociations]: agrregated stores are [" + storesAggregated.length + "][" + storesAggregated + "]");

		// add pareameters to params

		var parameters = {};
		for(var i = 0; i < stores.length; i++) {
			var store = stores[i];
			var storeId = this.getStoreId(store);
			parameters[storeId] = this.getStoreParametersValues(storeId);
		}

		Sbi.trace("[StoreManager.loadStoresByAssociations]: Loading joined dataset used by [" + Sbi.toSource(storesNotAggregated) + "] " +
				"not aggregated store(s)");
		Ext.Ajax.request({
		    url: Sbi.config.serviceReg.getServiceUrl('loadJoinedDataSetStore'),
		    method: 'GET',
		    params: {
		    	associationGroup:  Ext.JSON.encode(associationGroup)
		    	, parameters: Ext.JSON.encode( parameters )
		        , selections: Ext.JSON.encode(selections)
		        , datasets: Ext.JSON.encode(storesNotAggregated)
		    },
		    success : this.onAssociationGroupReloaded,
			failure: Sbi.exception.ExceptionHandler.handleFailure,
			scope: this
		});

		for(var i = 0; i < storesAggregated.length; i++) {
			var storeId = storesAggregated[i];
			Sbi.trace("[StoreManager.loadStoresByAssociations]: Loading joined dataset used by aggregated store [" + storeId + "]");
			var storeAggregations = storesAggregations[i];
			storeAggregations.dataset = storeId;
			Ext.Ajax.request({
			    url: Sbi.config.serviceReg.getServiceUrl('loadJoinedDataSetStore'),
			    method: 'GET',
			    params: {
			    	associationGroup:  Ext.JSON.encode(associationGroup)
			    	, parameters: Ext.JSON.encode( parameters )
			        , selections: Ext.JSON.encode(selections)
			        , datasets: Ext.JSON.encode([storeId])
			        , aggregations: Ext.JSON.encode(storeAggregations)
			    },
			    success : this.onAssociationGroupReloaded,
				failure: Sbi.exception.ExceptionHandler.handleFailure,
				scope: this
			});
		}

		Sbi.trace("[StoreManager.loadStoresByAssociations]: OUT");
	}

	/*
	 * storeId is optional: in case it is not specified, all stores are stopped
	 */
	, stopRefresh: function(value, storeId){
		if (storeId) { // if a storeId is defined, stopRefresh only on it
			var s = this.stores.get(storeId);
			s.stopped = value;
		} else { // if a storeId is NOT defined, stopRefresh on ALL stores
			for(var i = 0, l = this.stores.length; i < l; i++) {
				var s = this.stores.get(i);
				if (s.dsLabel !== undefined){
					s.stopped = value;
				}
			}
		}
	}

	/**
	 * @method
	 *
	 * refresh all stores of the store manager managed
	 */
	, forceRefresh: function(){
		for(var i = 0, l = this.stores.length; i < l; i++) {
			var s = this.getStore(i);
			//s.stopped = false;
			if (s !== undefined && s.dsLabel !== undefined && s.dsLabel !== 'testStore' && !s.stopped){
				s.load({
					params: s.pagingParams || {},
					callback: function(){this.ready = true;},
					scope: s,
					add: false
				});
			}
		}
	}



	// -----------------------------------------------------------------------------------------------------------------
    // association methods
	// -----------------------------------------------------------------------------------------------------------------

	, addAssociation: function(association){
		Sbi.trace("[StoreManager.addAssociation]: IN");

		if(Sbi.isNotValorized(association)) {
			Sbi.warn("[StoreManager.addAssociation]: Input parameter [association] is not defined");
			Sbi.trace("[StoreManager.addAssociation]: OUT");
		}

		if(Ext.isArray(association)) {
			Sbi.trace("[StoreManager.addAssociation]: Input parameter [association] is of type [Array]");
			for(var i = 0; i < store.length; i++) {
				this.addAssociation(association[i]);
			}
		} else if(Sbi.isNotExtObject(association)) {
			Sbi.trace("[StoreManager.addAssociation]: Input parameter [association] is of type [Object]");
			this.associations.add(association);
			Sbi.debug("[StoreManager.addAssociation]: Association [" + Sbi.toSource(association) + "] succesfully added");
		} else {
			Sbi.error("[StoreManager.addStore]: Input parameter [association] of type [" + (typeof store) + "] is not valid");
		}

		Sbi.trace("[StoreManager.addAssociation]: OUT");
	}

	/**
	 * @methods
	 *
	 * Returns all the associations defined in this store manager
	 *
	 *  @return {Object[]} The associations list
	 */
	, getAssociations: function() {
		return this.associations.getRange();
	}

	, getAssociation: function(associationId) {
		return this.associations.get(associationId);
	}

	, containsAssociation: function(association) {
		if(Ext.isString(association)) {
			return this.associations.containsKey(association);
		} else {
			return this.associations.contains(association);
		}
	}


	, getStoresByAssociation: function(a){
		for(var i=0; i<this.getAssociations().length; i++){
			var obj = this.getAssociations()[i];
			if (obj.description == a) {
				return obj.stores;
			}
		}

		return null;
	}


	, getAssociationsByStore: function(s){
		var assList = [];

		for(var i=0; i<this.getAssociations().length; i++){
			var obj = this.getAssociations()[i];
			if (obj.stores !== null && obj.stores !== undefined ){
				for(var i=0; i<obj.stores.length; i++){
					if (obj.stores[i] == s){assList.push(obj);}
				}
			}
		}

		return assList;
	}

	, removeAssociation: function(association, autoDestroy) {

		Sbi.trace("[StoreManager.removeAssociation]: IN");

		if(Sbi.isNotValorized(association)) {
			Sbi.trace("[StoreManager.removeAssociation]: Parameter [association] is not valorized");
			Sbi.trace("[StoreManager.removeAssociation]: OUT");
			return false;
		}

		association = this.stores.removeKey(association);

		if(association === false) {
			Sbi.trace("[StoreManager.removeAssociation]: Impossible to remove association [" + Sbi.toSource(association)  + "]");
		}

		Sbi.trace("[StoreManager.removeAssociation]: OUT");

		return association;
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // fonts methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, addFont: function(font){
		Sbi.trace("[StoreManager.addFont]: IN");

		if(Sbi.isNotValorized(font)) {
			Sbi.warn("[StoreManager.addFont]: Input parameter [font] is not defined");
			Sbi.trace("[StoreManager.addFont]: OUT");
		}

		if(Ext.isArray(font)) {
			Sbi.trace("[StoreManager.addFont]: Input parameter [font] is of type [Array]");
			for(var i = 0; i < store.length; i++) {
				this.addFont(font[i]);
			}
		} else if(Sbi.isNotExtObject(font)) {
			Sbi.trace("[StoreManager.addFont]: Input parameter [font] is of type [Object]");
			this.fonts.add(font);
			Sbi.debug("[StoreManager.addFont]: Font [" + Sbi.toSource(font) + "] succesfully added");
		} else {
			Sbi.error("[StoreManager.addStore]: Input parameter [font] of type [" + (typeof store) + "] is not valid");
		}

		Sbi.trace("[StoreManager.addFont]: OUT");
	}
	
	/**
	 * @methods
	 *
	 * Returns all the fonts defined in this store manager
	 *
	 *  @return {Object[]} The fonts list
	 */
	, getFonts: function() {
		return this.fonts.getRange();
	}
	
	, getFont: function(fontId) {
		return this.fonts.get(fontId);
	}

	, removeFont: function(font, autoDestroy) {

		Sbi.trace("[StoreManager.removeFont]: IN");

		if(Sbi.isNotValorized(font)) {
			Sbi.trace("[StoreManager.removeFont]: Parameter [font] is not valorized");
			Sbi.trace("[StoreManager.removeFont]: OUT");
			return false;
		}

		font = this.stores.removeKey(font);

		if(font === false) {
			Sbi.trace("[StoreManager.removeFont]: Impossible to remove font [" + Sbi.toSource(font)  + "]");
		}

		Sbi.trace("[StoreManager.removeFont]: OUT");

		return font;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // layout methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, addLayout: function(layout){
		Sbi.trace("[StoreManager.addLayout]: IN");

		if(Sbi.isNotValorized(layout)) {
			Sbi.warn("[StoreManager.addLayout]: Input parameter [layout] is not defined");
			Sbi.trace("[StoreManager.addLayout]: OUT");
		}

		if(Ext.isArray(layout)) {
			Sbi.trace("[StoreManager.addLayout]: Input parameter [layout] is of type [Array]");
			for(var i = 0; i < store.length; i++) {
				this.addLayout(layout[i]);
			}
		} else if(Sbi.isNotExtObject(layout)) {
			Sbi.trace("[StoreManager.addLayout]: Input parameter [layout] is of type [Object]");
			this.layouts.add(layout);
			Sbi.debug("[StoreManager.addLayout]: Font [" + Sbi.toSource(layout) + "] succesfully added");
		} else {
			Sbi.error("[StoreManager.addLayout]: Input parameter [layout] of type [" + (typeof store) + "] is not valid");
		}

		Sbi.trace("[StoreManager.layoutFont]: OUT");
	}
	
	/**
	 * @methods
	 *
	 * Returns layout defined in this store manager
	 *
	 *  @return {Object[]} The fonts list
	 */
	, getLayouts: function() {
		return this.layouts.getRange();
	}
	
	, getLayout: function(layoutId) {
		return this.layouts.get(layoutId);
	}

	, removeLayout: function(layout, autoDestroy) {

		Sbi.trace("[StoreManager.removeLayout]: IN");

		if(Sbi.isNotValorized(layout)) {
			Sbi.trace("[StoreManager.removeLayout]: Parameter [layout] is not valorized");
			Sbi.trace("[StoreManager.removeLayout]: OUT");
			return false;
		}

		layout = this.stores.removeKey(layout);

		if(layout === false) {
			Sbi.trace("[StoreManager.removeLayout]: Impossible to remove layout [" + Sbi.toSource(layout)  + "]");
		}

		Sbi.trace("[StoreManager.removeLayout]: OUT");

		return layout;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // filter methods
	// -----------------------------------------------------------------------------------------------------------------

	, addParameter: function(parameter){
		Sbi.trace("[StoreManager.addParameter]: IN");

		if(Sbi.isNotValorized(parameter)) {
			Sbi.warn("[StoreManager.addParameter]: Input parameter [parameter] is not defined");
			Sbi.trace("[StoreManager.addParameter]: OUT");
		}

		if(Ext.isArray(parameter)) {
			Sbi.trace("[StoreManager.addParameter]: Input parameter [parameter] is of type [Array]");
			for(var i = 0; i < store.length; i++) {
				this.addParameter(parameter[i]);
			}
		} else if(Sbi.isNotExtObject(parameter)) {
			Sbi.trace("[StoreManager.addParameter]: Input parameter [parameter] is of type [Object]");
			this.parameters.add(parameter);
			Sbi.debug("[StoreManager.addParameter]: Association [" + Sbi.toSource(parameter) + "] succesfully added");
		} else {
			Sbi.error("[StoreManager.addStore]: Input parameter [parameter] of type [" + (typeof parameter) + "] is not valid");
		}

		Sbi.trace("[StoreManager.addParameter]: OUT");
	}

	/**
	 * @methods
	 *
	 * Returns all the parameters defined in this store manager
	 *
	 *  @return {Object[]} The parameters list
	 */
	, getParameters: function() {
		return this.parameters.getRange();
	}

	, getParameter: function(parameterId) {
		return this.parameters.get(parameterId);
	}

	, containsParameter: function(parameter) {
		if(Ext.isString(parameter)) {
			return this.parameters.containsKey(parameter);
		} else {
			return this.parameters.contains(parameter);
		}
	}


	, removeParameter: function(parameter, autoDestroy) {

		Sbi.trace("[StoreManager.removeParameter]: IN");

		if(Sbi.isNotValorized(parameter)) {
			Sbi.trace("[StoreManager.removeParameter]: Parameter [parameter] is not valorized");
			Sbi.trace("[StoreManager.removeParameter]: OUT");
			return false;
		}

		parameter = this.parameters.removeKey(parameter);

		if(parameter === false) {
			Sbi.trace("[StoreManager.removeParameter]: Impossible to remove parameter [" + Sbi.toSource(parameter)  + "]");
		}

		Sbi.trace("[StoreManager.removeParameter]: OUT");

		return parameter;
	}

	/**
	 * @methods
	 *
	 * Returns true if the store can be parametrized, false otherwise
	 *
	 *  @return {boolean}
	 */
	, isParametricStore: function(s){
		alert('isParametricStore');
	}


	/**
	 * @methods
	 *
	 * @param {Ext.data.Store/String} store The store or the store id.
	 * @return {Object[]} The  parameters list
	 */
	, getStoreParameters: function(store){

		var storeId = this.getStoreId(store);

		var storeParameters = [];
		var parameters = this.getParameters();
		for(var i = 0; i < parameters.length; i++) {
			var parameter = parameters[i];
			if (parameter.labelObj == storeId){
				storeParameters.push(parameter);
			}
		}
		return storeParameters;
	}

	/**
	 * @method
	 *
	 * @returns the parameters' values for the given store. es. [{p1: value1},{p2: value2}]
	 */
	, getStoreParametersValues: function(store) {
		Sbi.trace("[StoreManager.getStoreParametersValues]: IN");

		var parametersValues = {};

		var storeId = this.getStoreId(store);

		var parameters = this.getStoreParameters(storeId);
		if (Sbi.isValorized(parameters)){
			Sbi.trace("[StoreManager.getStoreParametersValues]: Store [" + storeId + "] is parametric");

			for(f in parameters){
				var obj = parameters[f];
				if (Sbi.isValorized(obj.namePar)){
					var label = obj.namePar;
					var value = null;
					if (obj.scope == 'Relative'){
						value = this.getContextValue(obj.initialValue);
					}else{
						value = obj.initialValue;
					}

					if (Sbi.isValorized(value)){parametersValues[label] = value;}
				}
			}
		}
		Sbi.trace("[StoreManager.getStoreParametersValues]: OUT");

		return parametersValues;
	}

	, getContextValue: function(l){

		if (!Sbi.isValorized(Sbi.config.executionContext)){
			Sbi.trace("[StoreManager.getContextValue]: Impossible to get context value for label [" + l + "]. ExcutionContext is null !! ");
			return null;
		}

		Sbi.trace("[StoreManager.getContextValue]: get context value for label [" + l + "] ... ");
		for (p in Sbi.config.executionContext){
			var v = Sbi.config.executionContext[p];
			if (p == l){
				Sbi.trace("[StoreManager.getContextValue]: getted value [" + v + "]");
				return v;
			}
		}
	}


	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------


    , createStore: function(storeConf) {

    	Sbi.trace("[StoreManager.createStore]: IN");
    	Sbi.trace("[StoreManager.createStore]: store [" + storeConf.storeId + "] conf is equal to [" + Sbi.toSource(storeConf, true)+ "]");

    	var proxy = new Ext.data.HttpProxy({
			url: Sbi.config.serviceReg.getServiceUrl('loadDataSetStore', {
				pathParams: {datasetLabel: storeConf.storeId}
			})
			, method: 'GET'
	    	//, timeout : this.timeout
	    });
    	proxy.on('exception', this.onStoreLoadException, this);
    	Sbi.trace("[StoreManager.createStore]: proxy sucesfully created");

		Ext.define(storeConf.storeId, {
		     extend: 'Ext.data.Model',
		     fields: [
		         {name: 'data', type: 'string'}
		     ]
		});

		var reader = new Ext.data.JsonReader();
		reader.on('exception', this.onStoreReadException, this);
		Sbi.trace("[StoreManager.createStore]: reader sucesfully created");

		var storeType = "Sbi.widgets.store.InMemoryFilteredStore";
		if(storeConf.stype=="crosstab"){
			storeType = 'Sbi.cockpit.widgets.crosstab.CrossTabStore';
		}



//
//		for(var i=0; i<this.widgetConfig.length; i++){
//			var aWidget = this.widgetConfig[i];
//			if(aWidget.wtype == "crosstab" && (storeConf.storeId == aWidget.storeId)){
//				storeType = "Sbi.cockpit.widgets.crosstab.CrossTabStore";
//			}
//		}

		var store = Ext.create(storeType,{
			storeId: storeConf.storeId,
			storeType: 'sbi',
			storeConf: storeConf,
			model: storeConf.storeId,
	        proxy: proxy,
	        reader: reader,
	        remoteSort: false,
	        test: new Date()
	    });
		store.on('load', this.onStoreLoad, this);
		store.on('metachange', this.onStoreMetaChange, this);
		Sbi.trace("[StoreManager.createStore]: store sucesfully created");






		if(Sbi.isValorized(storeConf.aggregations)) {
			this.setAggregationOnStore(store, storeConf.aggregations);
			Sbi.trace("[StoreManager.createStore]: aggregations sucesfully add to store");
		} else {
			Sbi.trace("[StoreManager.createStore]: there are no aggregations to add to store");
		}


		Sbi.trace("[StoreManager.createStore]: IN");

		return store;
	}



    , createStoreOld: function(c) {
    	var s = null;
    	if (c[i].memoryPagination !== undefined &&  c[i].memoryPagination === false){
			//server pagination
			s = new Sbi.data.Store({
				storeId: c[i].storeId
				, autoLoad: false
				, refreshTime: c[i].refreshTime
				, limitSS: this.limitSS
				, memoryPagination: c[i].memoryPagination || false
			});
		} else {
			//local pagination (default)
			s = new Sbi.data.MemoryStore({
				storeId: c[i].storeId
				, autoLoad: false
				, refreshTime: c[i].refreshTime
				, rowsLimit:  c[i].rowsLimit || this.rowsLimit
				, memoryPagination: c[i].memoryPagination || true	//default pagination type is client side
			});
		}

    	s.ready = c[i].ready || false;
		s.storeType = 'sbi';

		//to optimize the execution time, the store is created with the stopped property to false, so it's loaded
		//when the component (widget or grid) is viewed.
		s.stopped = true;

		return s;
    }

    , createTestStore: function(c) {
    	var testStore = new Ext.data.JsonStore({
			id: 'testStore'
			, fields:['name', 'visits', 'views']
	        , data: [
	            {name:'Jul 07', visits: 245000, views: 3000000},
	            {name:'Aug 07', visits: 240000, views: 3500000},
	            {name:'Sep 07', visits: 355000, views: 4000000},
	            {name:'Oct 07', visits: 375000, views: 4200000},
	            {name:'Nov 07', visits: 490000, views: 4500000},
	            {name:'Dec 07', visits: 495000, views: 5800000},
	            {name:'Jan 08', visits: 520000, views: 6000000},
	            {name:'Feb 08', visits: 620000, views: 7500000}
	        ]
	    });

		testStore.ready = true;
		testStore.storeType = 'ext';
		return testStore;
    }

	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------

    , onAssociationGroupRefreshed: function(response, options) {
		if(response !== undefined && response.statusText=="OK") {
    		var r = response.responseText || response.responseXML;
			if(Sbi.isValorized(r)) {
				if(r.indexOf("error.mesage.description")>=0){
					Sbi.exception.ExceptionHandler.handleFailure(response);
				} else {
					//alert("Response of [/api/1.0/associations/]:" + r);
					this.associationGroups = Ext.JSON.decode(r);
				}
			} else {
				Sbi.exception.ExceptionHandler.showErrorMessage('Server response body is empty', 'Service Error');
			}
		} else {
			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		}
	}

    , onAssociationGroupReloaded: function(response, options) {
    	Sbi.trace("[StoreManager.onAssociationGroupReloaded]: IN");

    	Sbi.trace("[StoreManager.onAssociationGroupReloaded]: Options object contains the following properties: " + Sbi.toSource(options.params, true));
    	Sbi.trace("[StoreManager.onAssociationGroupReloaded]: Options object is equal to [" + Sbi.toSource(options.params, true) + "]");


    	var storeIds = Ext.JSON.decode(options.params.datasets);
    	var aggregations = Ext.JSON.decode(options.params.aggregations);
    	var associationGroup = Ext.JSON.decode(options.params.associationGroup);
    	var selections = Ext.JSON.decode(options.params.selections);

    	for(var i = 0; i < storeIds.length; i++) {
			var store = this.getStore(storeIds[i], aggregations);
			store.fireEvent('association', store, associationGroup, selections);
		}

		if(response !== undefined && response.statusText=="OK") {
    		var r = response.responseText || response.responseXML;
			if(Sbi.isValorized(r)) {
				if(r.indexOf("error.mesage.description")>=0){
					Sbi.exception.ExceptionHandler.handleFailure(response);
				} else {
					var stores =  Ext.JSON.decode(r);
					if(stores.errors && stores.errors.length > 0) {
						var msg = "Impossible to load dataset(s) " + options.params.datasets + " due to the following service errors: <p><ul>";
						for(var i = 0; i < stores.errors.length; i++) {
							msg += "<li>" + stores.errors[i].message + ";";
						}
						msg += "</ul>";

						Ext.Msg.show({
							   title: "Service error",
							   msg: msg,
							   buttons: Ext.Msg.OK,
							   icon: Ext.MessageBox.ERROR,
							   modal: false
						});
						return;
					}


					var aggregations = undefined;
					if(options.params && options.params.aggregations) {
						aggregations = Ext.JSON.decode(options.params.aggregations);
					}
					for(var s in stores) {
						Sbi.trace("[StoreManager.onAssociationGroupReloaded]: Reloaded store [" + s + "]");
						var data = stores[s];
						var store = this.getStore(s, aggregations);
						if(store) {
							//alert(s + " = " + Sbi.toSource(data));
				    		store.loadData(data);
				    		Sbi.trace("[StoreManager.onAssociationGroupReloaded]: Data sucesfully loaded into store [" + s + "] at aggregation level [" + aggregations + "]");
				    	} else {
				    		Sbi.error("[StoreManager.onAssociationGroupReloaded]: Impossible to load data into store [" + s + "] " +
				    				"at aggregation level [" + Sbi.toSource(aggregations) + "]");
				    	}
					}
				}
			} else {
				Sbi.exception.ExceptionHandler.showErrorMessage('Server response body is empty', 'Service Error');
			}
		} else {
			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		}

		Sbi.trace("[StoreManager.onAssociationGroupReloaded]: OUT");
	}

    , onStoreLoadException: function(proxy, response, operation, eOpts) {
    	Sbi.trace("[StoreManager.onStoreLoadException]: response attributes are [" + Sbi.toSource(response, true)) + "]";
    	Sbi.trace("[StoreManager.onStoreLoadException]: response status is equal to [" + response.status + "]");
    	Sbi.trace("[StoreManager.onStoreLoadException]: response status is equal to [" + response.statusText + "]");
    	Sbi.trace("[StoreManager.onStoreLoadException]: response status is equal to [" + response.responseText + "]");
	}

    , onStoreReadException: function(reader, response, error, eOpts) {
    	Sbi.trace("[StoreManager.onStoreReadException]: error is equal to [" + Sbi.toSource(error, true) + "]" );
    	Sbi.trace("[StoreManager.onStoreReadException]: eOpts is equal to [" + Sbi.toSource(eOpts, true) + "]" );

    	Sbi.trace("[StoreManager.onStoreReadException]: response attributes are [" + Sbi.toSource(response, true)) + "]";
    	Sbi.trace("[StoreManager.onStoreReadException]: response status is equal to [" + response.status + "]");
    	Sbi.trace("[StoreManager.onStoreReadException]: response status is equal to [" + response.statusText + "]");
    	Sbi.trace("[StoreManager.onStoreReadException]: response status is equal to [" + response.responseText + "]");
	}

    , onStoreMetaChange: function(store, meta) {
    	Sbi.trace("[StoreManager.onStoreMetaChange]: IN");

		try {
			var fieldsMeta = {};

			for(var i = 0; i < meta.fields.length; i++) {
				var f = meta.fields[i];
				if(Ext.isString(f)) {
					continue;
				}
				f.header = f.header || f.name;
				fieldsMeta[f.header] = f;
			}
			store.fieldsMeta = fieldsMeta;
		} catch(e) {
			alert("[StoreManager.onStoreMetaChange]: " + e);
		}

		//alert(this.getStoreId(store) +  " = " + Sbi.toSource(store.fieldsMeta));

		Sbi.trace("[StoreManager.onStoreMetaChange]: OUT");
	}

    , onStoreLoad: function(store, records, successful, eOpts) {

   	Sbi.trace("[StoreManager.onStoreLoad]: IN");

	if(store && store.crossTabStore && store.proxy.reader && store.proxy.reader.jsonData && store.proxy.reader.jsonData.metaData){
		store.myStoreMetaData = Ext.apply(store.proxy.reader.jsonData.metaData,{});
	}

	// {"service":"/1.0/datasets/AAA_SALES_1998/data",
	// "errors":[{"message":"An unexpected [java.lang.NullPointerException] exception has been trown during service execution"}]}

	var recordsNumber = store.getTotalCount();

	store.status = "ready";

	if (recordsNumber == 1){
		var rawData = store.getAt(0).raw;
		if (Sbi.isValorized(rawData) && Sbi.isValorized(rawData.errors)) {
			store.status = "error";

			var msg = "Impossible to load dataset [" + this.getStoreId(store) + "] due to the following service errors: <p><ul>";
			for(var i = 0; i < rawData.errors.length; i++) {
				msg += "<li>" + rawData.errors[i].message + ";";
			}
			msg += "</ul>";

			Ext.Msg.show({
				   title: "Service error",
				   msg: msg,
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR,
				   modal: false
			});
		

	}

	if(recordsNumber == 0) {
		Ext.Msg.show({
			   title: LN('sbi.qbe.messagewin.info.title'),
			   msg: LN('sbi.qbe.datastorepanel.grid.emptywarningmsg'),
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.INFO,
			   modal: false
		});
	}

	Sbi.trace("[StoreManager.onStoreLoad]: store [" + this.getStoreId(store) + "] reloaded succesfully: " + successful);
	if(successful) {
		Sbi.trace("[StoreManager.onStoreLoad]: record loaded for store [" + this.getStoreId(store) + "] are [" + records.length + "]");
	}
	Sbi.trace("[StoreManager.onStoreLoad]: eOpts for store [" + this.getStoreId(store) + "] is equal to [" + Sbi.toSource(eOpts, true) + "]");

	Sbi.trace("[StoreManager.onStoreLoad]: OUT");

}
     }


	/**
	 * @method
	 * Clones the input store by creating a new one with the same model and adding one record of the source one at a time,
	 * therefore the store is expected to contain data
	 *
	 * @param {Ext.data.Store} source The Store to be cloned
	 */
    ,
    cloneStore : function(source) {
        var target = Ext.create(Ext.getClassName(source), {
            model: source.model
            //, proxy: source.proxy
        });

        Ext.each (source.getRange (), function (record) {
            var newRecordData = Ext.clone (record.copy().data);
            var model = new source.model (newRecordData, newRecordData.id);

            target.add (model);
        });

        if (Ext.getClassName(source) == 'Sbi.widgets.store.InMemoryFilteredStore') {
        	target.inMemoryData = source.inMemoryData;
        }

        return target;
    }
    , cleanCache : function(){
    	// ids of data store
    	var ids  = this.getStoreIds();

    	if(ids.length == 0){
    		Sbi.exception.ExceptionHandler.showWarningMessage(LN("sbi.cockpit.storeManager.noDatastoreToClean"), "Warning");
    	}
    	else {

    	var params = {datasetLabels: ids};

    	Ext.Ajax.request({
		    url: Sbi.config.serviceReg.getServiceUrl('cleanCache', {
				pathParams: params
			}),
		    method: 'DELETE',
		    params: {
		       requestParam: 'notInRequestBody'
		    },
		    success : function(){
				Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.cockpit.storeManager.cacheCleaned'), 'Info');
		    },
			failure: function(){
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.cockpit.storeManager.errorInCleaningCache'), 'Service Error');
				},
			scope: this
		});

    	}
    }


});


