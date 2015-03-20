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
  * - Andrea Gioia (adrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
  */

Ext.ns("Sbi.geo.utils");

Sbi.geo.utils.LayerFactory = function(){
 
	return {
		
		createLayer : function( layerConf ){
			
			Sbi.trace("[LayerFactory.createLayer]: IN");
			
			Sbi.trace("[LayerFactory.createLayer]: layer type is equal to [" + layerConf.type + "]");
			
			var layer;
			if(layerConf.type === 'WMS') {
				layer = new OpenLayers.Layer.WMS(
					layerConf.name, layerConf.url, 
					layerConf.params, layerConf.options
				);
			} else if(layerConf.type === 'WFS') {
				var protocol = new OpenLayers.Protocol.WFS({
				    version: "1.1.0",
				    url:  layerConf.propsUrl,
				    featureType: layerConf.propsName
				    //featureNS: "http://www.openplans.org/topp",
				    //geometryName: "the_geom"
				});
				
				layer =  new OpenLayers.Layer.Vector(layerConf.label, {
				    strategies : [new OpenLayers.Strategy.Fixed()],
				    protocol : protocol
				});
				layer.refresh();
			} else if(layerConf.type === 'TMS') {
				layerConf.options.getURL = Sbi.geo.utils.GeoReportUtils.osm_getTileURL;
				layer = new OpenLayers.Layer.TMS(
					layerConf.name, layerConf.url, layerConf.options
				);
			} else if(layerConf.type === 'Google') {
				if(layerConf.options && layerConf.options.type) {				
					if(  (typeof layerConf.options.type) === "string") {
						if(layerConf.options.type === "google.maps.MapTypeId.TERRAIN") {
							layerConf.options.type = google.maps.MapTypeId.TERRAIN;
						} else if(layerConf.options.type === "google.maps.MapTypeId.HYBRID") {
							layerConf.options.type = google.maps.MapTypeId.HYBRID;
						} else if(layerConf.options.type === "google.maps.MapTypeId.SATELLITE") {
							layerConf.options.type = google.maps.MapTypeId.SATELLITE;
						} 
					}
				}
								
				layer = new OpenLayers.Layer.Google(
					layerConf.name, layerConf.options
				);
			} else if(layerConf.type === 'OSM') { 
				layer = new OpenLayers.Layer.OSM.Mapnik('OSM');
			}else {
				Sbi.exception.ExceptionHandler.showErrorMessage(
					'Layer type [' + layerConf.type + '] not supported'
				);
			}
			
			Sbi.trace("[LayerFactory.createLayer]: OUT");
			
			return layer;
		}
	};
	
}();







	