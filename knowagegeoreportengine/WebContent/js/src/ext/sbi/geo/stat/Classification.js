/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 **/

Ext.ns("Sbi.geo.stat");

//=====================================================================================
//Data Point Class
//======================================================================================
Sbi.geo.stat.DataPoint = function(config) {
	this.initialize(config.coordinates, config.value);
	Sbi.geo.stat.DataPoint.superclass.constructor.call(this, config);
};

/**
* @class Sbi.geo.stat.DataPoint
* @extends Ext.util.Observable
*/
Ext.extend(Sbi.geo.stat.DataPoint, Ext.util.Observable, {

	coordinates: null
	, value: null


	, initialize: function(coordinates, value) {
		this.coordinates =  coordinates || [];
		this.value = value;
	}

	, getValue: function() {
		return this.value;
	}

	/**
	 * By default coordinates are not case sensitive so for example ['Milano', 'Gennaio'] identify
	 * exactly the same datapoint of coordinate ['MILANO', 'geNNaio']
	 */
	, coordinatesAreEqualTo: function(c, isCaseSensitive) {
		if(c === undefined || c === null) return false;
		var tmpC = c;
		if (typeof tmpC !== 'string') tmpC += "";
		isCaseSensitive = isCaseSensitive || false;
		for(var i = 0; i < this.coordinates.length; i++) {
			var tmpCoordinate = this.coordinates[i];
			if (typeof tmpCoordinate !== 'string')	tmpCoordinate += "";
			if(isCaseSensitive) {
				if(tmpCoordinate != tmpC) return false;
			} else {
				if(tmpCoordinate.toUpperCase() != tmpC.toUpperCase()) return false;
			}
		}
		return true;
	}
});

//=====================================================================================
//Distribution Class
//======================================================================================
Sbi.geo.stat.Distribution = function(config) {
	config = config || {};
	this.initialize(config.dataPoints);
	Sbi.geo.stat.Distribution.superclass.constructor.call(this, config);
};

/**
* @class Sbi.geo.stat.Distribution
* @extends Ext.util.Observable
*/
Ext.extend(Sbi.geo.stat.Distribution, Ext.util.Observable, {

	dataPoints: null

	, initialize: function(dataPoints) {
		this.dataPoints =  dataPoints || [];
	}

	, getSize: function() {
		return this.dataPoints.length;
	}

	, getDataPointAt: function(index) {
		return this.dataPoints[index];
	}

	, addDataPoint: function(dataPoint) {
		this.dataPoints.push(dataPoint);
	}

	, getDataPoint: function(coordinates){
		for(var i = 0; i < this.dataPoints.length; i++) {
			if(this.dataPoints[i].coordinatesAreEqualTo(coordinates, false)) return this.dataPoints[i];
		}
		return null;
	}

	/**
     * @method
     * the max data point.
     */
    , getMaxDataPoint: function() {

    	var maxVal = Number.MIN_VALUE;
    	var maxDataPoint = null;
    	for(var i = 0; i < this.dataPoints.length; i++) {
    		if(this.dataPoints[i].value > maxVal) {
    			maxVal = this.dataPoints[i].value;
    			maxDataPoint = this.dataPoints[i];
    		}
    	}

        return maxDataPoint;
    }

    /**
     * @method
     * the min data point.
     */
    , getMinDataPoint: function() {

    	var minVal = Number.MAX_VALUE;
    	var minDataPoint = null;
    	for(var i = 0; i < this.dataPoints.length; i++) {
    		if(this.dataPoints[i].getValue() < minVal) {
    			minVal = this.dataPoints[i].getValue();
    			minDataPoint = this.dataPoints[i];
    		}
    		//Sbi.trace("[Distribution.getMinDataPoint] : last value read is equal to [" + this.dataPoints[i].getValue() + "]. Min val found so far is equal to [" + minVal + "]");
    	}

    	return minDataPoint;
    }

    , getValues: function() {
    	var values = [];
    	for(var i = 0; i < this.dataPoints.length; i++) {
    		values.push( this.dataPoints[i].getValue() );
    	}
    	return values;
    }
});





// =====================================================================================
// Bin Class
//======================================================================================
Sbi.geo.stat.Bin = function(config) {
	this.initialize(config.nbVal, config.dataPoints, config.counterForQuantils, config.lowerBound, config.upperBound, config.isLast);
	Sbi.geo.stat.Bin.superclass.constructor.call(this, config);
};

/**
 * @class Sbi.geo.stat.Bin
 * @extends Ext.util.Observable
 *
 *  Bin is category of the Classification.
 *  When they are defined, lowerBound is within the class
 *  and upperBound is outside the class.
 */
Ext.extend(Sbi.geo.stat.Bin, Ext.util.Observable, {
    label: null
    , nbVal: null
    , dataPoints: null
    , counterForQuantils: null
    , lowerBound: null
    , upperBound: null
    , isLast: false

    , initialize: function(nbVal, dataPoints, counterForQuantils, lowerBound, upperBound, isLast) {
        this.nbVal = nbVal;
        this.dataPoints = dataPoints;
        this.counterForQuantils = counterForQuantils || [];
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.isLast = isLast;
    }
});

//=====================================================================================
//Classification Class
//======================================================================================
Sbi.geo.stat.Classification = function(bins) {
	this.initialize(bins);
	Sbi.geo.stat.Classification.superclass.constructor.call(this, bins);
};

/**
 * @class Sbi.geo.stat.Classification
 * @extends Ext.util.Observable
 * Classification summarizes a Distribution by regrouping data within several Bins.
 */
Ext.extend(Sbi.geo.stat.Classification, Ext.util.Observable, {
    bins: []

    , initialize: function(bins) {
        this.bins = bins;
    }

    , getBoundsArray: function() {
        var bounds = [];
        for (var i = 0; i < this.bins.length; i++) {
            bounds.push(this.bins[i].lowerBound);
        }
        if (this.bins.length > 0) {
            bounds.push(this.bins[this.bins.length - 1].upperBound);
        }
        return bounds;
    }

    , getBins: function() {
    	return this.bins;
    }

    , getBin: function(dataPointCoordinates) {
    	for(var i = 0; i < bin.length; i++) {
    		for(var j = 0; j < bin.length; j++) {
    			bin[i].dataPoints[j].coordinatesAreEqualTo(dataPointCoordinates);
    			return bin;
    		}
    	}
    	return null;
    }
});

