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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeLineChartPanelHighcharts = function(config) {
	
	var defaultSettings = {
	};

	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime.runtimeLineChartPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.runtimeLineChartPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);
	
	this.chartDivId = Ext.id();
	
	c = Ext.apply(c, {
		html : '<div id="' + this.chartDivId + '" style="width: 4000px; height: 2000px;"></div>' //
		, autoScroll: true
	});
	
	Sbi.worksheet.runtime.RuntimeLineChartPanelHighcharts.superclass.constructor.call(this, c);
	
	this.init();
	
};

Ext.extend(Sbi.worksheet.runtime.RuntimeLineChartPanelHighcharts, Sbi.worksheet.runtime.RuntimeGenericChartPanel, {
	
	chartDivId : null
	, chart : null
	, chartConfig : null // mandatory object to be passed as a property of the constructor input object. The template is:
//							template: {
//								type:"stacked-linechart", 
//								colorarea:true, 
//								showvalues:true, 
//								showlegend:true, 
//								category:
//									{id:"it.eng.spagobi.SalesFact1998::customer(customer_id):fullname", alias:"Full Name", funct:"NONE", iconCls:"attribute", nature:"attribute"}, 
//								series:[
//									{id:"it.eng.spagobi.SalesFact1998:unitSales", alias:"Unit Sales", funct:"SUM", iconCls:"measure", nature:"measure", seriename:"Unit Sales", color:"#9220CD"}, 
//									{id:"it.eng.spagobi.SalesFact1998:storeSales", alias:"Store Sales", funct:"SUM", iconCls:"measure", nature:"measure", seriename:"Store Sales", color:"#624D0F"}
//								]
//							}
	
	
	, init : function () {
		this.initGeneric();
		this.loadChartData({
			'rows':[this.chartConfig.category]
			, 'measures': this.chartConfig.series
			, 'columns': this.chartConfig.groupingVariable ? [this.chartConfig.groupingVariable] : []
		});
	}

	, createChart: function () {
		
		  var retriever = new Sbi.worksheet.runtime.DefaultChartDimensionRetrieverStrategy();
		  var size = retriever.getChartDimension(this);
		  this.update(' <div id="' + this.chartDivId + '" style="width: ' + size.width + '; height: ' + size.height + ';"></div>');

		  var series = this.getSeries();
		  var categories = this.getCategories();
		  
		  var thisPanel = this;
		  
		  var  chartConf = {
					exporting : {
						//url : this.services['exportChart']
						buttons : {
							exportButton : {enabled : false}
				  			, printButton : {enabled : false}
						}
					},
					chart : {
						renderTo : this.chartDivId,
						defaultSeriesType : (this.chartConfig.colorarea === true) ?  'area' : 'line',
						spacingTop : 25,
						spacingRight : 75,
						spacingBottom : 25,
						spacingLeft : 75
					},
					plotOptions: this.getPlotOptions(),
					legend: {
						enabled: (this.chartConfig.showlegend !== undefined) ? this.chartConfig.showlegend : true,
						labelFormatter: function() {
							return thisPanel.formatLegendWithScale(this.name)
						},
						layout: 'vertical',
						align: 'right',
						itemStyle: {
							fontSize: this.legendFontSize + 'px'
						}
					},
					tooltip: {
						enabled: true,
						formatter: this.getTooltipFormatter()
					},
					colors: this.getColors(),
					title : {
						text : ''
					},
					yAxis : {
						title : {
							text : ''
						}
					},
					xAxis : {
						type:'linear',
						categories : categories,
						title : {
							text : this.chartConfig.category.alias
						}
					},
					series : series,
					credits : {
						enabled : false
					}
				};
		  this.addStyle(chartConf);
		  
		  this.chart = new Highcharts.Chart(chartConf);
		  
		 
	}
	
	, getPlotOptions : function () {
		var plotOptions = null;

		if (this.chartConfig.colorarea === true) {
			plotOptions = {
				area: {
					stacking: this.getStacking(),
					dataLabels: {
						enabled: (this.chartConfig.showvalues !== undefined) ? this.chartConfig.showvalues : true,
						formatter: this.getDataLabelsFormatter(),
						style: this.getValueStyle()
					}
				}
			};
		} else {
			plotOptions = {
				line: {
					stacking: this.getStacking(),
					dataLabels: {
						enabled: (this.chartConfig.showvalues !== undefined) ? this.chartConfig.showvalues : true,
						formatter: this.getDataLabelsFormatter(),
						style: this.getValueStyle()
					}
				}
			};
		}
		return plotOptions;
	}
	
	, getStacking : function () {
		switch (this.chartConfig.type) {
	        case 'side-by-side-linechart':
	        	return null;
	        case 'stacked-linechart':
	        	return 'normal';
	        case 'percent-stacked-linechart':
	        	return 'percent';
	        default: 
	        	alert('Unknown chart type!');
	        return null;
		}
	}
	
});