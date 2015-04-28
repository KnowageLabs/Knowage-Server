Ext.define('Sbi.chart.designer.ChartStructure', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Table'
    ],

    config :
    {
        title : 'Default custom Title',
        leftYAxisesPanel : {
            html: '<div>Left Y Axises</div>',
        },
        previewPanel : {
            html: '<div>Preview da mostrare</div>',
        },
        rightYAxisesPanel : {
            html: '<div>Right Y Axises</div>',
        },
        bottomXAxisesPanel : {
        	html: '<div>Bottom X Axises</div>',
        },
    },

    constructor: function(config) {
        this.callParent(config);
        
        this.title = config.title && config.title != null ? config.title: this.title;

        this.add(config.leftYAxisesPanel);
        this.add(config.previewPanel);
        this.add(config.rightYAxisesPanel);
        this.add({html: ''});
        this.add(config.bottomXAxisesPanel);
        this.add({html: ''});
    },

    xtype: 'layout-table',
    width: "100%",

    layout: {
        type: 'table',
        columns: 3,
        rows: 2,
        tableAttrs: {
            style: {
                width: '100%',
            }
        }
    },

    scrollable: true,

    defaults: {
        //bodyPadding: '15 20',
        border: true
    },

    items: []

});