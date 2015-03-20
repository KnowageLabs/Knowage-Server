Ext.define
(
	"Sbi.behavioural.lov.LOVModel", 
		
	{
		extend: 'Ext.data.Model',
			
		fields: 	
		[							 
	         {name: 'LOV_ID',			type: 'number'},
	         {name: 'LOV_LABEL',     	type: 'string'},
	         {name: 'LOV_NAME', 		type: 'string'},
	         {name: 'LOV_DESCRIPTION', 	type: 'string'},	         
	         {name: 'LOV_PROVIDER',    	type: 'string'},
	         {name: 'I_TYPE_CD',     	type: 'string'},
	         {name: 'I_TYPE_ID',      	type: 'string'},	         
	         {name: 'SELECTION_TYPE',   type: 'string'}   
         ],
         
         idProperty: "LOV_ID",
    
         proxy: 
         {
	        type: 'rest',
	        url: Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'LOV'}),
	        
	        appendId:false,
	        
	        reader: 
	        {
	            type: 'json'
	        }
         }
	}
);