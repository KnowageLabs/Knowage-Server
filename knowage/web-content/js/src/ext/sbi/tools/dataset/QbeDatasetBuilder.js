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
