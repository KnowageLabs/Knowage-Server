Ext.define('Sbi.chart.designer.ChartConfigurationModel',{
	extend: 'Ext.data.Model',
    fields: [
		'height', 'width', 'orientation',
		'backgroundColor', 'font', 'fontDimension',
		
		'title', 
		'titleAlign','titleColor','titleFont',
		'titleDimension','titleStyle',
		
		'subtitle', 
		'subtitleAlign','subtitleColor','subtitleFont',
		'subtitleDimension','subtitleStyle',
		
		'nodata', 
		'nodataAlign','nodataColor','nodataFont',
		'nodataDimension','nodataStyle',
		
		'legendPosition', 'legendLayout',
		'legendFloating','legendX','legendY',
		'legendAlign','legendColor','legendFont',
		'legendDimension','legendStyle',
		
	],
	
});