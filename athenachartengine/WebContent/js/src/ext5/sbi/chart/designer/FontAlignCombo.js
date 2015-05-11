Ext.define('Sbi.chart.designer.FontAlignCombo', {
    extend :'Ext.form.ComboBox',
    
    store: {
        fields: [ 'name','value' ],
        data: [ {
			name : 'Destra',
			value : 'right'
		}, {
			name : 'Sinistra',
			value : 'left'
		}, {
			name : 'Centro',
			value : 'center'
		} ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Allineamento',
    queryMode : 'local',
});