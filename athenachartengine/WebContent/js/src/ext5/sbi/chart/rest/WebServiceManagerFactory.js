Ext.define('Sbi.chart.rest.WebServiceManagerFactory', {
	
	config: {
		
	}

	, constructor: function(config) {
	    this.initConfig(config);
	    this.callParent();
	}
	
	, statics: { 

		chartWebServiceManager: undefined,
		coreWebServiceManager: undefined,
	
		getChartWebServiceManager: function(protocol, hostName, tcpPort, sbiExecutionId, userId) {
			
			if(!Sbi.chart.rest.WebServiceManagerFactory.chartWebServiceManager) {
				
				console.log('initializing Sbi.chart.rest.WebServiceManagerFactory.chartServiceManager...', Sbi.chart.rest.WebServiceManagerFactory.chartServiceManager);
				
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
				
				chartServiceManager.registerService('drilldownHighchart', {
					service: 'jsonChartTemplate/drilldownHighchart',
					method: 'POST'
				});
				
				Sbi.chart.rest.WebServiceManagerFactory.chartWebServiceManager = chartServiceManager;
				
			}
				
			return Sbi.chart.rest.WebServiceManagerFactory.chartWebServiceManager;
		}
		
		, getCoreWebServiceManager:  function(protocol, hostName, tcpPort, sbiExecutionId, userId) {
			
			if(!Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager) {
				
				console.log('initializing Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager...', Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager);
			
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
				
				Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager = coreServiceManager;
				
			}
			
			return Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager;
		}
	}
	
});