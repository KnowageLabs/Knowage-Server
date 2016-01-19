Ext.define('Sbi.chart.designer.AxisesContainerModel',{
    extend: 'Ext.data.Model',
    fields: [
		'id', 'axisName', 'axisType', 
		
		'yPositionDataLabels', 'colorDataLabels', 'formatDataLabels',
		
		'categoryColumn', 'categoryGroupby', 'categoryStacked', 
		'categoryStackedType', 'categoryOrderColumn', 'categoryOrderType', 
		'categoryDataType',
		
		'serieAxis', 'serieGroupingFunction', 'serieType', 'serieOrderType', 
		'serieColumn', 'serieColor', 'serieShowValue', 'serieShowPercentage', 'serieShowAbsValue',
		'seriePrecision',
		'seriePrefixChar', 'seriePostfixChar', 
		'serieDataType', 'serieFormat',
		
		/**
		 * This item is going to be removed since the serie tooltip HTML template
		 * is handled by the velocity model of the appropriate chart type (this is
		 * done staticly, "under the hood").
		 * 
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		//'serieTooltipTemplateHtml', 
		
		'serieTooltipBackgroundColor', 'serieTooltipAlign', 
		'serieTooltipColor', 'serieTooltipFont', 'serieTooltipFontWeight', 'serieTooltipFontSize',
		
		// For DIAL tag in SERIE tag (characteristic for the GAUGE chart)
		'backgroundColorDial',
		
		// For DATA_LABELS tag in SERIE tag (characteristic for the GAUGE chart)
		'yPositionDataLabels', 'formatDataLabels', 'colorDataLabels'
	],
});