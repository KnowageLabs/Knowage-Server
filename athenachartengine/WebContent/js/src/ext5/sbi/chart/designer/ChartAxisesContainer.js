Ext.define('Sbi.chart.designer.ChartAxisesContainer', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Ext.layout.container.HBox',
		'Sbi.chart.designer.ChartColumnsContainerManager',
		'Sbi.chart.designer.ChartUtils',
	],
	alternateClassName: ['ChartAxisesContainer'],
	
	xtype: 'layout-horizontal-box',
	layout: {
		type: 'hbox',
		pack: 'start',
		align: 'stretch'
	},
	defaults: {
	},
	
	statics: {
		addToAxisesContainer: function (panel) {
			var newPanel = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(
					panel.id , '' , null, true, 
					Sbi.chart.designer.ChartUtils.ddGroupMeasure, 
					Sbi.chart.designer.ChartUtils.ddGroupMeasure);
			if(newPanel != null) {
				Ext.log('Created new ChartColumnsContainer: id="' + newPanel.id + '"');
				panel.add(newPanel);
			}
		}
	},
	
	config: {
		otherPanel: null,
		
//		axis: {},
		
		/*
		Ext.log("Sbi.chart.designer.ChartAxisesContainer constructor BEGIN");
		
		var headerItems = [];
		
		var alias = 'Custom name';
		if(config.alias != '') {
			alias = config.alias;
		}
		var aliasTextfield = {
			xtype: 'textfield',
			flex: 6,
			allowBlank: false,
			height: 30,
            emptyText: 'Insert axis name',
			selectOnFocus: true,
			value: alias,
		};
		headerItems.push(aliasTextfield);
		
		var styleButton = {
			xtype:'button',
			icon: 'http://findicons.com/icon/download/66617/paint/24/png',
			flex: 1
		};
		headerItems.push(styleButton);
		
		if(config.otherPanel && config.otherPanel != null){
			var addButton = {
					xtype:'button',
					text: '+',
					flex: 1,
					handler: function(node, mouse){
						var panel = config.otherPanel;
						if (!panel.isVisible()) {
							panel.setVisible(true);
						}
						
						ChartAxisesContainer.addToAxisesContainer(panel);
					}
			};
			headerItems.push(addButton);
		}
		
		this.header = {
			title: {hidden: true },
			items: headerItems
		};
		

		
		Ext.log("Sbi.chart.designer.ChartAxisesContainer constructor END");
		*/
	},
	items: [],
	
});