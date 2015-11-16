Ext.define('Sbi.chart.designer.DocumentModel', {
    extend: 'Ext.data.Model',
    fields: [
       {name: 'id', type: 'int', mapping: 'DOCUMENT_ID'},
       {name: 'name', type: 'string', mapping: 'DOCUMENT_NAME'},
       {name: 'label', type: 'string', mapping: 'DOCUMENT_NM'},
       {name: 'descr', type: 'string', mapping: 'DOCUMENT_DESCR'}
    ],
});