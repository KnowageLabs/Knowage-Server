/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * ServiceRegistry - short description
  * 
  * Object documentation ...
  * 
  * by Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.service");

Sbi.service.ServiceRegistry = function(config) {
	
	config = config || {};
	
	this.baseUrl = Ext.apply({}, config.baseUrl || {}, {
		protocol: 'http'     
		, host: 'localhost'
	    , port: '8081'
	    , contextPath: 'SpagoBI'
	    , controllerPath: 'servlet/AdapterHTTP' 
	    , restServicesPath: 'restful-services' 
	});
	
	this.baseParams = Ext.apply({}, config.baseParams || {}, {
		SBI_EXECUTION_ID: -1
	});
	
	this.defaultAbsolute = config.defaultAbsolute !== undefined?  config.defaultAbsolute: false; 
	this.defaultServiceType = config.defaultServiceType !== undefined?  config.defaultServiceType: 'action'; 
		
	//this.addEvents();	
	
	// constructor
    Sbi.service.ServiceRegistry.superclass.constructor.call(this);
};

Ext.extend(Sbi.service.ServiceRegistry, Ext.util.Observable, {
    
    // static contens and methods definitions
	baseUrl: null
	, baseParams: null
	, defaultAbsolute: null
	, defaultServiceType: null 
	
   
    // public methods
    
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
        serviceUrl += '?'
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
    
    , getRestServiceUrlObject : function(s){
    	var serviceUrl;
    	
    	var baseUrlStr;
    	var serviceType;
    	var params;
               
        if(typeof s == 'string') {
        	s = {serviceName: s};
        }
        
        serviceType = s.serviceType || this.defaultServiceType;
        params = Ext.apply({}, s.baseParams || {}, this.baseParams);
                
        serviceUrl = this.getRestBaseUrlStr(s);
        serviceUrl += '/'+ s.serviceName;
      
        return {
        	url: serviceUrl,
        	params: params
        }
        	
    }    
    
    , getRestServiceUrl : function(s){
    	var serviceUrl;
    	
    	var baseUrlStr;
    	var serviceType;
    	var params;
    	var paramsString = "";
               
        if(typeof s == 'string') {
        	s = {serviceName: s};
        }
        
        serviceType = s.serviceType || this.defaultServiceType;
        params = Ext.apply({}, s.baseParams || {}, this.baseParams);
                
        serviceUrl = this.getRestBaseUrlStr(s);
        serviceUrl += '/'+ s.serviceName;
      
        
        	
        for(var p in params){
        	if(params[p] !== null) {
        		paramsString += '&' + p + '=' + params[p];
        	}
        }
        
        if(paramsString.length>0){
        	serviceUrl += '?'+paramsString.substring(1);
        }
        
        return serviceUrl;
    }     
    
    , getBaseUrlStr: function(s) {
    	var baseUrlStr;
    	
    	var isAbsolute = s.isAbsolute || this.defaultAbsolute;
    	var url = Ext.apply({}, s.baseUrl || {}, this.baseUrl);
    	
    	if(isAbsolute) {
    		baseUrlStr = url.protocol + '://' + url.host + ":" + url.port + '/' + url.contextPath + '/' + url.controllerPath;
    	} else {
    		baseUrlStr = '/' + url.contextPath+ '/' + url.controllerPath;
    	}
    	
    	return  baseUrlStr;
    }
    
    , getRestBaseUrlStr: function(s) {
    	var baseUrlStr;
    	
    	var isAbsolute = s.isAbsolute || this.defaultAbsolute;
    	var url = Ext.apply({}, s.baseUrl || {}, this.baseUrl);
    	
    	if(isAbsolute) {
    		baseUrlStr = url.protocol + '://' + url.host + ":" + url.port + '/' + url.contextPath + '/' + url.restServicesPath;
    	} else {
    		baseUrlStr = '/' + url.contextPath+ '/' + url.restServicesPath;
    	}
    	
    	return  baseUrlStr;
    }
    , getContextUrlStr: function(s) {
    	var baseUrlStr;
    	
    	var isAbsolute = s.isAbsolute || this.defaultAbsolute;
    	var url = Ext.apply({}, s.baseUrl || {}, this.baseUrl);
    	
    	if(isAbsolute) {
    		baseUrlStr = url.protocol + '://' + url.host + ":" + url.port + '/' + url.contextPath;
    	} else {
    		baseUrlStr = '/' + url.contextPath;
    	}
    	
    	return  baseUrlStr;
    }    
});