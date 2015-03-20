/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 

/**
 * Class: Sbi.geo.control.Legend
 * Create an overview map to display the extent of your main map and provide
 * additional navigation control.  Create a new overview map with the
 * <Sbi.geo.control.Legend> constructor.
 *
 * Inerits from:
 *  - <OpenLayers.Control>
 */

Ext.ns("Sbi.geo.control");

Sbi.geo.control.Legend = OpenLayers.Class(OpenLayers.Control, {
	
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
     * Constructor: Sbi.geo.control.Legend
     * Create a new options map
     *
     * Parameters:
     * object - {Object} Properties of this object will be set on the overview
     * map object.  Note, to set options on the map object contained in this
     * control, set <mapOptions> as one of the options properties.
     */
    initialize: function(options) {  
        
    	Sbi.trace("[Legend.initialize] : IN");
    	
    	Sbi.trace("[Legend.initialize] : options are equal to [" + Sbi.toSource(options) + "]");    	
       
    	OpenLayers.Control.prototype.initialize.apply(this, [options]);
    	// ovveride the main div class automatically generated 
    	// by parent's initialize method
    	this.displayClass = "map-tools"; 
    	this.id = "MapTools"; 
    	
    	if (this.div == null) {
    		Sbi.trace("[Legend.initialize] : div is null");
    	} else {
    		Sbi.trace("[Legend.initialize] : div is not null");
    	}
    	
        
        Sbi.trace("[Legend.initialize] : OUT");
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
    	Sbi.trace("[Legend.draw] : IN");
    	
    	this.div = document.getElementById("MapTools");
    	    	
    	if(this.div != null) {
    		Sbi.trace("[Legend.draw] : a div with id equal to [MapTools] already exist");
    	} else {
    		Sbi.trace("[Legend.draw] : a div with id equal to [MapTools] does not exist");
    		this.div = document.createElement('div');
    		this.div.id = "MapTools";
    	}
    	
    	OpenLayers.Control.prototype.draw.apply(this, arguments);
    	this.div.className = this.displayClass || "";
    	
        // create overview map DOM elements
        this.createContents();
    
        Sbi.trace("[Legend.draw] : OUT");
        
        return this.div;
    },
  
    /**
     * Method: createElementsAction
     * Defines the action elements on the map
     */
    createContents: function(){

    	// create main legend element
        this.legendElement = document.createElement('div');
        this.legendElement.id = 'Legend'; //OpenLayers.Util.createUniqueID('Legend');   
        this.legendElement.className = 'map-tools-element legend';
        
        // create legend popup window
        this.legendContentElement = document.createElement('div');
        this.legendContentElement.id = 'LegendContent'; // OpenLayers.Util.createUniqueID('LegendContent');   
        this.legendContentElement.className = "tools-content overlay"; 
        
        var legendContentCloseBtnElement = document.createElement('span');
        legendContentCloseBtnElement.className = "btn-close";
        this.legendContentElement.appendChild(legendContentCloseBtnElement);
        OpenLayers.Event.observe(legendContentCloseBtnElement, "click", 
	    		OpenLayers.Function.bindAsEventListener(this.closeLegend, this, 'close'));
       
        
        var titleElement = document.createElement("div");
        titleElement.innerHTML = " <\p> <h3>"+LN('sbi.geo.legendpanel.title')+"</h3><\p> <\p>";
        this.legendContentElement.appendChild(titleElement);
        
        var legendEditButton = document.createElement("label");
        OpenLayers.Element.addClass(legendEditButton, "labelSpan olButton");
        legendEditButton.id = "LegendEditButton";
        legendEditButton._legendEditButton = this.id;
        legendEditButton.innerHTML = "<a style='color:#3d90d4;' href='#'>"+LN('sbi.geo.legendpanel.changeStyle')+"</a><\p>.<\p> ";
        this.legendContentElement.appendChild(legendEditButton);
        
        var legendContentBodyElement = document.createElement('div');
        legendContentBodyElement.id = 'LegendBody'; // OpenLayers.Util.createUniqueID('LegendContent');   
        this.legendContentElement.appendChild(legendContentBodyElement);
        
        // create legend button
        var	legendButtonElement = document.createElement('span');
        legendButtonElement.className = 'icon';
        OpenLayers.Event.observe(legendButtonElement, "click", 
	    		OpenLayers.Function.bindAsEventListener(this.openLegend, this));
    	
	          
        // put everythings together
        this.legendElement.appendChild(legendButtonElement);
	    this.legendElement.appendChild(this.legendContentElement); 
	   
        this.div.appendChild(this.legendElement);
    },
    
    
    /**
     * Method: execClick
     * Executes the specific action
     */
    openLegend: function(el){
    	var controls = this.map.getControlsByClass("Sbi.geo.control.Layers");
    	for(var i = 0; i < controls.length; i++) {
    		if(controls[i].layersContentElement.opened = true) {
    			controls[i].closeLegend();
    		}
    	}
    	
    	
    	this.legendContentElement.style.height = '200px';
    	this.legendContentElement.style.width = '180px';
    	this.legendContentElement.style.display = 'block';
    	this.legendContentElement.opened = true;
    },
    
    closeLegend: function(el){
    	this.legendContentElement.style.height = '0px';
    	this.legendContentElement.style.width = '0px';
    	this.legendContentElement.style.display = 'none';
    	this.legendContentElement.closed = true;
    },
    
   
    /**
     * Method: onButtonClick
     *
     * Parameters:
     * evt - {Event}
     */
    onButtonClick: function(evt) {
    	var button = evt.buttonElement;
       
    	if(button._legendEditButton === this.id) {
    		this.showThematizerConfigurationWindow();
    	}
    },
    
    thematizerConfigurationWindow: null,
    
    showThematizerConfigurationWindow: function(){
    	Sbi.trace("[Legend.showThematizerConfigurationWindow]: IN");
		
    	if(this.thematizerConfigurationWindow === null){
    		Sbi.debug("[Legend.showThematizerConfigurationWindow]: creating window...");
			
    		var thematizerType = Sbi.geo.stat.Thematizer.supportedType[this.map.thematizer.thematyzerType];
			if(thematizerType) {
				Sbi.debug("[ControlPanel.initAnalysisControlPanel]: analysis type is equal to [" + thematizerType.typeName + "]");
				var thematizerControlPanelOptions = {
						map: this.map,
						manageIndicator: false,
						indicators: null,	
						thematizer: this.map.thematizer,
						bodyStyle:'padding:6px 6px 6px 6px; background-color:#FFFFFF'
					};
				
				this.thematizerControlPanel = new thematizerType.controlPanelClass(thematizerControlPanelOptions);
				Sbi.debug("[Legend.showThematizerConfigurationWindow]: control panel for [" + this.map.thematizer.thematyzerType + "] thematizer succesfully created");
			} else {
				Sbi.exception.ExceptionHandler.showErrorMessage('error: unsupported analysis type [' + this.analysisType + ']', 'Configuration error');
			}
			
			this.thematizerConfigurationWindow = new Ext.Window({
	            layout      : 'fit',
		        width		: 350,
		        height		: 310,
		        x			: 80,
		        y			: 30,
		        resizable	: false,
	            closeAction : 'close',
	            plain       : true,
	            title		: 'Style settings',
	            modal		: true,
	            items       : [this.thematizerControlPanel],
	            buttons		: [
	             {
                    text:'Ok',
                    handler: function(){
                    	var themathizerOptions = this.thematizerControlPanel.getThemathizerOptions();
                        this.map.thematizer.thematize(themathizerOptions);
                        this.thematizerConfigurationWindow.close();
                    },
                    scope: this
                } , {
                    text:'Cancel',
                    handler: function(){
                    	if(this.thematizerConfigurationWindow.optionChanged === true) {
                    		 this.map.thematizer.thematize( this.thematizerConfigurationWindow.oldOptions );
                    		 //this.thematizerControlPanel.synchronizeFormState();
                    	}
                    	this.thematizerConfigurationWindow.close();
                    }, 
                    scope: this
                    
                }, {
                    text:'Apply',
                    handler: function(){
                    	var themathizerOptions = this.thematizerControlPanel.getThemathizerOptions();
                    	this.map.thematizer.thematize(themathizerOptions);
                    	this.thematizerConfigurationWindow.optionChanged = true;
                    },
                    scope: this
	            }]
	                      
			});
			
			Sbi.debug("[Legend.showThematizerConfigurationWindow]: window successfully created");
		}
		
		this.thematizerConfigurationWindow.on("show", function(win) {
			win.oldOptions = this.thematizerControlPanel.getThemathizerOptions();
			win.optionChanged = false;
		}, this);
		this.thematizerConfigurationWindow.on("close", function(win) {
			this.thematizerConfigurationWindow = null;
		}, this);
		
		this.thematizerConfigurationWindow.show();
		
		Sbi.trace("[Legend.showThematizerConfigurationWindow]: OUT");
	},
	

    CLASS_NAME: 'Sbi.geo.control.Legend'
});
