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
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.PieChartDesignerPanel = function(config) { 

	var defaultSettings = {
		title: LN('sbi.worksheet.designer.piechartdesignerpanel.title')
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.pieChartDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.pieChartDesignerPanel);
	}
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.genericChartDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.genericChartDesignerPanel);
	}
	
	this.chartLib = 'highcharts';
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.chartlib) {
		this.chartLib = Sbi.settings.worksheet.chartlib;
	}
	this.chartLib = this.chartLib.toLowerCase();
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.addEvents("attributeDblClick", "attributeRemoved");
	
	this.init();
	
	c = {
		items: [this.form]
	};
	
	Sbi.worksheet.designer.PieChartDesignerPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.designer.PieChartDesignerPanel, Sbi.worksheet.designer.GenericChartDesignerPanel, {

	form: null
	, items: null
	, showValuesCheck: null
	, categoryContainerPanel: null
	, seriesContainerPanel: null
	, axisDefinitionPanel: null
	, showLegendCheck: null
	, showPercentageCheck: null
	, seriesPalette: null
	, chartLib: null
	
	, init: function () {
		
		this.showValuesCheck = new Ext.form.Checkbox({
			checked: false
			, fieldLabel: LN('sbi.worksheet.designer.piechartdesignerpanel.form.showvalues.title')
		});
		
		this.showLegendCheck = new Ext.form.Checkbox({
			checked: false
			, fieldLabel: LN('sbi.worksheet.designer.piechartdesignerpanel.form.showlegend.title')
		});
		
		this.showPercentageCheck = new Ext.form.Checkbox({
			checked: false
			, fieldLabel: LN('sbi.worksheet.designer.piechartdesignerpanel.form.showpercentage.title')
		});
		
		this.categoryContainerPanel = new Sbi.worksheet.designer.ChartCategoryPanel({
            width: 200
            , height: 70
            , initialData: null
            , ddGroup: this.ddGroup
            , tools: [{
            	id: 'list'
  	        	, handler: function() {
					this.seriesPalette.show();
				}
  	          	, scope: this
  	          	, qtip: LN('sbi.worksheet.designer.piechartdesignerpanel.categorypalette.title')
            }]
		});
		// propagate events
		this.categoryContainerPanel.on(
			'attributeDblClick' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute); 
			}, 
			this
		);
		this.categoryContainerPanel.on(
			'attributeRemoved' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeRemoved", this, attribute); 
			}, 
			this
		);
		
		this.seriesContainerPanel = new Sbi.worksheet.designer.ChartSeriesPanel({
            width: 430
            , height: 120
            , initialData: []
            , crosstabConfig: {}
            , ddGroup: this.ddGroup
            , displayColorColumn: false
		});
		
		this.imageContainerPanel = new Ext.Panel({
            width: 200
            , height: 120
            , html: '<div class="piechart" style="height: 100%;"></div>'
		});
		
	    this.axisDefinitionPanel = new Ext.Panel({
	        layout: 'table'
	        , baseCls:'x-plain'
		    , cls: 'centered-panel' //for center the panel
			, width: this.seriesContainerPanel.width+this.imageContainerPanel.width+20 //for center the panel
	        , padding: '0 10 10 10'
	        , layoutConfig: {columns : 2}
	        // applied to child components
	        //, defaults: {height: 100}
	        , items:[
	            this.seriesContainerPanel
	            , this.imageContainerPanel 
	            , {
		        	border: false
		        }
		        , this.categoryContainerPanel
		    ]
	    });
	    
		this.seriesPalette = new Sbi.widgets.SeriesPalette({
			title: LN('sbi.worksheet.designer.piechartdesignerpanel.categorypalette.title')
			, height: 300
			, width: 150
			, closeAction: 'hide'
		});
	    
		var controlsItems = new Array();
		
		switch (this.chartLib) {
	        case 'ext3':
	        	break;
	        default: 
	        	controlsItems.push(this.showValuesCheck);
		} 
		
    	controlsItems.push(this.showLegendCheck);
    	controlsItems.push(this.showPercentageCheck);
    	
		//creates the font options
		this.addFontStyleCombos(controlsItems);
		
		this.form = new Ext.Panel({
			border: false
			, layout: 'form'
			, items: [
				{
					xtype: 'form'
					, style: 'padding: 10px 0px 0px 15px;'
				//	, title: LN('sbi.worksheet.designer.barchartdesignerpanel.form.fieldsets.options')
					, border: false
					, items: controlsItems
				}
				, 
				this.axisDefinitionPanel
			]
		});
	}

	, getFormState: function() {
		var state = {};
		state.designer = 'Pie Chart';
		state.showvalues = this.showValuesCheck.getValue();
		state.showlegend = this.showLegendCheck.getValue();
		state.showpercentage = this.showPercentageCheck.getValue();
		state.category = this.categoryContainerPanel.getCategory();
		state.series = this.seriesContainerPanel.getContainedMeasures();
		state.colors = this.seriesPalette.getColors();
		this.getGenericFormState(state);
		return state;
	}
	
	, setFormState: function(state) {
		if (state.showvalues) this.showValuesCheck.setValue(state.showvalues);
		if (state.showlegend) this.showLegendCheck.setValue(state.showlegend);
		if (state.showpercentage) this.showPercentageCheck.setValue(state.showpercentage);
		if (state.category) this.categoryContainerPanel.setCategory(state.category);
		if (state.series) this.seriesContainerPanel.setMeasures(state.series);
		if (state.colors) this.seriesPalette.setColors(state.colors);
		this.setGenericFormState(state);
	}
	, validate: function(validFields){
		var valErr='';	
		valErr+=''+this.categoryContainerPanel.validate(validFields);
		valErr+=''+this.seriesContainerPanel.validate(validFields);
		
		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1)
			return LN("sbi.worksheet.designer.validation.invalidFields")+valErr;
		}
		
		if (this.categoryContainerPanel.category== null){
			return LN("sbi.designerchart.chartValidation.noCategory");
		}
		var store = this.seriesContainerPanel.store;
		var seriesCount = store.getCount();
		if(seriesCount == 0 ){
			return LN("sbi.designerchart.chartValidation.noSeries");
		}
		

		
		return; 

	}
	
	, containsAttribute: function (attributeId) {
		if (this.categoryContainerPanel.category == null) {
			return false;
		} else {
			return this.categoryContainerPanel.category.id == attributeId;
		}
	}
	
	, getFontFormWidth: function(){
		return 300;
	}

	
});
