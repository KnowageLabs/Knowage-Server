/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
/**
  * Object name 
  * 
  * this is just a staging area for utilities function waiting to be factored somewhere else
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

Sbi.geo.utils.GeoReportUtils = function(){
 
	return {
		
		/**
		 * computes mercator coordinates from latitude,longitude coordinates, 
		 * for map unsing mercator's projection (EPSG:900913)
		 */
		lonLatToMercator: function(ll) {
			var lon = ll.lon * 20037508.34 / 180;
			var lat = Math.log(Math.tan((90 + ll.lat) * Math.PI / 360)) / (Math.PI / 180);
			lat = lat * 20037508.34 / 180;
			return new OpenLayers.LonLat(lon, lat);
		}

		/**
		 * loads tile using google standard
		 */
		, osm_getTileURL: function(bounds) {
			var res = this.map.getResolution();
			var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
			var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
			var z = this.map.getZoom();
			var limit = Math.pow(2, z);

			if (y < 0 || y >= limit) {
				return OpenLayers.Util.getImagesLocation() + "404.png";
			} else {
				x = ((x % limit) + limit) % limit;
				return this.url + z + "/" + x + "/" + y + "." + this.type;
			}
		}
	};
	
}();







	