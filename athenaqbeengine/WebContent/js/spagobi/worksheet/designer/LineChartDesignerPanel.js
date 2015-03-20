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

Sbi.worksheet.designer.LineChartDesignerPanel = function(config) { 

	var defaultSettings = {
		title: LN('sbi.worksheet.designer.linechartdesignerpanel.title')
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.lineChartDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.lineChartDesignerPanel);
	}
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.genericChartDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.genericChartDesignerPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	
	
	this.chartLib = 'highcharts';
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.chartlib) {
		this.chartLib = Sbi.settings.worksheet.chartlib;
	}
	this.chartLib = this.chartLib.toLowerCase();

	
	Ext.apply(this, c);
	

	this.addEvents("attributeDblClick", "attributeRemoved");
	
	this.init();
	
	c = {
		items: [this.form]
	};
	
	
	
	if(Ext.isIE){
		this.on('resize', function(a,b,c,d){try{ this.form.setWidth(b-40);}catch(r){}}, this);
	}
	
	Sbi.worksheet.designer.LineChartDesignerPanel.superclass.constructor.call(this, c);
	
	this.on('afterLayout', this.addToolTips, this);
	
	this.categoryContainerPanel.on(	'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
	this.seriesGroupingPanel.on(	'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
	
};

Ext.extend(Sbi.worksheet.designer.LineChartDesignerPanel,Sbi.worksheet.designer.GenericChartDesignerPanel, {

	form: null
	, items: null
	, typeRadioGroup: null
	, colorAreaCheck: null
	, showValuesCheck: null
	, imageTemplate: null
	, categoryContainerPanel: null
	, seriesGroupingPanel: null
	, seriesContainerPanel: null
	, axisDefinitionPanel: null
	, showLegendCheck: null
	, radioGroupIds: null
	, chartLib: null
	
	, init: function () {
		
		this.initTemplate();
		
		this.radioGroupIds = [Ext.id(), Ext.id(), Ext.id()]; // generate random id
		
		this.typeRadioGroup = new Ext.form.RadioGroup({
			hideLabel: true,
			columns: 3,
			items: [
		        {name: 'type', height: 80, width: 80, id: this.radioGroupIds[0], ctCls:'side-by-side-linechart-line', inputValue: 'side-by-side-linechart', checked: true},
		        {name: 'type', height: 80, width: 80, id: this.radioGroupIds[1], ctCls:'stacked-linechart-line', inputValue: 'stacked-linechart'},
		        {name: 'type', height: 80, width: 80, id: this.radioGroupIds[2], ctCls:'percent-stacked-linechart-line', inputValue: 'percent-stacked-linechart'}
			]
		});
		this.typeRadioGroup.on('change', this.changeLineChartImage, this);
		
		this.colorAreaCheck = new Ext.form.Checkbox({
			checked: false
			, fieldLabel: LN('sbi.worksheet.designer.linechartdesignerpanel.form.colorarea.title')
		});
		this.colorAreaCheck.on('check', this.changeLineChartImage, this);
		
		this.showValuesCheck = new Ext.form.Checkbox({
			checked: false
			, fieldLabel: LN('sbi.worksheet.designer.linechartdesignerpanel.form.showvalues.title')
		});
		
		this.showLegendCheck = new Ext.form.Checkbox({
			checked: false
			, fieldLabel: LN('sbi.worksheet.designer.linechartdesignerpanel.form.showlegend.title')
		});
		
		
		this.categoryContainerPanel = new Sbi.worksheet.designer.ChartCategoryPanel({
            width: 200
            , height: 70
            , initialData: null
            , ddGroup: this.ddGroup
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
		
		this.seriesGroupingPanel = new Sbi.worksheet.designer.SeriesGroupingPanel({
            width: 430
            , height: 70
            , initialData: null
            , ddGroup: this.ddGroup
		});
		// propagate events
		this.seriesGroupingPanel.on(
			'attributeDblClick' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute); 
			}, 
			this
		);
		this.seriesGroupingPanel.on(
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
		});
		
		this.imageContainerPanel = new Ext.Panel({
            width: 200
            , height: 120
            , html: this.imageTemplate.apply(['side-by-side-linechart', 'line'])
		});
		
	    this.axisDefinitionPanel = new Ext.Panel({
	        layout: 'table'
	        , baseCls:'x-plain'
	        , cls: 'centered-panel' //for center the panel
	        , width: this.seriesContainerPanel.width+this.imageContainerPanel.width+20 //for center the panel
	        , padding: '10 10 10 10'
	        , layoutConfig: {columns : 2}
	        // applied to child components
	        //, defaults: {height: 100}
	        , items:[
	            this.seriesContainerPanel
	            , this.imageContainerPanel 
	            , this.seriesGroupingPanel
		        , this.categoryContainerPanel
		        
		        
		    ]
	    });
	    

	    var fieldsetItems = new Array();
	    
		switch (this.chartLib) {
        case 'ext3':
        	break;
        default: 
        	fieldsetItems.push(this.colorAreaCheck);
        	fieldsetItems.push(this.showValuesCheck);
		} 
		fieldsetItems.push(this.showLegendCheck);
	    
		//creates the font options
		this.addFontStyleCombos(fieldsetItems);
		
		this.form = new Ext.form.FormPanel({
			border: false
			, items: [
			    {
			    	layout: 'column'
			    	, padding: '10 10 10 10'
			    	, border: false
			    	, items: [
		  			    {
							xtype: 'fieldset'
//							, title: LN('sbi.worksheet.designer.linechartdesignerpanel.form.fieldsets.type')
							, columnWidth : .7
							, border: false
							, items: [this.typeRadioGroup]
						}
						, {
							xtype: 'fieldset'
//							, title: LN('sbi.worksheet.designer.linechartdesignerpanel.form.fieldsets.options')
							, columnWidth : .3
							, border: false
							, items: fieldsetItems
						}
			    	]
			    }
				, this.axisDefinitionPanel
			]
		});
	}
	
	, addToolTips: function(){
		this.removeListener('afterLayout', this.addToolTips, this);
		
		var sharedConf = {
			anchor : 'top'
			, width : 200
			, trackMouse : true
		};
	
		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-' + this.radioGroupIds[0],
			html: LN('sbi.worksheet.designer.linechartdesignerpanel.form.type.tooltip.side-by-side')
		}, sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-' + this.radioGroupIds[1],
			html: LN('sbi.worksheet.designer.linechartdesignerpanel.form.type.tooltip.stacked')
		}, sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-' + this.radioGroupIds[2],
			html: LN('sbi.worksheet.designer.linechartdesignerpanel.form.type.tooltip.percent-stacked')
		}, sharedConf));
	}
	
	, initTemplate: function () {
        this.imageTemplate = new Ext.Template('<div class="{0}-{1}-preview" style="height: 100%;"></div>');
        this.imageTemplate.compile();
	}
	
	, changeLineChartImage: function() {
		var type = this.typeRadioGroup.getValue().getGroupValue();
		var lineOrArea = this.colorAreaCheck.getValue() ? 'area' : 'line';
		var newHtml = this.imageTemplate.apply([type, lineOrArea]);
		this.imageContainerPanel.update(newHtml);
	}
	
	, getFormState: function() {
		var state = {};
		state.designer = 'Line Chart';
		state.type = this.typeRadioGroup.getValue().getGroupValue();
		state.colorarea = this.colorAreaCheck.getValue();
		state.showvalues = this.showValuesCheck.getValue();
		state.showlegend = this.showLegendCheck.getValue();
		state.category = this.categoryContainerPanel.getCategory();
		state.groupingVariable = this.seriesGroupingPanel.getSeriesGroupingAttribute();
		state.series = this.seriesContainerPanel.getContainedMeasures();
		this.getGenericFormState(state);
		return state;
	}
	
	, setFormState: function(state) {
		this.typeRadioGroup.suspendEvents();  // work-around, workaround, see note below
		this.colorAreaCheck.suspendEvents();  // work-around, workaround, see note below
		if (state.type) this.typeRadioGroup.setValue(state.type);
		if (state.colorarea) this.colorAreaCheck.setValue(state.colorarea);
		if (state.showvalues) this.showValuesCheck.setValue(state.showvalues);
		if (state.showlegend) this.showLegendCheck.setValue(state.showlegend);
		if (state.category) this.categoryContainerPanel.setCategory(state.category);
		if (state.groupingVariable) this.seriesGroupingPanel.setSeriesGroupingAttribute(state.groupingVariable);
		if (state.series) this.seriesContainerPanel.setMeasures(state.series);
		this.typeRadioGroup.resumeEvents();  // work-around, workaround, see note below
		this.colorAreaCheck.suspendEvents(); // work-around, workaround, see note below
		if (this.rendered) {                 // work-around, workaround, see note below
			this.changeLineChartImage.defer(200, this);// work-around, workaround, see note below
		}                                    // work-around, workaround, see note below
		/*
		 * when setting value to this.typeRadioGroup and this.colorAreaCheck, the 'change' event is raised
		 * and the changeLineChartImage is invoked, but it does not work properly (this.typeRadioGroup.getValue() 
		 * is null or the this.imageContainerPanel.update(newHtml) instruction does not work).
		 * Suspending the events and deferring the this.changeLineChartImage solve the issue.
		 */
		this.setGenericFormState(state);
	}
	, validate: function(validFields){
		var valErr='';
		valErr+= ''+this.categoryContainerPanel.validate(validFields);
		valErr+= ''+this.seriesContainerPanel.validate(validFields);
		valErr+= ''+this.seriesGroupingPanel.validate(validFields);
		
		if(valErr != ''){
			valErr = valErr.substring(0, valErr.length - 1)
			return LN("sbi.worksheet.designer.validation.invalidFields")+valErr;
		}
		
		if (this.categoryContainerPanel.category == null){
			return LN("sbi.designerchart.chartValidation.noCategory");
		}
		var store = this.seriesContainerPanel.store;
		var seriesCount = store.getCount();
		if (seriesCount == 0){
			return LN("sbi.designerchart.chartValidation.noSeries");
		}
		
		return; 

	}
	
	, containsAttribute: function (attributeId) {
		var category = this.categoryContainerPanel.getCategory();
		if (category != null && category.id == attributeId) {
			return true;
		}
		var groupingVariable = this.seriesGroupingPanel.getSeriesGroupingAttribute();
		if (groupingVariable != null && groupingVariable.id == attributeId) {
			return true;
		}
		return false;
	}
	
	, checkIfAttributeIsAlreadyPresent: function(aPanel, attribute) {
		var attributeId = attribute.id;
		var alreadyPresent = this.containsAttribute(attributeId);
		if (alreadyPresent) {
			Ext.Msg.show({
				   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
				   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.attributealreadypresent'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
			return false;
		}
		return true;
	}
	

});
