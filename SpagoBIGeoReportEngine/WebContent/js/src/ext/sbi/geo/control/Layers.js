/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Functions
  * 
  *  [list]
  * 
  * 
  * Authors
  * 
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

/** 
 * @requires OpenLayers/Control.js
 * @requires OpenLayers/BaseTypes.js
 * @requires OpenLayers/Events.js
 */

/**
 * Class: Sbi.geo.control.Layers
 * Create an overview map to display the extent of your main map and provide
 * additional navigation control.  Create a new overview map with the
 * <Sbi.geo.control.Layers> constructor.
 *
 * Inerits from:
 *  - <OpenLayers.Control>
 */

Ext.ns("Sbi.geo.control");

Sbi.geo.control.Layers = OpenLayers.Class(OpenLayers.Control, {
	
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
     * Constructor: Sbi.geo.control.Layers
     * Create a new options map
     *
     * Parameters:
     * object - {Object} Properties of this object will be set on the overview
     * map object.  Note, to set options on the map object contained in this
     * control, set <mapOptions> as one of the options properties.
     */
    initialize: function(options) {  
        
    	Sbi.trace("[Layers.initialize] : IN");
    	
    	Sbi.trace("[Layers.initialize] : options are equal to [" + Sbi.toSource(options) + "]");    	
       
    	OpenLayers.Control.prototype.initialize.apply(this, [options]);
    	// ovveride the main div class automatically generated 
    	// by parent's initialize method
    	this.displayClass = "map-tools"; 
    	this.id = "MapTools"; 
    	
    	if (this.div == null) {
    		Sbi.trace("[Layers.initialize] : div is null");
    	} else {
    		Sbi.trace("[Layers.initialize] : div is not null");
    	}
    	
        
        Sbi.trace("[Layers.initialize] : OUT");
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
    	Sbi.trace("[Layers.setMap] : IN");
    	
        OpenLayers.Control.prototype.setMap.apply(this, arguments);

        this.map.events.on({
            addlayer: this.redraw,
            changelayer: this.redraw,
            removelayer: this.redraw,
            changebaselayer: this.redraw,
            scope: this
        });
        if (this.outsideViewport) {
            this.events.attachToElement(this.div);
            this.events.register("buttonclick", this, this.onButtonClick);
        } else {
            this.map.events.register("buttonclick", this, this.onButtonClick);
        }
        
        Sbi.trace("[Layers.setMap] : OUT");
    },
        
    /**
     * Method: draw
     * Render the control in the browser.
     */    

    draw: function(px) {    	
    	Sbi.trace("[Layers.draw] : IN");

    	this.div = document.getElementById("MapTools");
    	
    	
    	if(this.div != null) {
    		Sbi.trace("[Layers.draw] : a div with id equal to [MapTools] already exist");
    	} else {
    		Sbi.trace("[Layers.draw] : a div with id equal to [MapTools] does not exist");
    		this.div = document.createElement('div');
    		this.div.id = "MapTools";
    	}
    	OpenLayers.Control.prototype.draw.apply(this, arguments);
    	
    	this.div.className = this.displayClass || "";
    	
        // create overview map DOM elements
        this.createContents();
        this.redraw();
    
        Sbi.trace("[Layers.draw] : OUT");
        
        return this.div;
    },
  
    /**
     * Method: createElementsAction
     * Defines the action elements on the map
     */
    createContents: function(){

    	// create main Layers element
        this.layersElement = this.createDiv('Layers', 'map-tools-element layers');
        	
        // create legend popup window
        this.layersContentElement = this.createDiv('LayersContent', 'tools-content overlay');
        
      
        // add close button
        var legendContentCloseBtnElement = document.createElement('span');
        legendContentCloseBtnElement.className = "btn-close";
        this.layersContentElement.appendChild(legendContentCloseBtnElement);
        OpenLayers.Event.observe(legendContentCloseBtnElement, "click", 
	    		OpenLayers.Function.bindAsEventListener(this.closeLegend, this, 'close'));
       
        this.layersContentBodyElement = document.createElement('div');
        this.layersContentBodyElement.id = 'LayersBody'; // OpenLayers.Util.createUniqueID('LegendContent');   
        this.layersContentElement.appendChild(this.layersContentBodyElement);
        
        var titleElement = document.createElement("div");
        titleElement.innerHTML = " <\p> <h3>"+LN('sbi.geo.layerpanel.title')+"</h3>";
        this.layersContentBodyElement.appendChild(titleElement);
        
        var editButton = document.createElement("label");
        OpenLayers.Element.addClass(editButton, "labelSpan olButton");
        editButton.id = "EditButton";
        editButton._editButton = this.id;
        editButton.innerHTML = "<a style='color:#3d90d4;' href='#'>"+LN('sbi.geo.layerpanel.addremove')+"</a><\p>.<\p> "; 
        this.layersContentBodyElement.appendChild(editButton);
        
        // from layer switcher
        this.baseLbl = document.createElement("div");
        this.baseLbl.innerHTML = OpenLayers.i18n("Base Layer");
        OpenLayers.Element.addClass(this.baseLbl, "baseLbl");
        
        this.baseLayersDiv = document.createElement("div");
        OpenLayers.Element.addClass(this.baseLayersDiv, "baseLayersDiv");

        this.dataLbl = document.createElement("div");
        this.dataLbl.innerHTML = OpenLayers.i18n("Overlays");
        OpenLayers.Element.addClass(this.dataLbl, "dataLbl");

        this.dataLayersDiv = document.createElement("div");
        OpenLayers.Element.addClass(this.dataLayersDiv, "dataLayersDiv");
        
        
        this.layersContentBodyElement.appendChild(this.baseLbl);
        this.layersContentBodyElement.appendChild(this.baseLbl);
        this.layersContentBodyElement.appendChild(this.baseLayersDiv);
        this.layersContentBodyElement.appendChild(this.dataLbl);
        this.layersContentBodyElement.appendChild(this.dataLayersDiv);
        
        
        // create legend button
        var	legendButtonElement = document.createElement('span');
        legendButtonElement.className = 'icon';
        OpenLayers.Event.observe(legendButtonElement, "click", 
	    		OpenLayers.Function.bindAsEventListener(this.openLegend, this));
    	
	          
        // put everythings together
        this.layersElement.appendChild(legendButtonElement);
	    this.layersElement.appendChild(this.layersContentElement); 
	   
        this.div.appendChild(this.layersElement);
    },
    
    /**
     * Method: redraw
     * Goes through and takes the current state of the Map and rebuilds the
     *     control to display that state. Groups base layers into a
     *     radio-button group and lists each data layer with a checkbox.
     *
     * Returns:
     * {DOMElement} A reference to the DIV DOMElement containing the control
     */
    redraw: function() {
    	
    	Sbi.trace("[Layers.redraw] : IN");
    	
        //if the state hasn't changed since last redraw, no need
        // to do anything. Just return the existing div.
        if (!this.checkRedraw()) {
        	Sbi.trace("[Layers.redraw] : state has not changed. No redraw needed");
        	Sbi.trace("[Layers.redraw] : OUT");
            return this.div;
        }

        //clear out previous layers
        this.clearLayersArray("base");
        this.clearLayersArray("data");

        var containsOverlays = false;
        var containsBaseLayers = false;

        // Save state -- for checking layer if the map state changed.
        // We save this before redrawing, because in the process of redrawing
        // we will trigger more visibility changes, and we want to not redraw
        // and enter an infinite loop.
        var len = this.map.layers.length;
        this.layerStates = new Array(len);
        for (var i=0; i <len; i++) {
            var layer = this.map.layers[i];
            this.layerStates[i] = {
                'name': layer.name,
                'visibility': layer.visibility,
                'inRange': layer.inRange,
                'id': layer.id
            };
            Sbi.trace("[Layers.redraw] : Found layer [" + Sbi.toSource(this.layerStates[i]) + "]");
        }

        var layers = this.map.layers.slice();
        if (!this.ascending) { layers.reverse(); }
        for(var i=0, len=layers.length; i<len; i++) {
            var layer = layers[i];
            var baseLayer = layer.isBaseLayer;

            if (layer.displayInLayerSwitcher) {

            	Sbi.trace("[Layers.redraw] : Property [displayInLayerSwitcher] of layer [" + layer.name + "] is set to [true]");
            	
                if (baseLayer) {
                    containsBaseLayers = true;
                } else {
                    containsOverlays = true;
                }

                // only check a baselayer if it is *the* baselayer, check data
                //  layers if they are visible
                var checked = (baseLayer) ? (layer == this.map.baseLayer)
                                          : layer.getVisibility();

                // create input element
                var inputElem = document.createElement("input"),
                    // The input shall have an id attribute so we can use
                    // labels to interact with them.
                    inputId = OpenLayers.Util.createUniqueID(
                        this.id + "_input_"
                    );

                inputElem.id = inputId;
                inputElem.name = (baseLayer) ? this.id + "_baseLayers" : layer.name;
                inputElem.type = (baseLayer) ? "radio" : "checkbox";
                inputElem.value = layer.name;
                inputElem.checked = checked;
                inputElem.defaultChecked = checked;
                inputElem.className = "olButton";
                inputElem._layer = layer.id;
                inputElem._layerSwitcher = this.id;

                if (!baseLayer && !layer.inRange) {
                	Sbi.trace("[Layers.redraw] : The input associated to layer [" + layer.name + "] will be disabled");
                    inputElem.disabled = true;
                }

                // create span
                var labelSpan = document.createElement("label");
                // this isn't the DOM attribute 'for', but an arbitrary name we
                // use to find the appropriate input element in <onButtonClick>
                labelSpan["for"] = inputElem.id;
                OpenLayers.Element.addClass(labelSpan, "labelSpan olButton");
                labelSpan._layer = layer.id;
                labelSpan._layerSwitcher = this.id;
                if (!baseLayer && !layer.inRange) {
                    labelSpan.style.color = "gray";
                }
                labelSpan.innerHTML = layer.name;
                labelSpan.style.verticalAlign = (baseLayer) ? "bottom"
                                                            : "baseline";
                // create line break
                var br = document.createElement("br");


                var groupArray = (baseLayer) ? this.baseLayers
                                             : this.dataLayers;
                groupArray.push({
                    'layer': layer,
                    'inputElem': inputElem,
                    'labelSpan': labelSpan
                });


                var groupDiv = (baseLayer) ? this.baseLayersDiv
                                           : this.dataLayersDiv;
                groupDiv.appendChild(inputElem);
                groupDiv.appendChild(labelSpan);
                groupDiv.appendChild(br);
            } else {
            	Sbi.trace("[Layers.redraw] : Property [displayInLayerSwitcher] of layer [" + layer.name + "] is set to [false]"); 	
            }
        }

        // if no overlays, dont display the overlay label
        this.dataLbl.style.display = (containsOverlays) ? "" : "none";

        // if no baselayers, dont display the baselayer label
        this.baseLbl.style.display = (containsBaseLayers) ? "" : "none";

        return this.div;
        
        Sbi.trace("[Layers.redraw] : OUT");
    },
    
    
    /**
     * Method: checkRedraw
     * Checks if the layer state has changed since the last redraw() call.
     *
     * Returns:
     * {Boolean} The layer state changed since the last redraw() call.
     */
    checkRedraw: function() {
    	return true;
    },
    
    /**
     * Method: clearLayersArray
     * User specifies either "base" or "data". we then clear all the
     *     corresponding listeners, the div, and reinitialize a new array.
     *
     * Parameters:
     * layersType - {String}
     */
    clearLayersArray: function(layersType) {
        this[layersType + "LayersDiv"].innerHTML = "";
        this[layersType + "Layers"] = [];
    },
    
    
    // TODO: move the following utilities methods to a dedicated class
    createDiv: function(id, className) {
    	 var divElement = document.createElement('div');
    	 divElement.id = id || OpenLayers.Util.createUniqueID('Layers');   
    	 divElement.className = className || '';
    	 
    	 return divElement;
    },
    
    
    
    
    /**
     * Method: execClick
     * Executes the specific action
     */
    openLegend: function(el){
    	var controls = this.map.getControlsByClass("Sbi.geo.control.Legend");
    	for(var i = 0; i < controls.length; i++) {
    		if(controls[i].legendContentElement.opened = true) {
    			controls[i].closeLegend();
    		}
    	}
    	
    	this.layersContentElement.style.height = '200px';
    	this.layersContentElement.style.width = '180px';
    	this.layersContentElement.style.display = 'block';
    	this.layersContentElement.opened = true;
    },
    
    closeLegend: function(el){
    	this.layersContentElement.style.height = '0px';
    	this.layersContentElement.style.width = '0px';
    	this.layersContentElement.style.display = 'none';
    	this.layersContentElement.closed = true;
    },
    
    /**
     * Method: onButtonClick
     *
     * Parameters:
     * evt - {Event}
     */
    onButtonClick: function(evt) {
       var button = evt.buttonElement;
       
       if (button._layerSwitcher === this.id) {
            if (button["for"]) {
                button = document.getElementById(button["for"]);
            }
            if (!button.disabled) {
                if (button.type == "radio") {
                    button.checked = true;
                    this.map.setBaseLayer(this.map.getLayer(button._layer));
                } else {
                    button.checked = !button.checked;
                    this.updateMap();
                }
            }
        } else if(button._editButton === this.id) {
        	this.showLayersCatalogueWindow();
        }
    },
    
    showLayersCatalogueWindow: function(){
    	var thisPanel = this;
		if(this.layersCatalogueWindow==null){
			var layersCatalogue = new Sbi.geo.tools.LayersCataloguePanel(); 
			
			this.layersCatalogueWindow = new Ext.Window({
	            layout      : 'fit',
		        width		: 700,
		        height		: 350,
	            closeAction :'hide',
	            plain       : true,
	            title		: LN('sbi.geo.layerpanel.catalogue'),
	            modal		: true,
	            items       : [layersCatalogue],
	            buttons		: [{
                    text: LN('sbi.geo.layerpanel.add'),
                    handler: function(){
                    	var selectedLayers = layersCatalogue.getSelectedLayers();
                    	var unselectedLayers = layersCatalogue.getUnselectedLayers();
                    	thisPanel.addSelectedLayers(selectedLayers);
                    	thisPanel.removeUnselectedLayers(unselectedLayers);
                    	thisPanel.layersCatalogueWindow.hide();
                    }
                }]
	                      
			});
		}
		
		
		this.layersCatalogueWindow.show();
	},
	
	removeUnselectedLayers: function(layers) {
		var layerLabelsMap = {};
		var needRedraw = false;
		
		
		for(var i = 0; i < layers.length; i++) {
			layerLabelsMap[layers[i]] = layers[i];
			//alert("UNSELECT: " + layers[i]);
		}
		
		for(var i = 0; i < this.map.layers.length; i++) {
			
			if(this.map.layers[i].conf) {
				if( layerLabelsMap[this.map.layers[i].conf.label] ) {
					//alert("REMOVED: " + this.map.layers[i].conf.label + ": " + Sbi.toSource(this.map.layers[i].conf));
					this.map.removeLayer(this.map.layers[i]);
					needRedraw = true;
				} else {
					//alert("NOT REMOVED: " + this.map.layers[i].conf.label + " : " + layerLabelsMap[this.map.layers[i].label] + " - " + Sbi.toSource(this.map.layers[i].conf));
				}
			}
		}
		
		if(needRedraw === true) {
			//this.redraw();
		}
	},
	
	addSelectedLayers: function(layers) {
		var thisPanel = this;
		
		var layersLabels = new Array();

		for (var i = 0; i < layers.length; i++) {
		    var selectedLayerLabel = layers[i];
		    layersLabels.push(selectedLayerLabel);
		}
		
	    //invoke service for layers properties
		Ext.Ajax.request({
			url: Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'layers/getLayerProperties',baseUrl:{contextPath: 'SpagoBI'}}),
			params: {labels: layersLabels},
			success : function(response, options) {
				if(response !== undefined && response.responseText !== undefined && response.statusText=="OK") {
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							var obj = JSON.parse(response.responseText);
							for(var i = 0; i < obj.root.length; i++) {
								var layerConf = obj.root[i];
								layerConf.options = Ext.util.JSON.decode(layerConf.propsOptions);
								delete layerConf.propsOptions;
								if(this.map.getLayersByName(layerConf.name).length > 0) {
									Sbi.debug("[Layers.addSelectedLayers] : Map already contains layer [" + layerConf.name + "]");
								} else {
									var layer = Sbi.geo.utils.LayerFactory.createLayer(layerConf);
									layer.conf = layerConf;	
									this.map.addLayer(layer);
								}
							}
							thisPanel.redraw();
						}
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			},
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure,  
			scope: this
		});
	},
    
    /**
     * Method: updateMap
     * Cycles through the loaded data and base layer input arrays and makes
     *     the necessary calls to the Map object such that that the map's
     *     visual state corresponds to what the user has selected in
     *     the control.
     */
    updateMap: function() {

        // set the newly selected base layer
        for(var i=0, len=this.baseLayers.length; i<len; i++) {
            var layerEntry = this.baseLayers[i];
            if (layerEntry.inputElem.checked) {
                this.map.setBaseLayer(layerEntry.layer, false);
            }
        }

        // set the correct visibilities for the overlays
        for(var i=0, len=this.dataLayers.length; i<len; i++) {
            var layerEntry = this.dataLayers[i];
            layerEntry.layer.setVisibility(layerEntry.inputElem.checked);
        }

    },


    CLASS_NAME: 'Sbi.geo.control.Layers'
});
