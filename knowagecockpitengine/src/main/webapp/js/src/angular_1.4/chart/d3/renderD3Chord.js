/*In this file is used code that is distribuited uner the license:
Released under the GNU General Public License, version 3.*/

/**
 * The rendering function for the PARALLEL chart.
 * @param jsonData JSON containing data (parameters) about the chart.
 * @param locale Information about the locale (language). Needed for the formatting of the series values (data labels and tooltips).
 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
function renderChordChart(jsonData,panel,handleCockpitSelection,locale,handleCrossNavigationTo)
{

	/**
	 *  'opacityMouseOver' - value for the opacity of the item (row) that is covered by the mouse pointer and all the items
	 *  that are linked (connected) to that row (item)
	 *
	 *  'opacityMouseOutAndDefault' - value of the opacity of all graphical items (arcs and stripes) when non of the items
	 *  (rows) is selected by the mouse pointer or when the mouse pointer leaves an item
	 */
	 // TODO: Maybe customizable ???
	var opacityMouseOutAndDefault = 0.6;
	var opacityMouseOver = 0.1;

	/**
	 * 'allFieldsObject' - object that contains information about the data that we got from the server. Particularly we get
	 * data about the rows and columns that our matrix (will) contain
	 *
	 * 'allFieldsArray' - array of all rows/columns items (fields) sorted in alphabetically ascending order that matrix (will)
	 *  contain. This way can sort all rows and columns of the future matrix in the same, alphabetically ascending, order
	 */
	var allFieldsObject = jsonData.data[0].metaData.fields;
	var allFieldsArray = new Array();

	 /**
  	  * 'columnsPairedWithRows' - contains data about which columns are linked to the particular row, that is, which column is in
  	  * the intersection with the particular row and what is the value of their intersection (the value of the matrix field). We
  	  * will need this data to see outgoing items (columns to which particular row is connected).
  	  *
  	  * 'rowsPairedWithColumns' - containes data about which rows are connected to the particular column (we will need this data
  	  * in pair woth the previous one - columnsPairedWithRows). We will need this to see incoming items (which rows (items) are
  	  * connected to the particular item (in this case, column))
  	  */
  	 var columnsPairedWithRows = new Array();
  	 var rowsPairedWithColumns = new Array();

	/**
	 * TODO: NOT USED: Useful when filtering is enabled (FILTER tags attribute 'value' is set to 'true')
	 */
	function contains(a, obj) {
	    for (var i = 0; i < a.length; i++) {
	        if (a[i].value === obj) {
	            return true;
	        }
	    }
	    return false;
   }

	/**
	 * TODO: NOT USED: Useful when filtering is enabled (FILTER tags attribute 'value' is set to 'true')
	 */
   function getIndex(a, obj) {
	   for (var i = 0; i < a.length; i++) {
		   if (a[i].value === obj) {
			   return a[i].index;
		   }
	   }
	   return -1;
   }

   /**
    * Returns an array of tick angles and labels, given a group.
    *
    * Modified so it can set an unique tick density for any CHORD chart document
    * (no matter how big numbers on arcs are - kilos, millions and so on). Also
    * the frequency of appearance of numeric labels on those arcs is set so they
    * are equidistant.
    * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    */
   function groupTicks(d)
   {
		var k = (d.endAngle - d.startAngle) / d.value;

		/**
		 * What will be a value by a single tick (value that distance between two
		 * subsequent ticks represents). This way we will split ticks so that we
		 * have 360 of them on all arcs, showing that way a value per 1 degree.
		 * This will be common for all CHORD charts and in this way we will have
		 * an uniformly organized charts.
		 */
		var valueByTick = Math.floor(totalValueOnArcs/360);

		/**
		 * The index that represents a letter in the "suffixes" array, so it
		 * replicates the suffix degree of a number. E.g. suffixNum=1 represents
		 * kilos, since it will take the 1th element in the "suffixes" array.
		 */
		var suffixNum = 0;

		var suffixes = ["", "k" , "M" , "G" , "T" , "P" , "E"];

		/**
		 * The precision that numerical label values will have (number of decimals
		 * behind the whole value of the number).
		 */
		var precision = 2;
		var precisPoweredValue = Math.pow(10,precision);

		var suffixNumPoweredValue = 0;

		/**
		 * Divide current arc on as many tick we have for it, so that every tick
		 * will have an offset of 'valuByTick' value.
		 */
		return d3.range(0, d.value, valueByTick).map(function(v, i) {

			/**
			 * If the value of the current tick, 'v', is greater then 1000 (1k),
			 * analyze the value in order to figure out what is the appropriate
			 * suffix that numeric label value will contain (show) - k, M, G and
			 * so on.
			 */
			if (v >= 1000)
			{
				suffixNum = Math.floor((v + "").length/3);

				/**
				 * If the number of numerals inside the current tick value is
				 * a while multiple of number 3 (3, 6, 9 and so on), consider
				 * it as a number that belongs to the previous numerical suffix.
				 * E.g. if the 'v' is 999555, we will consider it as a 999.55k,
				 * hence we will not use the mega (M) suffix, but rather kilo
				 * (k).
				 */
				if ((v + "").length%3 === 0) {
					suffixNum--;
				}
			}

			suffixNumPoweredValue = Math.pow(1000,suffixNum);

			return {

				startAngle: d.startAngle,
				endAngle: d.endAngle,
				angle: v * k + d.startAngle,
				label: i % 10 ? null : Math.round(parseFloat(v / suffixNumPoweredValue)*precisPoweredValue)/precisPoweredValue + "" + suffixes[suffixNum]

			};
		});
   }

   /**
    * 'deselectClickedItem' - indicator if user clicked on some item on the chart - if false, prevent mouse over
    * and mouse out events. If true every item on the chart should be deselected - fully colored
    *
    * 'indexOfItemSelected' - index of the item of the chart that is selected. We need this parameter to take care
    * if we should
    */
    var deselectClickedItem = undefined;
    var indexOfItemSelected = -1;
    var previouslySelected = false;

    var selectedSomeItem = false;
    var indexOfSelectedItem = -1;
    var enableMouseOver = true;
    var enableMouseOut = true;
    var enableOtherSelect = false;

    /**
     * Returns an event handler for fading a given chord group.
     */
	function fadeMouseOver()
	{
		return function(g, i) {

			setParamsClickAndMouseOver(g,i,false);

			/**
			 * With filtering we are getting pairs of stripes: one member of the pair is the source item (the row) whose
			 * outgoing stripe(s) we need to leave as default color (darker); the second member of the pair is the target
			 * item's stripe (both linking two items: source and target) that is coming into the source item (the row).
			 * We also need to leave this, target item's stripe as default color (darker).
			 *
			 * The same logic, just in other direction is for this part inside the 'fadeMouseOut' function - those stripes
			 * (that we mentioned in previous paragraph) need to be reset as default color (dark) except those that are the
			 * subject of our discussion: the soure and target item's stripe (links between them).
			 */
			if (enableMouseOver)
			{
				svg.selectAll(".chord path")
				 	.filter(function(d) { return d.source.index != i && d.target.index != i; })
				 	.transition()
				 	.style("opacity", opacityMouseOver);

				var tool=printTheResultWhenSelecting(i);

				tooltip.
			   // attr("hidden","false").
				transition().duration(50).style("opacity","1");
				//tooltip.style("background",myColors(d[groupcolumn]));
				var ttText = tooltip.html(tool);

				/**
				 * Call the function that enables positioning of the
				 * tooltip according to its dimensions.
				 */
				var chartHeight = jsonData.chart.height ? Number(jsonData.chart.height) : panel.offsetHeight;
				var chartWidth = jsonData.chart.width ? Number(jsonData.chart.width) : panel.offsetWidth;

				positionTheTooltip(d3.event.layerX,d3.event.layerY,ttText);

//			.style("left", (d3.event.pageX) + "px")
//				.style("top", (d3.event.pageY- 25) + "px");

			}
		}
	}

	function fadeMouseOut()
	{
		return function(g, i)
		{
			setParamsClickAndMouseOver(g,i,false);

			if (enableMouseOut)
			{
				svg.selectAll(".chord path")
				 	.filter(function(d) { return d.source.index != i && d.target.index != i; })
				 	.transition()
				 	.style("opacity", opacityMouseOutAndDefault);

				tooltip.
				   // attr("hidden","false").
					transition().duration(50).style("opacity","0");
					//tooltip.style("background",myColors(d[groupcolumn]));


			}
		};
	}

	function clickOnItem()
	{

		return function(g, i)
		{

			setParamsClickAndMouseOver(g,i,true);

			/**
			 * Reset all stripes to the default (mouse out, darker) color.
			 */
			svg.selectAll(".chord path")
			 	.filter(function(d) { return true; })
			 	.transition()
			 	.style("opacity", opacityMouseOutAndDefault);

			/**
			 * Find the one (stripes that link target and source items) that we need to leave as default
			 * (darker, mouse out) color. Other stripes will be shadowed (lighter (mouse over) color).
			 */
			svg.selectAll(".chord path")
				.filter(function(d) { return d.source.index != i && d.target.index != i; })
			 	.transition()
			 	.style("opacity", opacityMouseOver);

			tooltip.transition().duration(50).style("opacity","0");

			if(jsonData.chart.isCockpit==true){
				if(jsonData.chart.outcomingEventsEnabled){

				paramethers=crossNavigationParamethers(jsonData.data[0].rows[i]);
//				var selectParams={
//						categoryName:paramethers.categoryName,
//						categoryValue:paramethers.categoryValue,
//						serieName:paramethers.serieName,
//						serieValue:paramethers.serieValue,
//						groupingCategoryName:paramethers.groupingCategoryName,
//						groupingCategoryValue:paramethers.groupingCategoryValue
//				};
				var selectParams={};
				category= jsonData.categories[1].value;
				selectParams[category]=paramethers.CATEGORY_VALUE;

				handleCockpitSelection(selectParams);
				}
			}else if(jsonData.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
				paramethers=crossNavigationParamethers(jsonData.data[0].rows[i]);
				var navigParams={
					crossNavigationDocumentName:jsonData.crossNavigation.crossNavigationDocumentName,
					crossNavigationDocumentParams:jsonData.crossNavigation.crossNavigationDocumentParams,
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
				 * Collect all needed data for the cross-navigation (all output parameters for the CHORD chart document) and
				 * forward them towards the cross-navigation handler.
				 * NOTE: output parameters as series item name and category name are not taken into count at this time instance.
				 */
				var navigParams = crossNavigationParamethers(jsonData.data[0].rows[i]);
				navigParams["CATEGORY_NAME"] = jsonData.categories[0].value;
				navigParams["SERIE_NAME"] = jsonData.series.name;
				handleCrossNavigationTo(navigParams,"CHORD");

			}

		};
	}

	function crossNavigationParamethers(d){

		/**
		 * Only four output paramters needed for the CHORD chart type (namely, no need for GROUPING_NAME and GROUPING_VALUE - not used).
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var param = {
			"CATEGORY_NAME":null,
			"CATEGORY_VALUE":null,
			"SERIE_NAME":null,
			"SERIE_VALUE":null
		};

		param.CATEGORY_VALUE=d.column_0;

		serie=0;

		for(property in d){
			if(property != "column_0"){
				serie=serie+d[property];
			}
		}

		param.SERIE_VALUE=serie;

		return param;
	}


	/**
	 * Set parameters that are necessary for the events that we are listening to: the mouse over and click event.
	 * According to these parameters we are going to control when should we enable or disable listening to the
	 * mouse over and/or mouse out events.
	 */
	function setParamsClickAndMouseOver(d,i,isClick)
	{

		/**
		 * The function responsible for processing the 'click' event listener's call is calling this function.
		 * This function is not called by the 'fadeMouseOver'/'fadeMouseOut' functions that are responsible
		 * for processing the 'mouseover'/'mouseout' event listener's call.
		 */
		if (isClick)
		{
			enableMouseOver = false;

			/**
			 * No item is selected (clicked) on the chart - select it and freeze the chart.
			 */
			if (selectedSomeItem == false)
			{
				selectedSomeItem = true;
				enableMouseOut = false;
				indexOfSelectedItem = i;

				/**
				 * TODO:
				 * 		Temporary function for printing out the items (source and target) that are the
				 * 		result of the selection (clicking) operation on the chart's item.
				 */
				//printTheResultWhenSelecting(i);
			}
			/**
			 * Some item is already selected (the chart is still freeze).
			 */
			else
			{
				/**
				 * The item that is now clicked (selected) is already selected, hence we need to deselect it and
				 * unfreeze the chart.
				 */
				if (indexOfSelectedItem == i)
				{
					selectedSomeItem = false;
					enableMouseOut = true;
					indexOfSelectedItem = -1;
				}
				/**
				 * The item that we have now clicked (selected) is different for the one that is alreday selected,
				 * hence select the newly clicked (selected) item and keep the chart freeze.
				 */
				else
				{
					selectedSomeItem = true;
					enableMouseOut = false;
					indexOfSelectedItem = i;

					//printTheResultWhenSelecting(i);
				}
			}
		}
		/**
		 * This function is called by the 'fadeMouseOver'/'fadeMouseOut' functions that are responsible
		 * for processing the 'mouseover'/'mouseout' event listener's call.
		 */
		else
		{
			/**
			 * if-block:
			 * 		None of items (arcs) is selected (clicked) - the chart us 'unfreeze'
			 * else-block:
			 * 		Some item (arc) is selected (clicked) - the chart is 'freeze'
			 */
			if (selectedSomeItem == false)
			{
				enableMouseOver = true;
				enableMouseOut = true;
			}
			else
			{
				enableMouseOver = false;
				enableMouseOut = false;
			}
		}
	}

	/**
	 * @author Ana Tomic
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	function printTheResultWhenSelecting(i)
	{
		// With which columns is this (selected, clicked) row paired
		//console.log(columnsPairedWithRows[i]);
		// Which columns are paired with this (selected, clicked) row
	   //	console.log(rowsPairedWithColumns[i]);

	   	var ttp="<b>" + columnsPairedWithRows[i].row + "</b>"+ "<br/><br/>"+"<u>To</u>:";

	   	for(j=0;j<columnsPairedWithRows[i].pairedWith.length;j++){

	   		ttp+="<br/>";

	   		ttp+=columnsPairedWithRows[i].pairedWith[j].column;

	   		ttp+="&nbsp : &nbsp";

	   		if(jsonData.tooltip.prefix){
	   			ttp+=jsonData.tooltip.prefix;
	   			ttp+="&nbsp";
	   		}

	   		// OLD CODE (BEFORE 1060 AND 1061 CHANGE)
	   		// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   		//ttp+=Number(columnsPairedWithRows[i].pairedWith[j].value).toFixed(Number(jsonData.tooltip.precision));

	   		var value = Number(columnsPairedWithRows[i].pairedWith[j].value);

	   		/**
	   		 * Providing the formatting localization for the series' tooltip value for the incoming category item. The locale (language) and
	   		 * the precision for the series value is included in formatting, as well.
	   		 * [JIRA 1060 and 1061]
	   		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   		 */
	   		ttp += value.toLocaleString(locale,{ minimumFractionDigits: jsonData.tooltip.precision, maximumFractionDigits: jsonData.tooltip.precision});

	   		if(jsonData.tooltip.postfix){
	   			ttp+="&nbsp";
	   			ttp+=jsonData.tooltip.postfix;

	   		}

//	   		ttp+="<br/>";
	   	}

	   	if (columnsPairedWithRows[i].pairedWith.length == 0)
   		{
	   		ttp += " -";
   		}

	   	ttp += "<br/>";

	   	ttp+="<br/>"+"<u>From</u>: ";

	   	for(j=0;j<rowsPairedWithColumns[i].pairedWith.length;j++){

	   		ttp+="<br/>";

	   		ttp+=rowsPairedWithColumns[i].pairedWith[j].row;
	   		ttp+="&nbsp : &nbsp";
	   		if(jsonData.tooltip.prefix){
	   			ttp+=jsonData.tooltip.prefix;
	   			ttp+="&nbsp";
	   		}

	   		// OLD CODE (BEFORE 1060 AND 1061 CHANGE)
	   		// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   		//ttp+=Number(rowsPairedWithColumns[i].pairedWith[j].value).toFixed(Number(jsonData.tooltip.precision));

	   		var value = Number(rowsPairedWithColumns[i].pairedWith[j].value);

	   		/**
	   		 * Providing the formatting localization for the series' tooltip value for the incoming category item. The locale (language) and
	   		 * the precision for the series value is included in formatting, as well.
	   		 * [JIRA 1060 and 1061]
	   		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   		 */
	   		ttp += value.toLocaleString(locale,{ minimumFractionDigits: jsonData.tooltip.precision, maximumFractionDigits: jsonData.tooltip.precision});


	   		if(jsonData.tooltip.postfix){
	   			ttp+="&nbsp";
	   			ttp+=jsonData.tooltip.postfix;

	   		}

	   		//ttp+="<br/>";
	   	}

	   	if (rowsPairedWithColumns[i].pairedWith.length==0)
   		{
	   		ttp += " -";
   		}

	   	return ttp;
	}


	/**
	 * We will specify this value in order to leave enough space for labels that
	 * are going to surround the chart in order to
	 * (danilo.ristovski@mht.net)
	 */
	//var spaceForLabels = 20;


	/* TODO: Enable and customize empty DIV of specified height in order to make some space between the subtitle and
	 * the chart (values on ticks) ??? */

	var emptySplitDivHeight = 0;

	/**
	 * Normalize height and/or width of the chart if the dimension type for that dimension is
	 * "percentage". This way the chart will take the appropriate percentage of the screen's
	 * particular dimension (height/width).
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	if (jsonData.chart.heightDimType == "percentage")
	{
		var height = jsonData.chart.height ? panel.offsetHeight*Number(jsonData.chart.height)/100 : panel.offsetHeight;
	}
	else
	{
		var height = jsonData.chart.height ? Number(jsonData.chart.height) : panel.offsetHeight;
	}

	if (jsonData.chart.widthDimType == "percentage")
	{
		var width = jsonData.chart.width ? panel.offsetWidth*Number(jsonData.chart.width)/100 : panel.offsetWidth;
	}
	else
	{
		var width = jsonData.chart.width ? Number(jsonData.chart.width) : panel.offsetWidth;
	}

	/**
     * Correction for width and height if the other one is fixed and bigger than the panel dimension value.
     * E.g. if the height of the chart is higher than the height of the panel height, the width needs to
     * be corrected, since the vertical scrollbar appears. Without this correction, the chart will be cut
     * and not entirely presented, and the horizontal scrollbar will be present as well (and it should not
     * be, since the width should just expand as much as the panel is wide).
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    var widthCorrection = 0, heightCorrection = 0, overflowXHidden = "auto", overflowYHidden = "auto";

    if (!jsonData.chart.isCockpit && height > panel.offsetHeight && width==panel.offsetWidth) {
    	widthCorrection = 16;
    	overflowXHidden = "hidden";
    }

    if (!jsonData.chart.isCockpit && width > panel.offsetWidth && height==panel.offsetHeight) {
    	heightCorrection = 16;
    	overflowYHidden = "hidden";
    }

    width -= widthCorrection;
    height -= heightCorrection;

	var chartDivWidth=width;
	var chartDivHeight=height;
	var heightForChartSvg = height;

	if(jsonData.title.text!="" || jsonData.subtitle.text!=""){

		emptySplitDivHeight=10;

		chartDivHeight-=Number(removePixelsFromFontSize(jsonData.title.style.fontSize))*1.2;
		chartDivHeight-=Number(removePixelsFromFontSize(jsonData.subtitle.style.fontSize))*1.2;
		chartDivHeight-=emptySplitDivHeight*1.2;

		heightForChartSvg = height-(Number(removePixelsFromFontSize(jsonData.title.style.fontSize))
				 + Number(removePixelsFromFontSize(jsonData.subtitle.style.fontSize))
				 +emptySplitDivHeight)*1.2;

	}

	var innerRadius = Math.min(width,height) * .35;
    var outerRadius = innerRadius * 1.1;

    /**
     * Number of row/column elements of the squared martix
     */
    var elemSize = jsonData.data[0].results;

    var colors;
    if(jsonData.colors.length > 0){
    	colors=jsonData.colors;
    }else{
    	colors=getDefaultColorPalette();
    }

	var fill = d3.scale.ordinal()
    			.domain(d3.range(elemSize))
				.range(colors);

	var randomId=  Math.round((Math.random())*10000);

	//-- mainPanelTemp.setStyle("overflow-y","hidden"); --

	/**
	 * Create an invisible HTML form that will sit on the page where the chart (in this case, the CHORD) is rendered.
	 * This form will serve us as a media through which the data and customization for the rendered chart will be sent
	 * towards the Highcharts exporting service that will take the HTML of the CHORD chart, render it and take a snapshot
	 * that will be sent back towards the client (our browser) in a proper format (PDF or PNG) and downloaded to the local
	 * machine.
	 *
	 * This way, when the user clicks on the export option for the rendered chart, the JS code ("chartExecutionController.js")
	 * that fills the form (that we set here as a blank structure) will eventually submit it towards the Highcharts export
	 * service. The result is the exported chart. This code will catch the form by the ID that we set here.
	 *
	 * (migration from the ExtJS execution to AngularJS)
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */

	if (allFieldsObject.length < 1)
	{
		var emptyMsgFontSize = parseInt(jsonData.emptymessage.style.fontSize);
		//var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
		var emptyMsgTotal = emptyMsgFontSize;

		var emptyMsgAlignment = null;
		if(jsonData.emptymessage.style.align == "left") {
			emptyMsgAlignment = "flex-start";
		} else if (jsonData.emptymessage.style.align == "right") {
			emptyMsgAlignment = "flex-end";
		} else {
			emptyMsgAlignment = "center";
		}

		// Set empty text on the chart
		d3.select(panel)
			.style("color",jsonData.emptymessage.style.color)
			.style("display","flex")
			.style("align-items","center")
			.style("justify-content",emptyMsgAlignment)
    		.style("font-family",jsonData.emptymessage.style.fontFamily)
    		.style("font-style",jsonData.emptymessage.style.fontStyle)
    		.style("font-weight",jsonData.emptymessage.style.fontWeight)
    		.style("text-decoration",jsonData.emptymessage.style.textDecoration)
    		.style("font-size",emptyMsgFontSize)
			.text(jsonData.emptymessage.text);
	}
	else {
	panel.innerText = '';
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
	 * The body inside of which the chart will be rendered.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	d3.select(panel)
		.style("overflow-x",overflowXHidden)
		.style("overflow-y",overflowYHidden)
		.append("div").attr("id","main"+randomId)
		.attr("class","d3-container")
		.attr("class","d3chartclass")
		.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
		// Set the real height of the entire chart (the one that user specified)
		.style("height",height)
		.style("width",width)
		.style("background-color",jsonData.chart.style.backgroundColor)
		.style("font-style",jsonData.chart.style.fontStyle)
		.style("font-weight",jsonData.chart.style.fontWeight)
		.style("text-decoration",jsonData.chart.style.textDecoration)
		.style("font-size",jsonData.chart.style.fontSize);

	// Set title
	d3.select("#main"+randomId).append("div")
		.style("color",jsonData.title.style.color)
		.style("text-align",jsonData.title.style.align)
		.style("font-family",jsonData.title.style.fontFamily)
		.style("font-style",jsonData.title.style.fontStyle)
		.style("font-weight",jsonData.title.style.fontWeight)
		.style("text-decoration",jsonData.title.style.textDecoration)
		.style("font-size",jsonData.title.style.fontSize)
		.text(jsonData.title.text);

	// Set subtitle
	d3.select("#main"+randomId).append("div")
		.style("color",jsonData.subtitle.style.color)
		.style("text-align",jsonData.subtitle.style.align)
		.style("font-family",jsonData.subtitle.style.fontFamily)
		.style("font-style",jsonData.subtitle.style.fontStyle)
		.style("font-weight",jsonData.subtitle.style.fontWeight)
		.style("text-decoration",jsonData.subtitle.style.textDecoration)
		.style("font-size",jsonData.subtitle.style.fontSize)
		.text(jsonData.subtitle.text);

	d3.select("#main"+randomId).append("div").style("height", emptySplitDivHeight);

	d3.select("#main"+randomId).append("div").attr("id","chartD3"+randomId);

	var svg = d3.select("#chartD3"+randomId).append("div")
	 			.attr("class","chart")
	 			.style("width",width)
	 			.style("height",chartDivHeight)
	 			.attr("align","center")
				.append("svg:svg")
				.attr("width",width)
				.attr("height",heightForChartSvg)
				.attr("viewBox","-125 -125 "+(Number(width)+250)+" "+ (Number(heightForChartSvg)+250))
				.attr( "preserveAspectRatio","xMidYMid meet")
				.style("background-color",jsonData.chart.style.backgroundColor)
				.append("svg:g")
				.attr("transform", "translate(" + width / 2 + "," + ((Number(heightForChartSvg)) / 2) + ")");

	/**
	 * @author: Ana Tomic (atomic ana.tomic@mht.net)
	 */

	var tooltip=d3.select("#chartD3"+randomId)
	.append("div")
	.attr("class","tooltip")
	.style("opacity","0");

	/**
	 * values to be applied instead of undefined if some property is not specified
	 */
	var fontStyle=jsonData.tooltip.fontStyle ? jsonData.tooltip.fontStyle:'';
	var fontWeight=jsonData.tooltip.fontWeight?jsonData.tooltip.fontWeight:'';
	var textDecoration=jsonData.tooltip.textDecoration?jsonData.tooltip.textDecoration:'';
	var backgroundColor=jsonData.tooltip.backgroundColor?jsonData.tooltip.backgroundColor:'rgba(255, 255, 255, 0.85)';
	var borderWidth=jsonData.tooltip.borderWidth?jsonData.tooltip.borderWidth: '1';
	var borderRadius=jsonData.tooltip.borderRadius?jsonData.tooltip.borderRadius:'3';

	d3.select(panel).selectAll(".tooltip")
	.style("position","fixed")
	.style("text-align",jsonData.tooltip.align)
	.style("min-width",20)
	.style("max-width",1500)
	.style("min-height",20)
	.style("max-height",2000)
	.style("padding",10)
	.style("background-color",backgroundColor)
	.style("font-size",jsonData.tooltip.fontSize)
	.style("font-family",jsonData.tooltip.fontFamily)
	.style("border",borderWidth+"px solid")
	.style("border-color",jsonData.tooltip.fontColor)
	.style("border-radius",borderRadius+"px")
	.style("font-style",fontStyle)
	.style("font-weight",fontWeight)
	.style("text-decoration",textDecoration)
	.style("color",jsonData.tooltip.fontColor)
	.style("pointer-events","none")
	.style("left","50%")
	.style("top","50%")
	.style("transform","translate(-50%, -50%)");

	/**
	 * [START] Data processing part
	 */
	var rows = jsonData.data[0].rows;
	//var source,target,value;

	var matrix = new Array(rows.length);

//	for (var i = 0; i < matrix.length;i++)
//	{
//		matrix[i] = new Array(elemSize);
//	}

	// use dataset as-is
	 for (i = 0; i < rows.length; i++)
	 {
		 matrix[i] = [];

		 for (j = 0; j < rows.length; j++)
		 {
			var column = 'column_'+(j+1);
			matrix[i][j] = parseFloat(rows[i][column]);
		 };
  	 };

  	 // Variable that will let us render this chart part, but preventing listening for the mouseover event
  	 var arcs = null;

  	/**
  	  * Which column is paired with which row (row is the initial point of view)
  	  * - 	array of objects that are composed of row attribute and its array of
  	  * 	columns that are intersected with it and have value != 0
  	  */

  	 for (var i=0; i<allFieldsObject.length; i++)
	 {
  		 var tempObject = {};
  		 var arrayOfColumnsAndValues = new Array();

  		 var rowName = allFieldsObject[i].header;
  		 allFieldsArray.push(rowName);

  		 tempObject.row = allFieldsObject[i].header;

  		 for (var j=0; j<allFieldsObject.length; j++)
		 {
  			 var tempSubArrayObjects = {};

  			 if (rows[i]["column_"+(j+1)] != 0)
			 {
  				tempSubArrayObjects.column = allFieldsObject[j].header;
  				tempSubArrayObjects.value = rows[i]["column_"+(j+1)];

  				arrayOfColumnsAndValues.push(tempSubArrayObjects);
			 }
		 }

  		 tempObject.pairedWith = arrayOfColumnsAndValues;

  		 columnsPairedWithRows.push(tempObject);
	 }

  	 // draw the graph before getting data for populating table in the legend (no loosing time)
  	 /**
  	  * If the chart can be rendered (if the category selection user made is well structured
  	  * and provides expected perfect matrix - the one with the same number of rows and columns)
  	  * this function will return 'true'. Otherwise, we will receive 'false' and in this case
  	  * we will skip errors that could happen if we render such a chart (different dimensions).
  	  *
  	  * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  	  */
	 var drawSuccessful = drawGraph(matrix);

	 if (drawSuccessful)
  	 {
		 for (var i=0; i<allFieldsObject.length; i++)
		 {
	  		 var columnName = allFieldsObject[i].header;

	  		 var tempObject = {};

	  		 var tempArray = new Array();

	  		 tempObject.column = columnName;

	  		 // go through all rows for this (i-th) column
	  		 for (var j=0; j<allFieldsObject.length; j++)
			 {
	  			 var rowName = allFieldsObject[j].header;
	  			 var tempArraysObject = {};

	  			 /**
	  			  * "... && rowName!=columnName" => we do not need information about intersection of this columns with the row of the same name (i,i),
	  			  * since we got this information when populating the 'columnsPairedWithRows' array. We do must not duplicate the data.
	  			  */
	  			 // TODO: Should I leave the rows that we already found as intersected with the actual column? Maybe for two separate tables?
	  			 if (rows[j]["column_"+(i+1)] != 0 && rowName!=columnName)
				 {
	  				 tempArraysObject.row = rowName;
	  				 tempArraysObject.value = rows[j]["column_"+(i+1)];

	  				 tempArray.push(tempArraysObject);
				 }
			 }

	  		 tempObject.pairedWith = tempArray;

	  		 rowsPairedWithColumns.push(tempObject);
		 }

	  	  /**
		  * Check if there are any arcs (if there is non-zero value for the item that is
		  * selected in the Cockpit (Cockpit selection).
		  *
		  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  */
		 if (arcs!=null) {

			// since we have all the data, enable fading of stripes and rendering the table in legend
			 arcs
			 	.on("mouseover", fadeMouseOver())
	  	 	 	.on("mouseout", fadeMouseOut());

		  	 arcs.on
		  	 (
				 "click", clickOnItem()
		  	 );

		 }
  	 }
	 else
	 {
		 /**
		  * Clean the HTML body (content that was previously prepared for the CHORD chart)
		  * and place the error information message, so use can know that chart cannot be
		  * rendered due to bad data that user set in the Designer (chart document template).
		  *
		  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  */
		 cleanChart();

		 var errorInfoForChordChart =
			 	"<strong>" +
			 		"There is a problem with the rendering of the chart." +
		 		"</strong> " +

			 	"<br/>" +
			 	"<br/>" +

			 	"Please, check if data (series item and categories) are well defined. " +

			 	"<br/>" +
			 	"<br/>" +

			 	"<strong><u>" +
			 		"NOTE" +
		 		"</u></strong>" +
		 			": You must provide one series item and two categories " +
		 			"that should have the same number of different elements (a perfect matrix with <em>N</em> rows and <em>N</em> columns).";

		 d3.select(panel).append("div").html(errorInfoForChordChart);
	 }

	 /**
	  * We need sum of values on all arcs on the chart in order to provide equidistant
	  * ticks organization and the same princip for numeric labels.
	  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	  */
	 function sumOfAllArcs(ticks)
	 {
		 var tempMax = 0;

		 for (var i=0; i<ticks[0].length; i++)
		 {
			 tempMax += ticks[0][i].__data__.value;
		 }

		return tempMax;
	 }

	 var maximumValueOnArcs = 0;

	 function drawGraph(matrix)
	 {
		 /**
		  * If matrix dimensions are not the same (if there is no the same number
		  * of rows and columns of the matrix), display the error message, since
		  * the chart cannot be rendered - this scenario provides error when
		  * rendering the chart. This way we bypass this situation and problems.
		  * This function will return 'true' in case everything is all right (the
		  * same number of rows and columns of the matrix) and 'false' otherwise.
		  *
		  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  */
		 if (jsonData.chart.isCockpit || matrix.length == matrix[0].length)
		 {
			 /**
			  * The chord layout is designed to work in conjunction with the chord shape and the arc shape.
			  * The layout is used to generate data objects which describe the chords, serving as input to
			  * the chord shape. The layout also generates descriptions for the groups, which can be used as
			  * input to the arc shape.
			  *
			  * Data is specified by setting the associated matrix.
			  *
			  * IMPORTANT: 	The input matrix must be a square matrix of numbers.
			  * IMPORTANT: 	Each column i in the matrix corresponds to the same group as row i; the cell ij
			  * 			corresponds to the relationship from group i to group j.
			  *
			  * (from: https://github.com/mbostock/d3/wiki/Chord-Layout)
			  */
			 var chord = d3.layout.chord()
			  				.padding(.05)	// TODO: Customize ???
	  						.sortSubgroups(d3.descending)
	  						.matrix(matrix);


			/**
			  * If matrix has at least one non-zero item (item of value that is not zero).
			  * So we should have at least one non-zero serie value for the selection
			  * provided inside the Cockpit engine for the CHORD chart in order to draw arcs
			  * and ticks. If this condition is not satisfied (we are dealing with the one
			  * item of value zero), we will not have arcs and ticks, but a single text line
			  * in the middle of the panel (widget) inside which the chart is rendered. The
			  * value of the text will be the value (the name) of the item that is selected.
			  *
			  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			  */
			 if(matrix.length > 1 || (matrix.length==1 && matrix[0][0]!=0))
			 {
			 	// draws circles and defines the effect on the passage mouse
				var arcs1 = svg.append("svg:g").selectAll("path")
					.data(chord.groups)
					.enter();

				arcs =	arcs1.append("svg:path")
					.style("fill", function(d) { return fill(d.index); })
					.style("stroke", function(d) { return fill(d.index); })
					.attr("d", d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius));

			 		var ticks1 = svg.append("svg:g").selectAll("g")
						.data(chord.groups)
						.enter();


			 		/**
			  		* Set the value of total sum of values on all arcs to the global variable,
			  		* so we can access ot from the function that needs this information. That
			 		* function is 'groupTicks()'.
			  		* @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			  		*/
			 		totalValueOnArcs = sumOfAllArcs(ticks1);

			 	var ticks = ticks1.append("svg:g").selectAll("g")
					.data(groupTicks)
					.enter().append("svg:g")
					.attr("transform", function(d) {
						return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
					 	  + "translate(" + outerRadius + ",0)";
						});

				/**
			 	* Customization for category labels (desciptions over arcs of the CHORD chart).
			 	* @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 	*/
				var literalLabelsFontCustom = jsonData.xAxis.labels.style;

			 	ticks1.append("svg:text")
			  	.each(function(d,i) {  d.angle = (d.startAngle + d.endAngle) / 2; })
			   	.attr("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
			  	.attr("transform", function(d) {
					return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
					+ "translate(" + (innerRadius + (outerRadius - innerRadius) + 60) + ")"
					+ (d.angle > Math.PI ? "rotate(180)" : "");
			  	})
			  	.attr("fill", literalLabelsFontCustom.color)
			  	.style("font-family",literalLabelsFontCustom.fontFamily)
			  	.style("font-style",literalLabelsFontCustom.fontStyle)
			  	.style("font-size",literalLabelsFontCustom.fontSize)
			  	.style("font-weight",literalLabelsFontCustom.fontWeight)
			  	.style("text-decoration",literalLabelsFontCustom.textDecoration)
			  	.text(function(d,i) { return allFieldsArray[i];})

			 	//aggiunge le lineette "graduate"
			 	ticks.append("svg:line")
					.attr("x1", "1")
					.attr("y1", "0")
					.attr("x2", "5")
					.attr("y2", "0")
					.style("stroke", "#FF0000");	// TODO: Customize the color of ticks ???

			/**
			 * Customization for serie labels (ticks on arcs of the CHORD chart).			 *
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			var tickLabelsFontCustom = jsonData.yAxis.labels.style;

			 //aggiunge le label unit� di misura
			ticks.append("svg:text")
				.attr("x", "8")
				.attr("dy", ".35em")
				.attr("transform", function(d) { return d.angle > Math.PI ? "rotate(180)translate(-16)" : null; })
				.style("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
				.attr("fill", tickLabelsFontCustom.color)
				.style("font-family",tickLabelsFontCustom.fontFamily)
				.style("font-style",tickLabelsFontCustom.fontStyle)
				.style("font-size",tickLabelsFontCustom.fontSize)
				.style("font-weight",tickLabelsFontCustom.fontWeight)
				.style("text-decoration",tickLabelsFontCustom.textDecoration)
				.text(function(d) { return d.label; });

			}
			else
			{
				 /**
				 * Customization for category labels (desciptions over arcs of the CHORD chart).
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				var literalLabelsFontCustom = jsonData.xAxis.labels.style;

				svg.append("svg:g").selectAll("g")
					.data(chord.groups)
					.enter().append("svg:text")
			  		.attr("fill", literalLabelsFontCustom.color)
			  		.style("font-family",literalLabelsFontCustom.fontFamily)
			  		.style("font-style",literalLabelsFontCustom.fontStyle)
			  		.style("font-size",literalLabelsFontCustom.fontSize)
			  		.style("font-weight",literalLabelsFontCustom.fontWeight)
			  		.style("text-decoration",literalLabelsFontCustom.textDecoration)
			  		.text
			  		(
		  				function(d) {

		  					/**
		  					 * Return the text value of the only series zero value item that is
		  					 * selected by clicking on the arc towards which no one (no item) is
		  					 * coming.
		  					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  					 */
		  					return allFieldsArray[0];

	  					}
	  				);
			 }

			 //disegna le fasce da un'area ad un altra
			 svg.append("svg:g")
				.attr("class", "chord")
				.selectAll("path")
				.data(chord.chords)
				.enter().append("svg:path")
				.attr("d", d3.svg.chord().radius(innerRadius))
				.style("fill", function(d) { return fill(d.target.index); })
				.style("opacity", opacityMouseOutAndDefault)
				.style("stroke", "#000")	// TODO: Customize ??
				.style("stroke-width", ".5px");	// TODO: Customize ??

			 return true;
		 }
		 else
		 {
			 return false;
		 }
	}
}
	 /**
	 * [END] Data processing part
	 */
}