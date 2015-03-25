/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
 /**
 * Skies theme for Highcharts JS
 * @author Torstein Honsi
 */

Highcharts.theme = {
   colors: ["#514F78", "#42A07B", "#9B5E4A", "#72727F", "#1F949A", "#82914E", "#86777F", "#42A07B"],
   chart: {
      className: 'skies',
      borderWidth: 0,
      plotShadow: true,
      plotBackgroundImage: '/demo/gfx/skies.jpg',
      plotBackgroundColor: {
         linearGradient: [0, 0, 250, 500],
         stops: [
            [0, 'rgba(255, 255, 255, 1)'],
            [1, 'rgba(255, 255, 255, 0)']
         ]
      },
      plotBorderWidth: 1
   },
   title: {
      style: { 
         color: '#3E576F',
         font: '16px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
      }
   },
   subtitle: {
      style: { 
         color: '#6D869F',
         font: '12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
      }
   },
   xAxis: {
      gridLineWidth: 0,
      lineColor: '#C0D0E0',
      tickColor: '#C0D0E0',
      labels: {
         style: {
            color: '#666',
            fontWeight: 'bold'
         }
      },
      title: {
         style: {
            color: '#666',
            font: '12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
         }            
      }
   },
   yAxis: {
      alternateGridColor: 'rgba(255, 255, 255, .5)',
      lineColor: '#C0D0E0',
      tickColor: '#C0D0E0',
      tickWidth: 1,
      labels: {
         style: {
            color: '#666',
            fontWeight: 'bold'
         }
      },
      title: {
         style: {
            color: '#666',
            font: '12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
         }            
      }
   },
   legend: {
      itemStyle: {
         font: '9pt Trebuchet MS, Verdana, sans-serif',
         color: '#3E576F'
      },
      itemHoverStyle: {
         color: 'black'
      },
      itemHiddenStyle: {
         color: 'silver'
      }
   },
   labels: {
      style: {
         color: '#3E576F'
      }
   }
};

// Apply the theme
var highchartsOptions = Highcharts.setOptions(Highcharts.theme);

		
		
		
