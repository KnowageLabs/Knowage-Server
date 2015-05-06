var fontData = [
    ['Arial'],
    ['Times New Roman'],
    ['Tahoma'],
    ['Verdana']
];

var fontStore = Ext.create('Ext.data.ArrayStore', {
    fields: [{name: 'name'}],
    data: fontData
});

Ext.define('Sbi.chart.designer.FontCombo',{
    extend:'Ext.form.ComboBox', 
    store: fontStore,
    displayField: 'name',
    fieldLabel : 'Carattere',
    tdCls: '',
    editable : false,
    listeners: {
        change: function(sender, newValue, oldValue, opts) {
            this.inputEl.setStyle('font-family', newValue);
        }
    }
});