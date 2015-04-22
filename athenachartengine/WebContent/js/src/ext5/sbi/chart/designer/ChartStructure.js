/**
 *
 */

Ext.define('Sbi.chart.designer.ChartStructure', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.Table'
    ],

    config :
    {
        title : 'Custom Title',
    },

    xtype: 'layout-table',
    width: "100%",
    height: "100%",

    layout: {
        type: 'table',
        columns: 3,
        rows: 2,
        tableAttrs: {
            style: {
                width: '100%',
                height: '100%'
            }
        }
    },

    scrollable: true,

    defaults: {
        bodyPadding: '15 20',
        border: true
    },

    items: [
        {
            html: 'Cell A content',
            rowspan: 2
        },
        {
            html: 'Cell B content',
            colspan: 2
        },
        {
            html: 'Cell C content',
            cellCls: 'highlight'
        },
        {
            html: 'Cell D content'
        }
    ]

});