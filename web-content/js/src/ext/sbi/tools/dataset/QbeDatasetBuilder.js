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
Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.QbeDatasetBuilder = function(config) {

	var defaultSettings = {
		title: LN("sbi.tools.dataset.qbedatasetbuilder.title")
		, width: 800
		, height: 500
	};
	 
	if(Sbi.settings && Sbi.settings.tools && Sbi.settings.tools.dataset && Sbi.settings.tools.dataset.qbeDatasetBuilder) {
	   defaultSettings = Ext.apply(defaultSettings, Sbi.settings.tools.dataset.qbeDatasetBuilder);
	}
	 
	var c = Ext.apply(defaultSettings, config || {});
	 
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
		layout:'fit',
		closeAction: 'hide',
		plain: true,
		title: this.title,
		items: [this.iframe],
		listeners: this.listeners,
		scope: this.scope
	});
	
	Sbi.tools.dataset.QbeDatasetBuilder.superclass.constructor.call(this, c);
	
	this.addEvents('gotqbequery');

};

Ext.extend(Sbi.tools.dataset.QbeDatasetBuilder, Ext.Window, {
	
	title : null
	, iframe : null
	, qbeBaseUrl : Sbi.config.qbeDatasetBuildUrl // base URL for
													// SpagoBIQbeEngine web
													// application: its default
													// value is
													// Sbi.config.qbeDatasetBuildUrl
	, jsonQuery : null // query definition: it must be set in the constructor's
						// input object
	, qbeParameters : null // query parameters: it must be set in the
							// constructor's input object
	, datasourceLabel : null
	, datamart : null
	, datasetId : null
	
	, init: function () {
		
		this.iframe = new Ext.ux.ManagedIFramePanel({
			defaultSrc: this.getQbeViewUrl()
	        , loadMask: {msg: 'Loading...'}
	        , fitToParent: true
	        , frameConfig: {
            	disableMessaging: false
            }
	        , disableMessaging: false
	        , listeners: {
	        	'message': {
	        		fn: function(srcFrame, message) {
	        			var messageName = message.data.messageName;
	        			if (messageName == 'gotqbequery') {
	        				this.fireEvent('gotqbequery', this, message);
	        			} else if (messageName == 'catalogueready') {
	        				this.setQbeQuery();
	        			} else { 
	        				//alert('qbedatasetbuilder: Unknown message');
	        			}
	        		}
	        		, scope: this
	        	}
				, 'domready': {
					fn: function(frame) {
						if (!Ext.isChrome && frame.domWritable()) {
							frame.execScript('init()');
						}
					}
					, scope: this
				}
				, 'documentloaded': {  // workaround for Chrome (domready event isn't enough, may because it is fired too early)
									   // see https://spagobi.eng.it/jira/browse/SPAGOBI-1105 
					fn: function(frame) {
						if (Ext.isChrome && frame.domWritable()) {
							frame.execScript('init()');
						}
					}
					, scope: this
				}
	        }
		});
		
	}

	, getQbeQuery: function (handler, scope) {
		var message = {};
		message.messageName = 'getQbeQuery';
		this.iframe.sendMessage(message); // ask for the query
	}
	
	, setQbeQuery: function () {
		var message = {};
		message.messageName = 'setQbeQuery';
		message.jsonQuery = this.jsonQuery;
		message.qbeParameters = this.qbeParameters;
		this.iframe.sendMessage(message); // set the query
	}
	
	, getDatasourceLabel: function() {
		return this.datasourceLabel;
	}
	
	, getDatamart: function() {
		return this.datamart;
	}
	
	, getDatasetId: function() {
		return this.datasetId;
	}
	
	, setDatasetId: function(datasetId) {
		this.datasetId = datasetId;
	}
	
	, getQbeViewUrl: function() {
		var url = Sbi.config.qbeDatasetBuildUrl
			+ '&DATASOURCE_LABEL='
			+ this.datasourceLabel
			+ '&DATAMART_NAME=' 
			+ this.datamart;
		return url;
	}

	/*
	, mustRefreshQbeView: function() {
		var toReturn =
				this.currentDatasourceLabel != this.nextDatasourceLabel
				|| this.currentDatamart != this.nextDatamart
				|| this.currentDatasetId != this.nextDatasetId;
		return toReturn;
	}
	
	, refreshQbeView: function() {
		this.currentDatasourceLabel = this.nextDatasourceLabel;
		this.currentDatamart = this.nextDatamart;
		this.currentDatasetId = this.nextDatasetId;
		var newUrl = this.getQbeViewUrl();
		this.iframe.getFrame().setSrc( newUrl );
	}
	*/
	
});
