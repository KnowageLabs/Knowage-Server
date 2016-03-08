/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Repository of all the services
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.service.RestService', {
	extend: 'Ext.util.Observable',

	config: {
		serviceVersion: '1.0',
		url: null,
		externalUrl: null,
		subPath: null,
		method: "GET",
		pathParams: null,
		baseParams: {},
		jsonData: null,
		params: {},
		timeout : Ext.Ajax.timeout,
		/**
		 * @cfg {boolean} false
		 * If true the execution of the service can be very long.. So the loading mask is different..
		 */
		longExecution: false,
		/**
		 * @cfg {boolean} async
		 * If true the request is managed asynchronously.. In the sense that no progress bar is displayed
		 * and the call service by default does not call a callback function but throws the executedAsync
		 * event
		 */
		async: false
	},

	constructor : function(config) {
		config = Ext.apply(config || {}, {baseParams : Sbi.config.ajaxBaseParams || {}});  // apply Sbi.config.ajaxBaseParams as base params in the constructor
		this.initConfig(config);
		this.callParent();
		this.addEvents(
				/**
				 * @event executedAsync
				 * This event is thrown when a asynchronous service is executed
				 * @param {booelan} success true if the service returns with a success code, false otherwise
				 * @param {Object} response
				 */
				'executedAsync'
		);
	},

	getRestUrlWithParameters: function(withRequestParams){
		var url = this.url;

		if( this.serviceVersion){
			url = this.serviceVersion+"/"+url;
		}

		if( this.externalUrl){
			url = this.externalUrl + url;
		}

		var params = new Array();
		if(this.subPath!=null && this.subPath!=undefined){
			if(!this.subPath instanceof Array){
				params.push(this.subPath);
			}else{
				params = params.concat(this.subPath);
			}
		}

		if(this.pathParams){
			params = params.concat(this.pathParams);
		}

		if(params && url){
			for(var i=0; i<params.length; i++){
				var p = params[i];
				if(p!=null && p!=undefined){
					url = url+"/"+p;
				}else{
					url = url+"/null";
				}
			}
		}

		//add the request parameters
		if(withRequestParams){
			var first = true;
			var requestParams = this.getRequestParams();
			if(requestParams){
				for(var p in requestParams){
					if(first){
						url = url +'?';
						first = false;
					}else{
						url = url + '&';
					}
					var param = requestParams[p];
					url = url + p +'='+param;
				}
			}
		}

		return url;
	},

	getRequestParams: function(){
		return Ext.apply(this.params, this.baseParams );
	},
	
	callServiceInNewWindow: function(){
				window.open(this.getRestUrlWithParameters(true),'_blank');
	},

	callService:function(scope, successCallBack, failureCallBack, keepState, keepStateIfFails){

		var mySuccessCallBack= successCallBack;
		var myFailureCallBack= failureCallBack;

		if(!this.async){
			//open the loading mask
			if(this.longExecution){
				Sbi.olap.eventManager.fireEvent('executeService', LN('sbi.common.wait.long'));
			}else{
				Sbi.olap.eventManager.fireEvent('executeService');
			}

		}

		var thisPanel = this;

		if(!mySuccessCallBack && scope){

			mySuccessCallBack = function(response, options) {
				var withError = false;
				if(response != undefined && response.statusText != undefined && response.responseText!=null && response.responseText!=undefined) {
					if(response.responseText.length>21 && response.responseText.substring(0,13)=='{"errors":[{"'){
						withError = true;
					}

					if(thisPanel.async){
						thisPanel.fireEvent('executedAsync',!withError,response, keepState);
					}else if(withError){
						Sbi.olap.eventManager.fireEvent('serviceExecutedWithError', response, keepStateIfFails);
						Sbi.exception.ExceptionHandler.handleFailure(response);
					}else{
						Sbi.olap.eventManager.fireEvent('serviceExecuted', response, keepState);
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			};


		};

		if(!myFailureCallBack && scope){
			if(!this.async){
				myFailureCallBack = function (response, options) {
					Sbi.olap.eventManager.fireEvent('serviceExecutedWithError', response, keepStateIfFails);
					Sbi.exception.ExceptionHandler.handleFailure(response, options);
				};
			}else{
				myFailureCallBack = function(response, options) {
					thisPanel.fireEvent('executedAsync',false,response);
				};
			}

		};

		var ajaxConf = {
				url: this.getRestUrlWithParameters(),
				method: this.method,
				success : mySuccessCallBack,
				scope: scope,
				params: this.getRequestParams(),
				failure: myFailureCallBack,
				timeout: this.timeout
		};

		if(this.jsonData){
			ajaxConf.jsonData = this.jsonData;
		}

		Ext.Ajax.request(ajaxConf);
	}


});

