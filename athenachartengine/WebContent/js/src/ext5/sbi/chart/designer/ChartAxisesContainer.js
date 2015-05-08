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
					panel.id , '' , true, 
					Sbi.chart.designer.ChartUtils.ddGroup1, 
					Sbi.chart.designer.ChartUtils.ddGroup1);
			if(newPanel != null) {
				Ext.log('Created new ChartColumnsContainer: id="' + newPanel.id + '"');
				panel.add(newPanel);
			}
		}
	},
	
	config: {
		otherPanel: null,
		
		/* * * Internal components * * */
		alias: '', 
		//	type: '', 
		position: '', 
		styleRotate: '', 
		styleAlign: '', 
		styleColor: '', 
		styleFont: '', 
		styleFontWeigh: '', 
		styleFontSize: '',
		majorgridInterval: '',
		majorgridInterval: '',
		majorgridStyleTypeline: '',
		majorgridStyleColor: '',
		minorgridInterval: '',
		minorgridInterval: '',
		minorgridStyleTypeline: '',
		minorgridStyleColor: '',
		titleText: '',
		titleStyleAlign: '', 
		titleStyleColor: '', 
		titleStyleFont: '', 
		titleStyleFontWeigh: '', 
		titleStyleFontSize: '',
		/* * * END Internal components * * */
	},
	
	constructor: function(config) {
		this.callParent(config);
		if(config.id && config.id != '') {
			this.id = config.id;
		}
		
		Ext.log("Sbi.chart.designer.ChartAxisesContainer constructor BEGIN");
		
		var headerItems = [];
		
//		Ext.log("config.alias: ", config.alias);
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
//			text: 'Style',
			flex: 1
		};
		headerItems.push(styleButton);
		
		Ext.log("config.otherPanel: ", config.otherPanel);
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
		Ext.log(this.getId());
	},
	items: [],
	
});