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
	
	c = {
			layout: 'fit',
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
	
	iFrameContent: null,
	
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

	, createContent: function(responseText) {
		
    	Sbi.trace("[ChartEngineWidget.createContent]: IN");
    	    	
    	this.iFrameContent = Ext.create('Ext.panel.Panel',{
		    autoScroll: true,		    
		    html: '<iframe src="" width="100%" height="100%" frameborder="no"></iframe>',
		});
    	
		if(this.items){
			this.items.each( function(item) {
				this.items.remove(item);
				item.destroy();
			}, this);
		}
		
		this.add(this.iFrameContent);
		
		var el = this.getEl();
		var iFrameHTML = Ext.DomQuery.selectNode("iframe", el.dom);
		iFrameHTML.contentWindow.document.open();
		iFrameHTML.contentWindow.document.write(responseText);
		iFrameHTML.contentWindow.document.close();
		
		this.doLayout();
		
		this.up().body.unmask();
    	
		Sbi.trace("[ChartEngineWidget.createContent]: OUT");
	}
	
	, chartEngineServicePostCall: function(){
		
		var wData = Ext.JSON.encode(this.getWidgetDataAsJson());
		
		var thePanel = this;
		
		Ext.Ajax.request 
	    ({ 
	        url: Sbi.config.chartRuntimeUrl, 
	        method: 'POST',
	        params: {
	        	widgetData: wData
	        },
	        scope: thePanel,
	        success: function(response){
	        	this.successFunction(response.responseText);
	        },
			failure: function(response){
					this.failureFunction();				
			}
	    });
		
		this.up().body.mask('Loading...');
	}
	
	, getFieldMetaByValue: function(fieldValue) {
		var store = this.getStore();
		var records = store.getRange();
		var column;
		
		outerloop: for(var i = 0; i < records.length; i++) {
				
			var tmpData = records[i].getData();
			
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
		
		this.chartEngineServicePostCall();
		Sbi.cockpit.widgets.chartengine.ChartEngineWidget.superclass.onStoreLoad.call(this, this.getStore());
		
     	Sbi.trace("[ChartEngineWidget.onStoreLoad]: OUT");
	}
	
	, refresh:  function() {
    	Sbi.trace("[ChartEngineWidget.refresh]: IN");
		
    	this.chartEngineServicePostCall();

		Sbi.trace("[ChartEngineWidget.refresh]: OUT");
	}
	
	, successFunction: function(responseText) {
		
		this.createContent(responseText);		
		
    }
	
	, failureFunction: function() {
		Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.cockpit.widgets.chartengine.chartEngine.serverError'), 'Server Error');
		this.up().body.unmask();
    }

});


Sbi.registerWidget(Sbi.constants.cockpit.chart, {
	name: 'Chart'//LN('sbi.cockpit.widgets.image.imageWidgetDesigner.name')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/chartengine/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.chartengine.ChartEngineWidget'
	, designerClass: 'Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner'
});