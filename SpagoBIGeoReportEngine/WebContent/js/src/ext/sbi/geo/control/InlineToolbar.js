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
 * Class: OpenLayers.Control.ActionsMap
 * Create an overview map to display the extent of your main map and provide
 * additional navigation control.  Create a new overview map with the
 * <OpenLayers.Control.ActionsMap> constructor.
 *
 * Inerits from:
 *  - <OpenLayers.Control>
 */

Ext.ns("Sbi.geo.control");

Sbi.geo.control.InlineToolbar = OpenLayers.Class(OpenLayers.Control, {
	
	
	mainPanel: null,


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
     * class name olControlSbiActionsMapElement) may have padding or other style
     * attributes added via CSS.
     */
    size: new OpenLayers.Size(35,200),

    /**
     * APIProperty: layers
     * {Array(<OpenLayers.Layer>)} Ordered list of layers in the overview map.
     * If none are sent at construction, the base layer for the main map is used.
     */
    layers: null,
        
    /**
     * APIProperty: mapOptions
     * {Object} An object containing any non-default properties to be sent to
     * the overview map's map constructor.  These should include any non-default
     * options that the main map was constructed with.
     */
    mapOptions: null,
    
    /**
     * Property: handlers
     * {Object}
     */
    handlers: null,

    /**
     * Property: resolutionFactor
     * {Object}
     */
    resolutionFactor: 1,
    
    /** 
     * Property: buttons
     * {Array(DOMElement)} Array of Button Divs 
     */
    buttons: null,

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
     * Constructor: Sbi.geo.control.InlineToolbar
     * Create a new options map
     *
     * Parameters:
     * object - {Object} Properties of this object will be set on the overview
     * map object.  Note, to set options on the map object contained in this
     * control, set <mapOptions> as one of the options properties.
     */
    initialize: function(options) {
    	
    	Sbi.trace("[InlineToolbar.initialize] : IN");

    	Sbi.trace("[InlineToolbar.initialize] : options are equal to [" + Sbi.toSource(options) + "]");    	
     
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        this.displayClass = "panel-actions"; 
        //this.displayClass = "toolbar-actions"; 
         
        Sbi.trace("[InlineToolbar.initialize] : OUT");
    },
    
    /**
     * APIMethod: destroy
     * Deconstruct the control
     */
    destroy: function() {
        if (!this.mapDiv) { // we've already been destroyed
            return;
        }
        this.handlers.click.destroy();

        this.ovmap.destroy();
        this.ovmap = null;
        
        this.element.removeChild(this.mapDiv);
        this.mapDiv = null;

        this.div.removeChild(this.element);
        this.element = null;

        OpenLayers.Control.prototype.destroy.apply(this, arguments);    
    },

    /**
     * Method: draw
     * Render the control in the browser.
     */    
    draw: function() {
    	
    	Sbi.trace("[InlineToolbar.draw] : IN");

        OpenLayers.Control.prototype.draw.apply(this, arguments);
        this.div.className = this.div.className + " no-print";
    
        // create overview map DOM elements
        this.createContents();
       

        Sbi.trace("[InlineToolbar.draw] : OUT");
        
        return this.div;
    },
    

    /**
     * Method: createElementsAction
     * Defines the action elements on the map
     */
    createContents: function(){
    	Sbi.trace("[InlineToolbar.createContents] : IN");
         
    	var actionsPanel = document.createElement('ul');
    	actionsPanel.className = "list-actions";;
    	 
    	this.div.appendChild( actionsPanel );
        
    	actionsPanel.appendChild(this.createLIEl('span', 'Collapse control panel', 'btn-toggle first open', 'elBtnArrow' ));        
    	actionsPanel.appendChild(this.createLIEl('span', 'Print this map', 'btn-print', 'elBtnPrint' ));
    	actionsPanel.appendChild(this.createLIEl('span', 'Share this map', 'btn-share last', 'elBtnShare' ));
//      actionsPanel.appendChild(this.createLIEl('a', 'Download this map', 'btn-download last', 'elBtnDownload' ));
//      actionsPanel.appendChild(this.createLIEl('a', 'Make this map favourite', 'btn-favourite last', 'elBtnFavourite' ));
        
               
        Sbi.trace("[InlineToolbar.createContents] : OUT");
    },
    
    /**
     * Method: createLIEl
     * Creates the single element of the action list
     * 
     * Parameters:
     * d - {String} Element for the detail (ie. 'span', 'a')
     * t - {String} Text of detail element
     * c - {String} Class name to use
     * id - {String} Identifier of the element
     */
    createLIEl: function(d, t, c, id ){
	    var toReturn = document.createElement('li');
	    var	toReturnDet = document.createElement(d);
	    if (t !== null && t !== undefined){
	    	toReturnDet.innerHTML = t;    
	    } 
	    
	    toReturn.appendChild(toReturnDet);
	    toReturn.className = c;
	    toReturn.id = OpenLayers.Util.createUniqueID(id);  
	    OpenLayers.Event.observe(toReturn, "click", OpenLayers.Function.bindAsEventListener(this.execClick, this));
		    
		return toReturn;
    },
    
    /**
     * Method: execClick
     * Executes the specific action
     */
    execClick: function(event){
    	var targetEl = null;
    	if(event.currentTarget) {
    		targetEl = event.currentTarget 
    	} else {
    		targetEl = event.srcElement.parentNode;
    	}
    	
    	if (targetEl.id.indexOf('elBtnArrow')>=0){
    		if(this.mainPanel.controlPanel.collapsed) {
    			targetEl.className += ' open';
    			this.mainPanel.controlPanel.setVisible(true);
    			this.mainPanel.controlPanel.expand();
    		} else {
    			targetEl.className = targetEl.className.replace(/\bopen\b/,'');
    			this.mainPanel.controlPanel.collapse();
    			this.mainPanel.controlPanel.setVisible(false);
    		}
    	} else if (targetEl.id.indexOf('elBtnPrint')>=0){
    	     window.print();
    	} else if (targetEl.id.indexOf('elBtnShare')>=0){
    		this.showShareMapWindow('elBtnShare');
    	} else if (targetEl.id.indexOf('elBtnDownload')>=0){
    		var printProvider = new GeoExt.data.PrintProvider({
                capabilities: {"scales":[{"name":"1:25.000","value":"25000"},{"name":"1:50.000","value":"50000"},{"name":"1:100.000","value":"100000"},{"name":"1:200.000","value":"200000"},{"name":"1:500.000","value":"500000"},{"name":"1:1.000.000","value":"1000000"},{"name":"1:2.000.000","value":"2000000"},{"name":"1:4.000.000","value":"4000000"}],"dpis":[{"name":"56","value":"56"},{"name":"127","value":"127"},{"name":"190","value":"190"},{"name":"256","value":"256"}],"outputFormats":[{"name":"pdf"}],"layouts":[{"name":"A4 portrait","map":{"width":440,"height":483},"rotation":true}],"printURL":"http://localhost:8080/SpagoBIGeoReportEngine/pdf/print.pdf","createURL":"http://localhost:8080/SpagoBIGeoReportEngine/pdf/create.json"},
                customParams: {
                    mapTitle: "Printing Demo",
                    title: "Printing Demo",
                    comment: "This is a simple map printed from GeoExt."
                }
        	});
            var printPage = new GeoExt.data.PrintPage({
                printProvider: printProvider
            });
            printPage.fit(this.map, true);
            printProvider.print(this.map, printPage);
    	} else if (targetEl.id.indexOf('elBtnFavourite')>=0){
    		alert('elBtnFavourite');
    	}
    },

    
    showShareMapWindow: function(type){
		if(this.shareMapWindow != null){			
			this.shareMapWindow.destroy();
			this.shareMapWindow.close();
		}
		var shareMapLink = this.getShareMapContent('link');	 //type : 'link' or 'html'	
		shareMapLink.title = 'Link';
		var shareMapHtml = this.getShareMapContent('html');	 
		shareMapHtml.title = 'Html';
		var shareMapPanel = new Ext.TabPanel({
			 					bodyStyle:'padding:5px',
								activeTab: 0,
								items:[shareMapLink,shareMapHtml ]});
		shareMapPanel.addListener('tabchange', this.onActivate, this);		
		
		this.shareMapWindow = new Ext.Window({
            layout      : 'fit',
	        width		: 700,
	        height		: 130,
            closeAction :'destroy',
            plain       : true,
            resizable	: false,
            title		: LN('sbi.geo.controlpanel.control.share.title'),
            items       : [shareMapPanel],
            modal		: true
		});
		
		this.shareMapWindow.show();
	},
	
	getShareMapContent: function(type){
		var toReturn = '';
		var url = ''+Sbi.config.serviceRegistry.baseUrl.protocol +'://' + Sbi.config.serviceRegistry.baseUrl.host+':'+
		 		  Sbi.config.serviceRegistry.baseUrl.port+'/SpagoBI/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE&DIRECT_EXEC=TRUE&'+
		 		  'OBJECT_LABEL='+ Sbi.config.docLabel+'&OBJECT_VERSION=' + Sbi.config.docVersion+'&IS_FROM_SHARE=TRUE';
		
		 
		if (type=='link'){
			toReturn = new Ext.form.TextArea({
		  		  fieldLabel: 'Map link:' 
		  			  , name: 'shareTextLink'
			          , width: 690 
					  , xtype : 'textarea'
					  , hideLabel: false
					  , multiline: true
			          , margin: '0 0 0 0'
			          , readOnly: true
			          , selectOnFocus: true
			          , labelStyle:'font-weight:bold;'
			          , value: url
			          , listeners: {
			              afterrender: function(field) {
			                field.focus();
			              }
			          }
			        });
			 
		}else if (type == 'html'){
			var htmlCode = '<iframe name="htmlMap"  width="100%" height="100%"  src="'+url+'"></iframe>';
			toReturn = new Ext.form.TextArea({
		  		  fieldLabel: 'Map html:' 
		  			  , name: 'shareTextHtml'
			          , width: 690 
					  , xtype : 'textarea'
					  , hideLabel: false
					  , multiline: true
					  , selectOnFocus: true
			          , margin: '0 0 0 0'
			          , readOnly: true
			          , labelStyle:'font-weight:bold;'
			          , value: htmlCode
			          , listeners: {
			              afterrender: function(field) {
			                field.focus();
			              }
			          }
			        });
		}else{
			alert('WARNING: Is possible to share only the link url or the html of the map!');
		}
		return toReturn;
	},
	
	onActivate: function(el, t){
		t.focus(false,100);
	},

    CLASS_NAME: 'Sbi.geo.control.InlineToolbar'
});