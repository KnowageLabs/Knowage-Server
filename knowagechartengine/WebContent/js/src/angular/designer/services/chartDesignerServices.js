/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero
 *  General Public License as published by
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

angular.module('ChartDesignerService', [])
.service('ChartDesignerData',function(sbiModule_restServices, sbiModule_messaging,sbiModule_translate,sbiModule_config){
	
	this.getFontSizeOptions = function(){
		var data = [
			{name:"8px",value:"8px"},
			{name:"9px",value:"9px"},
			{name:"10px",value:"10px"},
			{name:"11px",value:"11px"},
			{name:"12px",value:"12px"},
			{name:"14px",value:"14px"},
			{name:"16px",value:"16px"},
			{name:"18px",value:"18px"},
			{name:"20px",value:"20px"},
			{name:"22px",value:"22px"},
			{name:"24px",value:"24px"},
			{name:"26px",value:"26px"},
			{name:"28px",value:"28px"},
			{name:"36px",value:"36px"},
			{name:"48px",value:"48px"},
			{name:"72px",value:"72px"},
		                           ]
		return data;
	};
	
	this.getDimensionMeasureTypeOptions = function(){
		var data = [
			  {name:"px",value:"pixels"},
			  {name:"%",value:"percentage"}                          
			                             ]                        
		return data;
	};
	
	this.getOrientationTypeOptions = function(){
		var data = [
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.vertical"),value:"vertical"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.horizontal"),value:"horizontal"}                          
			                               ]                          
		return data;
	};
	
	this.getAlignTypeOptions = function(){
		var data = [
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.left"),value:"left"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.center"),value:"center"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.right"),value:"right"},
			                               ]                          
		return data;
	};
	
	this.getVerticalAlignTypeOptions = function(){
		var data = [
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top"),value:"top"},
				{name:sbiModule_translate.load("sbi.chartengine.configuration.alignment.m"),value:"middle"},
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom"),value:"bottom"},
			                               ]                          
		return data;
	};
	
	this.getParallelOrderOptions = function(){
		var data = [
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top"),value:"top"},
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom"),value:"bottom"},
			                               ]                          
		return data;
	};
	
	this.getWordLayoutOptions = function(){
		var data = [
		            
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.horizontal"),value:"horizontal"},		            
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.vertical"),value:"vertical"},
				{name:sbiModule_translate.load("sbi.chartengine.configuration.wordcloud.wordLayout.horizontalAndVerticaal"),value:"horizontalAndVertical"},
				{name:sbiModule_translate.load("sbi.chartengine.configuration.wordcloud.wordLayout.randomAngle"),value:"custom"},
			                               ]                          
		return data;
	};
	
	this.getPositionTypeOptions = function(){
		var data = [
		      {name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top"),value:"top"},
		      {name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom"),value:"bottom"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.left"),value:"left"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.right"),value:"right"},
			                               ]                          
		return data;
	};
	
	this.getFontFamilyOptions = function(){
		var data = [
           	{name:"Arial",value:"Arial"},
           	{name:"Times New Roman",value:"Times New Roman"},
           	{name:"Tahoma",value:"Tahoma"},
           	{name:"Verdana",value:"Verdana"},
           	{name:"Impact",value:"Impact"},
           	{name:"Calibri",value:"Calibri"},
           	{name:"Cambria",value:"Cambria"},
           	{name:"Georgia",value:"Georgia"},
           	{name:"Gungsuh",value:"Gungsuh"},
                                      ]                        
		return data;
	};
	
	this.getFontStyleOptions = function(){
		var data = [	
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.nostyle"),value:""},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.bold"),value:"bold"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.normal"),value:"normal"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.italic"),value:"italic"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.underline"),value:"underline"},
        	                        ]                       
		return data;
	};
	
	this.getCssStyles = function(template){
		
		 
		var data = [
           	{array:template.style,type:"chart"},
           	{array:template.TITLE.style,type:"title"},
           	{array:template.SUBTITLE.style,type:"subtitle"},
           	{array:template.EMPTYMESSAGE.style,type:"nodata"},
           	{array:template.LEGEND.TITLE.style,type:"legendtitle"},	
           	                          ]
		template.LEGEND.style? data.push({array:template.LEGEND.style,type:"legend"}): null;
		template.LIMIT && template.LIMIT.style? data.push({array:template.LIMIT.style,type:"limitparallel"}): null;
		template.AXES_LIST.style? data.push({array:template.AXES_LIST.style,type:"axeslist"}): null;
		return data;
	};
	
	this.getTooltipBreadcrumbValueTypeOptions = function(){
		var data = [	
		 	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.absolute"),value:"absolute"},
		 	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.percentage"),value:"percentage"},
		 	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.combination"),value:"combination"},
		 	                        ]                           
		return data;
	};
	
	this.getChartConfigurationOptions = function(chart){
		
		var data = [
			{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.generic")},
            {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.titlesubtitle")},
            {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.nodata")}	
		];
		
		var legend = [
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.legendtitle")},
              {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.legenditems")}
		];
		
		switch (chart) {
		case 'parallel':
			var options = [
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.tooltip")},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.limit")},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.axislines")}
                                             ]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'sunburst':
			var options =[
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.sequence")},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.exlpanation")}
                                             ]
			Array.prototype.push.apply(data, options);
			return data;	
			break;
		case 'scatter':
			var options =[
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.ticksandlabels")}
			]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'treemap':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}]
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'wordcloud':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.wordsettings")}]
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'gauge':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")},
						  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.pane")}]     
			Array.prototype.push.apply(data, options);
			return data;  
			break;
		case 'line':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;     
			break;
		case 'heatmap':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}, {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.tooltip")}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'radar':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'bar':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'pie':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'chord':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette")}]
			Array.prototype.push.apply(data, options);
			return data;
			break;	
		default:
			break;
		}
	};
	
	this.getTemplateURLs = function(){
		var data = {
				genericDetailsURL:	sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/generic_details.html',
				titleSubtitleDetailsURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/title_and_subtitle.html',
				noDataDetailsURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/nodata.html',
				legendTitleURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/legend_title.html',
				legendItemsURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/legend_items.html',
				colorPaletteURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/color_palette.html',
				paneURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/pane.html',
				ticksURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/ticks.html',
				wordSettingsURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/word_settings.html',
				limitURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/limit.html',
				axisLinesURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/axis_lines.html',
				tooltipURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/tooltip.html',
				sequenceURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/sequence.html',
				explanationURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/explanation.html',
			 };                   
		return data;
	};
})
/**
 * Service for the Structure tab 
 */
.service("StructureTabService", function(sbiModule_restServices,sbiModule_messaging,sbiModule_translate,sbiModule_config){
	
	var translate = sbiModule_translate;
	
	this.getSeriesItemTypes = function() {
		
		var seriesItemTypes = 
		[
		 	{name: translate.load('sbi.chartengine.designer.charttype.notype'), value:''},
		 	{name: translate.load('sbi.chartengine.designer.charttype.bar'), value:'bar'},
		 	{name: translate.load('sbi.chartengine.designer.charttype.line'), value:'line'},
		 	{name: translate.load('sbi.chartengine.designer.charttype.area'), value:'area'},
		];
		
		return seriesItemTypes;
		
	}
    
    // Data for the Series item ordering types combobox
	this.getSeriesItemOrderingTypes = function() {
		
		var seriesItemOrderingTypes = 
		[
		 	{name: translate.load('sbi.chartengine.designer.seriesorder.none'), value:''},
	        {name: translate.load('sbi.chartengine.designer.seriesorder.asc'), value:'asc'}, 
	        {name: translate.load('sbi.chartengine.designer.seriesorder.desc'), value:'desc'}
		];
		
		return seriesItemOrderingTypes;
		
	}
	
	this.getScaleFactorsFixed = function() {
		
		var scaleFactorsFixed = 
		[
	       	{name: "No selection", value: "empty"},
	       	{name: "k (thousands)", value: "k"},
	       	{name: "M (millions)", value: "M"}
        ];
		
		return scaleFactorsFixed;
		
	}
	
	// Returns templates for specific details for series items on the Structure tab
	this.getSeriesItemsConfDetailsTemplateURL = function(detailsForOption) {
		
		var templatesURLs = "";
		
		switch(detailsForOption) {
			case "seriesItemConfig": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular/designer/directives/custom_directives/structure-tab/series_item_config_details.html"; break;
			case "seriesItemTooltip": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular/designer/directives/custom_directives/structure-tab/series_item_tooltip_details.html"; break;
			case "axisConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular/designer/directives/custom_directives/structure-tab/axis_configuration_details.html"; break;										
			case "axisTitleConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular/designer/directives/custom_directives/structure-tab/axis_title_details.html"; break;
			case "axisMajorGridConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular/designer/directives/custom_directives/structure-tab/axis_majorgrid_details.html"; break;
			case "axisMinorGridConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular/designer/directives/custom_directives/structure-tab/axis_minorgrid_details.html"; break;
		};
		
		return templatesURLs;
		
	}
	
	// Get the name of the Details panel on the Structure tab according to the options that is picked
	this.getStructureTabDetailsName = function(detailsForOption) {
		
		var detailsNameToReturn = "";
		
		switch(detailsForOption) {
			case "seriesItemConfig": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.seriesdetails.toolbar.title"); break;
			case "seriesItemTooltip": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.seriestooltipdetails.toolbar.title"); break;
			case "axisConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.configuration.toolbar.title"); break;
			case "axisTitleConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.title.toolbar.title"); break;
			case "axisMajorGridConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.majorgrid.toolbar.title"); break;
			case "axisMinorGridConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.minorgrid.toolbar.title"); break;
		}
		
		return detailsNameToReturn;
		
	}
	
	// The skeleton for the JS object that will hold the information about the values of properties of the series item TOOLTIP style
	this.getSeriesTooltipStyle = function() {
		
		var seriesTooltipStyle = {
			 	fontFamily:"",
				fontSize:"",
				fontWeight:"",
				color:"",
				align:""
			 };
				
		return seriesTooltipStyle;
		
	}
	
	// The types of aggregation for series items
	this.getSeriesItemAggregationTypes = function() {
		 
		var seriesItemAggregationTypes = 
		[
			{name:'AVG',value:'AVG'},
			{name:'COUNT',value:'COUNT'},
			{name:'MAX',value:'MAX'},
			{name:'MIN',value:'MIN'},
			{name:'SUM',value:'SUM'}
		];
		 
		return seriesItemAggregationTypes;
		 
	}
	
	// The skeleton for the JS object that will hold the information about the values of properties of the axis configuration style
	this.getAxisConfigurationStyle = function() {
		
		var axisConfigurationStyle = {
				rotate:"",
			 	fontFamily:"",
				fontSize:"",
				fontWeight:"",
				color:"",
				align:""
			 };
				
		return axisConfigurationStyle;
		
	}
	
	// The skeleton for the JS object that will hold the information about the values of properties of the axis TITLE configuration style
	this.getAxisTitleConfigurationStyle = function() {
		
		var axisTitleConfigurationStyle = {
			 	fontFamily:"",
				fontSize:"",
				fontWeight:"",
				color:"",
				align:""
			 };
				
		return axisTitleConfigurationStyle;
		
	}
	
	// The skeleton for the JS object that will hold the information about the values of properties of the axis MAJORGRID/MINORGRID configuration style
	this.getAxisMinorGridConfigurationStyle = function() {
		
		var axisMajorMinorGridConfigurationStyle = {
			 	typeline:"",
				color:""
			 };
				
		return axisMajorMinorGridConfigurationStyle;
		
	}
	
	// The skeleton for the JS object that will hold the information about the values of properties of the axis MAJORGRID/MINORGRID configuration style
	this.getAxisMajorGridConfigurationStyle = function() {
		
		return this.getAxisMinorGridConfigurationStyle();
		
	}
	
});	