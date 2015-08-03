/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.chartengine");

Sbi.cockpit.widgets.chartengine.ChartEngineWidget = function(config) {

	Sbi.trace("[ChartEngineWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {

	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.chartengine.ChartEngineWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});

	Ext.apply(this, c);
	
	this.aggregations = this.wconf.aggregations;

	this.iFrameId = Ext.id();
	
	this.createContent();
	
	c = {
			layout: 'fit',
			height: 500,
			items: [this.iFrameContent]
		};
	
	// constructor
	Sbi.cockpit.widgets.chartengine.ChartEngineWidget.superclass.constructor.call(this, c);
	
	var bounded = this.boundStore();
	if(bounded) {
		Sbi.trace("[ChartEngineWidget.constructor]: store [" + this.getStoreId() + "] succesfully bounded to widget [" + this.getWidgetName() + "]");
	} else {
		Sbi.error("[ChartEngineWidget.constructor]: store [" + this.getStoreId() + "] not bounded to widget [" + this.getWidgetName() + "]");
	}
	
	this.reload();
	
	Sbi.trace("[ChartEngineWidget.constructor]: OUT");
};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.chartengine.ChartEngineWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	widgetContent: null,
	
	iFrameContent: null,
	
	iFrameId: null,
	
	selections: null,
	
	associations: null,

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	setChartEngineSelection: function(selections, flag){
		
		if(Sbi.isValorized(selections) && flag){
			this.selections = Ext.JSON.encode( selections );
		} else {
			this.selections = "";
		}
		
	}
	
	, setChartEngineAssociations: function(associations, flag){
		
		if(Sbi.isValorized(associations) && flag){
			this.associations = Ext.JSON.encode( associations );
		} else {
			this.associations = "";
		}
		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	, createContent: function() {
		
    	Sbi.trace("[ChartEngineWidget.createContent]: IN");
    	    	
    	var chartRuntimeUrl = Sbi.config.chartRuntimeUrl;
    	
    	var widgetData = this.getWidgetDataAsJson();
    	
    	var charTemplate = 	
    	
    	this.widgetContent = Ext.create('Ext.form.FormPanel',{
    		standardSubmit: true,
    		url: chartRuntimeUrl,
    		border: false,
    		hideBorders: true,
    		items: 
    		[
    	    {
    	        xtype: 'hiddenfield',
    	        name: 'widgetData',
    	        value: Ext.JSON.encode(widgetData)
    	    }
    		]
    	});
    	
		this.iFrameContent = Ext.create('Ext.panel.Panel',{
		    autoScroll: true,		    
		    html: '<iframe name="' + this.iFrameId + '"  src="" width="100%" height="100%"></iframe>',
		    listeners: {
                render: function () {
                    this.up('window').body.mask('Loading...');
                },
                afterrender: function () {
                	
                	var thePanel = this;
                	
                	var el = this.getEl();
    				var iFrameHTML = Ext.DomQuery.selectNode("iframe", el.dom);
    				
    				iFrameHTML.onload = function () {
    					thePanel.up('window').body.unmask();
                    };
                }
            }
		});
		
		Sbi.trace("[ChartEngineWidget.createContent]: OUT");
	}
	
	, getFieldMetaByValue: function(fieldValue) {
		var store = this.getStore();
		var records = store.getRange();
		var column;
		
		outerloop: for(var r in records) {
			var tmpData = records[r].getData();
			
			for (var key in tmpData) {
				  if (tmpData[key] === fieldValue) {
					  column = key;
					  break outerloop;
				  }
			}
			
		}
		
		var fieldMeta = this.getFieldMetaByName(column);
		
    	return fieldMeta;
	}
	
	, getFieldMetaByName: function(fieldName) {
		var store = this.getStore();
		var fieldsMeta = store.fieldsMeta;
    	for(var h in fieldsMeta) {
    		var fieldMeta = fieldsMeta[h];
    		if(fieldMeta.name == fieldName) {
    			return fieldMeta;
    		}
    	}
    	return null;
	}
	
	, getWidgetDataAsJson: function(){
		
		var result = {};
		
		var WIDGET = {};
		
		WIDGET['widgetId'] = this.id;
		WIDGET['iFrameId'] = this.iFrameId;
		WIDGET['datasetLabel'] = this.storeId;
		WIDGET['selections'] = this.selections;
		WIDGET['associations'] = this.associations;
		WIDGET['chartTemplate'] = this.wconf.chartTemplate;
		WIDGET['aggregations'] = this.aggregations;
		
		result['widgetData'] = WIDGET;
		
		return result;
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================
	
	, onStoreLoad: function() {
		Sbi.trace("[ChartEngineWidget.onStoreLoad]: IN");
		Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime.superclass.onStoreLoad.call(this, this.getStore());
		
    	this.createContent();
		
		var form = this.widgetContent.getForm();				
		form.submit({target: this.iFrameId});
		
     	Sbi.trace("[ChartEngineWidget.onStoreLoad]: OUT");
	}
	
	, refresh:  function() {
    	Sbi.trace("[ChartEngineWidget.refresh]: IN");
		
    	this.createContent();
		
		var form = this.widgetContent.getForm();				
		form.submit({target: this.iFrameId});

		this.doLayout();
		this.up('window').body.mask('Loading...');

		Sbi.trace("[ChartEngineWidget.refresh]: OUT");
	}

});




Sbi.registerWidget(Sbi.constants.cockpit.chart, {
	name: 'Chart'//LN('sbi.cockpit.widgets.image.imageWidgetDesigner.name')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/chartengine/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.chartengine.ChartEngineWidget'
	, designerClass: 'Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner'
});