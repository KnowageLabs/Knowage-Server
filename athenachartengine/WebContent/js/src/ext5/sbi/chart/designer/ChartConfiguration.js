Ext.define('Sbi.chart.designer.ChartConfiguration',{
	extend: 'Ext.form.Panel',
	layout: 'anchor',
	border: false,
	item: [ ],
    main:null,
    second:null,
    
    constructor: function(config) {
    	
    	this.title = config.title && config.title != null ? config.title: this.title;
    	this.viewModel = config.viewModel;
        this.callParent(config);
        this.main = Ext.create('Sbi.chart.designer.ChartConfigurationMainContainer',{
        	id: "main",
        	viewModel: this.viewModel
        });
		this.second = Ext.create('Sbi.chart.designer.ChartConfigurationSecondContainer',{
			viewModel: this.viewModel
		});
		
        this.add(this.main);
		this.add(this.second);
    }
});