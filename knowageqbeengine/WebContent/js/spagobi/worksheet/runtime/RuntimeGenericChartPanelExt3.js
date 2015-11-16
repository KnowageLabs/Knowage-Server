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
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  * 
  * Public Events
  * 
  * Authors
  * 
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeGenericChartPanelExt3  = function(config) { 
	
	var defaultSettings = {
			defaultFontSize: 10
		};
	
	var c = Ext.apply(defaultSettings, config || {});
	
	this.addEvents();
	
	Sbi.worksheet.runtime.RuntimeGenericChartPanelExt3.superclass.constructor.call(this, c);	 	

};

Ext.extend(Sbi.worksheet.runtime.RuntimeGenericChartPanelExt3, Sbi.worksheet.runtime.RuntimeGenericChartPanel, {

	ieChartHeight: 400,
	charts: null, //the list of charts of the panel. Should be 1 for bar and line and can be more than one for pie
	

	exportContent: function() {
		var chartsByteArrays = new Array();
//		if(this.charts!=undefined && this.charts!=null){
//			for(var i =0; i<this.charts.length; i++){
//				chartsByteArrays.push((this.charts[i]).swf.exportPNG());
//			}
//		}
		
		var exportedChart = {CHARTS_ARRAY:this.byteArrays, SHEET_TYPE: 'CHART', CHART_TYPE:'ext3'};
		return exportedChart;
	}
	
	,headerClickHandler: function(event, element, object, chart, reloadCallbackFunction, reloadCallbackFunctionScope) {	
		
		if(!this.clickMenu){
			var clickMenuItems = new Array();
			if(!chart.bkseries){
				chart.bkseries = chart.series;
			}
			
			clickMenuItems.push(new Ext.menu.TextItem({text: LN('sbi.worksheet.runtime.worksheetruntimepanel.chart.visibleseries'), iconCls: 'show'}));
			clickMenuItems.push(new Ext.menu.Separator({}));
			
			for(var i=0; i<chart.series.length; i++){
				var freshCheck = new Ext.menu.CheckItem({
					checked: true,
					text: chart.series[i].displayName,
					serieNumber: i
				});
				freshCheck.on('checkchange', function(checkBox, checked){
					if(!checked){
						chart.hiddenseries.push(checkBox.serieNumber);
					}else{
						chart.hiddenseries.splice((chart.hiddenseries.indexOf(checkBox.serieNumber)),1);
					}
					
					var visibleSeries = new Array();
					//chart.setSeriesStylesByIndex(checkBox.serieNumber,{visibility:checked?"visible":"hidden"});
					for(var y=0; y<chart.bkseries.length; y++){
						if(chart.hiddenseries.indexOf(y)<0){
							visibleSeries.push(chart.bkseries[y]);
						}
					}

					chart.series = visibleSeries;
					if(reloadCallbackFunctionScope){
						reloadCallbackFunction(chart, reloadCallbackFunctionScope);
					}else{
						chart.refresh();
					}

				}, this);
				clickMenuItems.push(freshCheck);
				
			}

			this.clickMenu = new Ext.menu.Menu({
				items: clickMenuItems
			});
			
			//this.clickMenu.on('show',function(){chart.setSeriesStylesByIndex(0,{visibility: 'hidden'});}, this);


		}
		var x = 20;
		var y = 20;
		if (event!=null){
			x = event.getPageX();
			y = event.getPageY();
		}
		this.clickMenu.showAt([event.getPageX(), event.getPageY()]);

	}





	
	, getJsonStoreExt3: function(percent){
		var storeObject = {};
		
		var series = this.getSeries();
		var categories = this.getCategories();
		
		var data = new Array();
		var fields = new Array();
		var serieNames = new Array();

		
		for(var i=0; i<categories.length; i++){
			var z = {};
			var seriesum = 0;
			for(var j=0; j<series.length; j++){
				z['series'+j] = ((series[j]).data)[i];
				seriesum = seriesum + parseFloat(((series[j]).data)[i]);
			}
			if(percent){
				for(var j=0; j<series.length; j++){
					z['seriesflatvalue'+j] = z['series'+j];
					z['series'+j] = (z['series'+j]/seriesum)*100;;
				}	
			}
			z['seriesum'] = seriesum;
			z['categories'] = categories[i];
			data.push(z);
		}
		
		for(var j=0; j<series.length; j++){
			fields.push('series'+j);
			fields.push('seriesflatvalue'+j);
			serieNames.push(series[j].name);
		}
		
		fields.push('seriesum');
		fields.push('categories');

		
	    var store = new Ext.data.JsonStore({
	        fields:fields,
	        data: data
	    });
	    
	    storeObject.store = store;
	    storeObject.serieNames = serieNames;

	    return storeObject;
	}
	
	
	, addChartConfExt3: function(chartConf, showTipMask){
		
		
		this.addStyle(chartConf);
		
		if((this.chartConfig.showlegend !== undefined) ? this.chartConfig.showlegend : true){
			if (chartConf.extraStyle === undefined || chartConf.extraStyle == null) {
				chartConf.extraStyle = {};
			}
			chartConf.extraStyle.legend = this.legendStyle;
		}
		chartConf.tipRenderer = this.getTooltipFormatter();
	}
	
	, addStyle: function(chart){
		this.addFontStyles();

		
		var axisTitleStyleFontSize = this.defaultFontSize;
		var axisValueStylefontSize = this.defaultFontSize;
			
		if(this.axisTitleStyle && this.axisTitleStyle.fontSize && this.axisTitleStyle.fontSize!=""){
			axisTitleStyleFontSize = this.axisTitleStyle.fontSize;
		}
		
		if(this.axisValueStyle && this.axisValueStyle.fontSize && this.axisValueStyle.fontSize!=""){
			axisValueStylefontSize = this.axisValueStyle.fontSize;
		}
		
		var extraStyle ={};
		
		if(this.axisTitleStyle){
			extraStyle.font = {
				size: axisTitleStyleFontSize

			};
		}
		
		if(this.axisValueStyle){
			extraStyle.dataTip = {
				font:{
					size: axisValueStylefontSize
				}
			};
		}
		
		this.legendStyle = {
				display : 'right',
				border : {
					color : "bcbcbc",
					size : 1
				},
				padding : 5,
				font : {
					family : 'Tahoma',
					size : this.axisTitleStyle.fontSize || Sbi.settings.worksheet.runtime.chart.legend.fontSize || 10
				}
			};

		if(!chart.extraStyle){
			chart.extraStyle={};
		}

		return Ext.apply(chart.extraStyle, extraStyle);
	}

	
	


});