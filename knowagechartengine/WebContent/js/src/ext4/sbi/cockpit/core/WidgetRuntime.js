/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/





/**
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.cockpit.core");

/**
 * @class Sbi.cockpit.core.WidgetRuntime
 * @extends Ext.Panel
 *
 * It's an abstract class that simplify the implementation of the runtime facet of a new widget extension.
 * See {@link Sbi.cockpit.core.WidgetExtensionPointManager WidgetExtensionPointManager} to find out more
 * information on widget's extension point.
 */

/**
 * @cfg {Object} config the widget configuration object
 * @cfg {String} config.storeId The label of the dataset used to feed the widget
 * @cfg {String} config.wtype The type of the widget
 * @cfg {Object} config.wconf The custom configuration of the widget. Its content depends on the widget's #wtype
 * @cfg {Object} config.wstyle The style configuration of the widget. Its content depends on the widget's #wtype
 * @cfg {Object} config.wlayout The layout configuration of the widget. Its content depends on the widget's parent {@link #parentContainer container container}
 */
Sbi.cockpit.core.WidgetRuntime = function(config) {

	Sbi.trace("[WidgetRuntime.constructor]: IN");

	// init properties...
	var defaultSettings = {
		border: false
		, bodyBorder: false
		, hideBorders: true
		, frame: false
		, defaultMsg: ' '
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.core.WidgetRuntime', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	if(Sbi.isNotValorized(this.items) || (Ext.isArray(this.items) && this.items.length === 0)) {
		Sbi.trace("[WidgetRuntime.constructor]: the widget is empty");
		this.msgPanel = new Ext.Panel({
			html: this.defaultMsg
		});
		c = Ext.apply(c, {
			border: false
			, bodyBorder: false
			, hideBorders: true
			, frame: false
		   	, items: [this.msgPanel]
		});
	} else {
		Sbi.trace("[WidgetRuntime.constructor]: the widget has been properly initialized by subclass");
	}


	// constructor
	Sbi.cockpit.core.WidgetRuntime.superclass.constructor.call(this, c);

	this.on("beforeDestroy", function(){
		this.unboundStore();
		Sbi.trace("[WidgetRuntime.onBeforeDestroy]: store unbounded");
	}, this);

	Sbi.trace("[WidgetRuntime.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.core.WidgetRuntime, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Sbi.cockpit.core.WidgetPanel} parentContainer
     * The WidgetPanel object that contains the widget
     */
    parentContainer: null

    , loadingMask: null

    /**
     * @property {String} storeId
     * The label of the dataset used to feed the widget
     */
    , storeId: null

    , aggregations: null

    /**
     * @property {String} wtype
     * The wtype of the widget extension as registered in {@link Sbi.cockpit.core.WidgetExtensionPointManager}
     * to which this runtime class is associated.
     */
    , wtype: null

    /**
     * @property {String} wconf
     * The custom configuration of the widget. Its content depends on the widget type
     */
    , wconf: null

    /**
     * @property {String} wlayout
     * The custom layout configuration of the widget. Its content depends on the widget container used
     */
    , wlayout: null

    /**
     * @property {String} wstyle
     * The custom style of the widget.
     */
    , wstyle: null

    /**
     * @property {List} filters
     * The filters of the widget.
     */
    , filters: null

    /**
     * @property {String} wgeneric
     * The generic parameters of the widget.
     */
    , wgeneric: null

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

    , getWidgetId: function() {
    	return this.getId();
    }

	, getWidgetName: function() {
		return this.getGenericConfiguration().title || this.getWidgetId();
	}

    /**
     * Equivalent to reload. TODO delete it!
     */

    , refresh:  function() {
		Sbi.trace("[WidgetRuntime.refresh]: IN");
		this.reload();
		Sbi.trace("[WidgetRuntime.refresh]: OUT");
	}

	, reload: function(params, baseParams) {
		Sbi.trace("[WidgetRuntime.reload]: IN");
		var store = this.getStore();
		if(!store) {
			Sbi.warn("[WidgetRuntime.reload]: store [" + this.getStoreId() + "] not yet defiend in store manager");
			return;
		}
		store.removeAll();
		Sbi.storeManager.loadStore(store, params, baseParams);
		Sbi.trace("[WidgetRuntime.reload]: OUT");
	}


	/**
	 * Redraw the widget's content. The data are left unchanged
	 * This method is abstract and so must be implemented properly by subclass.
	 */
	, redraw:  function() {
		Sbi.trace("[WidgetRuntime.redraw]: IN");
		Sbi.trace("[WidgetRuntime.redraw]: OUT");
	}

	/**
	 * @method
	 *
	 * Sets the configuration of the widget. All part of the configuration are optional. If a part of the configuratio is not specified
	 * it is simply not applied.
	 *
	 * @param {Object} config The widget configuration object.
	 * @param {String} config.storeId The label of the dataset used to feed the widget.
	 * @param {String} config.wtype The wtype of the widget.
	 * @param {Object} config.wconf The custom configuration of the widget. Its content depends on the widget's #wtype.
	 * @param {Object} config.wstyle The style configuration of the widget. Its content depends on the widget's #wtype.
	 * @param {Object} config.wlayout The layout configuration of the widget. Its content depends on the widget's #parentContainer
	 * @param {Object} config.wgeneric The generic configuration of the widget.
	 * @param {boolean} refresh true to force the refresh of the widget after  the configuration is set, false otherwise. The default is true.
	 */
	, setConfiguration: function(config, refresh) {
		Sbi.trace("[WidgetRuntime.setConfiguration]: IN");

		var needRefresh = this.isRefreshable(config);

		var aggregations;
		if(config.storeConf) aggregations = config.storeConf.aggregations;
		this.setStoreId(config.storeId, aggregations, false);
		this.setWType(config.wtype, false);
		this.setCustomConfiguration(config.wconf, false);
    	this.setStyleConfiguration(config.wstyle, false);
    	this.setLayoutConfiguration(config.wlayout);
    	this.setGenericConfiguration(config.wgeneric);

    	if (Sbi.isValorized(refresh)){
    		if(refresh !== false) {
    			this.refresh();
    		} else {
    			Sbi.trace("[WidgetRuntime.setConfiguration]: Input parameter [refresh] is equal [" + refresh + "] to so widget won't be refreshed");
    		}
    	} else {
    		if(needRefresh) {
    			Sbi.trace("[WidgetRuntime.setConfiguration]: There are some changes in configurations so the widget will be refreshed");
    			this.refresh();
    		} else {
    			Sbi.trace("[WidgetRuntime.setConfiguration]: No change found in configuration, so widget won't be refreshed");
    		}
    	}

		Sbi.trace("[WidgetRuntime.setConfiguration]: OUT");
	}

	/**
	 * @method
	 *
	 * Returns the widget configuration object.
	 *
	 * @return {Object} The widget configuration object.
	 * @return {String} return.storeId The label of the dataset used to feed the widget.
	 * @return {String} return.wtype The wtype of the widget.
	 * @return {Object} return.wconf The custom configuration of the widget. Its content depends on the widget's #wtype.
	 * @return {Object} return.wstyle The style configuration of the widget. Its content depends on the widget's #wtype.
	 * @return {Object} return.wlayout The layout configuration of the widget. Its content depends on the widget's #parentContainer
	 */
    , getConfiguration: function() {
    	Sbi.trace("[WidgetRuntime.getConfiguration]: IN");

    	var config = {};

    	if(Sbi.isValorized(this.getStoreId())) config.storeId = this.getStoreId();
		if(Sbi.isValorized(this.getWType())) config.wtype = this.getWType();
    	config.wconf = this.getCustomConfiguration() || {};
    	config.wstyle = this.getStyleConfiguration() || {};
    	config.wlayout = this.getLayoutConfiguration() || {};
    	config.wgeneric = this.getGenericConfiguration() || {};

    	Sbi.trace("[WidgetRuntime.getConfiguration]: OUT");

    	return config;
	}

    /**
	 * @method
	 *
	 * Initialize the store
	 */
	, boundStore: function() {
		Sbi.trace("[WidgetRuntime.boundStore]: IN");
		var bounded = false;
		var store = this.getStore();
		if(Sbi.isValorized(store)) {
			store.on('metachange', this.onStoreMetaChange, this);
			store.on('datachanged', this.onDataChanged, this);
			store.on('beforeload', this.onStoreBeforeLoad, this);
			store.on('load', this.onStoreLoad, this);
			store.on('beforeassociation', this.onStoreBeforeAssociation, this);
			store.on('association', this.onStoreAssociation, this);
			// exceptions are centralized managed by the store manager
			bounded = true;
		} else {
			alert("[WidgetRuntime.boundStore]: Impossible to find bounded store");
			bounded = false;
		}

		Sbi.trace("[WidgetRuntime.boundStore]: OUT");
		return bounded;
	}

	, unboundStore: function() {
		Sbi.trace("[WidgetRuntime.unboundStore]: IN");
		var store = this.getStore();
		if(Sbi.isValorized(store)) {
			store.un('metachange', this.onStoreMetaChange, this);
			store.un('datachanged', this.onDataChanged, this);
			store.un('beforeload', this.onStoreBeforeLoad, this);
			store.un('load', this.onStoreLoad, this);
			store.un('beforeassociation', this.onStoreBeforeAssociation, this);
			store.on('association', this.onStoreAssociation, this);
		} else {
			Sbi.debug("Widget is not bound to any store or it is bound to a store that has already been removed from store manager");
		}

		Sbi.trace("[WidgetRuntime.unboundStore]: OUT");
	}

	, setStoreId: function(storeId, aggregations, refresh) {
		if(Sbi.isValorized(storeId)) {

			var currentStore = this.getStore();
			var currentAggregations = Sbi.storeManager.getAggregationOnStore(currentStore);

			if(storeId == this.storeId && Sbi.storeManager.isSameAggregationLevel(currentAggregations, aggregations)  ) {
				Sbi.trace("[WidgetRuntime.setStoreId]: New store id and aggregations are equal to the old one. Nothing to update.");
				Sbi.trace("[WidgetRuntime.setStoreId]: OUT");
				return;
			}

			this.unboundStore();
			this.storeId = storeId;
			this.aggregations = aggregations;
			Sbi.trace("[WidgetRuntime.setStoreId]: Store id set to [" + storeId + "]");
			this.boundStore();

			if(this.rendered === true && refresh !== false) {
				this.refresh();
			} else {
				Sbi.trace("[WidgetRuntime.setStoreId]: Input parameter [refresh] is equal [" + refresh + "] to so widget won't be refreshed");
			}
		} else {
			Sbi.trace("[WidgetRuntime.setStoreId]: Input parameter [storeId] is not valorized so the property [storeId] will be left unchanged");
		}

	}

    /**
     * @method
     *
     * Returns the label of the dataset used to feed the widget
     *
     * @return {String} the dataset's label
     */
    , getStoreId: function() {
    	return this.storeId;
    }

	/**
	 * @method
	 *
	 * Returns the store that feed this widget
	 *
	 * @param {boolean} forceCreation true to create and add a new store to <code>Sbi.storeManager</code> if there is not
	 * yet a store with #storeId already managed by global store manager, false otherwise. The default value is false.
	 *
	 * @return {Ext.data.Store} The store
	 */
	, getStore: function(forceCreation) {
		var store;

		Sbi.trace("[WidgetRuntime.getStore]: IN");

		if(Sbi.isNotValorized(this.getStoreId())) {
			Sbi.warn("[WidgetRuntime.getStore]: The widget have no store associated (i.e storeId in not valorized)");
			return null;
		}

		Sbi.trace("[WidgetRuntime.getStore]: aggregation defined on this widget are equals to [" + Sbi.toSource(this.aggregations) + "]");

		if(Sbi.storeManager.containsStore(this.getStoreId(), this.aggregations) === false && forceCreation === true) {
			Sbi.trace("[WidgetRuntime.getStore]: store [" + this.getStoreId() + "] will be added to store manager");
			var storeConfig = {storeId: this.getStoreId()};
			if(aggregations !== null) {
				storeConfig.aggregations = this.aggregations;
			}
			Sbi.storeManager.addStore(storeConfig);
		}
		store = Sbi.storeManager.getStore( this.getStoreId(), this.aggregations );

		Sbi.trace("[WidgetRuntime.getStore]: OUT");

		return store;
	}


	/**
	 * @method
	 * @private
	 *
     * Sets the wtype for this widget
     *
     * @param {String} The wtype
     * @param {boolean} refresh true to force the refresh after the setting of the property, false otherwise. The default is true.
     */
	, setWType: function(wtype, refresh) {
		if(Sbi.isValorized(wtype)) {
			this.wtype = wtype;
			Sbi.trace("[WidgetRuntime.setWType]: wtype set to [" + wtype + "]");
			if(refresh !== false) {
				this.refresh();
			} else {
				Sbi.trace("[WidgetRuntime.setWType]: Input parameter [refresh] is equal [" + refresh + "] to so widget won't be refreshed");
			}
		} else {
			Sbi.trace("[WidgetRuntime.setWType]: Input parameter [wtype] is not valorized so the property [wtype] will be left unchanged");
		}

	}

	/**
	 * @method
	 *
     * Gets the wtype for this widget as registered in {@link Sbi.cockpit.core.WidgetExtensionPointManager}
     *
     * @return {String} The wtype
     */
	, getWType: function() {
		return this.wtype;
	}

	/**
	 * @method
	 *
	 * Sets the custom configuration of the widget. Its content depends on the widget's #wtype.
	 *
	 * @param {Object} wconf The custom configuration of the widget.
	 * @param {boolean} refresh true to force the refresh after the setting of the property, false otherwise. The default is true.
	 */
	, setCustomConfiguration: function(wconf, refresh) {
		if(Sbi.isValorized(wconf)) {
			this.wconf = wconf;
			Sbi.trace("[WidgetRuntime.setCustomConfiguration]: wconf set to [" + Sbi.toSource(wconf) + "]");

			if (Sbi.isValorized(wconf.series)){
				this.aggregations.measures = wconf.series;
				Sbi.trace("[WidgetRuntime.setCustomConfiguration]: aggregations.measures set to [" + Sbi.toSource(wconf.measures) + "]");
			}

			if(refresh !== false) {
				this.refresh();
			} else {
				Sbi.trace("[WidgetRuntime.setCustomConfiguration]: Input parameter [refresh] is equal [" + refresh + "] to so widget won't be refreshed");
			}
		} else {
			Sbi.trace("[WidgetRuntime.setCustomConfiguration]: Input parameter [wconf] is not valorized so the property [wconf] will be left unchanged");
		}
	}

	/**
	 * @method
	 *
	 * Sets the wgeneric configuration of the widget.
	 *
	 * @param {Object} wgeneric The generic configuration of the widget.
	 * @param {boolean} refresh true to force the refresh after the setting of the property, false otherwise. The default is true.
	 */
	, setGenericConfiguration: function(wgeneric) {
		if(Sbi.isValorized(wgeneric)) {
			this.wgeneric = wgeneric;
			Sbi.trace("[WidgetRuntime.setGenericConfiguration]: wgeneric set to [" + Sbi.toSource(wgeneric) + "]");

			this.getParentComponent().refreshTitle();
		} else {
			Sbi.trace("[WidgetRuntime.setGenericConfiguration]: Input parameter [wgeneric] is not valorized so the property [wgeneric] will be left unchanged");
		}
	}

    /**
	 * @method
	 *
	 * Gets the custom configuration of the widget. Its content depends on the widget's #wtype.
	 *
	 * @return The custom configuration of the widget.
	 */
	, getCustomConfiguration: function() {
		Sbi.trace("[WidgetRuntime.getCustomConfiguration]: IN");

		if ( this.getWType() )  this.wconf.wtype =  this.getWType();
		var config = Ext.apply({}, this.wconf || {});
		Sbi.trace("[WidgetRuntime.getCustomConfiguration]: OUT");
		return config;
	}

	/**
	 * @method
	 *
	 * Gets the wgeneric configuration of the widget.
	 *
	* @return The generic configuration of the widget.
	 */
	, getGenericConfiguration: function() {
		Sbi.trace("[WidgetRuntime.getGenericConfiguration]: IN");

		var config = Ext.apply({}, this.wgeneric || {});
		Sbi.trace("[WidgetRuntime.getGenericConfiguration]: OUT");
		return config;
	}

	/**
	 * @method
	 *
	 * Gets the title of the widget, blank otherwise
	 *
	* @return The generic configuration of the widget.
	 */
	, getTitle: function() {
		Sbi.trace("[WidgetRuntime.getTitle]: IN");

		var config = Ext.apply({}, this.wgeneric || {});
		Sbi.trace("[WidgetRuntime.getTitle]: OUT");

		if (config.title){
			return config.title;
		} else {
			return '';
		}
	}

	/**
	 * @method
	 *
	 * Sets the style configuration of the widget. Its content depends on the widget's #wtype.
	 *
	 * @param {Object} wconf The custom configuration of the widget.
	 * @param {boolean} refresh true to force the refresh after the setting of the property, false otherwise. The default is true.
	 */
	, setStyleConfiguration: function(wstyle) {
		if(Sbi.isValorized(wstyle)) {
			this.wstyle = wstyle;
			Sbi.trace("[WidgetRuntime.setStyleConfiguration]: wstyle set to [" + Sbi.toSource(wstyle) + "]");
		} else {
			Sbi.trace("[WidgetRuntime.setStyleConfiguration]: Input parameter [wstyle] is not valorized so the property [wstyle] will be left unchanged");
		}
	}


	 /**
	 * @method
	 *
	 * Gets the style configuration of the widget. Its content depends on the widget's #wtype.
	 *
	 * @return The style configuration of the widget.
	 */
	, getStyleConfiguration: function() {
		return this.wstyle;
	}


	/**
	 * @method
	 *
	 * Sets the layout configuration of the widget. Its content depends on the widget's #parentContainer.
	 *
	 * @param {Object} wlayout The layout configuration of the widget.
	 * @param {boolean} toContainer true to force the set of the new layout also in the #parentContainer. The default is false.
	 */
	, setLayoutConfiguration: function(wlayout, toContainer) {
		if(Sbi.isValorized(wlayout)) {
			this.wlayout = wlayout;
			if(toContainer === true) {
				Sbi.trace("[WidgetRuntime.setLayoutConfiguration]: update layout configuration also in parent container");
				//TODO update the layoutcof of this widet also in its container
			}
			Sbi.trace("[WidgetRuntime.setLayoutConfiguration]: wlayout set to [" + Sbi.toSource(wlayout) + "]");
		} else {
			Sbi.trace("[WidgetRuntime.setLayoutConfiguration]: Input parameter [wlayout] is not valorized so the property [wlayout] will be left unchanged");
		}
	}

	 /**
	 * @method
	 *
	 * Gets the layout configuration of the widget. Its content depends on the widget's #parentContainer.
	 *
	 * @param {boolean} fromContainer true to get the layoutConf directly form #parentConatiner, false to get it from the widget cached copy.
	 * The widget's cached copy may be different from the one that come from the #parentContainer that is the most updated. Usually the
	 * widget's cached copy is synchronized with the #parentContainer one when the widget configuration is saved.
	 *
	 * @return The layout configuration of the widget.
	 */
	, getLayoutConfiguration: function(fromContainer) {
		var wl =  this.wlayout;
		if(fromContainer === false) {
			return wl;
		}

		if(this.isBoundToAContainer() === true) {
			wl = wl || {};
			wl.region = this.getWidgetRegion(true) || wl.region;
		}
		return wl;
	}

	/**
	 * @method
	 * @deprecated
	 *
	 * Returns the region used by this widget in the #parentContainer to which it is bound. Its content depends on the particular implementation
	 * of the container used.
	 *
	 * @param {boolean} relative true to return all measures expressed in relative units (i.e. % of the #parentContainer dimensions).
	 *
	 * @returns {Object} The region used by the widget
	 */
    , getWidgetRegion: function(relative) {
    	Sbi.trace("[WidgetRuntime.getWidgetRegion]: IN");

    	var r = null;

    	if(this.isBoundToAContainer() === true) {
    		Sbi.trace("[WidgetRuntime.getWidgetRegion]: widget [" + this.getId()+ "] is bound to a container");
    		var parentContainer = this.getParentContainer();
    		var parentComponent = this.getParentComponent();
    		r = parentContainer.getComponentRegion( parentComponent, relative );
    		if(r === null) {
    			Sbi.warn("[Widget.getWidgetManager]: Widget [" + this.toString() + "] is bound to a widget container but it is not possible to retrive the region it occupies");
    		}
    	} else {
    		Sbi.trace("[WidgetRuntime.getWidgetRegion]: widget [" + this.getId()+ "] is not bound to a container");
    		if(this.wlayout && this.wlayout.region) {
    			r = this.wlayout.region;
    		}
    	}

    	Sbi.trace("[WidgetRuntime.getWidgetRegion]: OUT");

    	return r;
    }

	/**
	 * @method
	 *
	 * Returns the #parentContainer that embed the widget. The parent component implementation depends on the container in
	 * which the widget is deployed
	 *
	 * @return {Sbi.cockpit.core.WidgetContainerComponent} The parent component
	 */
    , getParentComponent: function() {
		return this.parentComponent;
	}

    /**
	 * @method
	 *
	 * Sets the parent component that embed the widget. The parent component implementation depends on the container in
	 * which the widget is deployed
	 *
	 * @param {Sbi.cockpit.core.WidgetContainerComponent} component The parent component
	 *
	 */
    , setParentComponent: function(component) {
		this.parentComponent = component;
		Sbi.trace("[WidgetRuntime.setParentComponent]: Parent container of widget [" + this.id +  "] is [" + (component?component.id:"null") + "]");
	}

    /**
	 * @method
	 *
	 * Returns the container in which the widget is bounded, null if the widget is not bounded to any container
	 *
	 * @return {Sbi.cockpit.core.WidgetContainer} The container to which teh widget is bounded
	 */
    , getParentContainer: function() {
    	Sbi.trace("[WidgetRuntime.getParentContainer]: IN");

    	var container = null;

		var component = this.getParentComponent();

		if(Sbi.isValorized(component)) {
			container = component.getParentContainer();
		} else {
			Sbi.warn("[Widget.getParentContainer]: widget [" + this.id + "] is not bound to any component");
		}

		Sbi.trace("[WidgetRuntime.getParentContainer]: OUT");

		return container;
	}

    , setParentContainer: function(container) {
        Sbi.trace("[WidgetContainerComponent.setParentContainer]: IN");

        this.parentContainer = container;

        Sbi.trace("[WidgetContainerComponent.setParentContainer]: OUT");
    }

    /**
	 * @method
	 *
	 * Returns true if the widget is bounded to a container, false otherwise.
	 *
	 * @return {boolean} true if the widget is bounded to a container, false otherwise.
	 */
    , isBoundToAContainer: function() {
    	Sbi.trace("[WidgetRuntime.isBoundToAContainer]: IN");
    	var isBound = false;
    	var container = this.getParentContainer();
    	if(container != null) {
    		isBound = true;
    		//Sbi.trace("[WidgetRuntime.getParentContainer]: widget [" + this.id +  "] is bound to container [" + container.id + "]");
    	}
    	Sbi.trace("[WidgetRuntime.isBoundToAContainer]: OUT");
    	return isBound;
    }

    /**
	 * @method
	 *
	 * Returns the widget manager to which the widget is registered. The widget manager manage the lifecycle
	 * of all widgets bounded to a particular widget container.
	 *
	 * @return {Sbi.cockpit.core.WidgetManager} the widget manager to which the widget is registered.
	 */
    , getWidgetManager: function() {
    	Sbi.trace("[WidgetRuntime.getWidgetManager]: IN");

    	var widgetManager = null;

    	if(this.isBoundToAContainer() === true) {
    		widgetManager = this.getParentContainer().getWidgetManager();
    		if(widgetManager === null) {
    			Sbi.error("[Widget.getWidgetManager]: Widget [" + this.toString() + "] is bound to a widget container but it is not possible to retrive from it a valid widget manager");
    		}
    	} else {
    		Sbi.warn("[Widget.getWidgetManager]: It's not possble to retrieve widget manager of widget [" + this.toString() + "] because it is not bound to any widget container");
    	}

    	Sbi.trace("[WidgetRuntime.getWidgetManager]: OUT");

    	return widgetManager;
    }

    /**
	 * @method
	 *
	 * Returns all filters linked to the widget.
	 *
	 * @return {List} A list with all widgets' filters.
	 */
    , getAllFilters: function() {
    	Sbi.trace("[WidgetRuntime.getAllFilters]: IN");

    	if(this.isBoundToAContainer() === true) {
    		this.filters = this.getParentContainer().getAllWidgetFilters();
    		if(this.filters === null) {
    			Sbi.error("[Widget.getAllFilters]: Filters of [" + this.toString() + "] are loaded ");
    		}
    	} else {
    		Sbi.warn("[Widget.getAllFilters]: It's not possble to retrieve widget manager of widget [" + this.toString() + "] because it is not bound to any widget container");
    	}

    	Sbi.trace("[WidgetRuntime.getAllFilters]: OUT");

    	return this.filters;
    }

    /**
	 * @method
	 *
	 * @param {Object} the filter object reference
	 *
	 * Returns the value of the filter
	 *
	 * @return {String} A string with the value of the filter.
	 */
    , getFilterValue: function(f){
    	Sbi.trace("[WidgetRuntime.getFilterValue]: IN");
    	return null;
    	Sbi.trace("[WidgetRuntime.getFilterValue]: OUT");
    }

    /**
	 * @method
	 *
	 * Sets the value of the filter
	 *
	 * @param {Object} the filter object reference
	 * @param {String} the new value for the filter
	 *
	 * @return {String} A string with the value of the filter.
	 */
    , setFilterValue: function(f, v){
    	Sbi.trace("[WidgetRuntime.setFilterValue]: IN");
    	return;
    	Sbi.trace("[WidgetRuntime.setFilterValue]: OUT");
    }


    , showLoadingMask: function() {
    	Sbi.trace("[WidgetRuntime.showLoadingMask][" + this.getId() + "]: IN");
    	if(this.rendered === true) {
			Sbi.trace("[WidgetRuntime.showLoadingMask][" + this.getId() + "]: " +
					"widget is rendered so need to be masked");
			if(this.loadingMask === null) {
				Sbi.trace("[WidgetRuntime.showLoadingMask][" + this.getId() + "]: " +
				"initializing mask...");
				this.loadingMask = new Ext.LoadMask(this, {msg:"Please wait..."});
				Sbi.trace("[WidgetRuntime.showLoadingMask][" + this.getId() + "]: " +
				"mask succesfully initialized");
			}
			this.loadingMask.show();
		} else{
			Sbi.trace("[WidgetRuntime.showLoadingMask][" + this.getId() + "]: no need to mask because the widget is not rendered yet");
		}
    	Sbi.trace("[WidgetRuntime.showLoadingMask]: OUT");
    }

    , hideLoadingMask: function() {
    	Sbi.trace("[WidgetRuntime.hideLoadingMask][" + this.getId() + "]: IN");
		if(this.loadingMask !== null) {
			this.loadingMask.hide();
		}
		Sbi.trace("[WidgetRuntime.hideLoadingMask]: OUT");
    }

	, toString: function() {
		return this.id;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	, onRender: function(ct, position) {
		Sbi.cockpit.core.WidgetRuntime.superclass.onRender.call(this, ct, position);
	}

	, onStoreBeforeLoad: function(store, operation, eOpts) {
		Sbi.trace("[WidgetRuntime.onStoreBeforeLoad][" + this.getId() + "]: IN");
		this.showLoadingMask();
		Sbi.trace("[WidgetRuntime.onStoreBeforeLoad]: OUT");
	}

	, onStoreLoad: function(store) {
		Sbi.trace("[WidgetRuntime.onStoreLoad][" + this.getId() + "]: IN");
		this.hideLoadingMask();
		Sbi.trace("[WidgetRuntime.onStoreLoad]: OUT");
	}

	, onStoreBeforeAssociation: function(store, operation, eOpts) {
		Sbi.trace("[WidgetRuntime.onStoreBeforeAssociation][" + this.getId() + "]: IN");
		this.showLoadingMask();
		Sbi.trace("[WidgetRuntime.onStoreBeforeAssociation]: OUT");
	}

	, onStoreAssociation: function(store, operation, eOpts) {
		Sbi.trace("[WidgetRuntime.onStoreAssociation][" + this.getId() + "]: IN");
		this.hideLoadingMask();
		Sbi.trace("[WidgetRuntime.onStoreAssociation]: OUT");
	}

	,
	areIncomingEventsEnabled: function () {
		Sbi.trace("[WidgetRuntime.areIncomingEventsEnabled]: IN");
		var config = this.getConfiguration();
		var incomingeventsenabled = config.wgeneric.incomingeventsenabled !== undefined ? config.wgeneric.incomingeventsenabled : true;
		Sbi.trace("[WidgetRuntime.areIncomingEventsEnabled]: OUT, returning : " + incomingeventsenabled);
		return incomingeventsenabled;
	}

	,
	areOutcomingEventsEnabled: function () {
		Sbi.trace("[WidgetRuntime.areOutcomingEventsEnabled]: IN");
		var config = this.getConfiguration();
		var outcomingeventsenabled = config.wgeneric.outcomingeventsenabled !== undefined ? config.wgeneric.outcomingeventsenabled : true;
		Sbi.trace("[WidgetRuntime.areOutcomingEventsEnabled]: OUT, returning : " + outcomingeventsenabled);
		return outcomingeventsenabled;
	}



	, onDataChanged: function(store, eOpts) {
		// do nothing
	}

	, onStoreMetaChange: function(store, meta) {
		// if in meta is put last cache date put it in wgeneric to be included in title
		if(meta.cacheDate != undefined){
			//this.wgeneric.dateTitle = meta.cacheDate;
			this.getParentComponent().cacheDate = meta.cacheDate;
			this.getParentComponent().refreshTitle();
		}
	}

	/**
	 * @method
	 *
	 * Has to be overridden from AbstractChartWidget
	 */
	, maximize: function() {
		Sbi.trace("[WidgetRuntime.maximize]: Ext.window.Window.maximize method overriden has been called");
	}

	/**
	 * @method
	 *
	 * Has to be overridden from AbstractChartWidget
	 */
	, restore: function() {
		Sbi.trace("[WidgetRuntime.restore]: Ext.window.Window.restore method overriden has been called");
	}

	/**
	 * @method
	 *
	 * Has to be overridden from AbstractChartWidget
	 */
	, resize: function() {
		Sbi.trace("[WidgetRuntime.resize]: Ext.window.Window.resize method overriden has been called");
	}

	/**
	 * @method
	 *
	 * Evalute configuration change (wconf).
	 * If no change is found return false
	 */
	, isRefreshable: function(config) {
		Sbi.trace("[WidgetRuntime.isRefreshable]: IN");

		var wconfNew = Ext.JSON.encodeValue(config.wconf);

		var currentConf = this.getConfiguration();

		if (wconfNew == Ext.JSON.encodeValue(currentConf.wconf)) return false;

		return true;

		Sbi.trace("[WidgetRuntime.isRefreshable]: OUT");
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...


});