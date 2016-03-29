/*In this file is used code that is distribuited uner the license:
Copyright 2013 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

function renderSunburst(jsonObject)
{
	/*The part that we need to place into HTML (JSP) in order to attach 
	 * given data to them - we are going to create it through D3 notation */
			
	/* Check if configurable (from the Designer point of view)
	 * parameters are defined through the Designer. If not set
	 * the predefined values, instead. */			

	var chartOpacityOnMouseOver = (jsonObject.chart.opacMouseOver != '$chart.style.opacMouseOver') ? parseInt(jsonObject.chart.opacMouseOver) : 100 ;
	
	/* 'topPadding':	padding (empty space) between the breadcrumb 
	 * 					(toolbar) and the top of the chart when the
	 * 					toolbar is possitioned on the top of the chart. 
	 * 'bottomPadding':	padding (empty space) between the bottom of the 
	 * 					chart and the top of the breadcrumb (toolbar) 
	 * 					when the toolbar is possitioned on the top 
	 * 					of the chart. */
	var topPadding = 30;
	var bottomPadding = 30;
	
	var bcHeightFactor = 1;
	var bcWidthFactor = 1;
		
	// Breadcrumb dimensions: width, height, spacing, width of tip/tail.
	/**
	 * KNOWAGE-701 and 702 JIRA issue: removed height and width from the customization of the 
	 * toolbar (breadcrumbs), since those two are dictated by the dimensions of the words inside
	 * the each segment of the toolbar (breadcrumb).
	 * 		 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
//		var bcWidth = 2.5*parseInt(jsonObject.toolbar.style.width)*bcHeightFactor;
//		var bcHeight = parseInt(jsonObject.toolbar.style.height)*bcWidthFactor;
	var bcSpacing = parseInt(jsonObject.toolbar.style.spacing);
	var bcTail = parseInt(jsonObject.toolbar.style.tail);
	
	/**
	 * KNOWAGE-702 JIRA issue: removed height from the customization of the toolbar
	 * (breadcrumbs).		 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
//		if (bcHeight < Number(removePixelsFromFontSize(jsonObject.toolbar.style.fontSize)))
//		{
		bcHeight = Number(removePixelsFromFontSize(jsonObject.toolbar.style.fontSize)) + 5;
//		}
	
	/* Dimensions of the Sunburst chart. */
    /* Dimensions of the window in which chart is going to be placed.
     * Hence, radius of the circular Sunburst chart is going to be half of
     * the lesser dimension of that window. */				
    //var width = parseInt(jsonObject.chart.width);
//	    var width = jsonObject.chart.width;
//	    var height = 0;
		
	/**
	 * Normalize height and/or width of the chart if the dimension type for that dimension is
	 * "percentage". This way the chart will take the appropriate percentage of the screen's
	 * particular dimension (height/width).
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
    if (jsonObject.chart.heightDimType == "percentage")
	{
    	var heightNormalized = jsonObject.chart.height ? window.innerHeight*Number(jsonObject.chart.height)/100 : window.innerHeight;
	}
	else
	{
		var heightNormalized = jsonObject.chart.height ? Number(jsonObject.chart.height) : window.innerHeight;
	}	
	
	if (jsonObject.chart.widthDimType == "percentage")
	{
		var width = jsonObject.chart.width ? window.innerWidth*Number(jsonObject.chart.width)/100 : window.innerWidth;
	}
	else
	{
		var width = jsonObject.chart.width ? Number(jsonObject.chart.width) : window.innerWidth;
	}
    
    if (jsonObject.toolbar.style.position=="bottom")
	{
    	height = heightNormalized
		- (Number(removePixelsFromFontSize(jsonObject.title.style.fontSize)) 
						+ Number(removePixelsFromFontSize(jsonObject.subtitle.style.fontSize)))*1.4 
							- bottomPadding - topPadding - bcHeight;
	}
    else
	{
    	height = heightNormalized 
					- (Number(removePixelsFromFontSize(jsonObject.title.style.fontSize)) 
							+ Number(removePixelsFromFontSize(jsonObject.subtitle.style.fontSize)))*1.4 
								- topPadding*2 - bottomPadding - bcHeight;
	}
	
//	    var height = jsonObject.chart.height;
//		/* Manage chart position on the screen (in the window) depending on
//		 * the resizing of it, so the chart could be in the middle of it. */
//		window.onresize = function() 
//		{
//		    width = document.getElementById("chart").getBoundingClientRect().width;
//		    d3.select("#container").attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
//		};	
	
	var radius = Math.min(width,height)/2;	
	
	var chartOrientation = (width > height) ? "horizontal" : "vertical";		
	
    var tipFontSize = parseInt(jsonObject.tip.style.fontSize);
    var tipWidth = parseInt(jsonObject.tip.style.width);
	
    // Parameters (dimensions) for the toolbar (breadcrumb)
	var b = 
	{ 
//			w: bcWidth, 	
		h: bcHeight, 
		s: bcSpacing, 	
		t: bcTail 
	};
	
	/*var chartDivWidth=width;
	var chartDivHeight=height;
	
	if(jsonObject.title.style!="" || jsonObject.subtitle.style!=""){
		chartDivHeight-=jsonObject.title.style
	}*/
	
	
	/* Create necessary part of the HTML DOM - the one that code need to
	 * position chart on the page (D3 notation) */
	
	/**
	 * Add this root DIV so when we specify some font properties for the chart
	 * it can be applied on every chart element that has some elements that are
	 * using font properties, if they are not specified. For example, user defines
	 * font family for the chart, but not for the title. In this case we will 
	 * apple font family of the whole chart on the title DIV element, as well as
	 * on other DIV elements.
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	
	var randomId=  Math.round((Math.random())*10000);
	
	d3.select("body")
		.append("div").attr("id","main"+ randomId)
		.attr("class","d3-container")
		.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
		.style("height", heightNormalized)
		.style("width", width)
		.style("font-family", jsonObject.chart.style.fontFamily)
		.style("font-size", jsonObject.chart.style.fontSize)
		.style("font-style",jsonObject.chart.style.fontStyle)
		.style("font-weight",jsonObject.chart.style.fontWeight)
		.style("text-decoration",jsonObject.chart.style.textDecoration)
		.style("background-color",jsonObject.chart.style.backgroundColor);
	
	// If there is no data in the recieved JSON object - print empty message
	if (jsonObject.data[0].length < 1)
	{
		var emptyMsgFontSize = parseInt(jsonObject.emptymessage.style.fontSize);
		//var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
		var emptyMsgTotal = emptyMsgFontSize;
		
		// Set empty text on the chart
		d3.select("#main"+randomId).append("div")
			.style("color",jsonObject.emptymessage.style.color)
			.style("text-align",jsonObject.emptymessage.style.align)
    		.style("font-family",jsonObject.emptymessage.style.fontFamily)
    		.style("font-style",jsonObject.emptymessage.style.fontStyle)
    		.style("font-weight",jsonObject.emptymessage.style.fontWeight)
    		.style("text-decoration",jsonObject.emptymessage.style.textDecoration)
    		.style("font-size",emptyMsgFontSize)
			.text(jsonObject.emptymessage.text);	
	}
	else
	{			
		// Set title on the chart
		d3.select("#main"+randomId).append("div")
			.attr("id","title"+randomId)  
			.style("color",jsonObject.title.style.color)
			.style("text-align",jsonObject.title.style.align)
    		.style("font-family",jsonObject.title.style.fontFamily)
    		.style("font-style",jsonObject.title.style.fontStyle)
    		.style("font-weight",jsonObject.title.style.fontWeight)
    		.style("text-decoration",jsonObject.title.style.textDecoration)
    		.style("font-size",jsonObject.title.style.fontSize)
			.text(jsonObject.title.text);	
		
		// Set subtitle on the chart
		d3.select("#main"+randomId).append("div")
			.attr("id","subtitle"+randomId)  
			.style("color",jsonObject.subtitle.style.color)
			.style("text-align",jsonObject.subtitle.style.align)
    		.style("font-family",jsonObject.subtitle.style.fontFamily)
    		.style("font-style",jsonObject.subtitle.style.fontStyle)
    		.style("font-weight",jsonObject.subtitle.style.fontWeight)
    		.style("text-decoration",jsonObject.subtitle.style.textDecoration)
    		.style("font-size",jsonObject.subtitle.style.fontSize)
			.text(jsonObject.subtitle.text);
	    
		/* Put the topPadding on the the top of the chart, even if toolbar
		 * is set on the bottom of the chart (because of the clearer and 
		 * more structured view). */
	    d3.select("#main"+randomId).append("div").style("height",topPadding);
	    
	    /* Get the data about the height of the title, subtitle and toolbar 
	     * already placed on the chart. */
	    var titleHeight = d3.select("#title"+randomId)[0][0].getBoundingClientRect().height;
	    var subtitleHeight = d3.select("#subtitle"+randomId)[0][0].getBoundingClientRect().height;
	    var breadCrumbHeight = parseInt(jsonObject.toolbar.style.height);
	    
	    /* Sum of heights of all of the DOM elements above the chart's center:
	     * title, subtitle, toolbar (breadcrumb), padding between the toolbar 
	     * and the chart, half of the height of the chart. */
	    var sumOfHeightsAboveChartCenter = parseInt(titleHeight + subtitleHeight + height/2 + topPadding);		    	
	   		    
	    if (jsonObject.toolbar.style.position=="top")
		{   
	    	sumOfHeightsAboveChartCenter = parseInt(sumOfHeightsAboveChartCenter + bcHeight + topPadding);		    	
    		d3.select("#main"+randomId).append("div").attr("id","sequence"+randomId);	
    		d3.select("#main"+randomId).append("div").style("height",topPadding);
		}
	    
	    d3.select("#main"+randomId).append("div").attr("id","chart"+randomId).attr("class","d3chartclass");	        
    	
    	if (jsonObject.toolbar.style.position=="bottom")
		{   
	    	/* Add padding between the bottom of the chart and the top of 
	    	 * the toolbar (breadcrumb). */	  
    		d3.select("#main"+randomId).append("div").style("height",bottomPadding);
	    	d3.select("#main"+randomId).append("div").attr("id","sequence"+randomId);				
		}
    	
    	if (jsonObject.toolbar.style.position=="top")
		{   
    		d3.select("#main"+randomId).append("div").style("height",bottomPadding);
		}
	       
	
	
	/* Collect all possible colors into one array - PREDEFINED set of colors
	 * (the ones that we are going to use in case configuration for the
	 * current user (customized) is not set already) */	
	var children = new Array();	
	
	children = children.concat(d3.scale.category10().range());
	children = children.concat(d3.scale.category20().range());
	children = children.concat(d3.scale.category20b().range());
	children = children.concat(d3.scale.category20c().range());
	
	/* Map that will contain key-value pairs. Key is going to be name of each 
	 * individual element of the result (of the request for dataset). Value 
	 * will be the color that is going to be assigned to each element. */
	var colors = {}; 
	
	var colorArrangement = new Array();
	
	// Total size of all segments; we set this later, after loading the data.
	var totalSize = 0; 
	
	/* Put inside first "div" element the Suburst chart, i.e. SVG DOM
	 * element that will represent it. SVG window will be with previously
	 * defined dimensions ("width" and "height"). */		
	var vis = d3.select("#chart"+randomId).append("svg:svg")
	    .attr("width", width)
	    .attr("height", height)
	    .append("svg:g")
	    .attr("id", "container"+randomId)
	    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");	
	
	var partition = d3.layout.partition()
	    .size([2 * Math.PI, radius * radius])
	    .value(function(d) { return d.size; });
	
	/* Counting angular data for some particular element of dataset. */
	var arc = d3.svg.arc()
	    .startAngle(function(d) { return d.x; })
	    .endAngle(function(d) { return d.x + d.dx; })
	    .innerRadius(function(d) { return Math.sqrt(d.y); })
	    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });
	 
	/* Get hierarchy of root data (first level of the chart) - 
	 * data ordered by their presence in total ammount (100% of the sum). 
	 * E.g. if we have this distribution of data for particular query:
	 * USA: 78%, Canada: 12%, Mexico: 8%, No country: 2%, the "children"
	 * array (sequence) inside "json" variable will be in descending order: 
	 * USA, Canada, Mexico, No country. */
	var json = buildHierarchy(jsonObject.data[0]);
	
	createVisualization(json);
	}
	/**
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	function getGradientColorsHSL(fromH,fromS,fromL,toH,toS,toL,numberOfLayers) 
	{		
		var i, colors = [],

		deltaH = (toH - fromH) / numberOfLayers,
		deltaS = (toS - fromS) / numberOfLayers,
		deltaL = (toL - fromL) / numberOfLayers;

		for (i = 0; i <= numberOfLayers; i++) 
		{		        	
			colors.push( d3.hsl(fromH + deltaH * i, fromS + deltaS * i, fromL + deltaL * i) );
		}

			return colors;
	}
	
	// TODO: remove - not needed
//		function rgbToHsl(r, g, b){
//		    r /= 255, g /= 255, b /= 255;
//		    var max = Math.max(r, g, b), min = Math.min(r, g, b);
//		    var h, s, l = (max + min) / 2;
//
//		    if(max == min){
//		        h = s = 0; // achromatic
//		    }else{
//		        var d = max - min;
//		        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
//		        switch(max){
//		            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
//		            case g: h = (b - r) / d + 2; break;
//		            case b: h = (r - g) / d + 4; break;
//		        }
//		        h /= 6;
//		    }
//
//		    return [h, s, l];
//		}
	
	// Main function to draw and set up the visualization, once we have the data.
	function createVisualization(json) 
	{				
		// Basic setup of page elements.
		/* Set the initial configuration of the breadcrumb - 
		 * defining dimensions of the trail, color of the text 
		 * and position of it within the chart (top (default), bottom) */	
		initializeBreadcrumbTrail();
		
		// Bounding circle underneath the sunburst, to make it easier to detect
		// when the mouse leaves the parent g.
		vis.append("svg:circle")
			.attr("r", radius)
			.style("opacity", 0);

		// For efficiency, filter nodes to keep only those large enough to see.
		var nodes = partition.nodes(json).filter
		(
			function(d) 
			{
				return (d.dx > 0.005); // 0.005 radians = 0.29 degrees
			}
		);
		
		// NEW
		
		/* Dark colors for the chart's first layer items */
//			var storeFirstLayerColor = 
//			[
//			 	"#CC0000", 	// red
//			 	"#003D00", 	// green
//			 	"#151B54", 	// blue	
//			 	"#CC3399",	// purple
//			 	"#808080",	// gray
//			 	"#FF9900"	// orange			 			 	
//		 	];
		
		

		var storeFirstLayerColor = new Array();
		var allColorsLayered = new Array();
		
		var allColorsUserPicked;
		if(jsonObject.colors.length >0){
			allColorsUserPicked= jsonObject.colors;
			
		}else{
			allColorsUserPicked=getDefaultColorPalette();
		}
		
		// START: TODO: maybe not necessary (danristo)
		var differentLayersArray = new Array();
		var numberOfLayers = -1;
	   
		//count of elements on layer 0 indicates how many colors are needed
		var numberOfCategories=0;
		
		for (j=0; j<nodes.length; j++)
		{
			if (differentLayersArray.indexOf(nodes[j].layer) < 0)
			{
				differentLayersArray.push(nodes[j].layer);
				numberOfLayers++;
			}
			
			if(nodes[j].layer==0){
				numberOfCategories++;
			}
		}
		//if not enough colors specified fills from color pallete
		
		if(allColorsUserPicked.length < numberOfCategories ){
			colorPalette=allColorsUserPicked;
			picked=allColorsUserPicked.length;
			for(i=0;i<numberOfCategories-picked;i++){
				allColorsUserPicked.push(colorPalette[i%colorPalette.length]);
				
			}
			
		}
		
		// END : maybe not necessary
					
		for (p=0; p<allColorsUserPicked.length; p++)
		{
			storeFirstLayerColor.push(allColorsUserPicked[p]);
			
			var layeringColorRaw = allColorsUserPicked[p];	// start color			
			var layeringColorRGB = d3.rgb(layeringColorRaw);			
			var layeringColorHSL = layeringColorRGB.hsl();			
						
			// number of layers: 20
			var yyyHSL = getGradientColorsHSL(layeringColorHSL.h,layeringColorHSL.s,layeringColorHSL.l,0,0,1,15);
			var yyyRGB = new Array();
			//console.log(yyyHSL);
			
			for (w=0; w<yyyHSL.length; w++)
			{
				var uuu = yyyHSL[w];
				//console.log(uuu);
				//console.log(uuu.rgb());
				yyyRGB.push(uuu.rgb());
			}
			
			allColorsLayered.push(yyyRGB);
		}
		
		//console.log(storeFirstLayerColor);
		//console.log(allColorsLayered);
		
		
//			var storeColors = 
//			[
//			 	"red", "green", "blue", "orange", "purple"
//			 ];
//			
//			var varietiesOfMainColors = 
//			{
//				red: 	["#CC0000", "#FF4747", "#FF7A7A", "#FF9595", "#FFAAAA", "#FFBBBB", "#FFC9C9", "#FFD4D4", "#FFDDDD", "#FFE4E4", "#FFECEC"],
//				green: 	["#003D00", "#003100", "#194619", "#305830", "#456945", "#587858", "#698669", "#789278", "#869D86", "#92A792", "#9DB09D"],
//				blue: 	["#151B54", "#2C3265", "#414674", "#545882", "#65698E", "#747899", "#8286A3", "#8E92AC", "#999DB4", "#A3A7BC", "#BDC0CF"],
//				orange: ["#E68A00", "#FF9900", "#EBA133", "#FFAD33", "#FFC266", "#FFCC80", "#FFD699", "#FFE0B2", "#FFEBCC", "#FFF5E6", "#FFE3BA"],
//				purple: ["#7A297A", "#8A2E8A", "#993399", "#A347A3", "#AD5CAD", "#B870B8", "#C285C2", "#CC99CC", "#D6ADD6", "#E0C2E0", "#EBD6EB"],
//			};
		
//			var rbgRedColor = d3.rgb("#CC0000");
//			var rgbRedWhiteColor = d3.rgb("#FFECEC");
//			
//			var hslRedColor = rbgRedColor.hsl();
//			var hslRedWhiteColor = rgbRedWhiteColor.hsl();
		
//			var gradientColors = getGradientColorsHSL([hslRedColor,hslRedColor,hslRedWhiteColor,10]);
//			console.log(gradientColors);
		
//			var  baseColor = Ext.draw.Color.create(args[0]);
//		     var  from =args[1];
//		     var  to =args[2];
//		     var  number =args[3];
		
		var rootParentsNodes = getRootParentNodes(nodes);
		var counter = 0;
		
		var path = vis.data([json]).selectAll("path")
			.data(nodes)
			.enter().append("svg:path")
			.attr("display", function(d) { return (d.depth && (d.name!=""||d.name)) ? null : "none"; })
			.attr("d", arc)
			.attr('stroke', (jsonObject.chart.style.backgroundColor && 
								jsonObject.chart.style.backgroundColor!="" && 
									jsonObject.chart.style.backgroundColor!=undefined) ? 
											jsonObject.chart.style.backgroundColor : "#FFFFFF")	// color bewtween arcs (danristo)
			.attr('stroke-width', '2')	// spacing (width, padding) bewtween arcs (danristo)
			.attr("fill-rule", "evenodd")
			.style
			(
					"fill", 
					
					function(d,i) 
					{   	    								
						  /* Go through the array of key-value pairs (elements of the chart and their color)
						   * and check if there is unique element-color mapping. */
//							  if (colors[d.name] == undefined && d.name != "root")
//							  {
//								  var numberOfColor = Math.floor(Math.random()*children.length);
//								  colors[d.name] = children[numberOfColor];								  
//							  }
													
						if(d.name!=null && d.name!="")
						{
						  /* If current node is not a root */
						  if (d.name != "root")
						  {								  
							  /* If current node's parent name is root
							   * (if this node is part of the first layer) */
							  
							  if (d.parent.name=="root")
							  {									  
//									  colors[d.name] = varietiesOfMainColors[storeColors[counter]][0];	
								  //console.log(allColorsLayered[counter][0]); //ok
								  colors[d.name] = allColorsLayered[counter][0];
//									  colors[d.name] = varietiesOfMainColors[counter][0];		
								  counter++;
							  }		
							  else
							  {
								  //console.log(d.firstLayerParent);
//									  console.log(rootParentsNodes.indexOf(d.firstLayerParent));
								  //console.log(storeColors[rootParentsNodes.indexOf(d.firstLayerParent)]);
//									  colors[d.name] = varietiesOfMainColors[storeColors[rootParentsNodes.indexOf(d.firstLayerParent)]][d.layer+1];
								  //console.log(allColorsLayered[rootParentsNodes.indexOf(d.firstLayerParent)][d.layer+1]);
								  colors[d.name] = allColorsLayered[rootParentsNodes.indexOf(d.firstLayerParent)][d.layer+1];
							  }
							  
							  d['color'] = colors[d.name];
							  
							  //colorArrangement[i] = colors[d.name];
							 // console.log(colors[d.name]);
							  return colors[d.name];
						  }							  	
						}
						else
						{								 
							  return "invisible";
						  }		
						  
//							colorArrangement[i] = colors[d.name];
//							  
//							return colors[d.name];	
					}
			)					
			.style("opacity", 1)
			.on("mouseover", mouseover)
			.on("click",function(d){
				return clickFunction(d);
			});

			drawLegend();
		
			// Add the mouseleave handler to the bounding circle.
			d3.select("#container"+randomId).on("mouseleave", mouseleave);

			// Get total size of the tree = value of root node from partition.
			totalSize = path.node().__data__.value;
	 };
	
	 /**
	  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	  */
	 function getRootParentNodes(nodes)
	 {				 
		var arrayOfParents = [];
			
		 for (var i=0; i<nodes.length; i++)
		 {
			 if (nodes[i].parent && nodes[i].parent.name=="root")
			 {
				 arrayOfParents.push(nodes[i].name);
			 }
		 }
		 
		 return arrayOfParents;
	 };
	 
	// Fade all but the current sequence, and show it in the breadcrumb trail.
	function mouseover(d) 
	{	
	  var percentage = (100 * d.value / totalSize).toPrecision(3);
	  var percentageString = percentage + "%";
	  
	  if (percentage < 0.1) 
	  {
	    percentageString = "< 0.1%";
	  }
	  
	  /* If we already have move mouse over the chart, remove
	   * previous content for the "explanation", i.e. move the
	   * previous text inside the chart.  */
	  if (d3.select("#explanation"+randomId)[0][0] != null)
	  {
		  d3.select("#explanation"+randomId).remove();
		  d3.select("#percentage"+randomId).remove();
	  }	  
	  
	  d3.select("#chart"+randomId)   	
    	.append("div").attr("id","explanation"+randomId)
    	//.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
    	.append("span").attr("id","percentage"+randomId);
	  
	  d3.select("#explanation"+randomId)
	  	.append("text").html("</br>" + jsonObject.tip.text)
    	.style("text-align","center")
    	.style("font-family",jsonObject.tip.style.fontFamily)
  		.style("font-style",jsonObject.tip.style.fontStyle ? jsonObject.tip.style.fontStyle : "none")
		.style("font-weight",jsonObject.tip.style.fontWeight ? jsonObject.tip.style.fontWeight : "none")
		.style("text-decoration",jsonObject.tip.style.textDecoration ? jsonObject.tip.style.textDecoration : "none")
  		.style("font-size",tipFontSize); 
	
	  d3.select("#percentage"+randomId)
	  		.text(percentageString)		  	
	  		.style("font-family",jsonObject.tip.style.fontFamily)
    		.style("font-style",jsonObject.tip.style.fontStyle ? jsonObject.tip.style.fontStyle : "none")
    		.style("font-weight",jsonObject.tip.style.fontWeight ? jsonObject.tip.style.fontWeight : "none")
    		.style("text-decoration",jsonObject.tip.style.textDecoration ? jsonObject.tip.style.textDecoration : "none")
    		.style("font-size",tipFontSize)
    		.style("vertical-align","middle");		  
    			
	  var percentageHeight = document.getElementById('percentage'+randomId).getBoundingClientRect().height;
	  var explanationHeight = document.getElementById('explanation'+randomId).getBoundingClientRect().height;	
	
	  var explanLeftDistance = null;
	  
	  var a = (window.innerWidth-Number(width))/2;
	  var b = Number(width)/2;
	  var c = tipWidth/2;
	  
	  explanLeftDistance = (window.innerWidth > Number(width)) ? (a + b - c) : (b - c);		  
	 
	  d3.select("#explanation"+randomId)
	  	.style("color",jsonObject.tip.style.color)
		.style("position","absolute")
		.style("left",explanLeftDistance)
		.style("width",tipWidth)
		.style("text-align","center");
	    		
	  /* When width of the text area (rectangle) is set, count 
	   * the height of the invisible text rectangle in which 
	   * text will fit. */
	  
	  var textRectangleHight = d3.select("#explanation"+randomId)[0][0].clientHeight;	
	  var distanceFromTheTop = sumOfHeightsAboveChartCenter - textRectangleHight/2;
	    
	  /* When text area in the middle of the chart is determined,
	   * set the distance of the invisible text rectangle from
	   * the top of the window to the top of that rectangle.*/
	  d3.select("#explanation"+randomId)
		.style("top",distanceFromTheTop);    				    
	    
	  d3.select("#explanation"+randomId)
	      .style("visibility", "");
	
	  var sequenceArray = getAncestors(d);
	  updateBreadcrumbs(sequenceArray, percentageString);
	  
	  var opacMouseOver = chartOpacityOnMouseOver;
	  opacMouseOver = opacMouseOver/100; // normalize value from interval [1,100] to %
	  
	  d3.selectAll("path")
      	.style("opacity", opacMouseOver);
	
	  // Then highlight only those that are an ancestor of the current segment.
	  vis.selectAll("path")
	      .filter(function(node) {
	                return (sequenceArray.indexOf(node) >= 0);
	              })
	      .style("opacity", 1);
	}
	
	// Restore everything to full opacity when moving off the visualization.
	function mouseleave(d) {
	
	  // Hide the breadcrumb trail
	  d3.select("#trail"+randomId)
	      .style("visibility", "hidden");
	
	  // Deactivate all segments during transition.
	  d3.selectAll("path").on("mouseover", null);
	
	  // Transition each segment to full opacity and then reactivate it.
	  d3.selectAll("path")
	      .transition()
	      .duration(1000)
	      .style("opacity", 1)
	      .each("end", function() {
	              d3.select(this).on("mouseover", mouseover);
	            });
	
	  d3.select("#explanation"+randomId)
	      .style("visibility", "hidden");
	}
	
	// Given a node in a partition layout, return an array of all of its ancestor
	// nodes, highest first, but excluding the root.
	function getAncestors(node) {
	  var path = [];
	  var current = node;
	  while (current.parent) {
	    path.unshift(current);
	    current = current.parent;
	  }
	  return path;
	}
	
	/* Put the breadcrumb trail that will
	 * be positioned at the position where DOM element with given
	 * ID (#sequence) resides. */
	function initializeBreadcrumbTrail() 
	{
		// Add the svg area.
		/* Adds the new SVG DOM element to the current structure -
		 * it appends new SVG to the very first "div" element in order
		 * to present breadcrumb. It specifies its dimensions (width,
		 * height */		
		
		var trail = d3.select("#sequence"+randomId)
			.append("svg:svg")
			.attr("width", width)
			.attr("height", bcHeight)
			.attr("id", "trail"+randomId);
		  
		// Add the label at the end, for the percentage.
		/* Append to the newly created SVG element text subelement 
		 * that will contain value of the percentage that covered sequence
		 * represent. Here, predefined color of percentage text is black
		 * (#000). ("#000") = ("black") */
		trail
			.append("svg:text")
			.attr("id", "endlabel"+randomId)
			.style("fill", jsonObject.toolbar.style.percFontColor)
			.style("font-family", jsonObject.toolbar.style.fontFamily)
			.style("font-style",jsonObject.toolbar.style.fontStyle ? jsonObject.toolbar.style.fontStyle : "none")
    		.style("font-weight",jsonObject.toolbar.style.fontWeight ? jsonObject.toolbar.style.fontWeight : "none")
    		.style("text-decoration",jsonObject.toolbar.style.textDecoration ? jsonObject.toolbar.style.textDecoration : "none")
			.style("font-size", jsonObject.toolbar.style.fontSize);
	}
	
	/**
	 * Part of the code that provides dynamic width (and height - implemented in the
	 * previous part of the 'renderSunburst' function) of the breadcrumb, according 
	 * to dimensions of the text that is provided within them (breadcrumbs). Their 
	 * dimensions depend on the toolbar font size, font style and font family that
	 * user defined when creating the chart.
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */		
	
	var fontStyle = "";
	
	/**
	 * Take the type of the font style (layout) that is used in this chart for the
	 * toolbar (normal, italic, underline, bold). This is a string.
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	if (jsonObject.toolbar.style.fontStyle)
		fontStyle = jsonObject.toolbar.style.fontStyle;
	else if (jsonObject.toolbar.style.fontWeight)
		fontStyle = jsonObject.toolbar.style.fontWeight;
	else if (jsonObject.toolbar.style.textDecoration)
		fontStyle = jsonObject.toolbar.style.textDecoration;		
	
	/**
	 * Font size of the toolbar of the chart.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var fontSize = jsonObject.toolbar.style.fontSize;
	
	/**
	 * Font family of the toolbar of the chart.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var fontFamily = jsonObject.toolbar.style.fontFamily;
	
	/**
	 * Concatenation of those three elements for the font customization
	 * of the chart's toolbar. This is needed for function that evaluates
	 * the width of the text that should be set in the breadcrumb (this
	 * value depends on those three parameters.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	 */
	var toolbarFontCusomization = fontStyle + " " + fontSize + " " + fontFamily;
	
	// Generate a string that describes the points of a breadcrumb polygon.
	function breadcrumbPoints(d, i) 
	{		
		/**
		 * Get the width of the text inside the breadcrumb (depends on the font size, 
		 * font style and font family that user set for the toolbar of the chart). Use
		 * this value for specifying the coordinates for every single breadcrumb element. 
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
		var wd = getTextWidth(d.name,toolbarFontCusomization) + b.t;
		
		var points = [];
		
		points.push("0,0");							// (bottom, left)
		points.push(wd + ",0");						// (bottom, rigth)
		points.push(wd + b.t + "," + (b.h / 2));	// (middle,right)
		points.push(wd + "," + b.h);				// (top, right)
		points.push("0," + b.h);					// (top, left)
		  
		if (i > 0) 
		{ 
			// Leftmost breadcrumb; don't include 6th vertex.
			points.push(b.t + "," + (b.h / 2));	// (middle, left)
		}
		  
		return points.join(" ");
	}
	
	/**
	 * The function that provides the information about the width of the 
	 * text element of the breadcrumb (according to its font style, font
	 * size and font family).
	 * 
	 * @source 	stackoverflow.com/questions/118241/calculate-text-width-with-javascript 
	 * 			(answer #2)
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	 */
	function getTextWidth(text, font) 
	{
	    // if given, use cached canvas for better performance
	    // else, create new canvas
	    var canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement("canvas"));
	    var context = canvas.getContext("2d");
	    context.font = font;
	    var metrics = context.measureText(text);
	    
	    return metrics.width;
	};
	
	// Update the breadcrumb trail to show the current sequence and percentage.
	function updateBreadcrumbs(nodeArray, percentageString) {
	
	  // Data join; key function combines name and depth (= position in sequence).
	  var g = d3.select("#trail"+randomId)
	      .selectAll("g")
	      .data(nodeArray, function(d) { return d.name + d.depth; });
	
	  // Add breadcrumb and label for entering nodes.
	  var entering = g.enter().append("svg:g");
	
	  /* TODO: see how could possible be realized that breadcrumb items
	   * get different color for different levels, even if the same name. */
	  entering.append("svg:polygon")
	      .attr("points", breadcrumbPoints)
	      .style("fill", function(d) { return d.color; });
	  
	  /**
	   * Set the text in the appropriate breadcrumb element (toolbar).
	   * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   */
	  entering.append("svg:text")
  		.attr
  		(
			"x", 
	    	
			/**
			 * Position the X coordinate of the text of the particular breadcrumb 
			 * element.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			function(d,i)
			{ 		
				/**
				 * For the very first (zeroth) breadcrumb element do not count the 
				 * tail on its left side (the 6th vertex), since it does not have
				 * the one.
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if (i==0)
				{
					return (getTextWidth(d.name,toolbarFontCusomization)+b.t)/2;
				}
				else
				{
					return getTextWidth(d.name,toolbarFontCusomization)/2 + b.t;
				}
			}
  		)
  		.attr("y", b.h / 2)
  		.attr("dy", "0.35em")
  		.attr("text-anchor", "middle")
  		.style("font-family",jsonObject.toolbar.style.fontFamily)
  		.style("font-size",jsonObject.toolbar.style.fontSize)
  		.style("font-style",jsonObject.toolbar.style.fontStyle ? jsonObject.toolbar.style.fontStyle : "none")
  		.style("font-weight",jsonObject.toolbar.style.fontWeight ? jsonObject.toolbar.style.fontWeight : "none")
  		.style("text-decoration",jsonObject.toolbar.style.textDecoration ? jsonObject.toolbar.style.textDecoration : "none")
  		.style("text-shadow", "0px 0px 5px #FFFFFF")
      	.text(function(d) { return d.name; });
	
	  /**
	   * 'transformTranslate' - The aggregation (increasing) temporary variable 
	   * that keeps the translation value that tells the code how much we should 
	   * translate a particular element. 
	   * 
	   * 'overallWidth' - The overall width of the breadcrumb that is needed for 
	   * the percentage value that comes at the end of the breadcrumb.
	   * 
	   * 'prevNode' - Keeps the previous breadcrumb element (node) in order to
	   * count overall width of the breadcrumb.
	   * 
	   * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   */
	  var transformTranslate = 0;
	  var prevNode = null;
	  var overallWidth = 0;		  
	  
	  // Set position for entering and updating nodes.
	  g.attr
	  (
		  "transform", 
		  
		  function(d,i) 
		  { 	
			  /**
			   * Tranform translate the particular breadcrumb element according to its order number
			   * (position) in the breadcrumb (toolbar). Translation will be performed for all elements
			   * that follow the initial element of the breadcrumb (zeroth). Those elements will be
			   * positioned according to the width of the text inside them and the spacing and the tail
			   * value.
			   * 
			   * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			   */
			  if (i>0) 
			  {
				  if(i==1)
				  {
					  transformTranslate = getTextWidth(prevNode.name,toolbarFontCusomization) + b.s + b.t;
				  }
				  else
				  {
					  transformTranslate += getTextWidth(prevNode.name,toolbarFontCusomization) + b.s + b.t;
				  }
			  } 
			  
			  prevNode = d;
			  overallWidth += getTextWidth(d.name,toolbarFontCusomization) + b.s + b.t;
		    
			  return "translate(" + transformTranslate + ",0)";
		  }
	  );
	
	  // Remove exiting nodes.
	  g.exit().remove();		  
	  
	  // Now move and update the percentage at the end.
	  /**
	   * Count the X coordinate position for the percentage value that comes at
	   * the end of the breadcrumb. It is equal to the sum of overall width of 
	   * the breadcrumb and the value of the tail of every breadcrumb element
	   * (just for separation of the breadcrumb and the percentage value).
	   * 
	   * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   */		  
	  d3.select("#trail"+randomId).select("#endlabel"+randomId)
	  	  .attr("x", overallWidth + b.t)
	      .attr("y", b.h / 2)
	      .attr("dy", "0.35em")
	      .text(percentageString);
	
	  // Make the breadcrumb trail visible, if it's hidden.
	  d3.select("#trail"+randomId)
	      .style("visibility", "");
	
	}
	
	function drawLegend() 
	{	
		// Dimensions of legend item: width, height, spacing, radius of rounded rect.
//			var li = 
//					{ 
//						w: parseInt(jsonObject.legend.style.width), h: parseInt(jsonObject.legend.style.height), 
//						s: parseInt(jsonObject.legend.style.spacing), r: parseInt(jsonObject.legend.style.radius) 
//					};
		
		var li = 
		{ 
			w: 200, h: 50, 
			s: 10, r: 15 
		};

		var numOfColorElems = Object.keys(colors).length;
		
		var legend = d3.select("#legend"+randomId).append("svg:svg")
		.attr("width", li.w)
		.attr("height", numOfColorElems * (li.h + li.s));
				
		var g = legend.selectAll("g")
			.data(d3.entries(colors))
			.enter().append("svg:g")
			.attr
			(	
					"transform", 
					
					function(d, i) 
					{
						return "translate(0," + i * (li.h + li.s) + ")";
					}
			);

		g.append("svg:rect")
		.attr("rx", li.r)
		.attr("ry", li.r)
		.attr("width", li.w)
		.attr("height", li.h)
		.style("fill", function(d) { return d.value; });

		g.append("svg:text")
		.attr("x", li.w / 2)
		.attr("y", li.h / 2)
		.attr("dy", "0.35em")
		.attr("text-anchor", "middle")
		.text(function(d) { return d.key; });
	}
	
	/* ME: This function will be called whenever we click on "Legend" 
	 * checkbox (whether its already checked or it is not). It toggles
	 * legends visibility. */
	function toggleLegend() 
	{		
		var legend = d3.select("#legend"+randomId);
		
		if (legend.style("visibility") == "hidden") 
		{
			legend.style("visibility", "");
		} 
		else 
		{
			legend.style("visibility", "hidden");
		}
	}
	
	// Take a 2-column CSV and transform it into a hierarchical structure suitable
	// for a partition layout. The first column is a sequence of step names, from
	// root to leaf, separated by hyphens. The second column is a count of how 
	// often that sequence occurred.
	function buildHierarchy(jsonObject) 
	{
	  var root = { "name": "root", "children": [] };
	  
	  /* Total number of data received when requesting dataset. */
	  var dataLength = jsonObject.length;
	  
	  var counter = 0;
	  for (var i = 0; i < dataLength; i++) 
	  {
	    //var sequence = jsonObject[i].column_1;
	   // var size =+ jsonObject[i].column_2;
		  //console.log(i);
		  var sequence = jsonObject[i].sequence;
		  var size =+ jsonObject[i].value;
		  		
	    if (isNaN(size)) 
	    { 
	    	// e.g. if this is a header row
	    	continue;
	    }
	    
	    /* ME: Split single parts within received data in order
	     * to create visualization of levels that represent those
	     * data. */
	    var parts = sequence.split("_SEP_");
	    
	    var currentNode = root;		    
	    
	    for (var j = 0; j < parts.length; j++) 
	    {
	    	currentNode["layer"] = j-1;
    		currentNode["firstLayerParent"] = parts[0];	    
    		
	    	var children = currentNode["children"];
	    	var nodeName = parts[j];
	    	var childNode;
	    	
	    	if (j + 1 < parts.length) 
	    	{
	    		// Not yet at the end of the sequence; move down the tree.
	    		var foundChild = false;
	    		
	    		for (var k = 0; k < children.length; k++) 
	    		{				    			
	    			if (children[k]["name"] == nodeName) 
	    			{
	    				childNode = children[k];
	    				foundChild = true;
	    				break;
    				}
    			}
	    		
	    		// If we don't already have a child node for this branch, create it.
	    		if (!foundChild) 
	    		{		    			
	    			childNode = {"name": nodeName, "children": []};		    	
	    			children.push(childNode);
	    		}
    		
	    		currentNode = childNode;
	    		
	    		currentNode["firstLayerParent"] = parts[0];
	    		currentNode["layer"] = j-1;
	    	} 
	    	
	    	else 
	    	{
			 	// Reached the end of the sequence; create a leaf node.
			 	childNode = {"name": nodeName, "size": size};
			 	childNode["layer"] = j;
			 	childNode["firstLayerParent"] = parts[0];
			 	children.push(childNode);
	    	}	    		
	    
	    } 	// inner for loop
	    
	  }		// outter for loop
	  
	  return root;
	  
	};
	
	/**
	 * Cockpit handler - SUNBURST
	 */
	function clickFunction(d){
		console.log(jsonObject.crossNavigation.crossNavigationDocumentParams);
		if(jsonObject.chart.isCockpit==true){
			if(jsonObject.chart.outcomingEventsEnabled){
//				paramethers=crossNavigationParams(d);
//				
//				var selectParams={
//						categoryName:paramethers.categoryName,
//						categoryValue:paramethers.categoryValue,
//						serieName:paramethers.serieName,
//						serieValue:paramethers.serieValue,
//						groupingCategoryName:paramethers.groupingCategoryName,
//						groupingCategoryValue:paramethers.groupingCategoryValue	
//				};
			var selectParams=cockpitSelectionParams(d);
			handleCockpitSelection(selectParams);
			}
		}else if(jsonObject.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
			paramethers=crossNavigationParams(d);
			
			var navigParams={
				crossNavigationDocumentName:jsonObject.crossNavigation.crossNavigationDocumentName,
				crossNavigationDocumentParams:jsonObject.crossNavigation.crossNavigationDocumentParams,
				//categoryName:paramethers.categoryName,
				stringParameters:paramethers
			};
			console.log(paramethers);
			var chartType="SUNBURST";
			handleCrossNavigationTo(navigParams,chartType);
		}
		
		
	}
	
	function crossNavigationParams(d){
//			var par={
//				"categoryName":null,
//				"categoryValue":null,
//				"serieName":null,
//				"serieValue":null,
//				"groupingCategoryName":null,
//				"groupingCategoryValue":null
//			};
		toReturn="";
		var docParams=jsonObject.crossNavigation.crossNavigationDocumentParams;
		var categoryParams= cockpitSelectionParams(d);
		
		for(i=0;i<docParams.length;i++){
				   p=docParams[i];
				for(cat in categoryParams){
					var paramName= cat + "_NAME";
					console.log(paramName);
					if(docParams[i].type===paramName){
						toReturn+= docParams[i].urlName+ "="+cat+"&";
					}
					var paramValue=cat+"_VALUE";
					console.log(paramValue);
					if(docParams[i].type===paramValue){
						toReturn+= docParams[i].urlName+ "="+categoryParams[cat]+"&";
					}
				
					
				}
				if(p.type==="SERIE_VALUE"){
					toReturn+= docParams[i].urlName+ "="+d.value+"&";
				}
				
				if(p.type==="ABSOLUTE"){
					toReturn+= docParams[i].urlName+ "="+p.value+"&";
				}
				
				if(p.type==="RELATIVE"){
					toReturn+= docParams[i].urlName+ "="+p.value+"&";
				}
				
			
			
		}
		
//			par.categoryValue=d.name;
//			par.serieValue=d.value;
//			return par;
		
		return toReturn;
	}
	
	function cockpitSelectionParams(d){
		var params={};
		categories=jsonObject.categories;
		current=d;
		tempLayers=[];
		while(current.layer >= 0){
			var tempParam={};
			tempParam.category=categories[current.layer].value;
			tempParam.value=current.name;
			tempLayers.push(tempParam);
			current=current.parent;
		}
		while(tempLayers.length > 0){
			var tempObj= tempLayers.pop();
			params[tempObj.category]=tempObj.value;
		}
		
		
	  return params;	
		
	}
	
	
}	