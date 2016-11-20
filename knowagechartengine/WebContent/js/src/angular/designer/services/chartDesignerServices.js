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
				noDataDetailsURL: sbiModule_config.contextName + '/js/src/angular/designer/directives/custom_directives/configuration-tab/nodata.html'
					 
			 };                   
		return data;
	};
});	