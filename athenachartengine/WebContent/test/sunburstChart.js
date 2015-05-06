function renderSunburst(jsonObject)
{
	/* ME: The part that we need to place into HTML in order to attach 
	 * given data to them - we are going to create it through D3 notation */
	
//	document.getElementById.innerHTML="<div id="sequence"></div>"+
//    '<div id="chart"> '+
//	 '  <div id="explanation" style="visibility: hidden;"> '+
//	 '    <span id="percentage"></span><br/> '+
//	 '     of visits begin with this sequence of pages '+
//	 ' </div> '+
//	 ' </div>
	
//	<div id="sidebar">
//    	<input type="checkbox" id="togglelegend"> Legend<br/>
//    	<div id="legend" style="visibility: hidden;"></div>
// 	</div>
	
	console.log(jsonObject);
    
	/* ME: Create necessary part of the HTML DOM - the one that code need to
	 * position chart on the page (D3 notation) */
	d3.select("body")
		.append("div").attr("id","main")
		.style("font-family", jsonObject.font.type);
	
    d3.select("#main")
    	.append("div").attr("id","sequence");
    
    d3.select("#main")
    	.append("div").attr("id","chart")
    	.append("div").attr("id","explanation").attr("style","visibility: hidden;")
    	.append("span").attr("id","percentage");
    
    d3.select("body")
    	.append("div").attr("id","sidebar")
    	.append("input").attr("type","checkbox").attr("id","togglelegend");
    
    d3.select("#sidebar").append("text").html("Legend" + "</br>");
    
    d3.select("#sidebar")
    	.append("div").attr("id","legend").attr("style","visibility: hidden;");
    
/* !!! WE SHOULD CHANGE THE STATIC TEXT INSIDE THE CHART - it should
 * !!! depend on query that user is going to provide in order to create
 * !!! a proper dataset */
    d3.select("#explanation")
    	.append("text").html("</br>" + "of visits begin with this sequence of pages");
	
	// Dimensions of sunburst.
    /* ME: Dimensions of the window in which is chart going to be placed.
     * Hence, radius of the circular Sunburst chart is going to be half of
     * the lesser dimension of that window. */
	var width = 750;
	var height = 600;
	var radius = Math.min(width, height) / 2;	
	
	// Breadcrumb dimensions: width, height, spacing, width of tip/tail.
	var b = { w: 120, h: 30, s: 3, t: 10 };
	
	/* ME: Collect all possible colors into one array - PREDEFINED set of colors
	 * (the ones that we are going to use in case configuration for the
	 * current user (customized) is not set already) */	
	var children = new Array();	
	
	children = children.concat(d3.scale.category10().range());
	children = children.concat(d3.scale.category20().range());
	children = children.concat(d3.scale.category20b().range());
	children = children.concat(d3.scale.category20c().range());
	
	/* ME: Map that will contain key-value pairs. Key is going to be name of each 
	 * individual element of the result (of the request for dataset). Value 
	 * will be the color that is going to be assigned to each element. */
	var colors = {}; 
	
	// Total size of all segments; we set this later, after loading the data.
	var totalSize = 0; 
	
	/* ME: Put inside first "div" element the Suburst chart, i.e. SVG DOM
	 * element that will represent it. SVG window will be with previously
	 * defined dimensions ("width" and "height"). */
	
// ??? I'm not sure what appending of the SVG's subelement ("svg:g") really does ???
	var vis = d3.select("#chart").append("svg:svg")
	    .attr("width", width)
	    .attr("height", height)
	    .append("svg:g")
	    .attr("id", "container")
	    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");	
	
// ???
	var partition = d3.layout.partition()
	    .size([2 * Math.PI, radius * radius])
	    .value(function(d) { return d.size; });
	
	/* ME: This part is counting angular data for some particular 
	 * element of dataset. */
	var arc = d3.svg.arc()
	    .startAngle(function(d) { return d.x; })
	    .endAngle(function(d) { return d.x + d.dx; })
	    .innerRadius(function(d) { return Math.sqrt(d.y); })
	    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });
	 
	/* ME: We now get hierarchy of root data (first level of the chart) - 
	 * data ordered by their presence in total ammount (100% of the sum). 
	 * E.g. if we have this distribution of data for particular query:
	 * USA: 78%, Canada: 12%, Mexico: 8%, No country: 2%, the "children"
	 * array (sequence) inside "json" variable will be in descending order: 
	 * USA, Canada, Mexico, No country. */
	var json = buildHierarchy(jsonObject.data[0]);
  
//	console.log("JSON retrieved from buildHierarchy()...");
//	console.log(json);  
	
	createVisualization(json);
	
	// Main function to draw and set up the visualization, once we have the data.
	function createVisualization(json) 
	{	
		// Basic setup of page elements.
		/* ME: Set the initial configuration of the breadcrumb - 
		 * defining dimensions of the trail, color of the text 
		 * and position of it within the chart (top (default), bottom) */	
		initializeBreadcrumbTrail();
		//drawLegend();
		
		/* ME: Toggles the legend depending on whether checkbox is 
		 * checked. It calls toggleLegend() method. */
		d3.select("#togglelegend").on("click", toggleLegend);
		
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

		var path = vis.data([json]).selectAll("path")
			.data(nodes)
			.enter().append("svg:path")
			.attr("display", function(d) { return d.depth ? null : "none"; })
			.attr("d", arc)
			.attr("fill-rule", "evenodd")
			.style
			(
					"fill", 
					
					function(d) 
					{   	    	
						  /* Go through the array of key-value pairs (elements of the chart and their color)
						   * and check if there is unique element-color mapping. */
						  if (colors[d.name] == undefined && d.name != "root")
						  {
							  var numberOfColor = Math.floor(Math.random()*children.length);
							  colors[d.name] = children[numberOfColor];
						  }
					
						  return colors[d.name];	  
					}
			)					
			.style("opacity", 1)
			.on("mouseover", mouseover);

			drawLegend();
		
			// Add the mouseleave handler to the bounding circle.
			d3.select("#container").on("mouseleave", mouseleave);

			// Get total size of the tree = value of root node from partition.
			totalSize = path.node().__data__.value;
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
	
	  d3.select("#percentage")
	      .text(percentageString);
	
	  d3.select("#explanation")
	      .style("visibility", "");
	
	  var sequenceArray = getAncestors(d);
	  updateBreadcrumbs(sequenceArray, percentageString);
	
	  // Fade all the segments.
	  d3.selectAll("path")
	      .style("opacity", 0.3);
	
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
	  d3.select("#trail")
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
	
	  d3.select("#explanation")
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
	
	/* ME: this function will put the breadcrumb trail that will
	 * be positioned at the position where DOM element with given
	 * ID (#sequence) resides. */
/* !!! This part should be configurable - currently, breadcrumb trail
 * is posiotioned on the top of the chart, but it should be possible
 * to let the user to choose where he would like to place this element.
 * Predefine implementation put this element on the top, because 
 * given ID is attached to the top "div" element of the HTML DOM 
 * structure. */
	function initializeBreadcrumbTrail() 
	{
		// Add the svg area.
		/* ME: Adds the new SVG DOM element to the current structure -
		 * it appends new SVG to the very first "div" element in order
		 * to present breadcrumb. It specifies its dimensions (width,
		 * height */
		var trail = d3.select("#sequence")
			.append("svg:svg")
			.attr("width", width)
			.attr("height", 50)
			.attr("id", "trail");
		  
		// Add the label at the end, for the percentage.
		/* ME: Append to the newly created SVG element text subelement 
		 * that will contain value of the percentage that covered sequence
		 * represent. Here, predefined color of percentage text is black
		 * (#000). ("#000") = ("black") */
		trail
			.append("svg:text")
			.attr("id", "endlabel")
			.style("fill", "#000");
	}
	
	// Generate a string that describes the points of a breadcrumb polygon.
	function breadcrumbPoints(d, i) {
	  var points = [];
	  points.push("0,0");
	  points.push(b.w + ",0");
	  points.push(b.w + b.t + "," + (b.h / 2));
	  points.push(b.w + "," + b.h);
	  points.push("0," + b.h);
	  if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
	    points.push(b.t + "," + (b.h / 2));
	  }
	  return points.join(" ");
	}
	
	// Update the breadcrumb trail to show the current sequence and percentage.
	function updateBreadcrumbs(nodeArray, percentageString) {
	
	  // Data join; key function combines name and depth (= position in sequence).
	  var g = d3.select("#trail")
	      .selectAll("g")
	      .data(nodeArray, function(d) { return d.name + d.depth; });
	
	  // Add breadcrumb and label for entering nodes.
	  var entering = g.enter().append("svg:g");
	
	  entering.append("svg:polygon")
	      .attr("points", breadcrumbPoints)
	      .style("fill", function(d) { return colors[d.name]; });
	
	  entering.append("svg:text")
	      .attr("x", (b.w + b.t) / 2)
	      .attr("y", b.h / 2)
	      .attr("dy", "0.35em")
	      .attr("text-anchor", "middle")
	      .text(function(d) { return d.name; });
	
	  // Set position for entering and updating nodes.
	  g.attr("transform", function(d, i) {
	    return "translate(" + i * (b.w + b.s) + ", 0)";
	  });
	
	  // Remove exiting nodes.
	  g.exit().remove();
	  
	  // Now move and update the percentage at the end.
	  d3.select("#trail").select("#endlabel")
	      .attr("x", (nodeArray.length + 0.5) * (b.w + b.s))
	      .attr("y", b.h / 2)
	      .attr("dy", "0.35em")
	      .attr("text-anchor", "middle")
	      .text(percentageString);
	
	  // Make the breadcrumb trail visible, if it's hidden.
	  d3.select("#trail")
	      .style("visibility", "");
	
	}
	
	function drawLegend() 
	{	
		// Dimensions of legend item: width, height, spacing, radius of rounded rect.
		var li = { w: 120, h: 30, s: 3, r: 3 };

		var numOfColorElems = Object.keys(colors).length;
		
		var legend = d3.select("#legend").append("svg:svg")
		.attr("width", li.w)
		.attr("height", numOfColorElems * (li.h + li.s));
	
//		console.log("bbbbb");
//		console.log(legend.selectAll("g")
//				.data(d3.entries(colors))
//				.enter());
		
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
		
//		console.log(g);

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
		var legend = d3.select("#legend");
		
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
	  
	  /* ME: Total number of data received when requesting dataset. */
	  var dataLength = jsonObject.length;
	  
//	  console.log("Received JSON object:");
//	  console.log(jsonObject);
	  
	  for (var i = 0; i < dataLength; i++) 
	  {
	    //var sequence = jsonObject[i].column_1;
	   // var size =+ jsonObject[i].column_2;
		  
		  var sequence = jsonObject[i].sequence;
		  var size =+ jsonObject[i].value;
		  
		  console.log(size);
		
	    if (isNaN(size)) 
	    { 
	    	// e.g. if this is a header row
	    	continue;
	    }
	    
	    /* ME: Split single parts within received data in order
	     * to create visualization of levels that represent those
	     * data. */
	    var parts = sequence.split("-");
	    
//	    console.log("888888888888888888");
//	    console.log(parts);
	    
	    var currentNode = root;
	    
	    for (var j = 0; j < parts.length; j++) 
	    {
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
	    	} 
	    	
	    	else 
	    	{
			 	// Reached the end of the sequence; create a leaf node.
			 	childNode = {"name": nodeName, "size": size};
			 	children.push(childNode);
	    	}
	    
	    } 	// inner for loop
	    
	  }		// outter for loop
	  
	  return root;
	  
	};
}