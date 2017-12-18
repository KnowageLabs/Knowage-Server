var svgNS = "http://www.w3.org/2000/svg";
var attribNS="http://www.carto.net/attrib";



function BarChart( config ) {
	
	var defaults = {
    x: 0
    , y: 0
    , width: 100
    , height: 100
    , barPadding: 3
    , nrOfGridLine: 10
    , maxGridLineValue: -1
    , gridLineVisible: true
    , labelsVisible: true
    , scale: 1.0
  }
  
  Utils.apply(this, config, defaults);
	
  
  this.barWidth;	
	this.gridLineValues = [];
	
	this.unitList = [];
	this.names = [];
	this.values = [];
	this.colours = [];
	
	this.parentNode;
	this.chartUniqueName;
};

BarChart.prototype.setDataset = function( dataset ) {
	
  for(i = 0; i <  dataset.values.length; i++) {
		this.unitList[i] = "" + (i+1);	
	}
	
	Utils.apply(this, dataset);	
};



BarChart.prototype.setDimensions = function( dimensions ) {
	Utils.apply(this, dimensions);	
};




BarChart.prototype.render = function(parentNode, chartUniqueName, display) {


	var chartElement = document.createElementNS(svgNS,"g");
	chartElement.setAttribute("id", "" + chartUniqueName);
	
	chartElement.setAttribute("shape-rendering", "geometricPrecision");
	chartElement.setAttribute("font-size", "8");

	var chartBoxElement = document.createElementNS(svgNS,"rect");
	chartBoxElement.setAttribute("id", "" + chartUniqueName + "_box");
	chartBoxElement.setAttribute("x", "" + 0);
	chartBoxElement.setAttribute("y", "" + 0);
	chartBoxElement.setAttribute("width", "" + this.width);
	chartBoxElement.setAttribute("height", "" + this.height);
	chartBoxElement.setAttribute("fill", "none");
	chartBoxElement.setAttribute("stroke", "red");
	chartBoxElement.setAttribute("stroke-width", "2");
	chartBoxElement.setAttribute("display", "none");
	
	chartElement.appendChild(chartBoxElement);
	
	parentNode.appendChild(chartElement);
	

	this.barWidth = this.width / this.unitList.length;
  if(this.maxGridLineValue <= 0) {
		this.maxGridLineValue = Math.max.apply(null, this.values);																// highest theme value in db
	}

	
	this.calculategridLineValues();
  this.chart(chartElement, chartBoxElement);
	
	chartElement.setAttribute("display", display);
	//chartElement.setAttribute("transform", "translate(" + this.x + "," + this.y + ")");
	chartElement.setAttribute("transform", "translate(" + this.x + " " + this.y + ") scale(" + this.scale + ")");
	//chartElement.setAttribute("transform", "scale(0.5)");
}


BarChart.prototype.calculategridLineValues = function() {
	var roof;
	this.gridLineValues = [0];
	if(this.maxGridLineValue <= 0) {
		this.maxGridLineValue = Math.max.apply(null, this.values);																// highest theme value in db
	}
	var threshold = this.maxGridLineValue / this.nrOfGridLine;
	for(i = 1;i < this.nrOfGridLine + 1; i ++) {
		roof = Number((threshold * i).toFixed(2));																					// convert to number the truncated string
		this.gridLineValues[i] = roof;																															// populate
	}
}

BarChart.prototype.chart = function(chartElement) {
	chartElement.setAttribute("display","block");
	
	var BarSize = [];
	var i = 0;

 
  // make an array  that contains the class dependent color for each bar 
	for(i = 0;i < this.values.length; i ++) {
		var val = this.values[i];
// a default bar size value of 3px for "0", nodata and wrong data entry
		BarSize[i] = val / (this.maxGridLineValue / (this.height - 3)) + 3;
		//BarSize[i] = val / (this.maxGridLineValue / this.height);
		if(isNaN(BarSize[i])) BarSize[i] = 3;												// safe default value (wrong data format entry in db)
	}

// making the barlines elements
	for(i = 0; i < this.values.length; i ++) {
		var units_index = this.values[i];
		
		// next, create barlines elements
		var bar_offset = i * this.barWidth + (this.barWidth / 2);
		var barline = document.createElementNS(svgNS,"path");
		barline.setAttribute("id","bar" + units_index);
		
		//barline.setAttribute("d","M" + bar_offset + "," + this.height + "v-" + BarSize[i] + "");
		barline.setAttribute("d","M" + bar_offset + "," + this.height + "v-" + BarSize[i] + "");
		
		//barline.setAttribute("stroke","#" + this.colours[i]);
		barline.setAttribute("stroke",this.colours[i]);
		barline.setAttribute("stroke-width", this.barWidth - this.barPadding);
		chartElement.appendChild(barline);
	}
		
	// create gridlines and gridtext
	if(this.gridLineVisible) {
		for(i = 0; i < this.nrOfGridLine + 1; i ++) {
				var offset = (this.height  - ((this.height / this.nrOfGridLine) * i) );
			//var offset =(this.height / this.nrOfGridLine) * i;
			
			var gtext = this.gridLineValues[i];
			
			var gridline = document.createElementNS(svgNS,"path");
			gridline.setAttribute("id","gridline" + i);
			gridline.setAttribute("d","M0," + offset + "h" + this.width + "");
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
			var offset = i * this.barWidth + (this.barWidth / 2) - 10;
			
		
		
			var hAxisLabelText = document.createElementNS(svgNS,"text");
			hAxisLabelText.setAttribute("id","hAxisLabels" + i);
			hAxisLabelText.setAttribute("x","" + offset);
			hAxisLabelText.setAttribute("y","" + ((this.height -1) + 12 -1));
			hAxisLabelText.setAttribute("transform","rotate(45 " + offset + " " + ((this.height -1) + 22 -1) + ")");
			//hAxisLabelText.setAttribute("font-weight", "bold");
			hAxisLabelText.setAttribute("font-size", "10");
			
			g_text_node = document.createTextNode(this.names[i]);
			hAxisLabelText.appendChild(g_text_node);
			chartElement.appendChild(hAxisLabelText);
		}
	}
}


