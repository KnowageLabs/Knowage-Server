Ext.define('Sbi.chart.designer.ChartOrientationCombo',{
    extend : 'Ext.form.ComboBox',
	queryMode : 'local',
	value : 'h',
	triggerAction : 'all',
	forceSelection : true,
	editable : false,
	fieldLabel : 'Verso',
	displayField : 'name',
	valueField : 'value',
	store : {
		fields : [ 'name', 'value' ],
		data : [ {
			name : 'Verticale',
			value : 'v'
		}, {
			name : 'Orizzontale',
			value : 'h'
		} ]
	}
});