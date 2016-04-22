Ext.define('Sbi.chart.rest.WebServiceManager', {
    extend: 'Ext.util.Observable',
    requires: 'Sbi.chart.rest.WebService',
    config: {
        serviceConfig: {
            protocol: 'http:',
            hostName: 'localhost',
            tcpPort: '8080',
            
            context: 'DEFINE-THIS-ON-FACTORY',
            
            wsPrefix: '/api/1.0',
            sbiExecutionId: '',
            userId: '',
        },
        webServices: Ext.create('Ext.util.HashMap')
    }

    ,
    constructor: function(config) {
        this.initConfig(config);
        this.callParent();
    }

    ,
    registerService: function(serviceName, serviceConfig) {
         Ext.applyIf(serviceConfig, this.getServiceConfig());
         var newService = Ext.create('Sbi.chart.rest.WebService', serviceConfig);
         this.getWebServices().add(serviceName, newService);
    }

    , run: function(serviceName, parameters, urlParams, successFunction, failureFunction) {    	
    	Ext.log({level:'info'}, 'Starting service execution: ' + serviceName);

    	var ws = this.getWebServices().get(serviceName);
    	
    	if (!parameters) {
        	parameters = {};
        }
    	else if(ws.parameters) {
    		Ext.applyIf(parameters, ws.parameters);
    	}

    	parameters.SBI_EXECUTION_ID = this.getServiceConfig().sbiExecutionId;
        parameters.user_id = this.getServiceConfig().userId;
    	

    	var serviceUrl = ws.getUrl();
    	Ext.log({level: 'info'}, serviceUrl);
    	Ext.log({level: 'info'}, urlParams);
    	if(urlParams && urlParams.length > 0 && urlParams instanceof Array) {
    		urlParams.forEach(function(element, index, array){
    			serviceUrl = serviceUrl.replace('{'+index+'}', element);
    		});
    	}
    	Ext.log({level: 'info'}, serviceUrl);
    	
        if (ws != undefined) {
            Ext.Ajax.request({
                url: serviceUrl,
                method: ws.getMethod(),
                timeout: ws.getTimeout(),
                disableCaching: ws.getDisableCaching(),
                params: parameters,
                headers: {
                    'Content-Type': ws.getContentType()
                },
                success: function(response) {
                	Ext.log({level:'info'}, 'Completed service execution: ' + serviceName);
                	successFunction.call(this, response);
                },
                failure: function(response) {
                    Ext.log({level:'error'}, 'Request Failed: ' + response.status);
                    failureFunction.call(this, response);
                }
            });        } else {
            Ext.log({level: 'error'}, 'Sbi.chart.rest.WebServiceRegistry ' + serviceName + ' not registered!');
        }
    }
});