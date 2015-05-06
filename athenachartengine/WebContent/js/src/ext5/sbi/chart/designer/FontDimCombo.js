var dimData = [[8],[9],[10],[11],[12],[14],[16],[18],[20],[22],[24],[26],[28],[36],[48],[72]];

var dimStore = Ext.create('Ext.data.ArrayStore', {
    fields: [
        {name: 'name'}
    ],
    data: dimData
});

Ext.define('Sbi.chart.designer.FontDimCombo', {
    extend:'Ext.form.ComboBox',
    store: dimStore,
    editable : false,
    displayField: 'name',
    valueField: 'name',
    fieldLabel : 'Dimensione'
});