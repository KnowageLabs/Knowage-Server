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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Antonella Giachino (antonella.giachino@eng.it)
  */


Ext.ns("Sbi.engines.chart.data");

Sbi.engines.chart.data.Store = function(config) {
	this.alias2NameMap = {};
	this.dsLabel = config.datasetLabel;
	
	if(!config.url) {
		var serviceConfig;
		if(config.serviceName) {
			serviceConfig = {serviceName: config.serviceName};
			if(config.baseParams) {
				serviceConfig.baseParams = config.baseParams;
				delete config.baseParams;
			}
			delete config.serviceName;
			
			config.url = Sbi.config.serviceRegistry.getServiceUrl( serviceConfig );
		} else if(config.datasetLabel)	{
			var params = {};
			params.MESSAGE_DET ="DATASET_EXEC";
			params.id = config.storeId;
			params.label = config.datasetLabel;
			params.dsTypeCd = config.dsTypeCd || "";
			params.trasfTypeCd = config.dsTransformerType || "";
			params.start =-1; // to get all values
			params.limit =-1; // to get all values

			var pars =  config.dsPars;
			var separator = '';
			var arParams = [];
				if(Ext.isArray(pars)) {		
					for(var i = 0; i < pars.length; i++) {
						var strParams = {};
						var elem = pars[i];
						for(e in elem) {
							//strParams[e] = elem[e];
							strParams[e] = encodeURIComponent(elem[e]);							
							separator = ',';
						}
						arParams.push(strParams);
					}
				}

			params.pars = Ext.util.JSON.encode(arParams) || [];
			
			delete config.id;
			delete config.label;	
			delete config.dsTypeCd;	
			delete config.pars;	
			delete config.trasfTypeCd;
			delete config.numCharts;

			config.url = Sbi.config.serviceRegistry.getServiceUrl({serviceName:  'EXECUTE_DATASETS_ACTION'
																 , baseParams:params
																   });
			//alert("config.url: " + config.url.toSource());
		}	
	}
	
	this.refreshTime = config.refreshTime;	
	delete config.refreshTime;

	Sbi.engines.chart.data.Store.superclass.constructor.call(this, config);
};

Ext.extend(Sbi.engines.chart.data.Store, Ext.ux.data.PagingJsonStore, {	
	
    
	alias2FieldMetaMap: null
	, refreshTime: null
	, dsLabel: null
	
    // -- public methods ----------------------------------------------------------------
	, getFieldMetaByAlias: function(alias) {
		//if (this.ready){
			// assert
		/*
			if(!this.alias2FieldMetaMap) {
				Sbi.exception.ExceptionHandler.showErrorMessage('Impossible to [getFieldMetaByAlias]. Store has not loaded yet.', 'Wrong function call');
			}
			*/
			if (Ext.isArray(alias)){
				alias = alias[0].toUpperCase();
			}else{
				alias = alias.toUpperCase();
			}
			var m = this.alias2FieldMetaMap[alias];
			if(m){
				if(m.length === 0) {
					m = undefined;
				} else if(m.length === 1) {
					m = m[0];
				} else {
					m = m[0];
					alert('Warning: there are [' + m.length + '] fields whose alias is [' + alias + ']. Only the first one will be used');
				}
			}
			return m;
		//}
	}

	, getFieldNameByAlias: function(alias) {
		var fname;
		var fmeta = this.getFieldMetaByAlias(alias);
		if(fmeta) {
			fname = fmeta.name;
		}
		return fname;
	}
	
	, loadStore: function(){
		this.load({
			params: {}, 
			callback: function(){this.ready = true;}, 
			scope: this, 
			add: false
		});
	}
    
	, getDsLabel: function(){
		return this.dsLabel;
	}
	
    // -- private methods ----------------------------------------------------------------
   
    , onMetaChange : function(meta){
		this.alias2FieldMetaMap = {};
		var fields = meta.fields;
		for(var i = 0, l = fields.length, f; i < l; i++) {
			f = fields[i];
			if( typeof f === 'string' ) {
				f = {name: f};
			}
			f.header = f.header || f.name;
			f.header = f.header.toUpperCase();
			if(!this.alias2FieldMetaMap[f.header]) {
				this.alias2FieldMetaMap[f.header] = new Array();
			}
			this.alias2FieldMetaMap[f.header].push(f);
		}

		Sbi.engines.chart.data.Store.superclass.onMetaChange.call(this, meta);
    }

});