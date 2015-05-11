Ext.define('Sbi.chart.designer.AxisesContainerModel',{
    extend: 'Ext.data.Model',
    fields: [
		// {name: 'id', type: 'int'}, 
		'axisName', 'axisType', 
		
		'categoryColumn', 'categoryGroupby', 'categoryStacked', 
		'categoryStackedType', 'categoryOrderColumn', 'categoryOrderType', 
		
		'serieGroupingFunction', 'serieType', 'serieOrderType', 
		'serieColumn', 'serieColor', 'serieShowValue', 'seriePrecision',
		'seriePrefixChar', 'seriePostfixChar',
		
		//Da vedere se scorporare e mettere nel model 'Sbi.chart.designer.AxisesTooltipModel'
		'serieTooltipTemplateHtml', 'serieTooltipBackgroundColor', 'serieTooltipAlign', 
		'serieTooltipColor', 'serieTooltipFont', 'serieTooltipFontWeight', 'serieTooltipFontSize'
		
	],
});