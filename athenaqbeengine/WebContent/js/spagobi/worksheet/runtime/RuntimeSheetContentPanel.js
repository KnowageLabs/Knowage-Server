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
 *  contentloaded: fired after the data has been loaded
 * 
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeSheetContentPanel = function(config) { 

	var defaultSettings = {};
		
	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime && Sbi.settings.worksheet.runtime.runtimeSheetContentPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.runtimeSheetContentPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	if(config.hiddenContent){
		if(config.contentConfig==undefined || config.contentConfig==null){
			config.contentConfig = {};
		}
		config.contentConfig.hiddenContent=true;
	}
	
	Ext.apply(this, c);
	
    this.addEvents('contentloaded');
    this.addEvents('contentloading');
    
	this.content = this.initContent(c);
	
	//catch the event of the contentloaded and throws it to the parent
	this.content.on('contentloaded',function(empty){this.fireEvent('contentloaded', empty);},this);
	
	this.on('render', function(panel){
		(panel.getEl().on('click', function(event, element, object){this.content.fireEvent('contentclick', event )}, this));
	}, this);	
	c = {
		border: false,
		style:'padding:5px 15px 5px; text-align:center;',
		items: this.content,
		autoHeight: true
	};
	if(config.hiddenContent){
		c.hidden = true;
	}
	
	Sbi.worksheet.runtime.RuntimeSheetContentPanel.superclass.constructor.call(this, c);	

};

Ext.extend(Sbi.worksheet.runtime.RuntimeSheetContentPanel, Ext.Panel, {
	content : null
	, sheetName : null

	, exportContent: function(filtersValue){
		//if the content is hidden we return an empty type content
//		if(this.hidden){
//			return {SHEET_TYPE: 'EMPTY'};
//		}
		
		if(this.contentConfig.designer == 'Table') {
			var visibleselectfields = (this.contentConfig.visibleselectfields);
    		var params ={'visibleselectfields': visibleselectfields};
    		return this.content.exportContent(params);
		}else{
			return this.content.exportContent();
		}
	},
	
	initContent: function (c) {
		var items = [];
		switch (this.contentConfig.designer) {
	        case 'Pivot Table':
	        	return this.initCrossTab(c);
	        case 'Static Pivot Table':
	        	return this.initStaticCrossTab(c);
	        case 'Bar Chart':
	        	return Sbi.worksheet.runtime.RuntimeChartFactory.createBarChart({'chartConfig':this.contentConfig, sheetName : this.sheetName, fieldsOptions: this.fieldsOptions});
	        case 'Line Chart':
	        	return Sbi.worksheet.runtime.RuntimeChartFactory.createLineChart({'chartConfig':this.contentConfig, sheetName : this.sheetName, fieldsOptions: this.fieldsOptions});
	        case 'Pie Chart':
	        	return Sbi.worksheet.runtime.RuntimeChartFactory.createPieChart({'chartConfig':this.contentConfig, sheetName : this.sheetName, fieldsOptions: this.fieldsOptions});
	        case 'Table':
	        	return this.initTable(c);
	        default: 
	        	alert('Unknown widget!');
		}
	},
	
	
	initTable: function(c){
		
		var ieFixVar = {};
		
		if(Ext.isIE){
			ieFixVar ={
				padding: '0 5 0 0',	
				style:'width: 95%'
			}
		}

    	var table =  new Sbi.formviewer.DataStorePanel(Ext.apply({
    		split: true,
    		collapsible: false,
    		padding: '0 20 0 0',
    		autoScroll: true,
    		frame: false, 
    		border: false,
    		displayInfo: false,
    		pageSize: 50,
    		//autoHeight: true,
    		sortable: false,
    		gridConfig: {
    			height: Sbi.settings.worksheet.runtime.table.height || 400
    			//autoHeight: true // setting autoHeight to true, scrollbars do not appear (ExtJS sets overflow : visible to the element style)
    		},
    		services: {
    			loadDataStore: Sbi.config.serviceRegistry.getServiceUrl({
    				serviceName: 'EXECUTE_WORKSHEET_QUERY_ACTION'
    				, baseParams: {sheetName : this.sheetName, fieldsOptions:  Ext.encode(c.fieldsOptions)}//baseParams: {'visibleselectfields': Ext.encode(this.contentConfig.visibleselectfields)}
    			})
    		}
    	},ieFixVar));
    	if(!c.hiddenContent){
    		table.execQuery({'visibleselectfields': Ext.encode(this.contentConfig.visibleselectfields)});
    	}else{
    		this.on('afterrender', function(){this.fireEvent('contentloaded')}, this);
    	}
		return table;
	},
	
	initCrossTab: function(c){
		var crossTab = new Sbi.crosstab.CrosstabPreviewPanel(Ext.apply(c.contentConfig|| {},{
			hideLoadingMask: true,
			sheetName : this.sheetName,
			crosstabConfig: {autoHeight: true}, 
			fieldsOptions: c.fieldsOptions,
			title: false}));
		if(!c.hiddenContent){
			this.on('afterlayout',this.loadCrosstab,this);
		}else{
			this.on('afterrender', function(){this.fireEvent('contentloaded')}, this);
		}
		
		return crossTab;
	},
	
	initStaticCrossTab: function(c){
		var crossTab = new Sbi.crosstab.StaticCrosstabPreviewPanel(Ext.apply(c.contentConfig|| {},{
			hideLoadingMask: true,
			sheetName : this.sheetName,
			crosstabConfig: {autoHeight: true}, 
			fieldsOptions: c.fieldsOptions,
			title: false
		}));
		if(!c.hiddenContent){
			this.on('afterlayout',this.loadCrosstab,this);
		}else{
			this.on('afterrender', function(){this.fireEvent('contentloaded')}, this);
		}
		
		return crossTab;
	},
	
	loadCrosstab: function(){
		this.content.load(this.contentConfig.crosstabDefinition);
		this.un('afterlayout',this.loadCrosstab,this);
	},
	
	applyFilters: function(filtersValue){
		this.fireEvent('contentloading');
		this.contentConfig.hiddenContent=false;
		switch (this.contentConfig.designer) {
	        case 'Pivot Table':
	        	this.content.load(this.contentConfig.crosstabDefinition, filtersValue);
	        	break;
	        case 'Static Pivot Table':
	        	this.content.load(this.contentConfig.crosstabDefinition, filtersValue);
	        	break;
	        case 'Table':
        		var params ={'visibleselectfields': Ext.encode(this.contentConfig.visibleselectfields)};
        		if(filtersValue!=undefined && filtersValue!=null){
        			params.FILTERS = Ext.encode(filtersValue);
        		}
        		this.content.execQuery(params);
	        	break;
	        case 'Bar Chart':
	        	this.content.loadChartData({
	        		'rows':[this.contentConfig.category]
	        		, 'measures':this.contentConfig.series
	        		, 'columns': this.contentConfig.groupingVariable ? [this.contentConfig.groupingVariable] : []}
	        		, filtersValue);
	        	break;
	        case 'Line Chart':
	    		this.content.loadChartData({
	        		'rows':[this.contentConfig.category]
	        		, 'measures':this.contentConfig.series
	        		, 'columns': this.contentConfig.groupingVariable ? [this.contentConfig.groupingVariable] : []}
	        		, filtersValue);
	        	break;
	        case 'Pie Chart':
	        	this.content.loadChartData({'rows':[this.contentConfig.category],'measures':this.contentConfig.series},filtersValue);
	        	break;
		}

		this.show();
	}

	, getAdditionalData: function(){
		var data ={};
		if(this.contentConfig.designer=='Pivot Table') {
			data.crosstabDefinition = {'calculatedFields': this.content.getCalculatedFields()};
			data.crosstabDefinition.additionalData = {'columnWidth': this.content.getCrosstabColumnWidth()};
		}
		return data;
	}

});
