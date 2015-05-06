var fontStyleArray = [ {
	name : 'Bold',
	value : 'b'
}, {
	name : 'Normal',
	value : 'n'
}, {
	name : 'Italico',
	value : 'i'
}, {
	name : 'Sottolineato',
	value : 's'
} ];

Ext.define('Sbi.chart.designer.FontStyleCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: fontStyleArray
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Stile',
    queryMode : 'local',
});