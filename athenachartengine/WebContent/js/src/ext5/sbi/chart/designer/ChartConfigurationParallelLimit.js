Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelLimit", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartParallelLimit",
		columnWidth: 0.3,
		title: LN("sbi.chartengine.configuration.parallel.limit.title"),
		bodyPadding: 10,
		items: [],
		height: 150,
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		layout: 
		{
		    type: 'vbox'
		},
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			this.storeForSeriesBeforeDrop = Ext.data.StoreManager.lookup('storeForSeriesBeforeDrop');
			//console.log(this.storeForSeriesBeforeDrop);
			
			/* We are communicating with the ChartColumnsContainerManager.js for additional
			 * serie columns or for the removed ones (after loading of the Designer). */
			//console.log(this.storeForSeriesBeforeDrop.getAt(0));
			//console.log(config.viewModel.data.configModel.data.serieFilterColumn);
			
			this.seriesColumnsOnYAxisCombo = Ext.create
			(
				'Ext.form.ComboBox', 
				{
				    fieldLabel: LN("sbi.chartengine.configuration.parallel.limit.serieFilterColumn"),	
				    bind : '{configModel.serieFilterColumn}',
				    store: this.storeForSeriesBeforeDrop,
				    editable : false,
				    queryMode: 'local',
				    displayField: 'serieColumn',
				    valueField: 'serieColumn',
				    //value: this.storeForSeriesBeforeDrop.getAt(0)
				    value: config.viewModel.data.configModel.data.serieFilterColumn
				}
			);
			
			var items = 
			[
				{
					 xtype: 'numberfield',
					 bind : '{configModel.maxNumberOfLines}',	
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.limit.maxNumberOfLines"),	
					 width: "200",
					 value: "100",
					 maxValue: '1000',
					 minValue: '5'
				},
				
				/* Combobox for POSITION of the TOOLBAR (top, bottom) */
	         	{
	         		xtype : 'combo',
	         		queryMode : 'local',
	         		value : 'bottom',
	         		triggerAction : 'all',
	         		forceSelection : true,
	         		editable : false,
	         		fieldLabel : LN("sbi.chartengine.configuration.parallel.limit.orderTopMinBottomMax"), 
	         		bind : '{configModel.orderTopMinBottomMax}',
	         		displayField : 'name',
	         		valueField : 'value',
            		 
            		 store: 
            		 {
            			 fields : ['name', 'value'],
            			 
            			 data : 
        				 [ 
            				   {
            					   name : LN('sbi.chartengine.configuration.position.b'),	
            					   value : 'bottom'
            				   }, 
            				   
            				   {
            					   name : LN('sbi.chartengine.configuration.position.t'),	
            					   value : 'top'
            				   }
        				   ]
            		 }
	         	}
			 ];
						
			this.add(this.seriesColumnsOnYAxisCombo);
			this.add(items);
		},
		
		addItem: function(data)
		{
			this.seriesColumnsOnYAxisCombo.getStore().add(data);
		},
		
		removeItem: function(data)
		{
			this.seriesColumnsOnYAxisCombo.getStore().remove(data);
		}
});