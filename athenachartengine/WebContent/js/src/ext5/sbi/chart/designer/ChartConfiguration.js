Ext.define('Sbi.chart.designer.ChartConfiguration',{
	extend: 'Ext.form.Panel',
//	viewModel: 'chartconfigmodel',
//	session: true,
	border: false,
	layout: 'vbox',
	item: [ ],
	defaults:{
        width: 850,
    },
    main:null,
    second:null,
    
    constructor: function(config) {
    	
    	this.title = config.title && config.title != null ? config.title: this.title;
    	this.viewModel = config.viewModel;
        this.callParent(config);
        this.main = Ext.create('Sbi.chart.designer.ChartConfigurationMainContainer',{
        	viewModel: this.viewModel
        });
		this.second = Ext.create('Sbi.chart.designer.ChartConfigurationSecondContainer',{
			viewModel: this.viewModel
		});
		
        this.add(this.main);
		this.add(this.second);
    }
});