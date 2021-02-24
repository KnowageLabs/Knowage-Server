/**
 * The rendering function for the PARALLEL chart.
 * @param data JSON containing data (parameters) about the chart.
 * @param locale Information about the locale (language). Needed for the formatting of the series values (data labels and tooltips).
 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
function renderParallelChart(data,panel,handleCockpitSelection,chartEngineSettings,locale,handleCrossNavigationTo){

	var records = data.data[0];
	var chartEngineSharedSettings = chartEngineSettings;
	if(records.length>0){
		panel.innerText = '';
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

		var group = JSON.parse(data.chart.groups);
		var column = JSON.parse(data.chart.serie);

		var groups = [];

		var columns = [];

		var precisions={};
		var prefixes={};
		var postfixes={};

		/**
		 * The scaling factor of the current series item can be empty (no scaling - pure (original) value) or "k" (kilo), "M" (mega),
		 * "G" (giga), "T" (tera), "P" (peta), "E" (exa). That means we will scale our values according to this factor and display
		 * these abbreviations (number suffix) along with the scaled number. [JIRA 1060 and 1061]
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var scaleFactor = {};

		for (var i = 0; i< group.length; i++){

			groups.push(group[i][i]);
		}

		for (var i = 0; i<column.length;i++){

			columns.push(column[i][i]);
			precisions[column[i][i]]=column[i]["precision"];
			prefixes[column[i][i]]=column[i]["prefix"];
			postfixes[column[i][i]]=column[i]["postfix"];
			scaleFactor[column[i][i]] = column[i]["scaleFactor"];

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

		var colorsResponseDec = JSON.parse(colorsResponse);

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
	    	var heightNormalized = data.chart.height ? panel.offsetHeight*Number(data.chart.height)/100 : panel.offsetHeight;
		}
		else
		{
			var heightNormalized = data.chart.height ? Number(data.chart.height) : panel.offsetHeight;
		}

		if (data.chart.widthDimType == "percentage")
		{
			var widthNormalized = data.chart.width ? panel.offsetWidth*Number(data.chart.width)/100 : panel.offsetWidth;
		}
		else
		{
			var widthNormalized = data.chart.width ? Number(data.chart.width) : panel.offsetWidth;
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

		/**
		 * Create an invisible HTML form that will sit on the page where the chart (in this case, the PARALLEL) is rendered.
		 * This form will serve us as a media through which the data and customization for the rendered chart will be sent
		 * towards the Highcharts exporting service that will take the HTML of the PARALLEL chart, render it and take a snapshot
		 * that will be sent back towards the client (our browser) in a proper format (PDF or PNG) and downloaded to the local
		 * machine.
		 *
		 * This way, when the user clicks on the export option for the rendered chart, the JS code ("chartExecutionController.js")
		 * that fills the form (that we set here as a blank structure) will eventually submit it towards the Highcharts export
		 * service. The result is the exported chart. This code will catch the form by the ID that we set here.
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		d3.select(panel)
			.append("form").attr("id","export-chart-form").style("margin","0px")
			.append("input").attr("type","hidden").attr("name","options")
			.append("input").attr("type","hidden").attr("name","content")
			.append("input").attr("type","hidden").attr("name","type")
			.append("input").attr("type","hidden").attr("name","width")
			.append("input").attr("type","hidden").attr("name","constr")
			.append("input").attr("type","hidden").attr("name","async")
			.append("input").attr("type","hidden").attr("name","chartHeight")
			.append("input").attr("type","hidden").attr("name","chartWidth");

		/**
	     * Correction for width and height if the other one is fixed and bigger than the panel dimension value.
	     * E.g. if the height of the chart is higher than the height of the panel height, the width needs to
	     * be corrected, since the vertical scrollbar appears. Without this correction, the chart will be cut
	     * and not entirely presented, and the horizontal scrollbar will be present as well (and it should not
	     * be, since the width should just expand as much as the panel is wide).
	     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	     */
	    var widthCorrection = 0, heightCorrection = 0, overflowXHidden = "auto", overflowYHidden = "auto";

	    if (!data.chart.isCockpit && heightNormalized > panel.offsetHeight && widthNormalized==panel.offsetWidth) {
	    	widthCorrection = 16;
	    	overflowXHidden = "hidden";
	    }

	    if (!data.chart.isCockpit && widthNormalized > panel.offsetWidth && heightNormalized==panel.offsetHeight) {
	    	heightCorrection = 16;
	    	overflowYHidden = "hidden";
	    }

		/**
		 * The body inside of which the chart will be rendered.
		 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		d3.select(panel)
			.style("overflow-x",overflowXHidden)
			.style("overflow-y",overflowYHidden)
			.append("div").attr("id","main"+randomId)
			.attr("class","d3-container")
			.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
			.style("height",heightNormalized-heightCorrection)
			.style("width",widthNormalized-widthCorrection)
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
		d3.select("#main"+randomId).append("div").attr("id","chart"+randomId).style("width",widthNormalized-widthCorrection).style("height",chartDivHeight);

		var axesDivHeight = heightNormalized - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.4 - tableHeight - buttonHeight-20;

		var svg = d3.select("#chart"+randomId)
			.append("div").attr("class","d3chartclass")
				.style("float","left")
				.style("width",widthNormalized-legendWidth-widthCorrection)
				// "...-180" for table height plus pagination height (150+30)
				// "...-20" for bottom padding of the pagination
				.style("height", chartDivHeight)
				.append("svg:svg")
			//.style("font-size",18)
				.style("width", widthNormalized-legendWidth-widthCorrection)
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

		d3.select(panel).selectAll(".tooltip")
		.style("position","fixed")
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
		var maxNumOfRecsForDispTooltip = data.tooltip.maxNumberOfRecords;

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

							// (migration from the ExtJS execution to AngularJS)
							var darknessThreshold = chartEngineSharedSettings.parallel.tooltip.darknessThreshold;

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
							var chartHeight = Number(heightNormalized-heightCorrection);
							var chartWidth = Number(widthNormalized-widthCorrection);

							positionTheTooltip(d3.event.pageX + 10,d3.event.pageY +10 ,ttText);

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
					var chartHeight = Number(heightNormalized-heightCorrection);
					var chartWidth = Number(widthNormalized-widthCorrection);

					positionTheTooltip(d3.event.pageX +10 ,d3.event.pageY+ 10,ttText);
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
        if(data && data.yAxis && data.yAxis.labels  && data.yAxis.labels.style) {
           if (!data.yAxis.labels.style.rotation || isNaN(data.yAxis.labels.style.rotation))
           data.yAxis.labels.style.rotation = 0;
         }

		g.append("svg:g")
		.attr("class","axis")
		.each(function(d) { d3.select(this).call(axis.scale(y[d])); })
		.attr("fill",data.yAxis.labelValues.style.color)
		.style("font-family",data.yAxis.labelValues.style.fontFamily)
		.style("font-size",data.yAxis.labelValues.style.fontSize)
		.style("font-style",data.yAxis.labelValues.style.fontStyle)
		.style("font-weight",data.yAxis.labelValues.style.fontWeight)
		.style("text-decoration",data.yAxis.labelValues.style.textDecoration).append("svg:text")
		.attr("transform", "rotate("+data.yAxis.labels.style.rotation+")")
		.attr("text-anchor", "middle")
		.attr("y", -data.axis.axisColNamePadd)
		.attr("fill",data.yAxis.labels.style.color)
		.style("font-family",data.yAxis.labels.style.fontFamily)
		.style("font-size",data.yAxis.labels.style.fontSize)
		.style("font-style",data.yAxis.labels.style.fontStyle)
		.style("font-weight",data.yAxis.labels.style.fontWeight)
		.style("text-decoration",data.yAxis.labels.style.textDecoration)
		.text(String)
		.style({"cursor":"move"});


		g.selectAll(".axis line, .axis path").style({"fill":"none","stroke": data.axis.axisColor,"shape-rendering": "crispEdges"});

		// Add a brush for each axis.
		g.append("svg:g")
		 .attr("class","brush")
		.style({"fill-opacity":" .3","fill":data.axis.brushColor,"shape-rendering":" crispEdges"})
		.each(function(d) { d3.select(this).call(y[d].brush); })
		.selectAll("rect")
		.attr("x", brushx)
		.attr("width", brushWidth);

	}

	else{
		var emptyMsgFontSize = parseInt(data.emptymessage.style.fontSize);
		//var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
		var emptyMsgTotal = emptyMsgFontSize;

		var emptyMsgAlignment = null;
		if(data.emptymessage.style.align == "left") {
			emptyMsgAlignment = "flex-start";
		} else if (data.emptymessage.style.align == "right") {
			emptyMsgAlignment = "flex-end";
		} else {
			emptyMsgAlignment = "center";
		}

		// Set empty message
		d3.select(panel)
			.style("color",data.emptymessage.style.color)
			.style("display","flex")
			.style("align-items","center")
			.style("justify-content",emptyMsgAlignment)
    		.style("font-family",data.emptymessage.style.fontFamily)
    		.style("font-style",data.emptymessage.style.fontStyle)
    		.style("font-weight",data.emptymessage.style.fontWeight)
    		.style("text-decoration",data.emptymessage.style.textDecoration)
    		.style("font-size",emptyMsgFontSize)
		.text(data.emptymessage.text);

	}

	//cross navigation
	foreground.on("click",clickLine);

	function clickLine(d){

		if(data.chart.isCockpit==true){
			if(data.chart.outcomingEventsEnabled){
			var paramethers = crossNavigationParamethers(d);
			var selectParam_cross={
					categoryName:paramethers.categoryName,
					categoryValue:paramethers.categoryValue,
					serieName:paramethers.serieName,
					serieValue:paramethers.serieValue,
					groupingCategoryName:paramethers.groupingCategoryName,
					groupingCategoryValue:paramethers.groupingCategoryValue
			};


			var selectParam=cockpitSelectionParamethers(d);
			selectParam.selectParam_cross = selectParam_cross;
			handleCockpitSelection(selectParam);
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
		/**
		 * Implementation for the new Cross Navigation Definition interface.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		else {

			/**
			 * Collect all needed data for the cross-navigation (all output parameters for the PARALLEL chart document) and
			 * forward them towards the cross-navigation handler. The data represents all the output parameter categories and
			 * grouping categories value and name pairs.
			 *
			 * NOTE: output parameters as series items are not taken into count at this time instance.
			 */
			var navigParams = crossNavigationParamethers(d);
			handleCrossNavigationTo(navigParams);

		}

	}

	function crossNavigationParamethers(d) {

		var params = {

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
							.style("width",widthNormalized-widthCorrection)
							.style("padding-bottom",10)
							.style("padding-top",30);

		var table = tableDiv.append("div").attr("id","tDiv"+randomId).attr("align","center")
		                .attr("width", widthNormalized-widthCorrection)
		                .append("table")
						.style("width", widthNormalized-widthCorrection)

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
		     .style("width", widthNormalized-legendWidth-widthCorrection)
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
		     .style("width", widthNormalized-legendWidth-widthCorrection)
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
		    	 		// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) [JIRA 1060 and 1061]
		                return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column], scaleFactor:scaleFactor[column] };
		            });
		     }).enter()
		       .append("td")
		.on("click",function(d){return filterTable(d,allTableData);})
		       .text(function(d){ return formatTableCell(d) })
		       .style("text-align","center");

	}

	function formatTableCell(d) {

		var text='';

		/**
		 * The displaying of the numeric (series) values in the table of the PARALLEL chart is redefined, so now it considers the
		 * precision, prefix, suffix (postfix), thousands separator, formatting localization and scale factor. [JIRA 1060 and 1061]
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var scaleFactor = d.scaleFactor;
		var prefix = d.prefix;
		var precision = d.precision;
		var postfix = d.postfix;
		var value = d.value;

//		var seriesItemPrecisionDefined = precision!=null && precision!="" && (precision+"")!="0";

		/**
		 * If these parameters are undefined, it means that we are dealing with the category value, not the series item value,
		 * because the category does not posses any of them. In that case we only have a "value" parameter that represents the
		 * category value.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
   	   	if (scaleFactor==undefined && postfix==undefined && precision==undefined && prefix==undefined) {
   	   		text += value;
   	   	}
   	   	else {

	   	   	if (prefix) {
	 		   text += prefix +" ";
	 	   	}

	   	   	/*
   	    		The scaling factor of the current series item can be empty (no scaling - pure (original) value) or "k" (kilo), "M" (mega),
   	    		"G" (giga), "T" (tera), "P" (peta), "E" (exa). That means we will scale our values according to this factor and display
   	    		these abbreviations (number suffix) along with the scaled number. Apart form the scaling factor, the thousands separator
   	    		is included into the formatting of the number that is going to be displayed, as well as precision. [JIRA 1060 and 1061]
	   	    	@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
   			*/
	   		switch(scaleFactor.toUpperCase()) {

	   	  		case "EMPTY":

	   	  			/* No selection is provided for the number to be displayed as the data label (pure value). */
	//   	  			if (value%1==0) {
	//   	  				text += (value).toLocaleString(locale);
	//   	  				text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//   	  			}
	//   	  			else {
	//   	  				text += (value).toFixed(precision).toLocaleString(locale);
	//   	  			}

	   	  			text += value.toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
	   	  			break;

	   	  		case "K":

	//   	  			if (value/Math.pow(10,3)%1==0) {
	//   	  				text += Number(value/Math.pow(10,3)).toLocaleString(locale);
	//  					text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//   	  			}
	//   	  			else {
	//   	  				text += (value/Math.pow(10,3)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
	//   	  			}

	   	  			text += (value/Math.pow(10,3)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
	   	  			text += "k";
	   	  			break;

	  			case "M":

	//	   	  			if (value/Math.pow(10,6)%1==0) {
	//	   	  				text += Number(value/Math.pow(10,6)).toLocaleString(locale);
	//	   	  				text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//	   	  			}
	//	   	  			else {
	//	   	  				text += (value/Math.pow(10,6)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	//	   	  			}

	  				text += (value/Math.pow(10,6)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
	   	  			text += "M";
	   	  			break;

	  			case "G":

	//	   	  			if (value/Math.pow(10,9)%1==0) {
	//	   	  				text += Number(value/Math.pow(10,9)).toLocaleString(locale);
	//	   	  				text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//	   	  			}
	//	   	  			else {
	//	   	  				text += (value/Math.pow(10,9)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	//	   	  			}

	   	  			text += (value/Math.pow(10,9)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
	   	  			text += "G";
	   	  			break;

				case "T":

	//					if (value/Math.pow(10,12)%1==0) {
	//						text += Number(value/Math.pow(10,12)).toLocaleString(locale);
	//						text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//	   	  			}
	//	   	  			else {
	//	   	  				text += (value/Math.pow(10,12)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	//	   	  			}
	//
					text += (value/Math.pow(10,12)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
					text += "T";
	   	  			break;

				case "P":

	//	   	  			if (value/Math.pow(10,15)%1==0) {
	//	   	  				text += Number(value/Math.pow(10,15)).toLocaleString(locale);
	//	   	  				text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//	   	  			}
	//	   	  			else {
	//	   	  				text += (value/Math.pow(10,15)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	//	   	  			}

	   	  			text += (value/Math.pow(10,15)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	   	  			text += "P";
	   	  			break;

				case "E":

	//   					if (value/Math.pow(10,18)%1==0) {
	//   						text += Number(value/Math.pow(10,18)).toLocaleString(locale);
	//   						text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//   					}
	//	   	  			else {
	//	   	  				text += (value/Math.pow(10,18)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	//	   	  			}

	   					text += (value/Math.pow(10,18)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	   					text += "E";
	   					break;

				default:

	   					/* The same as for the case when user picked "no selection" - in case when the chart
	   					template does not contain the scale factor for current serie */
	//   					if (value%1==0) {
	//   						text += (value).toLocaleString(locale);
	//   						text += seriesItemPrecisionDefined ?  "." + "0".repeat(precision) : "";
	//	   	  			}
	//	   	  			else {
	//	   	  				text += (value).toFixed(precision).toLocaleString(locale);
	//	   	  			}

					text += value.toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
	   	  			break;

   	  		}

   			text += (postfix!="" ? " " : "") + postfix;

   	   	}

   	   	return text;

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
	    	d3.select(panel).selectAll(".notfade").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
	 		.style({"stroke":function(d) { return myColors(d[groupcolumn]);}});
           })
	     .selectAll("td")
	     .data(function(row){
	    	 return tableColumns.map(function(column) {
	    	 		// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) [JIRA 1060 and 1061]
	                return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column], scaleFactor:scaleFactor[column]};
	            });
	     }).enter()
	       .append("td")
		.on("click",function(d){return filterTable(d,filteredRows);})
	       .text(function(d){return formatTableCell(d)})
		   .style("text-align","center");
		}

		d3.select(panel).selectAll(".fade").style({"stroke": "#000","stroke-opacity": ".02"});
		d3.select(panel).selectAll(".notfade").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
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

			d3.select(panel).selectAll(".fade").attr("visible","false");
			d3.select(panel).selectAll(".notfade").attr("visible", function(d){
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
				// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) [JIRA 1060 and 1061]
				return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column], scaleFactor:scaleFactor[column]};
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
				// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) [JIRA 1060 and 1061]
				return {column: column, value: row[column], prefix: prefixes[column], postfix:postfixes[column], precision:precisions[column], scaleFactor:scaleFactor[column]};
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
