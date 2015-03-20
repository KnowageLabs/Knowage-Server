/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 

/**
 * Class: Sbi.geo.control.Measure
 * Create an overview map to display the extent of your main map and provide
 * additional navigation control.  Create a new overview map with the
 * <Sbi.geo.control.Measure> constructor.
 *
 * Inerits from:
 *  - <OpenLayers.Control>
 */

Ext.ns("Sbi.geo.control");

Sbi.geo.control.Measure = OpenLayers.Class(OpenLayers.Control, {
	
	 /**
     * Property: TYPE
     * {String} The TYPE of the control 
     * (values should be OpenLayers.Control.TYPE_BUTTON, 
     *  OpenLayers.Control.TYPE_TOGGLE or OpenLayers.Control.TYPE_TOOL)
     */
//	TYPE: OpenLayers.Control.TYPE_BUTTON,

    /**
     * Property: element
     * {DOMElement} The DOM element that contains the overview map
     */
    element: null,
    
    /**
     * APIProperty: ovmap
     * {<OpenLayers.Map>} A reference to the overview map itself.
     */
    ovmap: null,

    /**
     * APIProperty: size
     * {<OpenLayers.Size>} The overvew map size in pixels.  Note that this is
     * the size of the map itself - the element that contains the map (default
     * class name olControlSbiLegendMapElement) may have padding or other style
     * attributes added via CSS.
     */
    size: new OpenLayers.Size(35, 35),
        
    /**
     * APIProperty: mapOptions
     * {Object} An object containing any non-default properties to be sent to
     * the overview map's map constructor.  These should include any non-default
     * options that the main map was constructed with.
     */
    mapOptions: null,

    /** 
     * Property: position
     * {<OpenLayers.Pixel>} 
     */
    position: null,
    
    /**
     * popup windows for share the map url
     * */
    shareMapWindow: null,
    
    /**
     * Property: action clicked from the user
     * */
    action: null,
    
    /**
     * Property: legend configuration
     * */
    legendPanelConf: {},
    /**
     * Property: panel with the legend
     * */
    legendControlPanel: null,
   
    /**
     * Constructor: Sbi.geo.control.Measure
     * Create a new options map
     *
     * Parameters:
     * object - {Object} Properties of this object will be set on the overview
     * map object.  Note, to set options on the map object contained in this
     * control, set <mapOptions> as one of the options properties.
     */
    initialize: function(options) {  
        
    	Sbi.trace("[Measure.initialize] : IN");
    	
    	Sbi.trace("[Measure.initialize] : options are equal to [" + Sbi.toSource(options) + "]");    	
       
    	OpenLayers.Control.prototype.initialize.apply(this, [options]);
    	// ovveride the main div class automatically generated 
    	// by parent's initialize method
    	this.displayClass = "map-tools"; 
    	this.id = "MapTools"; 
    	
    	if (this.div == null) {
    		Sbi.trace("[Measure.initialize] : div is null");
    	} else {
    		Sbi.trace("[Measure.initialize] : div is not null");
    	}
    	
        
        Sbi.trace("[Measure.initialize] : OUT");
    },
    
    /**
     * APIMethod: destroy
     * Deconstruct the control
     */
    destroy: function() {
        if (!this.mapDiv) { // we've already been destroyed
            return;
        }
        this.ovmap.destroy();
        this.ovmap = null;
        
        this.element.removeChild(this.mapDiv);
        this.mapDiv = null;

        this.div.removeChild(this.element);
        this.element = null;

        OpenLayers.Control.prototype.destroy.apply(this, arguments);    
    },

    /**
     * Method: setMap
     *
     * Properties:
     * map - {<OpenLayers.Map>}
     */
    setMap: function(map) {
    	Sbi.trace("[Measure.setMap] : IN");
    	
        OpenLayers.Control.prototype.setMap.apply(this, arguments);

        if (this.outsideViewport) {
            this.events.attachToElement(this.div);
            this.events.register("buttonclick", this, this.onButtonClick);
        } else {
            this.map.events.register("buttonclick", this, this.onButtonClick);
        }
        
        Sbi.trace("[Measure.setMap] : OUT");
    },
    
    /**
     * Method: draw
     * Render the control in the browser.
     */    

    draw: function(px) {    	
    	Sbi.trace("[Measure.draw] : IN");
    	
    	this.div = document.getElementById("MapTools");
    	    	
    	if(this.div != null) {
    		Sbi.trace("[Measure.draw] : a div with id equal to [MapTools] already exist");
    	} else {
    		Sbi.trace("[Measure.draw] : a div with id equal to [MapTools] does not exist");
    		this.div = document.createElement('div');
    		this.div.id = "MapTools";
    	}
    	
    	OpenLayers.Control.prototype.draw.apply(this, arguments);
    	this.div.className = this.displayClass || "";
    	
        this.createContents();
    
        Sbi.trace("[Measure.draw] : OUT");
        
        return this.div;
    },
  
    /**
     * Method: createElementsAction
     * Defines the action elements on the map
     */
    createContents: function(){
    	// create main legend element
    	this.measureElement = document.createElement('div');
        this.measureElement.id = 'Measure'; //OpenLayers.Util.createUniqueID('Legend');   
        this.measureElement.className = 'map-tools-element measure';
        
        // create legend button
        var	measureButtonElement = document.createElement('span');
        measureButtonElement.className = 'icon';
        OpenLayers.Event.observe(measureButtonElement, "click", 
	    		OpenLayers.Function.bindAsEventListener(this.toggleMeasureWindowVisibility, this));
        
        // put everythings together
        this.measureElement.appendChild(measureButtonElement);
	    //this.measureElement.appendChild(this.legendContentElement); 
	   
        this.div.appendChild(this.measureElement);
    },
    
    measureWindow: null
    
    , toggleMeasureWindowVisibility: function() {
    	if(this.measureWindow === null) {
    		this.openMeasureWindow();
    	} else {
    		if(this.measureWindow.isVisible()) {
    			this.closeMeasureWindow();
    		} else {
    			this.openMeasureWindow();
    		}
    	}
    }
    
    , openMeasureWindow: function() {
    	if(this.measureWindow === null) {
    		
    		this.distanceButton = new Ext.Button({
    	        //xtype: 'button',
    			mesurementType: 'line',
    	        text: 'Distanza',
    	        scale: 'medium',
    	        iconCls: 'meaLinee',
    	        iconAlign: 'top',
    	        enableToggle: true,
    	        toggleGroup: 'measure',
    	        height: 70,
    	        flex: 1
	        });
    		
    		this.distanceButton.on('toggle', this.toggleControl, this);
    		
    		this.areaButton = new Ext.Button({
    	        //xtype: 'button',
    			mesurementType: 'polygon',
    	        text: 'Area',
    	        scale: 'medium',
    	        iconCls: 'meaArea',
    	        iconAlign: 'top',
    	        enableToggle: true,
    	        toggleGroup: 'measure',
    	        height: 70,
    	        flex: 1
    	    });
    		
    		this.areaButton.on('toggle', this.toggleControl, this);
    		
    		var buttonPanel = new Ext.Panel({
    			layout: 'fit',
    			
    			items:[{
    				layout:'hbox'
    				, height: 75	
    				, frame: false
    				, border: false
    				, layoutConfig: {
                         padding:'2',
                         align:'middle'
                    }
    			    , items: [this.distanceButton, this.areaButton]
    			}]
    		});
    		
    		var testPanel = new Ext.Panel({
    			layout: 'fit',
    			frame: false,
    			border: false,
    			flex: 1,
    			items: [{
    				xtype: 'box',
    		        autoEl: {
    		        	tag: 'h1'
    		        	, id: 'measureDiv'
    		        	, html: '...'
    		        	, style:"padding:15px 0 3px;text-align:center;vertical-align:middle;font-size:1.9em;font-weight:normal;font-family:cabinmedium,sans-serif,arial,helvetica;"
    		        	//, style:"padding:15px 0 3px;"
    		        }
    		    }]
    		});
    		
    		var mainPanel = new Ext.Panel({
    			 layout: {
                     type:'vbox',
                     padding:'1',
                     align:'stretch'
                 },
                 hideBorders: true,
                 border: false,
                 frame: false,
	   			 items: [buttonPanel, testPanel]
	   		});

    		this.measureWindow = new Ext.Window({
                layout      : 'fit',
    	        width		: 350,
    	        height		: 170,
    	        x			: 40,
    	        y			: 40,
    	        resizable	: false,
                closeAction : 'hide',
                plain       : true,
                title		: 'Measure',
                modal		: false,
                items       : [mainPanel]
                          
    		});
    		
    		this.measureWindow.on('hide', this.closeMeasureWindow, this);
    		
    		this.initInnerControls();
    	}
    	this.measureWindow.show();    	
    }
    
    , closeMeasureWindow: function() {
    	Sbi.trace("[Measure.closeMeasureWindow] : IN");
    	this.removeAllControls();
    	this.measureWindow.hide(); 
    	Sbi.trace("[Measure.closeMeasureWindow] : OUT");
    }
    
    , initInnerControls: function(){
    	
    	Sbi.trace("[Measure.initInnerControls] : IN");
    	
    	Sbi.trace("[Measure.initInnerControls] : map is equla to [" + this.map + "]");
         
        // style the sketch fancy
        var sketchSymbolizers = {
            "Point": {
                pointRadius: 4,
                graphicName: "square",
                fillColor: "white",
                fillOpacity: 1,
                strokeWidth: 1,
                strokeOpacity: 1,
                strokeColor: "#333333"
            },
            "Line": {
                strokeWidth: 3,
                strokeOpacity: 1,
                strokeColor: "#666666",
                strokeDashstyle: "dash"
            },
            "Polygon": {
                strokeWidth: 2,
                strokeOpacity: 1,
                strokeColor: "#666666",
                fillColor: "white",
                fillOpacity: 0.3
            }
        };
        var style = new OpenLayers.Style();
        style.addRules([
            new OpenLayers.Rule({symbolizer: sketchSymbolizers})
        ]);
        var styleMap = new OpenLayers.StyleMap({"default": style});
        
        // allow testing of specific renderers via "?renderer=Canvas", etc
        var renderer = OpenLayers.Layer.Vector.prototype.renderers;
        
        measureControls = {
            line: new OpenLayers.Control.Measure(
                OpenLayers.Handler.Path, {
                    persist: true,
                    handlerOptions: {
                        layerOptions: {
                            renderers: renderer,
                            styleMap: styleMap
                        }
                    }
                }
            ),
            polygon: new OpenLayers.Control.Measure(
                OpenLayers.Handler.Polygon, {
                    persist: true,
                    handlerOptions: {
                        layerOptions: {
                            renderers: renderer,
                            styleMap: styleMap
                        }
                    }
                }
            )
        };
        
        var control;
        for(var key in measureControls) {
            control = measureControls[key];
            control.events.on({
                "measure": this.handleMeasurements,
                "measurepartial": this.handleMeasurements
            });
            this.map.addControl(control);
        }
        
        Sbi.trace("[Measure.initInnerControls] : OUT");
    }
    
    , handleMeasurements: function(event) {
        var geometry = event.geometry;
        var units = event.units;
        var order = event.order;
        var measure = event.measure;
        var element = document.getElementById('measureDiv');
        var out = "";
        if(order == 1) {
            out += measure.toFixed(3) + " " + units;
        } else {
            out += measure.toFixed(3) + " " + units + "<sup>2</" + "sup>";
        }
        element.innerHTML = out;
    }

    , toggleControl: function(button, pressed) {
    	if(pressed !== true) return;
    	
        for(key in measureControls) {
            var control = measureControls[key];
            if(button.mesurementType == key) {
                control.activate();
            } else {
                control.deactivate();
            }
        }
    }
    
    , removeAllControls: function() {
    	for(key in measureControls) {
            var control = measureControls[key];
            control.deactivate();
        }
    	
    	this.distanceButton.toggle(false, true);
    	this.areaButton.toggle(false, true);
    	
    	var element = document.getElementById('measureDiv');
    	element.innerHTML = "...";
    }
    
    , CLASS_NAME: 'Sbi.geo.control.Measure'
});
