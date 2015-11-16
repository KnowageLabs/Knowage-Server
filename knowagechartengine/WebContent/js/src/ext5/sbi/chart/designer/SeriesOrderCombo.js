Ext.define('Sbi.chart.designer.SeriesOrderCombo', {
    extend:'Ext.form.ComboBox',
    
	store: {
		store: 'array',
		fields: ['name', 'value'],
		data: [ {name: LN('sbi.chartengine.designer.seriesorder.asc'), value:'asc'}, 
		        {name: LN('sbi.chartengine.designer.seriesorder.desc'), value:'desc'} ]
	},
	valueField: 'value',
	displayField: 'name',
	fieldLabel : LN('sbi.chartengine.designer.seriesordertype'),
});