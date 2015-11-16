/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

//================================================================
//CrossTab
//================================================================
//
//The cross tab is a grid with headers and for the x and for the y. 
//it's look like this:
//       ----------------
//       |     k        |
//       ----------------
//       |  y  |  x     |
//       ----------------
//       |y1|y2|x1|x2|x3|
//-----------------------
//| | |x1|  |  |  |  |  |
//| | |------------------
//| |x|x2|  |  |  |  |  |
//| | |------------------
//|k| |x3|  |  |  |  |  |
//| |--------------------
//| | |y1|  |  |  |  |  |
//| |y|------------------
//| | |y2|  |  |  |  |  |
//-----------------------
//
//The grid is structured in 4 panels:
//         -----------------------------------------
//         |emptypanelTopLeft|    columnHeaderPanel|
// table=  -----------------------------------------
//         |rowHeaderPanel   |    datapanel        | 
//         -----------------------------------------

Ext.ns("Sbi.crosstab.core");

Sbi.crosstab.core.CrossTab = function(config) {

	var defaultSettings = {
				percentageFontSize: 9,
				columnWidth: 80,
				rowHeight: 25,
				fontSize: 10,
				calculatedFields: new Array()};
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.crossTab) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.crossTab);
	}
	
	defaultSettings = Ext.apply(defaultSettings, config);
	
	Ext.apply(this, defaultSettings);

	//add the percent of the value respect to the total of the row or the column
	
	if(this.percenton==undefined || this.percenton==null || this.percenton==''){
		this.percenton = 'no';
	}
	
	this.manageDegenerateCrosstab(this.rowHeadersDefinition, this.columnHeadersDefinition);
	this.entries = new Sbi.crosstab.core.CrossTabData(this.entries);
    this.rowHeader = new Array();
    this.build(this.rowHeadersDefinition, 0, this.rowHeader, false);
    this.setFathers(this.rowHeader);
    this.setDragAndDrop(this.rowHeader, false, this);
    this.rowHeader[0][0].hidden=true;//hide the fake root header
    this.rowHeaderPanel = this.buildHeaderGroup(this.rowHeader, false);
    
    this.columnHeader = new Array();
    this.build(this.columnHeadersDefinition, 0, this.columnHeader, true);
    this.setFathers(this.columnHeader);
    this.setDragAndDrop(this.columnHeader, true, this);
    this.columnHeader[0][0].hidden=true;//hide the fake root header
    this.columnHeaderPanel = this.buildHeaderGroup(this.columnHeader, true);

		
    this.addDDArrowsToPage();
    this.createColumnResizer();
    
    var c = {
  		autoHeight: true,
  		border: false,
  		defaults: {autoScroll: true},
		padding : 10
	};

    this.addEvents();   

    if(this.calculatedFields!=null && this.calculatedFields.length>0){
    	this.on('afterrender', function(){
    		var i=0; 
    		for(i=0; i<this.calculatedFields.length; i++){
    			Sbi.crosstab.core.CrossTabCalculatedFields.calculateCF(this.calculatedFields[i].level, this.calculatedFields[i].horizontal, this.calculatedFields[i].operation, this.calculatedFields[i].name, this, true, null, this.percenton);
    		}
   	    	this.reloadHeadersAndTable();
    		
    	}, this);
    }
    
    
    if(this.measuresMetadata==undefined || this.measuresMetadata==null){
    	this.measuresMetadata = new Array();
    }
    
    //total position
    if(!this.rowSumStartColumn){
    	var entries = this.entries.getEntries();
    	
    	if(this.misuresOnRow){
    	    this.rowSumStartColumn = entries[0].length-1;  
    	    this.columnSumStartRow = entries.length-this.measuresMetadata.length;		
    	}else{
    	    this.rowSumStartColumn = entries[0].length-this.measuresMetadata.length;
    	    this.columnSumStartRow = entries.length-1;
    	}	
    }
    
    Sbi.crosstab.core.CrossTab.superclass.constructor.call(this, c);
};
	
Ext.extend(Sbi.crosstab.core.CrossTab, Ext.Panel, {
	entries: null // matrix with the data 
    ,rowHeaderPanelContainer: null //Panel with the header for the rows
    ,columnHeaderPanelContainer: null //Panel with the header for the columns
    ,rowHeader: null // Array. Every entry contains an array of HeaderEntry. At position 0 there is the external headers.
    ,columnHeader: null // Array. Every entry contains an array of HeaderEntry. At position 0 there is the external headers.
    ,emptypanelTopLeftTablePanels: null
    ,emptypanelTopLeftTableFirstPanels: null
    ,emptypanelTopLeft: null // The top-left corner of the table
    ,datapanel: null // The panel with the table of data
    ,rowHeaderPanel:null // An array. Every entry contains a Panel wich items are the rowHeader. i.e: rowHeaderPanel[0]= new Ext.Panel(...items :  rowHeader[0]), rowHeaderPanel[1]= new Ext.Panel(...items :  rowHeader[1])
    ,columnHeaderPanel: null // An array. Every entry contains a Panel wich items are the columnHeader. i.e: columnHeaderPanel[0]= new Ext.Panel(...items :  columnHeader[0]), columnHeaderPanel[1]= new Ext.Panel(...items :  columnHeader[1])
    ,table: null //the external table with 2 rows and 2 columns. It contains emptypanelTopLeft, columnHeaderPanel, rowHeaderPanel, datapanel
    ,checkBoxWindow: null //window with the checkBoxs for hide or show a column/line
	,columnWidth: null
	,rowHeight: null
	,fontSize: null
	,percentageFontSize: null
	,entriesPanel : null
	,crossTabCFWizard: null
	,clickMenu: null
	,withRowsSum: null
	,withColumnsSum: null
	,withRowsPartialSum: null
	,withColumnsPartialSum: null
	,calculatedFields: null
	,misuresOnRow: null
	,visibleColumns: null //the number of visible columns
	,visibleRows: null //the number of visible rows
	,measuresMetadata: null // metadata on measures: it is an Array, each entry is a json object with name, type and (in case of date/timestamp) format of the measure
	,visibleMeasuresMetadataLength: null
	,measuresNames: null
	,measuresPosition: null
	

	, manageDegenerateCrosstab: function(rowHeadersDefinition, columnHeadersDefinition) {
		if (rowHeadersDefinition.length == 1) { // degenerate crosstab (everything on columns)
			var array = [{'key': "Data", 'description': "Data"}];
			var wrapper = [array];
			rowHeadersDefinition.push(wrapper);
		}
		if (columnHeadersDefinition.length == 1) { // degenerate crosstab (everything on rows)
			var array = [{'key': "Data", 'description': "Data"}];
			var wrapper = [array];
			columnHeadersDefinition.push(wrapper);
		}
	}
	
    //================================================================
    // Loads and prepare the table with the data
    //================================================================
    
    // takes the data definition and prepare an ordered array with a panel for every tab cell
    // (in position 0 there is the cell at position (0,0) in the table, 
    // in positin 1 the cell (0,1) ecc..) 
    ,getEntries : function(){
    	var entries = this.entries.getEntries();
    	var toReturn = new Array();
    	var visiblei=0;//the visible row index. If i=2 and row[0].hidden=true, row[1].hidden=false  then  visiblei=i-1 = 1
        
    	for(var i=0; i<entries.length; i++){
    		if(!this.rowHeader[this.rowHeader.length-1][i].hidden){
    			var visiblej=0;
    			for(var j=0; j<entries[i].length; j++){

    				if(!this.columnHeader[this.columnHeader.length-1][j].hidden){
    					// get measure metadata (name, type and format)
    					var measureName = null;
    					if (this.misuresOnRow) {
    						measureName = this.rowHeader[this.rowHeader.length-1][i].name;
    					} else {
    						measureName = this.columnHeader[this.columnHeader.length-1][j].name;
    					}
    					
    					var measureMetadata = this.getMeasureMetadata(measureName);
    					// in case of calculated fields made with measures, measureMetadata is null!!!
    					var datatype =  measureMetadata.type;
    					var format = (measureMetadata !== null && measureMetadata.format !== null && measureMetadata.format !== '') ? measureMetadata.format : null;
    					// get also type of the cell (data, CF = calculated fields, partialsum)
    					var celltype = this.getCellType(this.rowHeader[this.rowHeader.length-1][i], this.columnHeader[this.columnHeader.length-1][j]);
    					
    					//Add the format to the format to the calculated fields
    					if(celltype=='CF' && Sbi.config.crosstabCalculatedFieldsDecimalePrecison!=undefined && Sbi.config.crosstabCalculatedFieldsDecimalePrecison!=null){
    						format = {};
    						format.decimalPrecision = Sbi.config.crosstabCalculatedFieldsDecimalePrecison;
    						format = Ext.encode(format);
    						datatype= 'float';
    					}
    					var backgroundColor = this.getCellBackgroundColor(i,j);
    					var scaleFactor = measureMetadata !== null ? measureMetadata.scaleFactor : 1;
    					var percent = this.calculatePercent(entries[i][j],i,j,measureMetadata,entries);
    					
    					
    					// put measure value and metadata into an array
    					var a = new Array();
    					a.push(entries[i][j]);
    					a.push('['+visiblei+','+visiblej+']');
    					a.push(datatype);
    					a.push(format);
    					a.push(celltype);
    					a.push(scaleFactor);
    					a.push(backgroundColor);
    					a.push(percent);
	    				toReturn.push(a);
	    				visiblej++;

    				}   				
    			}
    			visiblei++;
        	}	
    	}
    	this.visibleColumns=visiblej;
    	this.visibleRows=visiblei;
    	return toReturn;
    }
    
    , calculatePercent: function(value, i, j, measureMetadata, entries){
		//Add the percentage to the entries
		if(this.percenton=='row'){
			if(!this.misuresOnRow){
				return 100*parseFloat(value)/parseFloat(entries[i][measureMetadata.measurePosition+this.rowSumStartColumn]);
			}else{
				return 100*parseFloat(value)/parseFloat(entries[i][this.rowSumStartColumn]);	
			}
		} else if(this.percenton=='column'){
			if(this.misuresOnRow){
				return 100*parseFloat(value)/parseFloat(entries[measureMetadata.measurePosition+this.columnSumStartRow][j]);
			}else{
				return 100*parseFloat(value)/parseFloat(entries[this.columnSumStartRow][j]);	
			}
		}
		return null;
    }

	// returns the type of the cell (data, CF = calculated fields, partialsum) by the cell headers
	, getCellType: function(rowHeader, columnHeader) {
		if (rowHeader.type == 'CF' || columnHeader.type == 'CF') {
			return 'CF';
		}
		if (rowHeader.type == 'partialsum' || columnHeader.type == 'partialsum') {
			return 'partialsum';
		}
		return 'data';
	}

	, getMeasureMetadata: function (measureName) {
		//alert(measureName);
		for (var i = 0; i < this.measuresMetadata.length; i++) {
			if (this.measuresMetadata[i].name === measureName) {
				this.measuresMetadata[i].scaleFactor = (this.getMeasureScaleFactor(measureName).value);
				return this.measuresMetadata[i];
			}
		}
		var measureMeta = {
				type: 'float',
				format: null,
				scaleFactor: (this.getMeasureScaleFactor(measureName).value),
				name: measureName,
				measurePosition: this.measuresMetadata.length 
		}
		this.measuresMetadata.push(measureMeta);
		return measureMeta;
	}
	    	
	//returns the number of the visible (not hidden) rows		
    ,getRowsForView : function(){
    	var count =0;
    	for(var i=0; i<this.rowHeader[this.rowHeader.length-1].length; i++){
    		if(!this.rowHeader[this.rowHeader.length-1][i].hidden){
    			count++
    		}
    	}
    	return count;
    }
    
    //returns the number of the visible (not hidden) columns
    , getColumnsForView : function(){
    	var count =0;
    	for(var i=0; i<this.columnHeader[this.columnHeader.length-1].length; i++){
    		if(!this.columnHeader[this.columnHeader.length-1][i].hidden){
    			count++
    		}
    	}
    	return count;
    }
    
    //highlight a row of the table by adding a class to the cell elements (the additional class sets a background color)
    //i: the number of the row (visible)
    ,highlightRow: function(i){
		for(var y = 0; ; y++){
			//var el = Ext.get('['+i+','+y+']');
			//if (el == null) return;
			//el.addClass('crosstab-table-cells-highlight');
	   		var cel = document.getElementById('['+i+','+y+']');
	   		if (cel == null) return;
	   		cel.className += ' crosstab-table-cells-highlight'; // adding class crosstab-table-cells-highlight
		}
    }

    //highlight a column of the table by adding a class to the cell elements (the additional class sets a background color)
    //j: the number of the column (visible)
    ,highlightColumn: function(j){
		for (var y = 0; ; y++) {
			//var el = Ext.get('['+y+','+j+']');
			//if (el == null) return;
			//Ext.get('['+y+','+j+']').addClass('crosstab-table-cells-highlight');
	   		var cel = document.getElementById('['+y+','+j+']');
	   		if (cel == null) return;
	   		cel.className += ' crosstab-table-cells-highlight'; // adding class crosstab-table-cells-highlight
		}
    }
    
    //remove highlight of a row of the table by removing an additional class 
    //i: the number of the row (visible)
    ,removeHighlightOnRow: function(i){
		for(var y = 0; ; y++){
	   		var cel = document.getElementById('['+i+','+y+']');
	   		if (cel == null) return;
	   		cel.className = cel.className.replace(/\bcrosstab-table-cells-highlight\b/,''); // removing class crosstab-table-cells-highlight
		}
    }
         
    //remove highlight of a column of the table by removing an additional class 
    //j: the number of the column (visible)
    ,removeHighlightOnColumn: function(j){
 		for (var y = 0; ; y++) {
	   		var cel = document.getElementById('['+y+','+j+']');
	   		if (cel == null) return;
	   		cel.className = cel.className.replace(/\bcrosstab-table-cells-highlight\b/,''); // removing class crosstab-table-cells-highlight
 		}
    }
     
     //remove highlight from all the cell of the table by removing an additional class 
     ,removeHighlightOnTable: function(){
    	var entries = this.entries.getEntries();
		for(var i = 0; i<entries.length; i++){
			for(var y = 0; y<entries[0].length; y++){
		   		var cel = document.getElementById('['+i+','+y+']');
		   		if (cel == null) break;
		   		cel.className = cel.className.replace(/\bcrosstab-table-cells-highlight\b/,''); // removing class crosstab-table-cells-highlight
			}
		}
     }
     
     //serialize the crossTab: 
     //Create a JSONObject with the properties: data, columns, rows
     ,serializeCrossTab: function(){
    	 var serializedCrossTab = {}; 
    	 serializedCrossTab.data= this.entries.serializeEntries(this);
    	 serializedCrossTab.columns=  this.serializeHeader(this.columnHeader[0][0]);
    	 serializedCrossTab.rows=  this.serializeHeader(this.rowHeader[0][0]);
    	 serializedCrossTab.measures = this.measuresMetadata;
    	 serializedCrossTab.rowHeadersTitle = this.rowHeadersTitle;
    	 return serializedCrossTab;
     }
     
     //serialize a header and all his the subtree
 	 ,serializeHeader: function(header){
  		var node = {};
  		node.node_key =  header.name;
  		node.node_description = header.description;
 		if(header.childs.length>0){
 			var nodeChilds = new Array();
 			for(var i=0; i<header.childs.length; i++){
 				nodeChilds.push(this.serializeHeader(header.childs[i]));
 			}
 			node.node_childs = nodeChilds;
 		}
 		
 		return node;
 	}
 	    
    
    //================================================================
    // Build the headers
    //================================================================
	//    ----------------
	//    |     k        |
	//    ----------------
	//    |  y  |  x     |
	//    ----------------
	//    |y1|y2|x1|x2|x3|
	//    ----------------
    //Recursive function that builds the header panels (i.e. columnHeader and rowHeader)  
    //line: the definition of a subtree (for example ["x",[["x1"],["x2"],["x3"]] or ["y",[["y1"],["y2"]]])
    //level: the header level. For example the level of x is 1, for x1 is 2
    //headers: Or columnHeader or rowHeader
    //horizontal: true for columnHeader and false for rowHeader 
     , build : function(line, level, headers, horizontal){
		var name = line[0].key;
		var description = line[0].description;
		var leaf = false;
		var thisDimension;
    	if(line.length==1){
    		thisDimension = 1;
    		leaf = true;
    	}else{
    		var t=0;
    		var items = line[1];
    		for(var i=0; i<items.length; i++){
    			t= t+this.build(items[i], level+1, headers, horizontal);
    		}
    		thisDimension =t;
    	}
    	
    	var panelConfig = {
    			crosstab : this, 
    			percenton : this.percenton, 
    			name : name, 
    			description : description,
    			thisDimension : thisDimension, 
    			horizontal : horizontal, 
    			level : level,
    			columnWidth : this.columnWidth
    	};
    	
    	if(level%2==1 && horizontal && !leaf){//its a title header
    		panelConfig.titleHeader = true;	
    	}
    		
    	p = new Sbi.crosstab.core.HeaderEntry(panelConfig);
    	
    	if(!panelConfig.titleHeader){//if it is not a title header
    		this.setHeaderListener(p);
    	}

    	if(headers[level]==null){
    		headers[level]= new Array();
    	}
    	headers[level].push(p);
    	return thisDimension;
    }

     //Adds the listeners to the header
    , setHeaderListener: function(header, noMenu){
    	if(noMenu){
	    	header.addListener({render: function(theHeader) {
				//color the rows/columns when the mouse enter in the header
	    		theHeader.el.on('mouseenter', this.headerMouseenterHandler.createDelegate(this, [theHeader], true), this);
	    		theHeader.el.on('mouseleave', this.headerMouseleaveHandler.createDelegate(this, [theHeader], true), this);
				}, scope: this
		  	});
    	}else{
	    	header.addListener({render: function(theHeader) {
				// open the show/hide dialog
	    		theHeader.el.on('click', this.headerClickHandler.createDelegate(this, [theHeader], true), this);
				//color the rows/columns when the mouse enter in the header
	    		theHeader.el.on('mouseenter', this.headerMouseenterHandler.createDelegate(this, [theHeader], true), this);
	    		theHeader.el.on('mouseleave', this.headerMouseleaveHandler.createDelegate(this, [theHeader], true), this);
				}, scope: this
		  	});
    	}
    	
    }
    
    , headerClickHandler: function(event, element, object, theHeader) {
    	var theHandler = function(event, element, object, theHeader) {
			if(this.crossTabCFWizard!=null && this.crossTabCFWizard.isVisible()){
				this.crossTabCFWizard.addField("field["+theHeader.name+"]", theHeader.level, theHeader.horizontal);
			}else{
				if(this.clickMenu!=null){
					this.clickMenu.destroy();
				}
				this.clickMenu = new Sbi.crosstab.core.CrossTabContextualMenu(theHeader, this);
				this.clickMenu.showAt([event.getPageX(), event.getPageY()]);
			}
		}
    	theHandler.defer(100, this, [event, element, object, theHeader]); 
    	// This is a work-around (workaround, work around): without deferring the function call, if you open the calculated fields wizard, 
    	// then close it, and click quickly on a header, the context menu appears for a moment but it immediately disappears.
	}
    
    , headerMouseenterHandler: function(event, htmlElement, o, theHeader) {
		if(this.crossTabCFWizard!=null && this.crossTabCFWizard.isVisible() && this.crossTabCFWizard.isActiveLevel(theHeader.level, theHeader.horizontal)){
			theHeader.setWidth(theHeader.getWidth()-2);
			theHeader.setHeight(theHeader.getHeight()-2); 
			theHeader.addClass("crosstab-borderd");
		}

		var start=0;
		var i=0;
		var headers;
		
		if(theHeader.horizontal){
			headers = this.columnHeader;
		}else{
			headers = this.rowHeader
		}
		
		while(!this.isTheSameHeader(headers[theHeader.level][i],theHeader) && i<headers[theHeader.level].length){
			if(!headers[theHeader.level][i].hidden){
				start = start + headers[theHeader.level][i].thisDimension;
			}
			i++;
		}
		
		if( i<headers[theHeader.level].length){
			var end = start+headers[theHeader.level][i].thisDimension-1;
			if(theHeader.horizontal){
				for(i=start; i<=end; i++){
					this.highlightColumn(i);
				}
			}else{
				for(i=start; i<=end; i++){
					this.highlightRow(i);
				}
			}
		}
	}
    
    , headerMouseleaveHandler: function(event, htmlElement, o, theHeader) {
		
		if(this.crossTabCFWizard!=null && this.crossTabCFWizard.isVisible() && this.crossTabCFWizard.isActiveLevel(theHeader.level, theHeader.horizontal)){
			theHeader.removeClass("crosstab-borderd");
			theHeader.setWidth(theHeader.getWidth()+2);
			theHeader.setHeight(theHeader.getHeight()+2); 
		}
		this.removeHighlightOnTable();
			
	}
     
     //Sets the father of every HeaderEntry
    , setFathers : function(headers){
    	var heigth;	
    	for(var k=0; k<headers.length-1; k++){
    		var pannels = headers[k];
    		//index of the first child in the headers array
    		var heigthCount=0;
    		var i=0; 
	    	for(var y=0; y<pannels.length; y++){
	    		//index of the last child in the headers array
	    		heigth = pannels[y].thisDimension+heigthCount;
	    		pannels[y].childs = new Array();
	    		while(heigthCount<heigth){
	    			pannels[y].childs.push(headers[k+1][i]);
	    			headers[k+1][i].father = pannels[y];
	    			heigthCount = heigthCount+headers[k+1][i].thisDimension;
	    			i++;
	    		}
	    	}
    	}
    }
    
    //Sets the drag and drop function of every HeaderEntry
    , setDragAndDrop : function(headers, horizontal, myPanel){
	    for(var k=1; k<headers.length; k++){
			var pannels = headers[k];
	    	for(var y=0; y<pannels.length; y++){
	    		pannels[y].draggable = {

	    			// for the shadow of the panel to drag
	    			// draw the arrows
		    	    onDrag : function(e){
	    	            var pel = this.proxy.getEl();
	    	            this.x = pel.getLeft(true);
	    	            this.y = pel.getTop(true);
	    	            var s = this.panel.getEl().shadow;
	    	            if (s) {
	    	                s.realign(this.x, this.y, pel.getWidth(), pel.getHeight());
	    	            }
	    	            
	    	            var arrowTop = document.getElementById('ext-arrow-dd-top');  	            
	    	            arrowTop.style.visibility = "visible";
	    	            
	    	            var brothers =this.getDragData().panel.father.childs;
	    	            var i=0;
	    	        	var father = this.getDragData().panel.father;
	    	        	if(father==undefined || father==null){
	    	        		return;
	    	        	}
	    	            if(father.getEl()==undefined || father.getEl()==null){
	    	            	return;
	    	            }
	    	            if(horizontal){
	    	            	var arrowBottom = document.getElementById('ext-arrow-dd-bottom');
		    	            arrowBottom.style.visibility = "visible";
		    	            
		    	            //try to find the panel the cursor is over
		    	            for(i=0; i<brothers.length; i++){
			    	            if((brothers[i].getEl().getLeft(false)<e.xy[0] && brothers[i].getEl().getRight(false)>e.xy[0])){ 
			    	            	 //the cursor is over a brother panel
		    	            		arrowTop.style.left = brothers[i].getEl().getLeft(false)-5;
			    	           		arrowTop.style.top =  brothers[i].getEl().getTop(false)-10;
			    	           		arrowBottom.style.left = brothers[i].getEl().getLeft(false)-5;
			    	           		arrowBottom.style.top =  brothers[i].getEl().getBottom(false);
			    	            	return;
			    	            }
		    	            }  
		    	            //the cursor is out of the list of panels
		    	            if (father.getEl().getRight(false)<e.xy[0] ){
	    	            		arrowTop.style.left = father.getEl().getRight(false)-5;
		    	           		arrowTop.style.top =  father.getEl().getBottom(false)-10;
		    	           		arrowBottom.style.left = father.getEl().getRight(false)-5;
		    	           		arrowBottom.style.top =  father.getEl().getBottom(false)+pel.getHeight();
		    	            }else if (father.getEl().getLeft(false)>e.xy[0]){
	    	            		arrowTop.style.left = father.getEl().getLeft(false)-5;
		    	           		arrowTop.style.top =  father.getEl().getBottom(false)-10;
		    	           		arrowBottom.style.left = father.getEl().getLeft(false)-5;
		    	           		arrowBottom.style.top =  father.getEl().getBottom(false)+pel.getHeight();
		    	            }else{//the cursor is over the panel we try to dd
		    	            	
		    	            	var visibleBrother = this.getDragData().panel.getPreviousSibling(true);
		    	            	if (visibleBrother.name != this.getDragData().panel.name){
		    	            		arrowTop.style.left = visibleBrother.getEl().getRight(false)-5;
			    	           		arrowTop.style.top =  visibleBrother.getEl().getTop(false)-10;	
			    	           		arrowBottom.style.left = visibleBrother.getEl().getRight(false)-5;
		    	            		arrowBottom.style.top =  visibleBrother.getEl().getBottom(false);	
		    	            	}else{//there are no previous visible pannel
		    	            	
		    	            		if(!father.hidden){
		    	            			//not father is not the root, so we can take the father cordinates
		    	            			arrowTop.style.left = father.getEl().getLeft(false)-5;
		    	            			arrowTop.style.top =  father.getEl().getBottom(false)-10;
		    	            			arrowBottom.style.left = father.getEl().getLeft(false)-5;
		    	            			arrowBottom.style.top =  father.getEl().getBottom(false)+pel.getHeight();
		    	            		}else{
		    	            			//the father is the root, so we have to take the cordinates of the next brother
		    	            			visibleBrother = this.getDragData().panel.getNextSibling(true);
		    	            			if (visibleBrother.name != this.getDragData().panel.name){
				    	            		arrowTop.style.left = visibleBrother.getEl().getLeft(false)-5-pel.getWidth();
					    	           		arrowTop.style.top =  visibleBrother.getEl().getTop(false)-10;	
					    	           		arrowBottom.style.left = visibleBrother.getEl().getLeft(false)-5-pel.getWidth();
				    	            		arrowBottom.style.top =  visibleBrother.getEl().getBottom(false);	
		    	            			}else{//there is only one panel, the one we dd 
		    	            				arrowBottom.style.visibility = "hidden";
				    	    	            arrowTop.style.visibility = "hidden";	
				    	            	}
		    	            		}
		    	            	}
		    	            }
	    	            }else{
	    	            	var arrowTop2 = document.getElementById('ext-arrow-dd-top2');
		    	            arrowTop2.style.visibility = "visible";
		    	            for(i=0; i<brothers.length; i++){
			    	            //the cursor is over another panel
		    	            	if((brothers[i].getEl().getTop(false)<e.xy[1] && brothers[i].getEl().getBottom(false)>e.xy[1])){
		    	            		arrowTop.style.left = brothers[i].getEl().getLeft(false)-5;
			    	           		arrowTop.style.top =  brothers[i].getEl().getTop(false)-10;
			    	           		arrowTop2.style.left = brothers[i].getEl().getRight(false)-5;
			    	           		arrowTop2.style.top =  brothers[i].getEl().getTop(false)-10;
		    	    	            return;
		    	            	}
		    	            }
		    	            //the cursor is out of the list of panels
		    	            if (father.getEl().getBottom(false)<e.xy[1] ){
	    	            		arrowTop.style.left = father.getEl().getRight(false)-5;
		    	           		arrowTop.style.top =  father.getEl().getBottom(false)-10-pel.getHeight();
		    	           		arrowTop2.style.left = father.getEl().getRight(false)-5+pel.getWidth();
		    	           		arrowTop2.style.top =  father.getEl().getBottom(false)-10-pel.getHeight();
		    	            }else if (father.getEl().getTop(false)>e.xy[1]){
	    	            		arrowTop.style.left = father.getEl().getRight(false)-5;
		    	           		arrowTop.style.top =  father.getEl().getTop(false)-10;
		    	           		arrowTop2.style.left = father.getEl().getRight(false)-5+pel.getWidth();
		    	           		arrowTop2.style.top =  father.getEl().getTop(false)-10;
		    	            }else{//the cursor is over the panel we try to dd
		    	            	var visibleBrother = this.getDragData().panel.getPreviousSibling(true);
		    	            	if (visibleBrother.name != this.getDragData().panel.name){
		    	            		arrowTop.style.left = visibleBrother.getEl().getLeft(false)-5;
			    	           		arrowTop.style.top =  visibleBrother.getEl().getTop(false)-10+brothers[i-1].getEl().getHeight();
			    	           		arrowTop2.style.left = visibleBrother.getEl().getRight(false)-5;
			    	           		arrowTop2.style.top =  visibleBrother.getEl().getTop(false)-10+brothers[i-1].getEl().getHeight()
		    	            	}else{//there are no previous visible pannel
		    	            		if(!father.hidden){
		    	            			//not father is not the root, so we can take the father cordinates
		    	            			arrowTop.style.left = father.getEl().getRight(false)-5;
		    	            			arrowTop.style.top =  father.getEl().getTop(false)-10;
		    	            			arrowTop2.style.left = father.getEl().getRight(false)-5+pel.getWidth();
		    	            			arrowTop2.style.top =  father.getEl().getTop(false)-10;
		    	            		}else{
		    	            			//the father is the root, so we have to take the cordinates of the next brother
		    	            			visibleBrother = this.getDragData().panel.getNextSibling(true);
		    	            			if (visibleBrother.name != this.getDragData().panel.name){
				    	            		arrowTop.style.left = visibleBrother.getEl().getLeft(false)-5;
				    	            		arrowTop.style.top =  visibleBrother.getEl().getTop(false)-10-pel.getHeight();	
				    	            		arrowTop2.style.left = visibleBrother.getEl().getRight(false)-5;
				    	            		arrowTop2.style.top =  visibleBrother.getEl().getTop(false)-10-pel.getHeight();	
		    	            			}else{//there is only one panel, the one we dd 
		    	            				arrowBottom.style.visibility = "hidden";
				    	    	            arrowTop.style.visibility = "hidden";	
				    	            	}
		    	            		}
		    	            	}
		    	            }		    	            
	    	            }
			        },	    	       
			        //Upadate the positions of the entries in the line with the element dropped
			        endDrag : function(e){  
			        	var myXY;
			        	var exy;
			        	if(horizontal){
			        		myXY = this.getDragData().panel.getEl().getX();
			        		exy = 0;
			        	}else{
			        		myXY = this.getDragData().panel.getEl().getY();
			        		exy = 1;
			        	}
			        	
			        	var father =this.getDragData().panel.father;
			        	
	    	        	if(father==undefined || father==null){
	    	        		return;
	    	        	}
			        	
			        	var tempChilds = new Array();
			        	var xy;
			        	
			        	var hiddenPanels = new Array();
			        	var notHiddenPanels = new Array();
			        	for(var j=0; j<father.childs.length; j++){
			        		if(father.childs[j].hidden){
			        			hiddenPanels.push(father.childs[j]);
			        		}else{
			        			notHiddenPanels.push(father.childs[j]);
			        		}
			        	}

			        	father.childs =notHiddenPanels.concat(hiddenPanels);
			        	
			        	if(myXY<e.xy[exy]){
	    	        		var startPosition = father.childs.length-1;
	    	        		var endPosition = father.childs.length-1;
	    	        		
	    	        		for(var j=0; j<father.childs.length; j++){
	    	        			if(horizontal){
	    	        				xy = (father.childs[j]).getEl().getX();
	    	        			}else{
	    	        				xy = (father.childs[j]).getEl().getY();
	    	        			}
	    	        			if(xy>myXY || xy==0){
	    	        				startPosition = j-1;
	    	        				break;
	    	        			}
	    	        		}
	    	        		for(var j=startPosition; j<father.childs.length; j++){
	    	        			if(horizontal){
	    	        				xy = (father.childs[j]).getEl().getX();
	    	        			}else{
	    	        				xy = (father.childs[j]).getEl().getY();
	    	        			}

	    	        			if(xy>e.xy[exy] || xy==0){
	    	        				endPosition = j-1;
	    	        				break;
	    	        			}
	    	        		}

	    	        		var me = father.childs[startPosition];
	    	        		for(var j=startPosition; j<endPosition; j++){
	    	        			father.childs[j]=father.childs[j+1];
	    	        		}
	    	        		father.childs[endPosition] = me;
	    	        	}else{
	    	        		var startPosition = father.childs.length-1;
	    	        		var endPosition = 0;
	    	        		
	    	        		for(var j=0; j<father.childs.length; j++){
	    	        			if(horizontal){
	    	        				xy = (father.childs[j]).getEl().getX();
	    	        			}else{
	    	        				xy = (father.childs[j]).getEl().getY();
	    	        			}
	    	        			
	    	        			if(xy>e.xy[exy]){
	    	        				endPosition = j-1;
	    	        				break;
	    	        			}
	    	        		}
	    	        		if(endPosition <0){
	    	        			endPosition = 0;
	    	        		}
	    	        		
	    	        		for(var j=endPosition; j<father.childs.length; j++){
	    	        			if(horizontal){
	    	        				xy = (father.childs[j]).getEl().getX();
	    	        			}else{
	    	        				xy = (father.childs[j]).getEl().getY();
	    	        			}
	    	        			if(xy>myXY || xy==0){
	    	        				startPosition = j-1;
	    	        				break;
	    	        			}
	    	        		}

	    	        		var me = father.childs[startPosition];
	    	        		for(var j=startPosition; j>endPosition; j--){
	    	        			father.childs[j]=father.childs[j-1];
	    	        		}
	    	        		father.childs[endPosition] = me;
	    	        	}
			        	var arrowBottom = document.getElementById('ext-arrow-dd-bottom');
	    	            var arrowTop = document.getElementById('ext-arrow-dd-top');
	    	            var arrowTop2 = document.getElementById('ext-arrow-dd-top2');
	    	            arrowBottom.style.visibility = "hidden";
	    	            arrowTop.style.visibility = "hidden";
	    	            arrowTop2.style.visibility = "hidden";
	    	            myPanel.reload(headers, horizontal);
	    	        }
	    	    }
	    	}
		}
    }
    
    //Adds the arrows for the drag and drop to the page
    , addDDArrowsToPage : function(){
		var dh = Ext.DomHelper; 
		var bottomArrowDOM = {
			id: 'ext-arrow-dd-bottom',
			tag: 'div',
			cls: 'col-move-bottom',
			html: '&nbsp;',
			style: 'left: -100px; top: -100px; visibility: hidden;'
		};
		
		var topArrowDOM = {
			id: 'ext-arrow-dd-top',
			tag: 'div',
			cls: 'col-move-top',
			html: '&nbsp;',
			style: 'left: -100px; top: -100px; visibility: hidden;'
		};
		
		var topArrowDOM2 = {
			id: 'ext-arrow-dd-top2',
			tag: 'div',
			cls: 'col-move-top',
			html: '&nbsp;',
			style: 'left: -100px; top: -100px; visibility: hidden;'
		};
		
		var bodyElement = document.getElementsByTagName('body');

		dh.append(bodyElement[0].id, bottomArrowDOM);
		dh.append(bodyElement[0].id, topArrowDOM);
		dh.append(bodyElement[0].id, topArrowDOM2);
		
    }

    //reload the panels after a change (for example DD)
    , reload : function(headers, horizontal){
    	
    	var headersFresh=new Array();
    	headersFresh.push(headers[0]);
    	for(var k=1; k<headers.length; k++){
	    	var rowHeaderFresh=new Array();
	    	for(var y=0; y<headersFresh[k-1].length; y++){
	    		for(var i=0; i<headersFresh[k-1][y].childs.length; i++){
	    			rowHeaderFresh.push(headersFresh[k-1][y].childs[i]);
	    		}
	    	}
	    	headersFresh.push(rowHeaderFresh);   
    	}
    	
    	this.upadateAndReloadTable(headersFresh, horizontal);
    }
    
    // Build the columnHeaderPanel or the rowHeaderPanel
	, buildHeaderGroup : function(headers, horizontal) {
	
		var headerGroup = new Array();
		this.headersPanelHidden = new Array();
		var resizeHeandles = 'e';
		if (horizontal){
			resizeHeandles = 's';
		}
		var c;
		if(horizontal){
			c = {
					boxMinHeight: this.rowHeight
				};
		}else{
			c = {
					boxMinWidth: this.columnWidth
				};	
		}
		
		for(var y=1; y<headers.length; y++){
			var layoutConfig;
			if(horizontal){
				layoutConfig= {rows: 1};
			}else{
				layoutConfig= {columns: 1};
			}
			
			var headersPanel = new Array();
			var headersPanelHidden = new Array();
			for(var i=0; i<headers[y].length; i++){
				headers[y][i].columnWidth= this.columnWidth;
				if(!headers[y][i].hidden){
					headersPanel.push(headers[y][i]);
				}else{
					//we put the hidden panels in the tail of the table.
					//this because a bug of ie
					headersPanelHidden.push(headers[y][i]);
				}
			}
			headersPanel = headersPanel.concat(headersPanelHidden);
			c = Ext.apply(c,{
		    	cellCls: 'crosstab-header-panel-cell',
		    	layout:'table',
		    	border: false,
	            layoutConfig: layoutConfig,
				items:  headersPanel
		    });
		    var p = new Ext.Panel(c);  
		   	    
		    this.createResizable(p,resizeHeandles,headersPanel, horizontal, y);
		    headerGroup.push(p);

		}
		return headerGroup;
	}
	
	//Upadate the container table 
    , upadateAndReloadTable : function(headerGroup, horizontal){
    	if(horizontal){
    		this.updateTableY(this.getNewPositions(this.columnHeader[this.columnHeader.length-1], headerGroup[this.columnHeader.length-1]));
    		this.columnHeader = headerGroup;
    	}else{
    		this.updateTableX(this.getNewPositions(this.rowHeader[this.rowHeader.length-1], headerGroup[this.rowHeader.length-1]));
    		this.rowHeader = headerGroup;
    	}
    	
    	this.reloadHeadersAndTable();
    }

    //Calculate the translation vector after a transformation (for example DD)
    //headerLine2: the old headerLine
    //headerLine1: the new headerLine
    //For example:
    //headerLine2 = x y
    //headerLine1 = y x
    //returns 1 0 
    , getNewPositions : function(headerLine2, headerLine1){
    	var newPositions = new Array();
    	for(var y=0; y<headerLine1.length; y++){
        	for(var i=0; i<headerLine2.length; i++){
        		if(this.isTheSameHeader(headerLine2[i], headerLine1[y])){
        			if(headerLine2[i].hidden){
        				newPositions.push(i);
        			}else{
        				newPositions.push(i);
        			}
        			break;
        		}
        	}
    	}
    	return newPositions;
    }
    
    //check if header1.equals(header2)
    ,isTheSameHeader : function(header1, header2, debug){
    	var loop1 = header1; 
    	var loop2 = header2;
    	do{
    		if(debug){
    			alert(loop1.name+" "+loop2.name );
    		}
    		if(loop1.name!= loop2.name){
    			return false;
    		}
    		loop1 = loop1.father;
    		loop2 = loop2.father;
    	} while(loop1!=null);
    	return loop2==null;
    }
    
    //Update the order of the cells after a change in the column headers (Dd or hide/show)
    , updateTableY : function(newPositions){
    	var newEntries = new Array();
    	var entries = this.entries.getEntries();
    	for(var i=0; i<entries.length; i++){
    		var templine = new Array();
    		for(var y=0; y<entries[i].length; y++){
    		//	if(newPositions[y]!=null){
	        		templine.push(entries[i][newPositions[y]]);
	       // 	}
    		}
    		newEntries.push(templine);
    	}
    	  		
    	var celltypeOfColumnsNew = new Array();
    	for(var y=0; y<entries[0].length; y++){
    		celltypeOfColumnsNew.push(this.celltypeOfColumns[newPositions[y]]);
    	}
    	this.celltypeOfColumns = celltypeOfColumnsNew;
   		
    	this.entries.setEntries(newEntries);
    }

    //Update the order of the cells after a change in the row headers (Dd or hide/show)
    , updateTableX : function(newPositions){
    	var entries = this.entries.getEntries();
    	var newEntries = new Array();
    	for(var i=0; i<entries.length; i++){
    		newEntries.push(entries[newPositions[i]]);
    	}
    	
    	var celltypeOfRowsNew = new Array();
    	for(var i=0; i<entries.length; i++){
   			celltypeOfRowsNew.push(this.celltypeOfRows[newPositions[i]]);
        }
   		this.celltypeOfRows = celltypeOfRowsNew;
   	
    	this.entries.setEntries(newEntries);
    }
    
    //reload the container table
    , reloadTable : function(lazy){

    	var d1 = new Date();
    	
    	var tableRows = 2;
    	var tableColumns = 2;
    	var dataPanelStyle = "crosstab-table-data-panel";
    	var classEmptyBottomRight = 'crosstab-table-empty-bottom-right-panel';
    	
    	if(this.table!=null && this.datapanel!=null){
    		this.datapanel.destroy();
    		this.remove(this.table, false);
    	}
   		classEmptyBottomRight = classEmptyBottomRight+' crosstab-none-left-border-panel';    	   	
    	this.table = new Ext.Panel({  
    		cls: 'centered-panel',
            layout:'table',
            border: false,
            layoutConfig: {
                columns: tableColumns,
                rows: tableRows
            }
        });

    	
   	    this.entriesPanel = this.getEntries(true, true);
   		var rowForView = this.getRowsForView();
   		var columnsForView = this.getColumnsForView();

   		//Build the panel on the top, left.
   		//this panel is a table with this.columnHeader.length-2 empty rows
   		//and the title of the row headers
   		if(this.emptypanelTopLeft==null){
   			
   			var emptypanelTopLeftItems = new Array();
   			
   			//empty rows
   	    	this.emptypanelTopLeftTableFirstPanels = new Ext.Panel({ 
	    			colspan:  (this.rowHeader.length-1), 
	    			rowspan:  1,
	   	            width: (this.columnWidth*(this.rowHeader.length-1)),
	   	            height: (this.rowHeight), 
	    			cellCls: 'crosstab-table-empty-bottom-left-panel',
	   	            border: false,
	   	            html:"&nbsp; "
	   	        });
   				
   	    	emptypanelTopLeftItems.push(this.emptypanelTopLeftTableFirstPanels);
   	    	
   	    	for(var col = 0; col<this.columnHeader.length-3; col++){
   	    		emptypanelTopLeftItems.push(new Ext.Panel({ 
   	    			colspan:  (this.rowHeader.length-1), 
   	    			rowspan:  1,
   	   	            width: (this.columnWidth*(this.rowHeader.length-1)),
   	   	            height: (this.rowHeight), 
   	    			cellCls: 'crosstab-table-empty-bottom-left-panel',
   	   	            border: false,
   	   	            html:"&nbsp; "
   	   	        }));
   	    	}

   	    	this.emptypanelTopLeftTablePanels = new Array();
   	    	
   	    	for(var col = 0; col<this.rowHeadersTitle.length; col++){

   	    		this.emptypanelTopLeftTablePanels.push(new Sbi.crosstab.core.HeaderEntry({crosstab:this, name:this.rowHeadersTitle[col], thisDimension:1, horizontal:false, level:1, columnWidth: this.columnWidth, titleHeader: true}));
   	    		if(col==0){
   	    			this.emptypanelTopLeftTablePanels[0].cellCls = ' crosstab-table-empty-top-left-panel-leftmostcell ';
   	    		}else{
   	    			this.emptypanelTopLeftTablePanels[col].cellCls = ' crosstab-table-empty-top-left-panel-bottomcells ';
   	    		}

   	    		emptypanelTopLeftItems.push(this.emptypanelTopLeftTablePanels[col]);
   	    	}
   	    	
   	    	//add the measures title if the measures are on row
   	    	if(this.misuresOnRow){
   	    		this.emptypanelTopLeftTablePanels.push(new Sbi.crosstab.core.HeaderEntry({crosstab:this, name:LN('sbi.crosstab.crosstabdefinitionpanel.measures'), thisDimension:1, horizontal:false, level:1, columnWidth: this.columnWidth, titleHeader: true}));
   	    		if(this.rowHeadersTitle.length=0){
   	    			this.emptypanelTopLeftTablePanels[0].cellCls = ' crosstab-table-empty-top-left-panel-leftmostcell ';
   	    		}else{
   	    			this.emptypanelTopLeftTablePanels[col].cellCls = ' crosstab-table-empty-top-left-panel-bottomcells ';
   	    		}
   	    		
   	    		emptypanelTopLeftItems.push(this.emptypanelTopLeftTablePanels[col]);
   	    	}
   	    	
   			this.emptypanelTopLeft = new Ext.Panel({  
   	            layout:'table',
   	            border: false,
   	            layoutConfig: {
   	                columns: (this.rowHeader.length-1),
   	                rows:  (this.columnHeader.length-1)
   	            },
   	            items: emptypanelTopLeftItems
   	        });
   		} 	
 		
    	var store = new Ext.data.ArrayStore({
    	    autoDestroy: true,
    	    storeId: 'myStore',
    	    fields: [
    	             {name: 'name'},
    	             'divId',
    	             {name: 'datatype'},
    	             {name: 'format'},
    	             {name: 'celltype'},
    	             {name: 'scaleFactor'},
    	             {name: 'backgroundColor'},
    	             {name: 'percent'}
    	             
    	    ]
    	});
 	
    	store.loadData(this.entriesPanel);
    	var columnsForView = this.getColumnsForView();
    	
    	var ieOffset =0;
    	if(Ext.isIE){
    		ieOffset = 2;
    	}
    	
    	var tpl = new Ext.XTemplate(
    	    '<tpl for=".">'
    	    , '<div id="{divId}" class="x-panel crosstab-table-cells crosstab-table-cells-{celltype}" ' // the crosstab-table-cells class is needed as itemSelector
    	    , ' style="height: '+(this.rowHeight-2+ieOffset)+'px; width:'+(this.columnWidth-2)+'px; float:left; background-color: {backgroundColor}" >'
    	    , '  <div class="x-panel-bwrap"> '
    	    , '    <div style="width:'+(this.columnWidth-2)+'px; overflow:hidden; padding-top:'+(this.rowHeight-4-this.fontSize)/2+'px;font-size:'+this.fontSize+'px;">'
    	    , '    {[this.format(values.name, values.datatype, values.format, values.percent,'+this.percentageFontSize+', values.scaleFactor )]}'
    	    , '    </div> '
    	    , '  </div>'
    	    , '</div>'
    	    , '</tpl>'
    	    , {
    	    	format: this.format
    	    }
    	);
    	
    	var dataView = new Ext.DataView({
	        store: store,
	        tpl: tpl,
	        itemSelector: 'div.crosstab-table-cells',
	        trackOver:true
	    });
    	dataView.on('mouseleave', function(dataView, index, htmlNode, event) {
            clearTimeout(this.selectedColumnTimeout);
            clearTimeout(this.selectedRowTimeout);
            var divId = eval(htmlNode.id);
           	var row = divId[0];
           	var column = divId[1];
          	this.removeHighlightOnColumn(column);
          	this.removeHighlightOnRow(row);

        }, this);
    	dataView.on('mouseenter', function(dataView, index, htmlNode, event) {

    		var divId = eval(htmlNode.id);
           	var row = divId[0];
           	var column = divId[1];

           	//defer the highlight: during the navigation tipically the mouse
           	// passes over some cells with out stop in them. Deferring the highlightColumn
           	// and eith the clearTimeout in the mouseleve event we try to not highlight the transit cells
            this.selectedColumnTimeout=(this.highlightColumn).defer(200,this,[column]);
            this.selectedRowTimeout=(this.highlightRow).defer(200,this,[row]);
        }, this);
    	
    	this.datapanel = new Ext.Panel({
            width: (columnsForView)*(this.columnWidth),
            height: (rowForView)*(this.rowHeight)+1,
            cellCls: dataPanelStyle,
            border: false,
    	    layout:'fit',
    	    items: dataView
    	});

   		this.table.add(this.emptypanelTopLeft);
   		this.table.add(this.columnHeaderPanelContainer);
   		this.table.add(this.rowHeaderPanelContainer);
   		this.table.add(this.datapanel);

   		this.add(this.table);
   		if(!lazy){
	   		var d22 = new Date();
	   		this.rowHeaderPanelContainer.doLayout();
	   		var d3 = new Date();
	   		this.columnHeaderPanelContainer.doLayout();
	   		var d4 = new Date();
	   		this.datapanel.doLayout();
	   		var d5 = new Date();
	   		this.emptypanelTopLeft.doLayout();
	   		var d6 = new Date();
	
	   		this.table.doLayout(false);
	   		this.doLayout(false);
   		}
   		var d7 = new Date();
  		
    }
    
    , getCellNewType: function(rowId, columnId){
    	var datacolor = 'FFF';
    	var cfcolor = 'EEE';
    	var partialcolor = 'DDD';
    	var totalcolor = 'CCC';
    	var rowtype = this.celltypeOfRows[rowId];
    	var columntype = this.celltypeOfColumns[columnId];
    	if(columntype=='data'){
    		return rowtype;	
    	}else if(columntype=='cf'){
    		if(rowtype=='data'){
        		return 'cf';
        	} else {
        		return rowtype;
        	}	
    	}else if(columntype=='partialsum'){
    		if(rowtype=='totals'){
        		return 'totals';
        	} else {
        		return columntype;
        	}
    	}
    	return 'totals';
    }
    
    , getCellBackgroundColor: function(rowId, columnId){
  
    	var type = this.getCellNewType(rowId, columnId);
    	
    	if(type=='data'){
    		return 'FFF';	
    	}
    	if(type=='cf'){
    		return 'EEE';
    	}
    	if(type=='partialsum'){
    		return 'DDD'
    	}
    	return 'CCC';
    }
    
    , format: function(value, type, format, percent, percentFontSize, scaleFactor) {

    	if(value=='NA'){
    		if(this.naCellValue!= undefined && this.naCellValue!=null){
    			return this.naCellValue;
    		}else{
    			return value;
    		}
    		
    	}
		var str;
		try {
			var valueObj = value;
			if (type == 'int') {
				valueObj = parseInt(value);
				if(scaleFactor!=undefined && scaleFactor!=null){
					valueObj = valueObj/scaleFactor;
				}
			} else if (type == 'float') {
				valueObj = parseFloat(value);
				if(scaleFactor!=undefined && scaleFactor!=null){
					valueObj = valueObj/scaleFactor;
				}
			} else if (type == 'date') {
				valueObj = Date.parseDate(value, format);
			} else if (type == 'timestamp') {
				valueObj = Date.parseDate(value, format);
			}

			
			if(type=='float' || type=='int'){
				if(type=='float' && format!=null && format!=undefined){
					
					var formatOfCell= Ext.apply({},Sbi.locale.formats[type]);
					Ext.apply(formatOfCell,Ext.decode(format));
					
					str = Sbi.qbe.commons.Format.number(valueObj,formatOfCell);
				}else{
					str = Sbi.qbe.commons.Format.number(valueObj,Sbi.locale.formats[type]);
				}
				
				//if the tipe is a number and the percenton variable is not null in the configuration
				//we add the percent in the cell
				if(percent!=undefined && percent!=null && percent!='' && (type == 'float' || type == 'int')){
					str = '<div style=\'text-align: right;\'>' + str +'<span style="font-size:'+percentFontSize+'px;"> (' + Sbi.qbe.commons.Format.number(percent, 'int') +'%)</span></div>';
				}else{
					str = '<div style=\'text-align: right;\'>' + str +'</div>';
				}
			}

			//alert(value+" "+str+" "+format);
			
			return str;
		} catch (err) {
			return value;
		}
	}
    
    
    , reloadHeadersAndTable: function( lazy){    	
    		var span=1;
        	this.columnHeaderPanel = this.buildHeaderGroup(this.columnHeader, true);
        	if(this.columnHeaderPanelContainer!=null){
        		this.columnHeaderPanelContainer.destroy();
        	}
        	this.columnHeaderPanelContainer = new Ext.Panel({
	   			style: 'margin: 0px; padding: 0px;',
	   			cellCls: 'crosstab-column-header-panel-container',
	   			height: 'auto',
	   	        width: 'auto',
	   	        border: false,
	   	        layout:'table',
	   	        colspan: span,
	   	        layoutConfig: {
	   	            columns: 1
	   	        },
	   	        items: this.columnHeaderPanel
	   	    });

        	span=1;

	    	this.rowHeaderPanel = this.buildHeaderGroup(this.rowHeader, false);
	    	if(this.rowHeaderPanelContainer!=null){
	    		this.rowHeaderPanelContainer.destroy();
	    	}
	   		this.rowHeaderPanelContainer = new Ext.Panel({
	   			style: 'margin: 0px; padding: 0px; ',
	   			cellCls: 'crosstab-row-header-panel-container',
	   			height: 'auto',
	   	        width: 'auto',
	   	        layout:'table',
	   	        border: false,
	   	        rowspan: span,
	   	        layoutConfig: {
	   	    		
	   	            columns: this.rowHeader.length
	   	        },
	   	        items: this.rowHeaderPanel,
	   	        colspan: 1
	   	    });
		this.reloadTable(lazy);
    }
    
    , createColumnResizer : function(){
		var thisPanel = this;
		var headerPosition =0;
		
    	var iePinned = false;
    	if(Ext.isIE){
    		iePinned = true;
    	}
		
    	
    	for(headerPosition=0; headerPosition<this.columnHeader[this.columnHeader.length-1].length; headerPosition++){
	    	var columnHeaderLeaf = this.columnHeader[this.columnHeader.length-1][headerPosition];
	    	
	    	columnHeaderLeaf.on('render', function() {
				var resizer = new Ext.Resizable(this.id, {
				    handles: "e",
				    pinned: iePinned
				});
				resizer.on('resize', function(resizable, width, height, event ) {
					var columnWidth = width;
					thisPanel.columnWidth = columnWidth;
					thisPanel.reloadHeadersAndTable();
				}, this);
			}, columnHeaderLeaf);
    	}
    }
    
	, createResizable: function(aPanel, heandles, items, horizontal, headerLineIndex) {
		
    	var iePinned = false;
    	if(Ext.isIE){
    		iePinned = true;
    	}
		var thisPanel = this;
		aPanel.on('render', function() {
			var resizer = new Ext.Resizable(this.id, {
			    handles: heandles,
			    pinned: iePinned
			});
			resizer.on('resize', function(resizable, width, height, event) {
				//add the row names at the row header headers 
				if(!horizontal){
					(thisPanel.emptypanelTopLeftTablePanels[headerLineIndex-1]).updateStaticDimension(width);	
				}else{
					var originalH = thisPanel.emptypanelTopLeftTableFirstPanels.getHeight();
					thisPanel.emptypanelTopLeftTableFirstPanels.setHeight(originalH + (height - items[0].height));
				}
				
				for(var i=0; i<items.length; i++){
					if(horizontal){
						items[i].updateStaticDimension(height);
					}else{
						items[i].updateStaticDimension(width);
					}
				}
			}, this);
		}, aPanel);
	}
 
    
    //Add a new block in the table (a subtree with the headhers and a set of columns or rows)
    //level: the level in witch put the root of the subtree 
    //node: the root of the subtree of headers to add
    //headers: the headers (columnHeader or rowheader)
    //entries: the rows or columns with the data to add
    //horizontal: true if the entries are columns, false otherwise
    //lazy: true if we only want to insert the row/columns in the data structures, but not in spread the data in the GUI.
    //      it's usefull if we call this method more than one time: we call it with lazy=true far all the iteration and with lazy=false in the last one +
    //       (take a look at the method calculateCF)
    , addNewEntries: function(level,node,headers,entries, horizontal, lazy){//, entriesSum){
    	
    	var father = node.father;
    	var dimensionToAdd= node.thisDimension;
    	//update the father
    	father.childs.push(node);
    	
      	//update the fathers dimension of the subtree of headers where we put the node..
	   	while(father!=null){
	   		father.thisDimension= father.thisDimension+dimensionToAdd;
	   		father.leafsNumber = father.leafsNumber+dimensionToAdd;
	   		father=father.father;
	   	}
    	var nodeToAddList = new Array();
    	var freshNodeToAddList;
    	var nodePosition;
    	var startPos;
    	var endPos;
    	var nodeS;
    	nodeToAddList.push(node);

    	//Find the index in the headers[level] where put the node
    	var startDimension=0;
		for(var i=0; i<headers[level].length; i++){
			nodeS =  headers[level][i];
			if(nodeS.father == node.father){
				startDimension=startDimension+this.getLeafsNumber(node.father)-this.getLeafsNumber(node);
				startPos = i+nodeS.father.childs.length-1;
				break;
			}
			startDimension = startDimension+this.getLeafsNumber(nodeS);
		}
		
		
		//add the node before the totals
		if(level==2 && horizontal && this.withRowsSum && startDimension==headers[headers.length-1].length){
			if(this.misuresOnRow){
				startDimension = startDimension-1;	
			}else{
				startDimension = startDimension-this.measuresMetadata.length;
			}
			startPos = startPos-1;
    	}
		
		nodePosition=startDimension;
		
		//when we add a new node, we have to move forward all the
		//pannels that live after the nodePosition, and we have
		//to add the node and all its childs
    	for(var y=level; y<headers.length;y++){

			//move the pannels that live after the position of the pannels
			for(var j=headers[y].length-1; j>=startPos; j--){
				headers[y][j+nodeToAddList.length] = headers[y][j];
			}

			//add the node and all its childs
			for(var j=0; j<nodeToAddList.length; j++){
				headers[y][startPos+j] = nodeToAddList[j];
			}
			
			if(y<headers.length-1){
				//prepares the fresh variable for the next iteration with all the childs 
				freshNodeToAddList = new Array();
				for(var j=0; j<nodeToAddList.length; j++){
					freshNodeToAddList = freshNodeToAddList.concat(nodeToAddList[j].childs);
				}
				nodeToAddList = freshNodeToAddList;
				var freshStartDimension=0;
				
				for(var i=0; i<=headers[y+1].length; i++){
					if(startDimension == freshStartDimension){
						startPos = i;
						break;
					}
					if(headers[y+1][i]!=null){
						freshStartDimension=freshStartDimension+this.getLeafsNumber(headers[y+1][i]);
					}else{
						startPos = i;
						break;
					}
				}
			}
    	}
    	//UPDATE THE CELLTYPES
    	if(!horizontal){
    		for(var f=0; f<entries.length; f++){
    			this.celltypeOfRows.splice(nodePosition+f,0,"cf");
    		}
    	}else{
    		for(var f=0; f<entries.length; f++){
    			this.celltypeOfColumns.splice(nodePosition+f,0,"cf");
    		}
    	}

    	//add the columns or the rows
    	if(horizontal){
    		//The last element of the entries is the calculated field applied on the totals
    		//so we have to remove it and store in a local variable
    		this.entries.addColumns(nodePosition,entries);

    	}else{
    		//The last element of the entries is the calculated field applied on the totals
    		//so we have to remove it and store in a local variable
    		this.entries.addRows(nodePosition,entries);
    		
    	}

    	if(horizontal){
    		this.columnHeader = headers;
    	}else{
    		this.rowHeader = headers;
    	}
    	
    	if(!lazy){
	    	this.setFathers(headers);
			this.setDragAndDrop(headers, horizontal, this);
	    	this.reloadTable();
    	}
    }   

    , addCalculatedField: function(level, horizontal, op, CFName){
    	var calculatedField = new Sbi.crosstab.core.CrossTabCalculatedField(CFName, level, horizontal, op); 
    	if(this.calculatedFields==null){
    		this.calculatedFields = new Array();
    	}
    	this.calculatedFields.push(calculatedField);
    }
    
    , removeCalculatedField: function(node){
    	for(var x =0; x<this.calculatedFields.length; x++){
    		if(this.calculatedFields[x].name == node.name){
    			this.calculatedFields.splice(x,1);
    			break;
    		}
    	}
    	this.removeEntries(node);
    }
    
    , modifyCalculatedField: function(level, horizontal, op, CFName){
    	if(this.calculatedFields!=null){
    		for(var i=0; i<this.calculatedFields.length; i++){
    			if(this.calculatedFields[i].name == CFName){
    				this.calculatedFields[i] = new Sbi.crosstab.core.CrossTabCalculatedField(CFName, level, horizontal, op);
    			}
    		}
    	}
    }

    , getCalculatedFields: function() {
    	return this.calculatedFields;
    }
    
    // Returns an array with the lower and upper bounds of a header:
    // The lower bound is the id(position of the leaf inside the header[header.length-1] array) of the firs leaf, the upper is the id of the last one
    , getHeaderBounds: function(header, horizontal, level){
    	var headers;
    	var bounds = new Array();
    	var dimension=0;
    	
    	if(header != null){
    		horizontal = header.horizontal;
    		level = header.level;
    	}
    	if(level == null){
    		level =0;
    	}
    	if(horizontal){
    		headers = this.columnHeader[level];
    	}else{
    		headers = this.rowHeader[level];
    	}
    	if(header == null){
    		header = headers[0];
    	}
    	
    	for(var i=0; i<headers.length; i++){
    		if(headers[i]==header){
    			bounds[0] = dimension;
    			bounds[1] = dimension+this.getLeafsNumber(headers[i])-1;
    			break; 
    		}else{
    			dimension = dimension+this.getLeafsNumber(headers[i]);
    		}
    	}
    	
    	return bounds;
    } 
    
    
    
    , getLeafsNumber: function(header){
    	if(header.childs==0){
    		return 1;
    	}else{
    		var leafs=0;
    		for(var i=0; i<header.childs.length; i++){
    			leafs = leafs+this.getLeafsNumber(header.childs[i]);
    		}
    		return leafs;
    	}
    }
    
    
    //remove a header and all its childs from the headers structure
    , removeHeader: function(header, updateFathers){
    	
    	if(header.horizontal){
    		headers = this.columnHeader;
    	}else{
    		headers = this.rowHeader;
    	}
    	var array;
    	var value;
    	var freshArray;
    	if(updateFathers){
	    	var father = header.father;
	    	var leafsToRemove= this.getLeafsNumber(header);
	    	var dimensionToRemove= header.thisDimension;
	    	
	    	while(father!=null){
	    		
	    		if((this.getLeafsNumber(father)-leafsToRemove)==0){
	    			this.removeHeader(father, true);//se il padre =0 allora lo rimuovo
	    			return;
	    		}
	    		father.thisDimension= father.thisDimension-dimensionToRemove;
	    		father.leafsNumber= father.leafsNumber-leafsToRemove;
	    		father=father.father;
	    	}  
	    	
	    	for(var i=0; i<header.father.childs.length; i++){
	    		if(this.isTheSameHeader(header.father.childs[i],header)){
	    			for(var j=i; j<header.father.childs.length-1; j++){
	    				header.father.childs[j]=header.father.childs[j+1];
	    			}
	    			header.father.childs.length = header.father.childs.length-1;
	    			i=header.father.childs.length;
	    		}
	    	} 	
    	}
    	
    	for(var i=0; i<headers[header.level].length; i++){
    		if(this.isTheSameHeader(headers[header.level][i],header)){
    			for(var j=i; j<headers[header.level].length-1; j++){
    				headers[header.level][j]=headers[header.level][j+1];
    			}
    			headers[header.level].length = headers[header.level].length-1;
    			i=headers[header.level].length;
    		}
    	}

    	if(header.horizontal){
    		this.columnHeader[header.level] = headers[header.level];
    	}else{
    		this.rowHeader[header.level] = headers[header.level];
    	}

    	
    	for(var i=0; i<header.childs.length; i++){
    		this.removeHeader(header.childs[i], false);
    	}
    }   

    //remove the header and all its entries
    , removeEntries: function(header, lazy){
		var bounds = this.getHeaderBounds(header);
		if(header.horizontal){
			this.entries.removeColumns(bounds[0],bounds[1]);
		}else{
			this.entries.removeRows(bounds[0],bounds[1]);
		}
		
		this.removeHeader(header, true);

		if(!lazy){
			this.reloadHeadersAndTable();
		}
    }
    
    , showCFWizard: function(node, modality) {
   		this.crossTabCFWizard = new Sbi.crosstab.core.CrossTabCFWizard({
   			'baseNode' : node, 
   			'modality' : modality
   		}); 
   		this.crossTabCFWizard.show(this);  
   		this.crossTabCFWizard.on('applyCalculatedField', function(theNode, level, horizontal, op, CFName){
   			
    		Sbi.crosstab.core.CrossTabCalculatedFields.calculateCF(level, horizontal, op, CFName, this, null, null, this.percenton);	
    		this.addCalculatedField(level, horizontal, op, CFName);
    		
   		}, this); 
   		this.crossTabCFWizard.on('modifyCalculatedField', function(theNode, level, horizontal, op, CFName){
   			
   			//remove the old CF
   			var headers;
   	    	if(horizontal){
   	    		headers = this.columnHeader[level];
   	    	}else{
   	    		headers = this.rowHeader[level];
   	    	}  			
   			for(var i=0; i<headers.length; i++){
   				if(headers[i].name == theNode.name){
   					this.removeEntries(headers[i], true);
   				}
   			}

   			Sbi.crosstab.core.CrossTabCalculatedFields.calculateCF(level, horizontal, op, CFName, this, null, null, this.percenton);
    		this.modifyCalculatedField(level, horizontal, op, CFName);

   		}, this); 
    }
    
    //===========================================
    //                    UTILITY
    //===========================================    
	, cloneNode: function(node,father, noHeaderMenu){
		
		if(node.horizontal){
			var clonedNode = new Sbi.crosstab.core.HeaderEntry({crosstab:this, percenton: this.percenton, name:node.name, thisDimension: node.thisDimension, horizontal: node.horizontal, level: node.level, width: null, height: node.height,columnWidth: this.columnWidth});
		}else{
			var clonedNode = new Sbi.crosstab.core.HeaderEntry({crosstab:this, percenton: this.percenton, name:node.name, thisDimension: node.thisDimension, horizontal: node.horizontal, level: node.level, width: node.width, height: null,columnWidth: this.columnWidth});
		}
		clonedNode.type = node.type;
		clonedNode.father = father;
		for(var i=0; i<node.childs.length;i++){
			clonedNode.childs.push(this.cloneNode(node.childs[i],clonedNode,noHeaderMenu));
		}
		this.setHeaderListener(clonedNode, noHeaderMenu);
		return clonedNode;
	}
	
	,cloneHeader: function(header){
		var clonedHeader = new Array();
		for(var i=0; i<header.length; i++){
			var line = new Array();
			for(var j=0; j<header[i].length; j++){
				var node = header[i][j];
				var clonedFather = null;
				if(node.father!=null && i>0){
					for(var jj=0; jj<header[i-1].length; jj++){
						if(this.isTheSameHeader(clonedHeader[i-1][jj],node.father)){
							clonedFather = clonedHeader[i-1][jj];
							break;
						}
					}
				}
				line.push(this.cloneNode(node, clonedFather, true));
			}
			clonedHeader.push(line);
		}
		return clonedHeader;
	}
	
	
    
    ,printHeader: function(header){
    	var printed = new Array();
    	var length;
    	if(header.horizontal){
    		length = this.columnHeader.length;
    	}else{
    		length = this.rowHeader.length;
    	}
    	
    	for(var i= header.level; i<length; i++){
    		printed.push(this.findLevelHeaders(header, i));
    	}
    	return printed;
    }
    
    ,findLevelHeaders: function(header, level){
    	var a = new Array();
    	if (header.level == level){
    		a.push(header.name);
    		return a;
    	}else if (header.childs.length==0){
    		return a;
    	}else{
    		for(var i=0; i<header.childs.length; i++){
    			a = a.concat(this.findLevelHeaders(header.childs[i],level));
    		}
    		return a;
    	}
    }
    
	, getMeasureScaleFactor: function (theMeasureName){
		var i=0;
		var scaleFactor={value:1, text:''};
		var optionDefinition = null;
		for (; i < this.fieldsOptions.length; i++) {
			if (this.fieldsOptions[i].alias === theMeasureName) {
				optionDefinition = this.fieldsOptions[i];
				break;
			}
		}
		if(optionDefinition!=null){
			legendSuffix = optionDefinition.options.measureScaleFactor;
			if(legendSuffix != undefined && legendSuffix != null && legendSuffix!='NONE'){
				scaleFactor.text = LN('sbi.worksheet.config.options.measurepresentation.'+legendSuffix);
				switch (legendSuffix)
				{
				case 'K':
					scaleFactor.value=1000;
					break;
				case 'M':
					scaleFactor.value=1000000;
					break;
				case 'G':
					scaleFactor.value=1000000000;
					break;
				default:
					scaleFactor.value=1;
				}
			}
		}
		return scaleFactor;
	}
    
});