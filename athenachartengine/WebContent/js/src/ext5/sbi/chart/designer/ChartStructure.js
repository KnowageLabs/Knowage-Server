Ext.define('Sbi.chart.designer.ChartStructure', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Column'
    ],
	layout: 'column',
    config :
    {
        title : 'Default custom Title',
        leftYAxisesPanel : {html: '<div>Left Y Axises</div>'},
        previewPanel : {html: '<div>Preview da mostrare</div>'},
        rightYAxisesPanel : {html: '<div>Right Y Axises</div>'},
        bottomXAxisesPanel : {html: '<div>Bottom X Axises</div>'}
    },
    constructor: function(config) {
        this.callParent(config);
        this.title = config.title && config.title != null ? config.title: this.title;
				
		// composizione parte di sinistra
		var leftPanel = Ext.create('Ext.panel.Panel', {
			xtype: 'layout-vertical-box',
			layout: {
				type: 'vbox',
				pack: 'start',
				align: 'stretch'
			},
			columnWidth: 0.2,
		});
		var leftTopPanel = Ext.create('Ext.Container', {
			xtype: 'basic-panels',
			flex: 1,
		});
		leftTopPanel.add(config.leftYAxisesPanel);
		var leftBottomPanel = Ext.create('Ext.Container', {
			xtype: 'basic-panels',
			flex: 1,
		});
		leftPanel.add(leftTopPanel);
		leftPanel.add(leftBottomPanel);
		
		// composizione parte centrale
		var centralPanel = Ext.create('Ext.panel.Panel', {
			xtype: 'layout-vertical-box',
			layout: {
				type: 'vbox',
				pack: 'start',
				align: 'stretch'
			},
			columnWidth: 0.5,
		});
		var centralTopPanel = Ext.create('Ext.Container', {
			xtype: 'basic-panels',
			flex: 1,
		});
		centralTopPanel.add(config.previewPanel);
		
		var centralBottomPanel = Ext.create('Ext.Container', {
			xtype: 'basic-panels',
			flex: 1,
		});
		centralBottomPanel.add(config.bottomXAxisesPanel);
		
		centralPanel.add(centralTopPanel);
		centralPanel.add(centralBottomPanel);		
		
		// composizione parte di destra
		var rightPanel = Ext.create('Ext.panel.Panel', {
			xtype: 'layout-vertical-box',
			layout: {
				type: 'vbox',
				pack: 'start',
				align: 'stretch'
			},
			columnWidth: 0.3,
		});
		var rightTopPanel = Ext.create('Ext.Container', {
			xtype: 'basic-panels',
			flex: 1,
		});
		rightTopPanel.add(config.rightYAxisesPanel);
		var rightBottomPanel = Ext.create('Ext.Container', {
			xtype: 'basic-panels',
			flex: 1,
		});
		rightPanel.add(rightTopPanel);
		rightPanel.add(rightBottomPanel);
		
        this.add(leftPanel);		
        this.add(centralPanel);		
        this.add(rightPanel);
    },

    width: "100%",
    defaults: {
        border: true
    },
    items: []
});