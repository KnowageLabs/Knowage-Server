/**
 * Function needed for cleaning the already rendered chart (if the one exists
 * on the page). This operation is mandatory since we want to rerender the 
 * chart when resizing the window (panel). 
 * (danilo.ristovski@mht.net)
 */
function cleanChart()
{
	/**
	 * Select everything that body of the page contains (every child node)
	 * and remove it from the page.
	 */
	d3.select("body").selectAll("*").remove();
}

/**
 * Convert RGB to HSL.
 * 
 * @param r
 * @param g
 * @param b
 * @returns {Array}
 */
function rgbToHsl(r, g, b)
{
	r /= 255, g /= 255, b /= 255;
	var max = Math.max(r, g, b), min = Math.min(r, g, b);
	var h, s, l = (max + min) / 2;
	
	if(max == min){
	    h = s = 0; // achromatic
	}else{
	    var d = max - min;
	    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
	    switch(max){
	        case r: h = (g - b) / d + (g < b ? 6 : 0); break;
	        case g: h = (b - r) / d + 2; break;
	        case b: h = (r - g) / d + 4; break;
	    }
	    h /= 6;
	}
	
	return [h, s, l];
}

/**
 * Function for extracting the font size from the string value that contains
 * also the 'px' substring. Function is called whenever we need pure numeric
 * value of the size (especially for purposes of dynamic resizing of the chart).
 * 
 * @param fontSize String value of the size of the font
 * @returns Pure numeric value for size of the font
 * 
 * (danilo.ristovski@mht.net)
 */
function removePixelsFromFontSize(fontSize)
{
	var indexOfPx = fontSize.indexOf('px');
	
	if (indexOfPx > 0)
	{
		return fontSize.substring(0,indexOfPx);
	}
	else
	{
		return fontSize;
	}
}

/**
 * Providing the tooltip for charts and determining its position
 * on the chart.
 * 
 * @param chartHeight Height of the chart
 * @param chartWidth Width of the chart
 * @param ttText Tooltip text HTML (DOM) element
 * 
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
function positionTheTooltip(chartHeight,chartWidth,ttText)
{
	var windowHeight = window.innerHeight;
	var windowWidth = window.innerWidth;
	
	ttText
		.style("left", (d3.event.pageX) + "px")
		.style("top", (d3.event.pageY - 25) + "px");
	
	/**
	 * Old implementation that included vertical centering
	 * @commentedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	/*if (chartHeight < windowHeight && chartWidth < windowWidth)
	{
		ttText
			.style("left", (d3.event.pageX - (windowWidth-chartWidth)/2) + "px")
			.style("top", (d3.event.pageY - (windowHeight-chartHeight)/2 - 25) + "px");
	}
	
	if (chartHeight >= windowHeight && chartWidth >= windowWidth)
	{
		ttText
			.style("left", (d3.event.pageX) + "px")
			.style("top", (d3.event.pageY - 25) + "px");
	}
	
	if (chartHeight < windowHeight && chartWidth >= windowWidth)
	{					
		ttText
		.style("left", (d3.event.pageX) + "px")
		.style("top", (d3.event.pageY - (windowHeight-chartHeight)/2 - 25) + "px");
	}
	
	if (chartHeight >= windowHeight && chartWidth < windowWidth)
	{
		ttText
		.style("left", (d3.event.pageX - (windowWidth-chartWidth)/2) + "px")
		.style("top", (d3.event.pageY - 25) + "px");					
	}*/
}

/**
 * function returns array of ten colors that will be used by default 
 * if no color is set in designer 
 */
function  getDefaultColorPalette(){
	var defaultColors = ['#7cb5ec', '#434348', '#90ed7d', '#f7a35c', '#8085e9', 
	         '#f15c80', '#e4d354', '#2b908f', '#f45b5b', '#91e8e1'];
	return defaultColors;
}
