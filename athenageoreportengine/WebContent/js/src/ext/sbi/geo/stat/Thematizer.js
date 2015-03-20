/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/

Ext.ns("Sbi.geo.stat");


/**
 * Class: Sbi.geo.stat.Thematizer
 * Base class for geo-statistics. This class is not meant to be used directly, it serves
 * as the base for specific geo-statistics implementations.
 */
Sbi.geo.stat.Thematizer = function(map, config) {
	
	Sbi.trace("[Thematizer.constructor] : IN");
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	this.initialize(map, config);
	
	this.addEvents(
		/**
		* @event indicatorsChanged
		* Fires when ...
		* @param {Thematizer} this
		*/
		'indicatorsChanged'
		/**
		* @event filtersChanged
		* Fires when ...
		* @param {Thematizer} this
		*/
		, 'filtersChanged'
		/**
		* @event layerloaded
		* Fires when a new target layer has been successfully loaded
		* @param {Thematizer} this
		* @param {OpenLayers.Layer.Vector} the layer loaded
		*/
		, 'layerloaded'
	);


	Sbi.geo.stat.Thematizer.superclass.constructor.call(this, config);
	Sbi.trace("[Thematizer.constructor] : OUT");
};

/**
 * @class Sbi.geo.stat.Thematizer
 * @extends Ext.util.Observable
 * 
 * Base class for geo-statistics. This class is not meant to be used directly, it serves
 * as the base for specific geo-statistics implementations.
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.geo.stat.Thematizer, Ext.util.Observable, {
	
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	
	/**
	 * @property {String} indicator
	 * Defines the name of indicator used to create the thematization
	 */
    indicator: null
    
    /**
     * @property {String} indicatorContainer
     * Defines the object that contains the values of the specified indicator. It can be equal to:
     *  - 'store' if the values are taken from a store. In this case the value are taken directly from the
     * dataset's column whose name is equal to the #indicator;
     *  - 'layer' if the values are taken from the features contained in the target layer. In this case the 
     *  values are taken directly from the feature's property whose name is equal to the #indicator;
     *  
     * By default it is equal to 'layer'
     */
    , indicatorContainer: 'store' //'layer' - 'store'
    	
	/**
     * @property {OpenLayers.Layer.Vector} layer
     * The vector layer containing the features that are styled based on statistical values. 
     * If none is provided, one empty layer will be created. The layer usually is passed in empty
     * by the caller and shared with other thematizers (all the thematizers share the same empty layer).
     * When the thematizer get activated it delete the feature stored in the target layer, load a new features
     * set from the server and store them into the target layer. When the thematizer get deactivated it save the 
     * features contained in the target layer in order to restore them later when it will be 
     * reactivate again without having to download them from the server
     */
    , layer: null
    
    , cachedFeatures: null
    
    /**
	 * @property {String} layerName
	 * Defines the name of the layer loaded
	 */
    , layerName: null 
    
	/**
	 * @property {String} layerId
	 * Defines the name of the property of the feature that contains its the unique identifier
	 */
    , layerId: null 
    
    /**
     * @property {Ext.data.Store} store
     * The store containing the values of the indicator used to thematize the map. Only apply if 
     * #indicatorContainer is equal to 'store'
     */
    , store: null
    
    /**
	 * @property {String} storeId
	 * Defines the name of the field of the record that contains its the unique identifier. Only apply if 
     * #indicatorContainer is equal to 'store'
	 */
    , storeId: null //'COMUNE_ITA'
    
    /**
     * @property {Boolean} storeReload
     * Define if the store must be reloaded at the end of thematizer activation or not. 
     * Defalut to true. Only apply if #indicatorContainer is equal to 'store'
     */
    , storeReload: true
    
    /**
     * @property {Array} storeFilters
     * The filters applied to the store. Each filter in the array it's an object composed by
     * two properties: field and value.
     */
    , storeFilters: null
    
    /**
     * @property {OpenLayers.Format} format
     * The OpenLayers format used to get features from
     * the HTTP request response. GeoJSON is used if none is provided.
     */
    , format: null

    /**
	 * The service name to call in order to load target layer. If none is provided, the features
     * found in the provided vector layer will be used.
	 */
    , loadLayerServiceName: null
    
    /**
     * true to filter layer on load taking only feature whose id match with one of the disticnt values
     * contained in the storeId column of the dataset.
     */
    , filterLayerOnLoad: true

    /**
     * @property {Function} requestSuccess
     * Function called upon success with the HTTP request.
     */
    //, requestSuccess: function(request) {}

   
	/**
	 * @property {Function} requestFailure
	 * Function called upon failure with the HTTP request.
	 */
    //, requestFailure: function(request) {}

    /**
	 * @property {Boolean} featureSelection
	 * A boolean value specifying whether feature selection must
     * be put in place. If true a popup will be displayed when the
     * mouse goes over a feature.
	 */
    , featureSelection: true
    
    /**
	 * @property {String} nameAttribute
	 * The feature attribute that will be used as the popup title.
     * Only applies if featureSelection is true.
	 */
    , nameAttribute: null

    , indicators: null
    
    , filters: null
    
    /**
	 * @property {Object} defaultSymbolizer
	 * This symbolizer is used in the constructor to define
     * the default style in the style object associated with the
     * "default" render intent. This symbolizer is extended with
     * OpenLayers.Feature.Vector.style['default']. It can be
     * overridden in subclasses.
	 */
    , defaultSymbolizer: {}
    
    /**
	 * @property {Object} selectSymbolizer
	 * This symbolizer is used in the constructor to define
     * the select style in the style object associated with the
     * "select" render intent. When rendering selected features
     * it is extended with the default symbolizer. It can be
     * overridden in subclasses.
	 */
    , selectSymbolizer: {'strokeColor': '#000000'} // neutral stroke color

    
    /**
	 * @property {String} elementToMask
	 * The element o mask when masking is required
	 */
    , elementToMask: null
    , mask: null
    , active: false
    
    /**
	 * @property {String} legendDiv
	 * Reference to the DOM container for the legend to be generated.
	 */
    , legendDiv: null

    // =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	/**
	 * @method 
	 * 
	 * Controls that the configuration object passed in to the class constructor contains all the compulsory properties. 
	 * If it is not the case an exception is thrown. Use it when there are properties necessary for the object
	 * construction for whom is not possible to find out a valid default value.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the config object received as input
	 */
	, validateConfigObject: function(config) {
		
		Sbi.trace("[Thematizer.validateConfigObject] : IN");
		
		if(!config) {
			throw "Impossible to build thematizer. Config object is undefined";
		}
		
		
		
		if(config.indicatorContainer == 'store') {
			
			if(!config.storeType) {
				throw "Impossible to build thematizer. Configuration property [indicatorContainer] is equal to [store] but property [storeType] is undefined ";
			}
			
			if(config.storeType === "physicalStore") {
				if(!config.store) {
					throw "Impossible to build thematizer. Configuration property [indicatorContainer] is equal to [store] " +
							"and configuration property [storeType] is equal to [physicalStore] but property [store] is undefined ";
				}
				
				if(!config.storeId) {
					throw "Impossible to build thematizer. Configuration property [indicatorContainer] is equal to [store] " +
					"and configuration property [storeType] is equal to [physicalStore] but property [storeId] is undefined ";
				}
			} else if(config.storeType === "virtualStore"){
				if(!config.storeConfig) {
					config.storeConfig = {};
					Sbi.warn("Configuration property [indicatorContainer] is equal to [store] " +
					"and configuration property [storeType] is equal to [visrtualStore] but property [storeConfig] is undefined. " +
					"The thematizer will be initialized but the thematization cannot be performed until the virtual store's config wont be set properly");
				}
			} else {
				throw "Value [" + config.storeType + "] is not valid for property [storeType]";
			}		
		}
		
		if(config.storeType != "virtualStore" && !config.layerId) {
			throw "Impossible to build thematizer. Config property [layerId] must be defined if store type is not equal to [virtualStore]";
		}
		
		Sbi.trace("[Thematizer.validateConfigObject] : Config object passed to constructor has been succesfully validated");
		
		Sbi.trace("[Thematizer.validateConfigObject] : OUT");
	}

	/**
	 * @method 
	 * 
	 * Modify the configuration object passed in to the class constructor adding/removing properties. Use it for example to 
	 * rename a property or to filter out not necessary properties.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the modified version config object received as input
	 * 
	 */
	, adjustConfigObject: function(config) {
		if(config.elementToMask == undefined) config.elementToMask = Ext.getBody();
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // abstract methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
     * @method
     * Creates the classification that will be used for map thematization. 
     * It must be properly implemented by subclasses
     */
    , setClassification: function() {
    	// implement this in subclasses
    }
    
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	 /**
     * @method
     * To be overriden by subclasses.
     *
     * @param {Object} options
     */
    , thematize: function(options) {
    	Sbi.trace("[Thematizer.thematize] : IN");
        this.layer.renderer.clear();
        this.layer.redraw();
        this.updateLegend();
        this.layer.setVisibility(true);
        Sbi.trace("[Thematizer.thematize] : OUT");
    }
    
    /**
     * TODO: move this method in a proper utilities file
     */
    , convertToNumber: function(value) {
    	if(typeof value == "Number") return value;
    	return parseFloat(value);
    }
    
    /**
     * Apply the filters to the store
     */
    , filterStore: function(filterValues){
    	
    	Sbi.trace("[Thematizer.filterStore] : IN");
    	if(filterValues){
    		
    		if(this.store.readyForThematization === true) {
    			
    			Sbi.debug("[Thematizer.filterStore] : Store is ready so can be filtered");
    			//(this.thematyzerType + ": filterStore(ready) : " + this.store.readyForThematization);
    			Sbi.debug("[Thematizer.filterStore] : [" + this.thematyzerType + "] : " + Sbi.toSource(filterValues));
    			
	    		this.storeFilters = filterValues;
	    		
	    		var filterFunction = function(record, id) {
	    			for(var i=0; i<filterValues.length; i++){
		        		var fieldName = null;
		        		if(filterValues[i].fieldName) {
		        			fieldName = filterValues[i].fieldName;
		        		} else {
		        			if(filterValues[i].fieldHeader) {
		        				var field = this.getStoreFieldByHeader(filterValues[i].fieldHeader);
		        				filterValues[i].fieldName = fieldName;
		        				fieldName = filterValues[i].fieldName;	
		        			}
		        		}
		        		
		        		var value = filterValues[i].value;
		        		if(fieldName && value!=null && value!=undefined && value!="") {
		        			if(record.data[fieldName]!=value){
		        				return false;
		        			}
		        		} else {
		        			Sbi.debug("[Thematizer.filterStore] : there are filters with no field defined");
		        		}
		    		}
	    			
	    			return true;
	    		};
	    		
	    		this.store.filterBy(filterFunction, this);
	    		var records = this.store.getRange();
	    		for(var x = 0; x < records.length; x++) {
	    			Sbi.debug("[Thematizer.filterStore] : [" + this.thematyzerType + "] : records " + Sbi.toSource(this.store.getAt(x).data));
	    		}
	    		this.setClassification();
	    		
    		} else {
    			Sbi.debug("[Thematizer.filterStore] : Store is not ready so we filter it later");
    			this.pendingStoreFilters = filterValues;
    		}
    	}
    	Sbi.trace("[Thematizer.filterStore] : OUT");
    }
    
    /**
     * @method
     *
     * @param {Object} obj
     */
    , showDetails: function(obj) {
        var feature = obj.feature;
        // popup html
        var html = typeof this.nameAttribute == 'string' ?
            '<h4 style="margin-top:5px">'
                + feature.attributes[this.nameAttribute] +'</h4>' : '';
        html += this.indicator + ": " + feature.attributes[this.indicator];
        // create popup located in the bottom right of the map
        var bounds = this.layer.map.getExtent();
        var lonlat = new OpenLayers.LonLat(bounds.right, bounds.bottom);
        var size = new OpenLayers.Size(200, 100);
        var popup = new OpenLayers.Popup.AnchoredBubble(
            feature.attributes[this.nameAttribute],
            lonlat, size, html, 0.5, false);
        var symbolizer = feature.layer.styleMap.createSymbolizer(feature, 'default');
        popup.setBackgroundColor(symbolizer.fillColor);
        this.layer.map.addPopup(popup);
    }

    /**
     * @method
     *
     * @param {Object} obj
     */
    , hideDetails: function(obj) {
        //remove all other popups from screen
        var map= this.layer.map;
        for (var i = map.popups.length - 1; i >= 0; --i) {
            map.removePopup(map.popups[i]);
        }
    }
    
    , showMask: function(msg) {
		this.mask = new Ext.LoadMask(this.elementToMask, {msg: msg || "Loading"});
        this.mask.show();
	}
    
    , hideMask: function() {
    	if(this.mask) {
    		this.mask.hide();
        	this.mask = null;
    	}
    }
    
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	
    
    /**
     * @method
     * Method used to update the properties. It call #setOptions then check if some properties value is changed. If so
     * regenerate the classification on which the thematization is built on calling #setClassification method. It should
     * be overwritten by subclasses if not all the properties contained in options object can trigger a classification recalculation
     * but only a subset of them.
     * 
     * @param {Object} options object
     */
    , updateOptions: function(newOptions) {
    	Sbi.debug("[Thematizer.updateOptions] : IN");
        var oldOptions = Ext.apply({}, this.options);
        this.setOptions(newOptions);
        if (newOptions) {
        	var isSomethingChanged = false;
            for(o in newOptions) {
            	if(newOptions[o] !== oldOptions[o]) {
            		isSomethingChanged = true;
            		break;
            	}
            }
            if(isSomethingChanged) {
            	this.setClassification();
            }            
        }
        Sbi.debug("[Thematizer.updateOptions] : OUT");
    } 
     
    /**
     * @method 
     * 
     * @param {Object} newOptions an object containing the value of one
     * or more property that influence the generated thematization. This
     * method does not replace the old options just update them. To substitute
     * the old object with the one passed in use instead the #resetOption method. Finally
     * this method just update the value of the options without forcing also the update
     * of classification on which the thematization is built on. 
     * In order to update options and then also update the classification use #updateOptions method.
     */
    , setOptions: function(newOptions) {
    	Sbi.debug("[Thematizer.setOptions] : IN " + this.indicator);
    	
    	if (newOptions) {
            if (!this.options) {
                this.options = {};
            }
          
            // update our copy for clone
            Ext.apply(this.options,  newOptions);
      
            // add new options to this
            Ext.apply(this, newOptions);
        }  
    	
        Sbi.debug("[Thematizer.setOptions] : OUT" + this.indicator);
    }
    
  
    
    /**
     * @method 
     *
     * @return {Object} the option an object containing the value of all the properties that influence
     * the generated thematization. The returned object is the same set with the last call of the method
     * #setOptions. The content of the object change depending on the actual implementation of the thematizer 
     */
    , getOptions: function() {
    	return this.options || {};
    }
    
    
    
    /**
     * @method 
     *
     * method called only by onVirtualStoreLoaded. It is an extension of that callback.
     * 
     * TODO verify if it s possible to generalize it and use it as subpart of also onPhysicalStoreLoaded
     *
     * @param {Ext.data.Store} store the store that contains data used to generate the new thematization
     * @param {Object} meta the store's metadata as returned from measure catalogue service
     */
    , setData: function(store, meta) {
    	Sbi.trace("[Thematizer.setData] : IN " + store.getTotalCount());
    	
    	
    	if(this.active === false) {
    		Sbi.trace("[Thematizer.setData] : thematizer is not active");
    		this.pendingSetData = true;
    		this.store = store;
    		this.meta = meta;
    		Sbi.trace("[Thematizer.setData] : OUT");
    		return;
    	}
    	
    	// field metadata contained in metadata object are generated by method buildJoinedFieldMetdata of class InMemoryMaterializer
    	// and enriched by method join of class MeasureCatalogueCRUD
    	Sbi.trace("[Thematizer.setData] : store's metadata is equal to: " + Sbi.toSource(meta));
    	
    	if(!store) {
    		Sbi.warn("[Thematizer.setData] : Store input parameter is not defined");
    		return null;
    	}
    	
    	this.indicatorContainer = 'store';
    	this.store = store;
    	Sbi.debug("[Thematizer.setData] : The new store contains [" + store.getCount() + "] records");
    	
    	if(!meta) {
    		Sbi.warn("[Thematizer.setData] : Metadata input parameter is not specified so all other thematization property will be keep unchanged");
    		return null;
    	}
    	
    	
    	if(meta.geoId) {
    		this.storeId = meta.geoId;
    		Sbi.debug("[Thematizer.setData] : The new store id is equal to [" + this.storeId + "]");
    	} else {
    		Sbi.debug("[Thematizer.setData] : Property [storeId] is not specified in store's metadata so it will be used as [storeId] the old one that is equal to [" + this.storeId + "]");
    	}
    	
    	this.indicators = [];
    	var selectedIndictaor = null;
    	for(var i = 0; i < meta.fields.length; i++) {
    		var field = meta.fields[i];
    		Sbi.debug("[Thematizer.setData] : Scanning field [" + Sbi.toSource(field) + "]");
    		Sbi.debug("[Thematizer.setData] : Property role is equal to [" + field.role + "]");
    		if(field.role == 'MEASURE')  {
    			Sbi.debug("[Thematizer.setData] : new indicator found. It is equal to [" + field.header + "]");
    			selectedIndictaor = field.header;
    			this.indicators.push([field.header, field.header]);
    		}
    	}
    	if(selectedIndictaor != null) {
    		Sbi.debug("[Thematizer.setData] : The indicator will be set equal to [" + selectedIndictaor + "]");
    		this.indicator = selectedIndictaor;
    	} else {
    		Sbi.debug("[Thematizer.setData] : Indicator not specified in store's metadata so the ld one will be used [" + this.indicator + "]");
    	}
    	
    	this.filters = this.getAttributeFilters(store, meta);
    	
    	if(meta.geoIdHierarchyLevel) {
    		if(this.geoIdHierarchyLevel != meta.geoIdHierarchyLevel) {
    			this.loadHierarchyLevelMeta(meta.geoIdHierarchyLevel);
    			this.geoIdHierarchyLevel = meta.geoIdHierarchyLevel;
    		} else {
    			this.thematize({resetClassification: true});
    	    	this.fireEvent('indicatorsChanged', this, this.indicators, this.indicator);
    	    	this.fireEvent('filtersChanged', this, this.filters);
    	    	Sbi.debug("[Thematizer.setData] : filtersChanged fired");
    		}
    	} else {
    		Sbi.debug("[Thematizer.setData] : Property [geoIdHierarchyLevel] is not specified in store's metadata it will be used as [geoIdHierarchyLevel] the old one that is equal to [" + this.layerId + "]");
    	}
    	
    	Sbi.trace("[Thematizer.setData] : OUT");
    }
    
    /**
     * @method 
     *
     * @return {OpenLayers.Layer.Vector} the target layer
     */
    , getLayer: function() {
    	return this.layer;
    }
    
    /**
     * @method 
     *
     * @param {String} layer the layer to set as returned by service loadLayerServiceName
     * @param {Object} format the format used to encode layer content. If override the one
     * saved as property of this object
     */
    , setLayer: function(layer, format) {
    	 Sbi.trace("[Thematizer.setLayer] : IN");
    	
    	 Sbi.debug("[Thematizer.setLayer] : Input parameter layer is of type [" + (typeof layer) + "]");
    	  
	     var format = format || this.format || new OpenLayers.Format.GeoJSON();
	     Sbi.debug("[Thematizer.setLayer] : Layer formt is equal to  [" + format + "]");
	     
	     var features = format.read(layer);
	     var newFeatures = features;
	   
	     this.setFeatures(newFeatures);

		 Sbi.trace("[Thematizer.setLayer] : OUT");
    }
    
    , setFeatures: function(features) {
    	 this.layer.removeAllFeatures();
	     this.layer.addFeatures(features);
		 this.layer.renderer.clear();
	     this.layer.redraw();
	     this.map.zoomToExtent(this.layer.getDataExtent());
    }

    , getAttributeFilters: function(store, meta){
    	Sbi.debug("[Thematizer.getAttributeFilters] :IN");
    	
    	var filtersNames = new Array();//array with the name of the filters: all the attributes except the geoId
    	var filtersValueMap = new Array();//array with the values of the filters
    	var filters = new Array();//array with filters: contains definitions and values
    	//the order of the 3 arrays is the same. The values of the filter with name filtersNames[j] stay in filtersValueMap[j]
    	
    	Sbi.debug("[Thematizer.getAttributeFilters] : store type is equal to [" + this.storeType + "]");
    	Sbi.debug("[Thematizer.getAttributeFilters] : geoId column is equal to [" + meta.geoId + "]");
    	
    	Sbi.debug("[Thematizer.getAttributeFilters] : Building the array of filters positions ");

    	for(var i = 0; i < meta.fields.length; i++) {
    		
    		var field = meta.fields[i];
    		Sbi.debug("[Thematizer.getAttributeFilters] : Property role is equal to [" + field.role + "]");
    		if(field.role == 'ATTRIBUTE' && !((meta.geoId) && (meta.geoId==field.header)) )  {
    			Sbi.debug("[Thematizer.getAttributeFilters] : new filter found. It is equal to [" + field.header + "]");
    			filtersNames.push(field.name);
    			filtersValueMap.push({});
    			filters.push(field);
    		}
    	}
    	
    	Sbi.debug("[Thematizer.getAttributeFilters] : Building the map of the filters values " + store.data.length);
    	for(var i = 0; i < store.data.length; i++) {
    		//Sbi.trace("[Thematizer.getAttributeFilters] : Processing line " + i);
    		var row = store.data.items[i].data;
        	for(var j = 0; j < filtersNames.length; j++) {
        		var value = row[filtersNames[j]];
        		var filterValues = filtersValueMap[j];
        		filterValues[value] = value;
        		//Sbi.trace("[Thematizer.getAttributeFilters] : Added value [" + value + "] to [" + filtersNames[j] + "]");
        	}
    	}
    	Sbi.debug("[Thematizer.getAttributeFilters] : Built the map of the filters values");
    	
    	Sbi.debug("[Thematizer.getAttributeFilters] : Building the filters");
    	for(var j = 0; j < filtersNames.length; j++) {

    		filters[j].values = new Array();
    		
        	for(value in filtersValueMap[j]) {
        		filters[j].values.push([value]);
        	}
    	}
    	Sbi.debug("[Thematizer.getAttributeFilters] : Built the filters");
    	
    	return filters;
    }
   
    
    /**
     * @method 
     *  
     * @param {String} indicator the name of indicator whose values we want to extract
     * 
     * @return {Sbi.geo.stat.Distribution} the extracted distribution
     */
    , getDistribution: function(indicator) {
    	
    	Sbi.trace("[Thematizer.getDistribution] : IN");
    	
    	Sbi.trace("[Thematizer.getDistribution] : Extract values for indicator [" + indicator + "] from [" + this.indicatorContainer + "]");
    	
   	 	var values;
   	 	if(this.indicatorContainer === 'layer') {
   	 		if(!this.layerId)  {
	 			throw "Impossible to get distribution from layer. Configuration property [layerId] not defined";
	 		}
   	 		values = this.getDistributionFromLayer(indicator, this.layerId);
   	 	} else if(this.indicatorContainer === 'store') {
   	 		if(!this.storeId)  {
   	 			throw "Impossible to get distribution from store. Configuration property [storeId] not defined";
   	 		}
   	 		values = this.getDistributionFromStore(indicator, this.storeId);
   	 	} else {
   	 		Sbi.error("[Thematizer.getDistribution] : Impossible to extract indicators from a container of type [" + indicatorContainer + "]");
   	 	}
   	 	
   	 	Sbi.trace("[Thematizer.getDistribution] : Extracted [" + values.length + "] values for indicator [" + indicator + "]");
        
   	 	Sbi.trace("[Thematizer.getDistribution] : OUT");
   	 
        return values;
    }
    
    , getDistributionFromLayer: function(indicator, id) {
    	Sbi.trace("[Thematizer.getDistributionFromLayer] : IN");
    	
    	var distribution = new Sbi.geo.stat.Distribution();;
    	
    	var features = this.layer.features;
   	 	Sbi.trace("[Thematizer.getDistributionFromLayer] : Features number is equal to [" + features.length + "]");
        for (var i = 0; i < features.length; i++) {
        	var idValue = features[i].attributes[id];
        	var indicatorValue = features[i].attributes[indicator];
        	var numericIndicatorValue = this.convertToNumber(indicatorValue);
        	if(isNaN(numericIndicatorValue) == true) {
				Sbi.trace("[Thematizer.getDistributionFromLayer] : Value [" + indicatorValue + "] will be discarded because it is not a number");
			} else {
				var dataPoint = new Sbi.geo.stat.DataPoint({
					coordinates: [idValue]
					, value: numericIndicatorValue
				});
				distribution.addDataPoint( dataPoint );
			}
        }
        
        Sbi.trace("[Thematizer.getDistributionFromLayer] : OUT");
        
        return distribution;
    }
    
    , getDistributionFromStore: function(indicator, id) {
    	
    	Sbi.trace("[Thematizer.getDistributionFromStore] : IN");
    	
    	var distribution = new Sbi.geo.stat.Distribution();
    	var records = this.store.getRange();
	 	Sbi.trace("[Thematizer.getDistributionFromStore] : Records number is equal to [" + records.length + "]");
   	 	
	 	
	 	Sbi.trace("[Thematizer.getDistributionFromStore] : Indicator is equal to [" + indicator + "]");
	 	Sbi.trace("[Thematizer.getDistributionFromStore] : Store id is equal to [" + id + "]");
	 	
	 	var indicatorFiledName, idFiledName;
	 	for(var n = 0; n < records[0].fields.getCount(); n++) {
	 		var field = records[0].fields.itemAt(n);
	 		
	 		Sbi.trace("[Thematizer.getDistributionFromStore] : Store column [" + (n+1) + "] is equal to [" + field.header + "]");
	 		if(field.header == indicator) {
	 			indicatorFiledName = field.name;
	 		}
	 		if(field.header == id) {
	 			idFiledName = field.name;
	 		}
	 		
	 		if(indicatorFiledName && idFiledName) break;
	 	}
	 	
	 	if(!idFiledName) {
	 		alert("Impossible to find a column was header is equal to [" + id + "]");
	 	} else {
	 		Sbi.trace("[Thematizer.getDistributionFromStore] : Indicator column name is equal to [" + indicatorFiledName + "]");
		 	Sbi.trace("[Thematizer.getDistributionFromStore] : Store id column name is equal to [" + idFiledName + "]");
	 	}
	 	
	 	for (var i = 0; i < records.length; i++) {	
	 		var idValue = records[i].get(idFiledName);
   	 		var indicatorValue = records[i].get(indicatorFiledName);
   	 		var numericIndicatorValue = this.convertToNumber(indicatorValue);
        	if(isNaN(numericIndicatorValue) == true) {
				Sbi.trace("[Thematizer.getDistributionFromStore] : Value [" + indicatorValue + "] will be discarded because it is not a number");
			} else {
				Sbi.trace("[Thematizer.getDistributionFromStore] : Add datapoint [" + indicatorValue + " - " + idValue + "] to distribution");
				var dataPoint = new Sbi.geo.stat.DataPoint({
					coordinates: [idValue]
					, value: numericIndicatorValue
				});
				distribution.addDataPoint( dataPoint );
			}
        }
	 	
	 	Sbi.trace("[Thematizer.getDistributionFromStore] : OUT");
	 	
    	return distribution;
    }
    
    , getStoreFieldByHeader: function(fieldName) {
    	var field = null;
    	
    	if(this.store === null || this.store.getCount() == 0) return null;
    	
    	var firstRecord = this.store.getAt(0);
    		
	 	for(var n = 0; n < firstRecord.fields.getCount(); n++) {
	 		var f = firstRecord.fields.itemAt(n);
	 		if(f.header == fieldName) {
	 			field = f;
	 			break;
	 		}
	 	}
	 	
	 	return field;
    }
    
    , getStoreFieldByName: function(fieldId) {
    	var field = null;
    	
    	if(this.store === null || this.store.getCount() == 0) return null;
    	
    	var firstRecord = this.store.getAt(0);
    		
	 	for(var n = 0; n < firstRecord.fields.getCount(); n++) {
	 		var f = firstRecord.fields.itemAt(n);
	 		Sbi.trace("[Thematizer.getStoreFieldById] : " + Sbi.toSource(f, false));
	 		if(f.name == fieldId) {
	 			field = f;
	 			break;
	 		}
	 	}
	 	
	 	return field;
    }

    /**
     * @method 
     * Extend layer style for the default render intent and
     * for the select render intent if featureSelection is
     * set.
     *
     * @param {Array({<OpenLayers.Rule>})} rules Array of new rules to add
     * @param {Object} symbolizer Object with new styling options
     * @param {Object} context Object representing the new context
     */
    , extendStyle: function(rules, symbolizer, context) {
    	
    	Sbi.trace("[Thematizer.extendStyle] : IN");
    	
    	var defaultStyle = new OpenLayers.Style(
            	OpenLayers.Util.applyDefaults(
                  {'fillOpacity': 0.8},
                  OpenLayers.Feature.Vector.style['default']
              	)
          	);
          	
        var style = this.layer.styleMap.styles['default'];
        // replace rules entirely - the geostat object takes control
        // on the style rules of the "default" render intent
        if (rules) {
            style.rules = rules;
        } else{
        	style.rules = defaultStyle.rules;
        }
        
        if (symbolizer) {
            style.setDefaultStyle(
                OpenLayers.Util.applyDefaults(
                    symbolizer,
                    style.defaultStyle
                )
            );
        }
        if (context) {
            if (!style.context) {
                style.context = {};
            }
            OpenLayers.Util.extend(style.context, context);
        }
        
    	Sbi.trace("[Thematizer.extendStyle] : OUT");
    }

    
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Called by the constructor to initialize this object
	 * 
	 * @param {OpenLayers.Map} OpenLayers map object
	 * @param {Object} Hashtable of extra options
	 */

     , initialize: function(map, options) {
    	Sbi.trace("[Thematizer.initialize]: IN");
        
    	this.map = map;
        this.setOptions(options);
        
        if (!this.layer) {
        	Sbi.debug("[Thematizer.initialize]: target layer not specified. A new one will be created");
        	this.initLayer();
        } else {
        	Sbi.debug("[Thematizer.initialize]: target layer already defined");
        }
        
        if (this.featureSelection) {
        	Sbi.debug("[Thematizer.initialize]: feature selection enabled. A feature selection control will be created");
        	this.initFeatureSelectionControl();
        } else {
        	Sbi.debug("[Thematizer.initialize]: feature selection disabled");
        }
        
        this.legendDiv = Ext.get(this.options.legendDiv);
        
        Sbi.trace("[Thematizer.initialize]: OUT");
     }
     , deactivate: function(){
    	 Sbi.trace("[Thematizer.deactivate]: IN");
    	 
    	 this.active = false;
    	 
    	 if(this.layer.features && this.layer.features.length > 0){
    		 this.cachedFeatures = this.layer.features;
    		 Sbi.trace("[Thematizer.deactivate]: layer's features succesfully cached");
    	 }
    			
    	 
    	 Sbi.trace("[Thematizer.deactivate]: OUT");
     }
     , isFirstActivation: true
     , activate: function(){
    	 
    	 Sbi.trace("[Thematizer.activate]: IN");
    	 
    	 this.active = true;
    	 
    	 if(this.pendingSetData===true) {
			 this.setData(this.store, this.meta);
			 this.meta = null;
			 this.pendingSetData = false;
			 this.isFirstActivation = false;
			 return;
		 }
    	 
    	 if(this.isFirstActivation === false) {
    		 Sbi.debug("[Thematizer.activate]: Thematizer already activated");
    		 if(this.cachedFeatures != null) {
    			 this.setFeatures(this.cachedFeatures);
    		 }
    		     		 
    		 Sbi.trace("[Thematizer.activate]: OUT");
    		 return;
    	 }
    	 this.isFirstActivation = false;
    	 
    	 // get features from web service if a url is specified
         if (this.loadLayerServiceName && this.layerId && this.indicatorContainer != 'store') {
         	// if indicator container is store we will load te layer only after the store has been succesfully loaded
         	Sbi.debug("[Thematizer.activate]: Url attribute has been valorized to [" + Sbi.toSource(url) + "]. Features will be loaded from it");
         	this.loadLayer();
         } else {
         	Sbi.debug("[Thematizer.activate]: Url attribute or layerId has not been valorized");
         }
         
         if(this.indicatorContainer == 'store') {
         	
         	Sbi.debug("[Thematizer.activate]: Property [indicatorContainer] is equal to [store]");
         	
         	if(this.storeType === 'physicalStore') {
         		Sbi.debug("[Thematizer.initialize]: Loading physical store...");
         		this.store.on('metachange', function(store, meta) {
         			this.meta = meta;
         		}, this);
         		this.store.on('load', this.onPhysicalStoreLoaded, this);
         		
         		this.store.on('loadexception', function(store, options, response, e) {
         			Sbi.exception.ExceptionHandler.showErrorMessage("Error: " + e + ": " + Sbi.toSource(response, true), "Impossible to load store");
         			
         			Sbi.debug("[Thematizer.activate]: response text: " + response.responseText);
         			var r = Ext.util.JSON.decode(response.responseText);
         			//Sbi.debug("[Thematizer.activate]: response: " + Sbi.toSource(response, false));
         		});
         		
             	if(this.storeReload == true && this.store.readyForThematization !== true) {
             		this.loadPhysicalStore();
             	} else {
             		Sbi.debug("[Thematizer.activate]: Physical store already loaded. It doesn't need to be reloaded");
             		this.loadLayer();
             	}
         	} else if(this.storeType === 'virtualStore') {
         		if(this.storeReload == true) {
	         		Sbi.debug("[Thematizer.activate]: Loading virtual store...");
	         		if(this.storeConfig.params) {
	 	        		this.loadVirtualStore();
	 	        	} else {
	 	        		Sbi.warn("[Thematizer.activate]: Virtual store wont be loaded because [storeConfig.params] is not defined");
	 	        	}
         		} else {
             		Sbi.debug("[Thematizer.activate]: Virtual store already loaded. It doesn't need to be reloaded");
             	}
         	} else {
         		Sbi.debug("[Thematizer.activate]: Property [storeType] value [" + this.storeType + "] is not valid");
         	}
         } else if(this.indicatorContainer == 'layer') {
         	Sbi.debug("[Thematizer.activate]: Property [indicatorContainer] is equal to [layer]");
         } else {
         	Sbi.warn("[Thematizer.activate]: Property [indicatorContainer] is equal to [" + this.indicatorContainer + "]");
         }
     }
     
     /**
      * @method 
      * create an empty layer that will contains thematized features
      */
     , initLayer: function() {
    	 var styleMap = new OpenLayers.StyleMap({
             'default': new OpenLayers.Style(
                 OpenLayers.Util.applyDefaults(
                     this.defaultSymbolizer,
                     OpenLayers.Feature.Vector.style['default']
                 )
             ),
             'select': new OpenLayers.Style(this.selectSymbolizer)
         });
    	 
         var layer = new OpenLayers.Layer.Vector('geostat', {
             'displayInLayerSwitcher': false,
             'visibility': false,
             'styleMap': styleMap
         });
         
         map.addLayer(layer);
         this.layer = layer;
     }

     /**
      * @method 
      * create select feature control so that popups can
      * be displayed on feature selection
      */
     , initFeatureSelectionControl: function() {
    	 this.layer.events.on({
             'featureselected': this.showDetails,
             'featureunselected': this.hideDetails,
             scope: this
         });
         var selectFeature = new OpenLayers.Control.SelectFeature(
             this.layer,
             {'hover': true}
         );
         map.addControl(selectFeature);
         selectFeature.activate();
     }
     
     // -----------------------------------------------------------------------------------------------------------------
     // synchronization methods
 	 // -----------------------------------------------------------------------------------------------------------------
 	
     
     , loadHierarchyLevelMeta: function(levelName) {
     	Sbi.debug("[Thematizer.loadHierarchyLevelMeta] : IN");
     	
     	this.showMask("Loading metadata of geographical level [" + levelName + "]");
     		
     	var loadHierarchyLevelInfoServiceUrl = Sbi.config.serviceRegistry.getServiceUrl({
 			serviceName: 'GetHierarchyLevelMeta'
 			, baseParams: {levelName: levelName}
 		});
     	
     	Sbi.debug("[Thematizer.loadHierarchyLevelMeta] : loading metadata of level [" + levelName + "]...");		
 		Ext.Ajax.request({
 			url: loadHierarchyLevelInfoServiceUrl,
 			success : this.onHierarchyLevelMetaLoad,
 			failure: function(response) {
 				Sbi.exception.ExceptionHandler.handleFailure(response);
 				this.hideMask();
 			},  
 			scope: this
 		});
 		
 		Sbi.debug("[Thematizer.loadHierarchyLevelMeta] : OUT");
     }
     
     , onHierarchyLevelMetaLoad: function(response, options) {
     	Sbi.trace("[Thematizer.onLoadHierarchyLevelMeta] : IN");
     	
     	Sbi.debug("[Thematizer.onLoadHierarchyLevelMeta] : metadata of level [" + (options?Sbi.toSource(options.headers, true): 'undefined') + "] succesfully loaded");	
     	
     	this.hideMask();
    
 		if(response !== undefined && response.responseText !== undefined && response.statusText=="OK") {
 			if(response.responseText!=null && response.responseText!=undefined){
 				if(response.responseText.indexOf("error.mesage.description")>=0){
 					Sbi.exception.ExceptionHandler.handleFailure(response);
 				} else {
 					var levelMeta = JSON.parse(response.responseText);
 					this.layerName = levelMeta.layerName;
 					this.layerId = levelMeta.layerId;
 		     		this.featureSourceType = levelMeta.featureSourceType;
 		     		this.featureSource = levelMeta.featureSource;
 					this.loadLayer(this.onLayerLoaded);
 				}
 			}
 		} else {
 			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
 		}
 		
 		Sbi.trace("[Thematizer.onLoadHierarchyLevelMeta] : OUT");
 	}
    
    /**
     * @method
     * 
     * extract the distinct values of the storeId column
     * 
     * TODO: the extraction part is similar to the one used for extracting
     * indicator's values from store. Can we marge the code?
     */
    , getFeatureIdsFromStore: function() {
    	Sbi.trace("[Thematizer.getFeatureIdsFromStore]: IN " + this.store.getTotalCount());
    	var storeIdFiledName;
    	var records = this.store.getRange(0,1);
	 	for(var n = 0; n < records[0].fields.getCount(); n++) {
	 		var field = records[0].fields.itemAt(n);
	 		if(field.header == this.storeId) {
	 			storeIdFiledName = field.name;
	 			break;
	 		}
	 	}
    	var featureIds = this.store.collect(storeIdFiledName, false, true);
    	Sbi.debug("[Thematizer.getFeatureIdsFromStore]: found [" + featureIds.length + "] distinct feature id in store");
    	Sbi.trace("[Thematizer.getFeatureIdsFromStore]: OUT");
    	return featureIds;
    }
     
    , loadLayer: function(onSuccess, onFailure) {
    	
    	Sbi.trace("[Thematizer.loadLayer]: IN");
    	
    	this.showMask("Loading layer [" + this.layerName + "] ...");
    	
    	//try {
	    	Sbi.debug("[Thematizer.loadLayer]: onSuccess callback defined [" + (onSuccess != undefined) + "]");
	    	Sbi.debug("[Thematizer.loadLayer]: onFailure callback defined [" + (onFailure != undefined) + "]");
	    	
	     	var params = {
	     		layer: this.layerName
	     		, businessId: this.storeId
	     		, geoId: this.layerId
	     		, featureSourceType: this.featureSourceType
	     		, featureSource: this.featureSource
	     	};
	     	
	     	if(this.indicatorContainer == 'store' && this.filterLayerOnLoad === true) {
	     		var featureIds = this.getFeatureIdsFromStore();
		    	var encodedFeatures = Ext.util.JSON.encode(featureIds);
		    	params.featureIds = encodedFeatures;
	     	}
	     	
	     	Sbi.debug("[Thematizer.loadLayer]: Service parameters are equal to [" + Sbi.toSource(params) + "]");
	     	
	     	var loadLayerServiceUrl = Sbi.config.serviceRegistry.getServiceUrl({
	 			serviceName: this.loadLayerServiceName
	 			, baseParams: {} //params
	 		});
	     	
	     	Sbi.debug("[Thematizer.loadLayer]: Service url is equal to [" + loadLayerServiceUrl + "]");
	     	
	     	
	     	Sbi.debug("[Thematizer.loadLayer]: Loading layer [" + this.layerName + "] ...");
	     	
	     	Ext.Ajax.request({
	     		url: loadLayerServiceUrl
	     		, params: params
	     		, success : onSuccess || this.onSuccess
	     		, failure: onFailure || this.onFailure
	     		, scope: this
	     	});
	     	
	     	/*
	     	OpenLayers.Request.GET({
	     		url: loadLayerServiceUrl
	     		, success: onSuccess || this.onSuccess
	     		, failure: onFailure || this.onFailure
	     		, scope: this
	     	});
	     	*/
//    	} catch (e) {
//    		Sbi.exception.ExceptionHandler.showErrorMessage('An unexpected error occured whiel loading layer [' + this.layerName + ']: ' + e, 'Internal error');
//    	}
     	
     	
     	Sbi.trace("[Thematizer.loadLayer]: OUT");
     }
    
     , onLayerLoaded: function(response, options) {
    
    	 Sbi.trace("[Thematizer.onLayerLoaded] : IN");	 
    	 Sbi.debug("[Thematizer.onLayerLoaded] : Layer [" + options.params.layer + "] succesfully loaded");
    	 
    	 this.hideMask();
    	 
    	 this.showMask("Adding layer to map...");
		 var layer = response.responseXML;
	     if (!layer || !layer.documentElement) {
	    	 layer = response.responseText;
	     }
	     this.setLayer(layer);
	     this.hideMask();
	     
	     this.showMask("Thematizing layer...");
		 Sbi.debug("[Thematizer.onLayerLoaded] : Thematizing layer ...");
		 this.thematize({resetClassification: true});
		 Sbi.debug("[Thematizer.onLayerLoaded] : Layer succesfully thematized");
		 this.hideMask();	
		 
		 this.fireEvent('layerloaded', this, layer);
		 Sbi.trace("[Thematizer.onLayerLoaded]: event [layerloaded] fired");
		 
	     this.fireEvent('indicatorsChanged', this, this.indicators, this.indicator);
	     this.fireEvent('filtersChanged', this, this.filters);
	     
	     Sbi.trace("[Thematizer.onLayerLoaded] : OUT");
     }
     
     /**
      * @method 
      *
      * @param {Object} response
      */
     , onSuccess: function(response) {
     	 Sbi.trace("[Thematizer.onSuccess]: IN");
     	
     	 this.hideMask();
     	
     	 this.showMask("Adding layer to map...");
//       var doc = response.responseXML;
//       if (!doc || !doc.documentElement) {
//           doc = response.responseText;
//       }
//        
//       var format = this.format || new OpenLayers.Format.GeoJSON();
//        
//       var features = format.read(doc); 
//       this.layer.removeAllFeatures();
//       this.layer.addFeatures( features );
//       this.map.zoomToExtent(this.layer.getDataExtent());
     	 var layer = response.responseXML;
	     if (!layer || !layer.documentElement) {
	    	 layer = response.responseText;
	     }
	     this.setLayer(layer);
        
         //this.requestSuccess(response);
         
         this.hideMask();
         
         this.fireEvent('layerloaded', this, this.layer);
         Sbi.trace("[Thematizer.onSuccess]: event [layerloaded] fired [" + this.layer.features.length+ "]");
         
         Sbi.trace("[Thematizer.onSuccess]: OUT");
     }

     /**
      * @method 
      *
      * @param {Object} response
      */
     , onFailure: function(response) {
     	this.hideMask();
         this.requestFailure(response);
     }

     /**
      * @method 
      * Load the physical store
      */
     , loadPhysicalStore: function() {
    	 Sbi.debug("[Thematizer.loadPhysicalStore]: IN");
    	 this.store.load({});
    	 Sbi.debug("[Thematizer.loadPhysicalStore]: OUT");
     }
     
     /**
      * @method 
      * callback called when the physical store has been succesfully loaded
      * 
      * @param {Ext.data.Store} store
      */
     , onPhysicalStoreLoaded: function(store) {
    	Sbi.trace("[Thematizer.onPhysicalStoreLoaded]: IN");
    	Sbi.trace("[Thematizer.onPhysicalStoreLoaded]: Meta property list [" + Sbi.toSourcePropertiesList(this.meta)+ "]");
    	
    	this.store.readyForThematization = true;
    	
    	for(var i = 0; i <  this.meta.fields.length; i++) {
        	Sbi.trace("[Thematizer.onPhysicalStoreLoaded]: Field [" + this.meta.fields[i].header + "] role is equal to  [" + this.meta.fields[i].role + "]");
        }
    	
    	Sbi.trace("[Thematizer.onPhysicalStoreLoaded]: Geo Id column is equal to [" + this.storeId + "]");
    	
    	this.meta.geoId = this.storeId;
    	this.filters = this.getAttributeFilters(store, this.meta);
    	this.fireEvent('filtersChanged', this, this.filters);
    	Sbi.trace("[Thematizer.onPhysicalStoreLoaded]: filtersChanged fired");
    	
    	this.loadLayer();
    	
    	Sbi.trace("[Thematizer.onPhysicalStoreLoaded]: OUT");
     }
     
     /**
      * @method 
      * Load the virtual store
      */
     , loadVirtualStore: function() {
    	 
    	 Sbi.debug("[Thematizer.loadVirtualStore]: IN");
 
    	 if(!this.storeConfig.params) {
    		 Sbi.warn("Impossible to load virtual store because property [storeConfig.params] is undefined");
    		 return;
    	 } else {
    		 Sbi.debug("[Thematizer.loadVirtualStore]: Virtual store loader service's parameters are equal to [" + Sbi.toSource(this.storeConfig.params) + "]");
    	 }
    	 
    	 if(!this.storeConfig.url) {
    		 Sbi.warn("Impossible to load virtual store because property [storeConfig.url] is undefined");
    		 return;
    	 } else {
    		 Sbi.debug("[Thematizer.loadVirtualStore]: Virtual store loader service's url is equal to [" + this.storeConfig.url + "]");
    	 }
    	 
    	
    	 
    	 Ext.Ajax.request({
			url: this.storeConfig.url
			, params: this.storeConfig.params
			, success : this.onVirtualStoreLoaded
			, failure: function(response) {
				if(response.responseText){
					var err = Ext.decode(response.responseText);
					if(err.errors && err.errors[0] && err.errors[0].message && err.errors[0].message.indexOf("is not visible for the")){
						Sbi.exception.ExceptionHandler.showErrorMessage(err.errors[0].message);
						return;
					}
				}
				
				Sbi.exception.ExceptionHandler.handleFailure(response);

			}
			, scope: this
    	 });
    	 
    	 Sbi.debug("[Thematizer.loadPhysicalStore]: OUT");
     }
     
     /**
      * used during thematization to decide if go or stop the proces. the store can be not ready
      * because it is in loding phase.
      */
     , isStoreReady: function() {
    	 Sbi.trace("[Thematizer.isStoreReady]: IN");
    	 var isReady = this.storeId != null && this.store != null;
    	 Sbi.trace("[Thematizer.isStoreReady]: Store is ready [" + isReady + "]");
    	 Sbi.trace("[Thematizer.isStoreReady]: OUT");
    	 return isReady;
     }     
     
     , isLayerReady: function() {
    	 Sbi.trace("[Thematizer.isLayerReady]: IN");
    	 var isReady = this.layerId != null && this.layer != null && this.layer.features && this.layer.features.length > 0;
    	 Sbi.trace("[Thematizer.isLayerReady]: Layer is ready [" + isReady + "]");
    	 Sbi.trace("[Thematizer.isLayerReady]: OUT");
    	 return isReady;
     }
     
     , isThematizerReady: function() {
    	 Sbi.trace("[Thematizer.isThematizerReady]: IN");
    	 var isReady = this.isStoreReady() && this.isLayerReady();
    	 Sbi.trace("[Thematizer.isThematizerReady]: Thematizer is ready [" + isReady + "]");
    	 Sbi.trace("[Thematizer.isThematizerReady]: OUT");
    	 return isReady;
     }
    
     /**
      * @method 
      * callback called when the virtual store has been succesfully loaded
      */
     , onVirtualStoreLoaded: function(response, options) {
    	 Sbi.trace("[Thematizer.onVirtualStoreLoaded]: IN");
    	 
    	 if(response !== undefined && response.responseText !== undefined && response.statusText=="OK") {
    		 if(response.responseText!=null && response.responseText!=undefined) {
				if(response.responseText.indexOf("error.mesage.description")>=0){
					Sbi.exception.ExceptionHandler.handleFailure(response);
				} else {
					var r = Ext.util.JSON.decode(response.responseText);
			
					var store = new Ext.data.JsonStore({
					    fields: r.metaData.fields
					});
					store.loadData(r.rows);
					Sbi.trace("[Thematizer.onVirtualStoreLoaded]: Store now contains [" + r.rows.length + "] rows");
					this.setData(store, r.metaData);
				}
			}
		} else {
			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		}
    	 
    	Sbi.trace("[Thematizer.onVirtualStoreLoaded]: OUT");
	}
});











/* Copyright (c) 2006-2011 by OpenLayers Contributors (see authors.txt for 
 * full list of contributors). Published under the Clear BSD license.  
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Filter.js
 */

/**
 * Class: OpenLayers.Filter.Function
 * This class represents a filter function.
 * We are using this class for creation of complex 
 * filters that can contain filter functions as values.
 * Nesting function as other functions parameter is supported.
 * 
 * Inherits from
 * - <OpenLayers.Filter>
 */
OpenLayers.Filter.Function = OpenLayers.Class(OpenLayers.Filter, {

    /**
     * APIProperty: name
     * {String} Name of the function.
     */
    name: null,
    
    /**
     * APIProperty: params
     * {Array(<OpenLayers.Filter.Function> || String || Number)} Function parameters
     * For now support only other Functions, String or Number
     */
    params: null,  
    
    /** 
     * Constructor: OpenLayers.Filter.Function
     * Creates a filter function.
     *
     * Parameters:
     * options - {Object} An optional object with properties to set on the
     *     function.
     * 
     * Returns:
     * {<OpenLayers.Filter.Function>}
     */
    initialize: function(options) {
        OpenLayers.Filter.prototype.initialize.apply(this, [options]);
    },

    CLASS_NAME: "OpenLayers.Filter.Function"
});

Sbi.geo.stat.Thematizer.supportedType =  {};

Sbi.geo.stat.Thematizer.addSupportedType = function(typeName, thematizerClass, controlPanelClass) {
	Sbi.geo.stat.Thematizer.supportedType[typeName] = {
		typeName: typeName
		, thematizerClass: thematizerClass
		, controlPanelClass: controlPanelClass
	};
}

