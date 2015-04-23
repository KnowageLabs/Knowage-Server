Ext.define('Sbi.chart.rest.WebServiceManager', {
    extend: 'Ext.util.Observable',
    requires: 'Sbi.chart.rest.WebService',
    config: {
        serviceConfig: {
            protocol: 'http',
            hostName: 'localhost',
            tcpPort: '8080',
            context: '/athenachartengine',
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

    , run: function(serviceName, parameters, urlParams, successFunction) {    	
    	Ext.log({level:'info'}, 'Starting service execution: ' + serviceName);
    	
    	if (!parameters) {
        	parameters = {};
        }
        parameters.SBI_EXECUTION_ID = this.getServiceConfig().sbiExecutionId;
        parameters.user_id = this.getServiceConfig().userId;
    	
    	var ws = this.getWebServices().get(serviceName);

    	var serviceUrl = ws.getUrl();
    	Ext.log(serviceUrl);
    	Ext.log(urlParams);
    	if(urlParams && urlParams.length > 0 && urlParams instanceof Array) {
    		urlParams.forEach(function(element, index, array){
    			Ext.log('PIPpOOOOOOOOOOOO'+index);
    			serviceUrl = serviceUrl.replace('{'+index+'}', element);
    		});
    	}
    	Ext.log(serviceUrl);
    	
        if (ws != undefined) {
            Ext.Ajax.request({
                url: serviceUrl,
                method: ws.getMethod(),
                timeout: ws.getTimeout(),
                disableCaching: ws.getDisableCaching(),
                params: parameters,
                headers: {
                    'Content-Type': ws.getContentType(),
                },
                success: function(response) {
                	Ext.log({level:'info'}, 'Completed service execution: ' + serviceName);
                	successFunction.call(this, response);
                },
                failure: function(response) {
                    Ext.log({level:'error'}, 'Request Failed: ' + response.status);
                }
            });        } else {
            Ext.log({level: 'error'}, 'Sbi.chart.rest.WebServiceRegistry ' + serviceName + ' not registered!');
        }
    }
});