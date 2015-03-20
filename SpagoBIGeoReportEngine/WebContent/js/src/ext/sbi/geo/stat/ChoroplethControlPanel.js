/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/
Ext.ns("Sbi.geo.stat");

/**
 * The Choropleth class create a widget allowing to set up and display choropleth
 * thematization on a map
 *
 * ## Further Reading
 *
 * A choropleth map is a thematic map in which areas are shaded or patterned in 
 * proportion to the measurement of the statistical variable being displayed on 
 * the map, such as population density or per-capita income.
 * 
 * The choropleth map provides an easy way to visualize how a measurement varies 
 * across a geographic area or it shows the level of variability within a region.
 *
 *   - {@link http://en.wikipedia.org/wiki/Choropleth_map} - Wikipedia entry on Choroplet maps
 *   - {@link http://leafletjs.com/examples/choropleth.html} - An example of interactive choroplet map
 *
 * @author Andrea Gioia
 */
Sbi.geo.stat.ChoroplethControlPanel = Ext.extend(Ext.FormPanel, {
	
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
	 * @property {String} indicatorContainer
	 * The object that contains data points used by the thematizer. It is equal to 'store' if data points are 
	 * contained into an Ext.data.Store, it is equalt to 'layer' otherwise if datapoint are contained into an
	 * OpenLayers.Layer.Vector. By default it is equal to store.
	 */
	 indicatorContainer: 'store'
		 
	
	 /**
	  * @property {String} storeType
	  * The type of store that contains data points used by thematizer. Only apply if property #indicatorContainer is
	  * equal to 'store'. It is equal to 'physicalStore' if the store is feeded by a SpagoBI' dataset, it is equal to
	  * 'virtualStore' if the store is feeded by SpagoBI's measure catalogue.
	  */
     , storeType: 'physicalStore'
    	
     /**
      * @property {String} storeConfig
      * An object containing configuration used to generate the store. If property #storeType is equal to 'virtualStore' contains
      * for example the ids of the mesure to join in the generated dataset
      */
     , storeConfig: null
     
	/**
	 * @property {OpenLayers.Layer.Vector} layer
	 * The vector layer containing the features that
	 * are styled based on statistical values. If none is provided, one will
	 * be created.
	 */
    , layer: null

    /**
	 * @property {OpenLayers.Format} format
	 * The OpenLayers format used to get features from the HTTP request response. 
	 * GeoJSON is used if none is provided.
	 */
    , format: null

    /**
	 * The service name to call in order to load target layer. If none is provided, the features
     * found in the provided vector layer will be used.
	 */
    , loadLayerServiceName: null

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

    , manageIndicator: true
    
    /**
	 * @property {Array} indicators
	 * An array of selectable indicators. Each item of the array is an array composed by two element. The first is the name
	 * of the indictor the secon one is the indicator text (ie. human readable).
	 */
    , indicators: null
    
    /**
	 * @property {String} indicator
	 * The feature attribute currently chosen. Useful if callbacks are registered 
	 * on 'featureselected' and 'featureunselected' events
	 */
    , indicator: null

    
    /**
	 * @property {String} indicatorText
	 * The raw value of the currently chosen indicator (ie. human readable). Useful if callbacks are registered on 'featureselected'
	 * and 'featureunselected' events
	 */
    , indicatorText: null

    
    /**
     * Property: thematizer
     * {<mapfish.GeoStat.ProportionalSymbol>} The core component object.
     */
    , thematizer: null

    /**
     * Property: thematizationApplied
     * {Boolean} true if the thematization was applied
     */
    , thematizationApplied: false

    /**
     * Property: ready
     * {Boolean} true if the widget is ready to accept user commands.
     */
    , ready: false

    /**
     * Property: border
     *     Styling border
     */
    
    /**
     * @cfg {Boolean} [border=false]
     * `true` if the border should be ..., false if it is ...
     */
    , border: false


    /**
     * APIProperty: labelGenerator
     * Generator for bin labels
     */
    , labelGenerator: null
    
    /**
     * @property {Array} The list of user filters (combobox objects)
     * The user can filter the store by the dimension not equals to the geoId
     */
    , filters: null

    
    // =================================================================================================================
	// METHODS
	// =================================================================================================================
    
    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    /**
     * Perform thematization
     * 
     * @param {Boolean} exception If true show a message box to user if either
     * the widget isn't ready, or no indicator is specified, or no
     * method is specified.
     * @param {Object} additional options to pass to the thematizer.
     */
    , thematize: function(exception, additionaOptions) {
    	Sbi.trace("[ChoropletControlPanel.thematize] : IN");
    	
    	var doThematization = true;
    	
        if (!this.ready) {
            if (exception === true) {
                Ext.MessageBox.alert('Error', 'Component init not complete');
            }
            Sbi.warn("[ChoropletControlPanel.thematize] : Component init not complete");
            doThematization = false;
        }
        var options = this.getThemathizerOptions();
       
        if (!options.indicator) {
            if (exception === true) {
                Ext.MessageBox.alert('Error', 'You must choose an indicator');
            }
            Sbi.warn("[ChoropletControlPanel.thematize] : You must choose an indicator");
            doThematization = false;
        }
        if (!options.method) {
            if (exception === true) {
                Ext.MessageBox.alert('Error', 'You must choose a method');
            }
            Sbi.warn("[ChoropletControlPanel.thematize] : You must choose a method");
            doThematization = false;
        }

        options = Ext.apply(options, additionaOptions||{});
        
        if(doThematization) {
        	this.thematizer.thematize(options);
            this.thematizationApplied = true;
            Sbi.debug("[ChoropletControlPanel.thematize] : thematization succesfully applied");
        } else {
        	this.thematizer.setOptions(options);
        	Sbi.debug("[ChoropletControlPanel.thematize] : thematization not applied");
        }        
        
        Sbi.trace("[ChoropletControlPanel.thematize] : OUT");
    }
	
    // -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
    
	/**
	 * @method
	 * 
	 * convert form state object in a valid thematizerOption object. It is used when the state of this controller
	 * must be applied to the thematzier. 
	 */
	, getThemathizerOptions: function() {
		Sbi.trace("[ChoropletControlPanel.getThemathizerOptions] : IN");
		var formState = this.getFormState();
		var options = {};
		
		options.method = Sbi.geo.stat.Classifier[formState.method];
		options.numClasses = formState.classes;
		options.colors = new Array(2);
		options.colors[0] = new Sbi.geo.utils.ColorRgb();
		options.colors[0].setFromHex(formState.fromColor);
		options.colors[1] = new Sbi.geo.utils.ColorRgb();
		options.colors[1].setFromHex(formState.toColor);
		if(this.manageIndicator === true) {
			options.indicator = formState.indicator;
		}
		Sbi.trace("[ChoropletControlPanel.getThemathizerOptions] : OUT");
		
		return options;
	}
	
	/**
	 * @method
	 * 
	 * convert the thematizerOption object used by the thematizer in a valid form state. The generated formState object is applied
	 * to this controller. 
	 */

	, synchronizeFormState: function() {
		Sbi.trace("[ChoropletControlPanel.syncronizeFormState] : IN");
	
		
		var formState = this.thematizer.getAnalysisConf();
		
		Sbi.trace("[ChoropletControlPanel.syncronizeFormState] : new form state is equal to [" + Sbi.toSource(formState) + "]");
		
		this.setMethod(formState.method);
		this.setNumberOfClasses(formState.classes);
		this.setFromColor(formState.fromColor);
		this.setToColor(formState.toColor);
		if(this.manageIndicator === true) {
			this.setIndicator(formState.indicator);
		}
		
		Sbi.trace("[ChoropletControlPanel.syncronizeFormState] : OUT");
	}
	

	, getFormState: function() {
		var formState = {};
		
		formState.method = this.getMethod();
		formState.classes = this.getNumberOfClasses();
		formState.fromColor = this.getToColor();
		formState.toColor = this.getFromColor();
		if(this.manageIndicator === true) {
			formState.indicator = this.getIndicator();
		}
		formState.filtersDefaultValues = this.getFiltersDefaultValues();
		return formState;
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected classification method
	 */
	, getMethod: function() {
		return this.form.findField('method').getValue();
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected number of classes 
	 */
	, getNumberOfClasses: function() {
		return this.form.findField('numClasses').getValue();
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected lowerBoundColor
	 */
	, getToColor: function() {
		return this.form.findField('colorA').getValue();
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected upper bound color
	 */
	, getFromColor: function() {
		return this.form.findField('colorB').getValue();
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected indicator
	 */
	, getIndicator: function() {
		return this.manageIndicator === true? this.form.findField('indicator').getValue() : undefined;
	}
	
	/**
	 * @method
	 * 
	 * @return {Array} the default values of the filters
	 */
	, getFiltersDefaultValues: function() {
		var values = new Array();
		if(this.filters){
			for(var i=0; i<this.filters.length; i++){
				var filter = this.filters[i];
				values.push({
					name: filter.name,
					value : filter.filterDefaultValue
				});
			}
		}
		return values;
	}
	
	, setFormState: function(formState, riseEvent) {
		Sbi.trace("[ChoropletControlPanel.setFormState] : IN");
	
		this.setMethod(formState.method);
		this.setNumberOfClasses(formState.classes);
		this.setFromColor(formState.fromColor);
		this.setToColor(formState.toColor);
		if(this.manageIndicator === true) {
			this.setIndicator(formState.indicator);
		}
		this.setFiltersDefaultValues(formState.filtersDefaultValues);
		if(riseEvent === true) { this.onConfigurationChange(); }
		
		Sbi.trace("[ChoropletControlPanel.setFormState] : OUT");
	}
	
	, setMethod: function(method, riseEvent) {
		if(method) {
			var m = Sbi.geo.stat.Classifier[method];
			if(m) {
				this.form.findField('method').setValue(method);	
			} else {
				Sbi.warn("[ChoropletControlPanel.setMethod] : Classification method [" + formState.method + "] is not valid");
			}	
			if(riseEvent === true) {this.onConfigurationChange();}
		}
	}
	
	, setNumberOfClasses: function(classes, riseEvent) {
		if(classes) {
			this.form.findField('numClasses').setValue(classes);
			if(riseEvent === true) {this.onConfigurationChange();}
		} 
	}
	
	, setFromColor: function(color, riseEvent) {
		if(color) {
			this.form.findField('colorA').setValue(color);
			if(riseEvent === true) {this.onConfigurationChange();}
		} 
	}
	
	, setToColor: function(color, riseEvent) {
		if(color) {
			this.form.findField('colorB').setValue(color);
			if(riseEvent === true) {this.onConfigurationChange();}
		} 
	}
	
	, setIndicator: function(indicatorName, riseEvents) {
		Sbi.trace("[ChoropletControlPanel.setIndicator] : IN");
		
		if(this.manageIndicator !== true) {
			Sbi.trace("[ChoropletControlPanel.setIndicator] : the control panel does not manage indicators");
			return;
		}
		
		
		Sbi.trace("[ChoropletControlPanel.setIndicator] : Looking for indicator [" + indicatorName + "] ...");
		
		if(indicatorName && this.indicators) {
			var indicator = null;
			for(var i = 0; i < this.indicators.length; i++) {
				Sbi.trace("[ChoropletControlPanel.setIndicator] : Comparing indicator [" + indicatorName + "] with indicator [" + this.indicators[i][0] +"]");
				if (indicatorName == this.indicators[i][0]) {
					indicator = this.indicators[i];
					break;
				}
	        }
			if(indicator != null) {
				Sbi.trace("[ChoropletControlPanel.setIndicator] : Indicator [" + indicatorName + "] succesfully found");
				this.indicatorSelectionField.setValue(indicator[0]);
				this.indicator = indicator[0][0];
				this.indicatorText = indicator[0][1];
				
				if(riseEvents === true) { this.onConfigurationChange(); }
			} else {
				Sbi.warn("[ChoropletControlPanel.setIndicator] : Impossible to find indicator [" + indicatorName + "]");
			}
		}
		
		Sbi.trace("[ChoropletControlPanel.setIndicator] : OUT");
	}
	
	/**
	 * @method
	 * 
	 * @param {Array} the default values of the filters
	 */
	, setFiltersDefaultValues: function(filters) {
		if(filters){
			for(var i=0; i<filters.length; i++){
				var combo = this.form.findField(filters[i].name);
				if(combo && filters[i].value && filters[i].value!=""){
					combo.setValue(filters[i].value);
					combo.fireEvent("select");
				}
			}
		}
	}
    
	/**
	 * @method 
	 * Set a new list of indicators usable to generate the thematization
	 * 
	 * @param {Array} indicators new indicators list. each element is an array of two element:
	 * the first is the indicator name while the second one is the indicator text
	 * @param {String} indicator the name of the selected indicator. Must be equal to one of the 
	 * names of the indicators passed in as first parameter. It is optional. If not specified
	 * the first indicators of the list will be selected.
	 * @param {boolean} riseEvents true to rise an event in order to regenerate the thematization, false 
	 * otherwise. Optional. By default false.
	 */
	, setIndicators: function(indicators, indicator, riseEvents) {
		Sbi.trace("[ChoropletControlPanel.setIndicators] : IN");
		
		Sbi.trace("[ChoropletControlPanel.setIndicators] : New indicators number is equal to [" + indicators.length + "]");
		
    	this.indicators = indicators;
    	var newStore = new Ext.data.SimpleStore({
            fields: ['value', 'text'],
            data : this.indicators
        });
        this.indicatorSelectionField.bindStore(newStore);
        
        if(Ext.isArray(indicator)) indicator = indicator[0];
        indicator = indicator || indicators[0][0];
        this.setIndicator(indicator, riseEvents);
        
        Sbi.trace("[ChoropletControlPanel.setIndicators] : OUT");      
    },
	
    /**
     * Create the filter comboboxes
     * @param filters the fiters definition
     */
	setFilters: function(filters){
		if(!this.filters){
			this.filters = new Array();
		}

		if(this.setDefaultsValuesToFiltersButton){
			this.remove(setDefaultsValuesToFiltersButton, true);
		}
		
	
		//remove the old filters
		for(var i=0; i<this.filters.length; i++){
			this.remove(this.filters[i],true);
		}
		this.filters = new Array();
		
		//build the new filters
		for(var i=0; i<filters.length; i++){
			var filterDef = filters[i];
			var filter =new Ext.form.ComboBox  ({
	            fieldLabel: filterDef.header,
	            name: filterDef.name,
	            editable: false,
	            mode: 'local',
	            allowBlank: true,
	            valueField: 'val',
	            displayField: 'val',
	            emptyText: 'Select a value',
	            triggerAction: 'all',
	            store: new Ext.data.SimpleStore({
	            	fields: ['val'],
	                data : 	filterDef.values
	            }),
	            listeners: {
	                'select': {
	                    fn: function() {
	                    	this.filterDataSet();
	                    },
	                    scope: this
	                }
	            }
	        });

			this.filters.push(filter);
			this.add(filter);
		}
		
		if((Sbi.config.docLabel=="" && this.filters && this.filters.length>0)){
			this.setDefaultsValuesToFiltersButton =  new Ext.Button({
		    	text: LN('sbi.geo.analysispanel.filter.default'),
		        width: 30,
		        handler: function() {
		        	this.saveDefaultFiltersValue();
		        	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.geo.analysispanel.filter.default.ok'));
           		},
           		scope: this
			});
			this.add(this.setDefaultsValuesToFiltersButton);
			
		}
		
		this.doLayout();
		
	}
    
    /**
     * Execute the filters
     */
    , filterDataSet: function(){
    	Sbi.trace("[ChoropletControlPanel.filterDataSet] : IN");      
    	//get filter values
    	var filters = this.getFilters();
    	//filter the store
    	this.thematizer.filterStore(filters);
    	//update the thematization
    	this.thematize(false, {resetClassification: true});
    	Sbi.trace("[ChoropletControlPanel.filterDataSet] : OUT");      
    }
	
    /**
     * Gets filters values
     */
    , getFilters:function(){
    	Sbi.trace("[ChoropletControlPanel.getFilters] : IN.. Get the values of the filters"); 
    	var filters =new Array();
		if(this.filters){
			for(var i=0; i<this.filters.length; i++){
				var filter = this.filters[i];
				filters.push({
					field: filter.name,
					value: filter.getValue()
				});
			}
		}
		Sbi.trace("[ChoropletControlPanel.getFilters] : OUT");
		return filters;
    }
	
    /**
     * Save the default value of the filter in the filter as filterDefaultValue 
     */
    , saveDefaultFiltersValue:function(){
    	Sbi.trace("[ChoropletControlPanel.saveDefaultFiltersValue] : IN"); 
		if(this.filters){
			for(var i=0; i<this.filters.length; i++){
				var filter = this.filters[i];
				filter.filterDefaultValue =  filter.getValue();
			}
		}
		Sbi.trace("[ChoropletControlPanel.saveDefaultFiltersValue] : OUT");
    }
    
    //-----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
    
    /**
     * @private
     * Called by EXT when the component is rendered.
     */
    , onRender: function(ct, position) {
    	Sbi.trace("[ChoropletControlPanel.onRender] : IN");
    	
    	Sbi.geo.stat.ChoroplethControlPanel.superclass.onRender.apply(this, arguments);
       
    	if(this.thematizer == null) {
    		Sbi.trace("[ChoropletControlPanel.onRender] : thematizer ");
    		
	        var thematizerOptions = {
	           	'layer': this.layer,
	            'layerName': this.layerName,
	            'layerId' : this.geoId,
	            'loadLayerServiceName': this.loadLayerServiceName,
	            'requestSuccess': this.requestSuccess.createDelegate(this),
	            'requestFailure': this.requestFailure.createDelegate(this),
	                	
	            'format': this.format,
	            'featureSourceType': this.featureSourceType,
	            'featureSource': this.featureSource,
	            		
	            'featureSelection': this.featureSelection,
	            'nameAttribute': this.nameAttribute,
	            	        
	            'indicatorContainer': this.indicatorContainer,
	            'storeType': this.storeType,
	            'storeConfig': this.storeConfig,
	            'store': this.store,
	            'storeId' : this.businessId,
	            		       
	            'legendDiv': this.legendDiv,
	            'labelGenerator': this.labelGenerator      	
	        };
	
	        this.thematizer = new Sbi.geo.stat.ChoroplethThematizer(this.map, thematizerOptions);
    	} else {
    		Sbi.trace("[ChoropletControlPanel.onRender] : thematizer already defined");
    	}
    	
    	this.synchronizeFormState();
    	
    	
    	var targetLayer = this.thematizer.getLayer();
    	if(targetLayer != null && targetLayer.features > 0) {
    		Sbi.trace("[ChoropletControlPanel.onRender] : target layer already loaded");
    		this.ready = true;
			this.fireEvent('ready', this);
    	} else {
    		Sbi.trace("[ChoropletControlPanel.onRender] : target layer not already loaded");
    		this.thematizer.on('layerloaded', function(thematizer, layer){
    			Sbi.trace("[ChoropletControlPanel.onRender] : target layer has been just loaded");
    			if(this.ready !== true) { // do that only the first time
    				Sbi.trace("[ChoropletControlPanel.onRender] : set control panel state to ready");
    	    		this.ready = true;
    				this.fireEvent('ready', this);
    			} else {
    				Sbi.trace("[ChoropletControlPanel.onRender] : control panel state is aready set to ready");
    			}
    		}, this);
    	}
    	
    	
    	if(this.manageIndicator === true) {
	        this.thematizer.on('indicatorsChanged', function(thematizer, indicators, selectedIndicator){
				this.setIndicators(indicators, selectedIndicator, false);
			}, this);
    	}
        
        this.thematizer.on('filtersChanged', function(thematizer, filters){
			this.setFilters(filters);
		}, this);
        
        Sbi.trace("[ChoropletControlPanel.onRender] : OUT");
    }
    
    /**
     * @private
     * Called by EXT when the component is initialized.
     */
    , initComponent : function() {
    	Sbi.trace("[ChoropletControlPanel.initComponent] : IN");
    	
    	this.items = [];
    	if(this.manageIndicator === true) {
    		this.items.push(this.initIndicatorSelectionField());
    	}
    	
    	this.items.push(this.initMethodSelectionField());
    	this.items.push(this.initClassesNumberSelectionField());
    	this.items.push(this.initFromColorSelectionField());
    	this.items.push(this.initToColorSelectionField());

        Sbi.geo.stat.ChoroplethControlPanel.superclass.initComponent.apply(this);
       
        Sbi.trace("[ChoropletControlPanel.initComponent] : OUT");
    }
    
    
    /**
     * @private
     * Initialize the indicators' selection field
     */
    , initAddIndicatorsButton: function() {
    	this.addIndicatorButton = new Ext.Button({
	    	text: LN('sbi.geo.analysispanel.addindicators'),
	        width: 30,
	        handler: function() {
	        	this.showMeasureCatalogueWindow();
       		},
       		scope: this
	    });
    	
    	var panel = new Ext.Panel({
    		border: false
    		, frame: false
    		, bodyStyle: {padding: "2px 10px 10px 80px"}
    		, buttonAllign: 'center' 
    		, items: [this.addIndicatorButton]
    	});
    	
    	return panel;
    }
    
    , measureCatalogueWindow : null
    , showMeasureCatalogueWindow: function(){
		if(this.measureCatalogueWindow==null){
			var measureCatalogue = new Sbi.geo.tools.MeasureCataloguePanel();
			measureCatalogue.on('storeLoad', this.onStoreLoad, this);
			
			this.measureCatalogueWindow = new Ext.Window({
	            layout      : 'fit',
		        width		: 700,
		        height		: 350,
	            closeAction :'hide',
	            plain       : true,
	            title		: OpenLayers.Lang.translate('sbi.tools.catalogue.measures.window.title'),
	            items       : [measureCatalogue]
			});
		}
		
		this.measureCatalogueWindow.show();
	}
    
    , onStoreLoad: function(measureCatalogue, options, store, meta) {
		this.thematizer.setData(store, meta);
		this.storeType = 'virtualStore';
		var s = "";
		for(o in options) s += o + ";"
		Sbi.debug("[ControlPanel.onStoreLoad]: options.url = " + options.url);
		Sbi.debug("[ControlPanel.onStoreLoad]: options.params = " + Sbi.toSource(options.params));
		this.storeConfig = {
			url: options.url
			, params: options.params
		};
		
		alert("[ChoropletControlPanel.onStoreLoad]: options.params = " + Sbi.toSource(this.storeConfig));
		
	}
    
    
    
    
    
    /**
     * @private
     * Initialize the indicators' selection field
     */
    , initIndicatorSelectionField: function() {
    	this.indicatorSelectionField = new Ext.form.ComboBox  ({
            fieldLabel: LN('sbi.geo.analysispanel.indicator'),
            name: 'indicator',
            editable: false,
            valueField: 'value',
            displayField: 'text',
            mode: 'local',
            emptyText: LN('sbi.geo.analysispanel.emptytext'),
            valueNotFoundText: LN('sbi.geo.analysispanel.emptytext'),
            triggerAction: 'all',
            store: new Ext.data.SimpleStore({
                fields: ['value', 'text'],
                data : this.indicators
            }),
            listeners: {
                'select': {
                    fn: function() {
                    	// this.thematize(false);
                    },
                    scope: this
                }
            }
        });
    	
    	return this.indicatorSelectionField;
    }
    
    /**
     * @private
     * Initialize the method's selection field
     */
    , initMethodSelectionField: function() {
    	this.methodSelectionField = new Ext.form.ComboBox  ({
            xtype: 'combo',
            fieldLabel: LN('sbi.geo.analysispanel.method'),
            name: 'method',
            hiddenName: 'method',
            editable: false,
            valueField: 'value',
            displayField: 'text',
            mode: 'local',
            emptyText: 'Select a method',
            triggerAction: 'all',
            store: new Ext.data.SimpleStore({
                fields: ['value', 'text'],
                data : [['CLASSIFY_BY_EQUAL_INTERVALS', 'Equal Intervals'],
                        ['CLASSIFY_BY_QUANTILS', 'Quantils']]
            }),
            listeners: {
                'select': {
                	fn: function() {
                    	//this.thematize(false);
                    },
                    scope: this
                }
            }
        });
    	
    	return this.methodSelectionField;
    }
    
    /**
     * @private
     * Initialize the classes number's selection field
     */
    , initClassesNumberSelectionField: function() {
    	this.classesNumberSelectionField = new Ext.form.ComboBox  ({
            xtype: 'combo',
            fieldLabel: LN('sbi.geo.analysispanel.classes'),
            name: 'numClasses',
            editable: false,
            valueField: 'value',
            displayField: 'value',
            mode: 'local',
            value: 5,
            triggerAction: 'all',
            store: new Ext.data.SimpleStore({
                fields: ['value'],
                data: [[0], [1], [2], [3], [4], [5], [6], [7]]
            }),
            listeners: {
                'select': {
                	fn: function() {
                    	// this.thematize(false);
                    },
                    scope: this
                }
            }
        });
    	
    	return this.classesNumberSelectionField;
    }
    
    /**
     * @private
     * Initialize the method's selection field
     */
    , initFromColorSelectionField: function() {
    	this.fromColorSelectionField = new Ext.ux.ColorField({
            fieldLabel: LN('sbi.geo.analysispanel.fromcolor'),
            name: 'colorA',
            width: 100,
            allowBlank: false,
            value: "#FFFF00",
            listeners: {
                'select': {
                    fn: function() {
                    	//this.thematize(false);
                    },
                    scope: this
                }
            }
        });
    	return this.fromColorSelectionField;
    }
    
    /**
     * @private
     * Initialize the method's selection field
     */
    , initToColorSelectionField: function() {
    	this.toColorSelectionField = new Ext.ux.ColorField({
            xtype: 'colorfield',
            fieldLabel: LN('sbi.geo.analysispanel.tocolor'),
            name: 'colorB',
            width: 100,
            allowBlank: false,
            value: "#FF0000",
            listeners: {
                'select': {
                    fn: function() {
                    	//this.thematize(false);
                    },
                    scope: this
                }
            }
        });
    	
    	return this.toColorSelectionField;
    }
    
    //-----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , onConfigurationChange: function() {
    	//alert("Classification change");
    	this.thematize(false);
    }
    
});


Ext.reg('choropleth', Sbi.geo.stat.ChoroplethControlPanel);
