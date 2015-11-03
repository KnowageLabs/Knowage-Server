<%@ page language="java" contentType="text/html; charset=UTF-8"
 pageEncoding="UTF-8"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/adapters/standalone-framework.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-3d.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/exporting.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/no-data-to-display.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/d3/renderD3.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/ext5/sbi/chart/viewer/HighchartsCrossNavigationHelper.js"></script>
<script>

	// TODO: check if ext was imported (don't care)
	
	// TODO: Ext.create("Sbi.chart.library") (don't care)
	
	function initChartLibrary(panelId) {
		
		document.getElementById(panelId).setAttribute("style","height:0px");
		
		/*
		Highcharts.setOptions({
		    chart: {
		 	   renderTo: panelId,
		        backgroundColor: {
		            linearGradient: [0, 0, 500, 500],
		            stops: [
		                [0, 'rgb(255, 255, 255)'],
		                [1, 'rgb(240, 240, 255)']
		            ]
		        },
		        borderWidth: 2,
		        plotBackgroundColor: 'rgba(255, 255, 255, .9)',
		        plotShadow: true,
		        plotBorderWidth: 1
		    },
		    exporting: {
		        url: 'https://export.highcharts.com/'
		    }
		}); 
		*/
	}
	
	/* 	
		Inspect if chart type that we want to render on the window of the page (panel)
		is of D3 type (belongs to D3 library). We need this information for deciding 
		whether we will re-render the chart invoking the same rendering functions for
		the D3 chart.
		(danilo.ristovski@mht.net)
	*/
	function isChartD3(chartConf)
	{
		var chartType = chartConf.chart.type.toLowerCase();
		
		if(chartType == "wordcloud" || chartType == "sunburst" || chartType== "parallel" || chartType == "chord")
		{			
			return true;			
		}
		else
		{
			return false;
		}
	}

	function renderChart(chartConf){

		//new Highcharts.Chart(chartConf);
		// console.log(chartConf);
		
		/* 
			If rerendering the chart, we will need cleaning of the
			page before it. This method will destroy (remove) every
			page element that already existed on the page (potentially).
			It is implemented inside of the renderD3.js.
			@author: (danilo.ristovski@mht.net)
		*/
		cleanChart();
		
		if(chartConf.chart.type.toLowerCase() == "wordcloud"){
			
			renderWordCloud(chartConf);
			
		}
		else if (chartConf.chart.type.toLowerCase() == "sunburst")
		{
			renderSunburst(chartConf);
			
		}
		else if (chartConf.chart.type.toLowerCase() == "parallel")
		{
			renderParallelChart(chartConf);
			
		}
		else if (chartConf.chart.type.toLowerCase() == "chord")
		{
			renderChordChart(chartConf);
		}
		
		else
		{
			alert("Chart not defined");
		}
	}
	
	function handleCrossNavigationTo(e){
		if (!e.seriesOptions) {
			var chart = this;
			//chart.showLoading('Loading...');
			var categoryName=e.categoryName;
			var categoryValue = e.categoryValue;
			var serieName=e.serieName;
			var serieValue = e.serieValue;
			var groupingCategoryName=e.groupingCategoryName;
			var groupingCategoryValue=e.groupingCategoryValue;
			
			Sbi.chart.viewer.HighchartsCrossNavigationHelper.navigateTo(
					e.crossNavigationDocumentName, 
					e.crossNavigationDocumentParams,
					categoryName,
					categoryValue,
					serieName,
					serieValue,
					groupingCategoryName,
					groupingCategoryValue
					);
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager();
			chart.hideLoading();
		}
		
	};
	
	function handleCrossNavigationFrom(){
		Sbi.chart.viewer.HighchartsCrossNavigationHelper.navigateBackTo();
	};
	
</script>