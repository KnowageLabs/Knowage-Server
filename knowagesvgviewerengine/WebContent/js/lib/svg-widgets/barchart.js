var svgNS = "http://www.w3.org/2000/svg";
var attribNS="http://www.carto.net/attrib";



function BarChartRenderer(chartX, chartY, chartW, chartH, bar_padding) {
	this.bar_width;
	this.bar_padding = bar_padding;
	
	this.maxGridLineValue = -1;
	this.nrOfGridLine = 10;
	this.GridLineValues = [];
	
	this.unitList = [];
	this.names = [];
	this.values = [];
	this.colours = [];
	
	this.parentNode;
	this.chartUniqueName;
	
	this.chartX = chartX;
	this.chartY = chartY;
	this.chartW = chartW;
	this.chartH = chartH;
	
	this.gridLineVisible = true;
	this.labelsVisible = true;
}

//create a new window
BarChartRenderer.prototype.setDataset = function(names, values, colours) {
	for(i = 0; i <  values.length; i++) {
		this.unitList[i] = "" + (i+1);
	}
	this.names = names;
	this.values = values;
	this.colours = colours;
}

//create a new window
BarChartRenderer.prototype.render = function(parentNode, chartUniqueName, display) {

	/*
													<g id="chart" transform="translate(50 35)" font-size="8" shape-rendering="geometricPrecision" display="none">
													<rect id="chartBox" x="0" y="0" width="100" height="100" fill="none" stroke="gray" stroke-width=".2"/>
												</g>

	*/
		
	var chartElement = document.createElementNS(svgNS,"g");
	chartElement.setAttribute("id", "" + chartUniqueName);
	chartElement.setAttribute("transform", "translate(" + this.chartX + " " + this.chartY + ")");
	//chartElement.setAttribute("x", "" + this.chartX);
	//chartElement.setAttribute("y", "" + this.chartY);
	//chartElement.setAttribute("width", "" + this.chartW);
	//chartElement.setAttribute("height", "" + this.chartH);	
	chartElement.setAttribute("shape-rendering", "geometricPrecision");
	chartElement.setAttribute("font-size", "8");

	var chartBoxElement = document.createElementNS(svgNS,"rect");
	chartBoxElement.setAttribute("id", "" + chartUniqueName + "_box");
	chartBoxElement.setAttribute("x", "" + 0);
	chartBoxElement.setAttribute("y", "" + 0);
	chartBoxElement.setAttribute("width", "" + this.chartW);
	chartBoxElement.setAttribute("height", "" + this.chartH);
	chartBoxElement.setAttribute("fill", "none");
	chartBoxElement.setAttribute("stroke", "red");
	chartBoxElement.setAttribute("stroke-width", "2");
	chartBoxElement.setAttribute("display", "none");
	
	chartElement.appendChild(chartBoxElement);
	
	parentNode.appendChild(chartElement);
	

	this.bar_width = this.chartW / this.unitList.length;
  if(this.maxGridLineValue <= 0) {
		this.maxGridLineValue = Math.max.apply(null, this.values);																// highest theme value in db
	}

	
	this.calculateGridLineValues();
  this.chart(chartElement, chartBoxElement);
	
	chartElement.setAttribute("display", display);
}


BarChartRenderer.prototype.calculateGridLineValues = function() {
	var roof;
	this.GridLineValues = [0];
	if(this.maxGridLineValue <= 0) {
		this.maxGridLineValue = Math.max.apply(null, this.values);																// highest theme value in db
	}
	var threshold = this.maxGridLineValue / this.nrOfGridLine;
	for(i = 1;i < this.nrOfGridLine + 1; i ++) {
		roof = Number((threshold * i).toFixed(2));																					// convert to number the truncated string
		this.GridLineValues[i] = roof;																															// populate
	}
}

BarChartRenderer.prototype.chart = function(chartElement) {
	chartElement.setAttribute("display","block");
	
	var BarSize = [];
	var i = 0;

 
  // make an array  that contains the class dependent color for each bar 
	for(i = 0;i < this.values.length; i ++) {
		var val = this.values[i];
// a default bar size value of 3px for "0", nodata and wrong data entry
		BarSize[i] = val / (this.maxGridLineValue / (this.chartH - 3)) + 3;
		//BarSize[i] = val / (this.maxGridLineValue / this.chartH);
		if(isNaN(BarSize[i])) BarSize[i] = 3;												// safe default value (wrong data format entry in db)
	}

// making the barlines elements
	for(i = 0; i < this.values.length; i ++) {
		var units_index = this.values[i];
		
		// next, create barlines elements
		var bar_offset = i * this.bar_width + (this.bar_width / 2);
		var barline = document.createElementNS(svgNS,"path");
		barline.setAttribute("id","bar" + units_index);
		
		//barline.setAttribute("d","M" + bar_offset + "," + this.chartH + "v-" + BarSize[i] + "");
		barline.setAttribute("d","M" + bar_offset + "," + this.chartH + "v-" + BarSize[i] + "");
		
		//barline.setAttribute("stroke","#" + this.colours[i]);
		barline.setAttribute("stroke",this.colours[i]);
		barline.setAttribute("stroke-width", this.bar_width - this.bar_padding);
		chartElement.appendChild(barline);
	}
		
	// create gridlines and gridtext
	if(this.gridLineVisible) {
		for(i = 0; i < this.nrOfGridLine + 1; i ++) {
			//var offset = ((this.chartY + this.chartH - 3) - ((this.chartH - 3) / this.nrOfGridLine) * i) + (3 * (i == 0));
			//var offset = ((this.chartH - 3) - ((this.chartH - 3) / this.nrOfGridLine) * i) + (3 * (i == 0));		
			var offset = (this.chartH  - ((this.chartH / this.nrOfGridLine) * i) );
			//var offset =(this.chartH / this.nrOfGridLine) * i;
			
			var gtext = this.GridLineValues[i];
			
			var gridline = document.createElementNS(svgNS,"path");
			gridline.setAttribute("id","gridline" + i);
			gridline.setAttribute("d","M0," + offset + "h" + this.chartW + "");
			gridline.setAttribute("stroke","gray");
			gridline.setAttribute("stroke-width",".2");
			chartElement.appendChild(gridline);
			
			gridtext = document.createElementNS(svgNS,"text");
			gridtext.setAttribute("id","gridtext" + i);
			gridtext.setAttribute("x","" + (-3));
			gridtext.setAttribute("y","" + (offset + 3));
			gridtext.setAttribute("text-anchor","end");
			g_text_node = document.createTextNode(gtext);
			gridtext.appendChild(g_text_node);
			chartElement.appendChild(gridtext);
		}
	}
	

	if(this.labelsVisible) {
		for(i = 0; i < this.values.length; i ++) {
			var units_index = this.values[i];
			var offset = i * this.bar_width + (this.bar_width / 2) - 10;
			
		
		
			var hAxisLabelText = document.createElementNS(svgNS,"text");
			hAxisLabelText.setAttribute("id","hAxisLabels" + i);
			hAxisLabelText.setAttribute("x","" + offset);
			hAxisLabelText.setAttribute("y","" + ((this.chartH -1) + 12 -1));
			hAxisLabelText.setAttribute("transform","rotate(45 " + offset + " " + ((this.chartH -1) + 22 -1) + ")");
			//hAxisLabelText.setAttribute("font-weight", "bold");
			hAxisLabelText.setAttribute("font-size", "10");
			
			g_text_node = document.createTextNode(this.names[i]);
			hAxisLabelText.appendChild(g_text_node);
			chartElement.appendChild(hAxisLabelText);
		}
	}
}




function init(evt) {

	var barChartRenderer = new BarChartRenderer(3);
	barChartRenderer.setDataset(["asili","iscritti","docenti"], ["5","678","70"], [103050, 556050, 806450]);
	barChartRenderer.render(document.getElementById("chart"), document.getElementById("chartBox"));
	
}


