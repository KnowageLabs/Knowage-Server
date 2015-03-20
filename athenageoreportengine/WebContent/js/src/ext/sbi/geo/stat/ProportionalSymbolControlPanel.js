/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/

Ext.ns("Sbi.geo.stat");

/**
 * The ProportionalSymbol class create a widget allowing to set up and display proportional symbol
 * thematization on a map.
 * 
 * ## Further Reading
 * 
 * The proportional symbol thematization technique uses symbols of different sizes to represent data associated 
 * with different areas or locations within the map. For example, a disc may be shown at the location 
 * of each city in a map, with the area of the disc being proportional to the population of the city.
 *
 * @author Andrea Gioia
 */
Sbi.geo.stat.ProportionalSymbolControlPanel = Ext.extend(Ext.FormPanel, {

	/**
	 * @property {OpenLayers.Layer.Vector} layer
	 * The vector layer containing the features that
	 * are styled based on statistical values. If none is provided, one will
	 * be created.
	 */
    layer: null

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
	 * The indicator currently chosen
	 */
    , indicator: null
    
    /**
     * @property {String} indicator
     * The raw value of the currently chosen indicator (ie. human readable)
     */
    , indicatorText: null

    /**
     * @property {Sbi.geo.stat.ProportionalSymbolThematizer} thematizer
     * The core thematizer object.
     */
    , thematizer: null
    
    /**
     * @property {boolean} thematizationApplied
     * true if the thematization was applied
     */
    , thematizationApplied: false
   
    /**
     * @property {Boolean} ready
     * true if the widget is ready to accept user commands.
     */
    , ready: false

    /**
     * @property {Boolean} border
     * Styling border
     */
    , border: false
    

    // =================================================================================================================
	// METHODS
	// =================================================================================================================
    
    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    /**
     * Method: classify
     *    Reads the features to get the different value for
     *    the field given for indicator
     *    Creates a new Distribution and related Classification
     *    Then creates an new ProportionalSymbols and applies classification
     */
    , thematize: function(exception, additionaOptions) {
    	
    	Sbi.trace("[ProportionalSymbolControlPanel.thematize] : IN");
    	
    	var doThematization = true;
    	
        if (!this.ready) {
            if (exception) {
                Ext.MessageBox.alert('Error', 'Component init not complete');
            }
            return;
        }
        
        var options = this.getThemathizerOptions();
        
        if (!options.indicator) {
            if (exception === true) {
                Ext.MessageBox.alert('Error', 'You must choose an indicator');
            }
            doThematization = false;
        }
        
        options = Ext.apply(options, additionaOptions||{});
        
        if(doThematization) {
        	this.thematizer.thematize(options);
            this.thematizationApplied = true;
        } else {
        	this.thematizer.setOptions(options);
        } 
        
        Sbi.trace("[ProportionalSymbolControlPanel.thematize] : IN");
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
    
	, getThemathizerOptions: function() {
		Sbi.trace("[ProportionalSymbolControlPanel.getThemathizerOptions] : IN");
		var formState = this.getFormState();
		var options = {};
		if(this.manageIndicator === true) {
			options.indicator = formState.indicator;
		}
		options.minRadiusSize = formState.minRadiusSize;
		options.maxRadiusSize = formState.maxRadiusSize;
		
		Sbi.trace("[ProportionalSymbolControlPanel.getThemathizerOptions] : OUT");
		
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
		
		Sbi.trace("[ProportionalSymbolControlPanel.syncronizeFormState] : new form state is equal to [" + Sbi.toSource(formState) + "]");
		
		this.setMaxRadiusSize(formState.maxRadiusSize);
		this.setMinRadiusSize(formState.minRadiusSize);
		if(this.manageIndicator === true) {
			this.setIndicator(formState.indicator);
		}
		
		Sbi.trace("[ProportionalSymbolControlPanel.syncronizeFormState] : OUT");
	}
	
	, getFormState: function() {
		var formState = {};
		
		if(this.manageIndicator === true) {
			formState.indicator = this.getIndicator();
		}
		formState.minRadiusSize = this.getMinRadiusSize();
		formState.maxRadiusSize = this.getMaxRadiusSize();
		
		return formState;
	}
	
	, setFormState: function(formState, riseEvent) {
		Sbi.trace("[ProportionalSymbolControlPanel.setFormState] : IN");
	
		if(this.manageIndicator === true) {
			this.setIndicator(formState.indicator);
		}
		this.setMinRadiusSize(formState.minRadiusSize);
		this.setMaxRadiusSize(formState.maxRadiusSize);
		this.setFiltersDefaultValues(formState.filtersDefaultValues);
		if(riseEvent === true) { this.onConfigurationChange(); }
		
		Sbi.trace("[ProportionalSymbolControlPanel.setFormState] : OUT");
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected classification indicator
	 */
	, getIndicator: function() {
		return (this.manageIndicator ===  true)?this.form.findField('indicator').getValue(): undefined;
	}
	
	/**
	 * @method
	 * 
	 * @param {String} the indicator to set
	 */
	, setIndicator: function(indicator) {
		if(this.manageIndicator === true) {
			this.form.findField('indicator').setValue(indicator);
		}
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected classification min radius size
	 */
	, getMinRadiusSize: function() {
		return this.form.findField('minSize').getValue();
	}
	
	/**
	 * @method
	 * 
	 * @param {Number} the min radius size to set
	 */
	, setMinRadiusSize: function(minRadiusSize) {
		this.form.findField('minSize').setValue(minRadiusSize);
	}
	
	/**
	 * @method
	 * 
	 * @return {String} the selected classification max radius size
	 */
	, getMaxRadiusSize: function() {
		return this.form.findField('maxSize').getValue();
	}
	
	/**
	 * @method
	 * 
	 * @param {Number} the max radius size to set
	 */
	, setMaxRadiusSize: function(maxRadiusSize) {
		this.form.findField('maxSize').setValue(maxRadiusSize);
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

        Sbi.trace("[ChoropletControlPanel.setIndicators] : OUT");      
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
     * Create the filter comboboxes
     * @param filters the fiters definition
     */
	, setFilters: function(filters){
		Sbi.trace("[ChoropletControlPanel.setFilters] : IN");
		
		Sbi.trace("[ChoropletControlPanel.setFilters] : New filters number is equal to [" + filters.length + "]");

        
        Sbi.trace("[ChoropletControlPanel.setFilters] : OUT");      
	}
    
    // -----------------------------------------------------------------------------------------------------------------
    // init method
	// -----------------------------------------------------------------------------------------------------------------
    
	  /**
     * Method: onRender
     * Called by EXT when the component is rendered.
     */
    , onRender: function(ct, position) {
    	Sbi.geo.stat.ProportionalSymbolControlPanel.superclass.onRender.apply(this, arguments);
        
    	if(this.thematizer == null) {
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

    	   this.thematizer = new Sbi.geo.stat.ProportionalSymbolThematizer(this.map, thematizerOptions);
    	}
    	
    	this.synchronizeFormState();
    	
    	if(this.thematizer.getLayer != null) {
    		this.ready = true;
			this.fireEvent('ready', this);
    	} else {
    		this.thematizer.on('layerloaded', function(thematizer, layer){
    			if(this.ready !== true) { // do that only the first time
    	    		this.ready = true;
    				this.fireEvent('ready', this);
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
     
    }
	
    /**
     * @private
     * Called by EXT when the component is initialized.
     */
    , initComponent : function() {
        this.items = [];
        
        if(this.manageIndicator === true) {
        	this.items.push(this.initIndicatorSelectionField());
        }
        this.items.push(this.initMinRadiusSizeField());
        this.items.push(this.initMaxRadiusSizeField());
        
         
//        this.buttons = [{
//            text: 'OK',
//            handler: this.thematize,
//            scope: this
//        }];
        Sbi.geo.stat.ProportionalSymbolControlPanel.superclass.initComponent.apply(this);
    }

    /**
     * @private
     * Initialize the indicators' selection field
     */
    , initIndicatorSelectionField: function() {
    	this.indicatorSelectionField = new Ext.form.ComboBox({
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
                    	this.thematize(false);
                    },
                    scope: this
                }
            }
    	});
    	
    	return this.indicatorSelectionField;
    }
        
    /**
     * @private
     * Initialize the minRadiusSize' selection field
     */
    , initMinRadiusSizeField: function() {
    	this.minRadiuSize = new Ext.form.NumberField({
    		fieldLabel:'Min Size',
            name: 'minSize',
            width: 30,
            value: 2,
            maxValue: 20
    	});
    	
    	return this.minRadiuSize;
    }
    
    /**
     * @private
     * Initialize the minRadiusSize' selection field
     */
    , initMaxRadiusSizeField: function() {
    	this.maxRadiuSize = new Ext.form.NumberField({
             fieldLabel:'Max Size',
             name: 'maxSize',
             width: 30,
             value: 20,
             maxValue: 50
    	});
    	
    	return this.maxRadiuSize;
    }
 
    //-----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , onConfigurationChange: function() {
    	//alert("Classification change");
    	this.thematize(false);
    }
});
Ext.reg('proportionalsymbol', Sbi.geo.stat.ProportionalSymbolControlPanel);
