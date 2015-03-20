Ext.define('Sbi.behavioural.analyticalDriver.AnalyticalDriverUseModel',{
				
				extend: 'Ext.data.Model',
				fields: [
				     
				        "ID",
				        "USEID",
				        "LABEL",
				        "DESCRIPTION",
				        "NAME"
				            
				        ],
				        
				        idProperty: "USEID",
						
						proxy:{
							
							type: 'rest',
							url : Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'analyticalDriverUse'}),
							appendId: false,
							reader: {
								
								type: "json",
								root: "ADUSE"
								
							}
						}
			
		});