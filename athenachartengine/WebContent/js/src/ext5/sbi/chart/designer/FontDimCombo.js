Ext.define('Sbi.chart.designer.FontDimCombo', {
    extend :'Ext.form.ComboBox',
    store: {
        fields: [ 'name' ],
        data: [[8],[9],[10],[11],[12],[14],[16],[18],[20],[22],[24],[26],[28],[36],[48],[72]]
    },
    editable : false,
    displayField : 'name',
    valueField : 'name',
    fieldLabel : 'Dimensione'
});