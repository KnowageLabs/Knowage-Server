Ext.define('Sbi.chart.designer.FontVerticalAlignCombo', {
    extend :'Ext.form.ComboBox',
    
    store: {
        fields: [ 'name','value' ],
        data: [ {
			name : LN('sbi.chartengine.configuration.alignment.h'),
			value : 'high'
		}, {
			name : LN('sbi.chartengine.configuration.alignment.m'),
			value : 'center'
		}, {
			name : LN('sbi.chartengine.configuration.alignment.low'),
			value : 'low'
		} ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : LN('sbi.chartengine.configuration.alignment'),
    queryMode : 'local',
});