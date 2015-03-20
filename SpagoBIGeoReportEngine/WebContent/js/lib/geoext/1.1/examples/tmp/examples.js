/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[attribute-form]
 *  Attribute Form
 *  --------------
 *  Create a form with fields from attributes read from a WFS
 *  DescribeFeatureType response
 */

var form;

Ext.onReady(function() {
    Ext.QuickTips.init();

    // create attributes store
    var attributeStore = new GeoExt.data.AttributeStore({
        url: "data/describe_feature_type.xml"
    });

    form = new Ext.form.FormPanel({
        renderTo: document.body,
        autoScroll: true,
        height: 300,
        width: 350,
        defaults: {
            width: 120,
            maxLengthText: "too long",
            minLengthText: "too short"
        },
        plugins: [
            new GeoExt.plugins.AttributeForm({
                attributeStore: attributeStore,
                recordToFieldOptions: {
                    labelTpl: new Ext.XTemplate(
                        '{name}{[this.getStar(values)]}', {
                            compiled: true,
                            disableFormats: true,
                            getStar: function(v) {
                                return v.nillable ? '' : ' *';
                            }
                        }
                    )
                }
            })
        ]
    });

    attributeStore.load();
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[attributes]
 *  Attribute Store & Reader
 *  ------------------------
 *  Create records with attribute types and values with an AttributeStore.
 */

var store;
Ext.onReady(function() {
    
    // create a new attributes store
    store = new GeoExt.data.AttributeStore({
        url: "data/describe_feature_type.xml"
    });
    store.load();

    // create a grid to display records from the store
    var grid = new Ext.grid.GridPanel({
        title: "Feature Attributes",
        store: store,
        cm: new Ext.grid.ColumnModel([
            {id: "name", header: "Name", dataIndex: "name", sortable: true},
            {id: "type", header: "Type", dataIndex: "type", sortable: true}
        ]),
        sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
        autoExpandColumn: "name",
        renderTo: document.body,
        height: 300,
        width: 350
    });    

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[feature-grid]
 *  Grid with Features
 *  ------------------
 *  Synchronize selection of features between a grid and a layer.
 */

var mapPanel, store, gridPanel, mainPanel;

Ext.onReady(function() {
    // create map instance
    var map = new OpenLayers.Map();
    var wmsLayer = new OpenLayers.Layer.WMS(
        "vmap0",
        "http://vmap0.tiles.osgeo.org/wms/vmap0",
        {layers: 'basic'}
    );

    // create vector layer
    var vecLayer = new OpenLayers.Layer.Vector("vector");
    map.addLayers([wmsLayer, vecLayer]);

    // create map panel
    mapPanel = new GeoExt.MapPanel({
        title: "Map",
        region: "center",
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 6
    });
 
    // create feature store, binding it to the vector layer
    store = new GeoExt.data.FeatureStore({
        layer: vecLayer,
        fields: [
            {name: 'name', type: 'string'},
            {name: 'elevation', type: 'float'}
        ],
        proxy: new GeoExt.data.ProtocolProxy({
            protocol: new OpenLayers.Protocol.HTTP({
                url: "data/summits.json",
                format: new OpenLayers.Format.GeoJSON()
            })
        }),
        autoLoad: true
    });

    // create grid panel configured with feature store
    gridPanel = new Ext.grid.GridPanel({
        title: "Feature Grid",
        region: "east",
        store: store,
        width: 320,
        columns: [{
            header: "Name",
            width: 200,
            dataIndex: "name"
        }, {
            header: "Elevation",
            width: 100,
            dataIndex: "elevation"
        }],
        sm: new GeoExt.grid.FeatureSelectionModel() 
    });

    // create a panel and add the map panel and grid panel
    // inside it
    mainPanel = new Ext.Panel({
        renderTo: "mainpanel",
        layout: "border",
        height: 400,
        width: 920,
        items: [mapPanel, gridPanel]
    });
});

/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[layercontainer]
 *  Layer Tree
 *  ----------
 *  Create a layer tree with a LayerContainer.
 */

var store, tree, panel;
Ext.onReady(function() {
    
    // create a new WMS capabilities store
    store = new GeoExt.data.WMSCapabilitiesStore({
        url: "data/wmscap.xml"
    });
    // load the store with records derived from the doc at the above url
    store.load();

    // create a grid to display records from the store
    var grid = new Ext.grid.GridPanel({
        title: "WMS Capabilities",
        store: store,
        cm: new Ext.grid.ColumnModel([
            {header: "Name", dataIndex: "name", sortable: true},
            {id: "title", header: "Title", dataIndex: "title", sortable: true}
        ]),
        sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
        autoExpandColumn: "title",
        renderTo: "capgrid",
        height: 300,
        width: 350,
        floating: true,
        x: 10,
        y: 0,
        bbar: ["->", {
            text: "Add Layer",
            handler: function() {
                var record = grid.getSelectionModel().getSelected();
                if(record) {
                    var copy = record.copy();
                    // Ext 3.X does not allow circular references in objects passed 
                    // to record.set 
                    copy.data["layer"] = record.getLayer();
                    copy.getLayer().mergeNewParams({
                        format: "image/png",
                        transparent: "true"
                    });
                    panel.layers.add(copy);
                    panel.map.zoomToExtent(
                        OpenLayers.Bounds.fromArray(copy.get("llbbox"))
                    );
                }
            }
        }]
    });
    
    // create a map panel
    panel = new GeoExt.MapPanel({
        renderTo: "mappanel",
        width: 350,
        height: 300,
        floating: true,
        x: 570,
        y: 0
    });
    
    tree = new Ext.tree.TreePanel({
        renderTo: "tree",
        root: new GeoExt.tree.LayerContainer({
            text: 'Map Layers',
            layerStore: panel.layers,
            leaf: false,
            expanded: true
        }),
        enableDD: true,
        width: 170,
        height: 300,
        floating: true,
        x: 380,
        y: 0
    });
    

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[layeropacityslider]
 *  Layer Opacity Slider
 *  --------------------
 *  Use a slider to control layer opacity.
 */

var panel1, panel2, wms, slider;

Ext.onReady(function() {
    
    wms = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"}
    );

    // create a map panel with an embedded slider
    panel1 = new GeoExt.MapPanel({
        title: "Map 1",
        renderTo: "map1-container",
        height: 300,
        width: 400,
        map: {
            controls: [new OpenLayers.Control.Navigation()]
        },
        layers: [wms],
        extent: [-5, 35, 15, 55],
        items: [{
            xtype: "gx_opacityslider",
            layer: wms,
            vertical: true,
            height: 120,
            x: 10,
            y: 10,
            plugins: new GeoExt.LayerOpacitySliderTip({template: '<div>Opacity: {opacity}%</div>'})
        }]
    });
    // create a separate slider bound to the map but displayed elsewhere
    slider = new GeoExt.LayerOpacitySlider({
        layer: wms,
        aggressive: true, 
        width: 200,
        isFormField: true,
        inverse: true,
        fieldLabel: "opacity",
        renderTo: "slider",
        plugins: new GeoExt.LayerOpacitySliderTip({template: '<div>Transparency: {opacity}%</div>'})
    });
        
    var clone = wms.clone();
    var wms2 = new OpenLayers.Layer.WMS(
        "OpenLayers WMS",
        "http://vmap0.tiles.osgeo.org/wms/vmap0",
        {layers: 'basic'}
    );
    panel2 = new GeoExt.MapPanel({
        title: "Map 2",
        renderTo: "map2-container",
        height: 300,
        width: 400,
        map: {
            controls: [new OpenLayers.Control.Navigation()]
        },
        layers: [wms2, clone],
        extent: [-5, 35, 15, 55],
        items: [{
            xtype: "gx_opacityslider",
            layer: clone,
            complementaryLayer: wms2,
            changeVisibility: true,
            aggressive: true,
            vertical: true,
            height: 120,
            x: 10,
            y: 10,
            plugins: new GeoExt.LayerOpacitySliderTip()
        }]
    });
    
    var tree = new Ext.tree.TreePanel({
        width: 145,
        height: 300,
        renderTo: "tree",
        root: new GeoExt.tree.LayerContainer({
            layerStore: panel2.layers,
            expanded: true
        })
    });

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[legendpanel]
 *  Legend Panel
 *  ------------
 *  Display a layer legend in a panel.
 */


var mapPanel, legendPanel;

Ext.onReady(function() {
    var map = new OpenLayers.Map({allOverlays: true});
    map.addLayers([
        new OpenLayers.Layer.WMS(
            "Tasmania",
            "http://demo.opengeo.org/geoserver/wms?",
            {layers: 'topp:tasmania_state_boundaries', format: 'image/png', transparent: true},
            {singleTile: true}),
        new OpenLayers.Layer.WMS(
            "Cities and Roads",
            "http://demo.opengeo.org/geoserver/wms?",
            {layers: 'topp:tasmania_cities,topp:tasmania_roads', format: 'image/png', transparent: true},
            {singleTile: true}),
        new OpenLayers.Layer.Vector('Polygons', {styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    pointRadius: 8,
                    fillColor: "#00ffee",
                    strokeColor: "#000000",
                    strokeWidth: 2
                }) }) })
    ]);
    map.layers[2].addFeatures([
        new OpenLayers.Feature.Vector(OpenLayers.Geometry.fromWKT(
            "POLYGON(146.1 -41, 146.2 -41, 146.2 -41.1, 146.1 -41.1)"))
    ]);
    map.addControl(new OpenLayers.Control.LayerSwitcher());

    var addRemoveLayer = function() {
        if(mapPanel.map.layers.indexOf(water) == -1) {
            mapPanel.map.addLayer(water);
        } else {
            mapPanel.map.removeLayer(water);
        }
    };

    var moveLayer = function(idx) {
        var layer = layerRec0.getLayer();
        var idx = mapPanel.map.layers.indexOf(layer) == 0 ?
            mapPanel.map.layers.length : 0;
        mapPanel.map.setLayerIndex(layerRec0.getLayer(), idx);
    };

    var toggleVisibility = function() {
        var layer = layerRec1.getLayer();
        layer.setVisibility(!layer.getVisibility());
    };

    var updateHideInLegend = function() {
        layerRec0.set("hideInLegend", !layerRec0.get("hideInLegend"));
    };

    var updateLegendUrl = function() {
        var url = layerRec0.get("legendURL");
        layerRec0.set("legendURL", otherUrl);
        otherUrl = url;
    };

    mapPanel = new GeoExt.MapPanel({
        region: 'center',
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(146.4, -41.6),
        zoom: 7
    });
    
    // give the record of the 1st layer a legendURL, which will cause
    // UrlLegend instead of WMSLegend to be used
    var layerRec0 = mapPanel.layers.getAt(0);
    layerRec0.set("legendURL", "http://demo.opengeo.org/geoserver/wms?FORMAT=image%2Fgif&TRANSPARENT=true&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetLegendGraphic&EXCEPTIONS=application%2Fvnd.ogc.se_xml&LAYER=topp%3Atasmania_state_boundaries");

    // store the layer that we will modify in toggleVis()
    var layerRec1 = mapPanel.layers.getAt(1);

    // stores another legendURL for the legendurl button action
    var otherUrl = "http://www.geoext.org/trac/geoext/chrome/site/img/GeoExt.png";

    // create another layer for the add/remove button action
    var water = new OpenLayers.Layer.WMS("Bodies of Water",
        "http://demo.opengeo.org/geoserver/wms?",
        {layers: 'topp:tasmania_water_bodies', format: 'image/png', transparent: true},
        {singleTile: true});

    legendPanel = new GeoExt.LegendPanel({
        defaults: {
            labelCls: 'mylabel',
            style: 'padding:5px'
        },
        bodyStyle: 'padding:5px',
        width: 350,
        autoScroll: true,
        region: 'west'
    });

    new Ext.Panel({
        title: "GeoExt LegendPanel Demo",
        layout: 'border',
        renderTo: 'view',
        height: 400,
        width: 800,
        tbar: new Ext.Toolbar({
            items: [
                {text: 'add/remove', handler: addRemoveLayer},
                {text: 'movetop/bottom', handler: moveLayer },
                {text: 'togglevis', handler: toggleVisibility},
                {text: 'hide/show', handler: updateHideInLegend},
                {text: 'legendurl', handler: updateLegendUrl}
            ]
        }),
        items: [legendPanel, mapPanel]
    });
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[mappanel-div]
 *  Map Panel
 *  ---------
 *  Render a map panel in any block level page element.
 */

var mapPanel;

Ext.onReady(function() {
    Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
    var map = new OpenLayers.Map();
    var layer = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"}
    );
    map.addLayer(layer);

    mapPanel = new GeoExt.MapPanel({
        title: "GeoExt MapPanel",
        renderTo: "mappanel",
        stateId: "mappanel",
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4,
        // getState and applyState are overloaded so panel size
        // can be stored and restored
        getState: function() {
            var state = GeoExt.MapPanel.prototype.getState.apply(this);
            state.width = this.getSize().width;
            state.height = this.getSize().height;
            return state;
        },
        applyState: function(state) {
            GeoExt.MapPanel.prototype.applyState.apply(this, arguments);
            this.width = state.width;
            this.height = state.height;
        }
    });
});

// functions for resizing the map panel
function mapSizeUp() {
    var size = mapPanel.getSize();
    size.width += 40;
    size.height += 40;
    mapPanel.setSize(size);
}
function mapSizeDown() {
    var size = mapPanel.getSize();
    size.width -= 40;
    size.height -= 40;
    mapPanel.setSize(size);
}

/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[mappanel-viewport]
 *  Map Panel (in a Viewport)
 *  -------------------------
 *  Render a map panel in a viewport.
 */

var mapPanel;

Ext.onReady(function() {

    // if true a google layer is used, if false
    // the bluemarble WMS layer is used
    var google = false;

    var options, layer;
    var extent = new OpenLayers.Bounds(-5, 35, 15, 55);

    if (google) {

        options = {
            projection: new OpenLayers.Projection("EPSG:900913"),
            units: "m",
            numZoomLevels: 18,
            maxResolution: 156543.0339,
            maxExtent: new OpenLayers.Bounds(-20037508, -20037508,
                                             20037508, 20037508.34)
        };

        layer = new OpenLayers.Layer.Google(
            "Google Satellite",
            {type: G_SATELLITE_MAP, sphericalMercator: true}
        );

        extent.transform(
            new OpenLayers.Projection("EPSG:4326"), options.projection
        );

    } else {
        layer = new OpenLayers.Layer.WMS(
            "Global Imagery",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {layers: "bluemarble"},
            {isBaseLayer: true}
        );
    }

    var map = new OpenLayers.Map(options);

    new Ext.Viewport({
        layout: "border",
        items: [{
            region: "north",
            contentEl: "title",
            height: 50
        }, {
            region: "center",
            id: "mappanel",
            title: "Map",
            xtype: "gx_mappanel",
            map: map,
            layers: [layer],
            extent: extent,
            split: true
        }, {
            region: "east",
            title: "Description",
            contentEl: "description",
            width: 200,
            split: true
        }]
    });

    mapPanel = Ext.getCmp("mappanel");
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[mappanel-window]
 *  Map Panel (in a Window)
 *  -------------------------
 *  Render a map panel in a Window.
 */

var mapPanel;

Ext.onReady(function() {
    new Ext.Window({
        title: "GeoExt MapPanel Window",
        height: 400,
        width: 600,
        layout: "fit",
        items: [{
            xtype: "gx_mappanel",
            id: "mappanel",
            layers: [new OpenLayers.Layer.WMS(
                "Global Imagery",
                "http://maps.opengeo.org/geowebcache/service/wms",
                {layers: "bluemarble"}
            )],
            extent: "-5,35,15,55"
        }]
    }).show();
    
    mapPanel = Ext.getCmp("mappanel");
});
/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[permalink]
 *  Permalink
 *  ---------
 *  Display a permalink each time the map changes position.
 */

var permalinkProvider;

Ext.onReady(function() {

    // set a permalink provider
    permalinkProvider = new GeoExt.state.PermalinkProvider({encodeType: false});
    Ext.state.Manager.setProvider(permalinkProvider);

    var map = new OpenLayers.Map();
    map.addLayers([
        new OpenLayers.Layer.WMS(
            "Imagery",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {layers: "bluemarble"}
        ),
        new OpenLayers.Layer.WMS(
            "OSM",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {layers: "openstreetmap"}
        )
    ]);
    map.addControl(new OpenLayers.Control.LayerSwitcher());

    var mapPanel = new GeoExt.MapPanel({
        title: "GeoExt MapPanel",
        renderTo: "mappanel",
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4,
        stateId: "map",
        prettyStateKeys: true
    });

    // update link when state chnages
    var onStatechange = function(provider) {
        var l = provider.getLink();
        Ext.get("permalink").update("<a href=" + l + ">" + l + "</a>");
    };
    permalinkProvider.on({statechange: onStatechange});
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[popup]
 *  Feature Popup
 *  -------------
 *  Display a popup with feature information, which is positioned automatically.
 */

var mapPanel, popup;

Ext.onReady(function() {

    // create a vector layer, add features into it
    var vectorLayer = new OpenLayers.Layer.Vector("vector");
    vectorLayer.addFeatures([
        new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(-75, 45)
        ), new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(+75, -45)
        ), new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(+75, +45)
        ), new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(-75, -45)
        )]

    );

    // create select feature control
    var selectCtrl = new OpenLayers.Control.SelectFeature(vectorLayer);

    // define "createPopup" function
    var bogusMarkup = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
    function createPopup(feature) {
        popup = new GeoExt.Popup({
            title: 'My Popup',
            location: feature,
            width:200,
            html: bogusMarkup,
            maximizable: true,
            collapsible: true,
            anchorPosition: "auto"
        });
        // unselect feature when the popup
        // is closed
        popup.on({
            close: function() {
                if(OpenLayers.Util.indexOf(vectorLayer.selectedFeatures,
                                           this.feature) > -1) {
                    selectCtrl.unselect(this.feature);
                }
            }
        });
        popup.show();
    }

    // create popup on "featureselected"
    vectorLayer.events.on({
        featureselected: function(e) {
            createPopup(e.feature);
        }
    });

    // create Ext window including a map panel
    var mapwin = new Ext.Window({
        layout: "fit",
        title: "Map",
        closeAction: "hide",
        width: 650,
        height: 356,
        x: 50,
        y: 100,
        items: {
            xtype: "gx_mappanel",
            region: "center",
            layers: [
                new OpenLayers.Layer.WMS(
                    "OpenLayers WMS",
                    "http://vmap0.tiles.osgeo.org/wms/vmap0",
                    {layers: 'basic'} ),
                vectorLayer
            ]
        }
    });
    mapwin.show();

    mapPanel = mapwin.items.get(0);
    mapPanel.map.addControl(selectCtrl);
    selectCtrl.activate();
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[popup-more]
 *  Modifying Popups
 *  ----------------
 *  Update a popup with information from multiple locations.
 */

var mapPanel, popup;

Ext.onReady(function() {

    function addToPopup(loc) {

        // create the popup if it doesn't exist
        if (!popup) {
            popup = new GeoExt.Popup({
                title: "Popup",
                width: 200,
                maximizable: true,
                collapsible: true,
                map: mapPanel.map,
                anchored: true,
                listeners: {
                    close: function() {
                        // closing a popup destroys it, but our reference is truthy
                        popup = null;
                    }
                }
            });
        }

        // add some content to the popup (this can be any Ext component)
        popup.add({
            xtype: "box",
            autoEl: {
                html: "You clicked on (" + loc.lon.toFixed(2) + ", " + loc.lat.toFixed(2) + ")"
            }
        });

        // reset the popup's location
        popup.location = loc;
        
        popup.doLayout();

        // since the popup is anchored, calling show will move popup to this location
        popup.show();
    }

    // create Ext window including a map panel
    var mapPanel = new GeoExt.MapPanel({
        title: "Map",
        renderTo: "container",
        width: 650, height: 356,
        layers: [
            new OpenLayers.Layer.WMS(
                "Global Imagery",
                "http://maps.opengeo.org/geowebcache/service/wms",
                {layers: "bluemarble"}
            )
        ],
        center: [0, 0],
        zoom: 2
    });

    var control = new OpenLayers.Control.Click({
        trigger: function(evt) {
            var loc = mapPanel.map.getLonLatFromViewPortPx(evt.xy);
            addToPopup(loc);
        }
    });
    
    mapPanel.map.addControl(control);
    control.activate();

});

// simple control to handle user clicks on the map

OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {                

    defaultHandlerOptions: {
        single: true,
        double: false,
        pixelTolerance: 0,
        stopSingle: true
    },

    initialize: function(options) {

        this.handlerOptions = OpenLayers.Util.extend(
            options && options.handlerOptions || {}, 
            this.defaultHandlerOptions
        );
        OpenLayers.Control.prototype.initialize.apply(
            this, arguments
        ); 
        this.handler = new OpenLayers.Handler.Click(
            this, 
            {
                click: this.trigger
            }, 
            this.handlerOptions
        );
    },
    
    CLASS_NAME: "OpenLayers.Control.Click"

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[popup]
 *  Feature Popup
 *  -------------
 *  Display a popup with feature information.
 */

var mapPanel, popup;

Ext.onReady(function() {

    // create a vector layer, add a feature into it
    var vectorLayer = new OpenLayers.Layer.Vector("vector");
    vectorLayer.addFeatures(
        new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(-45, 5)
        )
    );

    // create select feature control
    var selectCtrl = new OpenLayers.Control.SelectFeature(vectorLayer);

    // define "createPopup" function
    var bogusMarkup = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
    function createPopup(feature) {
        popup = new GeoExt.Popup({
            title: 'My Popup',
            location: feature,
            width:200,
            html: bogusMarkup,
            maximizable: true,
            collapsible: true
        });
        // unselect feature when the popup
        // is closed
        popup.on({
            close: function() {
                if(OpenLayers.Util.indexOf(vectorLayer.selectedFeatures,
                                           this.feature) > -1) {
                    selectCtrl.unselect(this.feature);
                }
            }
        });
        popup.show();
    }

    // create popup on "featureselected"
    vectorLayer.events.on({
        featureselected: function(e) {
            createPopup(e.feature);
        }
    });

    // create Ext window including a map panel
    var mapwin = new Ext.Window({
        layout: "fit",
        title: "Map",
        closeAction: "hide",
        width: 650,
        height: 356,
        x: 50,
        y: 100,
        items: {
            xtype: "gx_mappanel",
            region: "center",
            layers: [
                new OpenLayers.Layer.WMS( 
                    "OpenLayers WMS",
                    "http://vmap0.tiles.osgeo.org/wms/vmap0",
                    {layers: 'basic'} ),
                vectorLayer
            ]
        }
    });
    mapwin.show();

    mapPanel = mapwin.items.get(0);
    mapPanel.map.addControl(selectCtrl);
    selectCtrl.activate();
});
 /**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[print-extent]
 *  Interactive Print Extent
 *  ------------------------
 *  Change print scale, center and rotation with the PrintExtent plugin.
 */

var mapPanel, printProvider;

Ext.onReady(function() {
    // The printProvider that connects us to the print service
    printProvider = new GeoExt.data.PrintProvider({
        method: "GET", // "POST" recommended for production use
        capabilities: printCapabilities, // from the info.json script in the html
        customParams: {
            mapTitle: "Printing Demo",
            comment: "This is a map printed from GeoExt."
        }
    });

    var printExtent = new GeoExt.plugins.PrintExtent({
        printProvider: printProvider
    });

    // The map we want to print, with the PrintExtent added as item.
    mapPanel = new GeoExt.MapPanel({
        renderTo: "content",
        width: 450,
        height: 320,
        layers: [new OpenLayers.Layer.WMS("Tasmania", "http://demo.opengeo.org/geoserver/wms",
            {layers: "topp:tasmania_state_boundaries"}, {singleTile: true})],
        center: [146.56, -41.56],
        zoom: 6,
        plugins: [printExtent],
        bbar: [{
            text: "Create PDF",
            handler: function() {
                // the PrintExtent plugin is the mapPanel's 1st plugin
                mapPanel.plugins[0].print();
            }
        }]
    });
    printExtent.addPage();
});
 /**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[print-form]
 *  Print Configuration with a Form
 *  -------------------------------
 *  Use form field plugins to control print output.
 */

var mapPanel, printPage;

Ext.onReady(function() {
    // The printProvider that connects us to the print service
    var printProvider = new GeoExt.data.PrintProvider({
        method: "GET", // "POST" recommended for production use
        capabilities: printCapabilities, // from the info.json script in the html
        customParams: {
            mapTitle: "Printing Demo"
        }
    });
    // Our print page. Stores scale, center and rotation and gives us a page
    // extent feature that we can add to a layer.
    printPage = new GeoExt.data.PrintPage({
        printProvider: printProvider
    });
    // A layer to display the print page extent
    var pageLayer = new OpenLayers.Layer.Vector();
    pageLayer.addFeatures(printPage.feature);

    // The map we want to print
    mapPanel = new GeoExt.MapPanel({
        region: "center",
        map: {
            eventListeners: {
                // recenter/resize page extent after pan/zoom
                "moveend": function(){ printPage.fit(this, {mode: "screen"}); }
            }
        },
        layers: [
            new OpenLayers.Layer.WMS("Tasmania", "http://demo.opengeo.org/geoserver/wms",
                {layers: "topp:tasmania_state_boundaries"}, {singleTile: true}),
            pageLayer
        ],
        center: [146.56, -41.56],
        zoom: 6
    });
    // The form with fields controlling the print output
    var formPanel = new Ext.form.FormPanel({
        region: "west",
        width: 150,
        bodyStyle: "padding:5px",
        labelAlign: "top",
        defaults: {anchor: "100%"},
        items: [{
            xtype: "textarea",
            name: "comment",
            value: "",
            fieldLabel: "Comment",
            plugins: new GeoExt.plugins.PrintPageField({
                printPage: printPage
            })
        }, {
            xtype: "combo",
            store: printProvider.layouts,
            displayField: "name",
            fieldLabel: "Layout",
            typeAhead: true,
            mode: "local",
            triggerAction: "all",
            plugins: new GeoExt.plugins.PrintProviderField({
                printProvider: printProvider
            })
        }, {
            xtype: "combo",
            store: printProvider.dpis,
            displayField: "name",
            fieldLabel: "Resolution",
            tpl: '<tpl for="."><div class="x-combo-list-item">{name} dpi</div></tpl>',
            typeAhead: true,
            mode: "local",
            triggerAction: "all",
            plugins: new GeoExt.plugins.PrintProviderField({
                printProvider: printProvider
            }),
            // the plugin will work even if we modify a combo value
            setValue: function(v) {
                v = parseInt(v) + " dpi";
                Ext.form.ComboBox.prototype.setValue.apply(this, arguments);
            }
        }, {
            xtype: "combo",
            store: printProvider.scales,
            displayField: "name",
            fieldLabel: "Scale",
            typeAhead: true,
            mode: "local",
            triggerAction: "all",
            plugins: new GeoExt.plugins.PrintPageField({
                printPage: printPage
            })
        }, {
            xtype: "textfield",
            name: "rotation",
            fieldLabel: "Rotation",
            plugins: new GeoExt.plugins.PrintPageField({
                printPage: printPage
            })
        }],
        buttons: [{
            text: "Create PDF",
            handler: function() {
                printProvider.print(mapPanel, printPage);
            }
        }]
    });
     
    // The main panel
    new Ext.Panel({
        renderTo: "content",
        layout: "border",
        width: 700,
        height: 420,
        items: [mapPanel, formPanel]
    });
});
 /**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[print-page]
 *  Print Your Map
 *  --------------
 *  Print the visible extent of a MapPanel with PrintPage and PrintProvider.
 */

var mapPanel, printPage;

Ext.onReady(function() {
    // The printProvider that connects us to the print service
    var printProvider = new GeoExt.data.PrintProvider({
        method: "GET", // "POST" recommended for production use
        capabilities: printCapabilities, // from the info.json script in the html
        customParams: {
            mapTitle: "Printing Demo",
            comment: "This is a simple map printed from GeoExt."
        }
    });
    // Our print page. Tells the PrintProvider about the scale and center of
    // our page.
    printPage = new GeoExt.data.PrintPage({
        printProvider: printProvider
    });

    // The map we want to print
    mapPanel = new GeoExt.MapPanel({
        region: "center",
        layers: [new OpenLayers.Layer.WMS("Tasmania", "http://demo.opengeo.org/geoserver/wms",
            {layers: "topp:tasmania_state_boundaries"}, {singleTile: true})],
        center: [146.56, -41.56],
        zoom: 7
    });
    // The legend to optionally include on the printout
    var legendPanel = new GeoExt.LegendPanel({
        region: "west",
        width: 150,
        bodyStyle: "padding:5px",
        layerStore: mapPanel.layers
    });
    
    var includeLegend; // controlled by the "Include legend?" checkbox
     
    // The main panel
    new Ext.Panel({
        renderTo: "content",
        layout: "border",
        width: 700,
        height: 420,
        items: [mapPanel, legendPanel],
        bbar: ["->", {
            text: "Print",
            handler: function() {
                // convenient way to fit the print page to the visible map area
                printPage.fit(mapPanel, true);
                // print the page, optionally including the legend
                printProvider.print(mapPanel, printPage, includeLegend && {legend: legendPanel});
            }
        }, {
            xtype: "checkbox",
            boxLabel: "Include legend?",
            handler: function() {includeLegend = this.checked}
        }]
    });
});
 /**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[print-preview-osm]
 *  Printing OpenStreetMap
 *  ----------------------------------------
 *  PrintMapPanel with an OSM map.
 */

var mapPanel, printDialog;

Ext.onReady(function() {
    // The PrintProvider that connects us to the print service
    var printProvider = new GeoExt.data.PrintProvider({
        method: "GET", // "POST" recommended for production use
        capabilities: printCapabilities, // provide url instead for lazy loading
        customParams: {
            mapTitle: "GeoExt Printing Demo",
            comment: "This demo shows how to use GeoExt.PrintMapPanel with OSM"
        }
    });
    
    // A MapPanel with a "Print..." button
    mapPanel = new GeoExt.MapPanel({
        renderTo: "content",
        width: 500,
        height: 350,
        map: {
            maxExtent: new OpenLayers.Bounds(
                -128 * 156543.0339,
                -128 * 156543.0339,
                128 * 156543.0339,
                128 * 156543.0339
            ),
            maxResolution: 156543.0339,
            units: "m",
            projection: "EPSG:900913"
        },
        layers: [new OpenLayers.Layer.OSM()],
        /*layers: [new OpenLayers.Layer.WMS("Tasmania State Boundaries",
            "http://demo.opengeo.org/geoserver/wms",
            {layers: "topp:tasmania_state_boundaries"}, {singleTile: true})],*/
        center: [16314984.568391, -5095295.7603428],
        zoom: 6,
        bbar: [{
            text: "Print...",
            handler: function(){
                // A window with the PrintMapPanel, which we can use to adjust
                // the print extent before creating the pdf.
                printDialog = new Ext.Window({
                    title: "Print Preview",
                    width: 350,
                    autoHeight: true,
                    items: [{
                        xtype: "gx_printmappanel",
                        // use only a PanPanel control
                        map: {controls: [new OpenLayers.Control.PanPanel()]},
                        sourceMap: mapPanel,
                        printProvider: printProvider
                    }],
                    bbar: [{
                        text: "Create PDF",
                        handler: function(){ printDialog.items.get(0).print(); }
                    }]
                });
                printDialog.show();
            }
        }]
    });

}); /**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[print-preview]
 *  Print Preview Window
 *  --------------------
 *  Use the PrintMapPanel for interactive print previews.
 */

var mapPanel, printDialog;

Ext.onReady(function() {
    // The PrintProvider that connects us to the print service
    var printProvider = new GeoExt.data.PrintProvider({
        method: "GET", // "POST" recommended for production use
        capabilities: printCapabilities, // provide url instead for lazy loading
        customParams: {
            mapTitle: "GeoExt Printing Demo",
            comment: "This demo shows how to use GeoExt.PrintMapPanel"
        }
    });
    
    // A MapPanel with a "Print..." button
    mapPanel = new GeoExt.MapPanel({
        renderTo: "content",
        width: 500,
        height: 350,
        map: {
            maxExtent: new OpenLayers.Bounds(
                143.835, -43.648,
                148.479, -39.574
            ),
            maxResolution: 0.018140625,
            projection: "EPSG:4326",
            units: 'degrees'
        },
        layers: [new OpenLayers.Layer.WMS("Tasmania State Boundaries",
            "http://demo.opengeo.org/geoserver/wms",
            {layers: "topp:tasmania_state_boundaries"},
            {singleTile: true, numZoomLevels: 8})],
        center: [146.56, -41.56],
        zoom: 0,
        bbar: [{
            text: "Print...",
            handler: function(){
                // A window with the PrintMapPanel, which we can use to adjust
                // the print extent before creating the pdf.
                printDialog = new Ext.Window({
                    title: "Print Preview",
                    layout: "fit",
                    width: 350,
                    autoHeight: true,
                    items: [{
                        xtype: "gx_printmappanel",
                        sourceMap: mapPanel,
                        printProvider: printProvider
                    }],
                    bbar: [{
                        text: "Create PDF",
                        handler: function(){ printDialog.items.get(0).print(); }
                    }]
                });
                printDialog.show();
            }
        }]
    });

});/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[renderer]
 *  Feature Renderer
 *  ----------------
 *  Render a vector feature with multiple symbolizers in a box component.
 */

var blue = {
    fillColor: "blue",
    fillOpacity: 0.25,
    strokeColor: "blue",
    strokeWidth: 2,
    pointRadius: 5
};

var custom = {
    point: {
        graphicName: "star",
        pointRadius: 8,
        fillColor: "yellow",
        strokeColor: "red",
        strokeWidth: 1
    },
    line: {
        strokeColor: "#669900",
        strokeWidth: 3
    },
    poly: {
        fillColor: "olive",
        fillOpacity: 0.25,
        strokeColor: "#666666",
        strokeWidth: 2,
        strokeDashstyle: "dot"
    }
};

var stacked = {
    point: [{
        pointRadius: 8,
        fillColor: "white",
        strokeColor: "red",
        strokeWidth: 2
    }, {
        graphicName: "star",
        pointRadius: 5,
        fillColor: "red"
    }],
    line: [{
        strokeColor: "red",
        strokeWidth: 5
    }, {
        strokeColor: "#ff9933",
        strokeWidth: 2
    }],
    poly: [{
        strokeWidth: 3,
        fillColor: "white",
        strokeColor: "#669900"
    }, {
        strokeWidth: 2,
        fillOpacity: 0,
        strokeColor: "red",
        strokeDashstyle: "dot"
    }]
};

var configs = [{
    symbolType: "Point",
    renderTo: "point_default"
}, {
    symbolType: "Line",
    renderTo: "line_default"
}, {
    symbolType: "Polygon",
    renderTo: "poly_default"
}, {
    symbolType: "Point",
    symbolizers: [blue],
    renderTo: "point_blue"
}, {
    symbolType: "Line",
    symbolizers: [blue],
    renderTo: "line_blue"
}, {
    symbolType: "Polygon",
    symbolizers: [blue],
    renderTo: "poly_blue"
}, {
    symbolType: "Point",
    symbolizers: [custom.point],
    renderTo: "point_custom"
}, {
    symbolType: "Line",
    symbolizers: [custom.line],
    renderTo: "line_custom"
}, {
    symbolType: "Polygon",
    symbolizers: [custom.poly],
    renderTo: "poly_custom"
}, {
    symbolType: "Point",
    symbolizers: stacked.point,
    renderTo: "point_stacked"
}, {
    symbolType: "Line",
    symbolizers: stacked.line,
    renderTo: "line_stacked"
}, {
    symbolType: "Polygon",
    symbolizers: stacked.poly,
    renderTo: "poly_stacked"
}];

Ext.onReady(function() {        
    for(var i=0; i<configs.length; ++i) {
        new GeoExt.FeatureRenderer(configs[i]);
    }
    $("render").onclick = render;
});

var format = new OpenLayers.Format.WKT();
var renderer, win;
function render() {
    var wkt = $("wkt").value;
    var feature;
    try {
        feature = format.read(wkt)
    } catch(err) {
        $("wkt").value = "Bad WKT: " + err;
    }
    var symbolizers;
    try {
        var value = $("symbolizers").value;
        symbolizers = eval("(" + value + ")");
        if (!symbolizers || symbolizers.constructor !== Array) {
            throw "Must be an array literal";
        }
    } catch(err) {
        $("symbolizers").value = "Bad symbolizers: " + err + "\n\n" + value;
        symbolizers = null;
    }
    if(feature && symbolizers) {
        if(!win) {
            renderer = new GeoExt.FeatureRenderer({
                feature: feature,
                symbolizers: symbolizers,
                width: 150,
                style: {margin: 4}
            });
            win = new Ext.Window({
                closeAction: "hide",
                layout: "fit",
                width: 175,
                items: [renderer]
            });
        } else {
            renderer.update({
                feature: feature,
                symbolizers: symbolizers
            });
        }
        win.show();
    }
}

/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[search-form]
 *  Filter Form Panel
 *  -----------------
 *  Use a form to build an OpenLayers filter.
 */

var formPanel;

Ext.onReady(function() {

    // create a protocol, this protocol is used by the form
    // to send the search request, this protocol's read
    // method received an OpenLayers.Filter instance,
    // which is derived from the content of the form
    var protocol = new OpenLayers.Protocol({
        read: function(options) {
            var f; html = [];

            f = options.filter;
            html.push([f.CLASS_NAME, ",", f.type, "<br />"].join(" "));

            f = options.filter.filters[0];
            html.push([f.CLASS_NAME, ",", f.type, ",",
                       f.property, ":", f.value, "<br />"].join(" "));

            f = options.filter.filters[1];
            html.push([f.CLASS_NAME, ",", f.type, ", ",
                       f.property, ": ", f.value].join(" "));

            Ext.get("filter").update(html.join(""));

        }
    });

    // create a GeoExt form panel (configured with an OpenLayers.Protocol
    // instance)
    formPanel = new GeoExt.form.FormPanel({
        width: 300,
        height: 200,
        protocol: protocol,
        items: [{
            xtype: "textfield",
            name: "name__like",
            value: "foo",
            fieldLabel: "name"
        }, {
            xtype: "textfield",
            name: "elevation__ge",
            value: "1200",
            fieldLabel: "maximum elevation"
        }],
        listeners: {
            actioncomplete: function(form, action) {
                // this listener triggers when the search request
                // is complete, the OpenLayers.Protocol.Response
                // resulting from the request is available
                // through "action.response"
            }
        }
    });

    formPanel.addButton({
        text: "search",
        handler: function() {
            // trigger search request, the options passed to doAction
            // are passed to the protocol's read method, so one
            // can register a read callback here
            var o = {
                callback: function(response) {
                }
            };
            this.search(o);
        },
        scope: formPanel
    });

    formPanel.render("formpanel");
});
/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.onReady(function(){

    new Ext.slider.SingleSlider({
        renderTo: "tip-slider",
        width: 214,
        minValue: 0,
        maxValue: 100,
        plugins: new GeoExt.SliderTip()
    });

    new Ext.slider.SingleSlider({
        renderTo: "custom-tip-slider",
        width: 214,
        increment: 10,
        minValue: 0,
        maxValue: 100,
        plugins: new GeoExt.SliderTip({
            getText: function(thumb){
                return String.format("<b>{0}% complete</b>", thumb.value);
            }
        })
    });

    new Ext.slider.SingleSlider({
        renderTo: "no-hover-tip",
        width: 214,
        increment: 10,
        minValue: 0,
        maxValue: 100,
        plugins: new GeoExt.SliderTip({hover: false})
    });
    
    new Ext.slider.MultiSlider({
        renderTo: "multi-slider-horizontal",
        width   : 214,
        minValue: 0,
        maxValue: 100,
        values  : [10, 50, 90],
        plugins : new GeoExt.SliderTip()
    });
    
    new Ext.slider.MultiSlider({
        renderTo : "multi-slider-vertical",
        vertical : true,
        height   : 214,
        minValue: 0,
        maxValue: 100,
        values  : [10, 50, 90],
        plugins : new GeoExt.SliderTip()
    });
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[style-grid]
 *  Style Reader
 *  ----------------
 *  Rendering and basic editing of SLD rules or an SLD ColorMap with a store
 *  created using GeoExt.data.StyleReader.
 */

var rasterSld = '<?xml version="1.0" encoding="ISO-8859-1"?><StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"><NamedLayer><Name>rain</Name><UserStyle><Name>rain</Name><Title>Rain distribution</Title><FeatureTypeStyle><Rule><RasterSymbolizer><Opacity>1.0</Opacity><ColorMap><ColorMapEntry color="#FF0000" quantity="0" /><ColorMapEntry color="#FFFFFF" quantity="100"/><ColorMapEntry color="#00FF00" quantity="2000"/><ColorMapEntry color="#0000FF" quantity="5000"/></ColorMap></RasterSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>';
var vectorSld = '<?xml version="1.0" encoding="ISO-8859-1"?><StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"><NamedLayer><Name>USA states population</Name><UserStyle><Name>population</Name><Title>Population in the United States</Title><Abstract>A sample filter that filters the United States into threecategories of population, drawn in different colors</Abstract><FeatureTypeStyle><Rule><Title>&lt; 2M</Title><ogc:Filter><ogc:PropertyIsLessThan> <ogc:PropertyName>PERSONS</ogc:PropertyName> <ogc:Literal>2000000</ogc:Literal></ogc:PropertyIsLessThan></ogc:Filter><PolygonSymbolizer> <Fill><!-- CssParameters allowed are fill (the color) and fill-opacity --><CssParameter name="fill">#4DFF4D</CssParameter><CssParameter name="fill-opacity">0.7</CssParameter> </Fill> </PolygonSymbolizer></Rule><Rule><Title>2M - 4M</Title><ogc:Filter><ogc:PropertyIsBetween><ogc:PropertyName>PERSONS</ogc:PropertyName><ogc:LowerBoundary><ogc:Literal>2000000</ogc:Literal></ogc:LowerBoundary><ogc:UpperBoundary><ogc:Literal>4000000</ogc:Literal></ogc:UpperBoundary></ogc:PropertyIsBetween></ogc:Filter><PolygonSymbolizer> <Fill><!-- CssParameters allowed are fill (the color) and fill-opacity --><CssParameter name="fill">#FF4D4D</CssParameter><CssParameter name="fill-opacity">0.7</CssParameter> </Fill> </PolygonSymbolizer></Rule><Rule><Title>&gt; 4M</Title><!-- like a linesymbolizer but with a fill too --><ogc:Filter><ogc:PropertyIsGreaterThan> <ogc:PropertyName>PERSONS</ogc:PropertyName> <ogc:Literal>4000000</ogc:Literal></ogc:PropertyIsGreaterThan></ogc:Filter><PolygonSymbolizer> <Fill><!-- CssParameters allowed are fill (the color) and fill-opacity --><CssParameter name="fill">#4D4DFF</CssParameter><CssParameter name="fill-opacity">0.7</CssParameter> </Fill> </PolygonSymbolizer></Rule><Rule><Title>Boundary</Title><LineSymbolizer><Stroke><CssParameter name="stroke-width">0.2</CssParameter></Stroke></LineSymbolizer><TextSymbolizer><Label><ogc:PropertyName>STATE_ABBR</ogc:PropertyName></Label><Font><CssParameter name="font-family">Times New Roman</CssParameter><CssParameter name="font-style">Normal</CssParameter><CssParameter name="font-size">14</CssParameter></Font><LabelPlacement><PointPlacement><AnchorPoint><AnchorPointX>0.5</AnchorPointX><AnchorPointY>0.5</AnchorPointY></AnchorPoint></PointPlacement></LabelPlacement></TextSymbolizer></Rule> </FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>';

var format = new OpenLayers.Format.SLD({multipleSymbolizers: true});

var vectorStyle = format.read(vectorSld).namedLayers["USA states population"].userStyles[0];
var rasterStyle = format.read(rasterSld).namedLayers["rain"].userStyles[0];

var vectorGrid, rasterGrid;

Ext.onReady(function() {
    
    var columns = [
        {dataIndex: "symbolizers", width: 26, xtype: "gx_symbolizercolumn"},
        {header: "Label", dataIndex: "label", editor: {xtype: "textfield"}},
        {header: "Filter", dataIndex: "filter", editor: {xtype: "textfield"}}
    ];
        
    vectorGrid = new Ext.grid.EditorGridPanel({
        width: 220,
        height: 115,
        columns: columns.concat(),
        viewConfig: {autoFill: true},
        store: {
            reader: new GeoExt.data.StyleReader(),
            data: vectorStyle
        },
        renderTo: "vectorgrid",
        enableDragDrop: true,
        sm: new Ext.grid.RowSelectionModel(),
        ddGroup: "vgrid",
        listeners: {
            afteredit: function(e) {e.grid.store.commitChanges();},
            render: function makeDD(grid) {
                store = grid.store;
                new Ext.dd.DropTarget(grid.getView().mainBody, {
                    ddGroup : "vgrid",
                    notifyDrop: function(dd, e, data){
                        var sm = grid.getSelectionModel();
                        var rows = sm.getSelections();
                        var cindex = dd.getDragData(e).rowIndex;
                        if (sm.hasSelection()) {
                            for (var i=0, ii=rows.length; i<ii; ++i) {
                                store.remove(store.getById(rows[i].id));
                                store.insert(cindex,rows[i]);
                                store.commitChanges();
                            }
                            sm.selectRecords(rows);
                        }  
                    }
                });
            },
            scope: this
        }
    });
    rasterGrid = new Ext.grid.EditorGridPanel({
        width: 220,
        height: 115,
        columns: columns.concat(),
        viewConfig: {autoFill: true},
        store: {
            reader: new GeoExt.data.StyleReader(),
            data: rasterStyle.rules[0].symbolizers[0]
        },
        renderTo: "rastergrid",
        listeners: {
            afteredit: function(e) {e.grid.store.commitChanges();}
        }
    });
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[toolbar]
 *  Toolbar with Actions
 *  --------------------
 *  Create a toolbar with GeoExt Actions.
 */

Ext.onReady(function() {
    Ext.QuickTips.init();

    var map = new OpenLayers.Map();
    var wms = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"}
    );
    var vector = new OpenLayers.Layer.Vector("vector");
    map.addLayers([wms, vector]);
    
    var ctrl, toolbarItems = [], action, actions = {};

    // ZoomToMaxExtent control, a "button" control
    action = new GeoExt.Action({
        control: new OpenLayers.Control.ZoomToMaxExtent(),
        map: map,
        text: "max extent",
        tooltip: "zoom to max extent"
    });
    actions["max_extent"] = action;
    toolbarItems.push(action);
    toolbarItems.push("-");

    // Navigation control and DrawFeature controls
    // in the same toggle group
    action = new GeoExt.Action({
        text: "nav",
        control: new OpenLayers.Control.Navigation(),
        map: map,
        // button options
        toggleGroup: "draw",
        allowDepress: false,
        pressed: true,
        tooltip: "navigate",
        // check item options
        group: "draw",
        checked: true
    });
    actions["nav"] = action;
    toolbarItems.push(action);

    action = new GeoExt.Action({
        text: "draw poly",
        control: new OpenLayers.Control.DrawFeature(
            vector, OpenLayers.Handler.Polygon
        ),
        map: map,
        // button options
        toggleGroup: "draw",
        allowDepress: false,
        tooltip: "draw polygon",
        // check item options
        group: "draw"
    });
    actions["draw_poly"] = action;
    toolbarItems.push(action);

    action = new GeoExt.Action({
        text: "draw line",
        control: new OpenLayers.Control.DrawFeature(
            vector, OpenLayers.Handler.Path
        ),
        map: map,
        // button options
        toggleGroup: "draw",
        allowDepress: false,
        tooltip: "draw line",
        // check item options
        group: "draw"
    });
    actions["draw_line"] = action;
    toolbarItems.push(action);
    toolbarItems.push("-");

    // SelectFeature control, a "toggle" control
    action = new GeoExt.Action({
        text: "select",
        control: new OpenLayers.Control.SelectFeature(vector, {
            type: OpenLayers.Control.TYPE_TOGGLE,
            hover: true
        }),
        map: map,
        // button options
        enableToggle: true,
        tooltip: "select feature"
    });
    actions["select"] = action;
    toolbarItems.push(action);
    toolbarItems.push("-");

    // Navigation history - two "button" controls
    ctrl = new OpenLayers.Control.NavigationHistory();
    map.addControl(ctrl);

    action = new GeoExt.Action({
        text: "previous",
        control: ctrl.previous,
        disabled: true,
        tooltip: "previous in history"
    });
    actions["previous"] = action;
    toolbarItems.push(action);

    action = new GeoExt.Action({
        text: "next",
        control: ctrl.next,
        disabled: true,
        tooltip: "next in history"
    });
    actions["next"] = action;
    toolbarItems.push(action);
    toolbarItems.push("->");

    // Reuse the GeoExt.Action objects created above
    // as menu items
    toolbarItems.push({
        text: "menu",
        menu: new Ext.menu.Menu({
            items: [
                // ZoomToMaxExtent
                actions["max_extent"],
                // Nav
                new Ext.menu.CheckItem(actions["nav"]),
                // Draw poly
                new Ext.menu.CheckItem(actions["draw_poly"]),
                // Draw line
                new Ext.menu.CheckItem(actions["draw_line"]),
                // Select control
                new Ext.menu.CheckItem(actions["select"]),
                // Navigation history control
                actions["previous"],
                actions["next"]
            ]
        })
    });

    var mapPanel = new GeoExt.MapPanel({
        renderTo: "mappanel",
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4,
        tbar: toolbarItems
    });
});
/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.examples");

// this function takes action based on the "action"
// parameter, it is used as a listener to layer
// nodes' "action" events
GeoExt.examples.onAction = function(node, action, evt) {
    var layer = node.layer;
    switch(action) {
    case "down":
        layer.map.raiseLayer(layer, -1);
        break;
    case "up":
        layer.map.raiseLayer(layer, +1);
        break;
    case "delete":
        layer.destroy();
        break;
    }
};

// custom layer node UI class
GeoExt.examples.LayerNodeUI = Ext.extend(
    GeoExt.tree.LayerNodeUI,
    new GeoExt.tree.TreeNodeUIEventMixin()
);

Ext.onReady(function() {
    Ext.QuickTips.init();

    // the map panel
    var mapPanel = new GeoExt.MapPanel({
        border: true,
        region: "center",
        center: [146.1569825, -41.6109735],
        zoom: 6,
        layers: [
            new OpenLayers.Layer.WMS("Tasmania State Boundaries",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_state_boundaries"
                }, {
                    buffer: 0,
                    // exclude this layer from layer container nodes
                    displayInLayerSwitcher: false
               }),
            new OpenLayers.Layer.WMS("Water",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_water_bodies",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                }),
            new OpenLayers.Layer.WMS("Cities",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_cities",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                }),
            new OpenLayers.Layer.WMS("Tasmania Roads",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_roads",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                })
        ]
    });

    // the layer tree panel. In this tree the node actions are set using 
    // the loader's "baseAttrs" property.
    var tree = new Ext.tree.TreePanel({
        region: "west",
        width: 250,
        title: "Layer Tree",
        loader: {
            applyLoader: false,
            uiProviders: {
                "ui": GeoExt.examples.LayerNodeUI
            }
        },
        // apply the tree node actions plugin to layer nodes
        plugins: [{
            ptype: "gx_treenodeactions",
            listeners: {
                action: GeoExt.examples.onAction
            }
        }],
        root: {
            nodeType: "gx_layercontainer",
            loader: {
                baseAttrs: {
                    radioGroup: "radiogroup",
                    uiProvider: "ui",
                    actions: [{
                        action: "delete",
                        qtip: "delete"
                    }, {
                        action: "up",
                        qtip: "move up",
                        update: function(el) { 
                            // "this" references the tree node 
                            var layer = this.layer, map = layer.map; 
                            if (map.getLayerIndex(layer) == map.layers.length - 1) { 
                                el.addClass('disabled'); 
                            } else { 
                                el.removeClass('disabled'); 
                            } 
                        } 
                    }, { 
                        action: "down", 
                        qtip: "move down", 
                        update: function(el) { 
                            // "this" references the tree node 
                            var layer = this.layer, map = layer.map; 
                            if (map.getLayerIndex(layer) == 1) { 
                                el.addClass('disabled'); 
                            } else { 
                                el.removeClass('disabled'); 
                            } 
                        } 
                    }]
                }
            }
        },
        rootVisible: false,
        lines: false
    });

    // the viewport
    new Ext.Viewport({
        layout: "fit",
        hideBorders: true,
        items: {
            layout: "border",
            deferredRender: false,
            items: [
                mapPanel,
                tree, {
                region: "east",
                contentEl: "desc",
                width: 250
            }]
        }
    });
});
/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

 /** api: example[tree-legend]
  *  Tree Legend
  *  -----------
  *  Render layer nodes with legends.
  */

// custom layer node UI class
var LayerNodeUI = Ext.extend(
    GeoExt.tree.LayerNodeUI,
    new GeoExt.tree.TreeNodeUIEventMixin()
);

Ext.onReady(function() {
    var mapPanel = new GeoExt.MapPanel({
        region: "center",
        center: [146.1569825, -41.6109735],
        zoom: 6,
        layers: [
            new OpenLayers.Layer.WMS("Tasmania State Boundaries",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_state_boundaries"
                }, {
                    buffer: 0,
                    // exclude this layer from layer container nodes
                    displayInLayerSwitcher: false
               }),
            new OpenLayers.Layer.WMS("Water",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_water_bodies",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                }),
            new OpenLayers.Layer.WMS("Cities",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_cities",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                }),
            new OpenLayers.Layer.WMS("Tasmania Roads",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_roads",
                    transparent: true,
                    format: "image/gif"
                }, {
                    buffer: 0
                })
        ]
    });

    var tree = new Ext.tree.TreePanel({
        region: "east",
        title: "Layers",
        width: 250,
        autoScroll: true,
        enableDD: true,
        // apply the tree node component plugin to layer nodes
        plugins: [{
            ptype: "gx_treenodecomponent"
        }],
        loader: {
            applyLoader: false,
            uiProviders: {
                "custom_ui": LayerNodeUI
            }
        },
        root: {
            nodeType: "gx_layercontainer",
            loader: {
                baseAttrs: {
                    uiProvider: "custom_ui"
                },
                createNode: function(attr) {
                    // add a WMS legend to each node created
                    attr.component = {
                        xtype: "gx_wmslegend",
                        layerRecord: mapPanel.layers.getByLayer(attr.layer),
                        showTitle: false,
                        // custom class for css positioning
                        // see tree-legend.html
                        cls: "legend"
                    }
                    return GeoExt.tree.LayerLoader.prototype.createNode.call(this, attr);
                }
            }
        },
        rootVisible: false,
        lines: false
    });

    new Ext.Viewport({
        layout: "fit",
        hideBorders: true,
        items: {
            layout: "border",
            items: [
                mapPanel, tree, {
                    contentEl: desc,
                    region: "west",
                    width: 250,
                    bodyStyle: {padding: "5px"}
                }
            ]
        }
    });
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[tree]
 *  Tree Nodes
 *  ----------
 *  Create all kinds of tree nodes.
 */

var mapPanel, tree;
Ext.onReady(function() {
    // create a map panel with some layers that we will show in our layer tree
    // below.
    mapPanel = new GeoExt.MapPanel({
        border: true,
        region: "center",
        // we do not want all overlays, to try the OverlayLayerContainer
        map: new OpenLayers.Map({allOverlays: false}),
        center: [146.1569825, -41.6109735],
        zoom: 6,
        layers: [
            new OpenLayers.Layer.WMS("Global Imagery",
                "http://maps.opengeo.org/geowebcache/service/wms", {
                    layers: "bluemarble"
                }, {
                    buffer: 0,
                    visibility: false
                }
            ),
            new OpenLayers.Layer.WMS("Tasmania State Boundaries",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_state_boundaries"
                }, {
                    buffer: 0
                }
            ),
            new OpenLayers.Layer.WMS("Water",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_water_bodies",
                    transparent: true,
                    format: "image/gif"
                }, {
                    isBaseLayer: false,
                    buffer: 0
                }
            ),
            new OpenLayers.Layer.WMS("Cities",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_cities",
                    transparent: true,
                    format: "image/gif"
                }, {
                    isBaseLayer: false,
                    buffer: 0
                }
            ),
            new OpenLayers.Layer.WMS("Tasmania Roads",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: "topp:tasmania_roads",
                    transparent: true,
                    format: "image/gif"
                }, {
                    isBaseLayer: false,
                    buffer: 0
                }
            ),
            // create a group layer (with several layers in the "layers" param)
            // to show how the LayerParamLoader works
            new OpenLayers.Layer.WMS("Tasmania (Group Layer)",
                "http://demo.opengeo.org/geoserver/wms", {
                    layers: [
                        "topp:tasmania_state_boundaries",
                        "topp:tasmania_water_bodies",
                        "topp:tasmania_cities",
                        "topp:tasmania_roads"
                    ],
                    transparent: true,
                    format: "image/gif"
                }, {
                    isBaseLayer: false,
                    buffer: 0,
                    // exclude this layer from layer container nodes
                    displayInLayerSwitcher: false,
                    visibility: false
                }
            )
        ]
    });

    // create our own layer node UI class, using the TreeNodeUIEventMixin
    var LayerNodeUI = Ext.extend(GeoExt.tree.LayerNodeUI, new GeoExt.tree.TreeNodeUIEventMixin());
        
    // using OpenLayers.Format.JSON to create a nice formatted string of the
    // configuration for editing it in the UI
    var treeConfig = [{
        nodeType: "gx_baselayercontainer"
    }, {
        nodeType: "gx_overlaylayercontainer",
        expanded: true,
        // render the nodes inside this container with a radio button,
        // and assign them the group "foo".
        loader: {
            baseAttrs: {
                radioGroup: "foo",
                uiProvider: "layernodeui"
            }
        }
    }, {
        nodeType: "gx_layer",
        layer: "Tasmania (Group Layer)",
        isLeaf: false,
        // create subnodes for the layers in the LAYERS param. If we assign
        // a loader to a LayerNode and do not provide a loader class, a
        // LayerParamLoader will be assumed.
        loader: {
            param: "LAYERS"
        }
    }];
    // The line below is only needed for this example, because we want to allow
    // interactive modifications of the tree configuration using the
    // "Show/Edit Tree Config" button. Don't use this line in your code.
    treeConfig = new OpenLayers.Format.JSON().write(treeConfig, true);

    // create the tree with the configuration from above
    tree = new Ext.tree.TreePanel({
        border: true,
        region: "west",
        title: "Layers",
        width: 200,
        split: true,
        collapsible: true,
        collapseMode: "mini",
        autoScroll: true,
        plugins: [
            new GeoExt.plugins.TreeNodeRadioButton({
                listeners: {
                    "radiochange": function(node) {
                        alert(node.text + " is now the active layer.");
                    }
                }
            })
        ],
        loader: new Ext.tree.TreeLoader({
            // applyLoader has to be set to false to not interfer with loaders
            // of nodes further down the tree hierarchy
            applyLoader: false,
            uiProviders: {
                "layernodeui": LayerNodeUI
            }
        }),
        root: {
            nodeType: "async",
            // the children property of an Ext.tree.AsyncTreeNode is used to
            // provide an initial set of layer nodes. We use the treeConfig
            // from above, that we created with OpenLayers.Format.JSON.write.
            children: Ext.decode(treeConfig)
            // Don't use the line above in your application. Instead, use
            //children: treeConfig
            
        },
        listeners: {
            "radiochange": function(node){
                alert(node.layer.name + " is now the the active layer.");
            }
        },
        rootVisible: false,
        lines: false,
        bbar: [{
            text: "Show/Edit Tree Config",
            handler: function() {
                treeConfigWin.show();
                Ext.getCmp("treeconfig").setValue(treeConfig);
            }
        }]
    });

    // dialog for editing the tree configuration
    var treeConfigWin = new Ext.Window({
        layout: "fit",
        hideBorders: true,
        closeAction: "hide",
        width: 300,
        height: 400,
        title: "Tree Configuration",
        items: [{
            xtype: "form",
            layout: "fit",
            items: [{
                id: "treeconfig",
                xtype: "textarea"
            }],
            buttons: [{
                text: "Save",
                handler: function() {
                    var value = Ext.getCmp("treeconfig").getValue()
                    try {
                        var root = tree.getRootNode();
                        root.attributes.children = Ext.decode(value);
                        tree.getLoader().load(root);
                    } catch(e) {
                        alert("Invalid JSON");
                        return;
                    }
                    treeConfig = value;
                    treeConfigWin.hide();
                }
            }, {
                text: "Cancel",
                handler: function() {
                    treeConfigWin.hide();
                }
            }]
        }]
    });
    
    new Ext.Viewport({
        layout: "fit",
        hideBorders: true,
        items: {
            layout: "border",
            deferredRender: false,
            items: [mapPanel, tree, {
                contentEl: "desc",
                region: "east",
                bodyStyle: {"padding": "5px"},
                collapsible: true,
                collapseMode: "mini",
                split: true,
                width: 200,
                title: "Description"
            }]
        }
    });
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[vector-legend]
 *  Vector Legend
 *  -------------------------
 *  Render a legend for a vector layer.
 */

var mapPanel, legendPanel;

Ext.onReady(function() {

    var rules = [
        new OpenLayers.Rule({
            title: "> 2000m",
            maxScaleDenominator: 3000000,
            filter: new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.GREATER_THAN,
                property: "elevation",
                value: 2000
            }),
            symbolizer: {
                graphicName: "star",
                pointRadius: 8,
                fillColor: "#99ccff",
                strokeColor: "#666666",
                strokeWidth: 1
            }
        }),
        new OpenLayers.Rule({
            title: "1500 - 2000m",
            maxScaleDenominator: 3000000,
            filter: new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.BETWEEN,
                property: "elevation",
                upperBoundary: 2000,
                lowerBoundary: 1500
            }),
            symbolizer: {
                graphicName: "star",
                pointRadius: 6,
                fillColor: "#6699cc",
                strokeColor: "#666666",
                strokeWidth: 1
            }
        }),
        new OpenLayers.Rule({
            title: "< 1500m",
            maxScaleDenominator: 3000000,
            filter: new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LESS_THAN,
                property: "elevation",
                value: 1500
            }),
            symbolizer: {
                graphicName: "star",
                pointRadius: 4,
                fillColor: "#0033cc",
                strokeColor: "#666666",
                strokeWidth: 1
            }
        }),
        new OpenLayers.Rule({
            title: "All",
            minScaleDenominator: 3000000,
            symbolizer: {
                graphicName: "star",
                pointRadius: 5,
                fillColor: "#99ccff",
                strokeColor: "#666666",
                strokeWidth: 1
            }
        })
    ];

    var imagery = new OpenLayers.Layer.WMS(
        "Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"},
        {displayInLayerSwitcher: false}
    );

    var summits = new OpenLayers.Layer.Vector("Summits", {
        strategies: [new OpenLayers.Strategy.Fixed()],
        protocol: new OpenLayers.Protocol.HTTP({
            url: "data/summits.json",
            format: new OpenLayers.Format.GeoJSON()
        }),
        styleMap: new OpenLayers.StyleMap(new OpenLayers.Style({}, {rules: rules}))
    });
    
    mapPanel = new GeoExt.MapPanel({
        renderTo: "mappanel",
        border: false,
        layers: [imagery, summits],
        center: [6.3, 45.6],
        height: 256, // IE6 wants this
        zoom: 8
    });
    
    legendPanel = new GeoExt.LegendPanel({
        layerStore: mapPanel.layers,
        renderTo: "legend",
        border: false
    });

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */


/** api: example[wfs-capabilities]
 *  WFS Capabilities Store
 *  ----------------------
 *  Create layer records from WFS capabilities documents.
 */

var store;

Ext.onReady(function() {

    // create a new WFS capabilities store
    store = new GeoExt.data.WFSCapabilitiesStore({
        url: "data/wfscap_tiny_100.xml",
        // set as a function that returns a hash of layer options.  This allows
        // to have new objects created upon each new OpenLayers.Layer.Vector
        // object creations.
        layerOptions: function() {
            return {
                visibility: false,
                displayInLayerSwitcher: false,
                strategies: [new OpenLayers.Strategy.BBOX({ratio: 1})]
            };
        }
    });
    // load the store with records derived from the doc at the above url
    store.load();

    // create a grid to display records from the store
    var grid = new Ext.grid.GridPanel({
        title: "WFS Capabilities",
        store: store,
        columns: [
            {header: "Title", dataIndex: "title", sortable: true, width: 250},
            {header: "Name", dataIndex: "name", sortable: true},
            {header: "Namespace", dataIndex: "namespace", sortable: true, width: 150},
            {id: "description", header: "Description", dataIndex: "abstract"}
        ],
        renderTo: "capgrid",
        height: 300,
        width: 650
    });
});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */


/** api: example[wms-capabilities]
 *  WMS Capabilities Store
 *  ----------------------
 *  Create layer records from WMS capabilities documents.
 */

var store;
Ext.onReady(function() {
    
    // create a new WMS capabilities store
    store = new GeoExt.data.WMSCapabilitiesStore({
        url: "data/wmscap.xml"
    });
    // load the store with records derived from the doc at the above url
    store.load();

    // create a grid to display records from the store
    var grid = new Ext.grid.GridPanel({
        title: "WMS Capabilities",
        store: store,
        columns: [
            {header: "Title", dataIndex: "title", sortable: true},
            {header: "Name", dataIndex: "name", sortable: true},
            {header: "Queryable", dataIndex: "queryable", sortable: true, width: 70},
            {id: "description", header: "Description", dataIndex: "abstract"}
        ],
        autoExpandColumn: "description",
        renderTo: "capgrid",
        height: 300,
        width: 650,
        listeners: {
            rowdblclick: mapPreview
        }
    });
    
    function mapPreview(grid, index) {
        var record = grid.getStore().getAt(index);
        var layer = record.getLayer().clone();
        
        var win = new Ext.Window({
            title: "Preview: " + record.get("title"),
            width: 512,
            height: 256,
            layout: "fit",
            items: [{
                xtype: "gx_mappanel",
                layers: [layer],
                extent: record.get("llbbox")
            }]
        });
        win.show();
    }

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 *
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[wms-tree]
 *  WMS Capabilities Tree
 *  ---------------------
 *  Create a tree loader from WMS capabilities documents.
 */
var tree, mapPanel;

Ext.onReady(function() {

    var root = new Ext.tree.AsyncTreeNode({
        text: 'GeoServer Demo WMS',
        loader: new GeoExt.tree.WMSCapabilitiesLoader({
            url: 'data/wmscap.xml',
            layerOptions: {buffer: 0, singleTile: true, ratio: 1},
            layerParams: {'TRANSPARENT': 'TRUE'},
            // customize the createNode method to add a checkbox to nodes
            createNode: function(attr) {
                attr.checked = attr.leaf ? false : undefined;
                return GeoExt.tree.WMSCapabilitiesLoader.prototype.createNode.apply(this, [attr]);
            }
        })
    });

    tree = new Ext.tree.TreePanel({
        root: root,
        region: 'west',
        width: 250,
        listeners: {
            // Add layers to the map when ckecked, remove when unchecked.
            // Note that this does not take care of maintaining the layer
            // order on the map.
            'checkchange': function(node, checked) { 
                if (checked === true) {
                    mapPanel.map.addLayer(node.attributes.layer); 
                } else {
                    mapPanel.map.removeLayer(node.attributes.layer);
                }
            }
        }
    });

    mapPanel = new GeoExt.MapPanel({
        zoom: 2,
        layers: [
            new OpenLayers.Layer.WMS("Global Imagery",
                "http://maps.opengeo.org/geowebcache/service/wms", 
                {layers: "bluemarble"},
                {buffer: 0}
            )
        ],
        region: 'center'
    });

    new Ext.Viewport({
        layout: "fit",
        hideBorders: true,
        items: {
            layout: "border",
            deferredRender: false,
            items: [mapPanel, tree, {
                contentEl: "desc",
                region: "east",
                bodyStyle: {"padding": "5px"},
                collapsible: true,
                collapseMode: "mini",
                split: true,
                width: 200,
                title: "Description"
            }]
        }
    });

});
/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[zoom-chooser]
 *  Scale Chooser
 *  -------------
 *  Use a ComboBox to display available map scales.
 */

var mapPanel;

Ext.onReady(function() {
    var map = new OpenLayers.Map();
    var layer = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"}
    );
    map.addLayer(layer);

    var scaleStore = new GeoExt.data.ScaleStore({map: map});
    var zoomSelector = new Ext.form.ComboBox({
        store: scaleStore,
        emptyText: "Zoom Level",
        tpl: '<tpl for="."><div class="x-combo-list-item">1 : {[parseInt(values.scale)]}</div></tpl>',
        editable: false,
        triggerAction: 'all', // needed so that the combo box doesn't filter by its current content
        mode: 'local' // keep the combo box from forcing a lot of unneeded data refreshes
    });

    zoomSelector.on('select', 
        function(combo, record, index) {
            map.zoomTo(record.data.level);
        },
        this
    );     

    map.events.register('zoomend', this, function() {
        var scale = scaleStore.queryBy(function(record){
            return this.map.getZoom() == record.data.level;
        });

        if (scale.length > 0) {
            scale = scale.items[0];
            zoomSelector.setValue("1 : " + parseInt(scale.data.scale));
        } else {
            if (!zoomSelector.rendered) return;
            zoomSelector.clearValue();
        }
    });

    mapPanel = new GeoExt.MapPanel({
        title: "GeoExt MapPanel",
        renderTo: "mappanel",
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4,
        bbar: [zoomSelector]
    });
});

/**
 * Copyright (c) 2008-2011 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[zoomslider]
 *  Zoom Slider
 *  -----------
 *  Use a slider to control map scale.
 */

var panel, slider;

Ext.onReady(function() {
    
    // create a map panel with an embedded slider
    panel = new GeoExt.MapPanel({
        title: "Map",
        renderTo: "map-container",
        height: 300,
        width: 400,
        map: {
            controls: [new OpenLayers.Control.Navigation()]
        },
        layers: [new OpenLayers.Layer.WMS(
            "Global Imagery",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {layers: "bluemarble"}
        )],
        extent: [-5, 35, 15, 55],
        items: [{
            xtype: "gx_zoomslider",
            vertical: true,
            height: 100,
            x: 10,
            y: 20,
            plugins: new GeoExt.ZoomSliderTip()
        }]
    });
    
    // create a separate slider bound to the map but displayed elsewhere
    slider = new GeoExt.ZoomSlider({
        map: panel.map,
        aggressive: true,                                                                                                                                                   
        width: 200,
        plugins: new GeoExt.ZoomSliderTip({
            template: "<div>Zoom Level: {zoom}</div>"
        }),
        renderTo: document.body
    });

});
