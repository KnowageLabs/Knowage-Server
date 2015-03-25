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
 * Authors - Antonella Giachino
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.KpiGUIHistorical =  function(config) {
		var defaultSettings = {id: 'formChartPanel'};

		if (Sbi.settings && Sbi.settings.kpi && Sbi.settings.kpi.KpiGUIHistorical) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.kpi.KpiGUIHistorical);
		}
		
		this.services = new Array();
		this.services['loadHistoricalValues'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_TREND_KPI_VALUES'
		  , baseParams: {
					LIGHT_NAVIGATOR_DISABLED: 'TRUE'
			}
		});

		var c = Ext.apply(defaultSettings, config || {});

		this.serverDateFormat = c.serverDateFormat;
		
		Ext.apply(this, c);
		
		this.initHistorical(c);

		Sbi.kpi.KpiGUIHistorical.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.kpi.KpiGUIHistorical , Ext.form.FormPanel, {
	items: null
  , fieldDateFrom: null
  , fieldDateTo: null
  , formatDateFrom: null
  , formatDateTo: null
  , serverExtTimestampFormat: null
  , btnUpdate: null
  , tb: null
  , miframe : null
  , perWin: null
  , services: null
  , serverDateFormat: null
  , kpiInstId: null
  , kpiDescr: null
  , chartPanel: null
  , store: null
  , template: null
  , loadMask: null
  , chartBaseUrl: null
  , noChartTrendMsg: null
  
  ,	initHistorical: function(config){
	    this.chartBaseUrl = config.chartBaseUrl;
	    this.serverExtTimestampFormat = config.serverExtTimestampFormat;
	  	var tmpDateFrom = new Date();
	  	tmpDateFrom.setDate(tmpDateFrom.getDate()-7);
	    this.chartid = Ext.id();
		this.fieldDateFrom = new Ext.form.DateField({
			id: 'from',
			name: 'from',
			width: 150, 
			fieldLabel: LN('sbi.generic.from'),
			format: config.localeExtDateFormat,	
			allowBlank: false,
			//x:20,
			value: tmpDateFrom
		});
		this.fieldDateTo = new Ext.form.DateField({
			id: 'to',
			name: 'to',
			width: 150, 
			fieldLabel: LN('sbi.generic.to'), 
			format: config.localeExtDateFormat,		
			allowBlank: false,
			x: 150,
			value: new Date()
		});
		this.btnUpdate = new Ext.Button({
			text: LN('sbi.generic.update2'),
			handler: function(){	
				var tmpField = {attributes: {kpiInstId: this.kpiInstId,
											 kpiDescr: this.kpiDescr}};
	        	this.fireEvent('click', this, this.update(tmpField));
	        }
	        , scope: this
		});
		var tb = new Ext.Toolbar({
			items: [this.fieldDateFrom, 
			        {xtype: 'tbspacer', width: 25}, 
			        this.fieldDateTo, 
			        {xtype: 'tbspacer', width: 30},
			        this.btnUpdate]
		});
		this.items = [tb];
	}
	
	, cleanPanel: function(){
		if(this.chartPanel != null){
			this.remove(this.chartPanel );			
		}
		if(this.perWin != null){
			this.remove(this.perWin );
		}
		if(this.noChartTrendMsg != null){
			this.remove(this.noChartTrendMsg);
		}
	}
	
	, update:  function(field){	
		this.showMask();
		this.cleanPanel();

		if (field == undefined || field == null || field.attributes.kpiInstId == undefined){
			this.noChartTrendMsg = new Ext.form.DisplayField({
				value: LN('sbi.kpi.trend.nodata'), 
				style: 'font-style: italic;'}); 
			this.add(this.noChartTrendMsg);
			this.doLayout();
			this.hideMask();
			return;
		}
		this.kpiInstId = field.attributes.kpiInstId;
		this.kpiDescr = field.attributes.kpiDescr;
		this.formatDateFrom = Sbi.commons.Format.date( this.fieldDateFrom.getValue() , this.serverDateFormat);
		this.formatDateTo = Sbi.commons.Format.date( this.fieldDateTo.getValue() , this.serverDateFormat);
	  	this.template = this.createTemplate();

	  	Ext.chart.Chart.CHART_URL = 'resources/yui_charts.swf';
		this.chartPanel = new Ext.chart.LineChart(this.template);
		
		this.perWin = new Ext.Panel({
	        scope: this,
	        autoScroll: true,
	        border: false,
	        height: 400,
	    	//title: LN('sbi.kpi.trend.title') + this.kpiDescr ,
	    	title: LN('sbi.kpi.trend.title') ,
	    	items: [this.chartPanel]			
		});		
		this.add(this.perWin);
		this.doLayout();
		this.render();
		//this.perWin.un('afterrender',this.hideMask,this);
		this.hideMask.defer(2000, this);
	}
	
	, createStore: function() {
		var store;
		var params = {kpiInstId: this.kpiInstId,
					  dateFrom: this.formatDateFrom,
					  dateTo: this.formatDateTo};

		var serviceConfig;
		serviceConfig = {serviceName: 'GET_TREND_KPI_VALUES'};
		serviceConfig.baseParams = params;	
		
		store = new Ext.data.JsonStore({
			autoLoad: false    	  
    	  , id : 'id'		
    	  , fields:[{name:'KPI_DATE'},{name:'KPI_VALUE', type:'float'}]  
    	  , root: 'trends'
		  , url: Sbi.config.serviceRegistry.getServiceUrl( serviceConfig )
		});
		
		store.on('exception', Sbi.exception.ExceptionHandler.onStoreLoadException, this);
		store.load();
		return store;
		
	}
	
	, createTemplate: function() {
		var template = {      
			    store: this.createStore()  
			  , url: this.chartBaseUrl
			  , xField: 'KPI_DATE'  
			  , extraStyle:{
  			    	xAxis: {labelRotation: 45  			    			
  			    	}
			    }
			, series: [{
			          type: 'line',
			          displayName: 'KPI Values',
			          yField: 'KPI_VALUE',
			          style: {
			        	  color: "#680000", 
			              size: 7,
			              borderColor: 0xCC0000,
			              fillColor:0xffffff,
			              connectPoints:true
			          }}]
			};
		
		return template;
	}
	
	/**
	 * Opens the loading mask 
	 */
    , showMask : function(){  
    	if (this.loadMask == null) {
    		this.loadMask = new Ext.LoadMask('formChartPanel', {msg: "Loading.."});    		
    	}
    	this.loadMask.show();
    }

	/**
	 * Closes the loading mask
	*/
	, hideMask: function() {
    	if (this.loadMask != null) {	
    		this.loadMask.hide();
    	}
	} 
});
