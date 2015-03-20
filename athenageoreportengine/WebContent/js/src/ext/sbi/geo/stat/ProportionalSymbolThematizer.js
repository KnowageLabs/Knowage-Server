/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/

Ext.ns("Sbi.geo.stat");


Sbi.geo.stat.ProportionalSymbolThematizer = function(map, config) {
	Sbi.trace("[ProportionalSymbolThematizer.constructor] : IN");
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	// constructor
	Sbi.geo.stat.ProportionalSymbolThematizer.superclass.constructor.call(this, map, config);
	Sbi.trace("[ProportionalSymbolThematizer.constructor] : OUT");
};

/**
 * @class Sbi.geo.stat.ProportionalSymbolThematizer
 * @extends Sbi.geo.stat.Thematizer
 * 
 * Use this class to create proportional symbols on a map.
 */
Ext.extend(Sbi.geo.stat.ProportionalSymbolThematizer, Sbi.geo.stat.Thematizer, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
    
	/**
	 * @property {Sbi.geo.stat.Classification} classification
	 * Defines the different classification to use
	 */
    classification: null
    
    /**
     * @property {Integer} minRadiusSize
     * The minimum radius size
     */
    , minRadiusSize: 2

    /**
     * APIProperty: maxRadiusSize
     * {Integer} The maximum radius size
     */
    , maxRadiusSize: 20
    
    , thematyzerType: "proportionalSymbols"


    // =================================================================================================================
	// METHODS
	// =================================================================================================================
    
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
   

    , initialize: function(map, options) {
    	Sbi.geo.stat.ProportionalSymbolThematizer.superclass.initialize.call(this, map, options);
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    
    /**
     * @method 
     * This function loops over the layer's features and applies already given classification.
     *
     * @param {Object} options object with a single {Boolean} property: resetClassification.
     */
    , thematize: function(options) {
        
    	Sbi.trace("[ProportionalSymbolThematizer.thematize] : IN");
    	
    	Sbi.trace("[ProportionalSymbolThematizer.thematize] : layer [" + this.layerName + "]");
    	if(this.layer){
    		Sbi.trace("[ProportionalSymbolThematizer.thematize] : layer contains [" + this.layer.features.length + "] feature");
    	}
    	
    	if (options) {
    		if(options.resetClassification) {
    			this.classify();
    		} else {
    			this.updateOptions(options);
    		}
        } else {
        	Sbi.trace("[ProportionalSymbolThematizer.thematize] : thematizer option not defined");
        }
    	
    	Sbi.trace("[ProportionalSymbolThematizer.thematize] : Checking if the thematizer is ready ...");
    	if(this.indicatorContainer == "store" && this.isThematizerReady() == false) {
    		Sbi.debug("[ProportionalSymbolThematizer.thematize] : thematizatoin aborted because the store is not ready");
    		Sbi.trace("[ProportionalSymbolThematizer.thematize] : OUT");
    		return;
    	}
    	
    	Sbi.trace("[ProportionalSymbolThematizer.thematize] : Thematizer is ready so we can go on with thematization");
        
        var calculateRadius = OpenLayers.Function.bind(
            function(feature) {
            	var size;
            	
            	var featureId = feature.attributes[this.layerId];
            	if(featureId == undefined || featureId == null) {
            		Sbi.warn("[ProportionalSymbolThematizer.thematize] :Impossible to extract [" + this.layerId + "] form feature");
            		return 0;
            	}
            	var dataPoint = this.distribution.getDataPoint([ feature.attributes[this.layerId] ]);
            	if(dataPoint) {
            		 var value = dataPoint.getValue();
                     var minValue = this.distribution.getMinDataPoint().getValue();
                     var maxValue = this.distribution.getMaxDataPoint().getValue();
                     
                     if(minValue == maxValue) { // we have only one point in the distribution
                    	 size = (this.maxRadiusSize + this.minRadiusSize)/2;
                     } else {
                    	 size = (value - minValue) / ( maxValue - minValue) *
                         (this.maxRadiusSize - this.minRadiusSize) + this.minRadiusSize;
                     }
                     
                     
                    
                     
                     Sbi.trace("[ProportionalSymbolThematizer.calculateRadius] : radius for feature [" + feature.attributes[this.layerId] + "]is equal to [" + size + "]");
            	} else {
            		size = 0;
            	}
               
                return size;
            }, this
        );
        
        this.extendStyle(null,
            {'pointRadius': '${calculateRadius}'},
            {'calculateRadius': calculateRadius}
        );
        
        Sbi.geo.stat.Thematizer.prototype.thematize.apply(this, arguments);
        
        Sbi.trace("[ProportionalSymbolThematizer.thematize] : OUT");
    }
    
    
    /**
     * @method
     * Creates the classification that will be used for map thematization
     */  
    , classify: function() {
    	Sbi.trace("[ProportionalSymbolThematizer.classify] : IN");
        
    	if(this.indicatorContainer != 'store' 
    		|| (this.indicatorContainer == "store" && this.isStoreReady() == true)) {
    		this.distribution = this.getDistribution(this.indicator);
        	Sbi.debug("[ProportionalSymbolThematizer.setClassification] : Extracted [" + this.distribution.getSize() + "] values for indicator [" + this.indicator + "]");
    	} else {
    		Sbi.trace("[ProportionalSymbolThematizer.classify] : classification not performed because the store is not ready");
    	}
    	   
        Sbi.trace("[ProportionalSymbolThematizer.classify] : OUT");
    }
    
    /**
     * @method
     * @deperecated use #classify instead
     */
    , setClassification: function() {
    	Sbi.trace("[ProportionalSymbolThematizer.setClassification] : IN");
    	Sbi.warn("[ProportionalSymbolThematizer.setClassification] : Method [setClassification] is deprecated. Use method [classify] instead");
    	this.classify();
    	Sbi.trace("[ProportionalSymbolThematizer.setClassification] : IN");
    }

   
    /**
     * @method
     * Method used to update the properties 
     * 	- indicator, 
     *  - minRadiusSize, 
     *  - maxRadiusSize.
     *
     * @param {Object} options object
     */
    , updateOptions: function(newOptions) {
    	Sbi.trace("[ProportionalSymbolThematizer.updateOptions] : IN");
    	var oldOptions = Ext.apply({}, this.options);
        this.setOptions(newOptions);
        if(newOptions) {
        	 if (newOptions.indicator != oldOptions.indicator
        			 || newOptions.minRadiusSize != newOptions.minRadiusSize
        			 || newOptions.maxRadiusSize != newOptions.maxRadiusSize) {
        		 this.classify();
             }
        }
       
        Sbi.trace("[ProportionalSymbolThematizer.updateOptions] : OUT");
    }    
    
    /**
     * Method: updateLegend
     *    Update the legendDiv content with new bins label
     */
    , updateLegend: function() {
    	Sbi.trace("[ProportionalSymbolThematizer.updateLegend] : IN");
    	
        if (!this.legendDiv) {
        	Sbi.trace("[ProportionalSymbolThematizer.updateLegend] : legend div not defined");
    		Sbi.trace("[ProportionalSymbolThematizer.updateLegend] : OUT");
            return;
        }
        
        this.legendDiv.update(""); 
        var element = document.createElement("div");
        element.innerHTML = "The dimension of each circle's</br>" +
        					"radius is directly proportional</br>" +
        					"to the measure od the indicator</br>";
        this.legendDiv.appendChild(element);
        
        var element = document.createElement("div");
        element.style.clear = "left";
        this.legendDiv.appendChild(element);


        Sbi.trace("[ProportionalSymbolThematizer.updateLegend] : OUT");
    }
    /**
     * @method
     * 
     * the analysis conf. this object can be passed as is at the method setFormState of the related
     * control panel
     */
    , getAnalysisConf: function() {
    	var thematizerOption = this.getOptions();
		
		var formState = {};
		
		formState.minRadiusSize = thematizerOption.minRadiusSize || 2;
		formState.maxRadiusSize = thematizerOption.maxRadiusSize || 20;
		if(thematizerOption.indicator) {
			formState.indicator = thematizerOption.indicator;
			Sbi.trace("[ProportionalSymbolThematizer.getAnalysisConf] : indicator is equal to [" + formState.indicator + "]");
		}
	
		
		return formState;
    }
    , setLayer: function(layer, format) {
   	 	Sbi.trace("[ProportionalSymbolThematizer.setLayer] : IN");
   	
   	 	Sbi.debug("[ProportionalSymbolThematizer.setLayer] : Input parameter layer is of type [" + (typeof layer) + "]");
   	  
	    var format = format || this.format || new OpenLayers.Format.GeoJSON();
	    Sbi.debug("[Thematizer.setLayer] : Layer formt is equal to  [" + format + "]");
	     
	    var features = format.read(layer);
	    newFeatures = features;
	     
	    var newFeatures = new Array();	     
	    for(var i = 0; i < features.length; i++) {
	    	var f =  features[i];
	    	var centroid = f.geometry.getCentroid();
	    	
	    	var newFeature = new OpenLayers.Feature.Vector( centroid, f.attributes, f.style);
	    	newFeatures.push(newFeature);
	    	Sbi.debug("[ProportionalSymbolThematizer.setLayer] : centroid [" + i + "] equals to [" + centroid.x + "," + centroid.y + "]");
	    }
	     
	    this.setFeatures(newFeatures);

		Sbi.trace("[ProportionalSymbolThematizer.setLayer] : OUT");
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
        var doc = response.responseXML;
        if (!doc || !doc.documentElement) {
            doc = response.responseText;
        }
        
        var format = this.format || new OpenLayers.Format.GeoJSON();
        
        var features = format.read(doc);
        var newFeatures = features;
        
	    var newFeatures = new Array();
	    for(var i = 0; i < features.length; i++) {
	    	var f =  features[i];
	    	var centroid = f.geometry.getCentroid();
	    	var newFeature = new OpenLayers.Feature.Vector( centroid, f.attributes, f.style);
	    	newFeatures.push(newFeature);
	    	Sbi.debug("[Thematizer.setLayer] : centroid [" + i + "] equals to [" + centroid.x + "," + centroid.y + "]");
	    }
	     
        this.layer.removeAllFeatures();
        this.layer.addFeatures( newFeatures );
        //this.requestSuccess(response);
        
        this.hideMask();
        
        this.fireEvent('layerloaded', this, this.layer);
        Sbi.trace("[Thematizer.onSuccess]: event [layerloaded] fired [" + this.layer.features.length+ "]");
        
        Sbi.trace("[Thematizer.onSuccess]: OUT");
    }    
});

Sbi.geo.stat.Thematizer.addSupportedType("proportionalSymbols", Sbi.geo.stat.ProportionalSymbolThematizer, Sbi.geo.stat.ProportionalSymbolControlPanel);
