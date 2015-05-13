Ext.define('Sbi.chart.rest.WebServiceManagerFactory', {
	
	config: {
		
	}

	, constructor: function(config) {
	    this.initConfig(config);
	    this.callParent();
	}
	
	, statics: { 
	
		getChartWebServiceManager: function(protocol, hostName, tcpPort, sbiExecutionId, userId) {
	        var chartServiceManager = Ext.create('Sbi.chart.rest.WebServiceManager', {
				serviceConfig: {
					protocol: protocol,
					hostName: hostName,
					tcpPort: tcpPort,
					context: '/athenachartengine',
					wsPrefix: '/api/1.0/',
					sbiExecutionId: sbiExecutionId,
					userId: userId
				}
			});
			
			chartServiceManager.registerService('jsonChartTemplate', {
				service: 'jsonChartTemplate/readChartTemplate',
				method: 'POST'
			});
			
			chartServiceManager.registerService('loadDatasetFields', {
				service: 'jsonChartTemplate/fieldsMetadata',
				method: 'GET'
			});
			
			return chartServiceManager;
		}
		
		, getCoreWebServiceManager:  function(protocol, hostName, tcpPort, sbiExecutionId, userId) {
			var coreServiceManager = Ext.create('Sbi.chart.rest.WebServiceManager', {
				serviceConfig: {
					protocol: protocol,
					hostName: hostName,
					tcpPort: tcpPort,
					context: '/athena',
					wsPrefix: '/restful-services/1.0/',
					sbiExecutionId: sbiExecutionId,
					userId: userId
				}
			});
			
			coreServiceManager.registerService('loadData', {
				service: 'datasets/{0}/data',
				method: 'POST'
			});
			
			coreServiceManager.registerService('loadDatasetFields', {
				service: 'datasets/{0}/fields',
				method: 'GET'
			});
			
			coreServiceManager.registerService('saveChartTemplate', {
				service: 'documents/saveChartTemplate',
				method: 'POST'
			});
			
			return coreServiceManager;
		}
	}
	
});