var alignData = [ {
	name : 'Destra',
	value : 'dx'
}, {
	name : 'Sinistra',
	value : 'sx'
}, {
	name : 'Centro',
	value : 'cx'
} ];

Ext.define('Sbi.chart.designer.FontAlignCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: alignData
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Allineamento',
    queryMode : 'local',
});