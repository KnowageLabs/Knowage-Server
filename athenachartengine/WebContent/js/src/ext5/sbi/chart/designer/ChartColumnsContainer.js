Ext.define('Sbi.chart.designer.ChartColumnsContainer', {
    extend: 'Ext.grid.Panel',
	alternateClassName: ['ChartColumnsContainer'],
	requires: [
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesContainerModel'
    ],
	statics: {
        idseed: 1,
	},
	
    config:{
		minHeight: 150,
//		width: '100%',
		flex: 1,
		axisData: {},
		
    },
    model: Sbi.chart.designer.AxisesContainerModel, 
    
    /*
    constructor: function(config) {
    	this.callParent(config);
    	this.initConfig(config);
    	
    	if(config.id && config.id != '') {
    		this.id = config.id;
    	}

    	console.log('[ChartColumnsContainer.js] config: ', config);

    	var axisData = Sbi.chart.designer.ChartUtils.convertJsonAxisObjToAxisData(config.axis);
    	
    	this.alias = axisData.alias;
    	this.axisType = axisData.axisType;
    	this.position = axisData.position;
    	this.styleRotate = axisData.styleRotate;
    	this.styleAlign = axisData.styleAlign;
    	this.styleColor = axisData.styleColor;
    	this.styleFont = axisData.styleFont;
    	this.styleFontWeigh = axisData.styleFontWeigh;
    	this.styleFontSize = axisData.styleFontSize;
    	this.majorgridInterval = axisData.majorgridInterval;
    	this.majorgridInterval = axisData.majorgridInterval;
    	this.majorgridStyleTypeline = axisData.majorgridStyleTypeline;
    	this.majorgridStyleColor = axisData.majorgridStyleColor;
    	this.minorgridInterval = axisData.minorgridInterval;
    	this.minorgridInterval = axisData.minorgridInterval;
    	this.minorgridStyleTypeline = axisData.minorgridStyleTypeline;
    	this.minorgridStyleColor = axisData.minorgridStyleColor;
    	this.titleText = axisData.titleText;
    	this.titleStyleAlign = axisData.titleStyleAlign;
    	this.titleStyleColor = axisData.titleStyleColor;
    	this.titleStyleFont = axisData.titleStyleFont;
    	this.titleStyleFontWeigh = axisData.titleStyleFontWeigh;
    	this.titleStyleFontSize = axisData.titleStyleFontSize;
    },
    */
    
    columns: [
        {
        	text: 'Default column name', 
            dataIndex: 'serieColumn',
            sortable: true,
            flex: 1
        }
    ],
    enableDragDrop: true,
    enableColumnHide:false,
    margin: '0 0 0 0'	
});