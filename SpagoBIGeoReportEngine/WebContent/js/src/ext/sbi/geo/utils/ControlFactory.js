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

Sbi.geo.utils.ControlFactory = function(){
 
	return {
		
		createControl : function( controlConf ){
			var control;
			
			if(controlConf.type === 'Navigation') {
				
				/**
				 * The navigation control handles map browsing with mouse 
				 * events (dragging, double-clicking, and scrolling the wheel).  
				 * URL: http://dev.openlayers.org/docs/files/OpenLayers/Control/Navigation-js.html
				 */
				control =  new OpenLayers.Control.Navigation();
				
			} else if(controlConf.type === 'KeyboardDefaults') {
				
				/**
			     * The KeyboardDefaults control adds panning and zooming functions, 
			     * controlled with the keyboard.  By default arrow keys pan, +/- keys 
			     * zoom & Page Up/Page Down/Home/End scroll by three quarters of a page.
			     */
				control =  new OpenLayers.Control.KeyboardDefaults();
				
			} else if(controlConf.type === 'MousePosition') {
				control =  new OpenLayers.Control.MousePosition();
			} else if(controlConf.type === 'OverviewMap') {
				control =  new OpenLayers.Control.OverviewMap({
					mapOptions: controlConf.mapOptions
				});
			} else if(controlConf.type === 'PanZoomBar') {
				control =  new OpenLayers.Control.PanZoomBar();
			} else if(controlConf.type === 'SbiActionsMap') {
				control =  new Sbi.geo.control.InlineToolbar();
			} else if(controlConf.type === 'SbiLegendMap') {
				control =  new Sbi.geo.control.Legend();
			} else if(controlConf.type === 'SbiMeasureMap') {
				control =  new Sbi.geo.control.Measure();
			} else if(controlConf.type === 'SbiLayersMap') {
				control =  new Sbi.geo.control.Layers();
			} else {
				control=null;
			}
		
			
			return control;
		}
	};
	
}();	