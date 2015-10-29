Ext.define('Sbi.chart.designer.AxisesContainerModel',{
    extend: 'Ext.data.Model',
    fields: [
		'id', 'axisName', 'axisType', 
		
		'yPositionDataLabels', 'colorDataLabels', 'formatDataLabels',
		
		'categoryColumn', 'categoryGroupby', 'categoryStacked', 
		'categoryStackedType', 'categoryOrderColumn', 'categoryOrderType', 
		'categoryDataType',
		
		'serieAxis', 'serieGroupingFunction', 'serieType', 'serieOrderType', 
		'serieColumn', 'serieColor', 'serieShowValue', 'seriePrecision',
		'seriePrefixChar', 'seriePostfixChar', 
		'serieDataType',
		
		'serieTooltipTemplateHtml', 'serieTooltipBackgroundColor', 'serieTooltipAlign', 
		'serieTooltipColor', 'serieTooltipFont', 'serieTooltipFontWeight', 'serieTooltipFontSize',
		
		// For DIAL tag in SERIE tag (characteristic for the GAUGE chart)
		'backgroundColorDial',
		
		// For DATA_LABELS tag in SERIE tag (characteristic for the GAUGE chart)
		'yPositionDataLabels', 'formatDataLabels', 'colorDataLabels'
	],
});