/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.core");

/**
 * @class Sbi.cockpit.core.WidgetManager
 * @extends Ext.util.Observable
 *
 *  It handles:
 *  - widgets lifecycle management: register, unregister, lookup
 *  - shared resources: through env
 *  - intra-widgets comunications: sendMessage (asyncronous: point to point or broadcast)
 */

/**
 * @cfg {Object} config
 * ...
 */
Sbi.cockpit.core.WidgetManager = function(config) {

	// init properties...
	var defaultSettings = {
		// set default values here
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.core', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.init();

	this.addEvents('selectionChange');

	// constructor
	Sbi.cockpit.core.WidgetManager.superclass.constructor.call(this, c);
};


Ext.extend(Sbi.cockpit.core.WidgetManager, Ext.util.Observable, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Ext.util.MixedCollection} widgets
     * The collection of widgets managed by this container
     */
    widgets: null

    /**
     * @property {Object} env
     * This container environment
     * WARNINGS: not used at the moment
     */
    , env: null

    /**
     * @property {Object} selections
     * The object containing current selection state. Selected values are indexed first by widget (widgetSelections) then by
     * field (fieldSelections) as shown in the following example:
     *
     *	{
     * 		ext-comp-2014 : {
	 *			STATE: {values:['CA','WA']}
	 *			, FAMILY:  {values:['Food', 'Drink']}
	 *		}
	 *		ext-comp-1031 : {
	 *			MEDIA: {values:['TV']}
	 *		  	, CUSTOMER:  {values:['79','99']}
	 *		}
	 *	}
     */
    , selections: null

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // widgets management methods
	// -----------------------------------------------------------------------------------------------------------------

    /**
     * @method
     *
     * Registers a widget to this widget manager.
     *
     * @param {Sbi.cockpit.core.WidgetRuntime} The widget.
     */
    , register: function(w) {
    	w.on('selection', this.onSelection, this);
    	this.widgets.add(w);
    	Sbi.info("[WidgetManager.register]: widget [" + this.widgets.getKey(w) + "] succesfully registered. Now there are [" + this.widgets.getCount()+ "] registered widget(s)");
	}

    /**
     * @method
     *
     * Unregisters a widget from this widget manager.
     *
     * @param {Sbi.cockpit.core.WidgetRuntime} The widget.
     */
	, unregister: function(w) {
		Sbi.trace("[WidgetManager.unregister]: IN");
		if(this.widgets.contains(w)) {
			var widgetId = this.widgets.getKey(w);
			var store = w.getStore();
			var storeId = Sbi.storeManager.getStoreId(store);
			var storeAggregation = Sbi.storeManager.getAggregationOnStore(store);

			if(Sbi.isValorized(store)) {
				w.unboundStore();
				Sbi.trace("[WidgetManager.unregister]: store [" + storeId + "] succesfully unbounded from widget [" + widgetId + "]");
			} else {
				Sbi.trace("[WidgetManager.unregister]: the widget have no store bounded to unbound");
			}


			Sbi.trace("[WidgetManager.unregister]: unregistering widget [" + this.widgets.getKey(w) + "]. " +
					"Before deletion there are [" + this.widgets.getCount()+ "] registered widget(s)");
			this.widgets.remove(w);
			Sbi.trace("[WidgetManager.unregister]: widget [" + this.widgets.getKey(w) + "] succesfully unregistered. " +
					"Now there are [" + this.widgets.getCount()+ "] registered widget(s)");

			if(Sbi.isValorized(store)) {
				if( this.isStoreUsed(storeId, storeAggregation) == false) {
					Sbi.storeManager.removeStore(store, true );
					Sbi.info("[WidgetManager.unregister]: store [" + storeId + "] succesfully removed");
				} else {
					Sbi.info("[WidgetManager.unregister]: store [" + storeId + "] not removed because there are other widgets using it");;
				}
			} else {
				Sbi.trace("[WidgetManager.unregister]: the widget have no store bounded to remove from store manager");
			}
		} else {
			Sbi.warn("[WidgetManager.unregister]: widget [" + this.widgets.getKey(w) + "] is not registered in this manager.");
		}
		Sbi.trace("[WidgetManager.unregister]: OUT");
	}

	/**
	 * @method
	 *
	 * Gets the specified registered widget
	 *
	 * @param {Sbi.cockpit.core.WidgetRuntime/String} The widget or its id.
	 */
	, getWidget: function(w) {
		if(!Ext.isString(w)) {
			w = this.widgets.getKey(w);
		}
		return this.widgets.get(w);
	}

	/**
	 * @methods
	 *
	 * Returns all the registered widgets.
	 *
	 * @return {Sbi.cockpit.core.WidgetRuntime[]} the list of registered widgets.
	 */
	, getWidgets: function() {
		return this.widgets.getRange();
	}

	, getWidgetCount: function() {
		return this.widgets.getCount();
	}

	 /**
	  * @method
	  *
     * Executes the specified function once for every registered widget, passing the following arguments:
     * <div class="mdetail-params"><ul>
     * <li><b>item</b> : Sbi.cockpit.core.WidgetRuntime<p class="sub-desc">The widget</p></li>
     * <li><b>index</b> : Number<p class="sub-desc">The widget's index</p></li>
     * <li><b>length</b> : Number<p class="sub-desc">The total number of widgets in the collection</p></li>
     * </ul></div>
     * The function should return a boolean value. Returning false from the function will stop the iteration.
     * @param {Function} fn The function to execute for each widget.
     * @param {Object} scope (optional) The scope (<code>this</code> reference) in which the function is executed. Defaults to the current widget in the iteration.
     */
	, forEachWidget: function(fn, scope) {
		this.widgets.each(fn, scope);
	}

	/**
	 * @method
	 *
	 * Returns a list of widgets that are feed by the specified store.
	 *
	 * @param {String} storeId The id of the store.
	 * @param {String} aggregations It's optional. If valorized the retuned widgets are the ones defined on the store at the
	 * specified aggregation level
	 *
	 * @return {Sbi.cockpit.core.WidgetRuntime[]} The list of widgets.
	 */
	, getWidgetsByStore: function(storeId, aggregations){
		Sbi.trace("[WidgetManager.getWidgetsByStore]: IN");

		if (Sbi.isNotValorized(storeId)){
			alert("[WidgetManager.getWidgetsByStore]: input parameter [storeId] must valorized");
			return;
		}
		if(!Ext.isString(storeId)) {
			alert("[WidgetManager.getWidgetsByStore]: input parameter [storeId] must be of type String");
			return;
		}

		var toReturn = new Ext.util.MixedCollection();

		if(aggregations === undefined) {
			Sbi.trace("[WidgetManager.getWidgetsByStore]: aggregation on store not defined");
			for(var i=0; i < this.widgets.getCount(); i++) {
				var w = this.widgets.get(i);
				if (Sbi.isValorized(w.getStoreId()) && w.getStoreId() == storeId){
					toReturn.add(w);
				}
			}
		} else {
			Sbi.trace("[WidgetManager.getWidgetsByStore]: aggregation on store are equal to [" + aggregations + "]");
			for(var i=0; i < this.widgets.getCount(); i++){
				var w = this.widgets.get(i);
				Sbi.trace("[WidgetManager.getWidgetsByStore]: processing widget [" + i + "][" + this.widgets.getKey(w) + "] ...");

				if ( Sbi.isValorized(w.getStoreId()) ){ // can be null for some widgets like the selection widgte
					var s = w.getStore();
					var a = Sbi.storeManager.getAggregationOnStore(s);
					if( w.getStoreId() === storeId && Sbi.storeManager.isSameAggregationLevel(a, aggregations) ) {
						toReturn.add(w);
					}
				}
			}
		}


		Sbi.trace("[WidgetManager.getWidgetsByStore]: store [" + storeId + "] is used " +
				"by [" + toReturn.getCount()  + "] widget(s)");
		Sbi.trace("[WidgetManager.getWidgetsByStore]: OUT");

		return toReturn;
	}

	/**
	 * @method
	 *
	 * Returns true if the store is used at least by one widget managed by this manager,
	 * false otherwise
	 *
	 * @param {String} storeId The id of the store.
	 *
	 * @return {boolean} true if the store is used at least by one widget managed by this manager,
	 * false otherwise.
	 */
	, isStoreUsed: function(storeId, aggregations) {
		Sbi.trace("[WidgetManager.isStoreUsed]: IN");
		var widgets = this.getWidgetsByStore(storeId, aggregations);
		var isUsed = (Sbi.isValorized(widgets)  && widgets.getCount() > 0);
		Sbi.trace("[WidgetManager.isStoreUsed]: Store [" + storeId + "] is used [" + isUsed+ "]");
		Sbi.trace("[WidgetManager.isStoreUsed]: OUT");
		return isUsed;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // selection management methods
	// -----------------------------------------------------------------------------------------------------------------

	// -- selection ----
	 /**
	 * @method
	 *
	 * @returns current #selections
	 *
	 */
	, getSelections: function() {
		return this.selections;
	}

	/**
	 * @method
	 *
	 * set the current #selections
	 *
	 */
	, setSelections: function(selections) {
		this.selections = selections;
	}

	/**
	 * @method
	 *
	 * clear current #selections
	 *
	 */
	, clearSelections: function() {
		Sbi.trace("[WidgetManager.clearSelections]: IN");

		Sbi.trace("[WidgetManager.clearSelections]: selections is equal to [" + Sbi.toSource(this.selections) + "]");

		this.selections = {};

		this.fireEvent('selectionChange');
		Sbi.storeManager.loadAllStores();

		Sbi.trace("[WidgetManager.clearSelections]: OUT");
	}

	/**
	 * @method
	 *
	 * add the passed in selections to the current #selections
	 */
    , addSelections: function(selections){
    	Sbi.trace("[WidgetManager.addSelections]: IN");

    	for (var widgetId in selections){
    		this.addWidgetSelections(widgetId, selections[widgetId]);
    	}
    	Sbi.trace("[WidgetManager.addSelections]: OUT");
    }

    // -- widget selections ----

    , clearSingleSelection: function(grid, rowIndex, colIndex){
        Sbi.trace("[WidgetManager.clearSingleSelection]: IN");

        grid.getStore().removeAt(rowIndex);

        Sbi.trace("[WidgetManager.clearSingleSelection]: OUT");
    }

    /**
	 * @method
	 *
	 * Returns the field selections over the specified widget
	 *
	 * @param {String} widgetId The widget id
	 *
	 * @return {Object} the field selections encoded using an object as the one show in the following
	 *  example:
	 *  	{
	 *			STATE: {values:['CA','WA']}
	 *			, FAMILY:  {values:['Food', 'Drink']}
	 *		}
	 *  If no selections are specified over the input widget an empty object is returned.
	 */
    , getWidgetSelections: function(widgetId) {
    	return this.selections[widgetId] || {};
    }

	/**
	 * @method
	 *
	 * Set the field selections over the specified widget
	 *
	 *  @param {String} widgetId The id of the widget
	 *  @param {Object} selections the widget selections encoded using an object as the one show in the following
	 *  example:
	 *  	{
	 *			STATE: {values:['CA','WA']}
	 *			, FAMILY:  {values:['Food', 'Drink']}
	 *		}
	 *
	 */
    , setWidgetSelections: function(widgetId, selections) {
    	this.selections[widgetId] = selections;
    }

    /**
	 * @method
	 *
	 * Clear selection of the specified widget
	 *
	 * @param {String} widgetId The widget id
	 *
	 */
    , clearWidgetSelections: function(widgetId){
    	Sbi.trace("[WidgetManager.clearWidgetSelections]: IN");
    	var widgetSelections = this.getWidgetSelections(widgetId);
    	if (Sbi.isValorized(widgetSelections) && Sbi.isNotEmptyObject(widgetSelections)){
			delete this.selections[widgetId];
			Sbi.debug("[WidgetManager.clearWidgetSelections]: selections specified over widget [" + widgetId + "] have been succesfully cleared");
    	} else {
    		Sbi.debug("[WidgetManager.clearWidgetSelections]: no selections specified over widget [" + widgetId + "]");
    	}
    	Sbi.trace("[WidgetManager.clearWidgetSelections]: IN");
    }

    , addWidgetSelections: function(widgetId, selections){
    	Sbi.trace("[WidgetManager.addWidgetSelections]: IN");
    	for (var fieldHeader in selections){
    		this.addFieldSelections(widgetId, fieldHeader, selections[fieldHeader].values);
    	}
    	Sbi.trace("[WidgetManager.addWidgetSelections]: OUT");
    }

    // -- field selections ----

    /**
     * @method
     *
     * @return {Object} the field selections encoded using an object as the one show in the following
	 *  example:
	 *  	FAMILY:  {values:['Food', 'Drink']}
	 *  If no selections are specified over the input filed an empty object is returned.
     */
    , getFieldSelections: function(widgetId, fieldHeader) {
    	return this.getWidgetSelections(widgetId)[fieldHeader] || {};
    }

    /**
     * @method
     */
    , setFieldSelections: function(widgetId, fieldHeader, selections) {
    	this.selections[widgetId] = this.selections[widgetId] || {};
    	this.selections[widgetId][fieldHeader] = selections;
    }

    , clearFieldSelections: function(widgetId, fieldHeader) {
    	this.selections[widgetId] = this.selections[widgetId] || {};
    	this.selections[widgetId][fieldHeader] = {values: []};
    	this.onSelection(this.getWidget(widgetId), this.selections[widgetId]);
    }

    , addFieldSelections: function(widgetId, fieldHeader, valuesToAdd) {
		var currentSelectedValues = this.getFieldSelectedValues(widgetId, fieldHeader);
		var values = Ext.Array.union(currentSelectedValues, valuesToAdd);
    	this.setFieldSelectedValues(widgetId, fieldHeader, values);
	}

    // -- value selections ----
    /**
     * @method
     *
     * @return {Array} the selected values over the specified field of the specified widget. An empty array
     * if no values are selected.
     */
    , getFieldSelectedValues: function(widgetId, fieldHeader) {
    	return this.getFieldSelections(widgetId, fieldHeader).values || [];
    }

    /**
     * @method
     */
    , setFieldSelectedValues: function(widgetId, fieldHeader, values) {
    	this.setFieldSelections(widgetId, fieldHeader, {values: values});
    }

    // -- store field selections ----

    // a store filed selections contain all the values selected for the specific field in all the widgets that use the
    // store that contains the field itself

    /**
	 * @method
	 */
	, getStoreFieldSelectedValues: function(storeId, fieldHeader) {
		Sbi.trace("[WidgetManager.getSelectionsOnField]: IN");

		if(Sbi.isNotValorized(storeId)) {
			Sbi.error("[WidgetManager.getStoreFieldSelectedValues]: Input parametr [storeId] must be valorized");
			return;
		}
		if(!Ext.isString(storeId)) {
			Sbi.error("[WidgetManager.getStoreFieldSelectedValues]: Input parametr [storeId] must be of type String");
			return;
		}

		var selectedValues = {};
		var widgets = this.getWidgetsByStore(storeId);

		Sbi.trace("[WidgetManager.getSelectionsOnField]: There are [" + widgets.getCount() + "] widget(s) associated to store [" + storeId + "]");
		for(var i = 0; i < widgets.getCount(); i++) {
			var widget = widgets.get(i);
			var values = this.getFieldSelectedValues(widget.getId(), fieldHeader);
			for(var j = 0; j < values.length; j++) {
				selectedValues[values[j]] = values[j];
				Sbi.trace("[SelectionsPanel.getSelectionsOnField]: Added value [" + values[j] + "] to selection on field [" + fieldHeader + "]");
			}
		}

		Sbi.trace("[WidgetManager.getSelectionsOnField]: OUT");

		return selectedValues;
	}

	// -- selection by association ----
	, clearAssociationSelections: function(associationName) {
		Sbi.trace("[WidgetManager.clearAssociationSelections]: IN");
		var association = Sbi.storeManager.getAssociation(associationName);
		for(var i = 0; i < association.fields.length; i++) {
			var storeId = association.fields[i].store;
			var fieldHeader = association.fields[i].column;
			var widgets = this.getWidgetsByStore(storeId);
			for(var j = 0; j < widgets.getCount(); j++) {
				var widgetId = widgets.get(j).getId();
				this.selections[widgetId] = this.selections[widgetId] || {};
		    	this.selections[widgetId][fieldHeader] = {values: []};
			}
		}
		//this.onDeselectionOnAssociation();

		var associationGroup = Sbi.storeManager.getAssociationGroupByAssociationId( association.id );
    	if(Sbi.isValorized(associationGroup)) {
    		this.applySelectionsOnAssociationGroup( associationGroup );
        	this.fireEvent('selectionChange');
    	} else {
    		alert("WidgetManager.clearAssociationSelections: Impossible to find association group that contains association [" + association.id + "]");
    	}
    	Sbi.trace("[WidgetManager.clearAssociationSelections]: IN");
	}

	// -- selections by store ----
	, getSelectionsByStore: function(store) {
		Sbi.trace("[WidgetManager.getSelectionsByStore]: IN");
		var selectedValues = {};
		var fields = Sbi.storeManager.getStoreFields(store);
		Sbi.trace("[WidgetManager.getSelectionsByStore]: store [" + store.storeId + "] has [" + fields.length + "] fields: [" + fields + "]");
		for(var i = 0; i < fields.length; i++) {
			
			//Added control on field header if the widget type is "table". 
			//This is mandatory after alias/column name change
			
			var widgets = this.getWidgetsByStore(store.storeId);

			for(var j = 0; j < widgets.getCount(); j++) {
				var widget = widgets.get(j);
			
				if(Sbi.isValorized(widget.wtype) && widget.wtype == 'table'){
					if((Sbi.isValorized(widget.wconf)) && 
						(Sbi.isValorized(widget.wconf.visibleselectfields))){
							for(var k = 0; k < widget.wconf.visibleselectfields.length; k++){
								if(fields[i].header === widget.wconf.visibleselectfields[k].alias){
									fields[i].header = widget.wconf.visibleselectfields[k].columnName;
									break;
								}
							}
					}
				}
			}
			
			var values = this.getStoreFieldSelectedValues(store.storeId, fields[i].header);
			var fieldSelectedValues = [];
			for(var value in values) { fieldSelectedValues.push(value); }
			Sbi.trace("[WidgetManager.getSelectionsByStore]: selected value for field [" + fields[i].header + "] are [" + Sbi.toSource(fieldSelectedValues)+ "]");
			selectedValues[fields[i].header] = fieldSelectedValues;
		}
		Sbi.trace("[WidgetManager.getSelectionsByStore]: Selected values on store [" + store.storeId + "] are equal to [" + Sbi.toSource(selectedValues)+ "]");
		Sbi.trace("[WidgetManager.getSelectionsByStore]: OUT");
		return selectedValues;
	}

	, getSelectionsByStores: function() {
		var selections =  {};
		Sbi.trace("[WidgetManager.getSelectionsByStores]: IN");
		var stores = Sbi.storeManager.getStores();
		Sbi.trace("[WidgetManager.getSelectionsByStores]: Number of stores is equal to [" + stores.length + "]");
		for(var i = 0; i < stores.length; i++) {
			selections[stores[i].storeId] = selections[stores[i].storeId] || {};
			Ext.apply(selections[stores[i].storeId], this.getSelectionsByStore(stores[i]));
		}
		Sbi.trace("[WidgetManager.getSelectionsByStore]: Selected values by stores are equal to [" + Sbi.toSource(selections)+ "]");
		Sbi.trace("[WidgetManager.getSelectionsByStores]: OUT");
		return selections;
	}

	// -- selections by associations ----

	/**
	 * @returns the selections grouped by associations like in the following example:
	 *
	 * 	{
	 * 		cityAssociation: ['Milan', 'Turin']
	 * 		, customerAssociation: ['Andrea', 'Sofia', 'Lucio']
	 * 	}
	 */
	, getSelectionsByAssociations: function() {

		Sbi.trace("[SelectionsPanel.getSelectionsByAssociations]: IN");

		var selectionsByAssociations = {};

		var selections = this.getSelections();

		var associations = Sbi.storeManager.getAssociationConfigurations();
		for(var i = 0; i <  associations.length; i++){
			var selectedValues = {};
			var fields = associations[i].fields;
			for(var j = 0; j <  fields.length; j++){
				var field = fields[j];
				var values = this.getStoreFieldSelectedValues(field.store, field.column);
				Ext.apply(selectedValues, values);
			}
			var results = [];
			for(var value in selectedValues) { results.push(value); }

			if(results.length > 0) {
				selectionsByAssociations[associations[i].id] = results;
			}
		}

		Sbi.trace("[SelectionsPanel.getSelectionsByAssociations]: OUT");

		return selectionsByAssociations;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

    , onWidgetAdd: function(index, widget, key) {
    	//widget.setParentContainer(this);
    }

    , onWidgetRemove: function(widget, key) {
    	//widget.setParentContainer(null);
    }

    , onDeselectionOnAssociation: function(associationId){
    	Sbi.trace("[WidgetManager.onDeselectionOnAssociation]: IN");
    	var associationGroup = Sbi.storeManager.getAssociationGroupByAssociationId( associationId );
    	if(Sbi.isValorized(associationGroup)) {
//    		var selections = this.getSelectionsByAssociations();
//        	Sbi.storeManager.loadStoresByAssociations(associationGroup,  selections);
    		this.applySelectionsOnAssociationGroup(associationId);
        	this.fireEvent('selectionChange');
    	} else {
    		alert("WidgetManager.onDeselectionOnAssociation: ERROR");
    	}
    	Sbi.trace("[WidgetManager.onDeselectionOnAssociation]: OUT");
    }

    , applySelectionsOnAssociationGroup: function(associationGroup) {
    	Sbi.trace("[WidgetManager.applySelectionsOnAssociationGroup]: IN");

    	if(Sbi.isNotValorized(associationGroup)) {
    		Sbi.warn("[WidgetManager.applySelectionsOnAssociationGroup]: Input parameter [associationGroup] is undefined");
    		return;
    	}
    	var selections = this.getSelectionsByAssociations();

    	for(var widgetId in this.selections)  {
    		var selectionsOnWidget = this.selections[widgetId];
    		var widget = this.getWidget(widgetId);
			Sbi.trace("[WidgetManager.applySelectionsOnAssociationGroup]: widget [" + widgetId +"] allow selection on field [" + widget.fieldsSelectionEnabled+ "]");
			if(widget && widget.fieldsSelectionEnabled === true) {
				for(var fieldHeader in selectionsOnWidget) {
	    			if(Sbi.isNotValorized(selections[fieldHeader]) && selectionsOnWidget[fieldHeader].values && selectionsOnWidget[fieldHeader].values.length > 0){
	    				selections[widget.getStore().storeId  + "." + fieldHeader] = selectionsOnWidget[fieldHeader].values;
	    			}
	    		}
			}

    	}

    	//alert("[WidgetManager.applySelectionsOnAssociationGroup]: " + Sbi.toSource(selections));


    	Sbi.storeManager.loadStoresByAssociations( associationGroup,  selections);
    	Sbi.trace("[WidgetManager.applySelectionsOnAssociationGroup]: OUT");
    }

    , applySelectionsOnAggregation: function(store) {
    	Sbi.trace("[WidgetManager.applySelectionsOnAggregation]: IN");
    	var selections = this.getSelectionsByStores();
		Sbi.storeManager.loadStoresByAggregations( store.storeId,  selections);
    	Sbi.trace("[WidgetManager.applySelectionsOnAggregation]: OUT");
    }

    , onSelection: function(widget, selectionsOnWidget){
    	Sbi.trace("[WidgetManager.onSelection]: IN");

    	this.setWidgetSelections(widget.getId(), selectionsOnWidget);

    	var associationGroup = Sbi.storeManager.getAssociationGroupByStore( widget.getStore() );

    	if(Sbi.isValorized(associationGroup)) {
    		this.applySelectionsOnAssociationGroup(associationGroup);
    	} else {
    		this.applySelectionsOnAggregation(widget.getStore());
    	}

    	this.fireEvent('selectionChange');

    	Sbi.trace("[WidgetManager.onSelection]: OUT");
    }



    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

    , init: function() {
    	this.widgets = new Ext.util.MixedCollection();
    	this.widgets.getKey = function(o){
	        return o.getId();
	    };
    	this.selections = {};
    	this.widgets.on('add', this.onWidgetAdd, this);
    	this.widgets.on('remove', this.onWidgetRemove, this);
	}

    // =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});