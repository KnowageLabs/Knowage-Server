/**
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
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

var chartExecutionWebServiceManagerFactory = angular.module('chartexecution.webservicemanagerfactory',[]);

chartExecutionWebServiceManagerFactory.service(	'chartExecutionWebServiceManagerFactory',
	
	function(sbiModule_messaging,sbiModule_translate,sbiModule_restServices) {
				
		var config = {	
				
			serviceConfig: {				
		        protocol: 'http:',
		        hostName: 'localhost',
		        tcpPort: '8080',          
		        context: '/knowagecssshartengine', 	        
		        wsPrefix: '/api/1.0',
		        sbiExecutionId: '',
		        userId: '',
		        service: '',
		        method: 'POST',
		        timeout: 60000,
		        disableCaching: false,
		        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
		        parameters: {}		
			},
	        
	        webService: new Array()
			
	    };
		
		var setConfig = function(configIn) {
//			console.log(configIn);
			for (var property in config.serviceConfig) {
			    if (configIn.hasOwnProperty(property)) {
			    	config.serviceConfig[property] = configIn[property];
			    }
			}
//			console.log(config);
		}
		
		var getConfig = function() {
			return config.serviceConfig;
		}
		
		var getUrl = function() {
			return getConfig().protocol + '//' + getConfig().hostName + ':' + getConfig().tcpPort + getConfig().context + 
			getConfig().wsPrefix + getConfig().service;
		}
		
		var toString = function() {
			return JSON.stringify(config);
		}
		
		var registerService = function(webServiceName,webServiceConfig) {
			config.webService.push({"webServiceName":webServiceName,"webServiceConfig":webServiceConfig});
		}
		
		var run = function(serviceName, parameters, urlParams, successFunction, failureFunction) {
	    					
			var ws = null;
	    	
	    	for (i=0; i<config.webService.length; i++) {
//	    		console.log(config.webService[i]);
//	    		console.log(config.webService[i].webServiceName);
//	    		console.log(serviceName);
	    		if (config.webService[i].webServiceName==serviceName) {
	    			ws = config.webService[i];
	    			break;
	    		}
	    		
	    	}
	    	
//	    	console.log(ws);
//	    	console.log(config);
	    	
	    	if (!parameters) {
	        	parameters = {};
	        }
	    	else if(ws.parameters) {
	    		Ext.applyIf(parameters, ws.parameters);	// TODO: Handle this line!
	    	}

	    	parameters.SBI_EXECUTION_ID = config.serviceConfig.sbiExecutionId;
	        parameters.user_id = config.serviceConfig.userId;	    	

	    	var serviceUrl = getUrl();
	    	
//	    	if(urlParams && urlParams.length > 0 && urlParams instanceof Array) {
//	    		urlParams.forEach(function(element, index, array){
//	    			serviceUrl = serviceUrl.replace('{'+index+'}', element);
//	    		});
//	    	}
	    	
	        if (ws != undefined) {
	        	
	        	// TODO: Check the diff between Ext and Angular !!!
	        	
//	            Ext.Ajax.request({
////	                url: serviceUrl+"jsonChartTemplate/readChartTemplate",
////	                method: config.serviceConfig.method,
//	                timeout: config.serviceConfig.timeout,
//	                disableCaching: config.serviceConfig.disableCaching,
//	                params: parameters,
//	                headers: {
//	                    'Content-Type': config.serviceConfig.contentType
//	                },
//	                success: function(response) {
//	                	Ext.log({level:'info'}, 'Completed service execution: ' + serviceName);
//	                	successFunction.call(this, response);
//	                },
//	                failure: function(response) {
//	                    Ext.log({level:'error'}, 'Request Failed: ' + response.status);
//	                    failureFunction.call(this, response);
//	                }
//	            });
	        	
	        	var urlPrefix = ".." + config.serviceConfig.wsPrefix;	// TODO: exchange for the configurable param (this info is already defined somewhere before)
	        	var urlSuffix = ws.webServiceConfig.service;
	        	
	        	/**
	        	 * The function that transforms the request from the JSON form to the one that is expected by the REST service that is called
	        	 * (MediaType.APPLICATION_FORM_URLENCODED). Information about the content type (mentioned one) that will be sent towards the
	        	 * server will also be included in the final object configuration that will be sent as a fourth parameter of the sbiModule
	        	 * REST service POST call (object that the 'configParams' variables references to).
	        	 */
	        	var transformRequest = function(obj) {
					        		
					var str = [];
					
					for(var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					
					return str.join("&");
				
	        	};
	        	
	        	var originalParameters = parameters;
	        	
	        	// Transformed parameters (needed for the sbiModule POST REST call).
	        	parameters = transformRequest(parameters);	
	        	
	        	/**
	        	 * The configuration of the parameters needed for the $http service invocation (performed under the 'sbiModule_restServices' hood) -
	        	 * header that will contain the content type of the request (must be synchronized with the one expected on the server-side) and the 
	        	 * function that will transform the incoming response (the original one cannot be JSON-ized by the Angular mechanisms).
	        	 */
	        	var configParams = { 
	        			
        			headers: {
        				'Content-Type': config.serviceConfig.contentType
    				}, 
    				
    				
	        		
        			transformResponse: function(obj) {	
    					
    					if (originalParameters.exportWebApp==true && originalParameters.chartType!="TREEMAP" && originalParameters.chartType!="HEATMAP") {
    						
    						/*
								WORKAROUND: Replacing in other way - from the ASCII code for the single quote character to the "escaped" single quote combination in order 
								to enable a proper (adequate) the exporting of the chart. This way we will decode the former single quote in the chart template that was
								exchanged for this code (JSON cannot handle single quote inside it) and have a single quote on its place in the exported chart. 
								@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							*/
    						// Replace ASCII value of the potential single quotes from the template (e.g. from a TITLE) with the a proper escape combination.
    						obj = obj.replace(new RegExp("&#39;",'g'),"\\'");
    						return obj;
    						
    					}
    					else {
    						obj = obj.replace(new RegExp("&#39;",'g'),"\\'");
//    						console.log(obj);
    						return eval("(" + obj + ")");	
    					}
    					
    				}
    				
				};
	        	
        	 	//Calling the REST service and its POST handler method via the 'sbiModule' service.
	        	sbiModule_restServices
	        		.promisePost(urlPrefix, urlSuffix, parameters, configParams)
		        	.then
		        	(
		        		function(response) { 
		        			console.info("[SUCCESS]: The form is submitted successfully.");
		        			
		        			/*
						 		@author: Radmila Selakovic (rselakov, radmila.selakovic@mht.net)
						 		checking if number of recordes get from data set is bigger then 5000
								if it is, then you will get message to reduse data set
								because scatter has limit of 5000 points
							*/
							console.log("response ",response)
		        			successFunction(response); 
	        			},
		        		
	        			function(response) { 
    	    				console.info("[FAILURE]: The form cannot be submitted because of some failure.");
    	    				sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chart.rendering.failure"), sbiModule_translate.load('sbi.generic.failure'));
    	    				failureFunction(response); 
        				}
		        	);
	            
	        } else {
//	            Ext.log({level: 'error'}, 'Sbi.chart.rest.WebServiceRegistry ' + serviceName + ' not registered!');	// TODO: Handle this line!!!
	        	sbiModule_messaging.showErrorMessage("","Failure!"); // TODO: Provide some meaningful message!!!
	        }
	    }
		
		// The API of the chartExecutionWebServiceManagerFactory service.
		return {
			setConfig: setConfig,
			getConfig: getConfig,
			getUrl: getUrl,
			toString: toString,
			registerService: registerService,
			run: run
		};
	}
);