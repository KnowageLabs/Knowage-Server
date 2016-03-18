/**
	 * The rendering function for the PARALLEL chart.
	 * 
	 * @param data JSON containing data (parameters) about the chart 
	 */
function renderParallelChart(data){
   
	var records = data.data[0];
	//console.log(records);
	if(records.length>0){

		if (records.length>data.limit.maxNumberOfLines){

			var limitcolumn = data.limit.serieFilterColumn;

			records.sort(function(obj1, obj2) {
				return obj1[limitcolumn] - obj2[limitcolumn];
			});
		
		
		var len = records.length;
		
		var max = data.limit.maxNumberOfLines;
		
		if (data.limit.orderTopMinBottomMax === 'top'){
			
			var slicedData = records.slice(len-max,len);
			
			records = slicedData;
		}
		else if (data.limit.orderTopMinBottomMax === 'bottom'){
			
			var slicedData = records.slice(0,max);
			
			records = slicedData;
		}}

		var groupcolumn = data.chart.group;

		var group = Ext.decode(data.chart.groups);
		var column = Ext.decode(data.chart.serie);

		var groups = [];

		var columns = [];

		var precisions={};
		var prefixes={};
		var postfixes={};

		for (var i = 0; i< group.length; i++){

			groups.push(group[i][i]);
		}

		for (var i = 0; i<column.length;i++){

			columns.push(column[i][i]);
			precisions[column[i][i]]=column[i]["precision"];
			prefixes[column[i][i]]=column[i]["prefix"];
			postfixes[column[i][i]]=column[i]["postfix"];
			
		}

		function pickColors(colors, n){ // picks n different colors from colors
			var selected=[];
			while(selected.length < n){   
				var c= colors[Math.floor(Math.random()*colors.length)];
				if(selected.indexOf(c)==-1){
					selected.push(c);
				}

			}
			return selected;
		}

		var allTableData;

        var colors = [];

		var colorsResponse=data.chart.colors;
		
	
	 var colorsResponseDec = Ext.decode(colorsResponse);
     
     for (var i = 0; i< colorsResponseDec.length; i++){

			colors.push(colorsResponseDec[i][i]);
		
		}
     
     if(colors.length == 0){
    	 colors=getDefaultColorPalette();
     }

     var myColors=d3.scale.ordinal().domain(groups).range(colors);

	    var brushWidth = data.axis.brushWidth;

		var brushx = -Number(brushWidth)/2;


		/**
		 * Normalize height and/or width of the chart if the dimension type for that dimension is
		 * "percentage". This way the chart will take the appropriate percentage of the screen's
		 * particular dimension (height/width).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (data.chart.heightDimType == "percentage")
		{
	    	var heightNormalized = data.chart.height ? window.innerHeight*Number(data.chart.height)/100 : window.innerHeight;
		}
		else
		{
			var heightNormalized = data.chart.height ? Number(data.chart.height) : window.innerHeight;
		}	
     
		if (data.chart.widthDimType == "percentage")
		{
			var widthNormalized = data.chart.width ? window.innerWidth*Number(data.chart.width)/100 : window.innerWidth;
		}
		else
		{
			var widthNormalized = data.chart.width ? Number(data.chart.width) : window.innerWidth;
		}

		var m = [40, 40, 40, 100],
		w = widthNormalized - m[1] - m[3],
		h = heightNormalized - m[0] - m[2];

		/**
		 * Configuration that we get directly from the VM (needed for displaying
		 * the full (complete) chart when resizing. The biggest problems are
		 * legend's width and table's height parameters.
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		
		var legendWidth = widthNormalized*0.2;
		
		if(widthNormalized < 1000){
			legendWidth=widthNormalized*0.25;
		}
		
		var tableRowElements = data.table.numberOfRows;
		var tablePaginationHeight = data.table.heightPageNavigator;
		var divHeightAfterTable = data.table.afterTableDivHeight;	
		
		var showTable= data.chart.showTableParallel;
        
		/**
		 * Variable that take value of 10 as bottom padding of the 
		 * chart itself (axes) when there is no need for the table.
		 */
		var tableHeight=10;
        
        if(showTable)
        {
        	tableHeight=210;
        }
        
		/**
		 * This is the part when we set the width of the chart itself (the width between axes
		 * on edges of the chart).
		 * 
		 * @modifiedBy: danristo (danilo.ristovski@mht.net)
		 */
		var x = d3.scale.ordinal().domain(columns).rangePoints([0, w-legendWidth]),
		y = {};

		var line = d3.svg.line(),
		axis = d3.svg.axis().orient("left"),
		foreground;

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
			.append("div").attr("id","main"+randomId)
			.attr("classs","d3-container")
			.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
			.style("height",heightNormalized)
			.style("width",widthNormalized)
			.style("background-color",data.chart.style.backgroundColor)
			.style("font-family", data.chart.style.fontFamily)
			.style("font-size",  data.chart.style.fontSize)
			.style("font-style",data.chart.style.fontStyle)
			.style("font-weight",data.chart.style.fontWeight)
			.style("text-decoration",data.chart.style.textDecoration);
				
		// Set title
		d3.select("#main"+randomId).append("div")
		.style("color",data.title.style.color)
		.style("text-align",data.title.style.align)
		.style("font-family",data.title.style.fontFamily)
		.style("font-style",data.title.style.fontStyle)
		.style("font-weight",data.title.style.fontWeight)
		.style("text-decoration",data.title.style.textDecoration)
		.style("font-size",data.title.style.fontSize)
		.text(data.title.text);

		// Set subtitle
		d3.select("#main"+randomId).append("div")
		.style("color",data.subtitle.style.color)
		.style("text-align",data.subtitle.style.align)
		.style("font-family",data.subtitle.style.fontFamily)
		.style("font-style",data.subtitle.style.fontStyle)
		.style("font-weight",data.subtitle.style.fontWeight)
		.style("text-decoration",data.subtitle.style.textDecoration)
		.style("font-size",data.subtitle.style.fontSize)
		.text(data.subtitle.text);        
		
		var groupsHeight=groups.length*20+60;
		var svgHeight;
		if(groupsHeight > (h + m[0] + m[2])){
			svgHeight=groupsHeight;
		}else{
			svgHeight=h + m[0] + m[2];
		}
		
		/**
		 * Height of the button for clearing brush selections is determined
		 * by an empirical approach.
		 * @author Ana Tomic
		 */
		var buttonHeight = 20;
		
		/**
		 * Height of the DIV that holds the chart itself (axes).
		 * @author Ana Tomic
		 */
		var chartDivHeight = heightNormalized - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.2 - tableHeight - buttonHeight-10;
		
		/**
		 * Add brush clearing selections button to the main DIV.
		 * @author Ana Tomic
		 */
		d3.select("#main"+randomId).append("div").attr("id","clearButton"+randomId).style("padding-left",m[3]).style("padding-top",10).append("button").style("border-radius","5px").style("background-color","").text("Clear selections").on("click", function(){return clearSelection();});
		d3.select("#main"+randomId).append("div").attr("id","chart"+randomId).style("width",widthNormalized).style("height",chartDivHeight);
			
		var axesDivHeight = heightNormalized - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.4 - tableHeight - buttonHeight-10;
		
		var svg = d3.select("#chart"+randomId)
			.append("div").attr("class","d3chartclass")
				.style("float","left")
				.style("width",widthNormalized-legendWidth)
				// "...-180" for table height plus pagination height (150+30)
				// "...-20" for bottom padding of the pagination  
				.style("height", chartDivHeight)
				.append("svg:svg")
			//.style("font-size",18)
				.style("width", widthNormalized-legendWidth)
				// "...-180" for table height plus pagination height (150+30)
				// "...-20" for bottom padding of the pagination  
				.style("height", chartDivHeight)
			.append("svg:g")
			.attr("transform", "translate(" + m[3] + "," + m[0] + ")");

		columns.forEach(function(d){
			records.forEach(function(p) {p[d] = +p[d]; });

			/**
			 * This is the part when we set the height of the chart itself.
			 * 
			 * @modifiedBy: danristo (danilo.ristovski@mht.net)
			 */
			y[d] = d3.scale.linear()
			.domain(d3.extent(records, function(p) {return p[d]; }))
			// "...-180" for table height plus pagination height (150+30)
			// "...-m[0]" for translation of the chart from the top downwards
			// "...-20" for bottom padding of the pagination 
			// "...-20" for enabling text on labels (serie values) to be visible
			.range([axesDivHeight - m[0], 0]);

			y[d].brush = d3.svg.brush()
			.y(y[d])
			.on("brush", brush);

		});
		//counting height of svg to depend on groups number
		var gr=JSON.parse(data.chart.groups);
		
		var svgHeight=Number(removePixelsFromFontSize(data.legend.title.style.fontSize))+8+gr.length*(Number(removePixelsFromFontSize(data.legend.element.style.fontSize))+8)+30;
	   
		var legend=d3.select("#chart"+randomId).append("div")
		         .style("float","right")
		         .style("width",legendWidth)
		         // "...-180" for table height plus pagination height (150+30)
		         // "...-20" for bottom padding of the pagination 
		         .style("height", axesDivHeight)
		         .style("overflow","auto")
		       
		         .append("svg:svg")
		         //.style("font-size",10)
		         // "...-180" for table height plus pagination height (150+30)
		         // "...-20" for bottom padding of the pagination 
		         .attr("height",svgHeight)
		         .style("width",legendWidth-25)
		         .append("svg:g")
		         .attr("transform", "translate("+0 + "," + m[0] + ")");
		/**
		 * by default legend title is groupcolumn name, if custom title for legend is specified in designer
		 * it is used instead of default
		 */
		var legendTitle=groupcolumn;
		if(data.legend.title.text != ""){
			legendTitle=data.legend.title.text;
		}
		
		legend.append("svg:g")
		.attr("transform",  "translate("+ (30) +"," + 0 + ")" )
		//.style("height",Number(removePixelsFromFontSize(data.legend.title.style.fontSize))+5)
		.append("svg:text")
		.style("fill",data.title.style.color)
		.style("font-family",data.legend.title.style.fontFamily)
		.style("font-size",data.legend.title.style.fontSize)
		.style("font-style",data.legend.title.style.fontStyle)
		.style("font-weight",data.legend.title.style.fontWeight)
		.style("text-decoration",data.legend.title.style.textDecoration)
		.attr("x", 20)
		.attr("y",-10)
		.attr("dy", ".31em")
		.text(legendTitle);


		 legend.selectAll("g.legend")
		.data(groups)
		.enter().append("svg:g")
		.attr("class", "legend")
		//.attr("height",Number(removePixelsFromFontSize(data.legend.element.style.fontSize)))
		.attr("transform", function(d, i) {
			return "translate("+ 20 +"," + (i*(Number(removePixelsFromFontSize(data.legend.element.style.fontSize))+8) +Number(removePixelsFromFontSize(data.legend.title.style.fontSize))+8) + ")"; 
			});

		legend.selectAll("g.legend").append("svg:rect")
		//.attr("class", String)
		.style({"stroke":function(d) { return myColors(d); }, "stroke-width":"3px", "fill": function(d) { return myColors(d); }})
		.attr("x", 0)
		.attr("y", -6)
		.attr("width", 10)
		.attr("height", 10);

		legend.selectAll("g.legend").append("svg:text")
		.style("font-family",data.legend.element.style.fontFamily)
		.style("font-size",data.legend.element.style.fontSize)
		.style("font-style",data.legend.element.style.fontStyle)
		.style("font-weight",data.legend.element.style.fontWeight)
		.style("text-decoration",data.legend.element.style.textDecoration)
		.attr("x", 20)
		.attr("dy", ".31em")
		.text(function(d) {	
			return d; });

		//tooltip
		var tooltip=d3.select("#chart"+randomId)
		.append("div")
		.attr("class","tooltip")
		.style("opacity","0");
		
		d3.selectAll(".tooltip")
		.style("position","absolute")
		.style("text-align","center")
		.style("min-width",10)
		.style("max-width",1000)
		.style("min-height",10)
		.style("max-height",800)
		.style("padding",3)
		.style("font-size",data.tooltip.fontSize)
		.style("font-family",data.tooltip.fontFamily)
		.style("border",data.tooltip.border+"px solid black")	// @modifiedBy: danristo (danilo.ristovski@mht.net)
		.style("border-radius",data.tooltip.borderRadius+"px")
		.style("pointer-events","none");
		
		foreground = svg.append("svg:g")
		.attr("class","foreground")
		.style({"fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		.selectAll("path")
		.data(records)
		.enter().append("svg:path")
		.attr("visible","true")
		.attr("d", path)
		.style("stroke", function(d) {return myColors(d[groupcolumn])});		
		
		/**
		 * This part is responsible for determining if the TOOLTIP should 
		 * be displayed on the chart. Current criteria for this issue is:
		 * if the number of all records that can be displayed at once (the 
		 * maximum number of them) is bigger than 'maxNumOfRecsForDispTooltip'
		 * do not display TOOLTIP for the lines (records) when mouse over.
		 * Otherwise, display TOOLTIP whenever mouse is over particular line
		 * its value. 		 
		 * @authors Lazar Kostic (koslazar), Ana Tomic (atomic)
		 * @commentedBy Danilo Ristovski (danristo)
		 */
		
		/**
		 * 'maxNumOfRecsForDispTooltip'	-	the maximum number of records that chart
		 * 									displays within which we can have (display)
		 * 									the TOOLTIP (if number of records of the 
		 * 									chart is bigger than this value, TOOLTIP
		 * 									will not be rendered).
		 */
		var maxNumOfRecsForDispTooltip = 20;
		
		if (records.length <= maxNumOfRecsForDispTooltip){

			foreground.on("mouseover",function(d){
				
				if(allTableData){

					for (var i=0; i<allTableData.length; i++)
					{
						if(d[data.chart.tooltip] === allTableData[i][data.chart.tooltip])
						{				
							/**
							 * Convert the RGB background color of the tooltip to its HSL pair in order
							 * to determine its darkness, i.e. its light level. If the color of the
							 * background (that depends on the color of the line over which the mouse is
							 * positioned) is too dark, we will put the white text of the tooltip. Otherwise,
							 * the color of the text will be black. 
							 * 
							 * NOTE: The threshold can be changed. Value of 0.4 is set as an example and the
							 * consequence of empirical approach.
							 * 
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							var rgbColorForTooltipBckgnd = d3.rgb(myColors(d[groupcolumn]));
							var hslColorForTooltipBckgnd = 
								rgbToHsl(rgbColorForTooltipBckgnd.r, rgbColorForTooltipBckgnd.g, rgbColorForTooltipBckgnd.b);						
							var degreeOfLightInColor = hslColorForTooltipBckgnd[2];
							
							var darknessThreshold = Sbi.settings.chart.parallel.tooltip.darknessThreshold;
							
							var tooltipBckgndColor = null;
							
							if (degreeOfLightInColor < darknessThreshold)
							{
								tooltipBckgndColor = "#FFFFFF";
							}
							else
							{
								tooltipBckgndColor = "#000000";
							}
	
							tooltip.transition().duration(50).style("opacity","1");
							
							tooltip.style("background", myColors(d[groupcolumn]));
							
							var ttText = tooltip.text(d[data.chart.tooltip])	
								/**
								 * Set the color of the text, determined on the base of the level
								 * of light (darkness) of the tooltip background color.
								 * 
								 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
								 */
								.style("color",tooltipBckgndColor);
								//.style("text-shadow", "1px 1px 2px #FFFFFF")	// @addedBy: danristo (danilo.ristovski@mht.net)																
														
							/**							 * 
							 * Call the function that enables positioning of the 
							 * tooltip according to its dimensions.
							 */			
							var chartHeight = Number(heightNormalized);
							var chartWidth = Number(widthNormalized);
							
							positionTheTooltip(chartHeight,chartWidth,ttText);	
							
//							.style("left", (d3.event.pageX) + "px")     
//							.style("top", (d3.event.pageY - 25) + "px");
						}

					}

				}
				else{

					tooltip.transition().duration(50).style("opacity","1");
					tooltip.style("background",myColors(d[groupcolumn]));
					var ttText = tooltip.text(d[data.chart.tooltip]);
					
//					.style("left", (d3.event.pageX) + "px")     
//					.style("top", (d3.event.pageY - 25) + "px");
					
					/**	
					 * Call the function that enables positioning of the 
					 * tooltip according to its dimensions.
					 */			
					var chartHeight = Number(heightNormalized);
					var chartWidth = Number(widthNormalized);
					
					positionTheTooltip(chartHeight,chartWidth,ttText);	
				}

			})
			.on("mouseout",function(d){
				tooltip.transition()
				.duration(200)
				.style("opacity","0");
			});

		}
		

		var g = svg.selectAll(".column")
		.data(columns)
		.enter().append("svg:g")
		.attr("class", "column")
		.style({"font-family":data.chart.font})
		
		/**
		 * Added so to follow the main font style of the chart: e.g. if the font style
		 * of the chart (main font style) is 'underline', then all elements on the 
		 * PARALLEL chart should have this style (undeline) as the base.
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		.style("font-size",data.chart.style.fontSize)
		.style("font-style",data.chart.style.fontStyle)
		.style("font-weight",data.chart.style.fontWeight)
		.style("text-decoration",data.chart.style.textDecoration)
		
		.attr("transform", function(d) {return "translate(" + x(d) + ")"; })
		.call(d3.behavior.drag()
				.origin(function(d) { return {x: x(d)}; })
				.on("dragstart", dragstart)
				.on("drag", drag)
				.on("dragend", dragend));

		// Axis
		g.append("svg:g")
		.attr("class","axis")
		.each(function(d) { d3.select(this).call(axis.scale(y[d])); })
		.attr("fill",data.yAxis.labels.style.color)
		.style("font-family",data.yAxis.labels.style.fontFamily)
		.style("font-size",data.yAxis.labels.style.fontSize)
		.style("font-style",data.yAxis.labels.style.fontStyle)
		.style("font-weight",data.yAxis.labels.style.fontWeight)
		.style("text-decoration",data.yAxis.labels.style.textDecoration)
		.append("svg:text")
		.attr("text-anchor", "middle")
		.attr("y", -data.axis.axisColNamePadd)
		.attr("fill",data.xAxis.labels.style.color)
		.style("font-family",data.xAxis.labels.style.fontFamily)
		.style("font-size",data.xAxis.labels.style.fontSize)
		.style("font-style",data.xAxis.labels.style.fontStyle)
		.style("font-weight",data.xAxis.labels.style.fontWeight)
		.style("text-decoration",data.xAxis.labels.style.textDecoration)
		.text(String)
		.style({"cursor":"move"});

		g.selectAll(".axis line, .axis path").style({"fill":"none","stroke": data.axis.axisColor,"shape-rendering": "crispEdges"});

		// Add a brush for each axis.
		g.append("svg:g")
		 .attr("class","brush")
		.style({"fill-opacity":" .3","stroke":data.axis.brushColor,"shape-rendering":" crispEdges"})
		.each(function(d) { d3.select(this).call(y[d].brush); })
		.selectAll("rect")
		.attr("x", brushx)
		.attr("width", brushWidth);

	}

	else{

		// Set empty message
		d3.select("body").append("div")
		.style("color",data.emptymessage.style.color)
		.style("text-align",data.emptymessage.style.align)
		.style("font-family",data.emptymessage.style.fontFamily)
		.style("font-style",data.emptymessage.style.fontWeight)
		.style("font-size",data.emptymessage.style.fontSize)
		.text(data.emptymessage.text);	

	}
	
	//cross navigation
	foreground.on("click",clickLine);
	
	function clickLine(d){
		if(data.chart.isCockpit==true){
			if(data.chart.outcomingEventsEnabled){
			var paramethers=cockpitSelectionParamethers(d);
//			var selectParams={
//					categoryValue:paramethers.groupingCategoryValue		
//			};
//			console.log(paramethers);
			handleCockpitSelection(paramethers);
			}
		}else if(data.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
			paramethers=crossNavigationParamethers(d);
			var navigParams={
				crossNavigationDocumentName:data.crossNavigation.crossNavigationDocumentName,
				crossNavigationDocumentParams:data.crossNavigation.crossNavigationDocumentParams,
				categoryName:paramethers.categoryName,
				categoryValue:paramethers.categoryValue,
				serieName:paramethers.serieName,
				serieValue:paramethers.serieValue,
				groupingCategoryName:paramethers.groupingCategoryName,
				groupingCategoryValue:paramethers.groupingCategoryValue
			};
			handleCrossNavigationTo(navigParams);
		}
	   
	}
	
	function crossNavigationParamethers(d){
		 var params={
				    "categoryName" : null,
					"categoryValue":null,
					"serieName":null,
					"serieValue":null,
					"groupingCategoryName":null,
					"groupingCategoryValue":null
				   };	
				   var category=data.chart.tooltip;
				   params.categoryName=category;
				   params.categoryValue=d[category];
				   var groupCategory=data.chart.group;
				   params.groupingCategoryName=groupCategory;
				   params.groupingCategoryValue=d[groupCategory];
				   
				   return params;
	}
	
	function cockpitSelectionParamethers(d){
		var params={};
		 var category=data.chart.tooltip;
		   
		  // params.categoryName=category;
		  // params.categoryValue=d[category];
		   var groupCategory=data.chart.group;
		   //params.groupingCategoryName=groupCategory;
		   //params.groupingCategoryValue=d[groupCategory];
		   
		   params[groupCategory]=d[groupCategory];
		   if(category != ''){
			   
			params[category]=d[category];   
		   }
		
		return params;
		
	}
	
	// TABLE
	
	if(showTable){
		
		var initialTableData=records;
		
	
		var allTableData=initialTableData; // all records or filtered records
		var currentTableData=allTableData.slice(0,5); // up to 5 recoords
		var firstDisplayed=1;
		var lastDisplayed=0;
		
		if(allTableData.length > 5){
			lastDisplayed=5;
		}else{
			lastDisplayed=allTableData.length;
		}
	
		var tableDiv = d3.select("#main"+randomId)
							.append("div").attr("id","tableDiv")
							.style("width",widthNormalized)						
							.style("padding-bottom",10)
							.style("padding-top",30);
		
		var table = tableDiv.append("div").attr("id","tDiv"+randomId).attr("align","center")
		                .attr("width", widthNormalized)
		                .append("table")
						.style("width", widthNormalized)
						
						/**
						 * The next style parameter setting allow us to reset font stylization provided 
						 * for the whole chart (independency of the table element over whole chart). 
						 * This way we can e.g. reset the text decoration (that the whole chart has) and
						 * provide that table does not have the one. This is important since (for now)
						 * table gets font customization from the legend's elements.
						 * 
						 * @author: danristo (danilo.ristovski@mht.net)
						 */ 
						//.style("display", "block") 
						
						/**
						 * For now, table text elements will use the font customization provided for
						 * legend's elements.
						 * 
						 * @author: danristo (danilo.ristovski@mht.net)
						 */
						.style("font-family", data.chart.style.fontFamily)
						.style("font-size", data.chart.style.fontSize)
						.style("font-style", data.chart.style.fontStyle)
						.style("font-weight", data.chart.style.fontWeight)
						.style("text-decoration", data.chart.style.textDecoration)
						.style("padding-right",25)
						.style("padding-left",m[3]);
		
		var paginationBar = tableDiv.append("div").attr("id","pBar"+randomId)
		                        .attr("align","center")
								//.style("padding-left",w/2+m[3]/2-150)
								.style("padding-top",10)
		                        .style("padding-left",m[3])
		                        .style("padding-right",25);
		
		var prevButton = paginationBar.append("button")
							.style("border-radius","5px")
							.style("background-color","")
							.text("<< Prev")
							.on("click", function(){ return showPrev(); });
		
		var paginationText = paginationBar.append("label")
								.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
								/**
								 * The next style parameter setting allow us to reset font stylization provided 
								 * for the whole chart (independency of the table element over whole chart). 
								 * This way we can e.g. reset the text decoration (that the whole chart has) and
								 * provide that table does not have the one. This is important since (for now)
								 * table gets font customization from the legend's elements.
								 * 
								 * @author: danristo (danilo.ristovski@mht.net)
								 */ 
								.style("display", "inline-block") 
								
								/**
								 * For now, table text elements will use the font customization provided for
								 * legend's elements.
								 * 
								 * @author: danristo (danilo.ristovski@mht.net)
								 */
								.style("font-family",data.legend.element.style.fontFamily)
								.style("font-size",data.legend.element.style.fontSize)
								.style("font-style",data.legend.element.style.fontStyle)
								.style("font-weight",data.legend.element.style.fontWeight)
								.style("text-decoration",data.legend.element.style.textDecoration);
		
		var nextButton = paginationBar.append("button")
							.style("border-radius","5px")
							.style("background-color","")
							.text("Next >>")
							.on("click", function(){ return showNext(); });
	
		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}
	
		if(lastDisplayed===allTableData.length){
			nextButton.attr("disabled","true");
		}
	
		//columns for table
		var tableColumns=[];
		tableColumns.push(groupcolumn);
		if(!(groupcolumn===data.chart.tooltip)){
		tableColumns.push(data.chart.tooltip);
		}
		tableColumns=tableColumns.concat(columns);
	
		
		//table header
		table.append("thead")
		      .style("background-color","silver") 
		      .style("border","1px solid black")
		      .attr("border-collapse","collapse")
		     .append("tr")
		     .style("width", widthNormalized-legendWidth)
		     .style("height","30px")
		     .selectAll("th")
		     .data(tableColumns).enter()
		     .append("th")
		     .text(function(d){return d;});
		
		//table body
		table.append("tbody")
		     .selectAll("tr")
		.data(currentTableData)
		     .enter()
		     .append("tr")
		     .style("width", widthNormalized-legendWidth)
		     .style("background-color",function(d,i){
		    	 if(i%2==1)return "lightgray";
		     })
		     .attr("class","tdata")
		.on("mouseover",function(d){
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);})
		     .on("mouseout",function(d){
			d3.select(this).style("outline","none");
		    	foreground.style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		 		.style({"stroke":function(d) { return myColors(d[groupcolumn]);}});
	           })
		     .selectAll("td")
		     .data(function(row){
		    	 return tableColumns.map(function(column) {
		                return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column] };
		            });
		     }).enter()
		       .append("td")
		.on("click",function(d){return filterTable(d,allTableData);})
		       .text(function(d){ return formatTableCell(d) })
		       .style("text-align","center");
		       
	}	
	
	function formatTableCell(d){
		  var text='';
   	   if(d.prefix) {
   		   text+=d.prefix +" ";
   	   }
   	   if(d.precision){
   		   text+=Number(d.value).toFixed(d.precision);
   		   
   	   }else{
   		   text+=d.value;
   	   }
   	   if(d.postfix){
   		   text+=" "+d.postfix;
   	   }
   	   return text
		
	}
	
	function dragstart(d) {
		i = columns.indexOf(d);
	}

	function drag(d) {
		x.range()[i] = d3.event.x;
		columns.sort(function(a, b) { return x(a) - x(b); });
		g.attr("transform", function(d) { return "translate(" + x(d) + ")"; });
		foreground.attr("d", path);
	}

	function dragend(d) {
		x.domain(columns).rangePoints([0, w-legendWidth]);
		var t = d3.transition().duration(500);
		t.selectAll(".column").attr("transform", function(d) { 
			return "translate(" + x(d) + ")"; });
		t.selectAll(".foreground path").attr("d", path);

	}

	// Returns the path for a given data point.
	function path(d) {
		return line(columns.map(function(p) { 
			return [x(p), y[p](d[p])]; }));
	}

	// Handles a brush event, toggling the display of foreground lines.
	function brush() {
		var actives = columns.filter(function(p) { return !y[p].brush.empty(); }),
		extents = actives.map(function(p) { return y[p].brush.extent(); });
		foreground.classed("fade", function(d) {

			return !actives.every(function(p, i) {

				return extents[i][0] <= d[p] && d[p] <= extents[i][1];
			});
		});

		foreground.classed("notfade", function(d) {
			return actives.every(function(p, i) {
				return extents[i][0] <= d[p] && d[p] <= extents[i][1];
			})
		});

		var allRows=records;
		
		filteredRows=allRows.filter(function(d) {
         
			return actives.every(function(p, i) {

				return extents[i][0] <= d[p] && d[p] <= extents[i][1];
			});
		});
		
		if(showTable){
		nextButton.attr("disabled",null);
		prevButton.attr("disabled",null);
		
		
		allTableData=filteredRows;
		
		currentTableData=allTableData.slice(0,5);
		firstDisplayed=1;
		if(allTableData.length > 5){
			lastDisplayed=5;
		}else{
			lastDisplayed=allTableData.length;
		}

		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}

		if(lastDisplayed===allTableData.length){
			nextButton.attr("disabled","true");
		}
		
		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
					/**
					 * The next style parameter setting allow us to reset font stylization provided 
					 * for the whole chart (independency of the table element over whole chart). 
					 * This way we can e.g. reset the text decoration (that the whole chart has) and
					 * provide that table does not have the one. This is important since (for now)
					 * table gets font customization from the legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */ 
					.style("display", "inline-block") 
					
					/**
					 * For now, table text elements will use the font customization provided for
					 * legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					.style("font-family",data.legend.element.style.fontFamily)
					.style("font-size",data.legend.element.style.fontSize)
					.style("font-style",data.legend.element.style.fontStyle)
					.style("font-weight",data.legend.element.style.fontWeight)
					.style("text-decoration",data.legend.element.style.textDecoration);
		
		var dummy=[];
		d3.select("table").select("tbody").selectAll("tr").data(dummy).exit().remove();
		
		 d3.select("table")
		   .select("tbody")
		   .selectAll("tr")
		.data(currentTableData)
		   .enter()
		   .append("tr")
	       .style("background-color",function(d,i){
	    	 if(i%2==1)return "lightgray";
	        })
		    .attr("class","tdata") 
		.on("mouseover",function(d){ 
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);
			})
	     .on("mouseout",function(d){
			d3.select(this).style("outline","none");
	    	d3.selectAll(".notfade").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
	 		.style({"stroke":function(d) { return myColors(d[groupcolumn]);}});
           })
	     .selectAll("td")
	     .data(function(row){
	    	 return tableColumns.map(function(column) {
	                return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column]};
	            });
	     }).enter()
	       .append("td")
		.on("click",function(d){return filterTable(d,filteredRows);})
	       .text(function(d){return formatTableCell(d)})
		   .style("text-align","center");
		}
		
		d3.selectAll(".fade").style({"stroke": "#000","stroke-opacity": ".02"}); 
		d3.selectAll(".notfade").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});

		
	}

	function selectSingleLine(selectedRow){
		foreground.attr("visible", function(d){
          return (d===selectedRow)?"true":"false";
      });
		
	

		d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
		d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});
	}

	function filterTable(selectedCell,coollectionToFilter){
		
		nextButton.attr("disabled",null);
		prevButton.attr("disabled",null);

		var filteredData=coollectionToFilter.filter(function(d){return d[selectedCell.column]===selectedCell.value;});		

		allTableData=filteredData;
		currentTableData=allTableData.slice(0,5);
		firstDisplayed=1;
		if(allTableData.length > 5){
			lastDisplayed=5;
		}else{
			lastDisplayed=allTableData.length;
		}
		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}

		if(lastDisplayed===allTableData.length){
			nextButton.attr("disabled","true");
		}
		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
					/**
					 * The next style parameter setting allow us to reset font stylization provided 
					 * for the whole chart (independency of the table element over whole chart). 
					 * This way we can e.g. reset the text decoration (that the whole chart has) and
					 * provide that table does not have the one. This is important since (for now)
					 * table gets font customization from the legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */ 
					.style("display", "inline-block") 
					
					/**
					 * For now, table text elements will use the font customization provided for
					 * legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					.style("font-family",data.legend.element.style.fontFamily)
					.style("font-size",data.legend.element.style.fontSize)
					.style("font-style",data.legend.element.style.fontStyle)
					.style("font-weight",data.legend.element.style.fontWeight)
					.style("text-decoration",data.legend.element.style.textDecoration);

		var dummy=[];
		d3.select("table").select("tbody").selectAll("tr").data(dummy).exit().remove();

		d3.select("table")
		.select("tbody")
		.selectAll("tr")
		.data(currentTableData)
		.enter()
		.append("tr")
		.style("background-color",function(d,i){
			if(i%2==1)return "lightgray";
		})
		.attr("class","tdata") 
		.on("mouseover",function(d){
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);
			})
		.on("mouseout",function(d){
  
			d3.select(this).style("outline","none");
			foreground.attr("visible", function(d){
				return (d[selectedCell.column]===selectedCell.value)?"true":"false";
			});

			d3.selectAll(".fade").attr("visible","false");
			d3.selectAll(".notfade").attr("visible", function(d){
				return (d[selectedCell.column]===selectedCell.value)?"true":"false";
			});

			d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
			d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
			.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});

			//foreground.selectAll(".fade").style({"stroke": "#000","stroke-opacity": ".02"});
		})
		.selectAll("td")
		.data(function(row){
			return tableColumns.map(function(column) {
				return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column]};
			});
		}).enter()
		.append("td")
		.text(function(d){return formatTableCell(d)})
		.style("text-align","center");



	}

	function updateTable(){
		var dummy=[];
		d3.select("table").select("tbody").selectAll("tr").data(dummy).exit().remove();

		d3.select("table")
		.select("tbody")
		.selectAll("tr")
		.data(currentTableData)
		.enter()
		.append("tr")
		.style("background-color",function(d,i){
			if(i%2==1)return "lightgray";
		})
		.attr("class","tdata") 
		.on("mouseover",function(d){ 			
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);})
		.on("mouseout",function(){
			d3.select(this).style("outline","none");
			foreground.attr("visible",function(d){
				return (allTableData.indexOf(d)!=-1)?"true":"false";
			});

			d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
			d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
			.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});


		})
		.selectAll("td")
		.data(function(row){
			return tableColumns.map(function(column) {
				return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column]};
			});
		}).enter()
		.append("td")
		.on("click",function(d){return filterTable(d,allTableData);})
		.text(function(d){return formatTableCell(d)})
		.style("text-align","center");

		foreground.attr("visible",function(d){
			return (allTableData.indexOf(d)!=-1)?"true":"false";
		});

	d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
	d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
	.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});
	}

	function showNext(){
		prevButton.attr("disabled",null);
		firstDisplayed=firstDisplayed+5;
		lastDisplayed=lastDisplayed+5;
		if(lastDisplayed>allTableData.length){
			lastDisplayed=allTableData.length;
		}



		currentTableData=[];
		currentTableData=allTableData.slice(firstDisplayed-1,lastDisplayed);



		if(lastDisplayed === allTableData.length){
			nextButton.attr("disabled","true");
		}



		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
			/**
			 * The next style parameter setting allow us to reset font stylization provided 
			 * for the whole chart (independency of the table element over whole chart). 
			 * This way we can e.g. reset the text decoration (that the whole chart has) and
			 * provide that table does not have the one. This is important since (for now)
			 * table gets font customization from the legend's elements.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */ 
			.style("display", "inline-block") 
			
			/**
			 * For now, table text elements will use the font customization provided for
			 * legend's elements.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			.style("font-family",data.legend.element.style.fontFamily)
			.style("font-size",data.legend.element.style.fontSize)
			.style("font-style",data.legend.element.style.fontStyle)
			.style("font-weight",data.legend.element.style.fontWeight)
			.style("text-decoration",data.legend.element.style.textDecoration);
		
		updateTable();	


	}

	function showPrev(){
		nextButton.attr("disabled",null);
		firstDisplayed=firstDisplayed-5;
		if(lastDisplayed===allTableData.length){
			if(allTableData.length%5!=0){
				lastDisplayed=lastDisplayed-(allTableData.length%5);
			}else{
				lastDisplayed=lastDisplayed-5;	
			}
		}
		else{
			lastDisplayed=lastDisplayed-5;
		}



		currentTableData=[];
		currentTableData=allTableData.slice(firstDisplayed-1,lastDisplayed);

		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}

		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
				/**
				 * The next style parameter setting allow us to reset font stylization provided 
				 * for the whole chart (independency of the table element over whole chart). 
				 * This way we can e.g. reset the text decoration (that the whole chart has) and
				 * provide that table does not have the one. This is important since (for now)
				 * table gets font customization from the legend's elements.
				 * 
				 * @author: danristo (danilo.ristovski@mht.net)
				 */ 
				.style("display", "inline-block") 
				
				/**
				 * For now, table text elements will use the font customization provided for
				 * legend's elements.
				 * 
				 * @author: danristo (danilo.ristovski@mht.net)
				 */
				.style("font-family",data.legend.element.style.fontFamily)
				.style("font-size",data.legend.element.style.fontSize)
				.style("font-style",data.legend.element.style.fontStyle)
				.style("font-weight",data.legend.element.style.fontWeight)
				.style("text-decoration",data.legend.element.style.textDecoration);		
		
		updateTable();	

	}
	
	
	function clearSelection(){
		columns.filter(function(p) { return y[p].brush.clear(); });
		brush();
		g.selectAll(".brush")
		.style({"fill-opacity":" .3","stroke":data.axis.brushColor,"shape-rendering":" crispEdges"})
		.each(function(d) { d3.select(this).call(y[d].brush); })
		.selectAll("rect")
		.attr("x", brushx)
		.attr("width", brushWidth);
	}
}
	