Ext.define('Sbi.chart.designer.ChartColumnsContainerManager', {
	requires: [
        'Sbi.chart.designer.ChartColumnsContainer'
    ],
    config:{
    	idseed: 1,
    },

    createChartColumnsContainer: function(id) {
    	var theIdSeed = this.getIdseed();
    	var chartColumnsContainerId = (id && id != '')? id: 'ChartColumnsContainer_' + theIdSeed;
    	this.setIdseed(theIdSeed + 1);
    	
    	
    	
    }

});