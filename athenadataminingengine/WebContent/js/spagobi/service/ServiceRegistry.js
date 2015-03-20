/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2008 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 
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
	    , port: '8080'
	    , contextPath: 'SpagoBI'
	    , controllerPath: 'servlet/AdapterHTTP'    
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