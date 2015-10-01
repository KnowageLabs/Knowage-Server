/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.cockpit.widgets.chartengine");


Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner = function(config) {
	
	var defaultSettings = {
		name: 'chartEngineWidgetDesigner',
//		title: LN('sbi.cockpit.widgets.chartengine.chartEngineDesigner.title'),
	};

	
	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.chartengine && Sbi.settings.cockpit.widgets.chartengine.chartEngineWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.chartengine.chartEngineWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.iFrameId = Ext.id();

	this.createContent();
	
	c = {
		layout: 'fit',
//		height: 500,
		header: false,
		border: false,
		items: [this.iFrameContent],
		tools: []
	};

	Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner.superclass.constructor.call(this, c);

	this.on(
			'beforerender' ,
			function (thePanel, attribute) {
				var form = this.chartEnginePanel.getForm();				
				form.submit({target: this.iFrameId});
				
			},
			this
		);
};

Ext.extend(Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	
	chartEnginePanel: null,
	
	wdigetChartDataset: null,
	
	chartTemplate: null,
	
	widgetContent: null,
	
	iFrameContent: null,
	
	iFrameId: null,
	
	aggregations: null,
	
	errorMsg: null

	, getDesignerState: function(running) {
		Sbi.trace("[ChartEngineWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Chart Engine Designer';
		state.wtype = Sbi.constants.cockpit.chart;
		state.chartTemplate = this.chartTemplate;
		state.aggregations = this.aggregations;
		state.wdigetChartDataset = this.wdigetChartDataset;
		
		Sbi.trace("[ChartEngineWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[ChartEngineWidgetDesigner.setDesignerState]: IN");
		
		Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner.superclass.setDesignerState(this, state);
		
		Sbi.trace("[ChartEngineWidgetDesigner.setDesignerState]: OUT");
	}
	
	, createContent: function() {
		
    	Sbi.trace("[ChartEngineWidgetDesigner.createContent]: IN");
    	
    	if(Sbi.isValorized(this.wdigetChartDataset)){
    		
    		var widgetData = this.getWidgetDataAsJson();
    	    	
	    	var chartDesignerUrl = Sbi.config.chartDesignerUrl;	    	 	
	    	
	    	this.chartEnginePanel = Ext.create('Ext.form.FormPanel',{
	    		standardSubmit: true,
	    		url: chartDesignerUrl,
	    		border: false,
	    		hideBorders: true,
	    		items: 
	    		[{
	    	        xtype: 'hiddenfield',
	    	        name: 'widgetData',
	    	        value: Ext.JSON.encode(widgetData)
	    	    }
	    		]
	    	});
	    	
	    	var randomId = Ext.id();
	    	
			this.iFrameContent = Ext.create('Ext.panel.Panel',{
				header:		false,
			    autoScroll: true,
			    html: 		'<iframe name="' + this.iFrameId + '" src="" width="100%" height="600"></iframe>',
			    listeners: {
	                render: function () {
	                	
	                	Ext.getBody().mask('Loading...');
	                },
	                afterrender: function () {
	                	
	                	var thePanel = this;
	                	
	                	var el = this.getEl();
	    				var iFrameHTML = Ext.DomQuery.selectNode("iframe", el.dom);
	    				
	    				iFrameHTML.onload = function () {
	    					Ext.getBody().unmask();
	                    };
	                }
	            }
			});
			
			this.doLayout();
    	}
		
		Sbi.trace("[ChartEngineWidgetDesigner.createContent]: OUT");
	}
	
	, getWidgetDataAsJson: function(){
		
		var result = {};
		
		var WIDGET = {};
		
		WIDGET['datasetLabel'] = this.wdigetChartDataset;
		
		if(Sbi.isValorized(this.chartTemplate)){
			WIDGET['chartTemplate'] = this.chartTemplate;
    	}		 
		
		result['widgetData'] = WIDGET;
		
		return result;
	}
	
	, setAggregationsOnChartEngine: function(){
	
		this.aggregations = {};
		
		if(Sbi.isValorized(this.chartTemplate) && 
				Sbi.isValorized(this.chartTemplate.CHART) && 
				Sbi.isValorized(this.chartTemplate.CHART.VALUES)) {
			
			if(Sbi.isValorized(this.chartTemplate.CHART.VALUES.SERIE)) {
			
				var chartSeries = this.chartTemplate.CHART.VALUES.SERIE;
				
				var measures = [];
				
				for(var i = 0; i < chartSeries.length; i++){
					
					var serie = {};
					
					serie['id'] = chartSeries[i].name;
					serie['columnName'] = chartSeries[i].column;
					serie['funct'] = Sbi.isValorized(chartSeries[i].groupingFunction) ? chartSeries[i].groupingFunction : 'SUM';
					serie['alias'] = serie['columnName'] + '_' + serie['funct'];
					
					measures.push(serie);					
				}
				
				this.aggregations['measures'] = measures;
			}
			
			if(Sbi.isValorized(this.chartTemplate.CHART.VALUES.CATEGORY)){
				
				var categories = [];
				
				var chartCategory= this.chartTemplate.CHART.VALUES.CATEGORY;
				
				if(chartCategory instanceof Array){
					
					for(var i = 0; i < chartCategory.length; i++){
						
						var category = {};
						
						category['id'] = chartCategory[i].name;
						category['columnName'] = chartCategory[i].column;
						category['alias'] = chartCategory[i].name;
						
						categories.push(category);
					}
				} else {
					var category = {};
					
					category['id'] = chartCategory.name;
					category['columnName'] = chartCategory.column;
					category['alias'] = chartCategory.name;
					
					categories.push(category);
				}
				
				this.aggregations['categories'] = categories;
			}
		}
	}
	
	, setErrorMessage: function (msg){
		this.errorMsg = msg;
	}
	
	, getErrorMessage: function(){
		return this.errorMsg;
	}
	
});
