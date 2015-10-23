Ext.define('Sbi.chart.designer.DocumentParamModel', {
    extend: 'Ext.data.Model',
//    idProperty: 'parameterUrlName',
    fields: [
       {name: 'label', type: 'string'},
       {name: 'fieldType', type: 'string'},
       {name: 'value', defaultValue: ''} //Here "defaultValue" is needed to fix a bug on firefox SBI-530/ATHENA-136 
    ],
});
