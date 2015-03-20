/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
Ext.ns("Sbi.geo");

/**
 * Class: mapfish.widgets.MapComponent
 *
 * A map container in order to be able to insert a map into a complex layout
 * Its main interest is to update the map size when the container is resized
 */
Sbi.geo.MapComponent = function(config) {
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
    Ext.apply(this, config);
    
    if(this.map == null) this.initMap();
    
    this.contentEl = this.map.div;

    // Set the map container height and width to avoid css 
    // bug in standard mode. 
    // See https://trac.mapfish.org/trac/mapfish/ticket/85
    var content = Ext.get(this.contentEl);
    content.setStyle('width', '100%');
    content.setStyle('height', '100%');
    
    Sbi.geo.MapComponent.superclass.constructor.call(this);
};

Ext.extend(Sbi.geo.MapComponent, Ext.Panel, {
    
	/**
	 * @property {Object} baseMapOptions
	 * 
     * base map's configurations. Can contains the following properites
     *
     * - projection: Set in the map options to override the default projection string of this map. Default is "EPSG:4326".
     * - displayProjection: Set in the map options to override the default projection string of this map. Default is 'EPSG:4326',
	 * - units: Set the map units.  Defaults to 'degrees'.  Possible values are 'degrees' (or 'dd'), 'm', 'ft', 'km', 'mi', 'inches'.
	 * - maxResolution: Default max is 360 deg 256 px, which corresponds to zoom level 0 on gmaps.  
	 *   Specify a different value in the map options if you are not using a geographic projection and displaying the whole world.
	 * - maxResolution: default is 156543.0339
	 * - maxExtent: The maximum extent for the map.  Defaults to the whole world in decimal degrees (-180, -90, 180, 90).  
	 *	            Specify a different extent in the map options if you are not using a geographic projection and displaying 
	 *			    the whole world.
     */
	baseMapOptions: null
	
	/**
     * Property: map
     * {}  
     */
	/**
	 * @property {OpenLayers.Map} map
     * the map
     */
    , map: null
    
	/**
	 * @property {Object} baseLayersConf
     * base layer's configurations.
     */
	, baseLayersConf: null
	
	/**
	 * @property {Object} baseLayersConf
     * over layer's configurations.
     */
	, overLayersConf: null
	
	/**
	 * @property {Array} layers
     * the layers that compose this map
     */
	, layers: null
	
	/**
	 * @property {Object} baseControlsConf
     * base control's configurations.
     */
	, baseControlsConf: null
	
	/**
	 * @property {Object} baseControlsConf
     * base centralPoint's configurations.
     */
	, baseCentralPointConf: null
	
	, mainPanel: null
    
    , mask: null

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
		
		Sbi.trace("[MapComponent.validateConfigObject] : IN");
		
		if(!config) {
			throw "Impossible to build MapComponent. Config object is undefined";
		}
		
		if(config.map == undefined && config.baseMapOptions == undefined) {
			throw "Impossible to build MapComponent. Neither map nor baseMapOptions are passed as input parameter to the constructor";
		}
		Sbi.trace("[MapComponent.validateConfigObject] : Config object passed to constructor has been succesfully validated");
		
		Sbi.trace("[MapComponent.validateConfigObject] : OUT");
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
		
		Sbi.trace("[MapComponent.adjustConfigObject] : IN");
		
		if(config.map == undefined) {
			var o = config.baseMapOptions;
			
			if(o.projection !== undefined && typeof o.projection === 'string') {
				o.projection = new OpenLayers.Projection( o.projection );
			}
			
			if(o.displayProjection !== undefined && typeof o.displayProjection === 'string') {
				o.displayProjection = new OpenLayers.Projection( o.displayProjection );
			}
			
			if(o.maxExtent !== undefined && typeof o.maxExtent === 'object') {
				o.maxExtent = new OpenLayers.Bounds(
						o.maxExtent.left, 
						o.maxExtent.bottom,
						o.maxExtent.right,
						o.maxExtent.top
	            );
			}
		}
		
		Sbi.trace("[MapComponent.adjustConfigObject] : OUT");
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
    
	
	, thematizers: {}
	, activeThematizerName: null 
	
	, addThematizer: function(name, thematizer) {
		this.thematizers[name] = thematizer;
		// we inject thematzer in map to make it accessible to the 
		// legend control that need it in order to change configurations
		this.map.thematizer = this.thematizer;
	}
	
	, activateThematizer: function(name) {
		Sbi.trace("[MapComponent.activateThematizer] : IN");
		var newThematizer = this.thematizers[name];
		if(newThematizer === undefined) {
			Sbi.warn("[MapComponent.activateThematizer] : A thematizer with name [" + name + "] does not exist");
			Sbi.trace("[MapComponent.activateThematizer] : OUT");
			return;
		}
		
		if(this.activeThematizerName != null) {
			var oldThematizer = this.getActiveThematizer();
			oldThematizer.deactivate();
		}
		
		this.activeThematizerName = name;
		this.map.thematizer = newThematizer;
		newThematizer.activate();
		
		Sbi.trace("[MapComponent.activateThematizer] : OUT");
	}
	
	, getActiveThematizer: function() {
		return this.thematizers[this.activeThematizerName];
	}
	
	 /**
     * @method
     * Set the central point of the map
     * 
    * @param {Object} center point. It is defined by lon, lt and zoom level
     */
	, setCenter: function(center) {
      	
    	Sbi.trace("MapComponent.setCenter: IN");
    	
		center = center || {};
      	this.baseCentralPointConf.lon = center.lon || this.baseCentralPointConf.lon || 18.530;
      	this.baseCentralPointConf.lat = center.lat || this.baseCentralPointConf.lat || 42.500;
      	this.baseCentralPointConf.zoomLevel = center.zoomLevel || this.baseCentralPointConf.zoomLevel || 5;
      	
        if(this.map.projection == 'EPSG:900913'){            
            this.centerPoint = Sbi.geo.utils.GeoReportUtils.lonLatToMercator(new OpenLayers.LonLat(this.baseCentralPointConf.lon, this.baseCentralPointConf.lat));
            this.map.setCenter(this.centerPoint, this.baseCentralPointConf.zoomLevel);
        } else if(this.map.projection == 'EPSG:4326') {
        	this.centerPoint = new OpenLayers.LonLat(this.baseCentralPointConf.lon, this.baseCentralPointConf.lat);
        	this.map.setCenter(this.centerPoint, this.baseCentralPointConf.zoomLevel);
        } else{
        	alert('Map Projection [' + this.map.projection + '] not supported yet');
        }
        
        Sbi.trace("MapComponent.setCenter: OUT");
         
    }
	
	, getBaseLayersConfig: function() {
		var layers = this.map.layers;
		var layersConf = [];
		for(var i = 0; i < layers.length; i++) {
			if(layers[i].conf && layers[i].conf.isBaseLayer === true) {
				layersConf.push(layers[i].conf);
			}
		}
		
		return layersConf;
	}
	
	, getOverLayersConfig: function() {
		var layers = this.map.layers;
		var layersConf = [];
		for(var i = 0; i < layers.length; i++) {
			if(layers[i].conf && layers[i].conf.isBaseLayer === false) {
				layersConf.push(layers[i].conf);
			}
		}
		
		return layersConf;
	}

	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
    /**
     * @method
     * Initialize the panel
     */
    , initComponent: function() {
    	Sbi.trace("MapComponent.initComponent: IN");
    	Sbi.geo.MapComponent.superclass.initComponent.apply(this, arguments);
    	
    	this.on("bodyresize", this.map.updateSize, this.map);
        Sbi.trace("MapComponent.initComponent: OUT");
    }

    /**
     * @method
     * Initialize the map
     */
    , initMap: function() {  
    	Sbi.trace("MapComponent.initMap: IN");
    	
    	this.baseMapOptions.eventListeners = {
//    		featureover: function(e) {
//    			e.feature.renderIntent = "select";
//    	        e.feature.layer.drawFeature(e.feature);
//    	            
//    	            
//    	        var lonlat = e.feature.geometry.getBounds().getCenterLonLat();
//
//    	        var html = 'Comune: ' +
//    	        	e.feature.attributes.COMUNE + '<br />' +
//    	        	'Popolazione: ' + 
//    	            e.feature.attributes.POPOLAZIONE;
//
//    	            var popup = new OpenLayers.Popup.Anchored(
//    	                    'myPopup',
//    	                    lonlat,
//    	                    new OpenLayers.Size(150, 60),
//    	                    html, 
//    	                    {size: {w: 14, h: 5}, offset: {x: -7, y: -7}},
//    	                    false
//    	            );
//
//    	            e.feature.tooltip = popup;
//    	            this.addPopup(popup);
//    	        },
//    	        
//    	        featureout: function(e) {
//    	            e.feature.renderIntent = "default";
//    	            e.feature.layer.drawFeature(e.feature);
//    	            this.removePopup(e.feature.tooltip);
//    	            e.feature.tooltip = null;
//    	        }
//    	        ,
//    	        featureclick: function(e) {
//    	            log("Map says: " + e.feature.id + " clicked on " + e.feature.layer.name);
//    	        }
    	};
    	
		this.map = new OpenLayers.Map('map', this.baseMapOptions);
		
	
		
		
		
		this.map.addControlToMap = function (control, px) {
			Sbi.debug("[Map.addControlToMap]: IN");
			
			if(control.div == null) {
				Sbi.debug("[Map.addControlToMap]: div is null");
			} else {
				Sbi.debug("[Map.addControlToMap]: div is not null");
			}
			
	        // If a control doesn't have a div at this point, it belongs in the viewport.
	        control.outsideViewport = (control.div != null);
	        
	        // If the map has a displayProjection, and the control doesn't, set 
	        // the display projection.
	        if (this.displayProjection && !control.displayProjection) {
	            control.displayProjection = this.displayProjection;
	        }    
	        
	        control.setMap(this);
	        var div = control.draw(px);
	        if (div) {
	            if(!control.outsideViewport) {
	                div.style.zIndex = this.Z_INDEX_BASE['Control'] +
	                                    this.controls.length;
	                this.viewPortDiv.appendChild( div );
	                Sbi.debug("[Map.addControlToMap]: control [" + control.CLASS_NAME + "] added to viewport");
	            }
	        }
	        Sbi.debug("[Map.addControlToMap]: OUT");
	    };
	    
	    this.initLayers();
	    this.initControls();
	    
	    Sbi.trace("MapComponent.initMap: OUT");
    }
    
    /**
     * @method
     * Initialize map's layers
     */
    , initLayers: function(c) {
    	
    	Sbi.trace("MapComponent.initLayers: IN");
		
    	this.layers = new Array();
				
		if(this.baseLayersConf && this.baseLayersConf.length > 0) {
			for(var i = 0; i < this.baseLayersConf.length; i++) {
				if(this.baseLayersConf[i].enabled === true) {
					var l = Sbi.geo.utils.LayerFactory.createLayer( this.baseLayersConf[i] );
					if(l.name === this.selectedBaseLayer) {
						l.selected = true;
					} else {
						l.selected = false;
					}
					l.conf = this.baseLayersConf[i];
					l.conf.isBaseLayer = true;
					this.layers.push( l	);
				}
			}			
		}
		
		if(this.overLayersConf && this.overLayersConf.length > 0) {
			for(var i = 0; i < this.overLayersConf.length; i++) {
				if(this.overLayersConf[i].enabled === true) {
					var l = Sbi.geo.utils.LayerFactory.createLayer( this.overLayersConf[i] );
					l.conf = this.baseLayersConf[i];
					l.conf.isBaseLayer = false;
					this.layers.push( l	);
				}
			}			
		}
		
		this.map.addLayers(this.layers);
		
		Sbi.trace("MapComponent.initLayers: OUT");
	}
    
    /**
     * @method
     * Initialize map's controls
     */
    , initControls: function() {
    	
    	Sbi.trace("MapComponent.initControls: IN");
    	
		if(this.baseControlsConf && this.baseControlsConf.length > 0) {
			for(var i = 0; i < this.baseControlsConf.length; i++) {
				if(this.baseControlsConf[i].enabled === true) {
					this.baseControlsConf[i].mapOptions = this.baseMapOptions;
					var c = Sbi.geo.utils.ControlFactory.createControl( this.baseControlsConf[i] );
					if(c != null) {
						Sbi.trace("[MainPanel.initControls] : adding control [" + Sbi.toSource(this.baseControlsConf[i]) + "] ...");
						if(c.div == null) {
							Sbi.trace("[MainPanel.initControls] : div is null");
						} else {
							Sbi.trace("[MainPanel.initControls] : div is not null");
						}
						if(c.CLASS_NAME == 'Sbi.geo.control.InlineToolbar') {
							c.mainPanel = this.mainPanel;
						}
						this.map.addControl( c );
						Sbi.trace("[MainPanel.initControls] : control [" + Sbi.toSource(this.baseControlsConf[i]) + "] succesfully added to the map");
					}
					
				}
			}			
		}
		
		Sbi.trace("MapComponent.initControls: OUT");
	}
    
	, mask: function() {
		this.mask = new Ext.LoadMask(Ext.get(this.contentEl), {msg:"Please wait..."});
        this.mask.show();
	}
    
    , unmask: function() {
    	this.mask.hide();
    }
});



Ext.reg('mapcomponent', Sbi.geo.MapComponent);
