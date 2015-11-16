Ext.define
(
	'Sbi.chart.designer.ChartConfigurationScatterZoomType', 
	{
	    extend :'Ext.form.ComboBox',
	    
	    id: "zoomType",
	    
	    store: 
	    {
	        fields: [ 'name','value' ],
	        
	        data: 
        	[ 
        	 	{
        	 		name : "x",
        	 		value : "x"
        	 	}, 
        	 	
        	 	{
        	 		name : "y",
        	 		value : "y"
        	 	}, 
        	 	
        	 	{
        	 		name : "xy",
        	 		value : "xy"
        	 	} 
			]
	    },
	    
	    editable : false,
	    
	    displayField: 'name',
	    valueField: 'value',
	    
	    fieldLabel : "Zoom type",	// TODO: LN()
	    queryMode : 'local',
	}
);