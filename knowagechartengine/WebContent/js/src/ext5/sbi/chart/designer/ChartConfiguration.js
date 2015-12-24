Ext.define('Sbi.chart.designer.ChartConfiguration',{
	extend: 'Ext.form.Panel',
	layout: 'anchor',
	border: false,
	item: [ ],
    main:null,
    second:null,
    
    /**
	 * NOTE: 
	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
	 * Allow vertical and horizontal scroll bar appearance for the chart
	 * configuration panel (the one that contains the main and the second 
	 * panel) on the Step 2 when its item are not visible anymore due to 
	 * resizing of the window of the browser.
	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
    //overflowX: "auto", 
    overflowY: "auto",
    
    requires: [
               'Sbi.chart.designer.ChartConfigurationSecondContainer'
           ],
    
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