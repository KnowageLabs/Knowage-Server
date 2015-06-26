Ext.define('Sbi.chart.designer.FontCombo',{
    extend : 'Ext.form.ComboBox',
    store: {
        fields: [ 'name' ],
        
        sorters: [{
            property: 'name',
            direction: 'ASC'
        }],
        
        data: [ [ 'Arial' ], [ 'Times New Roman' ], [ 'Tahoma' ], [ 'Verdana' ], ["Impact"],
        		["Calibri"], ["Cambria"], ["Georgia"], ["Gungsuh"] ]
    },
    displayField : 'name',
    valueField : 'name',
    fieldLabel : LN('sbi.chartengine.configuration.font'),
    tdCls : '',
    editable : false,
    listeners : {
        change: function(sender, newValue, oldValue, opts) {
            this.inputEl.setStyle('font-family', newValue);
        }
    }
});