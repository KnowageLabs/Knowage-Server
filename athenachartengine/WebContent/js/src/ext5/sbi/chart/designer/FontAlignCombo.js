Ext.define('Sbi.chart.designer.FontAlignCombo', {
    extend :'Ext.form.ComboBox',
    
    store: {
        fields: [ 'name','value' ],
        data: [ {
			name : LN('sbi.chartengine.configuration.alignment.r'),
			value : 'right'
		}, {
			name : LN('sbi.chartengine.configuration.alignment.l'),
			value : 'left'
		}, {
			name : LN('sbi.chartengine.configuration.alignment.c'),
			value : 'center'
		} ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : LN('sbi.chartengine.configuration.alignment'),
    queryMode : 'local',
});