/**
 *
 */

Ext.define('Sbi.chart.designer.ChartColumnsContainer', {
    extend: 'Ext.container.Container',

    requires: [
        'Ext.grid.*',
        'Ext.form.*',
        'Ext.layout.container.HBox',
        'Ext.dd.DropTarget',
        'Sbi.chart.designer.AxisesPicker'
    ],
    xtype: 'dd-grid-to-form',


    width: '100%',
    height: 300,
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    bodyPadding: 5,
    myData: [
        { name : 'Colonna 1'},
        { name : 'Colonna 2'},
        { name : 'Colonna 3'},
        { name : 'Categoria 1'},
    ],

    initComponent: function(){
        this.items = [
        /*
        {
            xtype: 'grid',
            viewConfig: {
                plugins: {
                    ddGroup: 'grid-to-form',
                    ptype: 'gridviewdragdrop',
                    enableDrop: false
                }
            },
            store: new Ext.data.Store({
                model: Sbi.chart.designer.AxisesContainerModel,
                data: this.myData
            }),
            columns: [
	            {
	                flex:  1,
	                header: 'Record Name',
	                sortable: true,
	                dataIndex: 'name'
	            },
            ],
            enableDragDrop: true,
            width: 325,
            margin: '0 5 0 0',
            title: 'Data Grid',
            tools: [{
                type: 'refresh',
                tooltip: 'Reset example',
                scope: this,
                handler: this.onResetClick
            }],
            selModel: new Ext.selection.RowModel({
                singleSelect : true
            })
        },
        */
        {
            xtype: 'form',
            flex: 1,
            title: 'Generic Form Panel',
            bodyPadding: 10,
            labelWidth: 100,
            defaultType: 'textfield',
            items: [{
                fieldLabel: 'Record Name',
                name: 'name'
            }, {
                fieldLabel: 'Column 1',
                name: 'column1'
            }, {
                fieldLabel: 'Column 2',
                name: 'column2'
            }]
        }];

        this.callParent();
    },

    onResetClick: function(){
        this.down('grid').getStore().loadData(this.myData);
        this.down('form').getForm().reset();
    },

    onBoxReady: function(){
        this.callParent(arguments);
        var form = this.down('form'),
            body = form.body;

        this.formPanelDropTarget = new Ext.dd.DropTarget(body, {
            ddGroup: 'grid-to-form',
            notifyEnter: function(ddSource, e, data) {
                //Add some flare to invite drop.
                body.stopAnimation();
                body.highlight();
            },
            notifyDrop: function(ddSource, e, data) {
                // Reference the record (single selection) for readability
                var selectedRecord = ddSource.dragData.records[0];

                // Load the record into the form
                form.getForm().loadRecord(selectedRecord);

                // Delete record from the source store.  not really required.
                ddSource.view.store.remove(selectedRecord);
                return true;
            }
        });
    },

    beforeDestroy: function(){
        var target = this.formPanelDropTarget;
        if (target) {
            target.unreg();
            this.formPanelDropTarget = null;
        }
        this.callParent();
    }
});