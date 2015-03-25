Ext.define('Sbi.behavioural.analyticalDriver.AnalyticalDriverModel',{
				
				extend: 'Ext.data.Model',
				fields: [
				     
				        "ID",
				        "LABEL",
				        "DESCRIPTION",
				        "NAME",
				        "FUNCTIONALFLAG",
				        "TEMPORALFLAG",
				        "INPUTTYPECD"
				        
				        
				        ],
				        
				        idProperty: "ID",
						
						proxy:{
							
							type: 'rest',
							url : Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'analyticalDriver'}),
							appendId: false,
							reader: {
								
								type: "json",
								root: "root"
								
							}
						}
			
		});