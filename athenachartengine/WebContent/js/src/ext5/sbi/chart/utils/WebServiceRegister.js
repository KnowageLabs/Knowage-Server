Ext.define('Sbi.chart.utils.WebServiceRegister', {
    extend: 'Ext.util.Observable',
    requires: 'Sbi.chart.utils.WebService',
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
    add: function(serviceName, serviceConfig) {
         Ext.applyIf(serviceConfig, this.getServiceConfig());
         var newService = Ext.create('Sbi.chart.utils.WebService', serviceConfig);
         this.getWebServices().add(serviceName, newService);
    }

    ,
    run: function(serviceName, parameters, successFunction) {        if (!parameters) {
        	parameters = {};
        }
        parameters.SBI_EXECUTION_ID = this.getServiceConfig().sbiExecutionId;
        parameters.user_id = this.getServiceConfig().userId;
    	
    	var ws = this.getWebServices().get(serviceName);

        if (ws != undefined) {
            Ext.Ajax.request({
                url: ws.getUrl(),
                method: ws.getMethod(),
                timeout: ws.getTimeout(),
                disableCaching: ws.getDisableCaching(),
                params: parameters,
                headers: {
                    'Content-Type': ws.getContentType(),
                },
                success: successFunction,
                failure: function(response) {
                    Ext.Msg.alert('Status', 'Request Failed: ' + response.status);
                }
            });        } else {
            Ext.log({level: 'error'}, 'Sbi.chart.utils.WebServiceRegistry ' + serviceName + ' not registered!');
        }
    }
});