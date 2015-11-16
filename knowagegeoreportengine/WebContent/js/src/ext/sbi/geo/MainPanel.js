/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
Ext.ns("Sbi.geo");

/**
 * Class: Sbi.geo.MainPanel
 * Main GUI of SpagoBIGeoReportEngine
 */
Sbi.geo.MainPanel = function(config) {
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	var defaultSettings = {
		mapName: 'Sbi.geo.mappanel.title'
		, controlPanelConf: {
			analysisPanelEnabled: true
			, measurePanelEnabled: false
			, earthPanelEnabled: true
		} 
		, toolbarConf: {
			enabled: false,
			zoomToMaxButtonEnabled: true,
			mouseButtonGroupEnabled: true,
			measureButtonGroupEnabled: true,
			wmsGroupEnabled: true,
			drawButtonGroupEnabled: true,
			historyButtonGroupEnabled: true
		},
		hideBorders: true
	};
		
	if(Sbi.settings && Sbi.settings.georeport && Sbi.settings.georeport.georeportPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.georeport.georeportPanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
	c.toolbarConf.enabled = false;
	
	Ext.apply(this, c);
	
	this.controlPanelConfOrignal = Ext.apply({}, this.controlPanelConf);
	this.toolbarConfOrignal = Ext.apply({}, this.toolbarConf);
	
	this.initServices();
	this.initStore();
	
	// enable disable debug panel
	this.controlPanelConf.debugPanelEnabled = true;
	this.controlPanelConf.debugPanelConf = {
		store: this.store
	};
	
	this.init();
	
	// inline ovveride to add none as possible collapse mode for border layout 
	// TODO move it to ovverides file
	Ext.layout.BorderLayout.Region.prototype.getCollapsedEl = Ext.layout.BorderLayout.Region.prototype.getCollapsedEl.createSequence(function(){
		if(this.collapseMode == 'none'){
            this.collapsedEl.enableDisplayMode('none');
        }
    });
	
	c = Ext.apply(c, {
         layout   : 'border',
         hideBorders: true,
         items    : [this.mapPanel, this.controlPanel]
         //items: [this.mapPanel]
	});

	// constructor
	Sbi.geo.MainPanel.superclass.constructor.call(this, c);
	
	// apply after render settings
	this.on('render', function() {
		this.mapComponent.setCenter();
		if(this.toolbarConf.enabled) {
			this.toolbar.initButtons.defer(500, this.toolbar);
		}
	}, this);	
};

/**
 * @class Sbi.geo.MainPanel
 * @extends Ext.Panel
 * 
 * ...
 */
Ext.extend(Sbi.geo.MainPanel, Ext.Panel, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
    services: null
    
    , map: null
    , lon: null
    , lat: null
    , zoomLevel: null
    
    , mapName: null
    , mapPanel: null
    
    , analysisType: null

    
    , targetLayer: null
    , controlPanel: null
    

    
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
		// patch for an old typo
		if(config.feautreInfo) {
			config.featureInfo = config.feautreInfo;
			delete config.feautreInfo;
		}
	}
	
	, validate: function (successHandler, failureHandler, scope) {
		Sbi.trace("[MainPanel.validate]: IN");
		
		var template = this.getAnalysisState();
		var templeteStr = Ext.util.JSON.encode(template);
		Sbi.trace("[MainPanel.validate]: template = " + templeteStr);
		
		Sbi.trace("[MainPanel.validate]: OUT");
		return templeteStr;
	}
	
	, getAnalysisState: function () {
		Sbi.trace("[MainPanel.getAnalysisState]: IN");
		
		var analysisState = {};
		
		var thematizer = this.mapComponent.getActiveThematizer();
		
		analysisState.mapName = this.mapName;
		analysisState.analysisType = this.mapComponent.activeThematizerName;
	
		analysisState.indicatorContainer = thematizer.indicatorContainer;
		analysisState.storeType = thematizer.storeType;
		
		if(analysisState.storeType === 'virtualStore') {
			analysisState.storeConfig = thematizer.storeConfig;
		} else { // it's a physicalStore
			analysisState.feautreInfo = this.feautreInfo;
			analysisState.indicators = this.indicators;
			analysisState.businessId = this.businessId;	
		}
		
		if(thematizer.storeFilters != null) {
			var encodedFilters = new Array();
			for(var i = 0; i < thematizer.storeFilters.length; i++) {
				var encodedFilter = [thematizer.storeFilters[i].fieldHeader, thematizer.storeFilters[i].value];
				encodedFilters.push(encodedFilter);
				
			}
			analysisState.filters = encodedFilters;
		}
		
		analysisState.geoId = this.geoId;
				
		// TODO we assume that different thematizer have no property with the same name
		// this must be improved in order to manage overlapps in the respect of
		// old versions
		Sbi.trace("[MainPanel.getAnalysisState]: Reading analysis configuration from thematizers ...");
		analysisState.analysisConf = {};
		for(var t in this.mapComponent.thematizers) {
			var thematizer = this.mapComponent.thematizers[t];
			var thematizerConf = thematizer.getAnalysisConf();
			Sbi.trace("[MainPanel.getAnalysisState]: Analysis configuration of thematizer [" + thematizer.thematyzerType + "] " +
					"is equal to [" + Sbi.toSource(thematizerConf) + "]");
			analysisState.analysisConf = Ext.apply(
					analysisState.analysisConf
					, thematizerConf
			);
		}
		Sbi.trace("[MainPanel.getAnalysisState]: Analysis configuration read succesfully from thematizers. It is equal to [" + Sbi.toSource(analysisState.analysisConf) + "]");
				
		analysisState.selectedBaseLayer = this.selectedBaseLayer;
		for(var i=0; i < this.map.getNumLayers(); i++) {
			var layer = this.map.getLayerIndex(i);
			if(layer.isBaseLayer && layer.selected) {
				analysisState.selectedBaseLayer = layer.name;
			}
		}
		
		analysisState.baseLayersConf = this.mapComponent.getBaseLayersConfig();
		analysisState.overLayersConf = this.mapComponent.getOverLayersConfig();
		
		analysisState.targetLayerConf = this.targetLayerConf;

		analysisState.controlPanelConf = this.controlPanelConfOrignal;		
		analysisState.toolbarConf = this.toolbarConfOrignal;	
		
//		var mapCenterPoint = this.map.getCenter();
//		analysisState.lon = mapCenterPoint.lon;
//		analysisState.lat = mapCenterPoint.lat;
//		analysisState.zoomLevel = this.map.getZoom();
		
		analysisState.lon = this.lon;
		analysisState.lat = this.lat;
		analysisState.zoomLevel = this.zoomLevel;
		
		Sbi.trace("[MainPanel.getAnalysisState]: OUT");
		return analysisState;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------

	, isStoreVirtual: function() {
		return this.indicatorContainer === "store" && this.storeType === "virtualStore";
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	// ...

	//-----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - MapOl: ...
	 *    - GetTargetDataset: ...
	 *    - GetTargetLayer: ...
	 *    
	 */
	, initServices: function() {
		this.services = this.services || new Array();	
		
		var params = {
			layer: this.targetLayerConf? this.targetLayerConf.name: null
			, businessId: this.businessId
			, geoId: this.geoId
		};
		
		if(this.targetLayerConf) {
			if(this.targetLayerConf.url) {
				params.featureSourceType = 'wfs';
				params.featureSource = this.targetLayerConf.url;
			} else {
				params.featureSourceType = 'file';
				params.featureSource = this.targetLayerConf.data;
			}
		}
		
		
		this.services['GetTargetDataset'] = this.services['GetTargetDataset'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GetTargetDataset'
			, baseParams: params
		});

	}
	
	/**
	 * @method 
	 * 
	 * initialize store
	 */
	, initStore: function() { 
		Sbi.trace("[MainPanel.initStore]: IN");
		if(this.indicatorContainer === "store") {
			if(this.storeType === "physicalStore") {
				Sbi.debug("[MainPanel.initStore]: Store will be loaded using service [GetTargetDataset] because " +
						"property [indicatorContainer] is equal to [" + this.indicatorContainer+ "] " +
						"and property [storeType] is equal to [physicalStore");
				
				this.store = new Ext.data.JsonStore({
					url: this.services['GetTargetDataset']
					, autoLoad: false
				})
			} else if(this.storeType === "virtualStore") {
				Sbi.debug("[MainPanel.initStore]: Store will be loaded using service [MeasureJoin] because " +
						"property [indicatorContainer] is equal to [" + this.indicatorContainer+ "] " +
						"and property [storeType] is equal to [virtualStore]");
				
				this.store = null;
			} else {
				Sbi.warn("Impossible to load initialize store because the value [" + this.storeType + "] of property [storeType] is not valid");
				this.store = null;
			}
		} else {
			Sbi.debug("[MainPanel.initStore]: Store wont be loaded because property [indicatorContainer] is equal to [" + this.indicatorContainer+ "]");			
		}
		Sbi.trace("[MainPanel.initStore]: OUT");
	}
	
	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	, init: function() {
		this.initMap();
		this.initMapPanel();
		this.initControlPanel();
	}
	
	, initMap: function() {  
		this.mapComponent = new Sbi.geo.MapComponent({
			baseMapOptions : this.baseMapOptions
			, baseLayersConf : this.baseLayersConf
			, baseControlsConf: this.baseControlsConf
			, baseCentralPointConf: {
				lon: this.lon
				, lat: this.lat
				, zoomLevel: this.zoomLevel
			}
			, mainPanel: this
		});
		
		this.map = this.mapComponent.map;
		this.initAnalysis();
	}
	
	, initAnalysis: function() {
		
		Sbi.trace("[MainPanel.initAnalysis]: IN");
		
		if(!this.indicatorContainer === "layer")  {
			for (var i = 0; i < this.indicators.length; i++){
				this.indicators[i][0] = this.indicators[i][0].toUpperCase();
			}
		}
		
		this.indicatorContainer = this.indicatorContainer  || 'layer';
		
		var loadLayerServiceName = null
		if(this.indicatorContainer == 'layer') {
			loadLayerServiceName = 'MapOl';
		} else if(this.indicatorContainer == 'store') {
			loadLayerServiceName = 'GetTargetLayer';
		}
		
		var featureSourceType = null;
		var featureSource = null;
		
		if(this.targetLayerConf) {
			if(this.targetLayerConf.url) {
				featureSourceType = 'wfs';
				featureSource = this.targetLayerConf.url;
			} else {
				featureSourceType = 'file';
				featureSource = this.targetLayerConf.data;
			}
		}
			
		var thematizerOptions = {
	       	'layer': null,
	        'layerName': this.targetLayerConf? this.targetLayerConf.name: null,
	        'layerId' : this.geoId,
	      	'loadLayerServiceName': loadLayerServiceName,
	         	
	        'format': null,
	        'featureSourceType': featureSourceType,
	        'featureSource': featureSource,
	        		
	      	'featureSelection': false,
	       	'nameAttribute': null,
	        	        
	       	'indicatorContainer': this.indicatorContainer || 'layer',
	       	'storeType': this.storeType || 'physicalStore',
	       	'storeConfig': this.storeConfig,
	       	'store': this.store,
	       	'storeId' : this.businessId,
	        		       
	        'legendDiv': 'LegendBody',
	        'labelGenerator': this.labelGenerator      	
	    };
		
	
		
		if(this.map.projection == "EPSG:900913") {
			 var format =  new OpenLayers.Format.GeoJSON({
				 externalProjection: new OpenLayers.Projection("EPSG:4326"),
			     internalProjection: new OpenLayers.Projection("EPSG:900913")
			 });
			 thematizerOptions.format = format;
		}
		
		this.initChoroplethAnalysisLayer();
		thematizerOptions.layer = this.targetLayer;
		
		for(var typeName in Sbi.geo.stat.Thematizer.supportedType) {
			var thematizerType = Sbi.geo.stat.Thematizer.supportedType[typeName];
			var thematizer = new thematizerType.thematizerClass(this.map, thematizerOptions);
			this.mapComponent.addThematizer(thematizerType.typeName, thematizer);
			Sbi.debug("[MainPanel.initAnalysis]: thematizer [" + thematizerType.typeName + "] succesfully initialized");
		}
		
		var thematizerType = Sbi.geo.stat.Thematizer.supportedType[this.analysisType];
		if (thematizerType !== undefined) {
			Sbi.debug("[MainPanel.initAnalysis]: active thematizer is equal to [" + thematizerType.typeName + "]");
			this.mapComponent.activateThematizer(this.analysisType);
		} else {
			Sbi.exception.ExceptionHandler.showErrorMessage('Unsupported thematizer type [' + this.analysisType + ']', 'Configuration error');
		}
		
		
		
		
		this.initAnalysislayerSelectControl();
		this.map.addControl(this.analysisLayerSelectControl); 
		//this.analysisLayerSelectControl.activate();
		
		Sbi.trace("[MainPanel.initAnalysis]: OUT");
	}
	
	, initProportionalSymbolsAnalysisLayer: function() {
		Sbi.trace("[MainPanel.initProportionalSymbolsAnalysisLayer]: IN");
		
		var layerName = this.targetLayerConf? this.targetLayerConf.text: 'Thematized layer';
		this.targetLayer = new OpenLayers.Layer.Vector(layerName, {
				'visibility': false  ,
				'styleMap': new OpenLayers.StyleMap({
	   				'select': new OpenLayers.Style(
	       				{'strokeColor': 'red', 'cursor': 'pointer'}
	   				)
				})
		});

		this.map.addLayer(this.targetLayer);		
		
		Sbi.trace("[MainPanel.initProportionalSymbolsAnalysisLayer]: OUT");
	}
	
	, initChoroplethAnalysisLayer: function() {
		Sbi.trace("[MainPanel.initChoroplethAnalysisLayer]: IN");
		
		var layerName = this.targetLayerConf? this.targetLayerConf.text: 'Thematized layer';
		this.targetLayer = new OpenLayers.Layer.Vector(layerName, {
        	'visibility': true,
          	'styleMap': new OpenLayers.StyleMap({
            	'default': new OpenLayers.Style(
                	OpenLayers.Util.applyDefaults(
                      {'fillOpacity': 0.8},
                      OpenLayers.Feature.Vector.style['default']
                  	)
              	),
              	'select': new OpenLayers.Style(
                  {'strokeColor': 'red', 'cursor': 'pointer'}
              	)
          	})
      	});
     
    
    	this.map.addLayer(this.targetLayer);       
    	
    	Sbi.trace("[MainPanel.initChoroplethAnalysisLayer]: OUT");
	}
	
	, initMapPanel: function() {
		
		//this.mapComponent = new Sbi.geo.MapComponent({map: this.map});
		var mapPanelConf = {
			title: LN(this.mapName),
			layout: 'fit',
			margins     : '0 0 0 0',
			cmargins    : '0 0 0 0',
			hideCollapseTool : true,
			hideBorders: true,
			border		: false,
			frame: false,
	       	items: [this.mapComponent]
	    };
		
		if(this.toolbarConf.enabled) {
			this.toolbarConf.map = this.map;
			this.toolbarConf.analysisLayerSelectControl = this.analysisLayerSelectControl;
			this.toolbarConf.featureHandler = this.featureHandler;
			//this.toolbar = new Sbi.geo.Toolbar(this.toolbarConf);
			mapPanelConf.tbar = this.toolbar;
		}

//since SpagoBI4.2 the detail document 'old style' isn't available. There is standard cross navigation management.
//		if(this.detailDocumentConf) {
//			
//			this.mapPanel = new Ext.TabPanel({
//			    region    : 'center',
//			    margins   : '0 0 0 0', 
//			    activeTab : 0,
//			    defaults  : {
//					autoScroll : true
//				},
//		       	items: [
//		       	   new Ext.Panel(mapPanelConf), 
//		       	   {
//			            title    : 'Info',
//			            html: '<div id="info"></div>',
//			            id: 'infotable',
//			            autoScroll: true
//			        }
//		       	]
//			});
//		} else {
//			delete mapPanelConf.title;
//			var m = new Ext.Panel(mapPanelConf);
//			
//			this.mapPanel = new Ext.Panel({
//			    region    : 'center',
//			    margins     : '0 0 0 0',
//				cmargins    : '0 0 0 0',
//				hideCollapseTool : true,
//				hideBorders: true,
//				border		: false,
//				frame: false,
//				layout: 'fit',
//			    defaults  : {
//					autoScroll : true
//				},
//		       	items: [m]
//			});
//			
//			this.map.mapComponent = this.mapComponent;
//
//		}	
		
		delete mapPanelConf.title;
		var m = new Ext.Panel(mapPanelConf);
		
		this.mapPanel = new Ext.Panel({
		    region    : 'center',
		    margins     : '0 0 0 0',
			cmargins    : '0 0 0 0',
			hideCollapseTool : true,
			hideBorders: true,
			border		: false,
			frame: false,
			layout: 'fit',
		    defaults  : {
				autoScroll : true
			},
	       	items: [m]
		});
		
		this.map.mapComponent = this.mapComponent;
	}
	
	, initControlPanel: function() {		
		this.controlPanelConf.map = this.map;
		this.controlPanelConf.mapComponnet = this.mapComponent;
		this.controlPanelConf.indicators = this.indicators;
		
		if(this.filters) {
			this.controlPanelConf.filterValues = new Array();
			for(var i = 0; i < this.filters.length; i++) {
				var filter = this.filters[i];
				Sbi.debug("[MainPanel.initControlPanel]: added value [" + filter[1] + "] for filter [" + filter[0] + "]");
				this.controlPanelConf.filterValues.push({fieldHeader: filter[0], value: filter[1]});
			}
		}
	
		this.controlPanelConf.controlledPanel = this;
		this.controlPanelConf.analysisType = this.analysisType;
		this.controlPanelConf.analysisConf = this.analysisConf;
		this.controlPanel = new Sbi.geo.ControlPanel(this.controlPanelConf);
	}
	
	// --------------------------------------------------------------------------------------------------
	// SELECTION Control
	// --------------------------------------------------------------------------------------------------
	
	, initAnalysislayerSelectControl: function() {
		this.analysisLayerSelectControl = new OpenLayers.Control.SelectFeature(
        		this.targetLayer
        		, {
        			multiple: true
        			, toggle: true
        			, box: true
        		}
        );

		this.featureHandler = new OpenLayers.Handler.Feature(
				this, this.targetLayer, {click: this.onTargetFeatureClick}
	    );
		this.featureHandler.activate();
		
		this.targetLayer.events.register("beforefeaturesadded", this, function(o) { 
			this.map.xfeatures = o.features;
		}); 
		
		this.targetLayer.events.register("featureselected", this, function(o) { 
			this.onTargetFeatureSelect(o.feature);
		}); 
        
        this.targetLayer.events.register("featureunselected", this, function(o) { 
			this.onTargetFeatureUnselect(o.feature);
		}); 
	}
	
	, onTargetFeatureClick: function(feature) {
		Sbi.trace("[MainPanel.onTargetFeatureClick]: IN");
		if(Ext.isEmpty(this.detailDocumentConf)) {
			this.detailDocumentConf = [];
		} 
		if(!Ext.isArray( this.detailDocumentConf )) {
			this.detailDocumentConf = [this.detailDocumentConf];
		}
		
		if(this.selectMode && this.selectMode == 'detail'){
			this.openPopup(feature);
		}else if(this.selectMode && this.selectMode == 'cross'){
			this.execCrossNav(feature);
		}else{
			this.openPopup(feature); //default
		}
		Sbi.trace("[MainPanel.onTargetFeatureClick]: OUT");
	}
	
	
	, onTargetFeatureSelect: function(feature) {
		Sbi.trace("[MainPanel.onTargetFeatureSelect]: IN");
		Sbi.trace("[MainPanel.onTargetFeatureSelect]: OUT");
	}
	
	, onTargetFeatureUnselect: function(feature) {
		Sbi.trace("[MainPanel.onTargetFeatureUnselect]: IN");
		Sbi.trace("[MainPanel.onTargetFeatureUnselect]: OUT");
	}
	
	
	// ==========================================================================================
	//	Utility methods used to create contextual popup win
	// ==========================================================================================
	
	, openPopup: function(feature) {
		Sbi.trace("[MainPanel.openPopup]: IN");
		var content = '';
		content += this.getFeatureInfoHtmlFragment(feature);
//		content += this.getDetailDocHtmlFragment(feature);
//		content += this.getInlineDocHtmlFragment(feature);

		var onPopupCloseFn = function(evt) {
			this.closePopup(feature);
        }.createDelegate(this, []);
        
        if( Ext.isEmpty(feature.popup) === false ) {
        	this.closePopup(feature);
        }
        
        Sbi.trace("[MainPanel.openPopup]: feature is equal to [" + feature + "]");
        feature.popup = 
        	new OpenLayers.Popup.FramedCloud(
        		Ext.id(), 
                feature.geometry.getBounds().getCenterLonLat(),
                new OpenLayers.Size(200, 150),
                content,
                null, 
                true, 
                onPopupCloseFn
        );
        feature.popup.contentDisplayClass = feature.popup.displayClass + " no-print";
        
        this.map.addPopup(feature.popup);
        
        Sbi.trace("[MainPanel.openPopup]: OUT");
	}
	
	, closePopup: function(feature) {
		if(feature.popup){
			this.map.removePopup(feature.popup);
			feature.popup.destroy();
			feature.popup = null;
		}
        var infoPanel = Ext.getCmp('infotable');
        if(infoPanel && infoPanel.body){
        	infoPanel.body.dom.innerHTML = '';
        }
	}
	
	
	// -----------------------------
	// Feature info part
	// -----------------------------
	
	, getFeatureInfoHtmlFragment: function(feature) {
		var info = "<div style='font-size:.8em'>";
		
		// TODO: we ignore feature info as passed in by template for the moment. 
		//Improve this in the feature. Maybe the feture's attribute to show can be read by
		// hierarchy level metadata
		
//		if(this.featureInfo) {
//		    for(var i=0; i<this.featureInfo.length; i++){
//		    	info = info+"<b>"+ this.featureInfo[i][0] +"</b>: " + feature.attributes[this.featureInfo[i][1]] + "<br />";    
//		    } 
//		} 
		
		// TODO put as title the geoID
		// todo read also measure... how?
		info += "<h1>" + feature.attributes[this.geoId] + "</h1><p>---------------<p> ";  
		
		for(var attribute in feature.attributes) {
			info += "<b>"+ attribute +"</b>: " + feature.attributes[attribute] + "<br />";    
		}
		
		// TODO read also measure... how?
		
	    info += "</div>";
	    
	    // TODO iprove style (css)
	    
	    return info;
	}
	
	// -----------------------------
	// Detail part
	// -----------------------------
	
	, getDetailDocHtmlFragment: function(feature) {
		var content  = '';
		
		for(var i = 0, l = this.detailDocumentConf.length; i < l; i++) {
			var params = this.getDetailDocParams(this.detailDocumentConf[i], feature);
			      
	        var execDetailFn =  this.getDetailDocExecFn(this.detailDocumentConf[i], params);
	       
	        this.detailDocumentConf[i].text = this.detailDocumentConf[i].text || 'Details';
	        
	        var link = this.getDetailDocExecLink(this.detailDocumentConf[i], execDetailFn);
	        
	        content += link;
		}
		
		return content;
	}
	
	, getDetailDocParams: function(detailDocumentConf, feature) {
		var params;
		var msgErr = ""; 
		
		params = Ext.apply({}, detailDocumentConf.staticParams);
		
		for(p in detailDocumentConf.dynamicParams) {
//			Since SpagoBI4.2 scope information is mandatory ('env','dataset','feature');
			
			var param = detailDocumentConf.dynamicParams[p];
			 
//			if (this.indicatorContainer === undefined || this.indicatorContainer === 'layer'){
//				//original management
//				var attrName = detailDocumentConf.dynamicParams[p].toUpperCase();			
//				params[p] = feature.attributes[attrName];
//			}else 
			if (param.scope === 'feature') {		            	  
	               for(p in param) {
	            	   if(p === 'scope') continue;	
	            	   var attrName = param[p];
	            	   var attrValue = feature.attributes[attrName];
	                   if( attrValue === undefined) {         			                   
	                    msgErr += 'Parameter "' + param[p] + '" undefined into the feature.<p>';
			           } else {			        	   			
						   params[p] = attrValue; 
			           }
	               }
            } else if (param.scope === 'env'){ 		            	  
	               for(p in param) { 
		                if(p === 'scope') continue;			              			            	  		                
		                  var tmpNamePar =  param[p];
		            	  if (p !== this.USER_ID && this.executionContext[tmpNamePar] === undefined) {
		            		   msgErr += 'Parameter "' + tmpNamePar + '" undefined into request. <p>';
	                    } else { 	                  
	                    		params[p] = this.executionContext[tmpNamePar]; 
	                    }
	               }
           } else if ((this.indicatorContainer !== undefined || this.indicatorContainer !== 'layer') && 
        		   			param.scope === 'dataset'){ 		            	  
               for(p in param) { 
	                if(p === 'scope') continue;			
	                var filterValue =  feature.attributes[this.geoId];
	                if (filterValue == undefined || filterValue == null || filterValue == ''){
	                	msgErr += 'Filter value '+ this.geoId+' has not a valid value for the feature.<p>';
	                }else{
	                	//var field = this.getStoreFieldByHeader(this.businessId);
	                	var firstRecord = this.store.getAt(0);	  
	                	var fieldJoin = null;
	                	var fieldFilter = null;	                	
	            	 	for(var n = 0; n < firstRecord.fields.getCount(); n++) {
	            	 		var f = firstRecord.fields.itemAt(n);
	            	 		if(f.header == this.businessId) {
	            	 			fieldJoin = f;	            	 			
	            	 		}else if (f.header == param[p]){
	            	 			fieldFilter = f;
	            	 		}
	            	 		if (fieldJoin != null && fieldFilter != null)
	            	 			break;
	            	 	}
	            	 	
	                	var filterRecIdx = this.store.find(fieldJoin.dataIndex, filterValue);
	                	if (filterRecIdx > -1){
	                		var filterRec = this.store.getAt(filterRecIdx);
		                	var fieldValue = filterRec.get(fieldFilter.dataIndex);
		                	if (fieldValue === undefined) {
			            		msgErr += 'Column "' + param[p] + '" undefined into the dataset. <p>';			            	
		                    } else {                                   
		                    	params[p]  = fieldValue;
		                   }
	                	}else
	                		msgErr += 'Column "' + this.businessId + '" with value "'+ filterValue +'" undefined into the dataset. <p>';
	                }
              }
      }	                 
     }
	 if  (msgErr != ""){
		Sbi.exception.ExceptionHandler.showWarningMessage(msgErr, 'Service Warning');
     }	
		
	 return params;
	}
	
	
	, getDetailDocExecFn: function(detailDocumentConf, detailDocParams) {
		var execDetailFn = "execDoc(";
        execDetailFn += '"' + detailDocumentConf.label + '",'; // documentLabel
        execDetailFn += '"' + this.role + '",'; // execution role
        execDetailFn += Ext.util.JSON.encode( detailDocParams ) + ','; // parameters
        execDetailFn += detailDocumentConf.displayToolbar + ','; // displayToolbar
        execDetailFn += detailDocumentConf.displaySliders + ','; // displaySliders
        execDetailFn += '"' + detailDocumentConf.label + '"'; // frameId
        execDetailFn += ")";
        
        return execDetailFn;
	} 
	
	, getDetailDocExecLink: function(detailDocumentConf, detailDocFn) {
		var link = '';
        
		link += '<center>';
        link += '<font size="1" face="Verdana">';
        link += '<a href="#" onclick=\'Ext.getCmp("' + this.mapPanel.getId() + '").setActiveTab("infotable");';
        link += 'Ext.getCmp("infotable").body.dom.innerHTML=';
        link += detailDocFn + '\';>';
        link += detailDocumentConf.text + '</a></font></center>';
        
        return link;
	}
	
	// -----------------------------
	// Inline doc part
	// -----------------------------
	
	, getInlineDocHtmlFragment: function(feature) {
		var content = '';
		
		if(Ext.isEmpty(this.inlineDocumentConf)) return content;
		
		var params = Ext.apply({}, this.inlineDocumentConf.staticParams);
		for(p in this.inlineDocumentConf.dynamicParams) {
			var attrName = this.inlineDocumentConf.dynamicParams[p].toUpperCase();
			params[p] = feature.attributes[attrName];
		}
		
        content += execDoc(
        		this.inlineDocumentConf.label, 
        		this.role, 
        		params, 
        		this.inlineDocumentConf.displayToolbar, 
        		this.inlineDocumentConf.displaySliders, 
        		this.inlineDocumentConf.label,
        		'300'
        );
        
        return content;
	}
	
	, execCrossNav: function(feature){	
//		this.showMask();
		var separator = '';
		
		var msg = {
			label: this.crossnav.label
	    	, windowName: this.name	||  parent.name // parent.name is used in document composition context			
	    	, typeCross: 'INTERNAL' //for manage correctly the IE workaround in document composition context 
	    }; 
		
		msg.parameters = '';
		var params = this.getDetailDocParams(this.crossnav, feature);
		for(p in params) {
		  	var values = params[p];
        	if (Ext.isArray(values)){
        		//multiple values management (...&P1=val1&P1=val2&P1=val3...)
        		for (var i=0; i< values.length; i++){
        			msg.parameters += separator + p + '=' + ((values[i]==='%')?'%25':values[i]);
        		}
        	} else
        		msg.parameters += separator + p + '=' + ((params[p]==='%')?'%25':params[p]);
			separator = '&';
		}
		if (this.executionContext.EXECUTION_CONTEXT !== undefined && 
			this.executionContext.EXECUTION_CONTEXT === 'DOCUMENT_COMPOSITION'){							
			//document composition context				
			if (params.typeCross == undefined || 
				   (params.typeCross !== undefined && params.typeCross == 'INTERNAL')){
				//internal cross	
				var frameName = "iframe_" + this.executionContext.DOCUMENT_LABEL;
	    		parent.execCrossNavigation(frameName, msg.label ,  msg.parameters );
	    		
			}
		}else{
			sendMessage(msg, 'crossnavigation');
		}
//			this.hideMask.defer(2000, this);
	}
});