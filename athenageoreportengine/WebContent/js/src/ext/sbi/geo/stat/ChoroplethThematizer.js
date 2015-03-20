/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/
 

Ext.ns("Sbi.geo.stat");

Sbi.geo.stat.ChoroplethThematizer = function(map, config) {
	Sbi.trace("[ChoroplethThematizer.constructor] : IN");
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	// constructor
	Sbi.geo.stat.ChoroplethThematizer.superclass.constructor.call(this, map, config);
	Sbi.trace("[ChoroplethThematizer.constructor] : OUT");
};

/**
 * @class Sbi.geo.stat.ChoroplethThematizer
 * @extends Sbi.geo.stat.Thematizer
 * 
 * Use this class to create choropleths on a map.
 */

/**
 * @cfg {Object} config
 * ...
 */

Ext.extend(Sbi.geo.stat.ChoroplethThematizer, Sbi.geo.stat.Thematizer, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	
	/**
	 * @property {Sbi.geo.stat.Classification} classification
	 * Defines the different classification to use
	 */
    classification: null

    /**
	 * @property {Array(<Sbi.geo.utils.ColorRgb>}} colors
	 * Array of 2 colors to be applied to features
	 */
    , colors: [
        new Sbi.geo.utils.ColorRgb([120, 120, 0]),
        new Sbi.geo.utils.ColorRgb([255, 0, 0])
    ]

    /**
     * APIProperty: method
     * {Integer} Specifies the distribution method to use. Possible
     *      values are:
     *      Sbi.geo.stat.Classifier.CLASSIFY_BY_QUANTILS and
     *      Sbi.geo.stat.Classifier.CLASSIFY_BY_EQUAL_INTERVALS
     */
    , method: Sbi.geo.stat.Classifier.CLASSIFY_BY_QUANTILS

    /**
     * APIProperty: numClasses
     * {Integer} Number of classes
     */
    , numClasses: 5

    /**
     * Property: defaultSymbolizer
     * {Object} Overrides defaultSymbolizer in the parent class
     */
    , defaultSymbolizer: {'fillOpacity': 1}

    /**
     * Property: colorInterpolation
     * {Array({<mapfish.Color>})} Array of {<mapfish.Color} resulting from the
     *      RGB color interpolation
     */
    , colorInterpolation: null
    
    , thematyzerType: "choropleth"

    // =================================================================================================================
	// METHODS
	// =================================================================================================================
    
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , initialize: function(map, options) {
    	Sbi.geo.stat.ChoroplethThematizer.superclass.initialize.call(this, map, options);
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
    /**
     * @method
     * 
     * the analysis conf. this object can be passed as is at the method setFormState of the related
     * control panel
     */
    , getAnalysisConf: function() {
    	var thematizerOption = this.getOptions();
		
		var formState = {};
		
		for(var method in Sbi.geo.stat.Classifier) {
			if(Sbi.geo.stat.Classifier[method] == thematizerOption.method) {
				formState.method = method;
			}
		}
		
		formState.classes = thematizerOption.numClasses;
		if(thematizerOption.colors && thematizerOption.colors.length > 1) {
			if(thematizerOption.colors[0]) {
				formState.fromColor = thematizerOption.colors[0].toHexString();
			}
			if(thematizerOption.colors[1]) {
				formState.toColor = thematizerOption.colors[1].toHexString();
			}
		}
		
		
		if(thematizerOption.indicator) {
			formState.indicator = thematizerOption.indicator;
			Sbi.trace("[ChoroplethThematizer.getAnalysisConf] : indicator is equal to [" + formState.indicator + "]");
		}
		
		return formState;
    }
    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    
  
    /**
     * @method 
     * Thematize the map using the classification
     *
     * @param {Object} options object with a single {Boolean} property: resetClassification.
     */
    , thematize: function(options) {
           
    	Sbi.trace("[ChoroplethThematizer.thematize] : IN");
    	
    	this.showMask("Buiding thematization ...");
    	   
    	if (options) {
    		if(options.resetClassification) {
    			this.classify();
    		} else {
    			this.updateOptions(options);
    		}
        }
    	  
    	Sbi.trace("[ChoroplethThematizer.thematize] : Checking if the thematizer is ready ...");
    	if(this.indicatorContainer == "store" && this.isThematizerReady() == false) {
    		Sbi.debug("[ChoroplethThematizer.thematize] : thematizatoin aborted because the thematizer is not ready");
    		Sbi.trace("[ChoroplethThematizer.thematize] : OUT");
    		return;
    	}
    	
    	Sbi.trace("[ChoroplethThematizer.thematize] : Thematizer is ready so we can go on with thematization");
        
    	
    	var bins = this.classification.getBins(); 
    	var filters = new Array(bins.length);
    	var rules = new Array(bins.length);
        for (var i = 0; i < bins.length; i++) {
        	
        	filters[i] = this.createClassFilter(bins[i], i);
            var rule = new OpenLayers.Rule({
                symbolizer: {fillColor: this.colorInterpolation[i].toHexString()},
                filter: filters[i]
            });
            rules[i] = rule;
        }
	    	
	
        this.extendStyle(rules);
        
        Sbi.geo.stat.ChoroplethThematizer.superclass.thematize.call(this, arguments);
        
        for (var i = 0; i < rules.length; i++) {
        	Sbi.trace("[ChoroplethThematizer.thematize] : Features thematized succesfully for class [" + i + "] are [" + filters[i].filteredFeatures + "] on [" + filters[i].dataPoints.length +"] expected");
        }
        
        this.hideMask();
        
        Sbi.trace("[ChoroplethThematizer.thematize] : OUT");
    }
    
    /**
     * @method
     * Create a feature filter that returns only features belonging to the specified class
     * 
     * @param {Sbi.geo.stat.Bin} bin the class's bin
     * @param {Integer} the class index
     */
    , createClassFilter: function(bin, binIndex) {
    	Sbi.trace("[ChoroplethThematizer.createClassFilter] : IN");
    	 
    	var filter = new OpenLayers.Filter.Function({
        	evaluate: function(attributes) { 
        		
        		this.invoked = true;
    	        for(var j = 0; j < this.dataPoints.length; j++) {
    	        	if(this.dataPoints[j].coordinatesAreEqualTo([attributes[this.layerId]])) {
    	        		Sbi.debug("[ChoroplethThematizer.createClassFilter]: Feature [" + attributes[this.layerId]+ "] belong to class [" + binIndex + "]");
    	        		this.filteredFeatures++;
    	        		return true;
    	        	} 
    	        }
    	        
    	        if(attributes[this.layerId] == undefined || attributes[this.layerId] == null) {
    	        	var s = "";
    	    		for(a in attributes) s += a + ";";
        			Sbi.trace("[Filter(" + this.binIndex + ").evaluate] :  feature does not contains attribute [" + this.layerId+ "]. Available attributes are [" + s + "]");
        		} else {
        			//Sbi.trace("[Filter(" + this.binIndex + ").evaluate] :  feature whose attribute [" + this.layerId + "] is equal to [" + attributes[this.layerId] + "] do not belong to this class");
        		}
    	        
    	        return false;
    	    }
    	});
    	filter.filteredFeatures = 0;
    	filter.layerId = this.layerId;
    	filter.binIndex = binIndex;
    	filter.dataPoints = bin.dataPoints;
    	filter.invoked = false;
    	
    	Sbi.trace("[ChoroplethThematizer.createClassFilter] : OUT");
    	
    	return filter;
    }
    
    /**
     * @method
     * Creates the classification that will be used for map thematization
     */
    , classify: function() {
    	Sbi.trace("[ChoroplethThematizer.classify] : IN");
    	
    	if(this.indicatorContainer != 'store' 
    		|| (this.indicatorContainer == "store" && this.isStoreReady() == true)) {
        
		    	var distribution = this.getDistribution(this.indicator);
		    	Sbi.debug("[ChoroplethThematizer.setClassification] : Extracted [" + distribution.getSize() + "] values for indicator [" + this.indicator + "]");
		        
		        var classificationOptions = {
		            'labelGenerator' : this.options.labelGenerator
		        };
		        
		        var classifier = new Sbi.geo.stat.Classifier({distribution: distribution, classificationOptions: classificationOptions});
		        this.classification = classifier.classify(
		            this.method,
		            this.numClasses,
		            null
		        );
		        this.createColorInterpolation();
    	} else {
    		Sbi.trace("[ChoroplethThematizer.classify] : classification not performed because the store is not ready");
    	}
        
        Sbi.trace("[ChoroplethThematizer.classify] : OUT");
    }
    
    /**
     * @method
     * @deperecated use #classify instead
     */
    , setClassification: function() {
    	Sbi.trace("[ChoroplethThematizer.setClassification] : IN");
    	Sbi.warn("[ChoroplethThematizer.setClassification] : Method [setClassification] is deprecated. Use method [classify] instead");
    	this.classify();
    	Sbi.trace("[ChoroplethThematizer.setClassification] : IN");
    }
    
    
    
    /**
     * @method
     * Method used to update the properties 
     * 	- indicator, 
     *  - method, 
     *  - numClasses,
     *  - colors
     *
     * @param {Object} options object
     */
    , updateOptions: function(newOptions) {
    	Sbi.trace("[ChoroplethThematizer.updateOptions] : IN");
        var oldOptions = Ext.apply({}, this.options);
        
        Sbi.debug("[ChoroplethThematizer.updateOptions]: Old options are equal to [" + Sbi.toSource(oldOptions, true)+ "]");
        Sbi.debug("[ChoroplethThematizer.updateOptions]: New options are equal to [" + Sbi.toSource(newOptions, true)+ "]");
        
        this.setOptions(newOptions);
        if (newOptions) {
        	
        	Sbi.debug("[ChoroplethThematizer.updateOptions]: " +
        	 		"newOptions.method [" + newOptions.method + "] " +
        	 				"- oldOptions.method [" + oldOptions.method+ "]");
        	 
        	Sbi.debug("[ChoroplethThematizer.updateOptions]: " +
         	 		" newOptions.numClasses [" +  newOptions.numClasses + "] " +
         	 				"- oldOptions.numClasses [" + oldOptions.numClasses + "]");
        	 
        	Sbi.debug("[ChoroplethThematizer.updateOptions]: " +
         	 		"newOptions.indicator [" + newOptions.indicator + "] " +
         	 				"- oldOptions.indicator [" + oldOptions.indicator+ "]");
        	 
        	if(newOptions.colors !== undefined && newOptions.colors !== null) {
//        		if(newOptions.colors.length > 0) {
//        			Sbi.debug("[ChoroplethThematizer.updateOptions]: " +
//                 	 		"newOptions.colors[0] [" + newOptions.colors[0] + "] " +
//                 	 				"- oldOptions.colors[0] [" + oldOptions.colors[0]+ "]");
//        		}
//        		
//        		if(newOptions.colors.length > 1) {
//        			Sbi.debug("[ChoroplethThematizer.updateOptions]: " +
//                 	 		"newOptions.colors[1] [" + newOptions.colors[1] + "] " +
//                 	 				"- oldOptions.colors[1] [" + oldOptions.colors[1]+ "]");
//        		}
        	}
        	
            if (newOptions.method != oldOptions.method ||
                newOptions.indicator != oldOptions.indicator ||
                newOptions.numClasses != oldOptions.numClasses) {
            	 Sbi.debug("[ChoroplethThematizer.updateOptions] : An option related to classification has changed");
                this.setClassification();
            } else if (newOptions.colors && (
                       !newOptions.colors[0].equals(oldOptions.colors[0]) ||
                       !newOptions.colors[1].equals(oldOptions.colors[1]))) {
            	Sbi.debug("[ChoroplethThematizer.updateOptions] : No option related to classifcation has changed");
                this.createColorInterpolation();
            } else {
            	Sbi.trace("[ChoroplethThematizer.updateOptions] : No option has changed");
            }
        }
        Sbi.trace("[ChoroplethThematizer.updateOptions] : OUT");
    }  


    /**
     * @method
     * 
     * Generates color interpolation in regard to classification
     */
    , createColorInterpolation: function() {
        var initialColors = this.colors;
        var numColors = this.classification.bins.length;
        this.colorInterpolation =
        	Sbi.geo.utils.ColorRgb.getColorsArrayByRgbInterpolation(
                initialColors[0], initialColors[1], numColors
            );
    }

    /**
     * @method
     * 
     * Update the legendDiv content with new bins label
     */
    , updateLegend: function() {
    	
    	Sbi.trace("[ChoroplethThematizer.updateLegend] : IN");
    	 
    	if (!this.legendDiv) {
    		Sbi.trace("[ChoroplethThematizer.updateLegend] : legend div not defined");
    		Sbi.trace("[ChoroplethThematizer.updateLegend] : OUT");
            return;
        }

        // TODO use css classes instead
        this.legendDiv.update("");       
        
        for (var i = 0; i < this.classification.bins.length; i++) {
            var element = document.createElement("div");
            element.style.backgroundColor = this.colorInterpolation[i].toHexString();
            element.style.width = "30px";
            element.style.height = "15px";
            element.style.cssFloat = "left";
            element.style.marginRight = "10px";
            this.legendDiv.appendChild(element);

            var element = document.createElement("div");
            element.innerHTML = this.classification.bins[i].label;
            this.legendDiv.appendChild(element);

            var element = document.createElement("div");
            element.style.clear = "left";
            this.legendDiv.appendChild(element);
        }
        
        Sbi.trace("[ChoroplethThematizer.updateLegend] : OUT");
    }
});

Sbi.geo.stat.Thematizer.addSupportedType("choropleth", Sbi.geo.stat.ChoroplethThematizer, Sbi.geo.stat.ChoroplethControlPanel);
