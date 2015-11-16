/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * Object name 
  * 
  * Singleton object that handle all errors generated on the client side
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
  * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
  */


Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeChartFactory = function(){
	// do NOT access DOM from here; elements don't exist yet
 
    // private variables
 
    // public space
	return {
	
		init : function() {
		},
		
		
        createLineChart : function(config) {
        	var chartLib = 'highcharts';
        	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.chartlib) {
        		chartLib = Sbi.settings.worksheet.chartlib;
        	}
        	chartLib = chartLib.toLowerCase();
    		switch (chartLib) {
		        case 'ext3':
		        	return new Sbi.worksheet.runtime.RuntimeLineChartPanelExt3(config);
		        default: 
		        	return new Sbi.worksheet.runtime.RuntimeLineChartPanelHighcharts(config);
			}       	
        },

        createBarChart : function(config) {
        	var chartLib = 'highcharts';
        	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.chartlib) {
        		chartLib = Sbi.settings.worksheet.chartlib;
        	}
        	chartLib = chartLib.toLowerCase();
    		switch (chartLib) {
		        case 'ext3':
		        	return new Sbi.worksheet.runtime.RuntimeBarChartPanelExt3(config);
		        default: 
		        	return new Sbi.worksheet.runtime.RuntimeBarChartPanelHighcharts(config);
			}       	
        },
        
        createPieChart : function(config) {
        	var chartLib = 'highcharts';
        	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.chartlib) {
        		chartLib = Sbi.settings.worksheet.chartlib;
        	}
        	chartLib = chartLib.toLowerCase();
    		switch (chartLib) {
		        case 'ext3':
		        	return new Sbi.worksheet.runtime.RuntimePieChartPanelExt3(config);
		        default: 
		        	return new Sbi.worksheet.runtime.RuntimePieChartPanelHighcharts(config);
			}       	
        }

	};
}();