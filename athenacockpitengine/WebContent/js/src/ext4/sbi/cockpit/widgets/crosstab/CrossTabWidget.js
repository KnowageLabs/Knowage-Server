/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.CrossTabWidget = function(config) {
	Sbi.trace("[CrossTabWidget.constructor]: IN");

	var defaultSettings = {
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.crosstab.CrossTabWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});

	Ext.apply(this, c);

	Sbi.cockpit.widgets.crosstab.CrossTabWidget.superclass.constructor.call(this, c);

	this.init();


	//create the configuration in a global variable. we must act in this way because we should manage the clicks on the table and from the table
	//we can access only global variables

	if(!Sbi.cockpit.widgets.crosstab.globalConfigs){
		Sbi.cockpit.widgets.crosstab.globalConfigs = new Array();
	}

	this.myGlobalId = Sbi.cockpit.widgets.crosstab.globalConfigs.length;
	Sbi.cockpit.widgets.crosstab.globalConfigs.push(this);


	this.sortOptions = {};
	this.sortOptions.myGlobalId = this.myGlobalId;

	//manage the load events
	this.getStore().on("load", this.loadCrosstab, this);
	this.getStore().on("refreshData", this.updateCrosstabAfterLinkedNavigation, this);
	Sbi.trace("[CrossTabWidget.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.widgets.crosstab.CrossTabWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	crosstabDefinitionvar: null
	, requestParameters: null // contains the parameters to be sent to the server on the crosstab load invocation
	, crosstab: null
	, calculatedFields: null
	, loadMask: null
	, autoScroll: true
	, sortOptions: null
	, myGlobalId: null
	, linked: false//used to understand if the dataset of the crosstab is linked to another object. We need this information in order to reload data the custom service or using the data taken from the store manager

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	, load: function(crosstabDefinition, filters) {
		Sbi.trace("[CrossTabWidget.load]: IN");

		this.crosstabDefinitionvar = this.getCrosstabDefinition();
		var datasetLabelEncoded = this.getStoreId();

		this.requestParameters = {
			crosstabDefinition: this.crosstabDefinition,
			datasetLabel: datasetLabelEncoded
		};
		if(filters!=undefined && filters!=null){
			this.requestParameters.FILTERS = Ext.util.JSON.encode(filters);
		}

		Sbi.storeManager.loadStore(this.getStore());
		this.linked = false;

		Sbi.trace("[CrossTabWidget.load]: OUT");
	}

	, sortCrosstab: function(){
		if(this.linked){
			this.updateCrosstab();
		}else{
			this.loadCrosstabAjaxRequest();
		}
	}

	, loadCrosstab: function(store, records, successful, eOpts){
		if(store.proxy.reader && store.proxy.reader.jsonData && store.proxy.reader.jsonData.metaData){
			store.myStoreMetaData = Ext.apply(store.proxy.reader.jsonData.metaData,{});
		}

		var dataStore = new Array();
		for(var i=0; i<records.length; i++){
			dataStore.push(records[i].data);
		}
		Ext.defer(this.updateCrosstab, 600, this, [dataStore, store.myStoreMetaData]);
		
		this.initFontOptions();
	}

	, updateCrosstabAfterLinkedNavigation: function(storedata, metadata){
		this.linked = true;
		this.updateCrosstab(storedata, metadata);
	}


	, updateCrosstab: function(storedata, metadata){

		this.crosstabDefinition = this.getCrosstabDefinition();

		var params={
				crosstabDefinition: this.crosstabDefinition
		};

		if(storedata!=null){
			this.storedData = {
					metadata: metadata,
					jsonData: storedata,
					sortOptions: this.sortOptions
			};
		}


		Ext.Ajax.request({
			url: Sbi.config.serviceReg.getServiceUrl('updateCrosstab', {
			}),
			method: 'POST',
			params: params,
	        success : function(response, opts) {
	        	this.hideLoadingMask();
	        	this.refreshCrossTab(response.responseText);
	        },
	        scope: this,
	        jsonData: Ext.encode(this.storedData),
			failure: function(response, options) {
				this.hideLoadingMask();
				Sbi.exception.ExceptionHandler.handleFailure(response, options);
			}
		});

	}

	, loadCrosstabAjaxRequest: function(){

		this.showLoadingMask();

		Ext.Ajax.request({
			url: Sbi.config.serviceReg.getServiceUrl('getCrosstab', {
			}),
			method: 'POST',
			params: this.requestParameters,
	        success : function(response, opts) {
	        	this.hideLoadingMask();
	        	this.refreshCrossTab(response.responseText);
	        },
	        scope: this,
	        jsonData: Ext.encode(this.sortOptions),
			failure: function(response, options) {
				this.hideLoadingMask();
				Sbi.exception.ExceptionHandler.handleFailure(response, options);
			}
		});
	}

	, refreshCrossTab: function(serviceResponseText) {

		this.crosstab = Ext.create("Sbi.cockpit.widgets.crosstab.HTMLCrossTab",{
			htmlData : serviceResponseText
			, bodyCssClass : 'crosstab'
			, widgetContainer: this
		});
		this.removeAll();
		this.add(this.crosstab);
		this.hideMask();
		this.doLayout();
	}

	, getCrosstabDefinition: function() {
		var crosstabDef = {};
		crosstabDef.config = this.wconf.config;
		//crosstabDef.config.maxcellnumber = 2000;
		crosstabDef.rows = this.wconf.rows;
		crosstabDef.columns = this.wconf.columns;
		crosstabDef.measures = this.wconf.measures;
		var crosstabDefinitionEncoded = Ext.JSON.encode(crosstabDef);
		return crosstabDefinitionEncoded;
	}

	, hideMask: function() {
    	if (this.loadMask != null) {
    		this.loadMask.hide();
    	}
    	this.fireEvent('contentloaded');
	}

    , showMask : function(){
    	if(!this.hideLoadingMask){
	    	if (this.loadMask == null) {
	    		this.loadMask = new Ext.LoadMask('CrosstabPreviewPanel', {msg: "Loading.."});
	    	}
	    	this.loadMask.show();
    	}
    }

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
    // -----------------------------------------------------------------------------------------------------------------
	, init: function() {
		this.load(null,null);
	}
	
	
	, initFontOptions: function() {
		
		//font options		
	    if(this.wconf === undefined || this.wconf === null){
	    	//do not change the CSS, do nothing
	    }
	    else{
	    	
	    	
	    	//font options
	    	
		    var tdMemberFontStyle = '#' + this.id + ' td.member { font: ';
		    var tdLevelFontStyle = '#' + this.id + ' td.level td.crosstab-header-text { font: ';
		    var tdDataFontStyle = '#' + this.id + ' td.data { font: ';
		    var tdNAFontStyle = '#' + this.id + ' td.na { font: '; 
		  
		    //crosstab headers font weight
		    if(this.wconf.tdLevelFontWeight === undefined || this.wconf.tdLevelFontWeight  === null){
		    	tdLevelFontStyle = tdLevelFontStyle + 'normal ';	
			} else {
				tdLevelFontStyle = tdLevelFontStyle + this.wconf.tdLevelFontWeight + ' ';
			}
		    
		    //measures headers font weight
		    if(this.wconf.tdMemberFontWeight === undefined || this.wconf.tdMemberFontWeight  === null){
		    	tdMemberFontStyle = tdMemberFontStyle + 'normal ';	
			} else {
				tdMemberFontStyle = tdMemberFontStyle + this.wconf.tdMemberFontWeight + ' ';
			}
		    
		    //measures font weight
		    if(this.wconf.tdDataFontWeight === undefined || this.wconf.tdDataFontWeight  === null){
		    	tdDataFontStyle = tdDataFontStyle + 'normal ';
		    	tdNAFontStyle = tdNAFontStyle + 'normal ';
			} else {
				tdDataFontStyle = tdDataFontStyle + this.wconf.tdDataFontWeight + ' ';
				tdNAFontStyle = tdNAFontStyle + this.wconf.tdDataFontWeight + ' ';
			}
	    
		 	//crosstab headers font size
		    if(this.wconf.tdLevelFontSize === undefined || this.wconf.tdLevelFontSize === null){
	    		
				if (this.wconf.fontSize == undefined || this.wconf.fontSize == null){
					tdLevelFontStyle = tdLevelFontStyle + '11px ';
				} else {
					tdLevelFontStyle = tdLevelFontStyle + this.wconf.fontSize + 'px ';
				}			
			} else {
				tdLevelFontStyle = tdLevelFontStyle + this.wconf.tdLevelFontSize + 'px ';
			}
	    	
	    	//measures headers font size
	    	if(this.wconf.tdMemberFontSize === undefined || this.wconf.tdMemberFontSize === null){
	    		
				if (this.wconf.fontSize == undefined || this.wconf.fontSize == null){
					tdMemberFontStyle = tdMemberFontStyle + '11px ';
				} else {
					tdMemberFontStyle = tdMemberFontStyle + this.wconf.fontSize + 'px ';
				}			
			} else {
				tdMemberFontStyle = tdMemberFontStyle + this.wconf.tdMemberFontSize + 'px ';
			}
	    	
	    	//measures font size
	    	if(this.wconf.tdDataFontSize === undefined || this.wconf.tdDataFontSize === null){
	    		
				if (this.wconf.fontSize == undefined || this.wconf.fontSize == null){
					tdDataFontStyle = tdDataFontStyle + '11px ';
					tdNAFontStyle = tdNAFontStyle + '11px ';
				} else {
					tdDataFontStyle = tdDataFontStyle + this.wconf.fontSize + 'px ';
					tdNAFontStyle = tdNAFontStyle + this.wconf.fontSize + 'px ';
				}			
			} else {
				tdDataFontStyle = tdDataFontStyle + this.wconf.tdDataFontSize + 'px ';
				tdNAFontStyle = tdNAFontStyle + this.wconf.tdDataFontSize + 'px ';
			}
	    	
		    
		    //font family
	    	if (this.wconf.fontType === undefined || this.wconf.fontType === null){
				tdLevelFontStyle = tdLevelFontStyle + 'tahoma,arial,verdana,sans-serif; ';
				tdMemberFontStyle = tdMemberFontStyle + 'tahoma,arial,verdana,sans-serif; ';
				tdDataFontStyle = tdDataFontStyle + 'tahoma,arial,verdana,sans-serif; ';
				tdNAFontStyle = tdNAFontStyle + 'tahoma,arial,verdana,sans-serif; ';
				
			} else {
				tdLevelFontStyle = tdLevelFontStyle + '' + this.wconf.fontType + '; ';
				tdMemberFontStyle = tdMemberFontStyle  + '' + this.wconf.fontType + '; ';
				tdDataFontStyle = tdDataFontStyle  + '' + this.wconf.fontType + '; ';
				tdNAFontStyle = tdNAFontStyle  + '' + this.wconf.fontType + '; ';
			}
	    	
	    	//font decoration
	    	//crosstab headers font decoration
		    if(this.wconf.tdLevelFontDecoration === undefined || this.wconf.tdLevelFontDecoration  === null){
				tdLevelFontStyle = tdLevelFontStyle + 'text-decoration: none; ';	
			} else {
				tdLevelFontStyle = tdLevelFontStyle + 'text-decoration: ' + this.wconf.tdLevelFontDecoration + '; ';
			}
		    
			//measures headers font decoration
		    if(this.wconf.tdMemberFontDecoration === undefined || this.wconf.tdMemberFontDecoration  === null){
				tdMemberFontStyle = tdMemberFontStyle + 'text-decoration: none; ';	
			} else {
				tdMemberFontStyle = tdMemberFontStyle + 'text-decoration: ' + this.wconf.tdMemberFontDecoration + '; ';
			}
		    
		    //measures font decoration
		    if(this.wconf.tdDataFontDecoration === undefined || this.wconf.tdDataFontDecoration  === null){
				tdDataFontStyle = tdDataFontStyle + 'text-decoration: none; ';
				tdNAFontStyle = tdNAFontStyle + 'text-decoration: none; ';
			} else {
				tdDataFontStyle = tdDataFontStyle + 'text-decoration: ' + this.wconf.tdDataFontDecoration + '; ';
				tdNAFontStyle = tdNAFontStyle + 'text-decoration: ' + this.wconf.tdDataFontDecoration + '; ';
			}
		    
		    
		    //font color
		    //crosstab headers font color
		    if(this.wconf.tdLevelFontColor === undefined || this.wconf.tdLevelFontColor  === null || this.wconf.tdLevelFontColor === ''){
		    	tdLevelFontStyle = tdLevelFontStyle + '} ';	
			} else {
				tdLevelFontStyle = tdLevelFontStyle + 'color: ' + this.wconf.tdLevelFontColor + '; } ';
			}
		    
			//measures headers font color
		    if(this.wconf.tdMemberFontColor === undefined || this.wconf.tdMemberFontColor  === null || this.wconf.tdMemberFontColor === ''){
		    	tdMemberFontStyle = tdMemberFontStyle + '} ';	
			} else {
				tdMemberFontStyle = tdMemberFontStyle + 'color: ' + this.wconf.tdMemberFontColor + '; } ';
			}
		    
		    //measures font color
		    if(this.wconf.tdDataFontColor === undefined || this.wconf.tdDataFontColor  === null || this.wconf.tdDataFontColor === ''){
		    	tdDataFontStyle = tdDataFontStyle + '} ';
		    	tdNAFontStyle = tdNAFontStyle + '} ';
			} else {
				tdDataFontStyle = tdDataFontStyle + 'color: ' + this.wconf.tdDataFontColor + '; } ';
				tdNAFontStyle = tdNAFontStyle + 'color: ' + this.wconf.tdDataFontColor + '; } ';
			}
		    

		    if(Ext.util.CSS.getRule(tdLevelFontStyle) !== undefined || Ext.util.CSS.getRule(tdLevelFontStyle) !== null){
		    	Ext.util.CSS.removeStyleSheet(this.id + '_chstyle');
		    	Ext.util.CSS.createStyleSheet(tdLevelFontStyle, this.id + '_chstyle');
		    }
		    else {
		    	Ext.util.CSS.createStyleSheet(tdLevelFontStyle, this.id + '_chstyle');
		    }
		    
		    if(Ext.util.CSS.getRule(tdMemberFontStyle) !== undefined || Ext.util.CSS.getRule(tdMemberFontStyle) !== null){
		    	Ext.util.CSS.removeStyleSheet(this.id + '_mhstyle');
		    	Ext.util.CSS.createStyleSheet(tdMemberFontStyle, this.id + '_mhstyle');
		    }
		    else {
		    	Ext.util.CSS.createStyleSheet(tdMemberFontStyle, this.id + '_mhstyle');
		    }
		    
		    if(Ext.util.CSS.getRule(tdDataFontStyle) !== undefined || Ext.util.CSS.getRule(tdDataFontStyle) !== null){
		    	Ext.util.CSS.removeStyleSheet(this.id + '_mdstyle');
		    	Ext.util.CSS.createStyleSheet(tdDataFontStyle, this.id + '_mdstyle');
		    }
		    else {
		    	Ext.util.CSS.createStyleSheet(tdDataFontStyle, this.id + '_mdstyle');
		    }
		    
		    if(Ext.util.CSS.getRule(tdNAFontStyle) !== undefined || Ext.util.CSS.getRule(tdNAFontStyle) !== null){
		    	Ext.util.CSS.removeStyleSheet(this.id + '_mnastyle');
		    	Ext.util.CSS.createStyleSheet(tdNAFontStyle, this.id + '_mnastyle');
		    }
		    else {
		    	Ext.util.CSS.createStyleSheet(tdNAFontStyle, this.id + '_mnastyle');
		    }
		    
	    }	
	}
	
});


Sbi.registerWidget('crosstab', {
	name: 'Static Pivot Table'
	, icon: 'js/src/ext4/sbi/cockpit/widgets/crosstab/table_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.crosstab.CrossTabWidget'
	, designerClass: 'Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner'
});