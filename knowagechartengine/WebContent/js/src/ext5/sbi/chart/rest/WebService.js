Ext.define('Sbi.chart.rest.WebService', {
    extend: 'Ext.util.Observable',
    config: {
        protocol: 'http:',
        hostName: 'localhost',
        tcpPort: '8080',      
        
        context: 'DEFINE-THIS-ON-CALLER', 
        
        wsPrefix: '/api/1.0',
        service: '',
        method: 'POST',
        timeout: 60000,
        disableCaching: false,
        contentType: 'application/x-www-form-urlencoded;  charset=utf-8',
        parameters: {}
    },
    constructor: function(config) {
        this.initConfig(config);
        this.callParent();
    }
    
    ,
    getUrl: function() {
        return this.getProtocol() + '//' + this.getHostName() + ':' + this.getTcpPort() + this.getContext() + this.getWsPrefix() + this.getService();
    }

    ,
    toString: function() {
        return Ext.JSON.encode(this);
    }
});