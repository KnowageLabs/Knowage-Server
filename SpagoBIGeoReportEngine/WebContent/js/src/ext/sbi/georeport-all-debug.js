/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
 Ext.ns("Sbi.settings");

Sbi.settings.georeport = {
	
	georeportPanel: {
	
		controlPanelConf: {
			layerPanelEnabled: true
			, analysisPanelEnabled: true
			, legendPanelEnabled: true
			, logoPanelEnabled: true
			, earthPanelEnabled: false
		}	
	
		, toolbarConf: {
			enabled: true
			, zoomToMaxButtonEnabled: true
			, mouseButtonGroupEnabled: true
			, drawButtonGroupEnabled: false
			, historyButtonGroupEnabled: true
		}
	
	
			
		 
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
  */

Ext.ns("Sbi.georeport");

Sbi.georeport.GeoReportPanel = function(config) {
	
	var defaultSettings = {
		mapName: 'sbi.georeport.mappanel.title'
		, controlPanelConf: {
			layerPanelEnabled: true
			, analysisPanelEnabled: true
			, legendPanelEnabled: true
			, logoPanelEnabled: true
			, earthPanelEnabled: true
		}	
		, toolbarConf: {
			enabled: true,
			zoomToMaxButtonEnabled: true,
			mouseButtonGroupEnabled: true,
			drawButtonGroupEnabled: true,
			historyButtonGroupEnabled: true
		}
	};
		
	if(Sbi.settings && Sbi.settings.georeport && Sbi.settings.georeport.georeportPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.georeport.georeportPanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
		
		
	this.services = this.services || new Array();	
	
	var params = {
		layer: this.targetLayerConf.name
		, businessId: this.businessId
		, geoId: this.geoId
	};
	if(this.targetLayerConf.url) {
		params.featureSourceType = 'wfs';
		params.featureSource = this.targetLayerConf.url;
	} else {
		params.featureSourceType = 'file';
		params.featureSource = this.targetLayerConf.data;
	}
	
	this.services['MapOl'] = this.services['MapOl'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MapOl'
		, baseParams: params
	});
	
	
	
	
	//this.addEvents('customEvents');
		
		
	this.initMap();
	this.initMapPanel();
	this.initControlPanel();
	
	c = Ext.apply(c, {
         layout   : 'border',
         items    : [this.controlPanel, this.mapPanel]
	});

	// constructor
	Sbi.georeport.GeoReportPanel.superclass.constructor.call(this, c);
	
	this.on('render', function() {
		this.setCenter();
		if(this.controlPanelConf.earthPanelEnabled === true) {
			this.init3D.defer(500, this);
		}
		if(this.toolbarConf.enabled) {
			this.initToolbarContent.defer(500, this);	
		}
	}, this);
	
	
	
};

Ext.extend(Sbi.georeport.GeoReportPanel, Ext.Panel, {
    
    services: null
    
    , baseLayersConf: null
    , layers: null
    
    , map: null
    , lon: null
    , lat: null
    , zoomLevel: null
    
    , showPositon: null
    , showOverview: null
    , mapName: null
    , mapPanel: null
    , controlPanel: null
    
    , analysisType: null
    , PROPORTIONAL_SYMBOLS:'proportionalSymbols'
    , CHOROPLETH:'choropleth'
    
    , targetLayer: null
    , geostatistic: null
    
    // -- public methods ------------------------------------------------------------------------
    
    
    , setCenter: function(center) {
      	
		center = center || {};
      	this.lon = center.lon || this.lon;
      	this.lat = center.lat || this.lat;
      	this.zoomLevel = center.zoomLevel || this.zoomLevel;
        
        if(this.map.projection == "EPSG:900913"){            
            this.centerPoint = Sbi.georeport.GeoReportUtils.lonLatToMercator(new OpenLayers.LonLat(this.lon, this.lat));
            this.map.setCenter(this.centerPoint, this.zoomLevel);
        } else if(this.map.projection == "EPSG:4326") {
        	this.centerPoint = new OpenLayers.LonLat(this.lon, this.lat);
        	this.map.setCenter(this.centerPoint, this.zoomLevel);
        } else{
        	alert("Map Projection not supported yet!");
        }
         
     }

	 // -- private methods ------------------------------------------------------------------------
	
	
	, initMap: function() {  
		var o = this.baseMapOptions;
	
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
		
		
		this.map = new OpenLayers.Map('map', this.baseMapOptions);
		this.initLayers();
		this.initControls();  
		this.initAnalysis();
    }

	, initLayers: function(c) {
		this.layers = new Array();
				
		if(this.baseLayersConf && this.baseLayersConf.length > 0) {
			for(var i = 0; i < this.baseLayersConf.length; i++) {
				if(this.baseLayersConf[i].enabled === true) {
					var l = Sbi.georeport.LayerFactory.createLayer( this.baseLayersConf[i] );
					this.layers.push( l	);
				}
			}			
		}
		
		this.map.addLayers(this.layers);
	}
	
	, initControls: function() {
		
		if(this.baseControlsConf && this.baseControlsConf.length > 0) {
			for(var i = 0; i < this.baseControlsConf.length; i++) {
				if(this.baseControlsConf[i].enabled === true) {
					this.baseControlsConf[i].mapOptions = this.baseMapOptions;
					var c = Sbi.georeport.ControlFactory.createControl( this.baseControlsConf[i] );
					this.map.addControl( c );
				}
			}			
		}
	}
	
	
	, init3D: function(center){
		center = center || {};
		this.lon = center.lon || this.lon;
		this.lat = center.lat || this.lat;
	    
	    if(this.map.projection == "EPSG:900913"){
	        
	    	var earth = new mapfish.Earth(this.map, 'map3dContainer', {
	    		lonLat: Sbi.georeport.GeoReportUtils.lonLatToMercator(new OpenLayers.LonLat(this.lon, this.lat)),
	            altitude: 50, //da configurare
	            heading: -60, //da configurare
	            tilt: 70,     //da configurare
	            range: 700}
	    	); //da configurare
	    	
	    } else if(this.map.projection == "EPSG:4326"){
	    	
	    	var earth = new mapfish.Earth(this.map, 'map3dContainer', {
	    		lonLat: new OpenLayers.LonLat(this.lon, this.lat),
	            altitude: 50,//da configurare
	            heading: -60,//da configurare
	            tilt: 70,//da configurare
	            range: 700
	       
	    	}); //da configurare	    
	    } else{
	    	alert('Map projection [' + this.map.projection + '] not supported yet!');
	    }
	  
	  }
	
	
	
	, initAnalysis: function() {
		
		var geostatConf = {
			map: this.map,
			layer: null, // this.targetLayer not yet defined here
			indicators:  this.indicators,
			url: this.services['MapOl'],
			loadMask : {msg: 'Analysis...', msgCls: 'x-mask-loading'},
			legendDiv : 'myChoroplethLegendDiv',
			featureSelection: false,
			listeners: {}
		};

		if(this.map.projection == "EPSG:900913") {
			 geostatConf.format = new OpenLayers.Format.GeoJSON({
				 externalProjection: new OpenLayers.Projection("EPSG:4326"),
			     internalProjection: new OpenLayers.Projection("EPSG:900913")
			 });
		}
		
		
		if (this.analysisType === this.PROPORTIONAL_SYMBOLS) {
			this.initProportionalSymbolsAnalysis();
			geostatConf.layer = this.targetLayer;
			this.geostatistic = new mapfish.widgets.geostat.ProportionalSymbol(geostatConf);
			
		} else if (this.analysisType === this.CHOROPLETH) {
			this.initChoroplethAnalysis();
			geostatConf.layer = this.targetLayer;
			this.geostatistic = new mapfish.widgets.geostat.Choropleth(geostatConf);
		} else {
			alert('error: unsupported analysis type [' + this.analysisType + ']');
		}
		
		this.map.addControl(this.analysisLayerSelectControl); 
		this.analysisLayerSelectControl.activate();
		
		
	}
	
	, initProportionalSymbolsAnalysis: function() {
	
		this.targetLayer = new OpenLayers.Layer.Vector(this.targetLayerConf.text, {
				'visibility': false  ,
				'styleMap': new OpenLayers.StyleMap({
	   				'select': new OpenLayers.Style(
	       				{'strokeColor': 'red', 'cursor': 'pointer'}
	   				)
				})
		});

		this.map.addLayer(this.targetLayer);
        this.analysisLayerSelectControl = new OpenLayers.Control.SelectFeature(this.targetLayer, {} );

        this.targetLayer.events.register("featureselected", this, function(o) { 
			//alert('select -> ' + this.getInfo(o.feature));
			this.onTargetFeatureSelect(o.feature);
		}); 
        
        this.targetLayer.events.register("featureunselected", this, function(o) { 
			//alert('unselect -> ' + this.getInfo(o.feature));
			this.onTargetFeatureUnselect(o.feature);
		}); 
		
	}
	
	, initChoroplethAnalysis: function() {
		this.targetLayer = new OpenLayers.Layer.Vector(this.targetLayerConf.text, {
        	'visibility': false,
          	'styleMap': new OpenLayers.StyleMap({
            	'default': new OpenLayers.Style(
                	OpenLayers.Util.applyDefaults(
                      {'fillOpacity': 0.5},
                      OpenLayers.Feature.Vector.style['default']
                  	)
              	),
              	'select': new OpenLayers.Style(
                  {'strokeColor': 'red', 'cursor': 'pointer'}
              	)
          	})
      	});
     
    
    	this.map.addLayer(this.targetLayer);                                    
    	this.analysisLayerSelectControl = new OpenLayers.Control.SelectFeature(this.targetLayer, {});
        
        this.targetLayer.events.register("featureselected", this, function(o) { 
			//alert('select -> ' + this.getInfo(o.feature));
			this.onTargetFeatureSelect(o.feature);
		}); 
        
        this.targetLayer.events.register("featureunselected", this, function(o) { 
			//alert('unselect -> ' + this.getInfo(o.feature));
			this.onTargetFeatureUnselect(o.feature);
		}); 
	}
	
	, onTargetFeatureSelect: function(feature) {
		this.selectedFeature = feature;
		
        
       

		var params = Ext.apply({}, this.detailDocumentConf.staticParams);
		for(p in this.detailDocumentConf.dynamicParams) {
			var attrName = this.detailDocumentConf.dynamicParams[p];
			params[p] = feature.attributes[attrName];
		}
		
		//alert(params.toSource());
		
        var execDetailFn = "execDoc(";
        execDetailFn += '"' + this.detailDocumentConf.label + '",'; // documentLabel
        execDetailFn += '"' + this.role + '",'; // execution role
        execDetailFn += Ext.util.JSON.encode(params) + ','; // parameters
        execDetailFn += this.detailDocumentConf.displayToolbar + ','; // displayToolbar
        execDetailFn += this.detailDocumentConf.displaySliders + ','; // displaySliders
        execDetailFn += '"' + this.detailDocumentConf.label + '"'; // frameId
        execDetailFn += ")";
       
        var link = '';
        link += '<center>';
        link += '<font size="1" face="Verdana">';
        link += '<a href="#" onclick=\'Ext.getCmp("' + this.mapPanel.getId() + '").setActiveTab("infotable");';
        link += 'Ext.getCmp("infotable").body.dom.innerHTML=';
        link += execDetailFn + '\';>';
        link += 'Dettagli</a></font></center>';

        params = Ext.apply({}, this.inlineDocumentConf.staticParams);
		for(p in this.inlineDocumentConf.dynamicParams) {
			var attrName = this.inlineDocumentConf.dynamicParams[p];
			params[p] = feature.attributes[attrName];
		}
		
		//alert(params.toSource());
        
        var content = "<div style='font-size:.8em'>" + this.getInfo(feature);
        content += link;
        content += execDoc(
        		this.inlineDocumentConf.label, 
        		this.role, 
        		params, 
        		this.inlineDocumentConf.displayToolbar, 
        		this.inlineDocumentConf.displaySliders, 
        		this.inlineDocumentConf.label,
        		'300'
        );
       
        popup = new OpenLayers.Popup.FramedCloud("chicken", 
                feature.geometry.getBounds().getCenterLonLat(),
                null,
                content,
                null, 
                true, 
                function(evt) {
        			this.analysisLayerSelectControl.unselect(this.selectedFeature);    
                }.createDelegate(this, [])
        );
        
        feature.popup = popup;
        this.map.addPopup(popup);
	}
	
	, onTargetFeatureUnselect: function(feature) {
		this.map.removePopup(feature.popup);
        feature.popup.destroy();
        feature.popup = null;
        var infoPanel = Ext.getCmp('infotable');
        if(infoPanel.body){
        	infoPanel.body.dom.innerHTML = '';
        }
	}
	
	, getInfo: function(feature) {
		//alert(feature.attributes.toSource());
		var info = "";
	    for(var i=0; i<this.feautreInfo.length; i++){
	    	info = info+"<b>"+ this.feautreInfo[i][0] +"</b>: " + feature.attributes[this.feautreInfo[i][1]] + "<br />";    
	    } 
	    return info;
	}
	
	
	
	
	

	
	
	, initMapPanel: function() {
		
		var mapPanelConf = {
			title: LN(this.mapName),
			layout: 'fit',
	       	items: {
		        xtype: 'mapcomponent',
		        map: this.map
		    }
	    };
		
		if(this.toolbarConf.enabled) {
			this.loadingButton = new Ext.Toolbar.Button({
	            tooltip: 'Please wait',
	            iconCls: "x-tbar-loading"
	        });
			
			this.toolbar = new mapfish.widgets.toolbar.Toolbar({
		    	map: this.map, 
		        configurable: false,
		        items: [this.loadingButton, ' Loading toolbar...']
			});
			
			this.loadingButton.disable();
			
			mapPanelConf.tbar = this.toolbar;
		}
	 
		
	 
		this.mapPanel = new Ext.TabPanel({
		    region    : 'center',
		    margins   : '3 3 3 0', 
		    activeTab : 0,
		    defaults  : {
				autoScroll : true
			},

	       	items: [
		       	new Ext.Panel(mapPanelConf), {
		            title    : 'Info',
		            html: '<div id="info"</div>',
		            id: 'infotable',
		            autoScroll: true
		        }
		    ]
		});
	}
	
	, initControlPanel: function() {
		
		
		var controlPanelItems = [];
		
		if(this.controlPanelConf.earthPanelEnabled === true) {
			controlPanelItems.push({
				title: LN('sbi.georeport.earthpanel.title'),
                html: '<center id="map3dContainer"></center>',
                split: true,
                height: 300,
                minSize: 300,
                maxSize: 500,
                collapsible: false                
	        });
		}
		
		if(this.controlPanelConf.layerPanelEnabled === true) {
			controlPanelItems.push({
	        	title: LN('sbi.georeport.layerpanel.title'),
	            collapsible: true,
	            autoHeight: true,
	            xtype: 'layertree',
	            map: this.map
	        });
		}
		
		if(this.controlPanelConf.analysisPanelEnabled === true) {
			controlPanelItems.push({
	        	title: LN('sbi.georeport.analysispanel.title'),
	            collapsible: true,
	            items: [this.geostatistic]
	        });
		}
		
		
		
		if(this.controlPanelConf.legendPanelEnabled === true) {
			controlPanelItems.push({
		           title: LN('sbi.georeport.legendpanel.title'),
		           collapsible: true,
		           height: 150,
		           html: '<center id="myChoroplethLegendDiv"></center>'
		     });
		}
		
		if(this.controlPanelConf.logoPanelEnabled === true) {
			controlPanelItems.push({
		           title: 'Logo',
		           collapsible: true,
		           height: 85,
		           html: '<center><img src="/SpagoBIGeoReportEngine/img/georeport.jpg" alt="GeoReport"/></center>'
		    });
		}
		
		if(this.controlPanelConf.logoPanelEnabled === false) {
			controlPanelItems.push({
		           title: 'Debug',
		           collapsible: true,
		           height: 85,
		           items: [new Ext.Button({
				    	text: 'Debug',
				        width: 30,
				        handler: function() {
		        	   		this.init3D();
		        	   		/*
		        	   		var size = this.controlPanel.getSize();
		        	   		size.width += 1;
		        	   		this.controlPanel.refreshSize(size);
		        	   		*/
		           		},
		           		scope: this
				    })]
		    });
		}
		
		this.controlPanel = new Ext.Panel({
			 title       : LN('sbi.georeport.controlpanel.title'),
		     region      : 'west',
		     split       : true,
		     width       : 300,
		     collapsible : true,
		     margins     : '3 0 3 3',
		     cmargins    : '3 3 3 3',
		     autoScroll	 : true,
		     items		 : controlPanelItems 
		}); 
		 
		 	
	}
	

	
	, addSeparator: function(){
          this.toolbar.add(new Ext.Toolbar.Spacer());
          this.toolbar.add(new Ext.Toolbar.Separator());
          this.toolbar.add(new Ext.Toolbar.Spacer());
    } 

	, initToolbarContent: function() {
			
		this.toolbar.items.each( function(item) {
			this.toolbar.items.remove(item);
            item.destroy();           
        }, this); 
		
		var vectorLayer = new OpenLayers.Layer.Vector("vector", { 
	    	displayInLayerSwitcher: false
	    });
	    this.map.addLayer(vectorLayer);
	    
	    if(this.toolbarConf.zoomToMaxButtonEnabled === true) {
		    this.toolbar.addControl(
		        new OpenLayers.Control.ZoomToMaxExtent({
		        	map: this.map,
		            title: 'Zoom to maximum map extent'
		        }), {
		            iconCls: 'zoomfull', 
		            toggleGroup: 'map'
		        }
		    );
			
		    this.addSeparator();
	    }
	    
	    if(this.toolbarConf.mouseButtonGroupEnabled === true) {
		    this.toolbar.addControl(
		    	new OpenLayers.Control.ZoomBox({
		    		title: 'Zoom in: click in the map or use the left mouse button and drag to create a rectangle'
	            }), {
		    		iconCls: 'zoomin', 
	                toggleGroup: 'map'
		    	}
	        );
	      
			this.toolbar.addControl(
				new OpenLayers.Control.ZoomBox({
					out: true,
	                title: 'Zoom out: click in the map or use the left mouse button and drag to create a rectangle'
	            }), {
					iconCls: 'zoomout', 
	                toggleGroup: 'map'
	            }
	        );
	          
			this.toolbar.addControl(
				new OpenLayers.Control.DragPan({
					isDefault: true,
	                title: 'Pan map: keep the left mouse button pressed and drag the map'
	            }), {
	                iconCls: 'pan', 
	                toggleGroup: 'map'
	            }
	        );
	          
			this.addSeparator();
	    }
	          
	    if(this.toolbarConf.drawButtonGroupEnabled === true) {
	    	this.toolbar.addControl(
	    		new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Point, {
	    			title: 'Draw a point on the map'
	            }), {
	    			iconCls: 'drawpoint', 
	                toggleGroup: 'map'
	            }
	        );
	          
			this.toolbar.addControl(
				new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Path, {
					title: 'Draw a linestring on the map'
				}), {
	                iconCls: 'drawline', 
	                toggleGroup: 'map'
				}
	        );
	          
			this.toolbar.addControl(
				new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Polygon, {
					title: 'Draw a polygon on the map'
	            }), {
	                iconCls: 'drawpolygon', 
	                toggleGroup: 'map'
	            }
	        );
	          
			this.addSeparator();
	    }
	      
	    if(this.toolbarConf.historyButtonGroupEnabled === true) {
	    	var nav = new OpenLayers.Control.NavigationHistory();
	        this.map.addControl(nav);
	        nav.activate();
	          
	        this.toolbar.add(
	        	new Ext.Toolbar.Button({
	        		iconCls: 'back',
	                tooltip: 'Previous view', 
	                handler: nav.previous.trigger
	            })
	         );
	          
	         this.toolbar.add(
	        	new Ext.Toolbar.Button({
	        		iconCls: 'next',
	                tooltip: 'Next view', 
	                handler: nav.next.trigger
	            })
	         );
	          
	         this.addSeparator();
	    }
	          
	    this.toolbar.activate();
	    
      }
});