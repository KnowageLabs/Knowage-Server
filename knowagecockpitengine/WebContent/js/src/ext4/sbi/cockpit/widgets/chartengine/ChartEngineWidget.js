/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
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

Ext.ns("Sbi.cockpit.widgets.chartengine");

Sbi.cockpit.widgets.chartengine.ChartEngineWidget = function(config) {

	Sbi.trace("[ChartEngineWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {
			fieldsSelectionEnabled: true
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.chartengine.ChartEngineWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});

	Ext.apply(this, c);
	
	this.aggregations = this.wconf.aggregations;
	
//	c = {
//			layout: 'fit',
//		};
	
	c = Ext.apply(c, {
		layout: 'fit'
	});
	
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
		
//		if (!Sbi.isValorized(iFrameHTML)){
//			this.onStoreLoad();
//		}
		
		if (Sbi.isValorized(iFrameHTML)){
			iFrameHTML.contentWindow.document.open();
			iFrameHTML.contentWindow.document.write(responseText);
			iFrameHTML.contentWindow.document.close();
		}
		
	    /**
	     * https://production.eng.it/jira/browse/KNOWAGE-557
	     * In order to enable "updatable/not-updatable" behavior
	     * I need to unbound and re-bound the store associated with
	     * the chart. In the refresh function, the create chart content
	     * must be invoked before to call superclass refresh
	     * 
	     * @author Giorgio Federici (giofeder, giorgio.federici@eng.it)
	     */
		
		if (!this.areIncomingEventsEnabled()) {
	     	this.unboundStore();
		} else {
			this.boundStore();
		}
		
		this.doLayout();
		
		this.up().body.unmask();
    	
		Sbi.trace("[ChartEngineWidget.createContent]: OUT");
	}
	
	, chartEngineServicePostCall: function(){
		
		var wData = Ext.JSON.encode(this.getWidgetDataAsJson());
		
		/*
			WORKAROUND: Replace the single quote character wherever in the JSON for the Cockpit with the ASCII code for a single quote character, so we can render the chart 
			inside the Cockpit engine even when the JSON template contains this character (e.g. "L'Italia"). Later, because of rendering the chart, this code
			will be replaced with the "escaped" single quote character combination (in order not to have "L&#39;Italia").
			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		*/
		wData = wData.replace(new RegExp("'",'g'),"&#39;");
		
		var thePanel = this;
		
		Ext.Ajax.request({ 
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
		var isWidgetUpdateable = this.wgeneric.incomingeventsenabled;
		var aggregationCategories = (this.wconf.aggregations && this.wconf.aggregations.categories)?
				this.wconf.aggregations.categories : [];
//		var aggregations = this.wconf.aggregations;
		
		
		if(!isWidgetUpdateable && aggregationCategories.length > 0) {
//		if(!isWidgetUpdateable) {
//			var associationsForThisStore = Sbi.storeManager.getAssociationGroupByStore(this.getStore());
//			var associations = associationsForThisStore.associations; //array
//			var datasets = associationsForThisStore.datasets; //array
			
			var toReturn = {};
			toReturn['header'] = aggregationCategories[0].columnName;
			
			return toReturn;
		} else {
			var store = this.getStore();
			var records = store.getRange();
			var column;
			
			outerloop: for(var i = 0; i < records.length; i++) {
				
				var tmpData = records[i].getData();
				
				for (var key in tmpData) {
					if (tmpData[key] == fieldValue) {
						column = key;
						break outerloop;
					}
				}
			}
			
			var fieldMeta = this.getFieldMetaByName(column);
			
			return fieldMeta;
		}
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
		
		var outcomingEventsEnabled = this.wgeneric.outcomingeventsenabled;
		var chartTemplate = this.wconf.chartTemplate;
		
		chartTemplate.CHART.outcomingEventsEnabled = outcomingEventsEnabled;
		
		WIDGET['chartTemplate'] = chartTemplate;
		WIDGET['aggregations'] = this.aggregations;
		
		var chartStore = this.getStore();
		
		if(Sbi.isValorized(chartStore)){
			var metaData = {};
			metaData['metaData'] = chartStore.meta;
			
			var inMemoryData = chartStore.inMemoryData || chartStore.data.items;
			
			metaData['results'] = inMemoryData.length;
			
			var rows = [];
			
			for(var i = 0; i < inMemoryData.length; i++){
				if(Sbi.isValorized(inMemoryData[i].data)){
					rows.push(inMemoryData[i].data)
				}
			}
			
			metaData['rows'] = rows;
			
			WIDGET['jsonData'] = metaData;
		}
		
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
    	
    	Sbi.cockpit.widgets.chartengine.ChartEngineWidget.superclass.refresh.call(this);
    	
		Sbi.trace("[ChartEngineWidget.refresh]: OUT");
	},
	
	successFunction: function(responseText) {
		
		this.createContent(responseText);
		this.up().body.unmask();
		
	    /**
	     * https://production.eng.it/jira/browse/KNOWAGE-557
	     * Force hiding load mask after the chart content is created
	     * in the success function
	     * 
	     * @author Giorgio Federici (giofeder, giorgio.federici@eng.it)
	     */
		this.hideLoadingMask();
    }
	
	, failureFunction: function() {
		Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.cockpit.widgets.chartengine.chartEngine.serverError'), 'Server Error');
		this.up().body.unmask();
		
	    /**
	     * https://production.eng.it/jira/browse/KNOWAGE-557
	     * Force hiding load mask after failed call
	     * 
	     * @author Giorgio Federici (giofeder, giorgio.federici@eng.it)
	     */
		this.hideLoadingMask();
    }
	
});


Sbi.registerWidget(Sbi.constants.cockpit.chart, {
	name: 'Chart'//LN('sbi.cockpit.widgets.image.imageWidgetDesigner.name')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/chartengine/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.chartengine.ChartEngineWidget'
	, designerClass: 'Sbi.cockpit.widgets.chartengine.ChartEngineWidgetDesigner'
});