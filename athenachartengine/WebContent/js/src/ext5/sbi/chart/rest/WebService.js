Ext.define('Sbi.chart.rest.WebService', {
    extend: 'Ext.util.Observable',
    config: {
        protocol: 'http',
        hostName: 'localhost',
        tcpPort: '8080',        
        
        /**
    	 * (Topic: context name and context path improvement)
    	 * 
    	 * This is context of the path (gives us the root URL part that point
    	 * to the root project responsible for rendering the application). This 
    	 * variable is the global one defined inside of the 'chart.jsp' file and 
    	 * it is used for purpose of dynamic path specification.
    	 * 
    	 * @author: danristo (danilo.ristovski@mht.net)
    	 */
    	context: Sbi.context,   
        
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
        return this.getProtocol() + '://' + this.getHostName() + ':' + this.getTcpPort() + this.getContext() + this.getWsPrefix() + this.getService();
    }

    ,
    toString: function() {
        return Ext.JSON.encode(this);
    }
});