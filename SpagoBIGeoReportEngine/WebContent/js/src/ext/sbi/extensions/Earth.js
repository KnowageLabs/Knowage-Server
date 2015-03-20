/*
* SpagoBI, the Open Source Business Intelligence suite
* 
* Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
* This file is part of MapFish Client, Copyright (C) 2007 Camptocamp 
* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the “Incompatible With Secondary Licenses” notice, according to the ExtJS Open Source License Exception for Development, version 1.03, January 23rd, 2012 http://www.sencha.com/legal/open-source-faq/open-source-license-exception-for-development/ 
* If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. 
* This file is an extension to Ext JS Library that is distributed under the terms of the GNU GPL v3 license. For any information, visit: http://www.sencha.com/license.
* 
* The original copyright notice of this file follows.*/
/*
 * Copyright (C) 2007-2008  Camptocamp
 *
 * This file is part of MapFish Client
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Client.  If not, see <http://www.gnu.org/licenses/>.
 */

google.load("earth", "1");

/*
 * Class mapfish.Earth
 * This class allows to add a Google Earth plugin in a div and to link it to
 * OpenLayers map. It creates a new vector layer to show the camera and lookAt
 * points on map.
 */
mapfish.Earth = OpenLayers.Class({

    // Default GE initial position
    lonLat: new OpenLayers.LonLat(0, 0),

    // Default GE altitude (meters)   
    altitude: 100,
    
    // Default GE heading (degrees)
    heading: 0,
    
    // Default GE tilt (degrees)
    tilt: 0,
    
    // Default GE range (meters)
    range: 100,
    
    // Optional KML URL to display in GE
    kmlUrl: null,
        
    /**
     * Function initialize
     * Prepares vector layer, adds links from 2D to 3D
     */
    initialize: function (map, earthDiv, options) {
    
        OpenLayers.Util.extend(this, options);        
        this.map = map;
        
        // GE is EPSG:4326
        this.geProjection = new OpenLayers.Projection("EPSG:4326");
        
        // Vector layer
        this.earthLayer = new OpenLayers.Layer.Vector("earthLayer");
        this.map.addLayer(this.earthLayer);

        // Camera and lookAt points to display
        this.features = [
            new OpenLayers.Feature.Vector(null, {role: 'line'}, {strokeColor: '#ff0000',
                                                                 strokeWidth: 3,
                                                                 pointRadius: 6}),
            new OpenLayers.Feature.Vector(null, {role: 'lookAt'}, {pointRadius: 8,
                                                                   fillColor: '#ff0000'}),
               new OpenLayers.Feature.Vector(null, {role: 'camera'}, {externalGraphic: 'eye.png',
                                                                      graphicHeight: 18,
                                                                      graphicWidth: 31,
                                                                      graphicYOffset: -3,
                                                                      rotation: 0})];                                                                             
        
        // Drag control to move camera ans lookAt points
        this.drag = new OpenLayers.Control.DragFeature(this.earthLayer, {
        
            earth: this,

            downFeature: function(pixel) {
                this.lastPixel = pixel;
                this.firstPixel = pixel;
                this.firstGeom = this.feature.geometry;
            },
                 
            moveFeature: function(pixel) {
     
                if (this.feature == null) {
                    return;
                }
                if (this.feature.attributes.role != 'line') {
                    var res = this.map.getResolution();
                    var x = res * (pixel.x - this.firstPixel.x) + this.firstGeom.x;
                    var y = res * (this.firstPixel.y - pixel.y) + this.firstGeom.y;
                    var lonLat = new OpenLayers.LonLat(x, y);
                    
                    if (this.feature.attributes.role == 'lookAt') {
                        this.earth.lookTo(lonLat)
                    } else if (this.feature.attributes.role == 'camera') {
                        this.earth.lookFrom(lonLat)
                    } 
                }
                this.lastPixel = pixel;
            }    
        });        
        map.addControl(this.drag);
        this.drag.activate();        
        
        // Refreshes GE on map move
        this.map.events.on({
            move: this.onMove,
            scope: this
        });                

        // Initializes GE
        this.initEarth(earthDiv);    
    },
    
    /**
     * Function transformToGE
     * Transforms a LonLat from map projection to GE projection
     */
    transformToGE: function(lonLat) {
        lonLat.transform(this.map.getProjectionObject(),
                         this.geProjection);
    },
    
    /**
     * Function transformFromGE
     * Transforms a LonLat from GE projection to map projection 
     */
    transformFromGE: function(geLonLat) {
        geLonLat.transform(this.geProjection,
                           this.map.getProjectionObject());
    },
    
    /**
     * Function initGE
     * Initializes Google Earth plugin
     */
    initGE: function(object) {
    
        this.ge = object;
        
        // Initializes position
        var lookAt = this.ge.createLookAt('');
        this.transformToGE(this.lonLat);
        lookAt.set(this.lonLat.lat, this.lonLat.lon,
                   this.altitude, this.ge.ALTITUDE_RELATIVE_TO_GROUND,
                   this.heading, this.tilt, this.range);
        this.ge.getView().setAbstractView(lookAt);
        
        // Initializes options
        this.ge.getWindow().setVisibility(true);
        this.ge.getOptions().setFlyToSpeed(this.ge.SPEED_TELEPORT);
        this.ge.getLayerRoot().enableLayerById(this.ge.LAYER_BUILDINGS, true);
        this.ge.getNavigationControl().setVisibility(this.ge.VISIBILITY_SHOW);
        
        // Downloads KML        
        if (this.kmlUrl) {
            google.earth.fetchKml(this.ge, this.kmlUrl, function(obj) {
                                                            this.ge.getFeatures().appendChild(obj); 
                                                        });
        }   
        
        // Adds listener to refresh camera and lookAt points on 2D map
        var self = this;
        google.earth.addEventListener(this.ge, "frameend", function() { self.onFrameEnd() });    
    },
    
    /**
     * Function failureGE
     * Displays and error when GE plugin load failed
     */
    failureGE: function(object) {
        alert("Google Earth load failed");
    },
    
    /**
     * Function initEarth
     * Creates GE plugin instance
     */
    initEarth: function(earthDiv) {
        var self = this;
        google.earth.createInstance(earthDiv, function(object) { self.initGE(object) },
                                              function(object) { self.failureGE(object) });    
    },
    
    /**
     * Function onMove
     * Changes GE position on 2D map move
     */
    onMove: function() {
        this.lookTo(this.map.getCenter());
    },
    
    /**
     * Function onFrameEnd
     * Changes camera and lookAt points on GE move
     */
    onFrameEnd: function() {
        this.refresh();
    },
       
    /** 
     * Function lookTo
     * Changes GE position on lookAt point move
     */
    lookTo: function(lonLat) {
        if (!this.ge) {
            return;
        }

        this.transformToGE(lonLat);
            
        var lookAt = this.ge.getView().copyAsLookAt(this.ge.ALTITUDE_RELATIVE_TO_GROUND);
        lookAt.setLongitude(lonLat.lon);
        lookAt.setLatitude(lonLat.lat);
        this.ge.getView().setAbstractView(lookAt);    
    },
    
    /**
     * Function lookFrom
     * Changes GE position on camera point move (rotation around lookAt point)
     */
    lookFrom: function(lonLat) {
        if (!this.ge) {
            return;
        }
    
        // Gets current lookAt position
        var lookAt = this.ge.getView().copyAsLookAt(this.ge.ALTITUDE_RELATIVE_TO_GROUND);  
        var geLonLat = new OpenLayers.LonLat(lookAt.getLongitude(),
                                             lookAt.getLatitude());
                                       
        // Computes distance between lookAt and camera for range computation      
        lonLatTmp = lonLat.clone();
        this.transformToGE(lonLatTmp);
        var dist = OpenLayers.Util.distVincenty(lonLatTmp, geLonLat) * 1000;
                                            
        // Computes rotation                                                        
        this.transformFromGE(geLonLat);            
        var rot = (180/Math.PI) * Math.atan((geLonLat.lon - lonLat.lon) / (geLonLat.lat - lonLat.lat));            
        if (geLonLat.lat < lonLat.lat) {
            rot = rot + 180;
        }
        lookAt.setHeading(rot);        
        
        // Computes range
        var tilt = lookAt.getTilt();
        var range = dist / Math.sin(tilt / (180/Math.PI));
        lookAt.setRange(range);
             
        this.ge.getView().setAbstractView(lookAt);
    },
    
    /**
     * Function refresh
     * Refreshes camera and lookAt drawings on 2D map
     */
    refresh: function() {
        if (!this.ge) {
            return;
        }   

        // Gets current camera ans lookAt positions
        lookAt = this.ge.getView().copyAsLookAt(this.ge.ALTITUDE_RELATIVE_TO_GROUND);
        var pl = new OpenLayers.LonLat(lookAt.getLongitude(), lookAt.getLatitude());
        this.transformFromGE(pl);    
        camera = this.ge.getView().copyAsCamera(this.ge.ALTITUDE_RELATIVE_TO_GROUND);
        var pc = new OpenLayers.LonLat(camera.getLongitude(), camera.getLatitude());
        this.transformFromGE(pc);

        // Computes new features positions
        this.earthLayer.removeFeatures(this.features);
        this.features[0].geometry =
            new OpenLayers.Geometry.LineString([new OpenLayers.Geometry.Point(pl.lon, pl.lat),
                                                new OpenLayers.Geometry.Point(pc.lon, pc.lat)]);
        this.features[1].geometry = new OpenLayers.Geometry.Point(pl.lon, pl.lat);
        this.features[2].geometry = new OpenLayers.Geometry.Point(pc.lon, pc.lat);
        
        // Computes eye orientation
        if (pl.lon == pc.lon) {
            this.features[2].style.rotation = 0;
        } else {
            var rot = (180/Math.PI) * Math.atan((pl.lon - pc.lon) / (pl.lat - pc.lat));        
            if (pl.lat > pc.lat) {
                this.features[2].style.rotation = rot + 180;
            } else {
                this.features[2].style.rotation = rot;
            }
        }
        
        // Redraws
        this.earthLayer.addFeatures(this.features);    
    }
});
