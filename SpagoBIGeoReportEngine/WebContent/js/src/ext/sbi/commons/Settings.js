/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
 Ext.ns("Sbi.settings");

Sbi.settings.georeport = {
		
	georeportPanel: {
	
	  	controlPanelConf: {
	   		analysisPanelEnabled: true
	   		, measurePanelEnabled: false
	   		, earthPanelEnabled: false
	   		, scopeInfoEnabled: false //if true shows the scope options when is possible
	  	} 
	 
	  	, toolbarConf: {
	  		enabled: false
	  		, zoomToMaxButtonEnabled: true
	  		, mouseButtonGroupEnabled: true
	  		, measureButtonGroupEnabled: true
	  		, wmsGroupEnabled: true
	  		, drawButtonGroupEnabled: true
	  		, historyButtonGroupEnabled: true
	  	}
	
	  	, saveWindow:{
	  		showDetailBar: true
	  	  , showSaveButton: false //if true shows the save button at the end of page
	  	}
			
		/**
		 * base map configuration's options passed to the constructor of OpenLayers Map Object 
		 * 
		 * @see http://dev.openlayers.org/docs/files/OpenLayers/Map-js.html#OpenLayers.Map.Constructor
		 */
	
		, baseMapOptions: {
	        
			/**
			 * {String} Set in the map options to override the default projection string this map - also set maxExtent, maxResolution, 
			 * and units if appropriate.  Default is “EPSG:4326”.
			 */
			projection: 'EPSG:900913',
	        
			/**
			 * {String} Requires proj4js support.Projection used by several controls to display data to user.  
			 * If this property is set, it will be set on any control which has a null displayProjection property at 
			 * the time the control is added to the map.
			 */
			displayProjection: 'EPSG:4326',
	        
			/**
			 * {String} The map units.  Defaults to 'degrees'.  Possible values are 'degrees' (or 'dd'), 'm', 'ft', 'km', 'mi', 'inches'.
			 */
			units: "m",
			
			/**
			 * {Float} Default max is 360 deg 256 px, which corresponds to zoom level 0 on gmaps.  
			 * Specify a different value in the map options if you are not using a geographic projection and displaying the whole world.
			 */
	        maxResolution: 156543.0339,
	        
	        /**
	         * {Object} The maximum extent for the map.  Defaults to the whole world in decimal degrees (-180, -90, 180, 90).  
	         * Specify a different extent in the map options if you are not using a geographic projection and displaying the whole world.
	         */
	        
			maxExtent: {
				left: -20037508, 
				bottom: -20037508,
				right:20037508, 
				top: 20037508.34
			}  
			
		}
	
		
		/**
	 	 * configurations of layers that must be loaded at startup
	 	 * 
	 	 * layer configuration is an object composed by the following attributes ...
	 	 * 	- name {String} A name for the layer (can be localized)
	 	 * 	- url {String} Base url for the WMS (e.g.  http://wms.jpl.nasa.gov/wms.cgi) 
	 	 * 	- params {Object} An object with key/value pairs representing the GetMap query string parameters and parameter values. 
	 	 * 	- options {Ojbect} Hashtable of extra options to tag onto the layer 
	 	 * 
	 	 * for more informations see:
	 	 * 		http://dev.openlayers.org/releases/OpenLayers-2.7/doc/apidocs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS.OpenLayers.Layer.WMS
	     */ 
		
		, selectedBaseLayer: "OpenStreetMap"
		, baseLayersConf: [
		    {
		    	type: 'WMS',
		    	name: "OpenLayers WMS",
     	        url: "http://labs.metacarta.com/wms/vmap0",
     	        params: {layers: 'basic'},
     	        options: {singleTile: false}, 
				enabled: false
     	    }, 
     	    {
     	    	type: 'WMS',
     	    	name: "NASA Global Mosaic",
     	    	url: "http://hypercube.telascience.org/cgi-bin/landsat7?",
     	    	params: {layers: "landsat7"},
     	    	options: {'isBaseLayer': true}, 
				enabled: false
     	    }, 
     	    {
     	    	type: 'WMS',
     	    	name: "Satellite",
     	    	url: "http://labs.metacarta.com/wms-c/Basic.py?",
     	    	params: {layers: 'satellite', format: 'image/png'}, 
				enabled: false
     	    }, 
     	    {
     	    	type: 'TMS',
     	    	name: 'OpenStreetMap',
     	    	url:  "http://tile.openstreetmap.org/",
     	    	options: {
     	    		type: 'png', 
     	    		//getURL: this.osm_getTileURL,
    		        displayOutsideMaxExtent: true
    		    }, 
				enabled: true
    		}/*, {
    			type: 'Google',
    			name: 'Google Physical',
    			options:  {type: google.maps.MapTypeId.TERRAIN},
				enabled: true
    		}, {
    			type: 'Google',
    			name: 'Google Streets',
    			options:  {numZoomLevels: 20},
				enabled: true
    		}, {
    			type: 'Google',
    			name: 'Google Hybrid',
    			options: {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20},
				enabled: true
    		}, {
    			type: 'Google',
    			name: 'Google Satellite',
    			options:  {type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22},
    			enabled: true
    		}  */   		
     	]
     	
     	/**
		 * Controls affect the display or behavior of the map.  They allow everything from panning and zooming 
		 * to displaying a scale indicator.
		 */
		, baseControlsConf: [
			
		   /**
		    * The navigation control handles map browsing with mouse events (dragging, double-clicking, and scrolling the wheel)
		    */
		    {
		    	type: 'Navigation', 
		    	enabled: true
		    },		
		    
		    /**
		     * The KeyboardDefaults control adds panning and zooming functions, controlled with the keyboard.  
		     * By default arrow keys pan, +/- keys zoom & Page Up/Page Down/Home/End scroll by three quarters of a page.
		     */
		    {
		    	type: 'KeyboardDefaults', 
		    	enabled: true
		    },
		    
			/**
			 * The MousePosition control displays geographic coordinates of the mouse pointer, as it is moved about the map.
			 */
			{
				type: 'MousePosition', 
				enabled: true
			},
			
			/**
			 * The OverMap control creates a small overview map, useful to display the extent of a zoomed map and your main map and provide 
			 * additional navigation options to the User. 
			 * By default the overview map is drawn in the lower right corner of the main map.
			 */
			{
				type: 'OverviewMap', 
				enabled: false
			}, 
			
			/**
			 * The PanZoomBar is a visible control. By default it is displayed in the upper left corner of the map as 4 
			 * directional arrows above a vertical slider.
			 */
			{
				type: 'PanZoomBar', 
				enabled: false
			},
			
			/**
			 * The SbiActionsMap is a visible control. By default it is displayed in the right corner of the map as 
			 * a div with multiple options like print, favourites,....
			 */
			{
				type: 'SbiActionsMap', 
				enabled: true
			},
			
			/**
			 * The SbiLegendMap is a visible control. By default it is displayed in the right corner of the map as 
			 * a button to view the legend
			 */
			{
				type: 'SbiLegendMap', 
				enabled: true
			},
			
			/**
			 * The SbiLayersMap is a visible control. By default it is displayed in the right corner of the map as 
			 * a button to view the layers
			 */
			{
				type: 'SbiLayersMap', 
				enabled: true
			}
			
,
			
			/**
			 * The SbiLayersMap is a visible control. By default it is displayed in the right corner of the map as 
			 * a button to view the measures (i.e. distance & area)
			 */
			{
				type: 'SbiMeasureMap', 
				enabled: true
			}
		]     	
		
	}

	,tools:{
		measurecatalogue:{
			//The list of columns in the measure catalogue
			columnsRef: ['alias','dsName', 'dsCategory', 'dsType'],
			//The properties of the measure to show in the detail of the measure
			measuresProperties: [{header:'Alias', dataIndex:'alias'},{header:'Type', dataIndex:'classType'},{header:'Column', dataIndex:'columnName'}],
			//The properties of the dataset to show in the detail of the measure
			datasetsProperties: [{header:'Name', dataIndex:'dsName'},{header:'Label', dataIndex:'dsLabel'},{header:'Category', dataIndex:'dsCategory'},{header:'Type', dataIndex:'dsType'}],
			//The properties considered by the filter
			filteringProperties:['alias','dsName', 'dsLabel', 'dsCategory', 'dsType']
		}
	} 
};

Sbi.settings.widgets = {
			TreeLookUpField : {
				//true to allow the selection of the internal node of the tree driver
				//false to allow the selection only foe the leafs
				allowInternalNodeSelection: true
			}
			//Details for specific file upload management (ex: img for document preview,...)
		  , FileUploadPanel: {
				imgUpload: {
					maxSizeFile: 10485760
				  , directory: '/preview/images' //starting from /resources directory
				  , extFiles: ['BMP', 'IMG', 'JPG', 'PNG', 'GIF']
				}
			}
};

