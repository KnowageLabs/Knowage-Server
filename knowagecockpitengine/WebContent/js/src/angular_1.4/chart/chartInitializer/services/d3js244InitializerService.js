/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


angular.module('chartInitializer')

.service('d3js244',function(){
	
	this.cleanChart = function(panel){
		
		d3.select(panel).selectAll("*").remove();
	}
	
	this.renderChart = function(chartConf,panel,locale){
		
		this.cleanChart(panel);
		
		if(chartConf.chart.type.toLowerCase() == "wordcloud") {			
			renderWordCloud(chartConf,panel,locale);			
		}
		else if (chartConf.chart.type.toLowerCase() == "sunburst") {
			renderSunburst(chartConf,panel,locale);			
		}
		else if (chartConf.chart.type.toLowerCase() == "parallel") {
			renderParallelChart(chartConf,panel,locale);			
		}
		else if (chartConf.chart.type.toLowerCase() == "chord") {
			renderChordChart(chartConf,panel,locale);
		}
		else {
			alert("Chart not defined");
		}
	}
	
	this.initChartLibrary = function(panel){
		
	}
	
	this.handleCockpitSelection = function(e){
		
		debugger;
		var cockpitWidgetManager = window.parent.cockpitPanel.widgetContainer.widgetManager;
		var cockpitWidgets = cockpitWidgetManager.widgets;
		//var widgetId = Sbi.chart.viewer.ChartTemplateContainer.widgetId;
		
		var selections = {};
//			selections[e.point.name] = {values: [e.point.series.name]};
		
		for(var i = 0; i < cockpitWidgets.getCount(); i++) {
			var widget = cockpitWidgets.get(i);
			
			if(widget && widget.wtype === 'chart' && widget.id === widgetId){
				
//					var fieldMeta = widget.getFieldMetaByValue(e.categoryValue);
//					var categoryFieldHeader = fieldMeta!=null?fieldMeta.header: null;
				
//				selections[categoryFieldHeader] = {values: [e.categoryValue]};					
				
				for(var category in e){
					
					
					 if (e.hasOwnProperty(category)) {
						 selections[category] = {values: [e[category]]};
					   
					    }
				}
             
                
					//console.log(selections);
				cockpitWidgetManager.onSelection(widget, selections);
			}
		}		
	}
})