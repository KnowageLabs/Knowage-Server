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
		chartExportWebServiceManager: undefined,
	
		getChartWebServiceManager: function(protocol, hostName, tcpPort, contextName, sbiExecutionId, userId) {
			
			if(!Sbi.chart.rest.WebServiceManagerFactory.chartWebServiceManager) {
				
				console.log('initializing Sbi.chart.rest.WebServiceManagerFactory.chartServiceManager...', Sbi.chart.rest.WebServiceManagerFactory.chartServiceManager);
				
		        var chartServiceManager = Ext.create('Sbi.chart.rest.WebServiceManager', {
					serviceConfig: {
						protocol: protocol,
						hostName: hostName,
						tcpPort: tcpPort,
						context: '/'+contextName,
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
		
		, getCoreWebServiceManager:  function(protocol, hostName, tcpPort, contextName, sbiExecutionId, userId) {
			
			if(!Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager) {
				
				console.log('initializing Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager...', Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager);
			
				var coreServiceManager = Ext.create('Sbi.chart.rest.WebServiceManager', {
					serviceConfig: {
						protocol: protocol,
						hostName: hostName,
						tcpPort: tcpPort,
						context: '/'+contextName,
						wsPrefix: '/restful-services/1.0/',
						sbiExecutionId: sbiExecutionId,
						userId: userId
					}
				});
				
				coreServiceManager.registerService('loadData', {
					service: 'datasets/{0}/data',
					method: 'GET'
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
		
		, getChartExportWebServiceManager:  function(protocol, hostName, tcpPort, contextName, sbiExecutionId, userId) {
			
			if(!Sbi.chart.rest.WebServiceManagerFactory.chartExportWebServiceManager) {
				
				console.log('initializing Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager...', Sbi.chart.rest.WebServiceManagerFactory.coreWebServiceManager);
			
				var chartExportServiceManager = Ext.create('Sbi.chart.rest.WebServiceManager', {
					serviceConfig: {
						protocol: protocol,
						hostName: hostName,
						tcpPort: tcpPort,
						context: '/'+contextName,
						wsPrefix: '/',
						sbiExecutionId: sbiExecutionId,
						userId: userId
					}
				});
				
				chartExportServiceManager.registerService('exportPng', {
					service: '',
					method: 'POST',
					parameters: {
						content:'options',
						type:'image/png',
						width:'600',
						scale: undefined,
						constr:'Chart',
						callback: undefined,
						async: 'true'
					}
				});
				
				Sbi.chart.rest.WebServiceManagerFactory.chartExportWebServiceManager = chartExportServiceManager;
				
			}
			
			return Sbi.chart.rest.WebServiceManagerFactory.chartExportWebServiceManager;
		}
	}
	
});