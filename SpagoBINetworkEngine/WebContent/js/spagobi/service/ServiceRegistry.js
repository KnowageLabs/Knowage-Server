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
  * Public Functions
  * 
  *  [list]
  * 
  * 
  * Authors
  * 
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.define('Sbi.service.ServiceRegistry', {
    extend: 'Ext.util.Observable'
    , baseUrl: null
	, baseParams: null
	, defaultAbsolute: null
	, defaultServiceType: null 
	
    , constructor: function(config) {
    	config = Ext.apply(config || {});

    	this.callParent(arguments);
    	
    	this.baseUrl = Ext.apply({}, config.baseUrl || {}, {
    		protocol: 'http'     
    		, host: 'localhost'
    	    , port: '8080'
    	    , contextPath: 'SpagoBIChartEngine'
    	    , controllerPath: 'servlet/AdapterHTTP'    
    	});
    	
    	this.baseParams = Ext.apply({}, config.baseParams || {}, {
    		SBI_EXECUTION_ID: -1
    	});
    	
    	this.defaultAbsolute = config.defaultAbsolute !== undefined?  config.defaultAbsolute: false; 
    	this.defaultServiceType = config.defaultServiceType !== undefined?  config.defaultServiceType: 'action'; 
    	//this.callParent(arguments);
    	
    }

	, setBaseUrl : function(url) {
	    Ext.apply(this.baseUrl, url); 
	 }
	     
	 , getServiceUrl : function(s){
	 	var serviceUrl;
	 	
	 	var baseUrlStr;
	 	var serviceType;
	 	var params;
	            
	     if(typeof s == 'string') {
	     	s = {serviceName: s};
	     }
	     
	     serviceType = s.serviceType || this.defaultServiceType;
	     params = Ext.apply({}, s.baseParams || {}, this.baseParams);
	             
	     serviceUrl = this.getBaseUrlStr(s);
	     serviceUrl += '?';
	     serviceUrl += (serviceType === 'action')? 'ACTION_NAME': 'PAGE';
	     serviceUrl += '=';
	     serviceUrl += s.serviceName;
	   
	     for(var p in params){
	     	if(params[p] !== null) {
	     		serviceUrl += '&' + p + '=' + params[p];
	     	}
	     }
	     
	     return serviceUrl;
	 }     
	 
	 , getBaseUrlStr: function(s) {
	 	var baseUrlStr;
	
	 	if (this.baseUrl.completeUrl !== undefined) {
	 		baseUrlStr = this.baseUrl.completeUrl;
	 	} else {
	     	var isAbsolute = s.isAbsolute || this.defaultAbsolute;
	     	var url = Ext.apply({}, s.baseUrl || {}, this.baseUrl);
	     	
	     	if(isAbsolute) {
	     		baseUrlStr = url.protocol + '://' + url.host + ":" + url.port + '/' + url.contextPath + '/' + url.controllerPath;
	     	} else {
	     		baseUrlStr = '/' + url.contextPath+ '/' + url.controllerPath;
	     	}
	 	}
	 	return  baseUrlStr;
	 }

});