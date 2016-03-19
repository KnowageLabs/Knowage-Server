/*In this file is used code that is distribuited uner the license:
Released under the GNU General Public License, version 3.*/

/**
 * Javascript function that serves for rendering the CHORD chart
 * @param jsonData Input data in JSON format, needed for rendering the chart on the client side
 */
function renderChordChart(jsonData)
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
    */
   // TODO: Customize text that goes next to the ticks (here it is 'k') ???
   function groupTicks(d)     
   {   	
		var k = (d.endAngle - d.startAngle) / d.value;
		
		return d3.range(0, d.value, 1000).map(function(v, i) {			
			return {
				startAngle: d.startAngle,
				endAngle: d.endAngle,
				angle: v * k + d.startAngle,
				label: i % 5 ? null : v / 1000 + "k"
				};
			});
   }
	
   /**
    * 'deselectClickedItem' - indicator if user clicked on some item on the chart - if false, prevent mouse over 
    * and mouse out events. If true every item on the chart should be deselected - full colored
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
				var chartHeight = jsonData.chart.height ? Number(jsonData.chart.height) : window.innerHeight;
				var chartWidth = jsonData.chart.width ? Number(jsonData.chart.width) : window.innerWidth;
				
				positionTheTooltip(chartHeight,chartWidth,ttText);	
				
//				.style("left", (d3.event.pageX) + "px")     
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
				selectParams[category]=paramethers.categoryValue;
				
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
			
		};
	}
	
	function crossNavigationParamethers(d){
		
		var param={
			"categoryName":null,
			"categoryValue":null,
			"serieName":null,
			"serieValue":null,
			"groupingCategoryName":null,
			"groupingCategoryValue":null
		};
		param.categoryValue=d.column_0;
		serie=0;
		
		for(property in d){
			if(property != "column_0"){
				serie=serie+d[property];
			}
		}
	   param.serieValue=serie;	
		
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
	   		ttp+=Number(columnsPairedWithRows[i].pairedWith[j].value).toFixed(Number(jsonData.tooltip.precision));
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
	   		ttp+=Number(rowsPairedWithColumns[i].pairedWith[j].value).toFixed(Number(jsonData.tooltip.precision));
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
		var height = jsonData.chart.height ? window.innerHeight*Number(jsonData.chart.height)/100 : window.innerHeight;
	}
	else
	{
		var height = jsonData.chart.height ? Number(jsonData.chart.height) : window.innerHeight;
	}	
	
	if (jsonData.chart.widthDimType == "percentage")
	{
		var width = jsonData.chart.width ? window.innerWidth*Number(jsonData.chart.width)/100 : window.innerWidth;
	}
	else
	{
		var width = jsonData.chart.width ? Number(jsonData.chart.width) : window.innerWidth;
	}
	
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
	
	d3.select("body")
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
	
	d3.selectAll(".tooltip")
	.style("position","absolute")
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
		 
		 d3.select("body").append("div").html(errorInfoForChordChart);
	 }
	 
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
			 var chord = d3.layout
			 				.chord()
			  				.padding(.05)
	  						.sortSubgroups(d3.descending)
	  						.matrix(matrix);	
			 
			 var tickLabelsFontCustom = jsonData.yAxis.labels.style;
			 
			 /**
			  * If matrix has at least one non-zero item (item of value that is not zero).
			  * So we should have at least one non-zero serie value for the selection
			  * provided inside the Cockpit engine for the CHORD chart in order to draw arcs
			  * and ticks. If this condition is not satisfied (we are dealing with the one
			  * item of value zero), we will not have arcs and ticks, but a single text line
			  * in the middle of the window (widget) inside which the chart is rendered. The
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
				
				 var	ticks = ticks1.append("svg:g").selectAll("g")
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
				  			+ "translate(" + (innerRadius + 60) + ")"
				  			+ (d.angle > Math.PI ? "rotate(180)" : "");
				  			})
				  		.attr("fill", literalLabelsFontCustom.color)
				  		.style("font-family",literalLabelsFontCustom.fontFamily)	
				  		.style("font-style",literalLabelsFontCustom.fontStyle)
				  		.style("font-size",literalLabelsFontCustom.fontSize)
				  		.style("font-weight",literalLabelsFontCustom.fontWeight)
				  		.style("text-decoration",literalLabelsFontCustom.textDecoration)
				  		.text(function(d,i) { return allFieldsArray[i]; })		  
	
				 //aggiunge le lineette "graduate"		 
				 ticks.append("svg:line")
					.attr("x1", "1")
					.attr("y1", "0")
					.attr("x2", "5")
					.attr("y2", "0")
					.style("stroke", "#FF0000");	// TODO: Customize the color of ticks ???
		       
				/**
				 * Customization for serie labels (ticks on arcs of the CHORD chart).			 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */			
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
	 
	 /**
	 * [END] Data processing part
	 */
}