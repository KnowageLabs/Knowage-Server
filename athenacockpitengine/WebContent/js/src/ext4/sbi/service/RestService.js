/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * A service proxy object
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it), Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.define('Sbi.service.RestService', {
	extend: 'Ext.util.Observable'

	, config: {
		name: 'restService'
		, description: 'It\'s just another rest service'
		, method: "GET"

		, baseUrlConf: {
			protocol: 'http'
			, host: 'localhost'
			, port: '8080'
			, contextPath: 'SpagoBI'
		}
		, controllerConf: {
			controllerPath: 'api'
			, serviceVersion: null // '1.0'
			, serviceVersionParamType: 'none' // path || query || header || none
			, serviceVersionParamName: 'version' // used only if serviceVersionParamType is equal to query or header
		}

		// specific to service type
		, resourcePath: null
		//, serviceName: 'DO_SOMETHING_ACTION'
		// ------------------------

		, basePathParams: {}
		, baseQueryParams:{}
		, baseFormParams:{}

		, absolute: false
	}

	, constructor : function(config) {
		this.initConfig(config);
		this.callParent();

		this.addEvents(
	        /**
	         * @event beforerequest
	         * This event is thrown when a service execution has been requested
	         * @param {RestService} this The service proxy for the request
	         * @param {Object} params The params object passed to the {@link #request} function
	         */
		        'beforerequest',
	        /**
	         * @event request
	         * This event is thrown when a service has finished execution
			 * @param {Object} response The service response
	         */
		        'request',
		    /**
		      * @event exception
		      * This event is thrown when an exception occured during service execution.
		      * @param {RestService} this The service proxy for the request
		      * @param {Object} response
		      */
			    'exception'
		);
	}

	, doRequest: function(options){

		params = params || {};
		options = options || {};


		this.fireEvent('beforerequest', this);

		var ajaxConf = {
			url: this.getServiceUrl(options),
			method: this.method,
			success : this.onRequestSuccess,
			callerOnSuccess: options.success,
			failure: this.onRequestFailure,
			callerOnFailure: options.failure,
			scope: this,
			callerScope: options.scope
		};

		if(this.jsonData){
			ajaxConf.jsonData = this.jsonData;
		}

		Ext.Ajax.request(ajaxConf);
	}

	, onRequestSuccess: function(response, options) {
		this.fireEvent('request', this, response, options);
		options.callerOnSuccess.call(options.callerScope, response, options);

	}

	, onRequestFailure: function() {
		this.fireEvent('exception', this);

	}

	, getServiceUrl : function(options){
    	var serviceUrl;

    	Sbi.trace("[RestService.getServiceUrl]: IN");

    	options = options || {};



        serviceUrl = this.getControllerUrl(options);

        var resourcePath = Sbi.isValorized(options.resourcePath)? options.resourcePath: this.resourcePath;
        serviceUrl += '/' + resourcePath;

        //add pathParams
        var pathParams = Ext.apply({}, options.pathParams || {}, this.basePathParams);
    	Sbi.trace("[RestService.getServiceUrl]: path params are equal to [" + Sbi.toSource(pathParams) + "]");
        var pathParamsCount = 0;
        for(var p in pathParams) pathParamsCount++;

        if(pathParamsCount > 0) {
        	var t = new Ext.Template([serviceUrl]);
        	t.compile();
        	serviceUrl = t.apply(pathParams);
        }


        //add queryParams
        var queryParams = Ext.apply({}, options.queryParams || {}, this.baseQueryParams);
    	Sbi.trace("[RestService.getServiceUrl]: query params are equal to [" + Sbi.toSource(queryParams) + "]");
        var queryParamsCount = 0;
        for(var p in queryParams) queryParamsCount++;

        if(queryParamsCount > 0) {
        	serviceUrl += '?';
            for(var p in queryParams){
            	if(queryParams[p] !== null) {
            		serviceUrl += '&' + p + '=' + queryParams[p];
            	}
            }
        }

        // append version
        var controllerConf = Ext.apply({}, options.controllerConf || {}, this.controllerConf);
    	if(Sbi.isValorized(controllerConf.serviceVersion) && controllerConf.serviceVersionParamType === 'query') {
    		 if(queryParamsCount > 0) {
    			 serviceUrl += '&' + controllerConf.serviceVersionParamName + '=' +controllerConf.serviceVersion;
    		 } else {
    			 serviceUrl += '?' + controllerConf.serviceVersionParamName + '=' +controllerConf.serviceVersion;
    		 }
    	}

    	Sbi.trace("[RestService.getServiceUrl]: OUT");

        return serviceUrl;
    }


	/**
	 * @method
	 *
	 * Create the controller url. If #controllerConf.serviceVersion is defined and
	 * #controllerConf.serviceVersionParamType is equal to <code>path</code> also #controllerConf.serviceVersion
	 * is appended to the url.
	 *
	 *  @param {Object} options (optional) the options used to build the base url
	 *
	 *  @return {String} The controller url
	 */
	, getControllerUrl: function(options) {
		var controllerUrl;

		// Sbi.trace("[RestService.getControllerUrl]: OUT");

		options = options || {};
		controllerUrl = this.getBaseUrl(options);
		var controllerConf = Ext.apply({}, options.controllerConf || {}, this.controllerConf);

       	if(controllerConf.controllerPath) {
       		controllerUrl += '/' + controllerConf.controllerPath;
       	}

    	if(Sbi.isValorized(controllerConf.serviceVersion) && controllerConf.serviceVersionParamType === 'path') {
    		controllerUrl += '/' + controllerConf.serviceVersion;
    	}

    	// Sbi.trace("[RestService.getControllerUrl]: OUT");

		return controllerUrl;
	}

	/**
	 * @method
	 *
	 * Create the base service url. The url include the #baseUrlConf.contextPath if defined. The url is absolute if
	 * #option.absolute is true or if it is not defined and #absolute is true, relative otherwise.
	 *
	 * @param {Object} options (optional) the options used to build the base url
	 * @param {Boolean} options.absolute (optional). Used to override the default #absolute property
	 * @param {Boolean} options.baseUrlConf (optional) base url configuration. Used to override the default #baseUrlConf property
	 *
	 * @return {String} The base url
	 */
	, getBaseUrl: function(options) {
		var baseUrl;

		// Sbi.trace("[RestService.getBaseUrl]: IN");

		options = options || {};

		var absolute = Sbi.isValorized(options.absolute)? options.absolute: this.absolute === true;
       	var url = Ext.apply({}, options.baseUrlConf || {}, this.baseUrlConf);

       	if(absolute) {
       		baseUrl = url.protocol + '://' + url.host + ":" + url.port + '/' + url.contextPath;
       	} else {
       		baseUrl = '/' + url.contextPath;
       	}

       	// Sbi.trace("[RestService.getBaseUrl]: OUT");

	    return  baseUrl;
	}

});

