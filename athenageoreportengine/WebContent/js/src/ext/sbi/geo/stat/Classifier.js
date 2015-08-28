/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/

Ext.ns("Sbi.geo.stat");


Sbi.geo.stat.Classifier = function(config) {
	
	config = config || {};
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	this.initialize(config.distribution, config.classificationOptions);

	Sbi.geo.stat.Classifier.superclass.constructor.call(this, config);
};
	
Sbi.geo.stat.Classifier.CLASSIFY_WITH_BOUNDS = 0;
Sbi.geo.stat.Classifier.CLASSIFY_BY_EQUAL_INTERVALS = 1;
Sbi.geo.stat.Classifier.CLASSIFY_BY_QUANTILS =  2;

/**
 * @class Sbi.geo.stat.Classifier
 * @extends Ext.util.Observable
 * 
 * Classifier class
 */
Ext.extend(Sbi.geo.stat.Classifier, Ext.util.Observable, {
	
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
	 * @property {Sbi.geo.stat.Distribution} distribution
	 */
	distribution: null

    /**
	 * @property {String} nbVal
	 * number of  value 
	 */
    , nbVal: null

    /**
	 * @property {String} minVal
	 * max value 
	 */
    , minVal: null

    /**
	 * @property {String} maxVal
	 * min value 
	 */
    , maxVal: null
    
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
		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Called by the constructor to initialize this object
	 * 
	 * @param {OpenLayers.Map} distribution distribution of the indicator
	 * @param {Object} options of extra options
	 */
    , initialize: function(distribution, options) {
        //OpenLayers.Util.extend(this, distribution);
        this.setDistribution(distribution);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , setDistribution: function(distribution) {
    	this.distribution = distribution;
        this.nbVal = this.distribution.getSize();
        this.minVal = null;
        this.maxVal = null;
    }
    
    /**
     * @method 
     * the max value.
     */
    , getMax: function() {
    	if(this.maxVal == null) {
    		this.maxVal = this.nbVal ? this.distribution.getMaxDataPoint().getValue(): 0;
    	}
        return this.maxVal;
    }

    /**
     * @method 
     * @return {Number} the min value.
     */
    , getMin: function() {
    	if(this.minVal == null) {
    		this.minVal = this.nbVal ? this.distribution.getMinDataPoint().getValue(): 0;
    	}
        return this.minVal;
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    /**
     * @method 
     * This function calls the appropriate classifyBy... function.
     * The name of classification methods are defined by class constants
     *
     * @param {Integer} method Method name constant as defined in this class
     * @param {Integer} nbBins Number of classes
     * @param {Array(Integer)} bounds Array of bounds to be used for by bounds method
     *
     * @return {Sbi.geo.stat.Classification} Classification
     */
    , classify: function(method, nbBins, bounds) {
    	Sbi.trace("[Classifier.classify] : IN");
    	
    	Sbi.debug("[Classifier.classify] : Input parameter [method] is equal to [" + method + "]");
    	Sbi.debug("[Classifier.classify] : Input parameter [nbBins] is equal to [" + nbBins + "]");
    	Sbi.debug("[Classifier.classify] : Input parameter [bounds] is equal to [" + bounds + "]");
    	
    	var classification = null;
        if (!nbBins) {
            nbBins = this.sturgesRule();
        }
        switch (method) {
        case Sbi.geo.stat.Classifier.CLASSIFY_WITH_BOUNDS:
            classification = this.classifyWithBounds(bounds);
            break;
        case Sbi.geo.stat.Classifier.CLASSIFY_BY_EQUAL_INTERVALS :
            classification = this.classifyByEqIntervals(nbBins);
            break;
        case Sbi.geo.stat.Classifier.CLASSIFY_BY_QUANTILS :
            classification = this.classifyByQuantils(nbBins);
            break;
        default:
           alert("Unsupported or invalid classification method [" + method + "]");
        }
        
        Sbi.trace("[Classifier.classify] : OUT");
        
        return classification;
    }

    , classifyByEqIntervals: function(nbBins) {
    	Sbi.trace("[Classifier.classifyByEqIntervals] : IN");
    	
    	Sbi.debug("[Classifier.classifyByEqIntervals] : min val equal to [" + this.getMin() + "]");
    	Sbi.debug("[Classifier.classifyByEqIntervals] : max val equal to [" + this.getMax() + "]");
    	
    	var binSize = (this.getMax() - this.getMin()) / nbBins;
    	Sbi.debug("[Classifier.classifyByEqIntervals] : Each one of the [" + nbBins + "] bins will have a size of [" + binSize + "]");
    	
        var bounds = [];
        var boundsStr = '';
        
        for(var i = 0; i <= nbBins; i++) {
            bounds[i] = this.getMin() + (i*binSize);
            boundsStr +=  bounds[i] + "; ";
        }
        Sbi.debug("[Classifier.classifyByEqIntervals] : Bounds array is equal to [" + boundsStr.trim() + "];");
        
        Sbi.trace("[Classifier.classifyByEqIntervals] : OUT");

        return this.classifyWithBounds(bounds);
    }

    
    , arrayToString: function(a, format) {
    	format = format || function(o){return o;};
    	var s = '';
        for (i = 0; i < a.length; i++) {
        	s += format(a[i]) + "; ";
        }
        return s.trim();
    }
    
    , classifyByQuantils: function(nbBins) {
    	Sbi.trace("[Classifier.classifyByQuantils] : IN");
        
    	var values = this.distribution.getValues();
    	Sbi.debug("[Classifier.classifyByQuantils] : Total number of value extracted from distribution is equal to [" + values.length + "];");
        values.sort(function(a,b) {a = a || 0; b = b || 0; return a-b;});
        Sbi.debug("[Classifier.classifyByQuantils] : Sorted values array is equal to [" + this.arrayToString(values) + "];");
        
// 		 26/08/2015: quantils are calculated always on ALL values (NOT ONLY DISTINCT VALUES)
//        var distinctValues = [];
//        var lastAddedValue = null;
//        for(var i = 0; i < values.length; i++) {
//        	if(values[i] != lastAddedValue){
//        		distinctValues.push(values[i]);
//        		lastAddedValue = values[i];
//        	}
//        }
//        Sbi.debug("[Classifier.classifyByQuantils] : Sorted distinct values array is equal to [" + this.arrayToString(distinctValues) + "];");
//        values = distinctValues;
        
        var binSize = Math.round(values.length  / nbBins);
        Sbi.debug("[Classifier.classifyByQuantils] : Each one of the [" + nbBins + "] bins will contain [" + binSize + "] values");

        var bounds = [];
        var boundsStr = '';
        var binLastValPos = (binSize == 0) ? 0 : binSize;

        if (values.length > 0) {
            bounds[0] = values[0];
            boundsStr +=  values[0] + "; ";
            for (i = 1; i < nbBins; i++) {
                bounds[i] = values[binLastValPos] || (bounds[i-1]+1);
                boundsStr +=  bounds[i] + "; ";
                binLastValPos += binSize;
            }
            if(values[values.length - 1] > bounds[bounds.length-1]) {
            	// this condition is always true except when the number of quantiles is greater 
            	// then the number of distict values in the distribuction
            	bounds.push(values[values.length - 1]);
            } else {
            	bounds.push(bounds[bounds.length-1] + 1);
            }
            
            boundsStr +=  values[values.length - 1] + "; ";
        }
        Sbi.debug("[Classifier.classifyByEqIntervals] : Bounds array is equal to [" + boundsStr.trim() + "];");
        
        Sbi.trace("[Classifier.classifyByQuantils] : OUT");
        
//        return this.classifyWithBounds(bounds);
        return this.classifyQuantilsWithBounds(bounds);
        
    }
    
    , classifyWithBounds: function(bounds) {
    	Sbi.trace("[Classifier.classifyWithBounds] : IN");
    	
        var bins = [];
        var binCount = [];
        var binDataPoints = [];
        var sortedDataPoints = [];
        for (var i = 0; i < this.distribution.getSize(); i++) {
            sortedDataPoints.push(this.distribution.getDataPointAt(i));
        }
        sortedDataPoints.sort(function(a,b) {return a.getValue() - b.getValue();});
        Sbi.debug("[Classifier.classifyWithBounds] : Sorted values array is equal to [" + this.arrayToString(sortedDataPoints, function(o){return o.getValue();}) + "];");
        Sbi.debug("[Classifier.classifyWithBounds] : Bounds array is equal to [" + this.arrayToString(bounds) + "];");
        
        
        var nbBins = bounds.length - 1;
        for (var i = 0; i < nbBins; i++) {
            binCount[i] = 0;
            binDataPoints[i] = [];
        }

        for (var i = 0; i < nbBins; i) {
            if (sortedDataPoints[0].getValue() < bounds[i + 1]) {
            	Sbi.debug("[Classifier.classifyWithBounds] : Added value [" + sortedDataPoints[0].getValue() + "] of type [" + (typeof sortedDataPoints[0].getValue()) + "] to bin [" + i + "] becuase it is less than bin ub [" + bounds[i + 1]+ "]");
                binCount[i] = binCount[i] + 1;
                binDataPoints[i].push(sortedDataPoints[0]);
                sortedDataPoints.shift();
                Sbi.trace("[Classifier.classifyWithBounds] : bin [" + i + "] now contains [" + binDataPoints[i].length+ "] data points");
                if(sortedDataPoints[0] === undefined) {
                	 Sbi.trace("[Classifier.classifyWithBounds] : no more data points to classify");
                	 break;
                }
            } else {
                i++;
                Sbi.trace("[Classifier.classifyWithBounds] : Increment to bin [" + i + "] because value [" + sortedDataPoints[0].getValue() + "] is greater then lb [" + bounds[i + 1] + "] of type [" + (typeof bounds[i + 1]) + "]");
            }
        }
        
        Sbi.trace("[Classifier.classifyWithBounds]: datapoints not classified [" + sortedDataPoints.length + "]");
        for(var i = 0; i < sortedDataPoints.length; i++) {
        	Sbi.trace("[Classifier.classifyWithBounds]: datapoint [" + sortedDataPoints[i].coordinates[0] + "] whose value is equal to [" + sortedDataPoints[0].getValue() + "] has been not classified. It will be added to the last bin");
        	binCount[nbBins - 1] = binCount[nbBins - 1] + 1;
        	binDataPoints[nbBins - 1].push(sortedDataPoints[i]);
        }
        
        var classifiedDataPoint = 0;
        for (var i = 0; i < nbBins; i++) {
        	
        	bins[i] = new Sbi.geo.stat.Bin({
        		nbVal: binCount[i]
        		, dataPoints: binDataPoints[i]
        		, lowerBound: bounds[i]
        		, upperBound: bounds[i + 1]
        		, isLast: i == (nbBins - 1)
        	});
        	classifiedDataPoint += binDataPoints[i].length;
        	Sbi.trace("[Classifier.classifyWithBounds] : Bin [" + i + "] is equal to [" + bounds[i]+ " - " + bounds[i + 1] + "] and contains [" + binDataPoints[i].length + "] data points");
          
            //var labelGenerator = this.labelGenerator || this.defaultLabelGenerator;
            bins[i].label = this.labelGenerator(bins[i], i, nbBins);
        }
        Sbi.trace("[Classifier.classifyWithBounds] : data points classified [" + classifiedDataPoint + "] over a total of [" + this.distribution.getSize() +"]");
        
        Sbi.trace("[Classifier.classifyWithBounds] : OUT");
        
        return new Sbi.geo.stat.Classification(bins);
    }
    
    
    , classifyQuantilsWithBounds: function(bounds) {
    	Sbi.trace("[Classifier.classifyQuantilsWithBounds] : IN");
    	
        var bins = [];
        var binCount = [];
        var binDataPoints = [];
        var binIstancesForCoord = [];
        var sortedDataPoints = [];
                
        for (var i = 0; i < this.distribution.getSize(); i++) {
            sortedDataPoints.push(this.distribution.getDataPointAt(i));
        }
        sortedDataPoints.sort(function(a,b) {return a.getValue() - b.getValue();});
        Sbi.debug("[Classifier.classifyQuantilsWithBounds] : Sorted values array is equal to [" + this.arrayToString(sortedDataPoints, function(o){return o.getValue();}) + "];");
        Sbi.debug("[Classifier.classifyQuantilsWithBounds] : Bounds array is equal to [" + this.arrayToString(bounds) + "];");
        
        
        var nbBins = bounds.length - 1;
        for (var i = 0; i < nbBins; i++) {
            binCount[i] = 0;
            binDataPoints[i] = [];
            binIstancesForCoord[i] = [];
        }

        
        for (var i = 0; i < nbBins; i) {
            if (sortedDataPoints[0].getValue() < bounds[i + 1]) {
            	Sbi.debug("[Classifier.classifyQuantilsWithBounds] : Added coordinate ["+sortedDataPoints[0].coordinates[0] +"] with value [" + sortedDataPoints[0].getValue() + "] of type [" + (typeof sortedDataPoints[0].getValue()) + "] to bin [" + i + "] becuase it is less than bin ub [" + bounds[i + 1]+ "]");
                binCount[i] = binCount[i] + 1;
                binDataPoints[i].push(sortedDataPoints[0]);
                    
                //defines total number of records for coordinates and bin (weight)
                var added = false;         	
            	var elem = binIstancesForCoord[i];
            	if (elem.length > 0){  
            		for (var e=0; e<elem.length; e++){
            			// if bin contains already some elements, updates the counter for the coordinate if exists otherwise adds the new one (added=false) (*)
            			if (elem[e].coord == sortedDataPoints[0].coordinates[0]){
                		  elem[e].count = (elem[e].count+1);
                		  added = true;
            			}
            		}
            		if (!added){
            			//adds the new coordinate counter into the bin (*)
            			var newEl = {};
                  		newEl.coord = sortedDataPoints[0].coordinates[0];
                  		newEl.count = 1;
                  		binIstancesForCoord[i].push(newEl);
            		}
            	 } else {
            		  //first element for the bin
            		var newEl = {};
              		newEl.coord = sortedDataPoints[0].coordinates[0];
              		newEl.count = 1;
              		binIstancesForCoord[i].push(newEl);
            	 }
                
               
                sortedDataPoints.shift();
                Sbi.trace("[Classifier.classifyQuantilsWithBounds] : bin [" + i + "] now contains [" + binDataPoints[i].length+ "] data points");
                if(sortedDataPoints[0] === undefined) {
                	 Sbi.trace("[Classifier.classifyQuantilsWithBounds] : no more data points to classify");
                	 break;
                }
            } else {
                i++;
                Sbi.trace("[Classifier.classifyQuantilsWithBounds] : Increment to bin [" + i + "] because value [" + sortedDataPoints[0].getValue() + "] is greater then lb [" + bounds[i + 1] + "] of type [" + (typeof bounds[i + 1]) + "]");
            }
        }
        
        Sbi.trace("[Classifier.classifyQuantilsWithBounds]: datapoints not classified [" + sortedDataPoints.length + "]");
        for(var i = 0; i < sortedDataPoints.length; i++) {
        	Sbi.trace("[Classifier.classifyQuantilsWithBounds]: datapoint [" + sortedDataPoints[i].coordinates[0] + "] whose value is equal to [" + sortedDataPoints[0].getValue() + "] has been not classified. It will be added to the last bin");
        	binCount[nbBins - 1] = binCount[nbBins - 1] + 1;
        	binDataPoints[nbBins - 1].push(sortedDataPoints[i]);
        	
        	//updates counters for the last bin with last elements
        	var added = false;
        	var elem = binIstancesForCoord[binIstancesForCoord.length-1];
        	if (elem.length > 0){  
        		for (var e=0; e<elem.length; e++){
        			// if bin contains already some elements, updates the counter for the coordinate if exists otherwise adds the new one (added=false) (*)
        			if (elem[e].coord == sortedDataPoints[i].coordinates[0]){
            		  elem[e].count = (elem[e].count+1);
            		  added = true;
        			}
        		}
        		if (!added){
        			//adds the new coordinate counter into the bin (*)
        			var newEl = {};
              		newEl.coord = sortedDataPoints[0].coordinates[0];
              		newEl.count = 1;
              		binIstancesForCoord[binIstancesForCoord.length-1].push(newEl);
        		}
        	 }
        }
        
        //creates summary array with bins and counters
        var counterForQuantils = this.defineBinOnOccNumber(binIstancesForCoord);
        
        var classifiedDataPoint = 0;
        for (var i = 0; i < nbBins; i++) {
        	
        	bins[i] = new Sbi.geo.stat.Bin({
        		nbVal: binCount[i]
        		, dataPoints: binDataPoints[i]
        		, counterForQuantils: counterForQuantils || []
        		, lowerBound: bounds[i]
        		, upperBound: bounds[i + 1]
        		, isLast: i == (nbBins - 1)
        	});
        	classifiedDataPoint += binDataPoints[i].length;
        	Sbi.trace("[Classifier.classifyQuantilsWithBounds] : Bin [" + i + "] is equal to [" + bounds[i]+ " - " + bounds[i + 1] + "] and contains [" + binDataPoints[i].length + "] data points");
            bins[i].label = this.labelGenerator(bins[i], i, nbBins);
        }
        Sbi.trace("[Classifier.classifyQuantilsWithBounds] : data points classified [" + classifiedDataPoint + "] over a total of [" + this.distribution.getSize() +"]");
        
        Sbi.trace("[Classifier.classifyQuantilsWithBounds] : OUT");
        
        return new Sbi.geo.stat.Classification(bins);
    }
    
    /**
	 * @method
	 * returns the bin with max number of occourences for each coordiante
	 */
    , defineBinOnOccNumber: function(binIstancesForCoord){
    	 Sbi.trace("[Classifier.defineBinOnOccNumber] : IN");
    	 
    	 var sortedBin = [];
    	 var isFirst = true;
    	 for (var i=0; i < binIstancesForCoord.length; i ++){
    		 var elBin = binIstancesForCoord[i];
    		 for (j=0; j<elBin.length; j ++){
    			 
    			 if (isFirst){
    				 //inserts the first element
    				 elBin[j].binIdx = i;
    				 sortedBin.push(elBin[j]);
    				 isFirst = false;
    			 }else{
    				 //checks if exist already an element with the specified coordinate
    				 var exists = false;
    				 var pos = 0;
    				 for (k=0; k < sortedBin.length; k ++){	    
    					 if (sortedBin[k].coord === elBin[j].coord){
    						 exists = true;
    						 pos = k;
    						 break;
    					 }
    				 }
    					 
					 if (exists){
						 // updates the counter 
						 if (sortedBin[pos].count+0 <= elBin[j].count+0) {
							 sortedBin[pos].count = elBin[j].count+0;  
							 sortedBin[pos].binIdx = i;
						 }
					 }else{    
						 //inserts the new counter for the bin
						 elBin[j].binIdx = i;
						 sortedBin.push(elBin[j]);
					 }    				  				
    			 }
    		 }
    	 }
    	 // print array content for debug 
    	 var s = '';
         for (i = 0; i < sortedBin.length; i++) {
        	 var bin = sortedBin[i];
        	 s += '[';
        	 for (b in bin) {
        		 s +=  b + ": " + bin[b] + "; ";
        	 }
        	 s += ']';
         }
    	 Sbi.debug("[Classifier.defineBinOnOccNumber] : Bounds summary array is equal to [" + s + "];");
    	 Sbi.trace("[Classifier.defineBinOnOccNumber] : OUT");
    	 return sortedBin;
    }
    
    /**
	 * @method
	 * Generator for bin labels
	 */
    , labelGenerator: function(bin, binIndex, nbBins) {
        return this.defaultLabelGenerator(bin, binIndex, nbBins);
    }
    /**
     * @method
     * Generator for bin labels
     *
     * Parameters:
     *   bin - {<mapfish.GeoStat.Bin>} Lower bound limit value
     *   binIndex - {Integer} Current bin index
     *   nBins - {Integer} Total number of bins
     */
    , defaultLabelGenerator: function(bin, binIndex, nbBins) {
       //.toFixed(3)
        return Sbi.commons.Format.number(bin.lowerBound, '0.000,00')  + ' - ' + Sbi.commons.Format.number(bin.upperBound, '0.000,00')  + ' (' + bin.nbVal + ')'
    }
    
    /**
     * Returns:
     * {Number} Maximal number of classes according to the Sturge's rule
     */
    , sturgesRule: function() {
        return Math.floor(1 + 3.3 * Math.log(this.nbVal, 10));
    }



});

