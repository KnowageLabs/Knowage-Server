/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 **/


Ext.ns("Sbi.geo.utils");


Sbi.geo.utils.ColorRgb = function(config) {
	
	config = config || {};
	
	if(Ext.isArray(config)) {
		var c = {};
		c.red = config[0];
		c.green = config[1];
		c.blue = config[2];
		config = c;
	}
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	this.initialize(config.red, config.green, config.blue);

	Sbi.geo.utils.ColorRgb.superclass.constructor.call(this, config);
};


/**
 * @class Sbi.geo.stat.Classifier
 * @extends Ext.util.Observable
 * 
 * Classifier class
 */
Ext.extend(Sbi.geo.utils.ColorRgb, Ext.util.Observable, {
	
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
    /**
	 * @property {Integer} redLevel
	 * redLevel
	 */
    redLevel: null

    /**
	 * @property {Integer} greenLevel
	 * greenLevel
	 */
    , greenLevel: null

    /**
	 * @property {Integer} blueLevel
	 * blueLevel 
	 */
    , blueLevel: null
    
    
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
	
	/**
     * @method 
     *
     * Called by the constructor to initialize this object
     * @param {Integer} red red level
     * @param {Integer} green green level
     * @param {Integer} blue blue level
     */
    , initialize: function(red, green, blue) {
    	this.redLevel = red;
    	this.greenLevel = green;
    	this.blueLevel = blue;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , getColorRgb: function() {
        return this;
    }
    
    , getRgbArray: function() {
        return [
            this.redLevel,
            this.greenLevel,
            this.blueLevel
        ];
    }
    
    , getRgbString: function() {
        return this.redLevel + "," + this.greenLevel + "," + this.blueLevel;
    }
    
    /**
     * APIMethod: setFromHex
     * Sets the color from a color hex string 
     *
     * Parameters:
     * rgbHexString - {String} Hex color string (format: #rrggbb)
     */
    , setFromHex: function(rgbHexString) {        
        var rgbArray = this.hex2rgbArray(rgbHexString);
        this.redLevel = rgbArray[0];
        this.greenLevel = rgbArray[1];
        this.blueLevel = rgbArray[2];
    }
    
    /**
     * APIMethod: setFromRgb
     * Sets the color from a color rgb string
     *
     */
    , setFromRgb: function(rgbString) {
        var color = dojo.colorFromString(rgbString);
        this.redLevel = color.r;
        this.greenLevel = color.g;
        this.blueLevel = color.b;
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
   
    /**
     * Method: hex2rgbArray
     * Converts a Hex color string to an Rbg array 
     *
     * Parameters:
     * rgbHexString - {String} Hex color string (format: #rrggbb)
     */
    , hex2rgbArray: function(rgbHexString) {
        if (rgbHexString.charAt(0) == '#') {
            rgbHexString = rgbHexString.substr(1);
        }
        var rgbArray = [
            parseInt(rgbHexString.substring(0,2),16),
            parseInt(rgbHexString.substring(2,4),16),
            parseInt(rgbHexString.substring(4,6),16)
        ];
        for (var i = 0; i < rgbArray.length; i++) {
            if (rgbArray[i] < 0 || rgbArray[i] > 255 ) {
                OpenLayers.Console.error("Invalid rgb hex color string: rgbHexString");
            }
        }        
        return rgbArray;
    }
    

    
    /**
     * APIMethod: toHexString
     * Converts the rgb color to hex string
     *
     */
    , toHexString: function() {
        var r = this.toHex(this.redLevel);
        var g = this.toHex(this.greenLevel);
        var b = this.toHex(this.blueLevel);
        return '#' + r + g + b;
    }
    
    /**
     * Method: toHex
     * Converts a color level to its hexadecimal value
     *
     * Parameters:
     * dec - {Integer} Decimal value to convert [0..255]
     */
    , toHex: function(dec) {
        // create list of hex characters
        var hexCharacters = "0123456789ABCDEF"
        // if number is out of range return limit
        if (dec < 0 || dec > 255 ) {
            var msg = "Invalid decimal value for color level";
            OpenLayers.Console.error(msg);
        }
        // decimal equivalent of first hex character in converted number
        var i = Math.floor(dec / 16)
        // decimal equivalent of second hex character in converted number
        var j = dec % 16
        // return hexadecimal equivalent
        return hexCharacters.charAt(i) + hexCharacters.charAt(j)
    }
    
    /**
     * APIMethod: equals
     *      Returns true if the colors at the same.
     *
     * Parameters:
     * {<mapfish.ColorRgb>} color
     */
    , equals: function(color) {
        return color.redLevel == this.redLevel &&
               color.greenLevel == this.greenLevel &&
               color.blueLevel == this.blueLevel;
    }

});

/**
 * APIMethod: getColorsArrayByRgbInterpolation
 *      Get an array of colors based on RGB interpolation.
 *
 * Parameters:
 * firstColor - {<mapfish.Color>} The first color in the range.
 * lastColor - {<mapfish.Color>} The last color in the range.
 * nbColors - {Integer} The number of colors in the range.
 *
 * Returns
 * {Array({<mapfish.Color>})} The resulting array of colors.
 */
Sbi.geo.utils.ColorRgb.getColorsArrayByRgbInterpolation = function(firstColor, lastColor, nbColors) {
	
    var resultColors = [];
    
    Sbi.trace("[ColorRgb.getColorsArrayByRgbInterpolation] : IN");
    
    Sbi.trace("[ColorRgb.getColorsArrayByRgbInterpolation] : First color is equal to [" + firstColor.getRgbString() +"]");
    Sbi.trace("[ColorRgb.getColorsArrayByRgbInterpolation] : Last color is equal to [" + lastColor.getRgbString() +"]");
    
    var colorA = firstColor.getColorRgb();
    var colorB = lastColor.getColorRgb();
    
    var colorAVal = colorA.getRgbArray();
    var colorBVal = colorB.getRgbArray();
    
    if (nbColors == 1) {
        return [colorA];
    }
    
    var stepOnRedAxis = (colorBVal[0] - colorAVal[0]) / (nbColors - 1);
    var stepOnGreenAxis = (colorBVal[1] - colorAVal[1]) / (nbColors - 1);
    var stepOnBlueAxis = (colorBVal[2] - colorAVal[2]) / (nbColors - 1);
    
    for (var i = 0; i < nbColors; i++) {
        var rgbTriplet = [];
        rgbTriplet[0] = colorAVal[0] + i * stepOnRedAxis;
        rgbTriplet[1] = colorAVal[1] + i * stepOnGreenAxis;
        rgbTriplet[2] = colorAVal[2] + i * stepOnBlueAxis;
        resultColors[i] = new Sbi.geo.utils.ColorRgb([parseInt(rgbTriplet[0]), 
        parseInt(rgbTriplet[1]), parseInt(rgbTriplet[2])]);
        Sbi.trace("[ColorRgb.getColorsArrayByRgbInterpolation] :Color [" + i + "] is equal to [" + resultColors[i].getRgbString() +"]");
    }
    
    Sbi.trace("[ColorRgb.getColorsArrayByRgbInterpolation] : OUT");
    
    return resultColors;
};




